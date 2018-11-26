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
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.model.compiler.constraint.ConstraintList;
import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.module.ComponentProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;


/**
 * A compiler-internal representation of an EFSM component.
 *
 * @author Robi Malik
 */

abstract class EFSMComponent
{

  //#########################################################################
  //# Constructors
  EFSMComponent(final ComponentProxy comp,
                final CompiledRange range)
  {
    mComponent = comp;
    mRange = range;
    mEventInstanceMap = new HashMap<>();
  }


  //#########################################################################
  //# Simple Access
  ComponentProxy getComponentProxy()
  {
    return mComponent;
  }

  IdentifierProxy getIdentifier()
  {
    return mComponent.getIdentifier();
  }

  String getName()
  {
    return mComponent.getName();
  }

  CompiledRange getRange()
  {
    return mRange;
  }


  //#######################################################################
  //# Subsumption Checking
  boolean subsumes(final TransitionGroup newTransitionGroup,
                   final EFSMEventInstance inst)
  {
    final TransitionGroup transitionGroup = mEventInstanceMap.get(inst);
    return subsumes(newTransitionGroup, transitionGroup);
  }

  boolean isSubsumed(final TransitionGroup newTransitionGroup,
                     final EFSMEventInstance inst)
  {
    final TransitionGroup transitionGroup = mEventInstanceMap.get(inst);
    return subsumes(transitionGroup, newTransitionGroup);
  }

  boolean subsumes(final TransitionGroup group1,
                   final TransitionGroup group2)
  {
    if (group1 == group2) {
      return true;
    } else if (group1 == null) {
      return group2.isSelfloopOnly();
    } else if (group2 == null) {
      return isNeutral(group1);
    } else {
      return group1.subsumes(group2);
    }
  }

  boolean isNeutral(final TransitionGroup group)
  {
    if (group == null) {
      return true;
    } else {
      return group.isSelfloopOnly() &&
             group.getNumberOfTransitions() == getRange().size();
    }
  }


  //#########################################################################
  //# Building the Transition Relation
  abstract TransitionGroup createTransitionGroup
    (EFSMEventInstance inst,
     ConstraintList constraints,
     EFSMTransitionIteratorFactory factory)
    throws EvalException;

  void associateEventInstance(final EFSMEventInstance inst,
                              final TransitionGroup transitionGroup)
  {
    if (!isNeutral(transitionGroup)) {
      mEventInstanceMap.put(inst, transitionGroup);
    }
  }

  void removeEventInstance(final EFSMEventInstance inst)
  {
    mEventInstanceMap.remove(inst);
  }

  long getTransitionCode(final SimpleExpressionProxy source,
                         final SimpleExpressionProxy target)
  {
    final long s = mRange.indexOf(source);
    final long t = mRange.indexOf(target);
    return getTransitionCode(s, t);
  }

  TLongObjectMap<List<EFSMEventInstance>> getAllTransitions()
  {
    final TLongObjectMap<List<EFSMEventInstance>> map =
      new TLongObjectHashMap<>();
    for (final Map.Entry<EFSMEventInstance,TransitionGroup>
         entry : mEventInstanceMap.entrySet()) {
      final EFSMEventInstance event = entry.getKey();
      final TransitionGroup group = entry.getValue();
      group.collect(map, event);
    }
    return map;
  }


  //#########################################################################
  //# Transition Encoding
  static long getTransitionCode(final long s, final long t)
  {
    return (s << 32) | t;
  }

  static int getTransitionSource(final long transition)
  {
    return (int) (transition >>> 32);
  }

  static int getTransitionTarget(final long transition)
  {
    return (int) (transition & 0x7fffffff);
  }


  //#########################################################################
  //# Inner Class TransitionGroup
  static class TransitionGroup
  {
    //#######################################################################
    //# Constructor
    private TransitionGroup()
    {
      mTransitions = null;
      mNumberOfSelfloops = 0;
    }

    TransitionGroup(final TLongArrayList transitions)
    {
      mTransitions = transitions.toArray();
      Arrays.sort(mTransitions);
      int selfloops = 0;
      for (final long transition : mTransitions) {
        if (getTransitionSource(transition) == getTransitionTarget(transition)) {
          selfloops++;
        }
      }
      mNumberOfSelfloops = selfloops;
    }

    //#######################################################################
    //# Simple Access
    boolean isEmpty()
    {
      return mTransitions == null;
    }

    boolean isSelfloopOnly()
    {
      if (mTransitions == null) {
        return true;
      } else {
        return mNumberOfSelfloops == mTransitions.length;
      }
    }

    int getNumberOfTransitions()
    {
      if (mTransitions == null) {
        return 0;
      } else {
        return mTransitions.length;
      }
    }

    //#######################################################################
    //# Subsumption Checking
    boolean subsumes(final TransitionGroup group)
    {
      final long[] groupTransitions = group.mTransitions;
      if (mTransitions == null) {
        return groupTransitions == null;
      } else if (groupTransitions == null) {
        return true;
      } else if (mTransitions.length < groupTransitions.length) {
        return false;
      } else if (mNumberOfSelfloops < group.mNumberOfSelfloops) {
        return false;
      } else {
        final int d = mTransitions.length - groupTransitions.length + 1;
        int i = 0;
        for (int j = 0; j < groupTransitions.length; j++) {
          final int stop = d + j;
          final long transition = groupTransitions[j];
          while (i < stop && mTransitions[i] < transition) {
            i++;
          }
          if (i == stop || mTransitions[i] > transition) {
            return false;
          }
        }
        return true;
      }
    }

    //#######################################################################
    //# Building the Transition Relation
    private void collect(final TLongObjectMap<List<EFSMEventInstance>> map,
                         final EFSMEventInstance event)
    {
      if (mTransitions != null) {
        for (final long transition : mTransitions) {
          List<EFSMEventInstance> list = map.get(transition);
          if (list == null) {
            list = new ArrayList<>();
            map.put(transition, list);
          }
          list.add(event);
        }
      }
    }

    //#######################################################################
    //# Debugging
    @Override
    public String toString()
    {
      final StringBuilder builder = new StringBuilder("[");
      boolean first = true;
      for (final long transition : mTransitions) {
        if (first) {
          first = false;
        } else {
          builder.append(',');
        }
        final long s = getTransitionSource(transition);
        builder.append(s);
        builder.append("->");
        final long t = getTransitionTarget(transition);
        builder.append(t);
      }
      builder.append(']');
      return builder.toString();
    }

    //#######################################################################
    //# Data Members
    private final long[] mTransitions;
    private final int mNumberOfSelfloops;
  }


  //#########################################################################
  //# Data Members
  private final ComponentProxy mComponent;
  private final CompiledRange mRange;
  private final Map<EFSMEventInstance,TransitionGroup> mEventInstanceMap;


  //#########################################################################
  //# Class Constants
  static final TransitionGroup EMPTY_GROUP = new TransitionGroup();

}
