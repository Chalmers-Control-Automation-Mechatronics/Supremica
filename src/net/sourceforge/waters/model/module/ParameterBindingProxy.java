//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.module
//# CLASS:   ParameterBindingProxy
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.module;

import net.sourceforge.waters.model.base.NamedProxy;


/**
 * A name-value pair.
 *
 * Parameter bindings are used in the binding list of instance components
 * ({@link InstanceProxy}) to describe which values are to be passed to a
 * module when it is instantiated. Each parameter binding consists of a
 * name, identifying which parameter of the instantiated module is to be
 * bound, and an expression, which evaluates to the value to be bound to
 * that parameter.
 *
 * @author Robi Malik
 */

public interface ParameterBindingProxy extends NamedProxy {

  //#########################################################################
  //# Getters and Setters
  public ExpressionProxy getExpression();

}
