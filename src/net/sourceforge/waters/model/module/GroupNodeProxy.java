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


/**
 * <P>A hierarchical node that can contain other nodes.</P>
 *
 * <P>Group nodes can be used to combine several nodes in a graph into a
 * single node, in order to reduce the number of edges in certain cases.
 * When translating a graph into an automaton, each edge that starts at a
 * group nodes is translated into transitions originating from each simple
 * node contained in that group node.</P>
 *
 * <P>In a nondeterministic graph, edges can also end in a group node.
 * In this case, the translated automaton contains transitions into each
 * simple node contained in that group node.</P>
 *
 * <P>Graphically, group nodes are represented as boxes. All nodes contained
 * in the area of the rectangle are considered as belonging to the group
 * node. Group nodes can overlap and be contained within each other.</P>
 *
 * <P>This group node class is only concerned with the logical grouping
 * structure. Its methods enable the user to access and change the set of
 * simple nodes or group nodes that are considered as immediately
 * contained. But it does not ensure that this information is consistent
 * with the geometric information.</P>
 *
 * @author Robi Malik
 */

public interface GroupNodeProxy extends NodeProxy
{

  //#########################################################################
  //# Getters and Setters
  /**
   * Gets the geometric information of this node.
   * @return A {@link BoxGeometryProxy} identifying the position and size
   *         of the box representing this group node in a graph.
   */
  public BoxGeometryProxy getGeometry();

}
