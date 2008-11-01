//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.plain.des;
//# CLASS:   ProductDESElementFactory
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.plain.des;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
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
  implements ProductDESProxyFactory
{

  //#########################################################################
  //# Static Class Methods
  public static ProductDESElementFactory getInstance()
  {
    return INSTANCE;
  }

  
  //#########################################################################
  //# Factory Methods
  public AutomatonElement createAutomatonProxy
    (final String name,
     final ComponentKind kind,
     final Collection<? extends EventProxy> events,
     final Collection<? extends StateProxy> states,
     final Collection<? extends TransitionProxy> transitions)
  {
    return new AutomatonElement(name, kind, events, states, transitions);
  }

  public AutomatonElement createAutomatonProxy
    (final String name,
     final ComponentKind kind)
  {
    return new AutomatonElement(name, kind);
  }

  public ConflictTraceElement createConflictTraceProxy
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

  public ConflictTraceElement createConflictTraceProxy
    (final String name,
     final ProductDESProxy des,
     final List<? extends EventProxy> events,
     final ConflictKind kind)
  {
    return new ConflictTraceElement(name, des, events, kind);
  }

  public EventElement createEventProxy
    (final String name,
     final EventKind kind,
     final boolean observable)
  {
    return new EventElement(name, kind, observable);
  }

  public EventElement createEventProxy(final String name, final EventKind kind)
  {
    return new EventElement(name, kind);
  }

  public LoopTraceElement createLoopTraceProxy
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

  public LoopTraceElement createLoopTraceProxy
    (final String name,
     final ProductDESProxy des,
     final List<? extends EventProxy> events,
     final int index)
  {
    return new LoopTraceElement(name, des, events, index);
  }

  public ProductDESElement createProductDESProxy
    (final String name,
     final String comment,
     final URI location,
     final Collection<? extends EventProxy> events,
     final Collection<? extends AutomatonProxy> automata)
  {
    return new ProductDESElement(name, comment, location, events, automata);
  }

  public ProductDESElement createProductDESProxy
      (final String name,
       final Collection<? extends EventProxy> events,
       final Collection<? extends AutomatonProxy> automata)
  {
    return new ProductDESElement(name, events, automata);
  }

  public ProductDESElement createProductDESProxy(final String name)
  {
    return new ProductDESElement(name);
  }

  public SafetyTraceElement createSafetyTraceProxy
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

  public SafetyTraceElement createSafetyTraceProxy
    (final String name,
     final ProductDESProxy des,
     final List<? extends EventProxy> events)
  {
    return new SafetyTraceElement(name, des, events);
  }

  public SafetyTraceElement createSafetyTraceProxy
    (final ProductDESProxy des,
     final List<? extends EventProxy> events)
  {
    return new SafetyTraceElement(des, events);
  }

  public StateElement createStateProxy
    (final String name,
     final boolean initial,
     final Collection<? extends EventProxy> propositions)
  {
    return new StateElement(name, initial, propositions);
  }

  public StateElement createStateProxy(final String name)
  {
    return new StateElement(name);
  }

  public TraceStepElement createTraceStepProxy
    (final EventProxy event,
     final Map<AutomatonProxy,StateProxy> statemap)
  {
    return new TraceStepElement(event, statemap);
  }

  public TraceStepElement createTraceStepProxy(final EventProxy event)
  {
    return new TraceStepElement(event);
  }

  public TransitionElement createTransitionProxy
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

}
