//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui.renderer
//# CLASS:   GeometryTools
//###########################################################################
//# $Id: GeometryTools.java,v 1.14 2007-02-25 09:42:49 robi Exp $
//###########################################################################

package net.sourceforge.waters.gui.renderer;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.Rectangle2D;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.module.AbstractModuleProxyVisitor;
import net.sourceforge.waters.model.module.BoxGeometryProxy;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.GroupNodeProxy;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.PointGeometryProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.model.module.SplineGeometryProxy;
import net.sourceforge.waters.subject.base.ProxySubject;
import net.sourceforge.waters.subject.module.BoxGeometrySubject;
import net.sourceforge.waters.subject.module.EdgeSubject;
import net.sourceforge.waters.subject.module.GroupNodeSubject;
import net.sourceforge.waters.subject.module.NodeSubject;
import net.sourceforge.waters.subject.module.PointGeometrySubject;
import net.sourceforge.waters.subject.module.SimpleNodeSubject;
import net.sourceforge.waters.subject.module.SplineGeometrySubject;

import net.sourceforge.waters.xsd.module.SplineKind;


public final class GeometryTools
{

  //#########################################################################
  //# Universal Tools (Using Visitors)
  public static void translate(final ProxySubject item, final Point2D delta)
  {
    if (mTranslator == null) {
      mTranslator = new TranslateVisitor();
    }
    mTranslator.translate(item, delta);
  }
  

  //#########################################################################
  //# Nodes
  public static Point2D getPosition(final NodeProxy node)
  {
    if (mNodePositionVisitor == null) {
      mNodePositionVisitor = new NodePositionVisitor();
    }
    return mNodePositionVisitor.getPosition(node);
  }

  public static Point2D getTopLeftPosition(final NodeProxy node)
  {
    if (mTopLeftPositionVisitor == null) {
      mTopLeftPositionVisitor = new TopLeftPositionVisitor();
    }
    return mTopLeftPositionVisitor.getTopLeftPosition(node);
  }

  public static Point2D getDefaultPosition(final NodeProxy node,
                                           final Point2D turningpoint)
  {
    if (mDefaultPositionVisitor == null) {
      mDefaultPositionVisitor = new DefaultPositionVisitor();
    }
    return mDefaultPositionVisitor.getDefaultPosition(node, turningpoint);
  }


  //#########################################################################
  //# Edges
  /**
   * Checks whether an edge is a selfloop.
   * An edge is rendered as a selfloop if its source and target nodes
   * are the same, and in case of a group node, the source and target
   * geometry must also be the same.
   */
  public static boolean isSelfloop(final EdgeProxy edge)
  {
    final NodeProxy source = edge.getSource();
    final NodeProxy target = edge.getTarget();
    if (source != target) {
      return false;
    } else if (source instanceof SimpleNodeProxy) {
      return true;
    } else {
      final Point2D start = getStartPoint(edge);
      final Point2D end = getEndPoint(edge);
      return start.distanceSq(end) < EPSILON2;
    }
  }

  /**
   * Gets the start point of an edge.
   * This method retrieves the edge's start point position from its
   * geometry if present, or otherwise calculates a default position
   * from the edge's source node.
   */
  public static Point2D getStartPoint(final EdgeProxy edge)
  {
    final PointGeometryProxy pointGeo = edge.getStartPoint();
    final NodeProxy source = edge.getSource();
    if (source == null) {
      return pointGeo.getPoint();
    } else if (source instanceof SimpleNodeProxy) {
      return getPosition(source);
    } else if (pointGeo == null) {
      final SplineGeometryProxy edgeGeo = edge.getGeometry();
      final NodeProxy target = edge.getTarget();
      final Point2D aux;
      if (edgeGeo != null && !edgeGeo.getPoints().isEmpty()) {
        aux = edgeGeo.getPoints().get(0);
      } else if (target == null ||
                 target instanceof GroupNodeProxy &&
                 edge.getEndPoint() != null) {
        aux = edge.getEndPoint().getPoint();
      } else {
        aux = getPosition(target);
      }
      return getDefaultPosition(source, aux);
    } else {
      return pointGeo.getPoint();
    }
  }

