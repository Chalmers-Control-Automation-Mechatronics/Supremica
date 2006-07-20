//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.plain.module
//# CLASS:   SimpleParameterElement
//###########################################################################
//# $Id: SimpleParameterElement.java,v 1.5 2006-07-20 02:28:37 robi Exp $
//###########################################################################

package net.sourceforge.waters.plain.module;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleParameterProxy;


/**
 * An immutable implementation of the {@link SimpleParameterProxy} interface.
 *
 * @author Robi Malik
 */

public abstract class SimpleParameterElement
  extends ParameterElement
  implements SimpleParameterProxy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new simple parameter.
   * @param name The name of the new simple parameter.
   * @param required The required status of the new simple parameter.
   * @param defaultValue The default value of the new simple parameter.
   */
  protected SimpleParameterElement(final String name,
                                   final boolean required,
                                   final SimpleExpressionProxy defaultValue)
  {
    super(name, required);
    mDefaultValue = defaultValue;
  }

  /**
   * Creates a new simple parameter using default values.
   * This constructor creates a simple parameter with
   * the required status set to <CODE>true</CODE>.
   * @param name The name of the new simple parameter.
   * @param defaultValue The default value of the new simple parameter.
   */
  protected SimpleParameterElement(final String name,
                                   final SimpleExpressionProxy defaultValue)
  {
    this(name,
         true,
         defaultValue);
  }


  //#########################################################################
  //# Cloning
  public SimpleParameterElement clone()
  {
    return (SimpleParameterElement) super.clone();
  }


  //#########################################################################
  //# Equality and Hashcode
  public boolean equalsByContents(final Proxy partner)
  {
    if (super.equalsByContents(partner)) {
      final SimpleParameterElement downcast = (SimpleParameterElement) partner;
      return
        mDefaultValue.equalsByContents(downcast.mDefaultValue);
    } else {
      return false;
    }
  }

  public int hashCodeByContents()
  {
    int result = super.hashCodeByContents();
    result *= 5;
    result += mDefaultValue.hashCodeByContents();
    return result;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.module.SimpleParameterProxy
  public SimpleExpressionProxy getDefaultValue()
  {
    return mDefaultValue;
  }


  //#########################################################################
  //# Data Members
  private final SimpleExpressionProxy mDefaultValue;

}
