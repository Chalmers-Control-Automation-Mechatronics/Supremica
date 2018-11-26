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
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;


/**
 * A compiler-internal representation of an EFSM component originating
 * from a simple component ({@link SimpleComponentProxy}).
 *
 * @author Robi Malik
 */

class EFSMSimpleComponent extends EFSMComponent
{

  //#########################################################################
  //# Constructors
  EFSMSimpleComponent(final SimpleComponentProxy comp,
                      final CompiledRange range)
  {
    super(comp, range);
  }


  //#########################################################################
  //# Simple Access
  @Override
  SimpleComponentProxy getComponentProxy()
  {
    return (SimpleComponentProxy) super.getComponentProxy();
  }

  ComponentKind getKind()
  {
    final SimpleComponentProxy comp = getComponentProxy();
    return comp.getKind();
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.model.compiler.efsm.EFSMComponent
  @Override
  TransitionGroup createTransitionGroup
    (final EFSMEventInstance inst,
     final ConstraintList constraints,
     final EFSMTransitionIteratorFactory factory)
  {
    // TODO Auto-generated method stub
    return null;
  }


  //#########################################################################
  //# Auxiliary Methods
  void initialiseTransitions(final Map<EFSMEventDeclaration,TLongArrayList> map)
  {
    final int size = map.size();
    final Map<TLongArrayList,TransitionGroup> groupMap = new HashMap<>(size);
    mEventMap = new HashMap<>(size);
    for (final Map.Entry<EFSMEventDeclaration,TLongArrayList> entry :
         map.entrySet()) {
      final EFSMEventDeclaration decl = entry.getKey();
      final TLongArrayList transitions = entry.getValue();
      TransitionGroup group;
      if (transitions == null || transitions.size() == 0) {
        group = EMPTY_GROUP;
      } else {
        group = groupMap.get(transitions);
        if (group == null) {
          group = new TransitionGroup(transitions);
          groupMap.put(transitions, group);
        }
      }
      mEventMap.put(decl, group);
    }
  }


  //#########################################################################
  //# Debugging
  @Override
  public String toString()
  {
    return "EFSM " + getKind() + " " + getName();
  }


  //#########################################################################
  //# Data Members
  private Map<EFSMEventDeclaration,TransitionGroup> mEventMap;

}
