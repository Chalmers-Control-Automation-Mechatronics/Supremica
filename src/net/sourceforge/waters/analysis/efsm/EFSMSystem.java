//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFSM Analysis
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

class EFSMSystem implements Comparable<EFSMSystem>
{

  //#########################################################################
  //# Constructors
  EFSMSystem(final String name, final EFSMVariableContext context)
  {
    this(name, context, DEFAULT_SIZE);
  }

  EFSMSystem(final String name,
             final EFSMVariableContext context,
             final int size)
  {
    this(name,
         new ArrayList<EFSMVariable>(size),
         new ArrayList<EFSMTransitionRelation>(size),
         context);
  }

  EFSMSystem(final String name,
             final List<EFSMVariable> variables,
             final List<EFSMTransitionRelation> transitionRelations,
             final EFSMVariableContext context)
  {
    mName = name;
    mTransitionRelations = transitionRelations;
    mVariables = variables;
    mVariableContext = context;
  }


  //#########################################################################
  //# Simple Access
  String getName()
  {
    return mName;
  }

  List<EFSMTransitionRelation> getTransitionRelations()
  {
    return mTransitionRelations;
  }

  List<EFSMVariable> getVariables()
  {
    return mVariables;
  }

  EFSMVariableContext getVariableContext()
  {
    return mVariableContext;
  }


  void setName(final String name)
  {
    mName = name;
  }

  void addTransitionRelation(final EFSMTransitionRelation transitionRelation)
  {
    mTransitionRelations.add(transitionRelation);
  }

  void removeTransitionRelation(final EFSMTransitionRelation transitionRelation)
  {
    mTransitionRelations.remove(transitionRelation);
  }

  void addVariable(final EFSMVariable variable)
  {
    mVariables.add(variable);
  }

  void removeVariable(final EFSMVariable var)
  {
    mVariables.remove(var);
  }


  double getEstimatedSize()
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


  //#########################################################################
  //# Interface java.util.Comparable<EFSMSystem>
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
