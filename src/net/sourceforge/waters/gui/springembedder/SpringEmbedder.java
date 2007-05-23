//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui.springembedder
//# CLASS:   SpringEmbedder
//###########################################################################
//# $Id: SpringEmbedder.java,v 1.35 2007-05-23 08:36:50 robi Exp $
//###########################################################################


package net.sourceforge.waters.gui.springembedder;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import javax.swing.SwingUtilities;

import net.sourceforge.waters.gui.renderer.GeometryAbsentException;
import net.sourceforge.waters.gui.renderer.GeometryTools;
import net.sourceforge.waters.gui.renderer.LabelBlockProxyShape;
import net.sourceforge.waters.subject.module.EdgeSubject;
import net.sourceforge.waters.subject.module.GraphSubject;
import net.sourceforge.waters.subject.module.GroupNodeSubject;
import net.sourceforge.waters.subject.module.LabelBlockSubject;
import net.sourceforge.waters.subject.module.LabelGeometrySubject;
import net.sourceforge.waters.subject.module.NodeSubject;
import net.sourceforge.waters.subject.module.PointGeometrySubject;
import net.sourceforge.waters.subject.module.SimpleNodeSubject;
import net.sourceforge.waters.subject.module.SplineGeometrySubject;

import net.sourceforge.waters.xsd.module.SplineKind;


