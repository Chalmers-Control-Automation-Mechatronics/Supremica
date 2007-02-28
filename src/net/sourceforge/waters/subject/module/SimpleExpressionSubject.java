//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.module
//# CLASS:   SimpleExpressionSubject
//###########################################################################
//# $Id: SimpleExpressionSubject.java,v 1.7 2007-02-28 00:03:24 robi Exp $
//###########################################################################

package net.sourceforge.waters.subject.module;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.subject.base.ModelChangeEvent;


/**
 * The subject implementation of the {@link SimpleExpressionProxy} interface.
 *
 * @author Robi Malik
 */

public abstract class SimpleExpressionSubject
  extends ExpressionSubject
  implements SimpleExpressionProxy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new simple expression.
   * @param plainText The original text of the new simple expression, or <CODE>null</CODE>.
   */
  protected SimpleExpressionSubject(final String plainText)
  {
    mPlainText = plainText;
  }

  /**
   * Creates a new simple expression using default values.
   * This constructor creates a simple expression with
   * the original text set to <CODE>null</CODE>.
   */
  protected SimpleExpressionSubject()
  {
    this(null);
  }


  //#########################################################################
  //# Cloning
  public SimpleExpressionSubject clone()
  {
    final SimpleExpressionSubject cloned = (SimpleExpressionSubject) super.clone();
    return cloned;
  }


  //#########################################################################
  //# Equality and Hashcode
  public boolean equalsWithGeometry(final Proxy partner)
  {
    if (super.equalsByContents(partner)) {
      final SimpleExpressionSubject downcast = (SimpleExpressionSubject) partner;
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
  //# Setters
  /**
   * Sets the original text of this expression.
   */
  public void setPlainText(final String plainText)
  {
    if (mPlainText.equals(plainText)) {
      return;
    }
    mPlainText = plainText;
    final ModelChangeEvent event =
      ModelChangeEvent.createGeometryChanged(this, mPlainText);
    fireModelChanged(event);
  }


  //#########################################################################
  //# Data Members
  private String mPlainText;

}
