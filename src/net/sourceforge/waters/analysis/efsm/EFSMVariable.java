//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFSM Analysis
//# PACKAGE: net.sourceforge.waters.model.analysis.efa
//# CLASS:   EFSMVariable
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efsm;

import gnu.trove.set.hash.THashSet;

import java.util.BitSet;
import java.util.Collection;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.constraint.ConstraintList;
import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.expr.UnaryOperator;
import net.sourceforge.waters.model.module.ComponentProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.ModuleProxyCloner;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.VariableComponentProxy;


/**
 * A representation of an EFSM variable for use in compositional
 * analysis.
 *
 * @author Robi Malik, Sahar Mohajerani
 */

public class EFSMVariable implements Comparable<EFSMVariable> {

  //#########################################################################
  //# Constructors
  EFSMVariable(final VariableComponentProxy var,
               final CompiledRange range,
               final ModuleProxyFactory factory,
               final CompilerOperatorTable op)
  {
    mComponent = var;
    mRange = range;
    final ModuleProxyCloner cloner = factory.getCloner();
    final IdentifierProxy ident = var.getIdentifier();
    mVariableName = (SimpleExpressionProxy) cloner.getClone(ident);
    final SimpleExpressionProxy temp = (SimpleExpressionProxy) cloner.getClone(ident);
    final UnaryOperator next = op.getNextOperator();
    mPrimedVariableName = factory.createUnaryExpressionProxy(next, temp);
    mInitialStatePredicate =
      (SimpleExpressionProxy) cloner.getClone(var.getInitialStatePredicate());
    mTransitionRelations = new THashSet<EFSMTransitionRelation>();
    mSelfloops = new EFSMEventEncoding();
  }


  //#########################################################################
  //# Overrides for java.lang.Object
  @Override
  public String toString()
  {
    return mVariableName.toString();
  }


  //#########################################################################
  //# Interface java.lang.Comparable<EFSMVariable>
  @Override
  public int compareTo(final EFSMVariable var)
  {
    return mComponent.compareTo(var.mComponent);
  }


  //#########################################################################
  //# Simple Access
  ComponentProxy getComponent()
  {
    return mComponent;
  }

  public CompiledRange getRange()
  {
    return mRange;
  }

  String getName()
  {
    return mVariableName.toString();
  }

  SimpleExpressionProxy getVariableName()
  {
    return mVariableName;
  }

  SimpleExpressionProxy getPrimedVariableName()
  {
    return mPrimedVariableName;
  }

  public SimpleExpressionProxy getInitialStatePredicate()
  {
    return mInitialStatePredicate;
  }


  /**
   * Gets an event encoding representing selfloops involving this variable.
   * These are updates that were found as selfloops in all states of some
   * EFSM. These updates need to be considered as always enabled when
   * unfolding the variable. Logically, it can be assumed that there is
   * an EFSM for each selfloop update, containing just a single selfloop.
   */
  public EFSMEventEncoding getSelfloops()
  {
    return mSelfloops;
  }

  /**
   * Returns a collection containing all transition relations (EFSMs)
   * using this variable.
   */
  public Collection<EFSMTransitionRelation> getTransitionRelations()
  {
    return mTransitionRelations;
  }

  /**
   * Returns the single transition relation using this variable.
   * @return If this variable is used by exactly one transition relation,
   *         that transition relation is returned; otherwise the result
   *         is <CODE>null</CODE>.
   */
  public EFSMTransitionRelation getTransitionRelation()
  {
    if (mTransitionRelations.size() == 1) {
      return mTransitionRelations.iterator().next();
    } else {
      return null;
    }
  }

  public void addSelfloop(final ConstraintList update)
  {
    mSelfloops.createEventId(update);
  }

  /**
   * Removes the given updates from the selfloops recorded for this variable.
   * This method rebuilds the selfloop event encoding, so event IDs will
   * become invalid.
   */
  public void removeSelfloops(final Collection<ConstraintList> updates)
  {
    final int size = mSelfloops.size();
    final BitSet victims = new BitSet(size);
    for (final ConstraintList update : updates) {
      final int e = mSelfloops.getEventId(update);
      victims.set(e);
    }
    final EFSMEventEncoding newSelfloops =
      new EFSMEventEncoding(size - updates.size());
    for (int e = EventEncoding.NONTAU; e < size; e++) {
      if (!victims.get(e)) {
        final ConstraintList update = mSelfloops.getUpdate(e);
        newSelfloops.createEventId(update);
      }
    }
    mSelfloops = newSelfloops;
  }

  public void addTransitionRelation(final EFSMTransitionRelation trans)
  {
    mTransitionRelations.add(trans);
  }

  public void removeTransitionRelation(final EFSMTransitionRelation trans)
  {
    mTransitionRelations.remove(trans);
  }


  /**
   * Return whether this variable is local.
   * @return <CODE>true</CODE> if the variable occurs in at most one
   *         transition relation.
   */
  public boolean isLocal()
  {
    return mTransitionRelations.size() <= 1;
  }


  //#########################################################################
  //# Data Members
  private final ComponentProxy mComponent;
  private final CompiledRange mRange;
  private final SimpleExpressionProxy mVariableName;
  private final SimpleExpressionProxy mPrimedVariableName;
  private final SimpleExpressionProxy mInitialStatePredicate;

  /**
   * Event encoding representing selfloops involving this variable.
   * @see #getSelfloops()
   */
  private EFSMEventEncoding mSelfloops;
  /**
   * Collection of transition relations (EFSM) using this variable.
   */
  private final Collection<EFSMTransitionRelation> mTransitionRelations;

}
