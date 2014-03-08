//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFA Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efa
//# CLASS:   SimpleEFATransitionLabel
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa.simple;

import java.util.*;

import net.sourceforge.waters.model.compiler.constraint.ConstraintList;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.xsd.base.EventKind;

/**
 * A simple implementation of transition labels in EFAs.
 * <p/>
 * @author Mohammad Reza Shoaei
 */
public class SimpleEFATransitionLabel
{

  private final Set<SimpleEFAVariable> mVariables;
  private ConstraintList mConstraint;
  private final Set<SimpleEFAComponent> mTransitionRelations;
  private boolean mIsBlocked;
  private EventKind mKind;
  private boolean mIsObservable;
  private final SimpleEFAEventDecl mEvent;

  public SimpleEFATransitionLabel(final SimpleEFAEventDecl event,
                                  final ConstraintList constraint)
  {
    mConstraint = constraint;
    mEvent = event;
    mVariables = new HashSet<>();
    mTransitionRelations = new HashSet<>();
    mIsObservable = mEvent.isObservable();
    mKind = mEvent.getKind();
  }

  public SimpleEFATransitionLabel(final SimpleEFAEventDecl event)
  {
    this(event, ConstraintList.TRUE);
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

  public void setBlocked() {
    mIsBlocked = true;}

  public Set<SimpleEFAVariable> getVariables()
  {
    return mVariables;
  }

  public boolean addVariable(final SimpleEFAVariable var)
  {
    return mVariables.add(var);
  }

  public boolean addEFAComponent(final SimpleEFAComponent efa)
  {
    return mTransitionRelations.add(efa);
  }

  public boolean addVariables(final Collection<SimpleEFAVariable> vars)
  {
    return mVariables.addAll(vars);
  }

  public void setKind(final EventKind kind)
  {
    mKind = kind;
  }

  public void setObservable(final boolean observable)
  {
    mIsObservable = observable;
  }

  public void addToConstraints(final ConstraintList con){
    final List<SimpleExpressionProxy> newCons = new ArrayList<>(mConstraint.getConstraints());
    newCons.addAll(con.getConstraints());

    mConstraint = new ConstraintList(newCons);
  }

  @Override
  public boolean equals(final Object other){
    if (other != null && other.getClass() == getClass()) {
      final SimpleEFATransitionLabel expected = (SimpleEFATransitionLabel) other;
      return expected.getEvent().equals(mEvent) &&
              expected.getConstraint().equals(mConstraint);
    }
    return false;
  }
  @Override
  public int hashCode()
  {
    int hash = 7;
    hash = 37 * hash + Objects.hashCode(mConstraint);
    hash = 37 * hash + Objects.hashCode(mEvent);
    return hash;
  }

  @Override
  public String toString(){
    final StringBuilder events = new StringBuilder();
    events.append("{");
    events.append(getEvent().toString());
    events.append(" : ");
    events.append(getConstraint().toString());
    events.append("}");
    return events.toString();
  }
}
