//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.module
//# CLASS:   VariableSubject
//###########################################################################
//# $Id: VariableSubject.java,v 1.9 2007-02-12 21:38:49 robi Exp $
//###########################################################################

package net.sourceforge.waters.subject.module;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.module.ModuleProxyVisitor;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.VariableProxy;
import net.sourceforge.waters.subject.base.ModelChangeEvent;
import net.sourceforge.waters.subject.base.MutableSubject;


/**
 * The subject implementation of the {@link VariableProxy} interface.
 *
 * @author Robi Malik
 */

public final class VariableSubject
  extends MutableSubject
  implements VariableProxy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new variable.
   * @param name The name of the new variable.
   * @param type The type of the new variable.
   * @param initialValue The initial value of the new variable.
   * @param markedValue The marked value of the new variable, or <CODE>null</CODE>.
   */
  public VariableSubject(final String name,
                         final SimpleExpressionProxy type,
                         final SimpleExpressionProxy initialValue,
                         final SimpleExpressionProxy markedValue)
  {
    mName = name;
    mType = (SimpleExpressionSubject) type;
    mType.setParent(this);
    mInitialValue = (SimpleExpressionSubject) initialValue;
    mInitialValue.setParent(this);
    mMarkedValue = (SimpleExpressionSubject) markedValue;
    if (mMarkedValue != null) {
      mMarkedValue.setParent(this);
    }
  }

  /**
   * Creates a new variable using default values.
   * This constructor creates a variable with
   * the marked value set to <CODE>null</CODE>.
   * @param name The name of the new variable.
   * @param type The type of the new variable.
   * @param initialValue The initial value of the new variable.
   */
  public VariableSubject(final String name,
                         final SimpleExpressionProxy type,
                         final SimpleExpressionProxy initialValue)
  {
    this(name,
         type,
         initialValue,
         null);
  }


  //#########################################################################
  //# Cloning
  public VariableSubject clone()
  {
    final VariableSubject cloned = (VariableSubject) super.clone();
    cloned.mType = mType.clone();
    cloned.mType.setParent(cloned);
    cloned.mInitialValue = mInitialValue.clone();
    cloned.mInitialValue.setParent(cloned);
    if (mMarkedValue != null) {
      cloned.mMarkedValue = mMarkedValue.clone();
      cloned.mMarkedValue.setParent(cloned);
    }
    return cloned;
  }


  //#########################################################################
  //# Equality and Hashcode
  public boolean equalsByContents(final Proxy partner)
  {
    if (super.equalsByContents(partner)) {
      final VariableSubject downcast = (VariableSubject) partner;
      return
        mName.equals(downcast.mName) &&
        mType.equalsByContents(downcast.mType) &&
        mInitialValue.equalsByContents(downcast.mInitialValue) &&
        (mMarkedValue == null ? downcast.mMarkedValue == null :
         mMarkedValue.equalsByContents(downcast.mMarkedValue));
    } else {
      return false;
    }
  }

  public boolean equalsWithGeometry(final Proxy partner)
  {
    if (super.equalsByContents(partner)) {
      final VariableSubject downcast = (VariableSubject) partner;
      return
        mName.equals(downcast.mName) &&
        mType.equalsWithGeometry(downcast.mType) &&
        mInitialValue.equalsWithGeometry(downcast.mInitialValue) &&
        (mMarkedValue == null ? downcast.mMarkedValue == null :
         mMarkedValue.equalsWithGeometry(downcast.mMarkedValue));
    } else {
      return false;
    }
  }

  public int hashCodeByContents()
  {
    int result = super.hashCodeByContents();
    result *= 5;
    result += mName.hashCode();
    result *= 5;
    result += mType.hashCodeByContents();
    result *= 5;
    result += mInitialValue.hashCodeByContents();
    result *= 5;
    if (mMarkedValue != null) {
      result += mMarkedValue.hashCodeByContents();
    }
    return result;
  }

  public int hashCodeWithGeometry()
  {
    int result = super.hashCodeByContents();
    result *= 5;
    result += mName.hashCode();
    result *= 5;
    result += mType.hashCodeWithGeometry();
    result *= 5;
    result += mInitialValue.hashCodeWithGeometry();
    result *= 5;
    if (mMarkedValue != null) {
      result += mMarkedValue.hashCodeWithGeometry();
    }
    return result;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.base.Proxy
  public Object acceptVisitor(final ProxyVisitor visitor)
    throws VisitorException
  {
    final ModuleProxyVisitor downcast = (ModuleProxyVisitor) visitor;
    return downcast.visitVariableProxy(this);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.module.VariableProxy
  public String getName()
  {
    return mName;
  }

  public SimpleExpressionSubject getType()
  {
    return mType;
  }

  public SimpleExpressionSubject getInitialValue()
  {
    return mInitialValue;
  }

  public SimpleExpressionSubject getMarkedValue()
  {
    return mMarkedValue;
  }


  //#########################################################################
  //# Setters
  /**
   * Sets the Name of this variable.
   */
  public void setName(final String name)
  {
    if (mName.equals(name)) {
      return;
    }
    mName = name;
    final ModelChangeEvent event =
      ModelChangeEvent.createStateChanged(this);
    fireModelChanged(event);
  }

  /**
   * Sets the type of this variable.
   */
  public void setType(final SimpleExpressionSubject type)
  {
    if (mType == type) {
      return;
    }
    type.setParent(this);
    mType.setParent(null);
    mType = type;
    final ModelChangeEvent event =
      ModelChangeEvent.createStateChanged(this);
    fireModelChanged(event);
  }

  /**
   * Sets the initial value of this variable.
   */
  public void setInitialValue(final SimpleExpressionSubject initialValue)
  {
    if (mInitialValue == initialValue) {
      return;
    }
    initialValue.setParent(this);
    mInitialValue.setParent(null);
    mInitialValue = initialValue;
    final ModelChangeEvent event =
      ModelChangeEvent.createStateChanged(this);
    fireModelChanged(event);
  }

  /**
   * Sets the marked value of this variable.
   */
  public void setMarkedValue(final SimpleExpressionSubject markedValue)
  {
    if (mMarkedValue == markedValue) {
      return;
    }
    if (markedValue != null) {
      markedValue.setParent(this);
    }
    if (mMarkedValue != null) {
      mMarkedValue.setParent(null);
    }
    mMarkedValue = markedValue;
    final ModelChangeEvent event =
      ModelChangeEvent.createStateChanged(this);
    fireModelChanged(event);
  }


  //#########################################################################
  //# Data Members
  private String mName;
  private SimpleExpressionSubject mType;
  private SimpleExpressionSubject mInitialValue;
  private SimpleExpressionSubject mMarkedValue;

}
