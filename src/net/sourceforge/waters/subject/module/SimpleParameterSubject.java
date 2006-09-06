//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.module
//# CLASS:   SimpleParameterSubject
//###########################################################################
//# $Id: SimpleParameterSubject.java,v 1.7 2006-09-06 11:52:21 robi Exp $
//###########################################################################

package net.sourceforge.waters.subject.module;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleParameterProxy;
import net.sourceforge.waters.subject.base.ModelChangeEvent;


/**
 * The subject implementation of the {@link SimpleParameterProxy} interface.
 *
 * @author Robi Malik
 */

public abstract class SimpleParameterSubject
  extends ParameterSubject
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
  protected SimpleParameterSubject(final String name,
                                   final boolean required,
                                   final SimpleExpressionProxy defaultValue)
  {
    super(name, required);
    mDefaultValue = (SimpleExpressionSubject) defaultValue;
    mDefaultValue.setParent(this);
  }

  /**
   * Creates a new simple parameter using default values.
   * This constructor creates a simple parameter with
   * the required status set to <CODE>true</CODE>.
   * @param name The name of the new simple parameter.
   * @param defaultValue The default value of the new simple parameter.
   */
  protected SimpleParameterSubject(final String name,
                                   final SimpleExpressionProxy defaultValue)
  {
    this(name,
         true,
         defaultValue);
  }


  //#########################################################################
  //# Cloning
  public SimpleParameterSubject clone()
  {
    final SimpleParameterSubject cloned = (SimpleParameterSubject) super.clone();
    cloned.mDefaultValue = mDefaultValue.clone();
    cloned.mDefaultValue.setParent(cloned);
    return cloned;
  }


  //#########################################################################
  //# Equality and Hashcode
  public boolean equalsByContents(final Proxy partner)
  {
    if (super.equalsByContents(partner)) {
      final SimpleParameterSubject downcast = (SimpleParameterSubject) partner;
      return
        mDefaultValue.equalsByContents(downcast.mDefaultValue);
    } else {
      return false;
    }
  }

  public boolean equalsWithGeometry(final Proxy partner)
  {
    if (super.equalsByContents(partner)) {
      final SimpleParameterSubject downcast = (SimpleParameterSubject) partner;
      return
        mDefaultValue.equalsWithGeometry(downcast.mDefaultValue);
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

  public int hashCodeWithGeometry()
  {
    int result = super.hashCodeByContents();
    result *= 5;
    result += mDefaultValue.hashCodeWithGeometry();
    return result;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.module.SimpleParameterProxy
  public SimpleExpressionSubject getDefaultValue()
  {
    return mDefaultValue;
  }


  //#########################################################################
  //# Setters
  public void setDefaultValue(final SimpleExpressionSubject defaultValue)
  {
    if (mDefaultValue == defaultValue) {
      return;
    }
    defaultValue.setParent(this);
    mDefaultValue.setParent(null);
    mDefaultValue = defaultValue;
    final ModelChangeEvent event =
      ModelChangeEvent.createStateChanged(this);
    fireModelChanged(event);
  }


  //#########################################################################
  //# Data Members
  private SimpleExpressionSubject mDefaultValue;

}
