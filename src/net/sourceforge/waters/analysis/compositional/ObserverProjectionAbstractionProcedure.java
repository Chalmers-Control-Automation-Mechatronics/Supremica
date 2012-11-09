//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.compositional
//# CLASS:   ObserverProjectionAbstractionProcedure
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.compositional;

import net.sourceforge.waters.analysis.abstraction.ChainTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.ObservationEquivalenceTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.ObserverProjectionTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.TauLoopRemovalTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.TransitionRelationSimplifier;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.model.analysis.IdenticalKindTranslator;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.xsd.base.EventKind;


/**
 * An abstraction procedure that minimises automata according using
 * <I>observer projection</I>. The present implementation determines a
 * coarsest causal reporter map satisfying the observer property.
 * Nondeterminism in the projected automata is not resolved, nondeterministic
 * abstractions are used instead.
 *
 * @author Robi Malik
 */

class ObserverProjectionAbstractionProcedure
  extends TRConflictEquivalenceAbstractionProcedure
{

  //#########################################################################
  //# Factory Methods
  public static AbstractionProcedure createObserverProjectionProcedure
    (final AbstractCompositionalModelAnalyzer analyzer)
  {
    final ChainTRSimplifier chain = new ChainTRSimplifier();
    final TransitionRelationSimplifier loopRemover =
      new TauLoopRemovalTRSimplifier();
    chain.add(loopRemover);
    final ObserverProjectionTRSimplifier op =
      new ObserverProjectionTRSimplifier();
    final int limit = analyzer.getInternalTransitionLimit();
    op.setTransitionLimit(limit);
    op.setTransitionRemovalMode
      (ObservationEquivalenceTRSimplifier.TransitionRemoval.ALL);
    chain.add(op);
    return new ObserverProjectionAbstractionProcedure(analyzer, chain, op);
  }


  //#########################################################################
  //# Constructor
  private ObserverProjectionAbstractionProcedure
    (final AbstractCompositionalModelAnalyzer analyzer,
     final ChainTRSimplifier chain,
     final ObserverProjectionTRSimplifier op)
  {
    super(analyzer, chain, false);
    mOPSimplifier = op;
  }


  //#########################################################################
  //# Overrides for class TRSimplifierAbstractionProcedure
  @Override
  EventEncoding createEventEncoding(final AutomatonProxy aut,
                                    final EventProxy tau)
  {
    final EventEncoding eventEnc = super.createEventEncoding(aut, tau);
    final ProductDESProxyFactory factory = getFactory();
    final String name = "vtau:" + aut.getName();
    final EventProxy vtau =
      factory.createEventProxy(name, EventKind.UNCONTROLLABLE);
    final KindTranslator id = IdenticalKindTranslator.getInstance();
    final int codeOfVTau = eventEnc.addEvent(vtau, id, false);
    mOPSimplifier.setVisibleTau(codeOfVTau);
    return eventEnc;
  }


  //###########################################################################
  //# Data Members
  private final ObserverProjectionTRSimplifier mOPSimplifier;

}
