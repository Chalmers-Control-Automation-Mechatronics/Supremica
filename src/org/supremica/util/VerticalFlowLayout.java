
// Sample code file: VerticalFlowLayout.java
// Warning: This code has been marked up for HTML

/*===========================================================================
 $Workfile:   VerticalFlowLayout.java  $
 $Revision: 1.4 $
 $Date: 2004-06-11 21:12:52 $

 (C) Unpublished Copyright of Novell, Inc.  All Rights Reserved.

 No part of this file may be duplicated, revised, translated, localized or
 modified in any manner or compiled, linked, uploaded or downloaded to or
 from any computer system without the prior written consent of Novell, Inc.

 (c) Copyright 1997-1999 Novell, Inc.  All Rights Reserved.
===========================================================================*/

//package com.novell.utility.layouts;
package org.supremica.util;

// Sun
import java.awt.*;
import java.util.Enumeration;

//import com.novell.application.console.shell.*;
// import com.objectspace.jgl.adapters.*;       // VectorArray
import java.util.Vector;

/**
 * This layout is used to vertically line components up in a column.  It's
 * like AWT's FlowLayout, but it works vertically.  It's ideal for lining
 * buttons on a panel vertically down one side.
 * <p>
 * This layout gives you a lot more control than FlowLayout does in that you
 * can:
 * <UL>
 * <LI> Align the components along a left, center, or right axis </LI>
 * <LI> Size the components to all have the same widths (which is handy
 * for buttons) or default to their various preferred widths </LI>
 * <LI> Set external horizontal and vertical paddings around the column of
 * components </LI>
 * <LI> Set internal horiztonal and vertical paddings within each component
 * to uniformly increase their size beyond their preferred size
 * </UL>
 * <p>
 * This layout will not display all of the components if there isn't
 * enough room vertically to show them. If a component can be partially show,
 * it will be partially shown, but those beyond the bottom boundary will not
 * appear at all (this is the same behavior that FlowLayout has when there
 * isn't enough horizontal room to display components).
 * <p>
 * When you add a component to a container with the VerticalFlowLayout
 * layout manager, be sure to specify a string for where to add the
 * component, for example:
 * <pre>
 *    Panel p = new Panel();
 *    p.setLayout(new VerticalFlowLayout());
 *    p.add(new Button("OK"), "Center"); // defaults to center if you don't
 *                                       // provide a constraint
 * </pre>
 *
 * @version 1.0, 7/2/97
 * @author Lee Lowry
 */
