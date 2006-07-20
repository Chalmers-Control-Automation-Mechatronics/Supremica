//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.module
//# CLASS:   BooleanConstantSubject
//###########################################################################
//# $Id: BooleanConstantSubject.java,v 1.5 2006-07-20 02:28:37 robi Exp $
//###########################################################################

package net.sourceforge.waters.subject.module;

import net.sourceforge.waters.model.base.Proxy;
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
  //# Equality and Hashcode
  public boolean equalsByContents(final Proxy partner)
  {
    if (super.equalsByContents(partner)) {
      final BooleanConstantSubject downcast = (BooleanConstantSubject) partner;
      return
        (mIsValue == downcast.mIsValue);
    } else {
      return false;
    }
  }

  public int hashCodeByContents()
  {
    int result = super.hashCodeByContents();
    result *= 5;
    if (mIsValue) {
      result++;
    }
    return result;
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
    if (mIsValue == value) {
      return;
    }
    mIsValue = value;
    final ModelChangeEvent event =
      ModelChangeEvent.createStateChanged(this);
    fireModelChanged(event);
  }


  //#########################################################################
  //# Data Members
  private boolean mIsValue;

}
