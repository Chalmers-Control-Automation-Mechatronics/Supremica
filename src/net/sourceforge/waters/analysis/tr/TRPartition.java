//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.tr
//# CLASS:   TRPartition
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.tr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.model.des.StateProxy;

/**
 * <P>A partition to represent equivalent states of a {@link
 * ListBufferTransitionRelation}.</P>
 *
 * <P>A partition consists of a list of equivalence classes, each representing
 * a set of equivalent states. States in the same equivalence class are
 * equivalent and may be merged.</P>
 *
 * <P>This class represents states as integer primitives. There are two
 * standard ways to represent and access a partition.</P>
 * <DL>
 * <DT>Class List.</DT>
 * <DD>A list of integer arrays (<CODE>{@link List}&lt;int[]&gt;</CODE>),
 * each of which contains the states in an equivalence class. The state
 * code of an equivalence class is given by its position in the list.
 * The list may contain <CODE>null</CODE> entries to represent unused
 * or bad equivalence class numbers.</DD>
 * <DT>State-to-Class Map.</DD>
 * <DD>An integer array that assigns to each state the number of its
 * equivalence class (which is the position of the class in the above
 * class list). Two states are equivalent of their array entries contain
 * the same equivalence class number. The array may contain negative
 * entries as class numbers to represent bad states that are not merged
 * into any equivalence class but removed.</DD>
 * </DL>
 * <P>This class supports both of the above partition representation.
 * The user can initialise the partition by providing data in either
 * form, and access information using both forms. The missing index
 * structure is computed automatically when requested the first time.</P>
 *
 * @author Robi Malik, Sahar Mohajerani
 */

