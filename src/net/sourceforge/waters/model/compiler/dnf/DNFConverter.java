//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.dnf
//# CLASS:   DNFConverter
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler.dnf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.context.
  CompilerExpressionComparator;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.ExpressionComparator;
import net.sourceforge.waters.model.expr.TypeMismatchException;
import net.sourceforge.waters.model.expr.UnaryOperator;
import net.sourceforge.waters.model.module.AbstractModuleProxyVisitor;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.IntConstantProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.UnaryExpressionProxy;


/**
 * @author Robi Malik
 */

public class DNFConverter extends AbstractModuleProxyVisitor {

  //#########################################################################
  //# Constructors
  public DNFConverter(final ModuleProxyFactory factory,
                      final CompilerOperatorTable optable)
  {
    this(factory, optable, new CompilerExpressionComparator(optable));
  }

  public DNFConverter(final ModuleProxyFactory factory,
                      final CompilerOperatorTable optable,
                      final Comparator<SimpleExpressionProxy> comparator)
  {
    mFactory = factory;
    mOperatorTable = optable;
    mComparator = comparator;
  }


  //##########################################################################
  //# Invocation
  public CompiledNormalForm convertToDNF(final SimpleExpressionProxy expr)
    throws EvalException
  {
    final BinaryOperator andop = mOperatorTable.getAndOperator();
    final BinaryOperator orop = mOperatorTable.getOrOperator();
    return convert(expr, orop, andop, false);
  }

  public CompiledNormalForm convertToCNF(final SimpleExpressionProxy expr)
    throws EvalException
  {
    final BinaryOperator andop = mOperatorTable.getAndOperator();
    final BinaryOperator orop = mOperatorTable.getOrOperator();
    return convert(expr, andop, orop, true);
  }

  public CompiledNormalForm convert(final SimpleExpressionProxy expr,
                                    final BinaryOperator op1,
                                    final BinaryOperator op2,
                                    final boolean emptyval)
    throws EvalException
  {
    try {
      mNegative = false;
      mTopLevelOperator = op1;
      mClauseLevelOperator = op2;
      return (CompiledNormalForm) expr.acceptVisitor(this);
    } catch (final VisitorException exception) {
      final Throwable cause = exception.getCause();
      if (cause instanceof EvalException) {
        throw (EvalException) cause;
      } else {
        throw exception.getRuntimeException();
      }
    }
  }

  public SimpleExpressionProxy getNegatedLiteral
    (final SimpleExpressionProxy expr)
    throws TypeMismatchException
  {
    return getNormalisedLiteral(expr, true);
  }

  public List<SimpleExpressionProxy> createSortedClauseList
    (final CompiledNormalForm nf)
  {
    final Collection<CompiledClause> clauses = nf.getClauses();
    final int numclauses = clauses.size();
    final List<SimpleExpressionProxy> list =
      new ArrayList<SimpleExpressionProxy>(numclauses);
    for (final CompiledClause clause : clauses) {
      final BinaryOperator op = clause.getOperator();
      final Collection<SimpleExpressionProxy> literals = clause.getLiterals();
      final List<SimpleExpressionProxy> sorted =
        new ArrayList<SimpleExpressionProxy>(literals);
      Collections.sort(sorted, mComparator);
      SimpleExpressionProxy expr;
      final Iterator<SimpleExpressionProxy> iter = sorted.iterator();
      if (iter.hasNext()) {
        expr = iter.next();
        while (iter.hasNext()) {
          final SimpleExpressionProxy next = iter.next();
          expr = mFactory.createBinaryExpressionProxy(op, expr, next);
        }
      } else if (op == mOperatorTable.getOrOperator()) {
        expr = mFactory.createIntConstantProxy(0);
      } else {
        expr = mFactory.createIntConstantProxy(1);
      }
      list.add(expr);
    }
    Collections.sort(list, mComparator);
    return list;
  }


