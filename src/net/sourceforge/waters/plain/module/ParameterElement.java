//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.plain.module
//# CLASS:   ParameterElement
//###########################################################################
//# $Id: ParameterElement.java,v 1.2 2005-11-03 01:24:16 robi Exp $
//###########################################################################

package net.sourceforge.waters.plain.module;

import net.sourceforge.waters.model.module.ParameterProxy;
import net.sourceforge.waters.plain.base.NamedElement;


/**
 * An immutable implementation of the {@link ParameterProxy} interface.
 *
 * @author Robi Malik
 */

public abstract class ParameterElement
  extends NamedElement
  implements ParameterProxy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new parameter.
   * @param name The name of the new parameter.
   * @param required The required status of the new parameter.
   */
  protected ParameterElement(final String name,
                             final boolean required)
  {
    super(name);
    mIsRequired = required;
  }

  /**
   * Creates a new parameter using default values.
   * This constructor creates a parameter with
   * the required status set to <CODE>true</CODE>.
   * @param name The name of the new parameter.
   */
  protected ParameterElement(final String name)
  {
    this(name,
         true);
  }


  //#########################################################################
  //# Cloning
  public ParameterElement clone()
  {
    return (ParameterElement) super.clone();
  }


  //#########################################################################
  //# Equality
  public boolean equals(final Object partner)
  {
    if (super.equals(partner)) {
      final ParameterElement downcast = (ParameterElement) partner;
      return
        (mIsRequired == downcast.mIsRequired);
    } else {
      return false;
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.module.ParameterProxy
  public boolean isRequired()
  {
    return mIsRequired;
  }


  //#########################################################################
  //# Data Members
  private final boolean mIsRequired;

}
