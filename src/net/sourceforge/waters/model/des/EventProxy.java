//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.des
//# CLASS:   EventProxy
//###########################################################################
//# $Id: EventProxy.java,v 1.1 2005-02-17 01:43:35 knut Exp $
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


public class EventProxy extends ImmutableNamedProxy {

  //#########################################################################
  //# Constructors
  public EventProxy(final String name, final EventKind kind)
  {
    super(name);
    mKind = kind;
  }

  EventProxy(final EventType event)
  {
    super(event);
    mKind = event.getKind();
  }


  //#########################################################################
  //# Getters and Setters
  public EventKind getKind()
  {
    return mKind;
  }

  public void setKind(final EventKind kind)
  {
    mKind = kind;
  }


  //#########################################################################
  //# Equals and Hashcode
  public boolean equals(final Object partner)
  {
    if (super.equals(partner)) {
      final EventProxy event = (EventProxy) partner;
      return mKind.equals(event.mKind);
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
    }
  }


  //#########################################################################
  //# Data Members
  private EventKind mKind;


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

}
