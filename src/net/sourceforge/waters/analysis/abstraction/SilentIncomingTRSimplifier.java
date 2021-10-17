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
import gnu.trove.set.hash.TIntHashSet;
import gnu.trove.stack.TIntStack;
import gnu.trove.stack.array.TIntArrayStack;

import java.util.BitSet;
import java.util.List;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.options.BooleanOption;
import net.sourceforge.waters.model.options.LeafOptionPage;
import net.sourceforge.waters.model.options.Option;


/**
 * <P>A list buffer transition relation implementation of the
 * <I>Silent Incoming Rule</I> or the <I>Only Silent Incoming Rule</I>.</P>
 *
 * <P>The <I>Silent Incoming Rule</I> removes a transition when a &tau;
 * event links two states <I>x</I> and&nbsp;<I>y</I> where at most
 * the source state&nbsp;<I>x</I> contains the precondition
 * marking&nbsp;&alpha;. If the target state&nbsp;<I>y</I> becomes
 * unreachable, it is removed, too. All transitions originating from the target
 * state&nbsp;<I>y</I> are copied to the source state&nbsp;<I>x</I>.</P>
 *
 * <P>The implementation can be configured to remove only transitions leading
 * to states that become unreachable, giving the <I>Only Silent Incoming
 * Rule</I>.</P>
 *
 * <P>The implementation supports both standard and generalised nonblocking
 * variants of the abstraction, including a generalisation that takes
 * <I>always enabled</I> events into account. If a precondition marking is
 * configured, only transitions leading to states not marked by the
 * precondition marking can be abstracted (as described above). Without a
 * precondition marking, only transitions leading to states with an outgoing
 * &tau; or always enabled ({@link EventStatus#STATUS_ALWAYS_ENABLED})
 * transition can be abstracted.</P>
 *
 * <P><I>References:</I><BR>
 * Hugo Flordal, Robi Malik. Compositional Verification
 * in Supervisory Control. SIAM Journal of Control and Optimization,
 * <STRONG>48</STRONG>(3), 1914&ndash;1938, 2009.<BR>
 * Robi Malik, Ryan Leduc. A Compositional Approach for Verifying
 * Generalised Nonblocking. Proc. 7th International Conference on Control and
 * Automation, ICCA'09, 448&ndash;453, Christchurch, New Zealand, 2009.<BR>
 * Colin Pilbrow, Robi Malik. Compositional Nonblocking Verification with
 * Always Enabled Events and Selfloop-only Events. Proc. 2nd International
 * Workshop on Formal Techniques for Safety-Critical Systems, FTSCS 2013,
 * 147&ndash;162, Queenstown, New Zealand, 2013.</P>
 *
 * @author Rachel Francis, Robi Malik, Colin Pilbrow
 */

