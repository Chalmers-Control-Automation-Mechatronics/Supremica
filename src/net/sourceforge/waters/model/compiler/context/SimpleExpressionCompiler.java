//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.context
//# CLASS:   SimpleExpressionCompiler
//###########################################################################
//# $Id: SimpleExpressionCompiler.java,v 1.1 2008-06-16 07:09:51 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.compiler.context;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.AbstractSimpleExpressionSimplifier;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.TypeMismatchException;
import net.sourceforge.waters.model.expr.UnaryOperator;
import net.sourceforge.waters.model.module.AbstractModuleProxyVisitor;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.EnumSetExpressionProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.IndexedIdentifierProxy;
import net.sourceforge.waters.model.module.IntConstantProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.QualifiedIdentifierProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.model.module.UnaryExpressionProxy;


public class SimpleExpressionCompiler
  extends AbstractSimpleExpressionSimplifier
{
 
  //#########################################################################
  //# Constructors
  public SimpleExpressionCompiler(final ModuleProxyFactory factory,
                                  final CompilerOperatorTable optable)
  {
    super(factory);
    mOperatorTable = optable;
    mSimplificationVisitor = new SimplificationVisitor();
    mAtomicVisitor = new AtomicVisitor();
    mRangeVisitor = new RangeVisitor();
    mIsEvaluating = false;
    mContext = null;
  }


  //#########################################################################
  //# Invocation
  public SimpleExpressionProxy simplify(final SimpleExpressionProxy expr,
                                        final BindingContext context)
    throws EvalException
  {
    try {
      mIsEvaluating = false;
      mContext = context;
      return simplify(expr);
    } finally {
      mContext = null;
    }
  }

  public SimpleExpressionProxy eval(final SimpleExpressionProxy expr,
                                    final BindingContext context)
    throws EvalException
  {
    try {
      mIsEvaluating = true;
      mContext = context;
      return simplify(expr);
    } finally {
      mIsEvaluating = false;
      mContext = null;
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.expr.SimpleExpressionSimplifier
  public SimpleExpressionProxy simplify(final SimpleExpressionProxy expr)
    throws EvalException
  {
    try {
      return (SimpleExpressionProxy)
        expr.acceptVisitor(mSimplificationVisitor);
    } catch (final VisitorException exception) {
      final Throwable cause = exception.getCause();
      if (cause instanceof EvalException) {
        throw (EvalException) cause;
      } else {
        throw new WatersRuntimeException(cause);
      }
    }
  }

  public boolean isAtomicValue(final SimpleExpressionProxy expr)
  {
    try {
      return (Boolean) expr.acceptVisitor(mAtomicVisitor);
    } catch (final VisitorException exception) {
      final Throwable cause = exception.getCause();
      throw new WatersRuntimeException(cause);
    }
  }


  //#########################################################################
  //# Specific Access
  public CompiledRange getRangeValue(final SimpleExpressionProxy expr)
    throws EvalException
  {
    return mRangeVisitor.getRangeValue(expr);
  }


  //#########################################################################
  //# Auxiliary Methods
  private SimpleExpressionProxy getBoundExpression(final IdentifierProxy ident)
  {
    if (mContext == null) {
      return null;
    } else {
      return mContext.getBoundExpression(ident);
    }
  }

  //#########################################################################
  //# Inner Class SimplificationVisitor
  private class SimplificationVisitor
    extends AbstractModuleProxyVisitor
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    public SimpleExpressionProxy visitBinaryExpressionProxy
      (final BinaryExpressionProxy expr)
      throws VisitorException
    {
      try {
        final BinaryOperator operator = expr.getOperator();
        return operator.simplify(expr, SimpleExpressionCompiler.this);
      } catch (final EvalException exception) {
        throw wrap(exception);
      }
    }

    public EnumSetExpressionProxy visitEnumSetExpressionProxy
      (final EnumSetExpressionProxy expr)
      throws VisitorException
    {
      final List<SimpleIdentifierProxy> items = expr.getItems();
      for (final SimpleIdentifierProxy item : items) {
        final SimpleExpressionProxy found = getBoundExpression(item);
        if (found == null) {
          if (mContext != null) {
            final ModuleBindingContext modulecontext =
              mContext.getModuleBindingContext();
            modulecontext.addBinding(item, item);
          }
        } else if (found.equalsByContents(item)) {
          // nothing ...
        } else {
          final String name = item.getName();
          final DuplicateIdentifierException exception =
            new DuplicateIdentifierException(name, item);
          throw wrap(exception);
        }
      }
      return expr;
    }

    public SimpleExpressionProxy visitIndexedIdentifierProxy
      (IndexedIdentifierProxy ident)
      throws VisitorException
    {
      final List<SimpleExpressionProxy> origIndexes = ident.getIndexes();
      final int size = origIndexes.size();
      final List<SimpleExpressionProxy> compiledIndexes =
        new ArrayList<SimpleExpressionProxy>(size);
      boolean change = false;
      for (final SimpleExpressionProxy orig : origIndexes) {
        final SimpleExpressionProxy compiled =
          (SimpleExpressionProxy) orig.acceptVisitor(this);
        change |= (orig != compiled);
        compiledIndexes.add(compiled);
      }
      if (change) {
        final String name = ident.getName();
        final ModuleProxyFactory factory = getFactory();
        ident = factory.createIndexedIdentifierProxy(name, compiledIndexes);
      }
      return processIdentifier(ident);
    }

    public IntConstantProxy visitIntConstantProxy(final IntConstantProxy expr)
    {
      return expr;
    }

    public SimpleExpressionProxy visitQualifiedIdentifierProxy
      (QualifiedIdentifierProxy ident)
      throws VisitorException
    {
      try {
        final IdentifierProxy base0 = ident.getBaseIdentifier();
        final SimpleExpressionProxy basev =
          (SimpleExpressionProxy) base0.acceptVisitor(this);
        final IdentifierProxy base1 = getIdentifierValue(basev);
        final IdentifierProxy comp0 = ident.getComponentIdentifier();
        final SimpleExpressionProxy compv =
          (SimpleExpressionProxy) comp0.acceptVisitor(this);
        final IdentifierProxy comp1 = getIdentifierValue(compv);
        if (base0 != base1 || comp0 != comp1) {
          final ModuleProxyFactory factory = getFactory();
          ident = factory.createQualifiedIdentifierProxy(base1, comp1);
        }
        return processIdentifier(ident);
      } catch (final TypeMismatchException exception) {
        throw wrap(exception);
      }
    }

    public SimpleExpressionProxy visitSimpleIdentifierProxy
      (final SimpleIdentifierProxy ident)
      throws VisitorException
    {
      return processIdentifier(ident);
    }

    public SimpleExpressionProxy visitUnaryExpressionProxy
      (final UnaryExpressionProxy expr)
      throws VisitorException
    {
      try {
        final UnaryOperator operator = expr.getOperator();
        return operator.simplify(expr, SimpleExpressionCompiler.this);
      } catch (final EvalException exception) {
        throw wrap(exception);
      }
    }

    //#######################################################################
    //# Auxiliary Methods
    private SimpleExpressionProxy processIdentifier
      (final IdentifierProxy ident)
      throws VisitorException
    {
      final SimpleExpressionProxy bound = getBoundExpression(ident);
      if (bound != null) {
        return bound;
      } else if (mIsEvaluating) {
        final UndefinedIdentifierException exception =
          new UndefinedIdentifierException(ident);
        throw wrap(exception);
      } else {
        return ident;
      }
    }

  }


  //#########################################################################
  //# Inner Class AtomicVisitor
  private class AtomicVisitor
    extends AbstractModuleProxyVisitor
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    public Boolean visitBinaryExpressionProxy(final BinaryExpressionProxy expr)
    {
      final BinaryOperator operator = expr.getOperator();
      if (operator == mOperatorTable.getRangeOperator()) {
        final SimpleExpressionProxy lhs = expr.getLeft();
        final SimpleExpressionProxy rhs = expr.getRight();
        return (lhs instanceof IntConstantProxy &&
                rhs instanceof IntConstantProxy);
      } else {
        return false;
      }
    }

    public Boolean visitEnumSetExpressionProxy
      (final EnumSetExpressionProxy expr)
    {
      final List<SimpleIdentifierProxy> items = expr.getItems();
      for (final SimpleIdentifierProxy item : items) {
        final SimpleExpressionProxy bound = getBoundExpression(item);
        if (!item.equalsByContents(bound)) {
          return false;
        }
      }
      return true;
    }

    public Boolean visitIdentifierProxy(final IdentifierProxy ident)
    {
      final SimpleExpressionProxy bound = getBoundExpression(ident);
      return ident.equalsByContents(bound);
    }

    public Boolean visitIntConstantProxy(final IntConstantProxy expr)
    {
      return true;
    }

    public Boolean visitSimpleExpressionProxy(final SimpleExpressionProxy expr)
    {
      return false;
    }

  }


  //#########################################################################
  //# Inner Class AtomicVisitor
  private class RangeVisitor
    extends AbstractModuleProxyVisitor
  {

    //#######################################################################
    //# Invocation
    private CompiledRange getRangeValue(final SimpleExpressionProxy expr)
      throws EvalException
    {
      try {
        return (CompiledRange) expr.acceptVisitor(this);
      } catch (final VisitorException exception) {
        final Throwable cause = exception.getCause();
        if (cause instanceof EvalException) {
          throw (EvalException) cause;
        } else {
          throw exception.getRuntimeException();
        }
      }
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    public CompiledRange visitBinaryExpressionProxy
      (final BinaryExpressionProxy expr)
      throws VisitorException
    {
      try {
        final BinaryOperator operator = expr.getOperator();
        if (operator == mOperatorTable.getRangeOperator()) {
          final SimpleExpressionProxy lhsExpr = expr.getLeft();
          final int lhsInt = getIntValue(lhsExpr);
          final SimpleExpressionProxy rhsExpr = expr.getRight();
          final int rhsInt = getIntValue(rhsExpr);
          return new CompiledIntRange(lhsInt, rhsInt);
        } else {
          return visitSimpleExpressionProxy(expr);
        }
      } catch (final TypeMismatchException exception) {
        throw wrap(exception);
      }
    }

    public CompiledRange visitEnumSetExpressionProxy
      (final EnumSetExpressionProxy expr)
    {
      final List<SimpleIdentifierProxy> items = expr.getItems();
      return new CompiledEnumRange(items);
    }

    public CompiledRange visitSimpleExpressionProxy
      (final SimpleExpressionProxy expr)
      throws VisitorException
    {
      final TypeMismatchException exception =
        new TypeMismatchException(expr, "RANGE");
      throw wrap(exception);
    }

  }


  //#########################################################################
  //# Data Members
  private final CompilerOperatorTable mOperatorTable;
  private final SimplificationVisitor mSimplificationVisitor;
  private final AtomicVisitor mAtomicVisitor;
  private final RangeVisitor mRangeVisitor;

  private boolean mIsEvaluating;
  private BindingContext mContext;

}
