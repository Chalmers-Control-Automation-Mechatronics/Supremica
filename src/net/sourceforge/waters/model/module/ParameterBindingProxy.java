//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.module
//# CLASS:   ParameterBindingProxy
//###########################################################################
//# $Id: ParameterBindingProxy.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.module;

import java.io.IOException;
import java.util.List;
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.base.ModelException;
import net.sourceforge.waters.model.base.ModelPrinter;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyFactory;
import net.sourceforge.waters.model.base.UniqueElementProxy;
import net.sourceforge.waters.xsd.base.ElementType;
import net.sourceforge.waters.xsd.module.ExpressionType;
import net.sourceforge.waters.xsd.module.ParameterBindingType;


/**
 * A name-value pair.
 *
 * Parameter bindings are used in the binding list of instance components
 * ({@link InstanceProxy}) to describe which values are to be passed to a
 * module when it is instantiated. Each parameter binding consists of a
 * name, identifying which parameter of the instantiated module is to be
 * bound, and an expression, which evaluates to the value to be bound to
 * that parameter.
 *
 * @author Robi Malik
 */

public class ParameterBindingProxy extends UniqueElementProxy {

  //#########################################################################
  //# Constructors
  /**
   * Creates a new parameter binding.
   * @param  name        The name of the parameter to be bound.
   * @param  expr        The expression to be bound to the name.
   */
  public ParameterBindingProxy(final String name, final ExpressionProxy expr)
  {
    super(name);
    mExpression = expr;
  }

  /**
   * Creates a parameter binding from a parsed XML structure.
   * @param  binding     The parsed XML structure of the new parameter
   *                     binding.
   * @throws ModelException to indicate that the XML structure could
   *                     not be converted due to serious semantic
   *                     inconsistencies.
   */
  ParameterBindingProxy(final ParameterBindingType binding)
    throws ModelException
  {
    super(binding);
    final ProxyFactory factory =
      new ExpressionProxy.ExpressionProxyFactory();
    final ExpressionType expr = binding.getExpression();
    mExpression = (ExpressionProxy) factory.createProxy(expr);
  }


  //#########################################################################
  //# Getters and Setters
  public ExpressionProxy getExpression()
  {
    return mExpression;
  }

  public void setExpression(final ExpressionProxy expr)
  {
    mExpression = expr;
  }


  //#########################################################################
  //# Equals and Hashcode
  public boolean equals(final Object partner)
  {
    if (partner != null && getClass() == partner.getClass() &&
	super.equals(partner)) {
      final ParameterBindingProxy binding = (ParameterBindingProxy) partner;
      return getExpression().equals(binding.getExpression());
    } else {
      return false;
    }    
  }


  //#########################################################################
  //# Printing
  public void pprint(final ModelPrinter printer)
    throws IOException
  {
    printer.print(getName());
    printer.print(" = ");
    mExpression.pprint(printer);
  }


  //#########################################################################
  //# Marshalling
  public void toJAXBElement(final ElementType element)
    throws JAXBException
  {
    super.toJAXBElement(element);
    final ParameterBindingType binding = (ParameterBindingType) element;
    ExpressionType expr = getExpression().toExpressionType();
    binding.setExpression(expr);
  }


  //#########################################################################
  //# Data Members
  private ExpressionProxy mExpression;


  //#########################################################################
  //# Local Class ParameterBindingProxyFactory
  static class ParameterBindingProxyFactory implements ProxyFactory
  {
    //#######################################################################
    //# Interface waters.model.module.ProxyFactory
    public Proxy createProxy(final ElementType element)
      throws ModelException
    {
      final ParameterBindingType binding = (ParameterBindingType) element;
      return new ParameterBindingProxy(binding);
    }

    public List getList(ElementType parent)
    {
      throw new UnsupportedOperationException();
    }

  }


  //#########################################################################
  //# Local Class ParameterBindingElementFactory
  static abstract class ParameterBindingElementFactory
    extends ModuleElementFactory
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.model.base.ElementFactory
    public ElementType createElement(final Proxy proxy)
      throws JAXBException
    {
      return getFactory().createParameterBinding();
    }

  }

}