  //##########################################################################
  //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
  public CompiledNormalForm visitBinaryExpressionProxy
    (final BinaryExpressionProxy expr)
    throws VisitorException
  {
    try {
      final BinaryOperator andop = mOperatorTable.getAndOperator();
      final BinaryOperator orop = mOperatorTable.getOrOperator();
      final BinaryOperator op = expr.getOperator();
      if (op == orop || op == andop) {
        BinaryOperator mergeop = op;
        if (mNegative) {
          if (op == orop) {
            mergeop = andop;
          } else {
            mergeop = orop;
          }
        }
        final SimpleExpressionProxy lhs = expr.getLeft();
        final SimpleExpressionProxy rhs = expr.getRight();
        final CompiledNormalForm lhsnf =
          (CompiledNormalForm) lhs.acceptVisitor(this);
        final CompiledNormalForm rhsnf =
          (CompiledNormalForm) rhs.acceptVisitor(this);
        return merge(mergeop, lhsnf, rhsnf);
      } else if (mOperatorTable.getComplementaryOperator(op) == null) {
        throw new TypeMismatchException(expr, "BOOLEAN");
      } else {
        final SimpleExpressionProxy literal =
          getNormalisedLiteral(expr, mNegative);
        return createNormalForm(literal);
      }
    } catch (final TypeMismatchException exception) {
      throw wrap(exception);
    }
  }

  public CompiledNormalForm visitIdentifierProxy(final IdentifierProxy ident)
    throws VisitorException
  {
    try {
      final SimpleExpressionProxy literal =
        getNormalisedLiteral(ident, mNegative);
      return createNormalForm(literal);
    } catch (final TypeMismatchException exception) {
      throw wrap(exception);
    }
  }

  public CompiledNormalForm visitIntConstantProxy
    (final IntConstantProxy intconst)
    throws VisitorException
  {
    try {
      final boolean value = getBoolean(intconst) ^ mNegative;
      return createNormalForm(value);
    } catch (final TypeMismatchException exception) {
      throw wrap(exception);
    }
  }

  public CompiledNormalForm visitSimpleExpressionProxy
    (final SimpleExpressionProxy expr)
    throws VisitorException
  {
    final TypeMismatchException exception =
      new TypeMismatchException(expr, "BOOLEAN");
    throw wrap(exception);
  }

  public CompiledNormalForm visitUnaryExpressionProxy
    (final UnaryExpressionProxy expr)
    throws VisitorException
  {
    try {
      final UnaryOperator op = expr.getOperator();
      if (op == mOperatorTable.getNotOperator()) {
        mNegative = !mNegative;
        final SimpleExpressionProxy subterm = expr.getSubTerm();
        final CompiledNormalForm result =
          (CompiledNormalForm) subterm.acceptVisitor(this);
        mNegative = !mNegative;
        return result;
      } else {
        final SimpleExpressionProxy literal =
          getNormalisedLiteral(expr, mNegative);
        return createNormalForm(literal);
      }
    } catch (final TypeMismatchException exception) {
      throw wrap(exception);
    }
  }


  //#########################################################################
  //# Merging
  private CompiledNormalForm merge(final BinaryOperator op,
                                   final CompiledNormalForm nf1,
                                   final CompiledNormalForm nf2)
    throws TypeMismatchException
  {
    if (op == mTopLevelOperator) {
      return mergeAdd(nf1, nf2);
    } else {
      return mergeMultiply(nf1, nf2);
    }
  }

  private CompiledNormalForm mergeAdd(final CompiledNormalForm nf1,
                                      final CompiledNormalForm nf2)
    throws TypeMismatchException
  {
    final Collection<CompiledClause> clauses1 = nf1.getClauses();
    final Collection<CompiledClause> clauses2 = nf2.getClauses();
    final Collection<CompiledClause> newclauses =
      new ArrayList<CompiledClause>(clauses2.size());
    nextclause:
    for (final CompiledClause clause2 : clauses2) {
      final CompiledClause negclause2 = getNegatedClause(clause2);
      final Iterator<CompiledClause> iter1 = clauses1.iterator();
      while (iter1.hasNext()) {
        final CompiledClause clause1 = iter1.next();
        if (clause1.equals(negclause2)) {
          return createNormalFormOne();
        } else if (clause2.isSubsumedBy(clause1)) {
          continue nextclause;
        } else if (clause1.isSubsumedBy(clause2)) {
          iter1.remove();
        }
      }
      newclauses.add(clause2);
    }
    nf1.addAll(newclauses);
    return nf1;
  }

