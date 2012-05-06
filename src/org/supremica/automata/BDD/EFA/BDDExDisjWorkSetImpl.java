package org.supremica.automata.BDD.EFA;

import gnu.trove.TIntHashSet;
import gnu.trove.TIntIntHashMap;
import gnu.trove.TIntIterator;
import gnu.trove.TIntObjectHashMap;
import gnu.trove.TIntProcedure;
import java.util.Arrays;
import java.util.Random;

/**
 * A subclass of BDDExDisjAbstractWorkSet, implementing the abstract work set class. During the fix-point computation, 
 * the event work set can pick the useful component (event index) for the next round
 * 
 * @author zhennan
 * @version  1.0
 */

public class BDDExDisjWorkSetImpl extends BDDExDisjAbstractWorkSet {

    private Random random = new Random();
    
    public BDDExDisjWorkSetImpl(BDDExDisjDepSets depSets, int size) {
        super(depSets, size);
    }

    /** Pick the "right" component 
     * 1. If the algorithm just begins, find a "good" initial component (index)
     * 2. Otherwise, use the component2influencedComponents to find a most influenced component (index)
     */
    @Override
    public int pickOne(boolean forward) {
        this.forward = forward;
        int candidateSize = 0;
        /* CASE 1: when the fix-point computation begins, how to pick the initial component and its BDD. */
        if (whetherFirst) {
            candidateSize = pickFirstComponents();
            if(candidateSize == 0)
                choice = Integer.MAX_VALUE;
            else
                choice = rl.choose(selectedCandidate, candidateSize);
        } else {
            /* CASE 2: For the subsequent choices, use the field component2influencedComponents to choose. */
            candidateSize = pickSubsequentComponents();
            choice = rl.choose(selectedCandidate, candidateSize);
        }
        return choice;
    }

    /** Here is the simple idea to pick the first component when the fix-point algorithm begins: 
     * 1. Find the components of which the BDD expression contain the initial state BDD as the source state;
     * 2. From these components, build the alternatives (called firstAlternatives, 
     * which is a map: [influencedNumOfComponents (component1, component2 ...)]);
     * In other words, pick the component which can influence most components if it is chosen.  
     * 3. Pick the component set which has the largest influenced value and use the second stage heuristics to randomly pick one. 
     */
    private int pickFirstComponents() {

        int candidateSize = 0;
        TIntObjectHashMap<TIntIntHashMap> component2influncedComponentsMap = null;
        TIntHashSet firstComponentCandidates = null;
                
        if (forward) {
            component2influncedComponentsMap = depSets.getForwardComponentToInfluencedComponentMap();
            firstComponentCandidates = depSets.getInitialComponentCandidates();
        } else {
            component2influncedComponentsMap = depSets.getBackwardComponentToInfluencedComponentMap();
            firstComponentCandidates = depSets.getMarkedComponentCandidates();
        }

        if (choice == -1) { // The real first, round number is 0        
            for(TIntIterator compItr = firstComponentCandidates.iterator(); compItr.hasNext();){             
                 int compIndex = compItr.next();
                 TIntIntHashMap influencedComponentsMap = component2influncedComponentsMap.get(compIndex);
                    int influencedSize = 0;
                    int[] influencedComponentIndicesAsKeys = influencedComponentsMap.keys();
                    for (int j = 0; j < influencedComponentIndicesAsKeys.length; j++) {
                        influencedSize += influencedComponentsMap.get(influencedComponentIndicesAsKeys[j]);
                    }
                    if (!firstAlternatives.contains(influencedSize)) {
                        TIntHashSet components = new TIntHashSet();
                        components.add(compIndex);
                        firstAlternatives.put(influencedSize, components);
                    } else {
                        firstAlternatives.get(influencedSize).add(compIndex);
                    }
            }
                   
            int[] sortedKeys = firstAlternatives.keys();
            Arrays.sort(sortedKeys);
            selectedCandidate = ((TIntHashSet) firstAlternatives.get(sortedKeys[sortedKeys.length - 1])).toArray();
            choiceWithInfluencedValue = sortedKeys[sortedKeys.length - 1];
            candidateSize = selectedCandidate.length;
            
            return candidateSize;
        
        } else { // Selected an initial, but the tmp result didn't change. 
            
            firstAlternatives.get(choiceWithInfluencedValue).remove(choice);
            if (firstAlternatives.get(choiceWithInfluencedValue).size() > 0) { // Has other components which have the same influenced value 
                selectedCandidate = firstAlternatives.get(choiceWithInfluencedValue).toArray(); // as the choice ?
                candidateSize = selectedCandidate.length;
                return candidateSize;
            } else { // If not, the firstAlternatives removes this entry and search the second most influenced value (key) 
                firstAlternatives.remove(choiceWithInfluencedValue);
                int[] sortedKeys = firstAlternatives.keys(); // The same routine as before
                Arrays.sort(sortedKeys);
                if (sortedKeys.length == 0) { // If all of locations and values are marked, this will result in 0
                    candidateSize = 0;
                } else {
                    selectedCandidate = ((TIntHashSet) firstAlternatives.get(sortedKeys[sortedKeys.length - 1])).toArray();
                    choiceWithInfluencedValue = sortedKeys[sortedKeys.length - 1];
                    candidateSize = selectedCandidate.length;
                }
                return candidateSize;
            }
        }
    }

