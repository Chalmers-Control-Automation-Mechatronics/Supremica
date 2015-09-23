//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
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

import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;


/**
 * <P>A transition relation simplifier that is aware of state markings.</P>
 *
 * <P>This class implements support for two marking propositions:</P>
 * <UL>
 * <LI>The default marking (omega), which indicates termination.
 *     It is the standard marking proposition used by nonblocking and
 *     nonconflicting checks.</LI>
 * <LI>The precondition marking (alpha), which is used for generalised
 *     nonblocking.</LI>
 * </UL>
 * <P>Both markings are specified by their ID within the transition relation
 * to be simplified, which means that they may have to be changed when
 * a new transition relation is passed to the simplifier. The markings may
 * be absent in a given transition relation, or irrelevant for a particular
 * algorithm, in which case an ID of&nbsp;<CODE>-1</CODE> is used to specify
 * a missing marking.</P>
 *
 * @author Robi Malik
 */

public abstract class AbstractMarkingTRSimplifier
  extends AbstractTRSimplifier
{

  //#########################################################################
  //# Constructors
  public AbstractMarkingTRSimplifier()
  {
    this(null);
  }

  public AbstractMarkingTRSimplifier
    (final ListBufferTransitionRelation rel)
  {
    super(rel);
    mPreconditionMarkingID = mDefaultMarkingID = -1;
    mPropositions = null;
  }


  //#########################################################################
  //# Configuration
  /**
   * Sets the marking IDs used by this simplifier.
   * @param preconditionID
   *          ID of precondition marking proposition,
   *          or <CODE>-1</CODE> if unused or not present.
   * @param defaultID
   *          ID of default marking proposition,
   *          or <CODE>-1</CODE> if unused or not present.
   */
  @Override
  public void setPropositions(final int preconditionID, final int defaultID)
  {
    mPreconditionMarkingID = preconditionID;
    mDefaultMarkingID = defaultID;
    mPropositions = null;
  }

  public int getPreconditionMarkingID()
  {
    return mPreconditionMarkingID;
  }

  public int getDefaultMarkingID()
  {
    return mDefaultMarkingID;
  }

  public int[] getPropositions()
  {
    if (mPropositions == null) {
      if (mPreconditionMarkingID < 0) {
        if (mDefaultMarkingID < 0) {
          mPropositions = new int[0];
        } else {
          mPropositions = new int[1];
          mPropositions[0] = mDefaultMarkingID;
        }
      } else {
        if (mDefaultMarkingID < 0) {
          mPropositions = new int[1];
          mPropositions[0] = mPreconditionMarkingID;
        } else {
          mPropositions = new int[2];
          mPropositions[0] = mDefaultMarkingID;
          mPropositions[1] = mPreconditionMarkingID;
        }
      }
    }
    return mPropositions;
  }

  /**
   * Returns whether this simplifier respects deadlock states when removing
   * selfloops.
   * This method affects how the {@link #removeProperSelfLoopEvents()}
   * checks for selfloops. If the simplifier is deadlock aware, then
   * events not enabled in deadlock states can still be considered as
   * selfloop events and removed.
   * The default implementation returns <CODE>false</CODE> as deadlock
   * awareness only preservers conflict equivalence and should not be
   * used in general.
   */
  public boolean isDumpStateAware()
  {
    return false;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.analysis.abstraction.
  //# TransitionRelationSimplifier
  @Override
  public boolean isPartitioning()
  {
    return true;
  }

  @Override
  public TRSimplifierStatistics createStatistics()
  {
    final TRSimplifierStatistics stats =
      new TRSimplifierStatistics(this, true, false);
    return setStatistics(stats);
  }


  //#########################################################################
  //# Automaton Simplification
  /**
   * Attempts to simplify the automaton by removing redundant selfloop events.
   * This method searches for any non-tau events that appear only as
   * selfloops and are selflooped in all states of the transition
   * relation, marks such events as unused, and removes the selfloops from the
   * transition relation. Deadlock states are skipped in the search if the
   * simplifier is configured to be deadlock aware.
   * @return <CODE>true</CODE> if at least one event was removed,
   *         <CODE>false</CODE> otherwise.
   * @see #isDumpStateAware()
   */
  protected boolean removeProperSelfLoopEvents()
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    if (isDumpStateAware() && mDefaultMarkingID >= 0) {
      return rel.removeProperSelfLoopEvents(mDefaultMarkingID);
    } else {
      return rel.removeProperSelfLoopEvents();
    }
  }


  //#########################################################################
  //# Data Members
  private int mPreconditionMarkingID;
  private int mDefaultMarkingID;
  private int[] mPropositions;

}
