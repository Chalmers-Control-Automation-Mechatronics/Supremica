//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2020 Robi Malik
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

import net.sourceforge.waters.model.base.Proxy;


/**
 * <P>An edge in a graph.</P>
 *
 * <P>The transitions between the states of a finite-state machine are
 * represented as edges between the nodes of a graph.  These edges are
 * directed, i.e., they can be considered as arrows that link two nodes
 * ({@link NodeProxy}), called the <I>source</I> and <I>target</I>. In
 * addition, each edge has an associated set of <I>labels</I> that
 * represent the events for the transition. Each edge can have several
 * labels in its label block ({@link LabelBlockProxy}), and each label will
 * produce its own transition when the graph is translated into a
 * finite-state machine.</P>
 *
 * <P>The way how an edge is to be rendered graphically is stored in
 * separate <I>geometry</I> objects.</P>
 *
 * @author Robi Malik
 */

public interface EdgeProxy extends Proxy {

  //#########################################################################
  //# Getters
  /**
   * Gets the source node of this edge.
   */
  // @ref
  // @optional
  public NodeProxy getSource();

  /**
   * Gets the target node of this edge.
   */
  // @ref
  // @optional
  public NodeProxy getTarget();

  /**
   * Gets the label block of this edge. The label block contains a list
   * of labels, each representing one event and therefore one transition
   * in the automaton obtained from this graph.
   */
  // @default empty  
  public LabelBlockProxy getLabelBlock();

  // @optional
  public GuardActionBlockProxy getGuardActionBlock();

  /**
   * Gets the rendering information for this edge.
   * @return A spline geometry object containing the control points
   *         for the edge, or <CODE>null</CODE> if the edge is rendered as
   *         a straight line. The spline geometry does not contain the
   *         start and end points of the edge, as these can be obtained
   *         from the position of the source and target nodes, or the
   *         #getStartPoint() and #getEndPoint() methods.
   */
  public SplineGeometryProxy getGeometry();

  /**
   * Gets the rendering information for the start point of this edge.
   * For most edges, the location of the start point is the position of
   * the source node. But in some cases, namely if the source node
   * is a group node ({@link GroupNodeProxy}), an alternative start
   * position may be specified.
   * @return A point geometry object defining an alternative start
   *         point, or <CODE>null</CODE> if the start point is obtained
   *         from the position of the source node.
   */
  public PointGeometryProxy getStartPoint();

  /**
   * Gets the rendering information for the end point of this edge.
   * For most edges, the location of the end point is the position of the
   * target node. But in some cases, namely if the target node is a group
   * node ({@link GroupNodeProxy}), an alternative end position may be
   * specified. This only makes sense in a nondeterministic graph.
   * @return A point geometry object defining an alternative end
   *         point, or <CODE>null</CODE> if the end point is obtained
   *         from the position of the target node.
   */
  public PointGeometryProxy getEndPoint();

}
