//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.cpp.analysis
//# CLASS:   NativeModelAnalyser
//###########################################################################
//# $Id: NativeModelAnalyser.java,v 1.1 2006-08-15 01:43:06 robi Exp $
//###########################################################################

package net.sourceforge.waters.cpp.analysis;

import net.sourceforge.waters.model.analysis.AnalysisResult;
import net.sourceforge.waters.model.analysis.ModelAnalyser;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * <P>The abstract base class of all native model analysers.</P>
 *
 * @author Robi Malik
 */

public abstract class NativeModelAnalyser implements ModelAnalyser
{

  //#########################################################################
  //# Constructors
  public NativeModelAnalyser(final ProductDESProxy input,
			     final ProductDESProxyFactory factory)
  {
    mInput = input;
    mFactory = factory;
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

  //#########################################################################
  //# Native Methods
  static {
    System.loadLibrary("waters");
  }


  //#########################################################################
  //# Data Members
  private final ProductDESProxy mInput;
  private final ProductDESProxyFactory mFactory;

}
