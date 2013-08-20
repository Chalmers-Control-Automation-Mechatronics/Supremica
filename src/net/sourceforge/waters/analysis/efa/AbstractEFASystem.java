//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFA Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efa
//# CLASS:   AbstractEFASystem
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa;

import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Robi Malik
 */
public abstract class AbstractEFASystem<L, 
                                        V extends AbstractEFAVariable<L>, 
                                        TR extends AbstractEFATransitionRelation<L>, 
                                        C extends AbstractEFAVariableContext<L, V>>
 implements Comparable<AbstractEFASystem<?, ?, ?, ?>>
{

  //#########################################################################
  //# Constructors
  public AbstractEFASystem(final String name,
                           final C context)
  {
    this(name, context, DEFAULT_SIZE);
  }

  public AbstractEFASystem(final String name,
                           final C context,
                           final int size)
  {
    this(name,
         new ArrayList<V>(size),
         new ArrayList<TR>(size),
         context);
  }

  public AbstractEFASystem(final String name,
                           final List<V> variables,
                           final List<TR> transitionRelations,
                           final C context)
  {
    mName = name;
    mTransitionRelations = transitionRelations;
    mVariables = variables;
    mVariableContext = context;
  }

  //#########################################################################
  //# Simple Access
  public String getName()
  {
    return mName;
  }

  protected List<TR> getTransitionRelations()
  {
    return mTransitionRelations;
  }

  public List<V> getVariables()
  {
    return mVariables;
  }

  public C getVariableContext()
  {
    return mVariableContext;
  }

  public void setName(final String name)
  {
    mName = name;
  }

  protected void addTransitionRelation(final TR transitionRelation)
  {
    mTransitionRelations.add(transitionRelation);
  }

  protected void removeTransitionRelation(final TR transitionRelation)
  {
    mTransitionRelations.remove(transitionRelation);
  }

  public void addVariable(final V variable)
  {
    mVariables.add(variable);
  }

  public void removeVariable(final V var)
  {
    mVariables.remove(var);
  }

  public double getEstimatedSize()
  {
    double size = 1;
    for (final AbstractEFATransitionRelation<?> efaTR : mTransitionRelations) {
      final ListBufferTransitionRelation rel = efaTR.getTransitionRelation();
      size *= rel.getNumberOfStates();
    }
    for (final V var : mVariables) {
      size *= var.getRange().size();
    }
    return size;
  }

  //#########################################################################
  //# Interface java.util.Comparable<EFASystem>
  @Override
  public int compareTo(final AbstractEFASystem<?, ?, ?, ?> system)
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
  //# Class Constants
  private static final int DEFAULT_SIZE = 16;
  //#########################################################################
  //# Data Members
  private final List<TR> mTransitionRelations;
  private final List<V> mVariables;
  private final C mVariableContext;
  private String mName;
  
}
