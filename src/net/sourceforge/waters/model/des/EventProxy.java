//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.des
//# CLASS:   EventProxy
//###########################################################################
//# $Id: EventProxy.java,v 1.2 2005-02-21 19:19:51 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.des;

import java.io.IOException;
import java.util.List;
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.base.ImmutableNamedProxy;
import net.sourceforge.waters.model.base.ModelPrinter;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyFactory;
import net.sourceforge.waters.xsd.base.ElementType;
import net.sourceforge.waters.xsd.base.EventKind;
import net.sourceforge.waters.xsd.des.EventListType;
import net.sourceforge.waters.xsd.des.EventRefListType;
import net.sourceforge.waters.xsd.des.EventRefType;
import net.sourceforge.waters.xsd.des.EventType;


/**
 * <P>An event used by the automata in a DES.</P>
 *
 * <P>Each {@link ProductDESProxy} object consists of a set of automata
 * and an event alphabet. Each event can be used by one or more automata;
 * synchronisation is modelled by several automata using the same event.
 * The {@link ProductDESProxy} data structure is set up in such a way
 * that each event object is used only once. If several automata (or
 * transitions) share an event, they all will use the same object.</P>
 *
 * <P>In contrast to the event declarations of a module (class {@link
 * net.sourceforge.waters.model.module.EventDeclProxy}), the events
 * in a {@link ProductDESProxy} represent single events only.
 * Each event contains the following information.</P>
 *
 * <DL>
 * <DT><STRONG>Name.</STRONG></DT>
 * <DD>A string defining the name of the event. This name may be
 * a result from compilation of a module and therefore may contain
 * special charcters, e.g., <CODE>machine[1].start</CODE>.</DD>
 * <DT><STRONG>Kind.</STRONG></DT>
 * <DD>The type of the event. This can be <I>controllable</I>,
 * <I>uncontrollable</I>, or <I>proposition</I>.</DD>
 * <DT><STRONG>Observability.</STRONG></DT>
 * <DD>A boolean flag, indicating whether the event is
 * <I>observable</I>.</DD>
 * </DL>
 *
 * @author Robi Malik
 */

public class EventProxy extends ImmutableNamedProxy {

  //#########################################################################
  //# Constructors
  /**
   * Creates an observable event.
   * @param  name        The name of the new event.
   * @param  kind        The kind of the new event.
   */
  public EventProxy(final String name, final EventKind kind)
  {
    this(name, kind, true);
  }

  /**
   * Creates an event.
   * @param  name        The name of the new event.
   * @param  kind        The kind of the new event.
   * @param  observable  <CODE>true</CODE> if the event is to be observable,
   *                     <CODE>false</CODE> otherwise.
   */
  public EventProxy(final String name,
		    final EventKind kind,
		    final boolean observable)
  {
    super(name);
    mKind = kind;
    mIsObservable = observable;
  }

  /**
   * Creates an event from a parsed XML structure.
   * @param  decl        The parsed XML structure of the event.
   * @throws ModelException to indicate that the XML structure could
   *                     not be converted due to serious semantic
   *                     inconsistencies.
   */
  EventProxy(final EventType event)
  {
    super(event);
    mKind = event.getKind();
    mIsObservable = event.isObservable();
  }


  //#########################################################################
  //# Getters and Setters
  /**
   * Gets the kind of this event.
   * @return One of {@link EventKind#CONTROLLABLE},
   *         {@link EventKind#UNCONTROLLABLE}, or
   *         {@link EventKind#PROPOSITION}.
   */
  public EventKind getKind()
  {
    return mKind;
  }

  /**
   * Sets the kind of this event.
   * @param  kind        The new event kind, one of
   *                     {@link EventKind#CONTROLLABLE},
   *                     {@link EventKind#UNCONTROLLABLE}, or
   *                     {@link EventKind#PROPOSITION}.
   */
  public void setKind(final EventKind kind)
  {
    mKind = kind;
  }

  /**
   * Gets the observability status of this event.
   * @return <CODE>true</CODE> if the event is observable,
   *         <CODE>false</CODE> otherwise.
   */
  public boolean isObservable()
  {
    return mIsObservable;
  }

  /**
   * Sets the observability status of this event.
   * @param  observable  <CODE>true</CODE> if the event is to observable,
   *                     <CODE>false</CODE> otherwise.
   */
  public void setObservable(final boolean observable)
  {
    mIsObservable = observable;
  }


  //#########################################################################
  //# Equals and Hashcode
  public boolean equals(final Object partner)
  {
    if (super.equals(partner)) {
      final EventProxy event = (EventProxy) partner;
      return
	mKind.equals(event.mKind) &&
	(mIsObservable == event.mIsObservable);
    } else {
      return false;
    }    
  }


  //#########################################################################
  //# Printing
  public void pprint(final ModelPrinter printer)
    throws IOException
  {
    final EventKind kind = getKind();
    final String kindname = kind.toString();
    final String lowername = kindname.toLowerCase();
    printer.print(lowername);
    printer.print(' ');
    if (!isObservable()) {
      printer.print("unobservable ");
    }
    printer.print(getName());
  }


  //#########################################################################
  //# Marshalling
  public void toJAXBElement(final ElementType element)
    throws JAXBException
  {
    super.toJAXBElement(element);
    if (element instanceof EventType) {
      final EventType event = (EventType) element;
      event.setKind(getKind());
      event.setObservable(isObservable());
    }
  }


  //#########################################################################
  //# Local Class EventProxyFactory
  static class EventProxyFactory implements ProxyFactory
  {

    //#######################################################################
    //# Interface waters.model.base.ProxyFactory
    public Proxy createProxy(final ElementType element)
    {
      final EventType event = (EventType) element;
      return new EventProxy(event);
    }

    public List getList(ElementType parent)
    {
      final EventListType list = (EventListType) parent;
      return list.getList();
    }

  }


  //#########################################################################
  //# Local Class EventElementFactory
  static class EventElementFactory extends DESElementFactory
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.model.base.ElementFactory
    public ElementType createElement(final Proxy proxy)
      throws JAXBException
    {
      return getFactory().createEvent();
    }

    public ElementType createContainerElement()
      throws JAXBException
    {
      return getFactory().createEventList();
    }

    public List getElementList(final ElementType container)
    {
      final EventListType list = (EventListType) container;
      return list.getList();
    }

  }


  //#########################################################################
  //# Local Class EventRefElementFactory
  static class EventRefElementFactory extends DESElementFactory
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.model.base.ElementFactory
    public ElementType createElement(final Proxy proxy)
      throws JAXBException
    {
      return getFactory().createEventRef();
    }

    public ElementType createContainerElement()
      throws JAXBException
    {
      return getFactory().createEventRefList();
    }

    public List getElementList(final ElementType container)
    {
      final EventRefListType list = (EventRefListType) container;
      return list.getList();
    }

  }


  //#########################################################################
  //# Data Members
  private EventKind mKind;
  private boolean mIsObservable;

}
