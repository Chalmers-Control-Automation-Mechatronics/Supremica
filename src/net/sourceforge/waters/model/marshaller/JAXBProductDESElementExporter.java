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

package net.sourceforge.waters.model.marshaller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import net.sourceforge.waters.model.base.DocumentProxy;
import net.sourceforge.waters.model.base.IndexedList;
import net.sourceforge.waters.model.base.NamedProxy;
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
import net.sourceforge.waters.xsd.base.AttributeMap;
import net.sourceforge.waters.xsd.base.ElementType;
import net.sourceforge.waters.xsd.base.NamedType;
import net.sourceforge.waters.xsd.des.Automaton;
import net.sourceforge.waters.xsd.des.AutomatonRef;
import net.sourceforge.waters.xsd.des.ConflictCounterExample;
import net.sourceforge.waters.xsd.des.CounterExampleType;
import net.sourceforge.waters.xsd.des.Event;
import net.sourceforge.waters.xsd.des.EventRef;
import net.sourceforge.waters.xsd.des.FirstTraceStateTuple;
import net.sourceforge.waters.xsd.des.LoopCounterExample;
import net.sourceforge.waters.xsd.des.NextTraceStateTuple;
import net.sourceforge.waters.xsd.des.ObjectFactory;
import net.sourceforge.waters.xsd.des.ProductDES;
import net.sourceforge.waters.xsd.des.SafetyCounterExample;
import net.sourceforge.waters.xsd.des.State;
import net.sourceforge.waters.xsd.des.Trace;
import net.sourceforge.waters.xsd.des.TraceState;
import net.sourceforge.waters.xsd.des.TraceStateTupleType;
import net.sourceforge.waters.xsd.des.TraceStepList;
import net.sourceforge.waters.xsd.des.Transition;


