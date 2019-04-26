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

package net.sourceforge.waters.model.marshaller;

import java.net.URI;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

import net.sourceforge.waters.model.base.IndexedList;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.xsd.base.AttributeMap;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.ElementType;
import net.sourceforge.waters.xsd.base.EventKind;
import net.sourceforge.waters.xsd.des.Automaton;
import net.sourceforge.waters.xsd.des.Event;
import net.sourceforge.waters.xsd.des.EventRef;
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
  @Override
  Proxy importElement(final ElementType element)
    throws WatersUnmarshalException
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

  @Override
  public ProductDESProxy importDocument(final ProductDES element,
                                        final URI uri)
    throws WatersUnmarshalException
  {
    // TODO Support import of counterexamples
    return importProductDES(element, uri);
  }


  //#########################################################################
  //# Importing Elements
  private ProductDESProxy importProductDES(final ProductDES element)
    throws WatersUnmarshalException
  {
    return importProductDES(element, null);
  }

  private ProductDESProxy importProductDES(final ProductDES element,
                                           final URI uri)
    throws WatersUnmarshalException
  {
    try {
      final String name = element.getName();
      final String comment = element.getComment();
      mProductDESEvents = new CheckedImportList<EventProxy>
        (ProductDESProxy.class, name, "event");
      mProductDESEventRefImporter = new EventRefImporter(mProductDESEvents);
      mProductDESEventListHandler.fromJAXBChecked
        (this, element, mProductDESEvents);
      final IndexedList<AutomatonProxy> automata =
        new CheckedImportList<AutomatonProxy>
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
    throws WatersUnmarshalException
  {
    try {
      final String name = element.getName();
      final ComponentKind kind = element.getKind();
      mAutomatonEvents = new CheckedImportList<EventProxy>
        (AutomatonProxy.class, name, "event");
      mAutomatonEventRefImporter = new EventRefImporter(mAutomatonEvents);
      mAutomatonEventRefListHandler.fromJAXBChecked
        (mProductDESEventRefImporter, element, mAutomatonEvents);
      mAutomatonStates = new CheckedImportList<StateProxy>
        (AutomatonProxy.class, name, "state");
      mAutomatonStateListHandler.fromJAXBChecked
        (this, element, mAutomatonStates);
      final Collection<TransitionProxy> transitions =
        new LinkedList<TransitionProxy>();
      mAutomatonTransitionListHandler.fromJAXB(this, element, transitions);
      final AttributeMap attribsElement = element.getAttributeMap();
      final Map<String,String> attribs = importAttributeMap(attribsElement);
      return mFactory.createAutomatonProxy
        (name, kind, mAutomatonEvents, mAutomatonStates, transitions, attribs);
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
    final AttributeMap attribsElement = element.getAttributeMap();
    final Map<String,String> attribs = importAttributeMap(attribsElement);
    return mFactory.createEventProxy(name, kind, observable, attribs);
  }

  private StateProxy importState(final State element)
    throws WatersUnmarshalException
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
    private EventRefImporter(final IndexedList<EventProxy> alphabet)
    {
      mAlphabet = alphabet;
    }

    //#######################################################################
    //# Overrides for Abstract Base Class JAXBImporter
    @Override
    EventProxy importElement(final ElementType element)
    {
      final EventRef eventref = (EventRef) element;
      final String name = eventref.getName();
      return mAlphabet.find(name);
    }

    //#######################################################################
    //# Data Members
    private final IndexedList<EventProxy> mAlphabet;

  }



  //#########################################################################
  //# Data Members
  private IndexedList<EventProxy> mProductDESEvents;
  private IndexedList<EventProxy> mAutomatonEvents;
  private IndexedList<StateProxy> mAutomatonStates;
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
