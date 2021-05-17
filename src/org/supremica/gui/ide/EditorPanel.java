//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 1999-2021 Knut Akesson, Martin Fabian, Robi Malik
//###########################################################################
//# This file is part of Waters/Supremica IDE.
//# Waters/Supremica IDE is free software: you can redistribute it and/or
//# modify it under the terms of the GNU General Public License as published
//# by the Free Software Foundation, either version 2 of the License, or
//# (at your option) any later version.
//# Waters/Supremica IDE is distributed in the hope that it will be useful,
//# but WITHOUT ANY WARRANTY; without even the implied warranty of
//# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
//# Public License for more details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters/Supremica IDE statically or dynamically with other modules
//# is making a combined work based on Waters/Supremica IDE. Thus, the terms
//# and conditions of the GNU General Public License cover the whole
//# combination.
//# In addition, as a special exception, the copyright holders of
//# Waters/Supremica IDE give you permission to combine Waters/Supremica IDE
//# with code included in the standard release of Supremica under the
//# Supremica Software License Agreement (or modified versions of such code,
//# with unchanged license). You may copy and distribute such a system
//# following the terms of the GNU GPL for Waters/Supremica IDE and the
//# licenses of the other code concerned.
//# Note that people who make modified versions of Waters/Supremica IDE are
//# not obligated to grant this special exception for their modified versions;
//# it is their choice whether to do so. The GNU General Public License gives
//# permission to release a modified version without this exception; this
//# exception also makes it possible to release a modified version which
//# carries forward this exception.
//###########################################################################

package org.supremica.gui.ide;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.sourceforge.waters.model.options.OptionChangeEvent;
import net.sourceforge.waters.model.options.OptionChangeListener;
import net.sourceforge.waters.gui.AliasesPanel;
import net.sourceforge.waters.gui.ComponentsTree;
import net.sourceforge.waters.gui.EventDeclListView;
import net.sourceforge.waters.gui.GraphEditorPanel;
import net.sourceforge.waters.gui.ModuleContext;
import net.sourceforge.waters.gui.ModuleWindowInterface;
import net.sourceforge.waters.gui.actions.WatersPopupActionManager;
import net.sourceforge.waters.gui.command.UndoInterface;
import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.gui.observer.Observer;
import net.sourceforge.waters.gui.observer.SubPanelSwitchEvent;
import net.sourceforge.waters.gui.observer.Subject;
import net.sourceforge.waters.gui.renderer.GeometryAbsentException;
import net.sourceforge.waters.gui.transfer.FocusTracker;
import net.sourceforge.waters.gui.transfer.SelectionOwner;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.compiler.CompilerOptions;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;


/**
 * The panel to edit modules.
 * It consists of a split pane, with events or components lists in
 * tabs on the left side, and the graph editor panel on the right side.
 *
 * @author Knut &Aring;kesson, Robi Malik
 */

