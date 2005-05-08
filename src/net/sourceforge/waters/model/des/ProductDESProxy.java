//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.des
//# CLASS:   ProductDESProxy
//###########################################################################
//# $Id: ProductDESProxy.java,v 1.3 2005-05-08 00:27:15 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.des;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.base.DocumentProxy;
import net.sourceforge.waters.model.base.DuplicateNameException;
import net.sourceforge.waters.model.base.ElementFactory;
import net.sourceforge.waters.model.base.HashSetProxy;
import net.sourceforge.waters.model.base.ModelException;
import net.sourceforge.waters.model.base.ModelPrinter;
import net.sourceforge.waters.model.base.MutableNamedProxy;
import net.sourceforge.waters.model.base.NameNotFoundException;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyFactory;
import net.sourceforge.waters.xsd.base.ElementType;
import net.sourceforge.waters.xsd.des.AutomataListType;
import net.sourceforge.waters.xsd.des.AutomatonType;
import net.sourceforge.waters.xsd.des.EventListType;
import net.sourceforge.waters.xsd.des.ProductDESType;


/**
 * <P>A collection of finite-state machines.</P>
 *
 * <P>A product DES is a set of finite-state machines that are to be
 * composed by the synchronous product operation, synchronising by shared
 * events. The product DES includes the event alphabet available to all
 * finite-state machines, but each finite-state machine has its own event
 * alphabet identifying the set of events it actually synchronises on.</P>
 *
 * <P>In contrast to the module representation ({@link
 * net.sourceforge.waters.model.module.ModuleProxy}), the product DES
 * representation is very simple and supports no parametric
 * structures. Most product DES are obtained from a module by
 * compilation.</P>
 * 
 * @see net.sourceforge.waters.model.module.ModuleProxy
 * @see net.sourceforge.waters.model.compiler.ModuleCompiler
 *
 * @author Robi Malik
 */