  /**
   * Gets the end point of an edge.
   * This method retrieves the edge's end point position from its
   * geometry if present, or otherwise calculates a default position
   * from the edge's target node.
   */
  public static Point2D getEndPoint(final EdgeProxy edge)
  {
    final PointGeometryProxy pointGeo = edge.getEndPoint();
    final NodeProxy target = edge.getTarget();
    if (target == null) {
      return pointGeo.getPoint();
    } else if (target instanceof SimpleNodeProxy) {
      return getPosition(target);
    } else if (pointGeo == null) {
      final SplineGeometryProxy edgeGeo = edge.getGeometry();
      final NodeProxy source = edge.getSource();
      final Point2D aux;
      if (edgeGeo != null && !edgeGeo.getPoints().isEmpty()) {
        aux = edgeGeo.getPoints().get(0);
      } else if (source == null ||
                 source instanceof GroupNodeProxy &&
                 edge.getStartPoint() != null) {
        aux = edge.getStartPoint().getPoint();
      } else {
        aux = getPosition(source);
      }
      return getDefaultPosition(target, aux);
    } else {
      return pointGeo.getPoint();
    }
  }

  /**
   * Gets the position of the turning point of an edge.
   * The turning point is the single control point of the
   * edge's spline geometry, or the centre of the straight line
   * between the edge's start and end point.
   * @throws IllegalArgumentException if the spline geometry has
   *         more than one control point.
   */
  public static Point2D getTurningPoint1(final EdgeProxy edge)
  {
    final SplineGeometryProxy geo = edge.getGeometry();
    if (geo == null) {
      return getDefaultMidPoint(edge);
    } else {
      final Point2D point = getSinglePoint(geo);
      switch (geo.getKind()) {
      case INTERPOLATING:
        return point;
      case BEZIER:
        return convertToTurn(edge, point);
      default:
        throw new IllegalArgumentException
          ("Unknown spline kind: " + geo.getKind());
      }
    }
  }

  /**
   * Gets the position of the control point of an edge.
   * This method tries to find a bezier control point for the
   * given edge.
   * @throws IllegalArgumentException if the spline geometry has
   *         more than one control point.
   */
  public static Point2D getControlPoint1(final EdgeProxy edge)
  {
    final SplineGeometryProxy geo = edge.getGeometry();
    if (geo == null) {
      return getDefaultMidPoint(edge);
    } else {
      final Point2D point = getSinglePoint(geo);
      switch (geo.getKind()) {
      case INTERPOLATING:
        return convertToControl(edge, point);
      case BEZIER:
        return point;
      default:
        throw new IllegalArgumentException
          ("Unknown spline kind: " + geo.getKind());
      }
    }
  }

  /**
   * Gets the single control point of an edge.
   * This method checks the edge's spline geometry and returns its only
   * control point.
   * @throws IllegalArgumentException if the spline geometry does
   *         not have exactly one control point.
   */
  public static Point2D getSinglePoint(final EdgeProxy edge)
  {
    return getSinglePoint(edge.getGeometry());
  }

  /**
   * Gets the single control point of a spline.
   * This method checks the given spline geometry and returns its only
   * control point.
   * @throws IllegalArgumentException if the spline geometry does
   *         not have exactly one control point.
   */
  public static Point2D getSinglePoint(final SplineGeometryProxy geo)
  {
    final List<Point2D> points = geo.getPoints();
    if (points.size() == 1) {
      return points.get(0);
    } else {
      throw new IllegalArgumentException
        ("Unsupported number of control points in spline!");
    }
  }

