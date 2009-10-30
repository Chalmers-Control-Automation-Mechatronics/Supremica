//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.module
//# CLASS:   GroupNodeProxy
//###########################################################################
//# $Id$
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