public class ProductDESProxy
  extends MutableNamedProxy
  implements DocumentProxy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates an empty product DES.
   * @param  name        The name for the new product DES.
   */
  public ProductDESProxy(final String name)
  {
    this(name, null);
  }

  /**
   * Creates an empty product DES.
   * @param  name        The name for the new product DES.
   * @param  location    The full path name of the file where this product DES
   *                     will be saved to.
   */
  public ProductDESProxy(final String name, final File location)
  {
    super(name);
    mLocation = location;
    mEventSetProxy = new EventSetProxy();
    mEventFactory = new EventLookupFactory(mEventSetProxy);
    mAutomataSetProxy = new AutomataSetProxy();
  }

  /**
   * Creates a product DES from a parsed XML structure.
   * This method is for internal use only and should not be called
   * directly; use class
   * {@link net.sourceforge.waters.model.base.ProxyMarshaller
   * ProxyMarshaller} instead.
   * @param  des         The parsed XML structure of the new product DES.
   * @param  location    The full path name of the file where this product DES
   *                     will be saved to.
   * @throws ModelException to indicate that the XML structure could
   *                     not be converted due to serious semantic
   *                     inconsistencies.
   */
  ProductDESProxy(final ProductDESType des, final File location)
    throws ModelException
  {
    super(des);
    mLocation = location;
    mEventSetProxy = new EventSetProxy(des);
    mEventFactory = new EventLookupFactory(mEventSetProxy);
    mAutomataSetProxy = new AutomataSetProxy(des, mEventFactory);
  }


  //#########################################################################
  //# Getters and Setters
  /**
   * Gets the set of events for this product DES.
   * @return  An unmodifiable set of objects of type {@link EventProxy}.
   */
  public Collection getEvents()
  {
    return Collections.unmodifiableCollection(mEventSetProxy);
  }

  /**
   * Adds an event to this product DES.
   * This method makes sure that the product DES's event set includes
   * the given event, by adding it if not already present.
   * @param  event  The event to be added.
   * @return The event that is now contained in the event list. This may be
   *         the given event or another one that is equal and was there
   *         before.
   * @throws DuplicateNameException to indicate that the product DES already
   *                has another event with the same name.
   */
  public EventProxy addEvent(final EventProxy event)
    throws DuplicateNameException
  {
    return (EventProxy) mEventSetProxy.insert(event);
  }

  /**
   * Finds an event with given name.
   * @param  name        The name of the event to be found.
   * @return The corresponding event,
   *         or <CODE>null</CODE> if none was found.
   */
  public EventProxy findEvent(final String name)
    throws NameNotFoundException
  {
    return (EventProxy) mEventSetProxy.find(name);
  }

  /**
   * Tries to find an event with given name.
   * @param  name        The name of the event to be found.
   * @return The corresponding even.
   * @throws NameNotFoundException to indicate that the product DES does not
   *                      contain any event with the given name.
   */
  public EventProxy getEvent(final String name)
  {
    return (EventProxy) mEventSetProxy.get(name);
  }

  /**
   * Gets the list of automata for this product DES.
   * @return  An unmodifiable list of objects of type {@link AutomatonProxy}.
   */
  public Collection getAutomata()
  {
    return Collections.unmodifiableCollection(mAutomataSetProxy);
  }

  /**
   * Adds an automaton to this product DES.
   * This method appends the given automaton to the product DES's automata
   * list. Before adding, it makes sure that the automaton's events are
   * all included in the event set of the product DES by adding any that
   * are missing.
   * @param  aut    The automaton to be added.
   * @throws DuplicateNameException to indicate that the DES already
   *                has a different automaton with the same name.
   */
  public void addAutomaton(final AutomatonProxy aut)
    throws DuplicateNameException
  {
    aut.setEventFactory(mEventFactory);
    mAutomataSetProxy.insert(aut);
  }

  /**
   * Finds an automaton with given name.
   * @param  name        The name of the automaton to be found.
   * @return The corresponding automaton.
   * @throws NameNotFoundException to indicate that the product DES does not
   *                      contain any automaton with the given name.
   */
  public AutomatonProxy findAutomaton(final String name)
    throws NameNotFoundException
  {
    return (AutomatonProxy) mAutomataSetProxy.find(name);
  }

  /**
   * Tries to find an automaton with given name.
   * @param  name        The name of the automaton to be found.
   * @return The corresponding automaton.
   * @throws NameNotFoundException to indicate that the product DES does not
   *                      contain any automaton with the given name.
   */
  public AutomatonProxy getAutomaton(final String name)
  {
    return (AutomatonProxy) mAutomataSetProxy.get(name);
  }


  //#########################################################################
  //# Equals and Hashcode
  public boolean equals(final Object partner)
  {
    if (super.equals(partner)) {
      final ProductDESProxy des = (ProductDESProxy) partner;
      return
	mEventSetProxy.equals(des.mEventSetProxy) &&
	mAutomataSetProxy.equals(des.mAutomataSetProxy);
    } else {
      return false;
    }    
  }


  //#########################################################################
  //# Printing
  public void pprint(final ModelPrinter printer)
    throws IOException
  {
    printer.print("DES ");
    printer.println(getName());
    mEventSetProxy.pprint(printer);
    mAutomataSetProxy.pprint(printer);
  }


  //#########################################################################
  //# Marshalling
  public void toJAXBElement(final ElementType element)
    throws JAXBException
  {
    super.toJAXBElement(element);
    final ProductDESType des = (ProductDESType) element;
    final ElementFactory eventfactory = new EventProxy.EventElementFactory();
    final EventListType eventlist =
      (EventListType) mEventSetProxy.toJAXB(eventfactory);
    des.setEventList(eventlist);
    final ElementFactory autfactory =
      new AutomatonProxy.AutomatonElementFactory();
    final AutomataListType autlist =
      (AutomataListType) mAutomataSetProxy.toJAXB(autfactory);
    des.setAutomataList(autlist);
  }

  public ProductDESType toProductDESType()
    throws JAXBException
  {
    final ElementFactory factory = new ProductDESElementFactory();
    return (ProductDESType) toJAXB(factory);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.base.DocumentProxy
  public File getLocation()
  {
    return mLocation;
  }

  public void setLocation(final File location)
  {
    mLocation = location;
  }


  //#########################################################################
  //# Local Class EventSetProxy
  private static class EventSetProxy extends HashSetProxy {

    //#######################################################################
    //# Constructors
    EventSetProxy()
    {
    }

    EventSetProxy(final ProductDESType des)
      throws ModelException
    {
      super(des.getEventList(), new EventProxy.EventProxyFactory());
    }

    //#######################################################################
    //# Overrides from abstract class HashSetProxy
    protected String getPPrintName()
    {
      return "EVENTS";
    }
  
  }


  //#########################################################################
  //# Local Class AutomataSetProxy
  private static class AutomataSetProxy extends HashSetProxy {

    //#######################################################################
    //# Constructors
    AutomataSetProxy()
    {
    }

    AutomataSetProxy(final ProductDESType des,
		     final EventLookupFactory eventfactory)
      throws ModelException
    {
      super(des.getAutomataList(),
	    new AutomatonProxy.AutomatonProxyFactory(eventfactory));
    }

    //#######################################################################
    //# Overrides from abstract class HashSetProxy
    protected String getPPrintName()
    {
      return "AUTOMATA";
    }
  
  }


  //#########################################################################
  //# Local Class ProductDESElementFactory
  private static class ProductDESElementFactory extends DESElementFactory
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.model.base.ElementFactory
    public ElementType createElement(final Proxy proxy)
      throws JAXBException
    {
      return getFactory().createProductDES();
    }

    public ElementType createContainerElement()
      throws JAXBException
    {
      throw new UnsupportedOperationException
	("ProductDES has no containing list!");
    }

    public List getElementList(final ElementType container)
    {
      throw new UnsupportedOperationException
	("ProductDES has no containing list!");
    }

  }


  //#########################################################################
  //# Data Members
  private File mLocation;
  private final HashSetProxy mEventSetProxy;
  private final EventLookupFactory mEventFactory;
  private final HashSetProxy mAutomataSetProxy;

}
