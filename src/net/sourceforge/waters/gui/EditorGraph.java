//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
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

import gnu.trove.set.hash.THashSet;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.gui.command.AbstractEditCommand;
import net.sourceforge.waters.gui.command.Command;
import net.sourceforge.waters.gui.command.CompoundCommand;
import net.sourceforge.waters.gui.command.DeleteCommand;
import net.sourceforge.waters.gui.command.EditCommand;
import net.sourceforge.waters.gui.command.InsertCommand;
import net.sourceforge.waters.gui.command.MoveCommand;
import net.sourceforge.waters.gui.command.UpdateCommand;
import net.sourceforge.waters.gui.renderer.GuardActionBlockProxyShape;
import net.sourceforge.waters.gui.renderer.LabelBlockProxyShape;
import net.sourceforge.waters.gui.transfer.InsertInfo;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.module.ConditionalProxy;
import net.sourceforge.waters.model.module.DefaultModuleProxyVisitor;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.ForeachProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.GroupNodeProxy;
import net.sourceforge.waters.model.module.GuardActionBlockProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.LabelGeometryProxy;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;
import net.sourceforge.waters.model.module.ModuleProxyVisitor;
import net.sourceforge.waters.model.module.NestedBlockProxy;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.model.module.SplineKind;
import net.sourceforge.waters.subject.base.ArrayListSubject;
import net.sourceforge.waters.subject.base.IndexedHashSetSubject;
import net.sourceforge.waters.subject.base.IndexedSetSubject;
import net.sourceforge.waters.subject.base.ListSubject;
import net.sourceforge.waters.subject.base.ModelChangeEvent;
import net.sourceforge.waters.subject.base.ModelObserver;
import net.sourceforge.waters.subject.base.MutableSubject;
import net.sourceforge.waters.subject.base.ProxySubject;
import net.sourceforge.waters.subject.base.Subject;
import net.sourceforge.waters.subject.module.BoxGeometrySubject;
import net.sourceforge.waters.subject.module.ConditionalSubject;
import net.sourceforge.waters.subject.module.EdgeSubject;
import net.sourceforge.waters.subject.module.ForeachSubject;
import net.sourceforge.waters.subject.module.GeometryTools;
import net.sourceforge.waters.subject.module.GraphSubject;
import net.sourceforge.waters.subject.module.GroupNodeSubject;
import net.sourceforge.waters.subject.module.GuardActionBlockSubject;
import net.sourceforge.waters.subject.module.LabelBlockSubject;
import net.sourceforge.waters.subject.module.LabelGeometrySubject;
import net.sourceforge.waters.subject.module.NestedBlockSubject;
import net.sourceforge.waters.subject.module.NodeSubject;
import net.sourceforge.waters.subject.module.PointGeometrySubject;
import net.sourceforge.waters.subject.module.SimpleExpressionSubject;
import net.sourceforge.waters.subject.module.SimpleNodeSubject;
import net.sourceforge.waters.subject.module.SplineGeometrySubject;


/**
 * <P>A double-up graph used by a {@link GraphEditorPanel} during drag
 * operations.</P>
 *
 * <P>When a drag action is initiated in the {@link GraphEditorPanel}, it
 * creates a so-called secondary graph of type <CODE>EditorGraph</CODE>.
 * This is a full implementation of the {@link GraphProxy} interface, which
 * is passed to the renderers for display. It is updated continuously during
 * the course of the drag operation, but these changes are not written through
 * to the model immediately.</P>
 *
 * <P>When the drag operation is completed, the <CODE>EditorGraph</CODE>
 * computes a single command object that represents all changes applied to
 * the double-up graph, and this command is registered with the undo
 * manager for execution on the main model.</P>
 *
 * @see GraphEditorPanel
 * @see Command
 *
 * @author Simon Ware, Robi Malik
 */

