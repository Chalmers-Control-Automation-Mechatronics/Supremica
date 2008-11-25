//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.constraint
//# CLASS:   DoubleNegationRule
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler.constraint;

import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.UnaryOperator;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.model.module.UnaryExpressionProxy;


/**
 * <P>A simplification rule to remove double negation.</P>
 *
 * <PRE>
 *   !!EXPR
 *   ------
 *    EXPR
 * </PRE>
 *
 * @author Robi Malik
 */

class DoubleNegationRule extends DirectReplacementRule
{

  //#########################################################################
  //# Construction
  static DoubleNegationRule createRule
    (final ModuleProxyFactory factory,
     final CompilerOperatorTable optable)
  {
    final UnaryOperator op = optable.getNotOperator();
    final PlaceHolder EXPR = new PlaceHolder(factory, "EXPR");
    final SimpleIdentifierProxy expr = EXPR.getIdentifier();
    final UnaryExpressionProxy notexpr =
      factory.createUnaryExpressionProxy(op, expr);
    final UnaryExpressionProxy template =
      factory.createUnaryExpressionProxy(op, notexpr);
    return new DoubleNegationRule(template, expr, EXPR);
  }


  //#########################################################################
  //# Constructors
  private DoubleNegationRule(final SimpleExpressionProxy template,
                             final SimpleExpressionProxy replacement,
                             final PlaceHolder placeholder)
  {
    super(template, replacement, placeholder);
  }

}