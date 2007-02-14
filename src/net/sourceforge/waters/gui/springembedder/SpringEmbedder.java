//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui.springembedder
//# CLASS:   SpringEmbedder
//###########################################################################
//# $Id: SpringEmbedder.java,v 1.30 2007-02-14 22:43:37 siw4 Exp $
//###########################################################################


package net.sourceforge.waters.gui.springembedder;

import java.awt.geom.Point2D;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.List;
import java.util.LinkedList;
import java.util.Collections;
import javax.swing.SwingUtilities;

import net.sourceforge.waters.gui.renderer.GeometryAbsentException;
import net.sourceforge.waters.gui.renderer.GeometryTools;
import net.sourceforge.waters.subject.module.EdgeSubject;
import net.sourceforge.waters.subject.module.GraphSubject;
import net.sourceforge.waters.subject.module.NodeSubject;
import net.sourceforge.waters.subject.module.PointGeometrySubject;
import net.sourceforge.waters.subject.module.SimpleNodeSubject;
import net.sourceforge.waters.subject.module.SplineGeometrySubject;
import java.util.ArrayList;
import net.sourceforge.waters.subject.module.LabelGeometrySubject;
import java.awt.Point;
import net.sourceforge.waters.subject.module.GroupNodeSubject;
import net.sourceforge.waters.gui.renderer.LabelBlockProxyShape;


public class SpringEmbedder
  implements Runnable, EmbedderSubject
{

  //#########################################################################
  //# Constructors
  public SpringEmbedder(GraphSubject graph)
  {
    this(graph, 228424);
  }
  
  public SpringEmbedder(Collection<NodeSubject> nodes,
                        Collection<EdgeSubject> edges)
  {
    this(nodes, edges, 228424);
  }

  public SpringEmbedder(GraphSubject graph, final int seed)
  {
    this(graph.getNodesModifiable(), graph.getEdgesModifiable(), seed);
  }
  
  public SpringEmbedder(Collection<NodeSubject> nodes,
                        Collection<EdgeSubject> edges, int seed)
  {
    mRandom = new Random(seed);
    mNodes = nodes;
    mEdges = edges;
    mObservers = new LinkedList<EmbedderObserver>();
  }

  //#########################################################################
  //# Observer Pattern
  public void addObserver(EmbedderObserver observer)
  {
    mObservers.add(observer);
  }
  
  public void removeObserver(EmbedderObserver observer)
  {
    mObservers.remove(observer);
  }
  
  private void fireEvent(EmbedderEvent event)
  {
    // Just in case they try to change the list in response to the call ...
    final Collection<EmbedderObserver> copy =
      new ArrayList<EmbedderObserver>(mObservers);
    for (EmbedderObserver observer : copy) {
      observer.embedderChanged(event);
    }
  }

  
  //#########################################################################
  //# Interface java.lang.Runnable
  public void run()
  {
    springEmbedders.add(this);
    final int numnodes = mNodes.size();
    mNodeMap = new HashMap<SimpleNodeSubject,NodeWrapper>(numnodes);
    final int numedges = mEdges.size();
    mEdgeMap = new HashMap<EdgeSubject,EdgeWrapper>(numedges);
    for (final NodeSubject node : mNodes) {
      if (node instanceof SimpleNodeSubject) {
        final SimpleNodeSubject simple = (SimpleNodeSubject) node;
        final NodeWrapper wrapper = new NodeWrapper(simple);
        mNodeMap.put(simple, wrapper);
      } else {
        springEmbedders.remove(this);
        return;
        /*throw new GeometryAbsentException
          ("SpringEmbedder does not support nodes of type " +
           node.getClass().getName() + "!");*/
      }
    }
    for (final EdgeSubject edge : mEdges) {
      final EdgeWrapper wrapper = new EdgeWrapper(edge);
      mEdgeMap.put(edge, wrapper);
      for (final NodeWrapper node : mNodeMap.values()) {
        node.addNeighbours(wrapper);
      }
    }
    mWrapperSet = new WrapperSet();
    mBackgroundAttraction = BACKGROUND_ATTRACTION / numnodes;
    mInitialStateAttraction = INITIALSTATE_ATTRACTION;
    mNodeRepulsion = NODE_REPULSION / numnodes;
    mNodeEdgeRepulsion = NODEEDGE_REPULSION / numnodes;
    mEdgeRepulsion = EDGE_REPULSION / numedges;
    mCenter = (Point2D) POINT_CENTER.clone();
    
    fireEvent(new EmbedderEvent(EmbedderEventType.START));
    long stoptime = Long.MAX_VALUE;

    int count = 0;
    double limit = CONVERGENCE_CONST;
    for (int i = 1; i < NUM_PASSES; i++) {
      limit *= 4.0;
    }
    for (mPass = NUM_PASSES; mPass >= 0; mPass--) {
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
    SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          updateModel();
        }
      });
    springEmbedders.remove(this);
    mFinished = true;
    fireEvent(new EmbedderEvent(EmbedderEventType.STOP));
  }
  
  public int getProgress()
  {
    return NUM_PASSES - mPass;
  }

