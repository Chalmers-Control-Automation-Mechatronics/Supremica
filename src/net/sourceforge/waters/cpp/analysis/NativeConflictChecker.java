//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.cpp.analysis
//# CLASS:   NativeConflictChecker
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.cpp.analysis;

import net.sourceforge.waters.model.analysis.AbstractConflictChecker;
import net.sourceforge.waters.model.analysis.ConflictChecker;
import net.sourceforge.waters.model.analysis.ConflictKindTranslator;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.des.ConflictTraceProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * @author Robi Malik
 */

public class NativeConflictChecker
  extends NativeModelVerifier
  implements ConflictChecker
{

  //#########################################################################
  //# Constructors
  public NativeConflictChecker(final ProductDESProxyFactory factory)
  {
    this(null, factory);
  }

  public NativeConflictChecker(final ProductDESProxy model,
                               final ProductDESProxyFactory factory)
  {
    this(model, null, factory);
  }

  public NativeConflictChecker(final ProductDESProxy model,
                               final EventProxy marking,
                               final ProductDESProxyFactory factory)
  {
    super(model, factory);
    mMarking = marking;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelAnalyser
  public void setModel(final ProductDESProxy model)
  {
    super.setModel(model);
    mUsedMarking = null;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ConflictChecker
  public void setMarkingProposition(EventProxy marking)
  {
    mMarking = marking;
    mUsedMarking = null;
    clearAnalysisResult();
  }

  public EventProxy getMarkingProposition()
  {
    return mMarking;
  }

  public ConflictTraceProxy getCounterExample()
  {
    return (ConflictTraceProxy) super.getCounterExample();
  }


  //#########################################################################
  //# Auxiliary Methods
  public EventProxy getUsedMarkingProposition()
  {
    if (mUsedMarking == null) {
      if (mMarking == null) {
        final ProductDESProxy model = getModel();
        mUsedMarking = AbstractConflictChecker.getMarkingProposition(model);
      } else {
        mUsedMarking = mMarking;
      }
    }
    return mUsedMarking;
  }

  public KindTranslator getKindTranslator()
  {
    return ConflictKindTranslator.getInstance();
  }

  
  //#########################################################################
  //# Native Methods
  native VerificationResult runNativeAlgorithm();

  public String getTraceName()
  {
    return getModel().getName() + ":conflicting";
  }

    
  //#########################################################################
  //# Data Members
  private EventProxy mMarking;
  private EventProxy mUsedMarking;

}
