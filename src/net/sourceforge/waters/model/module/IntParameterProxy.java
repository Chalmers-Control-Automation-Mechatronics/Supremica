//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.module
//# CLASS:   IntParameterProxy
//###########################################################################
//# $Id: IntParameterProxy.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.module;

import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.base.ModelException;
import net.sourceforge.waters.model.base.ProxyFactory;
import net.sourceforge.waters.model.expr.SimpleExpressionProxy;
import net.sourceforge.waters.model.expr.SimpleExpressionProxyFactory;
import net.sourceforge.waters.xsd.base.ElementType;
import net.sourceforge.waters.xsd.module.IntParameterType;


/**
 * <P>An integer parameter for a Waters module.</P>
 *
 * <P>An integer parameter is a parameter that is bound to an integer when
 * a module is compiled. This can be used to parameterise the number of
 * components in a module as in the following example which models a simple
 * factory with a variable number&nbsp;<I>n</I> of machines.</P>
 *
 * <PRE>
 * MODULE small_factory_variable;
 * PARAMETERS
 *   int n = 2;
 * EVENTS
 *   controllable start[1..n];
 *   uncontrollable finish[1..n];
 * COMPONENTS
 *   FOR i IN 1..n
 *     plant mach[i];
 *   ENDFOR
 * </PRE>
 *
 * @author Robi Malik
 */

public class IntParameterProxy extends SimpleParameterProxy {

  //#########################################################################
  //# Constructors
  /**
   * Creates an integer parameter.
   * @param  name        The name of the new parameter.
   * @param  expr        The default value of the new parameter.
   * @param  required    A flag, <CODE>true</CODE> if the new parameter should
   *                     be required, <CODE>false</CODE> otherwise.
   */
  public IntParameterProxy(final String name,
			   final SimpleExpressionProxy expr,
			   final boolean required)
  {
    super(name, required);
    mDefault = expr;
  }

  /**
   * Creates an integer parameter from a parsed XML structure.
   * @param  param       The parsed XML structure of the parameter.
   * @throws ModelException to indicate that the XML structure could
   *                     not be converted due to serious semantic
   *                     inconsistencies.
   */
  IntParameterProxy(final IntParameterType param)
    throws ModelException
  {
    super(param);
    final ProxyFactory factory = new SimpleExpressionProxyFactory();
    mDefault = (SimpleExpressionProxy) factory.createProxy(param.getDefault());
  }


  //#########################################################################
  //# Getters and Setters
  public SimpleExpressionProxy getDefault()
  {
    return mDefault;
  }

  public void setDefault(final SimpleExpressionProxy expr)
  {
    mDefault = expr;
  }

  public String getTypeName()
  {
    return "int";
  }


  //#########################################################################
  //# Equals and Hashcode
  public boolean equals(final Object partner)
  {
    if (super.equals(partner)) {
      final IntParameterProxy param = (IntParameterProxy) partner;
      return getDefault().equals(param.getDefault());
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
    final IntParameterType param = (IntParameterType) element;
    param.setDefault(getDefault().toSimpleExpressionType());
  }


  //#########################################################################
  //# Data Members
  private SimpleExpressionProxy mDefault;

}
