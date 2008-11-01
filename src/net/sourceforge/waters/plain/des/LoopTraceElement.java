//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.plain.des
//# CLASS:   LoopTraceElement
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
import net.sourceforge.waters.model.des.LoopTraceProxy;
import net.sourceforge.waters.model.des.TraceStepProxy;


/**
 * A counterexample trace for a loop property of a product DES.
 * This is a simple immutable implementation of the {@link LoopTraceProxy}
 * interface.
 *
 * @author Robi Malik
 */

public final class LoopTraceElement
  extends TraceElement
  implements LoopTraceProxy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new loop trace.
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
   * @param  index        The loop index of the new trace.
   * @throws ItemNotFoundException to indicate that one of the given
   *                      automata, events, or states cannot be found
   *                      in the product DES.
   */
  LoopTraceElement(final String name,
                   final String comment,
                   final URI location,
                   final ProductDESProxy des,
                   final Collection<? extends AutomatonProxy> automata,
                   final List<? extends TraceStepProxy> steps,
                   final int index)
  {
    super(name, comment, location, des, automata, steps);
    mLoopIndex = index;
  }

  /**
   * Creates a new loop trace using default values.  This constructor
   * provides a simple interface to create a trace for a deterministic
   * product DES. It creates a trace with a <CODE>null</CODE> file
   * location, with a set of automata equal to that of the product DES, and
   * without any state information in the trace steps.
   * @param  name         The name to be given to the new trace.
   * @param  des          The product DES for which the new trace is
   *                      generated.
   * @param  events       The list of events constituting the new trace,
   *                      or <CODE>null</CODE> if empty.
   * @param  index        The loop index of the new trace.
   * @throws ItemNotFoundException to indicate that one of the given
   *                      events cannot be found in the product DES.
   */
  LoopTraceElement(final String name,
                   final ProductDESProxy des,
                   final List<? extends EventProxy> events,
                   final int index)
  {
    super(name, des, events);
    mLoopIndex = index;
  }


  //#########################################################################
  //# Interface java.lang.Cloneable
  /**
   * Returns a copy of this product DES.
   */
  public LoopTraceElement clone()
  {
    return (LoopTraceElement) super.clone();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.base.Proxy
  public Object acceptVisitor(final ProxyVisitor visitor)
    throws VisitorException
  {
    final ProductDESProxyVisitor desvisitor = (ProductDESProxyVisitor) visitor;
    return desvisitor.visitLoopTraceProxy(this);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.des.LoopTraceProxy
  public int getLoopIndex()
  {
    return mLoopIndex;
  }


  //#########################################################################
  //# Equals and Hashcode
  public Class<LoopTraceProxy> getProxyInterface()
  {
    return LoopTraceProxy.class;
  }

  public boolean equalsByContents(final Proxy partner)
  {
    if (super.equalsByContents(partner)) {
      final LoopTraceProxy trace = (LoopTraceProxy) partner;
      return mLoopIndex == trace.getLoopIndex();
    } else {
      return false;
    }    
  }

  public int hashCodeByContents()
  {
    return super.hashCodeByContents() + 5 * mLoopIndex;
  }


  //#########################################################################
  //# Data Members
  private final int mLoopIndex;

}
