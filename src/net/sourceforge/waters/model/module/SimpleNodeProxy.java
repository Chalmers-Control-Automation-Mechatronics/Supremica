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

package net.sourceforge.waters.model.module;


/**
 * <P>A node representing a state.</P>
 *
 * <P>The states of a finite-state machine are represented as simple nodes
 * in a graph, connected to each other by edges representing the state
 * transitions. A simple node thus can be viewed as a point in a graph
 * ({@link GraphProxy}) with edges ({@link EdgeProxy}) linking to it.</P>
 *
 * <P>The information attached to a simple node is mostly used for
 * rendering.</P>
 * <DL>
 * <DT><STRONG>Name.</STRONG></DT>
 * <DD>A string identifying the node.</DD>
 * <DT><STRONG>Initial.</STRONG>
 * <DD>A boolean flag identifying whether the node represents an initial
 * state or not. In a <I>deterministic</I> graph, there must be exactly
 * one initial node.</DD>
 * <DT><STRONG>Geometry information.</STRONG></DT>
 * <DD>In addition, each node may have layout information to describe
 * the node's position in the graph, and the placement of the label, i.e.,
 * the name string, relative to the node.</DD>
 * </DL>
 *
 * @author Robi Malik
 */

public interface SimpleNodeProxy extends NodeProxy {


  //#########################################################################
  //# Getters and Setters
  /**
   * Gets the initial status of this node.
   * @return <CODE>true</CODE> if this is an initial node,
   *         <CODE>false</CODE> otherwise.
   */
  // @default false
  public boolean isInitial();

  /**
   * Gets the geometric position of this node.
   * @return A {@link PointGeometryProxy} identifying the position of the
   *         center of the circle representing this node in a graph.
   */
  public PointGeometryProxy getPointGeometry();

  /**
   * Gets the position of the initial state arrow of this node. Initial
   * nodes can be rendered using a small arrow pointing against the node.
   * This attribute defines the relative position of the start point of
   * that arrow. It only make sense for initial nodes and should be
   * <CODE>null</CODE> in all other cases.
   * @return A {@link PointGeometryProxy} identifying the start point
   *         of the initial state arrow relative to the centre of the node.
   */
  public PointGeometryProxy getInitialArrowGeometry();

  /**
   * Gets the geometric position of the label of this node.
   * @return A {@link PointGeometryProxy} identifying the position of the
   *         label's anchor point relative to the center of the circle
   *         representing the node.
   */
  public LabelGeometryProxy getLabelGeometry();

}
