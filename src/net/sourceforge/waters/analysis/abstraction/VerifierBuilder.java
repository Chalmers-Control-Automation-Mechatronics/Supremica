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

import gnu.trove.list.array.TLongArrayList;

import java.io.PrintWriter;
import java.io.StringWriter;

import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.analysis.tr.WatersLongHashingStrategy;
import net.sourceforge.waters.analysis.tr.WatersLongIntHashMap;
import net.sourceforge.waters.model.analysis.Abortable;
import net.sourceforge.waters.model.analysis.AnalysisAbortException;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.OverflowKind;


/**
 * <P>A skeleton implementation for verifier-based analysis algorithms.</P>
 *
 * <P>A <I>verifier</I> is constructed from a transition relation
 * (deterministic or nondeterministic) and a set of local events. The local
 * events are hidden, and the state space of the synchronous composition of
 * the input transition relation with itself is explored. While exploring,
 * the state pairs encountered are checked for certain conditions.</P>
 *
 * <P>Several DES algorithms can be implemented through this template.
 * This includes the verification of diagnosability and opacity, and the
 * OP-verifier algorithm to check whether a projection has the observer
 * property.</P>
 *
 * @see OPVerifierTRSimplifier
 *
 * @author Robi Malik
 */

public class VerifierBuilder implements Abortable
{