  /**
   * Gets the centre point of an edge.
   * This method computes the centre between the start and end points
   * of the given edge. It does <I>not</I> consider the edge's spline
   * geometry.
   */
  public static Point2D getDefaultMidPoint(final EdgeProxy edge)
  {
    final Point2D start = getStartPoint(edge);
    if (isSelfloop(edge)) {
      final double x = start.getX() + TieEdgeProxyShape.DEFAULT_OFFSET_X; 
      final double y = start.getY() + TieEdgeProxyShape.DEFAULT_OFFSET_Y;
      return new Point2D.Double(x, y); 
    } else {
      final Point2D end = getEndPoint(edge);
      return getMidPoint(start, end);
    }
  }

  /**
   * Calculates the middle between two given points.
   */
  public static Point2D getMidPoint(final Point2D p1, final Point2D p2)
  {
    final double x = 0.5 * (p1.getX() + p2.getX());
    final double y = 0.5 * (p1.getY() + p2.getY());
    return new Point2D.Double(x, y);
  }

  /**
   * Calculates the position of a quadratic spline edge's turning point
   * from its control point.
   */
  public static Point2D convertToTurn(final EdgeProxy edge,
                                      final Point2D control)
  {
    final Point2D start = getStartPoint(edge);
    final Point2D end = getEndPoint(edge);
    final double x = 0.25 * (start.getX() + 2.0 * control.getX() + end.getX());
    final double y = 0.25 * (start.getY() + 2.0 * control.getY() + end.getY());
    return new Point2D.Double(x, y);
  }

  /**
   * Calculates the position of a quadratic spline edge's control point
   * from its turning point.
   */
  public static Point2D convertToControl(final EdgeProxy edge,
                                         final Point2D mid)
  {
    final Point2D start = getStartPoint(edge);
    final Point2D end = getEndPoint(edge);
    final double x = 2.0 * mid.getX() - 0.5 * (start.getX() + end.getX());
    final double y = 2.0 * mid.getY() - 0.5 * (start.getY() + end.getY());
    return new Point2D.Double(x, y);
  }

  /**
   * Changes the geometry of an edge.
   * This method changes the edge's spline geometry to reflect the
   * given new centre point. It produces either a quadratic spline
   * with a single control point, or a straight line if the given
   * point is close enough to the straight line between the edge's
   * source and target. For selfloop edges, the given new point is
   * always adhered to.
   */
  public static void createMidGeometry(final EdgeSubject edge,
                                       final Point2D newpoint,
                                       final SplineKind kind)
  {
    if (isSelfloop(edge)) {
      setSpecifiedMidPoint(edge, newpoint, kind);
      return;
    }
    final Point2D start = getStartPoint(edge);
    final Point2D end = getEndPoint(edge);
    if (start.distanceSq(end) < EPSILON2) {
      setSpecifiedMidPoint(edge, newpoint, kind);
      return;
    }
    final Line2D line = new Line2D.Double(start, end);
    if (line.ptLineDist(newpoint) >= 1.5) {
      setSpecifiedMidPoint(edge, newpoint, kind);
    } else {
      edge.setGeometry(null);
    }
  }

  /**
   * Creates default geometry for an edge.
   * This method simply clears the geometry of the given edge.
   */
  public static void createDefaultGeometry(final EdgeSubject edge)
  {
    edge.setGeometry(null);
  }

  /**
   * Changes the middle point of an edge.
   * This method changes the edge's spline geometry to reflect the
   * given new centre point. It produces spline with a single control
   * point, or replaces the existing one.
   */
  public static void setSpecifiedMidPoint(final EdgeSubject edge,
                                          final Point2D newpoint,
                                          final SplineKind kind)
  {
    final SplineGeometrySubject oldgeo = edge.getGeometry();
    if (oldgeo == null || oldgeo.getPoints().size() != 1) {
      final List<Point2D> points = Collections.singletonList(newpoint);
      final SplineGeometrySubject newgeo =
        new SplineGeometrySubject(points, kind);
      edge.setGeometry(newgeo);
    } else {
      final List<Point2D> points = oldgeo.getPointsModifiable();
      final Point2D oldpoint = points.iterator().next();
      if (oldpoint.distanceSq(newpoint) >= EPSILON2) {
        points.set(0, newpoint);
      }
      oldgeo.setKind(kind);
    }
  }

