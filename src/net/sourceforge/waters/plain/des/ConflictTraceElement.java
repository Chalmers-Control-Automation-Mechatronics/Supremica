//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.plain.des
//# CLASS:   ConflictTraceElement
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.plain.des;

import java.net.URI;
import java.util.Collection;
import java.util.List;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyVisitor;
import net.sourceforge.waters.model.des.ConflictTraceProxy;
import net.sourceforge.waters.model.des.TraceStepProxy;

import net.sourceforge.waters.xsd.des.ConflictKind;


/**
 * A counterexample trace for a conflict property of a product DES.
 * This is a simple immutable implementation of the {@link ConflictTraceProxy}
 * interface.
 *
 * @author Robi Malik
 */

public class ConflictTraceElement
  extends TraceElement
  implements ConflictTraceProxy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new conflict trace.
   * @param  name         The name to be given to the new trace.
   * @param  comment      A comment describing the new trace,
   *                      or <CODE>null</CODE>.
   * @param  location     The URI to be associated with the new
   *                      document, or <CODE>null</CODE>.
   * @param  des          The product DES for which this trace is
   *                      generated.
   * @param  automata     The set of automata for the new trace,
   *                      or <CODE>null</CODE> if empty.
   * @param  steps        The list of trace steps consituting the
   *                      new trace. This list may not be empty, because
   *                      the first step must always represent the
   *                      initial state.
   * @param  kind         The type of this conflict trace,
   *                      one of {@link ConflictKind#CONFLICT},
   *                      {@link ConflictKind#DEADLOCK}, or
   *                      {@link ConflictKind#LIVELOCK}.
   * @throws ItemNotFoundException to indicate that one of the given
   *                      automata, events, or states cannot be found
   *                      in the product DES.
   */
  ConflictTraceElement(final String name,
                       final String comment,
                       final URI location,
                       final ProductDESProxy des,
                       final Collection<? extends AutomatonProxy> automata,
                       final List<? extends TraceStepProxy> steps,
                       final ConflictKind kind)
  {
    super(name, comment, location, des, automata, steps);
    mKind = kind;
  }

  /**
   * Creates a new conflict trace using default values.  This constructor
   * provides a simple interface to create a trace for a deterministic
   * product DES. It creates a trace with a <CODE>null</CODE> file
   * location, with a set of automata equal to that of the product DES, and
   * without any state information in the trace steps.
   * @param  name         The name to be given to the new trace.
   * @param  des          The product DES for which the new trace is
   *                      generated.
   * @param  events       The list of events constituting the new trace,
   *                      or <CODE>null</CODE> if empty.
   * @param  kind         The type of this conflict trace,
   *                      one of {@link ConflictKind#CONFLICT},
   *                      {@link ConflictKind#DEADLOCK}, or
   *                      {@link ConflictKind#LIVELOCK}.
   * @throws ItemNotFoundException to indicate that one of the given
   *                      events cannot be found in the product DES.
   */
  ConflictTraceElement(final String name,
                       final ProductDESProxy des,
                       final List<? extends EventProxy> events,
                       final ConflictKind kind)
  {
    super(name, des, events);
    mKind = kind;
  }


  //#########################################################################
  //# Interface java.lang.Cloneable
  /**
   * Returns a copy of this product DES.
   */
  public ConflictTraceElement clone()
  {
    return (ConflictTraceElement) super.clone();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.base.Proxy
  public Object acceptVisitor(final ProxyVisitor visitor)
    throws VisitorException
  {
    final ProductDESProxyVisitor desvisitor = (ProductDESProxyVisitor) visitor;
    return desvisitor.visitConflictTraceProxy(this);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.des.ConflictTraceProxy
  public ConflictKind getKind()
  {
    return mKind;
  }


  //#########################################################################
  //# Equals and Hashcode
  public Class<ConflictTraceProxy> getProxyInterface()
  {
    return ConflictTraceProxy.class;
  }

  public boolean equalsByContents(final Proxy partner)
  {
    if (super.equalsByContents(partner)) {
      final ConflictTraceElement trace = (ConflictTraceElement) partner;
      return mKind.equals(trace.mKind);
    } else {
      return false;
    }    
  }

  public int hashCodeByContents()
  {
    return super.hashCodeByContents() + 5 * mKind.hashCode();
  }


  //#########################################################################
  //# Data Members
  private final ConflictKind mKind;

}
