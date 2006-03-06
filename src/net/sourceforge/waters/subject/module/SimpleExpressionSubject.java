//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.module
//# CLASS:   SimpleExpressionSubject
//###########################################################################
//# $Id: SimpleExpressionSubject.java,v 1.4 2006-03-06 17:08:46 markus Exp $
//###########################################################################

package net.sourceforge.waters.subject.module;

import net.sourceforge.waters.model.module.SimpleExpressionProxy;


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
   */
  protected SimpleExpressionSubject()
  {
  }


  //#########################################################################
  //# Cloning
  public SimpleExpressionSubject clone()
  {
    return (SimpleExpressionSubject) super.clone();
  }

}