  public static Point2D getRadialStartPoint(final EdgeProxy edge,
                                            final Point2D dir)
  {
    final NodeProxy source = edge.getSource();
    final PointGeometryProxy start = edge.getStartPoint();
    return getRadialPoint(source, start, dir);
  }

  public static Point2D getRadialEndPoint(final EdgeProxy edge,
                                          final Point2D dir)
  {
    final NodeProxy target = edge.getTarget();
    final PointGeometryProxy end = edge.getEndPoint();
    return getRadialPoint(target, end, dir);
  }

  public static Point2D getRadialPoint(final NodeProxy node,
                                       final PointGeometryProxy geo,
                                       final Point2D dir)
  {
    if (node == null) {
      return geo.getPoint();
    } else if (node instanceof SimpleNodeProxy) {
      final SimpleNodeProxy simple = (SimpleNodeProxy) node;
      final Point2D pos = getPosition(simple);
      final Point2D normdir = getNormalizedDirection(pos, dir);
      return getRadialPoint(simple, normdir);
    } else if (node instanceof GroupNodeProxy) {
      if (geo != null) {
        return geo.getPoint();
      } else {
        final GroupNodeProxy group = (GroupNodeProxy) node;
        return getDefaultPosition(group, dir);
      }
    } else {
      throw new ClassCastException
        ("Unknown node type: " + node.getClass().getName() + "!");
    }
  }

  public static Point2D getRadialPoint(final SimpleNodeProxy node,
                                       final Point2D normdir)
  {
    final Point2D center = getPosition(node);
    final double dx = normdir.getX();
    final double dy = normdir.getY();
    final double x = center.getX() + SimpleNodeProxyShape.RADIUS * dx;
    final double y = center.getY() + SimpleNodeProxyShape.RADIUS * dy;
    return new Point2D.Double(x, y);
  }


  //#########################################################################
  //# General Geometric Auxiliaries
  /**
   * Calculates a normalized direction vector between to given points.
   */
  public static Point2D getNormalizedDirection(final Point2D start,
                                               final Point2D end)
  {
    final double dx = end.getX() - start.getX();
    final double dy = end.getY() - start.getY();
    final Point2D dir = new Point2D.Double(dx, dy);
    normalize(dir);
    return dir;
  }

  /**
   * Normalizes a given vector. This method replaces the x and y coordinates
   * of the given point so that the length of the result is equal to 1.0,
   * but the direction is unchanged. If the length of the given vector
   * is not at least {@link #EPSILON}, an arbitrary direction is assigned.
   */
  public static void normalize(final Point2D dir)
  {
    final double dx = dir.getX();
    final double dy = dir.getY();
    final double len = Math.sqrt(dx * dx + dy * dy);
    if (len > GeometryTools.EPSILON) {
      final double norm = 1.0 / len;
      dir.setLocation(norm * dx, norm * dy);
    } else {
      dir.setLocation(1.0, 0.0);
    }
  }

