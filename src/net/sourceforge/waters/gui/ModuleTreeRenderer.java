//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   ModuleTreeRenderer
//###########################################################################
//# $Id: ModuleTreeRenderer.java,v 1.7 2007-03-01 02:20:03 robi Exp $
//###########################################################################

package net.sourceforge.waters.gui;

import java.awt.Component;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.model.module.InstanceProxy;
import net.sourceforge.waters.model.module.ForeachProxy;
import net.sourceforge.waters.model.module.ParameterBindingProxy;


public class ModuleTreeRenderer
    extends DefaultTreeCellRenderer
{

	//#######################################################################
	//# Interface javax.swing.tree.TreeCellRenderer
    public Component getTreeCellRendererComponent
		(final JTree tree, final Object value, final boolean sel,
		 final boolean expanded, final boolean leaf,
		 final int row, final boolean hasFocus)
    {
        super.getTreeCellRendererComponent
			(tree, value, sel, expanded, leaf, row, hasFocus);
		final DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
        final ComponentInfo ci = (ComponentInfo) node.getUserObject();
        final Proxy proxy = ci.getComponent();
		if (proxy instanceof SimpleComponentProxy) {
			final SimpleComponentProxy comp = (SimpleComponentProxy) proxy;
			switch (comp.getKind()) {
			case PLANT:
                setIcon(IconLoader.ICON_PLANT);
                setToolTipText("Plant");
				break;
			case PROPERTY:
                setIcon(IconLoader.ICON_PROPERTY);
                setToolTipText("Property");
				break;
			case SPEC:
                setIcon(IconLoader.ICON_SPEC);
                setToolTipText("Specification");
				break;
			case SUPERVISOR:
                setIcon(IconLoader.ICON_SUPERVISOR);
                setToolTipText("Supervisor");
				break;
			default:
				throw new IllegalArgumentException
					("Unknown component kind: " + comp.getKind() + "!");
			}
		} else if (proxy instanceof InstanceProxy) {
            setIcon(IconLoader.ICON_INSTANCE);
            setToolTipText(null);
        } else if (proxy instanceof ParameterBindingProxy) {
            setIcon(IconLoader.ICON_BINDING);
            setToolTipText(null);
        } else if (proxy instanceof ForeachProxy) {
            setIcon(IconLoader.ICON_FOREACH);
            setToolTipText(null);
            //setEditable(false);
        } else {
            setToolTipText(null);
        }
        return this;
    }

}
