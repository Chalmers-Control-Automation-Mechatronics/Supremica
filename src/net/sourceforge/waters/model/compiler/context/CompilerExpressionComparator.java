//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.context;
//# CLASS:   CompilerExpressionComparator
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler.context;

import java.util.Comparator;
import java.util.Iterator;

import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.expr.UnaryOperator;
import net.sourceforge.waters.model.module.AbstractModuleProxyVisitor;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.EnumSetExpressionProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.IndexedIdentifierProxy;
import net.sourceforge.waters.model.module.IntConstantProxy;
import net.sourceforge.waters.model.module.QualifiedIdentifierProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.model.module.UnaryExpressionProxy;


public class CompilerExpressionComparator
  extends AbstractModuleProxyVisitor
  implements Comparator<SimpleExpressionProxy>
{

  //#########################################################################
  //# Constructors
  public CompilerExpressionComparator(final CompilerOperatorTable optable)
  {
    this(optable, null, true);
  }

  public CompilerExpressionComparator(final CompilerOperatorTable optable,
                                      final BindingContext context)
  {
    this(optable, context, true);
  }

  public CompilerExpressionComparator(final CompilerOperatorTable optable,
                                      final boolean negliterals)
  {
    this(optable, null, negliterals);
  }

  public CompilerExpressionComparator(final CompilerOperatorTable optable,
                                      final BindingContext context,
                                      final boolean negliterals)
  {
    mOperatorTable = optable;
    mContext = context;
    mSupportsNegativeLiterals = negliterals;
  }


  //#########################################################################
  //# Interface java.util.Comparator
  public int compare(final SimpleExpressionProxy expr1,
                     final SimpleExpressionProxy expr2)
  {
    final ComparatorInfo info1 = getComparatorInfo(expr1);
    final ComparatorInfo info2 = getComparatorInfo(expr2);
    if (info1 == info2) {
      return info1.compare(expr1, expr2);
    } else {
      return info1.getComparatorValue() - info2.getComparatorValue();
    }
  }


  //#########################################################################
  //# Auxliary Methods
  private ComparatorInfo getComparatorInfo(final SimpleExpressionProxy expr)
  {
    try {
      return (ComparatorInfo) expr.acceptVisitor(this);
    } catch (final VisitorException exception) {
      throw exception.getRuntimeException();
    }
  }

  private boolean isEnumAtom(final SimpleIdentifierProxy ident)
  {
    if (mContext == null) {
      return false;
    } else {
      return mContext.isEnumAtom(ident);
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
  public ComparatorInfo visitBinaryExpressionProxy
    (final BinaryExpressionProxy expr)
  {
    return BINARY;
  }

  public ComparatorInfo visitEnumSetExpressionProxy
    (final EnumSetExpressionProxy enumset)
  {
    return ENUM_SET;
  }

  public ComparatorInfo visitIntConstantProxy(final IntConstantProxy intconst)
  {
    return CONSTANT;
  }

  public ComparatorInfo visitIndexedIdentifierProxy
    (final IndexedIdentifierProxy ident)
  {
    return INDEXED_IDENT;
  }

  public ComparatorInfo visitQualifiedIdentifierProxy
    (final QualifiedIdentifierProxy ident)
  {
    return QUAL_IDENT;
  }

  public ComparatorInfo visitSimpleIdentifierProxy
    (final SimpleIdentifierProxy ident)
  {
    if (isEnumAtom(ident)) {
      return ENUM_ATOM;
    } else {
      return SIMPLE_IDENT;
    }
  }

  public ComparatorInfo visitUnaryExpressionProxy
    (final UnaryExpressionProxy expr)
    throws VisitorException
  {
    final UnaryOperator op = expr.getOperator();
    if (op == mOperatorTable.getNotOperator() && mSupportsNegativeLiterals) {
      final SimpleExpressionProxy subterm = expr.getSubTerm();
      final ComparatorInfo subinfo =
        (ComparatorInfo) subterm.acceptVisitor(this);
      if (subinfo instanceof IdentifierInfo) {
        return NEGATED_IDENT;
      } else if (subinfo == PRIMED_IDENT) {
        return NEGATED_PRIMED_IDENT;
      } else {
        return UNARY;
      }
    } else if (op == mOperatorTable.getNextOperator()) {
      final SimpleExpressionProxy subterm = expr.getSubTerm();
      final ComparatorInfo subinfo =
        (ComparatorInfo) subterm.acceptVisitor(this);
      if (subinfo instanceof IdentifierInfo) {
        return PRIMED_IDENT;
      } else {
        return UNARY;
      }
    } else {
      return UNARY;
    }
  }


  //#########################################################################
  //# Inner Class ComparatorInfo
  private abstract class ComparatorInfo {

    //#######################################################################
    //# Constructor
    ComparatorInfo(final int value)
    {
      mComparatorValue = value;
    }

    //#######################################################################
    //# Simple Access
    int getComparatorValue()
    {
      return mComparatorValue;
    }

    abstract int compare(final SimpleExpressionProxy expr1,
                         final SimpleExpressionProxy expr2);

    //#######################################################################
    //# Data Members
    private final int mComparatorValue;

  }


  //#########################################################################
  //# Inner Class IdentifierInfo
  private abstract class IdentifierInfo extends ComparatorInfo {

    //#######################################################################
    //# Constructor
    IdentifierInfo(final int value)
    {
      super(value);
    }

  }


  //#########################################################################
  //# Inner Class SimpleIdentifierInfo
  private class SimpleIdentifierInfo extends IdentifierInfo {

    //#######################################################################
    //# Constructor
    SimpleIdentifierInfo(final int value)
    {
      super(value);
    }

    //#######################################################################
    //# Simple Access
    int compare(final SimpleExpressionProxy expr1,
                final SimpleExpressionProxy expr2)
    {
      final SimpleIdentifierProxy ident1 = (SimpleIdentifierProxy) expr1;
      final SimpleIdentifierProxy ident2 = (SimpleIdentifierProxy) expr2;
      final String name1 = ident1.getName();
      final String name2 = ident2.getName();
      return name1.compareTo(name2);
    }

  }


  //#########################################################################
  //# Inner Class IndexedIdentifierInfo
  private class IndexedIdentifierInfo extends IdentifierInfo {

    //#######################################################################
    //# Constructor
    IndexedIdentifierInfo(final int value)
    {
      super(value);
    }

    //#######################################################################
    //# Simple Access
    int compare(final SimpleExpressionProxy expr1,
                final SimpleExpressionProxy expr2)
    {
      final IndexedIdentifierProxy ident1 = (IndexedIdentifierProxy) expr1;
      final IndexedIdentifierProxy ident2 = (IndexedIdentifierProxy) expr2;
      final String name1 = ident1.getName();
      final String name2 = ident2.getName();
      final int namecomp = name1.compareTo(name2);
      if (namecomp != 0) {
        return namecomp;
      }
      final Iterator<SimpleExpressionProxy> iter1 =
        ident1.getIndexes().iterator();
      final Iterator<SimpleExpressionProxy> iter2 =
        ident2.getIndexes().iterator();
      while (iter1.hasNext()) {
        if (iter2.hasNext()) {
          final SimpleExpressionProxy index1 = iter1.next();
          final SimpleExpressionProxy index2 = iter2.next();
          final int indexcomp =
            CompilerExpressionComparator.this.compare(index1, index2);
          if (indexcomp != 0) {
            return indexcomp;
          }
        } else {
          return 1;
        }
      }
      return iter2.hasNext() ? -1 : 0;
    }

  }


  //#########################################################################
  //# Inner Class QualifiedIdentifierInfo
  private class QualifiedIdentifierInfo extends IdentifierInfo {

    //#######################################################################
    //# Constructor
    QualifiedIdentifierInfo(final int value)
    {
      super(value);
    }

    //#######################################################################
    //# Simple Access
    int compare(final SimpleExpressionProxy expr1,
                final SimpleExpressionProxy expr2)
    {
      final QualifiedIdentifierProxy ident1 = (QualifiedIdentifierProxy) expr1;
      final QualifiedIdentifierProxy ident2 = (QualifiedIdentifierProxy) expr2;
      final IdentifierProxy base1 = ident1.getBaseIdentifier();
      final IdentifierProxy base2 = ident2.getBaseIdentifier();
      final int basecomp =
        CompilerExpressionComparator.this.compare(base1, base2);
      if (basecomp != 0) {
        return basecomp;
      }
      final IdentifierProxy comp1 = ident1.getComponentIdentifier();
      final IdentifierProxy comp2 = ident2.getComponentIdentifier();
      return CompilerExpressionComparator.this.compare(comp1, comp2);
    }

  }


  //#########################################################################
  //# Inner Class LiteralInfo
  private abstract class LiteralInfo extends ComparatorInfo {

    //#######################################################################
    //# Constructor
    LiteralInfo(final int value)
    {
      super(value);
    }

    //#######################################################################
    //# Simple Access
    int compare(final SimpleExpressionProxy expr1,
                final SimpleExpressionProxy expr2)
    {
      final SimpleExpressionProxy child1 = getChild(expr1);
      final SimpleExpressionProxy child2 = getChild(expr2);
      return CompilerExpressionComparator.this.compare(child1, child2);
    }

    abstract SimpleExpressionProxy getChild(final SimpleExpressionProxy expr);

  }


  //#########################################################################
  //# Inner Class LiteralInfo1
  private class LiteralInfo1 extends LiteralInfo {

    //#######################################################################
    //# Constructor
    LiteralInfo1(final int value)
    {
      super(value);
    }

    //#######################################################################
    //# Simple Access
    SimpleExpressionProxy getChild(final SimpleExpressionProxy expr)
    {
      final UnaryExpressionProxy unary = (UnaryExpressionProxy) expr;
      return unary.getSubTerm();
    }

  }


  //#########################################################################
  //# Inner Class LiteralInfo2
  private class LiteralInfo2 extends LiteralInfo {

    //#######################################################################
    //# Constructor
    LiteralInfo2(final int value)
    {
      super(value);
    }

    //#######################################################################
    //# Simple Access
    SimpleExpressionProxy getChild(final SimpleExpressionProxy expr)
    {
      final UnaryExpressionProxy unary0 = (UnaryExpressionProxy) expr;
      final UnaryExpressionProxy unary1 =
        (UnaryExpressionProxy) unary0.getSubTerm();
      return unary1.getSubTerm();
    }

  }


  //#########################################################################
  //# Inner Class IntConstantInfo
  private class IntConstantInfo extends ComparatorInfo {

    //#######################################################################
    //# Constructor
    IntConstantInfo(final int value)
    {
      super(value);
    }

    //#######################################################################
    //# Simple Access
    int compare(final SimpleExpressionProxy expr1,
                final SimpleExpressionProxy expr2)
    {
      final IntConstantProxy intconst1 = (IntConstantProxy) expr1;
      final IntConstantProxy intconst2 = (IntConstantProxy) expr2;
      final int value1 = intconst1.getValue();
      final int value2 = intconst2.getValue();
      if (value1 < value2) {
        return -1;
      } else if (value1 > value2) {
        return 1;
      } else {
        return 0;
      }
    }

  }


  //#########################################################################
  //# Inner Class UnaryExpressionInfo
  private class UnaryExpressionInfo extends ComparatorInfo {

    //#######################################################################
    //# Constructor
    UnaryExpressionInfo(final int value)
    {
      super(value);
    }

    //#######################################################################
    //# Simple Access
    int compare(final SimpleExpressionProxy expr1,
                final SimpleExpressionProxy expr2)
    {
      final UnaryExpressionProxy unary1 = (UnaryExpressionProxy) expr1;
      final UnaryExpressionProxy unary2 = (UnaryExpressionProxy) expr2;
      final UnaryOperator op1 = unary1.getOperator();
      final UnaryOperator op2 = unary2.getOperator();
      final int opcomp =
        mOperatorTable.getOperatorValue(op1) -
        mOperatorTable.getOperatorValue(op2);
      if (opcomp != 0) {
        return opcomp;
      }
      final SimpleExpressionProxy subterm1 = unary1.getSubTerm();
      final SimpleExpressionProxy subterm2 = unary2.getSubTerm();
      return CompilerExpressionComparator.this.compare(subterm1, subterm2);
    }

  }


  //#########################################################################
  //# Inner Class BinaryExpressionInfo
  private class BinaryExpressionInfo extends ComparatorInfo {

    //#######################################################################
    //# Constructor
    BinaryExpressionInfo(final int value)
    {
      super(value);
    }

    //#######################################################################
    //# Simple Access
    int compare(final SimpleExpressionProxy expr1,
                final SimpleExpressionProxy expr2)
    {
      final BinaryExpressionProxy binary1 = (BinaryExpressionProxy) expr1;
      final BinaryExpressionProxy binary2 = (BinaryExpressionProxy) expr2;
      final BinaryOperator op1 = binary1.getOperator();
      final BinaryOperator op2 = binary2.getOperator();
      final int opcomp =
        mOperatorTable.getOperatorValue(op1) -
        mOperatorTable.getOperatorValue(op2);
      if (opcomp != 0) {
        return opcomp;
      }
      final SimpleExpressionProxy lhs1 = binary1.getLeft();
      final SimpleExpressionProxy lhs2 = binary2.getLeft();
      final int lhscomp =
        CompilerExpressionComparator.this.compare(lhs1, lhs2);
      if (lhscomp != 0) {
        return lhscomp;
      }
      final SimpleExpressionProxy rhs1 = binary1.getRight();
      final SimpleExpressionProxy rhs2 = binary2.getRight();
      return CompilerExpressionComparator.this.compare(rhs1, rhs2);
    }

  }


  //#########################################################################
  //# Inner Class EnumSetExpressionInfo
  private class EnumSetExpressionInfo extends ComparatorInfo {

    //#######################################################################
    //# Constructor
    EnumSetExpressionInfo(final int value)
    {
      super(value);
    }

    //#######################################################################
    //# Simple Access
    int compare(final SimpleExpressionProxy expr1,
                final SimpleExpressionProxy expr2)
    {
      final EnumSetExpressionProxy enum1 = (EnumSetExpressionProxy) expr1;
      final EnumSetExpressionProxy enum2 = (EnumSetExpressionProxy) expr2;
      final Iterator<SimpleIdentifierProxy> iter1 =
        enum1.getItems().iterator();
      final Iterator<SimpleIdentifierProxy> iter2 =
        enum2.getItems().iterator();
      while (iter1.hasNext()) {
        if (iter2.hasNext()) {
          final String name1 = iter1.next().getName();
          final String name2 = iter2.next().getName();
          final int itemcomp = name1.compareTo(name2);
          if (itemcomp != 0) {
            return itemcomp;
          }
        } else {
          return 1;
        }
      }
      return iter2.hasNext() ? -1 : 0;
    }

  }


  //#########################################################################
  //# Data Members
  private final CompilerOperatorTable mOperatorTable;
  private final BindingContext mContext;
  private final boolean mSupportsNegativeLiterals;


  //#########################################################################
  //# Class Constants
  private final ComparatorInfo SIMPLE_IDENT = new SimpleIdentifierInfo(0);
  private final ComparatorInfo INDEXED_IDENT = new IndexedIdentifierInfo(1);
  private final ComparatorInfo QUAL_IDENT = new QualifiedIdentifierInfo(2);
  private final ComparatorInfo NEGATED_IDENT = new LiteralInfo1(3);
  private final ComparatorInfo PRIMED_IDENT = new LiteralInfo1(4);
  private final ComparatorInfo NEGATED_PRIMED_IDENT = new LiteralInfo2(5);
  private final ComparatorInfo CONSTANT = new IntConstantInfo(6);
  private final ComparatorInfo ENUM_ATOM = new SimpleIdentifierInfo(7);
  private final ComparatorInfo UNARY = new UnaryExpressionInfo(8);
  private final ComparatorInfo BINARY = new BinaryExpressionInfo(9);
  private final ComparatorInfo ENUM_SET = new EnumSetExpressionInfo(10);

}
