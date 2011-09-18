//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   AutomatonResult
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis;

import net.sourceforge.waters.model.base.Proxy;


/**
 * A result record returned by an {@link AutomatonBuilder}. An automaton
 * result consists of a structured object, typically an automaton
 * ({@link net.sourceforge.waters.model.des.AutomatonProxy AutomatonProxy})
 * or a product DES ({@link net.sourceforge.waters.model.des.ProductDESProxy
 * ProductDESProxy}) representing the result of an analysis algorithm such
 * as projection, minimisation, or synthesis. In addition, it may contain
 * some statistics about the analysis run.
 *
 * @author Robi Malik
 */

public class AutomatonResult<P extends Proxy> extends AnalysisResult
{

  //#########################################################################
  //# Constructors
  /**
   * Creates an automaton result representing an incomplete run.
   */
  public AutomatonResult()
  {
  }


  //#########################################################################
  //# Simple Access Methods
  /**
   * Gets the object computed by the model analyser,
   * or <CODE>null</CODE> if the computation was unsuccessful.
   */
  public P getComputedProxy()
  {
    return mComputedProxy;
  }

  /**
   * Sets the computed object (e.g.,
   * {@link net.sourceforge.waters.model.des.AutomatonProxy AutomatonProxy})
   * for this result. Setting the computed object also marks the analysis run
   * as completed and sets the Boolean result.
   * @param  proxy  The computed object, or <CODE>null</CODE> to
   *                indicate an unsuccessful computation. The Boolean analysis
   *                result is set to <CODE>false</CODE> if and only if this
   *                parameter is <CODE>null</CODE>.
   */
  public void setComputedProxy(final P proxy)
  {
    setSatisfied(proxy != null);
    mComputedProxy = proxy;
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.model.analysis.AnalysisResult
  @Override
  public void merge(final AnalysisResult other)
  {
    super.merge(other);
    mComputedProxy = null;
  }


  //#########################################################################
  //# Data Members
  private P mComputedProxy;

}
