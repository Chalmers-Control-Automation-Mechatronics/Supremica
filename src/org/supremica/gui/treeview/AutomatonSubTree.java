
/********************* AutomatonSubTree.java *************/

// An AutomatonSubTree is a tree node with the automaton name as root
// and the events as children and the states as children
package org.supremica.gui.treeview;

import javax.swing.*;
import org.supremica.automata.Automaton;
import org.supremica.gui.Supremica;

public class AutomatonSubTree
    extends SupremicaTreeNode
{
    private static final long serialVersionUID = 1L;

    private static ImageIcon plantIcon = new ImageIcon(Supremica.class.getResource("/icons/waters/plant.gif"));
    private static ImageIcon specificationIcon = new ImageIcon(Supremica.class.getResource("/icons/waters/spec.gif"));
    private static ImageIcon supervisorIcon = new ImageIcon(Supremica.class.getResource("/icons/waters/supervisor.gif"));
    
    public AutomatonSubTree(Automaton automaton, boolean includeAlphabet, boolean includeStates)
    {
        //super(automaton.getName());
        super(automaton);
        
        // If we are to show either, but not both, the "Alphabet" and/or "State" nodes are unnecessary
        if (includeAlphabet && includeStates)
        {
            add(new AlphabetSubTree(automaton.getAlphabet()));
            add(new StateSetSubTree(automaton.getStateSet()));
        }
        else if (includeAlphabet)
        {
            AlphabetSubTree.buildSubTree(automaton.getAlphabet(), this);
        }
        else if (includeStates)
        {
            StateSetSubTree.buildSubTree(automaton.getStateSet(), this);
        }
    }
    
    public Icon getOpenIcon()
    {
        //return null;
        Automaton aut = (Automaton) userObject;
        
        if (aut.isPlant())
        {
            return plantIcon;
        }
        else if (aut.isSpecification())
        {
            return specificationIcon;
        }
        else if (aut.isSupervisor())
        {
            return supervisorIcon;
        }
        else
        {
            return null;
        }
    }
    
    public Icon getClosedIcon()
    {
        // Same icon as for open
        return getOpenIcon();
    }
    
    public String toString()
    {
        return ((Automaton) userObject).getName();
    }
}
