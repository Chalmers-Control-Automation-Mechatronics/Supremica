//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.module
//# CLASS:   EventListExpressionProxy
//###########################################################################
//# $Id: EventListExpressionProxy.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.module;

import java.io.IOException;
import java.util.List;
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.base.ElementFactory;
import net.sourceforge.waters.model.base.ModelException;
import net.sourceforge.waters.model.base.ModelPrinter;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.xsd.base.ElementType;
import net.sourceforge.waters.xsd.module.EventListExpressionType;
import net.sourceforge.waters.xsd.module.EventListType;


/**
 * An expression defined by an event list.
 *
 * An event list expression evaluates to a list of events that have been
 * grouped together such as it may occur in a parameter binding ({@link
 * ParameterBindingProxy}) of an instance component. The event list
 * functionality is implemented in class {@link EventListProxy}; this class
 * is just a wrapper so that event lists can be considered as expressions.
 *
 * @author Robi Malik
 */

public class EventListExpressionProxy extends ExpressionProxy {

  //#########################################################################
  //# Constructors
  /**
   * Creates an event list expression.
   * @param  list        The event list that defines the new expression.
   */
  public EventListExpressionProxy(final EventListProxy list)
  {
    mEventListProxy = list;
  }

  /**
   * Creates an event list expression from a parsed XML structure.
   * @param  expr        The parsed XML structure representing the
   *                     expression to be created.
   * @throws ModelException to indicate that the XML structure could
   *                     not be converted due to serious semantic
   *                     inconsistencies.
   */
  EventListExpressionProxy(final EventListExpressionType expr)
    throws ModelException
  {
    super(expr);
    mEventListProxy = new EventListProxy(expr.getEventList());
  }


  //#########################################################################
  //# Getters and Setters
  public EventListProxy getEventList()
  {
    return mEventListProxy;
  }


  //#########################################################################
  //# Equals and Hashcode
  public boolean equals(final Object partner)
  {
    if (partner != null &&
	getClass() == partner.getClass() &&
	super.equals(partner)) {
      final EventListExpressionProxy expr = 
	(EventListExpressionProxy) partner;
      return getEventList().equals(expr.getEventList());
    } else {
      return false;
    }    
  }


  //#########################################################################
  //# Printing
  public void pprint(final ModelPrinter printer)
    throws IOException
  {
    mEventListProxy.pprint(printer);
  }


  //#########################################################################
  //# Marshalling
  public void toJAXBElement(final ElementType element)
    throws JAXBException
  {
    super.toJAXBElement(element);
    final EventListExpressionType expr = (EventListExpressionType) element;
    final EventListType list = mEventListProxy.toEventListType();
    expr.setEventList(list);
  }

  public EventListExpressionType toEventListExpressionType()
    throws JAXBException
  {
    final ElementFactory factory = new EventListExpressionElementFactory();
    return (EventListExpressionType) toJAXB(factory);
  }


  //#########################################################################
  //# Local Class EventListExpressionElementFactory
  private static class EventListExpressionElementFactory
    extends ModuleElementFactory
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.model.base.ElementFactory
    public ElementType createElement(final Proxy proxy)
      throws JAXBException
    {
      return getFactory().createEventListExpression();
    }

    public ElementType createContainerElement()
      throws JAXBException
    {
      throw new UnsupportedOperationException
	("EventListExpression has no containing list!");
    }

    public List getElementList(final ElementType container)
    {
      throw new UnsupportedOperationException
	("EventListExpression has no containing list!");
    }

  }


  //#########################################################################
  //# Data Members
  private final EventListProxy mEventListProxy;

}
