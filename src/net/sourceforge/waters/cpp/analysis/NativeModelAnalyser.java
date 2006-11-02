//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.cpp.analysis
//# CLASS:   NativeModelAnalyser
//###########################################################################
//# $Id: NativeModelAnalyser.java,v 1.2 2006-11-02 22:40:29 robi Exp $
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
  public NativeModelAnalyser(final ProductDESProxyFactory factory)
  {
    this(null, factory);
  }

  public NativeModelAnalyser(final ProductDESProxy model,
			     final ProductDESProxyFactory factory)
  {
    mFactory = factory;
    mModel = model;
  }


  //#########################################################################
  //# Simple Acess Methods
  public ProductDESProxyFactory getFactory()
  {
    return mFactory;
  }

  public ProductDESProxy getModel()
  {
    return mModel;
  }

  public void setModel(final ProductDESProxy model)
  {
    mModel = model;
  }


  //#########################################################################
  //# Native Methods
  static {
    System.loadLibrary("waters");
  }


  //#########################################################################
  //# Data Members
  private final ProductDESProxyFactory mFactory;
  private ProductDESProxy mModel;

}
