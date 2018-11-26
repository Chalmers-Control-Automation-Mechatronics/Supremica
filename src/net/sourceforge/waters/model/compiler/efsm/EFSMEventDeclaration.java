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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import net.sourceforge.waters.model.compiler.constraint.ConstraintList;
import net.sourceforge.waters.model.compiler.context.CompilationInfo;
import net.sourceforge.waters.model.compiler.efa.EFAEventNameBuilder;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;


/**
 * A compiler-internal representation of an event declaration when
 * compiling a normalised EFSM system.
 *
 * @see EFSMCompiler
 * @author Robi Malik
 */

class EFSMEventDeclaration
{
  //#######################################################################
  //# Constructor
  EFSMEventDeclaration(final EventDeclProxy decl)
  {
    mEventDecl = decl;
    mGuardedActions = ConstraintList.TRUE;
    mInstances = new LinkedList<>();
  }


  //#######################################################################
  //# Simple Access
  EventDeclProxy getEventDeclProxy()
  {
    return mEventDecl;
  }

  String getName()
  {
    return mEventDecl.getName();
  }

  ConstraintList getGuardedActions()
  {
    return mGuardedActions;
  }

  boolean provideGuardedActions(final ConstraintList ga)
  {
    if (ga.isTrue()) {
      return true;
    } else if (mGuardedActions.isTrue()) {
      mGuardedActions = ga;
      return true;
    } else {
      return mGuardedActions.equals(ga);
    }
  }

  int getNumberOfInstances()
  {
    return mInstances.size();
  }

  public Iterable<EFSMEventInstance> getInstances()
  {
    return mInstances;
  }

  void addInstance(final EFSMEventInstance inst)
  {
    mInstances.add(inst);
  }

  void removeAllInstances()
  {
    mInstances.clear();
  }


  //#########################################################################
  //# Event Compilation
  void provideSuffixes(final EFAEventNameBuilder namer)
  {
    try {
      namer.restart();
      for (final EFSMEventInstance event : mInstances) {
        final ConstraintList ga = event.getGuardedActions();
        namer.addGuard(ga);
      }
      for (final EFSMEventInstance event : mInstances) {
        final ConstraintList ga = event.getGuardedActions();
        final String suffix = namer.getNameSuffix(ga);
        event.setSuffix(suffix);
      }
      Collections.sort(mInstances);
    } finally {
      namer.clear();
    }
  }

  void compile(final ModuleProxyFactory factory,
               final CompilationInfo compilationInfo,
               final List<EventDeclProxy> output)
  {
    for (final EFSMEventInstance event : mInstances) {
      final EventDeclProxy decl = event.createEventDeclProxy(factory);
      output.add(decl);
      compilationInfo.add(decl, mEventDecl);
    }
  }


  //#######################################################################
  //# Debugging
  @Override
  public String toString()
  {
    return "EFSM-event " + getName() +
      " (" + getNumberOfInstances() + " instances)";
  }


  //#######################################################################
  //# Data Members
  private final EventDeclProxy mEventDecl;
  private ConstraintList mGuardedActions;
  private final List<EFSMEventInstance> mInstances;
}
