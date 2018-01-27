package org.supremica.automata.BDD.EFA;


/**
 * @author Sajed Miremadi
 */

public class BDDEdgeFactory
{
    BDDExtendedAutomata bddExAutomata;

    public BDDEdgeFactory(final BDDExtendedAutomata bddExAutomata)
    {
        this.bddExAutomata = bddExAutomata;
    }

    public BDDEdges createEdges()
    {
        return new BDDMonolithicEdges(bddExAutomata);
    }

}

