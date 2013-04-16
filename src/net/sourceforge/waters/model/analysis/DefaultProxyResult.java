//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   DefaultProxyResult
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis;

import net.sourceforge.waters.model.analysis.des.AnalysisResult;
import net.sourceforge.waters.model.base.Proxy;


/**
 * The standard implementation of the {@link ProxyResult} interface.
 * The default proxy result provides read/write access to all the data
 * provided by the interface.
 *
 * @author Robi Malik
 */

public class DefaultProxyResult<P extends Proxy>
  extends DefaultAnalysisResult
  implements ProxyResult<P>
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a proxy result representing an incomplete run.
   */
  public DefaultProxyResult()
  {
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ProxyResult<P>
  public P getComputedProxy()
  {
    return mComputedProxy;
  }

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
