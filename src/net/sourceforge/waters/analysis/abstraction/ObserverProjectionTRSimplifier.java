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

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TRPartition;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisException;


/**
 * <P>An implementation of the observer projection algorithm.</P>
 *
 * <P>This simplifier determines a coarsest causal reporter map satisfying the
 * observer property for the input automaton and repartitions accordingly.
 * Nondeterminism in the projected automaton is not resolved, nondeterministic
 * abstractions are used instead.</P>
 *
 * <P><I>Reference:</I> K. C. Wong and W. M. Wonham, On the Computation of
 * Observers in Discrete-Event Systems. Discrete Event Dynamic Systems,
 * <STRONG>14</STRONG>&nbsp;(1), 2004, 55-107.</P>
 *
 * @author Robi Malik
 */

public class ObserverProjectionTRSimplifier
  extends AbstractTRSimplifier
{

  //#########################################################################
  //# Constructors
  public ObserverProjectionTRSimplifier()
  {
    this(null);
  }

  public ObserverProjectionTRSimplifier(final ListBufferTransitionRelation rel)
  {
    super(rel);
    mBisimulator = new ObservationEquivalenceTRSimplifier();
    mBisimulator.setEquivalence(ObservationEquivalenceTRSimplifier.
                                Equivalence.OBSERVATION_EQUIVALENCE);
    mBisimulator.setAppliesPartitionAutomatically(false);
    mBisimulator.setStatistics(null);
    mVisibleTau = -1;
  }


  //#########################################################################
  //# Configuration
  /**
   * Specifies an event code to be used for tau events made visible by
   * the observer projection algorithm.
   */
  public void setVisibleTau(final int vtau)
  {
    mVisibleTau = vtau;
  }

  /**
   * Gets the event code used for tau events made visible by
   * the observer projection algorithm.
   */
  public int getVisibleTau()
  {
    return mVisibleTau;
  }

  /**
   * Sets the mode which redundant transitions are to be removed.
   * @see ObservationEquivalenceTRSimplifier.TransitionRemoval
   */
  public void setTransitionRemovalMode
    (final ObservationEquivalenceTRSimplifier.TransitionRemoval mode)
  {
    mBisimulator.setTransitionRemovalMode(mode);
  }

  /**
   * Gets the mode which redundant transitions are to be removed.
   * @see ObservationEquivalenceTRSimplifier.TransitionRemoval
   */
  public ObservationEquivalenceTRSimplifier.TransitionRemoval
    getTransitionRemovalMode()
  {
    return mBisimulator.getTransitionRemovalMode();
  }

  /**
   * Sets the transition limit. The transition limit specifies the maximum
   * number of transitions (including stored silent transitions of the
   * transitive closure) that will be stored.
   * @param limit
   *          The new transition limit, or {@link Integer#MAX_VALUE} to allow
   *          an unlimited number of transitions.
   */
  public void setTransitionLimit(final int limit)
  {
    mBisimulator.setTransitionLimit(limit);
  }

  /**
   * Gets the transition limit.
   * @see #setTransitionLimit(int) setTransitionLimit()
   */
  public int getTransitionLimit()
  {
    return mBisimulator.getTransitionLimit();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.analysis.abstraction.
  //# TransitionRelationSimplifier
  @Override
  public int getPreferredInputConfiguration()
  {
    return ListBufferTransitionRelation.CONFIG_PREDECESSORS;
  }

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
  public TRSimplifierStatistics createStatistics()
  {
    final TRSimplifierStatistics stats =
      new TRSimplifierStatistics(this, true, true);
    return setStatistics(stats);
  }

  @Override
  public void reset()
  {
    mBisimulator.reset();
    super.reset();
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.analysis.abstraction.AbstractTRSimplifier
  @Override
  protected boolean runSimplifier()
  throws AnalysisException
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int numTransBefore = rel.getNumberOfTransitions();
    mBisimulator.setTransitionRelation(rel);
    TRPartition partition;
    while (true) {
      checkAbort();
      final boolean modified = mBisimulator.run();
      if (!modified && rel.getNumberOfTransitions() == numTransBefore) {
        return false;
      }
      partition = mBisimulator.getResultPartition();
      if (partition == null) {
        break;
      } else if (!makeEventsVisible(partition)) {
        break;
      }
      mBisimulator.setUpInitialPartition(partition);
    }
    setResultPartition(partition);
    applyResultPartitionAutomatically();
    return true;
  }

  @Override
  public void applyResultPartition()
  throws AnalysisException
  {
    mBisimulator.applyResultPartition();
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int tau = EventEncoding.TAU;
    rel.replaceEvent(mVisibleTau, tau);
    rel.removeEvent(mVisibleTau);
    rel.removeRedundantPropositions();
  }


  //#########################################################################
  //# Auxiliary Methods
  private boolean makeEventsVisible(final TRPartition partition)
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int numStates = rel.getNumberOfStates();
    final int[] pmap = new int[numStates];
    int code = 0;
    for (final int[] array : partition.getClasses()) {
      if (array != null) {
        for (final int state : array) {
          pmap[state] = code;
        }
      }
      code++;
    }
    final TransitionIterator iter =
      rel.createPredecessorsModifyingIterator();
    final TIntArrayList victims = new TIntArrayList();
    final int tau = EventEncoding.TAU;
    boolean modified = false;
    for (int target= 0; target < numStates; target++) {
      if (rel.isReachable(target)) {
        final int targetClass = pmap[target];
        iter.reset(target, tau);
        while (iter.advance()) {
          final int source = iter.getCurrentSourceState();
          final int sourceClass = pmap[source];
          if (sourceClass != targetClass) {
            iter.remove();
            victims.add(source);
          }
        }
        if (!victims.isEmpty()) {
          modified = true;
          rel.addTransitions(victims, mVisibleTau, target);
          victims.clear();
        }
      }
    }
    return modified;
  }


  //#########################################################################
  //# Data Members
  private final ObservationEquivalenceTRSimplifier mBisimulator;
  private int mVisibleTau;

}
