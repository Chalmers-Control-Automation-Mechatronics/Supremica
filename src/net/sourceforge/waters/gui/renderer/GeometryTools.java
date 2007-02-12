//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui.renderer
//# CLASS:   GeometryTools
//###########################################################################
//# $Id: GeometryTools.java,v 1.10 2007-02-12 23:24:14 robi Exp $
//###########################################################################


package net.sourceforge.waters.gui.renderer;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.Rectangle2D;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.GroupNodeProxy;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.PointGeometryProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.model.module.SplineGeometryProxy;
import net.sourceforge.waters.subject.module.EdgeSubject;
import net.sourceforge.waters.subject.module.SplineGeometrySubject;

import net.sourceforge.waters.xsd.module.SplineKind;


public final class GeometryTools
{
  public static Point2D defaultPosition(NodeProxy node, Point2D turningpoint)
  {
    if (node instanceof SimpleNodeProxy) {
      return defaultPosition((SimpleNodeProxy) node);
    } else if (node instanceof GroupNodeProxy) {
      return defaultPosition((GroupNodeProxy) node, turningpoint);
    } else {
      throw new ClassCastException
        ("Unknown node type " + node.getClass().getName());
    }
  }

  public static Point2D defaultPosition(SimpleNodeProxy node)
  {
    return node.getPointGeometry().getPoint();
  }

  public static Point2D defaultPosition(GroupNodeProxy node, Point2D point)
  {
    final Rectangle2D r = node.getGeometry().getRectangle();
    return GeometryTools.findIntersection(r, point);
  }

  public static Point2D getPosition(NodeProxy node)
  {
    if (node instanceof SimpleNodeProxy) {
      return getPosition((SimpleNodeProxy) node);
    } else if (node instanceof GroupNodeProxy) {
      return getPosition((GroupNodeProxy) node);
    } else {
      throw new ClassCastException
        ("Unknown node type " + node.getClass().getName());
    }
  }

  public static Point2D getPosition(SimpleNodeProxy node)
  {
    return node.getPointGeometry().getPoint();
  }

  public static Point2D getPosition(GroupNodeProxy node)
  {
    Rectangle2D r = node.getGeometry().getRectangle();
    return new Point2D.Double(r.getCenterX(), r.getCenterY());
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
      return defaultPosition(source, aux);
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
      return defaultPosition(target, aux);
    } else {
      return pointGeo.getPoint();
    }
  }

  /**
   * Gets the position of the handle point of an edge.
   * The handle position either is the single control point of the
   * edge's spline geometry, or the centre of the straight line
   * between the edge's start and end point.
   * @throws IllegalArgumentException if the spline geometry has
   *         more than one control point.
   */
  public static Point2D getHandlePoint1(final EdgeProxy edge)
  {
    final Point2D point = getSpecifiedMidPoint(edge);
    if (point != null) {
      return point;
    } else {
      return getDefaultMidPoint(edge);
    }
  }

  /**
   * Gets the centre point of an edge.
   * This method checks the edge's geometry and returns the specified
   * centre point if one is set, or <CODE>null</CODE> in case of a
   * straight line.
   * @throws IllegalArgumentException if the spline geometry has
   *         more than one control point.
   */
  public static Point2D getSpecifiedMidPoint(final EdgeProxy edge)
  {
    final SplineGeometryProxy geo = edge.getGeometry();
    if (geo == null) {
      return null;
    }
    final List<Point2D> points = geo.getPoints();
    final int size = points.size();
    switch (size) {
    case 0:
      return null;
    case 1:
      return points.iterator().next();
    default:
      throw new IllegalArgumentException
        ("More than one control point in spline!");
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
    final Point2D p1 = getStartPoint(edge);
    final Point2D p2 = getEndPoint(edge);
    return getMidPoint(p1, p2);
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
   * Changes the middle point of an edge.
   * This method changes the edge's spline geometry to reflect the
   * given new centre point. It produces spline with a single control
   * point, or replaces the existing one.
   */
  public static void setSpecifiedMidPoint(final EdgeSubject edge,
                                          final Point2D newpoint)
  {
    final SplineGeometrySubject oldgeo = edge.getGeometry();
    if (oldgeo == null || oldgeo.getPoints().size() != 1) {
      final List<Point2D> points = Collections.singletonList(newpoint);
      final SplineGeometrySubject newgeo =
        new SplineGeometrySubject(points, SplineKind.INTERPOLATING);
      edge.setGeometry(newgeo);
    } else {
      final List<Point2D> points = oldgeo.getPointsModifiable();
      final Point2D oldpoint = points.iterator().next();
      if (oldpoint.distanceSq(newpoint) >= EPSILON2) {
        points.set(0, newpoint);
      }
    }
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
                                       final Point2D newpoint)
  {
    final Point2D p1 = getStartPoint(edge);
    final Point2D p2 = getEndPoint(edge);
    if (p1.distanceSq(p2) < EPSILON2) {
      setSpecifiedMidPoint(edge, newpoint);
    } else {
      final Line2D line = new Line2D.Double(p1, p2);
      final double distance = line.ptLineDist(newpoint);
      if (distance >= 1.5) {
        setSpecifiedMidPoint(edge, newpoint);
      } else {
        edge.setGeometry(null);
      }
    }
  }

  /**
   * Creates default geometry for an edge.
   * This method either clears the geometry of the given edge (straight
   * line), or creates a spline geometry with default turning point for
   * a selfloop.
   */
  public static void createDefaultGeometry(final EdgeSubject edge)
  {
    final NodeProxy source = edge.getSource();
    final NodeProxy target = edge.getTarget();
    if (source == target && source instanceof SimpleNodeProxy) {
      // *** BUG ***
      // Should use named constant!
      // ***
      final Point2D point = getPosition(source);
      point.setLocation(point.getX() + 20, point.getY() + 20);
      setSpecifiedMidPoint(edge, point);
    } else {
      edge.setGeometry(null);
    }
  }

  public static Point2D getRadialPoint(Point2D p, 
                                       Point2D center,
                                       double radius)
  {
    double width = center.getX() - p.getX();
    double height = center.getY() - p.getY();
    double angle = Math.atan2(height, width);
    return new Point2D.Double(center.getX() - Math.cos(angle) * radius,
                              center.getY() - Math.sin(angle) * radius);
  }

  public static Line2D[] getLineSegmentsOfRectangle(Rectangle2D r)
  {
    Line2D[] lines = new Line2D[4];
    lines[0] = new Line2D.Double(r.getMinX(), r.getMinY(),
                                 r.getMaxX(), r.getMinY());
    lines[1] = new Line2D.Double(r.getMaxX(), r.getMinY(),
                                 r.getMaxX(), r.getMaxY());
    lines[2] = new Line2D.Double(r.getMaxX(), r.getMaxY(),
                                 r.getMinX(), r.getMaxY());
    lines[3] = new Line2D.Double(r.getMinX(), r.getMaxY(),
                                 r.getMinX(), r.getMinY());
    return lines;
  }

  public static double[] convertLineIntoEquation(Line2D l)
  {
    double[] eq = new double[3];
    eq[0] = l.getY1() - l.getY2();
    eq[1] = -(l.getX1() - l.getX2());
    eq[2] = -(eq[0] * l.getX1() + eq[1] * l.getY1());
    return eq;
  }

  public static Point2D getControlPoint(QuadCurve2D quad)
  {
    return new Point2D.Double(
                              (2 * quad.getCtrlX() - (quad.getX1() + quad.getX2()) / 2),
                              (2 * quad.getCtrlY() - (quad.getY1() + quad.getY2()) / 2));
  }

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
