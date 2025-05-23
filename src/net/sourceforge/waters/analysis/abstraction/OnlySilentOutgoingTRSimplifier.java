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

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisException;


/**
 * <P>A list buffer transition relation implementation of the
 * <I>Only Silent Outgoing Rule</I>.</P>
 *
 * <P>The original <I>Only Silent Outgoing Rule</I> (Flordal &amp; Malik,
 * 2009) removes all states&nbsp;<I>x</I> that do not have the precondition
 * or default marking, if they only have outgoing &tau; transitions. The
 * incoming transitions to&nbsp;<I>x</I> are redirected to all the (&tau;)
 * successor states of&nbsp;<I>x</I>. If <I>x</I> is an initial state,
 * then its successors become initial states.</P>
 *
 * <P>This simplifier implements a slightly enhanced version that allows
 * the state&nbsp;<I>x</I> to have the default marking. If <I>x</I> is
 * marked and at least one of its (&tau;) successors is also marked, then
 * <I>x</I> can still be removed as above.</P>
 *
 * <P><I>References:</I><BR>
 * Hugo Flordal, Robi Malik. Compositional Verification in Supervisory Control.
 * SIAM Journal of Control and Optimization, <STRONG>48</STRONG>(3),
 * 1914&ndash;1938, 2009.<BR>
 * Robi Malik, Ryan Leduc. Compositional Nonblocking Verification Using
 * Generalised Nonblocking Abstractions, IEEE Transactions on Automatic
 * Control, <STRONG>58</STRONG>(8), 1&ndash;13, 2013.</P>
 *
 * @author Rachel Francis, Robi Malik
 */

public class OnlySilentOutgoingTRSimplifier
  extends AbstractMarkingTRSimplifier
{

  //#########################################################################
  //# Constructors
  public OnlySilentOutgoingTRSimplifier()
  {
  }

  public OnlySilentOutgoingTRSimplifier(final ListBufferTransitionRelation rel)
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


  //#########################################################################
  //# Interface
  //# net.sourceforge.waters.analysis.abstraction.TransitionRelationSimplifier
  @Override
  public int getPreferredInputConfiguration()
  {
    return ListBufferTransitionRelation.CONFIG_ALL;
  }

  @Override
  public boolean isObservationEquivalentAbstraction()
  {
    return true;
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.analysis.abstraction.AbstractTRSimplifier
  @Override
  protected boolean runSimplifier()
  throws AnalysisException
  {
    final int alphaID = getPreconditionMarkingID();
    final int omegaID = getDefaultMarkingID();
    final int tauID = EventEncoding.TAU;
    final ListBufferTransitionRelation rel = getTransitionRelation();
    if ((rel.getProperEventStatus(tauID) & EventStatus.STATUS_UNUSED) != 0) {
      return false;
    }
    final TransitionIterator iter = rel.createSuccessorsReadOnlyIterator();
    final TIntArrayList targets = new TIntArrayList();
    final int numStates = rel.getNumberOfStates();
    boolean modified = false;

    main:
    for (int source = 0; source < numStates; source++) {
      if (rel.isReachable(source) &&
          (alphaID < 0 || !rel.isMarked(source, alphaID))) {
        checkAbort();
        boolean markingNeeded =
          omegaID >= 0 && rel.isMarked(source, omegaID);
        iter.resetState(source);
        while (iter.advance()) {
          final int eventID = iter.getCurrentEvent();
          if (eventID == tauID) {
            final int target = iter.getCurrentTargetState();
            targets.add(target);
            markingNeeded &= !rel.isMarked(target, omegaID);
          } else {
            targets.clear();
            continue main;
          }
        }
        if (!targets.isEmpty()) {
          if (!markingNeeded) {
            rel.removeOutgoingTransitions(source, tauID);
            for (int i = 0; i < targets.size(); i++) {
              final int target = targets.get(i);
              rel.copyIncomingTransitions(source, target);
            }
            rel.removeIncomingTransitions(source);
            rel.setReachable(source, false);
            modified = true;
          }
          targets.clear();
        }
      }
    }
    if (modified) {
      applyResultPartitionAutomatically();
    }
    return modified;
  }

  @Override
  protected void applyResultPartition()
  throws AnalysisException
  {
    super.applyResultPartition();
    final ListBufferTransitionRelation rel = getTransitionRelation();
    rel.removeTauSelfLoops();
    rel.removeRedundantPropositions();
    removeProperSelfLoopEvents();
  }


  //#########################################################################
  //# Data Members
  private boolean mDumpStateAware = false;

}
