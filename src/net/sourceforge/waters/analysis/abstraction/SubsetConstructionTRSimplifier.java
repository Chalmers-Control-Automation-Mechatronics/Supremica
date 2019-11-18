//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
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

import java.util.List;

import net.sourceforge.waters.analysis.options.BooleanOption;
import net.sourceforge.waters.analysis.options.DoubleOption;
import net.sourceforge.waters.analysis.options.Option;
import net.sourceforge.waters.analysis.options.OptionMap;
import net.sourceforge.waters.analysis.tr.AbstractStateBuffer;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.IntSetBuffer;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.OneEventCachingTransitionIterator;
import net.sourceforge.waters.analysis.tr.PreTransitionBuffer;
import net.sourceforge.waters.analysis.tr.TauClosure;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.OverflowKind;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * A list buffer transition relation implementation of the subset
 * construction algorithm.
 *
 * @author Robi Malik
 */

public class SubsetConstructionTRSimplifier
  extends AbstractTRSimplifier
{

  //#########################################################################
  //# Constructors
  public SubsetConstructionTRSimplifier()
  {
    this(null);
  }

  public SubsetConstructionTRSimplifier(final ListBufferTransitionRelation rel)
  {
    super(rel);
  }


  //#########################################################################
  //# Configuration
  @Override
  public void setStateLimit(final int limit)
  {
    mConfiguredStateLimit = limit;
  }

  @Override
  public int getStateLimit()
  {
    return mConfiguredStateLimit;
  }

  /**
   * Sets the maximum factor by which the number of states may increase
   * before subset construction is aborted. This option can be used as
   * a relative state limit, which depends on the size of the input
   * automaton. If a hard state limit is also set using {@link
   * #setStateLimit(int)} then subset construction is aborted if either
   * the hard or the relative state limit is reached.
   * @param  maxIncrease  The maximum factor by which the number of states may
   *                      increase. For example, a value of <CODE>2.0</CODE>
   *                      means that subset construction is stopped when
   *                      the number of encountered subsets hits twice the
   *                      number of states of the input automaton.
   */
  public void setMaxIncrease(final double maxIncrease)
  {
    mMaxIncrease = maxIncrease;
  }

  /**
   * Gets the maximum factor by which the number of states may increase
   * before subset construction is aborted.
   * @see #setMaxIncrease(double)
   */
  public double getMaxIncrease()
  {
    return mMaxIncrease;
  }

  @Override
  public void setTransitionLimit(final int limit)
  {
    mTransitionLimit = limit;
  }

  @Override
  public int getTransitionLimit()
  {
    return mTransitionLimit;
  }

  /**
   * Sets whether dump states are pruned during subset construction.
   * @see #isDumpStateAware()
   */
  public void setDumpStateAware(final boolean aware)
  {
    mDumpStateAware = aware;
  }

  /**
   * <P>Gets whether dump states are pruned during subset construction.</P>
   *
   * <P>If set to <CODE>true</CODE>, any state set encountered during subset
   * construction, which includes the dump state of the input transition
   * relation, will be replaced by a dump state in the output transition
   * relation, and no exploration will happen beyond these states.</P>
   *
   * <P>The default of this setting is <CODE>false</CODE>.</P>
   */
  public boolean isDumpStateAware()
  {
    return mDumpStateAware;
  }

  /**
   * <P>Sets how failing events are treated by subset construction.</P>
   *
   * <P>Events with status {@link EventStatus#STATUS_FAILING}
   * are understood to cause verification to result in a <CODE>false</CODE>
   * result if enabled, therefore no further exploration is needed after
   * such a transition has occurred. If such an event is additionally
   * always enabled  ({@link EventStatus#STATUS_ALWAYS_ENABLED}), other
   * transitions outgoing from states with the event can be suppressed.</P>
   *
   * <P>This method controls whether failing event transitions should be
   * replaced by selfloops. The default of this setting is <CODE>false</CODE>,
   * causing failing event transitions to be redirected to the dump of the
   * output transition relation.</P>
   */
  public void setFailingEventsAsSelfloops(final boolean selfloops)
  {
    mFailingEventsAsSelfloops = selfloops;
  }

  /**
   * Returns how failing events are treated by subset construction.
   * @see #setFailingEventsAsSelfloops(boolean) setFailingEventsAsSelfloops()
   */
  public boolean getFailingEventsAsSelfloops()
  {
    return mFailingEventsAsSelfloops;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.analysis.abstraction.
  //# TransitionRelationSimplifier
  @Override
  public int getPreferredInputConfiguration()
  {
    return ListBufferTransitionRelation.CONFIG_SUCCESSORS;
  }

  @Override
  public boolean isPartitioning()
  {
    return false;
  }

  @Override
  public boolean isAlwaysEnabledEventsSupported()
  {
    return true;
  }

  @Override
  public void reset()
  {
    super.reset();
    mSetOffsets = null;
    mStateSetBuffer = null;
    mTransitionBuffer = null;
  }

  @Override
  public TRSimplifierStatistics createStatistics()
  {
    final TRSimplifierStatistics stats =
      new TRSimplifierStatistics(this, true, true);
    return setStatistics(stats);
  }


  //#########################################################################
  //# Overrides for
  //# net.sourceforge.waters.analysis.abstraction.AbstractTRSimplifier
  @Override
  protected void setUp()
  throws AnalysisException
  {
    super.setUp();
    final ListBufferTransitionRelation rel = getTransitionRelation();
    if ((rel.getProperEventStatus(EventEncoding.TAU) &
         EventStatus.STATUS_UNUSED) == 0) {
      mIsDeterministic = false;
      final TauClosure closure =
        rel.createSuccessorsTauClosure(mTransitionLimit);
      mTauIterator = closure.createIterator();
      mEventIterator = closure.createPostEventClosureIterator(-1);
    } else if (!rel.isDeterministic()) {
      mIsDeterministic = false;
      mTauIterator = null;
      final TransitionIterator iter = rel.createSuccessorsReadOnlyIterator();
      mEventIterator = new OneEventCachingTransitionIterator(iter);
    } else {
      mIsDeterministic = true;
      return;
    }

    if (Double.isFinite(mMaxIncrease)) {
      final int numReachable = rel.getNumberOfReachableStates();
      final int maxStates = (int) Math.ceil(mMaxIncrease * numReachable);
      mEffectiveStateLimit = Math.min(maxStates, mConfiguredStateLimit);
    } else {
      mEffectiveStateLimit = mConfiguredStateLimit;
    }

    final int numEvents = rel.getNumberOfProperEvents();
    final int numStates = rel.getNumberOfStates();
    mSetOffsets = new TIntArrayList(numStates);
    mStateSetBuffer = new IntSetBuffer(numStates);
    mTransitionBuffer = new PreTransitionBuffer(numEvents, mTransitionLimit);
    mDumpStateIndex = -1;
  }

  @Override
  protected boolean runSimplifier()
  throws AnalysisException
  {
    if (mIsDeterministic) {
      return false;
    } else {
      // 1. Collect initial state set.
      final ListBufferTransitionRelation rel = getTransitionRelation();
      final int numStates = rel.getNumberOfStates();
      final TIntHashSet init = new TIntHashSet();
      for (int state = 0; state < numStates; state++) {
        if (rel.isInitial(state)) {
          if (mTauIterator == null) {
            init.add(state);
          } else {
            checkAbort();
            mTauIterator.resetState(state);
            while (mTauIterator.advance()) {
              final int tausucc = mTauIterator.getCurrentTargetState();
              init.add(tausucc);
            }
          }
        }
      }
      int last = 0;
      if (!init.isEmpty()) {
        final int offset = mStateSetBuffer.add(init);
        mSetOffsets.add(offset);
        last = offset;
      } else if (numStates == 0) {
        return false;
      }

      // 2. Expand subset states.
      final int dumpIndex = mDumpStateAware ? rel.getDumpStateIndex() : -1;
      final int numEvents = rel.getNumberOfProperEvents();
      final IntSetBuffer.IntSetIterator iter = mStateSetBuffer.iterator();
      final TIntArrayList current = new TIntArrayList();
      states:
      for (int source = 0; source < mSetOffsets.size(); source++) {
        final int set = mSetOffsets.get(source);
        events:
        for (int e = EventEncoding.NONTAU; e < numEvents; e++) {
          checkAbort();
          iter.reset(set);
          mEventIterator.resetEvent(e);
          final byte status = rel.getProperEventStatus(e);
          if (EventStatus.isFailingEvent(status)) {
            while (iter.advance()) {
              final int state = iter.getCurrentData();
              mEventIterator.resetState(state);
              if (mEventIterator.advance()) {
                if (mFailingEventsAsSelfloops) {
                  mTransitionBuffer.addTransition(source, e, source);
                } else {
                  createDumpState();
                  mTransitionBuffer.addTransition(source, e, mDumpStateIndex);
                }
                if (EventStatus.isAlwaysEnabledEvent(status)) {
                  continue states;
                } else {
                  break;
                }
              }
            }
          } else {
            while (iter.advance()) {
              final int state = iter.getCurrentData();
              mEventIterator.resume(state);
              while (mEventIterator.advance()) {
                final int target = mEventIterator.getCurrentTargetState();
                if (target == dumpIndex) {
                  createDumpState();
                  mTransitionBuffer.addTransition(source, e, mDumpStateIndex);
                  current.clear();
                  continue events;
                }
                current.add(target);
              }
            }
            if (!current.isEmpty()) {
              current.sort();  // duplicates have been suppressed by iterator
              final int offset = mStateSetBuffer.add(current);
              current.clear();
              final int target;
              if (offset > last) {
                target = mSetOffsets.size();
                if (target >= mEffectiveStateLimit) {
                  final Logger logger = LogManager.getLogger();
                  logger.debug("State limit of {} reached, aborting.",
                               mEffectiveStateLimit);
                  throw new OverflowException(OverflowKind.STATE,
                                              mEffectiveStateLimit);
                }
                mSetOffsets.add(offset);
                last = offset;
              } else {
                target = mSetOffsets.binarySearch(offset);
              }
              mTransitionBuffer.addTransition(source, e, target);
            }
          }
        }
      }

      // 3. Build new transition relation.
      applyResultPartitionAutomatically();
      return true;
    }
  }

  @Override
  protected void applyResultPartition()
  throws AnalysisException
  {
    if (mSetOffsets != null) {
      final ListBufferTransitionRelation rel = getTransitionRelation();
      final AbstractStateBuffer oldStateBuffer = rel.getStateBuffer();
      final int numDetStates = mSetOffsets.size();
      final int numEvents = rel.getNumberOfProperEvents();
      final int numTrans = mTransitionBuffer.size();
      final int config = getPreferredInputConfiguration();
      if (mDumpStateIndex < 0) {
        rel.reset(numDetStates, numTrans, config);
      } else {
        rel.reset(numDetStates, mDumpStateIndex, numTrans, config);
      }
      rel.setInitial(0, true);
      final IntSetBuffer.IntSetIterator iter = mStateSetBuffer.iterator();
      for (int detstate = 0; detstate < numDetStates; detstate++) {
        long markings = rel.createMarkings();
        final int offset = mSetOffsets.get(detstate);
        iter.reset(offset);
        while (iter.advance()) {
          final int state = iter.getCurrentData();
          final long stateMarkings = oldStateBuffer.getAllMarkings(state);
          markings = rel.mergeMarkings(markings, stateMarkings);
        }
        rel.setAllMarkings(detstate, markings);
      }
      rel.removeRedundantPropositions();
      //mSetOffsets = null;
      //mStateSetBuffer = null;
      rel.removeEvent(EventEncoding.TAU);
      mTransitionBuffer.addOutgoingTransitions(rel);
      mTransitionBuffer = null;

      final TIntArrayList forbiddenVictims =  new TIntArrayList(numEvents);
      final TransitionIterator eventIter = rel.createSuccessorsReadOnlyIterator();
      final TransitionIterator failingIter = mFailingEventsAsSelfloops ?
        rel.createSuccessorsReadOnlyIteratorByStatus
          (EventStatus.STATUS_FAILING, EventStatus.STATUS_ALWAYS_ENABLED) :
        null;
      mDumpStateIndex = rel.getDumpStateIndex();
      for (int e = EventEncoding.NONTAU; e < numEvents; e++) {
        if (isSelfloopEventExceptInFailingStates(e, eventIter, failingIter)) {
          final byte status = rel.getProperEventStatus(e);
          if (EventStatus.isFailingEvent(status)) {
            forbiddenVictims.add(e);
          } else {
            rel.removeEvent(e);
          }
        }
      }
      for (int i = 0; i < forbiddenVictims.size(); i++) {
        final int e = forbiddenVictims.get(i);
        rel.removeEvent(e);
      }
    }
  }

  @Override
  protected void tearDown()
  {
    mTauIterator = mEventIterator = null;
  }

  @Override
  public List<Option<?>> getOptions(final OptionMap db)
  {
    final List<Option<?>> options = super.getOptions(db);
    db.append(options, TRSimplifierFactory.
               OPTION_SubsetConstruction_MaxIncrease);
    db.append(options, TRSimplifierFactory.
              OPTION_TransitionRelationSimplifier_DumpStateAware);
    db.append(options, TRSimplifierFactory.
               OPTION_SubsetConstruction_FailingEventsAsSelfLoops);
    return options;
  }

  @Override
  public void setOption(final Option<?> option)
  {
    if (option.hasID(TRSimplifierFactory.OPTION_SubsetConstruction_MaxIncrease)) {
      final DoubleOption propOption = (DoubleOption) option;
      setMaxIncrease(propOption.getValue());
    }
    else if (option.hasID(TRSimplifierFactory.OPTION_TransitionRelationSimplifier_DumpStateAware)) {
      final BooleanOption propOption = (BooleanOption) option;
      setDumpStateAware(propOption.getValue());
    }
    else if (option.hasID(TRSimplifierFactory.OPTION_SubsetConstruction_FailingEventsAsSelfLoops)) {
      final BooleanOption propOption = (BooleanOption) option;
      setFailingEventsAsSelfloops(propOption.getValue());
    }
    else {
      super.setOption(option);
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private void createDumpState()
  {
    if (mDumpStateIndex < 0) {
      final int[] empty = new int[0];
      mDumpStateIndex = mSetOffsets.size();
      final int offset = mStateSetBuffer.add(empty);
      mSetOffsets.add(offset);
    }
  }

  private boolean isSelfloopEventExceptInFailingStates
    (final int e,
     final TransitionIterator eventIter,
     final TransitionIterator failingIter)
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final byte status = rel.getProperEventStatus(e);
    if (EventStatus.isUsedEvent(status)) {
      final int numStates = rel.getNumberOfStates();
      for (int s = 0; s < numStates; s++) {
        if (s == mDumpStateIndex) {
          continue;
        } else if (mFailingEventsAsSelfloops) {
          failingIter.resetState(s);
          if (failingIter.advance()) {
            continue;
          }
        }
        eventIter.reset(s, e);
        if (!eventIter.advance()) {
          return false;
        } else if (eventIter.getCurrentTargetState() != s) {
          return false;
        }
      }
      return true;
    } else {
      return false;
    }
  }


  //#########################################################################
  //# Simple Access Methods
  public int[] getSourceSet(final int stateIndex)
  {
    return mStateSetBuffer.getSetContents(mSetOffsets.get(stateIndex));
  }


  //#########################################################################
  //# Data Members
  private int mConfiguredStateLimit = Integer.MAX_VALUE;
  private double mMaxIncrease = Double.POSITIVE_INFINITY;
  private int mTransitionLimit = Integer.MAX_VALUE;
  private boolean mDumpStateAware = false;
  private boolean mFailingEventsAsSelfloops = false;

  private int mEffectiveStateLimit = Integer.MAX_VALUE;
  private boolean mIsDeterministic;
  private TransitionIterator mTauIterator;
  private TransitionIterator mEventIterator;
  private TIntArrayList mSetOffsets;
  private IntSetBuffer mStateSetBuffer;
  private PreTransitionBuffer mTransitionBuffer;
  private int mDumpStateIndex;

}
