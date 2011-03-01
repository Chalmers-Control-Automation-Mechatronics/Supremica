package org.supremica.automata.BDD.EFA;

import java.util.Set;
import gnu.trove.TIntArrayList;
import java.util.Iterator;
import gnu.trove.TIntObjectHashMap;
import net.sf.javabdd.BDD;
import org.supremica.log.Logger;
import org.supremica.log.LoggerFactory;

/**
 *
 * @author zhennan
 *
 */
public class BDDExDisjunctiveHeuristicReachabilityAlgorithms {

    private static final Logger logger = LoggerFactory
            .createLogger(BDDExDisjunctiveHeuristicReachabilityAlgorithms.class);

    public static BDD forwardWorkSetAlgorithm(BDDExtendedAutomata bddAutomata, BDD initialStates, BDD forbiddenStates) {

        BDDExDisjunctiveAbstractWorkSet variableWorkSet = new BDDDisjVariableWorkSet(bddAutomata);
        TIntObjectHashMap<BDDExDisjunctiveDependentSet> autIndex2DepSetMap = bddAutomata.getAutIndex2DependentSet();
        BDD reachableStatesBDD = initialStates.and(forbiddenStates.not());
        BDD previousReachableStatesBDD = null;
        boolean firstRound = true;

        do {
            variableWorkSet.reset();
            previousReachableStatesBDD = reachableStatesBDD.id();
            reachableStatesBDD = internalForwardWorkSetAlgorithm(bddAutomata, variableWorkSet, autIndex2DepSetMap, reachableStatesBDD, forbiddenStates,firstRound);
            if (firstRound) {
                firstRound = false;
            }
        } while (!previousReachableStatesBDD.equals(reachableStatesBDD));

        return reachableStatesBDD;
    }

    private static BDD internalForwardWorkSetAlgorithm(BDDExtendedAutomata bddAutomata,
            BDDExDisjunctiveAbstractWorkSet variableWorkSet,
            TIntObjectHashMap<BDDExDisjunctiveDependentSet> autIndex2DepSetMap,
            BDD reachableStatesBDD, BDD forbiddenStates, boolean firstRound) {

        BDD previousReachableStatesBDD = null;
        while (!variableWorkSet.empty()) {
            previousReachableStatesBDD = reachableStatesBDD.id();
            int choice = -1;
            if (firstRound) {
                choice = variableWorkSet.pickOne(firstRound);
                firstRound = false;
            } else {
                choice = variableWorkSet.pickOne(firstRound);
            }

            BDDExDisjunctiveDependentSet curDependentSet = autIndex2DepSetMap.get(choice);
            BDD curTransition = curDependentSet.getPartialForwardTransition();

            
            TIntArrayList executedEventIndexList = new TIntArrayList();
            BDD nextStatesOfCurTransition;
            BDD previousReachableStatesOfCurTransition;
            do {
                previousReachableStatesOfCurTransition = reachableStatesBDD.id();
                BDD nextStatesWithEvents = reachableStatesBDD.and(curTransition).exist(bddAutomata.sourceStateVariables);
                BDD executedEventsBDD = nextStatesWithEvents.id().exist(bddAutomata.destStateVariables);
                Set<BDD> eventBDDSet = bddAutomata.eventBDD2eventIndices.keySet();
                for (Iterator<BDD> eventIterator = eventBDDSet.iterator(); eventIterator.hasNext();) {
                    BDD anEventBDD = eventIterator.next();
                    int eventIndex = bddAutomata.eventBDD2eventIndices.get(anEventBDD);
                    if (!executedEventsBDD.and(anEventBDD).equals(bddAutomata.manager.getZeroBDD())
                            && !executedEventIndexList.contains(eventIndex)) {
                        executedEventIndexList.add(eventIndex);
                    }
                }
                nextStatesOfCurTransition = nextStatesWithEvents.exist(bddAutomata.getEventVarSet()).replace(bddAutomata.destToSourceLocationPairing).replace(bddAutomata.destToSourceVariablePairing);
                reachableStatesBDD = reachableStatesBDD.or(nextStatesOfCurTransition.and(forbiddenStates.not()));
            } while (!reachableStatesBDD.equals(previousReachableStatesOfCurTransition));

            variableWorkSet.advance(choice, !reachableStatesBDD.equals(previousReachableStatesBDD),
                    executedEventIndexList);
        }
        return reachableStatesBDD;
    }

