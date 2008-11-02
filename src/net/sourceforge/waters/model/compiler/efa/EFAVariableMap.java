//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.efa
//# CLASS:   EFAVariableMap
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler.efa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.model.base.ProxyAccessor;
import net.sourceforge.waters.model.base.ProxyAccessorByContents;
import net.sourceforge.waters.model.base.ProxyAccessorHashMapByContents;
import net.sourceforge.waters.model.base.ProxyAccessorMap;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.compiler.context.
  UndefinedIdentifierException;
import net.sourceforge.waters.model.expr.UnaryOperator;
import net.sourceforge.waters.model.module.AbstractModuleProxyVisitor;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.ComponentProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.IndexedIdentifierProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.QualifiedIdentifierProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.UnaryExpressionProxy;


/**
 * A collection of EFA variables used by the EFA compiler ({@link
 * EFACompiler}). This symbol table contains all variables mantained by the
 * EFA compiler, i.e., the variables that occur in guards and represent the
 * current state of EFA components such as variables ({@link
 * net.sourceforge.waters.model.module.VariableComponentProxy
 * VariableComponentProxy}) or automata {@link
 * net.sourceforge.waters.model.module.SimpleComponentProxy
 * SimpleComponentProxy}). It maps expressions ({@link
 * SimpleExpressionProxy}) representing variable names to variable objects
 * ({@link EFAVariable}) containing the the computed range of its state
 * space. The table contains entries for the current and the next state of
 * each variable.
 *
 * @see EFACompiler
 * @author Robi Malik
 */

class EFAVariableMap {

  //#########################################################################
  //# Constructors
  EFAVariableMap(final ModuleProxyFactory factory,
                 final CompilerOperatorTable optable)
  {
    mFactory = factory;
    mOperatorTable = optable;
    mComparator = new EFAExpressionComparator();
    mCollector = new EFAVariableCollector();
    mMap = null;
  }


  //#########################################################################
  //# Simple Access
  void clear()
  {
    mMap = null;
  }

  void reset(final int size)
  {
    mMap = new HashMap<ProxyAccessor<SimpleExpressionProxy>,EFAVariable>(size);
  }

  int size()
  {
    return mMap.size();
  }

  EFAVariable getVariable(final SimpleExpressionProxy varname)
  {
    final ProxyAccessor<SimpleExpressionProxy> accessor =
      new ProxyAccessorByContents<SimpleExpressionProxy>(varname);
    return getVariable(accessor);
  }

  EFAVariable getVariable(final ProxyAccessor<SimpleExpressionProxy> accessor)
  {
    init();
    return mMap.get(accessor);
  }

  void createVariables(final ComponentProxy comp,
                       final IdentifierProxy ident,
                       final CompiledRange range)
  {
    init();
    final ProxyAccessor<SimpleExpressionProxy> curaccessor =
      new ProxyAccessorByContents<SimpleExpressionProxy>(ident);
    final EFAVariable curvar = new EFAVariable(comp, ident, range);
    mMap.put(curaccessor, curvar);
    final UnaryOperator nextop = mOperatorTable.getNextOperator();
    final UnaryExpressionProxy nextident =
      mFactory.createUnaryExpressionProxy(nextop, ident);
    final ProxyAccessor<SimpleExpressionProxy> nextaccessor =
      new ProxyAccessorByContents<SimpleExpressionProxy>(nextident);
    final EFAVariable nextvar = new EFAVariable(comp, nextident, range);
    mMap.put(nextaccessor, nextvar);
  }

  void checkIdentifier(final IdentifierProxy ident)
    throws UndefinedIdentifierException
  {
    init();
    final ProxyAccessor<SimpleExpressionProxy> accessor =
      new ProxyAccessorByContents<SimpleExpressionProxy>(ident);
    if (!mMap.containsKey(accessor)) {
      throw new UndefinedIdentifierException(ident, "variable");
    }
  }

  Comparator<SimpleExpressionProxy> getExpressionComparator()
  {
    return mComparator;
  }

  Collection<EFAVariable> collectVariables(final SimpleExpressionProxy expr)
  {
    return mCollector.collect(expr);
  }


