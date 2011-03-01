package org.supremica.automata.BDD.EFA;

import gnu.trove.TIntArrayList;

/**
 *
 * @author zhennan
 */
public class BDDDisjEventWorkSet extends BDDExDisjunctiveAbstractWorkSet {

    private  int[][] dependentMatrix;
    public BDDDisjEventWorkSet(BDDExtendedAutomata bddExAutomata) {
        super(bddExAutomata);
        this.dependentMatrix = new int[size][size];
        initialize();
    }

    @Override
    public final void initialize() {
        for (int i = 0; i < size; i++) {
            TIntArrayList curEventDepAutIndices = autIndex2DepMap.get(i).getEventDependentAutomata();
            int matrixIndex = 1;
            for (int j = 0; j < curEventDepAutIndices.size(); j++) {
                dependentMatrix[i][0] = curEventDepAutIndices.size();
                dependentMatrix[i][matrixIndex] = curEventDepAutIndices.get(j);
                matrixIndex++;
            }
        }
    }

    @Override
    public int pickOne(boolean first) {
        int selectedAutomataSizeFromH1 = 0;
        selectedAutomataSizeFromH1 = h1_most_event_followers();
        choice = rl.choose(selectedAutomata, selectedAutomataSizeFromH1);
        return choice;
    }

    private int h1_most_event_followers() {
        int selectedAutomataSize = 0;
        int best = 0;
        for (int i = 0; i < size; i++) {
            if (workset[i] > 0) {
                int c = dependentMatrix[i][0];
                if (best < c) {
                    best = c;
                    selectedAutomataSize = 0;
                }
                if (best == c) {
                    selectedAutomata[selectedAutomataSize++] = i;
                }
            }
        }
        return selectedAutomataSize;
    }

    @Override
    public void record_change(int automaton, TIntArrayList executedEventIndexList) {
        int count = dependentMatrix[automaton][0];
        for (int i = 1; i <= count; i++) {
            int a = dependentMatrix[automaton][i];
            if (workset[a] == 0) {
                worksetCount++;
            }
            workset[a]++;
        }
    }
}
