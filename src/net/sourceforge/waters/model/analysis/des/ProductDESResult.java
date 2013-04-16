//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   ProductDESResult
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis.des;

import java.util.Collection;

import net.sourceforge.waters.model.analysis.ProxyResult;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;


/**
 * A result record returned by an {@link ProductDESBuilder}.
 * A product DES result contains collection of automata ({@link
 * net.sourceforge.waters.model.des.ProductDESProxy ProductDESProxy})
 * representing the result of an analysis algorithm such as compositional
 * synthesis. In addition, it may contain some statistics
 * about the analysis run.
 *
 * @author Robi Malik
 */

public interface ProductDESResult
  extends ProxyResult<ProductDESProxy>
{

  //#########################################################################
  //# Simple Access Methods
  /**
   * Gets the product DES computed by the model analyser,
   * or <CODE>null</CODE> if the computation was unsuccessful.
   */
  public ProductDESProxy getComputedProductDES();

  /**
   * Gets the collection of automata computed by the model analyser,
   * or <CODE>null</CODE> if the computation was unsuccessful.
   */
  public Collection<AutomatonProxy> getComputedAutomata();

  /**
   * Sets the computed product DES for this result. Setting the computed
   * object also marks the analysis run as completed and sets the Boolean
   * result.
   * @param  des    The computed product DES, or <CODE>null</CODE> to
   *                indicate an unsuccessful computation. The Boolean analysis
   *                result is set to <CODE>false</CODE> if and only if this
   *                parameter is <CODE>null</CODE>.
   */
  public void setComputedProductDES(final ProductDESProxy des);

}
