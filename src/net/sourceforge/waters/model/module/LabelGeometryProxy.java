//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.module
//# CLASS:   LabelGeometryProxy
//###########################################################################
//# $Id: LabelGeometryProxy.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.module;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.List;
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.base.ElementFactory;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.xsd.base.ElementType;
import net.sourceforge.waters.xsd.module.AnchorPosition;
import net.sourceforge.waters.xsd.module.PointType;
import net.sourceforge.waters.xsd.module.LabelGeometryType;


/**
 * <P>A geometry object representing a label position.</P>
 *
 * <P>Labels are text boxes positioned relative to the item (node or edge)
 * they belong to. Since the size of a text box depends on font sizes, its
 * precise size cannot be predicted easily. To support more appealing
 * layout on different platforms, each label geometry also has an anchor
 * position that defines which point of the text box is defined by the
 * offset.</P>
 *
 * <P>To render a label appropriately, we first have to identify the
 * position&nbsp;<I>p</I> of the object that is labelled:</P>
 * <UL>
 * <LI>For nodes, <I>p</I> is the center of the node circle.</LI>
 * <LI>For straight edges, <I>p</I> is the center point of the edge.</LI>
 * <LI>For edges with a single control point, <I>p</I> is the position
 *     of the control point.</LI>
 * </UL>
 * <P>Having determined position&nbsp;<I>p</I>, we add the label geometry's
 * offset to <I>p</I> to obtain the position of the label box's anchor point.
 * To position the label box, we then have to determine its size using
 * the fonts to be used. Then the label box is positioned such that the
 * anchor point is a the appropriate position:</P>
 * <UL>
 * <LI>{@link AnchorPosition#NW} - The anchor point is at the top left
 *     corner of the label box.</LI>
 * <LI>{@link AnchorPosition#N} - The anchor point is at the center of
 *     the top edge of the label box.</LI>
 * <LI>{@link AnchorPosition#NE} - The anchor point is at the top right
 *     corner of the label box.</LI>
 * <LI>{@link AnchorPosition#W} - The anchor point is at the center of
 *     the left edge of the label box.</LI>
 * <LI>{@link AnchorPosition#C} - The anchor point is at the center of
 *     the label box.</LI>
 * <LI>{@link AnchorPosition#E} - The anchor point is at the center of
 *     the right edge of the label box.</LI>
 * <LI>{@link AnchorPosition#SW} - The anchor point is at the bottom left
 *     corner of the label box.</LI>
 * <LI>{@link AnchorPosition#S} - The anchor point is at the center of
 *     the bottom edge of the label box.</LI>
 * <LI>{@link AnchorPosition#SE} - The anchor point is at the bottom right
 *     corner of the label box.</LI>
 * </UL>
 *
 * @author Robi Malik
 */


public class LabelGeometryProxy extends GeometryProxy {

  //#########################################################################
  //# Constructors
  /**
   * Creates a new label geometry.
   * @param  x           The initial x offset of the position.
   * @param  y           The initial y offset of the position.
   */
  public LabelGeometryProxy(final int x, final int y)
  {
    this(new Point(x, y));
  }

  /**
   * Creates a new label geometry.
   * @param  x           The initial x offset of the position.
   * @param  y           The initial y offset of the position.
   * @param  anchor      The anchor position for the text box.
   */
  public LabelGeometryProxy(final int x,
			    final int y,
			    final AnchorPosition anchor)
  {
    this(new Point(x, y), anchor);
  }

  /**
   * Creates a new label geometry.
   * @param  offset      The point to be used to represent the offset
   *                     of the label's relative to the item being labelled.
   */
  public LabelGeometryProxy(final Point2D offset)
  {
    this(offset, AnchorPosition.SW);
  }

  /**
   * Creates a new label geometry.
   * @param  offset      The point to be used to represent the displacement
   *                     of the label's relative to the item being labelled.
   * @param  anchor      The anchor position for the text box.
   */
  public LabelGeometryProxy(final Point2D offset, final AnchorPosition anchor)
  {
    mOffset = offset;
    mAnchor = anchor;
  }

  /**
   * Creates a copy of a label geometry.
   * The copy will contain a new point object.
   * @param  partner     The geometry object to be copied.
   */
  public LabelGeometryProxy(final LabelGeometryProxy partner)
  {
    mOffset = (Point2D) partner.mOffset.clone();
    mAnchor = partner.mAnchor;
  }

  /**
   * Creates a label geometry from a parsed XML structure.
   * @param  geo         The parsed XML structure representing the
   *                     label geometry to be created.
   */
  LabelGeometryProxy(final LabelGeometryType geo)
  {
    final PointType geopoint = geo.getPoint();
    final int x = geopoint.getX();
    final int y = geopoint.getY();
    mOffset = new Point(x, y);
    mAnchor = geo.getAnchor();
  }


  //#########################################################################
  //# Interface java.lang.Cloneable
  /**
   * Returns a copy of this label geometry.
   */
  public Object clone()
  {
    return new LabelGeometryProxy(this);
  }


  //#########################################################################
  //# Equals and Hashcode
  public boolean equals(final Object partner)
  {
    if (partner != null && getClass() == partner.getClass() &&
	super.equals(partner)) {
      final LabelGeometryProxy geo = (LabelGeometryProxy) partner;
      return
	getOffset().equals(geo.getOffset()) &&
	getAnchor().equals(geo.getAnchor());
    } else {
      return false;
    }
  }


  //#########################################################################
  //# Getters and Setters
  /**
   * Gets the offset of this label geometry.
   * The offset defines the difference of the label's anchor position
   * with respect to the labelled item.
   * This method returns a reference to the point object used by the
   * geometry object, so any changes to it will immediately affect the
   * geometry object.
   */
  public Point2D getOffset()
  {
    return mOffset;
  }

  /**
   * Sets the offset for this label geometry.
   */
  public void setOffset(final Point2D offset)
  {
    mOffset = offset;
  }

  /**
   * Gets the anchor position of this label geometry.
   * The anchor position defines which point of the label's text box is
   * defined by the label position.
   */
  public AnchorPosition getAnchor()
  {
    return mAnchor;
  }

  /**
   * Sets the anchor position for this label geometry.
   */
  public void setAnchor(final AnchorPosition anchor)
  {
    mAnchor = anchor;
  }


  //#########################################################################
  //# Marshalling
  public void toJAXBElement(final ElementType element)
    throws JAXBException
  {
    super.toJAXBElement(element);
    if (element instanceof LabelGeometryType) {
      final LabelGeometryType geo = (LabelGeometryType) element;
      final PointType point = createPoint(mOffset);
      geo.setPoint(point);
      geo.setAnchor(mAnchor);
    }
  }

  public LabelGeometryType toLabelGeometryType()
    throws JAXBException
  {
    final ElementFactory factory = new LabelGeometryElementFactory();
    return (LabelGeometryType) toJAXB(factory);
  }


  //#########################################################################
  //# Local Class LabelGeometryElementFactory
  private static class LabelGeometryElementFactory
    extends ModuleElementFactory
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.model.base.ElementFactory
    public ElementType createElement(final Proxy proxy)
      throws JAXBException
    {
      return getFactory().createLabelGeometry();
    }

    public ElementType createContainerElement()
      throws JAXBException
    {
      throw new UnsupportedOperationException
	("LabelGeometry has no containing list!");
    }

    public List getElementList(final ElementType container)
    {
      throw new UnsupportedOperationException
	("LabelGeometry has no containing list!");
    }

  }


  //#########################################################################
  //# Data Members
  private Point2D mOffset;
  private AnchorPosition mAnchor;

}
