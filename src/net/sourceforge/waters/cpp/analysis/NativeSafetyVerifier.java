//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.cpp.analysis
//# CLASS:   NativeSafetyVerifier
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.cpp.analysis;

import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.SafetyVerifier;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.SafetyTraceProxy;


/**
 * @author Robi Malik
 */

public abstract class NativeSafetyVerifier
  extends NativeModelVerifier
  implements SafetyVerifier
{

  //#########################################################################
  //# Constructors
  public NativeSafetyVerifier(final KindTranslator translator,
                              final ProductDESProxyFactory factory)
  {
    this(null, translator, factory);
  }

  public NativeSafetyVerifier(final ProductDESProxy model,
                              final KindTranslator translator,
                              final ProductDESProxyFactory factory)
  {
    super(model, factory);
    mKindTranslator = translator;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.SafetyVerifier
  public void setKindTranslator(KindTranslator translator)
  {
    mKindTranslator = translator;
    clearAnalysisResult();
  }

  public KindTranslator getKindTranslator()
  {
    return mKindTranslator;
  }

  public SafetyTraceProxy getCounterExample()
  {
    return (SafetyTraceProxy) super.getCounterExample();
  }


  //#########################################################################
  //# Native Methods
  native VerificationResult runNativeAlgorithm();


  //#########################################################################
  //# Data Members
  private KindTranslator mKindTranslator;

}
