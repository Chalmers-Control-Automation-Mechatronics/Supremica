//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

package net.sourceforge.waters.model.compiler.constraint;

import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.UnaryOperator;
import net.sourceforge.waters.model.module.IntConstantProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;


/**
 * <P>A simplification to perform substitution for a positive or negative
 * literal.</P>
 *
 * <PRE>!VARNAME    VARNAME</PRE>
 *
 * <P>There are two versions of this rule. The <I>positive</I> rule
 * substitutes <CODE>VARNAME</CODE> by&nbsp;<CODE>1</CODE> in the presence
 * of a positive literal&nbsp;<CODE>VARNAME</CODE>. The <I>negative</I>
 * rule substitutes <CODE>VARNAME</CODE> by&nbsp;<CODE>0</CODE> in the
 * presence of a negative literal&nbsp;<CODE>!VARNAME</CODE>.</P>
 *
 * @author Robi Malik
 */

class BooleanLiteralRule extends SimplificationRule
{

  //#########################################################################
  //# Construction
  static BooleanLiteralRule createPositiveLiteralRule
    (final ModuleProxyFactory factory,
     final CompilerOperatorTable optable)
  {
    final BooleanVariablePlaceHolder VARNAME =
      new BooleanVariablePlaceHolder(factory, "VARNAME");
    final SimpleIdentifierProxy ident = VARNAME.getIdentifier();
    final IntConstantProxy value = factory.createIntConstantProxy(1);
    return new BooleanLiteralRule(ident, VARNAME, value);
  }

  static BooleanLiteralRule createNegativeLiteralRule
    (final ModuleProxyFactory factory,
     final CompilerOperatorTable optable)
  {
    final UnaryOperator op = optable.getNotOperator();
    final BooleanVariablePlaceHolder VARNAME =
      new BooleanVariablePlaceHolder(factory, "VARNAME");
    final SimpleIdentifierProxy ident = VARNAME.getIdentifier();
    final SimpleExpressionProxy template =
      factory.createUnaryExpressionProxy(op, ident);
    final IntConstantProxy value = factory.createIntConstantProxy(0);
    return new BooleanLiteralRule(template, VARNAME, value);
  }


  //#########################################################################
  //# Constructor
  private BooleanLiteralRule(final SimpleExpressionProxy template,
                             final BooleanVariablePlaceHolder placeholder,
                             final IntConstantProxy value)
  {
    super(template, placeholder);
    mPlaceHolder = placeholder;
    mValue = value;
  }


  //#########################################################################
  //# Invocation Interface
  boolean isMakingReplacement()
  {
    return true;
  }

  void execute(final ConstraintPropagator propagator)
    throws EvalException
  {
    final SimpleExpressionProxy varname = mPlaceHolder.getBoundExpression();
    propagator.processEquation(varname, mValue);
  }


  //#########################################################################
  //# Data Members
  private final BooleanVariablePlaceHolder mPlaceHolder;
  private final IntConstantProxy mValue;

}
