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

import gnu.trove.list.array.TLongArrayList;

import java.io.PrintWriter;
import java.io.StringWriter;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.analysis.tr.WatersLongHashingStrategy;
import net.sourceforge.waters.analysis.tr.WatersLongIntHashMap;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.OverflowKind;


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
    mStateLimit = limit;
  }

  /**
   * Gets the state limit.
   * @see #setStateLimit(int) setStateLimit()
   */
  public int getStateLimit()
  {
    return mStateLimit;
  }


  //#########################################################################
  //# Simple Access
  /**
   * Returns whether or not the last invocation found the observer property
   * to be satisfied.
   */
  public boolean getOPResult()
  {
    return mOPResult;
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
  public OPSearchTRSimplifierStatistics getStatistics()
  {
    return (OPSearchTRSimplifierStatistics) super.getStatistics();
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
  protected void setUp()
    throws AnalysisException
  {
    super.setUp();
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int numStates = rel.getNumberOfStates();
    mVerifierStatePairs = new TLongArrayList(numStates);
    mVerifierStateMap =
      new WatersLongIntHashMap(numStates, VerifierPairHashingStrategy.INSTANCE);
    mTransitionIterator1 = rel.createSuccessorsReadOnlyIterator();
    mTransitionIterator2 = rel.createSuccessorsReadOnlyIterator();
    mOPResult = true;
  }

  @Override
  protected boolean runSimplifier()
    throws AnalysisException
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int numStates = rel.getNumberOfStates();
    for (int s = 0; mOPResult && s < numStates; s++) {
      if (rel.isReachable(s)) {
        checkAbort();
        expandVerifierSingleton(s);
      }
    }
    for (int pindex = 0;
         mOPResult && pindex < mVerifierStatePairs.size();
         pindex++) {
      checkAbort();
      final long pair = mVerifierStatePairs.get(pindex);
      expandVerifierPair(pair);
    }
    return mOPResult;
  }

  @Override
  protected void tearDown()
  {
    final OPSearchTRSimplifierStatistics stats = getStatistics();
    if (stats != null && mVerifierStatePairs != null) {
      stats.recordVerifier(mVerifierStatePairs.size());
    }
    mVerifierStatePairs = null;
    mVerifierStateMap = null;
    mTransitionIterator1 = mTransitionIterator2 = null;
  }


  //#########################################################################
  //# Auxiliary Methods
  private void expandVerifierSingleton(final int code)
    throws OverflowException
  {
    int event = Integer.MAX_VALUE;
    mTransitionIterator1.resetState(code);
    while (mTransitionIterator1.advance()) {
      event = mTransitionIterator1.getCurrentEvent();
      if (mTransitionIterator1.getCurrentEvent() == EventEncoding.TAU) {
        final int succ = mTransitionIterator1.getCurrentTargetState();
        enqueueSuccessor(code, succ);
      } else {
        break;
      }
    }

    while (event < Integer.MAX_VALUE) {
      int next = getNextEvent(mTransitionIterator1);
      while (next == event) {
        // It is nondeterministic, so let us find and add all the pairs ...
        final int succ1 = mTransitionIterator1.getCurrentTargetState();
        mTransitionIterator2.reset(code, event);
        while (true) {
          mTransitionIterator2.advance();
          final int succ2 = mTransitionIterator2.getCurrentTargetState();
          if (succ1 == succ2) {
            break;
          }
          enqueueSuccessor(succ1, succ2);
        }
        next = getNextEvent(mTransitionIterator1);
      }
      event = next;
    }
  }

  private void expandVerifierPair(final long pair)
    throws OverflowException
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int lastEvent = rel.getNumberOfProperEvents() - 1;
    mTransitionIterator1.resetEvents(0, lastEvent);
    mTransitionIterator2.resetEvents(0, lastEvent);
    final int code1 = (int) (pair & 0xffffffffL);
    final int code2 = (int) (pair >> 32);

    mTransitionIterator1.resetState(code1);
    int event1 = getNextEvent(mTransitionIterator1);
    boolean entau1 = false;
    while (event1 == EventEncoding.TAU) {
      final int succ1 = mTransitionIterator1.getCurrentTargetState();
      enqueueSuccessor(succ1, code2);
      entau1 = true;
      event1 = getNextEvent(mTransitionIterator1);
    }

    mTransitionIterator2.resetState(code2);
    int event2 = getNextEvent(mTransitionIterator2);
    boolean entau2 = false;
    while (event2 == EventEncoding.TAU) {
      final int succ2 = mTransitionIterator2.getCurrentTargetState();
      enqueueSuccessor(code1, succ2);
      entau2 = true;
      event2 = getNextEvent(mTransitionIterator2);
    }

    for (final int prop : getPropositions()) {
      final boolean marked1 = rel.isMarked(code1, prop);
      final boolean marked2 = rel.isMarked(code2, prop);
      if (marked1 && !marked2 && !entau2) {
        mOPResult = false;
        return;
      } else if (marked2 && !marked1 && !entau1) {
        mOPResult = false;
        return;
      }
    }

    while (event1 < Integer.MAX_VALUE || event2 < Integer.MAX_VALUE) {
      if (event1 < event2) {
        if (entau2) {
          int next1;
          do {
            next1 = getNextEvent(mTransitionIterator1);
          } while (next1 == event1);
          event1 = next1;
        } else {
          mOPResult = false;
          return;
        }
      } else if (event2 < event1) {
        if (entau1) {
          int next2;
          do {
            next2 = getNextEvent(mTransitionIterator2);
          } while (next2 == event2);
          event2 = next2;
        } else {
          mOPResult = false;
          return;
        }
      } else { // event1 == event2
        int next1, next2;
        do {
          final int succ1 = mTransitionIterator1.getCurrentTargetState();
          mTransitionIterator2.resetEvents(event1, lastEvent);
          while (true) {
            next2 = getNextEvent(mTransitionIterator2);
            if (next2 == event2) {
              final int succ2 = mTransitionIterator2.getCurrentTargetState();
              enqueueSuccessor(succ1, succ2);
            } else {
              break;
            }
          }
          next1 = getNextEvent(mTransitionIterator1);
        } while (next1 == event1);
        event1 = next1;
        event2 = next2;
      }
    }
  }

  private void enqueueSuccessor(final int code1, final int code2)
    throws OverflowException
  {
    if (code1 != code2) {
      final long pair = getPair(code1, code2);
      final int lookup = mVerifierStateMap.get(pair);
      if (lookup == 0) {
        final int pindex = mVerifierStateMap.size();
        if (pindex >= mStateLimit) {
          throw new OverflowException(OverflowKind.STATE, mStateLimit);
        }
        final ListBufferTransitionRelation rel = getTransitionRelation();
        final int pcode = pindex + rel.getNumberOfStates();
        mVerifierStatePairs.add(pair);
        mVerifierStateMap.put(pair, pcode);
      }
    }
  }

  private long getPair(final int code1, final int code2)
  {
    if (code1 < code2) {
      return code1 | ((long) code2 << 32);
    } else {
      return code2 | ((long) code1 << 32);
    }
  }

  private int getNextEvent(final TransitionIterator iter)
  {
    if (iter.advance()) {
      return iter.getCurrentEvent();
    } else {
      return Integer.MAX_VALUE;
    }
  }


  //#########################################################################
  //# Debugging
  @Override
  public String toString()
  {
    final StringWriter writer = new StringWriter();
    final PrintWriter printer = new PrintWriter(writer);
    dump(printer);
    printer.flush();
    return writer.toString();
  }

  private void dump(final PrintWriter writer)
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int numStates = rel.getNumberOfStates();
    writer.println("PAIRS");
    for (int i = 0; i < mVerifierStatePairs.size(); i++) {
      final int code = numStates + i;
      final long pair = mVerifierStatePairs.get(i);
      final int p1 = (int) (pair & 0xffffffffL);
      final int p2 = (int) (pair >> 32);
      writer.println("  " + code + ": (" + p1 + '/' + p2 + ')');
    }
  }


  //#########################################################################
  //# Inner Class VerifierPairHashingStrategy
  private static class VerifierPairHashingStrategy
    implements WatersLongHashingStrategy
  {

    //#######################################################################
    //# Interface gnu.trove.TLongHashingStrategy
    @Override
    public int computeHashCode(final long val)
    {
      final int h0 = -2128831035;
      final int h1 = 16777619;
      final int lo = (int) (val & 0xffffffffL);
      final int hi = (int) (val >> 32);
      return ((h0 * h1) ^ hi) * h1 ^ lo;
    }

    @Override
    public boolean equals(final long val1, final long val2)
    {
      return val1 == val2;
    }

    //#######################################################################
    //# Class Constants
    private static final VerifierPairHashingStrategy INSTANCE =
      new VerifierPairHashingStrategy();
    private static final long serialVersionUID = 1L;

  }


  //#########################################################################
  //# Data Members
  private int mStateLimit = Integer.MAX_VALUE;

  private TLongArrayList mVerifierStatePairs;
  private WatersLongIntHashMap mVerifierStateMap;
  private TransitionIterator mTransitionIterator1;
  private TransitionIterator mTransitionIterator2;
  private boolean mOPResult;

}








