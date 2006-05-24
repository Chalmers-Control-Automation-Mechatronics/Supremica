//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.module
//# CLASS:   ParameterSubject
//###########################################################################
//# $Id: ParameterSubject.java,v 1.5 2006-05-24 09:13:02 markus Exp $
//###########################################################################

package net.sourceforge.waters.subject.module;

import net.sourceforge.waters.model.module.ParameterProxy;
import net.sourceforge.waters.subject.base.ModelChangeEvent;
import net.sourceforge.waters.subject.base.NamedSubject;


/**
 * The subject implementation of the {@link ParameterProxy} interface.
 *
 * @author Robi Malik
 */

public abstract class ParameterSubject
  extends NamedSubject
  implements ParameterProxy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new parameter.
   * @param name The name of the new parameter.
   * @param required The required status of the new parameter.
   */
  protected ParameterSubject(final String name,
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
  protected ParameterSubject(final String name)
  {
    this(name,
         true);
  }


  //#########################################################################
  //# Cloning
  public ParameterSubject clone()
  {
    final ParameterSubject cloned = (ParameterSubject) super.clone();
    return cloned;
  }


  //#########################################################################
  //# Equality
  public boolean equals(final Object partner)
  {
    if (super.equals(partner)) {
      final ParameterSubject downcast = (ParameterSubject) partner;
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
  //# Setters
  /**
   * Sets the required status of this parameter.
   */
  public void setRequired(final boolean required)
  {
    if (mIsRequired == required) {
      return;
    }
    mIsRequired = required;
    final ModelChangeEvent event =
      ModelChangeEvent.createStateChanged(this);
    fireModelChanged(event);
  }


  //#########################################################################
  //# Data Members
  private boolean mIsRequired;

}
