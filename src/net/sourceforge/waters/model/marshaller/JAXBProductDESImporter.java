//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.marshaller
//# CLASS:   JAXBProductDESImporter
//###########################################################################
//# $Id: JAXBProductDESImporter.java,v 1.5 2006-09-19 15:53:20 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.marshaller;

import java.net.URI;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.base.IndexedSet;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;

import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.ElementType;
import net.sourceforge.waters.xsd.base.EventKind;
import net.sourceforge.waters.xsd.des.Automaton;
import net.sourceforge.waters.xsd.des.EventRef;
import net.sourceforge.waters.xsd.des.Event;
import net.sourceforge.waters.xsd.des.ProductDES;
import net.sourceforge.waters.xsd.des.State;
import net.sourceforge.waters.xsd.des.Transition;


class JAXBProductDESImporter
  extends JAXBDocumentImporter<ProductDESProxy,ProductDES>
{

  //#########################################################################
  //# Constructors
  public JAXBProductDESImporter(final ProductDESProxyFactory factory)
  {
    mFactory = factory;
  }


  //#########################################################################
  //# Overrides for Abstract Base Class JAXBImporter
  Proxy importElement(final ElementType element)
  {
    if (element instanceof Transition) {
      return importTransition((Transition) element);
    } else if (element instanceof State) {
      return importState((State) element);
    } else if (element instanceof Event) {
      return importEvent((Event) element);
    } else if (element instanceof Automaton) {
      return importAutomaton((Automaton) element);
    } else if (element instanceof ProductDES) {
      return importProductDES((ProductDES) element);
    } else {
      throw new ClassCastException
        ("JAXBProductDESImporter cannot handle element of type " +
         element.getClass().getName() + "!");
    }
  }

  public ProductDESProxy importDocument(final ProductDES element,
                                        final URI uri)
  {
    return importProductDES(element, uri);
  }


  //#########################################################################
  //# Importing Elements
  private ProductDESProxy importProductDES(final ProductDES element)
  {
    return importProductDES(element, null);
  }

  private ProductDESProxy importProductDES(final ProductDES element,
                                           final URI uri)
  {
    try {
      final String name = element.getName();
      final String comment = element.getComment();
      mProductDESEvents = new CheckedImportSet<EventProxy>
        (ProductDESProxy.class, name, "event");
      mProductDESEventRefImporter = new EventRefImporter(mProductDESEvents);
      mProductDESEventListHandler.fromJAXBChecked
        (this, element, mProductDESEvents);
      final IndexedSet<AutomatonProxy> automata =
        new CheckedImportSet<AutomatonProxy>
        (ProductDESProxy.class, name, "automaton");
      mProductDESAutomataListHandler.fromJAXBChecked(this, element, automata);
      return mFactory.createProductDESProxy
        (name, comment, uri, mProductDESEvents, automata);
    } finally {
      mProductDESEvents = null;
      mProductDESEventRefImporter = null;
    }
  }

  private AutomatonProxy importAutomaton(final Automaton element)
  {
    try {
      final String name = element.getName();
      final ComponentKind kind = element.getKind();
      mAutomatonEvents = new CheckedImportSet<EventProxy>
        (AutomatonProxy.class, name, "event");
      mAutomatonEventRefImporter = new EventRefImporter(mAutomatonEvents);
      mAutomatonEventRefListHandler.fromJAXBChecked
        (mProductDESEventRefImporter, element, mAutomatonEvents);
      mAutomatonStates = new CheckedImportSet<StateProxy>
        (AutomatonProxy.class, name, "state");
      mAutomatonStateListHandler.fromJAXBChecked
        (this, element, mAutomatonStates);
      final Collection<TransitionProxy> transitions =
        new LinkedList<TransitionProxy>();
      mAutomatonTransitionListHandler.fromJAXB(this, element, transitions);
      return mFactory.createAutomatonProxy
        (name, kind, mAutomatonEvents, mAutomatonStates, transitions);
    } finally {
      mAutomatonEvents = null;
      mAutomatonStates = null;
      mAutomatonEventRefImporter = null;
    }
  }

  private EventProxy importEvent(final Event element)
  {
    final String name = element.getName();
    final EventKind kind = element.getKind();
    final boolean observable = element.isObservable();
    return mFactory.createEventProxy(name, kind, observable);
  }

  private StateProxy importState(final State element)
  {
    final String name = element.getName();
    final boolean initial = element.isInitial();
    final Collection<EventProxy> propositions = new LinkedList<EventProxy>();
    mStateEventRefListHandler.fromJAXB
      (mAutomatonEventRefImporter, element, propositions);
    return mFactory.createStateProxy(name, initial, propositions);
  }

  private TransitionProxy importTransition(final Transition element)
  {
    final String sourcename = element.getSource();
    final StateProxy source = mAutomatonStates.find(sourcename);
    final String eventname = element.getEvent();
    final EventProxy event = mAutomatonEvents.find(eventname);
    final String targetname = element.getTarget();
    final StateProxy target = mAutomatonStates.find(targetname);
    return mFactory.createTransitionProxy(source, event, target);
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
      final EventRef eventref = (EventRef) element;
      final String name = eventref.getName();
      return mAlphabet.find(name);
    }

    //#######################################################################
    //# Data Members
    private final IndexedSet<EventProxy> mAlphabet;
    
  }



  //#########################################################################
  //# Data Members
  private IndexedSet<EventProxy> mProductDESEvents;
  private IndexedSet<EventProxy> mAutomatonEvents;
  private IndexedSet<StateProxy> mAutomatonStates;
  private EventRefImporter mProductDESEventRefImporter;
  private EventRefImporter mAutomatonEventRefImporter;

  private final ProductDESProxyFactory mFactory;

  private final ProductDESEventListHandler
    mProductDESEventListHandler = new ProductDESEventListHandler();
  private final ProductDESAutomataListHandler
    mProductDESAutomataListHandler = new ProductDESAutomataListHandler();
  private final AutomatonEventRefListHandler
    mAutomatonEventRefListHandler = new AutomatonEventRefListHandler();
  private final AutomatonStateListHandler
    mAutomatonStateListHandler = new AutomatonStateListHandler();
  private final AutomatonTransitionListHandler
    mAutomatonTransitionListHandler = new AutomatonTransitionListHandler();
  private final StateEventRefListHandler
    mStateEventRefListHandler = new StateEventRefListHandler();

}
