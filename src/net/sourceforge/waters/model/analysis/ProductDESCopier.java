//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   ProductDESCopier
//###########################################################################
//# $Id: ProductDESCopier.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.analysis;

import net.sourceforge.waters.model.des.ProductDESProxy;


/**
 * @author Robi Malik
 */

public class ProductDESCopier extends ModelAnalyser
{

  //#########################################################################
  //# Constructors
  public ProductDESCopier(final ProductDESProxy input)
  {
    super(input);
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
