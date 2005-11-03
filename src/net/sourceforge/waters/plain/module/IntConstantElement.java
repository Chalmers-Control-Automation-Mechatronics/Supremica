//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.plain.module
//# CLASS:   IntConstantElement
//###########################################################################
//# $Id: IntConstantElement.java,v 1.2 2005-11-03 01:24:16 robi Exp $
//###########################################################################

package net.sourceforge.waters.plain.module;

import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.module.IntConstantProxy;
import net.sourceforge.waters.model.module.ModuleProxyVisitor;


/**
 * An immutable implementation of the {@link IntConstantProxy} interface.
 *
 * @author Robi Malik
 */

public final class IntConstantElement
  extends SimpleExpressionElement
  implements IntConstantProxy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new integer constant.
   * @param value The integer value of the new integer constant.
   */
  public IntConstantElement(final int value)
  {
    mValue = value;
  }


  //#########################################################################
  //# Cloning
  public IntConstantElement clone()
  {
    return (IntConstantElement) super.clone();
  }


  //#########################################################################
  //# Equality
  public boolean equals(final Object partner)
  {
    if (super.equals(partner)) {
      final IntConstantElement downcast = (IntConstantElement) partner;
      return
        (mValue == downcast.mValue);
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
    return downcast.visitIntConstantProxy(this);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.module.IntConstantProxy
  public int getValue()
  {
    return mValue;
  }


  //#########################################################################
  //# Data Members
  private final int mValue;

}
