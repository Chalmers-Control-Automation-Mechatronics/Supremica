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
import net.sourceforge.waters.analysis.abstraction.ObserverProjectionTRSimplifier;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.model.analysis.IdenticalKindTranslator;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.xsd.base.EventKind;


/**
 * @author Robi Malik
 */

class ObserverProjectionAbstractionProcedure extends
  ConflictCheckerAbstractionProcedure
{

  //#########################################################################
  //# Constructors
  ObserverProjectionAbstractionProcedure
    (final AbstractCompositionalModelAnalyzer analyzer,
     final ChainTRSimplifier chain,
     final ObserverProjectionTRSimplifier op)
  {
    super(analyzer, chain);
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
