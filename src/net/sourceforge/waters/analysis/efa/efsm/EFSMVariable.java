//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

package net.sourceforge.waters.analysis.efa.efsm;


import gnu.trove.set.hash.THashSet;

import java.util.BitSet;
import java.util.Collection;
import java.util.Set;

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
  protected Set<EFSMTransitionRelation> getTransitionRelations()
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
  private final Set<EFSMTransitionRelation> mTransitionRelations;
  /**
   * Event encoding representing selfloops involving this variable.
   * @see #getSelfloops()
   */
  private EFSMEventEncoding mSelfloops;

}
