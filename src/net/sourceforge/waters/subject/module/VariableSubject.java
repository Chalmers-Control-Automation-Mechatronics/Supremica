//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.module
//# CLASS:   VariableSubject
//###########################################################################
//# $Id: VariableSubject.java,v 1.2 2006-03-02 12:12:49 martin Exp $
//###########################################################################

package net.sourceforge.waters.subject.module;

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
   */
  public VariableSubject(final String name,
                         final SimpleExpressionProxy type,
                         final SimpleExpressionProxy initialValue)
  {
    mName = name;
    mType = (SimpleExpressionSubject) type;
    mType.setParent(this);
    mInitialValue = (SimpleExpressionSubject) initialValue;
    mInitialValue.setParent(this);
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
    return cloned;
  }


  //#########################################################################
  //# Equality
  public boolean equals(final Object partner)
  {
    if (super.equals(partner)) {
      final VariableSubject downcast = (VariableSubject) partner;
      return
        mName.equals(downcast.mName) &&
        mType.equals(downcast.mType) &&
        mInitialValue.equals(downcast.mInitialValue);
    } else {
      return false;
    }
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


  //#########################################################################
  //# Setters
  /**
   * Sets the Name of this variable.
   */
  public void setName(final String name)
  {
    final boolean change = !mName.equals(name);
    mName = name;
    if (change) {
      final ModelChangeEvent event =
        ModelChangeEvent.createStateChanged(this);
      fireModelChanged(event);
    }
  }

  /**
   * Sets the type of this variable.
   */
  public void setType(final SimpleExpressionSubject type)
  {
    final boolean change = (mType != type);
    type.setParent(this);
    mType.setParent(null);
    mType = type;
    if (change) {
      final ModelChangeEvent event =
        ModelChangeEvent.createStateChanged(this);
      fireModelChanged(event);
    }
  }

  /**
   * Sets the initial value of this variable.
   */
  public void setInitialValue(final SimpleExpressionSubject initialValue)
  {
    final boolean change = (mInitialValue != initialValue);
    initialValue.setParent(this);
    mInitialValue.setParent(null);
    mInitialValue = initialValue;
    if (change) {
      final ModelChangeEvent event =
        ModelChangeEvent.createStateChanged(this);
      fireModelChanged(event);
    }
  }


  //#########################################################################
  //# Data Members
  private String mName;
  private SimpleExpressionSubject mType;
  private SimpleExpressionSubject mInitialValue;

}
