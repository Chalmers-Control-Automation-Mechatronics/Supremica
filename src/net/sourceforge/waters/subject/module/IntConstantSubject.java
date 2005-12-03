//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.module
//# CLASS:   IntConstantSubject
//###########################################################################
//# $Id: IntConstantSubject.java,v 1.3 2005-12-03 21:30:42 robi Exp $
//###########################################################################

package net.sourceforge.waters.subject.module;

import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.module.IntConstantProxy;
import net.sourceforge.waters.model.module.ModuleProxyVisitor;
import net.sourceforge.waters.subject.base.ModelChangeEvent;


/**
 * The subject implementation of the {@link IntConstantProxy} interface.
 *
 * @author Robi Malik
 */

public final class IntConstantSubject
  extends SimpleExpressionSubject
  implements IntConstantProxy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new integer constant.
   * @param value The integer value of the new integer constant.
   */
  public IntConstantSubject(final int value)
  {
    mValue = value;
  }


  //#########################################################################
  //# Cloning
  public IntConstantSubject clone()
  {
    final IntConstantSubject cloned = (IntConstantSubject) super.clone();
    return cloned;
  }


  //#########################################################################
  //# Equality
  public boolean equals(final Object partner)
  {
    if (super.equals(partner)) {
      final IntConstantSubject downcast = (IntConstantSubject) partner;
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
  //# Setters
  /**
   * Sets the integer value of this constant.
   */
  public void setValue(final int value)
  {
    if (mValue == value) {
      return;
    }
    mValue = value;
    final ModelChangeEvent event =
      ModelChangeEvent.createStateChanged(this);
    fireModelChanged(event);
  }


  //#########################################################################
  //# Data Members
  private int mValue;

}