  /**
   * Finds the intersection between a rectangle and a line.
   * @param  rect   The rectangle to be tested.
   * @param  point  The end point of the line to be tested.
   *                This method finds the line from the center of the
   *                rectangle and this point, and intersects it with the
   *                boundary of the rectangle. It returns the intersection
   *                point closest to the given point.
   */
  public static Point2D findIntersection(final Rectangle2D rect,
                                         final Point2D point)
  {
    // Rectangle coordinates : x0/y0 --- x1/y1
    final double x0 = rect.getX();
    final double x1 = x0 + rect.getWidth();
    final double y0 = rect.getY();
    final double y1 = y0 + rect.getHeight();
    // Line through center of rectangle and point : xc/yc --- xp/yp
    final double xc = 0.5 * (x0 + x1);
    final double yc = 0.5 * (y0 + y1);
    final double xp = point.getX();
    final double yp = point.getY();
    // Looking for intersection point closest to point,
    // which is on the rectangle ...
    double best = Double.MAX_VALUE;
    Point2D found  = new Point2D.Double();
    // Try verticals
    if (Math.abs(xp - xc) > EPSILON) {
      // y = yc + (x - xc) * slope
      final double slope = (yp - yc) / (xp - xc);
      // Intersect with left edge: x = y0
      final double yi1 = yc + (x0 - xc) * slope;
      if (yi1 >= y0 && yi1 <= y1) {
        final double dx1 = x0 - xp;
        final double dy1 = yi1 - yp;
        best = dx1 * dx1 + dy1 * dy1;
        found.setLocation(x0, yi1);
        // Intersect with bottom edge: x = x1
        final double yi2 = yc + (x1 - xc) * slope;
        final double dx2 = x1 - xp;
        final double dy2 = yi2 - yp;
        final double dist2 = dx2 * dx2 + dy2 * dy2;
        if (dist2 < best) {
          best = dist2;
          found.setLocation(x1, yi2);
        }
      }
    }
    // Try horizontals
    if (Math.abs(yp - yc) > EPSILON) {
      // x = xc + (y - yc) * slope
      final double slope = (xp - xc) / (yp - yc);
      // Intersect with top edge: y = y0
      final double xi1 = xc + (y0 - yc) * slope;
      if (xi1 >= x0 && xi1 <= x1) {
        final double dx1 = xi1 - xp;
        final double dy1 = y0 - yp;
        final double dist1 = dx1 * dx1 + dy1 * dy1;
        if (dist1 < best) {
          best = dist1;
          found.setLocation(xi1, y0);
        }
        // Intersect with bottom edge: y = y1
        final double xi2 = xc + (y1 - yc) * slope;
        final double dx2 = xi2 - xp;
        final double dy2 = y1 - yp;
        final double dist2 = dx2 * dx2 + dy2 * dy2;
        if (dist2 < best) {
          best = dist2;
          found.setLocation(xi2, y1);
        }
      }
    }
    return found;
  }


  //#########################################################################
  //# Inner Class GeometryVisitor
  private abstract static class GeometryVisitor
    extends AbstractModuleProxyVisitor
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    public Object visitSimpleNodeProxy(final SimpleNodeProxy simple)
      throws VisitorException
    {
      return visitPointGeometryProxy(simple.getPointGeometry());
    }

    public Object visitGroupNodeProxy(final GroupNodeProxy group)
      throws VisitorException
    {
      return visitBoxGeometryProxy(group.getGeometry());
    }

