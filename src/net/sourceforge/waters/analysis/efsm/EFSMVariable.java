//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFSM Analysis
//# PACKAGE: net.sourceforge.waters.model.analysis.efa
//# CLASS:   EFSMVariable
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efsm;


import java.util.BitSet;
import java.util.Collection;

import net.sourceforge.waters.analysis.efa.AbstractEFAVariable;
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

  @Override
  @SuppressWarnings("unchecked")
  public Collection<EFSMTransitionRelation> getTransitionRelations()
  {
    return (Collection<EFSMTransitionRelation>) super.getTransitionRelations();
  }

  @Override
  public EFSMTransitionRelation getTransitionRelation()
  {
    return (EFSMTransitionRelation) super.getTransitionRelation();
  }


  //#########################################################################
  //# Data Members
  /**
   * Event encoding representing selfloops involving this variable.
   * @see #getSelfloops()
   */
  private EFSMEventEncoding mSelfloops;

}
