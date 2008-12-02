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

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.compiler.constraint.ConstraintList;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;


/**
 * A compiler-internal representation of the set of all the transitions
 * associated with a given event in one particular automaton. More than one
 * partial transition ({@link EFATransition}) may be associated to the same
 * event, for different values of the EFA variables.
 *
 * @author Robi Malik
 */

class EFATransitionGroup implements Comparable<EFATransitionGroup>
{

  //#########################################################################
  //# Constructors
  EFATransitionGroup(final SimpleComponentProxy comp)
  {
    mComponent = comp;
    mPartialTransitions = new HashMap<ConstraintList,EFATransition>();
    mHasTrueGuard = false;
  }


  //#########################################################################
  //# Simple Access
  Collection<ConstraintList> getGuards()
  {
    return mPartialTransitions.keySet();
  }

  Collection<EFATransition> getPartialTransitions()
  {
    return mPartialTransitions.values();
  }

  EFATransition getPartialTransition(final ConstraintList guard)
  {
    return mPartialTransitions.get(guard);
  }

  boolean isEmpty()
  {
    return mPartialTransitions.isEmpty();
  }

  boolean isTrivial()
  {
    return mPartialTransitions.size() == 1 && mHasTrueGuard;
  }

  boolean hasTrueGuard()
  {
    return mHasTrueGuard;
  }

  void addTransition(final ConstraintList guard,
                     final Proxy location)
  {
    addTransition(guard, null, location);
  }

  void addTransition(final ConstraintList guard,
                     final NodeProxy node,
                     final Proxy location)
  {
    final EFATransition trans = createTransition(guard);
    trans.addSource(node, location);
    mHasTrueGuard |= guard.isTrue();
  }

  void replaceTransition(final ConstraintList victim,
                         final ConstraintList replacement)
  {
    final EFATransition oldtrans = mPartialTransitions.get(victim);
    mPartialTransitions.remove(victim);
    final EFATransition newtrans = createTransition(replacement);
    newtrans.addSources(oldtrans);
  }

  void replaceTransition(final ConstraintList victim,
                         final Collection<ConstraintList> replacements)
  {
    final EFATransition oldtrans = mPartialTransitions.get(victim);
    mPartialTransitions.remove(victim);
    for (final ConstraintList replacement : replacements) {
      final EFATransition newtrans = createTransition(replacement);
      newtrans.addSources(oldtrans);
    }
  }


  //#########################################################################
  //# Interface java.lang.Comparable
  public int compareTo(final EFATransitionGroup group)
  {
    final int numclauses1 = mPartialTransitions.size();
    final int numclauses2 = group.mPartialTransitions.size();
    if (numclauses1 != numclauses2) {
      return numclauses1 - numclauses2;
    }
    final int numstates1 = mComponent.getGraph().getNodes().size();
    final int numstates2 = group.mComponent.getGraph().getNodes().size();
    if (numstates1 != numstates2) {
      return numstates1 - numstates2;
    }
    final IdentifierProxy ident1 = mComponent.getIdentifier();
    final IdentifierProxy ident2 = group.mComponent.getIdentifier();
    return ident1.compareTo(ident2);
  }


  //#########################################################################
  //# Auxiliary Methods
  private EFATransition createTransition(final ConstraintList guard)
  {
    EFATransition trans = mPartialTransitions.get(guard);
    if (trans == null) {
      trans = new EFATransition(mComponent, guard);
      mPartialTransitions.put(guard, trans);
    }
    return trans;
  }


  //#########################################################################
  //# Data Members
  private final SimpleComponentProxy mComponent;
  private final Map<ConstraintList,EFATransition> mPartialTransitions;
  private boolean mHasTrueGuard;

}
