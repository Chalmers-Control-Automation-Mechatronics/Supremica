//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.context
//# CLASS:   UnsupportedOperatorException
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler.context;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.expr.BuiltInFunction;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.Operator;


public class UnsupportedOperatorException extends EvalException {

  //#########################################################################
  //# Constructors
  UnsupportedOperatorException(final Operator op)
  {
    this(op, (Proxy) null);
  }

  UnsupportedOperatorException(final Operator op,
                               final Proxy location)
  {
    super("Operator " + op.getName() + " not supported!", location);
  }

  UnsupportedOperatorException(final Operator op,
                               final String explanation)
  {
    this(op, explanation, null);
  }

  UnsupportedOperatorException(final Operator op,
                               final String explanation,
                               final Proxy location)
  {
    super("Operator " + op.getName() + " is not supported " +
          explanation + "!", location);
  }
  UnsupportedOperatorException(final BuiltInFunction function)
  {
    this(function, (Proxy) null);
  }

  UnsupportedOperatorException(final BuiltInFunction function,
                               final Proxy location)
  {
    super("Function " + function.getName() + " not supported!", location);
  }

  UnsupportedOperatorException(final BuiltInFunction function,
                               final String explanation)
  {
    this(function, explanation, null);
  }

  UnsupportedOperatorException(final BuiltInFunction function,
                               final String explanation,
                               final Proxy location)
  {
    super("Function " + function.getName() + " is not supported " +
          explanation + "!", location);
  }


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
