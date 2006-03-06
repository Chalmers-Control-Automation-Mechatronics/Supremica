//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.plain.module
//# CLASS:   BooleanConstantElement
//###########################################################################
//# $Id: BooleanConstantElement.java,v 1.3 2006-03-06 17:08:45 markus Exp $
//###########################################################################

package net.sourceforge.waters.plain.module;

import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.module.BooleanConstantProxy;
import net.sourceforge.waters.model.module.ModuleProxyVisitor;


/**
 * An immutable implementation of the {@link BooleanConstantProxy} interface.
 *
 * @author Robi Malik
 */

public final class BooleanConstantElement
  extends SimpleExpressionElement
  implements BooleanConstantProxy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new boolean constant.
   * @param value The boolean value of the new boolean constant.
   */
  public BooleanConstantElement(final boolean value)
  {
    mIsValue = value;
  }


  //#########################################################################
  //# Cloning
  public BooleanConstantElement clone()
  {
    return (BooleanConstantElement) super.clone();
  }


  //#########################################################################
  //# Equality
  public boolean equals(final Object partner)
  {
    if (super.equals(partner)) {
      final BooleanConstantElement downcast = (BooleanConstantElement) partner;
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
  //# Data Members
  private final boolean mIsValue;

}
