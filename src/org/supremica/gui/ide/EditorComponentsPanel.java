//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: org.supremica.gui.ide
//# CLASS:   EditorComponentsPanel
//###########################################################################
//# $Id: EditorComponentsPanel.java,v 1.37 2007-06-20 19:43:38 flordal Exp $
//###########################################################################


package org.supremica.gui.ide;

import java.awt.event.ActionEvent;
import javax.swing.tree.*;

import java.util.*;

import net.sourceforge.waters.gui.ComponentInfo;
import net.sourceforge.waters.gui.EditorEditVariableDialog;
import net.sourceforge.waters.gui.ModuleTree;
import net.sourceforge.waters.gui.ModuleWindowInterface;
import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.subject.base.AbstractSubject;
import net.sourceforge.waters.subject.base.ListSubject;
import net.sourceforge.waters.subject.base.Subject;
import net.sourceforge.waters.subject.module.ForeachSubject;
import net.sourceforge.waters.subject.module.IdentifierSubject;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;
import net.sourceforge.waters.subject.module.VariableSubject;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.NamedProxy;
import net.sourceforge.waters.model.module.ForeachComponentProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.module.SimpleComponent;



import org.supremica.gui.WhiteScrollPane;

/**
 * This is the panel to the left in the IDE in "Editor mode". Contains
 * a ModuleTree representation of the currently active ModuleSubject.
 */
