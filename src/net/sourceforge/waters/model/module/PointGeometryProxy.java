//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.module
//# CLASS:   PointGeometryProxy
//###########################################################################
//# $Id: PointGeometryProxy.java,v 1.2 2005-11-03 01:24:16 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.module;

import java.awt.geom.Point2D;

import net.sourceforge.waters.model.base.GeometryProxy;


/**
 * <P>A geometry object representing a single point.</P>
 *
 * <P>This geometry object is used for objects whose rendering information
 * consists of a single point position, e.g., nodes.</P>
 *
 * <P>Technically, this class simply is a wrapper of a {@link Point2D}
 * object that makes it accessible in a subclass of {@link GeometryProxy}.</P>
 *
 * @author Robi Malik
 */

public interface PointGeometryProxy extends GeometryProxy {


  //#########################################################################
  //# Getters and Setters
  /**
   * Gets the point identifying this PointGeometry. This method returns a
   * reference to the Point object used by the geometry object, so any
   * changes to it will immediately affect the geometry object.
   */
  public Point2D getPoint();

}
