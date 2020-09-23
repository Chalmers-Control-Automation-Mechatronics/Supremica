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

package net.sourceforge.waters.gui;

import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.gui.renderer.LabelShape;
import net.sourceforge.waters.model.module.GuardActionBlockProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.ModuleProxyCloner;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.SplineKind;
import net.sourceforge.waters.subject.base.IndexedSetSubject;
import net.sourceforge.waters.subject.module.BoxGeometrySubject;
import net.sourceforge.waters.subject.module.EdgeSubject;
import net.sourceforge.waters.subject.module.GeometryTools;
import net.sourceforge.waters.subject.module.GraphSubject;
import net.sourceforge.waters.subject.module.GroupNodeSubject;
import net.sourceforge.waters.subject.module.GuardActionBlockSubject;
import net.sourceforge.waters.subject.module.LabelBlockSubject;
import net.sourceforge.waters.subject.module.LabelGeometrySubject;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;
import net.sourceforge.waters.subject.module.NodeSubject;
import net.sourceforge.waters.subject.module.PointGeometrySubject;
import net.sourceforge.waters.subject.module.SimpleNodeSubject;


/**
 * A collection of utility methods for creation of new graphical elements
 * by the editors. This class contains only static methods.
 *
 * @author Robi Malik
 */

public class GraphTools {

  //#########################################################################
  //# Creation of Graphical Elements
  /**
   * Creates a simple node.
   * This method creates a new simple node with a new name that can be
   * safely inserted to the given graph.
   * @param  graph     The graph to receive the new node.
   * @param  pos       The position of the new node.
   */
  public static SimpleNodeSubject getCreatedSimpleNode
    (final GraphSubject graph, final Point2D pos)
  {
    final String name = findNewSimpleNodeName(graph);
    final IndexedSetSubject<NodeSubject> nodes = graph.getNodesModifiable();
    final boolean initial = nodes.isEmpty();
    final PointGeometrySubject geo = new PointGeometrySubject(pos);
    final LabelGeometrySubject label =
      new LabelGeometrySubject(LabelShape.DEFAULT_NODE_LABEL_OFFSET);
    return new SimpleNodeSubject(name, null, null, initial, geo, null, label);
  }

  /**
   * Creates a group node.
   * This method creates a new group node with a new name that can be
   * safely inserted to the given graph.
   * @param  graph     The graph to receive the new node.
   * @param  rect      The geometry of the new group node.
   */
  public static GroupNodeSubject getCreatedGroupNode
    (final GraphSubject graph, final Rectangle2D rect)
  {
    final String name = findNewGroupNodeName(graph);
    final BoxGeometrySubject geo = new BoxGeometrySubject(rect);
    return new GroupNodeSubject(name, null, null, null, geo);
  }

  /**
   * Creates an edge with empty label blocks.
   * @param  graph     The graph to receive the new edge.
   * @param  source    The source node of the new edge.
   * @param  target    The target node of the new edge.
   * @param  start     The start point to be used,
   *                   or <CODE>null</CODE> for simple source nodes.
   * @param  end       The end point to be used,
   *                   or <CODE>null</CODE> for simple target nodes.
   */
  public static EdgeSubject getCreatedEdge(final GraphSubject graph,
                                           final NodeSubject source,
                                           final NodeSubject target,
                                           final Point2D start,
                                           final Point2D end)
  {
    return getCreatedEdge(graph, source, target, start, end, null, null);
  }

