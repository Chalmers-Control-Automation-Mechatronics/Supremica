//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.trcomp
//# CLASS:   TRConflictTraceProxy
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.trcomp;

import net.sourceforge.waters.model.analysis.des.AbstractConflictChecker;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.des.ConflictTraceProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyVisitor;
import net.sourceforge.waters.xsd.des.ConflictKind;

/**
 * @author Robi Malik
 */
public class TRConflictTraceProxy
  extends TRTraceProxy
  implements ConflictTraceProxy
{

  //#########################################################################
  //# Constructors
  public TRConflictTraceProxy(final ProductDESProxy des)
  {
    this(des, ConflictKind.CONFLICT);
  }

  public TRConflictTraceProxy(final ProductDESProxy des,
                              final ConflictKind kind)
  {
    super(AbstractConflictChecker.getTraceName(des), null, des);
    mKind = kind;
  }

  /**
   * Creates a new conflict trace by copying another.
   * This copy constructor performs a deep copy of events and state lists,
   * but only shallow copy of the automata and abstraction steps.
   */
  TRConflictTraceProxy(final TRConflictTraceProxy trace)
  {
    super(trace);
    mKind = trace.mKind;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.base.Proxy
  @Override
  public Class<? extends Proxy> getProxyInterface()
  {
    return ConflictTraceProxy.class;
  }

  @Override
  public Object acceptVisitor(final ProxyVisitor visitor)
    throws VisitorException
  {
    final ProductDESProxyVisitor desVisitor = (ProductDESProxyVisitor) visitor;
    return desVisitor.visitConflictTraceProxy(this);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.base.Proxy
  @Override
  public ConflictKind getKind()
  {
    return mKind;
  }


  //#########################################################################
  //# Interface java.lang.Cloneable
  @Override
  public TRConflictTraceProxy clone()
  {
    return (TRConflictTraceProxy) super.clone();
  }


  //#########################################################################
  //# Data Members
  private final ConflictKind mKind;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = -8991236718278268958L;

}
