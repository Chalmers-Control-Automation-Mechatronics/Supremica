package org.supremica.gui.ide;

import javax.swing.*;
import javax.swing.tree.*;
import java.util.*;
import java.awt.event.MouseListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import org.supremica.gui.WhiteScrollPane;
import net.sourceforge.waters.model.module.*;
import net.sourceforge.waters.model.base.*;
import net.sourceforge.waters.gui.EditorWindow;
import net.sourceforge.waters.gui.ComponentInfo;
import net.sourceforge.waters.gui.ModuleTreeRenderer;

import javax.swing.tree.*;

class EditorComponentsPanel
	extends WhiteScrollPane
{
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
//		return moduleContainer.getModuleProxy().getName();
	}


	private void createContentPane()
	{
		final ArrayList l;
		DefaultMutableTreeNode treeNode = null;

		ModuleProxy module = moduleContainer.getModuleProxy();

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
			treeNode.add(makeTreeFromComponent((ElementProxy) (l.get(i))));
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


/*
		JButton NewSimpleButton = new JButton("New Simple Component");

		NewSimpleButton.addActionListener(this);
		NewSimpleButton.setActionCommand("newsimple");

		JButton NewForeachButton = new JButton("New Foreach Component");

		NewForeachButton.addActionListener(this);
		NewForeachButton.setActionCommand("newforeach");

		JButton DeleteComponentButton = new JButton("Remove Component");

		if (module == null)
		{
			NewSimpleButton.setEnabled(false);
			NewForeachButton.setEnabled(false);
			DeleteComponentButton.setEnabled(false);
		}

		JPanel content = new JPanel();
		Box b = new Box(BoxLayout.PAGE_AXIS);

		b.add(new JScrollPane(moduleSelectTree));

		JPanel buttonpanel = new JPanel();

		buttonpanel.add(NewSimpleButton);
		buttonpanel.add(NewForeachButton);
		buttonpanel.add(newInstanceButton = new JButton("New Instance"));
		buttonpanel.add(newBindingButton = new JButton("New Binding"));
		buttonpanel.add(DeleteComponentButton);
		newInstanceButton.setActionCommand("newinstance");
		newInstanceButton.addActionListener(this);
		newBindingButton.setActionCommand("newbinding");
		newBindingButton.addActionListener(this);
		b.add(buttonpanel);
		content.add(b);
		content.setLayout(new GridLayout(1, 1));

		return content;
*/
	}


	private DefaultMutableTreeNode makeTreeFromComponent(ElementProxy e)
	{
		if (e instanceof SimpleComponentProxy)
		{
			final Object userobject = new ComponentInfo(((IdentifiedElementProxy) e).getName(), e);

			return new DefaultMutableTreeNode(userobject, false);
		}
		else if (e instanceof InstanceProxy)
		{
			InstanceProxy i = (InstanceProxy) e;
			String name = "<html><b>Instance </b><i>" + i.getName() + "</i> = <i>" + i.getModuleName() + "</i></html>";
			final Object userobject = new ComponentInfo(name, e);
			DefaultMutableTreeNode tmp = new DefaultMutableTreeNode(userobject, true);

			for (int j = 0; j < i.getBindingList().size(); j++)
			{
				tmp.add(makeTreeFromComponent((ElementProxy) (i.getBindingList().get(j))));
			}

			return tmp;
		}
		else if (e instanceof ParameterBindingProxy)
		{
			ParameterBindingProxy i = (ParameterBindingProxy) e;
			String name = "<html><b>Binding: </b><i>" + i.getName() + "</i> = <i>" + i.getExpression().toString() + "</i></html>";
			final Object userobject = new ComponentInfo(name, e);

			return new DefaultMutableTreeNode(userobject, false);
		}
		else
		{
			String name;
			ForeachProxy v = (ForeachProxy) e;
			ElementProxy range = v.getRange();
			ElementProxy guard = v.getGuard();

			name = "<html><b>Foreach </b><i>" + ((NamedProxy) e).getName() + "</i>";

			if (range != null)
			{
				name += " <b>in<b> <i>" + range.toString() + "</i>";
			}

			if (guard != null)
			{
				name += " <b>Where</b> <i>" + guard.toString() + "</i></html>";
			}

			final Object userobject = new ComponentInfo(name, e);
			final DefaultMutableTreeNode tn = new DefaultMutableTreeNode(userobject, true);

			for (int i = 0; i < v.getBody().size(); i++)
			{
				tn.add(makeTreeFromComponent((ElementProxy) (v.getBody().get(i))));
			}

			return tn;
		}
	}

	private class TreeMouseAdapter
		extends MouseAdapter
	{
		JTree moduleSelectTree;
		ModuleProxy module;

//		ComponentEditorPanel ed = null;

		public TreeMouseAdapter(ModuleProxy module, JTree moduleSelectTree)
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

					if (node == null)
					{
						return;
					}

					Object nodeInfo = node.getUserObject();

					if (node.isLeaf())
					{
						SimpleComponentProxy scp = (SimpleComponentProxy) (((ComponentInfo) nodeInfo).getComponent());

						if (scp != null)
						{
//							if (ed == null)
//							{
//								ed = new ComponentEditorPanel(moduleContainer, scp);
//							}
							EditorPanel editorPanel = moduleContainer.getEditorPanel();
							editorPanel.setRightComponent(moduleContainer.getComponentEditorPanel(scp));
//							ed = new EditorWindow(scp.getName(), module, scp);
						}
					}
				}
			}
		}
	}

}