  /**
   * Creates an edge.
   * @param  graph     The graph to receive the new edge.
   * @param  source    The source node of the new edge.
   * @param  target    The target node of the new edge.
   * @param  start     The start point to be used,
   *                   or <CODE>null</CODE> for simple source nodes.
   * @param  end       The end point to be used,
   *                   or <CODE>null</CODE> for simple target nodes.
   * @param labelBlock The label block for the new edge, or <CODE>null</CODE>
   *                   to create an empty one. To be cloned.
   * @param gaBlock    The guard/action block for the new edge,
   *                   or <CODE>null</CODE>. To be cloned.
   */
  public static EdgeSubject getCreatedEdge(final GraphSubject graph,
                                           final NodeSubject source,
                                           final NodeSubject target,
                                           final Point2D start,
                                           final Point2D end,
                                           final LabelBlockProxy labelBlock,
                                           final GuardActionBlockProxy gaBlock)
  {
    final PointGeometrySubject startGeo;
    if (source instanceof SimpleNodeSubject || start == null) {
      startGeo = null;
    } else {
      startGeo = new PointGeometrySubject(start);
    }
    final PointGeometrySubject endGeo;
    if (target instanceof SimpleNodeSubject || end == null) {
      endGeo = null;
    } else {
      endGeo = new PointGeometrySubject(end);
    }
    final ModuleProxyCloner cloner = ModuleSubjectFactory.getCloningInstance();
    final LabelBlockSubject labelClone =
      (LabelBlockSubject) cloner.getClone(labelBlock);
    final GuardActionBlockSubject gaClone =
      (GuardActionBlockSubject) cloner.getClone(gaBlock);
    final EdgeSubject edge = new EdgeSubject(source, target,
                                             labelClone, gaClone, null,
                                             startGeo, endGeo);
    GeometryTools.createDefaultGeometry(edge);


    Point2D newPoint = GeometryTools.getTurningPoint1(edge);
    final List<EdgeSubject> edges = graph.getEdgesModifiable();
    final List<EdgeSubject> selfLoops = new ArrayList<EdgeSubject>();
    for (final EdgeSubject e : edges) {
      if ((e.getSource() == edge.getSource() && e
        .getTarget() == edge.getTarget()) && e != edge) {
        selfLoops.add(e);
      }
    }

    final Point2D centrePoint = GeometryTools.getEndPoint(edge);
    final double dist =
      Point2D.distance(centrePoint.getX(), centrePoint.getY(),
                       newPoint.getX(), newPoint.getY());
    int count = 0;
    boolean found = false;
    double degreesFrom = -45;
    double degreesTo = 90;
    double degreesPrev = -45;
    int i = 8;
    while (!found) {
      found = true;
      for (final EdgeSubject e : selfLoops) {
        final Point2D edgePoint = GeometryTools.getTurningPoint1(e);
        if (Math.abs(edgePoint.getX() - newPoint.getX()) < 1
            && Math.abs(edgePoint.getY() - newPoint.getY()) < 1) {
          count++;
          if (count == 4) {
            degreesFrom = -90;
          }
          if(count == i){
            i *= 2;
            degreesPrev /= 2;
            degreesFrom -= degreesPrev;
            degreesTo /= 2;
          }
          degreesFrom += degreesTo;
          final double radians = Math.toRadians(degreesFrom);
          final double x =
            centrePoint.getX() + dist * Math.cos(radians);
          final double y =
            centrePoint.getY() + dist * Math.sin(radians);
          newPoint = new Point2D.Double(x, y);
          GeometryTools.setSpecifiedMidPoint(edge, newPoint,
                                             SplineKind.INTERPOLATING);
          found = false;
          break;
        }
      }
    }


    return edge;
  }


  //#########################################################################
  //# Finding Node Names
  /**
   * Determines a new name for a simple node in the given graph.
   */
  public static String findNewSimpleNodeName(final GraphSubject graph)
  {
    final Set<String> empty = Collections.emptySet();
    return findNewSimpleNodeName(graph, empty);
  }

  /**
   * Determines a new name for a group node in the given graph.
   */
  public static String findNewGroupNodeName(final GraphSubject graph)
  {
    final Set<String> empty = Collections.emptySet();
    return findNewGroupNodeName(graph, empty);
  }

  /**
   * Determines a new name for a simple node in the given graph.
   * @param  graph    The graph to receive the new node.
   * @param  alsoUsed A set of names to be considered as taken already.
   */
  public static String findNewSimpleNodeName(final GraphSubject graph,
                                             final Set<String> alsoUsed)
  {
    return findNewNodeName(graph, "S", alsoUsed);
  }

  /**
   * Determines a new name for a group node in the given graph.
   * @param  graph    The graph to receive the new group node.
   * @param  alsoUsed A set of names to be considered as taken already,
   *                  or <CODE>null</CODE>.
   */
  public static String findNewGroupNodeName(final GraphSubject graph,
                                            final Set<String> alsoUsed)
  {
    return findNewNodeName(graph, "G", alsoUsed);
  }


