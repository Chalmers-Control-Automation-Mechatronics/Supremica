//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

package net.sourceforge.waters.analysis.tr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
 * <DT>State-to-Class Map.</DT>
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
   * Gets the number of states covered by this partition.
   * This is the number of states of the original automaton before it
   * was partitioned.
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
   * An empty partition contains no classes. It consists of only bad states
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
   * Checks whether two states are in the same equivalence class according
   * to this partition. This method may set up the state-to-class map as a
   * side effect.
   * @return <CODE>true</CODE> if the two states are assigned to the same
   *         class, or both are assigned no class.
   */
  public boolean isEquivalent(final int state1, final int state2)
  {
    if (state1 == state2) {
      return true;
    } else {
      setUpStateToClass();
      return mStateToClass[state1] == mStateToClass[state2];
    }
  }

  /**
   * Changes this partition by merging two states. The classes of the
   * two given states are combined and assigned the class number of the
   * first state. This class is modified to represent the union of
   * the classes of the two states. The other class is marked as unused
   * (<CODE>null</CODE>).
   * @param  state1   The first of the two states to be merged,
   *                  whose class number is retained.
   * @param  state2   The second of the two states to be merged,
   *                  whose class number is marked as unused.
   * @return <CODE>true</CODE> if the partition has been changed;
   *         <CODE>false</CODE> if the two given states were already in
   *         the same class.
   */
  public boolean mergeStates(final int state1, final int state2)
  {
    if (state1 == state2) {
      return false;
    }
    setUpStateToClass();
    final int class1 = mStateToClass[state1];
    final int class2 = mStateToClass[state2];
    return mergeClasses(class1, class2);
  }

  /**
   * Changes this partition by merging two classes. The first of the
   * two given classes is extended by adding all states of the second
   * class. The second class is marked as unused (<CODE>null</CODE>).
   * @param  class1   The first of the two classes to be merged,
   *                  which is extended by the second class.
   * @param  class2   The second of the two classes to be merged,
   *                  which becomes unused after the merge.
   * @return <CODE>true</CODE> if the partition has been changed;
   *         <CODE>false</CODE> if the two given classes are the same.
   */
  public boolean mergeClasses(final int class1, final int class2)
  {
    if (class1 == class2) {
      return false;
    }
    if (mClasses == null) {
      for (int s = 0; s < mNumberOfStates; s++) {
        if (mStateToClass[s] == class2) {
          mStateToClass[s] = class1;
        }
      }
    } else {
      setUpStateToClass();
      final int[] array1 = mClasses.get(class1);
      final int[] array2 = mClasses.get(class2);
      for (final int s : array2) {
        mStateToClass[s] = class1;
      }
      final int[] combined = new int[array1.length + array2.length];
      System.arraycopy(array1, 0, combined, 0, array1.length);
      System.arraycopy(array2, 0, combined, array1.length, array2.length);
      mClasses.set(class1, combined);
      mClasses.set(class2, null);
    }
    return true;
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

  /**
   * Returns whether this partition merges at least two states
   * into a proper state (not undefined) in the output.
   */
  public boolean containsMergedStates()
  {
    if (mClasses != null) {
      for (final int[] clazz : mClasses) {
        if (clazz != null && clazz.length >= 2) {
          return true;
        }
      }
      return false;
    } else {
      int count = 0;
      for (int s = 0; s < mNumberOfStates; s++) {
        if (mStateToClass[s] >= 0) {
          count++;
        }
      }
      return count > mNumberOfClasses;
    }
  }

  /**
   * Returns the smallest unused class number (index of <CODE>null</CODE>
   * class), or -1 if the partition contains no unused classes.
   */
  public int getUnusedClass()
  {
    if (mClasses != null) {
      return mClasses.indexOf(null);
    } else {
      final BitSet used = new BitSet(mNumberOfClasses);
      for (final int clazz : mStateToClass) {
        if (clazz >= 0) {
          used.set(clazz);
        }
      }
      final int unused = used.nextClearBit(0);
      return unused < mNumberOfClasses ? unused : -1;
    }
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
   * An identical partition consists of only singleton classes.
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
   * The two given state encodings must contain the same states, but possibly
   * with different codes. The computed partition maps states in the first
   * encoding to singleton classes in the second encoding, marking any states
   * not present in the second encoding as bad.
   * @param  first      State encoding containing states to be partitioned.
   * @param  second     State encoding containing result states of partition.
   * @return A partition, or <CODE>null</CODE> if the partition is found to
   *         be trivial
   */
  public static TRPartition createReencodingPartition
    (final StateEncoding first, final StateEncoding second)
  {
    final int[] stateToClass = new int[first.getNumberOfStates()];
    boolean trivial = first.getNumberOfStates() == second.getNumberOfStates();
    for (int i = 0; i < first.getNumberOfStates(); i++) {
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
   * Creates a partition to convert from one state encoding to another.
   * The two given state encodings may contain different states, with the
   * re-encoding map linking states from the first encoding to states of
   * the second. The computed partition maps states in the first encoding
   * to singleton classes in the second encoding, marking any states not
   * present in the second encoding as bad.
   * @param  first      State encoding containing states to be partitioned.
   * @param  second     State encoding containing result states of partition.
   * @param  reencoding State map from first to second states. Missing entries
   *                    in the mapped are assumed to map states to themselves.
   * @return A partition, or <CODE>null</CODE> if the partition is found to
   *         be trivial
   */
  public static TRPartition createReencodingPartition
    (final StateEncoding first, final StateEncoding second,
     final Map<StateProxy,StateProxy> reencoding)
  {
    final int[] stateToClass = new int[first.getNumberOfStates()];
    boolean trivial = first.getNumberOfStates() == second.getNumberOfStates();
    for (int i = 0; i < first.getNumberOfStates(); i++) {
      final StateProxy state1 = first.getState(i);
      StateProxy state2 = reencoding.get(state1);
      if (state2 == null) {
        state2 = state1;
      }
      final int clazz = second.getStateCode(state2);
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
   * and assigns the unreachable states to no class (<CODE>null</CODE>).
   * Each reachable state receives a class code that is as small as possible
   * if the unreachable states have no class, so that the resulting partition
   * can be used to re-encode a transition relation with unreachable states
   * to use fewer states.
   * @return The computed partition or <CODE>null</CODE> if all states
   *         are reachable.
   */
  public static TRPartition createMinimalReachabilityPartition
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
   * Creates an identical partition with unreachable states removed.
   * This method creates a partition that assigns all reachable
   * states in the given transition relation to one-element classes,
   * and assigns the unreachable states to no class.
   * The class code assigned to each reachable state is equal to its
   * state number. The unreachable states appear as empty (<CODE>null</CODE>)
   * classes in the partition.
   * @return The computed partition.
   */
  public static TRPartition createIdenticalReachabilityPartition
    (final ListBufferTransitionRelation rel)
  {
    final int numStates = rel.getNumberOfStates();
    final int[] stateToClass = new int[numStates];
    for (int s = 0; s < numStates; s++) {
      if (rel.isReachable(s)) {
        stateToClass[s] = s;
      } else {
        stateToClass[s] = -1;
      }
    }
    return new TRPartition(stateToClass, numStates);
  }


  /**
   * Combines two partitions. This method computes the partition that results
   * if a state space is first partitioned using the given <CODE>first</CODE>
   * partition, and afterwards the result is further partitioned using the
   * given <CODE>second</CODE> partition. If either partition is
   * <CODE>null</CODE>, the other one is returned.
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
      for (int i = 0; i < mClasses.size(); i++) {
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
    final StringBuilder builder = new StringBuilder();
    if (mClasses != null) {
      for (int c = 0; c < mNumberOfClasses; c++) {
        final int[] clazz = mClasses.get(c);
        if (clazz != null) {
          builder.append(c);
          String sep = ": [";
          for (final int s : clazz) {
            builder.append(sep);
            builder.append(s);
            sep = ", ";
          }
          builder.append("]\n");
        }
      }
    } else {
      for (int s = 0; s < mNumberOfStates; s++) {
        builder.append(s);
        final int c = mStateToClass[s];
        if (c >= 0) {
          builder.append(" -> ");
          builder.append(c);
        } else {
          builder.append(" --");
        }
        builder.append("\n");
      }
    }
    return builder.toString();
  }


  //#########################################################################
  //# Data Members
  private final int mNumberOfStates;
  private final int mNumberOfClasses;
  private List<int[]> mClasses;
  private int[] mStateToClass;

}
