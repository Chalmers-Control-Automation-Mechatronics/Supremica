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

import net.sourceforge.waters.model.compiler.context.VariableContext;


/**
 * @author Robi Malik, Sahar Mohajerani
 */
public class EFSMSystem
{

  //#########################################################################
  //# Constructors
  public EFSMSystem(final String name, final VariableContext context)
  {
    this(name, context, DEFAULT_SIZE);
  }

  public EFSMSystem(final String name, final VariableContext context, final int size)
  {
    this(name,
         new ArrayList<EFSMVariable>(size),
         new ArrayList<EFSMTransitionRelation>(size),
         context);
  }

  public EFSMSystem(final String name,
                    final List<EFSMVariable> variables,
                    final List<EFSMTransitionRelation> transitionRelations,
                    final VariableContext context)
  {
    mTransitionRelations = transitionRelations;
    mVariables = variables;
    mName = name;
    mVariableContext = context;
  }

  //#########################################################################
  //# Simple Access
  public List<EFSMTransitionRelation> getTransitionRelations() {
    return mTransitionRelations;
  }

  public String getName() {
    return mName;
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

  public void setName(final String name) {
    mName = name;
  }

  public VariableContext getVariableContext( ){
    return mVariableContext;
  }

  //#########################################################################
  //# Data Members
  private final List<EFSMTransitionRelation> mTransitionRelations;
  private final List<EFSMVariable> mVariables;
  private final VariableContext mVariableContext;


  //#########################################################################
  //# Class Constants
  private static final int DEFAULT_SIZE = 16;
  private String mName;

}
