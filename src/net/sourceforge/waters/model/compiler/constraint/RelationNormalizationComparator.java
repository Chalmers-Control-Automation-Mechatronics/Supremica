//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2018 Robi Malik
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

import net.sourceforge.waters.model.expr.ExpressionComparator;
import net.sourceforge.waters.model.expr.UnaryOperator;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.context.VariableContext;
import net.sourceforge.waters.model.module.DefaultModuleProxyVisitor;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.UnaryExpressionProxy;


/**
 * <P>The expression comparator used to normalise equations. Positive
 * primed literals are sorted first, followed by positive unprimed
 * literals, followed by all other expressions in the ordering given by the
 * operator table. This ordering ensures that assignments are normalised as
 * <CODE>x'=x</CODE> and not <CODE>x=x'</CODE>.</P>
 *
 * @author Robi Malik
 */

class RelationNormalizationComparator
  extends ExpressionComparator
{

  //#########################################################################
  //# Constructor
  RelationNormalizationComparator(final CompilerOperatorTable optable,
                                  final VariableContext context)
  {
    super(optable);
    mVisitor = new LiteralTypeVisitor(optable, context);
  }


  //##########################################################################
  //# Interface java.util.Comparator
  public int compare(final SimpleExpressionProxy expr1,
                     final SimpleExpressionProxy expr2)
  {
    final LiteralType type1 = mVisitor.getLiteralType(expr1);
    final LiteralType type2 = mVisitor.getLiteralType(expr2);
    if (type1 != type2) {
      return type1.compareTo(type2);
    } else {
      return super.compare(expr1, expr2);
    }
  }


  //##########################################################################
  //# Inner Class LiteralTypeVisitor
  private static class LiteralTypeVisitor
    extends DefaultModuleProxyVisitor
  {

    //#######################################################################
    //# Constructor
    private LiteralTypeVisitor(final CompilerOperatorTable optable,
                               final VariableContext context)
    {
      mNextOperator = optable.getNextOperator();
      mContext = context;
    }

    //#######################################################################
    //# Invocation
    private LiteralType getLiteralType(final SimpleExpressionProxy expr)
    {
      try {
        return (LiteralType) expr.acceptVisitor(this);
      } catch (final VisitorException exception) {
        throw exception.getRuntimeException();
      }
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    public LiteralType visitIdentifierProxy(final IdentifierProxy ident)
    {
      if (mContext.isEnumAtom(ident)) {
        return visitSimpleExpressionProxy(ident);
      } else {
        return LiteralType.VARIABLE;
      }
    }

    public LiteralType visitSimpleExpressionProxy
      (final SimpleExpressionProxy expr)
    {
      return LiteralType.OTHER;
    }

    public LiteralType visitUnaryExpressionProxy
      (final UnaryExpressionProxy expr)
      throws VisitorException
    {
      if (expr.getOperator() == mNextOperator) {
        final SimpleExpressionProxy subterm = expr.getSubTerm();
        final LiteralType subtype = (LiteralType) subterm.acceptVisitor(this);
        if (subtype == LiteralType.VARIABLE) {
          return LiteralType.NEXTVARIABLE;
        }
      }
      return visitSimpleExpressionProxy(expr);
    }

    //#######################################################################
    //# Data Members
    private final UnaryOperator mNextOperator;
    private final VariableContext mContext;

  }


  //##########################################################################
  //# Inner Class LiteralType
  private static enum LiteralType {
    NEXTVARIABLE, VARIABLE, OTHER
  }


  //#########################################################################
  //# Data Members
  private final LiteralTypeVisitor mVisitor;

}
