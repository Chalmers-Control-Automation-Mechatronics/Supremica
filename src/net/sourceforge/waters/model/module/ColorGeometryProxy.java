//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.module
//# CLASS:   ColorGeometryProxy
//###########################################################################
//# $Id: ColorGeometryProxy.java,v 1.2 2005-11-03 01:24:16 robi Exp $
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
