//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   ProductDESResult
//###########################################################################
//# $Id: ProductDESResult.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.analysis;

import net.sourceforge.waters.model.des.ProductDESProxy;


public class ProductDESResult extends AnalysisResult
{

  //#########################################################################
  //# Constructors
  public ProductDESResult(final ProductDESProxy des)
  {
    this(true, des);
  }

  public ProductDESResult(final boolean satisfied)
  {
    this(satisfied, null);
  }

  public ProductDESResult(final boolean satisfied,
			  final ProductDESProxy des)
  {
    super(satisfied);
    mProductDES = des;
  }


  //#########################################################################
  //# Simple Access Methods
  public ProductDESProxy getProductDES()
  {
    return mProductDES;
  }


  //#########################################################################
  //# Data Members
  private final ProductDESProxy mProductDES;

}
