//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.module
//# CLASS:   SimpleParameterProxy
//###########################################################################
//# $Id: SimpleParameterProxy.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.module;

import java.io.IOException;
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.base.ModelPrinter;
import net.sourceforge.waters.model.expr.SimpleExpressionProxy;
import net.sourceforge.waters.xsd.base.ElementType;
import net.sourceforge.waters.xsd.module.SimpleParameterType;


/**
 * <P>A scalar type parameter for a Waters module.</P>
 *
 * <P>A simple parameter is a parameter that is bound to the value of an
 * expression when a module is compiled. There can be different types of
 * simple parameters, represented by the subclasses of this general base
 * class.</P>
 *
 * @author Robi Malik
 */

public abstract class SimpleParameterProxy extends ParameterProxy {

  //#########################################################################
  //# Constructors
  /**
   * Creates a simple parameter.
   * @param  name        The name of the new parameter.
   * @param  required    A flag, <CODE>true</CODE> if the new parameter should
   *                     be required, <CODE>false</CODE> otherwise.
   */
  public SimpleParameterProxy(final String name,
			      final boolean required)
  {
    super(name);
    mIsRequired = required;
  }

  /**
   * Creates a simple parameter from a parsed XML structure.
   * @param  param       The parsed XML structure of the parameter.
   * @throws ModelException to indicate that the XML structure could
   *                     not be converted due to serious semantic
   *                     inconsistencies.
   */
  SimpleParameterProxy(final SimpleParameterType param)
  {
    super(param);
    mIsRequired = param.isRequired();
  }

  //#########################################################################
  //# Getters and Setters
  public boolean isRequired()
  {
    return mIsRequired;
  }

  public void setRequired(boolean required)
  {
    mIsRequired = required;
  }

  public abstract SimpleExpressionProxy getDefault();

  public abstract void setDefault(final SimpleExpressionProxy proxy);

  public abstract String getTypeName();


  //#########################################################################
  //# Equals and Hashcode
  public boolean equals(final Object partner)
  {
    if (super.equals(partner)) {
      final SimpleParameterProxy param = (SimpleParameterProxy) partner;
      return isRequired() == param.isRequired();
    } else {
      return false;
    }    
  }


  //#########################################################################
  //# Printing
  public void pprint(final ModelPrinter printer)
    throws IOException
  {
    super.pprint(printer);
    printer.print(getTypeName());
    printer.print(' ');
    printer.print(getName());
    printer.print(" = ");
    getDefault().pprint(printer);
  }


  //#########################################################################
  //# Marshalling
  public void toJAXBElement(final ElementType element)
    throws JAXBException
  {
    super.toJAXBElement(element);
    final SimpleParameterType param = (SimpleParameterType) element;
    param.setRequired(isRequired());
  }


  //#########################################################################
  //# Data Members
  private boolean mIsRequired;

}
