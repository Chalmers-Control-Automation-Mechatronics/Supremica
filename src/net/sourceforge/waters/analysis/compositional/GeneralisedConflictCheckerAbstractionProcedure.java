//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.compositional
//# CLASS:   GeneralisedConflictCheckerAbstractionProcedure
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.compositional;

import java.util.Collection;
import java.util.List;

import net.sourceforge.waters.analysis.abstraction.ChainTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.TransitionRelationSimplifier;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.StateEncoding;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;


/**
 * @author Robi Malik
 */

class GeneralisedConflictCheckerAbstractionProcedure extends
  ConflictCheckerAbstractionProcedure
{

  //#########################################################################
  //# Constructors
  GeneralisedConflictCheckerAbstractionProcedure
    (final AbstractCompositionalModelAnalyzer analyzer,
     final ChainTRSimplifier simplifier)
  {
    super(analyzer, simplifier);
  }


  //#########################################################################
  //# Simple Access
  @Override
  ChainTRSimplifier getSimplifier()
  {
    return (ChainTRSimplifier) super.getSimplifier();
  }


  //#########################################################################
  //# Overrides for class TRSimplifierAbstractionProcedure
  @Override
  EventEncoding createEventEncoding(final AutomatonProxy aut,
                                    final EventProxy tau)
  {
    final KindTranslator translator = getKindTranslator();
    final Collection<EventProxy> props = getPropositions();
    final EventEncoding eventEnc =
      new EventEncoding(aut, translator, tau, props,
                        EventEncoding.FILTER_PROPOSITIONS);
    final EventProxy preconditionMarking = getUsedPreconditionMarking();
    mPreconditionMarkingID =
      eventEnc.getEventCode(preconditionMarking);
    if (mPreconditionMarkingID < 0) {
      mPreconditionMarkingID =
        eventEnc.addEvent(preconditionMarking, translator, true);
    }
    final EventProxy defaultMarking = getUsedDefaultMarking();
    mDefaultMarkingID = eventEnc.getEventCode(defaultMarking);
    if (mDefaultMarkingID < 0) {
      mDefaultMarkingID =
        eventEnc.addEvent(defaultMarking, translator, true);
    }
    final TransitionRelationSimplifier simplifier = getSimplifier();
    simplifier.setPropositions(mPreconditionMarkingID, mDefaultMarkingID);
    return eventEnc;
  }

  @Override
  MergeStep createStep(final AutomatonProxy input,
                       final StateEncoding inputStateEnc,
                       final AutomatonProxy output,
                       final StateEncoding outputStateEnc,
                       final EventProxy tau)
  {
    final AbstractCompositionalModelAnalyzer analyzer = getAnalyzer();
    final ChainTRSimplifier simplifier = getSimplifier();
    final List<int[]> partition = simplifier.getResultPartition();
    final boolean reduced =
      simplifier.isReducedMarking(mPreconditionMarkingID);
    if (simplifier.isObservationEquivalentAbstraction()) {
      return new ObservationEquivalenceStep(analyzer, output, input, tau,
                                            inputStateEnc, partition,
                                            reduced, outputStateEnc);
    } else {
      return new ConflictEquivalenceStep(analyzer, output, input, tau,
                                         inputStateEnc, partition,
                                         reduced, outputStateEnc);
    }
  }


  //#######################################################################
  //# Data Members
  private int mPreconditionMarkingID;
  private int mDefaultMarkingID;

}
