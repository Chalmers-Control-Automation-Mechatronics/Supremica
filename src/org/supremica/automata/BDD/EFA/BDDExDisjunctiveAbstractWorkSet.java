package org.supremica.automata.BDD.EFA;

import gnu.trove.TIntArrayList;
import gnu.trove.TIntObjectHashMap;

/**
 *
 * @author zhennan
 *
 */
public abstract class BDDExDisjunctiveAbstractWorkSet {

    protected BDDExtendedAutomata bddExAutomata;
    protected TIntObjectHashMap<BDDExDisjunctiveDependentSet> autIndex2DepMap;
    protected int size;
    protected int[] workset;
    protected int worksetCount;
    protected int[] selectedAutomata;
    protected int choice;
    protected int track_advanced, track_not_advanced;
    protected ReinforcementLearningPlusTabuSearch rl;

    protected BDDExDisjunctiveAbstractWorkSet(BDDExtendedAutomata bddAutomata) {
        this.bddExAutomata = bddAutomata;
        this.autIndex2DepMap = bddAutomata.getAutIndex2DependentSet();
        this.size = autIndex2DepMap.keys().length;
        this.workset = new int[size];
        this.selectedAutomata = new int[size];
        this.worksetCount = size;
        this.rl = ReinforcementLearningPlusTabuSearch.getInstance();
        rl.setSize(size);
    }

    public abstract void initialize();
    public abstract int pickOne(boolean first);
    public abstract void record_change(int automaton, TIntArrayList executedEventIndexList);

    public void reset() {
        worksetCount = size;
        choice = -1;
        for (int i = 0; i < size; i++) {
            workset[i] = 1;
        }
        track_advanced = track_not_advanced = 0;
        rl.reset();
    }

    public void advance(int automaton, boolean changed, TIntArrayList executedEventIndexList) {
        workset[automaton] = 0;
        worksetCount--;
        if (changed) {
            record_change(automaton, executedEventIndexList);
        }
        rl.advance(automaton, changed);
        keep_track(changed);
    }

    private void keep_track(boolean changed) {
        if (changed) {
            track_advanced++;
            track_not_advanced = 0;
        } else {
            track_advanced = 0;
            track_not_advanced++;
        }
    }

    public boolean empty() {
        return worksetCount <= 0;
    }

}
