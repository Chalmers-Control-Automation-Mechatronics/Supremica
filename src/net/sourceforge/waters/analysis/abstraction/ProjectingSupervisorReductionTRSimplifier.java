//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2018 Robi Malik
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

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * <P>A transition relation simplifier that ...</P>
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
   * number of verifier pairs that will be created by the OP-Verifier.
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
  //# Overrides for
  //# net.sourceforge.waters.analysis.abstraction.AbstractTRSimplifier
  @Override
  protected void setUp() throws AnalysisException
  {
    super.setUp();
    mEventInfo = new LinkedList<>();
  }

  @Override
  public boolean runSimplifier() throws AnalysisException
  {
    final Logger logger = LogManager.getLogger();
    setUpEventInfo();
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final Iterator<EventInfo> iter = mEventInfo.iterator();
    int numRemoved = 0;
    while (iter.hasNext()) {
      final EventInfo info = iter.next();
      final int event = info.getEvent();
      final byte status = rel.getProperEventStatus(event);
      rel.setProperEventStatus(event, status | EventStatus.STATUS_LOCAL);
      if (mVerifierBuilder.buildVerifier(rel)) {
        numRemoved++;
      } else {
        rel.setProperEventStatus(event, status);
        iter.remove();
      }
    }
    logger.debug("Proposing to remove {} events.", numRemoved);
    return false;
  }

  @Override
  protected void tearDown()
  {
    super.tearDown();
    mEventInfo = null;
  }


  //#########################################################################
  //# Algorithm
  private void setUpEventInfo()
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int numEvents = rel.getNumberOfProperEvents();
    final EventInfo[] info = new EventInfo[numEvents];
    for (int event = EventEncoding.NONTAU; event < numEvents; event++) {
      if (!isControllable(event)) {
        info[event] = new EventInfo(event);
        mEventInfo.add(info[event]);
      }
    }
    final TransitionIterator iter = rel.createAllTransitionsReadOnlyIterator();
    while (iter.advance()) {
      final int event = iter.getCurrentEvent();
      if (info[event] != null) {
        final boolean selfloop =
          iter.getCurrentFromState() == iter.getCurrentToState();
        info[event].addTransition(selfloop);
      }
    }
    Collections.sort(mEventInfo);
  }

  private boolean isControllable(final int event)
  {
    final int supervised = getSupervisedEvent();
    if (supervised >= 0) {
      return event == supervised;
    } else {
      final ListBufferTransitionRelation rel = getTransitionRelation();
      final byte status = rel.getProperEventStatus(event);
      return EventStatus.isControllableEvent(status);
    }
  }


  //#########################################################################
  //# Inner Class EventInfo
  private class EventInfo implements Comparable<EventInfo>
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

    private void addTransition(final boolean selfloop)
    {
      if (selfloop) {
        mNumSelfloopTransitions++;
      } else {
        mNumNonSelfloopTransitions++;
      }
    }

    //#######################################################################
    //# Interface java.util.Comparable<EventInfo>
    @Override
    public int compareTo(final EventInfo info)
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
    private final int mEvent;
    private int mNumNonSelfloopTransitions;
    private int mNumSelfloopTransitions;
  }


  //#########################################################################
  //# Inner Class SupervisorReductionVerifierBuilder
  private class SupervisorReductionVerifierBuilder extends VerifierBuilder
  {
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
  private final VerifierBuilder mVerifierBuilder =
    new SupervisorReductionVerifierBuilder();
  private List<EventInfo> mEventInfo;

}
