
/*
 *  Copyright © Northwoods Software Corporation, 1998-2002. All Rights
 *  Reserved.
 *
 *  Restricted Rights: Use, duplication, or disclosure by the U.S.
 *  Government is subject to restrictions as set forth in subparagraph
 *  (c) (1) (ii) of DFARS 252.227-7013, or in FAR 52.227-19, or in FAR
 *  52.227-14 Alt. III, as applicable.
 *
 */
package org.supremica.gui.recipeEditor;

import java.awt.*;
import javax.swing.*;
import com.nwoods.jgo.*;

/**
 * A simple view holding objects that can be dragged into another view.
 */
public class Palette
	extends JGoGridView
{
	public Palette()
	{

		// don't let users change the palette using the mouse
		getDocument().setModifiable(false);
		setGridOrigin(new Point(5, 5));
		setHidingDisabledScrollbars(true);
	}

	/**
	 * This value will be either JScrollBar.VERTICAL (the default),
	 * or JScrollBar.HORIZONTAL.
	 */
	public int getOrientation()
	{
		return myOrientation;
	}

	/**
	 * This value determines how the layoutItems method positions the items in
	 * this palette and how the user may be able to scroll the window.
	 * <p>
	 * When the value is JScrollBar.VERTICAL, the layoutItems method will fill
	 * each row of items before adding a new row.  There is a vertical scroll bar
	 * if needed, but no horizontal scroll bar.
	 * When the value is JScrollBar.HORIZONTAL, the layoutItems method will
	 * position items in columns, and there is no vertical scroll bar.
	 */
	public void setOrientation(int o)
	{
		int old = myOrientation;

		if ((old != o) && ((o == JScrollBar.VERTICAL) || (o == JScrollBar.HORIZONTAL)))
		{
			myOrientation = o;

			layoutItems();
			firePropertyChange("orientation", old, o);
		}
	}

	// position all of the items, according to the orientation and grid,
	// without overlapping
	public void layoutItems()
	{
		boolean vert = (getOrientation() == JScrollBar.VERTICAL);

		if (vert)
		{
			if (getVerticalScrollBar() == null)
			{
				JScrollBar vsbar = new JScrollBar(JScrollBar.VERTICAL);

				vsbar.setSize(vsbar.getPreferredSize());
				vsbar.setUnitIncrement(50);
				setVerticalScrollBar(vsbar);
			}

			setHorizontalScrollBar(null);
		}
		else
		{
			if (getHorizontalScrollBar() == null)
			{
				JScrollBar hsbar = new JScrollBar(JScrollBar.HORIZONTAL);

				hsbar.setSize(hsbar.getPreferredSize());
				hsbar.setUnitIncrement(50);
				setHorizontalScrollBar(hsbar);
			}

			setVerticalScrollBar(null);
		}

		// don't care about undo/redo
		getDocument().setSuspendUpdates(true);

		// position all objects vertically so they don't overlap
		int wView = getExtentSize().width;
		int hView = getExtentSize().height;
		int wCell = getGridWidth();
		int hCell = getGridHeight();
		int xOrig = getGridOrigin().x;
		int yOrig = getGridOrigin().y;
		int xPnt = xOrig;
		int yPnt = yOrig;

		// just modify top-level objects, not parts of areas
		JGoListPosition pos = getDocument().getFirstObjectPos();

		while (pos != null)
		{
			JGoObject obj = getDocument().getObjectAtPos(pos);

			pos = getDocument().getNextObjectPosAtTop(pos);

			obj.setTopLeft(xPnt, yPnt);

			if (vert)
			{
				xPnt += Math.max(wCell, (int) (Math.ceil((double) obj.getWidth() / wCell)) * wCell);

				if (xPnt > wView - wCell)
				{
					xPnt = xOrig;
					yPnt += Math.max(hCell, (int) (Math.ceil((double) obj.getHeight() / hCell)) * hCell);
				}
			}
			else
			{
				yPnt += Math.max(hCell, (int) (Math.ceil((double) obj.getHeight() / hCell)) * hCell);

				if (yPnt > hView - hCell)
				{
					yPnt = yOrig;
					xPnt += Math.max(wCell, (int) (Math.ceil((double) obj.getWidth() / wCell)) * wCell);
				}
			}
		}

		// minimize the size of the document
		Dimension docsize = getPrintDocumentSize();

		getDocument().setDocumentSize(docsize);
		getDocument().setSuspendUpdates(false);
	}

	// call layoutItems when items are added or removed from this palette's document
	public void documentChanged(JGoDocumentEvent evt)
	{
		super.documentChanged(evt);

		if (evt.isBeforeChanging())
		{
			return;
		}

		if ((evt.getHint() == JGoDocumentEvent.INSERTED) || (evt.getHint() == JGoDocumentEvent.REMOVED))
		{
			layoutItems();
		}
	}

	// limit the dimensions of this palette window
	public Dimension getPreferredSize()
	{
		return new Dimension(50, 200);
	}

	public Dimension getMinimumSize()
	{
		return new Dimension(50, 100);
	}

	// call layoutItems when the view is realized
	public void addNotify()
	{
		super.addNotify();
		setDropEnabled(false);
		layoutItems();
	}

	// call layoutItems when the view changes shape
	public void doLayout()
	{
		super.doLayout();
		layoutItems();
	}

	// call layoutItems when the grid changes
	public void onGridChange(int what)
	{
		super.onGridChange(what);

		if ((what == JGoGridView.ChangedDimensions) || (what == JGoGridView.ChangedOrigin))
		{
			layoutItems();
		}
	}

	// disable picking any view object
	public JGoObject pickObject(Point pointToCheck, boolean selectableOnly)
	{
		return null;
	}

	// disable autoscrolling
	public void autoscroll(Point location) {}

	// Palette state
	private int myOrientation = JScrollBar.VERTICAL;
}
