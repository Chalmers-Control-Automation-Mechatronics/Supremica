//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.module
//# CLASS:   ExpressionProxy
//###########################################################################
//# $Id: ExpressionProxy.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.module;

import java.util.List;
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.base.ElementFactory;
import net.sourceforge.waters.model.base.ElementProxy;
import net.sourceforge.waters.model.base.ModelException;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyFactory;
import net.sourceforge.waters.model.expr.SimpleExpressionProxy;
import net.sourceforge.waters.model.expr.SimpleExpressionProxyFactory;
import net.sourceforge.waters.xsd.base.ElementType;
import net.sourceforge.waters.xsd.module.EventListExpressionType;
import net.sourceforge.waters.xsd.module.ExpressionType;
import net.sourceforge.waters.xsd.module.SimpleExpressionType;


/**
 * The abstract base class of all expressions.
 *
 * This is the abstract base class of all expressions that may occur in a
 * module. This includes the expressions in package {@link
 * net.sourceforge.waters.model.expr} as well as the special event list
 * expressions ({@link
 * net.sourceforge.waters.model.module.EventListExpressionProxy}) that are
 * specific to modules.
 *
 * @author Robi Malik
 */

public abstract class ExpressionProxy extends ElementProxy {

  //#########################################################################
  //# Constructors
  /**
   * Creates an expression.
   */
  public ExpressionProxy()
  {
  }

  /**
   * Creates an expression from a parsed XML structure.
   * @param  expr        The parsed XML structure of the new expression.
   * @throws ModelException to indicate that the XML structure could
   *                     not be converted due to serious semantic
   *                     inconsistencies.
   */
  public ExpressionProxy(final ExpressionType expr)
  {
  }


  //#########################################################################
  //# Marshalling
  public ExpressionType toExpressionType()
    throws JAXBException
  {
    final ElementFactory factory = new ExpressionElementFactory();
    return (ExpressionType) toJAXB(factory);
  }


  //#########################################################################
  //# Local Class ExpressionProxyFactory
  static class ExpressionProxyFactory implements ProxyFactory
  {
    //#######################################################################
    //# Interface waters.model.module.ProxyFactory
    public Proxy createProxy(final ElementType element)
      throws ModelException
    {
      if (element instanceof SimpleExpressionType) {
	return mSimpleExpressionProxyFactory.createProxy(element);
      } else if (element instanceof EventListExpressionType) {
	final EventListExpressionType expr =
	  (EventListExpressionType) element;
	return new EventListExpressionProxy(expr);
      } else {
	throw new ClassCastException
	  ("Can't create event expression proxy for class " +
	   element.getClass().getName() + "!");
      }
    }

    public List getList(ElementType parent)
    {
      throw new UnsupportedOperationException
	("No default list implemented in " + getClass().getName() + "!");
    }

    //#######################################################################
    //# Data Members
    private final SimpleExpressionProxyFactory
      mSimpleExpressionProxyFactory = new SimpleExpressionProxyFactory();

  }


  //#########################################################################
  //# Local Class ExpressionElementFactory
  static class ExpressionElementFactory
    extends ModuleElementFactory
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.model.base.ElementFactory
    public ElementType createElement(final Proxy proxy)
      throws JAXBException
    {
      if (proxy instanceof SimpleExpressionProxy) {
	final SimpleExpressionProxy expr = (SimpleExpressionProxy) proxy;
	return expr.createElement(getFactory());
      } else if (proxy instanceof EventListExpressionProxy) {
	return getFactory().createEventListExpression();
      } else {
	throw new ClassCastException
	  ("Can't marshal object of type " +
	   proxy.getClass().getName() + " as event expression!");
      }
    }

    public ElementType createContainerElement()
      throws JAXBException
    {
      throw new UnsupportedOperationException
	("No default list implemented in " + getClass().getName() + "!");
    }

    public List getElementList(final ElementType container)
    {
      throw new UnsupportedOperationException
	("No default list implemented in " + getClass().getName() + "!");
    }

  }

}
