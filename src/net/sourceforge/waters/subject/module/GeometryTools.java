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

package net.sourceforge.waters.subject.module;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.module.BoxGeometryProxy;
import net.sourceforge.waters.model.module.DefaultModuleProxyVisitor;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.GroupNodeProxy;
import net.sourceforge.waters.model.module.HornerPolynomial;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.LabelGeometryProxy;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.PointGeometryProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.model.module.SplineGeometryProxy;
import net.sourceforge.waters.model.module.SplineKind;
import net.sourceforge.waters.subject.base.ProxySubject;


/**
 * A collection of methods to read and modify the geometry of module
 * elements. This class provides several static methods to read and
 * write geometry attributes and fill in default values if the actual
 * value is absent.
 *
 * @author Robi Malik
 */

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

  public static Point2D getTopLeftPosition
    (final LabelBlockProxy blocked,
     final Collection<? extends NodeProxy> nodes)
  {
    double minx = Double.POSITIVE_INFINITY;
    double miny = Double.POSITIVE_INFINITY;
    if (blocked != null) {
      final Point2D point = getTopLeftPosition(blocked);
      minx = point.getX();
      miny = point.getY();
    }
    for (final NodeProxy node : nodes) {
      final Point2D point = getTopLeftPosition(node);
      final double x = point.getX();
      final double y = point.getY();
      minx = Math.min(minx, x);
      miny = Math.min(miny, y);
    }
    return new Point2D.Double(minx, miny);
  }

  public static Point2D getTopLeftPosition(final Proxy proxy)
  {
    if (mTopLeftPositionVisitor == null) {
      mTopLeftPositionVisitor = new TopLeftPositionVisitor();
    }
    return mTopLeftPositionVisitor.getTopLeftPosition(proxy);
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
      return start.distanceSq(end) < EPSILON_SQ;
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
   * This method tries to find a B&eacute;zier control point for the
   * given edge.
   * @throws IllegalArgumentException if the spline geometry has
   *         more than one control point.
   */
  public static Point2D getSingleBezierControlPoint(final EdgeProxy edge)
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
   * Gets the positions of the two control points of a cubic edge.
   * This method tries to find two B&eacute;zier control points for the
   * given edge.
   * @throws IllegalArgumentException if the spline geometry has
   *         more than two control points.
   */
  public static Point2D[] getCubicBezierControlPoints(final EdgeProxy edge)
  {
    final Point2D[] points = new Point2D[2];
    final Point2D start = getStartPoint(edge);
    final Point2D end = getEndPoint(edge);
    final SplineGeometryProxy geo = edge.getGeometry();
    final List<Point2D> geopoints;
    final int geosize;
    if (geo == null) {
      geopoints = null;
      geosize = 0;
    } else {
      geopoints = geo.getPoints();
      geosize = geopoints.size();
    }
    switch (geosize) {
    case 0:
      points[0] = getPointOnLine(start, end, ONE_THIRD);
      points[1] = getPointOnLine(start, end, 2.0 * ONE_THIRD);
      return points;
    case 1:
      final Point2D control = getSingleBezierControlPoint(edge);
      points[0] = getPointOnQuadratic(start, control, end, ONE_THIRD);
      points[1] = getPointOnQuadratic(start, control, end, 2.0 * ONE_THIRD);
      break;
    case 2:
      final Iterator<Point2D> iter = geopoints.iterator();
      points[0] = iter.next();
      points[1] = iter.next();
      if (geo.getKind() == SplineKind.BEZIER) {
        return points;
      }
      break;
    default:
      throw new IllegalArgumentException
        ("Unsupported number of control points in spline!");
    }
    final double factor = 0.5 * ONE_THIRD;  // 1/6
    final double xi0 = start.getX();
    final double yi0 = start.getY();
    final double xi1 = points[0].getX();
    final double yi1 = points[0].getY();
    final double xi2 = points[1].getX();
    final double yi2 = points[1].getY();
    final double xi3 = end.getX();
    final double yi3 = end.getY();
    final double x1 =
      factor * (18.0 * xi1 - 9.0 * xi2 - 5.0 * xi0 + 2.0 * xi3);
    final double y1 =
      factor * (18.0 * yi1 - 9.0 * yi2 - 5.0 * yi0 + 2.0 * yi3);
    final double x2 =
      factor * (18.0 * xi2 - 9.0 * xi1 - 5.0 * xi3 + 2.0 * xi0);
    final double y2 =
      factor * (18.0 * yi2 - 9.0 * yi1 - 5.0 * yi3 + 2.0 * yi0);
    points[0].setLocation(x1, y1);
    points[1].setLocation(x2, y2);
    return points;
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
      final double x = start.getX() + SELFLOOP_OFFSET_X;
      final double y = start.getY() + SELFLOOP_OFFSET_Y;
      return new Point2D.Double(x, y);
    } else {
      final Point2D end = getEndPoint(edge);
      return getMidPoint(start, end);
    }
  }

  /**
   * Finds the point on an edge that is closest to a given point.
   */
  public static Point2D findClosestPointOnEdge(final EdgeProxy edge,
                                               final Point2D point)
  {
    if (isSelfloop(edge)) {
      throw new UnsupportedOperationException
        ("Finding closest point not implemented for selfloops!");
    } else if (edge.getGeometry() == null) {
      final Point2D start = getStartPoint(edge);
      final Point2D end = getEndPoint(edge);
      return findClosestPointOnLine(start, end, point);
    } else {
      final Point2D start = getStartPoint(edge);
      final Point2D end = getEndPoint(edge);
      final Point2D ctrl = getSingleBezierControlPoint(edge);
      return findClosestPointOnQuadratic(start, ctrl, end, point);
    }
  }

  /**
   * Finds the point closest to a point 'p' on the line
   * between two points 'pt1' and 'pt2'.
   * @param pt1              one end of the line.
   * @param pt2              the other end of the line.
   * @param p                the point to find the closest point to.
   * @return The point closest to 'p' on the line.
   */
  public static Point2D findClosestPointOnLine(final Point2D pt1,
                                               final Point2D pt2,
                                               final Point2D p)
  {
    // If the two points represent the same point then
    // they are the closest point.
    if (pt1.equals(pt2)) {
      return pt1;
    }
    // Compute the relative position of the closest point to the point 'p'
    // u = ((p - pt1) . (pt2 - pt1)) / ((pt2 - pt1) . (pt2 - pt1))
    // where '.' is the vector dot product.
    final double x = p.getX();
    final double y = p.getY();
    final double x1 = pt1.getX();
    final double y1 = pt1.getY();
    final double x2 = pt2.getX();
    final double y2 = pt2.getY();
    final double dx = x2 - x1;
    final double dy = y2 - y1;
    final double u = (dx * (x - x1) + dy * (y - y1)) / (dx * dx + dy * dy);
    // Remove this conditional statement if you allow the closest point to be
    // exterior to the direct line between pt1 and pt2.
    if (u >= 1.0) {
      return pt2;
    } else if (u <= 0.0) {
      return pt1;
    } else {
      // Create the closest point
      return new Point2D.Double(x2 * u + x1 * (1.0 - u),
                                y2 * u + y1 * (1.0 - u));
    }
  }


  /**
   * Finds the point closest to a point 'p' on the B&eacute;zier quadratic
   * curve between two points 'pt1' and 'pt2', and with control point 'c'.
   * @param pt1              One end of the curve.
   * @param c                The B&eacute;zier control point of the curve.
   * @param pt2              The other end of the curve.
   * @param p                The point to find the closest point to.
   * @return The point closest to point 'p' on the curve.
   */
  public static Point2D findClosestPointOnQuadratic(final Point2D pt1,
                                                    final Point2D c,
                                                    final Point2D pt2,
                                                    final Point2D p)
  {
    final HornerPolynomial biquadratic =
      getClosestPointBiquadratic(pt1, c, pt2, p);
    if (biquadratic == null) {
      // If the curve is (almost) straight, or if the start and end points
      // are (almost) the same, treat it as two lines ...
      final Point2D turn = convertToTurn(pt1, pt2, c);
      final Point2D candidate1 = findClosestPointOnLine(pt1, turn, p);
      final Point2D candidate2 = findClosestPointOnLine(pt2, turn, p);
      if (candidate1.equals(candidate2)) {
        return candidate1;
      } else if (candidate1.distanceSq(p) < candidate2.distanceSq(p)) {
        return candidate1;
      } else {
        return candidate2;
      }
    } else {
      // Otherwise we can minimise the biquadratic ...
      final double tmin = biquadratic.findBiquadraticMinimum(0.0, 1.0);
      // Return the point ...
      if (tmin <= 0.0) {
        return pt1;
      } else if (tmin >= 1.0) {
        return pt2;
      } else {
        return getPointOnQuadratic(pt1, c, pt2, tmin);
      }
    }
  }

  public static HornerPolynomial getClosestPointBiquadratic(final Point2D pt1,
                                                            final Point2D c,
                                                            final Point2D pt2,
                                                            final Point2D p)
  {
    // f(t) = (1-t)^2*pt1 + 2t*(1-t)*c + t^2*pt2
    //      = pt1 - 2t*pt1 + t^2*pt1 +2t*c - 2t^2*c + t^2*pt2
    //      = (pt1+pt2-2c)*t^2 + (2c-2pt1)*t + pt1
    // d(t) = || f(t) - p ||^2
    // First calculate coefficients of f(t) - p:
    // f(t) - p = a2*t^2 + a1*t + a0
    final double x1 = pt1.getX();
    final double y1 = pt1.getY();
    final double x2 = pt2.getX();
    final double y2 = pt2.getY();
    final double xc = c.getX();
    final double yc = c.getY();
    final double x = p.getX();
    final double y = p.getY();
    final double a2x = x1 + x2 - 2.0 * xc;
    final double a2y = y1 + y2 - 2.0 * yc;
    final double a1x = 2.0 * (xc - x1);
    final double a1y = 2.0 * (yc - y1);
    final double a0x = x1 - x;
    final double a0y = y1 - y;
    // Now square them to get the coefficients of d(t):
    // d(t) = d4*t^4 + d3*t^3 + d2*t^2 + d1*t + d0
    final double d4 = a2x * a2x + a2y * a2y;
    if (d4 == 0.0) {
      return null;
    }
    final double d3 = 2.0 * (a2x * a1x + a2y * a1y);
    final double d2 = a1x * a1x + a1y * a1y + 2.0 * (a2x * a0x + a2y * a0y);
    final double d1 = 2.0 * (a1x * a0x + a1y * a0y);
    final double d0 = a0x * a0x + a0y * a0y;
    return new HornerPolynomial(d4, d3, d2, d1, d0);
  }

  /**
   * Calculates a bounding box for a quadratic B&eacute;zier spline.
   * This method calculates a tight and exact bounding box by taking
   * into account all three control points and calculating extremals.
   * @param  pt1             One end of the curve.
   * @param  c               The B&eacute;zier control point of the curve.
   * @param  pt2             The other end of the curve.
   * @return A tight bounding box of the curve.
   */
  public static Rectangle2D getQuadraticBoundingBox(final Point2D pt1,
                                                    final Point2D c,
                                                    final Point2D pt2)
  {
    double xmin, xmax, ymin, ymax;
    final double x1 = pt1.getX();
    final double x2 = pt2.getX();
    if (x1 < x2) {
      xmin = x1;
      xmax = x2;
    } else {
      xmin = x2;
      xmax = x1;
    }
    final double xc = c.getX();
    final double xdet = x1 + x2 - 2.0 * xc;
    if (Math.abs(xdet) > EPSILON) {
      final double t = (x1 - xc) / xdet;
      if (t >= 0.0 && t <= 1.0) {
        final double t1 = 1.0 - t;
        final double xe = t1 * t1 * x1 + 2.0 * t * t1 * xc + t * t * x2;
        if (xe < xmin) {
          xmin = xe;
        } else if (xe > xmax) {
          xmax = xe;
        }
      }
    }
    final double y1 = pt1.getY();
    final double y2 = pt2.getY();
    if (y1 < y2) {
      ymin = y1;
      ymax = y2;
    } else {
      ymin = y2;
      ymax = y1;
    }
    final double yc = c.getY();
    final double ydet = y1 + y2 - 2.0 * yc;
    if (Math.abs(ydet) > EPSILON) {
      final double t = (y1 - yc) / ydet;
      if (t >= 0.0 && t <= 1.0) {
        final double t1 = 1.0 - t;
        final double ye = t1 * t1 * y1 + 2.0 * t * t1 * yc + t * t * y2;
        if (ye < ymin) {
          ymin = ye;
        } else if (ye > ymax) {
          ymax = ye;
        }
      }
    }
    return new Rectangle2D.Double(xmin, ymin, xmax - xmin, ymax - ymin);
  }

  /**
   * Calculates a bounding box for a cubic B&eacute;zier spline.
   * This method calculates a tight and exact bounding box by taking
   * into account all four control points and calculating extremals.
   * @param  pt1             One end of the curve.
   * @param  c1              The first B&eacute;zier control point of the
   *                         curve.
   * @param  c2              The second B&eacute;zier control point of the
   *                         curve.
   * @param  pt2             The other end of the curve.
   * @return A tight bounding box of the curve.
   */
  public static Rectangle2D getCubicBoundingBox(final Point2D pt1,
                                                final Point2D c1,
                                                final Point2D c2,
                                                final Point2D pt2)
  {
    double xmin, xmax, ymin, ymax;
    final double x0 = pt1.getX();
    final double x1 = c1.getX();
    final double x2 = c2.getX();
    final double x3 = pt2.getX();
    if (x0 < x3) {
      xmin = x0;
      xmax = x3;
    } else {
      xmin = x3;
      xmax = x0;
    }
    final double ax3 = x3 - 3 * x2 + 3 * x1 - x0;
    final double ax2 = 3 * (x2 - 2 * x1 + x0);
    final double ax1 = 3 * (x1 - x0);
    final double ax0 = x0;
    final HornerPolynomial px = new HornerPolynomial(ax3, ax2, ax1, ax0);
    final HornerPolynomial nx = px.normalize();
    final double[] ex = nx.findCubicExtremals();
    if (ex != null) {
      for (final double t : ex) {
        if (t > 0.0 && t < 1.0) {
          final double x = nx.getValue(t);
          if (x < xmin) {
            xmin = x;
          } else if (x > xmax) {
            xmax = x;
          }
        }
      }
    }
    final double y0 = pt1.getY();
    final double y1 = c1.getY();
    final double y2 = c2.getY();
    final double y3 = pt2.getY();
    if (y0 < y3) {
      ymin = y0;
      ymax = y3;
    } else {
      ymin = y3;
      ymax = y0;
    }
    final double ay3 = y3 - 3 * y2 + 3 * y1 - y0;
    final double ay2 = 3 * (y2 - 2 * y1 + y0);
    final double ay1 = 3 * (y1 - y0);
    final double ay0 = y0;
    final HornerPolynomial py = new HornerPolynomial(ay3, ay2, ay1, ay0);
    final HornerPolynomial ny = py.normalize();
    final double[] ey = ny.findCubicExtremals();
    if (ey != null) {
      for (final double t : ey) {
        if (t > 0.0 && t < 1.0) {
          final double y = ny.getValue(t);
          if (y < ymin) {
            ymin = y;
          } else if (y > ymax) {
            ymax = y;
          }
        }
      }
    }
    return new Rectangle2D.Double(xmin, ymin, xmax - xmin, ymax - ymin);
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
   * Calculates a point along the line between two other points.
   * @param  p1     The start point of the line.
   * @param  p2     The end point of the line.
   * @param  offset The relative distance travelled along the line.
   *                If set to&nbsp;<CODE>0</CODE>,
   *                the method returns&nbsp;<CODE>p1</CODE>;
   *                If set to&nbsp;<CODE>1</CODE>,
   *                the method returns&nbsp;<CODE>p2</CODE>;
   *                For offsets between <CODE>0</CODE>
   *                and&nbsp;<CODE>1</CODE>, a point between the start and
   *                end of the line is returned.
   */
  public static Point2D getPointOnLine
    (final Point2D p1, final Point2D p2, final double offset)
  {
    final double x1 = p1.getX();
    final double y1 = p1.getY();
    final double x2 = p2.getX();
    final double y2 = p2.getY();
    final double x = x1 + offset * (x2 - x1);
    final double y = y1 + offset * (y2 - y1);
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
    return convertToTurn(start, end, control);
  }

  /**
   * Calculates the position of a quadratic spline edge's turning point
   * from its control point.
   */
  public static Point2D convertToTurn(final Point2D start,
                                      final Point2D end,
                                      final Point2D control)
  {
    final double x = 0.25 * (start.getX() + 2.0 * control.getX() + end.getX());
    final double y = 0.25 * (start.getY() + 2.0 * control.getY() + end.getY());
    return new Point2D.Double(x, y);
  }

  /**
   * Calculates the position of a quadratic spline edge's control point
   * from its turning point.
   */
  public static Point2D convertToControl(final EdgeProxy edge,
                                         final Point2D turn)
  {
    final Point2D start = getStartPoint(edge);
    final Point2D end = getEndPoint(edge);
    return convertToControl(start, end, turn);
  }

  /**
   * Calculates the position of a quadratic spline edge's control point
   * from its turning point.
   */
  public static Point2D convertToControl(final Point2D start,
                                         final Point2D end,
                                         final Point2D turn)
  {
    final double x = 2.0 * turn.getX() - 0.5 * (start.getX() + end.getX());
    final double y = 2.0 * turn.getY() - 0.5 * (start.getY() + end.getY());
    return new Point2D.Double(x, y);
  }

  public static Point2D getPointOnQuadratic(final Point2D start,
                                            final Point2D control,
                                            final Point2D end,
                                            final double t)
  {
    if (t == 0.0) {
      return start;
    } else if (t == 1.0) {
      return end;
    } else {
      final double x1 = start.getX();
      final double y1 = start.getY();
      final double x2 = end.getX();
      final double y2 = end.getY();
      final double xc = control.getX();
      final double yc = control.getY();
      final double t2 = t * t;
      final double tbar = 1.0 - t;
      final double tbar2 = tbar * tbar;
      final double tt = 2.0 * t * tbar;
      final double x = tbar2 * x1 + tt * xc + t2 * x2;
      final double y = tbar2 * y1 + tt * yc + t2 * y2;
      return new Point2D.Double(x, y);
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
                                        final Point2D newpoint,
                                       final SplineKind kind)
  {
    if (isSelfloop(edge)) {
      setSpecifiedMidPoint(edge, newpoint, kind);
      return;
    }
    final Point2D start = getStartPoint(edge);
    final Point2D end = getEndPoint(edge);
    if (start.distanceSq(end) < EPSILON_SQ) {
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
   * given new centre point. It produces a spline with a single control
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
      if (oldpoint.distanceSq(newpoint) >= EPSILON_SQ) {
        points.set(0, newpoint);
      }
      oldgeo.setKind(kind);
    }
  }

  public static Point2D getRadialStartPoint(final EdgeProxy edge,
                                            final Point2D dir,
                                            final int radius)
  {
    final NodeProxy source = edge.getSource();
    final PointGeometryProxy start = edge.getStartPoint();
    return getRadialPoint(source, start, dir, radius);
  }

  public static Point2D getRadialEndPoint(final EdgeProxy edge,
                                          final Point2D dir,
                                          final int radius)
  {
    final NodeProxy target = edge.getTarget();
    final PointGeometryProxy end = edge.getEndPoint();
    return getRadialPoint(target, end, dir, radius);
  }

  public static Point2D getRadialPoint(final NodeProxy node,
                                       final PointGeometryProxy geo,
                                       final Point2D dir,
                                       final int radius)
  {
    if (node == null) {
      return geo.getPoint();
    } else if (node instanceof SimpleNodeProxy) {
      final SimpleNodeProxy simple = (SimpleNodeProxy) node;
      final Point2D pos = getPosition(simple);
      final Point2D normdir = getNormalizedDirection(pos, dir);
      return getRadialPoint(simple, normdir, radius);
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
                                       final Point2D normdir,
                                       final int radius)
  {
    final Point2D center = getPosition(node);
    final double dx = normdir.getX();
    final double dy = normdir.getY();
    final double x = center.getX() + radius * dx;
    final double y = center.getY() + radius * dy;
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
    final Point2D found  = new Point2D.Double();
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
    extends DefaultModuleProxyVisitor
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    @Override
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

    @Override
    public Object visitGroupNodeProxy(final GroupNodeProxy group)
      throws VisitorException
    {
      return visitBoxGeometryProxy(group.getGeometry());
    }

    @Override
    public Object visitLabelBlockProxy(final LabelBlockProxy block)
      throws VisitorException
    {
      return visitLabelGeometryProxy(block.getGeometry());
    }

    @Override
    public Object visitSimpleNodeProxy(final SimpleNodeProxy simple)
      throws VisitorException
    {
      return visitPointGeometryProxy(simple.getPointGeometry());
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
    @Override
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

    @Override
    public Object visitEdgeProxy(final EdgeProxy edge)
      throws VisitorException
    {
      final SplineGeometryProxy splineGeo = edge.getGeometry();
      if (splineGeo != null) {
        visitSplineGeometryProxy(splineGeo);
      }
      final PointGeometryProxy startGeo = edge.getStartPoint();
      if (startGeo != null) {
        visitPointGeometryProxy(startGeo);
      }
      final PointGeometryProxy endGeo = edge.getEndPoint();
      if (endGeo != null) {
        visitPointGeometryProxy(endGeo);
      }
      return null;
    }

    @Override
    public Object visitLabelGeometryProxy(final LabelGeometryProxy geo)
    {
      final LabelGeometrySubject subject = (LabelGeometrySubject) geo;
      final Point2D offset = geo.getOffset();
      final double x = offset.getX() + mDelta.getX();
      final double y = offset.getY() + mDelta.getY();
      offset.setLocation(x, y);
      subject.setOffset(offset);
      return null;
    }

    @Override
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

    @Override
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
    @Override
    public Point2D visitBoxGeometryProxy(final BoxGeometryProxy geo)
    {
      final Rectangle2D rect = geo.getRectangle();
      final double x = rect.getCenterX();
      final double y = rect.getCenterY();
      return new Point2D.Double(x, y);
    }

    @Override
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
    private Point2D getTopLeftPosition(final Proxy proxy)
    {
      try {
        return (Point2D) proxy.acceptVisitor(this);
      } catch (final VisitorException exception) {
        throw new WatersRuntimeException(exception);
      }
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    @Override
    public Point2D visitBoxGeometryProxy(final BoxGeometryProxy geo)
    {
      final Rectangle2D rect = geo.getRectangle();
      final double x = rect.getX();
      final double y = rect.getY();
      return new Point2D.Double(x, y);
    }

    @Override
    public Object visitLabelGeometryProxy(final LabelGeometryProxy geo)
    {
      return geo.getOffset();
    }

    @Override
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
    @Override
    public Point2D visitBoxGeometryProxy(final BoxGeometryProxy geo)
    {
      final Rectangle2D rect = geo.getRectangle();
      return findIntersection(rect, mTarget);
    }

    @Override
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
  public static final double EPSILON = 0.00001;

  /**
   * The square of {@link #EPSILON}.
   */
  public static final double EPSILON_SQ = EPSILON * EPSILON;

  /**
   * One third.
   */
  private static final double ONE_THIRD = 1.0 / 3.0;

  /**
   * Square root of 2.
   */
  public static final double SQRT2 = Math.sqrt(2.0);

  /**
   * The default opening angle of a selfloop. The aperture of a selfloop
   * defines the angle between its two lines connecting to the node.
   * This is currently set to 72 degrees.
   */
  public static final double SELFLOOP_APERTURE = 0.4 * Math.PI;  // 72deg

  private static final double SELFLOOP_SIN = Math.sin(0.5 * SELFLOOP_APERTURE);

  /**
   * The relative radius of the circular component of a selfloop.
   */
  public static final double SELFLOOP_RADIUS =
    SELFLOOP_SIN / (1.0 + SELFLOOP_SIN);
  /**
   * The arc angle of the circular component of a selfloop with default shape.
   */
  public static final double SELFLOOP_EXTENT =
    180.0 + Math.toDegrees(SELFLOOP_APERTURE);

  /**
   * The height and width of a selfloop with default geometry.
   */
  public static final double SELFLOOP_SIZE = 48.0;
  /**
   * The x component of the default control point given to a selfloop
   * without explicit geometry.
   */
  public static final double SELFLOOP_OFFSET_X =
    (SELFLOOP_SIN + 1.0) / (2.0 * SELFLOOP_SIN + SQRT2) * SQRT2 * SELFLOOP_SIZE;
  /**
   * The y component of the default control point given to a selfloop
   * without explicit geometry.
   */
  public static final double SELFLOOP_OFFSET_Y = - SELFLOOP_OFFSET_X;

  /**
   * The distance of the control point of a selfloop to the node,
   * when the height and width of the selfloop are both equal to one and
   * the selfloop has standard 45 degrees (north-east) orientation.
   */
  public static final double SELFLOOP_DISTANCE_UNIT =
    2.0 * (SELFLOOP_SIN + 1.0) / (2.0 * SELFLOOP_SIN + SQRT2);

}