  //#########################################################################
  //# Auxiliary Methods
  private void init()
  {
    if (mMap == null) {
      mMap = new HashMap<ProxyAccessor<SimpleExpressionProxy>,EFAVariable>();
    }
  }


  //#########################################################################
  //# Inner Class EFAVariableCollector
  private class EFAVariableCollector
    extends AbstractModuleProxyVisitor
  {

    //#######################################################################
    //# Constructor
    private EFAVariableCollector()
    {
      mMap = new HashMap<ProxyAccessor<SimpleExpressionProxy>,EFAVariable>();
    }

    //#######################################################################
    //# Invocation
    Collection<EFAVariable> collect(final SimpleExpressionProxy expr)
    {
      try {
        process(expr);
        final Collection<EFAVariable> values = mMap.values();
        return new ArrayList<EFAVariable>(values);
      } catch (final VisitorException exception) {
        throw exception.getRuntimeException();
      } finally {
        mMap.clear();
      }
    }


    //#######################################################################
    //# Auxiliary Methods
    void process(final SimpleExpressionProxy expr)
      throws VisitorException
    {
      final ProxyAccessor<SimpleExpressionProxy> accessor =
        new ProxyAccessorByContents<SimpleExpressionProxy>(expr);
      final EFAVariable var = getVariable(accessor);
      if (var == null) {
        expr.acceptVisitor(this);
      } else {
        mMap.put(accessor, var);
      }
    }


    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    public Object visitBinaryExpressionProxy(final BinaryExpressionProxy expr)
      throws VisitorException
    {
      final SimpleExpressionProxy lhs = expr.getLeft();
      process(lhs);
      final SimpleExpressionProxy rhs = expr.getRight();
      process(rhs);
      return null;
    }

    public Object visitIndexedIdentifierProxy
      (final IndexedIdentifierProxy ident)
      throws VisitorException
    {
      final List<SimpleExpressionProxy> indexes = ident.getIndexes();
      for (final SimpleExpressionProxy index : indexes) {
        process(index);
      }
      return null;
    }

    public Object visitQualifiedIdentifierProxy
      (final QualifiedIdentifierProxy ident)
      throws VisitorException
    {
      final IdentifierProxy base = ident.getBaseIdentifier();
      base.acceptVisitor(this);
      final IdentifierProxy comp = ident.getComponentIdentifier();
      return comp.acceptVisitor(this);
    }

    public Object visitSimpleExpressionProxy
      (final SimpleExpressionProxy expr)
    {
      return null;
    }

    public Object visitUnaryExpressionProxy
      (final UnaryExpressionProxy expr)
      throws VisitorException
    {
      final SimpleExpressionProxy subterm = expr.getSubTerm();
      process(subterm);
      return null;
    }

    //#######################################################################
    //# Data Members
    private final Map<ProxyAccessor<SimpleExpressionProxy>,EFAVariable> mMap;

  }


  //#########################################################################
  //# Inner Class EFAExpressionComparator
  private class EFAExpressionComparator
    implements Comparator<SimpleExpressionProxy>
  {

    //#######################################################################
    //# Interface java.util.Comparator
    public int compare(final SimpleExpressionProxy expr1,
                       final SimpleExpressionProxy expr2)
    {
      final EFAVariable var1 = getVariable(expr1);
      final EFAVariable var2 = getVariable(expr2);
      if (var1 == null) {
        if (var2 == null) {
          final Comparator<SimpleExpressionProxy> comparator =
            mOperatorTable.getExpressionComparator();
          return comparator.compare(expr1, expr2);
        } else {
          return 1;
        }
      } else {
        if (var2 == null) {
          return -1;
        } else {
          return var1.compareTo(var2);
        }
      }
    }

  }


  //#########################################################################
  //# Data Members
  private final ModuleProxyFactory mFactory;
  private final CompilerOperatorTable mOperatorTable;
  private final EFAExpressionComparator mComparator;
  private final EFAVariableCollector mCollector;

  private Map<ProxyAccessor<SimpleExpressionProxy>,EFAVariable> mMap;

}
