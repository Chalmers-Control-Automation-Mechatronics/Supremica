//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui.springembedder
//# CLASS:   SpringEmbedder
//###########################################################################
//# $Id: SpringEmbedder.java,v 1.9 2006-10-03 14:57:07 knut Exp $
//###########################################################################


package net.sourceforge.waters.gui.springembedder;

import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import javax.swing.SwingUtilities;

import net.sourceforge.waters.subject.module.EdgeSubject;
import net.sourceforge.waters.subject.module.GraphSubject;
import net.sourceforge.waters.subject.module.NodeSubject;
import net.sourceforge.waters.subject.module.PointGeometrySubject;
import net.sourceforge.waters.subject.module.SimpleNodeSubject;
import net.sourceforge.waters.subject.module.SplineGeometrySubject;


public class SpringEmbedder
  implements Runnable
{

  //#########################################################################
  //# Constructors
  public SpringEmbedder(GraphSubject graph)
  {
    this(graph, 228424);
  }

  private SpringEmbedder(GraphSubject graph, final int seed)
  {
    mRandom = new Random(seed);
    mGraph = graph;
    final Collection<NodeSubject> nodes = graph.getNodesModifiable();
    final int numnodes = nodes.size();
    mNodeMap = new HashMap<SimpleNodeSubject,NodeWrapper>(numnodes);
    final Collection<EdgeSubject> edges = graph.getEdgesModifiable();
    final int numedges = edges.size();
    mEdgeMap = new HashMap<EdgeSubject,EdgeWrapper>(edges.size());
    for (final NodeSubject node : nodes) {
      if (node instanceof SimpleNodeSubject) {
        final SimpleNodeSubject simple = (SimpleNodeSubject) node;
        final NodeWrapper wrapper = new NodeWrapper(simple);
        mNodeMap.put(simple, wrapper);
      } else {
        throw new IllegalArgumentException
          ("SpringEmbedder does not support nodes of type " +
           node.getClass().getName() + "!");
      }
    }
    for (final EdgeSubject edge : edges) {
      final EdgeWrapper wrapper = new EdgeWrapper(edge);
      mEdgeMap.put(edge, wrapper);
    }
    mBackgroundAttraction = BACKGROUND_ATTRACTION / numnodes;
    mNodeRepulsion = NODE_REPULSION / numnodes;
    mNodeEdgeRepulsion = NODEEDGE_REPULSION / numnodes;
    mEdgeRepulsion = EDGE_REPULSION / numedges;
  }


  //#########################################################################
  //# Interface java.lang.Runnable
  public void run()
  {
    int count = 0;
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
    } while (maxdelta > CONVERGENCE_CONST);
    SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          updateModel();
        }
      });
  }


  //#########################################################################
  //# Auxiliary Methods
  private synchronized double calculateDisplacements()
  {
    double maxdelta = 0.0;
    for (final NodeWrapper wrapper : mNodeMap.values()) {
      final double delta = wrapper.calculateDisplacement();
      if (delta > maxdelta) {
        maxdelta = delta;
      }
    }
    for (final EdgeWrapper wrapper : mEdgeMap.values()) {
      final double delta = wrapper.calculateDisplacement();
      if (delta > maxdelta) {
        maxdelta = delta;
      }
    }
    for (final NodeWrapper wrapper : mNodeMap.values()) {
      wrapper.updatePoint();
    }
    for (final EdgeWrapper wrapper : mEdgeMap.values()) {
      wrapper.updatePoint();
    }
    return maxdelta;
  }

  private synchronized void updateGraphLocation()
  {
	double minX = Double.MAX_VALUE;
	double minY = Double.MAX_VALUE;

    for (final NodeWrapper wrapper : mNodeMap.values())
    {
		Point2D currPoint = wrapper.getNewPoint();
		double currX = currPoint.getX();
		double currY = currPoint.getY();
		if (currX < minX)
		{
			minX = currX;
		}
		if (currY < minY)
		{
			minY = currY;
		}
    }

    for (final EdgeWrapper wrapper : mEdgeMap.values())
    {
		Point2D currPoint = wrapper.getNewPoint();
		double currX = currPoint.getX();
		double currY = currPoint.getY();
		if (currX < minX)
		{
			minX = currX;
		}
		if (currY < minY)
		{
			minY = currY;
		}
    }

    double moveX = -(minX - GRAPH_MARGINAL_CONST);
    double moveY = -(minY - GRAPH_MARGINAL_CONST);

	POINT_CENTER.setLocation(POINT_CENTER.getX() + moveX, POINT_CENTER.getY() + moveY);
	for (final NodeWrapper wrapper : mNodeMap.values())
	{
		wrapper.moveNewPoint(moveX, moveY);
	}

	for (final EdgeWrapper wrapper : mEdgeMap.values())
	{
		wrapper.moveNewPoint(moveX, moveY);
	}
  }

  private synchronized void updateModel()
  {
    updateGraphLocation();

    for (final NodeWrapper wrapper : mNodeMap.values()) {
      wrapper.updateModel();
    }
    for (final EdgeWrapper wrapper : mEdgeMap.values()) {
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
    if (len != 0) {
      return new Point2D.Double((dx / len) * constant,
                                (dy / len) * constant);
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
    return new Point2D.Double(-dx * constant, -dy * constant);
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

  private int i = 0;

  //#########################################################################
  //# Inner Class NodeWrapper
  private class NodeWrapper
  {

    //#######################################################################
    //# Constructor
    private NodeWrapper(final SimpleNodeSubject node)
    {
      mGeometry = node.getPointGeometry();
      isInitial = node.isInitial();
      mOldPoint = mGeometry.getPoint();
    }

    //#######################################################################
    //# Simple Access
    private Point2D getNewPoint()
    {
      return mNewPoint;
    }

    private void setNewPoint(final Point2D point)
    {
      mNewPoint = point;
    }

    private void moveNewPoint(double dx, double dy)
    {
		mNewPoint.setLocation(mNewPoint.getX() + dx, mNewPoint.getY() + dy);
	}

    //#######################################################################
    //# Auxiliary Methods
    private void updatePoint()
    {
      mOldPoint = mNewPoint;
    }

    private void updateModel()
    {
      mGeometry.setPoint(mNewPoint);
    }

    private double calculateDisplacement()
    {
      final Point2D delta2 = attraction(mOldPoint,
                                        POINT_CENTER,
                                        mBackgroundAttraction);
      double dx = delta2.getX();
      double dy = delta2.getY();
      for (final NodeWrapper other : mNodeMap.values()) {
        if (other != this) {
          final Point2D delta1 = repulsion(mOldPoint,
                                           other.mOldPoint,
                                           mNodeRepulsion);
          dx += delta1.getX();
          dy += delta1.getY();
         }
      }
      for (final EdgeWrapper edge : mEdgeMap.values()) {
        final NodeWrapper other;
        if (edge.getSource() == this) {
          other = edge.getTarget();
        } else if (edge.getTarget() == this) {
          other = edge.getSource();
        } else {
          other = null;
        }
        if (other != null && other != this) {
          final Point2D delta = attraction(mOldPoint,
                                           other.mOldPoint,
                                           NODE_ATTRACTION);
          dx += delta.getX();
          dy += delta.getY();
        }
      }
      // Pull initial state towards the upper left corner...
      // Makes it easier to find and gives more consistently drawn graphs with
      // a natural flow from upper left towards lower right
      //   This also means that the best position for node labels will be above
      // and to the right of the node... not below and to the left!
      //   It would be nice if in the initial random layout, the initial state
      // was put close to the top left corner to reduce the slow rotation of
      // symmetric figures...
      if (isInitial)
      {
          final Point2D delta = attraction(mOldPoint,
                                           POINT_INITIAL_ZERO,
                                           INITIALSTATE_ATTRACTION);
          dx += delta.getX();
          dy += delta.getY();
      }
      final double x = mOldPoint.getX() + dx;
      final double y = mOldPoint.getY() + dy;
      mNewPoint = new Point2D.Double(x, y);
      return mOldPoint.distance(mNewPoint);
    }


    //#######################################################################
    //# Data Members
    private final PointGeometrySubject mGeometry;
    private Point2D mOldPoint;
    private Point2D mNewPoint;
    private boolean isInitial;
  }


  //#########################################################################
  //# Inner Class EdgeWrapper
  private class EdgeWrapper
  {

    //#######################################################################
    //# Constructor
    private EdgeWrapper(final EdgeSubject edge)
    {
      mGeometry = edge.getGeometry();
      mStartPoint = edge.getStartPoint();
      mEndPoint = edge.getEndPoint();
      mOldPoint = mGeometry.getPoints().get(0);
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

    private void setNewPoint(final Point2D point)
    {
      mNewPoint = point;
    }

    private Point2D getNewPoint()
    {
      return mNewPoint;
    }

    private void moveNewPoint(double dx, double dy)
    {
		mNewPoint.setLocation(mNewPoint.getX() + dx, mNewPoint.getY() + dy);
	}

    //#######################################################################
    //# Auxiliary Methods
    private void updatePoint()
    {
      mOldPoint = mNewPoint;
    }

    private void updateModel()
    {
      mGeometry.getPointsModifiable().set(0, mNewPoint);
      mStartPoint.setPoint(mSource.getNewPoint());
      mEndPoint.setPoint(mTarget.getNewPoint());
    }

    private double calculateDisplacement()
    {
      double dx = 0.0;
      double dy = 0.0;
      for (final EdgeWrapper other : mEdgeMap.values()) {
        if (other != this) {
          final Point2D delta = repulsion(mOldPoint,
                                          other.mOldPoint,
                                          mEdgeRepulsion);
          dx += delta.getX();
          dy += delta.getY();
        }
      }
      for (final NodeWrapper node : mNodeMap.values()) {
        if (node != mSource && node != mTarget) {
          final Point2D delta = repulsion(mOldPoint,
                                          node.mOldPoint,
                                          mNodeEdgeRepulsion);
          dx += delta.getX();
          dy += delta.getY();
        } else {
          final Point2D delta = attraction(mOldPoint,
                                           node.mOldPoint,
                                           EDGE_ATTRACTION);
          dx += delta.getX();
          dy += delta.getY();
        }
      }
      if (mSource == mTarget) {
        final Point2D delta = repulsion(mOldPoint,
                                        mSource.mOldPoint,
                                        SELFLOOP_REPULSION);
        dx += delta.getX();
        dy += delta.getY();
      } else {
        final Point2D delta = edgeAttraction(mOldPoint,
                                             mSource.mOldPoint,
                                             mTarget.mOldPoint,
                                             EDGE_ATTRACTION);
        dx += delta.getX();
        dy += delta.getY();
      }
      final double x = mOldPoint.getX() + dx;
      final double y = mOldPoint.getY() + dy;
      mNewPoint = new Point2D.Double(x, y);
      return mOldPoint.distance(mNewPoint);
    }


    //#######################################################################
    //# Data Members
    private final SplineGeometrySubject mGeometry;
    private final PointGeometrySubject mStartPoint;
    private final PointGeometrySubject mEndPoint;
    private final NodeWrapper mSource;
    private final NodeWrapper mTarget;
    private Point2D mOldPoint;
    private Point2D mNewPoint;
  }


  //###########################################################################
  //# Data Members
  private final Random mRandom;
  private final GraphSubject mGraph;
  private final Map<SimpleNodeSubject,NodeWrapper> mNodeMap;
  private final Map<EdgeSubject,EdgeWrapper> mEdgeMap;
  private final double mBackgroundAttraction;
  private final double mNodeRepulsion;
  private final double mNodeEdgeRepulsion;
  private final double mEdgeRepulsion;


  //###########################################################################
  //# Class Constants
  private static final double BACKGROUND_ATTRACTION = 0.05;
  private static final double EDGE_ATTRACTION = 0.05;
  private static final double NODE_ATTRACTION = 0.05;
  private static final double INITIALSTATE_ATTRACTION = BACKGROUND_ATTRACTION/10;
  private static final double NODE_REPULSION = 1600.0;
  private static final double SELFLOOP_REPULSION = 100.0;
  private static final double EDGE_REPULSION = 80.0;
  private static final double NODEEDGE_REPULSION = 150.0;

  private static final double CONVERGENCE_CONST = 0.025;

  private static final double GRAPH_MARGINAL_CONST = 50.0;

  private static final int UPDATE_CONST = 5;

  private static final Point2D POINT_ZERO = new Point2D.Double(0.0, 0.0);
  private static final Point2D POINT_INITIAL_ZERO = new Point2D.Double(0.0, 0.0);
  private static final Point2D POINT_CENTER = new Point2D.Double(200.0, 200.0);
}
