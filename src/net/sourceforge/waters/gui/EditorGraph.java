//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   EditorGraph
//###########################################################################
//# $Id: EditorGraph.java,v 1.20 2007-08-10 04:34:31 robi Exp $
//###########################################################################

package net.sourceforge.waters.gui;

import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import net.sourceforge.waters.gui.command.Command;
import net.sourceforge.waters.gui.command.MoveObjects;
import net.sourceforge.waters.gui.renderer.GeometryTools;

import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.ModuleProxyVisitor;
import net.sourceforge.waters.model.module.NodeProxy;
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
import net.sourceforge.waters.subject.module.IdentifierSubject;
import net.sourceforge.waters.subject.module.LabelBlockSubject;
import net.sourceforge.waters.subject.module.LabelGeometrySubject;
import net.sourceforge.waters.subject.module.NodeSubject;
import net.sourceforge.waters.subject.module.PointGeometrySubject;
import net.sourceforge.waters.subject.module.SimpleNodeSubject;
import net.sourceforge.waters.subject.module.SplineGeometrySubject;

import net.sourceforge.waters.xsd.module.SplineKind;


public class EditorGraph
  extends MutableSubject
  implements GraphProxy
{
  public EditorGraph(final GraphSubject graph)
  {
    mGraph = graph;
    mChanged = new HashSet<Subject>();
    mObserverMap = new IdentityHashMap<NodeSubject,EditorNode>
      (graph.getNodes().size());
    mFakeMap = new IdentityHashMap<Subject,Subject>
      (graph.getNodes().size() + graph.getEdges().size()+1);
    mOriginalMap = new IdentityHashMap<Subject, Subject>
      (graph.getNodes().size() + graph.getEdges().size()+1);
    mEdges = new ArrayListSubject<EdgeSubject>(graph.getEdges().size());
    mNodes = new IndexedHashSetSubject<NodeSubject>(graph.getNodes().size());
    mEdges.setParent(this);
    mNodes.setParent(this);
    if (graph.getBlockedEvents() != null)	{
      mBlockedEvents = graph.getBlockedEvents().clone();
      mFakeMap.put(mBlockedEvents, graph.getBlockedEvents());
      mOriginalMap.put(graph.getBlockedEvents(), mBlockedEvents);
      mBlockedEvents.setParent(this);
    } else {
      mBlockedEvents = null;
    }

    final Collection<NodeSubject> nodes = graph.getNodesModifiable();
    final Iterator<NodeSubject> iter = nodes.iterator();
    while (iter.hasNext()) {
      NodeSubject temp = (NodeSubject) iter.next();
      if (temp instanceof SimpleNodeSubject) {
        SimpleNodeSubject np = (SimpleNodeSubject) temp;
        addNode(np);
      }	else if (temp instanceof GroupNodeSubject) {
        GroupNodeSubject gn = (GroupNodeSubject) temp;
        addGroupNode(gn);
      }
    }
    for (final EdgeSubject edge : graph.getEdgesModifiable()) {
      if (edge.getSource() != null && edge.getTarget() != null) {
        addEdge(edge);
      }	else {
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

  public Object acceptVisitor(ProxyVisitor p)
    throws VisitorException
  {
    return ((ModuleProxyVisitor)p).visitGraphProxy(this);
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

  public Set<NodeSubject> getNodeSubjects()
  {
    return Collections.unmodifiableSet(mNodes);
  }

  public List<EdgeSubject> getEdgeSubjects()
  {
    return Collections.unmodifiableList(mEdges);
  }

  public boolean isDeterministic()
  {
    return mGraph.isDeterministic();
  }


  //#########################################################################
  //# Accessing the Fake Maps
  public Subject getCopy(Subject o)
  {
    Subject s = mOriginalMap.get(o);
    if (s == null) {
      if (o instanceof LabelBlockSubject) {
        s = getCopy((LabelBlockSubject)o);
      } else if (o instanceof GuardActionBlockSubject) {
        s = getCopy((GuardActionBlockSubject)o);
      } else if (o instanceof LabelGeometrySubject) {
        s = getCopy((LabelGeometrySubject)o);
      }
    }
    return s;
  }
	
  public Subject getCopy(LabelBlockSubject o)
  {
    if (o.getParent() instanceof EdgeSubject) {
      EdgeSubject s = (EdgeSubject)mOriginalMap.get(o.getParent());
      return s.getLabelBlock();
    } else {
      return mOriginalMap.get(o);
    }
  }

  public Subject getCopy(GuardActionBlockSubject o)
  {
    EdgeSubject s = (EdgeSubject)mOriginalMap.get(o.getParent());
    return s.getGuardActionBlock();
  }

  public Subject getCopy(LabelGeometrySubject o)
  {
    SimpleNodeSubject s = (SimpleNodeSubject)mOriginalMap.get(o.getParent());
    return s.getLabelGeometry();
  }

  public Subject getOriginal(Subject o)
  {
    Subject s = mFakeMap.get(o);
    if (s == null) {
      if (o instanceof LabelBlockSubject) {
        s = getOriginal((LabelBlockSubject)o);
      } else if (o instanceof GuardActionBlockSubject) {
        s = getOriginal((GuardActionBlockSubject)o);
      } else if (o instanceof LabelGeometrySubject) {
        s = getOriginal((LabelGeometrySubject)o);
      }
    }
    return s;
  }

  public Subject getOriginal(LabelBlockSubject o)
  {
    if (o.getParent() instanceof EdgeSubject) {
      EdgeSubject s = (EdgeSubject)mFakeMap.get(o.getParent());
      return s.getLabelBlock();
    } else {
      return mFakeMap.get(o);
    }
  }

  public Subject getOriginal(GuardActionBlockSubject o)
  {
    EdgeSubject s = (EdgeSubject)mFakeMap.get(o.getParent());
    return s.getGuardActionBlock();
  }

  public Subject getOriginal(LabelGeometrySubject o)
  {
    SimpleNodeSubject s = (SimpleNodeSubject)mFakeMap.get(o.getParent());
    return s.getLabelGeometry();
  }

  public Collection<EdgeSubject> getNodeEdges(NodeSubject n)
  {
    return Collections.unmodifiableCollection(mObserverMap.get(n).getEdges());
  }


  //#########################################################################
  //# Updating
  /**
   * Creates a command to transform the original graph into the same
   * state as this editor graph.
   * @param  name    The name to be given to the new command.
   */
  Command createUpdateCommand(final String description)
  {
    final Map<ProxySubject, ProxySubject> changed =
      new IdentityHashMap<ProxySubject, ProxySubject>(mChanged.size());
    for (Subject s : mChanged) {
      ProxySubject orig = (ProxySubject) getOriginal(s);
      if (orig != null) {
        changed.put(orig, (ProxySubject)s);
      }
    }
    return new MoveObjects(changed, mGraph, description);
  }


  //#########################################################################
  //# Auxiliary Methods
  private Point2D defaultPosition(NodeSubject node, Point2D turningpoint)
  {
    if (node instanceof SimpleNodeSubject) {
      return defaultPosition((SimpleNodeSubject)node);
    }
    return defaultPosition((GroupNodeSubject)node, turningpoint);
  }

  private Point2D defaultPosition(SimpleNodeSubject node)
  {
    return node.getPointGeometry().getPoint();
  }

  private Point2D defaultPosition(GroupNodeSubject node, Point2D point)
  {
    Rectangle2D r = node.getGeometry().getRectangle();
    return GeometryTools.findIntersection(r, point);
  }

  private Point2D getPosition(NodeSubject node)
  {
    if (node instanceof SimpleNodeSubject) {
      return getPosition((SimpleNodeSubject)node);
    }
    return getPosition((GroupNodeSubject)node);
  }

  private Point2D getPosition(SimpleNodeSubject node)
  {
    return node.getPointGeometry().getPoint();
  }

  private Point2D getPosition(GroupNodeSubject node)
  {
    Rectangle2D r = node.getGeometry().getRectangle();
    return new Point2D.Double(r.getCenterX(), r.getCenterY());
  }

  /**
   * Creates a copy of the given edge and adds it to this graph.
   * @param  edge0   The egde in the original graph a copy of which
   *                 is to be added.
   */
  EdgeSubject addEdge(final EdgeSubject edge0)
  {
    final EdgeSubject edge1 = edge0.clone();
    final NodeSubject source0 = edge0.getSource();
    final NodeSubject target0 = edge0.getTarget();
    if (source0 != null) {
      final NodeSubject source1 = (NodeSubject) mOriginalMap.get(source0);
      edge1.setSource(source1);
      final EditorNode node = mObserverMap.get(source1);
      node.addEdge(edge1);
    }
    if (target0 != null) {
      final NodeSubject target1 = (NodeSubject) mOriginalMap.get(target0);
      edge1.setTarget(target1);
      final EditorNode node = mObserverMap.get(target1);
      node.addEdge(edge1);
    }
    mEdges.add(edge1);
    mFakeMap.put(edge1, edge0);
    mOriginalMap.put(edge0, edge1);
    return edge1;
  }

  private void removeEdge(EdgeSubject e)
  {
    EdgeSubject remove = (EdgeSubject)mOriginalMap.get(e);
    mEdges.remove(remove);
    mFakeMap.remove(remove);
    mOriginalMap.remove(e);
  }

  private void removeNode(NodeSubject n)
  {
    NodeSubject remove = (NodeSubject)mOriginalMap.get(n);
    mNodes.remove(remove);
    mFakeMap.remove(remove);
    mOriginalMap.remove(n);
  }

  private void addGroupNode(GroupNodeSubject gn)
  {
    if (gn.getGeometry() == null) {
      throw new IllegalArgumentException("GroupNode " + gn +
                                         " has no Geometry Data");
    } else {
      GroupNodeSubject group = gn.clone();
      mNodes.add(group);
      mObserverMap.put(group, new EditorGroupNode(group));
      mFakeMap.put(group, gn);
      mOriginalMap.put(gn, group);
    }
  }

  private void addNode(SimpleNodeSubject n)
  {
    final SimpleNodeSubject node = n.clone();
    mNodes.add(node);
    mObserverMap.put(node, new EditorSimpleNode(node));
    mFakeMap.put(node, n);
    mOriginalMap.put(n, node);
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
    final LabelGeometrySubject geo0 = block0.getGeometry();
    final LabelGeometrySubject geo1 = block1.getGeometry();
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
  //# Static Class Methods
  public static LabelGeometrySubject defaultLabelBlockOffset()
  {
    Point2D p = new Point2D.Double(10, 10);
    return new LabelGeometrySubject(p);
  }

  public static LabelGeometrySubject defaultGuardActionBlockOffset()
  {
    Point2D p = new Point2D.Double(10, 10);
    return new LabelGeometrySubject(p);
  }

  public static LabelGeometrySubject defaultLabelOffset()
  {
    Point2D p = new Point2D.Double(-10, 10);
    return new LabelGeometrySubject(p);
  }

  public static PointGeometrySubject defaultNodePosition()
  {
    Random rand = new Random();
    Point2D p = new Point2D.Double(rand.nextInt(1000), rand.nextInt(1000));
    return new PointGeometrySubject(p);
  }

  public static PointGeometrySubject defaultInitialStateArrow()
  {
    Point2D p = new Point2D.Double(0, -10);
    return new PointGeometrySubject(p);
  }

  public static void updateChildNodes(GraphSubject graph) {
    List<GroupNodeSubject> groups = new ArrayList<GroupNodeSubject>();
    for (NodeSubject n : graph.getNodesModifiable()) {
      if (n instanceof GroupNodeSubject) {
        ((GroupNodeSubject) n).getImmediateChildNodesModifiable().clear();
        groups.add((GroupNodeSubject) n);
      }
    }
    // sort all the node groups from smallest to largest
    Collections.sort(groups, new Comparator<GroupNodeSubject>() {
      public int compare(GroupNodeSubject g1, GroupNodeSubject g2)
      {
        Rectangle2D r1 = g1.getGeometry().getRectangle();
        Rectangle2D r2 = g2.getGeometry().getRectangle();
        return (int) ((r1.getHeight() * r1.getWidth())
                      - (r2.getHeight() * r2.getWidth()));
      }
    });
    // go through all the nodes
    for (NodeSubject n : graph.getNodesModifiable()) {
      Rectangle2D r1;
      if (n instanceof GroupNodeSubject) {
        r1 = ((GroupNodeSubject) n).getGeometry().getRectangle();
      } else {
        Point2D p = ((SimpleNodeSubject) n).getPointGeometry().getPoint();
        r1 = new Rectangle((int) p.getX(), (int) p.getY(), 1, 1);
      }
      Collection<GroupNodeSubject> parents = new ArrayList<GroupNodeSubject>();
      mainloop:
      for (GroupNodeSubject group : groups) {
        if (n != group) {
          if (group.getGeometry().getRectangle().contains(r1)) {
            for (GroupNodeSubject parent : parents) {
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
      switch (event.getKind()) {
      case ModelChangeEvent.ITEM_ADDED:
      case ModelChangeEvent.ITEM_REMOVED:
	{
	  final Subject parent = esource.getParent();
	  if (parent != null && parent instanceof GroupNodeSubject) {
	    mChanged.add(parent);
	  }
	}
	break;
      case ModelChangeEvent.STATE_CHANGED:
	if (esource instanceof EdgeSubject) {
          mChanged.add(esource);
	  final EdgeSubject edge = (EdgeSubject) esource;
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
	mChanged.add(esource);
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
      mEdges = new IdentityHashMap<EdgeSubject, Boolean>();
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

    private final NodeSubject mNode;
    private final IdentityHashMap<EdgeSubject, Boolean> mEdges;
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
      mUpdate = true;
      final Point2D newpos = getNodeSubject().getPointGeometry().getPoint();
      if (!mPoint.equals(newpos)) {
	for (final EdgeSubject edge : getEdges()) {
	  transformEdge(edge, mPoint, newpos);
	}
	mPoint = newpos;
      }
      mUpdate = false;
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
	  } else if (edge.getTarget() == node)	{
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
  //# Data Members
  private boolean mUpdate = false;
  private final GraphSubject mGraph;
  private final ListSubject<EdgeSubject> mEdges;
  private final IndexedSetSubject<NodeSubject> mNodes;
  private final IdentityHashMap<NodeSubject, EditorNode> mObserverMap;
  private final IdentityHashMap<Subject, Subject> mFakeMap;
  private final IdentityHashMap<Subject, Subject> mOriginalMap;
  private final LabelBlockSubject mBlockedEvents;
  private final Set<Subject> mChanged;

}
