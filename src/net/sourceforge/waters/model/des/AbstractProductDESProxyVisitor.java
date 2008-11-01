//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.des
//# CLASS:   AbstractProductDESProxyVisitor
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.des;

import net.sourceforge.waters.model.base.AbstractProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;


public class AbstractProductDESProxyVisitor
  extends AbstractProxyVisitor
  implements ProductDESProxyVisitor
{

  public Object visitAutomatonProxy(final AutomatonProxy proxy)
    throws VisitorException
  {
    return visitNamedProxy(proxy);
  }

  public Object visitConflictTraceProxy(final ConflictTraceProxy proxy)
    throws VisitorException
  {
    return visitTraceProxy(proxy);
  }

  public Object visitEventProxy(final EventProxy proxy)
    throws VisitorException
  {
    return visitNamedProxy(proxy);
  }

  public Object visitLoopTraceProxy(final LoopTraceProxy proxy)
    throws VisitorException
  {
    return visitTraceProxy(proxy);
  }

  public Object visitProductDESProxy(final ProductDESProxy proxy)
    throws VisitorException
  {
    return visitDocumentProxy(proxy);
  }

  public Object visitSafetyTraceProxy(final SafetyTraceProxy proxy)
    throws VisitorException
  {
    return visitTraceProxy(proxy);
  }

  public Object visitStateProxy(final StateProxy proxy)
    throws VisitorException
  {
    return visitNamedProxy(proxy);
  }

  public Object visitTraceProxy(final TraceProxy proxy)
    throws VisitorException
  {
    return visitDocumentProxy(proxy);
  }

  public Object visitTraceStepProxy(final TraceStepProxy proxy)
    throws VisitorException
  {
    return visitProxy(proxy);
  }

  public Object visitTransitionProxy(final TransitionProxy proxy)
    throws VisitorException
  {
    return visitProxy(proxy);
  }

}
