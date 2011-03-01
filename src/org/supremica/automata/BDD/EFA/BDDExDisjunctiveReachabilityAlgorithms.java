package org.supremica.automata.BDD.EFA;

import gnu.trove.TIntArrayList;
import gnu.trove.TIntObjectHashMap;
import java.util.ArrayList;
import java.util.List;
import net.sf.javabdd.BDD;
import static org.supremica.automata.BDD.EFA.ReachabilityUtilities.image;
import static org.supremica.automata.BDD.EFA.ReachabilityUtilities.preImage;
import static org.supremica.automata.BDD.EFA.ReachabilityUtilities.restrictedImage;
import static org.supremica.automata.BDD.EFA.ReachabilityUtilities.restrictedPreImage;

/**
 *
 * @author zhennan
 *
 * This class uses no heuristics, which is intended to test and compare the performance to the one with heuristics
 * The code has been tested for several examples built in Supremica
 */

public class BDDExDisjunctiveReachabilityAlgorithms {

    public static BDD forwardWorkSetAlgorithm(BDDExtendedAutomata bddAutomata, BDD initialStates) {

        List<BDDExDisjunctiveDependentSet> dependentSets = new ArrayList<BDDExDisjunctiveDependentSet>();
        TIntObjectHashMap<BDDExDisjunctiveDependentSet> autIndex2DependentSetMap = bddAutomata.getAutIndex2DependentSet();
        int[] automataIndices = autIndex2DependentSetMap.keys();

        for(int i = 0; i < automataIndices.length; i++)
            dependentSets.add(autIndex2DependentSetMap.get(automataIndices[i]));

        BDD reachableStatesBDD = initialStates.id();
        BDD previousReachableStatesBDD = null;
        BDDExDisjunctiveDependentSet currentDependentSet;

        for (int i = 0; i < dependentSets.size(); i++) {
            currentDependentSet = dependentSets.get(i);
            dependentSets.remove(currentDependentSet);

            BDD curTransitionRelation = currentDependentSet.getPartialForwardTransitionRelation();
            previousReachableStatesBDD = reachableStatesBDD.id();
            reachableStatesBDD = image(bddAutomata, reachableStatesBDD, curTransitionRelation);

            if (!reachableStatesBDD.equals(previousReachableStatesBDD)) {
                TIntArrayList dependent = currentDependentSet.getEventDependentAutomata();
                for (int index = 0; index < dependent.size(); index++) {
                    if (!dependentSets.contains(autIndex2DependentSetMap.get(dependent.get(index)))) {
                        dependentSets.add(autIndex2DependentSetMap.get(dependent.get(index)));
                    }
                }
            }
            i--;
        }
        previousReachableStatesBDD.free();

        return reachableStatesBDD;
    }

     public static BDD backwardWorkSetAlgorithm(BDDExtendedAutomata bddAutomata, BDD markedStates) {
        List<BDDExDisjunctiveDependentSet> dependentSets = new ArrayList<BDDExDisjunctiveDependentSet>();
        TIntObjectHashMap<BDDExDisjunctiveDependentSet> autIndex2DependentSetMap = bddAutomata.getAutIndex2DependentSet();
        int[] automataIndices = autIndex2DependentSetMap.keys();

        for(int i = 0; i < automataIndices.length; i++)
            dependentSets.add(autIndex2DependentSetMap.get(automataIndices[i]));

        BDD coreachableStatesBDD = markedStates.id();
        BDD previousCoreachableStatesBDD = null;
        BDDExDisjunctiveDependentSet currentDependentSet;

        for (int i = 0; i < dependentSets.size(); i++) {
            currentDependentSet = dependentSets.get(i);
            dependentSets.remove(currentDependentSet);

            previousCoreachableStatesBDD = coreachableStatesBDD.id();
            BDD curTransitionRelation = currentDependentSet.getPartialBackwardTransitionRelation();
            coreachableStatesBDD = preImage(bddAutomata, coreachableStatesBDD, curTransitionRelation);

            if (!coreachableStatesBDD.equals(previousCoreachableStatesBDD)) {
                TIntArrayList dependent = currentDependentSet.getEventDependentAutomata();
                for (int index = 0; index < dependent.size(); index++) {
                    if (!dependentSets.contains(autIndex2DependentSetMap.get(dependent.get(index)))) {
                        dependentSets.add(autIndex2DependentSetMap.get(dependent.get(index)));
                    }
                }
            }
            i--;
        }
        previousCoreachableStatesBDD.free();
        return coreachableStatesBDD;
    }

