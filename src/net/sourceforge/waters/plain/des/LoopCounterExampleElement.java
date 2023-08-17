//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
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
import java.util.Collections;

import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.LoopCounterExampleProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyVisitor;
import net.sourceforge.waters.model.des.TraceProxy;


/**
 * A counterexample that shows that a model fails a liveness property.
 * This is a simple immutable implementation of the {@link
 * LoopCounterExampleProxy} interface.
 *
 * @author Robi Malik
 */

public class LoopCounterExampleElement
  extends CounterExampleElement
  implements LoopCounterExampleProxy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new loop counterexample by specifying all arguments.
   * @param  name         The name to be given to the new counterexample.
   * @param  comment      A comment describing the new counterexample,
   *                      or <CODE>null</CODE>.
   * @param  location     The URI to be associated with the new
   *                      counterexample, or <CODE>null</CODE>.
   * @param  des          The product DES for which this counterexample is
   *                      generated.
   * @param  automata     The set of automata for the new counterexample,
   *                      or <CODE>null</CODE> if empty.
   * @param  trace        The trace that defines the new counterexample.
   */
  LoopCounterExampleElement(final String name,
                            final String comment,
                            final URI location,
                            final ProductDESProxy des,
                            final Collection<? extends AutomatonProxy> automata,
                            final TraceProxy trace)
  {
    super(name, comment, location,
          des, automata, Collections.singletonList(trace));
  }

  /**
   * Creates a new loop counterexample using default values.
   * This constructor provides a simple interface with a <CODE>null</CODE>
   * file location, with a set of automata equal to that of the product DES.
   * @param  name         The name to be given to the new counterexample.
   * @param  des          The product DES for which the new counterexample is
   *                      generated.
   * @param  trace        The trace that defines the new counterexample.
   */
  LoopCounterExampleElement(final String name,
                            final ProductDESProxy des,
                            final TraceProxy trace)
  {
    super(name, des, Collections.singletonList(trace));
  }


  //#########################################################################
  //# Interface java.lang.Cloneable
  /**
   * Returns a copy of this loop counterexample.
   */
  @Override
  public LoopCounterExampleElement clone()
  {
    return (LoopCounterExampleElement) super.clone();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.base.Proxy
  @Override
  public Class<LoopCounterExampleProxy> getProxyInterface()
  {
    return LoopCounterExampleProxy.class;
  }

  @Override
  public Object acceptVisitor(final ProxyVisitor visitor)
    throws VisitorException
  {
    final ProductDESProxyVisitor desVisitor = (ProductDESProxyVisitor) visitor;
    return desVisitor.visitLoopCounterExampleProxy(this);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.des.LoopCounterExampleProxy
  @Override
  public TraceProxy getTrace()
  {
    return getTraces().get(0);
  }


 //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 4573034098912761217L;

}
