package net.sourceforge.waters.gui.renderer;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.awt.geom.Rectangle2D;

import net.sourceforge.waters.model.module.GroupNodeProxy;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import java.awt.geom.QuadCurve2D;

public final class GeometryTools
{
    public static Point2D defaultPosition(NodeProxy node, Point2D turningpoint)
    {
        if (node instanceof SimpleNodeProxy)
        {
            return defaultPosition((SimpleNodeProxy)node);
        }
        return defaultPosition((GroupNodeProxy)node, turningpoint);
    }

    public static Point2D defaultPosition(SimpleNodeProxy node)
    {
        return node.getPointGeometry().getPoint();
    }

    public static Point2D defaultPosition(GroupNodeProxy node, Point2D point)
    {
        Rectangle2D r = node.getGeometry().getRectangle();
        return GeometryTools.findIntersection(r, point);
    }

    public static Point2D getPosition(NodeProxy node)
    {
        if (node instanceof SimpleNodeProxy)
        {
            return getPosition((SimpleNodeProxy)node);
        }
        return getPosition((GroupNodeProxy)node);
    }

    public static Point2D getPosition(SimpleNodeProxy node)
    {
        return node.getPointGeometry().getPoint();
    }

    public static Point2D getRadialPoint(Point2D p, Point2D center, double radius)
    {
        double width = center.getX() - p.getX();
        double height = center.getY() - p.getY();
        double angle = Math.atan2(height, width);
        return new Point2D.Double(center.getX() - Math.cos(angle) * radius,
            center.getY() - Math.sin(angle) * radius);
    }

    public static Point2D getPosition(GroupNodeProxy node)
    {
        Rectangle2D r = node.getGeometry().getRectangle();
        return new Point2D.Double(r.getCenterX(), r.getCenterY());
    }

    public static Point2D getMidPoint(Point2D p1, Point2D p2)
    {
        double w = p2.getX() - p1.getX();
        double h = p2.getY() - p1.getY();
        return new Point2D.Double(p1.getX() + (w / 2), p1.getY() + (h / 2));
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
}
