//# -*- tab-width: 4  indent-tabs-mode: nil  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: org.supremica.gui.ide
//# CLASS:   EditorPanel
//###########################################################################
//# $Id$
//###########################################################################


package org.supremica.gui.ide;

import java.awt.Component;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.sourceforge.waters.gui.AliasesPanel;
import net.sourceforge.waters.gui.ComponentsTree;
import net.sourceforge.waters.gui.EditorWindowInterface;
import net.sourceforge.waters.gui.EventDeclListView;
import net.sourceforge.waters.gui.ModuleContext;
import net.sourceforge.waters.gui.ModuleWindowInterface;
import net.sourceforge.waters.gui.actions.WatersPopupActionManager;
import net.sourceforge.waters.gui.command.UndoInterface;
import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.gui.observer.Observer;
import net.sourceforge.waters.gui.observer.SubPanelSwitchEvent;
import net.sourceforge.waters.gui.observer.Subject;
import net.sourceforge.waters.gui.renderer.GeometryAbsentException;
import net.sourceforge.waters.gui.transfer.SelectionOwner;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;

import org.supremica.gui.ide.actions.Actions;
import org.supremica.properties.Config;
import org.supremica.properties.SupremicaPropertyChangeEvent;
import org.supremica.properties.SupremicaPropertyChangeListener;


/**
 * The panel to edit modules.
 * It consists of a split pane, with events or components lists in
 * tabs on the left side, and the graph editor panel on the right side.
 *
 * @author Knut &Aring;kesson
 */

