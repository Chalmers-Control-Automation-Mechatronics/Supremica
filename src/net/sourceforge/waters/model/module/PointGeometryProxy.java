//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.module
//# CLASS:   PointGeometryProxy
//###########################################################################
//# $Id: PointGeometryProxy.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.module;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.List;
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.base.ElementFactory;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.xsd.base.ElementType;
import net.sourceforge.waters.xsd.module.PointType;
import net.sourceforge.waters.xsd.module.PointGeometryType;


/**
 * <P>A geometry object representing a single point.</P>
 *
 * <P>This geometry object is used for objects whose rendering information
 * consists of a single point position, e.g., nodes.</P>
 *
 * <P>Technically, this class simply is a wrapper of the {@link Point}
 * class that makes it a subclass of {@link GeometryProxy}. The points in a
 * PointGeometryProxy object are stored and returned as references, so they
 * can be updated directly from outside.</P>
 *
 * @author Robi Malik
 */

public class PointGeometryProxy extends GeometryProxy {

  //#########################################################################
  //# Constructors
  /**
   * Creates a new point geometry.
   * @param  x           The initial x coordinate of the position.
   * @param  y           The initial y coordinate of the position.
   */
  public PointGeometryProxy(final int x, final int y)
  {
    this(new Point(x, y));
  }

  /**
   * Creates a new point geometry.
   * @param  point       The point position to be used. The new object
   *                     will use a reference to this object.
   */
  public PointGeometryProxy(final Point2D point)
  {
    mPoint = point;
  }

  /**
   * Creates a copy of a point geometry.
   * The copy will contain a new point object.
   * @param  partner     The object to be copied from.
   */
  public PointGeometryProxy(final PointGeometryProxy partner)
  {
    super(partner);
    mPoint = (Point2D) partner.mPoint.clone();
  }

  /**
   * Creates a point geometry from a parsed XML structure.
   * @param  geo         The parsed XML structure representing the
   *                     point geometry to be created.
   */
  PointGeometryProxy(final PointGeometryType geo)
  {
    final PointType geopoint = geo.getPoint();
    final int x = geopoint.getX();
    final int y = geopoint.getY();
    mPoint = new Point(x, y);
  }


  //#########################################################################
  //# Interface java.lang.Cloneable
  /**
   * Returns a copy of this point geometry.
   */
  public Object clone()
  {
    return new PointGeometryProxy(this);
  }


  //#########################################################################
  //# Equals and Hashcode
  public boolean equals(final Object partner)
  {
    if (super.equals(partner)) {
      final PointGeometryProxy geo = (PointGeometryProxy) partner;
      return getPoint().equals(geo.getPoint());
    } else {
      return false;
    }
  }


  //#########################################################################
  //# Getters and Setters
  /**
   * Gets the point identifying this PointGeometry. This method returns a
   * reference to the Point object used by the geometry object, so any
   * changes to it will immediately affect the geometry object.
   */
  public Point2D getPoint()
  {
    return mPoint;
  }

  /**
   * Sets the point identifying this PointGeometry to a new position.
   * @param  point       The point position to be used. The PointGeometry
   *                     will use a reference to this object.
   */
  public void setPoint(final Point2D point)
  {
    mPoint = point;
  }


  //#########################################################################
  //# Marshalling
  public void toJAXBElement(final ElementType element)
    throws JAXBException
  {
    super.toJAXBElement(element);
    if (element instanceof PointGeometryType) {
      final PointGeometryType geo = (PointGeometryType) element;
      final PointType point = createPoint(mPoint);
      geo.setPoint(point);
    }
  }

  PointGeometryType toPointGeometryType()
    throws JAXBException
  {
    final ElementFactory factory = new PlainPointGeometryElementFactory();
    return (PointGeometryType) toJAXB(factory);
  }

  PointGeometryType toStartPointGeometryType()
    throws JAXBException
  {
    final ElementFactory factory = new StartPointGeometryElementFactory();
    return (PointGeometryType) toJAXB(factory);
  }

  PointGeometryType toEndPointGeometryType()
    throws JAXBException
  {
    final ElementFactory factory = new EndPointGeometryElementFactory();
    return (PointGeometryType) toJAXB(factory);
  }


  //#########################################################################
  //# Local Class PointGeometryElementFactory
  private abstract static class PointGeometryElementFactory
    extends ModuleElementFactory
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.model.base.ElementFactory
    public ElementType createContainerElement()
      throws JAXBException
    {
      throw new UnsupportedOperationException
	("PointGeometry has no containing list!");
    }

    public List getElementList(final ElementType container)
    {
      throw new UnsupportedOperationException
	("PointGeometry has no containing list!");
    }

  }


  //#########################################################################
  //# Local Class PlainPointGeometryElementFactory
  private static class PlainPointGeometryElementFactory
    extends PointGeometryElementFactory
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.model.base.ElementFactory
    public ElementType createElement(final Proxy proxy)
      throws JAXBException
    {
      return getFactory().createPointGeometry();
    }

  }


  //#########################################################################
  //# Local Class StartPointGeometryElementFactory
  private static class StartPointGeometryElementFactory
    extends PointGeometryElementFactory
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.model.base.ElementFactory
    public ElementType createElement(final Proxy proxy)
      throws JAXBException
    {
      return getFactory().createStartPointGeometry();
    }

  }


  //#########################################################################
  //# Local Class EndPointGeometryElementFactory
  private static class EndPointGeometryElementFactory
    extends PointGeometryElementFactory
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.model.base.ElementFactory
    public ElementType createElement(final Proxy proxy)
      throws JAXBException
    {
      return getFactory().createEndPointGeometry();
    }

  }


  //#########################################################################
  //# Data Members
  private Point2D mPoint;

}
