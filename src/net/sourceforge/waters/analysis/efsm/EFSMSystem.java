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

import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;


/**
 * @author Robi Malik, Sahar Mohajerani
 */
public class EFSMSystem implements Comparable<EFSMSystem>
{

  //#########################################################################
  //# Constructors
  public EFSMSystem(final String name, final EFSMVariableContext context)
  {
    this(name, context, DEFAULT_SIZE);
  }

  public EFSMSystem(final String name, final EFSMVariableContext context, final int size)
  {
    this(name,
         new ArrayList<EFSMVariable>(size),
         new ArrayList<EFSMTransitionRelation>(size),
         context);
  }

  public EFSMSystem(final String name,
                    final List<EFSMVariable> variables,
                    final List<EFSMTransitionRelation> transitionRelations,
                    final EFSMVariableContext context)
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

  public void removeVariable (final EFSMVariable var ){
    mVariables.remove(var);
  }

  public void setName(final String name) {
    mName = name;
  }

  public EFSMVariableContext getVariableContext( ){
    return mVariableContext;
  }

  public double getEstimatedSize()
  {
    double size = 1;
    for (final EFSMTransitionRelation efsmTR : mTransitionRelations) {
      final ListBufferTransitionRelation rel = efsmTR.getTransitionRelation();
      size = size * rel.getNumberOfStates();
    }
    for (final EFSMVariable var : mVariables) {
      size = size * var.getRange().size();
    }
    return size;
  }

  @Override
  public int compareTo(final EFSMSystem system)
  {
    final double size1 = getEstimatedSize();
    final double size2 = system.getEstimatedSize();
    if (size1 < size2) {
      return -1;
    } else if (size1 > size2) {
      return 1;
    } else {
      return 0;
    }
  }


  //#########################################################################
  //# Data Members
  private final List<EFSMTransitionRelation> mTransitionRelations;
  private final List<EFSMVariable> mVariables;
  private final EFSMVariableContext mVariableContext;


  //#########################################################################
  //# Class Constants
  private static final int DEFAULT_SIZE = 16;
  private String mName;

}
