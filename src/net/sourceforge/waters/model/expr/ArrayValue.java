//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.expr
//# CLASS:   ArrayValue
//###########################################################################
//# $Id: ArrayValue.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.expr;


public interface ArrayValue extends Value {

  public Value find(Value index, SimpleExpressionProxy indexexpr)
    throws EvalException;

}