  //#######################################################################
  //# Constructors
  public VerifierBuilder()
  {
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.Abortable
  @Override
  public void requestAbort()
  {
    mIsAborting = true;
  }

  @Override
  public boolean isAborting()
  {
    return mIsAborting;
  }

  @Override
  public void resetAbort()
  {
    mIsAborting = false;
  }


  //#########################################################################
  //# Configuration
  /**
   * Sets the state limit. The states limit specifies the maximum
   * number of verifier pairs that will be stored in the verifier.
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

  /**
   * Gets the preferred configuration of a transition relation to
   * be processed by a verifier builder.
   * @return {@link ListBufferTransitionRelation#CONFIG_SUCCESSORS}
   */
  public int getPreferredInputConfiguration()
  {
    return ListBufferTransitionRelation.CONFIG_SUCCESSORS;
  }


  //#########################################################################
  //# Simple Access
  protected TransitionIterator getIterator1()
  {
    return mTransitionIterator1;
  }

  protected TransitionIterator getIterator2()
  {
    return mTransitionIterator2;
  }

  protected boolean setFailedResult()
  {
    return mVerificationSuccess = false;
  }


  //#########################################################################
  //# Invocation
  public boolean buildVerifier(final ListBufferTransitionRelation rel)
    throws AnalysisException
  {
    try {
      mTransitionRelation = rel;
      setUp();
      return buildVerifier();
    } finally {
      tearDown();
    }
  }

  public boolean isVerificationSuccess()
  {
    return mVerificationSuccess;
  }

  public boolean isOPSatisfied()
  {
    return mOPSatisfied;
  }

  public int getNumberOfPairs()
  {
    return mNumberOfPairs;
  }


  //#########################################################################
  //# Algorithm
  protected void setUp()
    throws AnalysisException
  {
    final int numStates = mTransitionRelation.getNumberOfStates();
    mNumberOfPairs = -1;
    mVerifierStatePairs = new TLongArrayList(numStates);
    mVerifierStateMap =
      new WatersLongIntHashMap(numStates, VerifierPairHashingStrategy.INSTANCE);
    mTransitionRelation.reconfigure(ListBufferTransitionRelation.CONFIG_SUCCESSORS);
    mTransitionIterator1 = mTransitionRelation.createSuccessorsReadOnlyIterator();
    mTransitionIterator2 = mTransitionRelation.createSuccessorsReadOnlyIterator();
    mVerificationSuccess = mOPSatisfied = true;
  }

  protected boolean buildVerifier()
    throws AnalysisException
  {
    final int numStates = mTransitionRelation.getNumberOfStates();
    for (int s = 0; mVerificationSuccess && s < numStates; s++) {
      if (mTransitionRelation.isReachable(s)) {
        checkAbort();
        expandVerifierSingleton(s);
      }
    }
    for (int pindex = 0;
         mVerificationSuccess && pindex < mVerifierStatePairs.size();
         pindex++) {
      checkAbort();
      final long pair = mVerifierStatePairs.get(pindex);
      expandVerifierPair(pair);
    }
    return mVerificationSuccess;
  }

  protected void tearDown()
  {
    mNumberOfPairs = mVerifierStatePairs.size();
    mTransitionRelation = null;
    mVerifierStatePairs = null;
    mVerifierStateMap = null;
    mTransitionIterator1 = mTransitionIterator2 = null;
  }


  //#########################################################################
  //# Auxiliary Methods
  protected void expandVerifierSingleton(final int code)
    throws OverflowException
  {
    int prev = Integer.MAX_VALUE;
    mTransitionIterator1.resetState(code);
    while (mTransitionIterator1.advance()) {
      final int event = mTransitionIterator1.getCurrentEvent();
      final byte status = mTransitionRelation.getProperEventStatus(event);
      if (EventStatus.isLocalEvent(status)) {
        final int succ = mTransitionIterator1.getCurrentTargetState();
        enqueueSuccessor(code, succ);
        if (!mVerificationSuccess) {
          return;
        }
      } else if (event == prev) {
        // It is nondeterministic, so let us find and add all the pairs ...
        final int succ1 = mTransitionIterator1.getCurrentTargetState();
        mTransitionIterator2.reset(code, event);
        while (mTransitionIterator2.advance()) {
          final int succ2 = mTransitionIterator2.getCurrentTargetState();
          if (succ1 == succ2) {
            break;
          }
          enqueueSuccessor(succ1, succ2);
          if (!mVerificationSuccess) {
            return;
          }
        }
      } else {
        prev = event;
      }
    }
  }

  protected void expandVerifierPair(final long pair)
    throws OverflowException
  {
    final int lastEvent = mTransitionRelation.getNumberOfProperEvents() - 1;
    final int code1 = (int) (pair & 0xffffffffL);
    final int code2 = (int) (pair >> 32);
    mTransitionIterator1.resetEvents(0, lastEvent);
    mTransitionIterator1.resetState(code1);
    mTransitionIterator1.advance();
    mTransitionIterator2.resetEvents(0, lastEvent);
    mTransitionIterator2.resetState(code2);
    mTransitionIterator2.advance();
    boolean hasLocal = false;
    boolean hasMismatch = false;
    while (mTransitionIterator1.isValid() || mTransitionIterator2.isValid()) {
      while (mTransitionIterator1.isValid()) {
        final int event1 = mTransitionIterator1.getCurrentEvent();
        final byte status = mTransitionRelation.getProperEventStatus(event1);
        if (EventStatus.isLocalEvent(status)) {
          final int succ1 = mTransitionIterator1.getCurrentTargetState();
          enqueueSuccessor(succ1, code2);
          if (!mVerificationSuccess) {
            return;
          }
          mTransitionIterator1.advance();
          hasLocal = true;
        } else {
          break;
        }
      }
      while (mTransitionIterator2.isValid()) {
        final int event2 = mTransitionIterator2.getCurrentEvent();
        final byte status = mTransitionRelation.getProperEventStatus(event2);
        if (EventStatus.isLocalEvent(status)) {
          final int succ2 = mTransitionIterator2.getCurrentTargetState();
          enqueueSuccessor(code1, succ2);
          if (!mVerificationSuccess) {
            return;
          }
          mTransitionIterator2.advance();
          hasLocal = true;
        } else {
          break;
        }
      }
      if (mTransitionIterator1.isValid() && mTransitionIterator2.isValid()) {
        final int event1 = mTransitionIterator1.getCurrentEvent();
        final int event2 = mTransitionIterator2.getCurrentEvent();
        if (event1 == event2) {
          int succ1 = mTransitionIterator1.getCurrentTargetState();
          int succ2 = mTransitionIterator2.getCurrentTargetState();
          enqueueSuccessor(succ1, succ2);
          if (!mVerificationSuccess) {
            return;
          }
          while (mTransitionIterator2.advance() &&
                 mTransitionIterator2.getCurrentEvent() == event2) {
            succ2 = mTransitionIterator2.getCurrentTargetState();
            enqueueSuccessor(succ1, succ2);
            if (!mVerificationSuccess) {
              return;
            }
          }
          while (mTransitionIterator1.advance() &&
                 mTransitionIterator1.getCurrentEvent() == event1) {
            succ1 = mTransitionIterator1.getCurrentTargetState();
            mTransitionIterator2.resetEvents(event2, lastEvent);
            while (mTransitionIterator2.advance() &&
                   mTransitionIterator2.getCurrentEvent() == event2) {
              succ2 = mTransitionIterator2.getCurrentTargetState();
              enqueueSuccessor(succ1, succ2);
              if (!mVerificationSuccess) {
                return;
              }
            }
          }
        } else if (event1 < event2) {
          hasMismatch = true;
          mTransitionIterator1.advance();
        } else {
          hasMismatch = true;
          mTransitionIterator2.advance();
        }
      } else if (mTransitionIterator1.isValid()) {
        hasMismatch = true;
        mTransitionIterator1.advance();
      } else if (mTransitionIterator2.isValid()) {
        hasMismatch = true;
        mTransitionIterator2.advance();
      }
    }
    if (!hasLocal && hasMismatch) {
      mOPSatisfied = false;
    }
  }

  protected void enqueueSuccessor(final int code1, final int code2)
    throws OverflowException
  {
    if (code1 != code2) {
      final long pair = getPair(code1, code2);
      final int lookup = mVerifierStateMap.get(pair);
      if (lookup == 0 && newStatePair(code1, code2)) {
        final int pindex = mVerifierStateMap.size();
        if (pindex >= mStateLimit) {
          throw new OverflowException(OverflowKind.STATE, mStateLimit);
        }
        final int pcode = pindex + mTransitionRelation.getNumberOfStates();
        mVerifierStatePairs.add(pair);
        mVerifierStateMap.put(pair, pcode);
      }
    }
  }

  protected boolean newStatePair(final int code1, final int code2)
  {
    return true;
  }

  protected long getPair(final int code1, final int code2)
  {
    if (code1 < code2) {
      return code1 | ((long) code2 << 32);
    } else {
      return code2 | ((long) code1 << 32);
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
    final int numStates = mTransitionRelation.getNumberOfStates();
    writer.println("PAIRS");
    for (int i = 0; i < mVerifierStatePairs.size(); i++) {
      final int code = numStates + i;
      final long pair = mVerifierStatePairs.get(i);
      final int p1 = (int) (pair & 0xffffffffL);
      final int p2 = (int) (pair >> 32);
      writer.println("  " + code + ": (" + p1 + '/' + p2 + ')');
    }
  }

  /**
   * Checks whether the verifier builder has been requested to abort,
   * and if so, performs the abort by throwing an {@link AnalysisAbortException}.
   */
  protected void checkAbort()
    throws AnalysisAbortException
  {
    if (mIsAborting) {
      final AnalysisAbortException exception = new AnalysisAbortException();
      throw exception;
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
    private static final long serialVersionUID = 8090001263554800555L;
  }


  //#########################################################################
  //# Data Members
  private int mStateLimit = Integer.MAX_VALUE;
  private ListBufferTransitionRelation mTransitionRelation;

  private boolean mVerificationSuccess = false;
  private boolean mOPSatisfied = false;
  private int mNumberOfPairs = -1;

  private TLongArrayList mVerifierStatePairs;
  private WatersLongIntHashMap mVerifierStateMap;
  private TransitionIterator mTransitionIterator1;
  private TransitionIterator mTransitionIterator2;
  private boolean mIsAborting;

}
