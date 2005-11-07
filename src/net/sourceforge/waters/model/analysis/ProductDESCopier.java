//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   ProductDESCopier
//###########################################################################
//# $Id: ProductDESCopier.java,v 1.3 2005-11-07 00:47:34 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.analysis;

import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * @author Robi Malik
 */

public class ProductDESCopier extends ModelAnalyser
{

  //#########################################################################
  //# Constructors
  public ProductDESCopier(final ProductDESProxyFactory factory,
			  final ProductDESProxy input,
			  final EventProxy proposition)
  {
    super(factory, input);
    mProposition = proposition;
  }

  public ProductDESCopier(final ProductDESProxyFactory factory,
			  final ProductDESProxy input)
  {
    this(factory, input, null);
  }


  //#########################################################################
  //# Invocation
  public ProductDESResult run()
  {
    return (ProductDESResult) super.run();
  }


  //#########################################################################
  //# Simple Acess Methods
  public ProductDESResult getResult()
  {
    return (ProductDESResult) super.getResult();
  }
  
  public EventProxy getProposition()
  {
    return mProposition;
  }


  //#########################################################################
  //# Native Methods
  public native ProductDESResult callNativeMethod();


  //#########################################################################
  //# Data Members
  private final EventProxy mProposition;

}
