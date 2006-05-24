//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.plain.module
//# CLASS:   ExpressionElement
//###########################################################################
//# $Id: ExpressionElement.java,v 1.4 2006-05-24 09:13:02 markus Exp $
//###########################################################################

package net.sourceforge.waters.plain.module;

import net.sourceforge.waters.model.module.ExpressionProxy;
import net.sourceforge.waters.plain.base.Element;


/**
 * An immutable implementation of the {@link ExpressionProxy} interface.
 *
 * @author Robi Malik
 */

public abstract class ExpressionElement
  extends Element
  implements ExpressionProxy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new expression.
   */
  protected ExpressionElement()
  {
  }


  //#########################################################################
  //# Cloning
  public ExpressionElement clone()
  {
    return (ExpressionElement) super.clone();
  }

}
