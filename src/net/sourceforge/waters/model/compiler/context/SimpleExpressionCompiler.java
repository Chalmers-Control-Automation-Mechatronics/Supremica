//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.context
//# CLASS:   SimpleExpressionCompiler
//###########################################################################
//# $Id: SimpleExpressionCompiler.java,v 1.3 2008-06-19 21:26:59 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.compiler.context;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import net.sourceforge.waters.model.base.VisitorException;
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
import net.sourceforge.waters.model.module.ModuleProxyCloner;
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
    this(factory, optable, optable.getExpressionComparator());
  }
    
  public SimpleExpressionCompiler
    (final ModuleProxyFactory factory,
     final CompilerOperatorTable optable,
     final Comparator<SimpleExpressionProxy> comparator)
  {
    super(factory);
    mOperatorTable = optable;
    mComparator = comparator;
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
        throw exception.getRuntimeException();
      }
    }
  }

  public boolean isAtomicValue(final SimpleExpressionProxy expr)
  {
    try {
      return (Boolean) expr.acceptVisitor(mAtomicVisitor);
    } catch (final VisitorException exception) {
      throw exception.getRuntimeException();
    }
  }

  public Comparator<SimpleExpressionProxy> getExpressionComparator()
  {
    return mComparator;
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

  private boolean isEnumAtom(final IdentifierProxy ident)
  {
    if (mContext == null) {
      return false;
    } else {
      return mContext.isEnumAtom(ident);
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
      final int numitems = items.size();
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
      final ModuleProxyCloner cloner = getCloner();
      return (EnumSetExpressionProxy) cloner.getClone(expr);
    }

    public SimpleExpressionProxy visitIndexedIdentifierProxy
      (final IndexedIdentifierProxy ident)
      throws VisitorException
    {
      final String name = ident.getName();
      final List<SimpleExpressionProxy> origIndexes = ident.getIndexes();
      final int size = origIndexes.size();
      final List<SimpleExpressionProxy> compiledIndexes =
        new ArrayList<SimpleExpressionProxy>(size);
      for (final SimpleExpressionProxy orig : origIndexes) {
        final SimpleExpressionProxy compiled =
          (SimpleExpressionProxy) orig.acceptVisitor(this);
        compiledIndexes.add(compiled);
      }
      final ModuleProxyFactory factory = getFactory();
      final IndexedIdentifierProxy copy =
        factory.createIndexedIdentifierProxy(name, compiledIndexes);
      try {
        return processIdentifier(copy, true);
      } catch (final UndefinedIdentifierException exception) {
        exception.provideLocation(ident);
        throw wrap(exception);
      }
    }

    public IntConstantProxy visitIntConstantProxy(final IntConstantProxy expr)
    {
      final ModuleProxyCloner cloner = getCloner();
      return (IntConstantProxy) cloner.getClone(expr);
    }

    public SimpleExpressionProxy visitQualifiedIdentifierProxy
      (final QualifiedIdentifierProxy ident)
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
        final ModuleProxyFactory factory = getFactory();
        final QualifiedIdentifierProxy copy =
          factory.createQualifiedIdentifierProxy(base1, comp1);
        return processIdentifier(copy, true);
      } catch (final TypeMismatchException exception) {
        throw wrap(exception);
      } catch (final UndefinedIdentifierException exception) {
        exception.provideLocation(ident);
        throw wrap(exception);
      }
    }

    public SimpleExpressionProxy visitSimpleIdentifierProxy
      (final SimpleIdentifierProxy ident)
      throws VisitorException
    {
      try {
        return processIdentifier(ident, false);
      } catch (final UndefinedIdentifierException exception) {
        exception.provideLocation(ident);
        throw wrap(exception);
      }
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
      (final IdentifierProxy ident, final boolean alreadyCloned)
      throws UndefinedIdentifierException
    {
      final SimpleExpressionProxy bound = getBoundExpression(ident);
      if (bound != null) {
        final ModuleProxyCloner cloner = getCloner();
        return (SimpleExpressionProxy) cloner.getClone(bound);
      } else if (mIsEvaluating) {
        throw new UndefinedIdentifierException(ident);
      } else if (alreadyCloned) {
        return ident;
      } else {
        final ModuleProxyCloner cloner = getCloner();
        return (IdentifierProxy) cloner.getClone(ident);
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

    public Boolean visitIntConstantProxy(final IntConstantProxy expr)
    {
      return true;
    }

    public Boolean visitSimpleExpressionProxy(final SimpleExpressionProxy expr)
    {
      return false;
    }

    public Boolean visitSimpleIdentifierProxy
      (final SimpleIdentifierProxy ident)
    {
      return isEnumAtom(ident);
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
  private final Comparator<SimpleExpressionProxy> mComparator;
  private final SimplificationVisitor mSimplificationVisitor;
  private final AtomicVisitor mAtomicVisitor;
  private final RangeVisitor mRangeVisitor;

  private boolean mIsEvaluating;
  private BindingContext mContext;

}
