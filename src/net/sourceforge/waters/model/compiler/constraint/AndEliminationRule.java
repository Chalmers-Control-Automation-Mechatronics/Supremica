//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.constraint
//# CLASS:   AndEliminationRule
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler.constraint;

import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;


/**
 * <P>A simplification rule used for normalisation of AND expressions.</P>
 *
 * <PRE>
 *   LHS &amp; RHS
 *   ---------
 *   LHS, RHS
 * </PRE>
 *
 * @author Robi Malik
 */

class AndEliminationRule extends DirectReplacementRule
{

  //#########################################################################
  //# Construction
  static AndEliminationRule createRule
    (final ModuleProxyFactory factory,
     final CompilerOperatorTable optable)
  {
    final BinaryOperator op = optable.getAndOperator();
    final PlaceHolder LHS = new PlaceHolder(factory, "LHS");
    final PlaceHolder RHS = new PlaceHolder(factory, "RHS");
    final SimpleIdentifierProxy lhs = LHS.getIdentifier();
    final SimpleIdentifierProxy rhs = RHS.getIdentifier();
    final SimpleExpressionProxy template =
      factory.createBinaryExpressionProxy(op, lhs, rhs);
    return new AndEliminationRule(template, LHS, RHS, lhs, rhs);
  }


  //#########################################################################
  //# Constructors
  private AndEliminationRule(final SimpleExpressionProxy template,
                             final PlaceHolder LHS,
                             final PlaceHolder RHS,
                             final SimpleExpressionProxy lhs,
                             final SimpleExpressionProxy rhs)
  {
    super(template,
          new SimpleExpressionProxy[] {lhs, rhs},
          new PlaceHolder[] {LHS, RHS});
  }

}