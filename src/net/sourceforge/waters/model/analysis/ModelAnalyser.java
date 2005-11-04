//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   ModelAnalyser
//###########################################################################
//# $Id: ModelAnalyser.java,v 1.2 2005-11-04 02:21:17 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.analysis;

import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * <P>The main model analyser class.</P>
 *
 * @author Robi Malik
 */

public abstract class ModelAnalyser
{

  //#########################################################################
  //# Constructors
  public ModelAnalyser(final ProductDESProxyFactory factory,
		       final ProductDESProxy input)
  {
    mFactory = factory;
    mInput = input;
    mResult = null;
  }


  //#########################################################################
  //# Invocation
  public AnalysisResult run()
  {
    mResult = null;
    mResult = callNativeMethod();
    return mResult;
  }


  //#########################################################################
  //# Simple Acess Methods
  public ProductDESProxyFactory getFactory()
  {
    return mFactory;
  }

  public ProductDESProxy getInput()
  {
    return mInput;
  }

  public AnalysisResult getResult()
  {
    return mResult;
  }
  

  //#########################################################################
  //# Native Methods
  public abstract AnalysisResult callNativeMethod();

  static {
    System.loadLibrary("waters");
  }


  //#########################################################################
  //# Data Members
  private final ProductDESProxyFactory mFactory;
  private final ProductDESProxy mInput;
  private AnalysisResult mResult;

}
