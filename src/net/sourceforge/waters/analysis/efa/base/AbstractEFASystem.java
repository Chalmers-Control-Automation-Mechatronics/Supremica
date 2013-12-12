//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFA Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efa
//# CLASS:   AbstractEFASystem
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa.base;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.model.base.ProxyTools;

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

  public void setName(final String name)
  {
    mName = name;
  }

  public List<V> getVariables()
  {
    return mVariables;
  }

  public void addVariable(final V variable)
  {
    mVariables.add(variable);
  }

  public void removeVariable(final V var)
  {
    mVariables.remove(var);
  }

  public C getVariableContext()
  {
    return mVariableContext;
  }

  protected List<TR> getTransitionRelations()
  {
    return mTransitionRelations;
  }

  protected boolean addTransitionRelation(final TR transitionRelation)
  {
    return mTransitionRelations.add(transitionRelation);
  }

  protected void removeTransitionRelation(final TR transitionRelation)
  {
    mTransitionRelations.remove(transitionRelation);
  }


  //#########################################################################
  //# Heuristics
  public double getEstimatedStateSpace()
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
  //# Interface java.util.Comparable<AbstractEFASystem>
  @Override
  public int compareTo(final AbstractEFASystem<?, ?, ?, ?> system)
  {
    final double size1 = getEstimatedStateSpace();
    final double size2 = system.getEstimatedStateSpace();
    if (size1 < size2) {
      return -1;
    } else if (size1 > size2) {
      return 1;
    } else {
      return 0;
    }
  }


  //#########################################################################
  //# Debugging
  @Override
  public String toString()
  {
    final StringBuilder buffer = new StringBuilder();
    buffer.append(ProxyTools.getShortClassName(this));
    buffer.append(' ');
    buffer.append(mName);
    buffer.append("\nTRs:");
    boolean first = true;
    for (final TR tr: mTransitionRelations) {
      if (first) {
        first = false;
      } else {
        buffer.append(',');
      }
      buffer.append(' ');
      buffer.append(tr.getName());
    }
    buffer.append("\nVars:");
    first = true;
    for (final V var : mVariables) {
      if (first) {
        first = false;
      } else {
        buffer.append(',');
      }
      buffer.append(' ');
      buffer.append(var.getName());
    }
    return buffer.toString();
  }


  //#########################################################################
  //# Data Members
  private String mName;
  private final List<TR> mTransitionRelations;
  private final List<V> mVariables;
  private final C mVariableContext;


  //#########################################################################
  //# Class Constants
  private static final int DEFAULT_SIZE = 16;

}
