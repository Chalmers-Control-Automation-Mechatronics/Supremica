//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.analysis.efa
//# CLASS:   EFSMSystem
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Robi Malik, Sahar Mohajerani
 */
class EFSMSystem
{

  //#########################################################################
  //# Constructors
  public EFSMSystem() {
    mTransitionRelations = new ArrayList<EFSMTransitionRelation>();
    mVariables = new ArrayList<EFSMVariable>();
  }

  public EFSMSystem(final List<EFSMVariable> variables,
                    final List<EFSMTransitionRelation> transitionRelations)
  {
    mTransitionRelations = transitionRelations;
    mVariables = variables;
  }


  //#########################################################################
  //# Simple Access
  public List<EFSMTransitionRelation> getTransitionRelations() {
    return mTransitionRelations;
  }

  public List<EFSMVariable> getVariables() {
    return mVariables;
  }

  public void addTransitionRelation(final EFSMTransitionRelation
                                    transitionRelation) {
    mTransitionRelations.add(transitionRelation);
  }

  public void addVariable(final EFSMVariable variable) {
    mVariables.add(variable);
  }


  //#########################################################################
  //# Data Members
  private final List<EFSMTransitionRelation> mTransitionRelations;
  private final List<EFSMVariable> mVariables;
}
