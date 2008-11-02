//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.efa
//# CLASS:   EFAEvent
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler.efa;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.sourceforge.waters.model.base.ProxyAccessorMap;
import net.sourceforge.waters.model.base.ProxyAccessorHashMapByContents;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;

import net.sourceforge.waters.xsd.base.EventKind;


/**
 * <P>A compiler-internal representation of an event group in a system
 * of synchronised EFA.</P>
 *
 * <P>Each compiled event represents a single event declaration ({@link
 * EventDeclProxy}) of the input module, which has been flattened so as not
 * to allow any further array indexes. EFA compilation may split this
 * event, creating individual events for different value combinations of
 * EFA variable components ({@link
 * net.sourceforge.waters.model.module.VariableComponentProxy
 * VariableComponentProxy}).</P>
 *
 * @see {@link EFACompiler}.
 * @author Robi Malik
 */

class EFAEvent {

  //#########################################################################
  //# Constructors
  EFAEvent(final EventDeclProxy decl)
  {
    mEventDecl = decl;
    mVariables = new ProxyAccessorHashMapByContents<IdentifierProxy>();
    mTransitionMap = new HashMap<SimpleComponentProxy,EFATransitionGroup>();
  }


  //#########################################################################
  //# Simple Access
  EventDeclProxy getEventDecl()
  {
    return mEventDecl;
  }

  EventKind getKind()
  {
    return mEventDecl.getKind();
  }

  EFATransitionGroup getTransitionGroup(final SimpleComponentProxy comp)
  {
    return mTransitionMap.get(comp);
  }

  Collection<EFATransitionGroup> getTransitionGroups()
  {
    return mTransitionMap.values();
  }

  void addVariable(final IdentifierProxy ident)
  {
    mVariables.addProxy(ident);
  }

  void addVariables(final Collection<? extends IdentifierProxy> idents)
  {
    mVariables.addAll(idents);
  }

  void addTransitions(final SimpleComponentProxy comp,
                      final CompiledGuard guard,
                      final IdentifierProxy label)
  {
    EFATransitionGroup trans = mTransitionMap.get(comp);
    if (trans == null) {
      trans = new EFATransitionGroup(comp);
      mTransitionMap.put(comp, trans);
    }
    trans.addTransitions(guard, label);
  }


  //#########################################################################
  //# Data Members
  /**
   * The event declaration in the input module,
   * from which this event is being compiled.
   */
  private final EventDeclProxy mEventDecl;
  /**
   * The <I>event variable set</I>, consisting of all the variables
   * whose value may change when this event occurs.
   * @see {@link EFACompiler}.
   */
  private final ProxyAccessorMap<IdentifierProxy> mVariables;
  /**
   * The map that assigns to each automaton ({@link SimpleComponentProxy})
   * the collection of transitions of this event that are to be associated
   * with it. Automata that do not have the event in their alphabet are
   * not listed.
   */
  private final Map<SimpleComponentProxy,EFATransitionGroup> mTransitionMap;

}
