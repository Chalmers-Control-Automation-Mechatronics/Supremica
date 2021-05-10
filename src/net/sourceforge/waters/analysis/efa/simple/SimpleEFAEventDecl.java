//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
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

package net.sourceforge.waters.analysis.efa.simple;

import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.THashSet;
import gnu.trove.set.hash.TIntHashSet;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.model.base.EventKind;
import net.sourceforge.waters.model.base.NamedProxy;
import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;
import net.sourceforge.waters.model.module.ModuleHashCodeVisitor;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.plain.base.NamedElement;
import net.sourceforge.waters.subject.module.EventDeclSubject;

/**
 *
 * @author Mohammad Reza Shoaei
 */
public class SimpleEFAEventDecl
        extends NamedElement
        implements EventProxy
{

  //#########################################################################
  //# Constructors
  public SimpleEFAEventDecl(final EventDeclProxy decl)
  {
    super(decl.getName());
    mEFAEventDecl = decl;
    mVariables = new TIntHashSet();
    mPrimeVariables = new TIntHashSet();
    mUnPrimeVariables = new TIntHashSet();
    mComponents = new THashSet<>();
    mIsObservable = decl.isObservable();
    mKind = decl.getKind();
  }

  public SimpleEFAEventDecl(final IdentifierProxy identifier,
                            final EventKind kind)
  {
    super(identifier.getPlainText());
    mEFAEventDecl = new EventDeclSubject(identifier, kind);
    mVariables = new TIntHashSet();
    mPrimeVariables = new TIntHashSet();
    mUnPrimeVariables = new TIntHashSet();
    mComponents = new THashSet<>();
    mIsObservable = true;
    mKind = kind;
  }

  //#########################################################################
  //# Overrides for java.lang.Object
  @Override
  public String toString()
  {
    return mEFAEventDecl.getName();
  }

  //#########################################################################
  //# Simple Access
  public EventDeclProxy getEventDecl()
  {
    return mEFAEventDecl;
  }

  @Override
  public EventKind getKind()
  {
    return mEFAEventDecl.getKind();
  }

  public IdentifierProxy getIdentifier()
  {
    return mEFAEventDecl.getIdentifier();
  }

  public List<SimpleExpressionProxy> getRanges()
  {
    return mEFAEventDecl.getRanges();
  }

  @Override
  public boolean isObservable()
  {
    return mIsObservable;
  }

  @Override
  public Map<String, String> getAttributes()
  {
    return mEFAEventDecl.getAttributes();
  }

  public boolean isBlocked()
  {
    return mIsBlocked;
  }

  public void setBlocked()
  {
    mVariables.clear();
    mComponents.clear();
    mIsBlocked = true;
  }

  public void addComponent(final SimpleEFAComponent component)
  {
    mComponents.add(component.getIdentifier());
  }

  public void removeComponent(final SimpleEFAComponent component)
  {
    mComponents.remove(component.getIdentifier());
  }

  public TIntSet getVariables()
  {
    return mVariables;
  }

  public boolean isLocal()
  {
    return mComponents.size() == 1;
  }

  public boolean isProposition()
  {
    return mEFAEventDecl.getKind() == EventKind.PROPOSITION;
  }

  public boolean isLocalIn(final SimpleEFAComponent component)
  {
    return isLocal() && mComponents.contains(component.getIdentifier());
  }

  public void addVariable(final int var)
  {
    mVariables.add(var);
  }

  public void addPrimeVariable(final int var)
  {
    mPrimeVariables.add(var);
    mVariables.add(var);
  }

  public void addUnPrimeVariable(final int var)
  {
    mUnPrimeVariables.add(var);
    mVariables.add(var);
  }

  public void addAllVariable(final int[] var)
  {
    mVariables.addAll(var);
  }

  public void addAllPrimeVariable(final int[] var)
  {
    mPrimeVariables.addAll(var);
    mVariables.addAll(var);
  }

  public void addAllUnPrimeVariable(final int[] var)
  {
    mUnPrimeVariables.addAll(var);
    mVariables.addAll(var);
  }

  public Set<IdentifierProxy> getComponents()
  {
    return mComponents;
  }

  public TIntSet getPrimeVariables()
  {
    return mPrimeVariables;
  }

  public TIntSet getUnPrimeVariables()
  {
    return mUnPrimeVariables;
  }

  public void clearAllVariables()
  {
    mVariables.clear();
    mPrimeVariables.clear();
    mUnPrimeVariables.clear();
  }

  //#########################################################################
  //# Setters
  /**
   * Sets the kind of this event declaration.
   */
  public void setKind(final EventKind kind)
  {
    if (mKind == kind) {
      return;
    }
    mKind = kind;
  }

  /**
   * Sets the observability status of this event declaration.
   */
  public void setObservable(final boolean observable)
  {
    mIsObservable = observable;
  }

  @Override
  public boolean equals(final Object other)
  {
    if ((other != null) && (other.getClass() == getClass())) {
      final ModuleEqualityVisitor eq = new ModuleEqualityVisitor(false);
      final SimpleEFAEventDecl expected = (SimpleEFAEventDecl) other;
      return eq.isEqualList(Collections.singletonList(expected.mEFAEventDecl),
                            Collections.singletonList(mEFAEventDecl));
    }
    return false;
  }

  @Override
  public int hashCode()
  {
    final ModuleHashCodeVisitor hash = ModuleHashCodeVisitor.getInstance(false);
    return hash.getListHashCode(Collections.singletonList(mEFAEventDecl));
  }

    @Override
    public SimpleEFAEventDecl clone() {
      return new SimpleEFAEventDecl(getIdentifier(),getKind());
    }

    @Override
    public Class<EventProxy> getProxyInterface() {
        return EventProxy.class;
    }

    @Override
    public Object acceptVisitor(final ProxyVisitor visitor) throws VisitorException {
        return null;
    }

    @Override
    public String getName()
  {
    return mEFAEventDecl.getName();
  }

  //#########################################################################
  //# Data Members
  /**
   * The event declaration in the input module, from which this event is being
   * compiled.
   */
  private final EventDeclProxy mEFAEventDecl;
  /**
   * The <I>event variable set</I>, consisting of all the variables whose value
   * may change when this event occurs. This set contains only the EFA variable
   * objects for the current state of the concerned variables.
   */
  private final TIntHashSet mVariables;
  private final TIntHashSet mPrimeVariables;
  private final TIntHashSet mUnPrimeVariables;
  /**
   * The map that assigns to each automaton ({@link SimpleComponentProxy}) the
   * collection of transitions of this event that are to be associated with it.
   * Automata that do not have the event in their alphabet are not listed.
   * Automata that block the event are listed with an empty transition group.
   */
  private final Set<IdentifierProxy> mComponents;
  /**
   * A flag indicating that the event has been recognised as globally blocked.
   * In some cases, the EFA compiler can identify that an event is globally
   * disabled and can never cause a violation of a safety property. Such an
   * event is marked as blocked, and no transitions are generated for it.
   */
  private boolean mIsBlocked;
  private EventKind mKind;
  private boolean mIsObservable;

  @Override
  public int compareTo(final NamedProxy namedProxy) {
    return this.getName().compareTo(namedProxy.getName());
  }


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = -3739541318333865287L;

}