    /* Find the reachable states while excluding the forbidden states*/
    public static BDD restrictedForwardWorkSetAlgorithm(BDDExtendedAutomata bddAutomata, BDD initialStates, BDD forbidenStatesBDD) {
        BDD reachableStatesBDD = internalRestrictedForward(bddAutomata, initialStates, forbidenStatesBDD);
        BDD previousReachablestates = null;

        do{
            previousReachablestates = reachableStatesBDD.id();
            reachableStatesBDD = internalRestrictedForward(bddAutomata, reachableStatesBDD, forbidenStatesBDD);
        }while(!reachableStatesBDD.equals(previousReachablestates));

        previousReachablestates.free();
        return reachableStatesBDD;
    }

    private static BDD internalRestrictedForward(BDDExtendedAutomata bddAutomata, BDD initialStates, BDD forbidenStatesBDD){
        List<BDDExDisjunctiveDependentSet> dependentSets = new ArrayList<BDDExDisjunctiveDependentSet>();
        TIntObjectHashMap<BDDExDisjunctiveDependentSet> autIndex2DependentSetMap = bddAutomata.getAutIndex2DependentSet();
        int[] automataIndices = autIndex2DependentSetMap.keys();

        for(int i = 0; i < automataIndices.length; i++)
            dependentSets.add(autIndex2DependentSetMap.get(automataIndices[i]));

        BDD reachableStatesBDD = initialStates.id();
        BDD previousReachableStatesBDD = null;
        BDDExDisjunctiveDependentSet currentDependentSet;

        for (int i = 0; i < dependentSets.size(); i++) {

            currentDependentSet = dependentSets.get(i);
            dependentSets.remove(currentDependentSet);

            BDD curTransitionRelation = currentDependentSet.getPartialForwardTransitionRelation();

            previousReachableStatesBDD = reachableStatesBDD.id();
            reachableStatesBDD = restrictedImage(bddAutomata, reachableStatesBDD, forbidenStatesBDD, curTransitionRelation);

            if (!reachableStatesBDD.equals(previousReachableStatesBDD)) {
                TIntArrayList dependent = currentDependentSet.getEventDependentAutomata();
                for (int index = 0; index < dependent.size(); index++) {
                    if (!dependentSets.contains(autIndex2DependentSetMap.get(dependent.get(index)))) {
                        dependentSets.add(autIndex2DependentSetMap.get(dependent.get(index)));
                    }
                }
            }
            i--;
        }
        previousReachableStatesBDD.free();

        return reachableStatesBDD;
    }

    public static BDD restrictedBackwardWorkSetAlgorithm(BDDExtendedAutomata bddAutomata, BDD markedStates, BDD forbiddenStates, BDD reachableStates) {
        BDD coreachableStatesBDD = internalRestrictedBackward(bddAutomata, markedStates, forbiddenStates, reachableStates);
        BDD previousCoReachablestatesBDD = null;

        do{
            previousCoReachablestatesBDD = coreachableStatesBDD.id();
            coreachableStatesBDD = internalRestrictedBackward(bddAutomata, coreachableStatesBDD, forbiddenStates, reachableStates);
        }while(!coreachableStatesBDD.equals(previousCoReachablestatesBDD));

        previousCoReachablestatesBDD.free();
        return coreachableStatesBDD;
    }

