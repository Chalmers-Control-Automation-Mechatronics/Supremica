//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: org.supremica.gui.ide
//# CLASS:   EditorComponentsPanel
//###########################################################################
//# $Id: EditorComponentsPanel.java,v 1.23 2006-07-12 03:59:29 knut Exp $
//###########################################################################


package org.supremica.gui.ide;

import javax.swing.*;
import javax.swing.tree.*;

import java.util.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import net.sourceforge.waters.gui.ComponentInfo;
import net.sourceforge.waters.gui.EditorEditVariableDialog;
import net.sourceforge.waters.gui.EditorNewDialog;
import net.sourceforge.waters.gui.EditorWindow;
import net.sourceforge.waters.gui.EditorWindowInterface;
import net.sourceforge.waters.gui.ModuleTreeRenderer;
import net.sourceforge.waters.gui.ModuleWindowInterface;
import net.sourceforge.waters.model.printer.ProxyPrinter;
import net.sourceforge.waters.subject.base.AbstractSubject;
import net.sourceforge.waters.subject.base.Subject;
import net.sourceforge.waters.subject.module.ForeachSubject;
import net.sourceforge.waters.subject.module.InstanceSubject;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.ParameterBindingSubject;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;
import net.sourceforge.waters.subject.module.SimpleExpressionSubject;
import net.sourceforge.waters.subject.module.VariableSubject;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.gui.ModuleTree;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;


import org.supremica.gui.WhiteScrollPane;


class EditorComponentsPanel
	extends WhiteScrollPane
	implements EditorPanelInterface, ModuleWindowInterface
{
	private static final long serialVersionUID = 1L;

	private String name;
	private ModuleContainer moduleContainer;

	private ModuleTree moduleSelectTree;
	private boolean modified = true;

	EditorComponentsPanel(ModuleContainer moduleContainer, String name)
	{
		this.moduleContainer = moduleContainer;
		this.name = name;

		createContentPane();

		setPreferredSize(IDEDimensions.leftEditorPreferredSize);
		setMinimumSize(IDEDimensions.leftEditorMinimumSize);
	}

	// TO DO This is a dummy method implemted just to satisfy
	// the EditorPanelInterface. Thies method will never be called.
	// This solution is so ugly that it has to be removed ASAP.
	public DefaultListModel getEventDataList()
	{
		return null;
	}

	public String getName()
	{
		return name;
//		return moduleContainer.getModule().getName();
	}


	private void createContentPane()
	{
		final ArrayList l;
		DefaultMutableTreeNode treeNode = null;

		moduleSelectTree = new ModuleTree(this);

		//DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();

		getViewport().add(moduleSelectTree);

	}


	// Open up an component dialog and allow the user to create a new component
	public void addComponent()
	{
		EditorNewComponentDialog editor = new EditorNewComponentDialog(this);
	}

	// To do remove this
	public void addEvent()
	{

	}

	public void addComponent(final AbstractSubject o)
	{
//		logEntry("addComponent: " + o);

		ModuleSubject module = getModuleSubject();

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

//	public IndexedList<EventDeclSubject> getEventDeclListModifiable();

	public ModuleSubject getModuleSubject()
	{
		return this.moduleContainer.getModule();
	}

	public EditorWindowInterface showEditor(SimpleComponentSubject component)
	{
		final EditorPanel editorPanel =
			moduleContainer.getEditorPanel();
		if (component != null) {
			editorPanel.setRightComponent
				(moduleContainer.getComponentEditorPanel(component));
		}
		return editorPanel.getActiveEditorWindowInterface();
	}

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
