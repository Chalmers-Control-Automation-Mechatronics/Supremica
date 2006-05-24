//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.module
//# CLASS:   ExpressionSubject
//###########################################################################
//# $Id: ExpressionSubject.java,v 1.5 2006-05-24 09:13:02 markus Exp $
//###########################################################################

package net.sourceforge.waters.subject.module;

import net.sourceforge.waters.model.module.ExpressionProxy;
import net.sourceforge.waters.subject.base.MutableSubject;


/**
 * The subject implementation of the {@link ExpressionProxy} interface.
 *
 * @author Robi Malik
 */

public abstract class ExpressionSubject
  extends MutableSubject
  implements ExpressionProxy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new expression.
   */
  protected ExpressionSubject()
  {
  }


  //#########################################################################
  //# Cloning
  public ExpressionSubject clone()
  {
    return (ExpressionSubject) super.clone();
  }

}
