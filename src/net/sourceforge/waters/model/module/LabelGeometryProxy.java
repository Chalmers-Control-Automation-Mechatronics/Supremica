//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.module
//# CLASS:   LabelGeometryProxy
//###########################################################################
//# $Id: LabelGeometryProxy.java,v 1.2 2005-11-03 01:24:16 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.module;

import java.awt.geom.Point2D;

import net.sourceforge.waters.model.base.GeometryProxy;

import net.sourceforge.waters.xsd.module.AnchorPosition;


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


public interface LabelGeometryProxy extends GeometryProxy {

  //#########################################################################
  //# Getters
  /**
   * Gets the offset of this label geometry.
   * The offset defines the difference of the label's anchor position
   * with respect to the labelled item.
   * This method returns a reference to the point object used by the
   * geometry object, so any changes to it will immediately affect the
   * geometry object.
   */
  public Point2D getOffset();

  /**
   * Gets the anchor position of this label geometry.
   * The anchor position defines which point of the label's text box is
   * defined by the label position.
   */
  // @default AnchorPosition.NW
  public AnchorPosition getAnchor();

}
