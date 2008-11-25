//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.constraint
//# CLASS:   EqualsToTrueRule
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler.constraint;

import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.module.IntConstantProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;


/**
 * <P>A simplification rule simplify Boolean equations involving TRUE</P>
 *
 * <PRE>
 *   1 == EXPR
 *   ---------
 *     EXPR
 * </PRE>
 *
 * <P><CODE>EXPR</CODE> must be an expression of Boolean range.</P>
 *
 * @author Robi Malik
 */

class EqualsToTrueRule extends DirectReplacementRule
{

  //#########################################################################
  //# Construction
  static EqualsToTrueRule createRule
    (final ModuleProxyFactory factory,
     final CompilerOperatorTable optable)
  {
    final BinaryOperator op = optable.getEqualsOperator();
    final IntConstantProxy trueconst = factory.createIntConstantProxy(1);
    final PlaceHolder EXPR = new BooleanExpressionPlaceHolder(factory, "EXPR");
    final SimpleIdentifierProxy expr = EXPR.getIdentifier();
    final BinaryExpressionProxy template =
      factory.createBinaryExpressionProxy(op, trueconst, expr);
    return new EqualsToTrueRule(template, EXPR, expr);
  }


  //#########################################################################
  //# Constructor
  private EqualsToTrueRule(final SimpleExpressionProxy template,
                           final PlaceHolder placeholder,
                           final SimpleExpressionProxy expr)
  {
    super(template, expr, placeholder);
  }

}