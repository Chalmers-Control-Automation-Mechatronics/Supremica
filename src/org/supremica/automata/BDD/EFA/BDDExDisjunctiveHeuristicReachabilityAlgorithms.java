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

    @SuppressWarnings("unused")
    private static final Logger logger = LoggerFactory
            .createLogger(BDDExDisjunctiveHeuristicReachabilityAlgorithms.class);

    public static BDD forwardWorkSetAlgorithm(final BDDExtendedAutomata bddAutomata, final BDD initialStates, final BDD forbiddenStates) {

        final BDDExDisjunctiveAbstractWorkSet variableWorkSet = new BDDDisjVariableWorkSet(bddAutomata);
        final TIntObjectHashMap<BDDExDisjunctiveDependentSet> autIndex2DepSetMap = bddAutomata.getAutIndex2DependentSet();
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

    private static BDD internalForwardWorkSetAlgorithm(final BDDExtendedAutomata bddAutomata,
            final BDDExDisjunctiveAbstractWorkSet variableWorkSet,
            final TIntObjectHashMap<BDDExDisjunctiveDependentSet> autIndex2DepSetMap,
            BDD reachableStatesBDD, final BDD forbiddenStates, boolean firstRound) {

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

            final BDDExDisjunctiveDependentSet curDependentSet = autIndex2DepSetMap.get(choice);
            final BDD curTransition = curDependentSet.getPartialForwardTransition();


            final TIntArrayList executedEventIndexList = new TIntArrayList();
            BDD nextStatesOfCurTransition;
            BDD previousReachableStatesOfCurTransition;
            do {
                previousReachableStatesOfCurTransition = reachableStatesBDD.id();
                final BDD nextStatesWithEvents = reachableStatesBDD.and(curTransition).exist(bddAutomata.sourceStateVariables);
                final BDD executedEventsBDD = nextStatesWithEvents.id().exist(bddAutomata.destStateVariables);
                final Set<BDD> eventBDDSet = bddAutomata.eventBDD2eventIndices.keySet();
                for (final Iterator<BDD> eventIterator = eventBDDSet.iterator(); eventIterator.hasNext();) {
                    final BDD anEventBDD = eventIterator.next();
                    final int eventIndex = bddAutomata.eventBDD2eventIndices.get(anEventBDD);
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

    public static BDD backWorkSetAlgorithm(final BDDExtendedAutomata bddAutomata, final BDD markedStates, final BDD reachableStates, final BDD forbiddenStates) {

        final BDDExDisjunctiveAbstractWorkSet variableWorkSet = new BDDDisjVariableWorkSet(bddAutomata);
        final TIntObjectHashMap<BDDExDisjunctiveDependentSet> autIndex2DepSetMap = bddAutomata.getAutIndex2DependentSet();
        final BDD reachableTargetStates = reachableStates.replace(bddAutomata.sourceToDestLocationPairing).replace(bddAutomata.sourceToDestVariablePairing);
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

    private static BDD internalBackwardWorkSetAlgorithm(final BDDExtendedAutomata bddAutomata,
            final BDDExDisjunctiveAbstractWorkSet variableWorkSet,
            final TIntObjectHashMap<BDDExDisjunctiveDependentSet> autIndex2DepSetMap,
            BDD coreachableStatesBDD, final BDD reachableTargetStates, final BDD forbiddenStates, boolean firstRound) {

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

            final BDDExDisjunctiveDependentSet curDependentSet = autIndex2DepSetMap.get(choice);
            final BDD curTransition = curDependentSet.getPartialBackwardTransition();


            final TIntArrayList executedEventIndexList = new TIntArrayList();
            BDD nextStatesOfCurTransition;
            BDD previousCoreachableStatesOfCurTransition;
            do {
                previousCoreachableStatesOfCurTransition = coreachableStatesBDD.id();
                final BDD nextStatesWithEvents = (coreachableStatesBDD.and(curTransition).exist(bddAutomata.sourceStateVariables)).and(reachableTargetStates);
                final BDD executedEventsBDD = nextStatesWithEvents.id().exist(bddAutomata.destStateVariables);
                final Set<BDD> eventBDDSet = bddAutomata.eventBDD2eventIndices.keySet();
                for (final Iterator<BDD> eventIterator = eventBDDSet.iterator(); eventIterator.hasNext();) {
                    final BDD anEventBDD = eventIterator.next();
                    final int eventIndex = bddAutomata.eventBDD2eventIndices.get(anEventBDD);
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

     public static BDD uncontrollableBackWorkSetAlgorithm(final BDDExtendedAutomata bddAutomata, final BDD forbiddenStates, final BDD reachableStates) {

        final BDDExDisjunctiveAbstractWorkSet variableWorkSet = new BDDDisjVariableWorkSet(bddAutomata);
        final TIntObjectHashMap<BDDExDisjunctiveDependentSet> autIndex2DepSetMap = bddAutomata.getAutIndex2DependentSet();
        final BDD reachableTargetStates = reachableStates.replace(bddAutomata.sourceToDestLocationPairing).replace(bddAutomata.sourceToDestVariablePairing);
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
      private static BDD internalUncontrollableBackwardWorkSetAlgorithm(final BDDExtendedAutomata bddAutomata,
            final BDDExDisjunctiveAbstractWorkSet variableWorkSet,
            final TIntObjectHashMap<BDDExDisjunctiveDependentSet> autIndex2DepSetMap,
            final BDD coreachableStatesBDD, final BDD reachableTargetStates, boolean firstRound) {

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

            final BDDExDisjunctiveDependentSet curDependentSet = autIndex2DepSetMap.get(choice);
            final BDD curTransition = curDependentSet.getPartialBackwardTransition().and(bddAutomata.uncontrollableEventsBDD);


            final TIntArrayList executedEventIndexList = new TIntArrayList();
            BDD nextStatesOfCurTransition;
            BDD previousCoreachableStatesOfCurTransition;
            do {
                previousCoreachableStatesOfCurTransition = coreachableStatesBDD.id();
                final BDD nextStatesWithEvents = (coreachableStatesBDD.and(curTransition).exist(bddAutomata.sourceStateVariables)).and(reachableTargetStates);
                final BDD executedEventsBDD = nextStatesWithEvents.id().exist(bddAutomata.destStateVariables);
                final Set<BDD> eventBDDSet = bddAutomata.eventBDD2eventIndices.keySet();
                for (final Iterator<BDD> eventIterator = eventBDDSet.iterator(); eventIterator.hasNext();) {
                    final BDD anEventBDD = eventIterator.next();
                    final int eventIndex = bddAutomata.eventBDD2eventIndices.get(anEventBDD);
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