public class SilentIncomingTRSimplifier
  extends AbstractMarkingTRSimplifier
{

  //#########################################################################
  //# Constructors
  public SilentIncomingTRSimplifier()
  {
  }

  public SilentIncomingTRSimplifier(final ListBufferTransitionRelation rel)
  {
    super(rel);
  }


  //#########################################################################
  //# Configuration
  /**
   * Sets whether this simplifier should consider deadlock states when
   * removing selfloops.
   * @see AbstractMarkingTRSimplifier#isDumpStateAware()
   */
  public void setDumpStateAware(final boolean aware)
  {
    mDumpStateAware = aware;
  }

  /**
   * Gets whether this simplifier considers deadlock states when
   * removing selfloops.
   */
  @Override
  public boolean isDumpStateAware()
  {
    return mDumpStateAware;
  }

  /**
   * Sets whether abstraction is applied to all states or only to states
   * that become unreachable. When this option is set to <CODE>true</CODE>
   * (the default), then the <I>Silent Incoming Rule</I> is only applied
   * to tau-transitions that lead to a state that becomes unreachable
   * by application of the rule. When set to <CODE>false</CODE>, the rule
   * is applied to all tau transitions leading to a state not marked by
   * the precondition, regardless of whether these states become unreachable
   * or not.
   */
  public void setRestrictsToUnreachableStates(final boolean restrict)
  {
    mRestrictsToUnreachableStates = restrict;
  }

  /**
   * Gets whether abstraction is applied to all states or only to states
   * that become unreachable.
   * @see #setRestrictsToUnreachableStates(boolean) setRestrictsToUnreachableStates()
   */
  public boolean getRestrictsToUnreachableStates()
  {
    return mRestrictsToUnreachableStates;
  }

  @Override
  public List<Option<?>> getOptions(final LeafOptionPage db)
  {
    final List<Option<?>> options = super.getOptions(db);
    db.append(options, StepSimplifierFactory.
              OPTION_SilentIncoming_RestrictsToUnreachableStates);
    db.append(options, StepSimplifierFactory.
              OPTION_TransitionRelationSimplifier_DumpStateAware);
    return options;
  }

  @Override
  public void setOption(final Option<?> option)
  {
    if (option.hasID(StepSimplifierFactory.
                     OPTION_SilentIncoming_RestrictsToUnreachableStates)) {
      final BooleanOption boolOption = (BooleanOption) option;
      setRestrictsToUnreachableStates(boolOption.getValue());
    } else if (option.hasID(StepSimplifierFactory.
                            OPTION_TransitionRelationSimplifier_DumpStateAware)) {
      final BooleanOption boolOption = (BooleanOption) option;
      setDumpStateAware(boolOption.getValue());
    } else {
      super.setOption(option);
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.analysis.abstraction.TransitionRelationSimplifier
  @Override
  public int getPreferredInputConfiguration()
  {
    return ListBufferTransitionRelation.CONFIG_SUCCESSORS;
  }

  @Override
  public boolean isObservationEquivalentAbstraction()
  {
    return true;
  }

  @Override
  public boolean isAlwaysEnabledEventsSupported()
  {
    return getPreconditionMarkingID() >= 0;
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.analysis.abstraction.AbstractTRSimplifier
  @Override
  public void setUp()
    throws AnalysisException
  {
    super.setUp();
    if (getPreconditionMarkingID() < 0) {
      final ListBufferTransitionRelation rel = getTransitionRelation();
      mAlwaysEnabledIterator = rel.createSuccessorsReadOnlyIteratorByStatus
        (EventStatus.STATUS_ALWAYS_ENABLED);
    }
  }

  @Override
  public boolean runSimplifier()
    throws AnalysisException
  {
    final int tauID = EventEncoding.TAU;
    final ListBufferTransitionRelation rel = getTransitionRelation();
    if ((rel.getProperEventStatus(tauID) & EventStatus.STATUS_UNUSED) != 0) {
      return false;
    }
    final int numStates = rel.getNumberOfStates();
    final BitSet keep = new BitSet(numStates);
    if (mRestrictsToUnreachableStates) {
      for (int state = 0; state < numStates; state++) {
        if (rel.isInitial(state) ||
            !isReducible(state) ||
            !rel.isReachable(state)) {
          keep.set(state);
        }
      }
      final TransitionIterator iter =
        rel.createAllTransitionsReadOnlyIterator();
      while (iter.advance()) {
        if (iter.getCurrentEvent() != tauID) {
          final int target = iter.getCurrentTargetState();
          keep.set(target);
        }
      }
    } else {
      for (int state = 0; state < numStates; state++) {
        if (!isReducible(state) || !rel.isReachable(state)) {
          keep.set(state);
        }
      }
    }
    checkAbort();
    if (keep.cardinality() == numStates) {
      return false;
    }
    final TransitionIterator reader = rel.createSuccessorsReadOnlyIterator();
    final TransitionIterator writer = rel.createSuccessorsModifyingIterator();
    final TIntArrayList targets = new TIntArrayList();
    final TIntStack stack = new TIntArrayStack();
    boolean modified = false;
    for (int source = 0; source < numStates; source++) {
      if (rel.isReachable(source)) {
        checkAbort();
        final TIntHashSet visited = new TIntHashSet();
        stack.push(source);
        visited.add(source);
        while (stack.size() > 0) {
          final int current = stack.pop();
          reader.reset(current, tauID);
          while (reader.advance()) {
            final int target = reader.getCurrentTargetState();
            if (!keep.get(target) && visited.add(target)) {
              stack.push(target);
              targets.add(target);
            }
          }
        }
        if (!targets.isEmpty()) {
          // TODO Implement transition limit!
          rel.copyOutgoingTransitions(targets, source);
          writer.reset(source, tauID);
          while (writer.advance()) {
            final int target = writer.getCurrentTargetState();
            if (visited.contains(target)) {
              writer.remove();
            }
          }
          targets.clear();
          modified = true;
        }
      }
    }
    if (modified) {
      applyResultPartitionAutomatically();
    }
    return modified;
  }

  @Override
  protected void tearDown()
  {
    mAlwaysEnabledIterator = null;
    super.tearDown();
  }

  @Override
  protected void applyResultPartition()
    throws AnalysisException
  {
    super.applyResultPartition();
    final ListBufferTransitionRelation rel = getTransitionRelation();
    rel.checkReachability();
    rel.removeTauSelfLoops();
    removeProperSelfLoopEvents();
    rel.removeRedundantPropositions();

//    final Logger logger = LogManager.getLogger();
//    if (logger.isDebugEnabled()) {
//      final TransitionIterator iter =
//        rel.createAllTransitionsReadOnlyIterator(EventEncoding.TAU);
//      int count = 0;
//      while (iter.advance()) {
//        count++;
//      }
//      logger.debug("{} tau transitions remaining.",  count);
//    }
  }


  //#########################################################################
  //# Auxiliary Methods
  /**
   * Returns whether the given state may be simplified by the silent
   * continuation rule. A state is reducible if it has an outgoing
   * <I>always enabled</I> ({@link EventStatus#STATUS_ALWAYS_ENABLED})
   * transition or, in the case of generalised nonblocking,
   * if it is not marked by the precondition (alpha) marking.
   */
  private boolean isReducible(final int state)
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int alphaID = getPreconditionMarkingID();
    if (alphaID < 0) {
      mAlwaysEnabledIterator.resetState(state);
      return mAlwaysEnabledIterator.advance();
    } else {
      return !rel.isMarked(state, alphaID);
    }
  }


  //#########################################################################
  //# Data Members
  private boolean mRestrictsToUnreachableStates = true;
  private boolean mDumpStateAware = false;

  private TransitionIterator mAlwaysEnabledIterator;

}
