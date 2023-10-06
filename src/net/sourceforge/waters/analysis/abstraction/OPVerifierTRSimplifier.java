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

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.OverflowException;


/**
 * <P>A transition relation simplifier that checks for a given automaton
 * whether its natural projection removing tau events satisfies the
 * observer property, using the OP-Verifier algorithm.</P>
 *
 * <P>This is a lightweight implementation only of OP-Verifier. The input
 * transition relation is assumed to be tau-loop free. If this is not
 * the case, {@link TauLoopRemovalTRSimplifier} should be called before,
 * or {@link OPVerifierTRChain} should be used instead. Unlike {@link
 * OPSearchAutomatonSimplifier}, this class has no support for OP-Search.</P>
 *
 * <P><I>References:</I><BR>
 * Patr&iacute;cia N. Pena and Jos&eacute; E. R. Cury and St&eacute;phane
 * Lafortune. Polynomial-time verification of the observer property in
 * abstractions. Proc. 2008 American Control Conference, Seattle,
 * Washington, USA, 465-470, 2008.</P>
 *
 * @author Robi Malik
 */

public class OPVerifierTRSimplifier
  extends AbstractMarkingTRSimplifier
{

  //#######################################################################
  //# Constructors
  public OPVerifierTRSimplifier()
  {
  }

  public OPVerifierTRSimplifier(final ListBufferTransitionRelation rel)
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
  public void setStateLimit(final int limit)
  {
    mVerifierBuilder.setStateLimit(limit);
  }

  /**
   * Gets the state limit.
   * @see #setStateLimit(int) setStateLimit()
   */
  public int getStateLimit()
  {
    return mVerifierBuilder.getStateLimit();
  }


  //#########################################################################
  //# Simple Access
  /**
   * Returns whether or not the last invocation found the observer property
   * to be satisfied.
   */
  public boolean getOPResult()
  {
    return mVerifierBuilder.isVerificationSuccess();
  }


  //#########################################################################
  //# Interface
  //# net.sourceforge.waters.analysis.abstraction.TransitionRelationSimplifier
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
  public OPSearchTRSimplifierStatistics getStatistics()
  {
    return (OPSearchTRSimplifierStatistics) super.getStatistics();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.Abortable
  @Override
  public void requestAbort()
  {
    super.requestAbort();
    mVerifierBuilder.requestAbort();
  }

  @Override
  public void resetAbort()
  {
    super.resetAbort();
    mVerifierBuilder.resetAbort();
  }


  //#########################################################################
  //# Overrides for
  //# net.sourceforge.waters.analysis.abstraction.AbstractTRSimplifier
  @Override
  public TRSimplifierStatistics createStatistics()
  {
    final TRSimplifierStatistics stats =
      new OPSearchTRSimplifierStatistics(this, true, false);
    return setStatistics(stats);
  }

  @Override
  protected boolean runSimplifier()
    throws AnalysisException
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    return mVerifierBuilder.buildVerifier(rel);
  }

  @Override
  protected void tearDown()
  {
    final OPSearchTRSimplifierStatistics stats = getStatistics();
    if (stats != null) {
      final int numPairs = mVerifierBuilder.getNumberOfPairs();
      stats.recordVerifier(numPairs);
    }
  }


  //#########################################################################
  //# InnerClass OPVerifierBuilder
  private class OPVerifierBuilder extends VerifierBuilder
  {
    //#######################################################################
    //# Overrides for
    //# net.sourceforge.waters.analysis.abstraction.VerifierBuilder
    @Override
    protected void expandVerifierPair(final long pair)
      throws OverflowException
    {
      final TransitionIterator iter1 = getIterator1();
      final TransitionIterator iter2 = getIterator2();
      final ListBufferTransitionRelation rel = getTransitionRelation();
      final int lastEvent = rel.getNumberOfProperEvents() - 1;
      iter1.resetEvents(0, lastEvent);
      iter2.resetEvents(0, lastEvent);
      final int code1 = (int) (pair & 0xffffffffL);
      final int code2 = (int) (pair >> 32);

      iter1.resetState(code1);
      int event1 = getNextEvent(iter1);
      boolean entau1 = false;
      while (event1 == EventEncoding.TAU) {
        final int succ1 = iter1.getCurrentTargetState();
        enqueueSuccessor(succ1, code2);
        entau1 = true;
        event1 = getNextEvent(iter1);
      }

      iter2.resetState(code2);
      int event2 = getNextEvent(iter2);
      boolean entau2 = false;
      while (event2 == EventEncoding.TAU) {
        final int succ2 = iter2.getCurrentTargetState();
        enqueueSuccessor(code1, succ2);
        entau2 = true;
        event2 = getNextEvent(iter2);
      }

      for (final int prop : getPropositions()) {
        final boolean marked1 = rel.isMarked(code1, prop);
        final boolean marked2 = rel.isMarked(code2, prop);
        if (marked1 && !marked2 && !entau2) {
          setFailedResult();
          return;
        } else if (marked2 && !marked1 && !entau1) {
          setFailedResult();
          return;
        }
      }

      while (event1 < Integer.MAX_VALUE || event2 < Integer.MAX_VALUE) {
        if (event1 < event2) {
          if (entau2) {
            int next1;
            do {
              next1 = getNextEvent(iter1);
            } while (next1 == event1);
            event1 = next1;
          } else {
            setFailedResult();
            return;
          }
        } else if (event2 < event1) {
          if (entau1) {
            int next2;
            do {
              next2 = getNextEvent(iter2);
            } while (next2 == event2);
            event2 = next2;
          } else {
            setFailedResult();
            return;
          }
        } else { // event1 == event2
          int next1, next2;
          do {
            final int succ1 = iter1.getCurrentTargetState();
            iter2.resetEvents(event1, lastEvent);
            while (true) {
              next2 = getNextEvent(iter2);
              if (next2 == event2) {
                final int succ2 = iter2.getCurrentTargetState();
                enqueueSuccessor(succ1, succ2);
              } else {
                break;
              }
            }
            next1 = getNextEvent(iter1);
          } while (next1 == event1);
          event1 = next1;
          event2 = next2;
        }
      }
    }

    //#######################################################################
    //# Auxiliary Methods
    private int getNextEvent(final TransitionIterator iter)
    {
      if (iter.advance()) {
        return iter.getCurrentEvent();
      } else {
        return Integer.MAX_VALUE;
      }
    }
  }


  //#########################################################################
  //# Data Members
  private final VerifierBuilder mVerifierBuilder = new OPVerifierBuilder();

}
