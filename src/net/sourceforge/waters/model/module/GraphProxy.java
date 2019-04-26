//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
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

package net.sourceforge.waters.model.module;

import java.util.Collection;
import java.util.Set;

import net.sourceforge.waters.model.base.Proxy;


/**
 * A graph representing a finite-state machine.
 *
 * @author Robi Malik
 */

public interface GraphProxy extends Proxy
{

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
   * @return The label block of blocked events or <CODE>null</CODE> to
   *         indicate that there are no blocked events.
   */
  // @optional
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
