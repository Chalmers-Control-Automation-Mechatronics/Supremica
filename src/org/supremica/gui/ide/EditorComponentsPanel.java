//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: org.supremica.gui.ide
//# CLASS:   EditorComponentsPanel
//###########################################################################
//# $Id: EditorComponentsPanel.java,v 1.14 2005-11-10 21:54:42 robi Exp $
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
import net.sourceforge.waters.model.printer.ProxyPrinter;
import net.sourceforge.waters.subject.base.AbstractSubject;
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
		final AbstractSubject subject = (AbstractSubject) e;
		final ProxyPrinter printer = moduleContainer.getPrinter();
		final String text = printer.toString(subject);
		final Object userobject = new ComponentInfo(text, subject);
		if (subject instanceof SimpleComponentSubject) {
			return new DefaultMutableTreeNode(userobject, false);
		} else if (subject instanceof InstanceSubject) {
			final InstanceSubject inst = (InstanceSubject) subject;
			final DefaultMutableTreeNode tmp =
				new DefaultMutableTreeNode(userobject, true);
			final List<ParameterBindingSubject> bindings =
				inst.getBindingListModifiable() ;
			for (final ParameterBindingSubject binding : bindings) {
				tmp.add(makeTreeFromComponent(binding));
			}
			return tmp;
		} else if (subject instanceof ParameterBindingSubject) {
			return new DefaultMutableTreeNode(userobject, false);
		} else if (subject instanceof ForeachSubject) {
			final ForeachSubject foreach = (ForeachSubject) subject;
			final DefaultMutableTreeNode tn =
				new DefaultMutableTreeNode(userobject, true);
			final List<AbstractSubject> body = foreach.getBodyModifiable();
			for (final AbstractSubject item : body) {
				tn.add(makeTreeFromComponent(item));
			}
			return tn;
		} else {
			throw new IllegalArgumentException
				("Don't know how to make tree from subject of type " +
				 e.getClass().getName() + "!");
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
