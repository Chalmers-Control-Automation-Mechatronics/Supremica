
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.gui
//# CLASS:   ModuleTreeRenderer
//###########################################################################
//# $Id: ModuleTreeRenderer.java,v 1.2 2005-02-18 03:09:06 knut Exp $
//###########################################################################
package net.sourceforge.waters.gui;

import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.xml.bind.JAXBException;
import net.sourceforge.waters.model.base.*;
import net.sourceforge.waters.model.module.*;
import java.util.ArrayList;
import net.sourceforge.waters.xsd.base.ComponentKind;
import javax.swing.tree.DefaultMutableTreeNode;

class ModuleTreeRenderer
	extends DefaultTreeCellRenderer
{
	Icon plantIcon;
	Icon propIcon;
	Icon specIcon;
	Icon foreachIcon;
	Icon instIcon;
	Icon bindingIcon;

	public ModuleTreeRenderer(Icon foreach, Icon plant, Icon prop, Icon spec, Icon inst, Icon binding)
	{
		plantIcon = plant;
		propIcon = prop;
		specIcon = spec;
		foreachIcon = foreach;
		instIcon = inst;
		bindingIcon = binding;
	}

	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus)
	{
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

		ComponentInfo ci = (ComponentInfo) ((DefaultMutableTreeNode) value).getUserObject();

		if ((ci.getComponent() instanceof InstanceProxy))
		{
			setIcon(instIcon);
			setToolTipText(null);
		}

		if ((ci.getComponent() instanceof ParameterBindingProxy))
		{
			setIcon(bindingIcon);
			setToolTipText(null);
		}

		if ((ci.getComponent() instanceof ForeachProxy))
		{
			setIcon(foreachIcon);
			setToolTipText(null);

			//setEditable(false);
		}
		else
		{
			setToolTipText(null);    //no tool tip
		}

		if (leaf && (ci.getComponent() instanceof SimpleComponentProxy))
		{
			SimpleComponentProxy scp = (SimpleComponentProxy) ci.getComponent();

			if (scp.getKind() == ComponentKind.PLANT)
			{
				setIcon(plantIcon);
			}

			if (scp.getKind() == ComponentKind.PROPERTY)
			{
				setIcon(propIcon);
			}

			if (scp.getKind() == ComponentKind.SPEC)
			{
				setIcon(specIcon);
			}

			setToolTipText(null);
		}
		else
		{
			setToolTipText(null);    //no tool tip
		}

		return this;
	}
}
