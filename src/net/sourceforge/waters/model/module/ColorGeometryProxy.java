//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
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

import java.awt.Color;
import java.util.Set;

import net.sourceforge.waters.model.base.GeometryProxy;


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

public interface ColorGeometryProxy extends GeometryProxy {


  //#########################################################################
  //# Getters and Setters
  /**
   * Gets the colour set identifying this colour geometry.
   * This method returns a modifiable set, so any
   * changes to it will immediately affect the geometry object.
   */
  public Set<Color> getColorSet();

}
