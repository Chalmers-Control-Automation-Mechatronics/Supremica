
/******************** EventSubTree.java *******************/

// An EventSubTree is a tree node with the event name as root
// and the event properties as children
package org.supremica.gui.treeview;

import javax.swing.*;
import org.supremica.automata.LabeledEvent;
import org.supremica.gui.Supremica;

public class EventSubTree
    extends SupremicaTreeNode
{
    private static final long serialVersionUID = 1L;

    //private static ImageIcon controllableIcon = new ImageIcon(Supremica.class.getResource("/icons/ControllableEvent16.gif"));
    private static ImageIcon controllableIcon = new ImageIcon(Supremica.class.getResource("/icons/waters/controllable.gif"));
    //private static ImageIcon uncontrollableIcon = new ImageIcon(Supremica.class.getResource("/icons/UncontrollableEvent16.gif"));
    private static ImageIcon uncontrollableIcon = new ImageIcon(Supremica.class.getResource("/icons/waters/uncontrollable.gif"));
    
    private int directLeafs = 0;
    
    public EventSubTree(LabeledEvent event)
    {
        super(event);    // Note that this also caches the event for quick access
        
        SupremicaTreeNode currControllableNode = new SupremicaTreeNode("controllable: " + event.isControllable());
        add(currControllableNode); directLeafs++;
        
        SupremicaTreeNode currObservableNode = new SupremicaTreeNode("observable: " + event.isObservable());
        add(currObservableNode); directLeafs++;
        
        // Hide junk...
        /*
        SupremicaTreeNode currPrioritizedNode = new SupremicaTreeNode("prioritized: " + event.isPrioritized());
        add(currPrioritizedNode); directLeafs++;
         
        SupremicaTreeNode currOperatorIncreaseNode = new SupremicaTreeNode("operatorIncrease: " + event.isOperatorIncrease());
        add(currOperatorIncreaseNode); directLeafs++;
         
        SupremicaTreeNode currOperatorResetNode = new SupremicaTreeNode("operatorReset: " + event.isOperatorReset());
        add(currOperatorResetNode); directLeafs++;
         */
    }
    
    /**
     * Change this to reflect the correct number of children/properties/leaves
     * Could this be calculated from sizeof(LabeledEvent)? It should not.
     * This depends only on the above construction
     *
     * This method is used to quickly determine if an event occurs in all 
     * automata or not, see showIntersection in EventsViewerPanel.
     */
    public int numDirectLeafs()
    {
        return directLeafs;
    }
    
    public Icon getOpenIcon()
    {
        //return null;
        if (((LabeledEvent) userObject).isControllable())
        {
            return controllableIcon;
        }
        else
        {
            return uncontrollableIcon;
        }
    }
    
    public Icon getClosedIcon()
    {
        //return null;
        return getOpenIcon();
    }
    
    public String toString()
    {
        return ((LabeledEvent) userObject).getLabel();
    }
}
