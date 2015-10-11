//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
package org.supremica.automata.BDD.EFA;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.set.hash.TIntHashSet;


/**
 * Regarding the automaton-based partitioning technique for ordinary automata,
 * the class implements the abstract class BDDPartitionCoordinator.
 *
 * @author zhennan
 */

public final class BDDPartitionCoordinatorAut extends BDDPartitionCoordinator
{

  /**
   * The automaton two-dimensional dependent matrix.
   */
  private final int[][] dependentMatrix;

  /**
   * The size of automata.
   */
  private final int size;

  /**
   * Keep track of the active automaton index.
   */
  private final boolean[] workset;

  /**
   * The size of active automata.
   */
  private int worksetCount;

  /**
   * When the number of components chosen by the heuristics is not single, put
   * them into this field.
   */
  private final int[] selectedAutIndices;

  /** Final choice */
  private int choice;

  /**
   * Reinforcement learning plus Tabu search to find the most useful component
   * from selectedCandidate. Now, only simple version is applied. Thus it can
   * be improved at the later stage.
   */
  private final BDDPartitionHeuristicsLearner learner;

  public BDDPartitionCoordinatorAut(final BDDPartitionSet autPartitions)
  {
    super(autPartitions);

    if (!(autPartitions instanceof BDDPartitionSetAut)) {
      throw new IllegalArgumentException("autPartitions must be an instance of BDDPartitionSetAut");
    }
    this.size = partitions.getCompIndexToCompBDDMap().size();

    this.workset = new boolean[size];

    this.selectedAutIndices = new int[size];
    learner = new BDDPartitionHeuristicsLearner(size);

    // initialize the dependent matrix
    this.dependentMatrix = new int[size][size];
    for (int automatonIndex = 0; automatonIndex < size; automatonIndex++) {
      final TIntHashSet depAutIndexSet =
        partitions.getForwardDependentComponentMap().get(automatonIndex);
      if (depAutIndexSet != null) {
        dependentMatrix[automatonIndex][0] = depAutIndexSet.size();
        int matrixIndex = 1;
        for (final TIntIterator depAutIndexItr =
          depAutIndexSet.iterator(); depAutIndexItr.hasNext();) {
          dependentMatrix[automatonIndex][matrixIndex++] =
            depAutIndexItr.next();
        }
      }
    }

    reset();
  }

  @Override
  public int pickOne(final boolean forForward)
  {
    // Regarding the automaton partitioning, we don't distinguish forward and backward.
    // The same dependency set is used to pick the next component.
    final int selectedAutSize = getMostFollowers();
    choice = learner.choose(selectedAutIndices, selectedAutSize);
    return choice;
  }

  private int getMostFollowers()
  {
    int selectedAutSize = 0;
    int best = 0;
    for (int automatonIndex = 0; automatonIndex < size; automatonIndex++) {
      if (workset[automatonIndex]) {
        final int depSize = dependentMatrix[automatonIndex][0];
        if (best < depSize) {
          best = depSize;
          selectedAutSize = 0;
        }
        if (best == depSize) {
          selectedAutIndices[selectedAutSize++] = automatonIndex;
        }
      }
    }
    return selectedAutSize;
  }

  @Override
  public void advance(final boolean forward, final boolean changed)
  {
    workset[choice] = false;
    worksetCount--;
    record(changed);
    learner.advance(choice, changed);
  }

  private void record(final boolean changed)
  {
    if (changed) {
      final int depSize = dependentMatrix[choice][0];
      for (int i = 1; i <= depSize; i++) {
        final int autIndex = dependentMatrix[choice][i];
        if (!workset[autIndex]) {
          workset[autIndex] = true;
          worksetCount++;
        }
      }
    }
  }

  @Override
  public void reset()
  {
    worksetCount = size;
    choice = -1;

    for (int i = 0; i < workset.length; i++) {
      workset[i] = true;
    }

    learner.reset();
  }

  @Override
  public boolean empty()
  {
    return worksetCount == 0;
  }

}
