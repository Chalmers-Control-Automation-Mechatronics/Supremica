
/*
 *  Supremica Software License Agreement
 *
 *  The Supremica software is not in the public domain
 *  However, it is freely available without fee for education,
 *  research, and non-profit purposes.  By obtaining copies of
 *  this and other files that comprise the Supremica software,
 *  you, the Licensee, agree to abide by the following
 *  conditions and understandings with respect to the
 *  copyrighted software:
 *
 *  The software is copyrighted in the name of Supremica,
 *  and ownership of the software remains with Supremica.
 *
 *  Permission to use, copy, and modify this software and its
 *  documentation for education, research, and non-profit
 *  purposes is hereby granted to Licensee, provided that the
 *  copyright notice, the original author's names and unit
 *  identification, and this permission notice appear on all
 *  such copies, and that no charge be made for such copies.
 *  Any entity desiring permission to incorporate this software
 *  into commercial products or to use it for commercial
 *  purposes should contact:
 *
 *  Knut Akesson (KA), knut@supremica.org
 *  Supremica,
 *  Haradsgatan 26A
 *  431 42 Molndal
 *  SWEDEN
 *
 *  to discuss license terms. No cost evaluation licenses are
 *  available.
 *
 *  Licensee may not use the name, logo, or any other symbol
 *  of Supremica nor the names of any of its employees nor
 *  any adaptation thereof in advertising or publicity
 *  pertaining to the software without specific prior written
 *  approval of the Supremica.
 *
 *  SUPREMICA AND KA MAKES NO REPRESENTATIONS ABOUT THE
 *  SUITABILITY OF THE SOFTWARE FOR ANY PURPOSE.
 *  IT IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED WARRANTY.
 *
 *  Supremica or KA shall not be liable for any damages
 *  suffered by Licensee from the use of this software.
 *
 *  Supremica is owned and represented by KA.
 */
package org.supremica.gui.editor;

import java.awt.*;
import java.awt.event.*;
import java.awt.print.*;
import java.awt.dnd.*;
import java.awt.datatransfer.*;
import java.awt.geom.*;
import javax.swing.*;
import java.util.*;
import com.nwoods.jgo.*;
import org.supremica.automata.Automaton;
import org.supremica.automata.State;

