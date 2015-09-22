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

import java.io.Serializable;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ConflictTraceProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.LoopTraceProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.SafetyTraceProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceStepProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;
import net.sourceforge.waters.xsd.des.ConflictKind;

/**
 * <P>A factory for the <I>plain</I> implementation of the Automaton
 * submodule.</P>
 *
 * @author Robi Malik
 */

public class ProductDESElementFactory
  implements ProductDESProxyFactory, Serializable
{

  //#########################################################################
  //# Static Class Methods
  public static ProductDESElementFactory getInstance()
  {
    return INSTANCE;
  }


  //#########################################################################
  //# Factory Methods
  @Override
  public AutomatonProxy createAutomatonProxy
    (final String name,
     final ComponentKind kind,
     final Collection<? extends EventProxy> events,
     final Collection<? extends StateProxy> states,
     final Collection<? extends TransitionProxy> transitions,
     final Map<String,String> attribs)
  {
    return new AutomatonElement(name, kind, events,
                                states, transitions, attribs);
  }

  @Override
  public AutomatonProxy createAutomatonProxy
  (final String name,
   final ComponentKind kind,
   final Collection<? extends EventProxy> events,
   final Collection<? extends StateProxy> states,
   final Collection<? extends TransitionProxy> transitions)
{
  return new AutomatonElement(name, kind, events, states, transitions);
}

  @Override
  public AutomatonProxy createAutomatonProxy
    (final String name,
     final ComponentKind kind)
  {
    return new AutomatonElement(name, kind);
  }

  @Override
  public ConflictTraceProxy createConflictTraceProxy
    (final String name,
     final String comment,
     final URI location,
     final ProductDESProxy des,
     final Collection<? extends AutomatonProxy> automata,
     final List<? extends TraceStepProxy> steps,
     final ConflictKind kind)
  {
    return new ConflictTraceElement(name, comment, location, des,
                                    automata, steps, kind);
  }

  @Override
  public ConflictTraceProxy createConflictTraceProxy
    (final String name,
     final ProductDESProxy des,
     final List<? extends EventProxy> events,
     final ConflictKind kind)
  {
    return new ConflictTraceElement(name, des, events, kind);
  }

  @Override
  public EventProxy createEventProxy
    (final String name,
     final EventKind kind,
     final boolean observable,
     final Map<String,String> attribs)
  {
    return new EventElement(name, kind, observable, attribs);
  }

  @Override
  public EventProxy createEventProxy
    (final String name,
     final EventKind kind,
     final boolean observable)
{
  return new EventElement(name, kind, observable);
}

  @Override
  public EventProxy createEventProxy(final String name, final EventKind kind)
  {
    return new EventElement(name, kind);
  }

  @Override
  public LoopTraceProxy createLoopTraceProxy
    (final String name,
     final String comment,
     final URI location,
     final ProductDESProxy des,
     final Collection<? extends AutomatonProxy> automata,
     final List<? extends TraceStepProxy> steps,
     final int index)
  {
    return new LoopTraceElement(name, comment, location, des,
                                automata, steps, index);
  }

  @Override
  public LoopTraceProxy createLoopTraceProxy
    (final String name,
     final ProductDESProxy des,
     final List<? extends EventProxy> events,
     final int index)
  {
    return new LoopTraceElement(name, des, events, index);
  }

  @Override
  public ProductDESProxy createProductDESProxy
    (final String name,
     final String comment,
     final URI location,
     final Collection<? extends EventProxy> events,
     final Collection<? extends AutomatonProxy> automata)
  {
    return new ProductDESElement(name, comment, location, events, automata);
  }

  @Override
  public ProductDESProxy createProductDESProxy
      (final String name,
       final Collection<? extends EventProxy> events,
       final Collection<? extends AutomatonProxy> automata)
  {
    return new ProductDESElement(name, events, automata);
  }

  @Override
  public ProductDESProxy createProductDESProxy(final String name)
  {
    return new ProductDESElement(name);
  }

  @Override
  public SafetyTraceProxy createSafetyTraceProxy
    (final String name,
     final String comment,
     final URI location,
     final ProductDESProxy des,
     final Collection<? extends AutomatonProxy> automata,
     final List<? extends TraceStepProxy> steps)
  {
    return new SafetyTraceElement(name, comment, location, des,
                                  automata, steps);
  }

  @Override
  public SafetyTraceProxy createSafetyTraceProxy
    (final String name,
     final ProductDESProxy des,
     final List<? extends EventProxy> events)
  {
    return new SafetyTraceElement(name, des, events);
  }

  @Override
  public SafetyTraceProxy createSafetyTraceProxy
    (final ProductDESProxy des,
     final List<? extends EventProxy> events)
  {
    return new SafetyTraceElement(des, events);
  }

  @Override
  public StateProxy createStateProxy
    (final String name,
     final boolean initial,
     final Collection<? extends EventProxy> propositions)
  {
    return new StateElement(name, initial, propositions);
  }

  @Override
  public StateProxy createStateProxy(final String name)
  {
    return new StateElement(name);
  }

  @Override
  public TraceStepProxy createTraceStepProxy
    (final EventProxy event,
     final Map<AutomatonProxy,StateProxy> statemap)
  {
    return new TraceStepElement(event, statemap);
  }

  @Override
  public TraceStepProxy createTraceStepProxy(final EventProxy event)
  {
    return new TraceStepElement(event);
  }

  @Override
  public TransitionProxy createTransitionProxy
    (final StateProxy source,
     final EventProxy event,
     final StateProxy target)
  {
    return new TransitionElement(source, event, target);
  }


  //#########################################################################
  //# Static Class Variables
  private static final ProductDESElementFactory INSTANCE =
    new ProductDESElementFactory();


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}








