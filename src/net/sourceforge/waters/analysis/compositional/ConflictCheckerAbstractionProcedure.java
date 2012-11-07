//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.compositional
//# CLASS:   ConflictCheckerAbstractionProcedure
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.compositional;

import java.util.List;

import net.sourceforge.waters.analysis.abstraction.TransitionRelationSimplifier;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.StateEncoding;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;


/**
 * @author Robi Malik
 */

class ConflictCheckerAbstractionProcedure extends
  TRSimplifierAbstractionProcedure
{

  //#########################################################################
  //# Constructor
  ConflictCheckerAbstractionProcedure
    (final AbstractCompositionalModelAnalyzer analyzer,
     final TransitionRelationSimplifier simplifier)
  {
    super(analyzer, simplifier);
  }


  //#########################################################################
  //# Overrides for TRSimplifierAbstractionProcedure
  @Override
  EventEncoding createEventEncoding(final AutomatonProxy aut,
                                    final EventProxy tau)
  {
    final EventEncoding eventEnc =
      super.createEventEncoding(aut, tau);
    final TransitionRelationSimplifier simplifier = getSimplifier();
    final EventProxy omega = getUsedDefaultMarking();
    final int omegaID = eventEnc.getEventCode(omega);
    simplifier.setDefaultMarkingID(omegaID);
    return eventEnc;
  }

  @Override
  AbstractionStep createStep(final AutomatonProxy input,
                             final StateEncoding inputStateEnc,
                             final AutomatonProxy output,
                             final StateEncoding outputStateEnc,
                             final EventProxy tau)
  {
    final AbstractCompositionalModelAnalyzer analyzer = getAnalyzer();
    final TransitionRelationSimplifier simplifier = getSimplifier();
    final List<int[]> partition = simplifier.getResultPartition();
    if (simplifier.isObservationEquivalentAbstraction()) {
      return new ObservationEquivalenceStep(analyzer, output, input, tau,
                                            inputStateEnc, partition,
                                            false, outputStateEnc);
    } else {
      return new ConflictEquivalenceStep(analyzer, output, input, tau,
                                         inputStateEnc, partition,
                                         false, outputStateEnc);
    }
  }

}
