//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
package org.supremica.automata.BDD.EFA;

/**
 * A helper class as the second stage of heuristic selection. The same idea
 * (even some implementation) is from Arash.
 *
 * @author zhennan
 */

public class BDDPartitionHeuristicsLearner
{

  private static final int ADD_REWARD = 2;
  private static final int ADD_PUNISH = -1;

  private final int max_activity;
  private int[] activity = null, queue2 = null;
  private final boolean punish_inactive;

  public BDDPartitionHeuristicsLearner(final int size)
  {
    max_activity = size;
    punish_inactive = true;
    activity = new int[size];
    queue2 = new int[size];
  }

  public void reset()
  {
    if (activity != null) {
      for (int i = 0; i < activity.length; i++) {
        activity[i] = 0;
      }
    }
  }

  public int choose(final int[] queue, final int size)
  {
    if (size <= 0) {
      throw new IllegalArgumentException("the arguement size cannot be zero!");
    }
    if (size == 1) {
      return queue[0];
    }
    return find_best_active(queue, size);
  }

  public void advance(final int element, final boolean changed)
  {
    if (punish_inactive) {
      activity[element] += (changed) ? ADD_REWARD : ADD_PUNISH;
      // dont let it grow more than we can handle...
      if (activity[element] > max_activity) {
        activity[element] = max_activity;
      } else if (activity[element] < -max_activity) {
        activity[element] = -max_activity;
      }
    }
  }

  private int find_best_active(final int[] queue, final int size)
  {
    int count = 0;
    int best = Integer.MIN_VALUE;
    for (int i = 0; i < size; i++) {
      final int current = queue[i];
      if (activity[current] > best) {
        best = activity[current];
        count = 0;
      }
      if (activity[current] == best) {
        queue2[count++] = current;
      }
    }
    return queue2[(int) (Math.random() * count)];
  }
}