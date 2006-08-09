//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: org.supremica.gui.ide
//# CLASS:   EditorComponentsPanel
//###########################################################################
//# $Id: EditorComponentsPanel.java,v 1.25 2006-08-09 10:22:16 robi Exp $
//###########################################################################


package org.supremica.gui.ide;

import java.awt.event.ActionEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.tree.*;

import net.sourceforge.waters.gui.ComponentInfo;
import net.sourceforge.waters.gui.EditorEditVariableDialog;
import net.sourceforge.waters.gui.EditorNewDialog;
import net.sourceforge.waters.gui.ModuleTree;
import net.sourceforge.waters.gui.ModuleWindowInterface;
import net.sourceforge.waters.subject.base.AbstractSubject;
import net.sourceforge.waters.subject.base.Subject;
import net.sourceforge.waters.subject.module.ForeachSubject;
import net.sourceforge.waters.subject.module.InstanceSubject;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.ParameterBindingSubject;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;
import net.sourceforge.waters.subject.module.VariableSubject;

import org.supremica.gui.WhiteScrollPane;


class EditorComponentsPanel
	extends WhiteScrollPane
	implements EditorPanelInterface
{
	private static final long serialVersionUID = 1L;

	private String name;
	private ModuleContainer moduleContainer;

	private ModuleTree moduleSelectTree;
	private boolean modified = true;

	EditorComponentsPanel(final ModuleContainer moduleContainer,
						  final ModuleWindowInterface root,
						  final String name)
	{
		this.moduleContainer = moduleContainer;
		this.name = name;
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

	// To do remove this
	public void addEvent()
	{
	}

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

			if ((o instanceof SimpleComponentSubject))
			{
				SimpleComponentSubject scp = (SimpleComponentSubject) o;

				//
				//logEntry("Adding SimpleComponentSubject: " + scp.getName());

				//EditorWindow ed = new EditorWindow(scp.getName() + " - Waters Editor", module, scp, this, this);
			}

			//Add node to module tree
			((ModuleTree) moduleSelectTree).addComponent(o);
		}
	}

	public ModuleSubject getModuleSubject()
	{
		return moduleContainer.getModule();
	}


	//######################################################################
	//# Interface java.awt.event.ActionListener
	public void actionPerformed(ActionEvent e) {
		if("add variable".equals(e.getActionCommand())) {
			//add new variable
			TreePath currentSelection = moduleSelectTree.getSelectionPath();
			if (currentSelection != null)
			{
				// Get the node in the tree
				DefaultMutableTreeNode targetNode = (DefaultMutableTreeNode)
				(currentSelection.getLastPathComponent());
				Subject component = ((ComponentInfo)
						targetNode.getUserObject()).getComponent();

				if(component instanceof VariableSubject) {
					component = component.getParent().getParent();
					EditorEditVariableDialog.showDialog(null,
							(SimpleComponentSubject) component, moduleSelectTree);
				} else if(component instanceof SimpleComponentSubject) {
					EditorEditVariableDialog.showDialog(null,
							(SimpleComponentSubject) component, moduleSelectTree);
				} else {
					System.err.println("ModuleWindow.actionPerformed(): " +
					"'add variable' performed by illegal node type");
				}
			}
		}

		if("edit variable".equals(e.getActionCommand())) {
			//edit existing variable
			TreePath currentSelection = moduleSelectTree.getSelectionPath();
			if (currentSelection != null)
			{
				// Get the node in the tree
				DefaultMutableTreeNode targetNode = (DefaultMutableTreeNode)
				(currentSelection.getLastPathComponent());
				Subject component = ((ComponentInfo)
						targetNode.getUserObject()).getComponent();

				if(component instanceof VariableSubject) {
					Subject parent = component.getParent().getParent();
					EditorEditVariableDialog.showDialog((VariableSubject) component,
							(SimpleComponentSubject) parent, moduleSelectTree);
				} else {
					System.err.println("ModuleWindow.actionPerformed(): " +
					"'edit variable' performed by illegal node type");
				}
			}
		}

		if("add simple component".equals(e.getActionCommand())) {
			moduleContainer.getActions().editorAddSimpleComponentAction.doAction();
		}
	}
}
