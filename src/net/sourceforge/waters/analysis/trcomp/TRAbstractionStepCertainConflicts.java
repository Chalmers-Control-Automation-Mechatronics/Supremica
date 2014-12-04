//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.trcomp
//# CLASS:   TRAbstractionStepCertainConflicts
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.trcomp;

import java.util.Collection;
import java.util.Collections;

import net.sourceforge.waters.analysis.abstraction.LimitedCertainConflictsTRSimplifier;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TRAutomatonProxy;
import net.sourceforge.waters.model.analysis.AnalysisException;

import org.apache.log4j.Logger;


/**
 * An abstraction step representing certain conflicts simplification.
 *
 * @author Robi Malik
 */

class TRAbstractionStepCertainConflicts
  extends TRAbstractionStep
{

  //#########################################################################
  //# Constructor
  TRAbstractionStepCertainConflicts(final TRAbstractionStep pred,
                                    final EventEncoding eventEncoding,
                                    final int defaultMarking,
                                    final LimitedCertainConflictsTRSimplifier simplifier)
  {
    super(pred.getName());
    mPredecessor = pred;
    mEventEncoding = eventEncoding;
    mDefaultMarking = defaultMarking;
    mSimplifier = simplifier;
    mLevels = simplifier.getLevels();
  }


  //#########################################################################
  //# Simple Access
  TRAbstractionStep getPredecessor()
  {
    return mPredecessor;
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
    throws AnalysisException
  {
    mSimplifier.setDefaultMarkingID(mDefaultMarking);
    mSimplifier.setPreferredOutputConfiguration(preferredConfig);
    final int inputConfig = mSimplifier.getPreferredInputConfiguration();
    final TRAutomatonProxy inputAut =
      mPredecessor.getOutputAutomaton(inputConfig);
    // We are going to destructively change this automaton,
    // so we need to clear the copy cached on the predecessor.
    mPredecessor.clearOutputAutomaton();
    final ListBufferTransitionRelation inputRel =
      inputAut.getTransitionRelation();
    final EventEncoding inputEventEncoding = new EventEncoding(mEventEncoding);
    final ListBufferTransitionRelation outputRel =
      new ListBufferTransitionRelation(inputRel, inputEventEncoding, inputConfig);
    mSimplifier.setTransitionRelation(outputRel);
    mSimplifier.run();
    return new TRAutomatonProxy(inputEventEncoding, outputRel);
  }

  @Override
  public void expandTrace(final TRTraceProxy trace)
    throws AnalysisException
  {
    // TODO
    assert false : "Certain conflicts trace expansion not yet implemented!";
  }


  //#########################################################################
  //# Debugging
  @Override
  public void report(final Logger logger)
  {
    if (logger.isDebugEnabled()) {
      logger.debug("Expanding certain conflicts of " + getName() + " ...");
    }
  }


  //#########################################################################
  //# Data Members
  private final TRAbstractionStep mPredecessor;
  private final EventEncoding mEventEncoding;
  private final int mDefaultMarking;
  private final LimitedCertainConflictsTRSimplifier mSimplifier;
  @SuppressWarnings("unused")
  private final int[] mLevels;

}
