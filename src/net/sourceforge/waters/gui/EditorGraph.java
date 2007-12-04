//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   EditorGraph
//###########################################################################
//# $Id: EditorGraph.java,v 1.25 2007-12-04 03:22:54 robi Exp $
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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.gui.command.Command;
import net.sourceforge.waters.gui.command.CompoundCommand;
import net.sourceforge.waters.gui.command.DeleteCommand;
import net.sourceforge.waters.gui.command.InsertCommand;
import net.sourceforge.waters.gui.command.MoveEdgeCommand;
import net.sourceforge.waters.gui.command.MoveGroupNodeCommand;
import net.sourceforge.waters.gui.command.MoveGuardActionBlockCommand;
import net.sourceforge.waters.gui.command.MoveLabelBlockCommand;
import net.sourceforge.waters.gui.command.MoveLabelGeometryCommand;
import net.sourceforge.waters.gui.command.MoveSimpleNodeCommand;
import net.sourceforge.waters.gui.command.RedirectEdgeCommand;
import net.sourceforge.waters.gui.command.UpdateCommand;
import net.sourceforge.waters.gui.renderer.GeometryTools;
import net.sourceforge.waters.gui.renderer.LabelBlockProxyShape;
import net.sourceforge.waters.gui.transfer.InsertInfo;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.module.AbstractModuleProxyVisitor;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.GroupNodeProxy;
import net.sourceforge.waters.model.module.GuardActionBlockProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.LabelGeometryProxy;
import net.sourceforge.waters.model.module.ModuleProxyVisitor;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.model.unchecked.Casting;

import net.sourceforge.waters.subject.base.AbstractSubject;
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
import net.sourceforge.waters.subject.module.EdgeSubject;
import net.sourceforge.waters.subject.module.GraphSubject;
import net.sourceforge.waters.subject.module.GroupNodeSubject;
import net.sourceforge.waters.subject.module.GuardActionBlockSubject;
import net.sourceforge.waters.subject.module.LabelBlockSubject;
import net.sourceforge.waters.subject.module.LabelGeometrySubject;
import net.sourceforge.waters.subject.module.NodeSubject;
import net.sourceforge.waters.subject.module.PointGeometrySubject;
import net.sourceforge.waters.subject.module.SimpleNodeSubject;
import net.sourceforge.waters.subject.module.SplineGeometrySubject;

import net.sourceforge.waters.xsd.module.SplineKind;