/*  public double getProgress()
  {
    return 1.0 - (mPass + 1.0) / (NUM_PASSES + 1.0);
  }*/
  
  public static boolean setUpGeometry(GraphSubject graph)
    throws GeometryAbsentException
  {
    return setUpGeometry(graph, 228424);
  }

  public static boolean setUpGeometry(GraphSubject graph, int seed)
    throws GeometryAbsentException
  {
    boolean runEmbedder = false;
    Random rand = new Random(seed);
    if (graph.getBlockedEvents() != null) {
      if (graph.getBlockedEvents().getGeometry() == null) {
        // TODO: Calculate better position
        graph.getBlockedEvents().setGeometry(new LabelGeometrySubject(new Point(5, 5)));
      }
    }
		for (NodeSubject node : graph.getNodesModifiable())
		{
			if (node instanceof SimpleNodeSubject)
			{
				SimpleNodeSubject n = (SimpleNodeSubject) node;
				if (n.isInitial())
				{
					if (n.getInitialArrowGeometry() == null)
					{
						n.setInitialArrowGeometry
						(new PointGeometrySubject(new Point(-5, -5)));
					}
				}
				
				if (n.getPointGeometry() == null)
				{
					runEmbedder = true;
					final int base;
					final int spread;
					if (n.isInitial())
					{
						base = 10;
						spread = 50;
					}
					else
					{
						base = 100;
						spread = 500;
					}
					n.setPointGeometry(new PointGeometrySubject
                             (new Point(base + rand.nextInt(spread),
                                        base + rand.nextInt(spread))));
				}
				if (n.getLabelGeometry() == null)
				{
					n.setLabelGeometry(new LabelGeometrySubject(new Point(5, 5)));
				}
			}
			else if (node instanceof GroupNodeSubject)
			{
				if (((GroupNodeSubject)node).getGeometry() == null)
				{
					throw new GeometryAbsentException("There is no geometry information"
							+ " for a group node in this graph");
				}
			}
		}
		for (EdgeSubject edge : graph.getEdgesModifiable())
		{
      if (!(edge.getSource() instanceof GroupNodeSubject)) {
        edge.setStartPoint(null);
      }
      if (!(edge.getTarget() instanceof GroupNodeSubject)) {
        edge.setEndPoint(null);
      }
			if (edge.getLabelBlock().getGeometry() == null)
			{
				LabelGeometrySubject offset =
					new LabelGeometrySubject
					(new Point(LabelBlockProxyShape.DEFAULTOFFSETX,
							LabelBlockProxyShape.DEFAULTOFFSETY));
				edge.getLabelBlock().setGeometry(offset);
			}
		}
		return runEmbedder;
  }

  //#########################################################################
  //# Auxiliary Methods
  private synchronized double calculateDisplacements()
  {
    final Collection<? extends GeometryWrapper> wrappers =
      mPass == 0 ? mEdgeMap.values() : mWrapperSet;
    if (mPass == 1) {
      mInitialStateAttraction *= INITIALSTATE_DECAY;
    }
    for (final GeometryWrapper wrapper : wrappers) {
      wrapper.calculateDisplacement();
    }
    double maxdelta = 0.0;
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
      final double movex = factor * dx;
      final double movey = factor * dy;
      final double newx = mCenter.getX() + movex;
      final double newy = mCenter.getY() + movey;
      mCenter.setLocation(newx, newy);
      if (mPass > 1) {
        maxdelta = Math.sqrt(dx * dx + dy * dy);
      }
    }
    for (final GeometryWrapper wrapper : wrappers) {
      final double delta = wrapper.getDelta();
      if (delta > maxdelta) {
        maxdelta = delta;
      }
    }
    for (final GeometryWrapper wrapper : wrappers) {
      wrapper.updatePoint();
    }
    return maxdelta;
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
    synchronized(springEmbedders) {
      for (SpringEmbedder embedder : springEmbedders) {
        embedder.stop();
      }
    }
  }

  public void stop()
  {
    mStop = true;
  }
  
  public boolean isFinished()
  {
    return mFinished;
  }


  //#########################################################################
  //# Inner Class GeometryWrapper

  private abstract class GeometryWrapper
  {
    //#######################################################################
    //# Constructor
    private GeometryWrapper(final Point2D old)
    {
      mOldPoint = old;
    }

    //#######################################################################
    //# Simple Access
    Point2D getOldPoint()
    {
      return mOldPoint;
    }

    Point2D getNewPoint()
    {
      return mNewPoint;
    }

    void setNewPoint(final double x, final double y)
    {
      if (mNewPoint == null || mNewPoint == mOldPoint) {
        mNewPoint = new Point2D.Double(x, y);
      } else {
        mNewPoint.setLocation(x, y);
      }
    }

    void move(final double dx, final double dy)
    {
      final double newx = mNewPoint.getX();
      final double newy = mNewPoint.getY();
      setNewPoint(newx, newy);
    }

    //#######################################################################
    //# Auxiliary Methods
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

    //#######################################################################
    //# Layouting
    abstract void calculateDisplacement();
    abstract double getDelta();
    abstract void updateModel();

    //#######################################################################
    //# Data Members
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
      for (final  NodeWrapper other : mNeighbours.keySet()) {
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
      super(GeometryTools.getHandlePoint1(edge));
      mEdge = edge;
      final NodeSubject source = edge.getSource();
      mSource = mNodeMap.get(source);
      final NodeSubject target = edge.getTarget();
      mTarget = mNodeMap.get(target);
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
          final Point2D delta = repulsion(old,
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
      if (mSource == mTarget) {
        final Point2D delta = repulsion(old,
                                        mSource.getOldPoint(),
                                        SELFLOOP_REPULSION);
        dx += delta.getX();
        dy += delta.getY();
      } else {
        final Point2D delta = edgeAttraction(old,
                                             mSource.getOldPoint(),
                                             mTarget.getOldPoint(),
                                             EDGE_ATTRACTION);
        dx += delta.getX();
        dy += delta.getY();
      }
      final double x = old.getX() + dx;
      final double y = old.getY() + dy;
      setNewPoint(x, y);
    }

    double getDelta()
    {
      if (mPass > 0) {
        return getDeltaChange(mSource) + getDeltaChange(mTarget);
      } else {
        final Point2D oldpoint = getOldPoint();
        final Point2D newpoint = getNewPoint();
        return oldpoint.distance(newpoint);
      }
    }

    void updateModel()
    {
      final Point2D point = getNewPoint();
      GeometryTools.createMidGeometry(mEdge, point);
    }


    //#######################################################################
    //# Data Members
    private final EdgeSubject mEdge;
    private final NodeWrapper mSource;
    private final NodeWrapper mTarget;
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
  private final Collection<EmbedderObserver> mObservers;
  private Map<SimpleNodeSubject,NodeWrapper> mNodeMap;
  private Map<EdgeSubject,EdgeWrapper> mEdgeMap;
  private Set<GeometryWrapper> mWrapperSet;
  private double mBackgroundAttraction;
  private double mNodeRepulsion;
  private double mNodeEdgeRepulsion;
  private double mEdgeRepulsion;
  private Point2D mCenter;

  private double mInitialStateAttraction;
  private int mPass;

  private volatile boolean mStop = false;
  private boolean mFinished = false;


  //###########################################################################
  //# Class Constants
  static final int NUM_PASSES = 3;
  private static final double EPSILON = 0.00002;
  private static final double MULTI_EDGE_WEIGHT = 1.333;
  private static final double SELFLOOP_WEIGHT = 1.05;

  private static final double BACKGROUND_ATTRACTION = 0.05;
  private static final double INITIALSTATE_ATTRACTION =
    0.22 * BACKGROUND_ATTRACTION;
  private static final double INITIALSTATE_DECAY = 0.9;
  private static final double EDGE_ATTRACTION = 0.05;
  private static final double NODE_ATTRACTION = 0.1;
  private static final double NODE_REPULSION = 1600.0;
  private static final double SELFLOOP_REPULSION = 100.0;
  private static final double EDGE_REPULSION = 80.0;
  private static final double NODEEDGE_REPULSION = 200.0;

  private static final double GRAPH_MARGINAL_CONST = 48.0;
  private static final double CENTER_MOVE_CONST = 0.01;
  private static final double CONVERGENCE_CONST = 0.033;
  private static final int UPDATE_CONST = 5;

  private static final Point2D POINT_ZERO = new Point2D.Double(0.0, 0.0);
  private static final Point2D POINT_CENTER = new Point2D.Double(200.0, 200.0);

  private static final List<SpringEmbedder> springEmbedders =
    Collections.synchronizedList(new LinkedList<SpringEmbedder>());

}
