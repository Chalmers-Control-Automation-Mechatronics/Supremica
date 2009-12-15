//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters GUI
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   GraphTools
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import net.sourceforge.waters.gui.renderer.GeometryTools;
import net.sourceforge.waters.gui.renderer.LabelProxyShape;
import net.sourceforge.waters.model.module.GuardActionBlockProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.ModuleProxyCloner;
import net.sourceforge.waters.subject.base.IndexedSetSubject;
import net.sourceforge.waters.subject.module.BoxGeometrySubject;
import net.sourceforge.waters.subject.module.EdgeSubject;
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
      new LabelGeometrySubject(LabelProxyShape.DEFAULT_OFFSET);
    final PointGeometrySubject initgeo =
      initial ? new PointGeometrySubject(new Point(-5, -5)) : null;
    return new SimpleNodeSubject(name, null, initial, geo, initgeo, label);
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
    return new GroupNodeSubject(name, null, null, geo);
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
   * @param  alsUsed  A set of names to be considered as taken already.
   */
  public static String findNewSimpleNodeName(final GraphSubject graph,
                                             final Set<String> alsoUsed)
  {
    return findNewNodeName(graph, "S", alsoUsed);
  }

  /**
   * Determines a new name for a group node in the given graph.
   * @param  graph    The graph to receive the new group node.
   * @param  alsUsed  A set of names to be considered as taken already,
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


  //#########################################################################
  //# Updating the Group Node Hierarchy
  public static boolean updateGroupNodeHierarchy(final GraphSubject graph)
  {
    final List<GroupNodeSubject> groups = new ArrayList<GroupNodeSubject>();
    for (final NodeSubject n : graph.getNodesModifiable()) {
      if (n instanceof GroupNodeSubject) {
        ((GroupNodeSubject) n).getImmediateChildNodesModifiable().clear();
        groups.add((GroupNodeSubject) n);
      }
    }
    // Sort all the node groups from smallest to largest ...
    Collections.sort(groups, new Comparator<GroupNodeSubject>() {
      public int compare(GroupNodeSubject g1, GroupNodeSubject g2)
      {
        Rectangle2D r1 = g1.getGeometry().getRectangle();
        Rectangle2D r2 = g2.getGeometry().getRectangle();
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
        Point2D p = ((SimpleNodeSubject) n).getPointGeometry().getPoint();
        r1 = new Rectangle((int) p.getX(), (int) p.getY(), 1, 1);
      }
      Collection<GroupNodeSubject> parents = new ArrayList<GroupNodeSubject>();
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
            group.getImmediateChildNodesModifiable().add(n);
            parents.add(group);
          }
        }
      }
    }
    return !groups.isEmpty();
  }

}