abstract class JAXBProductDESElementExporter
  <D extends DocumentProxy, T extends NamedType>
  extends JAXBDocumentExporter<D,T>
  implements ProductDESProxyVisitor
{

  //#########################################################################
  //# Interface net.sourceforge.waters.model.des.ProductDESProxyVisitor
  @Override
  public Automaton visitAutomatonProxy(final AutomatonProxy proxy)
    throws VisitorException
  {
    final Automaton element = mFactory.createAutomaton();
    copyAutomatonProxy(proxy, element);
    return element;
  }

  @Override
  public Object visitConflictCounterExampleProxy
    (final ConflictCounterExampleProxy proxy)
    throws VisitorException
  {
    final ConflictCounterExample element =
      mFactory.createConflictCounterExample();
    copyConflictCounterExampleProxy(proxy, element);
    return element;
  }

  @Override
  public Object visitCounterExampleProxy(final CounterExampleProxy proxy)
    throws VisitorException
  {
    return visitDocumentProxy(proxy);
  }

  @Override
  public Event visitEventProxy(final EventProxy proxy)
    throws VisitorException
  {
    final Event element = mFactory.createEvent();
    copyEventProxy(proxy, element);
    return element;
 }

  @Override
  public Object visitLoopCounterExampleProxy
    (final LoopCounterExampleProxy proxy)
    throws VisitorException
  {
    final LoopCounterExample element = mFactory.createLoopCounterExample();
    copyLoopCounterExampleProxy(proxy, element);
    return element;
  }

  @Override
  public ProductDES visitProductDESProxy(final ProductDESProxy proxy)
    throws VisitorException
  {
    final ProductDES element = mFactory.createProductDES();
    copyProductDESProxy(proxy, element);
    return element;
  }

  @Override
  public Object visitSafetyCounterExampleProxy
    (final SafetyCounterExampleProxy proxy)
    throws VisitorException
  {
    final SafetyCounterExample element =
      mFactory.createSafetyCounterExample();
    copySafetyCounterExampleProxy(proxy, element);
    return element;
  }

  @Override
  public State visitStateProxy(final StateProxy proxy)
    throws VisitorException
  {
    final State element = mFactory.createState();
    copyStateProxy(proxy, element);
    return element;
  }

  @Override
  public Object visitTraceProxy(final TraceProxy proxy)
    throws VisitorException
  {
    final Trace element = mFactory.createTrace();
    copyTraceProxy(proxy, element);
    return element;
  }

  @Override
  public TraceStateTupleType visitTraceStepProxy(final TraceStepProxy proxy)
    throws VisitorException
  {
    final Map<AutomatonProxy,StateProxy> statemap = proxy.getStateMap();
    if (statemap.isEmpty()) {
      return null;
    } else {
      final TraceStateTupleType element =
        mIsFirstTraceStep ?
        mFactory.createFirstTraceStateTuple() :
        mFactory.createNextTraceStateTuple();
      copyTraceStepProxy(proxy, element);
      return element;
    }
  }

  @Override
  public Transition visitTransitionProxy(final TransitionProxy proxy)
    throws VisitorException
  {
    final Transition element = mFactory.createTransition();
    copyTransitionProxy(proxy, element);
    return element;
  }


  //#########################################################################
  //# Copying Data
  private void copyAutomatonProxy(final AutomatonProxy proxy,
                                  final Automaton element)
    throws VisitorException
  {
    try {
      copyNamedProxy(proxy, element);
      element.setKind(proxy.getKind());
      mAutomatonEvents = new CheckedExportList<EventProxy>
        (proxy.getEvents(), proxy, "event");
      mAutomatonEventRefExporter =
        new RefExporter<EventProxy>(mAutomatonEvents);
      mAutomatonEventRefListHandler.toJAXB
        (mProductDESEventRefExporter, mAutomatonEvents, element);
      mAutomatonStates = new CheckedExportList<StateProxy>
        (proxy.getStates(), proxy, "state");
      mAutomatonStateListHandler.toJAXB(this, mAutomatonStates, element);
      final Set<TransitionProxy> transitions =
        new TreeSet<TransitionProxy>(proxy.getTransitions());
      mAutomatonTransitionListHandler.toJAXB(this, transitions, element);
      final Map<String,String> attribs = proxy.getAttributes();
      final AttributeMap attribsElement = createAttributeMap(attribs);
      element.setAttributeMap(attribsElement);
    } finally {
      mAutomatonEvents = null;
      mAutomatonStates = null;
      mAutomatonEventRefExporter = null;
    }
  }

  private void copyConflictCounterExampleProxy
    (final ConflictCounterExampleProxy proxy,
     final ConflictCounterExample element)
    throws VisitorException
  {
    element.setKind(proxy.getKind());
    copyCounterExampleProxy(proxy, element);
  }

  private void copyCounterExampleProxy(final CounterExampleProxy proxy,
                                       final CounterExampleType element)
    throws VisitorException
  {
    copyDocumentProxy(proxy, element);
    try {
      final ProductDESProxy des = proxy.getProductDES();
      element.setProductDES(des.getName());
      final IndexedList<AutomatonProxy> automata =
        new CheckedExportList<AutomatonProxy>
          (des.getAutomata(), proxy, "automaton");
      final RefExporter<AutomatonProxy> autexporter =
        new RefExporter<AutomatonProxy>(automata);
      mCounterExampleAutomata = new CheckedExportList<AutomatonProxy>
        (proxy.getAutomata(), proxy, "automaton");
      mCounterExampleAutomatonRefListHandler.toJAXB
        (autexporter, mCounterExampleAutomata, element);
      final IndexedList<EventProxy> events =
        new CheckedExportList<EventProxy>(des.getEvents(), proxy, "event");
      mProductDESEventRefExporter = new RefExporter<EventProxy>(events);
      final Collection<TraceProxy> traces = proxy.getTraces();
      mCounterExampleTraceListHandler.toJAXB(this, traces, element);
    } finally {
      mCounterExampleAutomata = null;
      mProductDESEventRefExporter = null;
    }
  }

  private void copyEventProxy(final EventProxy proxy, final Event element)
  {
    copyNamedProxy(proxy, element);
    element.setKind(proxy.getKind());
    if (!proxy.isObservable()) {
      element.setObservable(false);
    }
    final Map<String,String> attribs = proxy.getAttributes();
    final AttributeMap attribsElement = createAttributeMap(attribs);
    element.setAttributeMap(attribsElement);
  }

  private void copyLoopCounterExampleProxy
    (final LoopCounterExampleProxy proxy,
     final LoopCounterExample element)
    throws VisitorException
  {
    copyCounterExampleProxy(proxy, element);
  }

  private void copyProductDESProxy(final ProductDESProxy proxy,
                                   final ProductDES element)
    throws VisitorException
  {
    try {
      copyDocumentProxy(proxy, element);
      mProductDESEvents = new CheckedExportList<EventProxy>
        (proxy.getEvents(), proxy, "event");
      mProductDESEventRefExporter =
        new RefExporter<EventProxy>(mProductDESEvents);
      mProductDESEventListHandler.toJAXB(this, mProductDESEvents, element);
      final Collection<AutomatonProxy> automata =
        new CheckedExportList<AutomatonProxy>
          (proxy.getAutomata(), proxy, "automaton");
      mProductDESAutomataListHandler.toJAXB(this, automata, element);
    } finally {
      mProductDESEvents = null;
      mProductDESEventRefExporter = null;
    }
  }

  private void copySafetyCounterExampleProxy
    (final SafetyCounterExampleProxy proxy,
     final SafetyCounterExample element)
    throws VisitorException
  {
    copyCounterExampleProxy(proxy, element);
  }

  private void copyStateProxy(final StateProxy proxy, final State element)
    throws VisitorException
  {
    copyNamedProxy(proxy, element);
    if (proxy.isInitial()) {
      element.setInitial(true);
    }
    final Collection<EventProxy> props = proxy.getPropositions();
    mStateEventRefListHandler.toJAXB
      (mAutomatonEventRefExporter, props, element);
  }

  private void copyTraceProxy(final TraceProxy proxy,
                              final Trace element)
    throws VisitorException
  {
    final String name = proxy.getName();
    if (name != null && !name.equals("")) {
      element.setName(name);
    }
    final List<TraceStepProxy> steps = proxy.getTraceSteps();
    final Iterator<TraceStepProxy> iter = steps.iterator();
    final TraceStepProxy step0 = iter.next();
    mIsFirstTraceStep = true;
    final FirstTraceStateTuple tracestate0 =
      (FirstTraceStateTuple) visitTraceStepProxy(step0);
    if (tracestate0 != null || iter.hasNext()) {
      final TraceStepList listelem = mFactory.createTraceStepList();
      element.setTraceStepList(listelem);
      if (tracestate0 != null) {
        listelem.setInitialState(tracestate0);
      }
      mIsFirstTraceStep = false;
      final List<ElementType> outlist =
        listelem.getEventRefAndNextTraceStateTuple();
      while (iter.hasNext()) {
        final TraceStepProxy step = iter.next();
        final EventProxy event = step.getEvent();
        final EventRef eventref =
          mProductDESEventRefExporter.visitEventProxy(event);
        outlist.add(eventref);
        final NextTraceStateTuple tracestate =
          (NextTraceStateTuple) visitTraceStepProxy(step);
        if (tracestate != null) {
          outlist.add(tracestate);
        }
      }
    }
    final int loop = proxy.getLoopIndex();
    if (loop >= 0) {
      element.setLoopIndex(loop);
    }
  }

  private void copyTraceStepProxy(final TraceStepProxy proxy,
                                  final TraceStateTupleType element)
    throws VisitorException
  {
    final List<TraceState> outlist = element.getList();
    final Map<AutomatonProxy,StateProxy> statemap = proxy.getStateMap();
    final Set<AutomatonProxy> keyset = statemap.keySet();
    final List<AutomatonProxy> sorted = new ArrayList<AutomatonProxy>(keyset);
    Collections.sort(sorted);
    for (final AutomatonProxy aut : sorted) {
      mCounterExampleAutomata.checkUnique(aut);
      final StateProxy state = statemap.get(aut);
      if (state != null) {
        // TODO Check whether automaton contains state
        final TraceState tracestate = mFactory.createTraceState();
        tracestate.setAutomaton(aut.getName());
        tracestate.setState(state.getName());
        outlist.add(tracestate);
      }
    }
  }

  private void copyTransitionProxy(final TransitionProxy proxy,
                                   final Transition element)
    throws VisitorException
  {
    copyProxy(proxy, element);
    final StateProxy source = proxy.getSource();
    mAutomatonStates.checkUnique(source);
    element.setSource(source.getName());
    final StateProxy target = proxy.getTarget();
    mAutomatonStates.checkUnique(target);
    element.setTarget(target.getName());
    final EventProxy event = proxy.getEvent();
    mAutomatonEvents.checkUnique(event);
    element.setEvent(event.getName());
  }


  //#########################################################################
  //# Inner Class RefExporter
  private class RefExporter<P extends NamedProxy>
    extends DefaultProductDESProxyVisitor
  {

    //#######################################################################
    //# Constructors
    private RefExporter(final IndexedList<? extends P> automatonEvents)
    {
      mAlphabet = automatonEvents;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.des.ProductDESProxyVisitor
    @Override
    public AutomatonRef visitAutomatonProxy(final AutomatonProxy proxy)
      throws VisitorException
    {
      mAlphabet.checkUnique(proxy);
      final AutomatonRef element = mFactory.createAutomatonRef();
      copyNamedProxy(proxy, element);
      return element;
    }

    @Override
    public EventRef visitEventProxy(final EventProxy proxy)
      throws VisitorException
    {
      mAlphabet.checkUnique(proxy);
      final EventRef element = mFactory.createEventRef();
      copyNamedProxy(proxy, element);
      return element;
    }

    //#######################################################################
    //# Data Members
    private final IndexedList<? extends P> mAlphabet;

  }


  //#########################################################################
  //# Data Members
  private IndexedList<EventProxy> mProductDESEvents;
  private IndexedList<EventProxy> mAutomatonEvents;
  private IndexedList<StateProxy> mAutomatonStates;
  private IndexedList<AutomatonProxy> mCounterExampleAutomata;
  private RefExporter<EventProxy> mProductDESEventRefExporter;
  private RefExporter<EventProxy> mAutomatonEventRefExporter;
  private boolean mIsFirstTraceStep;

  private final ProductDESEventListHandler
    mProductDESEventListHandler = new ProductDESEventListHandler(mFactory);
  private final ProductDESAutomataListHandler
    mProductDESAutomataListHandler =
    new ProductDESAutomataListHandler(mFactory);
  private final AutomatonEventRefListHandler
    mAutomatonEventRefListHandler = new AutomatonEventRefListHandler(mFactory);
  private final AutomatonStateListHandler
    mAutomatonStateListHandler = new AutomatonStateListHandler(mFactory);
  private final AutomatonTransitionListHandler
    mAutomatonTransitionListHandler =
    new AutomatonTransitionListHandler(mFactory);
  private final StateEventRefListHandler
    mStateEventRefListHandler = new StateEventRefListHandler(mFactory);
  private final CounterExampleAutomatonRefListHandler
    mCounterExampleAutomatonRefListHandler =
    new CounterExampleAutomatonRefListHandler(mFactory);
  private final CounterExampleTraceListHandler
    mCounterExampleTraceListHandler =
    new CounterExampleTraceListHandler(mFactory);

  private static final ObjectFactory mFactory = new ObjectFactory();

}
