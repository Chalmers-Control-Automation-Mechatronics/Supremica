//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.module
//# CLASS:   BoxGeometryProxy
//###########################################################################
//# $Id: BoxGeometryProxy.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.module;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.List;
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.base.ElementFactory;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.xsd.base.ElementType;
import net.sourceforge.waters.xsd.module.BoxType;
import net.sourceforge.waters.xsd.module.BoxGeometryType;


/**
 * <P>A geometry object representing a rectangle.</P>
 *
 * <P>This geometry object is used for objects that are represented
 * graphically as a rectangle, i.e, group nodes ({@link GroupNodeProxy}).</P>
 *
 * <P>Technically, this class simply is a wrapper of the {@link
 * Rectangle2D} class that makes it a subclass of {@link
 * GeometryProxy}. The rectangles in a <CODE>BoxGeometryProxy</CODE> object
 * are stored and returned as references, so they can be updated directly
 * from outside.</P>
 *
 * @author Robi Malik
 */

public class BoxGeometryProxy extends GeometryProxy {

  //#########################################################################
  //# Constructors
  /**
   * Creates a new box geometry.
   * @param  x           The x coordinate of the top left corner of the box.
   * @param  y           The y coordinate of the top left corner of the box.
   * @param  width       The initial width of the box.
   * @param  height      The initial height of the box.
   */
  public BoxGeometryProxy(final int x,
			  final int y,
			  final int width,
			  final int height)
  {
    this(new Rectangle(x, y, width, height));
  }

  /**
   * Creates a new box geometry.
   * @param  rect        The rectangle to be used. The new object
   *                     will use a reference to this object.
   */
  public BoxGeometryProxy(final Rectangle2D rect)
  {
    mRectangle = rect;
  }

  /**
   * Creates a copy of a box geometry.
   * The copy will contain a new rectangle object.
   * @param  partner     The object to be copied from.
   */
  public BoxGeometryProxy(final BoxGeometryProxy partner)
  {
    super(partner);
    mRectangle = (Rectangle2D) partner.mRectangle.clone();
  }

  /**
   * Creates a box geometry from a parsed XML structure.
   * @param  geo         The parsed XML structure representing the
   *                     box geometry to be created.
   */
  BoxGeometryProxy(final BoxGeometryType geo)
  {
    final BoxType box = geo.getBox();
    final int x = box.getX();
    final int y = box.getY();
    final int width = box.getWidth();
    final int height = box.getHeight();
    mRectangle = new Rectangle(x, y, width, height);
  }


  //#########################################################################
  //# Interface java.lang.Cloneable
  /**
   * Returns a copy of this box geometry.
   */
  public Object clone()
  {
    return new BoxGeometryProxy(this);
  }


  //#########################################################################
  //# Equals and Hashcode
  public boolean equals(final Object partner)
  {
    if (super.equals(partner)) {
      final BoxGeometryProxy geo = (BoxGeometryProxy) partner;
      return mRectangle.equals(geo.mRectangle);
    } else {
      return false;
    }
  }


  //#########################################################################
  //# Getters and Setters
  /**
   * Gets the rectangle identifying this box geometry. This method returns a
   * reference to the Rectangle object used by the geometry object, so any
   * changes to it will immediately affect the geometry object.
   */
  public Rectangle2D getRectangle()
  {
    return mRectangle;
  }

  public void setRectangle(final Rectangle2D rect)
  {
    mRectangle = rect;
  }



  //#########################################################################
  //# Marshalling
  public void toJAXBElement(final ElementType element)
    throws JAXBException
  {
    super.toJAXBElement(element);
    if (element instanceof BoxGeometryType) {
      final BoxGeometryType geo = (BoxGeometryType) element;
      final BoxType box = createBox(mRectangle);
      geo.setBox(box);
    }
  }

  public BoxGeometryType toBoxGeometryType()
    throws JAXBException
  {
    final ElementFactory factory = new BoxGeometryElementFactory();
    return (BoxGeometryType) toJAXB(factory);
  }


  //#########################################################################
  //# Local Class BoxGeometryElementFactory
  private static class BoxGeometryElementFactory
    extends ModuleElementFactory
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.model.base.ElementFactory
    public ElementType createElement(final Proxy proxy)
      throws JAXBException
    {
      return getFactory().createBoxGeometry();
    }

    public ElementType createContainerElement()
      throws JAXBException
    {
      throw new UnsupportedOperationException
	("BoxGeometry has no containing list!");
    }

    public List getElementList(final ElementType container)
    {
      throw new UnsupportedOperationException
	("BoxGeometry has no containing list!");
    }

  }


  //#########################################################################
  //# Data Members
  private Rectangle2D mRectangle;

}