public class TRPartition
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a partition using data in class-list form.
   * @param classes      List of equivalence classes.
   * @param numStates    The total number of states to be partitioned.
   *                     The largest state number mentioned in the equivalence
   *                     classes must be less than the number of states.
   */
  public TRPartition(final List<int[]> classes, final int numStates)
  {
    mNumberOfStates = numStates;
    mNumberOfClasses = classes.size();
    mClasses = classes;
    mStateToClass = null;
  }

  /**
   * Creates a partition using data in state-to-class map form.
   * @param stateToClass Array assigning equivalence class numbers to states.
   * @param numClasses   The total number of equivalence classes.
   *                     The largest class number mentioned in the map
   *                     must be less than the number of classes.
   */
  public TRPartition(final int[] stateToClass, final int numClasses)
  {
    mNumberOfStates = stateToClass.length;
    mNumberOfClasses = numClasses;
    mClasses = null;
    mStateToClass = stateToClass;
  }


  //#########################################################################
  //# Simple Access
  /**
   * Gets the number of states partitioned by this partitioned.
   */
  public int getNumberOfStates()
  {
    return mNumberOfStates;
  }

  /**
   * Gets the number of equivalence classes in this partition.
   */
  public int getNumberOfClasses()
  {
    return mNumberOfClasses;
  }

  /**
   * Checks whether this partition is empty.
   * A partition is empty contains no classes. It consists of only bad states
   * that are removed as a result of partitioning.
   */
  public boolean isEmpty()
  {
    return mNumberOfClasses == 0;
  }

  /**
   * Gets the list of equivalence classes.
   * @return A list of integer arrays, each of which contains the states in
   *         an equivalence class. The state code of an equivalence class is
   *         given by its position in the list. The list may contain
   *         <CODE>null</CODE> entries to represent unused or bad equivalence
   *         class numbers.
   */
  public List<int[]> getClasses()
  {
    setUpClasses();
    return mClasses;
  }

  /**
   * Gets the state-to-class map representing this partition.
   * @return An integer array that assigns to each state the number of its
   *         equivalence class. The array may contain negative entries to
   *         represent bad states that are not merged into any equivalence
   *         class but removed.
   */
  public int[] getStateToClass()
  {
    setUpStateToClass();
    return mStateToClass;
  }

  /**
   * Gets the states in the given equivalence class.
   * @param  clazz    The number of the equivalence class to be checked.
   * @return An array containing the state codes in this equivalence class,
   *         or <CODE>null</CODE> indicating an empty or bad class.
   */
  public int[] getStates(final int clazz)
  {
    setUpClasses();
    return mClasses.get(clazz);
  }

  /**
   * Gets the equivalence class number of the given state.
   * @param  state    The number of the state to be looked up.
   * @return The number of the state's equivalence class. A negative
   *         number may be returned to indicate a bad state that is not
   *         merged into any class.
   */
  public int getClassCode(final int state)
  {
    setUpStateToClass();
    return mStateToClass[state];
  }

  /**
   * Returns the number of assigned states in this partition.
   * Assigned states are states of partitioned state space that have been
   * assigned a class number.
   */
  public int getNumberOfAssignedStates()
  {
    int count = 0;
    if (mClasses != null) {
      for (final int[] clazz : mClasses) {
        if (clazz != null) {
          count += clazz.length;
        }
      }
    } else {
      for (int s = 0; s < mNumberOfStates; s++) {
        if (mStateToClass[s] >= 0) {
          count++;
        }
      }
    }
    return count;
  }


  //#########################################################################
  //# Static Methods
  /**
   * Creates an empty partition.
   * An empty partition consists of only bad states that are removed
   * as a result of partitioning.
]  * @param numStates    The total number of states to be partitioned.
   */
  public static TRPartition createEmptyPartition(final int numStates)
  {
    final List<int[]> classes = Collections.emptyList();
    return new TRPartition(classes, 0);
  }

  /**
   * Creates an identical partition.
   * An empty partition consists of only singleton classes.
   * It does not merge any states and keeps every state in a class
   * containing only that state.
]  * @param numStates    The total number of states to be partitioned.
   */
  public static TRPartition createIdenticalPartition(final int numStates)
  {
    final int[] stateToClass = new int[numStates];
    for (int s = 0; s < numStates; s++) {
      stateToClass[s] = s;
    }
    return new TRPartition(stateToClass, numStates);
  }

  /**
   * Creates a partition to convert from one state encoding to another.
   */
  public static TRPartition createReencodingPartition(final StateEncoding first,
                                                      final StateEncoding second)
  {
    final int[] stateToClass = new int[first.getNumberOfStates()];
    boolean trivial = first.getNumberOfStates() == second.getNumberOfStates();
    for (int i=0; i< first.getNumberOfStates(); i++) {
      final StateProxy state = first.getState(i);
      final int clazz = second.getStateCode(state);
      if (clazz != i) {
        trivial = false;
      }
      stateToClass[i] = clazz;
    }
    if (trivial) {
      return null;
    } else {
      return new TRPartition(stateToClass, second.getNumberOfStates());
    }
  }

  /**
   * Creates a partition to remove unreachable states from a transition
   * relation. This method creates a partition that assigns all reachable
   * states in the given transition relation to one-element classes,
   * and removes all unreachable states.
   * @return The computed partition or <CODE>null</CODE> if all states
   *         are reachable.
   */
  public static TRPartition createReachabilityPartition
    (final ListBufferTransitionRelation rel)
  {
    final int numStates = rel.getNumberOfStates();
    final int[] stateToClass = new int[numStates];
    int count = 0;
    for (int s = 0; s < numStates; s++) {
      if (rel.isReachable(s)) {
        stateToClass[s] = count++;
      } else {
        stateToClass[s] = -1;
      }
    }
    if (count == numStates) {
      return null;
    } else {
      return new TRPartition(stateToClass, count);
    }
  }


  /**
   * Combines two partitions. This method computes the partition that results
   * if a state space is first partitioned using the given <CODE>first</CODE>
   * partition, and afterwards the result is further partitioned using the
   * given <CODE>second partition. If either partition is <CODE>null</CODE>,
   * the other one is returned.
   */
  public static TRPartition combine(final TRPartition first,
                                    final TRPartition second)
  {
    if (first == null) {
      return second;
    } else if (second == null) {
      return first;
    } else if (first.mStateToClass != null && second.mStateToClass != null) {
      return first.combineUsingStateToClass(second);
    } else if (first.mClasses != null && second.mClasses != null) {
      return first.combineUsingClasses(second);
    } else if (first.mStateToClass != null) {
      second.setUpStateToClass();
      return first.combineUsingStateToClass(second);
    } else {
      second.setUpClasses();
      return first.combineUsingClasses(second);
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private void setUpClasses()
  {
    if (mClasses == null) {
      final int[] counts = new int[mNumberOfClasses];
      for (int s = 0; s < mNumberOfStates; s++) {
        final int c = mStateToClass[s];
        if (c >= 0) {
          counts[c]++;
        }
      }
      mClasses = new ArrayList<>(mNumberOfClasses);
      for (int c = 0; c < mNumberOfClasses; c++) {
        final int count = counts[c];
        if (count > 0) {
          final int[] clazz = new int[count];
          mClasses.add(clazz);
          counts[c] = 0;
        } else {
          mClasses.add(null);
        }
      }
      for (int s = 0; s < mNumberOfStates; s++) {
        final int c = mStateToClass[s];
        if (c >= 0) {
          final int index = counts[c]++;
          final int[] clazz = mClasses.get(c);
          clazz[index] = s;
        }
      }
    }
  }

  private void setUpStateToClass()
  {
    if (mStateToClass == null) {
      mStateToClass = new int[mNumberOfStates];
      Arrays.fill(mStateToClass, -1);
      for (int i = 0; i < mClasses.size(); i++)
      {
        final int[] clazz = mClasses.get(i);
        if (clazz != null) {
          for (final int s : clazz) {
            mStateToClass[s] = i;
          }
        }
      }
    }
  }

  private TRPartition combineUsingClasses(final TRPartition second)
  {
    assert mNumberOfClasses == second.getNumberOfStates();
    final int numClasses = second.getNumberOfClasses();
    final List<int[]> classes = new ArrayList<>(numClasses);
    for (final int[] clazz2 : second.getClasses()) {
      if (clazz2 != null) {
        int count = 0;
        for (final int s2 : clazz2) {
          final int[] clazz1 = mClasses.get(s2);
          if (clazz1 != null) {
            count += clazz1.length;
          }
        }
        if (count > 0) {
          final int[] clazz = new int[count];
          int i = 0;
          for (final int s2 : clazz2) {
            final int[] clazz1 = mClasses.get(s2);
            if (clazz1 != null) {
              for (final int s1 : clazz1) {
                clazz[i++] = s1;
              }
            }
          }
          classes.add(clazz);
        } else {
          classes.add(null);
        }
      } else {
        classes.add(null);
      }
    }
    return new TRPartition(classes, mNumberOfStates);
  }

  private TRPartition combineUsingStateToClass(final TRPartition second)
  {
    assert mNumberOfClasses == second.getNumberOfStates();
    final int[] stateToClass = new int[mNumberOfStates];
    for (int s = 0; s < mNumberOfStates; s++) {
      int c = mStateToClass[s];
      if (c >= 0) {
        c = second.getClassCode(c);
      }
      stateToClass[s] = c;
    }
    return new TRPartition(stateToClass, second.getNumberOfClasses());
  }


  //#########################################################################
  //# Debugging
  @Override
  public String toString()
  {
    final StringBuilder buffer = new StringBuilder();
    if (mClasses != null) {
      for (int c = 0; c < mNumberOfClasses; c++) {
        final int[] clazz = mClasses.get(c);
        if (clazz != null) {
          buffer.append(c);
          String sep = ": [";
          for (final int s : clazz) {
            buffer.append(sep);
            buffer.append(s);
            sep = ", ";
          }
          buffer.append("]\n");
        }
      }
    } else {
      for (int s = 0; s < mNumberOfStates; s++) {
        buffer.append(s);
        final int c = mStateToClass[s];
        if (c >= 0) {
          buffer.append(" -> ");
          buffer.append(c);
        } else {
          buffer.append(" --");
        }
        buffer.append("\n");
      }
    }
    return buffer.toString();
  }


  //#########################################################################
  //# Data Members
  private final int mNumberOfStates;
  private final int mNumberOfClasses;
  private List<int[]> mClasses;
  private int[] mStateToClass;

}