// Provide a view of a AutomatonDocument
// Implement various command handlers
public class AutomatonView
	extends JGoGridView
	implements JGoViewListener
{

	// State
	protected Point myDefaultLocation = new Point(80, 80);
	protected AutomataEditor myApp = null;
	protected JInternalFrame myInternalFrame = null;
	protected Automaton theAutomaton = null;
	private JPopupMenu statePopupMenu;

	public AutomatonView(AutomatonDocument doc)
	{
		super(doc);

		this.theAutomaton = doc.getAutomaton();

		initPopups();
	}

	public void initPopups()
	{
		statePopupMenu = new JPopupMenu();

		JMenuItem cutItem = new JMenuItem("Cut");

		statePopupMenu.add(cutItem);

		JMenuItem copyItem = new JMenuItem("Copy");

		statePopupMenu.add(copyItem);

		JMenuItem deleteItem = new JMenuItem("Delete");

		statePopupMenu.add(deleteItem);
		statePopupMenu.addSeparator();

		JMenuItem selectAllItem = new JMenuItem("Select all");

		statePopupMenu.add(selectAllItem);
		statePopupMenu.addSeparator();

		JMenuItem statusItem = new JMenuItem("Status");

		statePopupMenu.add(statusItem);
	}

	public void initialize(AutomataEditor app, JInternalFrame frame)
	{
		myApp = app;
		myInternalFrame = frame;

		addViewListener(this);
		setGridWidth(10);
		setGridHeight(10);
		setSnapMove(JGoGridView.SnapJump);
		showGrid();
	}

	// convenience method--the return value is a AutomatonDocument instead
	// of a JGoDocument
	AutomatonDocument getDoc()
	{
		return (AutomatonDocument) getDocument();
	}

	AutomataEditor getApp()
	{
		return myApp;
	}

	JInternalFrame getInternalFrame()
	{
		return myInternalFrame;
	}

	// handle DELETE key as well as the page up/down keys
	public void onKeyEvent(KeyEvent evt)
	{
		int t = evt.getKeyCode();

		if (t == KeyEvent.VK_DELETE)
		{
			deleteSelection();
		}
		else
		{
			super.onKeyEvent(evt);
		}
	}

	// implement JGoViewListener
	// just need to keep the actions enabled appropriately
	// depending on the selection
	public void viewChanged(JGoViewEvent e)
	{

		// if the selection changed, maybe some commands need to
		// be disabled or re-enabled
		switch (e.getHint())
		{

		case JGoViewEvent.UPDATE_ALL :
		case JGoViewEvent.SELECTION_GAINED :
		case JGoViewEvent.SELECTION_LOST :
		case JGoViewEvent.SCALE_CHANGED :
			AppAction.updateAllActions();
			break;
		}
	}

	// implement JGoDocumentListener
	// here we just need to keep the title bar up-to-date
	public void documentChanged(JGoDocumentEvent evt)
	{
		if ((evt.getHint() == AutomatonDocument.NameChanged) && (getInternalFrame() != null))
		{
			updateTitle();
		}

		if ((evt.getHint() == JGoDocumentEvent.CHANGED) || (evt.getHint() == JGoDocumentEvent.ARRANGED) || (evt.getHint() == JGoDocumentEvent.INSERTED) || (evt.getHint() == JGoDocumentEvent.REMOVED))
		{
			getDoc().setChanged(true);
		}

		super.documentChanged(evt);
	}

	// have the title bar for the internal frame include the name
	// of the document
	public void updateTitle()
	{
		if (getInternalFrame() != null)
		{
			String title = getDoc().getName();

			getInternalFrame().setTitle(title);
			getInternalFrame().repaint();
		}
	}

	// override newLink to allow the document to decide what
	// kind of link to create
	public void newLink(JGoPort from, JGoPort to)
	{
		getDoc().newLink(from, to);
	}

	// implement commands for creating activities in the Demo
	// let AutomatonDocument do the work
	public void insertNode(Point loc)
	{
		if (loc == null)
		{
			loc = getDefaultLocation();
		}

		if (loc == null)
		{
			loc = new Point(100, 70);
		}

		getDoc().newStateNode(loc);
	}

	// the default place to put stuff if not dragged there
	public Point getDefaultLocation()
	{

		// to avoid constantly putting things in the same place,
		// keep shifting the default location
		if (myDefaultLocation != null)
		{
			myDefaultLocation.x += 10;
			myDefaultLocation.y += 10;
		}

		return myDefaultLocation;
	}

	public boolean validDestinationPort(JGoPort to)
	{
		return true;
	}

	public boolean validSourcePort(JGoPort to)
	{
		return true;
	}

	public boolean validLink(JGoPort from, JGoPort to)
	{
		return true;
	}

	public void setDefaultLocation(Point loc)
	{
		myDefaultLocation = loc;
	}

	// toggle the grid appearance
	void showGrid()
	{
		int style = getGridStyle();

		if (style == JGoGridView.GridInvisible)
		{
			style = JGoGridView.GridDot;

			setGridPen(JGoPen.black);
		}
		else
		{
			style = JGoGridView.GridInvisible;
		}

		setGridStyle(style);
	}

	// printing support
	public Rectangle2D.Double getPrintPageRect(Graphics2D g2, PageFormat pf)
	{

		// leave some space at the bottom for a footer
		return new Rectangle2D.Double(pf.getImageableX(), pf.getImageableY(), pf.getImageableWidth(), pf.getImageableHeight() - 20);
	}

	public void printDecoration(Graphics2D g2, PageFormat pf, int hpnum, int vpnum)
	{

		// draw corners around the getPrintPageRect area
		super.printDecoration(g2, pf, hpnum, vpnum);

		// print the n,m page number in the footer
		String msg = Integer.toString(hpnum);

		msg += ", ";
		msg += Integer.toString(vpnum);

		Paint oldpaint = g2.getPaint();

		g2.setPaint(Color.black);

		Font oldfont = g2.getFont();

		g2.setFont(new Font(JGoText.getDefaultFontFaceName(), Font.PLAIN, 10));
		g2.drawString(msg, (int) (pf.getImageableX() + pf.getImageableWidth() / 2), (int) (pf.getImageableY() + pf.getImageableHeight() - 10));
		g2.setPaint(oldpaint);
		g2.setFont(oldfont);
	}

	public double getPrintScale(Graphics2D g2, PageFormat pf)
	{
		return getScale();
	}

	void zoomIn()
	{
		double newscale = Math.rint(getScale() / 0.9f * 100f) / 100f;

		setScale(newscale);
	}

	void zoomOut()
	{
		double newscale = Math.rint(getScale() * 0.9f * 100f) / 100f;

		setScale(newscale);
	}

	void zoomNormal()
	{
		setScale(1.0d);
	}

	void zoomToFit()
	{
		double newscale = 1;

		if (!getDocument().isEmpty())
		{
			double extentWidth = getExtentSize().width;
			double printWidth = getPrintDocumentSize().width;
			double extentHeight = getExtentSize().height;
			double printHeight = getPrintDocumentSize().height;

			newscale = Math.min((extentWidth / printWidth), (extentHeight / printHeight));
		}

		newscale *= getScale();

		if (newscale > 1)
		{
			newscale = 1;
		}

		setScale(newscale);
		setViewPosition(0, 0);
	}

	public void deleteSelection()
	{
		theAutomaton.beginTransaction();

		JGoSelection theSelection = getSelection();
		JGoListPosition pos = theSelection.getFirstObjectPos();

		while (pos != null)
		{
			JGoObject obj = getObjectAtPos(pos);

			if (obj instanceof StateNode)
			{
				State theState = ((StateNode) obj).getState();

				theAutomaton.removeState(theState);
			}

			pos = theSelection.getNextObjectPos(pos);
		}

		super.deleteSelection();
		theAutomaton.endTransaction();
	}

	public boolean doMouseDblClick(int modifiers, Point dc, Point vc)
	{
		JGoObject obj = pickDocObject(dc, false);

		if (obj != null)
		{

			// get top level object
			while (obj.getParent() != null)
			{
				obj = obj.getParent();
			}

			/*
			 *  if (obj instanceof StateNode)
			 *  {
			 *  ((StateNode)obj).colorChange();
			 *  }
			 */
		}
		else
		{
			insertNode(dc);
		}

		return super.doMouseDblClick(modifiers, dc, vc);
	}

	public boolean isChanged()
	{
		return getDoc().isChanged();
	}

	// an example of how to implement popup menus
	public boolean doMouseDown(int modifiers, Point dc, Point vc)
	{
		JGoObject obj = pickDocObject(dc, true);

		if ((obj != null) && (getCurrentMouseEvent() != null) && getCurrentMouseEvent().isPopupTrigger())
		{
			selectObject(obj);

			return doPopupMenu(modifiers, dc, vc);
		}

		// otherwise implement the default behavior
		return super.doMouseDown(modifiers, dc, vc);
	}

	public boolean doMouseUp(int modifiers, Point dc, Point vc)
	{
		JGoObject obj = pickDocObject(dc, true);

		if ((obj != null) && (getCurrentMouseEvent() != null) && getCurrentMouseEvent().isPopupTrigger())
		{
			selectObject(obj);

			return doPopupMenu(modifiers, dc, vc);
		}

		// otherwise implement the default behavior
		return super.doMouseUp(modifiers, dc, vc);
	}

	public boolean doPopupMenu(int modifiers, Point dc, Point vc)
	{

		// JGoObject obj =
		statePopupMenu.show(this, vc.x, vc.y);

		return true;
	}
}
