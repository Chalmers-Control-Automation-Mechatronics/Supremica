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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.dnf.CompiledClause;
import net.sourceforge.waters.model.expr.BinaryOperator;
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
     final EFAVariableMap varmap)
  {
    mFactory = factory;
    mOperatorTable = optable;
    mVariableMap = varmap;
    mSubstitutionVisitor = new SubstitutionVisitor();
  }


  //#########################################################################
  //# Invocation
  CompiledClause propagate(final CompiledClause clause)
  {
    final Collection<SimpleExpressionProxy> literals = clause.getLiterals();
    boolean change = false;
    mIsFalse = false;
    mOpenLiterals = new ArrayList<SimpleExpressionProxy>(literals);
    mProcessedEquations = new LinkedList<SimpleExpressionProxy>();
    while (simplify()) {
      change = true;
    }
    if (mIsFalse) {
      return null;
    } else if (change) {
      final BinaryOperator op = clause.getOperator();
      final int size = mOpenLiterals.size() + mProcessedEquations.size();
      final CompiledClause result = new CompiledClause(op, size);
      result.addAll(mProcessedEquations);
      result.addAll(mOpenLiterals);
      return result;
    } else {
      return clause;
    }
  }


  //#########################################################################
  //# Algorithm
  boolean simplify()
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
      iter.remove();
      mProcessedEquations.add(literal);
      final SimpleExpressionProxy rhs = binary.getRight();
      substitute(lhs, rhs);
      return !mIsFalse;
    }
    return false;
  }

  void substitute(final SimpleExpressionProxy varname,
                  final SimpleExpressionProxy replacement)
  {
    
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
      throws VisitorException
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
  private final EFAVariableMap mVariableMap;
  private final SubstitutionVisitor mSubstitutionVisitor;

  private boolean mIsFalse;
  private List<SimpleExpressionProxy> mOpenLiterals;
  private List<SimpleExpressionProxy> mProcessedEquations;

}