/**
 * <P>A double-up graph used by the controlled surface during drag
 * operations.</P>
 *
 * <P>When a drag action is initiated in the controlled surface, it creates
 * a so-called secondary graph of type <CODE>EditorGraph</CODE>. This is a
 * full implementation of the {@link GraphProxy} interface, which is passed
 * to the renderers for display. It is updated continuously during the
 * course of the drag operation, but these changes are not written through
 * to the model immediately.</P>
 *
 * <P>When the drag operation is completed, the <CODE>EditorGraph</CODE>
 * computes a single command object that represents all changes applied to
 * the double-up graph, and this command is registered with the undo
 * manager for execution on the main model.</P>
 *
 * @see ControlledSurface, Command
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
      final ChangeRecord record =
        new LabelBlockChangeRecord(blocked, mBlockedEvents);
      addChangeRecord(record);
    } else {
      mBlockedEvents = null;
    }

    for (final NodeSubject node : graph.getNodesModifiable()) {
      if (node instanceof SimpleNodeSubject) {
        SimpleNodeSubject simple = (SimpleNodeSubject) node;
        addNode(simple);
      } else if (node instanceof GroupNodeSubject) {
        GroupNodeSubject group = (GroupNodeSubject) node;
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
  public Class<GraphProxy> getProxyInterface()
  {
    return GraphProxy.class;
  }

  public Object acceptVisitor(final ProxyVisitor visitor)
    throws VisitorException
  {
    final ModuleProxyVisitor mvisitor = (ModuleProxyVisitor) visitor;
    return mvisitor.visitGraphProxy(this);
  }

  public LabelBlockProxy getBlockedEvents()
  {
    return mBlockedEvents;
  }

  public Set<NodeProxy> getNodes()
  {
    final Set<NodeProxy> downcast = Casting.toSet(mNodes);
    return Collections.unmodifiableSet(downcast);
  }

  public List<EdgeProxy> getEdges()
  {
    final List<EdgeProxy> downcast = Casting.toList(mEdges);
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

  Collection<EdgeSubject> getNodeEdges(NodeSubject n)
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
  Command createUpdateCommand(final ControlledSurface surface,
                              final String description,
                              final boolean selecting)
  {
    final List<ProxySubject> modified = new LinkedList<ProxySubject>();
    final List<ProxySubject> added = new LinkedList<ProxySubject>();
    final List<ProxySubject> removed = new LinkedList<ProxySubject>();
    int minpass = Integer.MAX_VALUE;
    int maxpass = Integer.MIN_VALUE;
    for (final ChangeRecord record : mOriginalMap.values()) {
      final ProxySubject original = record.getOriginal();
      switch (record.getChangeKind()) {
      case ModelChangeEvent.ITEM_ADDED:
        added.add(original);
        break;
      case ModelChangeEvent.ITEM_REMOVED:
        removed.add(original);
        break;
      case ModelChangeEvent.STATE_CHANGED:
      case ModelChangeEvent.GEOMETRY_CHANGED:
        modified.add(original);
        break;
      default:
        if (record.hasImplicitChanges()) {
          modified.add(original);
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
    final CompoundCommand compound =
      new UpdateCommand(modified, added, removed,
                        surface, description, selecting);
    for (int pass = minpass; pass <= maxpass; pass++) {
      for (final ChangeRecord record : mFakeMap.values()) {
        final Command cmd = record.getUpdateCommand(surface, pass);
        if (cmd != null && cmd.isSignificant()) {
          compound.addCommand(cmd);
        }
      }
    }
    if (!compound.isSignificant()) {
      return null;
    } else {
      compound.end();
      return compound;
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  /**
   * Creates a copy of the given edge and adds it to this graph.
   * @param  edge0   The egde in the original graph a copy of which
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
    final ChangeRecord record = new EdgeChangeRecord(edge0, edge1);
    addChangeRecord(record);
    return edge1;
  }

  private void addGroupNode(final GroupNodeSubject group0)
  {
    if (group0.getGeometry() == null) {
      throw new IllegalArgumentException("Group node " + group0 +
                                         " has no geometry!");
    } else {
      GroupNodeSubject group1 = group0.clone();
      mNodes.add(group1);
      mObserverMap.put(group1, new EditorGroupNode(group1));
      final ChangeRecord record =
        new GroupNodeChangeRecord(group0, group1);
      addChangeRecord(record);
    }
  }

  private void addNode(final SimpleNodeSubject node0)
  {
    final SimpleNodeSubject node1 = node0.clone();
    mNodes.add(node1);
    mObserverMap.put(node1, new EditorSimpleNode(node1));
    final ChangeRecord record =
      new SimpleNodeChangeRecord(node0, node1);
    addChangeRecord(record);
  }

  private void addChangeRecord(final ChangeRecord record)
  {
    final ProxySubject original = record.getOriginal();
    mOriginalMap.put(original, record);
    final ProxySubject fake = record.getFake();
    mFakeMap.put(fake, record);
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
    moveEdgeHandle(edge0, dx, dy);
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
                      final double dy)
  {
    final EdgeSubject edge1 = (EdgeSubject) getCopy(edge0);
    final Point2D point = GeometryTools.getTurningPoint1(edge0);
    point.setLocation(point.getX() + dx, point.getY() + dy);
    GeometryTools.createMidGeometry(edge1, point, SplineKind.INTERPOLATING);
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
    final LabelGeometrySubject geo0 = block0.getGeometry();
    final LabelGeometrySubject geo1 = block1.getGeometry();
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
   * This listener receives all change event associated with this
   * editor graph.
   */
  private class CopiedModelListener
    implements ModelObserver
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.subject.base.ModelObserver
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
            final ChangeRecord record = new SimpleNodeChangeRecord
              (null, simple, ModelChangeEvent.ITEM_ADDED);
            addChangeRecord(record);
          } else if (node instanceof GroupNodeSubject) {
            final GroupNodeSubject group = (GroupNodeSubject) node;
            mObserverMap.put(group, new EditorGroupNode(group));
            final ChangeRecord record = new GroupNodeChangeRecord
              (null, group, ModelChangeEvent.ITEM_ADDED);
            addChangeRecord(record);
          } else {
            throw new ClassCastException("Adding unknown node type: " +
                                         node.getClass().getName());
          }
        } else if (esource == mEdges) {
          final EdgeSubject edge = (EdgeSubject) event.getValue();
          final ChangeRecord record =
            new EdgeChangeRecord(null, edge, ModelChangeEvent.ITEM_ADDED);
          addChangeRecord(record);
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
            final ProxySubject original = record.getOriginal();
            mOriginalMap.remove(original);
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
        final ProxySubject item = (ProxySubject) esource;
        mChangeRecordCreator.createChangeRecord
          (item, ModelChangeEvent.GEOMETRY_CHANGED);
        break;
      }
    }
  }


  //#########################################################################
  //# Inner Class EditorNode
  private abstract class EditorNode
    implements ModelObserver
  {
    protected EditorNode(NodeSubject node)
    {
      mNode = node;
      mEdges = new HashMap<EdgeSubject, Boolean>();
    }

    public void removeEdge(EdgeSubject edge)
    {
      if (mEdges.remove(edge) != null) {
        edge.removeModelObserver(this);
      }
    }

    public void addEdge(EdgeSubject edge)
    {
      if (mEdges.put(edge, new Boolean(true)) != null) {
        edge.addModelObserver(this);
      }
    }

    public Set<EdgeSubject> getEdges()
    {
      return Collections.unmodifiableSet(mEdges.keySet());
    }

    public void modelChanged(ModelChangeEvent event)
    {
      if (event.getSource() instanceof EdgeSubject) {
        EdgeSubject e = (EdgeSubject) event.getSource();
        if (e.getSource() != getNodeSubject() &&
            e.getTarget() != getNodeSubject()) {
          removeEdge(e);
        }
      }
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
    public SimpleNodeSubject getNodeSubject()
    {
      return (SimpleNodeSubject) super.getNodeSubject();
    }

    //#######################################################################
    //# Editing
    public void update()
    {
      mIsUpdating = true;
      final Point2D newpos = getNodeSubject().getPointGeometry().getPoint();
      if (!mPoint.equals(newpos)) {
        for (final EdgeSubject edge : getEdges()) {
          transformEdge(edge, mPoint, newpos);
        }
        mPoint = newpos;
      }
      mIsUpdating = false;
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
    public EditorGroupNode(GroupNodeSubject node)
    {
      super(node);
      mRect = node.getGeometry().getRectangle();
    }

    //#######################################################################
    //# Simple Access
    public GroupNodeSubject getNodeSubject()
    {
      return (GroupNodeSubject) super.getNodeSubject();
    }

    //#######################################################################
    //# Editing
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
    ChangeRecord(final ProxySubject original,
                 final ProxySubject fake)
    {
      this(original, fake, ModelChangeEvent.NO_CHANGE);
    }

    ChangeRecord(final ProxySubject original,
                 final ProxySubject fake,
                 final int kind)
    {
      mOriginal = original == null ? fake : original;
      mFake = fake;
      mChangeKind = kind;
    }

    //#######################################################################
    //# Simple Access
    ProxySubject getOriginal()
    {
      return mOriginal;
    }

    void setOriginal(final ProxySubject original)
    {
      mOriginalMap.remove(mOriginal);
      mOriginal = original;
      mOriginalMap.put(mOriginal, this);
    }

    ProxySubject getFake()
    {
      return mFake;
    }

    void setFake(final ProxySubject fake)
    {
      mFakeMap.remove(mFake);
      mFake = fake;
      mFakeMap.put(mFake, this);
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

    Command getUpdateCommand(final ControlledSurface surface, final int pass)
    {
      return null;
    }

    Command createDeleteCommand(final ControlledSurface surface)
    {
      final List<ProxySubject> origs = Collections.singletonList(mOriginal);
      final List<InsertInfo> deletes = surface.getDeletionVictims(origs);
      return new DeleteCommand(deletes, surface, false);
    }


    //#######################################################################
    //# Data Members
    private ProxySubject mOriginal;
    private ProxySubject mFake;
    private int mChangeKind;
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
    SimpleNodeSubject getOriginal()
    {
      return (SimpleNodeSubject) super.getOriginal();
    }

    SimpleNodeSubject getFake()
    {
      return (SimpleNodeSubject) super.getFake();
    }

    //#######################################################################
    //# Updating
    Command getUpdateCommand(final ControlledSurface surface, final int pass)
    {
      if (pass == getPass()) {
        switch (getChangeKind()) {
        case ModelChangeEvent.ITEM_ADDED:
          final Point2D pos = getFake().getPointGeometry().getPoint();
          final SimpleNodeSubject node =
            GraphTools.getCreatedSimpleNode(mGraph, pos);
          setOriginal(node);
          return new InsertCommand(node, surface, false);
        case ModelChangeEvent.ITEM_REMOVED:
          return createDeleteCommand(surface);
        case ModelChangeEvent.GEOMETRY_CHANGED:
          return new MoveSimpleNodeCommand(getOriginal(), getFake());
        default:
          return null;
        }
      } else {
        return null;
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
    GroupNodeSubject getOriginal()
    {
      return (GroupNodeSubject) super.getOriginal();
    }

    GroupNodeSubject getFake()
    {
      return (GroupNodeSubject) super.getFake();
    }

    //#######################################################################
    //# Updating
    Command getUpdateCommand(final ControlledSurface surface, final int pass)
    {
      if (pass == getPass()) {
        switch (getChangeKind()) {
        case ModelChangeEvent.ITEM_ADDED:
          final Rectangle2D rect = getFake().getGeometry().getRectangle();
          final GroupNodeSubject group =
            GraphTools.getCreatedGroupNode(mGraph, rect);
          setOriginal(group);
          return new InsertCommand(group, surface, false);
        case ModelChangeEvent.ITEM_REMOVED:
          return createDeleteCommand(surface);
        case ModelChangeEvent.GEOMETRY_CHANGED:
          return new MoveGroupNodeCommand(getOriginal(), getFake());
        default:
          return null;
        }
      } else {
        return null;
      }
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
      super(original, fake);
    }

    EdgeChangeRecord(final EdgeSubject original,
                     final EdgeSubject fake,
                     final int kind)
    {
      super(original, fake, kind);
    }

    //#######################################################################
    //# Simple Access
    EdgeSubject getOriginal()
    {
      return (EdgeSubject) super.getOriginal();
    }

    EdgeSubject getFake()
    {
      return (EdgeSubject) super.getFake();
    }

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
    int getPass()
    {
      if (getChangeKind() == ModelChangeEvent.ITEM_REMOVED) {
        return PASS0_UNLINK;
      } else {
        return super.getPass();
      }
    }

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

    Command getUpdateCommand(final ControlledSurface surface, final int pass)
    {
      final EdgeSubject fake = getFake();
      final EdgeSubject original = getOriginal();
      if (pass == getPass()) {
        switch (getChangeKind()) {
        case ModelChangeEvent.ITEM_ADDED:
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
          setOriginal(edge);
          return new InsertCommand(edge, surface, false);
        case ModelChangeEvent.ITEM_REMOVED:
          return createDeleteCommand(surface);
        case ModelChangeEvent.STATE_CHANGED:
          final NodeSubject source =
            (NodeSubject) EditorGraph.this.getOriginal(fake.getSource());
          final PointGeometrySubject newstart = fake.getStartPoint();
          final PointGeometrySubject oldstart = original.getStartPoint();
          Command scmd = null;
          if (source != original.getSource() ||
              !ProxyTools.equalsWithGeometry(oldstart, newstart)) {
            final Point2D point =
              newstart == null ? null : newstart.getPoint();
            scmd = new RedirectEdgeCommand(original, source, true, point);
          }
          final NodeSubject target =
            (NodeSubject) EditorGraph.this.getOriginal(fake.getTarget());
          final PointGeometrySubject newend = fake.getEndPoint();
          final PointGeometrySubject oldend = original.getEndPoint();
          Command tcmd = null;
          if (target != original.getTarget() ||
              !ProxyTools.equalsWithGeometry(oldend, newend)) {
            final Point2D point = newend == null ? null : newend.getPoint();
            tcmd = new RedirectEdgeCommand(original, target, false, point);
          }
          return createDoubleCommand(scmd, tcmd);
        case ModelChangeEvent.GEOMETRY_CHANGED:
          return new MoveEdgeCommand(original, fake);
        default:
          return null;
        }
      } else if (pass == PASS0_UNLINK &&
                 getChangeKind() == ModelChangeEvent.STATE_CHANGED) {
        final NodeSubject source = fake.getSource();
        final ChangeRecord srecord = mFakeMap.get(source);
        final Command scmd =
          srecord.getChangeKind() == ModelChangeEvent.ITEM_REMOVED ?
          new RedirectEdgeCommand(original, null, true, null) :
          null;
        final NodeSubject target = fake.getTarget();
        final ChangeRecord trecord = mFakeMap.get(target);
        final Command tcmd =
          trecord.getChangeKind() == ModelChangeEvent.ITEM_REMOVED ?
          new RedirectEdgeCommand(original, null, false, null) :
          null;
        return createDoubleCommand(scmd, tcmd);
      } else {
        return null;
      }
    } 

    //#######################################################################
    //# Auxiliary Methods
    private Command createDoubleCommand(final Command source,
                                        final Command target)
    {
      if (source == null) {
        return target;
      } else if (target == null) {
        return source;
      } else {
        final CompoundCommand cmd = new CompoundCommand("Edge Redirection");
        cmd.addCommand(source);
        cmd.addCommand(target);
        cmd.end();
        return cmd;
      }
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
    LabelGeometrySubject getOriginal()
    {
      return (LabelGeometrySubject) super.getOriginal();
    }

    LabelGeometrySubject getFake()
    {
      return (LabelGeometrySubject) super.getFake();
    }

    //#######################################################################
    //# Updating
    Command getUpdateCommand(final ControlledSurface surface, final int pass)
    {
      if (pass == getPass()) {
        switch (getChangeKind()) {
        case ModelChangeEvent.GEOMETRY_CHANGED:
          return new MoveLabelGeometryCommand(getOriginal(), getFake());
        default:
          return null;
        }
      } else {
        return null;
      }
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
    LabelBlockSubject getOriginal()
    {
      return (LabelBlockSubject) super.getOriginal();
    }

    LabelBlockSubject getFake()
    {
      return (LabelBlockSubject) super.getFake();
    }

    //#######################################################################
    //# Updating
    Command getUpdateCommand(final ControlledSurface surface, final int pass)
    {
      if (pass == getPass()) {
        switch (getChangeKind()) {
        case ModelChangeEvent.GEOMETRY_CHANGED:
          return new MoveLabelBlockCommand(getOriginal(), getFake());
        default:
          return null;
        }
      } else {
        return null;
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
    GuardActionBlockSubject getOriginal()
    {
      return (GuardActionBlockSubject) super.getOriginal();
    }

    GuardActionBlockSubject getFake()
    {
      return (GuardActionBlockSubject) super.getFake();
    }

    //#######################################################################
    //# Updating
    Command getUpdateCommand(final ControlledSurface surface, final int pass)
    {
      if (pass == getPass()) {
        switch (getChangeKind()) {
        case ModelChangeEvent.GEOMETRY_CHANGED:
          return new MoveGuardActionBlockCommand(getOriginal(), getFake());
        default:
          return null;
        }
      } else {
        return null;
      }
    }
  }


  //#########################################################################
  //# Inner Class FakeGetterVisitor
  private class FakeGetterVisitor
    extends AbstractModuleProxyVisitor
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
          throw new WatersRuntimeException(exception);
        }
      } else {
        return record.getFake();
      }
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    public Object visitProxy(final Proxy proxy)
    {
      return null;
    }

    public ProxySubject visitLabelBlockProxy(final LabelBlockProxy original)
    {
      final LabelBlockSubject osubject = (LabelBlockSubject) original;
      final EdgeSubject oedge = (EdgeSubject) osubject.getParent();
      final EdgeSubject fedge = (EdgeSubject) getFake(oedge);
      return fedge.getLabelBlock();
    }

    public GuardActionBlockSubject visitGuardActionBlockProxy
      (final GuardActionBlockProxy original)
    {
      final GuardActionBlockSubject osubject =
        (GuardActionBlockSubject) original;
      final EdgeSubject oedge = (EdgeSubject) osubject.getParent();
      final EdgeSubject fedge = (EdgeSubject) getFake(oedge);
      return fedge.getGuardActionBlock();
    }

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
    extends AbstractModuleProxyVisitor
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
          throw new WatersRuntimeException(exception);
        }
      } else {
        return record.getOriginal();
      }
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    public Object visitProxy(final Proxy proxy)
    {
      return null;
    }

    public ProxySubject visitLabelBlockProxy(final LabelBlockProxy fake)
    {
      final LabelBlockSubject fsubject = (LabelBlockSubject) fake;
      final EdgeSubject fedge = (EdgeSubject) fsubject.getParent();
      final EdgeSubject oedge = (EdgeSubject) getOriginal(fedge);
      return oedge == null ? null : oedge.getLabelBlock();
    }

    public GuardActionBlockSubject visitGuardActionBlockProxy
      (final GuardActionBlockProxy fake)
    {
      final GuardActionBlockSubject fsubject =
        (GuardActionBlockSubject) fake;
      final EdgeSubject fedge = (EdgeSubject) fsubject.getParent();
      final EdgeSubject oedge = (EdgeSubject) getOriginal(fedge);
      return oedge == null ? null : oedge.getGuardActionBlock();
    }

    public LabelGeometrySubject visitLabelGeometryProxy
      (final LabelGeometryProxy fake)
    {
      final LabelGeometrySubject fsubject = (LabelGeometrySubject) fake;
      final SimpleNodeSubject fnode = (SimpleNodeSubject) fsubject.getParent();
      final SimpleNodeSubject onode = (SimpleNodeSubject) getOriginal(fnode);
      return onode.getLabelGeometry();
    }
  }


  //#########################################################################
  //# Inner Class ChangeRecordCreatorVisitor
  private class ChangeRecordCreatorVisitor
    extends AbstractModuleProxyVisitor
  {
    //#######################################################################
    //# Invocation
    private ChangeRecord createChangeRecord(final ProxySubject fake,
                                            final int kind)
    {
      ChangeRecord record = mFakeMap.get(fake);
      if (record == null) {
        try{
          mOriginal = mOriginalGetter.getOriginal(fake);
          mFake = fake;
          mKind = kind;
          record = (ChangeRecord) fake.acceptVisitor(this);
          mOriginal = record.getOriginal();
          mOriginalMap.put(mOriginal, record);
          mFakeMap.put(fake, record);
        } catch (final VisitorException exception) {
          throw new WatersRuntimeException(exception);
        }
      } else {
        record.setChangeKind(kind);
      }
      return record;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    public ChangeRecord visitEdgeProxy(final EdgeProxy fake)
    {
      final EdgeSubject oedge = (EdgeSubject) mOriginal;
      final EdgeSubject fedge = (EdgeSubject) mFake;
      return new EdgeChangeRecord(oedge, fedge, mKind);
    }

    public ChangeRecord visitGroupNodeProxy(final GroupNodeProxy fake)
    {
      final GroupNodeSubject ogroup = (GroupNodeSubject) mOriginal;
      final GroupNodeSubject fgroup = (GroupNodeSubject) mFake;
      return new GroupNodeChangeRecord(ogroup, fgroup, mKind);
    }

    public ChangeRecord visitGuardActionBlockProxy
      (final GuardActionBlockProxy fake)
    {
      final GuardActionBlockSubject oblock =
        (GuardActionBlockSubject) mOriginal;
      final GuardActionBlockSubject fblock =
        (GuardActionBlockSubject) mFake;
      return new GuardActionBlockChangeRecord(oblock, fblock, mKind);
    }

    public ChangeRecord visitLabelBlockProxy(final LabelBlockProxy fake)
    {
      final LabelBlockSubject oblock = (LabelBlockSubject) mOriginal;
      final LabelBlockSubject fblock = (LabelBlockSubject) mFake;
      return new LabelBlockChangeRecord(oblock, fblock, mKind);
    }

    public ChangeRecord visitLabelGeometryProxy(final LabelGeometryProxy fake)
    {
      final LabelGeometrySubject ogeo = (LabelGeometrySubject) mOriginal;
      final LabelGeometrySubject fgeo = (LabelGeometrySubject) mFake;
      return new LabelGeometryChangeRecord(ogeo, fgeo, mKind);
    }

    public ChangeRecord visitSimpleNodeProxy(final SimpleNodeProxy fake)
    {
      final SimpleNodeSubject onode = (SimpleNodeSubject) mOriginal;
      final SimpleNodeSubject fnode = (SimpleNodeSubject) mFake;
      return new SimpleNodeChangeRecord(onode, fnode, mKind);
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

  private boolean mIsUpdating = false;


  //#########################################################################
  //# Class Constants
  private static final int PASS0_UNLINK = 0;
  private static final int PASS1_REMOVE = 1;
  private static final int PASS2_ADD = 2;
  private static final int PASS3_RELINK = 3;

  private static final int FIRST_PASS = PASS0_UNLINK;
  private static final int LAST_PASS = PASS3_RELINK;

  private static final LabelGeometrySubject DEFAULT_LABELBLOCK_GEO =
    new LabelGeometrySubject(LabelBlockProxyShape.DEFAULT_OFFSET);

}
