
/*
 *  Copyright © Northwoods Software Corporation, 2000-2002. All Rights
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
import java.awt.event.*;
import java.awt.print.*;
import java.awt.dnd.*;
import java.awt.geom.*;
import javax.swing.*;
import java.util.*;
import com.nwoods.jgo.*;

//import com.nwoods.jgo.examples.*;
// Provide a view of a RecipeDocument
// Implement various command handlers
public class RecipeView
	extends JGoView
	implements JGoViewListener
{
	private static final long serialVersionUID = 1L;

	public RecipeView() {}

	public RecipeView(JGoDocument doc)
	{
		super(doc);
	}

	public void initialize(RecipeEditor app, JInternalFrame frame)
	{
		myApp = app;
		myInternalFrame = frame;

		addViewListener(this);
		setIncludingNegativeCoords(true);
		setDragsRealtime(false);
		updateTitle();
	}

	// creating a RecipeView without specifying a document in the constructor
	// needs to create a RecipeDocument rather than a generic JGoDocument
	public JGoDocument createDefaultModel()
	{
		return new RecipeDocument();
	}

	// convenience method--the return value is a RecipeDocument instead
	// of a JGoDocument
	RecipeDocument getDoc()
	{
		return (RecipeDocument) getDocument();
	}

	RecipeEditor getApp()
	{
		return myApp;
	}

	JInternalFrame getInternalFrame()
	{
		return myInternalFrame;
	}

	// handle DELETE, HOME, and arrow keys as well as the page up/down keys
	public void onKeyEvent(KeyEvent evt)
	{
		int t = evt.getKeyCode();

		if (t == KeyEvent.VK_DELETE)
		{
			if (getDoc().isModifiable())
			{
				deleteSelection();
			}
		}
		else if (t == KeyEvent.VK_HOME)
		{
			setViewPosition(0, 0);
		}
		else if (t == KeyEvent.VK_RIGHT)
		{
			if (getDoc().isModifiable())
			{
				doMoveSelection(0, 10, 0, EventMouseUp);
			}
		}
		else if (t == KeyEvent.VK_LEFT)
		{
			if (getDoc().isModifiable())
			{
				doMoveSelection(0, -10, 0, EventMouseUp);
			}
		}
		else if (t == KeyEvent.VK_DOWN)
		{
			if (getDoc().isModifiable())
			{
				doMoveSelection(0, 0, 10, EventMouseUp);
			}
		}
		else if (t == KeyEvent.VK_UP)
		{
			if (getDoc().isModifiable())
			{
				doMoveSelection(0, 0, -10, EventMouseUp);
			}
		}
		else if ((t == KeyEvent.VK_F6) &&!evt.isControlDown() &&!evt.isShiftDown() &&!evt.isAltDown() &&!evt.isMetaDown())
		{
			zoomToFit();
		}
		else if (t == KeyEvent.VK_ESCAPE)
		{
			doCancelMouse();
		}
		else if ((t == KeyEvent.VK_ENTER) && (getState() == MouseStateMovingPort))
		{
			stopMovingPort();
		}
		else if (Character.isLetter(evt.getKeyChar()))
		{
			if (!selectNextNode(evt.getKeyChar()))
			{
				Toolkit.getDefaultToolkit().beep();
			}
		}
		else
		{
			super.onKeyEvent(evt);
		}
	}

	// new kinds of "modes"
	public static final int MouseStateRoutingLink = MouseStateLast + 1;
	public static final int MouseStateMovingPort = MouseStateLast + 2;

	public void doCancelMouse()
	{
		myCanceling = true;
		myMouseUpDocPoint.x = -9999;
		myMouseUpDocPoint.y = -9999;

		if (getState() == MouseStateRoutingLink)
		{
			cancelRoutedLink();
		}
		else if (getState() == MouseStateMovingPort)
		{
			stopMovingPort();
		}
		else
		{
			super.doCancelMouse();
			getSelection().clearSelection();
		}

		myCanceling = false;
	}

	// This implements additional mouse behaviors on mouse down:
	// - maybe start creating a link with a user-defined path
	// - consider starting to move a port
	// - if it's a right button mouse down bring up a properties
	// dialog for the object
	public boolean doMouseDown(int modifiers, Point dc, Point vc)
	{
		if (getState() == MouseStateRoutingLink)
		{
			if (getCurrentObject() == null)
			{
				startRoutedLink(modifiers, dc, vc);
			}
			else
			{
				JGoObject obj = pickDocObject(dc, false);

				if ((obj instanceof JGoPort) && myFromPort.validLink((JGoPort) obj))
				{    // found a destination port
					finishRoutedLink(modifiers, dc, vc, obj);
				}
				else
				{
					addPointToRoutedLink(modifiers, dc, vc);
				}
			}

			return true;
		}

		if (getState() == MouseStateMovingPort)
		{
			pickMovablePort(modifiers, dc, vc);

			return true;
		}

		if ((modifiers & InputEvent.BUTTON3_MASK) != 0)
		{
			JPopupMenu popup = myPopupMenu;

			popup.removeAll();

			JGoObject obj = pickDocObject(dc, true);

			if (obj != null)
			{
				obj = obj.getTopLevelObject();

				getSelection().selectObject(obj);
				popup.add(getApp().ObjectPropertiesAction);
				popup.addSeparator();

				if (obj instanceof FlowLink)
				{
					FlowLink link = (FlowLink) obj;
					AppAction insertPointAction = new AppAction("Insert Point", getApp())
					{
						private static final long serialVersionUID = 1L;

						public void actionPerformed(ActionEvent e)
						{
							insertPointIntoLink();
						}

						public boolean canAct()
						{
							return super.canAct() && getView().getDoc().isModifiable();
						}
					};

					popup.add(insertPointAction);

					if (link.getNumPoints() > (link.isOrthogonal()
											   ? 6
											   : 2))
					{
						AppAction removeSegmentAction = new AppAction("Remove Segment", getApp())
						{
							private static final long serialVersionUID = 1L;

							public void actionPerformed(ActionEvent e)
							{
								removeSegmentFromLink();
							}

							public boolean canAct()
							{
								return super.canAct() && getView().getDoc().isModifiable();
							}
						};

						popup.add(removeSegmentAction);
					}

					if ((link.getFromPort().getParent() instanceof OperationNode) && (link.getToPort().getParent() instanceof OperationNode))
					{
						AppAction remoteConnectionAction = new AppAction("Remote Connection", getApp())
						{
							private static final long serialVersionUID = 1L;

							public void actionPerformed(ActionEvent e)
							{
								replaceLinkWithRemoteConnection();
							}

							public boolean canAct()
							{
								return super.canAct() && getView().getDoc().isModifiable();
							}
						};

						popup.add(remoteConnectionAction);
					}

					popup.addSeparator();
				}

				popup.add(getApp().CopyAction);
				popup.add(getApp().CutAction);
			}
			else
			{

				// no object selected
				popup.add(getApp().FilePropertiesAction);
				popup.addSeparator();

				AppAction pasteAction = new AppAction("Paste", getApp())
				{
					private static final long serialVersionUID = 1L;

					public void actionPerformed(ActionEvent e)
					{
						pasteHere();
					}

					public boolean canAct()
					{
						return super.canAct() && getView().getDoc().isModifiable();
					}
				};

				popup.add(pasteAction);

				myPopupPoint = dc;
			}

			popup.show(this, vc.x, vc.y);

			return true;
		}

		// otherwise implement the default behavior
		return super.doMouseDown(modifiers, dc, vc);
	}

	// This implements additional mouse behaviors on mouse down:
	// - when creating a link with a user-defined path, have the end follow
	// the mouse pointer between clicks
	public boolean doMouseMove(int modifiers, Point dc, Point vc)
	{
		if (getState() == MouseStateRoutingLink)
		{
			followPointerForRoutedLink(modifiers, dc, vc);

			return true;
		}

		if (getState() == MouseStateMovingPort)
		{
			followPointerForMovablePort(modifiers, dc, vc);

			return true;
		}

		return super.doMouseMove(modifiers, dc, vc);
	}

	private JPopupMenu myPopupMenu = new JPopupMenu();
	private Point myMouseUpDocPoint = new Point();

	// This implements additional mouse behaviors on mouse up:
	// - mouse up must be ignored when the user is specifying the stroke
	// points for a routed link
	// - when moving a port, drop the port at this point
	public boolean doMouseUp(int modifiers, Point dc, Point vc)
	{
		myMouseUpDocPoint.setLocation(dc);

		if (getState() == MouseStateRoutingLink)
		{

			// don't need to do anything
			return false;
		}

		if (getState() == MouseStateMovingPort)
		{
			dropMovablePort(modifiers, dc, vc);

			return false;    // continue in MovingPort mode
		}

		// otherwise implement the default behavior
		return super.doMouseUp(modifiers, dc, vc);
	}

	// state for MouseStateRoutingLink mode
	private JGoPort myFromPort = null;
	private boolean myOrthogonal = true;
	private boolean myHoriz = true;

	public void startRoutingLink()
	{
		setState(MouseStateRoutingLink);
		getDocument().startTransaction();
		setCurrentObject(null);

		myFromPort = null;

		setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
	}

	void cancelRoutedLink()
	{
		if (!hasFocus())
		{
			return;
		}

		if (getCurrentObject() != null)
		{
			removeObject(getCurrentObject());
			setCurrentObject(null);
		}

		myFromPort = null;

		setCursor(getDefaultCursor());
		getDocument().endTransaction(false);
		setState(MouseStateNone);
	}

	void startRoutedLink(int modifiers, Point dc, Point vc)
	{
		JGoObject obj = pickDocObject(dc, false);

		if ((obj instanceof JGoPort) && ((JGoPort) obj).isValidSource())
		{    // found the source port

			// remember it for when we create the link
			myFromPort = (JGoPort) obj;

			// create a new stroke starting at or near the first mouse down point
			JGoStroke stroke = new JGoStroke();
			Point lp = myFromPort.getFromLinkPoint(dc);

			stroke.addPoint(lp);

			if (myOrthogonal)
			{

				// add a next-to-last point that gets modified to maintain orthogonality
				if (myFromPort.getFromSpot() == JGoObject.SideLeft)
				{
					myHoriz = true;

					stroke.addPoint(lp.x - myFromPort.getEndSegmentLength(), lp.y);
				}
				else if (myFromPort.getFromSpot() == JGoObject.SideRight)
				{
					myHoriz = true;

					stroke.addPoint(lp.x + myFromPort.getEndSegmentLength(), lp.y);
				}
				else if (myFromPort.getFromSpot() == JGoObject.TopMiddle)
				{
					myHoriz = false;

					stroke.addPoint(lp.x, lp.y - myFromPort.getEndSegmentLength());
				}
				else if (myFromPort.getFromSpot() == JGoObject.BottomMiddle)
				{
					myHoriz = false;

					stroke.addPoint(lp.x, lp.y + myFromPort.getEndSegmentLength());
				}
				else
				{    // assume horizontally towards the right
					myHoriz = true;

					stroke.addPoint(lp.x + myFromPort.getEndSegmentLength(), lp.y);
				}
			}

			stroke.addPoint(dc);    // the last point is modified in doMouseMove to follow the mouse

			// the stroke is temporarily a view object
			addObjectAtTail(stroke);
			setCurrentObject(stroke);
		}
	}

	void followPointerForRoutedLink(int modifiers, Point dc, Point vc)
	{
		JGoStroke myStroke = (JGoStroke) getCurrentObject();

		if (myStroke != null)
		{
			int numpts = myStroke.getNumPoints();

			myStroke.setPoint(numpts - 1, dc);

			if (myOrthogonal && (numpts > 2))
			{
				Point penult = new Point(myStroke.getPoint(numpts - 2));

				if (myHoriz)
				{
					penult.x = dc.x;
				}
				else
				{
					penult.y = dc.y;
				}

				// ?? what if previous points aren't right
				myStroke.setPoint(numpts - 2, penult);
			}
		}
	}

	void addPointToRoutedLink(int modifiers, Point dc, Point vc)
	{

		// keep adding points to the stroke representing the future link
		JGoStroke myStroke = (JGoStroke) getCurrentObject();

		myHoriz = !myHoriz;

		myStroke.addPoint(dc);
	}

	void finishRoutedLink(int modifiers, Point dc, Point vc, JGoObject obj)
	{
		JGoPort toPort = (JGoPort) obj;
		JGoStroke myStroke = (JGoStroke) getCurrentObject();

		// get rid of the stroke
		removeObject(myStroke);

		// create the link in the document
		JGoLabeledLink link = getDoc().newLink(myFromPort, toPort);

		// now update the link's stroke points
		// add all but the first and last of the points the user clicked on
		// (the first and last specified ports)
		Point firstpt = link.getPoint(0);
		Point secondpt = link.getPoint(1);
		boolean startseg = false;
		Point nexttolastpt = link.getPoint(link.getNumPoints() - 2);
		Point lastpt = link.getPoint(link.getNumPoints() - 1);
		boolean endseg = false;

		if (myFromPort.getFromSpot() != JGoObject.NoSpot)
		{
			startseg = true;

			if (myOrthogonal)
			{
				Point lssecond = link.getPoint(1);
				Point mysecond = new Point(myStroke.getPoint(1));
				Point mythird = myStroke.getPoint(2);

				if (mysecond.x == mythird.x)
				{
					mysecond.y = lssecond.y;
				}
				else if (mysecond.y == mythird.y)
				{
					mysecond.x = lssecond.x;
				}

				myStroke.setPoint(1, mysecond);
			}
		}

		if (toPort.getToSpot() != JGoObject.NoSpot)
		{
			endseg = true;

			if (myOrthogonal)
			{

				// fix up the next to last drawn point to account for
				// any end segment in the link (rather than directly to the port)
				int numpts = myStroke.getNumPoints();
				Point lspenult = link.getPoint(link.getNumPoints() - 2);
				Point mypenult = new Point(myStroke.getPoint(numpts - 2));
				Point myante = myStroke.getPoint(numpts - 3);

				if (mypenult.x == myante.x)
				{
					mypenult.y = lspenult.y;
				}
				else if (mypenult.y == myante.y)
				{
					mypenult.x = lspenult.x;
				}

				myStroke.setPoint(numpts - 2, mypenult);
			}
		}

		link.removeAllPoints();
		link.addPoint(firstpt);

		if (startseg)
		{
			link.addPoint(secondpt);
		}

		for (int i = 1; i < myStroke.getNumPoints() - 1; i++)
		{
			link.addPoint(myStroke.getPoint(i));
		}

		if (endseg)
		{
			link.addPoint(nexttolastpt);
		}

		link.addPoint(lastpt);
		selectObject(link);
		setCurrentObject(null);    // now we can throw the temp view link away

		myFromPort = null;

		setCursor(getDefaultCursor());
		getDocument().endTransaction("created routing link");
		setState(MouseStateNone);
	}

	// state for MouseStateMovingPort mode
	private Point myOriginalPosition = new Point();
	private Point myOffset = new Point();

	public void startMovingPort()
	{
		setState(MouseStateMovingPort);
		getDocument().startTransaction();
		setCurrentObject(null);
		setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
	}

	void stopMovingPort()
	{
		if (!hasFocus())
		{
			return;
		}

		if (getCurrentObject() != null)
		{
			getCurrentObject().setTopLeft(myOriginalPosition);
		}

		setCursor(getDefaultCursor());

		if (getDocument() != null)
		{
			getDocument().endTransaction(false);
		}

		setState(MouseStateNone);
	}

	void pickMovablePort(int modifiers, Point dc, Point vc)
	{
		JGoObject obj = pickDocObject(dc, false);

		if (obj instanceof JGoPort)
		{
			JGoPort p = (JGoPort) obj;

			setCurrentObject(p);
			myOriginalPosition.setLocation(p.getTopLeft());

			myOffset.x = dc.x - p.getLeft();
			myOffset.y = dc.y - p.getTop();
		}
	}

	void followPointerForMovablePort(int modifiers, Point dc, Point vc)
	{
		if (getCurrentObject() != null)
		{
			getCurrentObject().setTopLeft(dc.x - myOffset.x, dc.y - myOffset.y);
		}
	}

	void dropMovablePort(int modifiers, Point dc, Point vc)
	{
		if (getCurrentObject() != null)
		{
			setCurrentObject(null);

			if (getDocument() != null)
			{
				getDocument().endTransaction("moved port");
			}
		}
		else
		{
			if (getDocument() != null)
			{
				getDocument().endTransaction(false);
			}
		}
	}

	// popup menu commands
	void insertPointIntoLink()
	{
		if (getSelection().getPrimarySelection() instanceof JGoLink)
		{
			JGoLink s = (JGoLink) getSelection().getPrimarySelection();
			int i = s.getSegmentNearPoint(myMouseUpDocPoint);

			if (i < 1)
			{
				i = 1;    // don't add to first segment
			}
			else if (i >= s.getNumPoints() - 2)
			{
				i = s.getNumPoints() - 3;    // don't add to last segment
			}

			Point a = s.getPoint(i);
			Point b = s.getPoint(i + 1);
			Point closest = new Point((a.x + b.x) / 2, (a.y + b.y) / 2);

			getDocument().startTransaction();
			s.insertPoint(i + 1, closest);

			if ((s instanceof JGoLink) && ((JGoLink) s).isOrthogonal())
			{    // when orthogonal, gotta insert two points
				s.insertPoint(i + 1, closest);
			}

			getSelection().clearSelection(s);
			selectObject(s);
			getDocument().endTransaction("inserted point into link stroke");
		}
	}

	void removeSegmentFromLink()
	{
		if (getSelection().getPrimarySelection() instanceof JGoLink)
		{
			JGoLink s = (JGoLink) getSelection().getPrimarySelection();
			int i = s.getSegmentNearPoint(myMouseUpDocPoint);

			getDocument().startTransaction();

			if (s.isOrthogonal())
			{    // will have at least 7 points

				// don't remove either first two or last two segments
				i = Math.max(i, 2);
				i = Math.min(i, s.getNumPoints() - 5);

				Point a = s.getPoint(i);
				Point b = s.getPoint(i + 1);

				s.removePoint(i);

				// to maintain orthogonality, gotta remove two points
				s.removePoint(i);

				// now fix up following point to maintain orthogonality
				Point c = new Point(s.getPoint(i));

				if (a.x == b.x)
				{
					c.y = a.y;
				}
				else
				{
					c.x = a.x;
				}

				s.setPoint(i, c);
			}
			else
			{    // will have at least 3 points
				i = Math.max(i, 1);    // don't remove point 0
				i = Math.min(i, s.getNumPoints() - 2);    // don't remove last point

				s.removePoint(i);
			}

			getSelection().clearSelection(s);
			selectObject(s);
			getDocument().endTransaction("removed segment from link stroke");
		}
	}

	void replaceLinkWithRemoteConnection()
	{
		if (getSelection().getPrimarySelection() instanceof FlowLink)
		{
			FlowLink s = (FlowLink) getSelection().getPrimarySelection();
			String flowlabeltext = s.getText();
			JGoPort from = s.getFromPort();
			Point fromPt = from.getFromLinkPoint();
			double fromDir = from.getFromLinkDir();

			fromPt.x += (int) (100 * Math.cos(fromDir));
			fromPt.y += (int) (100 * Math.sin(fromDir));

			JGoPort to = s.getToPort();
			int n = s.getNumPoints() - 3;
			Point toPt = to.getToLinkPoint();
			double toDir = to.getToLinkDir();

			toPt.x += (int) (100 * Math.cos(toDir));
			toPt.y += (int) (100 * Math.sin(toDir));

			getDocument().startTransaction();
			s.unlink();

			int cid = getDoc().getNextConnectorID();
			RemoteConnectorNode bn1 = new RemoteConnectorNode();
			RemoteConnectorNode bn2 = new RemoteConnectorNode();

			bn1.initialize(fromPt, "C" + Integer.toString(cid), bn2);
			getDoc().getFirstLayer().addObjectAtTail(bn1);

			FlowLink l1 = (FlowLink) getDoc().newLink(from, bn1.getPort());

			l1.setArrowHeads(false, true);
			l1.setText(flowlabeltext);
			bn2.initialize(toPt, "C" + Integer.toString(cid), bn1);
			getDoc().getFirstLayer().addObjectAtTail(bn2);

			FlowLink l2 = (FlowLink) getDoc().newLink(bn2.getPort(), to);

			// don't need to turn on arrowhead
			l2.setText(flowlabeltext);
			getDocument().endTransaction("replaced link with remote connectors");
		}
	}

	// background double click bring up dialog for whole process
	public boolean doMouseDblClick(int modifiers, Point dc, Point vc)
	{
		if (!super.doMouseDblClick(modifiers, dc, vc))
		{

			// handle labeled link labels
			JGoObject obj = pickDocObject(dc, true);

			if ((obj != null) && (obj instanceof FlowLabel))
			{
				FlowLabel lab = (FlowLabel) obj;

				if (lab.getLink() != null)
				{
					return lab.getLink().doMouseDblClick(modifiers, dc, vc, this);
				}
			}

			// otherwise, assume a background double click
			getApp().editRecipeProperties();

			return true;
		}

		return false;
	}

	// although we use the standard drag-and-drop behavior, we do
	// want to modify the objects that are dropped
	public void drop(DropTargetDropEvent e)
	{

		// try standard drop action
		JGoCopyEnvironment map = getDocument().createDefaultCopyEnvironment();

		getDocument().startTransaction();

		if (getDocument().isModifiable() && doDrop(e, map))
		{
			Iterator i = map.values().iterator();

			while (i.hasNext())
			{
				Object o = i.next();

				if (o instanceof OperationNode)
				{
					OperationNode anode = (OperationNode) o;

					anode.resetEditability();
					anode.getLabel().setSelectable(true);
				}
			}

			getDocument().endTransaction("Drop");
		}
		else
		{
			e.rejectDrop();
			getDocument().endTransaction(false);
		}
	}

	public void selectAll()
	{
		JGoSelection selection = getSelection();
		JGoLayer layer = getFirstLayer();

		while (layer != null)
		{
			if (layer.isVisible())
			{
				JGoListPosition pos = layer.getFirstObjectPos();

				while (pos != null)
				{
					JGoObject obj = layer.getObjectAtPos(pos);

					// only consider top-level objects
					pos = layer.getNextObjectPosAtTop(pos);

					if (obj.isVisible() &&!(obj instanceof FlowLabel) && (obj.isSelectable() || obj.isGrabChildSelection() || (obj.redirectSelection() != obj)))
					{
						getSelection().extendSelection(obj);
					}
				}
			}

			layer = getNextLayer(layer);
		}
	}

	private boolean containsRect(Rectangle rect, Rectangle obj)
	{
		int width = rect.width;
		int height = rect.height;
		int W = obj.width;
		int H = obj.height;

		if ((width <= 0) || (height <= 0) || (W < 0) || (H < 0))
		{
			return false;
		}

		int x = rect.x;
		int y = rect.y;
		int X = obj.x;
		int Y = obj.y;

		return ((X >= x) && (Y >= y) && (X + W <= x + width) && (Y + H <= y + height));
	}

	public void selectInBox(Rectangle rect)
	{
		JGoLayer layer = getFirstLayer();

		while (layer != null)
		{
			if (layer.isVisible())
			{
				JGoListPosition pos = layer.getFirstObjectPos();

				while (pos != null)
				{
					JGoObject obj = layer.getObjectAtPos(pos);

					pos = layer.getNextObjectPosAtTop(pos);

					if (obj.isVisible() &&!(obj instanceof FlowLabel) && containsRect(rect, obj.getBoundingRect()) && (obj.isSelectable() || (obj.isGrabChildSelection() && (obj instanceof JGoObjectCollection)) || (obj.redirectSelection() != obj)))
					{
						getSelection().extendSelection(obj);
					}
				}
			}

			layer = getNextLayer(layer);
		}
	}

	public void moveSelection(JGoSelection sel, int modifiers, int offsetx, int offsety, int event)
	{
		if ((offsetx == 0) && (offsety == 0))
		{
			return;
		}

		JGoObject prim = sel.getPrimarySelection();
		FlowLabel flabel = null;

		if ((prim instanceof FlowLabel) && (sel.getNumObjects() == 1))
		{
			flabel = (FlowLabel) prim;
		}

		super.moveSelection(sel, modifiers, offsetx, offsety, event);

		if (flabel != null)
		{
			FlowLink l = flabel.getLink();

			if (l != null)
			{
				Rectangle rect = flabel.getBoundingRect();
				Point center = new Point(rect.x + rect.width / 2, rect.y + rect.height / 2);
				int nFirstPoint = 1;
				int nLastPoint = l.getNumPoints() - 1;
				Point R = new Point();
				double mindist = 10e20d;
				int minseg = 0;
				Point minpnt = new Point();
				Point A = null;
				Point B = null;

				for (int i = nFirstPoint; i < nLastPoint - 1; i++)
				{
					A = l.getPoint(i);
					B = l.getPoint(i + 1);

					JGoStroke.getNearestPointOnLine(A, B, center, R);

					double newdist = (R.x - center.x) * (R.x - center.x) + (R.y - center.y) * (R.y - center.y);

					if (newdist < mindist)
					{
						mindist = newdist;
						minseg = i;

						minpnt.setLocation(R);
					}
				}

				A = l.getPoint(minseg);
				B = l.getPoint(minseg + 1);

				flabel.setSegment(minseg);
				R.setLocation(minpnt);

				double rdist = Math.sqrt((A.x - R.x) * (A.x - R.x) + (A.y - R.y) * (A.y - R.y));
				double sdist = Math.sqrt((A.x - B.x) * (A.x - B.x) + (A.y - B.y) * (A.y - B.y));

				if (sdist <= 0)
				{
					flabel.setSegmentDistance(0);
				}
				else
				{
					flabel.setSegmentDistance((int) Math.rint(rdist * 100 / sdist));
				}

				flabel.setOffset(center.x - R.x, center.y - R.y);
			}
		}
	}

	public void paste()
	{
		JGoDocument doc = getDocument();

		if (doc != null)
		{
			doc.startTransaction();
		}

		Toolkit toolkit = getToolkit();

		if (toolkit == null)
		{
			toolkit = JGoGlobal.getToolkit();
		}

		JGoCopyEnvironment map = pasteFromClipboard(toolkit.getSystemClipboard());
		Iterator i = map.values().iterator();
		boolean any = false;
		JGoSelection sel = getSelection();

		while (i.hasNext())
		{
			Object o = i.next();

			if (o instanceof JGoObject)
			{
				JGoObject obj = (JGoObject) o;

				if (obj.isTopLevel() && (obj.getDocument() == doc) &&!(obj instanceof FlowLabel))
				{
					if (!any)
					{
						any = true;

						sel.clearSelection();
					}

					sel.extendSelection(obj);
				}
			}
		}

		if (doc != null)
		{
			doc.endTransaction(getEditPresentationName(10));
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
		if ((evt.getHint() == RecipeDocument.NAME_CHANGED) || (evt.getHint() == JGoDocumentEvent.MODIFIABLE_CHANGED))
		{
			updateTitle();
		}

		super.documentChanged(evt);
	}

	// have the title bar for the internal frame include the name
	// of the document and whether it's read-only
	public void updateTitle()
	{
		if (getInternalFrame() != null)
		{
			String title = getDoc().getName();

			if (!getDocument().isModifiable())
			{
				title += " (read-only)";
			}

			getInternalFrame().setTitle(title);
			getInternalFrame().repaint();
		}
	}

	protected JGoLink createTemporaryLinkForNewLink(JGoPort from, JGoPort to)
	{
		JGoLink temp = new JGoLink(from, to);

		temp.setOrthogonal(true);

		return temp;
	}

	// override newLink to force the creation of a FlowLink instead of JGoLink
	// let RecipeDocument do the work
	public void newLink(JGoPort from, JGoPort to)
	{
		getDoc().newLink(from, to);
		getDoc().endTransaction("new FlowLink");
	}

	// if the user fails to connect a partly drawn new link, we create a
	// ExternalConnectorNode and hook it up to that instead
	protected void noNewLink(JGoPort from, JGoPort to)
	{
		if (myCanceling)
		{
			getDoc().endTransaction(false);
		}
		else if ((from == null) && (to != null))
		{
			ExternalConnectorNode bn = new ExternalConnectorNode();

			bn.initialize(myMouseUpDocPoint, "in", false);
			getDoc().getFirstLayer().addObjectAtTail(bn);
			getDoc().newLink(bn.getPort(), to);
			getDoc().endTransaction("linked to new input");
		}
		else if ((from != null) && (to == null))
		{
			ExternalConnectorNode bn = new ExternalConnectorNode();

			bn.initialize(myMouseUpDocPoint, "out", true);
			getDoc().getFirstLayer().addObjectAtTail(bn);

			JGoLink l = getDoc().newLink(from, bn.getPort());

			l.setArrowHeads(false, true);
			getDoc().endTransaction("linked to new output");
		}
		else
		{
			getDoc().endTransaction(false);
		}
	}

	public void reLink(JGoLink oldlink, JGoPort from, JGoPort to)
	{
		if (to != null)
		{
			if (to.getParent() instanceof BasicNode)
			{
				oldlink.setArrowHeads(false, true);

				BasicNode bn = (BasicNode) to.getParent();

				bn.getDocument().removeObject(bn);
			}
			else
			{
				oldlink.setArrowHeads(false, false);
			}
		}

		if ((from != null) && (from.getParent() instanceof BasicNode))
		{
			BasicNode bn = (BasicNode) from.getParent();

			bn.getDocument().removeObject(bn);
		}

		super.reLink(oldlink, from, to);
	}

	// implement commands for creating activities in the process
	// let RecipeDocument do the work
	public void insertOperation()
	{
		getDoc().startTransaction();

		Point loc = getDefaultLocation();

		if (loc == null)
		{
			loc = new Point(100, 70);
		}

		OperationNode snode = getDoc().newNode(OperationNode.Operation);

		snode.setTopLeft(loc);
		getDoc().endTransaction("new Operation");
	}

	public void insertInput()
	{
		getDoc().startTransaction();

		Point loc = getDefaultLocation();

		if (loc == null)
		{
			loc = new Point(100, 70);
		}

		OperationNode snode = getDoc().newNode(OperationNode.Start);

		snode.setTopLeft(loc);
		getDoc().endTransaction("new Output");
	}

	public void insertOutput()
	{
		getDoc().startTransaction();

		Point loc = getDefaultLocation();

		if (loc == null)
		{
			loc = new Point(100, 70);
		}

		OperationNode snode = getDoc().newNode(OperationNode.Finish);

		snode.setTopLeft(loc);
		getDoc().endTransaction("new Input");
	}

	public void insertComment()
	{
		getDoc().startTransaction();

		Point loc = getDefaultLocation();

		if (loc == null)
		{
			loc = new Point(100, 70);
		}

		Comment cmnt = new Comment("a comment");

		cmnt.setEditable(true);
		cmnt.setTopLeft(loc);
		getDoc().getFirstLayer().addObjectAtHead(cmnt);
		getDoc().endTransaction("new Comment");
	}

	public void pasteHere()
	{
		getDoc().startTransaction();

		Point docpt = myPopupPoint;

		getSelection().clearSelection();
		paste();    // selects all newly pasted objects

		Rectangle copybounds = new Rectangle(JGoDocument.computeBounds(getSelection()));

		moveSelection(getSelection(), 0, docpt.x - copybounds.x, docpt.y - copybounds.y, EventMouseUp);
		getDoc().endTransaction("Paste Here");
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

	public void setDefaultLocation(Point loc)
	{
		myDefaultLocation = loc;
	}

	public void editDrawRoutedLink()
	{
		javax.swing.JOptionPane.showMessageDialog(this, "Starting a mode to allow you to click on a port and\n" + "  start drawing a link whose path you can specify by clicking.\n" + "This mode stops when you click on another port, or when you\n" + "  type ESCAPE to cancel the new link.", "Drawing Routed Link Mode", javax.swing.JOptionPane.INFORMATION_MESSAGE);
		startRoutingLink();
	}

	public void editMovePort()
	{
		javax.swing.JOptionPane.showMessageDialog(this, "Starting a mode to allow you to reposition ports on a node.\n" + "The relative position may not be maintained or permitted if\n" + "  the node definition doesn't support it.\n" + "Type ESCAPE or ENTER to stop this mode.", "Moving Port Mode", javax.swing.JOptionPane.INFORMATION_MESSAGE);
		startMovingPort();
	}

	// bring up the appropriate dialog, based on the current selection
	public void editObjectProperties()
	{
		JGoSelection sel = getSelection();

		if (sel.isEmpty())
		{
			getApp().editRecipeProperties();

			return;
		}

		getDocument().startTransaction();

		JGoObject obj = sel.getPrimarySelection();

		obj = obj.getTopLevelObject();

		if (obj instanceof OperationNode)
		{
			editOperation((OperationNode) obj);
		}
		else if (obj instanceof FlowLink)
		{
			editFlow((FlowLink) obj);
		}
		else if (obj instanceof FlowLabel)
		{
			FlowLabel lab = (FlowLabel) obj;

			editFlow(lab.getLink());
		}

		getDocument().endTransaction("Object Properties");
	}

	public void editOperation(OperationNode snode)
	{
		if (snode == null)
		{
			return;
		}

		switch (snode.getOperationType())
		{

		case OperationNode.Start :
			JOptionPane.showInternalMessageDialog(this, "A Start node has no editable properties", "information", JOptionPane.INFORMATION_MESSAGE);
			break;

		case OperationNode.Finish :
			JOptionPane.showInternalMessageDialog(this, "A Finish node has no editable properties", "information", JOptionPane.INFORMATION_MESSAGE);
			break;

		default :
		case OperationNode.Operation :
			OperationDialog activityDialog = new OperationDialog(getFrame(), snode);

			activityDialog.setLocationRelativeTo(null);
			activityDialog.setVisible(true);
			break;
		}
	}

	public void editFlow(FlowLink link)
	{
		if (link == null)
		{
			return;
		}

		FlowDialog flowDialog = new FlowDialog(getFrame(), link);

		flowDialog.setLocationRelativeTo(null);
		flowDialog.setVisible(true);
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

	public void zoomIn()
	{
		myOriginalScale = true;

		double newscale = Math.rint(getScale() / 0.9f * 100f) / 100f;

		setScale(newscale);
	}

	public void zoomOut()
	{
		myOriginalScale = true;

		double newscale = Math.rint(getScale() * 0.9f * 100f) / 100f;

		setScale(newscale);
	}

	public void zoomNormal()
	{
		myOriginalScale = true;

		setScale(1.0d);
	}

	public void zoomToFit()
	{
		if (myOriginalScale)
		{
			myOriginalDocPosition = getViewPosition();
			myOriginalDocScale = getScale();

			RescaleToFit();
		}
		else
		{
			setViewPosition(myOriginalDocPosition);
			setScale(myOriginalDocScale);
		}

		myOriginalScale = !myOriginalScale;
	}

	public void RescaleToFit()
	{
		Dimension dispSize = getCanvas().getSize();
		Rectangle docbounds = new Rectangle(getDocument().computeBounds());

		// include the (0, 0) point
		if (docbounds.x > 0)
		{
			docbounds.width += docbounds.x;
			docbounds.x = 0;
		}

		if (docbounds.y > 0)
		{
			docbounds.height += docbounds.y;
			docbounds.y = 0;
		}

		setViewPosition(0, 0);

		double newscale = 1;

		if ((docbounds.width > 0) && (docbounds.height > 0))
		{
			newscale = Math.min((dispSize.width / (double) docbounds.width), (dispSize.height / (double) docbounds.height));
		}

		if (newscale > 1)
		{
			newscale = 1;
		}

		setScale(newscale);
	}

	public boolean matchesNodeLabel(OperationNode node, char c)
	{
		if (node == null)
		{
			return false;
		}

		String name = node.getText();

		return ((name.length() > 0) && (Character.toUpperCase(name.charAt(0)) == c));
	}

	public boolean selectNextNode(char c)
	{
		c = Character.toUpperCase(c);

		JGoDocument doc = getDocument();
		OperationNode startnode = null;
		JGoObject obj = getSelection().getPrimarySelection();

		if ((obj != null) && (obj instanceof OperationNode))
		{
			startnode = (OperationNode) obj;
		}

		JGoListPosition startpos = null;

		if (startnode != null)
		{
			startpos = doc.findObject(startnode);
		}

		JGoListPosition pos = startpos;

		if (pos != null)
		{
			pos = doc.getNextObjectPosAtTop(pos);
		}

		while (pos != null)
		{
			obj = doc.getObjectAtPos(pos);
			pos = doc.getNextObjectPosAtTop(pos);

			if (obj instanceof OperationNode)
			{
				OperationNode pn = (OperationNode) obj;

				if (matchesNodeLabel(pn, c))
				{
					selectObject(pn);
					scrollRectToVisible(pn.getBoundingRect());

					return true;
				}
			}
		}

		pos = doc.getFirstObjectPos();

		while ((pos != null) && (pos != startpos))
		{
			obj = doc.getObjectAtPos(pos);
			pos = doc.getNextObjectPosAtTop(pos);

			if (obj instanceof OperationNode)
			{
				OperationNode pn = (OperationNode) obj;

				if (matchesNodeLabel(pn, c))
				{
					selectObject(pn);
					scrollRectToVisible(pn.getBoundingRect());

					return true;
				}
			}
		}

		return false;
	}

	// State
	protected Point myDefaultLocation = new Point(10, 10);
	protected RecipeEditor myApp = null;
	protected JInternalFrame myInternalFrame = null;
	private Point myPopupPoint = new Point(0, 0);
	private boolean myOriginalScale = true;
	private Point myOriginalDocPosition = new Point(0, 0);
	private double myOriginalDocScale = 1;
	private boolean myCanceling = false;
}
