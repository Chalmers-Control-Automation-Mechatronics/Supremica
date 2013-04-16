//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.cpp.analysis
//# CLASS:   NativeModelVerifier
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.cpp.analysis;

import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.DefaultVerificationResult;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.analysis.des.AnalysisResult;
import net.sourceforge.waters.model.analysis.des.KindTranslator;
import net.sourceforge.waters.model.analysis.des.ModelVerifier;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.TraceProxy;


/**
 * @author Robi Malik
 */

public abstract class NativeModelVerifier
  extends NativeModelAnalyzer
  implements ModelVerifier
{

  //#########################################################################
  //# Constructors
  public NativeModelVerifier(final ProductDESProxyFactory factory,
                             final KindTranslator translator)
  {
    this(null, factory, translator);
  }

  public NativeModelVerifier(final ProductDESProxy model,
                             final ProductDESProxyFactory factory,
                             final KindTranslator translator)
  {
    super(model, factory, translator);
    mExplorerMode = ExplorerMode.BEST_GUESS;
  }


  //#########################################################################
  //# Overrides net.sourceforge.waters.model.analysis.AbstractModelVerifier
  @Override
  public VerificationResult createAnalysisResult()
  {
    return new DefaultVerificationResult();
  }


  //#########################################################################
  //# Configuration
  public void setExplorerMode(final ExplorerMode mode)
  {
    mExplorerMode = mode;
  }

  public ExplorerMode getExplorerMode()
  {
    return mExplorerMode;
  }


  //#########################################################################
  //# Invocation
  public boolean run()
    throws AnalysisException
  {
    if (getModel() == null) {
      throw new NullPointerException("No model given!");
    } else {
      clearAnalysisResult();
      final long start = System.currentTimeMillis();
      try {
        final AnalysisResult result = runNativeAlgorithm();
        final long stop = System.currentTimeMillis();
        result.setRuntime(stop - start);
        setAnalysisResult(result);
        return result.isSatisfied();
      } catch (final AnalysisException exception) {
        final long stop = System.currentTimeMillis();
        final AnalysisResult result = createAnalysisResult();
        result.setException(exception);
        result.setRuntime(stop - start);
        throw exception;
      }
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelVerifier
  public boolean isSatisfied()
  {
    final VerificationResult result = getAnalysisResult();
    if (result != null) {
      return result.isSatisfied();
    } else {
      throw new IllegalStateException("Call run() first!");
    }
  }

  public TraceProxy getCounterExample()
  {
    if (isSatisfied()) {
      throw new IllegalStateException("No trace for satisfied property!");
    } else {
      final VerificationResult result = getAnalysisResult();
      return result.getCounterExample();
    }
  }

  @Override
  public VerificationResult getAnalysisResult()
  {
    return (VerificationResult) super.getAnalysisResult();
  }


  //#########################################################################
  //# Native Methods
  abstract VerificationResult runNativeAlgorithm() throws AnalysisException;

  public abstract String getTraceName();


  //#########################################################################
  //# Data Members
  private ExplorerMode mExplorerMode;

}