    @Override
    public void record_change(int componentIndex) {
        if (changed) {
            if (whetherFirst) { // If the first picked component works, firstAlternatives is useless. 
                whetherFirst = false; // Next, pick the subsequent component.
                firstAlternatives.clear();
            }

            TIntHashSet dependentEventIndexSet = null;
            if (forward) {
                dependentEventIndexSet = depSets.getForwardDependentComponentMap().get(componentIndex);
            } else {
                dependentEventIndexSet = depSets.getBackwardDependentComponentMap().get(componentIndex);
            }

            dependentEventIndexSet.forEach(new TIntProcedure() {

                @Override
                public boolean execute(int dependentEventIndex) {
                    if (!workset[dependentEventIndex]) {
                        workset[dependentEventIndex] = true;
                        worksetCount++;
                    }
                    return true;
                }
            });
            
            // build the newSubsequent alternatives
            newSubsequentAlternatives.clear();
            TIntIntHashMap choiceInfluencedComponentMap = null;
            if (forward) {
                choiceInfluencedComponentMap = depSets.getForwardComponentToInfluencedComponentMap().get(choice);
            } else {
                choiceInfluencedComponentMap = depSets.getBackwardComponentToInfluencedComponentMap().get(choice);
            }
            int[] influencedComponentIndicesAsKeys = choiceInfluencedComponentMap.keys();
            for (int i = 0; i < influencedComponentIndicesAsKeys.length; i++) {
                int influencedValue = choiceInfluencedComponentMap.get(influencedComponentIndicesAsKeys[i]);
                if (!newSubsequentAlternatives.contains(influencedValue)) {
                    TIntHashSet components = new TIntHashSet();
                    components.add(influencedComponentIndicesAsKeys[i]);
                    newSubsequentAlternatives.put(influencedValue, components);
                } else {
                    newSubsequentAlternatives.get(influencedValue).add(influencedComponentIndicesAsKeys[i]);
                }
            }
            oldSubsequentAlternatives = newSubsequentAlternatives;
        }
    }

    private int pickSubsequentComponents() {
        int candidateSize = 0;
        if (changed) { // Yeah, the chosen component works. The new subsequentAlternatives is built. 
            int[] sortedInfluencedValues = newSubsequentAlternatives.keys();
            if(sortedInfluencedValues.length == 0)
                return randomSelection();
            Arrays.sort(sortedInfluencedValues);
            choiceWithInfluencedValue = sortedInfluencedValues[sortedInfluencedValues.length - 1];
            selectedCandidate = newSubsequentAlternatives.get(choiceWithInfluencedValue).toArray();        
            candidateSize = selectedCandidate.length;
        } else if (choiceWithInfluencedValue != -1) { // No, the choice doesn't make the temp result change. Use the old subsequent alternatives
            oldSubsequentAlternatives.get(choiceWithInfluencedValue).remove(choice);
            if (oldSubsequentAlternatives.get(choiceWithInfluencedValue).size() > 0) {
                selectedCandidate = oldSubsequentAlternatives.get(choiceWithInfluencedValue).toArray();
                candidateSize = selectedCandidate.length;
            } else {
                oldSubsequentAlternatives.remove(choiceWithInfluencedValue);
                int[] sortedInfluencedValues = oldSubsequentAlternatives.keys();
                if(sortedInfluencedValues.length == 0){
                    return randomSelection();
                }
                Arrays.sort(sortedInfluencedValues);
                choiceWithInfluencedValue = sortedInfluencedValues[sortedInfluencedValues.length - 1];
                selectedCandidate = oldSubsequentAlternatives.get(choiceWithInfluencedValue).toArray();
                candidateSize = selectedCandidate.length;
            }
        } else {
            return randomSelection();
        }
        return candidateSize;
    }
    
    private int randomSelection()
    {
        int candidateSize = 0; // the size of candidates (should be 1 in this case)
        int comIndex = 0; // the random final choice 
        do {
            comIndex = random.nextInt(size);
        } while (!workset[comIndex]);

        candidateSize = 1;
        selectedCandidate = new int[]{comIndex};
        
        choiceWithInfluencedValue = -1;
        
        // reconstruct the oldSubsequentAlternatives
        oldSubsequentAlternatives.clear();
        TIntIntHashMap influencedComponentToValue = null;
        if (forward) {
            influencedComponentToValue = depSets.getForwardComponentToInfluencedComponentMap().get(comIndex);
        } else {
            influencedComponentToValue = depSets.getBackwardComponentToInfluencedComponentMap().get(comIndex);
        }
        int[] influencedComponentIndicesAsKeys = influencedComponentToValue.keys();
        for (int i = 0; i < influencedComponentIndicesAsKeys.length; i++) {
            int influencedValue = influencedComponentToValue.get(influencedComponentIndicesAsKeys[i]);
            if (!oldSubsequentAlternatives.contains(influencedValue)) {
                TIntHashSet components = new TIntHashSet();
                components.add(influencedComponentIndicesAsKeys[i]);
                oldSubsequentAlternatives.put(influencedValue, components);
            } else {
                oldSubsequentAlternatives.get(influencedValue).add(influencedComponentIndicesAsKeys[i]);
            }
        }
        return candidateSize;
    }
}
