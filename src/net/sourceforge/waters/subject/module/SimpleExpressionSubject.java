//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.module
//# CLASS:   SimpleExpressionSubject
//###########################################################################
//# $Id: SimpleExpressionSubject.java,v 1.2 2005-11-03 01:24:16 robi Exp $
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
