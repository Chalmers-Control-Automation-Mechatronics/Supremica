//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.module
//# CLASS:   BooleanConstantSubject
//###########################################################################
//# $Id: BooleanConstantSubject.java,v 1.6 2006-09-06 11:52:21 robi Exp $
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
   * @param plainText The original text of the new boolean constant, or <CODE>null</CODE>.
   * @param value The boolean value of the new boolean constant.
   */
  public BooleanConstantSubject(final String plainText,
                                final boolean value)
  {
    super(plainText);
    mIsValue = value;
  }

  /**
   * Creates a new boolean constant using default values.
   * This constructor creates a boolean constant with
   * the original text set to <CODE>null</CODE>.
   * @param value The boolean value of the new boolean constant.
   */
  public BooleanConstantSubject(final boolean value)
  {
    this(null,
         value);
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

  public boolean equalsWithGeometry(final Proxy partner)
  {
    if (super.equalsWithGeometry(partner)) {
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

  public int hashCodeWithGeometry()
  {
    int result = super.hashCodeWithGeometry();
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
