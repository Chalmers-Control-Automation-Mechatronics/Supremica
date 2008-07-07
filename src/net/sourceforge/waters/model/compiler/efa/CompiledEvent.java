//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.efa
//# CLASS:   CompiledEvent
//###########################################################################
//# $Id: CompiledEvent.java,v 1.1 2008-06-29 04:01:44 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.compiler.efa;

import java.util.Collection;
import java.util.LinkedList;

import net.sourceforge.waters.model.base.ProxyAccessorMap;
import net.sourceforge.waters.model.base.ProxyAccessorHashMapByContents;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.dnf.CompiledClause;
import net.sourceforge.waters.model.compiler.dnf.CompiledNormalForm;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;

import net.sourceforge.waters.xsd.base.EventKind;


class CompiledEvent {

  //#########################################################################
  //# Constructors
  CompiledEvent(final EventDeclProxy decl,
                final CompilerOperatorTable optable)
  {
    mEventDecl = decl;
    mVariables = new ProxyAccessorHashMapByContents<IdentifierProxy>();
    mGuardCollections = new LinkedList<CompiledGuardCollection>();
    final BinaryOperator andop = optable.getAndOperator();
    mAdditionalConjuncts = new CompiledNormalForm(andop);
  }


  //#########################################################################
  //# Simple Access
  EventDeclProxy getEventDecl()
  {
    return mEventDecl;
  }

  EventKind getKind()
  {
    return mEventDecl.getKind();
  }

  void addVariable(final IdentifierProxy ident)
  {
    mVariables.addProxy(ident);
  }

  void addVariables(final Collection<? extends IdentifierProxy> idents)
  {
    mVariables.addAll(idents);
  }

  void addGuardCollection(final CompiledGuardCollection collection)
  {
    final Collection<CompiledGuard> guards = collection.getGuards();
    if (guards.size() == 1) {
      final CompiledGuard guard = guards.iterator().next();
      final CompiledNormalForm cnf = guard.getCNF();
      mAdditionalConjuncts.addAll(cnf);
    } else {
      final Collection<CompiledClause> shared =
        collection.removeSharedConjuncts();
      mAdditionalConjuncts.addAll(shared);
      mGuardCollections.add(collection);
    }
  }


  //#########################################################################
  //# Data Members
  private final EventDeclProxy mEventDecl;
  private final ProxyAccessorMap<IdentifierProxy> mVariables;
  private final Collection<CompiledGuardCollection> mGuardCollections;
  /**
   * Additional conjuncts in CNF.
   * If in an automaton all transitions associated with a particular event
   * have the same guard/action block, then that component does not need to
   * be considered when separating the event. Instead, the constraints
   * imposed by the one guard/action block are stored as additional
   * conjuncts that constrain the possible value combinations for this
   * event.
   */
  private final CompiledNormalForm mAdditionalConjuncts;

}
