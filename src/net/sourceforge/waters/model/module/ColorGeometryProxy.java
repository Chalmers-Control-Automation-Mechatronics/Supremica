//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.module
//# CLASS:   ColorGeometryProxy
//###########################################################################
//# $Id: ColorGeometryProxy.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.module;

import java.awt.Color;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.base.ElementFactory;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.xsd.base.ElementType;
import net.sourceforge.waters.xsd.module.ColorGeometryType;
import net.sourceforge.waters.xsd.module.ColorType;


/**
 * <P>A geometry object representing colour information.
 * This geometry object is used for objects whose rendering information
 * simply consists of a colour. Presently, this is used in event
 * declarations ({@link EventDeclProxy}) to provide rendering information
 * for events of type <I>proposition</I>.</P>
 *
 * <P>To support event lists, where a single entry can refer to several
 * events, a colour geometry object can contain several colours. Therefore,
 * the colour information is represented as a set of {@link java.awt.Color}
 * objects.</P>
 *
 * @author Robi Malik
 */

public class ColorGeometryProxy extends GeometryProxy {

  //#########################################################################
  //# Constructors
  /**
   * Creates a new colour geometry.
   * @param  colors      The set containing the colours representing the new
   *                     colour geometry.
   */
  public ColorGeometryProxy(final Set colors)
  {
    mColorSet = colors;
  }

  /**
   * Creates a new colour geometry representing a single colour.
   * @param  color       The single colour used by the new colour geometry.
   */
  public ColorGeometryProxy(final Color color)
  {
    mColorSet = new HashSet();
    mColorSet.add(color);
  }

  /**
   * Creates a colour geometry from a parsed XML structure.
   * @param  geo         The parsed XML structure representing the
   *                     colour geometry to be created.
   */
  ColorGeometryProxy(final ColorGeometryType geo)
  {
    final ColorType geocolor = geo.getColor();
    final int red = geocolor.getRed();
    final int green = geocolor.getGreen();
    final int blue = geocolor.getBlue();
    final Color color = new Color(red, green, blue);
    mColorSet = new HashSet();
    mColorSet.add(color);
  }


  //#########################################################################
  //# Equals and Hashcode
  public boolean equals(final Object partner)
  {
    if (super.equals(partner)) {
      final ColorGeometryProxy geo = (ColorGeometryProxy) partner;
      final boolean result = mColorSet.equals(geo.mColorSet);
      return result;
    } else {
      return false;
    }
  }


  //#########################################################################
  //# Getters and Setters
  /**
   * Gets the colour set identifying this colour geometry.
   * This method returns a modifiable set, so any
   * changes to it will immediately affect the geometry object.
   */
  public Set getColorSet()
  {
    return mColorSet;
  }


  //#########################################################################
  //# Marshalling
  public void toJAXBElement(final ElementType element)
    throws JAXBException
  {
    super.toJAXBElement(element);
    if (element instanceof ColorGeometryType) {
      final Iterator iter = mColorSet.iterator();
      final Color color = (Color) iter.next();
      if (iter.hasNext()) {
	throw new IllegalStateException
	  ("Can't marshall ColorGeometry with more than one colour!");
      }
      final int red = color.getRed();
      final int green = color.getGreen();
      final int blue = color.getBlue();
      final ColorGeometryType geo = (ColorGeometryType) element;
      final ColorType jaxbcolor = createColor(red, green, blue);
      geo.setColor(jaxbcolor);
    }
  }

  ColorGeometryType toColorGeometryType()
    throws JAXBException
  {
    final ElementFactory factory = new ColorGeometryElementFactory();
    return (ColorGeometryType) toJAXB(factory);
  }


  //#########################################################################
  //# Local Class ColorGeometryElementFactory
  private static class ColorGeometryElementFactory
    extends ModuleElementFactory
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.model.base.ElementFactory
    public ElementType createElement(final Proxy proxy)
      throws JAXBException
    {
      return getFactory().createColorGeometry();
    }

    public ElementType createContainerElement()
      throws JAXBException
    {
      throw new UnsupportedOperationException
	("ColorGeometry has no containing list!");
    }

    public List getElementList(final ElementType container)
    {
      throw new UnsupportedOperationException
	("ColorGeometry has no containing list!");
    }

  }


  //#########################################################################
  //# Data Members
  private final Set mColorSet;

}
