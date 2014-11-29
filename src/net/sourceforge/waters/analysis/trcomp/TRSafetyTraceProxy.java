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
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyVisitor;
import net.sourceforge.waters.model.des.SafetyTraceProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceStepProxy;


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
  //# Providing a Comment
  void provideComment(final SafetyDiagnostics diag)
  {
    final ProductDESProxy des = getProductDES();
    final int lastStep = getNumberOfSteps() - 1;
    final TraceStepProxy step = getTraceSteps().get(lastStep);
    final EventProxy event = step.getEvent();
    AutomatonProxy foundAut = null;
    StateProxy foundState = null;
    for (final AutomatonProxy aut : getAutomata()) {
      if (getState(aut, lastStep) != null) {
        foundAut = aut;
        foundState = lastStep > 0 ? getState(aut, lastStep - 1) : null;
      }
    }
    final String comment =
      diag.getTraceComment(des, event, foundAut, foundState);
    setComment(comment);
  }


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = -8991236718278268938L;

}