    public Object visitEdgeProxy(final EdgeProxy edge)
      throws VisitorException
    {
      final SplineGeometryProxy geo = edge.getGeometry();
      if (geo != null) {
        return visitSplineGeometryProxy(geo);
      } else {
        return null;
      }
    }

  }


  //#########################################################################
  //# Inner Class TranslateVisitor
  private static class TranslateVisitor
    extends GeometryVisitor
  {

    //#######################################################################
    //# Invocation
    private void translate(final ProxySubject item, final Point2D delta)
    {
      try {
        mDelta = delta;
        item.acceptVisitor(this);
      } catch (final VisitorException exception) {
        throw new WatersRuntimeException(exception);
      }
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    public Object visitBoxGeometryProxy(final BoxGeometryProxy geo)
    {
      final BoxGeometrySubject subject = (BoxGeometrySubject) geo;
      final Rectangle2D rect = geo.getRectangle();
      final double x = rect.getX() + mDelta.getX();
      final double y = rect.getY() + mDelta.getY();
      final double width = rect.getWidth();
      final double height = rect.getHeight();
      rect.setFrame(x, y, width, height);
      subject.setRectangle(rect);
      return null;
    }

    public Object visitPointGeometryProxy(final PointGeometryProxy geo)
    {
      final PointGeometrySubject subject = (PointGeometrySubject) geo;
      final Point2D point = geo.getPoint();
      final double x = point.getX() + mDelta.getX();
      final double y = point.getY() + mDelta.getY();
      point.setLocation(x, y);
      subject.setPoint(point);
      return null;
    }

    public Object visitSplineGeometryProxy(final SplineGeometryProxy geo)
    {
      final SplineGeometrySubject subject = (SplineGeometrySubject) geo;
      final List<Point2D> points = subject.getPointsModifiable();
      final int size = points.size();
      for (int i = 0; i < size; i++) {
        final Point2D point = points.get(i);
        final double x = point.getX() + mDelta.getX();
        final double y = point.getY() + mDelta.getY();
        point.setLocation(x, y);
        points.set(i, point);
      }
      return null;
    }

    //#######################################################################
    //# Data Members
    private Point2D mDelta;

  }


  //#########################################################################
  //# Inner Class NodePositionVisitor
  private static class NodePositionVisitor
    extends GeometryVisitor
  {

    //#######################################################################
    //# Invocation
    private Point2D getPosition(final NodeProxy node)
    {
      try {
        return (Point2D) node.acceptVisitor(this);
      } catch (final VisitorException exception) {
        throw new WatersRuntimeException(exception);
      }
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    public Point2D visitBoxGeometryProxy(final BoxGeometryProxy geo)
    {
      final Rectangle2D rect = geo.getRectangle();
      final double x = rect.getCenterX();
      final double y = rect.getCenterY();
      return new Point2D.Double(x, y);
    }

    public Object visitPointGeometryProxy(final PointGeometryProxy geo)
    {
      return geo.getPoint();
    }

  }
  

  //#########################################################################
  //# Inner Class TopLeftPositionVisitor
  private static class TopLeftPositionVisitor
    extends GeometryVisitor
  {

    //#######################################################################
    //# Invocation
    private Point2D getTopLeftPosition(final NodeProxy node)
    {
      try {
        return (Point2D) node.acceptVisitor(this);
      } catch (final VisitorException exception) {
        throw new WatersRuntimeException(exception);
      }
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    public Point2D visitBoxGeometryProxy(final BoxGeometryProxy geo)
    {
      final Rectangle2D rect = geo.getRectangle();
      final double x = rect.getX();
      final double y = rect.getY();
      return new Point2D.Double(x, y);
    }

    public Object visitPointGeometryProxy(final PointGeometryProxy geo)
    {
      return geo.getPoint();
    }

  }
  

  //#########################################################################
  //# Inner Class DefaultPositionVisitor
  private static class DefaultPositionVisitor
    extends GeometryVisitor
  {

    //#######################################################################
    //# Invocation
    private Point2D getDefaultPosition(final NodeProxy node,
                                       final Point2D target)
    {
      try {
        mTarget = target;
        return (Point2D) node.acceptVisitor(this);
      } catch (final VisitorException exception) {
        throw new WatersRuntimeException(exception);
      }
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    public Point2D visitBoxGeometryProxy(final BoxGeometryProxy geo)
    {
      final Rectangle2D rect = geo.getRectangle();
      return findIntersection(rect, mTarget);
    }

    public Object visitPointGeometryProxy(final PointGeometryProxy geo)
    {
      return geo.getPoint();
    }

    //#######################################################################
    //# Data Members
    private Point2D mTarget;

  }
  

  //#########################################################################
  //# Singleton Variables for Visitors
  private static TranslateVisitor mTranslator = null;
  private static NodePositionVisitor mNodePositionVisitor = null;
  private static TopLeftPositionVisitor mTopLeftPositionVisitor = null;
  private static DefaultPositionVisitor mDefaultPositionVisitor = null;


  //#########################################################################
  //# Class Constants
  /**
   * Accuracy constant. Used to determine when points are so close that
   * they are considered equal.
   */
  public static final double EPSILON = 0.001;

  /**
   * The square of {@link #EPSILON}.
   */
  public static final double EPSILON2 = EPSILON * EPSILON;

}
