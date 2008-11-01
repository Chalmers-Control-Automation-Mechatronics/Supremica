//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.efa
//# CLASS:   ConstraintPropagator
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler.efa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.context.SimpleExpressionCompiler;
import net.sourceforge.waters.model.compiler.dnf.CompiledClause;
import net.sourceforge.waters.model.compiler.dnf.DNFConverter;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.TypeMismatchException;
import net.sourceforge.waters.model.expr.UnaryOperator;
import net.sourceforge.waters.model.module.AbstractModuleProxyVisitor;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.IndexedIdentifierProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.QualifiedIdentifierProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.UnaryExpressionProxy;


class ConstraintPropagator
{

  //#########################################################################
  //# Constructors
  ConstraintPropagator
    (final ModuleProxyFactory factory,
     final CompilerOperatorTable optable,
     final SimpleExpressionCompiler compiler,
     final EFAVariableMap varmap)
  {
    final Comparator<SimpleExpressionProxy> comparator =
      varmap.getExpressionComparator();
    mFactory = factory;
    mOperatorTable = optable;
    mSimpleExpressionCompiler = compiler;
    mDNFConverter = new DNFConverter(factory, optable, comparator);
    mVariableMap = varmap;
    mOccursCheckVisitor = new OccursCheckVisitor();
    mSubstitutionVisitor = new SubstitutionVisitor();
  }


  //#########################################################################
  //# Invocation
  CompiledClause propagate(final CompiledClause clause)
  {
    final Comparator<SimpleExpressionProxy> comparator =
      mVariableMap.getExpressionComparator();
    final Collection<SimpleExpressionProxy> literals = clause.getLiterals();
    mOpenLiterals = new TreeSet<SimpleExpressionProxy>(comparator);
    mOpenLiterals.addAll(literals);
    final int oldsize = mOpenLiterals.size();
    mProcessedEquations = new ArrayList<SimpleExpressionProxy>(oldsize);
    mIsFalse = false;
    boolean change = false;
    while (simplify()) {
      change = true;
    }
    final CompiledClause result;
    if (mIsFalse) {
      result = null;
    } else if (change) {
      final BinaryOperator op = clause.getOperator();
      final int newsize = mOpenLiterals.size() + mProcessedEquations.size();
      result = new CompiledClause(op, newsize);
      result.addAll(mProcessedEquations);
      result.addAll(mOpenLiterals);
      return result;
    } else {
      result = clause;
    }
    mOpenLiterals = null;
    mProcessedEquations = null;
    return result;
  }


  //#########################################################################
  //# Algorithm
  private boolean simplify()
  {
    final BinaryOperator eqop = mOperatorTable.getEqualsOperator();
    final Iterator<SimpleExpressionProxy> iter = mOpenLiterals.iterator();
    while (iter.hasNext()) {
      final SimpleExpressionProxy literal = iter.next();
      if (!(literal instanceof BinaryExpressionProxy)) {
        continue;
      }
      final BinaryExpressionProxy binary = (BinaryExpressionProxy) literal;
      final BinaryOperator op = binary.getOperator();
      if (op != eqop) {
        continue;
      }
      final SimpleExpressionProxy lhs = binary.getLeft();
      final EFAVariable var = mVariableMap.getVariable(lhs);
      if (var == null) {
        continue;
      }
      final SimpleExpressionProxy rhs = binary.getRight();
      if (mOccursCheckVisitor.occurs(lhs, rhs)) {
        continue;
      }
      iter.remove();
      mProcessedEquations.add(literal);
      substitute(lhs, rhs);
      return !mIsFalse;
    }
    return false;
  }

  private void substitute(final SimpleExpressionProxy varname,
                          final SimpleExpressionProxy replacement)
  {
    final List<SimpleExpressionProxy> literals =
      new ArrayList<SimpleExpressionProxy>(mOpenLiterals);
    mOpenLiterals.clear();
    for (final SimpleExpressionProxy literal : literals) {
      final SimpleExpressionProxy subst =
        mSubstitutionVisitor.substitute(literal, varname, replacement);
      if (literal == subst) {
        mOpenLiterals.add(literal);
      } else {
        final SimpleExpressionProxy simp = simplify(subst);
        if (isBooleanValue(simp)) {
          if (!getBooleanValue(simp)) {
            mIsFalse = true;
            return;
          }
        } else if (mOpenLiterals.add(simp)) {
          final SimpleExpressionProxy complement = getNegatedLiteral(simp);
          if (mOpenLiterals.contains(complement)) {
            mIsFalse = true;
            return;
          }
        }
      }
    }    
  }


