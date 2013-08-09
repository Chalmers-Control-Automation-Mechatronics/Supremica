//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFA Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efa
//# CLASS:   AbstractEFATransitionRelation
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa;

import java.util.Collection;
import java.util.List;

import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.model.module.SimpleNodeProxy;


/**
 * @author Robi Malik
 */
public abstract class AbstractEFATransitionRelation
  implements Comparable<AbstractEFATransitionRelation>
{

  //#########################################################################
  //# Constructors
  public AbstractEFATransitionRelation(final ListBufferTransitionRelation rel,
                               final AbstractEFAEventEncoding events,
                               final Collection<? extends AbstractEFAVariable> variables,
                               final List<SimpleNodeProxy> nodes)
  {
    mTransitionRelation = rel;
    mEventEncoding = events;
    mVariables = variables;
    mNodeList = nodes;
  }

  public AbstractEFATransitionRelation(final ListBufferTransitionRelation rel,
                               final AbstractEFAEventEncoding events,
                               final Collection<? extends AbstractEFAVariable> variables)
  {
    this(rel, events, variables, null);
  }


  //#########################################################################
  //# Simple Access
  public ListBufferTransitionRelation getTransitionRelation()
  {
    return mTransitionRelation;
  }

  public AbstractEFAEventEncoding getEventEncoding()
  {
    return mEventEncoding;
  }

  public List<SimpleNodeProxy> getNodeList()
  {
    return mNodeList;
  }

  public String getName()
  {
    return mTransitionRelation.getName();
  }

  public void setName(final String name)
  {
    mTransitionRelation.setName(name);
  }


  public Collection<? extends AbstractEFAVariable> getVariables()
  {
    return mVariables;
  }

  /**
   * Registers this transition relation by adding its reference to all
   * its variables.
   */
  public void register()
  {
    for (final AbstractEFAVariable var : mVariables) {
      var.addTransitionRelation(this);
    }
  }

  /**
   * Deregisters this transition relation by removing its reference from all
   * its variables.
   */
  public void dispose()
  {
    for (final AbstractEFAVariable var : mVariables) {
      var.removeTransitionRelation(this);
    }
  }


  //#########################################################################
  //# Interface java.util.Comparable
  @Override
  public int compareTo(final AbstractEFATransitionRelation efaTR)
  {
    final String name1 = getName();
    final String name2 = efaTR.getName();
    return name1.compareTo(name2);
  }


  //#########################################################################
  //# Debugging
  @Override
  public String toString()
  {
    return mTransitionRelation.getName() + "\n" + mTransitionRelation.toString();
  }


  //#########################################################################
  //# Data Members
  private final ListBufferTransitionRelation mTransitionRelation;
  private final AbstractEFAEventEncoding mEventEncoding;
  private final List<SimpleNodeProxy> mNodeList;
  private final Collection<? extends AbstractEFAVariable> mVariables;

}
