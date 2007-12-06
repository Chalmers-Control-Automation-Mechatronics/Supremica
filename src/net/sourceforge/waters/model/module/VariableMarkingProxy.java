//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.module
//# CLASS:   VariableMarkingProxy
//###########################################################################
//# $Id: VariableMarkingProxy.java,v 1.3 2007-12-06 08:41:20 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.module;

import net.sourceforge.waters.model.base.Proxy;


/**
 * A proposition-predicate pair used to represent the marking of a
 * {@link VariableComponentProxy}.
 *
 * @author Robi Malik
 */

public interface VariableMarkingProxy extends Proxy {

  //#########################################################################
  //# Simple Access
  /**
   * Gets the proposition event of this marking.
   * @return An identifier referring to a proposition event of the module.
   */
  public IdentifierProxy getProposition();

  /**
   * Gets the marking predicate of this marking.
   * @return An expression that evaluates to true (i.e., nonzero) for
   *         marked states only.
   */
  public SimpleExpressionProxy getPredicate();

}
