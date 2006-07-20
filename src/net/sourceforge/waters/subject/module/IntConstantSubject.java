//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.module
//# CLASS:   IntConstantSubject
//###########################################################################
//# $Id: IntConstantSubject.java,v 1.6 2006-07-20 02:28:37 robi Exp $
//###########################################################################

package net.sourceforge.waters.subject.module;

import net.sourceforge.waters.model.base.Proxy;
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
  //# Equality and Hashcode
  public boolean equalsByContents(final Proxy partner)
  {
    if (super.equalsByContents(partner)) {
      final IntConstantSubject downcast = (IntConstantSubject) partner;
      return
        (mValue == downcast.mValue);
    } else {
      return false;
    }
  }

  public int hashCodeByContents()
  {
    int result = super.hashCodeByContents();
    result *= 5;
    result += mValue;
    return result;
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
