//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.module
//# CLASS:   EventAliasProxy
//###########################################################################
//# $Id: EventAliasProxy.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.module;

import java.util.List;
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.base.ModelException;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.expr.IdentifierProxy;
import net.sourceforge.waters.xsd.base.ElementType;
import net.sourceforge.waters.xsd.module.EventAliasListType;
import net.sourceforge.waters.xsd.module.EventAliasType;
import net.sourceforge.waters.xsd.module.ForeachEventAliasType;
import net.sourceforge.waters.xsd.module.ForeachType;


/**
 * <P>An alias representing a group of events.</P>
 *
 * <P>An event alias is used to give a name of a group events used together
 * in different edges or graphs of a module. This kind of alias can occur
 * in the <I>event alias list</I> of a module, or in the body of a
 * <I>foreach alias</I> construct ({@link ForeachEventAliasProxy}).  The
 * name of an event can be an arbitrary identifier; it can be indexed to
 * support the use of the <I>foreach alias</I> construct. Its value must be
 * an event list expression ({@link EventListExpressionProxy}).</P>
 *
 * @author Robi Malik
 */

public class EventAliasProxy extends AliasProxy {

  //#########################################################################
  //# Constructors
  /**
   * Creates an event alias.
   * @param  ident       The name for the new alias.
   * @param  eventlist   The list of events defining the new alias.
   */
  public EventAliasProxy(final IdentifierProxy ident,
			 final EventListProxy eventlist)
  {
    this(ident, new EventListExpressionProxy(eventlist));
  }

  /**
   * Creates an event alias.
   * @param  ident       The name for the new alias.
   * @param  expr        The event list expression defining the new alias.
   */
  public EventAliasProxy(final IdentifierProxy ident,
			 final EventListExpressionProxy expr)
  {
    super(ident);
    mExpression = expr;
  }

  /**
   * Creates an event alias from a parsed XML structure.
   * This method is for internal use only and should not be called
   * directly.
   * @param  alias       The parsed XML structure of the new element.
   * @throws ModelException to indicate that the XML structure could
   *                     not be converted due to serious semantic
   *                     inconsistencies.
   */
  EventAliasProxy(final EventAliasType alias)
    throws ModelException
  {
    super(alias);
    mExpression = new EventListExpressionProxy(alias.getExpression());
  }


  //#########################################################################
  //# Getters and Setters
  public ExpressionProxy getExpression()
  {
    return mExpression;
  }

  public EventListProxy getEventList()
  {
    return mExpression.getEventList();
  }

  public void setExpression(final ExpressionProxy expr)
  {
    mExpression = (EventListExpressionProxy) expr;
  }


  //#########################################################################
  //# Equals and Hashcode
  public boolean equals(final Object partner)
  {
    if (super.equals(partner)) {
      final EventAliasProxy alias = (EventAliasProxy) partner;
      return
	getExpression().equals(alias.getExpression());
    } else {
      return false;
    }    
  }


  //#########################################################################
  //# Marshalling
  public void toJAXBElement(final ElementType element)
    throws JAXBException
  {
    super.toJAXBElement(element);
    final EventAliasType alias = (EventAliasType) element;
    alias.setExpression(mExpression.toEventListExpressionType());
  }

 
  //#########################################################################
  //# Local Class EventAliasFactory
  static class EventAliasProxyFactory implements ForeachProxyFactory
  {
    //#######################################################################
    //# Interface waters.model.module.ProxyFactory
    public Proxy createProxy(final ElementType element)
      throws ModelException
    {
      if (element instanceof EventAliasType) {
	final EventAliasType alias = (EventAliasType) element;
	return new EventAliasProxy(alias);
      } else if (element instanceof ForeachEventAliasType) {
	final ForeachEventAliasType foreach = (ForeachEventAliasType) element;
 	return new ForeachEventAliasProxy(foreach, this);
      } else {
	throw new ClassCastException
	  ("Can't create event alias proxy for class " +
	   element.getClass().getName() + "!");
      }
    }

    public List getList(final ElementType parent)
    {
      final EventAliasListType list = (EventAliasListType) parent;
      return list.getList();
    }

    //#######################################################################
    //# Interface waters.model.module.ForeachProxyFactory
    public ElementType getForeachBody(final ForeachType foreach)
    {
      final ForeachEventAliasType foreachalias =
	(ForeachEventAliasType) foreach;
      return foreachalias.getEventAliasList();
    }

  }


  //#########################################################################
  //# Local Class EventAliasElementFactory
  static class EventAliasElementFactory extends ModuleElementFactory
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.model.base.ElementFactory
    public ElementType createElement(Proxy proxy)
      throws JAXBException
    {
      if (proxy instanceof EventAliasProxy) {
	return getFactory().createEventAlias();
      } else if (proxy instanceof ForeachProxy) {
	return getFactory().createForeachEventAlias();
      } else {
	throw new ClassCastException
	  ("Can't marshal object of type " +
	   proxy.getClass().getName() + " as event alias!");
      }
    }

    public ElementType createContainerElement()
      throws JAXBException
    {
      return getFactory().createEventAliasList();
    }

    public List getElementList(final ElementType container)
    {
      final EventAliasListType list = (EventAliasListType) container;
      return list.getList();
    }

  }


  //#########################################################################
  //# Data Members
  private EventListExpressionProxy mExpression;

}
