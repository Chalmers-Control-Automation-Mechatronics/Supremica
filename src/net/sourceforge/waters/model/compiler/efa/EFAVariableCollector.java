//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.efa
//# CLASS:   EFAVariableCollector
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler.efa;

import java.util.HashSet;
import java.util.Set;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyAccessorHashMapByContents;
import net.sourceforge.waters.model.base.ProxyAccessorMap;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.dnf.CompiledClause;
import net.sourceforge.waters.model.expr.UnaryOperator;
import net.sourceforge.waters.model.module.AbstractModuleProxyVisitor;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.ModuleProxyVisitor;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.UnaryExpressionProxy;


/**
 * <P>A visitor to collect combinations of EFA variables in guards that
 * need to be split.</P>
 * 
 * <P>If some literal in a guard uses more than one EFA variable, the event
 * needs to be split over the values of one of the variables, before events
 * for the variable automata can be generated. More specifically, a
 * conjunct in a partial guard may contain one variable in its primed and
 * non-primed form, but it may not contain two different variables.</P>
 *
 * <P>This visitor analyses all the literals in a partial guard and
 * computes a set of variable combinations ({@link EFAVariableCombination})
 * representing those combinations of variables that contain more than one
 * variable and need to be split.</P>
 *
 * @author Robi Malik
 */

public class EFAVariableCollector extends AbstractModuleProxyVisitor
{

  //#########################################################################
  //# Constructor
  EFAVariableCollector(final CompilerOperatorTable optable)
  {
    mNextOperator = optable.getNextOperator();
    mVariables = new ProxyAccessorHashMapByContents<SimpleExpressionProxy>();
  }


  //#########################################################################
  //# Invocation
  Set<EFAVariableCombination> collectEFAVariableCombinations
    (final CompiledClause clause)
  {
    final Set<EFAVariableCombination> result =
      new HashSet<EFAVariableCombination>();
    for (final SimpleExpressionProxy literal : clause.getLiterals()) {
      final EFAVariableCombination combination =
        collectEFAVariableCombination(literal);
      if (combination != null) {
        result.add(combination);
      }
    }
    return result;
  }

  EFAVariableCombination collectEFAVariableCombination
    (final SimpleExpressionProxy expr)
  {
    try {
      expr.acceptVisitor(this);
      return EFAVariableCombination.create(mVariables);
    } catch (final VisitorException exception) {
      throw exception.getRuntimeException();
    } finally {
      mVariables.clear();
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.base.ProxyVisitor
  public Object visitProxy(final Proxy proxy)
  {
    return null;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
  public Object visitBinaryExpressionProxy(final BinaryExpressionProxy expr)
    throws VisitorException
  {
    final SimpleExpressionProxy left = expr.getLeft();
    left.acceptVisitor(this);
    final SimpleExpressionProxy right = expr.getLeft();
    right.acceptVisitor(this);
    return null;
  }

  public Object visitIdentifierProxy(final IdentifierProxy ident)
  {
    mVariables.addProxy(ident);
    return null;
  }

  public Object visitUnaryExpressionProxy(final UnaryExpressionProxy expr)
    throws VisitorException
  {
    if (expr.getOperator() == mNextOperator) {
      mVariables.addProxy(expr);
    } else {
      final SimpleExpressionProxy subterm = expr.getSubTerm();
      subterm.acceptVisitor(this);
    }
    return null;
  }


  //#########################################################################
  //# Data Members
  private final UnaryOperator mNextOperator;
  private final ProxyAccessorMap<SimpleExpressionProxy> mVariables;

}
