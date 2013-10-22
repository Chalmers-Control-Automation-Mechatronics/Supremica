//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFSM Analysis
//# PACKAGE: net.sourceforge.waters.model.analysis.efa
//# CLASS:   EFSMVariable
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa.efsm;


import gnu.trove.set.hash.THashSet;

import java.util.BitSet;
import java.util.Collection;

import net.sourceforge.waters.analysis.efa.base.AbstractEFAVariable;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.constraint.ConstraintList;
import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.VariableComponentProxy;


/**
 * A representation of an EFSM variable for use in compositional
 * analysis.
 *
 * @author Robi Malik, Sahar Mohajerani
 */

public class EFSMVariable
  extends AbstractEFAVariable<ConstraintList>
{

  //#########################################################################
  //# Constructors
  EFSMVariable(final VariableComponentProxy var,
               final CompiledRange range,
               final ModuleProxyFactory factory,
               final CompilerOperatorTable op)
  {
    super(var, range, factory, op);
    mTransitionRelations = new THashSet<>();
    mSelfloops = new EFSMEventEncoding();
  }


  //#########################################################################
  //# Simple Access
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

  /**
   * Returns a collection containing all transition relations (EFAs) using this
   * variable.
   */
  protected Collection<EFSMTransitionRelation> getTransitionRelations()
  {
    return mTransitionRelations;
  }

  protected void addTransitionRelation(final EFSMTransitionRelation trans)
  {
    mTransitionRelations.add(trans);
  }

  protected void removeTransitionRelation(final EFSMTransitionRelation trans)
  {
    mTransitionRelations.remove(trans);
  }

  /**
   * Returns the single transition relation using this variable.
   * @return If this variable is used by exactly one transition relation,
   *         that transition relation is returned; otherwise the result
   *         is <CODE>null</CODE>.
   */
  public EFSMTransitionRelation getTransitionRelation()
  {
    final Collection<EFSMTransitionRelation> trans = getTransitionRelations();
    if (trans.size() == 1) {
      return trans.iterator().next();
    } else {
      return null;
    }
  }
  /**
   * Return whether this variable is local.
   * @return <CODE>true</CODE> if the variable occurs in at most one transition
   *         relation.
   */
  public boolean isLocal()
  {
    return mTransitionRelations.size() <= 1;
  }


  //#########################################################################
  //# Data Members
  private final Collection<EFSMTransitionRelation> mTransitionRelations;
  /**
   * Event encoding representing selfloops involving this variable.
   * @see #getSelfloops()
   */
  private EFSMEventEncoding mSelfloops;

}
