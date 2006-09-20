//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.module
//# CLASS:   GraphProxy
//###########################################################################
//# $Id: GraphProxy.java,v 1.4 2006-09-20 16:24:12 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.module;

import java.util.Collection;
import java.util.Set;

import net.sourceforge.waters.model.base.Proxy;


/**
 * A graph representing a finite-state machine.
 *
 * @author Robi Malik
 */

public interface GraphProxy extends Proxy {


  //#########################################################################
  //# Getters and Setters
  /**
   * Gets the determinism status of this graph.
   * A graph can be marked as <I>deterministic</I> or <I>nondeterministic</I>.
   * In a deterministic graph, there can be only one initial node,
   * and nodes can have at most one outgoing edge for each event.
   * Because of the possibility of instantiation, these conditions can only
   * be checked by a compiler with full accuracy, but editors can also use
   * this flag to perform some preliminary checks.
   * @return <CODE>true</CODE> if the graph is to produce a deterministic
   *         finite-state machine, <CODE>false</CODE> otherwise.
   */
  // @default true
  public boolean isDeterministic();

  /**
   * Gets the list of blocked events of this graph.
   * The blocked event list of an automaton defines a list of additional
   * events which the automaton synchronises on, but which are not
   * necessarily enabled in any of its states. This makes it possible to
   * specify that certain events globally disabled in any system where an
   * automaton is used.
   */
  // @default empty
  public LabelBlockProxy getBlockedEvents();

  /**
   * Gets the set of nodes of this graph.
   */
  public Set<NodeProxy> getNodes();

  /**
   * Gets the collection of edges of this graph.
   * Although duplicate edges are meaningless, implementations are not
   * required to check for duplicates, so the collection returned by
   * this method may or may not contain duplicate entries.
   */
  public Collection<EdgeProxy> getEdges();

}
