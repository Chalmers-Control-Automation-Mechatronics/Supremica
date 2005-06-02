//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.module
//# CLASS:   GeometryProxy
//###########################################################################
//# $Id: GeometryProxy.java,v 1.2 2005-06-02 12:18:03 flordal Exp $
//###########################################################################

package net.sourceforge.waters.model.module;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.base.ElementProxy;
import net.sourceforge.waters.model.base.IndexedCollectionProxy;
import net.sourceforge.waters.xsd.module.BoxType;
import net.sourceforge.waters.xsd.module.ColorType;
import net.sourceforge.waters.xsd.module.ObjectFactory;
import net.sourceforge.waters.xsd.module.PointType;


/**
 * <P>An abstract base class for all objects holding rendering information.</P>
 *
 * <P>This class is used as a base class by all classes that represent some
 * kind of rendering information. These classes usually contain some
 * geometric objects from the {@link java.awt} package.  The base class
 * provides a means of type safety, and implements some functionality for
 * marshalling, unmarshalling, and comparing that is used internally.</P>
 *
 * @author Robi Malik
 */
public abstract class GeometryProxy extends ElementProxy {

  //#########################################################################
  //# Constructors
  /**
   * Creates an empty geometry object.
   */
  GeometryProxy()
  {
  }

  /**
   * Creates a copy of a geometry object.
   * @param  partner     The object to be copied from.
   */
  GeometryProxy(final GeometryProxy partner)
  {
    super(partner);
  }


  //#########################################################################
  //# Auxiliary Methods
  static BoxType createBox(final Rectangle2D rect2d)
    throws JAXBException
  {
    Rectangle rect;
    if (rect2d instanceof Rectangle) {
      rect = (Rectangle) rect2d;
    } else {
      rect = new Rectangle();
      rect.setRect(rect2d);
    }
    return createBox(rect.x, rect.y, rect.width, rect.height);
  }

  static BoxType createBox(final int x,
			   final int y,
			   final int width,
			   final int height)
    throws JAXBException
  {
    final ObjectFactory factory = ModuleElementFactory.getFactory();
    final BoxType box = factory.createBox();
    box.setX(x);
    box.setY(y);
    box.setWidth(width);
    box.setHeight(height);
    return box;
  }

  static PointType createPoint(final Point2D point2d)
    throws JAXBException
  {
    Point point;
    if (point2d instanceof Point) {
      point = (Point) point2d;
    } else {
      point = new Point();
      point.setLocation(point2d);
    }
    return createPoint(point.x, point.y);
  }

  static PointType createPoint(final int x, final int y)
    throws JAXBException
  {
    final ObjectFactory factory = ModuleElementFactory.getFactory();
    final PointType point = factory.createPoint();
    point.setX(x);
    point.setY(y);
    return point;
  }

  static ColorType createColor(final int red, final int green, final int blue)
    throws JAXBException
  {
    final ObjectFactory factory = ModuleElementFactory.getFactory();
    final ColorType color = factory.createColor();
    color.setRed(red);
    color.setGreen(green);
    color.setBlue(blue);
    return color;
  }


  //#########################################################################
  //# Auxiliary Static Methods
  static boolean equalGeometry(final GeometryProxy geo1,
			       final GeometryProxy geo2)
  {
    if (geo1 == null) {
      return geo2 == null;
    } else {
      return geo1.equals(geo2);
    }
  }

}