    public static BDD internalRestrictedBackward(BDDExtendedAutomata bddAutomata, BDD markedStates, BDD forbiddenStates, BDD reachableStates) {
                List<BDDExDisjunctiveDependentSet> dependentSets = new ArrayList<BDDExDisjunctiveDependentSet>();
        TIntObjectHashMap<BDDExDisjunctiveDependentSet> autIndex2DependentSetMap = bddAutomata.getAutIndex2DependentSet();
        int[] automataIndices = autIndex2DependentSetMap.keys();

        for(int i = 0; i < automataIndices.length; i++)
            dependentSets.add(autIndex2DependentSetMap.get(automataIndices[i]));

        BDD coreachableStatesBDD = markedStates.and(reachableStates);
        BDD previousCoreachableStatesBDD = null;
        BDDExDisjunctiveDependentSet currentDependentSet;

        for (int i = 0; i < dependentSets.size(); i++) {

            currentDependentSet = dependentSets.get(i);
            dependentSets.remove(currentDependentSet);

            previousCoreachableStatesBDD = coreachableStatesBDD.id();
            BDD curTransitionRelation = currentDependentSet.getPartialBackwardTransitionRelation();
            coreachableStatesBDD = restrictedPreImage(bddAutomata, coreachableStatesBDD, forbiddenStates, curTransitionRelation);
            coreachableStatesBDD = coreachableStatesBDD.and(reachableStates);

            if (!coreachableStatesBDD.equals(previousCoreachableStatesBDD)) {
                TIntArrayList dependent = currentDependentSet.getEventDependentAutomata();
                for (int index = 0; index < dependent.size(); index++) {
                    if (!dependentSets.contains(autIndex2DependentSetMap.get(dependent.get(index)))) {
                        dependentSets.add(autIndex2DependentSetMap.get(dependent.get(index)));
                    }
                }
            }
            i--;
        }
        previousCoreachableStatesBDD.free();
        return coreachableStatesBDD;
    }

    public static BDD uncontrollableBackwardWorkSetAlgorithm(BDDExtendedAutomata bddAutomata, BDD forbidenStates) {
        BDD forbiddenStatesBDD = internalUncontrollableBackward(bddAutomata, forbidenStates);
        BDD previousFobiddenStatesBDD = null;

        do{
            previousFobiddenStatesBDD = forbiddenStatesBDD.id();
            forbiddenStatesBDD = internalUncontrollableBackward(bddAutomata, forbiddenStatesBDD);
        }while(!forbiddenStatesBDD.equals(previousFobiddenStatesBDD));

        previousFobiddenStatesBDD.free();
        return forbiddenStatesBDD;
    }

     public static BDD internalUncontrollableBackward(BDDExtendedAutomata bddAutomata, BDD forbidenStates) {

        List<BDDExDisjunctiveDependentSet> dependentSets = new ArrayList<BDDExDisjunctiveDependentSet>();
        TIntObjectHashMap<BDDExDisjunctiveDependentSet> autIndex2DependentSetMap = bddAutomata.getAutIndex2DependentSet();
        int[] automataIndices = autIndex2DependentSetMap.keys();

        for(int i = 0; i < automataIndices.length; i++)
            dependentSets.add(autIndex2DependentSetMap.get(automataIndices[i]));

        BDD currentForbiddenStatesBDD = forbidenStates.id();
        BDD previousFobiddenStates = null;
        BDDExDisjunctiveDependentSet currentDependentSet;

        for (int i = 0; i < dependentSets.size(); i++) {
            currentDependentSet = dependentSets.get(i);
            dependentSets.remove(currentDependentSet);

            previousFobiddenStates = currentForbiddenStatesBDD.id();
            BDD curTransitionRelation = currentDependentSet.getUncontrollableBackwardTransitionRelation();
            currentForbiddenStatesBDD = preImage(bddAutomata, currentForbiddenStatesBDD, curTransitionRelation);

            if (!currentForbiddenStatesBDD.equals(previousFobiddenStates)) {
                TIntArrayList dependent = currentDependentSet.getEventDependentAutomata();
                for (int index = 0; index < dependent.size(); index++) {
                    if (!dependentSets.contains(autIndex2DependentSetMap.get(dependent.get(index)))) {
                        dependentSets.add(autIndex2DependentSetMap.get(dependent.get(index)));
                    }
                }
            }
            i--;
        }
        previousFobiddenStates.free();
        return currentForbiddenStatesBDD;
     }
}
