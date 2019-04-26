//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
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
import java.util.Arrays;
import java.util.Collection;

import net.sourceforge.waters.model.base.ItemNotFoundException;
import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.DualCounterExampleProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyVisitor;
import net.sourceforge.waters.model.des.TraceProxy;


/**
 * A counterexample consisting of two traces.
 * This is a simple immutable implementation of the {@link
 * DualCounterExampleProxy} interface.
 *
 * @author Robi Malik
 */

public class DualCounterExampleElement
  extends CounterExampleElement
  implements DualCounterExampleProxy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new dual counterexample.
   * @param  name         The name to be given to the new counterexample.
   * @param  comment      A comment describing the new counterexample,
   *                      or <CODE>null</CODE>.
   * @param  location     The URI to be associated with the new
   *                      document, or <CODE>null</CODE>.
   * @param  des          The product DES for which this counterexample is
   *                      generated.
   * @param  automata     The set of automata for the new counterexample,
   *                      or <CODE>null</CODE> if empty.
   * @param  trace1       The first of the two traces that define the dual
   *                      counterexample.
   * @param  trace2       The second of the two traces that define the dual
   *                      counterexample.
   * @throws ItemNotFoundException to indicate that one of the given
   *                      automata, events, or states cannot be found
   *                      in the product DES.
   */
  public DualCounterExampleElement(final String name,
                                   final String comment,
                                   final URI location,
                                   final ProductDESProxy des,
                                   final Collection<? extends AutomatonProxy> automata,
                                   final TraceProxy trace1,
                                   final TraceProxy trace2)
  {
    super(name, comment, location, des, automata,
          Arrays.asList(new TraceProxy[] {trace1, trace2}));
  }


  //#########################################################################
  //# Interface java.lang.Cloneable
  /**
   * Returns a copy of this loop counterexample.
   */
  @Override
  public DualCounterExampleElement clone()
  {
    return (DualCounterExampleElement) super.clone();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.base.Proxy
  @Override
  public Class<DualCounterExampleProxy> getProxyInterface()
  {
    return DualCounterExampleProxy.class;
  }

  @Override
  public Object acceptVisitor(final ProxyVisitor visitor)
    throws VisitorException
  {
    final ProductDESProxyVisitor desVisitor = (ProductDESProxyVisitor) visitor;
    return desVisitor.visitDualCounterExampleProxy(this);
  }


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = -9128906018195923790L;

}
