//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.plain.module
//# CLASS:   IntConstantElement
//###########################################################################
//# $Id: IntConstantElement.java,v 1.6 2006-09-06 11:52:21 robi Exp $
//###########################################################################

package net.sourceforge.waters.plain.module;

import net.sourceforge.waters.model.base.Proxy;
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
   * @param plainText The original text of the new integer constant, or <CODE>null</CODE>.
   * @param value The integer value of the new integer constant.
   */
  public IntConstantElement(final String plainText,
                            final int value)
  {
    super(plainText);
    mValue = value;
  }

  /**
   * Creates a new integer constant using default values.
   * This constructor creates an integer constant with
   * the original text set to <CODE>null</CODE>.
   * @param value The integer value of the new integer constant.
   */
  public IntConstantElement(final int value)
  {
    this(null,
         value);
  }


  //#########################################################################
  //# Cloning
  public IntConstantElement clone()
  {
    return (IntConstantElement) super.clone();
  }


  //#########################################################################
  //# Equality and Hashcode
  public boolean equalsByContents(final Proxy partner)
  {
    if (super.equalsByContents(partner)) {
      final IntConstantElement downcast = (IntConstantElement) partner;
      return
        (mValue == downcast.mValue);
    } else {
      return false;
    }
  }

  public boolean equalsWithGeometry(final Proxy partner)
  {
    if (super.equalsWithGeometry(partner)) {
      final IntConstantElement downcast = (IntConstantElement) partner;
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

  public int hashCodeWithGeometry()
  {
    int result = super.hashCodeWithGeometry();
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
  //# Data Members
  private final int mValue;

}
