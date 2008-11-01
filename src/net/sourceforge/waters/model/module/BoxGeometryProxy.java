//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.module
//# CLASS:   BoxGeometryProxy
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.module;

import java.awt.geom.Rectangle2D;

import net.sourceforge.waters.model.base.GeometryProxy;


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

public interface BoxGeometryProxy extends GeometryProxy {


  //#########################################################################
  //# Getters and Setters
  /**
   * Gets the rectangle identifying this box geometry. This method returns a
   * reference to the Rectangle object used by the geometry object, so any
   * changes to it will immediately affect the geometry object.
   */
  public Rectangle2D getRectangle();

}
