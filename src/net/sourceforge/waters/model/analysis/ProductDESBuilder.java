//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   ProductDESBuilder
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis;

import net.sourceforge.waters.model.des.ProductDESProxy;


/**
 * <P>Interface of model analysers that compute a collection of automata
 * as a result.</P>
 *
 * @author Robi Malik
 */

public interface ProductDESBuilder extends ModelBuilder<ProductDESProxy>
{

  //#########################################################################
  //# More Specific Access to the Results
  /**
   * Gets the product DES computed by this algorithm.
   * @throws IllegalStateException if this method is called before
   *         model checking has completed, i.e., before {@link
   *         ModelAnalyzer#run() run()} has been called, or model checking
   *         has found that no proper result can be computed for the
   *         input model.
   */
  public ProductDESProxy getComputedProductDES();

  public ProductDESResult getAnalysisResult();

}