class EditorGraph
  extends MutableSubject
  implements GraphProxy
{
  EditorGraph(final GraphSubject graph)
  {
    mGraph = graph;
    mObserverMap = new HashMap<NodeSubject,EditorNode>
      (graph.getNodes().size());
    mFakeMap = new HashMap<ProxySubject,ChangeRecord>
      (graph.getNodes().size() + graph.getEdges().size()+1);
    mOriginalMap = new HashMap<ProxySubject,ChangeRecord>
      (graph.getNodes().size() + graph.getEdges().size()+1);
    mFakeGetter = new FakeGetterVisitor();
    mOriginalGetter = new OriginalGetterVisitor();
    mChangeRecordCreator = new ChangeRecordCreatorVisitor();
    mEdges = new ArrayListSubject<EdgeSubject>(graph.getEdges().size());
    mNodes = new IndexedHashSetSubject<NodeSubject>(graph.getNodes().size());
    mEdges.setParent(this);
    mNodes.setParent(this);
    if (graph.getBlockedEvents() != null) {
      final LabelBlockSubject blocked = graph.getBlockedEvents();
      mBlockedEvents = blocked.clone();
      mBlockedEvents.setParent(this);
      mChangeRecordCreator.createChangeRecordsForLabelBlock(blocked, mBlockedEvents);
    } else {
      mBlockedEvents = null;
    }

    for (final NodeSubject node : graph.getNodesModifiable()) {
      if (node instanceof SimpleNodeSubject) {
        final SimpleNodeSubject simple = (SimpleNodeSubject) node;
        addSimpleNode(simple);
      } else if (node instanceof GroupNodeSubject) {
        final GroupNodeSubject group = (GroupNodeSubject) node;
        addGroupNode(group);
      }
    }
    for (final EdgeSubject edge : graph.getEdgesModifiable()) {
      if (edge.getSource() != null && edge.getTarget() != null) {
        addEdge(edge);
      } else {
        throw new NullPointerException
          ("Can't import edge without source or target into secondary graph!");
      }
    }

    final CopiedModelListener listener = new CopiedModelListener();
    addModelObserver(listener);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.module.GraphProxy
  @Override
  public Class<GraphProxy> getProxyInterface()
  {
    return GraphProxy.class;
  }

  @Override
  public Object acceptVisitor(final ProxyVisitor visitor)
    throws VisitorException
  {
    final ModuleProxyVisitor mvisitor = (ModuleProxyVisitor) visitor;
    return mvisitor.visitGraphProxy(this);
  }

  @Override
  public LabelBlockProxy getBlockedEvents()
  {
    return mBlockedEvents;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Set<NodeProxy> getNodes()
  {
    final Set<?> precast = mNodes;
    final Set<NodeProxy> downcast = (Set<NodeProxy>) precast;
    return Collections.unmodifiableSet(downcast);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<EdgeProxy> getEdges()
  {
    final List<?> precast = mEdges;
    final List<EdgeProxy> downcast = (List<EdgeProxy>) precast;
    return Collections.unmodifiableList(downcast);
  }

  public Set<NodeSubject> getNodesModifiable()
  {
    return mNodes;
  }

  public List<EdgeSubject> getEdgesModifiable()
  {
    return mEdges;
  }

  @Override
  public boolean isDeterministic()
  {
    return mGraph.isDeterministic();
  }


  //#########################################################################
  //# Accessing the Fake Maps
  ProxySubject getCopy(final ProxySubject original)
  {
    return mFakeGetter.getFake(original);
  }

  ProxySubject getOriginal(final ProxySubject fake)
  {
    return mOriginalGetter.getOriginal(fake);
  }

  Collection<EdgeSubject> getNodeEdges(final NodeSubject n)
  {
    return Collections.unmodifiableCollection(mObserverMap.get(n).getEdges());
  }


  //#########################################################################
  //# Updating
  /**
   * Creates a command to transform the original graph into the same
   * state as this editor graph.
   * Presently, the following modifications are recognised and included
   * in the command.
   * <UL>
   * <LI>Changes of geometry of all graphical objects.</LI>
   * <LI>Addition and removal of nodes and group nodes.</LI>
   * <LI>Addition and removal of edges.</LI>
   * <LI>Change of the source or target of edges.</LI>
   * </UL>
   * @param  surface      The selection owner, used to select and
   *                      and deselect items if needed.
   * @param  description  The name to be given to the new command.
   * @param  selecting    <CODE>true</CODE> if any created items
   *                      are to be selected.
   * @return The command that will transform the graph, or <CODE>null</CODE>
   *         if no change has been detected.
   */
  Command createUpdateCommand(final GraphEditorPanel surface,
                              final String description,
                              final boolean selecting)
  {
    final List<ProxySubject> modified = new LinkedList<ProxySubject>();
    final List<ProxySubject> removed = new LinkedList<ProxySubject>();
    int minpass = Integer.MAX_VALUE;
    int maxpass = Integer.MIN_VALUE;
    int count = 0;
    for (final ChangeRecord record : mFakeMap.values()) {
      final ProxySubject original = record.createOriginal();
      switch (record.getChangeKind()) {
      case ModelChangeEvent.ITEM_ADDED:
        // Can't use objects from the secondary graph,
        // they will be duplicated when creating an InsertCommand ...
        count ++;
        break;
      case ModelChangeEvent.ITEM_REMOVED:
        count ++;
        removed.add(original);
        break;
      case ModelChangeEvent.STATE_CHANGED:
      case ModelChangeEvent.GEOMETRY_CHANGED:
        count ++;
        modified.add(original);
        break;
      default:
        if (record.hasImplicitChanges()) {
          count ++;
          // don't add to modified---not selected after undo
        }
        break;
      }
      final int minpass1 = record.getMinPass();
      if (minpass1 < minpass) {
        minpass = minpass1;
      }
      final int maxpass1 = record.getMaxPass();
      if (maxpass1 > maxpass) {
        maxpass = maxpass1;
      }
    }
    if (count == 0) {
      return null;
    }
    final List<Proxy> added = new LinkedList<>();
    final List<AbstractEditCommand> commands =
      new ArrayList<AbstractEditCommand>(count);
    for (int pass = minpass; pass <= maxpass; pass++) {
      for (final ChangeRecord record : mFakeMap.values()) {
        final AbstractEditCommand cmd = record.getUpdateCommand(surface, pass);
        if (cmd != null) {
          cmd.setUpdatesSelection(false);
          commands.add(cmd);
          final List<Proxy> created = cmd.getSelectionAfterInsert();
          added.addAll(created);
        }
      }
    }
    switch (commands.size()) {
    case 0:
      return null;
    case 1:
      final AbstractEditCommand cmd = commands.iterator().next();
      cmd.setUpdatesSelection(selecting);
      return cmd;
    default:
      final CompoundCommand compound;
      if (surface == null) {
        compound = new CompoundCommand(description);
      } else {
        removeDanglingEdges(modified);
        compound = new UpdateCommand(modified, added, removed,
                                     surface, description, selecting);
      }
      compound.addCommands(commands);
      compound.end();
      return compound;
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  /**
   * Creates a copy of the given edge and adds it to this graph.
   * @param  edge0   The edge in the original graph a copy of which
   *                 is to be added.
   */
  private EdgeSubject addEdge(final EdgeSubject edge0)
  {
    final EdgeSubject edge1 = edge0.clone();
    final NodeSubject source0 = edge0.getSource();
    final NodeSubject target0 = edge0.getTarget();
    if (source0 != null) {
      final NodeSubject source1 = (NodeSubject) getCopy(source0);
      edge1.setSource(source1);
      final EditorNode node = mObserverMap.get(source1);
      node.addEdge(edge1);
    }
    if (target0 != null) {
      final NodeSubject target1 = (NodeSubject) getCopy(target0);
      edge1.setTarget(target1);
      final EditorNode node = mObserverMap.get(target1);
      node.addEdge(edge1);
    }
    mEdges.add(edge1);
    new EdgeChangeRecord(edge0, edge1);
    return edge1;
  }

  private void addGroupNode(final GroupNodeSubject group0)
  {
    if (group0.getGeometry() == null) {
      throw new IllegalArgumentException("Group node " + group0 +
                                         " has no geometry!");
    } else {
      final GroupNodeSubject group1 = group0.clone();
      mNodes.add(group1);
      mObserverMap.put(group1, new EditorGroupNode(group1));
      new GroupNodeChangeRecord(group0, group1);
    }
  }

  private void addSimpleNode(final SimpleNodeSubject node0)
  {
    final SimpleNodeSubject node1 = node0.clone();
    mNodes.add(node1);
    final EditorSimpleNode esn =  new EditorSimpleNode(node1);
    mObserverMap.put(node1, esn);
    new SimpleNodeChangeRecord(node0, node1);
  }

  @SuppressWarnings("unlikely-arg-type")
  private void removeDanglingEdges(final Collection<ProxySubject> items)
  {
    final Set<ProxySubject> set = new THashSet<>(items);
    final Iterator<ProxySubject> iter = items.iterator();
    while (iter.hasNext()) {
      final ProxySubject item = iter.next();
      if (item instanceof EdgeProxy) {
        final EdgeProxy edge = (EdgeProxy) item;
        if (!set.contains(edge.getLabelBlock()) &&
            (!set.contains(edge.getSource()) ^
             !set.contains(edge.getTarget()))) {
          iter.remove();
        }
      }
    }
  }


  //#########################################################################
  //# Transformations
  /**
   * Moves a simple node.  This method is called to translate
   * the position of a simple node by a given fixed amount.
   * @param  node0   The node in the original graph.
   * @param  dx      The distance in x direction to be added to the
   *                 original position to obtain the new position.
   * @param  dy      The distance in y direction to be added to the
   *                 original position to obtain the new position.
   */
  void moveSimpleNode(final SimpleNodeSubject node0,
                      final double dx,
                      final double dy)
  {
    final SimpleNodeSubject node1 = (SimpleNodeSubject) getCopy(node0);
    final PointGeometrySubject geo0 = node0.getPointGeometry();
    final PointGeometrySubject geo1 = node1.getPointGeometry();
    movePointGeometry(geo0, geo1, dx, dy);
  }

  /**
   * Moves a group node.  This method is called to translate
   * the position of a group node by a given fixed amount.
   * @param  node0   The node in the original graph.
   * @param  dx      The distance in x direction to be added to the
   *                 original position to obtain the new position.
   * @param  dy      The distance in y direction to be added to the
   *                 original position to obtain the new position.
   */
  void moveGroupNode(final GroupNodeSubject node0,
                     final double dx,
                     final double dy)
  {
    final GroupNodeSubject node1 = (GroupNodeSubject) getCopy(node0);
    final BoxGeometrySubject geo0 = node0.getGeometry();
    final BoxGeometrySubject geo1 = node1.getGeometry();
    moveBoxGeometry(geo0, geo1, dx, dy);
  }

  /**
   * Moves an edge.  This method is called to translate the start point,
   * end point, and the handle point of an edge by a given fixed amount.
   * @param  edge0   The edge in the original graph.
   * @param  dx      The distance in x direction to be added to the
   *                 original position to obtain the new position.
   * @param  dy      The distance in y direction to be added to the
   *                 original position to obtain the new position.
   */
  void moveEdge(final EdgeSubject edge0, final double dx, final double dy)
  {
    moveEdgeStart(edge0, dx, dy);
    moveEdgeEnd(edge0, dx, dy);
    moveEdgeHandle(edge0, dx, dy, false, null);
  }

  /**
   * Moves the start point of an edge.  This method is called to translate
   * the start point of an edge by a given fixed amount.
   * @param  edge0   The edge in the original graph.
   * @param  dx      The distance in x direction to be added to the
   *                 original position to obtain the new position.
   * @param  dy      The distance in y direction to be added to the
   *                 original position to obtain the new position.
   */
  void moveEdgeStart(final EdgeSubject edge0, final double dx, final double dy)
  {
    final PointGeometrySubject geo0 = edge0.getStartPoint();
    if (geo0 != null) {
      final EdgeSubject edge1 = (EdgeSubject) getCopy(edge0);
      final PointGeometrySubject geo1 = edge1.getStartPoint();
      movePointGeometry(geo0, geo1, dx, dy);
    }
  }

  /**
   * Moves the end point of an edge.  This method is called to translate
   * the end point of an edge by a given fixed amount.
   * @param  edge0   The edge in the original graph.
   * @param  dx      The distance in x direction to be added to the
   *                 original position to obtain the new position.
   * @param  dy      The distance in y direction to be added to the
   *                 original position to obtain the new position.
   */
  void moveEdgeEnd(final EdgeSubject edge0, final double dx, final double dy)
  {
    final PointGeometrySubject geo0 = edge0.getEndPoint();
    if (geo0 != null) {
      final EdgeSubject edge1 = (EdgeSubject) getCopy(edge0);
      final PointGeometrySubject geo1 = edge1.getEndPoint();
      movePointGeometry(geo0, geo1, dx, dy);
    }
  }

  /**
   * Moves the handle point of an edge.  This method is called to translate
   * the handle point of an edge by a given fixed amount. If the start or
   * end point of the edge have also changed, methods {@link
   * #moveEdgeStart(EdgeSubject,double,double) moveEdgeStart()} and {@link
   * #moveEdgeEnd(EdgeSubject,double,double) #moveEdgeEnd()} should be called
   * first.
   * @param  edge0   The edge in the original graph.
   * @param  dx      The distance in x direction to be added to the
   *                 original position to obtain the new position.
   * @param  dy      The distance in y direction to be added to the
   *                 original position to obtain the new position.
   */
  void moveEdgeHandle(final EdgeSubject edge0,
                      final double dx,
                      final double dy,
                      final boolean moveAlongHalfWay,
                      final Point2D dragStart)
  {
    final EdgeSubject edge1 = (EdgeSubject) getCopy(edge0);
    final Point2D point = GeometryTools.getTurningPoint1(edge0);
    final Point2D newpoint = new Point2D.Double(point.getX() + dx, point.getY() + dy);
    final Point2D start = GeometryTools.getStartPoint(edge1);
    final Point2D end = GeometryTools.getEndPoint(edge1);
    double x = newpoint.getX();
    double y = newpoint.getY();
    //dragstart will == null if the self loop is just following a node
    if(GeometryTools.isSelfloop(edge1) && dragStart != null){
      double hx = dragStart.getX() - start.getX();
      double hy = dragStart.getY() - start.getY();
      double px = hx + dx;
      double py = hy + dy;
      double h = (hx * hx + hy * hy);
      double a0011 = (hx * px) + (hy * py);
      double a01 = (hx * py) - (hy * px);
      final double pointX = point.getX() - start.getX();
      final double pointY = point.getY() - start.getY();
      x = (a0011 * pointX - a01 * pointY) / h;
      y = (a01 * pointX + a0011 * pointY) / h;
      if (moveAlongHalfWay) {
        //directional move
        final double xDistAbs = Math.abs(x);
        final double yDistAbs = Math.abs(y);
        if (xDistAbs <= (TAN225 * yDistAbs)) {
          px = 0;
          py = y < 0 ? -1 : 1;
          a0011 = (x * px) + (y * py);
          a01 = (x * py) - (y * px);
          h = Math.sqrt(x * x + y * y);
          y = (a01 * x + a0011 * y) / h;
          x = 0;
        } else if (yDistAbs < (TAN225 * xDistAbs)) {
          px = x < 0 ? -1 : 1;
          py = 0;
          a0011 = (x * px) + (y * py);
          a01 = (x * py) - (y * px);
          h = Math.sqrt(x * x + y * y);
          x = (a0011 * x - a01 * y) / h;
          y = 0;
        } else {
          hx = x;
          hy = y;
          px = hx < 0 ? -HALF_SQRT2 : HALF_SQRT2;
          py = hy < 0 ? -HALF_SQRT2 : HALF_SQRT2;
          a0011 = (hx * px) + (hy * py);
          a01 = (hx * py) - (hy * px);
          h = Math.sqrt(hx * hx + hy * hy);
          x = (a0011 * hx - a01 * hy) / h;
          y = (a01 * hx + a0011 * hy) / h;
        }
      }
      x += start.getX();
      y += start.getY();
    }
    else{
      if(moveAlongHalfWay){
        //edge half way move
        final double sx = start.getX();
        final double sy = start.getY();
        final double ex = end.getX();
        final double ey = end.getY();
        final double px = newpoint.getX();
        final double py = newpoint.getY();
        final double mx = (sx + ex) / 2;
        final double my = (sy + ey) / 2;
        final double sxex = sx - ex;
        final double syey = sy - ey;
        //slope is undefined (vertical)
        if (sxex == 0) {
          x = px;
          y = my;
        }
        //slope = 0 (horizontal)
        else if (syey == 0) {
          x = mx;
          y = py;
        } else {
          final double eSlope = syey / sxex;
          final double pSlope = (-1) / eSlope;
          x = (-(eSlope * px) + (pSlope * mx) + py - my) / (pSlope - eSlope);
          y = (eSlope * (x - px)) + py;
        }
      }
    }
    newpoint.setLocation(x, y);
    GeometryTools.createMidGeometry(edge1, newpoint, SplineKind.INTERPOLATING);
  }

  /**
   * Updates edge geometry when dragging node.
   * This method is called when the end point of an edge has changed,
   * to transform the points of the edge's spline geometry.
   * The old position of the end point is taken from the original
   * graph, the new position from the copied graph. Then the
   * spline control points from the original graph are translated
   * and stored in the copied graph.
   * @param  edge0   The edge in the original graph.
   * @param  isStart <CODE>true</CODE> if the start point has changed;
   *                 <CODE>false</CODE> if the end point has changed.
   */
  void transformEdge(final EdgeSubject edge0, final boolean isStart)
  {
    final SplineGeometrySubject geo0 = edge0.getGeometry();
    if (geo0 != null) {
      final EdgeSubject edge1 = (EdgeSubject) getCopy(edge0);
      final Point2D old;
      final Point2D neo;
      final Point2D ref;
      if (isStart) {
        old = GeometryTools.getStartPoint(edge0);
        neo = GeometryTools.getStartPoint(edge1);
        ref = GeometryTools.getEndPoint(edge0);
      } else {
        old = GeometryTools.getEndPoint(edge0);
        neo = GeometryTools.getEndPoint(edge1);
        ref = GeometryTools.getStartPoint(edge0);
      }
      final double oldx = old.getX();
      final double oldy = old.getY();
      final double newx = neo.getX();
      final double newy = neo.getY();
      final double refx = ref.getX();
      final double refy = ref.getY();
      final double ox1 = oldx - refx;
      final double oy1 = oldy - refy;
      final double nx1 = newx - refx;
      final double ny1 = newy - refy;
      final double divide = ox1 * ox1 + oy1 * oy1;
      // Correction needed for very short edge, when ox1 and oy1 are 0 ...
      final double factor;
      if (Math.abs(divide) > GeometryTools.EPSILON) {
        factor = 1.0 / divide;
      } else if (divide >= 0.0) {
        factor = 1.0 / GeometryTools.EPSILON;
      } else {
        factor = -1.0 / GeometryTools.EPSILON;
      }
      final double a1 = factor * (nx1 * ox1 + ny1 * oy1);
      final double a2 = factor * (nx1 * oy1 - ny1 * ox1);
      final double a3 = -a2;
      final double a4 = a1;
      final SplineGeometrySubject geo1 = edge1.getGeometry();
      assert(geo1 != null);
      final List<Point2D> points0 = geo0.getPoints();
      final List<Point2D> points1 = geo1.getPointsModifiable();
      int i = 0;
      for (final Point2D point0 : points0) {
        final double cx1 = point0.getX() - refx;
        final double cy1 = point0.getY() - refy;
        final Point2D point1 = new Point2D.Double(a1 * cx1 + a2 * cy1 + refx,
                                                  a3 * cx1 + a4 * cy1 + refy);
        points1.set(i++, point1);
      }
    }
  }

  /**
   * Moves a node label.  This method is called to translate
   * the position of a node label by a given fixed amount.
   * @param  label0  The label geometry in the original graph that
   *                 represents the label to be transformed.
   * @param  dx      The distance in x direction to be added to the
   *                 original position to obtain the new position.
   * @param  dy      The distance in y direction to be added to the
   *                 original position to obtain the new position.
   */
  void moveLabelGeometry(final LabelGeometrySubject label0,
                         final double dx,
                         final double dy)
  {
    final LabelGeometrySubject label1 = (LabelGeometrySubject) getCopy(label0);
    moveLabelGeometry(label0, label1, dx, dy);
  }

  /**
   * Moves a label block.  This method is called to translate
   * the position of a label block by a given fixed amount.
   * @param  block0  The label block in the original graph.
   * @param  dx      The distance in x direction to be added to the
   *                 original position to obtain the new position.
   * @param  dy      The distance in y direction to be added to the
   *                 original position to obtain the new position.
   */
  void moveLabelBlock(final LabelBlockSubject block0,
                      final double dx,
                      final double dy)
  {
    final LabelBlockSubject block1 = (LabelBlockSubject) getCopy(block0);
    LabelGeometrySubject geo0 = block0.getGeometry();
    LabelGeometrySubject geo1 = block1.getGeometry();
    if (geo0 == null) {
      geo0 = DEFAULT_LABELBLOCK_GEO;
    }
    if (geo1 == null) {
      geo1 = DEFAULT_LABELBLOCK_GEO.clone();
      block1.setGeometry(geo1);
    }
    moveLabelGeometry(geo0, geo1, dx, dy);
  }

  /**
   * Moves a guard/action block.  This method is called to translate
   * the position of a guard/action block by a given fixed amount.
   * @param  block0  The guard/action block in the original graph.
   * @param  dx      The distance in x direction to be added to the
   *                 original position to obtain the new position.
   * @param  dy      The distance in y direction to be added to the
   *                 original position to obtain the new position.
   */
  void moveGuardActionBlock(final GuardActionBlockSubject block0,
                            final double dx,
                            final double dy)
  {
    final GuardActionBlockSubject block1 =
      (GuardActionBlockSubject) getCopy(block0);
    LabelGeometrySubject geo0 = block0.getGeometry();
    LabelGeometrySubject geo1 = block1.getGeometry();
    if (geo0 == null) {
      geo0 = DEFAULT_GUARDACTIONBLOCK_GEO;
    }
    if (geo1 == null) {
      geo1 = DEFAULT_GUARDACTIONBLOCK_GEO.clone();
      block1.setGeometry(geo1);
    }
    moveLabelGeometry(geo0, geo1, dx, dy);
  }


  private void moveBoxGeometry(final BoxGeometrySubject geo0,
                               final BoxGeometrySubject geo1,
                               final double dx,
                               final double dy)
  {
    final Rectangle2D rect = geo0.getRectangle();
    final double x = rect.getX();
    final double y = rect.getY();
    final double width = rect.getWidth();
    final double height = rect.getHeight();
    rect.setRect(x + dx, y + dy, width, height);
    geo1.setRectangle(rect);
  }

  private void moveLabelGeometry(final LabelGeometrySubject geo0,
                                 final LabelGeometrySubject geo1,
                                 final double dx,
                                 final double dy)
  {
    final Point2D point = geo0.getOffset();
    point.setLocation(point.getX() + dx, point.getY() + dy);
    geo1.setOffset(point);
  }

  private void movePointGeometry(final PointGeometrySubject geo0,
                                 final PointGeometrySubject geo1,
                                 final double dx,
                                 final double dy)
  {
    final Point2D point = geo0.getPoint();
    point.setLocation(point.getX() + dx, point.getY() + dy);
    geo1.setPoint(point);
  }


  //#########################################################################
  //# Inner Class CopiedModelListener
  /**
   * This listener receives all change events associated with this
   * editor graph.
   */
  private class CopiedModelListener
    implements ModelObserver
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.subject.base.ModelObserver
    @Override
    public void modelChanged(final ModelChangeEvent event)
    {
      final Subject esource = event.getSource();
      final Subject parent = esource.getParent();
      switch (event.getKind()) {
      case ModelChangeEvent.ITEM_ADDED:
        if (parent == null) {
          // ignore
        } else if (parent instanceof GroupNodeSubject) {
          final GroupNodeSubject group = (GroupNodeSubject) parent;
          final ChangeRecord record = mFakeMap.get(group);
          record.setChangeKind(ModelChangeEvent.GEOMETRY_CHANGED);
        } else if (esource == mNodes) {
          final Object node = event.getValue();
          if (node instanceof SimpleNodeSubject) {
            final SimpleNodeSubject simple = (SimpleNodeSubject) node;
            mObserverMap.put(simple, new EditorSimpleNode(simple));
            new SimpleNodeChangeRecord
              (null, simple, ModelChangeEvent.ITEM_ADDED);
          } else if (node instanceof GroupNodeSubject) {
            final GroupNodeSubject group = (GroupNodeSubject) node;
            mObserverMap.put(group, new EditorGroupNode(group));
            new GroupNodeChangeRecord
              (null, group, ModelChangeEvent.ITEM_ADDED);
          } else {
            throw new ClassCastException("Adding unknown node type: " +
                                         node.getClass().getName());
          }
        } else if (esource == mEdges) {
          final EdgeSubject edge = (EdgeSubject) event.getValue();
          new EdgeChangeRecord(null, edge, ModelChangeEvent.ITEM_ADDED);
          final NodeSubject source = edge.getSource();
          if (source != null) {
            mObserverMap.get(source).addEdge(edge);
          }
          final NodeSubject target = edge.getTarget();
          if (target != null) {
            mObserverMap.get(target).addEdge(edge);
          }
        }
        break;
      case ModelChangeEvent.ITEM_REMOVED:
        if (parent == null) {
          // ignore
        } else if (parent instanceof GroupNodeSubject) {
          final GroupNodeSubject group = (GroupNodeSubject) parent;
          final ChangeRecord record = mFakeMap.get(group);
          record.setChangeKind(ModelChangeEvent.GEOMETRY_CHANGED);
        } else if (esource == mNodes || esource == mEdges) {
          final ProxySubject item = (ProxySubject) event.getValue();
          final ChangeRecord record = mFakeMap.get(item);
          if (record == null) {
            // ignore
          } else if (record.getChangeKind() == ModelChangeEvent.ITEM_ADDED) {
            mFakeMap.remove(item);
          } else {
            record.setChangeKind(ModelChangeEvent.ITEM_REMOVED);
          }
        }
        break;
      case ModelChangeEvent.STATE_CHANGED:
        if (esource instanceof EdgeSubject) {
          final EdgeSubject edge = (EdgeSubject) esource;
          final ChangeRecord record = mFakeMap.get(edge);
          record.setChangeKind(ModelChangeEvent.STATE_CHANGED);
          final NodeSubject source = edge.getSource();
          if (source != null) {
            mObserverMap.get(source).addEdge(edge);
          }
          final NodeSubject target = edge.getTarget();
          if (target != null) {
            mObserverMap.get(target).addEdge(edge);
          }
        }
        break;
      case ModelChangeEvent.GEOMETRY_CHANGED:
        final Object value = event.getValue();
        final ProxySubject item;
        if (esource instanceof SimpleNodeSubject &&
            value instanceof LabelGeometrySubject) {
          item = (LabelGeometrySubject) value;

        } else {
          item = (ProxySubject) esource;
        }
        mChangeRecordCreator.createChangeRecord
          (item, ModelChangeEvent.GEOMETRY_CHANGED);
        break;
      }
    }

    @Override
    public int getModelObserverPriority()
    {
      return ModelObserver.RENDERING_PRIORITY;
    }
  }


  //#########################################################################
  //# Inner Class EditorNode
  private abstract class EditorNode
    implements ModelObserver
  {

    //#######################################################################
    //# Constructor
    protected EditorNode(final NodeSubject node)
    {
      mNode = node;
      mEdges = new HashMap<EdgeSubject, Boolean>();
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.subject.base.ModelObserver
    @Override
    public void modelChanged(final ModelChangeEvent event)
    {
      if (event.getSource() instanceof EdgeSubject) {
        final EdgeSubject e = (EdgeSubject) event.getSource();
        if (e.getSource() != getNodeSubject() &&
            e.getTarget() != getNodeSubject()) {
          removeEdge(e);
        }
      }
    }

    @Override
    public int getModelObserverPriority()
    {
      return ModelObserver.CLEANUP_PRIORITY_1;
    }

    //#######################################################################
    //# Simple Access
    public void removeEdge(final EdgeSubject edge)
    {
      if (mEdges.remove(edge) != null) {
        edge.removeModelObserver(this);
      }
    }

    public void addEdge(final EdgeSubject edge)
    {
      if (mEdges.put(edge, true) != null) {
        edge.addModelObserver(this);
      }
    }

    public Set<EdgeSubject> getEdges()
    {
      return Collections.unmodifiableSet(mEdges.keySet());
    }

    public abstract void update();

    /**
     * Updates edge geometry when dragging node.
     * @param  edge   The edge to be modified.
     * @param  old    The old position of the start or end point.
     * @param  neo    The new position of the start or end point.
     */
    protected final void transformEdge(final EdgeSubject edge,
                                       final Point2D old,
                                       final Point2D neo)
    {
      final SplineGeometrySubject geo = edge.getGeometry();
      if (geo != null && geo.getPoints().size() == 1) {
        final double ox = old.getX();
        final double oy = old.getY();
        final double nx = neo.getX();
        final double ny = neo.getY();
        final Point2D c = geo.getPointsModifiable().get(0);
        final double cx = c.getX();
        final double cy = c.getY();
        final Point2D newCenter;
        if (edge.getSource() == edge.getTarget()) {
          newCenter = new Point2D.Double(cx + nx - ox, cy + ny - oy);
        } else {
          final Point2D e;
          if (edge.getSource() == getNodeSubject()) {
            e = GeometryTools.getEndPoint(edge);
          } else {
            e = GeometryTools.getStartPoint(edge);
          }
          final double ex = e.getX();
          final double ey = e.getY();
          final double cx1 = cx - ex;
          final double cy1 = cy - ey;
          final double ox1 = ox - ex;
          final double oy1 = oy - ey;
          final double nx1 = nx - ex;
          final double ny1 = ny - ey;
          final double divide = ox1 * ox1 + oy1 * oy1;
          // Correction needed for very short edge, when ox1 and oy1 are 0 ...
          final double factor;
          if (Math.abs(divide) > GeometryTools.EPSILON) {
            factor = 1.0 / divide;
          } else if (divide >= 0.0) {
            factor = 1.0 / GeometryTools.EPSILON;
          } else {
            factor = -1.0 / GeometryTools.EPSILON;
          }
          final double a1 = factor * (nx1 * ox1 + ny1 * oy1);
          final double a2 = factor * (nx1 * oy1 - ny1 * ox1);
          final double a3 = -a2;
          final double a4 = a1;
          newCenter = new Point2D.Double(a1 * cx1 + a2 * cy1 + ex,
                                         a3 * cx1 + a4 * cy1 + ey);
        }
        geo.getPointsModifiable().set(0, newCenter);
      }
      if (edge.getSource() == getNodeSubject() &&
          edge.getStartPoint() != null) {
        edge.getStartPoint().setPoint(neo);
      }
      if (edge.getTarget() == getNodeSubject() &&
          edge.getEndPoint() != null) {
        edge.getEndPoint().setPoint(neo);
      }
    }

    public NodeSubject getNodeSubject()
    {
      return mNode;
    }

    //#######################################################################
    //# Data Members
    private final NodeSubject mNode;
    private final Map<EdgeSubject,Boolean> mEdges;
  }


  //#########################################################################
  //# Inner Class EditorSimpleNode
  private class EditorSimpleNode
    extends EditorNode
  {

    //#######################################################################
    //# Constructors
    public EditorSimpleNode(final SimpleNodeSubject node)
    {
      super(node);
       mPoint = node.getPointGeometry().getPoint();
    }

    //#######################################################################
    //# Simple Access
    @Override
    public SimpleNodeSubject getNodeSubject()
    {
      return (SimpleNodeSubject) super.getNodeSubject();
    }

    //#######################################################################
    //# Editing
    @Override
    public void update()
    {
      final Point2D newpos = getNodeSubject().getPointGeometry().getPoint();
      if (!mPoint.equals(newpos)) {
        for (final EdgeSubject edge : getEdges()) {
          transformEdge(edge, mPoint, newpos);
        }
        mPoint = newpos;
      }
    }

    //#######################################################################
    //# Data Members
    private Point2D mPoint;
  }


  //#########################################################################
  //# Inner Class EditorGroupNode
  private class EditorGroupNode
    extends EditorNode
  {
    //#######################################################################
    //# Constructors
    public EditorGroupNode(final GroupNodeSubject node)
    {
      super(node);
      mRect = node.getGeometry().getRectangle();
    }

    //#######################################################################
    //# Simple Access
    @Override
    public GroupNodeSubject getNodeSubject()
    {
      return (GroupNodeSubject) super.getNodeSubject();
    }

    //#######################################################################
    //# Editing
    @Override
    public void update()
    {
      final GroupNodeSubject node = getNodeSubject();
      final Rectangle2D newR = node.getGeometry().getRectangle();
      if (!mRect.equals(newR)) {
        for (final EdgeSubject edge : getEdges()) {
          final Point2D old;
          if (edge.getSource() == node) {
            old = edge.getStartPoint().getPoint();
          } else if (edge.getTarget() == node)  {
            old = edge.getEndPoint().getPoint();
          } else {
            continue;
          }
          final Point2D neo = new Point2D.Double
            (((old.getX() - mRect.getMinX()) / mRect.getWidth()) *
             newR.getWidth() + newR.getMinX(),
             ((old.getY() - mRect.getMinY()) / mRect.getHeight()) *
             newR.getHeight() + newR.getMinY());
          transformEdge(edge, old, neo);
        }
        mRect = newR;
      }
    }

    //#######################################################################
    //# Data Members
    private Rectangle2D mRect;
  }


  //#########################################################################
  //# Inner Class ChangeRecord
  private abstract class ChangeRecord
  {
    //#######################################################################
    //# Constructors
    ChangeRecord(final ProxySubject original, final ProxySubject fake)
    {
      this(original, fake, ModelChangeEvent.NO_CHANGE);
    }

    ChangeRecord(final ProxySubject original,
                 final ProxySubject fake,
                 final int kind)
    {
      mOriginal = original;
      mFake = fake;
      mChangeKind = kind;
      mFakeMap.put(fake, this);
      if (original != null) {
        mOriginalMap.put(original, this);
      }
    }

    //#######################################################################
    //# Simple Access
    ProxySubject getOriginal()
    {
      return mOriginal;
    }

    void setOriginal(final ProxySubject original)
    {
      if (mOriginal != null) {
        mOriginalMap.remove(mOriginal);
      }
      mOriginal = original;
      if (mOriginal != null) {
        mOriginalMap.put(mOriginal, this);
      }
    }

    ProxySubject getFake()
    {
      return mFake;
    }

    int getChangeKind()
    {
      return mChangeKind;
    }

    void setChangeKind(final int kind)
    {
      if (getChangeValue(kind) >= getChangeValue(mChangeKind)) {
        mChangeKind = kind;
      }
    }

    int getChangeValue(final int kind)
    {
      switch (kind) {
      case ModelChangeEvent.ITEM_ADDED:
      case ModelChangeEvent.ITEM_REMOVED:
        return 30;
      case ModelChangeEvent.STATE_CHANGED:
        return 20;
      case ModelChangeEvent.GEOMETRY_CHANGED:
        return 10;
      default:
        return 0;
      }
    }

    /**
     * Returns whether the graphical object linked to this change record
     * is implicitly changed. For example, an edge can be implicitly changed
     * despite having the status {@link ModelChangeEvent#NO_CHANGE NO_CHANGE}
     * if its source or target node moves.
     * @return <CODE>true</CODE> if the object is changed implicitly or
     *         explicitly, <CODE>false</CODE> otherwise.
     */
    boolean hasImplicitChanges()
    {
      return getChangeKind() != ModelChangeEvent.NO_CHANGE;
    }

    //#######################################################################
    //# Updating
    int getPass()
    {
      switch (mChangeKind) {
      case ModelChangeEvent.ITEM_ADDED:
        return PASS2_ADD;
      case ModelChangeEvent.ITEM_REMOVED:
        return PASS1_REMOVE;
      case ModelChangeEvent.STATE_CHANGED:
        return PASS3_RELINK;
      case ModelChangeEvent.GEOMETRY_CHANGED:
        return PASS3_RELINK;
      default:
        return -1;
      }
    }

    int getMinPass()
    {
      return getPass();
    }

    int getMaxPass()
    {
      return getPass();
    }

    ProxySubject createOriginal()
    {
      if (mOriginal != null) {
        return mOriginal;
      } else {
        throw new UnsupportedOperationException
          ("EditorGraph does not support automatic creation for class " +
           mFake.getClass().getName() + "!");
      }
    }

    AbstractEditCommand createInsertCommand(final GraphEditorPanel surface)
    {
      return new InsertCommand(createOriginal(), surface, null, false);
    }

    AbstractEditCommand createDeleteCommand(final GraphEditorPanel surface)
    {
      final List<ProxySubject> origs = Collections.singletonList(mOriginal);
      final List<InsertInfo> deletes = surface.getDeletionVictims(origs);
      return new DeleteCommand(deletes, surface, false);
    }

    AbstractEditCommand createMoveCommand(final GraphEditorPanel surface)
    {
      throw new UnsupportedOperationException
        ("EditorGraph does not support automatic movement for class " +
         mFake.getClass().getName() + "!");
    }

    AbstractEditCommand getUpdateCommand(final GraphEditorPanel surface,
                                         final int pass)
    {
      if (pass == getPass()) {
        switch (getChangeKind()) {
        case ModelChangeEvent.ITEM_ADDED:
          return createInsertCommand(surface);
        case ModelChangeEvent.ITEM_REMOVED:
          return createDeleteCommand(surface);
        case ModelChangeEvent.GEOMETRY_CHANGED:
          return createMoveCommand(surface);
        default:
          return null;
        }
      } else {
        return null;
      }
    }

    //#######################################################################
    //# Data Members
    private ProxySubject mOriginal;
    private final ProxySubject mFake;
    private int mChangeKind;
  }


  //#########################################################################
  //# Inner Class LabelChangeRecord
  private class LabelChangeRecord
    extends ChangeRecord
  {
    //#######################################################################
    //# Constructors
    LabelChangeRecord(final ProxySubject original, final ProxySubject fake)
    {
      super(original, fake);
    }
  }


  //#########################################################################
  //# Inner Class SimpleNodeChangeRecord
  private class SimpleNodeChangeRecord
    extends ChangeRecord
  {
    //#######################################################################
    //# Constructors
    SimpleNodeChangeRecord(final SimpleNodeSubject original,
                           final SimpleNodeSubject fake)
    {
      super(original, fake);
    }

    SimpleNodeChangeRecord(final SimpleNodeSubject original,
                           final SimpleNodeSubject fake,
                           final int kind)
    {
      super(original, fake, kind);
    }

    //#######################################################################
    //# Simple Access
    @Override
    SimpleNodeSubject getOriginal()
    {
      return (SimpleNodeSubject) super.getOriginal();
    }

    @Override
    SimpleNodeSubject getFake()
    {
      return (SimpleNodeSubject) super.getFake();
    }

    //#######################################################################
    //# Updating
    @Override
    SimpleNodeSubject createOriginal()
    {
      final SimpleNodeSubject original = getOriginal();
      if (original != null) {
        return original;
      } else {
        final Point2D pos = getFake().getPointGeometry().getPoint();
        final SimpleNodeSubject node =
          GraphTools.getCreatedSimpleNode(mGraph, pos);
        setOriginal(node);
        return node;
      }
    }

    @Override
    AbstractEditCommand createMoveCommand(final GraphEditorPanel surface)
    {
      final ModuleEqualityVisitor eq = new ModuleEqualityVisitor(true);
      final SimpleNodeSubject original = getOriginal();
      final PointGeometrySubject oldgeo = original.getPointGeometry();
      final PointGeometrySubject oldinit = original.getInitialArrowGeometry();
      final SimpleNodeSubject fake = getFake();
      final PointGeometrySubject newgeo = fake.getPointGeometry();
      final PointGeometrySubject newinit = fake.getInitialArrowGeometry();
      final boolean equalgeo = eq.equals(oldgeo, newgeo);
      final boolean equalinit = eq.equals(oldinit, newinit);
      if (equalinit) {
        if (equalgeo) {
          return null;
        } else {
          return new MoveCommand(oldgeo, newgeo, surface);
        }
      } else {
        final String name = equalgeo ? "Initial Arrow Change" : null;
        if (equalgeo && oldinit != null && newinit != null) {
          return new MoveCommand(oldinit, newinit, surface, name);
        } else {
          return new EditCommand(original, fake, surface, name);
        }
      }
    }
  }


  //#########################################################################
  //# Inner Class GroupNodeChangeRecord
  private class GroupNodeChangeRecord
    extends ChangeRecord
  {
    //#######################################################################
    //# Constructors
    GroupNodeChangeRecord(final GroupNodeSubject original,
                          final GroupNodeSubject fake)
    {
      super(original, fake);
    }

    GroupNodeChangeRecord(final GroupNodeSubject original,
                          final GroupNodeSubject fake,
                          final int kind)
    {
      super(original, fake, kind);
    }

    //#######################################################################
    //# Simple Access
    @Override
    GroupNodeSubject getOriginal()
    {
      return (GroupNodeSubject) super.getOriginal();
    }

    @Override
    GroupNodeSubject getFake()
    {
      return (GroupNodeSubject) super.getFake();
    }

    //#######################################################################
    //# Updating
    @Override
    GroupNodeSubject createOriginal()
    {
      final GroupNodeSubject original = getOriginal();
      if (original != null) {
        return original;
      } else {
        final Rectangle2D rect = getFake().getGeometry().getRectangle();
        final GroupNodeSubject group =
          GraphTools.getCreatedGroupNode(mGraph, rect);
        setOriginal(group);
        return group;
      }
    }

    @Override
    AbstractEditCommand createMoveCommand(final GraphEditorPanel surface)
    {
      final ModuleEqualityVisitor eq = new ModuleEqualityVisitor(true);
      final GroupNodeSubject original = getOriginal();
      final BoxGeometrySubject oldgeo = original.getGeometry();
      final GroupNodeSubject fake = getFake();
      final BoxGeometrySubject newgeo = fake.getGeometry();
      if (eq.equals(oldgeo, newgeo)) {
        return null;
      }
      final Rectangle2D oldrect = oldgeo.getRectangle();
      final Rectangle2D newrect = newgeo.getRectangle();
      final String name =
        oldrect.getWidth() == newrect.getWidth() &&
        oldrect.getHeight() == newrect.getHeight() ?
        "Group Node Movement" :
        "Group Node Reshaping";
      return new MoveCommand(oldgeo, newgeo, surface, name);
    }
  }


  //#########################################################################
  //# Inner Class EdgeChangeRecord
  private class EdgeChangeRecord
    extends ChangeRecord
  {
    //#######################################################################
    //# Constructors
    EdgeChangeRecord(final EdgeSubject original,
                     final EdgeSubject fake)
    {
      this(original, fake, ModelChangeEvent.NO_CHANGE);
    }

    EdgeChangeRecord(final EdgeSubject original,
                     final EdgeSubject fake,
                     final int kind)
    {
      super(original, fake, kind);
      LabelBlockSubject originalLB = null;
      LabelBlockSubject fakeLB = null;
      GuardActionBlockSubject originalGA = null;
      GuardActionBlockSubject fakeGA = null;
      if (original != null) {
        originalLB = original.getLabelBlock();
        originalGA = original.getGuardActionBlock();
      }
      if (fake != null) {
        fakeLB = fake.getLabelBlock();
        fakeGA = fake.getGuardActionBlock();
      }
      if (originalLB != null && fakeLB != null) {
        mChangeRecordCreator.createChangeRecordsForLabelBlock(originalLB,
                                                              fakeLB);
      }
      if (originalGA != null && fakeGA != null) {
        mChangeRecordCreator.createChangeRecordsForGuardActionBlock
          (originalGA, fakeGA);
      }
    }

    //#######################################################################
    //# Simple Access
    @Override
    EdgeSubject getOriginal()
    {
      return (EdgeSubject) super.getOriginal();
    }

    @Override
    EdgeSubject getFake()
    {
      return (EdgeSubject) super.getFake();
    }

    @Override
    boolean hasImplicitChanges()
    {
      if (getChangeKind() == ModelChangeEvent.NO_CHANGE) {
        final EdgeSubject original = getOriginal();
        final NodeSubject source = original.getSource();
        final ChangeRecord srecord = mOriginalMap.get(source);
        final int schange = srecord.getChangeKind();
        if (schange == ModelChangeEvent.GEOMETRY_CHANGED) {
          return true;
        }
        final NodeSubject target = original.getTarget();
        final ChangeRecord trecord = mOriginalMap.get(target);
        final int tchange = trecord.getChangeKind();
        return tchange == ModelChangeEvent.GEOMETRY_CHANGED;
      } else {
        return true;
      }
    }

    //#######################################################################
    //# Updating
    @Override
    int getPass()
    {
      if (getChangeKind() == ModelChangeEvent.ITEM_REMOVED) {
        return PASS0_UNLINK;
      } else {
        return super.getPass();
      }
    }

    @Override
    int getMinPass()
    {
      if (getChangeKind() == ModelChangeEvent.STATE_CHANGED) {
        final EdgeSubject fake = getFake();
        final NodeSubject source = fake.getSource();
        final ChangeRecord srecord = mFakeMap.get(source);
        if (srecord.getChangeKind() == ModelChangeEvent.ITEM_REMOVED) {
          return PASS0_UNLINK;
        }
        final NodeSubject target = fake.getTarget();
        final ChangeRecord trecord = mFakeMap.get(target);
        if (trecord.getChangeKind() == ModelChangeEvent.ITEM_REMOVED) {
          return PASS0_UNLINK;
        }
        return PASS3_RELINK;
      } else {
        return super.getMinPass();
      }
    }

    @Override
    EdgeSubject createOriginal()
    {
      final EdgeSubject original = getOriginal();
      if (original != null) {
        return original;
      } else {
        // This will not work if the source or target is created as well ...
        final EdgeSubject fake = getFake();
        final NodeSubject asource =
          (NodeSubject) EditorGraph.this.getOriginal(fake.getSource());
        final NodeSubject atarget =
          (NodeSubject) EditorGraph.this.getOriginal(fake.getTarget());
        final PointGeometrySubject startgeo = fake.getStartPoint();
        final Point2D start = startgeo == null ? null : startgeo.getPoint();
        final PointGeometrySubject endgeo = fake.getEndPoint();
        final Point2D end = endgeo == null ? null : endgeo.getPoint();
        final LabelBlockSubject lblock = fake.getLabelBlock();
        final GuardActionBlockSubject gablock = fake.getGuardActionBlock();
        final EdgeSubject edge = GraphTools.getCreatedEdge
          (mGraph, asource, atarget, start, end, lblock, gablock);
        final SplineGeometrySubject spline = fake.getGeometry();
        if(spline != null){
          edge.setGeometry(spline.clone());
        }
        setOriginal(edge);
        return edge;
      }
    }

    @Override
    AbstractEditCommand createMoveCommand(final GraphEditorPanel surface)
    {
      final EdgeSubject original = getOriginal();
      final SplineGeometrySubject oldgeo = original.getGeometry();
      final PointGeometrySubject oldstart = original.getStartPoint();
      final PointGeometrySubject oldend = original.getEndPoint();
      final EdgeSubject fake = getFake();
      final SplineGeometrySubject newgeo = fake.getGeometry();
      final PointGeometrySubject newstart = fake.getStartPoint();
      final PointGeometrySubject newend = fake.getEndPoint();
      final boolean hasnull =
        (oldgeo == null ^ newgeo == null) ||
        (oldstart == null ^ newstart == null) ||
        (oldend == null ^ newend == null);
      final String name = "Edge Reshaping";
      if (hasnull) {
        return createEditCommand(surface, name);
      } else {
        final ModuleEqualityVisitor eq = new ModuleEqualityVisitor(true);
        final boolean equalgeo = eq.equals(oldgeo, newgeo);
        final boolean equalstart = eq.equals(oldstart, newstart);
        final boolean equalend = eq.equals(oldend, newend);
        if (!equalgeo && equalstart && equalend) {
          return new MoveCommand(oldgeo, newgeo, surface, name);
        } else if (equalgeo && !equalstart && equalend) {
          return new MoveCommand(oldstart, newstart, surface, name);
        } else if (equalgeo && equalstart && !equalend) {
          return new MoveCommand(oldend, newend, surface, name);
        } else if (equalgeo && equalstart && equalend) {
          return null;
        } else {
          return createEditCommand(surface, name);
        }
      }
    }
    @Override
    AbstractEditCommand getUpdateCommand(final GraphEditorPanel surface,
                                         final int pass)
    {
      final EdgeSubject fake = getFake();
      final EdgeSubject original = createOriginal();
      if (pass == getPass()) {
        switch (getChangeKind()) {
        case ModelChangeEvent.ITEM_ADDED:
          return createInsertCommand(surface);
        case ModelChangeEvent.ITEM_REMOVED:
          return createDeleteCommand(surface);
        case ModelChangeEvent.STATE_CHANGED:
          return createEditCommand(surface, "Edge Redirection");
        case ModelChangeEvent.GEOMETRY_CHANGED:
          return createMoveCommand(surface);
        default:
          return null;
        }
      } else if (pass == PASS0_UNLINK &&
                 getChangeKind() == ModelChangeEvent.STATE_CHANGED) {
        final NodeSubject source = fake.getSource();
        final ChangeRecord srecord = mFakeMap.get(source);
        final boolean sdrop =
          srecord.getChangeKind() == ModelChangeEvent.ITEM_REMOVED;
        final NodeSubject target = fake.getTarget();
        final ChangeRecord trecord = mFakeMap.get(target);
        final boolean tdrop =
          trecord.getChangeKind() == ModelChangeEvent.ITEM_REMOVED;
        if (sdrop || tdrop) {
          final EdgeSubject template = original.clone();
          if (sdrop) {
            template.setSource(null);
            template.setStartPoint(null);
          }
          if (tdrop) {
            template.setTarget(null);
            template.setEndPoint(null);
          }
          return new EditCommand(original, template, surface,
                                 "Edge Redirection");
        } else {
          return null;
        }
      } else {
        return null;
      }
    }

    //#######################################################################
    //# Auxiliary Methods
    private AbstractEditCommand createEditCommand
      (final GraphEditorPanel surface, final String name)
    {
      final EdgeSubject original = getOriginal();
      final EdgeSubject fake = getFake();
      final EdgeSubject template = fake.clone();
      final NodeSubject fsource = fake.getSource();
      final NodeSubject osource =
        (NodeSubject) EditorGraph.this.getOriginal(fsource);
      template.setSource(osource);
      final NodeSubject ftarget = fake.getTarget();
      final NodeSubject otarget =
        (NodeSubject) EditorGraph.this.getOriginal(ftarget);
      template.setTarget(otarget);
      return new EditCommand(original, template, surface, name);
    }
  }


  //#########################################################################
  //# Inner Class LabelGeometryChangeRecord
  private class LabelGeometryChangeRecord
    extends ChangeRecord
  {
    //#######################################################################
    //# Constructors
    LabelGeometryChangeRecord(final LabelGeometrySubject original,
                              final LabelGeometrySubject fake)
    {
      super(original, fake);
    }

    LabelGeometryChangeRecord(final LabelGeometrySubject original,
                              final LabelGeometrySubject fake,
                              final int kind)
    {
      super(original, fake, kind);
    }

    //#######################################################################
    //# Simple Access
    @Override
    LabelGeometrySubject getOriginal()
    {
      return (LabelGeometrySubject) super.getOriginal();
    }

    @Override
    LabelGeometrySubject getFake()
    {
      return (LabelGeometrySubject) super.getFake();
    }

    //#######################################################################
    //# Updating
    @Override
    AbstractEditCommand createMoveCommand(final GraphEditorPanel surface)
    {
      return
        new MoveCommand(getOriginal(), getFake(), surface, "Label Movement");
    }
  }


  //#########################################################################
  //# Inner Class LabelBlockChangeRecord
  private class LabelBlockChangeRecord
    extends ChangeRecord
  {
    //#######################################################################
    //# Constructors
    LabelBlockChangeRecord(final LabelBlockSubject original,
                           final LabelBlockSubject fake)
    {
      super(original, fake);
    }

    LabelBlockChangeRecord(final LabelBlockSubject original,
                           final LabelBlockSubject fake,
                           final int kind)
    {
      super(original, fake, kind);
    }

    //#######################################################################
    //# Simple Access
    @Override
    LabelBlockSubject getOriginal()
    {
      return (LabelBlockSubject) super.getOriginal();
    }

    @Override
    LabelBlockSubject getFake()
    {
      return (LabelBlockSubject) super.getFake();
    }

    //#######################################################################
    //# Updating
    @Override
    AbstractEditCommand createMoveCommand(final GraphEditorPanel surface)
    {
      final LabelBlockSubject original = getOriginal();
      final LabelGeometrySubject oldgeo = original.getGeometry();
      final LabelBlockSubject fake = getFake();
      final LabelGeometrySubject newgeo = fake.getGeometry();
      if (oldgeo == null || newgeo == null) {
        return new EditCommand(original, fake, surface, "Events Movement");
      } else {
        return new MoveCommand(oldgeo, newgeo, surface);
      }
    }
  }


  //#########################################################################
  //# Inner Class GuardActionBlockChangeRecord
  private class GuardActionBlockChangeRecord
    extends ChangeRecord
  {
    //#######################################################################
    //# Constructors
    GuardActionBlockChangeRecord(final GuardActionBlockSubject original,
                                 final GuardActionBlockSubject fake)
    {
      super(original, fake);
    }

    GuardActionBlockChangeRecord(final GuardActionBlockSubject original,
                                 final GuardActionBlockSubject fake,
                                 final int kind)
    {
      super(original, fake, kind);
    }

    //#######################################################################
    //# Simple Access
    @Override
    GuardActionBlockSubject getOriginal()
    {
      return (GuardActionBlockSubject) super.getOriginal();
    }

    @Override
    GuardActionBlockSubject getFake()
    {
      return (GuardActionBlockSubject) super.getFake();
    }

    //#######################################################################
    //# Updating
    @Override
    AbstractEditCommand createMoveCommand(final GraphEditorPanel surface)
    {
      final GuardActionBlockSubject original = getOriginal();
      final LabelGeometrySubject oldgeo = original.getGeometry();
      final GuardActionBlockSubject fake = getFake();
      final LabelGeometrySubject newgeo = fake.getGeometry();
      if (oldgeo == null || newgeo == null) {
        return new EditCommand(original, fake, surface,
                               "Guard/Action Block Movement");
      } else {
        return new MoveCommand(oldgeo, newgeo, surface);
      }
    }
  }


  //#########################################################################
  //# Inner Class FakeGetterVisitor
  private class FakeGetterVisitor
    extends DefaultModuleProxyVisitor
  {
    //#######################################################################
    //# Invocation
    private ProxySubject getFake(final ProxySubject original)
    {
      final ChangeRecord record = mOriginalMap.get(original);
      if (record == null) {
        try {
          return (ProxySubject) original.acceptVisitor(this);
        } catch (final VisitorException exception) {
          throw exception.getRuntimeException();
        }
      } else {
        return record.getFake();
      }
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    @Override
    public Object visitProxy(final Proxy proxy)
    {
      return null;
    }

    @Override
    public ProxySubject visitLabelBlockProxy(final LabelBlockProxy original)
    {
      final LabelBlockSubject osubject = (LabelBlockSubject) original;
      final Subject parent = osubject.getParent();
      if (parent instanceof EdgeSubject) {
        final EdgeSubject oedge = (EdgeSubject) parent;
        final EdgeSubject fedge = (EdgeSubject) getFake(oedge);
        return fedge.getLabelBlock();
      }
      else {
        return mBlockedEvents;
      }
    }

    @Override
    public GuardActionBlockSubject visitGuardActionBlockProxy
      (final GuardActionBlockProxy original)
    {
      final GuardActionBlockSubject osubject =
        (GuardActionBlockSubject) original;
      final EdgeSubject oedge = (EdgeSubject) osubject.getParent();
      final EdgeSubject fedge = (EdgeSubject) getFake(oedge);
      return fedge.getGuardActionBlock();
    }

    @Override
    public LabelGeometrySubject visitLabelGeometryProxy
      (final LabelGeometryProxy original)
    {
      final LabelGeometrySubject osubject = (LabelGeometrySubject) original;
      final SimpleNodeSubject onode = (SimpleNodeSubject) osubject.getParent();
      final SimpleNodeSubject fnode = (SimpleNodeSubject) getFake(onode);
      return fnode.getLabelGeometry();
    }

  }


  //#########################################################################
  //# Inner Class OriginalGetterVisitor
  private class OriginalGetterVisitor
    extends DefaultModuleProxyVisitor
  {
    //#######################################################################
    //# Invocation
    private ProxySubject getOriginal(final ProxySubject fake)
    {
      final ChangeRecord record = mFakeMap.get(fake);
      if (record == null) {
        try{
          return (ProxySubject) fake.acceptVisitor(this);
        } catch (final VisitorException exception) {
          throw exception.getRuntimeException();
        }
      } else {
        final ProxySubject original = record.getOriginal();
        return original != null ? original : fake;
      }
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    @Override
    public Object visitProxy(final Proxy proxy)
    {
      return null;
    }

    @Override
    public ProxySubject visitLabelBlockProxy(final LabelBlockProxy fake)
    {
      final LabelBlockSubject fsubject = (LabelBlockSubject) fake;
      final Subject parent = fsubject.getParent();
      if (parent instanceof EdgeSubject) {
        final EdgeSubject fedge = (EdgeSubject) parent;
        final EdgeSubject oedge = (EdgeSubject) getOriginal(fedge);
        return oedge == null ? null : oedge.getLabelBlock();
      } else {
        return mGraph.getBlockedEvents();
      }
    }

    @Override
    public GuardActionBlockSubject visitGuardActionBlockProxy
      (final GuardActionBlockProxy fake)
    {
      final GuardActionBlockSubject fsubject =
        (GuardActionBlockSubject) fake;
      final EdgeSubject fedge = (EdgeSubject) fsubject.getParent();
      final EdgeSubject oedge = (EdgeSubject) getOriginal(fedge);
      return oedge == null ? null : oedge.getGuardActionBlock();
    }

    @Override
    public LabelGeometrySubject visitLabelGeometryProxy
      (final LabelGeometryProxy fake)
    {
      final LabelGeometrySubject fsubject = (LabelGeometrySubject) fake;
      final SimpleNodeSubject fnode = (SimpleNodeSubject) fsubject.getParent();
      final SimpleNodeSubject onode = (SimpleNodeSubject) getOriginal(fnode);
      if(onode == null){

      }
      return onode.getLabelGeometry();
    }
  }


  //#########################################################################
  //# Inner Class ChangeRecordCreatorVisitor
  private class ChangeRecordCreatorVisitor
    extends DefaultModuleProxyVisitor
  {
    //#######################################################################
    //# Invocation
    private ChangeRecord createChangeRecord(final ProxySubject fake,
                                            final int kind)
    {
      final ChangeRecord record = mFakeMap.get(fake);
      if (record == null) {
        try{
          mOriginal = mOriginalGetter.getOriginal(fake);
          mFake = fake;
          mKind = kind;
          return (ChangeRecord) fake.acceptVisitor(this);
        } catch (final VisitorException exception) {
          throw exception.getRuntimeException();
        }
      } else {
        record.setChangeKind(kind);
        return record;
      }
    }

    private void createChangeRecordsForLabelBlock
      (final LabelBlockSubject original,
       final LabelBlockSubject fake)
    {
      try {
        final List<? extends ProxySubject> originalList =
          original.getEventIdentifierListModifiable();
        final List<? extends ProxySubject> fakeList =
          fake.getEventIdentifierListModifiable();
        visitLists(originalList, fakeList);
      } catch (final VisitorException exception) {
        throw exception.getRuntimeException();
      }
    }

    private void createChangeRecordsForGuardActionBlock
      (final GuardActionBlockSubject orig,
       final GuardActionBlockSubject fake)
    {
      try {
        visitLists(orig.getGuardsModifiable(), fake.getGuardsModifiable());
        visitLists(orig.getActionsModifiable(), fake.getActionsModifiable());
      } catch (final VisitorException exception) {
        throw exception.getRuntimeException();
      }
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    @Override
    public ChangeRecord visitConditionalProxy(final ConditionalProxy fake)
      throws VisitorException
    {
      final ConditionalSubject oCond = (ConditionalSubject) mOriginal;
      final ConditionalSubject fCond = (ConditionalSubject) mFake;
      visitNestedBlockProxy(fake);
      visitProxies(oCond.getGuard(), fCond.getGuard());
      return null;
    }

    @Override
    public ChangeRecord visitEdgeProxy(final EdgeProxy fake)
    {
      final EdgeSubject oedge = (EdgeSubject) mOriginal;
      final EdgeSubject fedge = (EdgeSubject) mFake;
      return new EdgeChangeRecord(oedge, fedge, mKind);
    }

    @Override
    public ChangeRecord visitForeachProxy(final ForeachProxy fake)
      throws VisitorException
    {
      final ForeachSubject oForeach = (ForeachSubject) mOriginal;
      final ForeachSubject fForeach = (ForeachSubject) mFake;
      visitNestedBlockProxy(fake);
      visitProxies(oForeach.getRange(), fForeach.getRange());
      return null;
    }

    @Override
    public ChangeRecord visitGroupNodeProxy(final GroupNodeProxy fake)
    {
      final GroupNodeSubject ogroup = (GroupNodeSubject) mOriginal;
      final GroupNodeSubject fgroup = (GroupNodeSubject) mFake;
      return new GroupNodeChangeRecord(ogroup, fgroup, mKind);
    }

    @Override
    public ChangeRecord visitGuardActionBlockProxy
      (final GuardActionBlockProxy fake)
    {
      final GuardActionBlockSubject oblock =
        (GuardActionBlockSubject) mOriginal;
      final GuardActionBlockSubject fblock =
        (GuardActionBlockSubject) mFake;
      return new GuardActionBlockChangeRecord(oblock, fblock, mKind);
    }

    @Override
    public ChangeRecord visitLabelBlockProxy(final LabelBlockProxy fake)
    {
      final LabelBlockSubject oblock = (LabelBlockSubject) mOriginal;
      final LabelBlockSubject fblock = (LabelBlockSubject) mFake;
      return new LabelBlockChangeRecord(oblock, fblock, mKind);
    }

    @Override
    public ChangeRecord visitLabelGeometryProxy(final LabelGeometryProxy fake)
    {
      final LabelGeometrySubject ogeo = (LabelGeometrySubject) mOriginal;
      final LabelGeometrySubject fgeo = (LabelGeometrySubject) mFake;
      return new LabelGeometryChangeRecord(ogeo, fgeo, mKind);
    }

    @Override
    public ChangeRecord visitNestedBlockProxy(final NestedBlockProxy fake)
      throws VisitorException
    {
      final NestedBlockSubject oBlock = (NestedBlockSubject) mOriginal;
      final NestedBlockSubject fBlock = (NestedBlockSubject) mFake;
      new LabelChangeRecord(oBlock, fBlock);
      final ListSubject<? extends ProxySubject> originalList =
        oBlock.getBodyModifiable();
      final ListSubject<? extends ProxySubject> fakeList =
        fBlock.getBodyModifiable();
      visitLists(originalList, fakeList);
      return null;
    }

    @Override
    public ChangeRecord visitSimpleExpressionProxy
      (final SimpleExpressionProxy fake)
    {
      final SimpleExpressionSubject oexpr =
        (SimpleExpressionSubject) mOriginal;
      final SimpleExpressionSubject fexpr =
        (SimpleExpressionSubject) mFake;
      return new LabelChangeRecord(oexpr, fexpr);
    }

    @Override
    public ChangeRecord visitSimpleNodeProxy(final SimpleNodeProxy fake)
    {
      final SimpleNodeSubject onode = (SimpleNodeSubject) mOriginal;
      final SimpleNodeSubject fnode = (SimpleNodeSubject) mFake;
      return new SimpleNodeChangeRecord(onode, fnode, mKind);
    }

    //#######################################################################
    //# Auxiliary Methods
    private void visitLists(final List<? extends ProxySubject> originalList,
                            final List<? extends ProxySubject> fakeList)
      throws VisitorException
    {
      final Iterator<? extends ProxySubject> originalIter =
        originalList.iterator();
      final Iterator<? extends ProxySubject> fakeIter = fakeList.iterator();
      while (originalIter.hasNext()) {
        visitProxies(originalIter.next(), fakeIter.next());
      }
    }

    private void visitProxies(final ProxySubject original, final ProxySubject fake)
      throws VisitorException
    {
      mOriginal = original;
      mFake = fake;
      mOriginal.acceptVisitor(this);
    }

    //#######################################################################
    //# Data Members
    private ProxySubject mOriginal;
    private ProxySubject mFake;
    private int mKind;
  }


  //#########################################################################
  //# Data Members
  private final GraphSubject mGraph;
  private final LabelBlockSubject mBlockedEvents;
  private final ListSubject<EdgeSubject> mEdges;
  private final IndexedSetSubject<NodeSubject> mNodes;
  private final Map<NodeSubject,EditorNode> mObserverMap;
  private final Map<ProxySubject,ChangeRecord> mFakeMap;
  private final Map<ProxySubject,ChangeRecord> mOriginalMap;
  private final FakeGetterVisitor mFakeGetter;
  private final OriginalGetterVisitor mOriginalGetter;
  private final ChangeRecordCreatorVisitor mChangeRecordCreator;


  //#########################################################################
  //# Class Constants
  private static final int PASS0_UNLINK = 0;
  private static final int PASS1_REMOVE = 1;
  private static final int PASS2_ADD = 2;
  private static final int PASS3_RELINK = 3;

  @SuppressWarnings("unused")
  private static final int FIRST_PASS = PASS0_UNLINK;
  @SuppressWarnings("unused")
  private static final int LAST_PASS = PASS3_RELINK;

  private static final double TAN225 = Math.sqrt(2) -1;
  private static final double HALF_SQRT2 = 0.5*Math.sqrt(2);

  private static final LabelGeometrySubject DEFAULT_LABELBLOCK_GEO =
    new LabelGeometrySubject(LabelBlockProxyShape.DEFAULT_OFFSET);
  private static final LabelGeometrySubject DEFAULT_GUARDACTIONBLOCK_GEO =
    new LabelGeometrySubject(GuardActionBlockProxyShape.DEFAULT_OFFSET);

}
