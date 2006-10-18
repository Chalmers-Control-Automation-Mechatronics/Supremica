
/*************************** SupremicaTreeNode.java ****************/

// This class exists to add an enabled feature to the tree nodes
// We (sometimes) want to draw disabled nodes differently
// We may even want to forbid selection of disabled nodes
package org.supremica.gui.treeview;

import javax.swing.*;
import javax.swing.tree.*;

public class SupremicaTreeNode
    extends DefaultMutableTreeNode
{
    boolean enabled = true;
    
    public SupremicaTreeNode()
    {
        super();
    }
    
    public SupremicaTreeNode(Object obj)
    {
        super(obj);
    }
    
    public boolean isEnabled()
    {
        return enabled;
    }
    
    public void setEnabled(boolean b)
    {
        enabled = b;
    }
    
    public Icon getOpenIcon()
    {
        return null;
    }
    
    public Icon getLeafIcon()
    {
        return null;
    }
    
    public Icon getClosedIcon()
    {
        return null;
    }
    
    public Icon getDisabledIcon()
    {
        return null;
    }
}
