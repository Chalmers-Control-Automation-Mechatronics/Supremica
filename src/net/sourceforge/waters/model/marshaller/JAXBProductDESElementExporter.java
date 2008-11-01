//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.marshaller
//# CLASS:   JAXBProductDESExporter
//###########################################################################
//# $Id$
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
import net.sourceforge.waters.model.base.IndexedSet;
import net.sourceforge.waters.model.base.NamedProxy;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.des.AbstractProductDESProxyVisitor;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ConflictTraceProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.LoopTraceProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyVisitor;
import net.sourceforge.waters.model.des.SafetyTraceProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.des.TraceStepProxy;
import net.sourceforge.waters.model.des.TransitionProxy;

import net.sourceforge.waters.xsd.base.ElementType;
import net.sourceforge.waters.xsd.base.NamedType;
import net.sourceforge.waters.xsd.des.Automaton;
import net.sourceforge.waters.xsd.des.AutomatonRef;
import net.sourceforge.waters.xsd.des.ConflictTrace;
import net.sourceforge.waters.xsd.des.Event;
import net.sourceforge.waters.xsd.des.EventRef;
import net.sourceforge.waters.xsd.des.FirstTraceStateTuple;
import net.sourceforge.waters.xsd.des.LoopTrace;
import net.sourceforge.waters.xsd.des.NextTraceStateTuple;
import net.sourceforge.waters.xsd.des.ObjectFactory;
import net.sourceforge.waters.xsd.des.ProductDES;
import net.sourceforge.waters.xsd.des.SafetyTrace;
import net.sourceforge.waters.xsd.des.State;
import net.sourceforge.waters.xsd.des.TraceState;
import net.sourceforge.waters.xsd.des.TraceStateTupleType;
import net.sourceforge.waters.xsd.des.TraceStepList;
import net.sourceforge.waters.xsd.des.TraceType;
import net.sourceforge.waters.xsd.des.Transition;


