//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   ModelAnalyser
//###########################################################################
//# $Id: ModelAnalyser.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.analysis;

import net.sourceforge.waters.model.des.ProductDESProxy;


/**
 * <P>The main model analyser class.</P>
 *
 * @author Robi Malik
 */

public abstract class ModelAnalyser
{

  //#########################################################################
  //# Constructors
  public ModelAnalyser(final ProductDESProxy input)
  {
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
  private final ProductDESProxy mInput;
  private AnalysisResult mResult;

}
