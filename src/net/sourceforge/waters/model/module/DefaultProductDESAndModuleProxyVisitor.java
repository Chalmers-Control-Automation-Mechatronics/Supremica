//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2018 Robi Malik
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

package net.sourceforge.waters.model.module;

import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ConflictCounterExampleProxy;
import net.sourceforge.waters.model.des.CounterExampleProxy;
import net.sourceforge.waters.model.des.DefaultProductDESProxyVisitor;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.LoopCounterExampleProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyVisitor;
import net.sourceforge.waters.model.des.SafetyCounterExampleProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.des.TraceStepProxy;
import net.sourceforge.waters.model.des.TransitionProxy;


/**
 * <P>An empty implementation of the {@link ProductDESProxyVisitor}
 * and {@link ModuleProxyVisitor} interfaces.</P>
 *
 * <P>This class combines the implementations of {@link
 * DefaultProductDESProxyVisitor} and {@link ModuleProxyVisitor}. Like these
 * classes, it forms an adapter to make it more convenient to implement
 * visitors that do not explicitly implement all the visit methods.
 * All the visit methods in this adapter class do nothing or call the visit
 * method for the immediate superclass of their argument.</P>
 *
 * @author Robi Malik
 */

public class DefaultProductDESAndModuleProxyVisitor
  extends DefaultModuleProxyVisitor
  implements ProductDESProxyVisitor
{

  //#########################################################################
  //# Interface net.sourceforge.waters.model.des.ProductDESProxyVisitor
  @Override
  public Object visitAutomatonProxy(final AutomatonProxy proxy)
    throws VisitorException
  {
    return visitNamedProxy(proxy);
  }

  @Override
  public Object visitConflictCounterExampleProxy
    (final ConflictCounterExampleProxy proxy)
    throws VisitorException
  {
    return visitCounterExampleProxy(proxy);
  }

  @Override
  public Object visitCounterExampleProxy
    (final CounterExampleProxy proxy)
    throws VisitorException
  {
    return visitDocumentProxy(proxy);
  }

  @Override
  public Object visitEventProxy(final EventProxy proxy)
    throws VisitorException
  {
    return visitNamedProxy(proxy);
  }

  @Override
  public Object visitLoopCounterExampleProxy
    (final LoopCounterExampleProxy proxy)
    throws VisitorException
  {
    return visitCounterExampleProxy(proxy);
  }

  @Override
  public Object visitProductDESProxy(final ProductDESProxy proxy)
    throws VisitorException
  {
    return visitDocumentProxy(proxy);
  }

  @Override
  public Object visitSafetyCounterExampleProxy
    (final SafetyCounterExampleProxy proxy)
    throws VisitorException
  {
    return visitCounterExampleProxy(proxy);
  }

  @Override
  public Object visitStateProxy(final StateProxy proxy)
    throws VisitorException
  {
    return visitNamedProxy(proxy);
  }

  @Override
  public Object visitTraceProxy(final TraceProxy proxy)
    throws VisitorException
  {
    return visitProxy(proxy);
  }

  @Override
  public Object visitTraceStepProxy(final TraceStepProxy proxy)
    throws VisitorException
  {
    return visitProxy(proxy);
  }

  @Override
  public Object visitTransitionProxy(final TransitionProxy proxy)
    throws VisitorException
  {
    return visitProxy(proxy);
  }

}
