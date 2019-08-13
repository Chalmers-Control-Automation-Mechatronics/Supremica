package org.supremica.automata.BDD.SupremicaBDDBitVector;

import net.sf.javabdd.BDD;

/**
 *
 * @author sajed
 */
public class ResultOverflows {


    private final SupremicaBDDBitVector result;
    private final BDD overflows;

    public ResultOverflows(final SupremicaBDDBitVector result, final BDD overflows)
    {
        this.result = result;
        this.overflows = overflows;
    }

    public SupremicaBDDBitVector getResult()
    {
        return result;
    }

    public BDD getOverflows()
    {
        return overflows;
    }
    
}
