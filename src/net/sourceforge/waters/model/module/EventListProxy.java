//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.module
//# CLASS:   EventListProxy
//###########################################################################
//# $Id: EventListProxy.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.module;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.base.ElementFactory;
import net.sourceforge.waters.model.base.ModelException;
import net.sourceforge.waters.model.base.ModelPrinter;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyFactory;
import net.sourceforge.waters.model.base.TopLevelListProxy;
import net.sourceforge.waters.model.expr.SimpleExpressionProxy;
import net.sourceforge.waters.model.expr.SimpleExpressionProxyFactory;
import net.sourceforge.waters.xsd.base.ElementType;
import net.sourceforge.waters.xsd.module.EventListType;
import net.sourceforge.waters.xsd.module.ForeachEventType;
import net.sourceforge.waters.xsd.module.ForeachType;
import net.sourceforge.waters.xsd.module.IdentifierType;
import net.sourceforge.waters.xsd.module.ObjectFactory;


/**
 * <P>A structured list of event names.</P>
 *
 * <P>Event lists are used in various places in a module, where several
 * events are grouped together. The most common application is on
 * transitions in graphs ({@link LabelBlockProxy}), but event lists can also
 * be used for parameter bindings ({@link ParameterBindingProxy}) of
 * instance components or alias declarations ({@link EventAliasProxy}).</P>
 *
 * <P>Technically, an event list is an implementation of the {@link
 * java.util.List} interface, which can have two different kinds of
 * elements.</P>
 * <DL>
 * <DT>{@link net.sourceforge.waters.model.expr.IdentifierProxy}</DT>
 * <DD>Identifiers are used to include a single event with a given name (or
 * all elements of an array of events) in an event list. There can be
 * simple identifiers ({@link
 * net.sourceforge.waters.model.expr.SimpleIdentifierProxy}) that are just
 * names or indexed identifiers ({@link
 * net.sourceforge.waters.model.expr.IndexedIdentifierProxy}) that can have
 * one or more array indexes.</DD>
 * <DT>{@link ForeachEventProxy}</DT>
 * <DD>This construct can be used to include several events by processing a
 * loop.</DD>
 * </DL>
 *
 * @author Robi Malik
 */

public class EventListProxy extends TopLevelListProxy {

  //#######################################################################
  //# Constructor
  /**
   * Creates an empty event list.
   */
  public EventListProxy()
  {
  }

  /**
   * Creates an event list.
   * @param  input      The initial contents of the new event list.
   *                    Each element should be of type {@link
   *                    net.sourceforge.waters.model.expr.IdentifierProxy}
   *                    or {@link ForeachEventProxy}.
   */
  public EventListProxy(final Collection input)
  {
    super(input);
  }

  /**
   * Creates an event list from a parsed XML structure.
   * @param  parent      The parsed XML structure of the element containing
   *                     the event list to be created.
   * @throws ModelException to indicate that the XML structure could
   *                     not be converted due to serious semantic
   *                     inconsistencies.
   */
  EventListProxy(final ElementType parent)
    throws ModelException
  {
    super(parent, new MemberProxyFactory());
  }


  //#########################################################################
  //# Printing
  public void pprint(final ModelPrinter printer)
    throws IOException
  {
    if (getPPrintName() != null) {
      printer.print(getPPrintName());
      printer.print(' ');
    }

    final boolean shortprint = getShortPrint();
    if (shortprint) {
      if (size() == 0) {
	printer.print("{}");
	return;
      } else if (size() == 1 && get(0) instanceof SimpleExpressionProxy) {
	final SimpleExpressionProxy expr = (SimpleExpressionProxy) get(0);
	printer.print('{');
	expr.pprint(printer);
	printer.print('}');
	return;
      }
    }

    final Iterator iter = iterator();
    printer.println('{');
    printer.indentIn();
    while (iter.hasNext()) {
      final Proxy proxy = (Proxy) iter.next();
      proxy.pprintln(printer);
    }
    printer.indentOut();
    printer.print('}');
  }

  protected boolean getShortPrint()
  {
    return true;
  }


  //#########################################################################
  //# Marshalling
  /**
   * Creates an XML element representing the contents of this event list.
   */
  EventListType toEventListType()
    throws JAXBException
  {
    final ElementFactory factory = new MemberElementFactory();
    return (EventListType) toJAXB(factory);
  }


  //#########################################################################
  //# Static Class MemberProxyFactory
  private static class MemberProxyFactory implements ForeachProxyFactory {

    //#######################################################################
    //# Interface waters.model.base.ProxyFactory
    public Proxy createProxy(final ElementType element)
      throws ModelException
    {
      if (element instanceof IdentifierType) {
	return mSimpleExpressionProxyFactory.createProxy(element);
      } else if (element instanceof ForeachEventType) {
	final ForeachEventType foreach = (ForeachEventType) element;
	return new ForeachEventProxy(foreach, this);
      } else {
	throw new ClassCastException
	  ("Can't create event list member proxy for class " +
	   element.getClass().getName() + "!");
      }
    }

    public List getList(final ElementType parent)
    {
      final EventListType list = (EventListType) parent;
      return list.getList();
    }

    //#######################################################################
    //# Interface waters.model.module.ForeachProxyFactory
    public ElementType getForeachBody(final ForeachType foreach)
    {
      final ForeachEventType foreachevent = (ForeachEventType) foreach;
      return foreachevent.getEventList();
    }

    //#######################################################################
    //# Data Members
    private final SimpleExpressionProxyFactory
      mSimpleExpressionProxyFactory = new SimpleExpressionProxyFactory();

  }


  //#########################################################################
  //# Local Class MemberElementFactory
  static class MemberElementFactory extends ModuleElementFactory
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.model.base.ElementFactory
    public ElementType createElement(Proxy proxy)
      throws JAXBException
    {
      if (proxy instanceof SimpleExpressionProxy) {
	final SimpleExpressionProxy expr = (SimpleExpressionProxy) proxy;
	return expr.createElement(getFactory());
      } else if (proxy instanceof ForeachProxy) {
	final ForeachProxy foreach = (ForeachProxy) proxy;
	return getFactory().createForeachEvent();
      } else {
	throw new ClassCastException
	  ("Can't marshal object of type " +
	   proxy.getClass().getName() + " in an event list!");
      }
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

}
