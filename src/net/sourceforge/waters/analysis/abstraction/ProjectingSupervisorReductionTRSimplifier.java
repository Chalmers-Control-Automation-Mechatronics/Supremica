//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TRPartition;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AbortRequester;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.OverflowException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * <P>A transition relation simplifier that identifies events to be removed
 * for the purpose of supervisor reduction.</P>
 *
 * <P>This transition relation simplifier checks for each event, using a
 * verifier-type algorithm, whether removing the event results in an
 * equivalent supervisor with respect to the supervised events. Any events
 * that are found safe for removal are marked as local ({@link
 * EventStatus#STATUS_LOCAL}), so that they can be removed by subsequent
 * hiding ({@link SpecialEventsTRSimplifier}) and subset construction
 * ({@link SubsetConstructionTRSimplifier}).</P>
 *
 * @see VerifierBuilder
 *
 * @author Robi Malik
 */

public class ProjectingSupervisorReductionTRSimplifier
  extends AbstractSupervisorReductionTRSimplifier
{

  //#########################################################################
  //# Constructors
  public ProjectingSupervisorReductionTRSimplifier()
  {
  }

  public ProjectingSupervisorReductionTRSimplifier
    (final ListBufferTransitionRelation rel)
  {
    super(rel);
  }


  //#########################################################################
  //# Configuration
  /**
   * Sets the state limit. The states limit specifies the maximum
   * number of verifier pairs that will be created.
   * @param limit
   *          The new state limit, or {@link Integer#MAX_VALUE} to allow
   *          an unlimited number of states.
   */
  @Override
  public void setStateLimit(final int limit)
  {
    mVerifierBuilder.setStateLimit(limit);
  }

  /**
   * Gets the state limit.
   * @see #setStateLimit(int) setStateLimit()
   */
  @Override
  public int getStateLimit()
  {
    return mVerifierBuilder.getStateLimit();
  }

  public void setExhaustive(final boolean exhaustive)
  {
    mExhaustive = exhaustive;
  }

  public boolean isExhaustive()
  {
    return mExhaustive;
  }

  public void setEnsuringOP(final boolean ensure)
  {
    mEnsuringOP = ensure;
  }

  public boolean isEnsuringOP()
  {
    return mEnsuringOP;
  }

  public void setPreSimplificationEnabled(final boolean enable)
  {
    mPreSimplificationEnabled = enable;
  }

  public boolean isPreSimplificationEnabled()
  {
    return mPreSimplificationEnabled;
  }


  //#########################################################################
  //# Interface
  //# net.sourceforge.waters.analysis.abstraction.TransitionRelationSimplifier
  @Override
  public boolean isPartitioning()
  {
    return false;
  }

  @Override
  public int getPreferredInputConfiguration()
  {
    return ListBufferTransitionRelation.CONFIG_SUCCESSORS;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.Abortable
  @Override
  public void requestAbort(final AbortRequester sender)
  {
    super.requestAbort(sender);
    mVerifierBuilder.requestAbort(sender);
    if (mPreSimplifier != null) {
      mPreSimplifier.requestAbort(sender);
    }
  }

  @Override
  public void resetAbort()
  {
    super.resetAbort();
    mVerifierBuilder.resetAbort();
    if (mPreSimplifier != null) {
      mPreSimplifier.resetAbort();
    }
  }


  //#########################################################################
  //# Overrides for
  //# net.sourceforge.waters.analysis.abstraction.AbstractTRSimplifier
  @Override
  protected void setUp() throws AnalysisException
  {
    super.setUp();
    if (mEnsuringOP || mPreSimplificationEnabled) {
      final ChainTRSimplifier chain = new ChainTRSimplifier();
      final TransitionRelationSimplifier hider =
        new SpecialEventsTRSimplifier();
      chain.add(hider);
      final TauEliminationTRSimplifier tauEliminator = new
        TauEliminationTRSimplifier();
      tauEliminator.setTauOnly(true);
      tauEliminator.setAppliesPartitionAutomatically(true);
      chain.add(tauEliminator);
      final ObservationEquivalenceTRSimplifier bisimulator =
        new ObservationEquivalenceTRSimplifier();
      bisimulator.setEquivalence
        (ObservationEquivalenceTRSimplifier.Equivalence.BISIMULATION);
      bisimulator.setAppliesPartitionAutomatically(true);
      chain.add(bisimulator);
      chain.setPreferredOutputConfiguration
        (mVerifierBuilder.getPreferredInputConfiguration());
      mPreSimplifier = chain;
    }
  }

  @Override
  public TRSimplifierStatistics createStatistics()
  {
    final TRSimplifierStatistics stats =
      new TRSimplifierStatistics(this, false, false, false, false);
    return setStatistics(stats);
  }

  @Override
  protected boolean runSimplifier() throws AnalysisException
  {
    try {
      final Logger logger = LogManager.getLogger();
      setUpEventInfo();
      final int numRemoved =
        mExhaustive ? searchExhaustively() : searchGreedily();
      logger.debug("Proposing to remove {} events.", numRemoved);
      return numRemoved > 0;
    } catch (final AnalysisException exception) {
      restoreEventStatus();
      throw exception;
    } catch (final OutOfMemoryError error) {
      System.gc();
      restoreEventStatus();
      throw new OverflowException(error);
    } catch (final StackOverflowError error) {
      restoreEventStatus();
      throw new OverflowException(error);
    }
  }

  @Override
  protected void tearDown()
  {
    super.tearDown();
    mPreSimplifier = null;
    mEventInfo = null;
    mOutstandingInfo = null;
    mKnownBest = null;
  }


  //#########################################################################
  //# Algorithm
  private void setUpEventInfo()
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    int numEvents = rel.getNumberOfProperEvents();
    final EventInfo[] array = new EventInfo[numEvents];
    for (int e = EventEncoding.NONTAU; e < numEvents; e++) {
      final byte status = rel.getProperEventStatus(e);
      if (EventStatus.isUsedEvent(status) &&
          !EventStatus.isLocalEvent(status) &&
          !isSupervisedEvent(e)) {
        array[e] = new EventInfo(e);
      } else {
        numEvents--;
      }
    }
    final TransitionIterator iter = rel.createAllTransitionsReadOnlyIterator();
    while (iter.advance()) {
      final int e = iter.getCurrentEvent();
      final EventInfo info = array[e];
      if (info != null) {
        final boolean selfloop =
          iter.getCurrentFromState() == iter.getCurrentToState();
        info.addTransition(selfloop);
      }
    }
    mEventInfo = new ArrayList<>(numEvents);
    for (int e = EventEncoding.NONTAU; e < numEvents; e++) {
      if (array[e] != null) {
        mEventInfo.add(array[e]);
      }
    }
    Collections.sort(mEventInfo);
  }

  private void setUpOutstandingInfo()
  {
    final int numEvents = mEventInfo.size();
    mOutstandingInfo = new ResultInfo[numEvents];
    final ListIterator<EventInfo> iter = mEventInfo.listIterator(numEvents);
    ResultInfo next = null;
    int e = numEvents;
    while (iter.hasPrevious()) {
      final EventInfo eventInfo = iter.previous();
      final ResultInfo info = new ResultInfo(eventInfo);
      if (next != null) {
        info.add(next);
      }
      mOutstandingInfo[--e] = info;
      next = info;
    }
  }

  private int searchGreedily() throws AnalysisException
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    int numRemoved = 0;
    final TIntArrayList uncertain = new TIntArrayList(mEventInfo.size());
    for (final EventInfo info : mEventInfo) {
      final int e = info.getEvent();
      final byte status = rel.getProperEventStatus(e);
      rel.setProperEventStatus(e, status | EventStatus.STATUS_LOCAL);
      try {
        if (!mVerifierBuilder.isSuitableProjection(rel)) {
          rel.setProperEventStatus(e, status);
        } else if (mVerifierBuilder.isOPAcceptable()) {
          numRemoved += uncertain.size() + 1;
          uncertain.clear();
        } else {
          uncertain.add(e);
        }
      } catch (final OverflowException exception) {
        uncertain.add(e);
        LogManager.getLogger().warn
          ("Overflow building verifier, seach for projection stopped.");
        break;
      } catch (final OutOfMemoryError error) {
        System.gc();
        uncertain.add(e);
        LogManager.getLogger().warn
          ("Out of memory building verifier, seach for projection stopped.");
        break;
      }
    }
    for (int i = 0; i < uncertain.size(); i++) {
      final int e = uncertain.get(i);
      final byte status = rel.getProperEventStatus(e);
      rel.setProperEventStatus(e, status & ~EventStatus.STATUS_LOCAL);
    }
    return numRemoved;
  }

  private int searchExhaustively() throws AnalysisException
  {
    setUpOutstandingInfo();
    final ResultInfo parent = new ResultInfo();
    try {
      searchExhaustively(parent, 0);
    } catch (final OverflowException exception) {
      LogManager.getLogger().warn
        ("Overflow building verifier, seach for projection stopped.");
    } catch (final OutOfMemoryError error) {
      System.gc();
      LogManager.getLogger().warn
        ("Out of memory building verifier, seach for projection stopped.");
    }
    if (mKnownBest == null) {
      return 0;
    } else {
      final ListBufferTransitionRelation rel = getTransitionRelation();
      return mKnownBest.apply(rel);
    }
  }

  private void searchExhaustively(final ResultInfo parent,
                                  final int nextEvent)
    throws AnalysisException
  {
    if (nextEvent < mOutstandingInfo.length &&
        parent.canImprove(mKnownBest, mOutstandingInfo[nextEvent])) {
      final EventInfo info = mEventInfo.get(nextEvent);
      final int e = info.getEvent();
      final ListBufferTransitionRelation rel = getTransitionRelation();
      final byte status = rel.getProperEventStatus(e);
      rel.setProperEventStatus(e, status | EventStatus.STATUS_LOCAL);
      if (mVerifierBuilder.isSuitableProjection(rel)) {
        final Result result = new Result(info, parent);
        if (mVerifierBuilder.isOPAcceptable() &&
            result.isBetterThan(mKnownBest)) {
          mKnownBest = result;
        }
        searchExhaustively(result, nextEvent + 1);
      }
      rel.setProperEventStatus(e, status);
      searchExhaustively(parent, nextEvent + 1);
    }
  }

  private void restoreEventStatus()
  {
    if (mEventInfo != null) {
      final ListBufferTransitionRelation rel = getTransitionRelation();
      for (final EventInfo info : mEventInfo) {
        final int e = info.getEvent();
        final byte status = rel.getProperEventStatus(e);
        rel.setProperEventStatus(e, status & ~EventStatus.STATUS_LOCAL);
      }
    }
  }


  //#########################################################################
  //# Inner Class ResultInfo
  private static class ResultInfo implements Comparable<ResultInfo>
  {
    //#######################################################################
    //# Constructor
    private ResultInfo()
    {
    }

    private ResultInfo(final ResultInfo info)
    {
      mNumNonSelfloopTransitions = info.mNumNonSelfloopTransitions;
      mNumSelfloopTransitions = info.mNumSelfloopTransitions;
    }

    //#######################################################################
    //# Simple Access
    void addTransition(final boolean selfloop)
    {
      if (selfloop) {
        mNumSelfloopTransitions++;
      } else {
        mNumNonSelfloopTransitions++;
      }
    }

    void add(final ResultInfo info)
    {
      mNumNonSelfloopTransitions += info.mNumNonSelfloopTransitions;
      mNumSelfloopTransitions += info.mNumSelfloopTransitions;
    }

    boolean isBetterThan(final ResultInfo other)
    {
      if (other == null) {
        return true;
      } else {
        return compareTo(other) < 0;
      }
    }

    private boolean canImprove(final ResultInfo best,
                               final ResultInfo outstanding)
    {
      if (best == null) {
        return true;
      }
      final int nonSelfloops =
        mNumNonSelfloopTransitions + outstanding.mNumNonSelfloopTransitions;
      if (nonSelfloops > best.mNumNonSelfloopTransitions) {
        return true;
      } else if (nonSelfloops < best.mNumNonSelfloopTransitions) {
        return false;
      } else {
        return
          mNumSelfloopTransitions + outstanding.mNumSelfloopTransitions >
          best.mNumSelfloopTransitions;
      }
    }

     //#######################################################################
    //# Interface java.util.Comparable<EventInfo>
    @Override
    public int compareTo(final ResultInfo info)
    {
      final int result =
        info.mNumNonSelfloopTransitions - mNumNonSelfloopTransitions;
      if (result != 0) {
        return result;
      } else {
        return info.mNumSelfloopTransitions - mNumSelfloopTransitions;
      }
    }

    //#######################################################################
    //# Data Members
    private int mNumNonSelfloopTransitions;
    private int mNumSelfloopTransitions;
  }


  //#########################################################################
  //# Inner Class EventInfo
  private static class EventInfo extends ResultInfo
  {
    //#######################################################################
    //# Constructor
    private EventInfo(final int event)
    {
      mEvent = event;
    }

    //#######################################################################
    //# Simple Access
    private int getEvent()
    {
      return mEvent;
    }

    //#######################################################################
    //# Data Members
    private final int mEvent;
  }


  //#########################################################################
  //# Inner Class Result
  private static class Result extends ResultInfo
  {
    //#######################################################################
    //# Constructor
    private Result(final EventInfo info,
                   final ResultInfo parent)
    {
      super(info);
      mEvent = info.getEvent();
      if (parent instanceof Result) {
        mParent = (Result) parent;
        add(parent);
      } else {
        mParent = null;
      }
    }

    //#######################################################################
    //# Simple Access
    private int apply(final ListBufferTransitionRelation rel)
    {
      final byte status = rel.getProperEventStatus(mEvent);
      rel.setProperEventStatus(mEvent, status | EventStatus.STATUS_LOCAL);
      if (mParent == null) {
        return 1;
      } else {
        return mParent.apply(rel) + 1;
      }
    }

    //#######################################################################
    //# Data Members
    private final int mEvent;
    private final Result mParent;
  }


  //#########################################################################
  //# Inner Class SupervisorReductionLoopRemover
  @SuppressWarnings("unused")
  private class SupervisorReductionLoopRemover
    extends TauLoopRemovalTRSimplifier
  {
    //#######################################################################
    //# Overrides for
    //# net.sourceforge.waters.analysis.abstraction.TransitionRelationSimplifier
    @Override
    public void applyResultPartition()
      throws AnalysisException
    {
      if (isCompatibilityPreservingPartition()) {
        mFailedSimplification = false;
        super.applyResultPartition();
      } else {
        mFailedSimplification = true;
      }
    }

    //#######################################################################
    //# Auxiliary Methods
    private boolean isCompatibilityPreservingPartition()
    {
      final TRPartition partition = getResultPartition();
      if (partition != null) {
        final ListBufferTransitionRelation rel = getTransitionRelation();
        final int dumpIndex = rel.getDumpStateIndex();
        final TransitionIterator iter = rel.createSuccessorsReadOnlyIterator();
        final TIntArrayList supEvents = getSupervisedEvents();
        for (int i = 0; i < supEvents.size(); i++) {
          final int e = supEvents.get(i);
          for (final int[] clazz : partition.getClasses()) {
            boolean enabling = false;
            boolean disabling = false;
            for (final int s : clazz) {
              iter.reset(s, e);
              while (iter.advance()) {
                if (iter.getCurrentTargetState() == dumpIndex) {
                  if (enabling) {
                    return false;
                  } else {
                    disabling = true;
                  }
                } else {
                  if (disabling) {
                    return false;
                  } else {
                    enabling = true;
                  }
                }
              }
            }
          }
        }
      }
      return true;
    }
  }


  //#########################################################################
  //# Inner Class SupervisorReductionConditionalSimplifier
  @SuppressWarnings("unused")
  private class SupervisorReductionConditionalSimplifier
    extends ConditionalTRSimplifier
  {
    //#######################################################################
    //# Overrides for
    //# net.sourceforge.waters.analysis.abstraction.ConditionalTRSimplifier
    @Override
    protected boolean checkPreCondition()
      throws AnalysisException
    {
      return !mFailedSimplification;
    }

    @Override
    protected boolean isCopying()
    {
      return false;
    }
  }


  //#########################################################################
  //# Inner Class SupervisorReductionVerifierBuilder
  private class SupervisorReductionVerifierBuilder extends VerifierBuilder
  {
    //#######################################################################
    //# Invocation
    private boolean isSuitableProjection(ListBufferTransitionRelation rel)
      throws AnalysisException
    {
      if (mPreSimplifier != null) {
        final int config = mPreSimplifier.getPreferredInputConfiguration();
        rel = new ListBufferTransitionRelation(rel, config);
        mPreSimplifier.setTransitionRelation(rel);
        mPreSimplifier.run();
        if (mFailedSimplification) {
          return false;
        }
        rel = mPreSimplifier.getTransitionRelation();
      }
      return buildVerifier(rel);
    }

    private boolean isOPAcceptable()
    {
      if (mEnsuringOP) {
        return isOPSatisfied();
      } else {
        return true;
      }
    }

    //#######################################################################
    //# Overrides for
    //# net.sourceforge.waters.analysis.abstraction.VerifierBuilder
    @Override
    public boolean buildVerifier(final ListBufferTransitionRelation rel)
      throws AnalysisException
    {
      mDumpIndex = rel.getDumpStateIndex();
      return super.buildVerifier(rel);
    }

    @Override
    protected boolean newStatePair(final int code1, final int code2)
    {
      if (code1 == mDumpIndex || code2 == mDumpIndex) {
        return setFailedResult();
      } else {
        return true;
      }
    }

    //#########################################################################
    //# Data Members
    private int mDumpIndex;
  }


  //#########################################################################
  //# Data Members
  private boolean mExhaustive = false;
  private boolean mEnsuringOP = false;
  private boolean mPreSimplificationEnabled = true;

  private final SupervisorReductionVerifierBuilder mVerifierBuilder =
    new SupervisorReductionVerifierBuilder();
  private TransitionRelationSimplifier mPreSimplifier;
  private List<EventInfo> mEventInfo;
  private ResultInfo[] mOutstandingInfo;
  private Result mKnownBest;
  private boolean mFailedSimplification;

}
