package org.supremica.automata.BDD.EFA;

/**
 *
 * @author sajed
 */

import org.supremica.log.*;


public class BDDEdgeFactory
{
    @SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.createLogger(BDDEdgeFactory.class);

    BDDExtendedAutomata bddExAutomata;

    public BDDEdgeFactory(BDDExtendedAutomata bddExAutomata)
    {
        this.bddExAutomata = bddExAutomata;
    }

    public BDDEdges createEdges()
    {
        return new BDDMonolithicEdges(bddExAutomata);
    }

}

