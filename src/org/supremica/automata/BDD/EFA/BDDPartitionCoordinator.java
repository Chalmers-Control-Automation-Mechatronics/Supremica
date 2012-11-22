package org.supremica.automata.BDD.EFA;

/**
 * A helper class of which the purpose is to help the fix-point computation to find the most useful component 
 * (Event-based partitions for EFAs and Automaton-based for DFAs). 
 * 
 * @author  Zhennan
 * @version 2.0 
 */
public abstract class BDDPartitionCoordinator {

    /** 
     * The reference to the instance of BDDPartitionSet. 
     */
    protected BDDPartitionSet partitions;  
    
    protected BDDPartitionCoordinator(BDDPartitionSet partitions) {
        this.partitions = partitions;
    }

    /** 
     * Pick the most useful component based on different partitioning types.
     */
    protected abstract int pickOne(boolean forForward);

    /** 
     * Record whether the chosen component made the temporary result change during the fix-point computation. 
     */
    protected abstract void advance(boolean forward, boolean changed);

    /** 
     * Reset the work set which the count is the value of size and all components are in it. 
     */
    public abstract void reset();

    /** 
     * Detect whether the coordinator has some components. 
     */
    public abstract boolean empty();
 
}