public class EditorPanel
  extends MainPanel
  implements ModuleWindowInterface, ChangeListener,
             Subject, OptionChangeListener
{

  //#########################################################################
  //# Constructor
  public EditorPanel(final ModuleContainer moduleContainer,
                     final String name)
  {
    super(name);
    mModuleContainer = moduleContainer;
    mTabMap = new HashMap<SelectionOwner,Tab>();

    mTabbedPane = new JTabbedPane();
    mTabbedPane.addChangeListener(this);
    setLeftComponent(mTabbedPane);

    final IDE ide = mModuleContainer.getIDE();
    final WatersPopupActionManager manager = ide.getPopupActionManager();

    final AliasesPanel aliasesPanel =
      new AliasesPanel(mModuleContainer, manager);
    mAliasesTab = new Tab("Definitions", aliasesPanel);
    if (CompilerOptions.INCLUDE_INSTANTIATION.getValue()) {
      mAliasesTab.addToTabbedPane();
    }
    mTabMap.put(aliasesPanel.getConstantAliasesPanel(), mAliasesTab);
    mTabMap.put(aliasesPanel.getEventAliasesPanel(), mAliasesTab);
    final EventDeclListView eventsPanel =
      new EventDeclListView(this, manager);
    mEventsTab = new Tab("Events", eventsPanel);
    mEventsTab.addToTabbedPane();
    mTabMap.put(eventsPanel, mEventsTab);
    final ComponentsTree compPanel = new ComponentsTree(mModuleContainer, manager);
    mComponentsTab = new Tab("Components", compPanel);
    mComponentsTab.addToTabbedPane();
    mComponentsTab.activate();
    mTabMap.put(compPanel, mComponentsTab);
    estimateMinimumWidth(mTabbedPane, 1.2f);
    mLastFocusOwner = compPanel;

    mCommentPanel = new CommentPanel(moduleContainer);
    setRightComponent(mCommentPanel);
    CompilerOptions.INCLUDE_INSTANTIATION.addOptionChangeListener(this);
  }


  //#########################################################################
  //# Menu Setup
  @Override
  public void createPanelSpecificMenus(final IDEMenuBar menuBar)
  {
    menuBar.createCreateMenu();
    menuBar.createVerifyMenu();
    menuBar.createEditorAnalyzeMenu();
    menuBar.createEditorToolsMenu();
  }


  //#########################################################################
  //# Focus Switching
  @Override
  protected void activate()
  {
    FocusTracker.requestFocusFor(mLastFocusOwner);
  }

  @Override
  protected void deactivate()
  {
    final IDE ide = mModuleContainer.getIDE();
    final FocusTracker tracker = ide.getFocusTracker();
    final Object focusOwner = tracker.getLastWatersSelectionOwner();
    if (focusOwner instanceof Component) {
      mLastFocusOwner = (Component) focusOwner;
    }
  }

  public void close()
  {
    for (final SelectionOwner panel : mTabMap.keySet()) {
      panel.close();
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.ModuleWindowInterface
  @Override
  public UndoInterface getUndoInterface()
  {
    return mModuleContainer;
  }

  @Override
  public ModuleSubject getModuleSubject()
  {
    return mModuleContainer.getModule();
  }

  @Override
  public ModuleContext getModuleContext()
  {
    return mModuleContainer.getModuleContext();
  }

  @Override
  public ExpressionParser getExpressionParser()
  {
    return mModuleContainer.getExpressionParser();
  }

  @Override
  public IDE getRootWindow()
  {
    return (IDE) getTopLevelAncestor();
  }

  @Override
  public SelectionOwner getComponentsPanel()
  {
    return (SelectionOwner) mComponentsTab.getPanel();
  }

  @Override
  public SelectionOwner getEventsPanel()
  {
    return (SelectionOwner) mEventsTab.getPanel();
  }

  @Override
  public SelectionOwner getConstantAliasesPanel()
  {
    final AliasesPanel panel = (AliasesPanel) mAliasesTab.getPanel();
    return panel.getConstantAliasesPanel();
  }

  @Override
  public SelectionOwner getEventAliasesPanel()
  {
    final AliasesPanel panel = (AliasesPanel) mAliasesTab.getPanel();
    return panel.getEventAliasesPanel();
  }

  @Override
  public SelectionOwner getInstancePanel()
  {
    return (SelectionOwner) mComponentsTab.getPanel();
  }

  @Override
  public void showComponents()
  {
    mComponentsTab.activate();
  }

  @Override
  public void showEvents()
  {
    mEventsTab.activate();
  }

  @Override
  public ComponentEditorPanel showEditor(final SimpleComponentSubject comp)
    throws GeometryAbsentException
  {
    return setRightComponent(comp);
  }

  @Override
  public ComponentEditorPanel getComponentEditorPanel
    (final SimpleComponentSubject comp)
  {
    return mModuleContainer.getComponentEditorPanel(comp);
  }

  @Override
  public ComponentEditorPanel getActiveComponentEditorPanel()
  {
    if (getRightComponent() instanceof ComponentEditorPanel) {
      return (ComponentEditorPanel) getRightComponent();
    } else {
      return null;
    }
  }

  @Override
  public void showComment()
  {
    setRightComponent(mCommentPanel);
  }

  @Override
  public void showPanel(final SelectionOwner panel)
  {
    final Tab tab = mTabMap.get(panel);
    if (tab != null) {
      tab.activate();
    }
  }


  //#########################################################################
  //# Interface javax.swing.event.ChangeListener
  @Override
  public void stateChanged(final ChangeEvent event)
  {
    // Why is the focus not transfered automatically when clicking tabs?
    final JScrollPane scroll =
      (JScrollPane) mTabbedPane.getSelectedComponent();
    final Component panel = scroll.getViewport().getView();
    panel.requestFocusInWindow();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.observer.Subject
  @Override
  public void attach(final Observer o)
  {
    mObservers.add(o);
  }

  @Override
  public void detach(final Observer o)
  {
    mObservers.remove(o);
  }

  @Override
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


  @Override
  public void optionChanged(final OptionChangeEvent event)
  {
    if (CompilerOptions.INCLUDE_INSTANTIATION.getValue()) {
      mAliasesTab.addToTabbedPane(0);
    } else {
      mAliasesTab.removeFromTabbedPane();
    }
  }


  //#########################################################################
  //# Overrides for org.supremica.gui.ide.MainPanel
  @Override
  public ComponentEditorPanel setRightComponent(final Proxy proxy)
    throws GeometryAbsentException
  {
    final SimpleComponentSubject comp = (SimpleComponentSubject) proxy;
    final ComponentEditorPanel panel =
      mModuleContainer.createComponentEditorPanel(comp);
    setRightComponent(panel);
    final GraphEditorPanel surface = panel.getGraphEditorPanel();
    FocusTracker.requestFocusFor(surface);
    return panel;
  }

  @Override
  protected boolean setRightComponent(final JComponent newComponent)
  {
    if (super.setRightComponent(newComponent)) {
      final EditorChangedEvent event = new SubPanelSwitchEvent(this);
      fireEditorChangedEvent(event);
      return true;
    } else {
      return false;
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private void estimateMinimumWidth(final JTabbedPane tabbedPane,
                                      final float scale)
  {
    final StringBuilder builder = new StringBuilder("  ");
    for (int i = 0; i < tabbedPane.getTabCount(); i++) {
      final Component tab = tabbedPane.getComponentAt(i);
      builder.append(tab.getName());
      builder.append("  ");
    }
    final BufferedImage img =
      new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
    final Graphics2D g2d = img.createGraphics();
    final FontMetrics fm = g2d.getFontMetrics(tabbedPane.getFont());
    final int width = fm.stringWidth(builder.toString());
    g2d.dispose();
    final Dimension size = new Dimension((int) Math.ceil(width * scale), 0);
    tabbedPane.setMinimumSize(size);
  }


  //#########################################################################
  //# Inner Class Tab
  private class Tab
  {
    //#######################################################################
    //# Constructor
    private Tab(final String name, final JComponent panel)
    {
      mPanel = panel;
      mScrollPane = new JScrollPane(panel);
      mScrollPane.setName(name);
    }

    //#######################################################################
    //# Simple Access
    private JComponent getPanel()
    {
      return mPanel;
    }

    private void activate()
    {
      mTabbedPane.setSelectedComponent(mScrollPane);
      FocusTracker.requestFocusFor(mPanel);
    }

    private void addToTabbedPane()
    {
      mTabbedPane.add(mScrollPane);
    }

    private void addToTabbedPane(final int index)
    {
      mTabbedPane.add(mScrollPane, index);
    }

    private void removeFromTabbedPane()
    {
      mTabbedPane.remove(mScrollPane);
    }

    //#######################################################################
    //# Data Members
    private final JComponent mPanel;
    private final JScrollPane mScrollPane;
  }


  //#########################################################################
  //# Data Members
  private final ModuleContainer mModuleContainer;
  private final Map<SelectionOwner,Tab> mTabMap;

  private final JTabbedPane mTabbedPane;
  private final Tab mComponentsTab;
  private final Tab mEventsTab;
  private final Tab mAliasesTab;
  private final CommentPanel mCommentPanel;

  private final Collection<Observer> mObservers = new LinkedList<>();

  private Component mLastFocusOwner = null;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 7673179485024676170L;

}
