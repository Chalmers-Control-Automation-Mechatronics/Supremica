package org.supremica.automata.BDD.EFA;

/**
 *
 * @author sajed
 */
import net.sf.javabdd.*;
import org.supremica.log.*;
import org.supremica.util.SupremicaException;
import java.util.*;
import org.supremica.properties.Config;

public class BDDEdgeFactory
{
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

