//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.analysis.efa
//# CLASS:   EFSMSystem
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efsm;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Robi Malik, Sahar Mohajerani
 */
public class EFSMSystem
{

  //#########################################################################
  //# Constructors
  public EFSMSystem()
  {
    this(DEFAULT_SIZE);
  }

  public EFSMSystem(final int size)
  {
    mTransitionRelations = new ArrayList<EFSMTransitionRelation>(size);
    mVariables = new ArrayList<EFSMVariable>(size);
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


  //#########################################################################
  //# Class Constants
  private static final int DEFAULT_SIZE = 16;

}
