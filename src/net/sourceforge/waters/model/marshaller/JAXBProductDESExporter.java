//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.marshaller
//# CLASS:   JAXBProductDESExporter
//###########################################################################
//# $Id: JAXBProductDESExporter.java,v 1.2 2005-11-03 01:24:16 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.marshaller;

import java.util.Collection;
import java.util.List;
import java.util.TreeSet;
import java.util.Set;
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.base.IndexedSet;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.des.AbstractProductDESProxyVisitor;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyVisitor;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;

import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.ElementType;
import net.sourceforge.waters.xsd.base.EventKind;
import net.sourceforge.waters.xsd.des.AutomatonType;
import net.sourceforge.waters.xsd.des.EventRefType;
import net.sourceforge.waters.xsd.des.EventType;
import net.sourceforge.waters.xsd.des.ObjectFactory;
import net.sourceforge.waters.xsd.des.ProductDESType;
import net.sourceforge.waters.xsd.des.StateType;
import net.sourceforge.waters.xsd.des.TransitionType;


class JAXBProductDESExporter
  extends JAXBDocumentExporter<ProductDESProxy,ProductDESType>
  implements ProductDESProxyVisitor
{

  //#########################################################################
  //# Overrides for Abstract Base Class JAXBExporter
  ProductDESType exportDocument(final ProductDESProxy proxy)
    throws VisitorException
  {
    return visitProductDESProxy(proxy);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.des.ProductDESProxyVisitor
  public ProductDESType visitProductDESProxy(final ProductDESProxy proxy)
    throws VisitorException
  {
    try {
      final ProductDESType element = mFactory.createProductDES();
      copyProductDESProxy(proxy, element);
      return element;
    } catch (final JAXBException exception) {
      throw wrap(exception);
    }
  }

  public AutomatonType visitAutomatonProxy(final AutomatonProxy proxy)
    throws VisitorException
  {
    try {
      final AutomatonType element = mFactory.createAutomaton();
      copyAutomatonProxy(proxy, element);
      return element;
    } catch (final JAXBException exception) {
      throw wrap(exception);
    }
  }

  public EventType visitEventProxy(final EventProxy proxy)
    throws VisitorException
  {
    try {
      final EventType element = mFactory.createEvent();
      copyEventProxy(proxy, element);
      return element;
    } catch (final JAXBException exception) {
      throw wrap(exception);
    }
 }

  public StateType visitStateProxy(final StateProxy proxy)
    throws VisitorException
  {
    try {
      final StateType element = mFactory.createState();
      copyStateProxy(proxy, element);
      return element;
    } catch (final JAXBException exception) {
      throw wrap(exception);
    }
  }

  public TransitionType visitTransitionProxy(final TransitionProxy proxy)
    throws VisitorException
  {
    try {
      final TransitionType element = mFactory.createTransition();
      copyTransitionProxy(proxy, element);
      return element;
    } catch (final JAXBException exception) {
      throw wrap(exception);
    }
  }


  //#########################################################################
  //# Copying Data
  private void copyProductDESProxy(final ProductDESProxy proxy,
                                   final ProductDESType element)
    throws VisitorException
  {
    try {
      copyDocumentProxy(proxy, element);
      mProductDESEvents = new CheckedExportSet<EventProxy>
        (proxy.getEvents(), proxy, "event");
      mProductDESEventRefExporter = new EventRefExporter(mProductDESEvents);
      mProductDESEventListHandler.toJAXB(this, mProductDESEvents, element);
      final Set<AutomatonProxy> automata = new CheckedExportSet<AutomatonProxy>
        (proxy.getAutomata(), proxy, "automaton");
      mProductDESAutomataListHandler.toJAXB(this, automata, element);
    } catch (final JAXBException exception) {
      throw wrap(exception);
    } finally {
      mProductDESEvents = null;
      mProductDESEventRefExporter = null;
    }
  }

  private void copyAutomatonProxy(final AutomatonProxy proxy,
                                  final AutomatonType element)
    throws VisitorException
  {
    try {
      copyNamedProxy(proxy, element);
      element.setKind(proxy.getKind());
      mAutomatonEvents = new CheckedExportSet<EventProxy>
        (proxy.getEvents(), proxy, "event");
      mAutomatonEventRefExporter = new EventRefExporter(mAutomatonEvents);
      mAutomatonEventRefListHandler.toJAXB
        (mProductDESEventRefExporter, mAutomatonEvents, element);
      mAutomatonStates = new CheckedExportSet<StateProxy>
        (proxy.getStates(), proxy, "state");
      mAutomatonStateListHandler.toJAXB(this, mAutomatonStates, element);
      final Set<TransitionProxy> transitions =
        new TreeSet<TransitionProxy>(proxy.getTransitions());
      mAutomatonTransitionListHandler.toJAXB(this, transitions, element);
    } catch (final JAXBException exception) {
      throw wrap(exception);
    } finally {
      mAutomatonEvents = null;
      mAutomatonStates = null;
      mAutomatonEventRefExporter = null;
    }
  }

  private void copyEventProxy(final EventProxy proxy, final EventType element)
  {
    copyNamedProxy(proxy, element);
    element.setKind(proxy.getKind());
    element.setObservable(proxy.isObservable());
  }

  private void copyStateProxy(final StateProxy proxy, final StateType element)
    throws VisitorException
  {
    try {
      copyNamedProxy(proxy, element);
      element.setInitial(proxy.isInitial());
      final Collection<EventProxy> props = proxy.getPropositions();
      mStateEventRefListHandler.toJAXB
        (mAutomatonEventRefExporter, props, element);
    } catch (final JAXBException exception) {
      throw wrap(exception);
    }
  }

  private void copyTransitionProxy(final TransitionProxy proxy,
                                   final TransitionType element)
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
  //# Inner Class EventRefExporter
  private class EventRefExporter
    extends AbstractProductDESProxyVisitor
  {

    //#######################################################################
    //# Constructors
    private EventRefExporter(final IndexedSet<EventProxy> alphabet)
    {
      mAlphabet = alphabet;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.des.ProductDESProxyVisitor
    public EventRefType visitEventProxy(final EventProxy proxy)
      throws VisitorException
    {
      try {
        mAlphabet.checkUnique(proxy);
        final EventRefType element = mFactory.createEventRef();
        copyEventProxy(proxy, element);
        return element;
      } catch (final JAXBException exception) {
        throw wrap(exception);
      }
    }

    //#######################################################################
    //# Copying Data
    private void copyEventProxy(final EventProxy proxy,
                                final EventRefType element)
      throws VisitorException
    {
      copyNamedProxy(proxy, element);
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
  private EventRefExporter mProductDESEventRefExporter;
  private EventRefExporter mAutomatonEventRefExporter;

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

  private static final ObjectFactory mFactory = new ObjectFactory();

}
