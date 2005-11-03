//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.module
//# CLASS:   SplineGeometryProxy
//###########################################################################
//# $Id: SplineGeometryProxy.java,v 1.2 2005-11-03 01:24:16 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.module;

import java.util.List;
import java.awt.geom.Point2D;

import net.sourceforge.waters.model.base.GeometryProxy;

import net.sourceforge.waters.xsd.module.SplineKind;


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

public interface SplineGeometryProxy extends GeometryProxy {


  //#########################################################################
  //# Getters and Setters
  /**
   * Gets the list of control points of this spline geometry. This method
   * returns a reference to the list used by the geometry object, so any
   * changes to it will immediately affect the geometry object.
   * @return A modifiable list of {@link Point2D} objects.
   */
  public List<Point2D> getPoints();

  /**
   * Gets the spline type.
   * @return {@link SplineKind#INTERPOLATING} or {@link SplineKind#BEZIER}.
   */
  // @default SplineKind.INTERPOLATING
  public SplineKind getKind();

}
