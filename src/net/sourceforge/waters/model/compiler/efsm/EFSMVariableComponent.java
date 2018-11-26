//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2018 Robi Malik
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

package net.sourceforge.waters.model.compiler.efsm;

import gnu.trove.list.array.TLongArrayList;

import java.util.HashMap;
import java.util.Map;

import net.sourceforge.waters.model.compiler.constraint.ConstraintList;
import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.VariableComponentProxy;


/**
 * A compiler-internal representation of an EFSM component originating
 * from a variable ({@link VariableComponentProxy}).
 *
 * @author Robi Malik
 */

class EFSMVariableComponent extends EFSMComponent
{

  //#########################################################################
  //# Constructors
  EFSMVariableComponent(final VariableComponentProxy var,
                        final CompiledRange range)
  {
    super(var, range);
    mTransitionGroupMap = new HashMap<>();
  }


  //#########################################################################
  //# Simple Access
  @Override
  VariableComponentProxy getComponentProxy()
  {
    return (VariableComponentProxy) super.getComponentProxy();
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.model.compiler.efsm.EFSMComponent
  @Override
  TransitionGroup createTransitionGroup(final EFSMEventInstance event,
                                        final ConstraintList constraints,
                                        final EFSMTransitionIteratorFactory factory)
    throws EvalException
  {
    TransitionGroup group = getTransitionGroup(constraints);
    if (group == null) {
      final EFSMTransitionIterator iter =
        factory.createTransitionIterator(this, constraints);
      if (iter == null) {
        group = addEmptyTransitionGroup(constraints);
      } else {
        final TLongArrayList transitions =
          new TLongArrayList(iter.getEstimatedSize());
        while (iter.advance()) {
          final SimpleExpressionProxy source = iter.getCurrentSourceState();
          final SimpleExpressionProxy target = iter.getCurrentTargetState();
          final long code = getTransitionCode(source, target);
          transitions.add(code);
        }
        group = addTransitionGroup(constraints, transitions);
      }
    }
    if (group.isEmpty()) {
      return null;
    } else {
      return group;
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private TransitionGroup getTransitionGroup(final ConstraintList constraints)
  {
    return mTransitionGroupMap.get(constraints);
  }

  private TransitionGroup addTransitionGroup(final ConstraintList constraints,
                                             final TLongArrayList transitions)
  {
    if (transitions.size() == 0) {
      return addEmptyTransitionGroup(constraints);
    } else {
      final TransitionGroup group = new TransitionGroup(transitions);
      mTransitionGroupMap.put(constraints, group);
      return group;
    }
  }

  private TransitionGroup addEmptyTransitionGroup(final ConstraintList constraints)
  {
    mTransitionGroupMap.put(constraints, EMPTY_GROUP);
    return EMPTY_GROUP;
  }


  //#########################################################################
  //# Debugging
  @Override
  public String toString()
  {
    return "EFSM variable " + getName();
  }


  //#########################################################################
  //# Data Members
  private final Map<ConstraintList,TransitionGroup> mTransitionGroupMap;

}
