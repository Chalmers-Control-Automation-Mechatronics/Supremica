//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui.renderer
//# CLASS:   GeometryTools
//###########################################################################
//# $Id: GeometryTools.java,v 1.8 2007-02-12 03:54:09 siw4 Exp $
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
    if (edge.getSource() instanceof GroupNodeProxy) {
      final PointGeometryProxy pointGeo = edge.getStartPoint();
      if (pointGeo == null) {
        final NodeProxy source = edge.getSource();
        final SplineGeometryProxy edgeGeo = edge.getGeometry();
        final Point2D aux;
        if (edgeGeo == null || edgeGeo.getPoints().isEmpty()) {
          final NodeProxy target = edge.getTarget();
          aux = getPosition(target);
        } else {
          aux = edgeGeo.getPoints().get(0);
        }
        return defaultPosition(source, aux);
      } else {
        return pointGeo.getPoint();
      }
    } else {
      return getPosition(edge.getSource());
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
    if (edge.getTarget() instanceof GroupNodeProxy) {
      final PointGeometryProxy pointGeo = edge.getEndPoint();
      if (pointGeo == null) {
        final NodeProxy target = edge.getTarget();
        final SplineGeometryProxy edgeGeo = edge.getGeometry();
        final Point2D aux;
        if (edgeGeo == null || edgeGeo.getPoints().isEmpty()) {
          final NodeProxy source = edge.getSource();
          aux = getPosition(source);
        } else {
          final int index = edgeGeo.getPoints().size() - 1;
          aux = edgeGeo.getPoints().get(index);
        }
        return defaultPosition(target, aux);
      } else {
        return pointGeo.getPoint();
      }
    } else {
      return getPosition(edge.getTarget());
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
   * given new centre point. It produces either a quadratic spline
   * with a single control point, or a straight line if the given
   * point is close enough to the straight line between the edge's
   * source and target.
   */
  public static void setSpecifiedMidPoint(final EdgeSubject edge,
                                          final Point2D newpoint)
  {
    final SplineGeometrySubject oldgeo = edge.getGeometry();
    final Point2D p1 = getStartPoint(edge);
    final Point2D p2 = getEndPoint(edge);
    final Line2D line = new Line2D.Double(p1, p2);
    final double distance = line.ptLineDist(newpoint);
    if (distance < EPSILON) {
      edge.setGeometry(null);
    } else if (oldgeo == null || oldgeo.getPoints().size() != 1) {
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

  public static Point2D findIntersection(Line2D line1, Line2D line2)
  {
    double[] l1 = convertLineIntoEquation(line1);
    double[] l2 = convertLineIntoEquation(line2);
    double x = 0;
    double y = 0;
    double a = 0;
    double b = 0;
    double c = 0;
    a = l2[0] / l1[0];
    b = l2[1] - (l1[1] * a);
    c = l2[2] - (l1[2] * a);
    y = (-c) / b;
    a = l2[1] / l1[1];
    b = l2[0] - (l1[0] * a);
    c = l2[2] - (l1[2] * a);
    x = (-c) / b;
    return new Point2D.Double(x, y);
  }

  public static Point2D getControlPoint(QuadCurve2D quad)
  {
    return new Point2D.Double(
                              (2 * quad.getCtrlX() - (quad.getX1() + quad.getX2()) / 2),
                              (2 * quad.getCtrlY() - (quad.getY1() + quad.getY2()) / 2));
  }

  public static Point2D findIntersection(final Rectangle2D r, final Point2D p)
  {
    // can be optimized
    Line2D[] lines = GeometryTools.getLineSegmentsOfRectangle(r);
    Line2D line = new Line2D.Double(r.getCenterX(), r.getCenterY(),
                                    p.getX(), p.getY());
    Point2D intersection = null;
    int distance = Integer.MAX_VALUE;
    for (int i = 0; i < lines.length; i++)
      {
        Point2D t = GeometryTools.findIntersection(line, lines[i]);
        //            System.err.println("Distance: " + t.distance(p.getX(), p.getY()) + "SegDist:" + lines[i].ptSegDist(t));
        if (t.distance(p.getX(), p.getY()) < distance
            && lines[i].ptSegDist(t) <= 0.01)
          {
            distance = (int)t.distance(p.getX(), p.getY());
            intersection = t;
          }
      }
    // System.err.println(intersection);
    return intersection;
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
