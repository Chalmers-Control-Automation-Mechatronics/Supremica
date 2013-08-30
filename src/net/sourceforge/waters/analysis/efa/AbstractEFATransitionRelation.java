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
  public AbstractEFATransitionLabelEncoding<L> getTransitionLabelEncoding()
  {
    return mTransitionLabelEncoding;
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

  @Override
  public int compareTo(final AbstractEFATransitionRelation<?> efaTR)
  {
    final String name1 = getName();
    final String name2 = efaTR.getName();
    return name1.compareTo(name2);
  }

  @Override
  public String toString()
  {
    return mTransitionRelation.getName() + "\n" + mTransitionRelation.toString();
  }
  
  protected ListBufferTransitionRelation getTransitionRelation()
  {
    return mTransitionRelation;
  }

  protected List<SimpleNodeProxy> getNodeList()
  {
    return mNodeList;
  }
  protected void addVariable(final AbstractEFAVariable<L> variable)
  {
    mVariables.add(variable);
    variable.addTransitionRelation(this);
  }
  protected void removeVariable(final AbstractEFAVariable<L> variable)
  {
    mVariables.remove(variable);
    variable.removeTransitionRelation(this);
  }
  
  //#########################################################################
  //# Data Members
  private final ListBufferTransitionRelation mTransitionRelation;
  private final AbstractEFATransitionLabelEncoding<L> mTransitionLabelEncoding;
  private final Collection<AbstractEFAVariable<L>> mVariables;
  private final List<SimpleNodeProxy> mNodeList;
}