abstract class JAXBProductDESElementExporter
  <D extends DocumentProxy, T extends NamedType>
  extends JAXBDocumentExporter<D,T>
  implements ProductDESProxyVisitor
{

  //#########################################################################
  //# Interface net.sourceforge.waters.model.des.ProductDESProxyVisitor
  public Automaton visitAutomatonProxy(final AutomatonProxy proxy)
    throws VisitorException
  {
    final Automaton element = mFactory.createAutomaton();
    copyAutomatonProxy(proxy, element);
    return element;
  }

  public ConflictTrace visitConflictTraceProxy(final ConflictTraceProxy proxy)
    throws VisitorException
  {
    final ConflictTrace element = mFactory.createConflictTrace();
    copyConflictTraceProxy(proxy, element);
    return element;
  }

  public Event visitEventProxy(final EventProxy proxy)
    throws VisitorException
  {
    final Event element = mFactory.createEvent();
    copyEventProxy(proxy, element);
    return element;
 }

  public LoopTrace visitLoopTraceProxy(final LoopTraceProxy proxy)
    throws VisitorException
  {
    final LoopTrace element = mFactory.createLoopTrace();
    copyLoopTraceProxy(proxy, element);
    return element;
  }

  public ProductDES visitProductDESProxy(final ProductDESProxy proxy)
    throws VisitorException
  {
    final ProductDES element = mFactory.createProductDES();
    copyProductDESProxy(proxy, element);
    return element;
  }

  public State visitStateProxy(final StateProxy proxy)
    throws VisitorException
  {
    final State element = mFactory.createState();
    copyStateProxy(proxy, element);
    return element;
  }

  public SafetyTrace visitSafetyTraceProxy(final SafetyTraceProxy proxy)
    throws VisitorException
  {
    final SafetyTrace element = mFactory.createSafetyTrace();
    copySafetyTraceProxy(proxy, element);
    return element;
  }

  public Object visitTraceProxy(final TraceProxy proxy)
    throws VisitorException
  {
    return visitDocumentProxy(proxy);
  }

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
      mAutomatonEvents = new CheckedExportSet<EventProxy>
        (proxy.getEvents(), proxy, "event");
      mAutomatonEventRefExporter =
        new RefExporter<EventProxy>(mAutomatonEvents);
      mAutomatonEventRefListHandler.toJAXB
        (mProductDESEventRefExporter, mAutomatonEvents, element);
      mAutomatonStates = new CheckedExportSet<StateProxy>
        (proxy.getStates(), proxy, "state");
      mAutomatonStateListHandler.toJAXB(this, mAutomatonStates, element);
      final Set<TransitionProxy> transitions =
        new TreeSet<TransitionProxy>(proxy.getTransitions());
      mAutomatonTransitionListHandler.toJAXB(this, transitions, element);
    } finally {
      mAutomatonEvents = null;
      mAutomatonStates = null;
      mAutomatonEventRefExporter = null;
    }
  }

  private void copyConflictTraceProxy(final ConflictTraceProxy proxy,
                                      final ConflictTrace element)
    throws VisitorException
  {
    element.setKind(proxy.getKind());
    copyTraceProxy(proxy, element);
  }

  private void copyEventProxy(final EventProxy proxy, final Event element)
  {
    copyNamedProxy(proxy, element);
    element.setKind(proxy.getKind());
    if (!proxy.isObservable()) {
      element.setObservable(false);
    }
  }

  private void copyLoopTraceProxy(final LoopTraceProxy proxy,
                                  final LoopTrace element)
    throws VisitorException
  {
    element.setLoopIndex(proxy.getLoopIndex());
    copyTraceProxy(proxy, element);
  }

  private void copyProductDESProxy(final ProductDESProxy proxy,
                                   final ProductDES element)
    throws VisitorException
  {
    try {
      copyDocumentProxy(proxy, element);
      mProductDESEvents = new CheckedExportSet<EventProxy>
        (proxy.getEvents(), proxy, "event");
      mProductDESEventRefExporter =
        new RefExporter<EventProxy>(mProductDESEvents);
      mProductDESEventListHandler.toJAXB(this, mProductDESEvents, element);
      final Set<AutomatonProxy> automata = new CheckedExportSet<AutomatonProxy>
        (proxy.getAutomata(), proxy, "automaton");
      mProductDESAutomataListHandler.toJAXB(this, automata, element);
    } finally {
      mProductDESEvents = null;
      mProductDESEventRefExporter = null;
    }
  }

  private void copySafetyTraceProxy(final SafetyTraceProxy proxy,
                                    final SafetyTrace element)
    throws VisitorException
  {
    copyTraceProxy(proxy, element);
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
                              final TraceType element)
    throws VisitorException
  {
    copyDocumentProxy(proxy, element);
    try {
      final ProductDESProxy des = proxy.getProductDES();
      element.setProductDES(des.getName());
      final IndexedSet<EventProxy> events =
        new CheckedExportSet<EventProxy>(des.getEvents(), proxy, "event");
      final RefExporter<EventProxy> eventexporter =
        new RefExporter<EventProxy>(events);
      final IndexedSet<AutomatonProxy> automata =
        new CheckedExportSet<AutomatonProxy>
          (des.getAutomata(), proxy, "automaton");
      final RefExporter<AutomatonProxy> autexporter =
        new RefExporter<AutomatonProxy>(automata);
      mTraceAutomata = new CheckedExportSet<AutomatonProxy>
        (proxy.getAutomata(), proxy, "automaton");
      mTraceAutomatonRefListHandler.toJAXB
        (autexporter, mTraceAutomata, element);
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
          final EventRef eventref = eventexporter.visitEventProxy(event);
          outlist.add(eventref);
          final NextTraceStateTuple tracestate =
            (NextTraceStateTuple) visitTraceStepProxy(step);
          if (tracestate != null) {
            outlist.add(tracestate);
          }
        }
      }
    } finally {
      mTraceAutomata = null;
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
      mTraceAutomata.checkUnique(aut);
      final StateProxy state = statemap.get(aut);
      // check whether automaton contains state
      final TraceState tracestate = mFactory.createTraceState();
      tracestate.setAutomaton(aut.getName());
      tracestate.setState(state.getName());
      outlist.add(tracestate);
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
    extends AbstractProductDESProxyVisitor
  {

    //#######################################################################
    //# Constructors
    private RefExporter(final IndexedSet<P> alphabet)
    {
      mAlphabet = alphabet;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.des.ProductDESProxyVisitor
    public AutomatonRef visitAutomatonProxy(final AutomatonProxy proxy)
      throws VisitorException
    {
      mAlphabet.checkUnique(proxy);
      final AutomatonRef element = mFactory.createAutomatonRef();
      copyNamedProxy(proxy, element);
      return element;
    }

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
    private final IndexedSet<P> mAlphabet;
    
  }


  //#########################################################################
  //# Data Members
  private IndexedSet<EventProxy> mProductDESEvents;
  private IndexedSet<EventProxy> mAutomatonEvents;
  private IndexedSet<StateProxy> mAutomatonStates;
  private IndexedSet<AutomatonProxy> mTraceAutomata;
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
  private final TraceAutomatonRefListHandler
    mTraceAutomatonRefListHandler = new TraceAutomatonRefListHandler(mFactory);

  private static final ObjectFactory mFactory = new ObjectFactory();

}
