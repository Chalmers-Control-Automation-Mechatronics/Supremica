//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.plain.module
//# CLASS:   SimpleExpressionElement
//###########################################################################
//# $Id: SimpleExpressionElement.java,v 1.6 2007-02-28 00:03:24 robi Exp $
//###########################################################################

package net.sourceforge.waters.plain.module;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;


/**
 * An immutable implementation of the {@link SimpleExpressionProxy} interface.
 *
 * @author Robi Malik
 */

public abstract class SimpleExpressionElement
  extends ExpressionElement
  implements SimpleExpressionProxy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new simple expression.
   * @param plainText The original text of the new simple expression, or <CODE>null</CODE>.
   */
  protected SimpleExpressionElement(final String plainText)
  {
    mPlainText = plainText;
  }

  /**
   * Creates a new simple expression using default values.
   * This constructor creates a simple expression with
   * the original text set to <CODE>null</CODE>.
   */
  protected SimpleExpressionElement()
  {
    this(null);
  }


  //#########################################################################
  //# Cloning
  public SimpleExpressionElement clone()
  {
    return (SimpleExpressionElement) super.clone();
  }


  //#########################################################################
  //# Equality and Hashcode
  public boolean equalsWithGeometry(final Proxy partner)
  {
    if (super.equalsByContents(partner)) {
      final SimpleExpressionElement downcast = (SimpleExpressionElement) partner;
      return
        (mPlainText == null ? downcast.mPlainText == null :
         mPlainText.equals(downcast.mPlainText));
    } else {
      return false;
    }
  }

  public int hashCodeWithGeometry()
  {
    int result = super.hashCodeByContents();
    result *= 5;
    if (mPlainText != null) {
      result += mPlainText.hashCode();
    }
    return result;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.module.SimpleExpressionProxy
  public String getPlainText()
  {
    return mPlainText;
  }


  //#########################################################################
  //# Data Members
  private final String mPlainText;

}
