//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.compositional
//# CLASS:   TRSimplifierAbstractionProcedure
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.compositional;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.waters.analysis.abstraction.TransitionRelationSimplifier;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.StateEncoding;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * @author Robi Malik
 */
abstract class TRAbstractionProcedure
  extends AbstractAbstractionProcedure
{

  //#########################################################################
  //# Constructor
  TRAbstractionProcedure
    (final AbstractCompositionalModelAnalyzer analyzer,
     final TransitionRelationSimplifier simplifier)
  {
    super(analyzer);
    mSimplifier = simplifier;
  }


  //#########################################################################
  //# Overrides for AbstractionProcedure
  @Override
  public boolean run(final AutomatonProxy aut,
                     final Collection<EventProxy> local,
                     final List<AbstractionStep> steps)
    throws AnalysisException
  {
    try {
      assert local.size() <= 1 : "At most one tau event supported!";
      final Iterator<EventProxy> iter = local.iterator();
      final EventProxy tau = iter.hasNext() ? iter.next() : null;
      final EventEncoding eventEnc = createEventEncoding(aut, tau);
      final StateEncoding inputStateEnc = new StateEncoding(aut);
      final int config = mSimplifier.getPreferredInputConfiguration();
      ListBufferTransitionRelation rel =
        new ListBufferTransitionRelation(aut, eventEnc,
                                         inputStateEnc, config);
      getAnalyzer().showDebugLog(rel);
      mSimplifier.setTransitionRelation(rel);
      final int numStates = rel.getNumberOfStates();
      final int numTrans = rel.getNumberOfTransitions();
      final int numMarkings = rel.getNumberOfMarkings();
      mSimplifier.setTransitionRelation(rel);
      if (mSimplifier.run()) {
        rel = mSimplifier.getTransitionRelation();
        if (rel.getNumberOfReachableStates() == numStates &&
          rel.getNumberOfTransitions() == numTrans &&
          rel.getNumberOfMarkings() == numMarkings) {
          return false;
        }
        rel.removeRedundantPropositions();
        final ProductDESProxyFactory factory = getFactory();
        final StateEncoding outputStateEnc = new StateEncoding();
        final AutomatonProxy convertedAut =
          rel.createAutomaton(factory, eventEnc, outputStateEnc);
        final AbstractionStep step = createStep
          (aut, inputStateEnc, convertedAut, outputStateEnc, tau);
        steps.add(step);
        return true;
      } else {
        return false;
      }
    } finally {
      mSimplifier.reset();
    }
                       }

  @Override
  public void storeStatistics()
  {
    final CompositionalAnalysisResult result = getAnalysisResult();
    result.setSimplifierStatistics(mSimplifier);
  }

  @Override
  public void resetStatistics()
  {
    mSimplifier.createStatistics();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.Abortable
  public void requestAbort()
  {
    mSimplifier.requestAbort();
  }

  public boolean isAborting()
  {
    return mSimplifier.isAborting();
  }


  //#########################################################################
  //# Simple Access
  TransitionRelationSimplifier getSimplifier()
  {
    return mSimplifier;
  }


  //#########################################################################
  //# Auxiliary Methods
  EventEncoding createEventEncoding(final AutomatonProxy aut,
                                    final EventProxy tau)
  {
    final KindTranslator translator = getKindTranslator();
    Collection<EventProxy> filter = getPropositions();
    if (filter == null) {
      filter = Collections.emptyList();
    }
    return new EventEncoding(aut, translator, tau, filter,
                             EventEncoding.FILTER_PROPOSITIONS);
  }

  abstract AbstractionStep createStep
    (final AutomatonProxy input,
     final StateEncoding inputStateEnc,
     final AutomatonProxy output,
     final StateEncoding outputStateEnc,
     final EventProxy tau);


  //#########################################################################
  //# Data Members
  private final TransitionRelationSimplifier mSimplifier;

}