public class SpringEmbedder
  implements Runnable, EmbedderSubject
{

  //#########################################################################
  //# Constructors
  public SpringEmbedder(final GraphSubject graph)
  {
    this(graph, System.currentTimeMillis());
  }
  
  public SpringEmbedder(final Collection<NodeSubject> nodes,
                        final Collection<EdgeSubject> edges)
  {
    this(nodes, edges, null, System.currentTimeMillis());
  }

  public SpringEmbedder(final Collection<NodeSubject> nodes,
                        final Collection<EdgeSubject> edges,
                        final LabelBlockSubject blocked)
  {
    this(nodes, edges, blocked, System.currentTimeMillis());
  }

  public SpringEmbedder(final GraphSubject graph, final long seed)
  {
    this(graph.getNodesModifiable(),
         graph.getEdgesModifiable(),
         graph.getBlockedEvents(),
         seed);
  }
  
  public SpringEmbedder(final Collection<NodeSubject> nodes,
                        final Collection<EdgeSubject> edges,
                        final LabelBlockSubject blocked,
                        final long seed)
  {
    mRandom = new Random(seed);
    mNodes = nodes;
    mEdges = edges;
    mBlocked = blocked;
    mObservers = new LinkedList<EmbedderObserver>();
  }


  //#########################################################################
  //# Observer Pattern
  public void addObserver(final EmbedderObserver observer)
  {
    mObservers.add(observer);
  }
  
  public void removeObserver(final EmbedderObserver observer)
  {
    mObservers.remove(observer);
  }

  
  //#########################################################################
  //# Interface java.lang.Runnable
  public void run()
  {
    createWrappers();
    mSpringEmbedders.add(this);
    fireStarted();
    runToConvergence();
    mSpringEmbedders.remove(this);
    SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          updateModel();
          fireStopped();
        }
      });
  }
  

  //#########################################################################
  //# Geometry Setup
  public boolean setUpGeometry()
    throws GeometryAbsentException
  {
    boolean runEmbedder = false;
    if (mBlocked != null) {
      if (mBlocked.getGeometry() == null) {
        // *** BUG ***
        // Must calculate better position!
        // ***
        mBlocked.setGeometry(new LabelGeometrySubject(new Point(5, 5)));
      }
    }

    // *** BUG ***
    // This should used named constants throughout!
    // ***
    for (final NodeSubject node : mNodes) {
      if (node instanceof SimpleNodeSubject) {
        final SimpleNodeSubject simple = (SimpleNodeSubject) node;
        if (simple.isInitial()) {
          if (simple.getInitialArrowGeometry() == null) {
            simple.setInitialArrowGeometry
              (new PointGeometrySubject(new Point(-5, -5)));
          }
        }
        if (simple.getPointGeometry() == null) {
          runEmbedder = true;
          final int base;
          final int spread;
          if (simple.isInitial()) {
            base = 10;
            spread = 50;
          } else {
            base = 100;
            spread = 500;
          }
          simple.setPointGeometry
            (new PointGeometrySubject
                    (new Point(base + mRandom.nextInt(spread),
                               base + mRandom.nextInt(spread))));
        }
        if (simple.getLabelGeometry() == null) {
          simple.setLabelGeometry
            (new LabelGeometrySubject(new Point(5, 5)));
        }
      } else if (node instanceof GroupNodeSubject) {
        final GroupNodeSubject group = (GroupNodeSubject) node;
        if (group.getGeometry() == null) {
          throw new GeometryAbsentException
            ("There is no geometry information for group node '" +
             group.getName() + "' in this graph!");
        }
      } else {
        throw new ClassCastException
          ("Unknown node type: " + node.getClass().getName() + "!");
      }
    }

    for (final EdgeSubject edge : mEdges) {
      edge.setStartPoint(null);
      edge.setEndPoint(null);
      if (edge.getLabelBlock().getGeometry() == null) {
        final LabelGeometrySubject offset =
          new LabelGeometrySubject
               (new Point(LabelBlockProxyShape.DEFAULTOFFSETX,
                          LabelBlockProxyShape.DEFAULTOFFSETY));
        edge.getLabelBlock().setGeometry(offset);
      }
      if (edge.getGuardActionBlock() != null &&
          edge.getGuardActionBlock().getGeometry() == null) {
        // *** BUG ***
        // Not a very good position!
        // ***
        final LabelGeometrySubject offset =
          new LabelGeometrySubject
               (new Point(LabelBlockProxyShape.DEFAULTOFFSETX,
                          LabelBlockProxyShape.DEFAULTOFFSETY + 10));
        edge.getGuardActionBlock().setGeometry(offset);
      }
    }

    return runEmbedder;
  }


  //#########################################################################
  //# Observer Pattern
  private void fireEvent(final EmbedderEvent event)
  {
    // Just in case they try to change the list in response to the call ...
    final Collection<EmbedderObserver> copy =
      new ArrayList<EmbedderObserver>(mObservers);
    for (EmbedderObserver observer : copy) {
      observer.embedderChanged(event);
    }
  }

  private void fireStarted()
  {
    final EmbedderEvent event = EmbedderEvent.createEmbedderStartEvent(this);
    fireEvent(event);
  }

  private void fireStopped()
  {
    final EmbedderEvent event = EmbedderEvent.createEmbedderStopEvent(this);
    fireEvent(event);
  }

  private void fireProgress()
  {
    final EmbedderEvent event =
      EmbedderEvent.createEmbedderProgressEvent(this);
    fireEvent(event);
  }


  //#########################################################################
  //# Progress Reporting
  public int getProgress()
  {
    return NUM_PASSES - mPass;
  }

  public static int getMaxProgress()
  {
    return NUM_PASSES + 1;
  }


  //##########################################################################
  //# Auxiliary Methods
  private void createWrappers()
  {
    final int numnodes = mNodes.size();
    mNodeWrappers = new NodeWrapper[numnodes];
    mNodeMap = new HashMap<SimpleNodeSubject,NodeWrapper>(numnodes);
    mNextWrapperId = 0;
    for (final NodeSubject node : mNodes) {
      if (node instanceof SimpleNodeSubject) {
        final SimpleNodeSubject simple = (SimpleNodeSubject) node;
        final NodeWrapper wrapper = new NodeWrapper(simple);
        final int id = wrapper.getId();
        mNodeWrappers[id] = wrapper;
        mNodeMap.put(simple, wrapper);
      } else {
        throw new IllegalStateException
          ("SpringEmbedder does not support nodes of type " +
           node.getClass().getName() + "!");
      }
    }
    final int numedges = mEdges.size();
    mEdgeWrappers = new EdgeWrapper[numedges];
    mEdgeMap = new HashMap<EdgeSubject,EdgeWrapper>(numedges);
    mNextWrapperId = 0;
    for (final EdgeSubject edge : mEdges) {
      final EdgeWrapper wrapper = new EdgeWrapper(edge);
      final int id = wrapper.getId();
      mEdgeWrappers[id] = wrapper;
      mEdgeMap.put(edge, wrapper);
      for (final NodeWrapper node : mNodeMap.values()) {
        node.addNeighbours(wrapper);
      }
    }
    mMultiEdgePairs = new LinkedList<EdgeWrapperPair>();
    for (int e1 = 0; e1 < numedges; e1++) {
      final EdgeWrapper edge1 = mEdgeWrappers[e1];
      final NodeWrapper source1 = edge1.getSource();
      final NodeWrapper target1 = edge1.getTarget();
      for (int e2 = e1 + 1; e2 < numedges; e2++) {
        final EdgeWrapper edge2 = mEdgeWrappers[e2];
        if (edge2.getSource() == source1 && edge2.getTarget() == target1) {
          final EdgeWrapperPair pair = new EdgeWrapperPair(edge1, edge2);
          mMultiEdgePairs.add(pair);
        }
      }        
    }
    mWrapperSet = new WrapperSet();
    mBackgroundAttraction = BACKGROUND_ATTRACTION / numnodes;
    mInitialStateAttraction = INITIALSTATE_ATTRACTION;
    mNodeRepulsion = NODE_REPULSION / numnodes;
    mNodeEdgeRepulsion = NODEEDGE_REPULSION / numnodes;
    mEdgeRepulsion = EDGE_REPULSION / numedges;
    mCenter = (Point2D) POINT_CENTER.clone();
  }

  private void runToConvergence()
  {
    if (!mStop) {
      int count = 0;
      double limit = CONVERGENCE_CONST;
      for (int i = 1; i < NUM_PASSES; i++) {
        limit *= 4.0;
      }
      for (mPass = NUM_PASSES; mPass >= 0; mPass--) {
        fireProgress();
        double maxdelta;
        do {
          maxdelta = calculateDisplacements();
          if (count++ >= UPDATE_CONST) {
            count = 0;
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                  updateModel();
                }
              });
            Thread.yield();
          }
        } while (maxdelta > limit && !mStop);
        limit *= 0.25;
      }
    }
  }

  private synchronized double calculateDisplacements()
  {
    if (mPass == 1 && mInitialStateAttraction > 0.0) {
      mInitialStateAttraction -= INITIALSTATE_DECAY;
    }
    final Collection<? extends GeometryWrapper> wrappers =
      calculateNewPoints();
    double maxdelta = 0.0;
    double movex = 0.0;
    double movey = 0.0;
    if (mPass > 0) {
      double minx = Double.MAX_VALUE;
      double miny = Double.MAX_VALUE;
      for (final GeometryWrapper wrapper : wrappers) {
        final Point2D point = wrapper.getNewPoint();
        final double x = point.getX();
        final double y = point.getY();
        if (x < minx) {
          minx = x;
        }
        if (y < miny) {
          miny = y;
        }
      }
      final double dx = GRAPH_MARGINAL_CONST - minx;
      final double dy = GRAPH_MARGINAL_CONST - miny;
      final double factor = mPass * CENTER_MOVE_CONST;
      movex = factor * dx;
      movey = factor * dy;
      final double newx = mCenter.getX() + movex;
      final double newy = mCenter.getY() + movey;
      mCenter.setLocation(newx, newy);
      if (mPass > 1) {
        maxdelta = Math.sqrt(movex * movex + movey * movey);
      }
    }
    if (mPass == 1 && mInitialStateAttraction > 0.0) {
      return 100.0;
    }
    for (final GeometryWrapper wrapper : wrappers) {
      final double delta = wrapper.getDelta();
      if (delta > maxdelta) {
        maxdelta = delta;
      }
    }
    for (final GeometryWrapper wrapper : wrappers) {
      if (mPass > 0) {
        wrapper.move(movex, movey);
      }
      wrapper.updatePoint();
    }
    return maxdelta;
  }

  private Collection<? extends GeometryWrapper> calculateNewPoints()
  {
    for (final NodeWrapper node : mNodeWrappers) {
      node.initializeNewPoint();
    }
    final int numnodes = mNodeWrappers.length;
    for (int n1 = 0; n1 < numnodes; n1++) {
      final NodeWrapper node1 = mNodeWrappers[n1];
      for (int n2 = n1 + 1; n2 < numnodes; n2++) {
        final NodeWrapper node2 = mNodeWrappers[n2];
        final Point2D delta = node1.calculateNodeNodeDisplacement(node2);
        node1.move(delta);
        node2.moveNegative(delta);
      }
    }
    for (final EdgeWrapper edge : mEdgeWrappers) {
      edge.initializeNewPoint();
      if (mPass < NUM_PASSES) {
        for (final NodeWrapper node : mNodeWrappers) {
          final Point2D delta = edge.calculateEdgeNodeDisplacement(node);
          edge.move(delta);
          if (mPass > 0) {
            node.moveNegative(delta);
          }
        }
      }
    }
    if (mPass < NUM_PASSES) {
      for (final EdgeWrapperPair pair : mMultiEdgePairs) {
        final EdgeWrapper edge1 = pair.getEdge1();
        final EdgeWrapper edge2 = pair.getEdge2();
        final Point2D delta = edge1.calculateEdgeEdgeDisplacement(edge2);
        edge1.move(delta);
        edge2.moveNegative(delta);
      }
    }
    return mPass == 0 ? mEdgeMap.values() : mWrapperSet;
  }

  private synchronized void updateModel()
  {
    for (final GeometryWrapper wrapper : mWrapperSet) {
      wrapper.updateModel();
    }
  }

  private Point2D repulsion(final Point2D p1,
                            final Point2D p2,
                            final double constant)
  {
    final double dx = p1.getX() - p2.getX();
    final double dy = p1.getY() - p2.getY();
    final double len = dx * dx + dy * dy;
    if (len > EPSILON) {
      final int pass = mPass > 0 ? mPass : 1;
      final double factor = pass * constant / len;
      return new Point2D.Double(factor * dx, factor * dy);
    } else {
      return new Point2D.Double(mRandom.nextDouble(), mRandom.nextDouble());
    }
  }

  private Point2D attraction(final Point2D p1,
                             final Point2D p2,
                             final double constant)
  {
    final double dx = p1.getX() - p2.getX();
    final double dy = p1.getY() - p2.getY();
    final int pass = mPass > 0 ? mPass : 1;
    double factor = pass * constant;
    if (factor > 0.5) {
      factor = 0.5;
    }
    return new Point2D.Double(-dx * factor, -dy * factor);
  }



  private Point2D edgeAttraction(final Point2D p,
                                 final Point2D start,
                                 final Point2D end,
                                 final double constant)
  {
    final double px = p.getX();
    final double py = p.getY();
    final double mx = 0.5 * (end.getX() + start.getX());
    final double my = 0.5 * (end.getY() + start.getY());
    final double dx = end.getX() - start.getX();
    final double dy = end.getY() - start.getY();
    final double det = dx * dx + dy * dy;
    if (det > 0.0) {
      final double factor = (dy * (py - my) + dx * (px - mx)) / det;
      final double x = px - factor * dx;
      final double y = py - factor * dy;
      final Point2D base = new Point2D.Double(x, y);
      return attraction(p, base, constant);
    } else {
      return POINT_ZERO;
    }
  }

  public static void stopAll()
  {
    synchronized(mSpringEmbedders) {
      for (SpringEmbedder embedder : mSpringEmbedders) {
        embedder.stop();
      }
    }
  }

  public void stop()
  {
    mStop = true;
  }
  

  //#########################################################################
  //# Inner Class GeometryWrapper

  private abstract class GeometryWrapper
  {
    //#######################################################################
    //# Constructor
    private GeometryWrapper(final Point2D old)
    {
      mId = mNextWrapperId++;
      mOldPoint = old;
    }

    //#######################################################################
    //# Simple Access
    int getId()
    {
      return mId;
    }

    Point2D getOldPoint()
    {
      return mOldPoint;
    }

    Point2D getNewPoint()
    {
      return mNewPoint;
    }

    void setNewPoint(final Point2D point)
    {
      final double x = point.getX();
      final double y = point.getY();
      setNewPoint(x, y);
    }

    void setNewPoint(final double x, final double y)
    {
      if (mNewPoint == null || mNewPoint == mOldPoint) {
        mNewPoint = new Point2D.Double(x, y);
      } else {
        mNewPoint.setLocation(x, y);
      }
    }

    void move(final Point2D delta)
    {
      move(delta, 1.0);
    }

    void moveNegative(final Point2D delta)
    {
      move(delta, -1.0);
    }

    void move(final Point2D delta, final double factor)
    {
      final double dx = factor * delta.getX();
      final double dy = factor * delta.getY();
      move(dx, dy);
    }

    void move(final double dx, final double dy)
    {
      final double newx = mNewPoint.getX() + dx;
      final double newy = mNewPoint.getY() + dy;
      setNewPoint(newx, newy);
    }

    //#######################################################################
    //# Auxiliary Methods
    void initializeNewPoint()
    {
      setNewPoint(mOldPoint);
    }
 
    void updatePoint()
    {
      mOldPoint = mNewPoint;
    }

    double getDeltaChange(final Point2D reference)
    {
      return getDeltaChange(reference, reference);
    }

    double getDeltaChange(final GeometryWrapper reference)
    {
      return getDeltaChange(reference.getOldPoint(), reference.getNewPoint());
    }

    double getDeltaChange(final Point2D oldReference,
                          final Point2D newReference)
    {
      final double oldDelta = mOldPoint.distance(oldReference);
      final double newDelta = mNewPoint.distance(newReference);
      return Math.abs(oldDelta - newDelta);
    }

    Point2D getMove()
    {
      final double dx = mNewPoint.getX() - mOldPoint.getX();
      final double dy = mNewPoint.getY() - mOldPoint.getY();
      return new Point2D.Double(dx, dy);
    }

    //#######################################################################
    //# Layouting
    abstract void calculateDisplacement();
    abstract double getDelta();
    abstract void updateModel();

    //#######################################################################
    //# Data Members
    private final int mId;
    private Point2D mOldPoint;
    private Point2D mNewPoint;

  }


  //#########################################################################
  //# Inner Class NodeWrapper
  private class NodeWrapper extends GeometryWrapper
  {

    //#######################################################################
    //# Constructor
    private NodeWrapper(final SimpleNodeSubject node)
    {
      super(node.getPointGeometry().getPoint());
      mGeometry = node.getPointGeometry();
      mIsInitial = node.isInitial();
      mNeighbours = new HashMap<NodeWrapper,Double>();
      mSelfLoopFactor = 1.0;
    }

    //#######################################################################
    //# Auxiliary Methods
    private void addNeighbours(final EdgeWrapper edge)
    {
      final NodeWrapper other;
      if (edge.getSource() == this) {
        other = edge.getTarget();
      } else if (edge.getTarget() == this) {
        other = edge.getSource();
      } else {
        other = null;
      }
      if (other == this) {
        mSelfLoopFactor *= SELFLOOP_WEIGHT;
      } else if (other != null) {
        final Double weight = mNeighbours.get(other);
        if (weight == null) {
          mNeighbours.put(other, 1.0);
        } else {
          mNeighbours.put(other, weight * MULTI_EDGE_WEIGHT);
        }
      }
    }

    //#######################################################################
    //# Layouting Methods
    void calculateDisplacement()
    {
      final Point2D old = getOldPoint();
      final Point2D delta0 = attraction(old,
                                        mCenter,
                                        mBackgroundAttraction);
      double dx = delta0.getX();
      double dy = delta0.getY();
      if (mIsInitial && mInitialStateAttraction > EPSILON) {
        // Pull initial state towards the upper left corner...
        // Makes it easier to find and gives more consistently drawn graphs
        // with a natural flow from upper left towards lower right
        // This also means that the best position for node labels will be
        // above and to the right of the node... not below and to the left!
        final Point2D delta = attraction(old,
                                         POINT_ZERO,
                                         mInitialStateAttraction);
        dx += delta.getX();
        dy += delta.getY();
      }
      final double repulsion0 = mNodeRepulsion * mSelfLoopFactor;
      for (final NodeWrapper other : mNodeMap.values()) {
        if (other != this) {
          final double repulsion = repulsion0 * other.mSelfLoopFactor;
          final Point2D delta = repulsion(old,
                                          other.getOldPoint(),
                                          repulsion);
          dx += delta.getX();
          dy += delta.getY();
        }
      }
      final double degree = mNeighbours.size();
      final Set<Map.Entry<NodeWrapper,Double>> entries =
        mNeighbours.entrySet();
      for (final Map.Entry<NodeWrapper,Double> entry : entries) {
        final NodeWrapper other = entry.getKey();
        final double weight = entry.getValue() / degree;
        final Point2D delta = attraction(old,
                                         other.getOldPoint(),
                                         weight * NODE_ATTRACTION);
        dx += delta.getX();
        dy += delta.getY();
      }
      final double x = old.getX() + dx;
      final double y = old.getY() + dy;
      setNewPoint(x, y);
    }

    void initializeNewPoint()
    {
      super.initializeNewPoint();
      final Point2D old = getOldPoint();
      final Point2D delta1 = attraction(old,
                                        mCenter,
                                        mBackgroundAttraction);
      move(delta1);
      if (mIsInitial && mInitialStateAttraction > EPSILON) {
        // Pull initial state towards the upper left corner...
        // Makes it easier to find and gives more consistently drawn graphs
        // with a natural flow from upper left towards lower right
        // This also means that the best position for node labels will be
        // above and to the right of the node... not below and to the left!
        final Point2D delta2 = attraction(old,
                                          POINT_ZERO,
                                          mInitialStateAttraction);
        move(delta2);
      }
    }

    Point2D calculateNodeNodeDisplacement(final NodeWrapper other)
    {
      final Point2D old = getOldPoint();
      final Point2D otherold = other.getOldPoint();
      final double repulsion0 =
        mNodeRepulsion * mSelfLoopFactor * other.mSelfLoopFactor;
      final Point2D delta = repulsion(old, otherold, repulsion0);
      if (mNeighbours.containsKey(other)) {
        final int degree = mNeighbours.size();
        final int otherdegree = other.mNeighbours.size();
        final double weight =
          mNeighbours.get(other) * (0.5 / degree + 0.5 / otherdegree);
        final Point2D delta1 =
          attraction(old, otherold, weight * NODE_ATTRACTION);
        final double dx = delta.getX() + delta1.getX();
        final double dy = delta.getY() + delta1.getY();
        delta.setLocation(dx, dy);
      }
      return delta;
    }

    double getDelta()
    {
      double delta = 0.0;
      if (mPass > 1 || mNeighbours.isEmpty()) {
        if (mIsInitial && mPass > 1) {
          delta += getDeltaChange(POINT_ZERO);
        } else {
          delta += getDeltaChange(mCenter);
        }
      }
      for (final NodeWrapper other : mNodeMap.values()) {
        if (other != this) {
          delta += getDeltaChange(other);
        }
      }
      for (final NodeWrapper other : mNeighbours.keySet()) {
        delta += getDeltaChange(other);
      }
      final int count = mNodeMap.size() + mNeighbours.size();
      if (count > 0.0) {
        return delta / count;
      } else {
        return delta;
      }
    }

    void updateModel()
    {
      final Point2D point = getNewPoint();
      mGeometry.setPoint(point);
    }


    //#######################################################################
    //# Data Members
    private final PointGeometrySubject mGeometry;
    private final boolean mIsInitial;
    private final Map<NodeWrapper,Double> mNeighbours;
    private double mSelfLoopFactor;
  }


  //#########################################################################
  //# Inner Class EdgeWrapper
  private class EdgeWrapper extends GeometryWrapper
  {

    //#######################################################################
    //# Constructor
    private EdgeWrapper(final EdgeSubject edge)
    {
      super(GeometryTools.getTurningPoint1(edge));
      mEdge = edge;
      final NodeSubject source = edge.getSource();
      final NodeWrapper node1 = mNodeMap.get(source);
      final NodeSubject target = edge.getTarget();
      final NodeWrapper node2 = mNodeMap.get(target);
      if (node1.getId() < node2.getId()) {
        mSource = node1;
        mTarget = node2;
      } else {
        mSource = node2;
        mTarget = node1;
      }
    }

    //#######################################################################
    //# Simple Access
    private NodeWrapper getSource()
    {
      return mSource;
    }

    private NodeWrapper getTarget()
    {
      return mTarget;
    }

    //#######################################################################
    //# Layouting Methods
    void calculateDisplacement()
    {
      final Point2D old = getOldPoint();
      double dx = 0.0;
      double dy = 0.0;
      for (final EdgeWrapper other : mEdgeMap.values()) {
        if (other != this) {
          final Point2D delta = repulsion(old,
                                          other.getOldPoint(),
                                          mEdgeRepulsion);
          dx += delta.getX();
          dy += delta.getY();
        }
      }
      for (final NodeWrapper node : mNodeMap.values()) {
        if (node != mSource && node != mTarget) {
          final Point2D closest = getClosestPoint(node);
          final Point2D delta = repulsion(closest,
                                          node.getOldPoint(),
                                          mNodeEdgeRepulsion);
          dx += delta.getX();
          dy += delta.getY();
        } else {
          final Point2D delta = attraction(old,
                                           node.getOldPoint(),
                                           EDGE_ATTRACTION);
          dx += delta.getX();
          dy += delta.getY();
        }
      }
      final double x = old.getX() + dx;
      final double y = old.getY() + dy;
      setNewPoint(x, y);
    }

    void initializeNewPoint()
    {
      super.initializeNewPoint();
      final Point2D old = getOldPoint();
      if (mSource == mTarget) {
        final Point2D hook = mSource.getOldPoint();
        final Point2D delta1 = repulsion(old, hook, SELFLOOP_REPULSION);
        move(delta1);
        final Point2D delta2 = attraction(old, hook, EDGE_ATTRACTION);
        move(delta2);
      } else {
        final Point2D source = mSource.getOldPoint();
        final Point2D target = mTarget.getOldPoint();
        final Point2D delta1 = attraction(old, source, EDGE_ATTRACTION);
        move(delta1);
        final Point2D delta2 = attraction(old, target, EDGE_ATTRACTION);
        move(delta2);
        final Point2D delta3 =
          edgeAttraction(old, source, target, EDGE_ATTRACTION);
        move(delta3);
      }
    }

    Point2D calculateEdgeNodeDisplacement(final NodeWrapper node)
    {
      if (node == mSource || node == mTarget) {
        return POINT_ZERO;
      } else {
        final Point2D closest = getClosestPoint(node);
        return repulsion(closest, node.getOldPoint(), mNodeEdgeRepulsion);
      }
    }

    Point2D calculateEdgeEdgeDisplacement(final EdgeWrapper other)
    {
      final Point2D old = getOldPoint();
      return repulsion(old, other.getOldPoint(), mEdgeRepulsion);
    }

    double getDelta()
    {
      if (mPass > 0) {
        return 0.5 * (getDeltaChange(mSource) + getDeltaChange(mTarget));
      } else {
        final Point2D oldpoint = getOldPoint();
        final Point2D newpoint = getNewPoint();
        return oldpoint.distance(newpoint);
      }
    }

    void updateModel()
    {
      final Point2D point = getNewPoint();
      GeometryTools.createMidGeometry(mEdge, point, SplineKind.INTERPOLATING);
    }


    //#######################################################################
    //# Auxiliary Methods
    final Point2D getClosestPoint(final NodeWrapper node)
    {
      if (mSource == mTarget) {
        return getOldPoint();
      } else {
        final Point2D start = mSource.getOldPoint();
        final Point2D end = mTarget.getOldPoint();
        final Point2D mid = getOldPoint();
        final double x = 2.0 * mid.getX() - 0.5 * (start.getX() + end.getX());
        final double y = 2.0 * mid.getY() - 0.5 * (start.getY() + end.getY());
        final Point2D control = new Point2D.Double(x, y);
        final Point2D point = node.getOldPoint();
        return GeometryTools.findClosestPointOnQuadratic
          (start, control, end, point);
      }
    }

    //#######################################################################
    //# Data Members
    private final EdgeSubject mEdge;
    private final NodeWrapper mSource;
    private final NodeWrapper mTarget;
  }


  //#########################################################################
  //# Inner Class EdgeWrapperPair
  private class EdgeWrapperPair
  {

    //#######################################################################
    //# Constructor
    private EdgeWrapperPair(final EdgeWrapper edge1, final EdgeWrapper edge2)
    {
      mEdge1 = edge1;
      mEdge2 = edge2;
    }

    //#######################################################################
    //# Simple Access
    private EdgeWrapper getEdge1()
    {
      return mEdge1;
    }

    private EdgeWrapper getEdge2()
    {
      return mEdge2;
    }

    //#######################################################################
    //# Data Members
    private final EdgeWrapper mEdge1;
    private final EdgeWrapper mEdge2;

  }


  //#########################################################################
  //# Inner Class WrapperSet
  private class WrapperSet extends AbstractSet<GeometryWrapper>
  {

    //#######################################################################
    //# Interface java.util.Collection
    public Iterator<GeometryWrapper> iterator()
    {
      return new WrapperSetIterator();
    }

    public int size()
    {
      return mNodeMap.size() + mEdgeMap.size();
    }

  }


  //#########################################################################
  //# Inner Class WrapperSetIterator
  private class WrapperSetIterator implements Iterator<GeometryWrapper>
  {

    //#######################################################################
    //# Constructor
    private WrapperSetIterator()
    {
      mInNodes = true;
      mIterator = mNodeMap.values().iterator();
    }

    //#######################################################################
    //# Interface java.util.Iterator
    public boolean hasNext()
    {
      if (mIterator.hasNext()) {
        return true;
      } else if (mInNodes) {
        mInNodes = false;
        mIterator = mEdgeMap.values().iterator();
        return mIterator.hasNext();
      } else {
        return false;
      }
    }

    public GeometryWrapper next()
    {
      if (mInNodes && !mIterator.hasNext()) {
        mInNodes = false;
        mIterator = mEdgeMap.values().iterator();
      }
      return mIterator.next();
    }

    public void remove()
    {
      throw new UnsupportedOperationException
        ("SpringEmbedder.WrapperSet does not support remove()!");
    }

    //#######################################################################
    //# Data Members
    private boolean mInNodes;
    private Iterator<? extends GeometryWrapper> mIterator;

  }


  //#########################################################################
  //# Data Members
  private final Random mRandom;
  private final Collection<NodeSubject> mNodes;
  private final Collection<EdgeSubject> mEdges;
  private final LabelBlockSubject mBlocked;
  private final Collection<EmbedderObserver> mObservers;

  private NodeWrapper[] mNodeWrappers;
  private EdgeWrapper[] mEdgeWrappers;
  private Map<SimpleNodeSubject,NodeWrapper> mNodeMap;
  private Map<EdgeSubject,EdgeWrapper> mEdgeMap;
  private List<EdgeWrapperPair> mMultiEdgePairs;
  private Set<GeometryWrapper> mWrapperSet;
  private double mBackgroundAttraction;
  private double mNodeRepulsion;
  private double mNodeEdgeRepulsion;
  private double mEdgeRepulsion;
  private Point2D mCenter;
  private double mInitialStateAttraction;
  private int mNextWrapperId;
  private int mPass;

  private volatile boolean mStop = false;

  private static final List<SpringEmbedder> mSpringEmbedders =
    Collections.synchronizedList(new LinkedList<SpringEmbedder>());


  //###########################################################################
  //# Class Constants
  private static final int NUM_PASSES = 3;
  private static final double EPSILON = 0.00002;
  private static final double MULTI_EDGE_WEIGHT = 1.333;
  private static final double SELFLOOP_WEIGHT = 1.05;

  private static final double BACKGROUND_ATTRACTION = 0.05;
  private static final double INITIALSTATE_ATTRACTION =
    0.22 * BACKGROUND_ATTRACTION;
  private static final double INITIALSTATE_DECAY =
    0.01 * INITIALSTATE_ATTRACTION;
  private static final double EDGE_ATTRACTION = 0.05;
  private static final double NODE_ATTRACTION = 0.1;
  private static final double NODE_REPULSION = 1000.0;
  private static final double SELFLOOP_REPULSION = 100.0;
  private static final double EDGE_REPULSION = 80.0;
  private static final double NODEEDGE_REPULSION = 160.0;

  private static final double GRAPH_MARGINAL_CONST = 48.0;
  private static final double CENTER_MOVE_CONST = 0.01;
  private static final double CONVERGENCE_CONST = 0.030;
  private static final int UPDATE_CONST = 10;

  private static final Point2D POINT_ZERO = new Point2D.Double(0.0, 0.0);
  private static final Point2D POINT_CENTER = new Point2D.Double(200.0, 200.0);

}
