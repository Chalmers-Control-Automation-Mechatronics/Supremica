//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   DefaultProductDESResult
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis;

import java.util.Collection;

import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;


/**
 * The standard implementation of the {@link ProductDESResult} interface.
 * The default product DES result provides read/write access to all the data
 * provided by the interface.
 *
 * @author Robi Malik
 */

public class DefaultProductDESResult
  extends DefaultProxyResult<ProductDESProxy>
  implements ProductDESResult
{

  //#########################################################################
  //# Constructors
  /**
   * Creates an automaton result representing an incomplete run.
   */
  public DefaultProductDESResult()
  {
  }


  //#########################################################################
  //# Simple Access Methods
  public ProductDESProxy getComputedProductDES()
  {
    return getComputedProxy();
  }

  public Collection<AutomatonProxy> getComputedAutomata()
  {
    final ProductDESProxy des = getComputedProductDES();
    if (des == null) {
      return null;
    } else {
      return des.getAutomata();
    }
  }

  public void setComputedProductDES(final ProductDESProxy des)
  {
    setComputedProxy(des);
  }

}
