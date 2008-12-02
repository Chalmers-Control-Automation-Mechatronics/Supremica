//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.constraint
//# CLASS:   RelationNormalizationComparator
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler.constraint;

import net.sourceforge.waters.model.expr.ExpressionComparator;
import net.sourceforge.waters.model.expr.UnaryOperator;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.context.VariableContext;
import net.sourceforge.waters.model.module.AbstractModuleProxyVisitor;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.UnaryExpressionProxy;


/**
 * <P>The expression comparator used to normalise equations.  Positive
 * primed literals are sorted first, followed by positive unprimed
 * literals, followed by all other expression in the ordering given by the
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
    extends AbstractModuleProxyVisitor
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
