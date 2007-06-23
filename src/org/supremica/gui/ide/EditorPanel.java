//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: org.supremica.gui.ide
//# CLASS:   EditorPanel
//###########################################################################
//# $Id: EditorPanel.java,v 1.58 2007-06-23 10:58:09 robi Exp $
//###########################################################################


package org.supremica.gui.ide;

import java.awt.Frame;
import java.awt.GridBagLayout;
import javax.swing.*;
import java.awt.event.ActionEvent;

import net.sourceforge.waters.gui.ControlledToolbar;
import net.sourceforge.waters.gui.EditorWindowInterface;
import net.sourceforge.waters.gui.ModuleWindowInterface;
import net.sourceforge.waters.gui.command.UndoInterface;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.subject.base.AbstractSubject;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;
import net.sourceforge.waters.xsd.base.EventKind;
import org.supremica.gui.ide.actions.Actions;


public class EditorPanel
    extends MainPanel
    implements ModuleWindowInterface
{
    private static final long serialVersionUID = 1L;
    
    private JTabbedPane tabPanel;
    private JComponent componentEditorPanel;
    
    private EditorEventsPanel eventsPanel;
    private EditorAliasesPanel aliasesPanel;
    private EditorComponentsPanel componentsPanel;
    private ButtonGroup editorButtonGroup;
    
    private final ModuleContainer mModuleContainer;
    
    public EditorPanel(ModuleContainer moduleContainer, String name)
    {
        super(name);
        mModuleContainer = moduleContainer;
        tabPanel = new JTabbedPane(JTabbedPane.BOTTOM);
        tabPanel.setPreferredSize(IDEDimensions.leftEditorPreferredSize);
        tabPanel.setMinimumSize(IDEDimensions.leftEditorMinimumSize);
        
        /*
                aliasesPanel = new EditorAliasesPanel(moduleContainer, "Aliases");
                aliasesPanel.setPreferredSize(IDEDimensions.leftEditorPreferredSize);
                aliasesPanel.setMinimumSize(IDEDimensions.leftEditorMinimumSize);
                tabPanel.add(aliasesPanel);
         */
        
        componentsPanel = new EditorComponentsPanel(moduleContainer, this, "Components");
        componentsPanel.setPreferredSize(IDEDimensions.leftEditorPreferredSize);
        componentsPanel.setMinimumSize(IDEDimensions.leftEditorMinimumSize);
        tabPanel.add(componentsPanel);
        
        eventsPanel = new EditorEventsPanel(this, "Events");
        eventsPanel.setPreferredSize(IDEDimensions.leftEditorPreferredSize);
        eventsPanel.setMinimumSize(IDEDimensions.leftEditorMinimumSize);
        tabPanel.add(eventsPanel);
        tabPanel.setSelectedComponent(componentsPanel);
        
        componentEditorPanel = getEmptyRightPanel();
        
        splitPanelHorizontal = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tabPanel, componentEditorPanel);
        splitPanelHorizontal.setContinuousLayout(false);
        splitPanelHorizontal.setOneTouchExpandable(false);
        splitPanelHorizontal.setDividerLocation(0.2);
        splitPanelHorizontal.setResizeWeight(0.0);
        
        ((GridBagLayout)getLayout()).setConstraints(splitPanelHorizontal, getGridBagConstraints());
        add(splitPanelHorizontal);
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
    
    public ExpressionParser getExpressionParser()
    {
        return mModuleContainer.getExpressionParser();
    }
    
    public EventKind guessEventKind(final IdentifierProxy ident)
    {
        return mModuleContainer.guessEventKind(ident);
    }
    
    public Frame getRootWindow()
    {
        return (Frame) getTopLevelAncestor();
    }
    
    public EditorWindowInterface showEditor(SimpleComponentSubject component)
    {
        final EditorPanel editorPanel = mModuleContainer.getEditorPanel();
        if (component != null)
        {
            editorPanel.setRightComponent(mModuleContainer.getComponentEditorPanel(component));
        }
        return editorPanel.getActiveEditorWindowInterface();
    }

    private CommentPanel commentPanel = null;
    
    /**
     * Displays a comment about the module.
     */   
    public void showComment()
    {
        if (commentPanel == null)
        {
            commentPanel = new CommentPanel(getModuleSubject());
        }
        
        final EditorPanel editorPanel = mModuleContainer.getEditorPanel();
        editorPanel.setRightComponent(commentPanel);
    }

    //######################################################################
    //#
    public void addToolBarEntries(IDEToolBar toolBar)
    {
        editorButtonGroup = new ButtonGroup();
        toolBar.addSeparator();
        (toolBar.add(getActions().editorSelectAction, editorButtonGroup)).setSelected(true);
        toolBar.setCommand(ControlledToolbar.Tool.SELECT.toString());
        toolBar.add(getActions().editorAddNodeAction, editorButtonGroup);
        toolBar.add(getActions().editorAddNodeGroupAction, editorButtonGroup);
        toolBar.add(getActions().editorAddEdgeAction, editorButtonGroup);
//		toolBar.add(getActions().editorAddEventAction, editorButtonGroup);
    }
    
    public EditorWindowInterface getActiveEditorWindowInterface()
    {
        if (getRightComponent() instanceof EditorWindowInterface)
        {
            return (EditorWindowInterface)getRightComponent();
        }
        return null;
        //	    return getEditorWindowInterface();
    }
    
    public void setRightComponent(JComponent newComponent)
    {
        super.setRightComponent(newComponent);
        
        // Update enablement of actions dependent on the right component (component editor panel)
        if (newComponent instanceof ComponentEditorPanel)
        {
            getActions().editorSavePostscriptAction.setEnabled(true);
            getActions().editorSaveEncapsulatedPostscriptAction.setEnabled(true);
            getActions().editorSavePDFAction.setEnabled(true);
            getActions().editorPrintAction.setEnabled(true);
            getActions().editorRunEmbedderAction.setEnabled(true);
        }
        else
        {
            getActions().editorSavePostscriptAction.setEnabled(false);
            getActions().editorSaveEncapsulatedPostscriptAction.setEnabled(false);
            getActions().editorSavePDFAction.setEnabled(false);
            getActions().editorPrintAction.setEnabled(false);
            getActions().editorRunEmbedderAction.setEnabled(false);
        }
    }
    
    public EditorPanelInterface getEditorPanelInterface()
    {
        return new EditorPanelInterfaceImpl();
    }
    
    class EditorPanelInterfaceImpl
        implements EditorPanelInterface
    {
        public void addComponent()
        {
            componentsPanel.addComponent();
        }
        
        public void addComponent(AbstractSubject component)
        {
            componentsPanel.addComponent(component);
        }
        
        public void addModuleEvent()
        {
            eventsPanel.addModuleEvent();
        }
        
        public void addComponentEvent()
        {
            eventsPanel.addComponentEvent();
        }
        
        public ModuleSubject getModuleSubject()
        {
            return componentsPanel.getModuleSubject();
        }
        
        public boolean componentNameAvailable(String name)
        {
            return componentsPanel.componentNameAvailable(name);
        }
    }
    
    public Actions getActions()
    {
        return mModuleContainer.getIDE().getActions();
    }
}
