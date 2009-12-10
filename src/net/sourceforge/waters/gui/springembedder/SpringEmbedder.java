//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui.springembedder
//# CLASS:   SpringEmbedder
//###########################################################################
//# $Id$
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
import net.sourceforge.waters.gui.renderer.HornerPolynomial;
import net.sourceforge.waters.gui.renderer.LabelBlockProxyShape;
import net.sourceforge.waters.gui.renderer.SimpleNodeProxyShape;
import net.sourceforge.waters.subject.module.EdgeSubject;
import net.sourceforge.waters.subject.module.GraphSubject;
import net.sourceforge.waters.subject.module.GroupNodeSubject;
import net.sourceforge.waters.subject.module.LabelBlockSubject;
import net.sourceforge.waters.subject.module.LabelGeometrySubject;
import net.sourceforge.waters.subject.module.NodeSubject;
import net.sourceforge.waters.subject.module.PointGeometrySubject;
import net.sourceforge.waters.subject.module.SimpleNodeSubject;

import net.sourceforge.waters.xsd.module.SplineKind;

import org.supremica.properties.Config;


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
  /**
   * Checks whether this embedder's graph needs geometry.
   * This method checks whether the nodes of the embedder's graph have
   * got geometry associated with them, without making any changes to
   * the graph.
   * @return <CODE>true</CODE> if any is missing geometry.
   *         In this case, a call to {@link #setUpGeometry()} would
   *         change the graph.
   * @throws GeometryAbsentException if a group node without geometry
   *         has been found. Group nodes cannot be assigned geometry
   *         automatically, and therefore the graph cannot be rendered
   *         when this exception is thrown.
   *
   */
  public boolean needsGeometry()
    throws GeometryAbsentException
  {
    if (mBlocked != null) {
      if (mBlocked.getGeometry() == null) {
        return true;
      }
    }
    for (final NodeSubject node : mNodes) {
      if (node instanceof SimpleNodeSubject) {
        final SimpleNodeSubject simple = (SimpleNodeSubject) node;
        if (simple.getPointGeometry() == null) {
          return true;
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
    return false;
  }

  /**
   * Sets up initial geometry for this embedder's graph.
   * This method checks whether the nodes of the embedder's graph have
   * got geometry associated with them, and fills in random positions
   * for any simple nodes that have not. Labels without geometry are placed
   * at their default positions.
   * @return <CODE>true</CODE> if any node has been assigned geometry.
   *         In this case, the GUI should invoke the spring embedder
   *         to layout the graph.
   * @throws GeometryAbsentException if a group node without geometry
   *         has been found. Group nodes cannot be assigned geometry
   *         automatically, and therefore the graph cannot be rendered
   *         when this exception is thrown.
   */
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

    for (final NodeSubject node : mNodes) {
      if (node instanceof SimpleNodeSubject) {
        final SimpleNodeSubject simple = (SimpleNodeSubject) node;
        if (simple.isInitial()) {
          if (simple.getInitialArrowGeometry() == null) {
            final PointGeometrySubject geo =
              new PointGeometrySubject(SimpleNodeProxyShape.DEFAULT_INITARROW);
            simple.setInitialArrowGeometry(geo);
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
          final LabelGeometrySubject geo =
            new LabelGeometrySubject(SimpleNodeProxyShape.DEFAULT_OFFSET);
          simple.setLabelGeometry(geo);
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
      // Fixing some broken models---these adjustments should not
      // be needed, but without them many old files would not be
      // displayed correctly :-(
      if (edge.getSource() instanceof SimpleNodeSubject) {
        edge.setStartPoint(null);
      }
      if (edge.getTarget() instanceof SimpleNodeSubject) {
        edge.setEndPoint(null);
      }
      if (edge.getLabelBlock().getGeometry() == null) {
        final LabelGeometrySubject offset =
          new LabelGeometrySubject(LabelBlockProxyShape.DEFAULT_OFFSET);
        edge.getLabelBlock().setGeometry(offset);
      }
      if (edge.getGuardActionBlock() != null &&
          edge.getGuardActionBlock().getGeometry() == null) {
        // *** BUG ***
        // Not a very good position!
        // ***
        final LabelGeometrySubject offset =
          new LabelGeometrySubject
               (new Point(LabelBlockProxyShape.DEFAULT_OFFSET_X,
                          LabelBlockProxyShape.DEFAULT_OFFSET_Y + 10));
        edge.getGuardActionBlock().setGeometry(offset);
      }
      edge.setGeometry(null);
    }

    return runEmbedder;
  }


  //#########################################################################
  //# Stopping
  public static void stopAll()
  {
    synchronized(mSpringEmbedders) {
      for (final SpringEmbedder embedder : mSpringEmbedders) {
        embedder.stop();
      }
    }
  }

  public void stop()
  {
    mStop = true;
  }


  //#########################################################################
  //# Observer Pattern
  private void fireEvent(final EmbedderEvent event)
  {
    // Just in case they try to change the list in response to the call ...
    final Collection<EmbedderObserver> copy =
      new ArrayList<EmbedderObserver>(mObservers);
    for (final EmbedderObserver observer : copy) {
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
  //# Algorithm
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
    int maxfanout = 1;
    for (final NodeWrapper wrapper : mNodeWrappers) {
      final int fanout = wrapper.getFanout();
      if (fanout > maxfanout) {
        maxfanout = fanout;
      }
    }
    mWrapperSet = new WrapperSet();
    mBackgroundAttraction = BACKGROUND_ATTRACTION / numnodes;
    mInitialStateAttraction = INITIALSTATE_ATTRACTION;
    mNodeRepulsion = NODE_REPULSION / numnodes;
    mNodeEdgeRepulsion = NODEEDGE_REPULSION / numnodes;
    mEdgeRepulsion = EDGE_REPULSION;
    mMaxAttraction = 0.5 / maxfanout;
    mCenter = (Point2D) POINT_CENTER.clone();
    final double diameter = 2.0 * Config.GUI_EDITOR_NODE_RADIUS.get();
    mJumpThresholdSq = diameter * diameter;
    mTotalJumpsAvailable = numnodes;
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

  private double calculateDisplacements()
  {
    if (mPass == 1 && mInitialStateAttraction > 0.0) {
      mInitialStateAttraction -= INITIALSTATE_DECAY;
    }
    final Collection<? extends GeometryWrapper> wrappers =
      calculateNewPoints();
    double maxdelta = 0.0;
    double movex = 0.0;
    double movey = 0.0;
    if (mPass > 0 && !wrappers.isEmpty()) {
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

  private void updateModel()
  {
    for (final GeometryWrapper wrapper : mWrapperSet) {
      wrapper.updateModel();
    }
  }


  //##########################################################################
  //# Geometry Auxiliaries
  private Point2D repulsion(final Point2D p1,
                            final Point2D p2,
                            final double constant)
  {
    final double dx = p1.getX() - p2.getX();
    final double dy = p1.getY() - p2.getY();
    final double len = dx * dx + dy * dy;
    if (len > 0.0) {
      final int pass = mPass > 0 ? mPass : 1;
      double factor = pass * constant / len;
      double x = factor * dx;
      double y = factor * dy;
      final double sqrep = x * x + y * y;
      if (sqrep > MAX_REPULSION_SQ) {
        factor *= Math.sqrt(MAX_REPULSION_SQ / sqrep);
        x = factor * dx;
        y = factor * dy;
      }
      return new Point2D.Double(x, y);
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
    if (factor > mMaxAttraction) {
      factor = mMaxAttraction;
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


  private static Point2D addPoints(final Point2D pt1, final Point2D pt2)
  {
    if (pt1 == null) {
      return pt2;
    } else if (pt2 == null) {
      return pt1;
    } else {
      final double x = pt1.getX() + pt2.getX();
      final double y = pt1.getY() + pt2.getY();
      pt1.setLocation(x, y);
      return pt1;
    }
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

    @SuppressWarnings("unused")
	Point2D getMove()
    {
      final double dx = mNewPoint.getX() - mOldPoint.getX();
      final double dy = mNewPoint.getY() - mOldPoint.getY();
      return new Point2D.Double(dx, dy);
    }

    //#######################################################################
    //# Layouting
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
      mJumps = null;
    }

    //#######################################################################
    //# Simple Access
    int getFanout()
    {
      return mNeighbours.size();
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
        return;
      }
      if (other == this) {
        mSelfLoopFactor *= SELFLOOP_WEIGHT;
      } else {
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
        final double weight = mNeighbours.get(other);
        final Point2D delta1 =
          attraction(old, otherold, weight * NODE_ATTRACTION);
        final double dx = delta.getX() + delta1.getX();
        final double dy = delta.getY() + delta1.getY();
        delta.setLocation(dx, dy);
      }
      return delta;
    }

    boolean isJumping(final EdgeWrapper edge, final Point2D closest)
    {
      if (mPass > 0) {
        final Point2D old = getOldPoint();
        final double distsq = old.distanceSq(closest);
        if (distsq < mJumpThresholdSq) {
          if (mJumps == null) {
            final int numedges = mEdgeWrappers.length;
            mJumps = new HashMap<EdgeWrapper,EdgeJump>(numedges);
          }
          final EdgeJump jump = mJumps.get(edge);
          if (jump == null) {
            final EdgeJump newjump = new EdgeJump(distsq);
            mJumps.put(edge, newjump);
          } else {
            jump.update(distsq);
            return jump.isJumping();
          }
        }
      }
      return false;
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
    private Map<EdgeWrapper,EdgeJump> mJumps;
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
      } else if (mSource == mTarget) {
        final Point2D old = getOldPoint();
        return repulsion(old, node.getOldPoint(), mNodeEdgeRepulsion);
      } else {
        final Point2D point = node.getOldPoint();
        final Point2D start = mSource.getOldPoint();
        final Point2D end = mTarget.getOldPoint();
        final Point2D turn = getOldPoint();
        final Point2D control =
          GeometryTools.convertToControl(start, end, turn);
        final HornerPolynomial biquadratic =
          GeometryTools.getClosestPointBiquadratic(start, control, end, point);
        final Point2D closest;
        final Point2D alternative;
        if (biquadratic == null) {
          final Point2D candidate1 =
            GeometryTools.findClosestPointOnLine(start, turn, point);
          final Point2D candidate2 =
            GeometryTools.findClosestPointOnLine(end, turn, point);
          if (candidate1.equals(candidate2)) {
            closest = candidate1;
          } else if (candidate1.distanceSq(point) <
                     candidate2.distanceSq(point)) {
            closest = candidate1;
          } else {
            closest = candidate2;
          }
          alternative = null;
        } else {
          final double[] extremals =
            biquadratic.findBiquadraticPseudoMinimals(0.0, 1.0);
          if (extremals.length == 1) {
            closest = GeometryTools.getPointOnQuadratic
              (start, control, end, extremals[0]);
            alternative = null;
          } else if (biquadratic.getValue(extremals[0]) <
                     biquadratic.getValue(extremals[1])) {
            closest = GeometryTools.getPointOnQuadratic
              (start, control, end, extremals[0]);
            alternative = GeometryTools.getPointOnQuadratic
              (start, control, end, extremals[1]);
          } else {
            closest = GeometryTools.getPointOnQuadratic
              (start, control, end, extremals[1]);
            alternative = GeometryTools.getPointOnQuadratic
              (start, control, end, extremals[0]);
          }
        }
        if (node.isJumping(this, closest)) {
          return POINT_ZERO;
        }
        Point2D result = null;
        double weight = mNodeEdgeRepulsion;
        final double distsq = start.distanceSq(end);
        final double dist0sq = start.distanceSq(turn);
        final double dist1sq = end.distanceSq(turn);
        if (dist0sq > 0.0 && dist1sq > 0.0) {
          final double dist0 = Math.sqrt(dist0sq);
          final double dist1 = Math.sqrt(dist1sq);
          final double flatness =
            - (dist0sq + dist1sq - distsq) / (2.0 * dist0 * dist1);
          if (flatness > EDGE_FLATNESS_THRESHOLD) {
            final double fweight =
              (flatness - EDGE_FLATNESS_THRESHOLD) /
              (1.0 - EDGE_FLATNESS_THRESHOLD);
            final double constant = weight * fweight;
            final Point2D flat =
              GeometryTools.findClosestPointOnLine(start, end, point);
            result = repulsion(flat, point, constant);
            weight -= constant;
          }
        }
        if (alternative == null) {
          final Point2D repulsion0 = repulsion(closest, point, weight);
          return addPoints(result, repulsion0);
        } else {
          final double constant = 0.5 * weight;
          final Point2D repulsion0 = repulsion(closest, point, constant);
          result = addPoints(result, repulsion0);
          final Point2D repulsion1 = repulsion(alternative, point, constant);
          return addPoints(result, repulsion1);
        }
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
  //# Inner Class EdgeJump
  /**
   * A simple record to keep track of nodes having to cross edges.
   * Occasionally, a node ends up on the wrong side of an edge, and cannot
   * cross it due to excessive repulsion. This class is used to temporarily
   * switch off the repulsion between the node and edge concerned to allow
   * the node to cross. This is called a <I>jump</I>.  The total number of
   * jumps per spring embedder run is limited.
   */
  private class EdgeJump
  {

    //#######################################################################
    //# Constructor
    EdgeJump(final double distance)
    {
      mLastDistance = distance;
      mIsJumping = false;
      mJumpsAvailable = MAX_JUMPS_EACH;
    }

    //#######################################################################
    //# Simple Access
    boolean isJumping()
    {
      return mIsJumping;
    }

    //#######################################################################
    //# Jump Switching
    void update(final double distance)
    {
      if (mIsJumping) {
        if (distance > mLastDistance) {
          mIsJumping = false;
        }
      } else if (mJumpsAvailable > 0 && mTotalJumpsAvailable > 0) {
        if (distance < mLastDistance) {
          mJumpsAvailable--;
          mTotalJumpsAvailable--;
          mIsJumping = true;
        }
      }
      mLastDistance = distance;
    }

    //#######################################################################
    //# Data Members
    private double mLastDistance;
    private boolean mIsJumping;
    private int mJumpsAvailable;
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
  /**
   * Constant limiting attraction. To avoid divergent behaviour, attraction
   * must not cause objects to move past the objects they are attracted
   * to. Therefore, attraction is limited by a constant that depends on the
   * maximum fanout of nodes in the graph.
   */
  private double mMaxAttraction;
  private Point2D mCenter;
  private double mInitialStateAttraction;
  private int mNextWrapperId;
  private int mPass;
  private int mTotalJumpsAvailable;
  private double mJumpThresholdSq;

  private volatile boolean mStop = false;

  private static final List<SpringEmbedder> mSpringEmbedders =
    Collections.synchronizedList(new LinkedList<SpringEmbedder>());


  //###########################################################################
  //# Class Constants
  private static final double EPSILON = 0.0000001;
  private static final double MAX_REPULSION_SQ = 50000.0;
  private static final int MAX_JUMPS_EACH = 3;

  private static final int NUM_PASSES = 3;
  private static final double MULTI_EDGE_WEIGHT = 1.333;
  private static final double SELFLOOP_WEIGHT = 1.05;

  private static final double BACKGROUND_ATTRACTION = 0.05;
  private static final double INITIALSTATE_ATTRACTION =
    0.33 * BACKGROUND_ATTRACTION;
  private static final double INITIALSTATE_DECAY =
    0.01 * INITIALSTATE_ATTRACTION;
  private static final double EDGE_ATTRACTION = 0.05;
  private static final double NODE_ATTRACTION = 0.04;
  private static final double NODE_REPULSION = 1000.0;
  private static final double SELFLOOP_REPULSION = 100.0;
  private static final double EDGE_REPULSION = 25.0;
  private static final double NODEEDGE_REPULSION = 200.0;
  private static final double EDGE_FLATNESS_THRESHOLD = 0.985;

  private static final double GRAPH_MARGINAL_CONST = 48.0;
  private static final double CENTER_MOVE_CONST = 0.01;
  private static final double CONVERGENCE_CONST = 0.030;
  private static final int UPDATE_CONST = 10;

  private static final Point2D POINT_ZERO = new Point2D.Double(0.0, 0.0);
  private static final Point2D POINT_CENTER = new Point2D.Double(200.0, 200.0);

}
