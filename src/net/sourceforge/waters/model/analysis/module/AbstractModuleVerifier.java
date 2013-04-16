//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis.module
//# CLASS:   AbstractModuleVerifier
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis.module;

import net.sourceforge.waters.model.analysis.DefaultVerificationResult;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;


/**
 * An abstract base class that can be used for all module verifier
 * implementations. In addition to the model and factory members inherited
 * from {@link AbstractModuleAnalyzer}, this class provides access to a
 * verification result member, and uses this to implement access to the
 * Boolean result value and the counterexample.
 *
 * @author Robi Malik, Sahar Mohajerani
 */

public abstract class AbstractModuleVerifier
  extends AbstractModuleAnalyzer
  implements ModuleVerifier
{

  //#########################################################################
  //# Constructors
  public AbstractModuleVerifier(final ModuleProxyFactory factory,
                                final KindTranslator translator)
  {
    this(null, factory, translator);
  }

  public AbstractModuleVerifier(final ModuleProxy model,
                                final ModuleProxyFactory factory,
                                final KindTranslator translator)
  {
    super(model, factory, translator);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModuleVerifier
  @Override
  public boolean isSatisfied()
  {
    final VerificationResult result = getAnalysisResult();
    if (result != null) {
      return result.isSatisfied();
    } else {
      throw new IllegalStateException("Call run() first!");
    }
  }

  @Override
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
  //# Overrides for net.sourceforge.waters.model.analysis.AbstractModuleAnalyser
  @Override
  protected VerificationResult createAnalysisResult()
  {
    return new DefaultVerificationResult();
  }


  //#########################################################################
  //# Setting the Result
  /**
   * Stores a verification result indicating that the property checked
   * is satisfied and marks the run as completed.
   * @return <CODE>true</CODE>
   */
  protected boolean setSatisfiedResult()
  {
    return setBooleanResult(true);
  }

  /**
   * Stores a verification result indicating that the property checked
   * is not satisfied and marks the run as completed.
   * @param  counterexample The counterexample obtained by verification.
   * @return <CODE>false</CODE>
   */
  protected boolean setFailedResult(final TraceProxy counterexample)
  {
    final VerificationResult result = getAnalysisResult();
    result.setCounterExample(counterexample);
    return setBooleanResult(false);
  }

}
