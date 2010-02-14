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
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.SafetyTraceProxy;
import net.sourceforge.waters.model.des.StateProxy;


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
    this(null, factory, translator);
  }

  public NativeSafetyVerifier(final ProductDESProxy model,
                              final ProductDESProxyFactory factory,
                              final KindTranslator translator)
  {
    super(model, factory, translator);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.SafetyVerifier
  public SafetyTraceProxy getCounterExample()
  {
    return (SafetyTraceProxy) super.getCounterExample();
  }


  //#########################################################################
  //# Native Methods
  native VerificationResult runNativeAlgorithm();


  //#########################################################################
  //# Auxiliary Methods
  public abstract String getTraceComment(final EventProxy event,
                                         final AutomatonProxy aut,
                                         final StateProxy state);

}
