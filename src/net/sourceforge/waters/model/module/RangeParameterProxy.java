//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.module
//# CLASS:   RangeParameterProxy
//###########################################################################
//# $Id: RangeParameterProxy.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.module;

import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.base.ModelException;
import net.sourceforge.waters.model.base.ProxyFactory;
import net.sourceforge.waters.model.expr.SimpleExpressionProxy;
import net.sourceforge.waters.model.expr.SimpleExpressionProxyFactory;
import net.sourceforge.waters.xsd.base.ElementType;
import net.sourceforge.waters.xsd.module.RangeParameterType;

/**
 * <P>A range parameter for a Waters module.</P>
 *
 * <P>A range parameter is a parameter that is bound to a range when
 * a module is compiled. This can be used to parameterise the number of
 * components in a module.</P>
 *
 * @author Robi Malik
 */

public class RangeParameterProxy extends SimpleParameterProxy {

  //#########################################################################
  //# Constructors
  /**
   * Creates a range parameter.
   * @param  name        The name of the new parameter.
   * @param  expr        The default value of the new parameter.
   * @param  required    A flag, <CODE>true</CODE> if the new parameter should
   *                     be required, <CODE>false</CODE> otherwise.
   */
  public RangeParameterProxy(final String name,
			     final SimpleExpressionProxy expr,
			     final boolean required)
  {
    super(name, required);
    mDefault = expr;
  }

  /**
   * Creates a range parameter from a parsed XML structure.
   * @param  param       The parsed XML structure of the parameter.
   * @throws ModelException to indicate that the XML structure could
   *                     not be converted due to serious semantic
   *                     inconsistencies.
   */
  RangeParameterProxy(final RangeParameterType param)
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
    return "range";
  }


  //#########################################################################
  //# Equals and Hashcode
  public boolean equals(final Object partner)
  {
    if (super.equals(partner)) {
      final RangeParameterProxy param = (RangeParameterProxy) partner;
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
    final RangeParameterType param = (RangeParameterType) element;
    param.setDefault(getDefault().toSimpleExpressionType());
  }


  //#########################################################################
  //# Data Members
  private SimpleExpressionProxy mDefault;

}
