package org.supremica.automata.BDD.EFA;

import gnu.trove.TIntArrayList;
import gnu.trove.TIntIntHashMap;
import gnu.trove.TIntIntProcedure;
import gnu.trove.TIntObjectHashMap;

/**
 *
 * @author zhennan
 */
public class BDDDisjVariableWorkSet extends BDDExDisjunctiveAbstractWorkSet{

    private boolean[] affactedAutomata;
    private int[] nbrOfAffactedVariables;
    public BDDDisjVariableWorkSet(final BDDExtendedAutomata bddExAutomata){
        super(bddExAutomata);
        initialize();
    }

    @Override
    public final void initialize() {
        affactedAutomata = new boolean[size];
        nbrOfAffactedVariables = new int[size];
    }

    @Override
    public int pickOne(final boolean first) {
        int selectedAutomataSizeFromH1 = 0;
        if(first)
            selectedAutomataSizeFromH1 = h1_least_guards_of_edges();
        else{
            selectedAutomataSizeFromH1 = h1_most_variable_followers();
        }
        choice = rl.choose(selectedAutomata, selectedAutomataSizeFromH1);
        return choice;
    }

    private int h1_least_guards_of_edges() {
        int selectedAutomataSize = 0;
        int best = Integer.MIN_VALUE;
        for (int i = 0; i < size; i++) {
            final int n = bddExAutomata.automaton2nbrGuards.get(bddExAutomata.theIndexMap.getExAutomatonAt(i));
            final int d = bddExAutomata.automaton2nbrEdges.get(bddExAutomata.theIndexMap.getExAutomatonAt(i));
            int c = 0;
            if (n == 0) {
                c = Integer.MAX_VALUE;
            } else {
                c = d / n;
            }
            if (c > best) {
                best = c;
                selectedAutomataSize = 0;
            }
            if (best == c) {
                selectedAutomata[selectedAutomataSize++] = i;
            }
        }
        return selectedAutomataSize;
    }
    private int h1_most_variable_followers() {
        int selectedAutomataSize = 0;
        double best = -1;
        for (int i = 0; i < size; i++) {
            if (workset[i] > 0) {
                final int n = nbrOfAffactedVariables[i];
                final int d = bddExAutomata.automaton2nbrEdges.get(bddExAutomata.theIndexMap.getExAutomatonAt(i));
                final double c = n/d;
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
    public void record_change(final int automaton, final TIntArrayList executedEventIndexList) {
        for(int i = 0; i < size; i++){
            affactedAutomata[i] = false;
            nbrOfAffactedVariables[i] = 0;
        }
        for (int i = 0; i < executedEventIndexList.size(); i++) {
            final TIntObjectHashMap<TIntIntHashMap> eventIndex2VariableDepAutomata = autIndex2DepMap.get(automaton).getEventIndex2VariableDepAutomata();
            final TIntIntHashMap autIndex2nbrOFInfluencedVariables = eventIndex2VariableDepAutomata.get(executedEventIndexList.get(i));
            if (autIndex2nbrOFInfluencedVariables != null) {
                autIndex2nbrOFInfluencedVariables.forEachEntry(new TIntIntProcedure() {
                    //@Override
                    public boolean execute(final int autIndex, final int nbr) {
                        affactedAutomata[autIndex] = true;
                        nbrOfAffactedVariables[autIndex] += nbr;
                        return true;
                    }
                });
            }
        }

        for (int i = 0; i < size; i++) {
            if (affactedAutomata[i]) {
                if (workset[i] == 0) {
                    worksetCount++;
                }
                workset[i]++;
            }
        }
    }
}