  private static String findNewNodeName(final GraphSubject graph,
                                        final String prefix,
                                        final Set<String> alsoUsed)
  {
    final IndexedSetSubject<NodeSubject> existing = graph.getNodesModifiable();
    int testindex = 0;
    String name;
    do {
      name = prefix + testindex++;
    } while (existing.containsName(name) || alsoUsed.contains(name));
    return name;
  }


  //#########################################################################
  //# Finding Initial States
  public static boolean hasMultipleInitialStates(final GraphSubject graph)
  {
    boolean found = false;
    for (final NodeSubject node : graph.getNodesModifiable()) {
      if (node instanceof SimpleNodeSubject) {
        final SimpleNodeSubject simple = (SimpleNodeSubject) node;
        if (simple.isInitial()) {
          if (found) {
            return true;
          } else {
            found = true;
          }
        }
      }
    }
    return false;
  }

  public static void setInitialArrowOffset(final SimpleNodeSubject node,
                                           final Point2D offset)
  {
    PointGeometrySubject geo = node.getInitialArrowGeometry();
    if (geo == null) {
      geo = new PointGeometrySubject(offset);
      node.setInitialArrowGeometry(geo);
    } else {
      geo.setPoint(offset);
    }
  }


  //#########################################################################
  //# Updating the Group Node Hierarchy
  public static boolean updateGroupNodeHierarchy(final GraphSubject graph)
  {
    final List<GroupNodeSubject> groups = new ArrayList<GroupNodeSubject>();
    final Map<GroupNodeSubject,List<NodeSubject>> newContainers =
      new HashMap<GroupNodeSubject,List<NodeSubject>>();
    for (final NodeSubject n : graph.getNodesModifiable()) {
      if (n instanceof GroupNodeSubject) {
        final GroupNodeSubject group = (GroupNodeSubject) n;
        groups.add(group);
        newContainers.put(group, new ArrayList<NodeSubject>());
      }
    }
    // Sort all the node groups from smallest to largest ...
    Collections.sort(groups, new Comparator<GroupNodeSubject>() {
      @Override
      public int compare(final GroupNodeSubject g1, final GroupNodeSubject g2)
      {
        final Rectangle2D r1 = g1.getGeometry().getRectangle();
        final Rectangle2D r2 = g2.getGeometry().getRectangle();
        return (int) ((r1.getHeight() * r1.getWidth())
                      - (r2.getHeight() * r2.getWidth()));
      }
    });
    // Go through all the nodes ...
    for (final NodeSubject n : graph.getNodesModifiable()) {
      final Rectangle2D r1;
      if (n instanceof GroupNodeSubject) {
        r1 = ((GroupNodeSubject) n).getGeometry().getRectangle();
      } else {
        final Point2D p = ((SimpleNodeSubject) n).getPointGeometry().getPoint();
        r1 = new Rectangle((int) p.getX(), (int) p.getY(), 1, 1);
      }
      final Collection<GroupNodeSubject> parents =
        new ArrayList<GroupNodeSubject>();
      mainloop:
      for (final GroupNodeSubject group : groups) {
        if (n != group) {
          if (group.getGeometry().getRectangle().contains(r1)) {
            for (final GroupNodeSubject parent : parents) {
              if (group.getGeometry().getRectangle().contains
                    (parent.getGeometry().getRectangle())) {
                continue mainloop;
              }
            }
            newContainers.get(group).add(n);
            parents.add(group);
          }
        }
      }
    }
    for (final GroupNodeSubject group : groups) {
      final ArrayList<NodeProxy> nodesToRemove = new ArrayList<NodeProxy>();
      final ArrayList<NodeProxy> nodesToAdd = new ArrayList<NodeProxy>();
      for (final NodeProxy node : group.getImmediateChildNodes())
      {
        if (!newContainers.get(group).contains(node)) {
          nodesToRemove.add(node);
        }
      }
      for (final NodeProxy node : newContainers.get(group)) {
        if (!group.getImmediateChildNodes().contains(node)) {
          nodesToAdd.add(node);
        }
      }
      for (final NodeProxy node : nodesToRemove) {
        group.getImmediateChildNodesModifiable().remove(node);
      }
      for (final NodeProxy node : nodesToAdd) {
        group.getImmediateChildNodesModifiable().add((NodeSubject)node);
      }
    }
    return !groups.isEmpty();
  }

}
