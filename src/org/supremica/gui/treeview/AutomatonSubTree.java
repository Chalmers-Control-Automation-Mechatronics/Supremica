
/********************* AutomatonSubTree.java *************/

// An AutomatonSubTree is a tree node with the automaton name as root
// and the events as children and the states as children
package org.supremica.gui.treeview;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import net.sourceforge.waters.gui.util.IconLoader;

import org.supremica.automata.Automaton;

public class AutomatonSubTree
    extends SupremicaTreeNode
{
    private static final long serialVersionUID = 1L;

    private static ImageIcon plantIcon = IconLoader.ICON_PLANT;
    private static ImageIcon specificationIcon = IconLoader.ICON_SPEC;
    private static ImageIcon supervisorIcon = IconLoader.ICON_SUPERVISOR;

    public AutomatonSubTree(final Automaton automaton, final boolean includeAlphabet, final boolean includeStates)
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

    @Override
    public Icon getOpenIcon()
    {
        //return null;
        final Automaton aut = (Automaton) userObject;

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

    @Override
    public Icon getClosedIcon()
    {
        // Same icon as for open
        return getOpenIcon();
    }

    @Override
    public String toString()
    {
        return ((Automaton) userObject).getName();
    }
}
