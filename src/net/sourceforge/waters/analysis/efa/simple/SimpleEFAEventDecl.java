//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFA Analysis
//# PACKAGE: net.sourceforge.waters.model.compiler.efa
//# CLASS:   EFAEventDecl
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa.simple;

import gnu.trove.set.hash.THashSet;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;
import net.sourceforge.waters.model.module.ModuleHashCodeVisitor;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.subject.module.EventDeclSubject;
import net.sourceforge.waters.xsd.base.EventKind;

/**
 * <P>A compiler-internal representation of an event group in a system of
 * synchronised EFA.</P>
 * <p/>
 * <P>Each compiled event represents a single event declaration ({@link
 * EventDeclProxy}) of the input module, which has been flattened so as not to
 * allow any further array indexes. EFA compilation may split this event,
 * creating individual events of type {@link EFAEvent} for different value
 * combinations of EFA variable components.</P>
 * <p/>
 * @author Robi Malik
 * @see {@link EFACompiler}.
 */
public class SimpleEFAEventDecl
{

  //#########################################################################
  //# Constructors
  public SimpleEFAEventDecl(final EventDeclProxy decl)
  {
    mEFAEventDecl = decl;
    mVariables = new THashSet<>();
    mPrimeVariables = new THashSet<>();
    mUnPrimeVariables = new THashSet<>();
    mComponents = new THashSet<>();
    mIsObservable = decl.isObservable();
    mKind = decl.getKind();
  }

  public SimpleEFAEventDecl(final IdentifierProxy identifier,
                            final EventKind kind)
  {
    mEFAEventDecl = new EventDeclSubject(identifier, kind);
    mVariables = new THashSet<>();
    mPrimeVariables = new THashSet<>();
    mUnPrimeVariables = new THashSet<>();
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

  public boolean isObservable()
  {
    return mEFAEventDecl.isObservable();
  }

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

  public void addVariable(final SimpleEFAVariable var)
  {
    mVariables.add(var);
  }

  public void addPrimeVariable(final SimpleEFAVariable var)
  {
    mPrimeVariables.add(var);
    mVariables.add(var);
  }

  public void addUnPrimeVariable(final SimpleEFAVariable var)
  {
    mUnPrimeVariables.add(var);
    mVariables.add(var);
  }

  public void addAllVariable(final Collection<SimpleEFAVariable> var)
  {
    mVariables.addAll(var);
  }

  public void addAllPrimeVariable(final Collection<SimpleEFAVariable> var)
  {
    mPrimeVariables.addAll(var);
    mVariables.addAll(var);
  }

  public void addAllUnPrimeVariable(final Collection<SimpleEFAVariable> var)
  {
    mUnPrimeVariables.addAll(var);
    mVariables.addAll(var);
  }

  public Set<SimpleEFAVariable> getVariables()
  {
    return mVariables;
  }

  public Set<SimpleEFAVariable> getPrimeVariables()
  {
    return mPrimeVariables;
  }

  public Set<SimpleEFAVariable> getUnPrimeVariables()
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
    if (mKind.equals(kind)) {
      return;
    }
    mKind = kind;
  }

  /**
   * Sets the observability status of this event declaration.
   */
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
      final ModuleEqualityVisitor eq =
       ModuleEqualityVisitor.getInstance(false);
      final SimpleEFAEventDecl expected = (SimpleEFAEventDecl) other;
      return eq
       .isEqualList(Collections.singletonList(expected.getEventDecl()),
                    Collections.singletonList(mEFAEventDecl));
    }
    return false;
  }

  @Override
  public int hashCode()
  {
    final ModuleHashCodeVisitor hash =
     ModuleHashCodeVisitor.getInstance(false);
    return hash.getListHashCode(Collections.singletonList(this.mEFAEventDecl));
  }

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
   * <p/>
   * @see {@link EFACompiler}.
   */
  private final Set<SimpleEFAVariable> mVariables;
  private final Set<SimpleEFAVariable> mPrimeVariables;
  private final Set<SimpleEFAVariable> mUnPrimeVariables;
  /**
   * The map that assigns to each automaton ({@link SimpleComponentProxy}) the
   * collection of transitions of this event that are to be associated with it.
   * Automata that do not have the event in their alphabet are not listed.
   * Automata that block the event are listed with an empty transition group.
   */
  private final Set<SimpleEFAComponent> mComponents;
  /**
   * A flag indicating that the event has been recognised as globally blocked.
   * In some cases, the EFA compiler can identify that an event is globally
   * disabled and can never cause a violation of a safety property. Such an
   * event is marked as blocked, and no transitions are generated for it.
   */
  private boolean mIsBlocked;
  private EventKind mKind;
  private boolean mIsObservable;
  
}
