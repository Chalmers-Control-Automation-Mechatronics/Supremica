//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.module
//# CLASS:   BooleanConstantSubject
//###########################################################################
//# $Id: BooleanConstantSubject.java,v 1.2 2006-03-02 12:12:49 martin Exp $
//###########################################################################

package net.sourceforge.waters.subject.module;

import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.module.BooleanConstantProxy;
import net.sourceforge.waters.model.module.ModuleProxyVisitor;
import net.sourceforge.waters.subject.base.ModelChangeEvent;


/**
 * The subject implementation of the {@link BooleanConstantProxy} interface.
 *
 * @author Robi Malik
 */

public final class BooleanConstantSubject
  extends SimpleExpressionSubject
  implements BooleanConstantProxy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new boolean constant.
   * @param value The boolean value of the new boolean constant.
   */
  public BooleanConstantSubject(final boolean value)
  {
    mIsValue = value;
  }


  //#########################################################################
  //# Cloning
  public BooleanConstantSubject clone()
  {
    final BooleanConstantSubject cloned = (BooleanConstantSubject) super.clone();
    return cloned;
  }


  //#########################################################################
  //# Equality
  public boolean equals(final Object partner)
  {
    if (super.equals(partner)) {
      final BooleanConstantSubject downcast = (BooleanConstantSubject) partner;
      return
        (mIsValue == downcast.mIsValue);
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
    return downcast.visitBooleanConstantProxy(this);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.module.BooleanConstantProxy
  public boolean isValue()
  {
    return mIsValue;
  }


  //#########################################################################
  //# Setters
  /**
   * Sets the boolean value of this constant.
   */
  public void setValue(final boolean value)
  {
    final boolean change = (mIsValue != value);
    mIsValue = value;
    if (change) {
      final ModelChangeEvent event =
        ModelChangeEvent.createStateChanged(this);
      fireModelChanged(event);
    }
  }


  //#########################################################################
  //# Data Members
  private boolean mIsValue;

}