class EditorComponentsPanel
    extends WhiteScrollPane
    implements EditorPanelInterface
{
    private static final long serialVersionUID = 1L;
    
    private String name;
    private ModuleContainer moduleContainer;
    
    private ModuleTree moduleSelectTree;
    private boolean modified = true;
    
    private ModuleWindowInterface root;
    
    EditorComponentsPanel(final ModuleContainer moduleContainer,
        final ModuleWindowInterface root,
        final String name)
    {
        this.moduleContainer = moduleContainer;
        this.name = name;
        this.root = root;
        createContentPane(root);
        setPreferredSize(IDEDimensions.leftEditorPreferredSize);
        setMinimumSize(IDEDimensions.leftEditorMinimumSize);
    }
    
    public String getName()
    {
        return name;
    }
    
    private void createContentPane(final ModuleWindowInterface root)
    {
        moduleSelectTree = new ModuleTree(root);
        getViewport().add(moduleSelectTree);
    }
    
    
    //#######################################################################
    //# org.supremica.gui.ide.EditorPanelInterface
    public void addComponent()
    {
        new EditorNewComponentDialog(this);
    }
    
    public void renameComponent(SimpleComponentSubject component)
    {
        new EditorRenameComponentDialog(this, component);
        moduleSelectTree.updateSelectedNode();
    }
    
    // To do remove this - this is a dummy impl used to satisfy EditorPanelInterface
    public void addComponentEvent()
    {
    }
    
    // To do remove this - this is a dummy impl used to satisfy EditorPanelInterface
    public void addModuleEvent()
    {
    }
    
    /**
     * Adds a subject to the active ModuleSubject and also graphically to the ModuleTree.
     */
    public void addComponent(final AbstractSubject o)
    {
        final ModuleSubject module = getModuleSubject();
        if (module != null)
        {
            modified = true;
            
            DefaultMutableTreeNode parentNode = null;
            TreePath parentPath = moduleSelectTree.getSelectionPath();
            
            if (parentPath == null)
            {
                //There's no selection. Default to the root node.
                parentNode = ((ModuleTree) moduleSelectTree).getRoot();
                
                moduleSelectTree.expandPath(new TreePath(parentNode.getPath()));
            }
            else
            {
                parentNode = (DefaultMutableTreeNode) (parentPath.getLastPathComponent());
            }
            
            ComponentInfo ci = (ComponentInfo) (parentNode.getUserObject());
            
            //logEntry("addComponent: Parent: " + parentNode.toString());
            
            if (ci.getComponent() instanceof ForeachSubject)
            {
                ((ForeachSubject) ci.getComponent()).getBodyModifiable().add(o);
            }
            else
            {
                module.getComponentListModifiable().add(o);
            }
            
            
            //Add node to module tree
            ((ModuleTree) moduleSelectTree).addComponent(o);
        }
    }
    
    public ModuleSubject getModuleSubject()
    {
        return moduleContainer.getModule();
    }
    
    public boolean componentNameAvailable(String name)
    {
        if (name == null || name.equals(""))
        {
            return false;
        }
        
        ModuleSubject subject = getModuleSubject();
        List<Proxy> componentList = subject.getComponentList();
        for(Proxy proxy : componentList)
        {
            if (!(proxy instanceof ForeachComponentProxy))
            {
                NamedProxy namedProxy = (NamedProxy)proxy;
                String currName = namedProxy.getName();
                if (name.equals(currName))
                {
                    return false;
                }
            }
        }
        return true;
    }
    
    
    //######################################################################
    //# Interface java.awt.event.ActionListener
    public void actionPerformed(ActionEvent e)
    {
        if("add variable".equals(e.getActionCommand()))
        {
            //add new variable
            TreePath currentSelection = moduleSelectTree.getSelectionPath();
            if (currentSelection != null)
            {
                // Get the node in the tree
                DefaultMutableTreeNode targetNode = (DefaultMutableTreeNode)
                (currentSelection.getLastPathComponent());
                Subject component = ((ComponentInfo)
                targetNode.getUserObject()).getComponent();
                
                if(component instanceof VariableSubject)
                {
                    component = component.getParent().getParent();
                    EditorEditVariableDialog.showDialog(null,
                        (SimpleComponentSubject) component, moduleSelectTree);
                }
                else if(component instanceof SimpleComponentSubject)
                {
                    EditorEditVariableDialog.showDialog(null,
                        (SimpleComponentSubject) component, moduleSelectTree);
                }
                else
                {
                    moduleContainer.getIDE().error("ModuleWindow.actionPerformed(): " +
                        "'add variable' performed by illegal node type");
                }
            }
        }
        
        if("edit variable".equals(e.getActionCommand()))
        {
            //edit existing variable
            TreePath currentSelection = moduleSelectTree.getSelectionPath();
            if (currentSelection != null)
            {
                // Get the node in the tree
                DefaultMutableTreeNode targetNode = (DefaultMutableTreeNode)
                (currentSelection.getLastPathComponent());
                Subject component = ((ComponentInfo)
                targetNode.getUserObject()).getComponent();
                
                if(component instanceof VariableSubject)
                {
                    Subject parent = component.getParent().getParent();
                    EditorEditVariableDialog.showDialog((VariableSubject) component,
                        (SimpleComponentSubject) parent, moduleSelectTree);
                }
                else
                {
                    moduleContainer.getIDE().error("ModuleWindow.actionPerformed(): " +
                        "'edit variable' performed by illegal node type");
                }
            }
        }
        
        if("delete variable".equals(e.getActionCommand()))
        {
            TreePath currentSelection = moduleSelectTree.getSelectionPath();
            if (currentSelection != null)
            {
                // Get the node in the tree
                DefaultMutableTreeNode targetNode = (DefaultMutableTreeNode) (currentSelection.getLastPathComponent());
                
                //Special treatment for variables
                VariableSubject variable = (VariableSubject) ((ComponentInfo)
                targetNode.getUserObject()).getComponent();
                
                //remove from model (take getParent()x2 because a variableSubject is the child of a list.)
                ((SimpleComponentSubject) variable.getParent().getParent())
                .getVariablesModifiable().remove(variable);
                
                //remove from module tree view
                moduleSelectTree.removeCurrentNode();
                return;
            }
            
            else
            {
                moduleContainer.getIDE().error("ModuleWindow.actionPerformed(): " +
                    "'delete variable' performed by illegal node type");
            }
        }
        
        if("delete component".equals(e.getActionCommand()))
        {
            TreePath currentSelection = moduleSelectTree.getSelectionPath();
            if (currentSelection != null)
            {
                // Find the depth of the component...
                int depth = currentSelection.getPathCount()-1;
                // Get the node in the tree
                DefaultMutableTreeNode targetNode = (DefaultMutableTreeNode) (currentSelection.getLastPathComponent());
                AbstractSubject component = ((ComponentInfo) targetNode.getUserObject()).getComponent();
                
                //Special treatment for variables
                if(component instanceof VariableSubject)
                {
                    //remove from model (take getParent()x2 because a variableSubject is the child of a list.)
                    ((SimpleComponentSubject) component.getParent().getParent()).getVariablesModifiable().remove(component);
                    
                    //remove from module tree view
                    moduleSelectTree.removeCurrentNode();
                    return;
                }
                
                // Find the way to the component in the module
                ListSubject<AbstractSubject> rootList = getModuleSubject().getComponentListModifiable();
                ListSubject<AbstractSubject> currentList = rootList;
                for (int i=1; i<depth; i++)
                {
                    // We're (depth-i) levels too deep
                    DefaultMutableTreeNode currentNode = targetNode;
                    for (int j=0; j<(depth-i); j++)
                    {
                        currentNode = (DefaultMutableTreeNode) (currentNode.getParent());
                    }
                    // Find currentNode (a ForeachSubject) in currentList and unfold a new ListSubject
                    ForeachSubject foreachSubject = (ForeachSubject) currentList.get(currentList.indexOf(((ComponentInfo) currentNode.getUserObject()).getComponent()));
                    currentList = foreachSubject.getBodyModifiable();
                }
                // I just realised there's a nicer way to do this... well, well.
                
                // Remove component from module
                currentList.remove(component);
                
                // Remove the component visually
                moduleSelectTree.removeCurrentNode();
            }
        }
        
        if ("copy component".equals(e.getActionCommand()))
        {
            TreePath currentSelection = moduleSelectTree.getSelectionPath();
            if (currentSelection != null)
            {
                // Get the node in the tree
                DefaultMutableTreeNode targetNode = (DefaultMutableTreeNode) (currentSelection.getLastPathComponent());
                // Get the component
                Subject component = ((ComponentInfo) targetNode.getUserObject()).getComponent();
                if(component instanceof SimpleComponentSubject)
                {
                    SimpleComponentSubject original = (SimpleComponentSubject) component;
                    SimpleComponentSubject copy = original.clone();
                    IdentifierSubject newIdentifier = original.getIdentifier().clone();
                    newIdentifier.setName("Copy of " + original.getName());
                    if (componentNameAvailable(newIdentifier.getName()))
                    {
                        copy.setIdentifier(newIdentifier);
                        addComponent(copy);
                    }
                    else
                    {
                        moduleContainer.getIDE().error("Name " + newIdentifier.getName() + " already exists in module.");
                    }
                }
            }
        }
         
        if ("rename component".equals(e.getActionCommand()))
        {
            TreePath currentSelection = moduleSelectTree.getSelectionPath();
            if (currentSelection != null)
            {
                // Get the node in the tree
                DefaultMutableTreeNode targetNode = (DefaultMutableTreeNode) (currentSelection.getLastPathComponent());
                // Get the component 
                Subject component = ((ComponentInfo) targetNode.getUserObject()).getComponent();
                if(component instanceof SimpleComponentSubject)
                {
                    SimpleComponentSubject simpleComponent = (SimpleComponentSubject) component;
                    renameComponent(simpleComponent);            
                }
            }
        }

        if("add simple component".equals(e.getActionCommand()))
        {
            moduleContainer.getIDE().getActions().editorAddSimpleComponentAction.doAction();
        }        
        
        if("show comment".equals(e.getActionCommand()))
        {
            root.showComment();
        }
        
        if("toPlantType".equals(e.getActionCommand()) ||
            "toSpecificationType".equals(e.getActionCommand()) ||
            "toSupervisorType".equals(e.getActionCommand()))
        {
            TreePath currentSelection = moduleSelectTree.getSelectionPath();
            if (currentSelection != null)
            {
                // Get the node in the tree
                DefaultMutableTreeNode targetNode = (DefaultMutableTreeNode)
                (currentSelection.getLastPathComponent());
                Subject component = ((ComponentInfo) targetNode.getUserObject()).getComponent();
                
                if(component instanceof SimpleComponentSubject)
                {
                    if("toPlantType".equals(e.getActionCommand()))
                        ((SimpleComponentSubject)component).setKind(ComponentKind.PLANT);
                    if ("toSpecificationType".equals(e.getActionCommand()))
                        ((SimpleComponentSubject)component).setKind(ComponentKind.SPEC);
                    if ("toSupervisorType".equals(e.getActionCommand()))
                        ((SimpleComponentSubject)component).setKind(ComponentKind.SUPERVISOR);
                }
                else
                {
                    moduleContainer.getIDE().error("ModuleWindow.actionPerformed(): " +
                        "'" + e.getActionCommand() + "' performed by illegal node type");
                }
            }
        }
    }
}
