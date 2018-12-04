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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.model.compiler.constraint.ConstraintList;
import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;


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
    this(comp, range, null);
  }

  private EFSMSimpleComponent(final SimpleComponentProxy comp,
                              final CompiledRange range,
                              final EFSMSimpleComponent plantifiedSpec)
  {
    super(comp, range);
    mPlantifiedSpec = plantifiedSpec;
    mPlantificiationNeeded = false;
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
    if (mPlantifiedSpec != null) {
      return ComponentKind.PLANT;
    } else {
      final SimpleComponentProxy comp = getComponentProxy();
      return comp.getKind();
    }
  }

  @Override
  boolean isPlantificationNeeded()
  {
    return mPlantificiationNeeded;
  }

  String getPlantificationSuffix()
  {
    if (mPlantifiedSpec != null) {
      return ":plant";
    } else {
      return null;
    }
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.model.compiler.efsm.EFSMComponent
  @Override
  TransitionGroup createTransitionGroup
    (final EFSMEventInstance inst,
     final ConstraintList constraints,
     final EFSMTransitionIteratorFactory factory)
    throws EvalException
  {
    final EFSMEventDeclaration decl = inst.getEFSMEventDeclaration();
    final TransitionGroup master = mEventMap.get(decl);
    if (master != null && master.isEmpty()) {
      return isConsideredControllable(decl) ? null : master;
    }
    final GroupKey key = new GroupKey(master, constraints);
    TransitionGroup group = getTransitionGroup(key);
    if (group == null) {
      final TLongArrayList transitions;
      if (master ==  null) {
        final int numStates = getRange().size();
        transitions = new TLongArrayList(numStates);
        for (int s = 0; s < numStates; s++) {
          final long transition = getTransitionCode(s, s);
          if (factory.isValidTransition(this, constraints, transition)) {
            transitions.add(transition);
          }
        }
      } else {
        final int numTransitions = master.getNumberOfTransitions();
        transitions = new TLongArrayList(numTransitions);
        for (int i = 0; i < numTransitions; i++) {
          final long transition = master.getTransition(i);
          if (factory.isValidTransition(this, constraints, transition)) {
            transitions.add(transition);
          }
        }
      }
      group = addTransitionGroup(key, transitions);
    }
    if (group.isEmpty() && isConsideredControllable(decl)) {
      return null;
    } else {
      return group;
    }
  }

  @Override
  TransitionGroup getTransitionGroup(final EFSMEventInstance inst)
  {
    final TransitionGroup group = super.getTransitionGroup(inst);
    if (group != null) {
      return group;
    } else {
      final EFSMEventDeclaration decl = inst.getEFSMEventDeclaration();
      return mEventMap.get(decl);
    }
  }

  @Override
  boolean associateEventInstance(final EFSMEventInstance inst,
                                 final TransitionGroup instGroup)
  {
    if (super.associateEventInstance(inst, instGroup)) {
      if (!isConsideredControllable(inst) && !instGroup.isAlwaysEnabled()) {
        mPlantificiationNeeded = true;
      }
      return true;
    } else {
      return false;
    }
  }

  @Override
  boolean isSuppressed(final EFSMEventInstance inst)
  {
    if (mPlantifiedSpec == null) {
      return false; // only suppress in plantification (no optimisation here!)
    } else {
      final TransitionGroup group = getTransitionGroup(inst);
      assert group != null;
      if (mPlantifiedSpec.isConsideredControllable(inst)) {
        return group.isSelfloopOnly();
      } else {
        return group.isAlwaysEnabled() && group.isSelfloopOnly();
      }
    }
  }


  //#########################################################################
  //# Building the Transition Relation
  void initialiseTransitions(final Map<EFSMEventDeclaration,TLongArrayList> map)
  {
    final int numStates = getRange().size();
    final int size = map.size();
    final Map<TLongArrayList,TransitionGroup> groupMap = new HashMap<>(size);
    mEventMap = new HashMap<>(size);
    for (final Map.Entry<EFSMEventDeclaration,TLongArrayList> entry :
         map.entrySet()) {
      final EFSMEventDeclaration decl = entry.getKey();
      final TLongArrayList transitions = entry.getValue();
      TransitionGroup group = groupMap.get(transitions);
      if (group == null) {
        group = new TransitionGroup(transitions, numStates);
        groupMap.put(transitions, group);
      }
      mEventMap.put(decl, group);
    }
    mTransitionGroupMap = new HashMap<>(size);
  }

  List<EFSMEventInstance> getAdditionalEventInstances()
  {
    if (mEventMap == null) {
      return null;
    }
    final Collection<EFSMEventInstance> associated = getAssociatedEventInstances();
    final List<EFSMEventInstance> result = new ArrayList<>(associated.size());
    for (final EFSMEventInstance inst : associated) {
      final EFSMEventDeclaration decl = inst.getEFSMEventDeclaration();
      if (!mEventMap.containsKey(decl)) {
        result.add(inst);
      }
    }
    if (mPlantifiedSpec != null) {
      for (final EFSMEventDeclaration decl : mEventMap.keySet()) {
        if (!mPlantifiedSpec.isConsideredControllable(decl)) {
          for (final EFSMEventInstance inst : decl.getInstances()) {
            result.add(inst);
          }
        }
      }
    }
    if (result.isEmpty()) {
      return null;
    }
    Collections.sort(result);
    return result;
  }


  //#########################################################################
  //# Plantification
  EFSMSimpleComponent prePlantify()
  {
    final SimpleComponentProxy proxy = getComponentProxy();
    final CompiledRange range = getRange();
    final EFSMSimpleComponent plant =
      new EFSMSimpleComponent(proxy, range, this);
    plant.prePlantify(this);
    return plant;
  }

  boolean isSubsumedPlantification()
  {
    assert mPlantifiedSpec != null;
    for (final EFSMEventInstance inst : getAssociatedEventInstances()) {
      if (!mPlantifiedSpec.isConsideredControllable(inst)) {
        final TransitionGroup plantGroup = getTransitionGroup(inst);
        final TransitionGroup specGroup =
          mPlantifiedSpec.getTransitionGroup(inst);
        if (!subsumes(specGroup, plantGroup)) {
          return false;
        }
      }
    }
    return true;
  }

  private void prePlantify(final EFSMSimpleComponent spec)
  {
    final Map<EFSMEventDeclaration,TransitionGroup>
      oldEventMap = spec.mEventMap;
    final int numEvents = oldEventMap.size();
    final int numStates = getRange().size();
    final Map<TransitionGroup,TransitionGroup> groupMap =
      new HashMap<>(numEvents);
    mEventMap = new HashMap<>(numEvents);
    for (final Map.Entry<EFSMEventDeclaration,TransitionGroup> entry :
         oldEventMap.entrySet()) {
      final EFSMEventDeclaration decl = entry.getKey();
      final TransitionGroup group = entry.getValue();
      TransitionGroup selflooped;
      if (spec.isConsideredControllable(decl)) {
        selflooped = group;
      } else {
        selflooped = groupMap.get(group);
        if (selflooped == null) {
          selflooped = group.addMissingSelfloops(numStates);
          groupMap.put(group, selflooped);
        }
      }
      mEventMap.put(decl, selflooped);
    }
    final int numTransitionGroups = spec.mTransitionGroupMap.size();
    mTransitionGroupMap = new HashMap<>(numTransitionGroups);
  }

  private boolean isConsideredControllable(final EFSMEventDeclaration decl)
  {
    switch (getKind()) {
    case PLANT:
      return true;
    case SPEC:
    case SUPERVISOR:
      return decl.getKind() == EventKind.CONTROLLABLE;
    default:
      return false;
    }
  }

  private boolean isConsideredControllable(final EFSMEventInstance inst)
  {
    final EFSMEventDeclaration decl = inst.getEFSMEventDeclaration();
    return isConsideredControllable(decl);
  }


  //#########################################################################
  //# Auxiliary Methods
  private TransitionGroup getTransitionGroup(final GroupKey key)
  {
    return mTransitionGroupMap.get(key);
  }

  private TransitionGroup addTransitionGroup(final GroupKey key,
                                             final TLongArrayList transitions)
  {
    final int numStates = getRange().size();
    final TransitionGroup group = new TransitionGroup(transitions, numStates);
    mTransitionGroupMap.put(key, group);
    return group;
  }


  //#########################################################################
  //# Debugging
  @Override
  public String toString()
  {
    return "EFSM " + getKind() + " " + getName();
  }


  //#########################################################################
  //# Inner Class GroupKey
  private static class GroupKey
  {
    //#######################################################################
    //# Constructor
    private GroupKey(final TransitionGroup master,
                     final ConstraintList constraints)
    {
      mMaster = master;
      mConstraints = constraints;
    }

    //#######################################################################
    //# Overrides for java.lang.Object
    @Override
    public boolean equals(final Object other)
    {
      if (other.getClass() == getClass()) {
        final GroupKey key = (GroupKey) other;
        return mMaster == key.mMaster && mConstraints.equals(key.mConstraints);
      } else {
        return false;
      }
    }

    @Override
    public int hashCode()
    {
      int result = mConstraints.hashCode();
      if (mMaster != null) {
        result += 5 * mMaster.hashCode();
      }
      return result;
    }

    //#######################################################################
    //# Data Members
    private final TransitionGroup mMaster;
    private final ConstraintList mConstraints;
  }


  //#########################################################################
  //# Data Members
  private final EFSMSimpleComponent mPlantifiedSpec;
  private Map<EFSMEventDeclaration,TransitionGroup> mEventMap;
  private Map<GroupKey,TransitionGroup> mTransitionGroupMap;
  private boolean mPlantificiationNeeded;

}
