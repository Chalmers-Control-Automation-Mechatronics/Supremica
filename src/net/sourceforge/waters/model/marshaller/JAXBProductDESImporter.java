//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.marshaller
//# CLASS:   JAXBProductDESImporter
//###########################################################################
//# $Id: JAXBProductDESImporter.java,v 1.2 2005-11-03 01:24:16 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.marshaller;

import java.io.File;
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
import net.sourceforge.waters.xsd.des.AutomatonType;
import net.sourceforge.waters.xsd.des.EventRefType;
import net.sourceforge.waters.xsd.des.EventType;
import net.sourceforge.waters.xsd.des.ProductDESType;
import net.sourceforge.waters.xsd.des.StateType;
import net.sourceforge.waters.xsd.des.TransitionType;


class JAXBProductDESImporter
  extends JAXBDocumentImporter<ProductDESProxy,ProductDESType>
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
    if (element instanceof TransitionType) {
      return importTransition((TransitionType) element);
    } else if (element instanceof StateType) {
      return importState((StateType) element);
    } else if (element instanceof EventType) {
      return importEvent((EventType) element);
    } else if (element instanceof AutomatonType) {
      return importAutomaton((AutomatonType) element);
    } else if (element instanceof ProductDESType) {
      return importProductDES((ProductDESType) element);
    } else {
      throw new ClassCastException
        ("JAXBProductDESImporter cannot handle element of type " +
         element.getClass().getName() + "!");
    }
  }

  public ProductDESProxy importDocument(final ProductDESType element,
                                        final File location)
  {
    return importProductDES(element, location);
  }


  //#########################################################################
  //# Importing Elements
  private ProductDESProxy importProductDES(final ProductDESType element)
  {
    return importProductDES(element, null);
  }

  private ProductDESProxy importProductDES(final ProductDESType element,
                                           final File location)
  {
    try {
      final String name = element.getName();
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
        (name, location, mProductDESEvents, automata);
    } finally {
      mProductDESEvents = null;
      mProductDESEventRefImporter = null;
    }
  }

  private AutomatonProxy importAutomaton(final AutomatonType element)
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

  private EventProxy importEvent(final EventType element)
  {
    final String name = element.getName();
    final EventKind kind = element.getKind();
    final boolean observable = element.isObservable();
    return mFactory.createEventProxy(name, kind, observable);
  }

  private StateProxy importState(final StateType element)
  {
    final String name = element.getName();
    final boolean initial = element.isInitial();
    final Collection<EventProxy> propositions = new LinkedList<EventProxy>();
    mStateEventRefListHandler.fromJAXB
      (mAutomatonEventRefImporter, element, propositions);
    return mFactory.createStateProxy(name, initial, propositions);
  }

  private TransitionProxy importTransition(final TransitionType element)
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
  private class EventRefImporter
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
      final EventRefType eventref = (EventRefType) element;
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