public class EditorPanel
    extends MainPanel
    implements ModuleWindowInterface, ChangeListener, Subject, SupremicaPropertyChangeListener
{

    //#######################################################################
    //# Constructor
    public EditorPanel(final ModuleContainer moduleContainer,
                       final String name)
    {
        super(name);
        mModuleContainer = moduleContainer;
        mTabMap = new HashMap<SelectionOwner,Tab>();

        mTabbedPane = new JTabbedPane(JTabbedPane.BOTTOM);
        mTabbedPane.setPreferredSize(IDEDimensions.leftEditorPreferredSize);
        mTabbedPane.setMinimumSize(IDEDimensions.leftEditorMinimumSize);
        mTabbedPane.addChangeListener(this);
        setLeftComponent(mTabbedPane);

		final IDE ide = mModuleContainer.getIDE();
        final WatersPopupActionManager manager = ide.getPopupActionManager();

        final AliasesPanel aliasesPanel =
          new AliasesPanel(this, manager);
        mAliasesTab = new Tab("Definitions", aliasesPanel);
        if (Config.INCLUDE_INSTANTION.get()) {
          mAliasesTab.addToTabbedPane();
        }
        mTabMap.put(aliasesPanel.getConstantAliasesPanel(), mAliasesTab);
        mTabMap.put(aliasesPanel.getEventAliasesPanel(), mAliasesTab);
        final EventDeclListView eventsPanel =
            new EventDeclListView(this, manager);
        mEventsTab = new Tab("Events", eventsPanel);
        mEventsTab.addToTabbedPane();
        mTabMap.put(eventsPanel, mEventsTab);
        final ComponentsTree compPanel = new ComponentsTree(this, manager);
        mComponentsTab = new Tab("Components", compPanel);
        mComponentsTab.addToTabbedPane();
        mComponentsTab.activate();
        mTabMap.put(compPanel, mComponentsTab);

        mCommentPanel = new CommentPanel(moduleContainer);
        setRightComponent(mCommentPanel);
        Config.INCLUDE_INSTANTION.addPropertyChangeListener(this);
    }


    //######################################################################
    //# Clean Up
    public void close()
    {
        for (final SelectionOwner panel : mTabMap.keySet()) {
            panel.close();
        }
    }


    //######################################################################
    //# Interface net.sourceforge.waters.gui.ModuleWindowInterface
    public UndoInterface getUndoInterface()
    {
        return mModuleContainer;
    }

    public ModuleSubject getModuleSubject()
    {
        return mModuleContainer.getModule();
    }

    public ModuleContext getModuleContext()
    {
        return mModuleContainer.getModuleContext();
    }

    public ExpressionParser getExpressionParser()
    {
        return mModuleContainer.getExpressionParser();
    }

    public IDE getRootWindow()
    {
        return (IDE) getTopLevelAncestor();
    }

    public SelectionOwner getComponentsPanel()
    {
        return (SelectionOwner) mComponentsTab.getPanel();
    }

    public SelectionOwner getEventsPanel()
    {
        return (SelectionOwner) mEventsTab.getPanel();
    }

    public SelectionOwner getConstantAliasesPanel()
    {
      final AliasesPanel panel = (AliasesPanel) mAliasesTab.getPanel();
      return panel.getConstantAliasesPanel();
    }

    public SelectionOwner getEventAliasesPanel()
    {
      final AliasesPanel panel = (AliasesPanel) mAliasesTab.getPanel();
      return panel.getEventAliasesPanel();
    }

    public void showComponents()
    {
        mComponentsTab.activate();
    }

    public void showEvents()
    {
        mEventsTab.activate();
    }

    public EditorWindowInterface showEditor(final SimpleComponentSubject comp)
    throws GeometryAbsentException
    {
      final ComponentEditorPanel panel =
        mModuleContainer.createComponentEditorPanel(comp);
      setRightComponent(panel);
      return panel;
    }

    public EditorWindowInterface getEditorWindowInterface
        (final SimpleComponentSubject comp)
    {
        return mModuleContainer.getComponentEditorPanel(comp);
    }

    public ComponentEditorPanel getActiveEditorWindowInterface()
    {
        if (getRightComponent() instanceof EditorWindowInterface) {
            return (ComponentEditorPanel) getRightComponent();
        } else {
            return null;
        }
    }

    public void showComment()
    {
        setRightComponent(mCommentPanel);
    }

    public void showPanel(final SelectionOwner panel)
    {
        final Tab tab = mTabMap.get(panel);
        if (tab != null) {
            tab.activate();
        }
    }


    //#######################################################################
    //# Interface javax.swing.event.ChangeListener
    public void stateChanged(final ChangeEvent event)
    {
        // Why is the focus not transfered automatically when clicking tabs?
        final JScrollPane scroll =
            (JScrollPane) mTabbedPane.getSelectedComponent();
        final Component panel = scroll.getViewport().getView();
        panel.requestFocusInWindow();
    }


    //#######################################################################
    //# Interface net.sourceforge.waters.gui.observer.Subject
    public void attach(final Observer o)
    {
        mObservers.add(o);
    }

    public void detach(final Observer o)
    {
        mObservers.remove(o);
    }

    public void fireEditorChangedEvent(final EditorChangedEvent event)
    {
        // Just in case they try to register or deregister observers
        // in response to the update ...
        final Collection<Observer> copy = new LinkedList<Observer>(mObservers);
        for (final Observer observer : copy) {
            observer.update(event);
        }
        mModuleContainer.fireEditorChangedEvent(event);
    }


    public void propertyChanged(final SupremicaPropertyChangeEvent event)
    {
      if (Config.INCLUDE_INSTANTION.get()) {
        mAliasesTab.addToTabbedPane(0);
      }
      else{
        mAliasesTab.removeFromTabbedPane();
      }
    }

    //######################################################################
    //#
    protected boolean setRightComponent(final JComponent newComponent)
    {
        if (super.setRightComponent(newComponent)) {
            final EditorChangedEvent event = new SubPanelSwitchEvent(this);
            fireEditorChangedEvent(event);

            // Update enablement of actions dependent on the right component
            // (component editor panel) --- to be deprecated ...
            if (newComponent instanceof ComponentEditorPanel) {
                getActions().editorSavePostscriptAction.setEnabled(true);
                getActions().editorSavePDFAction.setEnabled(true);
                getActions().editorPrintAction.setEnabled(true);
            } else {
                getActions().editorSavePostscriptAction.setEnabled(false);
                getActions().editorSavePDFAction.setEnabled(false);
                getActions().editorPrintAction.setEnabled(false);
            }
            return true;
        } else {
            return false;
        }
    }


    //#######################################################################
    //# Auxiliary Methods
    private Actions getActions()
    {
        return mModuleContainer.getIDE().getActions();
    }


    //#######################################################################
    //# Inner Class Tab
    private class Tab
    {

        //###################################################################
        //# Constructor
        private Tab(final String name, final JComponent panel)
        {
            mPanel = panel;
            mScrollPane = new JScrollPane(panel);
            mScrollPane.setName(name);
            mScrollPane.setPreferredSize
                (IDEDimensions.leftEditorPreferredSize);
            mScrollPane.setMinimumSize(IDEDimensions.leftEditorMinimumSize);
        }

        //###################################################################
        //# Simple Access
        private JComponent getPanel()
        {
            return mPanel;
        }

        private void activate()
        {
            mTabbedPane.setSelectedComponent(mScrollPane);
            mPanel.requestFocusInWindow();
        }

        private void addToTabbedPane(){
          mTabbedPane.add(mScrollPane);
        }

        private void addToTabbedPane(final int index){
          mTabbedPane.add(mScrollPane, index);
        }

        private void removeFromTabbedPane(){
          mTabbedPane.remove(mScrollPane);
        }

        //###################################################################
        //# Data Members
        private final JComponent mPanel;
        private final JScrollPane mScrollPane;

    }


    //#######################################################################
    //# Data Members
    private final ModuleContainer mModuleContainer;
    private final Map<SelectionOwner,Tab> mTabMap;

    private final JTabbedPane mTabbedPane;
    private final Tab mComponentsTab;
    private final Tab mEventsTab;
    private final Tab mAliasesTab;
    private final CommentPanel mCommentPanel;

    private final Collection<Observer> mObservers = new LinkedList<Observer>();


    //#########################################################################
    //# Class Constants
    private static final long serialVersionUID = 1L;


}