  //#########################################################################
  //# Auxiliary Methods
  private SimpleExpressionProxy simplify(final SimpleExpressionProxy literal)
  {
    try {
      return mSimpleExpressionCompiler.simplify(literal, null);
    } catch (final EvalException exception) {
      throw exception.getRuntimeException();
    }
  }

  private boolean isBooleanValue(final SimpleExpressionProxy literal)
  {
    return mSimpleExpressionCompiler.isBooleanValue(literal);
  }

  private boolean getBooleanValue(final SimpleExpressionProxy literal)
  {
    try {
      return mSimpleExpressionCompiler.getBooleanValue(literal);
    } catch (final TypeMismatchException exception) {
      throw exception.getRuntimeException();
    }
  }

  private SimpleExpressionProxy getNegatedLiteral
    (final SimpleExpressionProxy literal)
  {
    try {
      return mDNFConverter.getNegatedLiteral(literal);
    } catch (final TypeMismatchException exception) {
      throw exception.getRuntimeException();
    }
  }


  //#########################################################################
  //# Inner Class OccursCheckVisitor
  private class OccursCheckVisitor
    extends AbstractModuleProxyVisitor
  {

    //#######################################################################
    //# Invocation
    boolean occurs(final SimpleExpressionProxy varname,
                   final SimpleExpressionProxy expr)
    {
      try {
        mVarName = varname;
        return occurs(expr);
      } catch (final VisitorException exception) {
        throw exception.getRuntimeException();
      } finally {
        mVarName = null;
      }
    }

    //#######################################################################
    //# Auxiliary Methods
    private boolean occurs(final SimpleExpressionProxy expr)
      throws VisitorException
    {
      if (expr.equalsByContents(mVarName)) {
        return true;
      } else {
        return (Boolean) expr.acceptVisitor(this);
      }        
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    public Boolean visitBinaryExpressionProxy
      (final BinaryExpressionProxy expr)
      throws VisitorException
    {
      final SimpleExpressionProxy lhs = expr.getLeft();
      final SimpleExpressionProxy rhs = expr.getRight();
      return occurs(lhs) || occurs(rhs);
    }

    public Boolean visitIndexedIdentifierProxy
      (final IndexedIdentifierProxy ident)
      throws VisitorException
    {
      final List<SimpleExpressionProxy> indexes = ident.getIndexes();
      for (final SimpleExpressionProxy index : indexes) {
        if (occurs(index)) {
          return true;
        }
      }
      return false;
    }

    public Boolean visitQualifiedIdentifierProxy
      (final QualifiedIdentifierProxy ident)
      throws VisitorException
    {
      final IdentifierProxy base = ident.getBaseIdentifier();
      final Boolean occbase = (Boolean) base.acceptVisitor(this);
      if (occbase) {
        return occbase;
      }
      final IdentifierProxy comp = ident.getComponentIdentifier();
      return (Boolean) comp.acceptVisitor(this);
    }

    public Boolean visitSimpleExpressionProxy
      (final SimpleExpressionProxy expr)
    {
      return false;
    }

    public Boolean visitUnaryExpressionProxy
      (final UnaryExpressionProxy expr)
      throws VisitorException
    {
      final SimpleExpressionProxy subterm = expr.getSubTerm();
      return occurs(subterm);
    }

    //#######################################################################
    //# Data Members
    private SimpleExpressionProxy mVarName;

  }


  //#########################################################################
  //# Inner Class SubstitutionVisitor
  private class SubstitutionVisitor
    extends AbstractModuleProxyVisitor
  {

    //#######################################################################
    //# Invocation
    SimpleExpressionProxy substitute(final SimpleExpressionProxy expr,
                                     final SimpleExpressionProxy varname,
                                     final SimpleExpressionProxy replacement)
    {
      try {
        mVarName = varname;
        mReplacement = replacement;
        return substitute(expr);
      } catch (final VisitorException exception) {
        throw exception.getRuntimeException();
      } finally {
        mVarName = null;
        mReplacement = null;
      }
    }

    //#######################################################################
    //# Auxiliary Methods
    private SimpleExpressionProxy substitute(final SimpleExpressionProxy expr)
      throws VisitorException
    {
      if (expr.equalsByContents(mVarName)) {
        return mReplacement;
      } else {
        return (SimpleExpressionProxy) expr.acceptVisitor(this);
      }        
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    public SimpleExpressionProxy visitBinaryExpressionProxy
      (final BinaryExpressionProxy expr)
      throws VisitorException
    {
      final SimpleExpressionProxy oldlhs = expr.getLeft();
      final SimpleExpressionProxy newlhs = substitute(oldlhs);
      final SimpleExpressionProxy oldrhs = expr.getRight();
      final SimpleExpressionProxy newrhs = substitute(oldrhs);
      if (oldlhs != newlhs || oldrhs != newrhs) {
        final BinaryOperator op = expr.getOperator();
        return mFactory.createBinaryExpressionProxy(op, newlhs, newrhs);
      } else {
        return expr;
      }
    }

    public SimpleExpressionProxy visitIndexedIdentifierProxy
      (final IndexedIdentifierProxy ident)
      throws VisitorException
    {
      boolean change = false;
      final List<SimpleExpressionProxy> oldindexes = ident.getIndexes();
      final int size = oldindexes.size();
      final List<SimpleExpressionProxy> newindexes =
        new ArrayList<SimpleExpressionProxy>(size);
      for (final SimpleExpressionProxy oldindex : oldindexes) {
        final SimpleExpressionProxy newindex = substitute(oldindex);
        newindexes.add(newindex);
        change |= (oldindex != newindex);
      }
      if (change) {
        final String name = ident.getName();
        return mFactory.createIndexedIdentifierProxy(name, newindexes);
      } else {
        return ident;
      }
    }

    public SimpleExpressionProxy visitQualifiedIdentifierProxy
      (final QualifiedIdentifierProxy ident)
      throws VisitorException
    {
      final IdentifierProxy oldbase = ident.getBaseIdentifier();
      final IdentifierProxy newbase =
        (IdentifierProxy) oldbase.acceptVisitor(this);
      final IdentifierProxy oldcomp = ident.getComponentIdentifier();
      final IdentifierProxy newcomp =
        (IdentifierProxy) oldcomp.acceptVisitor(this);
      if (oldbase != newbase || oldcomp != newcomp) {
        return mFactory.createQualifiedIdentifierProxy(newbase, newcomp);
      } else {
        return ident;
      }
    }

    public SimpleExpressionProxy visitSimpleExpressionProxy
      (final SimpleExpressionProxy expr)
    {
      return expr;
    }

    public SimpleExpressionProxy visitUnaryExpressionProxy
      (final UnaryExpressionProxy expr)
      throws VisitorException
    {
      final SimpleExpressionProxy oldsubterm = expr.getSubTerm();
      final SimpleExpressionProxy newsubterm = substitute(oldsubterm);
      if (oldsubterm != newsubterm) {
        final UnaryOperator op = expr.getOperator();
        return mFactory.createUnaryExpressionProxy(op, newsubterm);
      } else {
        return expr;
      }
    }

    //#######################################################################
    //# Data Members
    private SimpleExpressionProxy mVarName;
    private SimpleExpressionProxy mReplacement;

  }


  //#########################################################################
  //# Data Members
  private final ModuleProxyFactory mFactory;
  private final CompilerOperatorTable mOperatorTable;
  private final SimpleExpressionCompiler mSimpleExpressionCompiler;
  private final DNFConverter mDNFConverter;
  private final EFAVariableMap mVariableMap;
  private final OccursCheckVisitor mOccursCheckVisitor;
  private final SubstitutionVisitor mSubstitutionVisitor;

  private boolean mIsFalse;
  private Collection<SimpleExpressionProxy> mOpenLiterals;
  private Collection<SimpleExpressionProxy> mProcessedEquations;

}
