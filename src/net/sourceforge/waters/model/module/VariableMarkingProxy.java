//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.module
//# CLASS:   VariableMarkingProxy
//###########################################################################
//# $Id: VariableMarkingProxy.java,v 1.1 2007-07-21 08:46:39 robi Exp $
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
  public IdentifierProxy getProposition();

  public SimpleExpressionProxy getPredicate();

}