  private CompiledNormalForm mergeMultiply(final CompiledNormalForm nf1,
                                           final CompiledNormalForm nf2)
    throws TypeMismatchException
  {
    final CompiledNormalForm result =
      new CompiledNormalForm(mTopLevelOperator);
    final Collection<CompiledClause> clauses1 = nf1.getClauses();
    final Collection<CompiledClause> clauses2 = nf2.getClauses();
    for (final CompiledClause clause1 : clauses1) {
      nextpair:
      for (final CompiledClause clause2 : clauses2) {
        final CompiledClause newclause = clause1.clone();
        for (final SimpleExpressionProxy literal : clause2.getLiterals()) {
          if (clause1.contains(literal)) {
            // skip
          } else if (clause1.contains(getNegatedLiteral(literal))) {
            continue nextpair;
          } else {
            newclause.add(literal);
          }
        }
        final CompiledClause negclause = getNegatedClause(newclause);
        final Collection<CompiledClause> oldclauses = result.getClauses();
        final Iterator<CompiledClause> iter = oldclauses.iterator();
        while (iter.hasNext()) {
          final CompiledClause oldclause = iter.next();
          if (oldclause.equals(negclause)) {
            return createNormalFormOne();
          } else if (newclause.isSubsumedBy(oldclause)) {
            continue nextpair;
          } else if (oldclause.isSubsumedBy(newclause)) {
            iter.remove();
          }
        }
        result.add(newclause);
      }
    }
    return result;
  }


  //#########################################################################
  //# Negation
  CompiledClause getNegatedClause(final CompiledClause clause)
    throws TypeMismatchException
  {
    final Collection<SimpleExpressionProxy> literals = clause.getLiterals();
    final Iterator<SimpleExpressionProxy> iter = literals.iterator();
    final SimpleExpressionProxy literal = iter.next();
    if (iter.hasNext()) {
      return null;
    }
    final BinaryOperator op = clause.getOperator();
    final SimpleExpressionProxy negliteral = getNegatedLiteral(literal);
    return new CompiledClause(op, negliteral);
  }

  SimpleExpressionProxy getNormalisedLiteral(final SimpleExpressionProxy expr,
                                             final boolean negated)
    throws TypeMismatchException
  {
    final UnaryOperator notop = mOperatorTable.getNotOperator();
    if (expr instanceof BinaryExpressionProxy) {
      final BinaryExpressionProxy binary = (BinaryExpressionProxy) expr;
      BinaryOperator op = binary.getOperator();
      if (negated) {
        op = mOperatorTable.getComplementaryOperator(op);
        if (op == null) {
          throw new TypeMismatchException(expr, "BOOLEAN");
        }
      }
      final SimpleExpressionProxy lhs = binary.getLeft();
      final SimpleExpressionProxy rhs = binary.getRight();
      final boolean swap;
      if (op.isSymmetric()) {
        swap = mComparator.compare(lhs, rhs) > 0;
      } else {
        final BinaryOperator swapped =
          mOperatorTable.getSwappedNormalOperator(op);
        swap = swapped != null;
        if (swap) {
          op = swapped;
        }
      }
      if (swap) {
        return mFactory.createBinaryExpressionProxy(op, rhs, lhs);
      } else {
        return mFactory.createBinaryExpressionProxy(op, lhs, rhs);
      }
    } else if (!negated) {
      return expr;
    } else if (expr instanceof UnaryExpressionProxy) {
      final UnaryExpressionProxy unary = (UnaryExpressionProxy) expr;
      final UnaryOperator op = unary.getOperator();
      if (op == notop) {
        return unary.getSubTerm();
      }
    }
    return mFactory.createUnaryExpressionProxy(notop, expr);
  }


  //#########################################################################
  //# Auxiliary Methods
  private CompiledNormalForm createNormalForm(final boolean value)
  {
    if (value ^ (mTopLevelOperator == mOperatorTable.getOrOperator())) {
      return createNormalFormZero();
    } else {
      return createNormalFormOne();
    }
  }

  private CompiledNormalForm createNormalFormZero()
  {
    return new CompiledNormalForm(mTopLevelOperator);
  }

  private CompiledNormalForm createNormalFormOne()
  {
    final CompiledClause clause = new CompiledClause(mClauseLevelOperator);
    return new CompiledNormalForm(mTopLevelOperator, clause);
  }

  private CompiledNormalForm createNormalForm
    (final SimpleExpressionProxy literal)
  {
    final CompiledClause clause =
      new CompiledClause(mClauseLevelOperator, literal);
    return new CompiledNormalForm(mTopLevelOperator, clause);
  }

  private boolean getBoolean(final IntConstantProxy intconst)
    throws TypeMismatchException
  {
    final int value = intconst.getValue();
    switch (value) {
    case 0:
      return false;
    case 1:
      return true;
    default:
      throw new TypeMismatchException(intconst, "BOOLEAN");
    }
  }


  //#########################################################################
  //# Data Members
  private final ModuleProxyFactory mFactory;
  private final CompilerOperatorTable mOperatorTable;
  private final Comparator<SimpleExpressionProxy> mComparator;

  private boolean mNegative;
  private BinaryOperator mTopLevelOperator;
  private BinaryOperator mClauseLevelOperator;

}
