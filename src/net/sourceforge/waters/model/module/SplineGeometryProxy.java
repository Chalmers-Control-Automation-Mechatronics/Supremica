//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

package net.sourceforge.waters.model.module;

import java.util.List;
import java.awt.geom.Point2D;

import net.sourceforge.waters.model.base.GeometryProxy;


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
