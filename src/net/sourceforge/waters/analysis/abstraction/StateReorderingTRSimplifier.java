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

package net.sourceforge.waters.analysis.abstraction;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.stack.array.TIntArrayStack;

import java.util.Arrays;
import java.util.BitSet;

import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TRPartition;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.EnumFactory;
import net.sourceforge.waters.model.analysis.ListedEnumFactory;
import net.sourceforge.waters.model.base.ProxyTools;


/**
 * <P>A transition relation simplifier to reorder the states of a
 * transition relation.</P>
 *
 * <P>This transition relation simplifier reorders the states of a
 * transition relation based on a configured state ordering ({@link
 * StateOrdering}), possibly also compacting the state encoding by
 * removing unreachable states.</P>
 *
 * @author Robi Malik
 */

public class StateReorderingTRSimplifier
  extends AbstractTRSimplifier
{

  //#########################################################################
  //# Constructors
  public StateReorderingTRSimplifier()
  {
  }

  public StateReorderingTRSimplifier(final ListBufferTransitionRelation rel)
  {
    super(rel);
  }


  //#########################################################################
  //# Configuration
  public void setStateOrdering(final StateOrdering ordering)
  {
    mStateOrdering = ordering;
  }

  public StateOrdering getStateOrdering()
  {
    return mStateOrdering;
  }

  public static EnumFactory<StateOrdering> getStateOrderingEnumFactory()
  {
    return EnumFactorySingletonHolder.theInstance;
  }


  //#########################################################################
  //# Interface
  //# net.sourceforge.waters.analysis.abstraction.TransitionRelationSimplifier
  @Override
  public boolean isPartitioning()
  {
    return true;
  }

  @Override
  public boolean isObservationEquivalentAbstraction()
  {
    return true;
  }

  @Override
  public int getPreferredInputConfiguration()
  {
    return mStateOrdering.getPreferredInputConfiguration();
  }

  @Override
  public TRSimplifierStatistics createStatistics()
  {
    final TRSimplifierStatistics stats =
      new TRSimplifierStatistics(this, true, false);
    return setStatistics(stats);
  }


  //#########################################################################
  //# Overrides for
  //# net.sourceforge.waters.analysis.abstraction.AbstractTRSimplifier
  @Override
  protected boolean runSimplifier()
    throws AnalysisException
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final TRPartition partition = mStateOrdering.getPartition(rel);
    if (partition == null) {
      return false;
    }
    setResultPartition(partition);
    applyResultPartitionAutomatically();
    return true;
  }


  //#########################################################################
  //# Inner Interface StateOrdering
  public static abstract class StateOrdering
  {
    //#######################################################################
    //# Constructor
    private StateOrdering(final boolean reversed)
    {
      mReversed = reversed;
    }

    //#######################################################################
    //# Simple Access
    public boolean isTrivial()
    {
      return false;
    }

    boolean isReversed()
    {
      return mReversed;
    }

    //#######################################################################
    //# Naming
    @Override
    public String toString()
    {
      final String name = getRawName();
      if (mReversed) {
        return name + " Reversed";
      } else {
        return name;
      }
    }

    String getConsoleName()
    {
      final String name = getRawName();
      if (mReversed) {
        return "Reversed" + name;
      } else {
        return name;
      }
    }

    String getRawName()
    {
      final String name = ProxyTools.getShortClassName(this);
      final String commonName = ProxyTools.getShortClassName(StateOrdering.class);
      final int len = name.length() - commonName.length();
      return name.substring(0, len);
    }

    //#######################################################################
    //# Invocation
    public int getPreferredInputConfiguration()
    {
      return 0;
    }

    TRPartition getPartition(final ListBufferTransitionRelation rel)
    {
      final int numStates = rel.getNumberOfStates();
      final int[] orderedStates = getOrderedStates(rel);
      final int numClasses = orderedStates.length;
      final int[] stateToClass = new int[numStates];
      boolean trivial = true;
      if (numClasses < numStates) {
        Arrays.fill(stateToClass, -1);
        trivial = false;
      }
      if (mReversed) {
        for (int i = 0, c = numClasses - 1; i < numClasses; i++, c--) {
          final int s = orderedStates[i];
          stateToClass[s] = c;
          trivial &= (s == c);
        }
      } else {
        for (int c = 0; c < numClasses; c++) {
          final int s = orderedStates[c];
          stateToClass[s] = c;
          trivial &= (s == c);
        }
      }
      if (trivial) {
        return null;
      } else {
        return new TRPartition(stateToClass, numStates);
      }
    }

    //#######################################################################
    //# Hooks
    abstract int[] getOrderedStates(ListBufferTransitionRelation rel);

    //#######################################################################
    //# Data Members
    private final boolean mReversed;
  }


  //#########################################################################
  //# Inner Class UnchangedStateOrdering
  private static class UnchangedStateOrdering extends StateOrdering
  {
    //#######################################################################
    //# Constructor
    private UnchangedStateOrdering(final boolean reversed)
    {
      super(reversed);
    }

    //#######################################################################
    //# Overrides for StateOrdering
    @Override
    public String toString()
    {
      if (isReversed()) {
        return "Reversed";
      } else {
        return getRawName();
      }
    }

    @Override
    String getConsoleName()
    {
      return toString();
    }

    @Override
    public boolean isTrivial()
    {
      return !isReversed();
    }

    @Override
    int[] getOrderedStates(final ListBufferTransitionRelation rel)
    {
      final int numStates = rel.getNumberOfStates();
      final int numReachable = rel.getNumberOfReachableStates();
      final int[] orderedStates = new int[numReachable];
      int orderIndex = 0;
      for (int s = 0; s < numStates; s++) {
        if (rel.isReachable(s)) {
          orderedStates[orderIndex++] = s;
        }
      }
      return orderedStates;
    }
  }


  //#########################################################################
  //# Inner Class BFSStateOrdering
  private static class BFSStateOrdering extends StateOrdering
  {
    //#######################################################################
    //# Constructor
    private BFSStateOrdering(final boolean reversed)
    {
      super(reversed);
    }

    //#######################################################################
    //# Overrides for StateOrdering
    @Override
    public int getPreferredInputConfiguration()
    {
      return ListBufferTransitionRelation.CONFIG_SUCCESSORS;
    }

    @Override
    int[] getOrderedStates(final ListBufferTransitionRelation rel)
    {
      final int numStates = rel.getNumberOfStates();
      final int dumpIndex = rel.getDumpStateIndex();
      final TIntArrayList queue = new TIntArrayList(numStates);
      final BitSet visited = new BitSet(numStates);
      visited.set(dumpIndex);
      for (int s = 0; s < numStates; s++) {
        if (rel.isInitial(s)) {
          queue.add(s);
          visited.set(s);
        }
      }
      final TransitionIterator iter = rel.createSuccessorsReadOnlyIterator();
      int pos = 0;
      while (pos < queue.size()) {
        final int s = queue.get(pos++);
        iter.resetState(s);
        while (iter.advance()) {
          final int t = iter.getCurrentTargetState();
          if (!visited.get(t)) {
            queue.add(t);
            visited.set(t);
          }
        }
      }
      if (rel.isReachable(dumpIndex)) {
        queue.add(dumpIndex);
      }
      return queue.toArray();
    }
  }


  //#########################################################################
  //# Inner Class DFSStateOrdering
  private static class DFSStateOrdering extends StateOrdering
  {
    //#######################################################################
    //# Constructor
    private DFSStateOrdering(final boolean reversed)
    {
      super(reversed);
    }

    //#######################################################################
    //# Overrides for StateOrdering
    @Override
    public int getPreferredInputConfiguration()
    {
      return ListBufferTransitionRelation.CONFIG_SUCCESSORS;
    }

    @Override
    int[] getOrderedStates(final ListBufferTransitionRelation rel)
    {
      final int numStates = rel.getNumberOfStates();
      final int dumpIndex = rel.getDumpStateIndex();
      final TIntArrayStack stack = new TIntArrayStack(numStates);
      final BitSet visited = new BitSet(numStates);
      visited.set(dumpIndex);
      for (int s = 0; s < numStates; s++) {
        if (rel.isInitial(s)) {
          stack.push(s);
          visited.set(s);
        }
      }
      final TransitionIterator iter = rel.createSuccessorsReadOnlyIterator();
      final TIntArrayList orderedStates = new TIntArrayList(numStates);
      while (stack.size() > 0) {
        final int s = stack.pop();
        orderedStates.add(s);
        iter.resetState(s);
        while (iter.advance()) {
          final int t = iter.getCurrentTargetState();
          if (!visited.get(t)) {
            stack.push(t);
            visited.set(t);
          }
        }
      }
      if (rel.isReachable(dumpIndex)) {
        orderedStates.add(dumpIndex);
      }
      return orderedStates.toArray();
    }
  }


  //#########################################################################
  //# Inner Class StateOrderingEnumFactory
  public static class StateOrderingEnumFactory
    extends ListedEnumFactory<StateOrdering>
  {
    //#######################################################################
    //# Constructor
    private StateOrderingEnumFactory()
    {
      register(new UnchangedStateOrdering(false));
      register(new UnchangedStateOrdering(true));
      register(new BFSStateOrdering(false));
      register(new BFSStateOrdering(true));
      register(new DFSStateOrdering(false));
      register(new DFSStateOrdering(true));
    }

    //#######################################################################
    //# Overrides for net.sourceforge.waters.model.analysis.EnumFactory
    @Override
    public String getConsoleName(final StateOrdering item)
    {
      return item.getConsoleName();
    }
  }

  private static class EnumFactorySingletonHolder {
    private static final StateOrderingEnumFactory theInstance =
      new StateOrderingEnumFactory();
  }


  //#########################################################################
  //# Data Members
  private StateOrdering mStateOrdering = new UnchangedStateOrdering(false);


  //#########################################################################
  //# Class Constants
  public static final StateOrdering UNCHANGED =
    new UnchangedStateOrdering(false);
  public static final StateOrdering REVERSED =
    new UnchangedStateOrdering(true);
  public static final StateOrdering BFS =
    new BFSStateOrdering(false);
  public static final StateOrdering BFS_REVERSED =
    new BFSStateOrdering(true);
  public static final StateOrdering DFS =
    new BFSStateOrdering(false);
  public static final StateOrdering DFS_REVERSED =
    new BFSStateOrdering(true);

}
