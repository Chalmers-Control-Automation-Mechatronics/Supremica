package org.supremica.automata.BDD.EFA;

/**
 * A decorator of BDDExDisjeventDepSets. 
 * By using this abstract class, the other two partitioning techniques are implemented. 
 * 
 * These two partitioning techniques are based automaton or variable. Note that these 
 * two partitioning approaches are just to group different event-based BDD together to
 * improve the performance in terms of time and memory.
 * 
 * 
 * @author zhennan
 * @version 1.0
 */

public abstract class BDDExDisjDepSetsDecorator extends BDDExDisjDepSets{

    BDDExDisjEventDepSets eventParDepSets;

    public BDDExDisjDepSetsDecorator(BDDExtendedAutomata bddExtendAutomata, BDDExDisjEventDepSets eventParDepSets){
        super(bddExtendAutomata);
        this.eventParDepSets = eventParDepSets;
    }
    
    public BDDExDisjEventDepSets getEventParDepSets(){
        return eventParDepSets;
    }
}
