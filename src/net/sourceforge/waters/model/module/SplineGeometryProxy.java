//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.module
//# CLASS:   SplineGeometryProxy
//###########################################################################
//# $Id: SplineGeometryProxy.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.module;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.awt.Point;
import java.awt.geom.Point2D;
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.base.ElementFactory;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.xsd.base.ElementType;
import net.sourceforge.waters.xsd.module.SplineKind;
import net.sourceforge.waters.xsd.module.PointType;
import net.sourceforge.waters.xsd.module.SplineGeometryType;


/**
 * <P>A geometry object representing the shape of a spline.</P>
 *
 * <P>Splines are defined by their list of control points that
 * define their shape. The start and end points of a spline are
 * not included, since they can be obtained from the position of the
 * nodes to which an edge connects.</P>
 * 
 * <P>Two types of splines are supported by the data structure:
 * <I>interpolating</I> splines which pass through their control points,
 * and <I>Bezier</I> splines.</P>
 *
 * @author Robi Malik
 */

public class SplineGeometryProxy extends GeometryProxy {

  //#########################################################################
  //# Constructors
  /**
   * Creates a new interpolating spline geometry.
   * @param  points      The list of control points to be used. The new object
   *                     will use a reference to this list.
   */
  public SplineGeometryProxy(final List points)
  {
    this(points, SplineKind.INTERPOLATING);
  }

  /**
   * Creates a new spline geometry.
   * @param  points      The list of control points to be used. The new object
   *                     will use a reference to this list.
   * @param  kind        The type of spline, {@link SplineKind#INTERPOLATING}
   *                     or {@link SplineKind#BEZIER}.
   */
  public SplineGeometryProxy(final List points, final SplineKind kind)
  {
    mPoints = points;
    mKind = kind;
  }

  /**
   * Creates a copy of a spline geometry.
   * The copy will contain new point objects.
   * @param  partner     The object to be copied from.
   */
  public SplineGeometryProxy(final SplineGeometryProxy partner)
  {
    super(partner);
    final List points = partner.mPoints;
    final Iterator iter = points.iterator();
    mPoints = new ArrayList(points.size());
    while (iter.hasNext()) {
      final Point2D point = (Point2D) iter.next();
      mPoints.add(point.clone());
    }
  }

  /**
   * Creates a spline geometry from a parsed XML structure.
   * @param  geo         The parsed XML structure representing the
   *                     spline geometry to be created.
   */
  SplineGeometryProxy(final SplineGeometryType geo)
  {
    final List geopoints = geo.getPoints();
    final Iterator iter = geopoints.iterator();
    mPoints = new ArrayList(geopoints.size());
    while (iter.hasNext()) {
      final PointType geopoint = (PointType) iter.next();
      final int x = geopoint.getX();
      final int y = geopoint.getY();
      final Point2D point = new Point(x, y);
      mPoints.add(point);
    }
    mKind = geo.getKind();
  }


  //#########################################################################
  //# Interface java.lang.Cloneable
  /**
   * Returns a copy of this spline geometry.
   */
  public Object clone()
  {
    return new SplineGeometryProxy(this);
  }


  //#########################################################################
  //# Equals and Hashcode
  public boolean equals(final Object partner)
  {
    if (partner != null && getClass() == partner.getClass() &&
	super.equals(partner)) {
      final SplineGeometryProxy geo = (SplineGeometryProxy) partner;
      return
	getPoints().equals(geo.getPoints()) &&
	getKind().equals(geo.getKind());
    } else {
      return false;
    }
  }


  //#########################################################################
  //# Getters and Setters
  /**
   * Gets the list of control points of this spline geometry. This method
   * returns a reference to the list used by the geometry object, so any
   * changes to it will immediately affect the geometry object.
   * @return A modifiable list of {@link Point2D} objects.
   */
  public List getPoints()
  {
    return mPoints;
  }

  /**
   * Gets the spline type.
   * @return {@link SplineKind#INTERPOLATING} or {@link SplineKind#BEZIER}.
   */
  public SplineKind getKind()
  {
    return mKind;
  }

  /**
   * Sets the spline type.
   */
  public void setKind(final SplineKind kind)
  {
    mKind = kind;
  }


  //#########################################################################
  //# Marshalling
  public void toJAXBElement(final ElementType element)
    throws JAXBException
  {
    super.toJAXBElement(element);
    if (element instanceof SplineGeometryType) {
      final SplineGeometryType geo = (SplineGeometryType) element;
      final List geopoints = geo.getPoints();
      final Iterator iter = mPoints.iterator();
      while (iter.hasNext()) {
	final Point2D point = (Point2D) iter.next();
	final PointType geopoint = createPoint(point);
	geopoints.add(geopoint);
      }
      geo.setKind(mKind);
    }
  }

  public SplineGeometryType toSplineGeometryType()
    throws JAXBException
  {
    final ElementFactory factory = new SplineGeometryElementFactory();
    return (SplineGeometryType) toJAXB(factory);
  }


  //#########################################################################
  //# Data Members
  private final List mPoints;
  private SplineKind mKind;


  //#########################################################################
  //# Local Class SplineGeometryElementFactory
  private static class SplineGeometryElementFactory
    extends ModuleElementFactory
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.model.base.ElementFactory
    public ElementType createElement(final Proxy proxy)
      throws JAXBException
    {
      return getFactory().createSplineGeometry();
    }

    public ElementType createContainerElement()
      throws JAXBException
    {
      throw new UnsupportedOperationException
	("SplineGeometry has no containing list!");
    }

    public List getElementList(final ElementType container)
    {
      throw new UnsupportedOperationException
	("SplineGeometry has no containing list!");
    }

  }
}
