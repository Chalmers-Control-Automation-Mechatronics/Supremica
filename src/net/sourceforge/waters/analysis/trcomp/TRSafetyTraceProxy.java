//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.trcomp
//# CLASS:   TRSafetyTraceProxy
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.trcomp;

import net.sourceforge.waters.model.analysis.des.SafetyDiagnostics;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyVisitor;
import net.sourceforge.waters.model.des.SafetyTraceProxy;


/**
 * @author Robi Malik
 */

public class TRSafetyTraceProxy
  extends TRTraceProxy
  implements SafetyTraceProxy
{

  //#########################################################################
  //# Constructors
  public TRSafetyTraceProxy(final ProductDESProxy des,
                            final SafetyDiagnostics diag)
  {
    super(diag.getTraceName(des), null, des);
  }

  /**
   * Creates a new safety trace by copying another.
   * This copy constructor performs a deep copy of events and state lists,
   * but only shallow copy of the automata and abstraction steps.
   */
  TRSafetyTraceProxy(final TRSafetyTraceProxy trace)
  {
    super(trace);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.base.Proxy
  @Override
  public Class<? extends Proxy> getProxyInterface()
  {
    return SafetyTraceProxy.class;
  }

  @Override
  public Object acceptVisitor(final ProxyVisitor visitor)
    throws VisitorException
  {
    final ProductDESProxyVisitor desVisitor =
      (ProductDESProxyVisitor) visitor;
    return desVisitor.visitSafetyTraceProxy(this);
  }


  //#########################################################################
  //# Interface java.lang.Cloneable
  @Override
  public TRSafetyTraceProxy clone()
  {
    return (TRSafetyTraceProxy) super.clone();
  }


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = -8991236718278268938L;

}
