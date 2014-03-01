//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFA Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efa
//# CLASS:   AbstractEFATransitionLabel
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa.base;

import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.analysis.efa.simple.SimpleEFAEventDecl;
import net.sourceforge.waters.analysis.efa.simple.SimpleEFAVariable;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.compiler.constraint.ConstraintList;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;
import net.sourceforge.waters.model.module.ModuleHashCodeVisitor;
import net.sourceforge.waters.xsd.base.EventKind;

/**
 * An abstract class for EFA transition labels.
 * @author Mohammad Reza Shoaei
 */
public abstract class AbstractEFATransitionLabel
{

  public AbstractEFATransitionLabel(final ConstraintList constraint,
                                    final SimpleEFAEventDecl event)
  {
    mConstraint = constraint;
    mEvent = event;
    mVariables = new THashSet<>();
    mTransitionRelations = new THashSet<>();
    mIsObservable = mEvent.isObservable();
    mKind = mEvent.getKind();
    mProxyList = new ArrayList<>();
    mProxyList.add(mEvent.getEventDecl());
    mProxyList.addAll(mConstraint.getConstraints());
  }

  public AbstractEFATransitionLabel(final SimpleEFAEventDecl event)
  {
    this(ConstraintList.TRUE, event);
  }

  public SimpleEFAEventDecl getEvent()
  {
    return mEvent;
  }

  public ConstraintList getConstraint()
  {
    return mConstraint;
  }

  public EventKind getKind()
  {
    return mKind;
  }

  public boolean isObservable()
  {
    return mIsObservable;
  }

  public Map<String, String> getAttributes()
  {
    return mEvent.getAttributes();
  }

  public boolean isBlocked()
  {
    return mIsBlocked;
  }

  public void setBlocked()
  {
    mVariables.clear();
    mTransitionRelations.clear();
    mIsBlocked = true;
  }

  public Set<SimpleEFAVariable> getVariables()
  {
    return mVariables;
  }

  public void addVariable(final SimpleEFAVariable var)
  {
    mVariables.add(var);
  }

  public void addVariables(final Collection<SimpleEFAVariable> vars)
  {
    mVariables.addAll(vars);
  }

  public void setKind(final EventKind kind)
  {
    if (mKind.equals(kind)) {
      return;
    }
    mKind = kind;
  }

  public void setObservable(final boolean observable)
  {
    if (mIsObservable == observable) {
      return;
    }
    mIsObservable = observable;
  }

  @Override
  public boolean equals(final Object other)
  {
    if (other != null && other.getClass() == getClass()) {
      final ModuleEqualityVisitor eq = new ModuleEqualityVisitor(false);
      final AbstractEFATransitionLabel expected =
       (AbstractEFATransitionLabel) other;
      final ArrayList<Proxy> pList = new ArrayList<>();
      pList.add(expected.getEvent().getEventDecl());
      pList.addAll(expected.getConstraint().getConstraints());
      return eq.isEqualList(mProxyList, pList);
    }
    return false;
  }

  @Override
  public int hashCode()
  {
    final ModuleHashCodeVisitor hash =
     ModuleHashCodeVisitor.getInstance(false);
    return hash.getListHashCode(this.mProxyList);
  }

  //#########################################################################
  //# Data Members
  private final Set<SimpleEFAVariable> mVariables;
  private final ConstraintList mConstraint;
  private final Set<? extends AbstractEFATransitionRelation<?>> mTransitionRelations;
  private boolean mIsBlocked;
  private EventKind mKind;
  private boolean mIsObservable;
  private final List<Proxy> mProxyList;
  private final SimpleEFAEventDecl mEvent;

}
