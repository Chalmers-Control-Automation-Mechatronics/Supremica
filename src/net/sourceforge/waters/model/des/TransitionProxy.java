//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.des
//# CLASS:   EventProxy
//###########################################################################
//# $Id: TransitionProxy.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.des;

import java.io.IOException;
import java.util.List;
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.base.ElementProxy;
import net.sourceforge.waters.model.base.ModelPrinter;
import net.sourceforge.waters.model.base.NameNotFoundException;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyFactory;
import net.sourceforge.waters.xsd.base.ElementType;
import net.sourceforge.waters.xsd.des.TransitionListType;
import net.sourceforge.waters.xsd.des.TransitionType;


public class TransitionProxy extends ElementProxy implements Comparable {

  //#########################################################################
  //# Constructors
  public TransitionProxy(final StateProxy source,
			 final StateProxy target,
			 final EventProxy event)
  {
    mSource = source;
    mTarget = target;
    mEvent = event;
  }

  TransitionProxy(final TransitionType trans,
		  final EventLookupFactory eventfactory,
		  final StateLookupFactory statefactory)
    throws NameNotFoundException
  {
    mSource = statefactory.findState(trans.getSource());
    mTarget = statefactory.findState(trans.getTarget());
    mEvent = eventfactory.findEvent(trans.getEvent());
  }


  //#########################################################################
  //# Getters and Setters
  public StateProxy getSource()
  {
    return mSource;
  }

  public StateProxy getTarget()
  {
    return mTarget;
  }

  public EventProxy getEvent()
  {
    return mEvent;
  }


  //#########################################################################
  //# Equals and Hashcode
  public boolean equals(final Object partner)
  {
    if (getClass() == partner.getClass() && super.equals(partner)) {
      final TransitionProxy trans = (TransitionProxy) partner;
      return
	getSource().refequals(trans.getSource()) &&
	getTarget().refequals(trans.getTarget()) &&
	getEvent().refequals(trans.getEvent());
    } else {
      return false;
    }    
  }

  public int hashCode()
  {
    return
      getSource().hashCode() +
      5 * getEvent().hashCode() +
      25 * getTarget().hashCode();
  }


  //#########################################################################
  //# Interface java.lang.Comparable
  public int compareTo(final Object partner)
  {
    final TransitionProxy trans = (TransitionProxy) partner;
    final int compsource = getSource().compareTo(trans.getSource());
    if (compsource != 0) {
      return compsource;
    }
    final int compevent = getEvent().compareTo(trans.getEvent());
    if (compevent != 0) {
      return compevent;
    }
    return getTarget().compareTo(trans.getTarget());
  }

 
  //#########################################################################
  //# Printing
  public void pprint(final ModelPrinter printer)
    throws IOException
  {
    printer.print(getSource().getName());
    printer.print(" -> ");
    printer.print(getTarget().getName());
    printer.print(" {");
    printer.print(getEvent().getName());
    printer.print('}');
  }


  //#########################################################################
  //# Marshalling
  public void toJAXBElement(final ElementType element)
    throws JAXBException
  {
    super.toJAXBElement(element);
    final TransitionType trans = (TransitionType) element;
    trans.setSource(mSource.getName());
    trans.setTarget(mTarget.getName());
    trans.setEvent(mEvent.getName());
  }


  //#########################################################################
  //# Data Members
  private final StateProxy mSource;
  private final StateProxy mTarget;
  private final EventProxy mEvent;


  //#########################################################################
  //# Local Class TransitionProxyFactory
  static class TransitionProxyFactory implements ProxyFactory
  {

    //#######################################################################
    //# Constructor
    TransitionProxyFactory(final EventLookupFactory eventfactory,
			   final StateLookupFactory statefactory)
    {
      mEventFactory = eventfactory;
      mStateFactory = statefactory;
    }

    //#######################################################################
    //# Interface waters.model.base.ProxyFactory
    public Proxy createProxy(final ElementType element)
      throws NameNotFoundException
    {
      final TransitionType trans = (TransitionType) element;
      return new TransitionProxy(trans, mEventFactory, mStateFactory);
    }

    public List getList(ElementType parent)
    {
      final TransitionListType list = (TransitionListType) parent;
      return list.getList();
    }

    //#######################################################################
    //# Data Members
    final EventLookupFactory mEventFactory;
    final StateLookupFactory mStateFactory;

  }


  //#########################################################################
  //# Local Class TransitionElementFactory
  static class TransitionElementFactory extends DESElementFactory
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.model.base.ElementFactory
    public ElementType createElement(Proxy proxy)
      throws JAXBException
    {
      return getFactory().createTransition();
    }

    public ElementType createContainerElement()
      throws JAXBException
    {
      return getFactory().createTransitionList();
    }

    public List getElementList(final ElementType container)
    {
      final TransitionListType list = (TransitionListType) container;
      return list.getList();
    }

  }

}
