//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.marshaller
//# CLASS:   JAXBTraceImporter
//###########################################################################
//# $Id: JAXBTraceImporter.java,v 1.4 2006-11-03 15:01:57 torda Exp $
//###########################################################################

package net.sourceforge.waters.model.marshaller;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import net.sourceforge.waters.model.base.IndexedSet;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ConflictTraceProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.LoopTraceProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.SafetyTraceProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.des.TraceStepProxy;

import net.sourceforge.waters.xsd.base.ElementType;
import net.sourceforge.waters.xsd.des.AutomatonRef;
import net.sourceforge.waters.xsd.des.ConflictKind;
import net.sourceforge.waters.xsd.des.ConflictTrace;
import net.sourceforge.waters.xsd.des.EventRef;
import net.sourceforge.waters.xsd.des.LoopTrace;
import net.sourceforge.waters.xsd.des.SafetyTrace;
import net.sourceforge.waters.xsd.des.TraceState;
import net.sourceforge.waters.xsd.des.TraceStateTuple;
import net.sourceforge.waters.xsd.des.TraceStepList;
import net.sourceforge.waters.xsd.des.TraceType;


class JAXBTraceImporter
  extends JAXBDocumentImporter<TraceProxy,TraceType>
{

  //#########################################################################
  //# Constructors
  public JAXBTraceImporter(final ProductDESProxyFactory factory)
  {
    mFactory = factory;
  }


  //#########################################################################
  //# Overrides for Abstract Base Class JAXBImporter
  Proxy importElement(final ElementType element)
  {
    throw new ClassCastException
      ("JAXBTraceImporter cannot handle element of type " +
       element.getClass().getName() + "!");
  }

  public TraceProxy importDocument(final TraceType element,
                                   final URI uri)
    throws WatersUnmarshalException
  {
    if (element instanceof SafetyTrace) {
      return importSafetyTrace((SafetyTrace) element, uri);
    } else if (element instanceof ConflictTrace) {
      return importConflictTrace((ConflictTrace) element, uri);
    } if (element instanceof LoopTrace) {
      return importLoopTrace((LoopTrace) element, uri);
    } else {
      throw new ClassCastException
        ("JAXBTraceImporter cannot handle trace element of type " +
         element.getClass().getName() + "!");
    }
  }


  //#########################################################################
  //# Importing Elements
  private ConflictTraceProxy importConflictTrace(final ConflictTrace element,
                                                 final URI uri)
    throws WatersUnmarshalException
  {
    final String name = element.getName();
    final String comment = element.getComment();
    final ProductDESProxy des = getProductDES(element, uri);
    final IndexedSet<AutomatonProxy> automata = getAutomata(element, des);
    final TraceStepList listelem = element.getTraceStepList();
    final List<TraceStepProxy> steps = getTraceSteps(listelem, des, automata);
    final ConflictKind kind = element.getKind();
    return mFactory.createConflictTraceProxy
      (name, comment, uri, des, automata, steps, kind);
  }

  private LoopTraceProxy importLoopTrace(final LoopTrace element,
                                         final URI uri)
    throws WatersUnmarshalException
  {
    final String name = element.getName();
    final String comment = element.getComment();
    final ProductDESProxy des = getProductDES(element, uri);
    final IndexedSet<AutomatonProxy> automata = getAutomata(element, des);
    final TraceStepList listelem = element.getTraceStepList();
    final List<TraceStepProxy> steps = getTraceSteps(listelem, des, automata);
    final int loopindex = element.getLoopIndex();
    return mFactory.createLoopTraceProxy
      (name, comment, uri, des, automata, steps, loopindex);
  }

  private SafetyTraceProxy importSafetyTrace(final SafetyTrace element,
                                             final URI uri)
    throws WatersUnmarshalException
  {
    final String name = element.getName();
    final String comment = element.getComment();
    final ProductDESProxy des = getProductDES(element, uri);
    final IndexedSet<AutomatonProxy> automata = getAutomata(element, des);
    final TraceStepList listelem = element.getTraceStepList();
    final List<TraceStepProxy> steps = getTraceSteps(listelem, des, automata);
    return mFactory.createSafetyTraceProxy
      (name, comment, uri, des, automata, steps);
  }


  //#########################################################################
  //# Auxiliary Methods
  private ProductDESProxy getProductDES(final TraceType element,
                                        final URI uri)
    throws WatersUnmarshalException
  {
    try {
      final DocumentManager manager = getDocumentManager();
      final String name = element.getProductDES();
      final ProductDESProxy des =
        manager.load(uri, name, ProductDESProxy.class);
      return des;
    } catch (final IOException exception) {
      throw new WatersUnmarshalException(exception);
    }
  }

  private IndexedSet<AutomatonProxy> getAutomata(final TraceType element,
                                                 final ProductDESProxy des)
  {
    final String name = element.getName();
    final IndexedSet<AutomatonProxy> automata0 =
      new CheckedExportSet<AutomatonProxy>(des.getAutomata(),
                                           des, "automaton");
    final AutomatonRefImporter importer = new AutomatonRefImporter(automata0);
    final IndexedSet<AutomatonProxy> automata1 =
      new CheckedImportSet<AutomatonProxy>
        (TraceProxy.class, name, "automaton");
    mTraceAutomatonRefListHandler.fromJAXBChecked
      (importer, element, automata1);
    return automata1;
  }

  private List<TraceStepProxy> getTraceSteps
    (final TraceStepList element,
     final ProductDESProxy des,
     final IndexedSet<AutomatonProxy> automata)
  {
    try {
      if (element == null) {
        final TraceStepProxy step0 = getTraceStep(null, null, automata);
        return Collections.singletonList(step0);
      } else {
        initStateCaches(automata);
        final List<TraceStepProxy> steps = new LinkedList<TraceStepProxy>();
        final IndexedSet<EventProxy> events =
          new CheckedExportSet<EventProxy>(des.getEvents(), des, "event");
        final EventRefImporter importer = new EventRefImporter(events);
        final TraceStateTuple tuple0 = element.getInitialState();
        final TraceStepProxy step0 = getTraceStep(null, tuple0, automata);
        steps.add(step0);
        final List<ElementType> list = element.getEventRefAndTraceStateTuple();
        final ListIterator<ElementType> iter = list.listIterator();
        while (iter.hasNext()) {
          final ElementType eventref = iter.next();
          final EventProxy event = importer.importElement(eventref);
          TraceStateTuple tuple = null;
          if (iter.hasNext()) {
            final ElementType next = iter.next();
            if (next instanceof TraceStateTuple) {
              tuple = (TraceStateTuple) next;
            } else {
              iter.previous();
            }
          }
          final TraceStepProxy step = getTraceStep(event, tuple, automata);
          steps.add(step);
        }
        return steps;
      }
    } finally {
      clearStateCaches();
    }
  }

  private TraceStepProxy getTraceStep
    (final EventProxy event,
     final TraceStateTuple element,
     final IndexedSet<AutomatonProxy> automata)
  {
    Map<AutomatonProxy,StateProxy> statemap = null;
    if (element != null) {
      final List<TraceState> list = element.getList();
      statemap = new IdentityHashMap<AutomatonProxy,StateProxy>(list.size());
      for (final TraceState entry : list) {
        final String autname = entry.getAutomaton();
        final AutomatonProxy aut = automata.find(autname);
        final String statename = entry.getState();
        final StateProxy state = getState(aut, statename);
        statemap.put(aut, state);
      }
    }
    return mFactory.createTraceStepProxy(event, statemap);
  }

  private StateProxy getState(final AutomatonProxy aut, final String name)
  {
    final IndexedSet<StateProxy> cache = getStateCache(aut);
    return cache.find(name);
  }

  private void initStateCaches(final IndexedSet<AutomatonProxy> automata)
  {
    final int size = automata.size();
    mStateCaches =
      new IdentityHashMap<AutomatonProxy,IndexedSet<StateProxy>>(size);
  }

  private IndexedSet<StateProxy> getStateCache(final AutomatonProxy aut)
  {
    IndexedSet<StateProxy> cache = mStateCaches.get(aut);
    if (cache == null) {
      cache = new CheckedExportSet<StateProxy>(aut.getStates(), aut, "state");
      mStateCaches.put(aut, cache);
    }
    return cache;      
  }

  private void clearStateCaches()
  {
    mStateCaches = null;
  }


  //#########################################################################
  //# Inner Class AutomatonRefImporter
  private static class AutomatonRefImporter
    extends JAXBImporter
  {

    //#######################################################################
    //# Constructors
    private AutomatonRefImporter(final IndexedSet<AutomatonProxy> alphabet)
    {
      mAlphabet = alphabet;
    }

    //#######################################################################
    //# Overrides for Abstract Base Class JAXBImporter
    AutomatonProxy importElement(final ElementType element)
    {
      return importAutomatonRef((AutomatonRef) element);
    }

    //#######################################################################
    //# Auxiliary Methods
    private AutomatonProxy importAutomatonRef(final AutomatonRef autref)
    {
      final String name = autref.getName();
      return mAlphabet.find(name);
    }

    //#######################################################################
    //# Data Members
    private final IndexedSet<AutomatonProxy> mAlphabet;
    
  }


  //#########################################################################
  //# Inner Class EventRefImporter
  private static class EventRefImporter
    extends JAXBImporter
  {

    //#######################################################################
    //# Constructors
    private EventRefImporter(final IndexedSet<EventProxy> alphabet)
    {
      mAlphabet = alphabet;
    }

    //#######################################################################
    //# Overrides for Abstract Base Class JAXBImporter
    EventProxy importElement(final ElementType element)
    {
      return importEventRef((EventRef) element);
    }

    //#######################################################################
    //# Auxiliary Methods
    private EventProxy importEventRef(final EventRef eventref)
    {
      final String name = eventref.getName();
      return mAlphabet.find(name);
    }

    //#######################################################################
    //# Data Members
    private final IndexedSet<EventProxy> mAlphabet;
    
  }


  //#########################################################################
  //# Data Members
  private final ProductDESProxyFactory mFactory;
  private final TraceAutomatonRefListHandler
    mTraceAutomatonRefListHandler = new TraceAutomatonRefListHandler();

  private Map<AutomatonProxy,IndexedSet<StateProxy>> mStateCaches;

}
