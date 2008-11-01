//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.efa
//# CLASS:   EFAVariableMap
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler.efa;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import net.sourceforge.waters.model.base.ProxyAccessor;
import net.sourceforge.waters.model.base.ProxyAccessorByContents;
import net.sourceforge.waters.model.base.ProxyAccessorHashMapByContents;
import net.sourceforge.waters.model.base.ProxyAccessorMap;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.compiler.context.
  UndefinedIdentifierException;
import net.sourceforge.waters.model.expr.UnaryOperator;
import net.sourceforge.waters.model.module.ComponentProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
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

  EFAVariable getVariable(final SimpleExpressionProxy varname)
  {
    init();
    final ProxyAccessor<SimpleExpressionProxy> accessor =
      new ProxyAccessorByContents<SimpleExpressionProxy>(varname);
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


  //#########################################################################
  //# Auxiliary Methods
  private void init()
  {
    if (mMap == null) {
      mMap = new HashMap<ProxyAccessor<SimpleExpressionProxy>,EFAVariable>();
    }
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

  private Map<ProxyAccessor<SimpleExpressionProxy>,EFAVariable> mMap;

}
