//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.plain.des
//# CLASS:   SafetyTraceElement
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.plain.des;

import java.net.URI;
import java.util.Collection;
import java.util.List;

import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyVisitor;
import net.sourceforge.waters.model.des.SafetyTraceProxy;
import net.sourceforge.waters.model.des.TraceStepProxy;


/**
 * A counterexample trace for a safety property of a product DES.
 * This is a simple immutable implementation of the {@link SafetyTraceProxy}
 * interface.
 *
 * @author Robi Malik
 */

public final class SafetyTraceElement
  extends TraceElement
  implements SafetyTraceProxy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new safety trace.
   * @param  name         The name to be given to the new trace.
   * @param  comment      A comment describing the new trace,
   *                      or <CODE>null</CODE>.
   * @param  location     The URI to be associated with the new
   *                      document, or <CODE>null</CODE>.
   * @param  des          The product DES for which this trace is
   *                      generated.
   * @param  automata     The set of automata for the new trace,
   *                      or <CODE>null</CODE> if empty.
   * @param  steps        The list of trace steps constituting the
   *                      new trace. This list may not be empty, because
   *                      the first step must always represent the
   *                      initial state.
   * @throws ItemNotFoundException to indicate that one of the given
   *                      automata, events, or states cannot be found
   *                      in the product DES.
   */
  SafetyTraceElement(final String name,
                     final String comment,
                     final URI location,
                     final ProductDESProxy des,
                     final Collection<? extends AutomatonProxy> automata,
                     final List<? extends TraceStepProxy> steps)
  {
    super(name, comment, location, des, automata, steps);
  }

  /**
   * Creates a new safety trace using default values.  This constructor
   * provides a simple interface to create a trace for a deterministic
   * product DES. It creates a trace with a <CODE>null</CODE> file
   * location, with a set of automata equal to that of the product DES, and
   * without any state information in the trace steps.
   * @param  name         The name to be given to the new trace.
   * @param  des          The product DES for which the new trace is
   *                      generated.
   * @param  events       The list of events constituting the new trace,
   *                      or <CODE>null</CODE> if empty.
   * @throws ItemNotFoundException to indicate that one of the given
   *                      events cannot be found in the product DES.
   */
  SafetyTraceElement(final String name,
                     final ProductDESProxy des,
                     final List<? extends EventProxy> events)
  {
    super(name, des, events);
  }

  /**
   * Creates a new safety trace using default values.  This constructor
   * provides a simple interface to create a controllability error trace
   * for a deterministic product DES. It creates a trace with a
   * <CODE>null</CODE> file location, with the name of the product DES
   * catenated with <CODE>&quot-uncontrollable&quot;</CODE>, with a set of
   * automata equal to that of the product DES, and without any state
   * information in the trace steps.
   * @param  des          The product DES for which the new trace is
   *                      generated.
   * @param  events       The list of events constituting the new trace,
   *                      or <CODE>null</CODE> if empty.
   * @throws ItemNotFoundException to indicate that one of the given
   *                      events cannot be found in the product DES.
   */
  SafetyTraceElement(final ProductDESProxy des,
                     final List<? extends EventProxy> events)
  {
    this(des.getName() + "-uncontrollable", des, events);
  }


  //#########################################################################
  //# Interface java.lang.Cloneable
  /**
   * Returns a copy of this product DES.
   */
  public SafetyTraceElement clone()
  {
    return (SafetyTraceElement) super.clone();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.base.Proxy
  public Object acceptVisitor(final ProxyVisitor visitor)
    throws VisitorException
  {
    final ProductDESProxyVisitor desvisitor = (ProductDESProxyVisitor) visitor;
    return desvisitor.visitSafetyTraceProxy(this);
  }


  //#########################################################################
  //# Equals and Hashcode
  public Class<SafetyTraceProxy> getProxyInterface()
  {
    return SafetyTraceProxy.class;
  }


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
