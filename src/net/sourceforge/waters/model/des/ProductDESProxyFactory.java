//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.des
//# CLASS:   ProductDESProxyFactory
//###########################################################################
//# $Id: ProductDESProxyFactory.java,v 1.3 2005-11-03 03:45:57 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.des;

import java.io.File;
import java.util.Collection;

import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;


public interface ProductDESProxyFactory
{
  /**
   * Creates a new automaton.
   * @param name         The name of the new automaton.
   * @param kind         The kind (<I>plant</I>, <I>specification</I>, etc.)
   *                     of the new automaton.
   * @param events       The event alphabet of the new automaton.
   * @param states       The list of states of the new automaton.
   * @param transitions  The list of transitions of the new automaton.
   */
  public AutomatonProxy createAutomatonProxy
      (String name,
       ComponentKind kind,
       Collection<? extends EventProxy> events,
       Collection<? extends StateProxy> states,
       Collection<? extends TransitionProxy> transitions);

  /**
   * Creates a new automaton using default values.
   * This method creates an automaton with empty lists of events,
   * states, and transitions.
   * @param name         The name of the new automaton.
   * @param kind         The kind (<I>plant</I>, <I>specification</I>, etc.)
   *                     of the new automaton.
   */
  public AutomatonProxy createAutomatonProxy
      (String name,
       ComponentKind kind);

  /**
   * Creates a new event.
   * @param name         The name of the new event.
   * @param kind         The kind of the new event.
   * @param observable   The observability status of the new event.
   */
  public EventProxy createEventProxy
      (String name,
       EventKind kind,
       boolean observable);

  /**
   * Creates a new event using default values.
   * This method creates an observable event.
   * @param name         The name of the new event.
   * @param kind         The kind of the new event.
   */
  public EventProxy createEventProxy
      (String name,
       EventKind kind);

  /**
   * Creates a new product DES.
   * @param name         The name of the new product DES.
   * @param location     The file location of the new product DES,
   *                     or <CODE>null</CODE>.
   * @param events       The event alphabet of the new product DES.
   * @param automata     The list of automata of the new product DES.
   */
  public ProductDESProxy createProductDESProxy
      (String name,
       File location,
       Collection<? extends EventProxy> events,
       Collection<? extends AutomatonProxy> automata);

  /**
   * Creates a new product DES using default values.
   * This method creates a product DES with a <CODE>null</CODE> file location.
   * @param name         The name of the new product DES.
   * @param events       The event alphabet of the new product DES.
   * @param automata     The list of automata of the new product DES.
   */
  public ProductDESProxy createProductDESProxy
      (String name,
       Collection<? extends EventProxy> events,
       Collection<? extends AutomatonProxy> automata);

  /**
   * Creates a new product DES using default values.
   * This method creates a product DES with a <CODE>null</CODE> file location,
   * and empty lists of events and automata.
   * @param name         The name of the new product DES.
   */
  public ProductDESProxy createProductDESProxy
      (String name);

  /**
   * Creates a new state.
   * @param name         The name of the new state.
   * @param initial      The initial status of the new state.
   * @param propositions The list of propositions of the new state.
   */
  public StateProxy createStateProxy
      (String name,
       boolean initial,
       Collection<? extends EventProxy> propositions);

  /**
   * Creates a new state using default values.
   * This method creates a state that is not initial and has no propositions.
   * @param name         The name of the new state.
   */
  public StateProxy createStateProxy
      (String name);

  /**
   * Creates a new transition.
   * @param source       The source state of the new transition.
   * @param event        The event labelling the new transition.
   * @param target       The target state of the new transition.
   */
  public TransitionProxy createTransitionProxy
      (StateProxy source,
       EventProxy event,
       StateProxy target);

}
