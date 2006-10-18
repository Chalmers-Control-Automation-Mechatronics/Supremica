
/************************ StateSubTree.java *******************/

// A StateSubTree is a tree node with the state name as root
// and the state properties as children
package org.supremica.gui.treeview;

import javax.swing.*;
import org.supremica.automata.State;
import org.supremica.gui.Supremica;

public class StateSubTree
    extends SupremicaTreeNode
{
    private static ImageIcon ordinaryStateIcon = new ImageIcon(Supremica.class.getResource("/icons/State16.gif"));
    private static ImageIcon markedStateIcon = new ImageIcon(Supremica.class.getResource("/icons/MarkedState16.gif"));
    private static ImageIcon markedInitialStateIcon = new ImageIcon(Supremica.class.getResource("/icons/MarkedInitialState16.gif"));
    private static ImageIcon forbiddenInitialStateIcon = new ImageIcon(Supremica.class.getResource("/icons/ForbiddenInitialState16.gif"));
    private static ImageIcon initialStateIcon = new ImageIcon(Supremica.class.getResource("/icons/InitialState16.gif"));
    private static ImageIcon forbiddenStateIcon = new ImageIcon(Supremica.class.getResource("/icons/ForbiddenState16.gif"));
    
    public StateSubTree(State state)
    {
        super(state);    // Note that this also caches the state for quick access
        
        if (state.isInitial())
        {
            SupremicaTreeNode initial = new SupremicaTreeNode("initial");
            
            add(initial);
        }
        
        if (state.isAccepting())
        {
            SupremicaTreeNode accepting = new SupremicaTreeNode("accepting");
            
            add(accepting);
        }
        
        if (state.isForbidden())
        {
            SupremicaTreeNode forbidden = new SupremicaTreeNode("forbidden");
            
            add(forbidden);
        }
    }
    
    // This calculates the number of direct leaf children
    // That is, the number of initial/accepting/forbidden leaf nodes
    public int numDirectLeafs()
    {
        State state = (State) getUserObject();
        int directleafs = 0;
        
        if (state.isInitial())
        {
            ++directleafs;
        }
        
        if (state.isForbidden())
        {
            ++directleafs;
        }
        
        if (state.isAccepting())
        {
            ++directleafs;
        }
        
        return directleafs;
    }
    
    public Icon getOpenIcon()
    {
        
        //return null;
        if (((State) userObject).isInitial())
        {
            if (((State) userObject).isForbidden())
            {
                return forbiddenInitialStateIcon;
            }
            else if (((State) userObject).isAccepting())
            {
                return markedInitialStateIcon;
            }
            else
            {
                return initialStateIcon;
            }
        }
        else if (((State) userObject).isForbidden())
        {
            return forbiddenStateIcon;
        }
        else if (((State) userObject).isAccepting())
        {
            return markedStateIcon;
        }
        else
        {
            return ordinaryStateIcon;
        }
    }
    
    public Icon getClosedIcon()
    {
        
        //return null;
        return getOpenIcon();
    }
    
    public Icon getLeafIcon()
    {
        
        //return null;
        return getOpenIcon();
    }
    
    public String toString()
    {
        return ((State) userObject).getName();
    }
}
