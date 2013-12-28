//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.sd
//# CLASS:   SDActivityLoopChecker
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.sd;

import net.sourceforge.waters.analysis.modular.ModularControlLoopChecker;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.analysis.des.ControlLoopChecker;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * <P>A model verifier to check whether a sampled-data system is
 * activity-loop free.</P>
 *
 * <P>The check is done using a configurable {@link ControlLoopChecker},
 * using a {@link KindTranslator} that designates all non-tick events
 * as controllable.</P>
 *
 * <P><STRONG>Reference.</STRONG>
 * Mahvash Baloch. A compositional approach for verifying sampled-data
 * supervisory control. M.Sc. Thesis, Dept. of Computing and Software,
 * McMaster University, March 2012.</P>
 *
 * @author Mahvash Baloch , Robi Malik
 */

public class SDActivityLoopChecker
  extends ModularControlLoopChecker
  implements ControlLoopChecker
{

  //#########################################################################
  //# Constructors
  public SDActivityLoopChecker
    (final ProductDESProxyFactory factory)
  {
    this(null, factory);
  }

  public SDActivityLoopChecker
    (final ControlLoopChecker checker,
     final ProductDESProxyFactory factory)
  {
    this(checker, null, factory);
  }

  public SDActivityLoopChecker
    (final ControlLoopChecker checker,
     final ProductDESProxy model,
     final ProductDESProxyFactory factory)
  {
    super(model,
          SDActivityLoopKindTranslator.getInstance(),
          factory);
    mControlLoopChecker = checker;
  }


  //#########################################################################
  //# Configuration
  public ControlLoopChecker getControlLoopChecker()
  {
    return mControlLoopChecker;
  }

  public void setControlLoopChecker(final ControlLoopChecker checker)
  {
    mControlLoopChecker = checker;
  }

  @Override
  public boolean supportsNondeterminism()
  {
    return false;
  }


  //#########################################################################
  //# Invocation
  @Override
  public boolean run() throws AnalysisException
  {
    setUp();
    try {
      final KindTranslator translator = getKindTranslator();
      mControlLoopChecker.setKindTranslator(translator);
      final ProductDESProxy model = getModel();
      mControlLoopChecker.setModel(model);
      final boolean result = mControlLoopChecker.run();
      return result;
    } finally {
      final VerificationResult data = mControlLoopChecker.getAnalysisResult();
      setAnalysisResult(data);
      tearDown();
    }
  }


  //#########################################################################
  //# Data Members
  private ControlLoopChecker mControlLoopChecker;

}
