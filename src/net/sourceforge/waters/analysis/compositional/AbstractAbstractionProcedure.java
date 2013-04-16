//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.compositional
//# CLASS:   AbstractAbstractionProcedure
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.compositional;

import java.util.Collection;

import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;

/**
 * A default implementation of the {@link AbstractionProcedure} interface.
 * This class makes it easy to access the model analyser
 * ({@link AbstractCompositionalModelAnalyzer}), but provides no additional
 * functionality.
 *
 * @author Robi Malik
 */
abstract class AbstractAbstractionProcedure implements AbstractionProcedure
{

  //#########################################################################
  //# Constructors
  AbstractAbstractionProcedure(final AbstractCompositionalModelAnalyzer analyzer)
  {
    mAnalyzer = analyzer;
  }


  //#########################################################################
  //# Simple Access
  AbstractCompositionalModelAnalyzer getAnalyzer()
  {
    return mAnalyzer;
  }

  CompositionalAnalysisResult getAnalysisResult()
  {
    return mAnalyzer.getAnalysisResult();
  }

  ProductDESProxyFactory getFactory()
  {
    return mAnalyzer.getFactory();
  }

  KindTranslator getKindTranslator()
  {
    return mAnalyzer.getKindTranslator();
  }

  Collection<EventProxy> getPropositions()
  {
    return mAnalyzer.getPropositions();
  }

  EventProxy getUsedDefaultMarking()
  {
    return mAnalyzer.getUsedDefaultMarking();
  }

  EventProxy getUsedPreconditionMarking()
  {
    return mAnalyzer.getUsedPreconditionMarking();
  }


  //#########################################################################
  //# Data Members
  private final AbstractCompositionalModelAnalyzer mAnalyzer;

}
