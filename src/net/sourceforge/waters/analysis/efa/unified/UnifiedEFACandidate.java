//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFA Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efa.unified
//# CLASS:   UnifiedEFACandidate
//###########################################################################
//# $Id$
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

  List<UnifiedEFAConflictChecker.VariableInfo> getVariables()
  {
    return mVariables;
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
