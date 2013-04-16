//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.gnonblocking
//# CLASS:   TRSimplifierAbstractionProcedure
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.gnonblocking;

import java.util.Collection;

import net.sourceforge.waters.analysis.abstraction.TransitionRelationSimplifier;
import net.sourceforge.waters.model.analysis.des.KindTranslator;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * A wrapper superclass to implement abstraction rules based on a
 * {@link TransitionRelationSimplifier}.
 *
 * @author Robi Malik
 */

abstract class TRSimplifierAbstractionRule extends AbstractionRule
{

  //#########################################################################
  //# Constructor
  TRSimplifierAbstractionRule(final ProductDESProxyFactory factory,
                              final KindTranslator translator,
                              final TransitionRelationSimplifier simplifier)
  {
    this(factory, translator, null, simplifier);
  }

  TRSimplifierAbstractionRule(final ProductDESProxyFactory factory,
                              final KindTranslator translator,
                              final Collection<EventProxy> propositions,
                              final TransitionRelationSimplifier simplifier)
  {
    super(factory, translator, propositions);
    mSimplifier = simplifier;
  }


  //#########################################################################
  //# Simple Access
  TransitionRelationSimplifier getSimplifier()
  {
    return mSimplifier;
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
  //# Data Members
  private final TransitionRelationSimplifier mSimplifier;

}
