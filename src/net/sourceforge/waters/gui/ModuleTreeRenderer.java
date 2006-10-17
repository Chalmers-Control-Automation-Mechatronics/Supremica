
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.gui
//# CLASS:   ModuleTreeRenderer
//###########################################################################
//# $Id: ModuleTreeRenderer.java,v 1.5 2006-10-17 23:31:07 flordal Exp $
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

public class ModuleTreeRenderer
    extends DefaultTreeCellRenderer
{
    Icon plantIcon;
    Icon propIcon;
    Icon specIcon;
    Icon supIcon;
    Icon foreachIcon;
    Icon instIcon;
    Icon bindingIcon;
    
    public ModuleTreeRenderer(Icon foreach, Icon plant, Icon prop, Icon spec, Icon sup, Icon inst, Icon binding)
    {
        plantIcon = plant;
        propIcon = prop;
        specIcon = spec;
        supIcon = sup;
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
        
        if (/*leaf &&*/ (ci.getComponent() instanceof SimpleComponentProxy))
        {
            SimpleComponentProxy scp = (SimpleComponentProxy) ci.getComponent();
            
            if (scp.getKind() == ComponentKind.PLANT)
            {
                setIcon(plantIcon);
                setToolTipText("Plant");
            }
            else if (scp.getKind() == ComponentKind.PROPERTY)
            {
                setIcon(propIcon);
                setToolTipText("Property");
            }
            else if (scp.getKind() == ComponentKind.SPEC)
            {
                setIcon(specIcon);
                setToolTipText("Specification");
            }
            else if (scp.getKind() == ComponentKind.SUPERVISOR)
            {
                setIcon(supIcon);
                setToolTipText("Supervisor");
            }
            else
            {
                setToolTipText(null);
            }
        }
        else
        {
            setToolTipText(null);    //no tool tip
        }
        
        return this;
    }
}
