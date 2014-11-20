//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.trcomp
//# CLASS:   TRAbstractionStepDrop
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.trcomp;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TRAutomatonProxy;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.xsd.base.ComponentKind;


/**
 * An abstraction step representing a trivial or nonblocking automaton
 * that was dropped during compositional verification.
 *
 * @author Robi Malik
 */

class TRAbstractionStepDrop
  extends TRAbstractionStep
{

  //#########################################################################
  //# Constructor
  TRAbstractionStepDrop(final TRAbstractionStep pred,
                        final TRAutomatonProxy aut)
  {
    mPredecessor = pred;
    final ListBufferTransitionRelation rel = aut.getTransitionRelation();
    final int numStates = rel.getNumberOfStates();
    int init = -1;
    for (int s = 0; s < numStates; s++) {
      if (rel.isInitial(s)) {
        init = s;
        break;
      }
    }
    assert init >= 0;
    mInitialState = init;
    pred.setSuccessor(this);
  }

  TRAbstractionStepDrop(final TRAbstractionStep pred,
                        final int initialState)
  {
    mPredecessor = pred;
    mInitialState = initialState;
    pred.setSuccessor(this);
  }


  //#########################################################################
  //# Simple Access
  public int getInitialState()
  {
    return mInitialState;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.analysis.trcomp.TRAbstractionStep
  @Override
  public Collection<TRAbstractionStep> getPredecessors()
  {
    return Collections.singletonList(mPredecessor);
  }

  @Override
  public TRAutomatonProxy createOutputAutomaton(final int preferredConfig)
    throws OverflowException
  {
    final EventEncoding enc = new EventEncoding();
    final int numStates = mInitialState == 0 ? 2 : mInitialState + 1;
    final int dumpIndex = mInitialState == 0 ? 1 : 0;
    final ListBufferTransitionRelation rel =
      new ListBufferTransitionRelation(":dummy",
                                       ComponentKind.PLANT,
                                       enc, numStates, dumpIndex,
                                       preferredConfig);
    for (int s = 0; s < numStates; s++) {
      final boolean init = s == mInitialState;
      rel.setReachable(s, init);
      rel.setInitial(s, init);
    }
    return new TRAutomatonProxy(enc, rel);
  }

  @Override
  public void expandTrace(final TRTraceProxy trace)
  {
    final int numSteps = trace.getNumberOfSteps();
    final int[] states = new int[numSteps];
    Arrays.fill(states, mInitialState);
    trace.addAutomaton(mPredecessor, states);
  }


  //#########################################################################
  //# Data Members
  private final TRAbstractionStep mPredecessor;
  private final int mInitialState;

}