    public static BDD backWorkSetAlgorithm(BDDExtendedAutomata bddAutomata, BDD markedStates, BDD reachableStates, BDD forbiddenStates) {

        BDDExDisjunctiveAbstractWorkSet variableWorkSet = new BDDDisjVariableWorkSet(bddAutomata);
        TIntObjectHashMap<BDDExDisjunctiveDependentSet> autIndex2DepSetMap = bddAutomata.getAutIndex2DependentSet();
        BDD reachableTargetStates = reachableStates.replace(bddAutomata.sourceToDestLocationPairing).replace(bddAutomata.sourceToDestVariablePairing);
        BDD coreachableStatesBDD = markedStates.and(reachableStates).and(forbiddenStates.not());
        
        BDD previousCoreachableStatesBDD = null;
        boolean firstRound = true;
        do {
            variableWorkSet.reset();
            previousCoreachableStatesBDD = coreachableStatesBDD.id();
            coreachableStatesBDD = internalBackwardWorkSetAlgorithm(bddAutomata, variableWorkSet,
                    autIndex2DepSetMap, coreachableStatesBDD, reachableTargetStates, forbiddenStates, firstRound);
            if (firstRound) {
                firstRound = false;
            }
        } while (!previousCoreachableStatesBDD.equals(coreachableStatesBDD));
        return coreachableStatesBDD;
    }

    private static BDD internalBackwardWorkSetAlgorithm(BDDExtendedAutomata bddAutomata,
            BDDExDisjunctiveAbstractWorkSet variableWorkSet,
            TIntObjectHashMap<BDDExDisjunctiveDependentSet> autIndex2DepSetMap,
            BDD coreachableStatesBDD, BDD reachableTargetStates, BDD forbiddenStates, boolean firstRound) {

        BDD previousCoreachableStatesBDD = null;
        while (!variableWorkSet.empty()) {
            previousCoreachableStatesBDD = coreachableStatesBDD.id();
            int choice = -1;
            if (firstRound) {
                choice = variableWorkSet.pickOne(firstRound);
                firstRound = false;
            } else {
                choice = variableWorkSet.pickOne(firstRound);
            }

            BDDExDisjunctiveDependentSet curDependentSet = autIndex2DepSetMap.get(choice);
            BDD curTransition = curDependentSet.getPartialBackwardTransition();


            TIntArrayList executedEventIndexList = new TIntArrayList();
            BDD nextStatesOfCurTransition;
            BDD previousCoreachableStatesOfCurTransition;
            do {
                previousCoreachableStatesOfCurTransition = coreachableStatesBDD.id();
                BDD nextStatesWithEvents = (coreachableStatesBDD.and(curTransition).exist(bddAutomata.sourceStateVariables)).and(reachableTargetStates);
                BDD executedEventsBDD = nextStatesWithEvents.id().exist(bddAutomata.destStateVariables);
                Set<BDD> eventBDDSet = bddAutomata.eventBDD2eventIndices.keySet();
                for (Iterator<BDD> eventIterator = eventBDDSet.iterator(); eventIterator.hasNext();) {
                    BDD anEventBDD = eventIterator.next();
                    int eventIndex = bddAutomata.eventBDD2eventIndices.get(anEventBDD);
                    if (!executedEventsBDD.and(anEventBDD).equals(bddAutomata.manager.getZeroBDD())
                            && !executedEventIndexList.contains(eventIndex)) {
                        executedEventIndexList.add(eventIndex);
                    }
                }
                nextStatesOfCurTransition = nextStatesWithEvents.exist(bddAutomata.getEventVarSet())
                        .replaceWith(bddAutomata.destToSourceLocationPairing).replaceWith(bddAutomata.destToSourceVariablePairing);
                coreachableStatesBDD = coreachableStatesBDD.or(nextStatesOfCurTransition.and(forbiddenStates.not()));
            } while (!coreachableStatesBDD.equals(previousCoreachableStatesOfCurTransition));

            variableWorkSet.advance(choice, !coreachableStatesBDD.equals(previousCoreachableStatesBDD),
                    executedEventIndexList);
        }
        return coreachableStatesBDD;
    }

