//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
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

package net.sourceforge.waters.model.compiler.efa;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.compiler.constraint.ConstraintList;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;


/**
 * A compiler-internal representation of the set of all the transitions
 * associated with a given event in one particular automaton. More than one
 * partial transition ({@link EFAAutomatonTransition}) may be associated to
 * the same event, for different values of the EFA variables.
 *
 * @author Robi Malik
 */

class EFAAutomatonTransitionGroup
  implements Comparable<EFAAutomatonTransitionGroup>
{

  //#########################################################################
  //# Constructors
  EFAAutomatonTransitionGroup(final SimpleComponentProxy comp)
  {
    mComponent = comp;
    mPartialTransitions = new HashMap<ConstraintList,EFAAutomatonTransition>();
    mHasTrueGuard = false;
  }


  //#########################################################################
  //# Simple Access
  SimpleComponentProxy getSimpleComponent()
  {
    return mComponent;
  }

  Collection<ConstraintList> getGuards()
  {
    return mPartialTransitions.keySet();
  }

  Collection<EFAAutomatonTransition> getPartialTransitions()
  {
    return mPartialTransitions.values();
  }

  EFAAutomatonTransition getPartialTransition(final ConstraintList guard)
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

  void addPartialTransition(final ConstraintList guard,
                            final Proxy location)
  {
    final EFAAutomatonTransition trans = createTransition(guard);
    trans.addSource(location);
    mHasTrueGuard |= guard.isTrue();
  }

  void setPartialTransitions(final Collection<EFAAutomatonTransition> parts)
  {
    mPartialTransitions.clear();
    for (final EFAAutomatonTransition trans : parts) {
      final ConstraintList guard = trans.getGuard();
      mPartialTransitions.put(guard, trans);
    }
  }


  //#########################################################################
  //# Overrides for java.lang.Object
  public String toString()
  {
    final StringBuilder buffer = new StringBuilder("{");
    final Iterator<ConstraintList> iter =
      mPartialTransitions.keySet().iterator();
    while (iter.hasNext()) {
      final ConstraintList guard = iter.next();
      buffer.append(guard.toString());
      if (iter.hasNext()) {
        buffer.append("; ");
      }
    }
    buffer.append("}");
    return buffer.toString();
  }


  //#########################################################################
  //# Interface java.lang.Comparable
  public int compareTo(final EFAAutomatonTransitionGroup group)
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
  private EFAAutomatonTransition createTransition(final ConstraintList guard)
  {
    EFAAutomatonTransition trans = mPartialTransitions.get(guard);
    if (trans == null) {
      trans = new EFAAutomatonTransition(guard);
      mPartialTransitions.put(guard, trans);
    }
    return trans;
  }


  //#########################################################################
  //# Data Members
  private final SimpleComponentProxy mComponent;
  private final Map<ConstraintList,EFAAutomatonTransition> mPartialTransitions;
  private boolean mHasTrueGuard;

}








