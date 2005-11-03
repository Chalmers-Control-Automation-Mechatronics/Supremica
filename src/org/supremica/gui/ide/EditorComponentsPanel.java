//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: org.supremica.gui.ide
//# CLASS:   EditorComponentsPanel
//###########################################################################
//# $Id: EditorComponentsPanel.java,v 1.12 2005-11-03 01:24:16 robi Exp $
//###########################################################################


package org.supremica.gui.ide;

import javax.swing.*;
import javax.swing.tree.*;
import java.util.*;
import java.awt.event.MouseListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import net.sourceforge.waters.gui.ComponentInfo;
import net.sourceforge.waters.gui.ModuleTreeRenderer;
import net.sourceforge.waters.subject.module.ForeachSubject;
import net.sourceforge.waters.subject.module.InstanceSubject;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.ParameterBindingSubject;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;
import net.sourceforge.waters.subject.module.SimpleExpressionSubject;

import org.supremica.gui.WhiteScrollPane;


class EditorComponentsPanel
	extends WhiteScrollPane
{
	private static final long serialVersionUID = 1L;

	private String name;
	private ModuleContainer moduleContainer;

	EditorComponentsPanel(ModuleContainer moduleContainer, String name)
	{
		this.moduleContainer = moduleContainer;
		this.name = name;

		createContentPane();

		setPreferredSize(IDEDimensions.leftEditorPreferredSize);
		setMinimumSize(IDEDimensions.leftEditorMinimumSize);
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

		final ModuleSubject module = moduleContainer.getModule();

		if (module != null)
		{
			l = new ArrayList(module.getComponentList());
			treeNode = new DefaultMutableTreeNode(new ComponentInfo("Module: " + module.getName(), null));
		}
		else
		{
			l = new ArrayList();
			treeNode = null;
		}

		for (int i = 0; i < l.size(); i++)
		{
			treeNode.add(makeTreeFromComponent(l.get(i)));
		}

		DefaultMutableTreeNode rootNode = treeNode;
		DefaultTreeModel treeModel = new DefaultTreeModel(rootNode);

		//TODO: Put some proper icons in place!
		final ImageIcon plantIcon = new ImageIcon(IDE.class.getResource("/icons/waters/plant.gif"));
		final ImageIcon specIcon = new ImageIcon(IDE.class.getResource("/icons/waters/spec.gif"));
		final ImageIcon propertyIcon = new ImageIcon(IDE.class.getResource("/icons/waters/property.gif"));
		final ImageIcon foreachIcon = null;
		final ImageIcon instanceIcon = new ImageIcon(IDE.class.getResource("/icons/waters/instance.gif"));
		final ImageIcon bindingIcon = null;

		JTree moduleSelectTree = new JTree(treeModel);

		moduleSelectTree.setCellRenderer(new ModuleTreeRenderer(foreachIcon, plantIcon, propertyIcon, specIcon, instanceIcon, bindingIcon));
		moduleSelectTree.setEditable(false);
		moduleSelectTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

		DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();

		//renderer.setLeafIcon(simpleIcon);
		//renderer.setOpenIcon(null);
		//renderer.setClosedIcon(null);
		//moduleSelectTree.setCellRenderer(renderer);
		MouseListener ml = new TreeMouseAdapter(module, moduleSelectTree);

		moduleSelectTree.addMouseListener(ml);
		getViewport().add(moduleSelectTree);

	}


	private DefaultMutableTreeNode makeTreeFromComponent(final Object e)
	{
		if (e instanceof SimpleComponentSubject) {
			final SimpleComponentSubject comp = (SimpleComponentSubject) e;
			final String name = comp.getName();
			final Object userobject = new ComponentInfo(name, comp);
			return new DefaultMutableTreeNode(userobject, false);
		} else if (e instanceof InstanceSubject) {
			final InstanceSubject inst = (InstanceSubject) e;
			final String name =
				"<html><b>Instance </b><i>" + inst.getName() + "</i> = <i>" +
				inst.getModuleName() + "</i></html>";
			final Object userobject = new ComponentInfo(name, inst);
			DefaultMutableTreeNode tmp =
				new DefaultMutableTreeNode(userobject, true);
			for (int j = 0; j < inst.getBindingList().size(); j++) {
				tmp.add(makeTreeFromComponent(inst.getBindingList().get(j)));
			}
			return tmp;
		} else if (e instanceof ParameterBindingSubject) {
			final ParameterBindingSubject binding =
				(ParameterBindingSubject) e;
			final String name =
				"<html><b>Binding: </b><i>" + binding.getName() +
				"</i> = <i>" + binding.getExpression().toString() +
				"</i></html>";
			final Object userobject = new ComponentInfo(name, binding);
			return new DefaultMutableTreeNode(userobject, false);
		} else {
			final ForeachSubject foreach = (ForeachSubject) e;
			final SimpleExpressionSubject range = foreach.getRange();
			final SimpleExpressionSubject guard = foreach.getGuard();
			final StringBuffer buffer = new StringBuffer();
			buffer.append("<html><b>Foreach </b><i>");
			buffer.append(foreach.getName());
			buffer.append("</i> <b>in<b> <i>");
			buffer.append(range);
			if (guard != null) {
				buffer.append(" <b>Where</b> <i>");
				buffer.append(guard);
				buffer.append("</i>");
			}
			buffer.append("</html>");
			final String name = buffer.toString();
			final Object userobject = new ComponentInfo(name, foreach);
			final DefaultMutableTreeNode tn =
				new DefaultMutableTreeNode(userobject, true);
			for (int i = 0; i < foreach.getBody().size(); i++) {
				tn.add(makeTreeFromComponent(foreach.getBody().get(i)));
			}
			return tn;
		}
	}


	private class TreeMouseAdapter
		extends MouseAdapter
	{
		JTree moduleSelectTree;
		ModuleSubject module;

//		ComponentEditorPanel ed = null;

		public TreeMouseAdapter(ModuleSubject module, JTree moduleSelectTree)
		{
			this.module = module;
			this.moduleSelectTree = moduleSelectTree;
		}

		public void mousePressed(MouseEvent e)
		{
			int selRow = moduleSelectTree.getRowForLocation(e.getX(), e.getY());
			TreePath selPath = moduleSelectTree.getPathForLocation(e.getX(), e.getY());

			if (selRow != -1)
			{
				if (e.getClickCount() == 2)
				{
					DefaultMutableTreeNode node = (DefaultMutableTreeNode) moduleSelectTree.getLastSelectedPathComponent();

					if (node == null) {
						return;
					} else if (node.isLeaf()) {
						final ComponentInfo compInfo =
							(ComponentInfo) node.getUserObject();
						final SimpleComponentSubject comp =
							(SimpleComponentSubject) compInfo.getComponent();
						if (comp != null) {
							final EditorPanel editorPanel =
								moduleContainer.getEditorPanel();
							editorPanel.setRightComponent
								(moduleContainer.getComponentEditorPanel(comp));
						}
					}
				}
			}
		}
	}

}
