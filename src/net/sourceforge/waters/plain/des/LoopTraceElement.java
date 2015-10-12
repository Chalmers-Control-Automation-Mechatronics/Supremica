//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

package net.sourceforge.waters.plain.des;

import java.net.URI;
import java.util.Collection;
import java.util.List;

import net.sourceforge.waters.model.base.ItemNotFoundException;
import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.LoopTraceProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyVisitor;
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
  @Override
  public LoopTraceElement clone()
  {
    return (LoopTraceElement) super.clone();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.base.Proxy
  @Override
  public Object acceptVisitor(final ProxyVisitor visitor)
    throws VisitorException
  {
    final ProductDESProxyVisitor desvisitor = (ProductDESProxyVisitor) visitor;
    return desvisitor.visitLoopTraceProxy(this);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.des.LoopTraceProxy
  @Override
  public int getLoopIndex()
  {
    return mLoopIndex;
  }


  //#########################################################################
  //# Equals and Hashcode
  @Override
  public Class<LoopTraceProxy> getProxyInterface()
  {
    return LoopTraceProxy.class;
  }


  //#########################################################################
  //# Data Members
  private final int mLoopIndex;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
