//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   ProductDESCopier
//###########################################################################
//# $Id: ProductDESCopier.java,v 1.2 2005-11-04 02:21:17 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.analysis;

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
			  final ProductDESProxy input)
  {
    super(factory, input);
  }


  //#########################################################################
  //# Simple Acess Methods
  public ProductDESResult getProductDESResult()
  {
    return (ProductDESResult) getResult();
  }
  

  //#########################################################################
  //# Native Methods
  public native AnalysisResult callNativeMethod();

}
