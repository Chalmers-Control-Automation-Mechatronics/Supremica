//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.plain.module
//# CLASS:   SimpleExpressionElement
//###########################################################################
//# $Id: SimpleExpressionElement.java,v 1.2 2005-11-03 01:24:16 robi Exp $
//###########################################################################

package net.sourceforge.waters.plain.module;

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
   */
  protected SimpleExpressionElement()
  {
  }


  //#########################################################################
  //# Cloning
  public SimpleExpressionElement clone()
  {
    return (SimpleExpressionElement) super.clone();
  }

}
