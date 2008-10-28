//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.efa
//# CLASS:   EFATransitionGroup
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler.efa;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.sourceforge.waters.model.base.ProxyAccessorHashMapByContents;
import net.sourceforge.waters.model.base.ProxyAccessorMap;
import net.sourceforge.waters.model.compiler.dnf.CompiledClause;
import net.sourceforge.waters.model.compiler.dnf.CompiledNormalForm;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;


/**
 * A compiler-internal representation of the set of all the transitions
 * associated with a given event in one particular automaton. More than one
 * partial transition ({@link EFATransition}) may be associated to the same
 * event, for different values of the EFA variables.
 *
 * @author Robi Malik
 */

class EFATransitionGroup
{

  //#########################################################################
  //# Constructors
  EFATransitionGroup()
  {
    mPartialTransitions = new HashMap<CompiledClause,EFATransition>();
    mGuards = new ProxyAccessorHashMapByContents<SimpleExpressionProxy>();
  }


  //#########################################################################
  //# Simple Access
  Collection<SimpleExpressionProxy> getGuards()
  {
    return mGuards == null ? null : mGuards.values();
  }

  void addTransitions(final CompiledGuard guard,
                      final IdentifierProxy label)
  {
    final CompiledNormalForm dnf = guard.getDNF();
    if (dnf.isTrue()) {
      final CompiledClause clause = dnf.getClauses().iterator().next();
      addTransition(clause, label);
      mGuards = null;
    } else {
      final SimpleExpressionProxy expr = guard.getExpression();
      for (final CompiledClause clause : dnf.getClauses()) {
        addTransition(clause, label);
      }
      mGuards.addProxy(expr);
    }
  }

  void addTransition(final CompiledClause clause, final IdentifierProxy label)
  {
    EFATransition trans = mPartialTransitions.get(clause);
    if (trans == null) {
      trans = new EFATransition(clause);
      mPartialTransitions.put(clause, trans);
    }
    trans.addSourceLabel(label);
  }


  //#########################################################################
  //# Data Members
  private final Map<CompiledClause,EFATransition> mPartialTransitions;
  private ProxyAccessorMap<SimpleExpressionProxy> mGuards;

}
