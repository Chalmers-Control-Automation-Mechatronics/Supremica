//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
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

package net.sourceforge.waters.analysis.efa.unified;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;


/**
 * A composition candidate used by the unified EFA conflict checker.
 * Contains EFAs and variables to be composed.
 *
 * @author Robi Malik, Sahar Mohajerani
 */

class UnifiedEFACandidate implements Comparable<UnifiedEFACandidate>
{

  //#######################################################################
  //# Constructors
  UnifiedEFACandidate(final Set<UnifiedEFATransitionRelation> trs,
                      final Set<UnifiedEFAConflictChecker.VariableInfo> vars)
  {
    mTransitionRelations = new ArrayList<>(trs);
    Collections.sort(mTransitionRelations);
    mVariables = new ArrayList<>(vars);
    Collections.sort(mVariables);
    mLocalEvents = new ArrayList<>();
  }


  //#######################################################################
  //# Simple Access
  List<UnifiedEFATransitionRelation> getTransitionRelations()
  {
    return mTransitionRelations;
  }

  List<UnifiedEFAConflictChecker.VariableInfo> getVariableInfo()
  {
    return mVariables;
  }

  List<UnifiedEFAVariable> getVariables()
  {
    final List<UnifiedEFAVariable> vars =
      new ArrayList<UnifiedEFAVariable>(mVariables.size());
    for (final UnifiedEFAConflictChecker.VariableInfo info : mVariables) {
      vars.add(info.getVariable());
    }
    return vars;
  }

  List<UnifiedEFAConflictChecker.EventInfo> getLocalEvents()
  {
    return mLocalEvents;
  }

  void addLocalEvent(final UnifiedEFAConflictChecker.EventInfo info)
  {
    mLocalEvents.add(info);
  }

  void setIsVariableLocal(final boolean local)
  {
    mIsVariableLocal = local;
  }

  boolean isVariableLocal()
  {
    return mIsVariableLocal;
  }


  //#######################################################################
  //# Overrides for java.lang.Object
  @Override
  public int hashCode()
  {
    return mTransitionRelations.hashCode() + mVariables.hashCode();
  }

  @Override
  public boolean equals(final Object object)
  {
    if (object != null && object.getClass() == getClass()) {
      final UnifiedEFACandidate candidate = (UnifiedEFACandidate) object;
      return
        candidate.mTransitionRelations.equals(mTransitionRelations) &&
        candidate.mVariables.equals(mVariables);
    } else {
      return false;
    }
  }


  //#######################################################################
  //# Interface java.util.Comparable
  @Override
  public int compareTo(final UnifiedEFACandidate candidate)
  {
    // TODO More efficient compare please
    return toString().compareTo(candidate.toString());
  }


  //#######################################################################
  //# Debugging
  @Override
  public String toString()
  {
    String sep = "";
    final StringBuilder buffer = new StringBuilder("{");
    for (final UnifiedEFATransitionRelation tr : mTransitionRelations) {
      buffer.append(sep);
      buffer.append(tr.getName());
      sep = ",";
    }
    sep = ";";
    for (final UnifiedEFAConflictChecker.VariableInfo var : mVariables) {
      buffer.append(sep);
      buffer.append(var.getName());
      sep = ",";
    }
    buffer.append('}');
    return buffer.toString();
  }


  //#######################################################################
  //# Data Members
  private final List<UnifiedEFATransitionRelation> mTransitionRelations;
  private final List<UnifiedEFAConflictChecker.VariableInfo> mVariables;
  private final List<UnifiedEFAConflictChecker.EventInfo> mLocalEvents;
  private boolean mIsVariableLocal = false;

}
