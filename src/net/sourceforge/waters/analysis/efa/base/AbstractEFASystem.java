//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
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
