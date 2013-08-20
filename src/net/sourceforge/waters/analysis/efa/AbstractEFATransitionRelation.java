//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFA Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efa
//# CLASS:   AbstractEFATransitionRelation
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa;

import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.model.module.SimpleNodeProxy;

import java.util.Collection;
import java.util.List;

/**
 * @author Robi Malik
 */
public abstract class AbstractEFATransitionRelation<L>
 implements Comparable<AbstractEFATransitionRelation<?>>
{

  //#########################################################################
  //# Constructors
  @SuppressWarnings("unchecked")
  protected AbstractEFATransitionRelation(final ListBufferTransitionRelation rel,
                                          final AbstractEFATransitionLabelEncoding<L> labels,
                                          final Collection<? extends AbstractEFAVariable<L>> variables,
                                          final List<SimpleNodeProxy> nodes)
  {
    mTransitionRelation = rel;
    mTransitionLabelEncoding = labels;
    mVariables = (Collection<AbstractEFAVariable<L>>) variables;
    mNodeList = nodes;
  }

  protected AbstractEFATransitionRelation(final ListBufferTransitionRelation rel,
                                          final AbstractEFATransitionLabelEncoding<L> labels,
                                          final Collection<? extends AbstractEFAVariable<L>> variables)
  {
    this(rel, labels, variables, null);
  }

  //#########################################################################
  //# Simple Access
  protected ListBufferTransitionRelation getTransitionRelation()
  {
    return mTransitionRelation;
  }

  public AbstractEFATransitionLabelEncoding<L> getTransitionLabelEncoding()
  {
    return mTransitionLabelEncoding;
  }

  protected List<SimpleNodeProxy> getNodeList()
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

  public Collection<? extends AbstractEFAVariable<L>> getVariables()
  {
    return mVariables;
  }

  /**
   * Registers this transition relation by adding its reference to all its
   * variables.
   */
  public void register()
  {
    for (final AbstractEFAVariable<L> var : mVariables) {
      var.addTransitionRelation(this);
    }
  }

  /**
   * Deregisters this transition relation by removing its reference from all its
   * variables.
   */
  public void dispose()
  {
    for (final AbstractEFAVariable<L> var : mVariables) {
      var.removeTransitionRelation(this);
    }
  }

  //#########################################################################
  //# Interface java.util.Comparable
  @Override
  public int compareTo(final AbstractEFATransitionRelation<?> efaTR)
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
  private final AbstractEFATransitionLabelEncoding<L> mTransitionLabelEncoding;
  private final Collection<AbstractEFAVariable<L>> mVariables;
  private final List<SimpleNodeProxy> mNodeList;
}
