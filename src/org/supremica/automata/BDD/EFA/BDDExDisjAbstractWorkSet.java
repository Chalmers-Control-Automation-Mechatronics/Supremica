package org.supremica.automata.BDD.EFA;

import gnu.trove.TIntHashSet;
import gnu.trove.TIntObjectHashMap;

/**
 * A helper class of which the purpose is to help the fix-point computation to find the most useful component (Event-based, 
 * Automaton-based or Variable-based). 
 * 
 * @author zhennan
 * @version 1.0 
 */
public abstract class BDDExDisjAbstractWorkSet {

    /** A reference to one of three depSets. */
    protected BDDExDisjDepSets depSets;
    
    /** 
     * The size of the work set is different depending on which partitioning technique to choose 
     * 1. For event-based partitioning, size = the number of events; 
     * 2. For automaton-based partitioning, size = the number of automata;
     * 3. For variable-based partitioning, size = the number of variables. 
     */
    protected int size;
    
    /** 
     * If work set [i] == true, it means that the component of which the index is equal to i is in the work set. 
     * Otherwise, it isn't.
     */
    protected boolean[] workset;
    
    /** The field is supposed to keep track of the number of components in the work set. */
    protected int worksetCount;
    
    /** At the beginning,  special actions need performing */
    protected boolean whetherFirst;
    
    /** Whether the work set is involved in a forward or backward search */
    boolean forward;
    
    /** When the number of components chosen by the heuristics > 1, put them into this field. 
     *  Besides, this field is used to choose other components if the chosen one does not cause
     *  the temporary result change during the fix-point computation.
     */
    protected int[] selectedCandidate;
    
    /** Reinforcement learning plus Tabu search to find the most useful component from selectedCandidate. 
     *  Now, only simple version is applied. Thus it can be improved at the later stage. 
     */
    protected ReinforcementLearningPlusTabuSearch rl;
    
    /** Final choice */
    protected int choice;
    
    /** The choice component has an influenced value which can be used to pick the next component */
    protected int choiceWithInfluencedValue;
    
    /** Other options can be chosen as the initial component (only if the choice component does not change the temporary result during the 
     *  fix-point computation) 
     */
    protected TIntObjectHashMap<TIntHashSet> firstAlternatives;
    
    /** Other options can be chosen as the subsequent component */
    protected TIntObjectHashMap <TIntHashSet> oldSubsequentAlternatives;
    
    /** Other options can be chosen as the subsequent component */
    protected TIntObjectHashMap <TIntHashSet> newSubsequentAlternatives;
    
    /** Keep track of whether the temporary result changed due to the choice */
    boolean changed;
   
    /** Abstract constructor */
    protected BDDExDisjAbstractWorkSet(BDDExDisjDepSets depSets, int size) {
        this.depSets = depSets;
        this.size = size;
        this.forward = true;
        this.workset = new boolean[size];
        this.selectedCandidate = new int[size];
        this.worksetCount = size;
        this.rl = ReinforcementLearningPlusTabuSearch.getInstance();
        rl.setSize(size);
        this.choice = -1;
        this.choiceWithInfluencedValue = -1;
        this.firstAlternatives = new TIntObjectHashMap<TIntHashSet>();
        this.changed = false;
        this.oldSubsequentAlternatives = new TIntObjectHashMap<TIntHashSet>();
        this.newSubsequentAlternatives = new TIntObjectHashMap<TIntHashSet>();
    }
    
    /* Pick the most useful component based on different partitioning types
       When run the fix-point computation, special actions need performing. 
     */
    protected abstract int pickOne(boolean forward);

    /* Whether the chosen component made the temporary result change during the fix-point computation */
    public void advance(int componentIndex, boolean changed) {
        this.changed = changed;
        workset[componentIndex] = false;
        worksetCount--;
        record_change(componentIndex);
        rl.advance(componentIndex, changed);
    }
    
    /* Record changes  based on different partitioning types*/
    protected abstract void record_change(int componentIndex);

    /** Reset the work set which the count is the value of size and all components are in it. */
    public void reset() {
        worksetCount = size;
        choice = -1;
        choiceWithInfluencedValue = -1;
        for (int i = 0; i < size; i++) {
            workset[i] = true;
        }
        changed = false;
        firstAlternatives.clear();
        oldSubsequentAlternatives.clear();
        newSubsequentAlternatives.clear();
        rl.reset();
    }

    /** Detect whether the work set is empty or not. */
    public boolean empty() {
        return worksetCount <= 0;
    }
    
    public void setStart() {
        whetherFirst = true;
    }
}
