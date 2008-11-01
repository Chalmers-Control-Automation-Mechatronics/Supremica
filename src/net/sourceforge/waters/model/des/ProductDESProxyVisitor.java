//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.des
//# CLASS:   ProductDESProxyVisitor
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.des;

import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;


public interface ProductDESProxyVisitor
  extends ProxyVisitor
{

  public Object visitAutomatonProxy(AutomatonProxy proxy)
    throws VisitorException;

  public Object visitConflictTraceProxy(ConflictTraceProxy proxy)
    throws VisitorException;

  public Object visitEventProxy(EventProxy proxy)
    throws VisitorException;

  public Object visitLoopTraceProxy(LoopTraceProxy proxy)
    throws VisitorException;

  public Object visitProductDESProxy(ProductDESProxy proxy)
    throws VisitorException;

  public Object visitSafetyTraceProxy(SafetyTraceProxy proxy)
    throws VisitorException;

  public Object visitStateProxy(StateProxy proxy)
    throws VisitorException;

  public Object visitTraceProxy(TraceProxy proxy)
    throws VisitorException;

  public Object visitTraceStepProxy(TraceStepProxy proxy)
    throws VisitorException;

  public Object visitTransitionProxy(TransitionProxy proxy)
    throws VisitorException;

}