public class VerticalFlowLayout
	implements LayoutManager2, java.io.Serializable
{

	//----------------------------------
	// Alignment Values
	//----------------------------------

	/**
	 * The center layout contraint.
	 */
	public static final String CENTER = "Center";

	/**
	 * The left layout contraint.
	 */
	public static final String LEFT = "Left";

	/**
	 * The right layout contraint.
	 */
	public static final String RIGHT = "Right";

	//************************************************************************
	// Data
	//************************************************************************
	private Vector /* Array */ m_components;
	private boolean m_bUniformWidths;
	private int m_vGap;
	private int m_externalPadLeft;
	private int m_externalPadRight;
	private int m_externalPadTop;
	private int m_externalPadBottom;
	private int m_internalPadX;
	private int m_internalPadY;
	private int m_widestWidth;
	private Dimension m_preferredSize;
	private Insets m_insets;

	/**
	 * Dirty flag, signaling that preferred sizes need to be recalculated
	 * because a component has been added or removed.
	 */
	private boolean m_bDirty = false;

	//----------------------------------
	// Inner classes
	//----------------------------------

	/**
	 * This class pairs up a component with an alignment value. Each component
	 * in the VerticalFlowLayout can be aligned to the Left, Center, or Right.
	 */
	private class AlignedComponent
	{
		Component m_comp;
		String m_alignment;

		public AlignedComponent(Component comp, String alignment)
		{
			m_comp = comp;
			m_alignment = alignment;
		}

		public Component getComponent()
		{
			return m_comp;
		}

		public String getAlignment()
		{
			return m_alignment;
		}
	}    //class AlignedComponent

	//************************************************************************
	// Constructors
	//************************************************************************

	/**
	 * Constructs a new VerticalFlowLayout.
	 */
	public VerticalFlowLayout()
	{
		m_bUniformWidths = true;
		m_vGap = 5;
		m_externalPadLeft = 0;
		m_externalPadRight = 0;
		m_externalPadTop = 0;
		m_externalPadBottom = 0;
		m_internalPadX = 0;
		m_internalPadY = 0;
		m_components = new Vector /* Array */();
	}

	/**
	 * Constructs a new VerticalFlowLayout with your own width sizing
	 * type and vertical gap values.
	 *
	 * @param uniformWidths size the widths of the component to be uniform
	 * or not
	 * @param vGap vertical gap
	 */
	public VerticalFlowLayout(boolean uniformWidths, int vGap)
	{
		m_bUniformWidths = uniformWidths;
		m_vGap = vGap;
		m_components = new Vector /* Array */();
	}

	//************************************************************************
	// Methods
	//************************************************************************

	/**
	 * Adds the specified component to the layout.
	 *
	 * @param name this is ignored
	 * @param comp the component to be added
	 * @exception IllegalArgumentException Invalid component or constraint.
	 */
	public void addLayoutComponent(String name, Component comp)
	{

		// Make sure we have a valid component
		if (comp == null)
		{

			// No, not valid
			throw new IllegalArgumentException("Cannot add component: component is null.");
		}

		// Special case: treat null the same as "Left"
		if ((name == null) || (name.length() == 0))
		{

			//D.out ("Assuming Left Layout");
			name = LEFT;
		}

		// Make sure we have a valid constraint
		if (name.equals(CENTER) || name.equals(LEFT) || name.equals(RIGHT))
		{

			// Yes, valid
			AlignedComponent alcomp = new AlignedComponent(comp, name);

			m_components.add(alcomp);
		}
		else
		{

			// No, not valid
			throw new IllegalArgumentException("Cannot add component: constraint is invalid.");
		}

		// This signals the preferredLayoutSize to recalculate based on this
		// new addition
		m_bDirty = true;
	}

	/**
	 * Remove the specified component.
	 *
	 * @param comp the component to be removed
	 * @exception IllegalArgumentException Invalid component.
	 */
	public void removeLayoutComponent(Component comp)
	{
		if (comp != null)
		{
			Enumeration e = m_components.elements();

			if (e != null)
			{
				while (e.hasMoreElements())
				{

					// Get the component/alignment pair
					AlignedComponent alcomp = (AlignedComponent) e.nextElement();

					// Pull just the component out and see if there's a match
					Component currComp = alcomp.getComponent();

					if (comp == currComp)
					{

						// Found the component, get rid of it
						m_components.remove(alcomp);

						// This signals the preferredLayoutSize to recalculate based
						// on this deletion
						m_bDirty = true;

						break;
					}
				}
			}
		}
		else
		{
			throw new IllegalArgumentException("Cannot remove component: component is null.");
		}
	}

	/**
	 * Gets the minimum dimensions needed to lay out the component
	 * contained in the specified target container.
	 *
	 * @param parent the Container on which to do the layout
	 * @see Container
	 * @see #preferredLayoutSize
	 * @return minimum layout size
	 */
	public Dimension minimumLayoutSize(Container parent)
	{
		return parent.getSize();
	}

	/**
	 * Gets the preferred dimensions for this layout given the components
	 * in the specified target container.
	 * <UL>
	 * <LI>For the horizontal size, this will go through all of the components
	 * and get the widest one and add the external and internal horizontal
	 * paddings.</LI>
	 * <LI>For the vertical size, this will add up the heights of all of the
	 * components and add in the internal and external vertical paddings.</UL>
	 *
	 * @param parent the Container on which to do the layout
	 * @see Container
	 * @see #minimumLayoutSize
	 * @return preferred preferred layout size
	 */
	public Dimension preferredLayoutSize(Container parent)
	{

		// see if insets/border has changed.  If so, recompute
		if ((m_insets == null) || (m_insets != parent.getInsets()))
		{
			m_bDirty = true;
		}

		// if new components, recalculate preferred size
		if ((m_preferredSize == null) || m_bDirty)
		{
			int height = 0;

			m_widestWidth = 0;
			m_preferredSize = null;

			// compute size of any border/insets
			int xx = 0;
			int yy = 0;

			m_insets = parent.getInsets();

			if (m_insets != null)
			{
				xx = m_insets.left + m_insets.right;
				yy = m_insets.bottom + m_insets.top;
			}

			//D.out("Parent container insets: "+insets.toString());
			// Go through all of the components and add things up
			Enumeration e = m_components.elements();

			if (e != null)
			{
				while (e.hasMoreElements())
				{

					// Get component/alignment pair
					AlignedComponent alcomp = (AlignedComponent) e.nextElement();

					// Pull just the component out
					Dimension compSize = alcomp.getComponent().getPreferredSize();
					int width = compSize.width;

					if (width > m_widestWidth)
					{

						// Keep track of the widest component
						m_widestWidth = width;
					}

					// Add the height, padding, and vertical gap to the
					// overall height tally
					height += compSize.height + (m_internalPadY * 2) + m_vGap;
				}

				// Everything is tallied; now create a new preferred size
				// figuring in the external padding.
				m_preferredSize = new Dimension(m_widestWidth + m_externalPadLeft + m_externalPadRight + (m_internalPadX * 2), height + m_externalPadTop + m_externalPadBottom);

				// add in any insets
				m_preferredSize.width += xx;
				m_preferredSize.height += yy;
			}
			else
			{

				// just set preferred size to size of border.
				m_preferredSize = new Dimension(xx, yy);
			}

			// Indicate preferred size is valid
			m_bDirty = false;
		}    //recompute preferred size

		//D.out("Preferred size for "+parent.getName()+":  "+m_preferredSize.toString());
		return m_preferredSize;
	}    //preferredLayoutSize

	/**
	 * Layout the components within the specified container
	 *
	 * @param parent the container that is being layed out
	 * @see Container
	 */
	public void layoutContainer(Container parent)
	{
		int yPos = m_externalPadTop;
		int yBot = m_externalPadBottom;
		int xPos = m_externalPadLeft;
		int xRight = m_externalPadRight;
		int compWidth = 0;
		int parentWidth = 0;
		int parentHeight = 0;

		// see if insets/border has changed.  If so, recompute
		if ((m_insets == null) || (m_insets != parent.getInsets()))
		{
			m_bDirty = true;
		}

		// get preferred size
		if (m_bDirty)
		{
			preferredLayoutSize(parent);
		}

		// get parent container's minimum size
		Dimension d = parent.getMinimumSize();

		if (d != null)
		{
			parentWidth = d.width;
			parentHeight = d.height;
		}

		// adjust starting pos for insets
		if (m_insets != null)
		{
			yPos += m_insets.top;
			yBot += m_insets.bottom;
			xPos += m_insets.left;
			xRight += m_insets.right;
		}

		// Go through all of the components and place them vertically.
		Enumeration e = m_components.elements();

		if (e != null)
		{
			while (e.hasMoreElements())
			{
				AlignedComponent alcomp = (AlignedComponent) e.nextElement();
				Component comp = alcomp.getComponent();
				String alignment = alcomp.getAlignment();

				if (comp.isVisible())
				{

					// compute height for this component = preferred height + 2*internal pady
					int height = comp.getPreferredSize().height + (m_internalPadY * 2);

					// compute width for this component + 2*m_internalPadX
					if (m_bUniformWidths == true)
					{

						// get the width of the widest component
						compWidth = m_widestWidth + (m_internalPadX * 2);
					}
					else
					{

						// compute width based on preferred size
						compWidth = comp.getPreferredSize().width + (m_internalPadX * 2);
					}

					if ((alignment.equals(LEFT)) || (parentWidth == 0) || (compWidth > parentWidth))
					{

						// left margin
						comp.setLocation(xPos, yPos);
					}
					else if (alignment.equals(RIGHT))
					{

						// right margin
						comp.setLocation(parentWidth - xRight - compWidth, yPos);
					}
					else
					{

						// centered between margins
						int centerPos = xPos + (parentWidth - xPos - xRight) / 2;

						comp.setLocation(centerPos - (compWidth / 2), yPos);
					}

					comp.setSize(compWidth, height);

					yPos += height + m_vGap;

					//D.out("alignment = "+alignment+"   height = "+height+"   width = "+compWidth+"   xpos = "+xPos+"   ypos = "+yPos);
				}
			}
		}
	}    //layoutContainer

	/**
	 * Adds the specified component to the layout, using the specified
	 * constraint object.
	 * @param comp the component to be added
	 * @param constraints  where/how the component is added to the layout.
	 */
	public void addLayoutComponent(Component comp, Object constraints)
	{
		if ((constraints == null) || (constraints instanceof String))
		{
			addLayoutComponent((String) constraints, comp);
		}
		else
		{
			throw new IllegalArgumentException("cannot add to layout: constraint must be a string (or null)");
		}
	}

	/**
	 * Returns the maximum size of this component.
	 * @see java.awt.Component#getMinimumSize()
	 * @see java.awt.Component#getPreferredSize()
	 * @see LayoutManager
	 */
	public Dimension maximumLayoutSize(Container target)
	{
		return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
	}

	/**
	 * Returns the alignment along the x axis.  This specifies how
	 * the component would like to be aligned relative to other
	 * components.  The value should be a number between 0 and 1
	 * where 0 represents alignment along the origin, 1 is aligned
	 * the furthest away from the origin, 0.5 is centered, etc.
	 */
	public float getLayoutAlignmentX(Container target)
	{
		return 0.5f;
	}

	/**
	 * Returns the alignment along the y axis.  This specifies how
	 * the component would like to be aligned relative to other
	 * components.  The value should be a number between 0 and 1
	 * where 0 represents alignment along the origin, 1 is aligned
	 * the furthest away from the origin, 0.5 is centered, etc.
	 */
	public float getLayoutAlignmentY(Container target)
	{
		return 0.5f;
	}

	/**
	 * Invalidates the layout, indicating that if the layout manager
	 * has cached information it should be discarded.
	 */
	public void invalidateLayout(Container target) {}

	//************************************************************************
	// Accessor methods
	//************************************************************************

	/**
	 * See if the widths are set to uniform.
	 *
	 * @return uniform width flag
	 */
	public boolean areWidthsUniform()
	{
		return m_bUniformWidths;
	}

	/**
	 * Get the vertical gap value.
	 *
	 * @return vertical gap
	 */
	public int getVerticalGap()
	{
		return m_vGap;
	}

	/**
	 * Returns the left external horizontal padding.  This is the space
	 * between the widest component in the column and the left edge.
	 *
	 * @return Left external horizontal padding.
	 */
	public int getExternalPadLeft()
	{
		return m_externalPadLeft;
	}

	/**
	 * Returns the right external horizontal padding.  This is the space
	 * between the widest component in the column and the left right edge.
	 *
	 * @return Right external horizontal padding.
	 */
	public int getExternalPadRight()
	{
		return m_externalPadRight;
	}

	/**
	 * Returns the top external vertical padding.  This is the space
	 * between the top component and the top edge.
	 *
	 * @return Top external vertical padding.
	 */
	public int getExternalPadTop()
	{
		return m_externalPadTop;
	}

	/**
	 * Returns the bottom external vertical padding.  This is the space
	 * between the bottom component and the bottom edge.
	 *
	 * @return Bottom external vertical padding.
	 */
	public int getExternalPadBottom()
	{
		return m_externalPadBottom;
	}

	/**
	 * Get the internal horizontal padding.  This is a simple way to grow
	 * the horizontal preferred size of all of the components by a certain
	 * amount.  It works great on buttons, because their preferred size
	 * is usually not wide enough to look good.
	 *
	 * @return internal horizontal padding
	 */
	public int getInternalPadX()
	{
		return m_internalPadX;
	}

	/**
	 * Get the internal vertical padding.  This is a simple way to grow
	 * the vertical preferred size of all of the components by a certain
	 * amount.  It works great on buttons, because their preferred size
	 * is usually too short.
	 *
	 * @return internal vertical padding
	 */
	public int getInternalPadY()
	{
		return m_internalPadY;
	}

	/**
	 * Set the uniform width flag. If you set this to true, the alignment
	 * value is ignored.
	 *
	 * @param uniformWidths
	 * <UL>
	 * <LI>true  = make all the widths uniform (sizing to match the widest) </LI>
	 * <LI>false = let all components take their various preferred widths </UL>
	 */
	public void setUniformWidths(boolean uniformWidths)
	{
		m_bUniformWidths = uniformWidths;
	}

	/**
	 * Set the vertical gap value.
	 *
	 * @param vGap vertical gap
	 */
	public void setVerticalGap(int vGap)
	{
		m_vGap = vGap;
	}

	/**
	 * Set the left external horizontal padding.  This is the space
	 * between the widest component in the column and the edge.
	 *
	 * @param Left external horizontal padding.
	 */
	public void setExternalPadLeft(int padding)
	{
		m_externalPadLeft = padding;
	}

	/**
	 * Set the right external horizontal padding.  This is the space
	 * between the widest component in the column and the edge.
	 *
	 * @param Right external horizontal padding.
	 */
	public void setExternalPadRight(int padding)
	{
		m_externalPadRight = padding;
	}

	/**
	 * Set the top external vertical padding.  This is the space
	 * between the top and the top edge.
	 *
	 * @param Top external vertical padding.
	 */
	public void setExternalPadTop(int padding)
	{
		m_externalPadTop = padding;
	}

	/**
	 * Set the bottom external vertical padding.  This is the space
	 * between the bottom and the bottom edge.
	 *
	 * @param Bottom external vertical padding.
	 */
	public void setExternalPadBottom(int padding)
	{
		m_externalPadBottom = padding;
	}

	/**
	 * Set the internal horizontal padding.  This is a simple way to grow
	 * the horizontal preferred size of all of the components by a certain
	 * amount.  It works great on buttons, because their preferred size
	 * is usually not wide enough to look good.
	 *
	 * @param padding internal horizontal padding
	 */
	public void setInternalPadX(int padding)
	{
		m_internalPadX = padding;
	}

	/**
	 * Set the internal vertical padding.  This is a simple way to grow
	 * the vertical preferred size of all of the components by a certain
	 * amount.  It works great on buttons, because their preferred size
	 * is usually too short.
	 *
	 * @param padding internal vertical padding
	*/
	public void setInternalPadY(int padding)
	{
		m_internalPadY = padding;
	}
}    //class VerticalFlowLayout