     public static BDD uncontrollableBackWorkSetAlgorithm(BDDExtendedAutomata bddAutomata, BDD forbiddenStates, BDD reachableStates) {

        BDDExDisjunctiveAbstractWorkSet variableWorkSet = new BDDDisjVariableWorkSet(bddAutomata);
        TIntObjectHashMap<BDDExDisjunctiveDependentSet> autIndex2DepSetMap = bddAutomata.getAutIndex2DependentSet();
        BDD reachableTargetStates = reachableStates.replace(bddAutomata.sourceToDestLocationPairing).replace(bddAutomata.sourceToDestVariablePairing);
        BDD coreachableStatesBDD = forbiddenStates.and(reachableStates);

        BDD previousCoreachableStatesBDD = null;
        boolean firstRound = true;
        do {
            variableWorkSet.reset();
            previousCoreachableStatesBDD = coreachableStatesBDD.id();
            coreachableStatesBDD = internalUncontrollableBackwardWorkSetAlgorithm(bddAutomata, variableWorkSet,
                    autIndex2DepSetMap, coreachableStatesBDD, reachableTargetStates, firstRound);
            if (firstRound) {
                firstRound = false;
            }
        } while (!previousCoreachableStatesBDD.equals(coreachableStatesBDD));
        return coreachableStatesBDD;
    }
      private static BDD internalUncontrollableBackwardWorkSetAlgorithm(BDDExtendedAutomata bddAutomata,
            BDDExDisjunctiveAbstractWorkSet variableWorkSet,
            TIntObjectHashMap<BDDExDisjunctiveDependentSet> autIndex2DepSetMap,
            BDD coreachableStatesBDD, BDD reachableTargetStates, boolean firstRound) {

        BDD previousCoreachableStatesBDD = null;
        while (!variableWorkSet.empty()) {
            previousCoreachableStatesBDD = coreachableStatesBDD.id();
            int choice = -1;
            if (firstRound) {
                choice = variableWorkSet.pickOne(firstRound);
                firstRound = false;
            } else {
                choice = variableWorkSet.pickOne(firstRound);
            }

            BDDExDisjunctiveDependentSet curDependentSet = autIndex2DepSetMap.get(choice);
            BDD curTransition = curDependentSet.getPartialBackwardTransition().and(bddAutomata.uncontrollableEventsBDD);


            TIntArrayList executedEventIndexList = new TIntArrayList();
            BDD nextStatesOfCurTransition;
            BDD previousCoreachableStatesOfCurTransition;
            do {
                previousCoreachableStatesOfCurTransition = coreachableStatesBDD.id();
                BDD nextStatesWithEvents = (coreachableStatesBDD.and(curTransition).exist(bddAutomata.sourceStateVariables)).and(reachableTargetStates);
                BDD executedEventsBDD = nextStatesWithEvents.id().exist(bddAutomata.destStateVariables);
                Set<BDD> eventBDDSet = bddAutomata.eventBDD2eventIndices.keySet();
                for (Iterator<BDD> eventIterator = eventBDDSet.iterator(); eventIterator.hasNext();) {
                    BDD anEventBDD = eventIterator.next();
                    int eventIndex = bddAutomata.eventBDD2eventIndices.get(anEventBDD);
                    if (!executedEventsBDD.and(anEventBDD).equals(bddAutomata.manager.getZeroBDD())
                            && !executedEventIndexList.contains(eventIndex)) {
                        executedEventIndexList.add(eventIndex);
                    }
                }
                nextStatesOfCurTransition = nextStatesWithEvents.exist(bddAutomata.getEventVarSet())
                        .replaceWith(bddAutomata.destToSourceLocationPairing).replaceWith(bddAutomata.destToSourceVariablePairing);
                coreachableStatesBDD.orWith(nextStatesOfCurTransition);
            } while (!coreachableStatesBDD.equals(previousCoreachableStatesOfCurTransition));

            variableWorkSet.advance(choice, !coreachableStatesBDD.equals(previousCoreachableStatesBDD),
                    executedEventIndexList);
        }
        return coreachableStatesBDD;
    }
}
