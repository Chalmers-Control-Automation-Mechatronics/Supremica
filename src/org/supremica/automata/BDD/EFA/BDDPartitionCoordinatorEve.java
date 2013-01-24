package org.supremica.automata.BDD.EFA;

import gnu.trove.TIntHashSet;
import gnu.trove.TIntIterator;
import gnu.trove.TIntObjectHashMap;


/**
 * Regarding the event-based partitioning technique, exclusively for extended finite automata,
 * the class implements the abstract class BDDPartitionCoordinator.
 *
 * @author  Zhennan
 * @version 2.0
 */


public final class BDDPartitionCoordinatorEve extends BDDPartitionCoordinator{

    /**
     * At the beginning,  special actions need performing.
     */
    private boolean firstTime;

    /**
     * A set of event indices which can be used as the first chosen event index
     */
    private TIntHashSet firstCandidateEventIndexSet;

    /**
     * When the number of components chosen by the heuristics is not single, put them into this field.
     * Besides, this field is used to choose another component if the chosen one does not cause the
     * temporary result change during the fix-point computation.
     */
    private final TIntHashSet candidateEventIndexSet;

    private final TIntHashSet activeEventIndexSet;

    /**
     * Reinforcement learning plus Tabu search to find the most useful component from selectedCandidate.
     * Now, only simple version is applied. Thus it can be improved at the later stage.
     */
    private final BDDPartitionHeuristicsLearner learner;

    /** Final choice */
    private int choice;
    //private int previous_choice;

    public BDDPartitionCoordinatorEve (final BDDPartitionSet eventPartitions) {
        super(eventPartitions);

        if(!(eventPartitions instanceof BDDPartitionSetEve)) {
            throw  new IllegalArgumentException("eventPartitions must be an instance of BDDPartitionSetEve");
        }
        learner = new BDDPartitionHeuristicsLearner(partitions.orgAutomata.unionAlphabet.size());
        activeEventIndexSet = new TIntHashSet(partitions.getCompIndexToCompBDDMap().keys());
        candidateEventIndexSet = new TIntHashSet();
        reset();
    }

    @Override
    public int pickOne(final boolean forForward) {
		// When the fix-point computations (forward or backward) start, how to pick one first partition.
		if (firstTime) {
			buildFirstCandidates(forForward); // update selectedCandidate array
		} else {
			buildSubsequentCandidates(forForward); // update selectedCandidate array
		}
		choice = learner.choose(candidateEventIndexSet.toArray(), candidateEventIndexSet.size());
        // previous_choice = choice;
		return choice;
    }

    private void buildFirstCandidates(final boolean forForward) {
        TIntObjectHashMap<TIntHashSet> eventIndex2DepEventIndexSetMap;

        if (forForward) {
            if (choice == -1) {
                firstCandidateEventIndexSet = partitions.getInitialComponentCandidates();
            }
            eventIndex2DepEventIndexSetMap = partitions.getForwardDependentComponentMap();
        } else {
            if (choice == -1) {
                firstCandidateEventIndexSet = partitions.getMarkedComponentCandidates();
            }
            eventIndex2DepEventIndexSetMap = partitions.getBackwardDependentComponentMap();
        }

        candidateEventIndexSet.clear();
        selectEventIndiceWithLargestDepSet(firstCandidateEventIndexSet, eventIndex2DepEventIndexSetMap);
    }

    private void buildSubsequentCandidates(final boolean forForward) {
        TIntObjectHashMap<TIntHashSet> eventIndex2DepEventIndexSetMap;
        if (forForward) {
            eventIndex2DepEventIndexSetMap = partitions.getForwardDependentComponentMap();
        } else {
            eventIndex2DepEventIndexSetMap = partitions.getBackwardDependentComponentMap();
        }
        selectEventIndiceWithLargestDepSet(activeEventIndexSet, eventIndex2DepEventIndexSetMap);
    }

    private void selectEventIndiceWithLargestDepSet(final TIntHashSet compIndexSet,
                                                    final TIntObjectHashMap<TIntHashSet> eventIndex2DepEventIndexSetMap) {
        int maxDepSize = -1;
        for (final TIntIterator compItr = compIndexSet.iterator(); compItr.hasNext();) {
            final int eventIndex = compItr.next();
                final int eventDepSize = eventIndex2DepEventIndexSetMap.get(eventIndex).size();
                if (eventDepSize > maxDepSize) {
                    maxDepSize = eventDepSize;
                    candidateEventIndexSet.clear();
                    candidateEventIndexSet.add(eventIndex);
                } else if (eventDepSize == maxDepSize) {
                    candidateEventIndexSet.add(eventIndex);
                }
        }
    }

    @Override
    public void advance(final boolean forward, final boolean changed) {
        activeEventIndexSet.remove(choice); // no matter whether the choice is good or not, it's not active any more.
        record(forward, changed); // record the changes
        learner.advance(choice, changed);
    }

    private void record(final boolean forward, final boolean changed) {
        if (changed) {
            if (firstTime) {
                firstTime = false;
                firstCandidateEventIndexSet.clear();
            }
            TIntObjectHashMap<TIntHashSet> eventIndex2DepEventIndexSetMap = null;
            if (forward) {
                eventIndex2DepEventIndexSetMap = partitions.getForwardDependentComponentMap();
            }
            else {
                eventIndex2DepEventIndexSetMap = partitions.getBackwardDependentComponentMap();
            }
            // update activeEventIndexSet
            for (final TIntIterator eventItr = eventIndex2DepEventIndexSetMap.get(choice).iterator(); eventItr.hasNext();) {
                activeEventIndexSet.add(eventItr.next());
            }
        } else {
            if (firstTime) {
                firstCandidateEventIndexSet.remove(choice);
            }
        }
    }

    @Override
    public void reset() {
        firstTime = true;
        choice = -1;
        learner.reset();
        candidateEventIndexSet.clear();

        activeEventIndexSet.clear();
        activeEventIndexSet.addAll(partitions.getCompIndexToCompBDDMap().keys());
    }


    @Override
    public boolean empty() {
        return activeEventIndexSet.size() == 0;
    }

}
