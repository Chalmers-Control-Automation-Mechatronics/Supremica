
/***************** SupremicaTreeCellRenderer.java ************/

// Tree cell renderer, manages the icons etc
package org.supremica.gui.treeview;

import java.awt.*;
import javax.swing.*;
import javax.swing.tree.*;

public class SupremicaTreeCellRenderer
    extends DefaultTreeCellRenderer
{
    private static final long serialVersionUID = 1L;

    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected,    //
        boolean expanded,    // true => openIcon, else closedIcon
        boolean leaf,    // true => leafIcon, cannot be open
        int row, boolean hasFocus)
    {
        super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        
        SupremicaTreeNode node = (SupremicaTreeNode) value;
        
        if (node.isEnabled() == false)
        {
            Icon icon = node.getDisabledIcon();
            
            if (icon == null)
            {
                icon = getDisabledIcon();
            }
            
            setIcon(icon);
            
            return this;
        }
        
        if (leaf)
        {
            Icon icon = node.getLeafIcon();
            
            if (icon == null)
            {
                icon = getDefaultLeafIcon();
            }
            
            setIcon(icon);
            
            return this;
        }
        else if (expanded)    // cannot be leaf
        {
            Icon icon = node.getOpenIcon();
            
            if (icon == null)
            {
                icon = getDefaultOpenIcon();
            }
            
            setIcon(icon);
            
            return this;
        }
        else    // must be non-expanded non-leaf
        {
            Icon icon = node.getClosedIcon();
            
            if (icon == null)
            {
                icon = getDefaultClosedIcon();
            }
            
            setIcon(icon);
            
            return this;
        }
    }
}
