
/*
 * Test of JGo for Grafchart
 *
 */
package org.jgrafchart;



import java.awt.*;
import java.awt.event.*;
import java.awt.dnd.*;
import java.awt.datatransfer.*;
import java.awt.geom.*;
import java.awt.print.*;

import javax.swing.*;
import javax.swing.border.*;

import java.util.*;

import com.nwoods.jgo.*;


public class GCView
	extends JGoGridView
	implements JGoViewListener
{

	private int stepCounter = 0;
	private int macroStepCounter = 0;
	protected Point myDefaultLocation = new Point(10, 10);
	protected Basic2GC myApp = null;
	public JInternalFrame myInternalFrame = null;
	private GrafcetThread grafcetThread;
	private boolean doIt = true;
	public boolean executing = false;
	public boolean compiledOnce = false;
	public int layer = 1;

	public GCView()
	{

		super();

		setSnapMove(SnapJump);

		// setGridSpot(JGoObject.Left);
		setGridWidth(10);
		setGridHeight(10);
		setKeyEnabled(true);
		setDefaultPortGravity(150);
		setBorder(new TitledBorder("Editor"));
		getDoc().setModifiable(true);
	}

	public GCView(JGoDocument doc)
	{

		super(doc);

		setSnapMove(SnapJump);

		// setGridSpot(JGoObject.Left);
		setGridWidth(10);
		setGridHeight(10);
		setKeyEnabled(true);
		setDefaultPortGravity(150);
		setBorder(new TitledBorder("Editor"));
		doc.setModifiable(true);
	}

	public GCView(JGoDocument doc, JInternalFrame frame)
	{

		super(doc);

		setSnapMove(SnapJump);

		// setGridSpot(JGoObject.Left);
		setGridWidth(10);
		setGridHeight(10);
		setKeyEnabled(true);
		setDefaultPortGravity(150);
		doc.setModifiable(true);

		myInternalFrame = frame;
	}

	GCDocument getDoc()
	{
		return (GCDocument) getDocument();
	}

	public void moveSelection(JGoSelection sel, int flags, int offsetx, int offsety, int event)
	{

		int snapmove = getSnapMove();

		if ((snapmove == SnapJump) || ((snapmove == SnapAfter) && (event == EventMouseUp)))
		{

			// snap each object individually
			Vector tlos = null;
			JGoListPosition pos = sel.getFirstObjectPos();
			Point temp = new Point();

			while (pos != null)
			{
				JGoObject obj = sel.getObjectAtPos(pos);

				pos = sel.getNextObjectPos(pos);

				if (obj.isDraggable())
				{
					JGoObject tlo = obj.getTopLevelObject();

					// see if the selected object is not a TopLevelObject
					if (tlo != obj)
					{

						// have we already, or will we later, move this object's TLO?
						if (sel.isSelected(tlo))
						{
							continue;
						}

						// have we already moved this object's TLO because a different
						// part of the same object was selected and moved?
						if ((tlos != null) && tlos.contains(tlo))
						{
							continue;
						}

						// remember each moved TLO
						if (tlos == null)
						{
							tlos = new Vector();
						}

						tlos.add(tlo);
					}

					Point spot = tlo.getLocation(temp);
					int newx = spot.x + offsetx;
					int newy = spot.y + offsety;

					findNearestGridPoint(newx, newy, temp);
					tlo.setLocation(temp);
				}
			}
		}
		else
		{
			super.moveSelection(sel, flags, offsetx, offsety, event);
		}
	}

	public void mySnapObject(JGoObject obj)
	{

		Point temp = new Point();
		JGoObject tlo = obj.getTopLevelObject();

		if (tlo == obj)
		{
			Point spot = obj.getLocation(temp);

			findNearestGridPoint(spot.x, spot.y, temp);
			obj.setLocation(temp);
		}
	}

	public Basic2GC getBasicApp()
	{
		return myApp;
	}

	public JInternalFrame getInternalFrame()
	{
		return myInternalFrame;
	}

	public void initialize(Basic2GC app, JInternalFrame frame)
	{

		myApp = app;
		myInternalFrame = frame;

		addViewListener(this);

		// setGridWidth(10);
		// setGridHeight(10);
		// setSnapMove(JGoGridView.SnapJump);
		// showGrid();
		updateTitle();
		addKeyListener(new KeyAdapter()
		{

			public void keyPressed(KeyEvent evt)
			{

				int t = evt.getKeyCode();

				if (t == KeyEvent.VK_DELETE)
				{
					JGoSelection sel = getSelection();

					// myView.removeDeletedPointers(sel);
					deleteHierarchies(sel);
					deleteSelection();
				}
				else if (evt.isControlDown() && (t == KeyEvent.VK_Q))
				{
					System.exit(0);
				}
			}
		});
		getDocument().addDocumentListener(new JGoDocumentListener()
		{

			public void documentChanged(JGoDocumentEvent e)
			{

				if (e.getHint() == GCDocument.NAME_CHANGED)
				{
					updateTitle();
				}

				myApp.processDocChange(e);
			}
		});
	}

	public void setStepCounter(int n)
	{
		stepCounter = n;
	}

	public int getStepCounter()
	{
		return stepCounter;
	}

	public void updateTitle()
	{

		if (getInternalFrame() != null)
		{
			String title = getDoc().getName();

			getInternalFrame().setTitle(title);
			getInternalFrame().repaint();
		}
	}

	public void deleteHierarchies(JGoSelection sel)
	{

		JGoListPosition pos = sel.getFirstObjectPos();
		JGoObject obj = sel.getObjectAtPos(pos);

		while ((obj != null) && (pos != null))
		{
			if (obj instanceof MacroStep)
			{
				MacroStep ms = (MacroStep) obj;

				if (ms.myContentDocument != null)
				{
					deleteHierarchyDocs(ms.myContentDocument);
				}

				if (ms.frame != null)
				{
					try
					{
						ms.frame.setClosed(true);
					}
					catch (Exception x) {}
				}
			}

			if (obj instanceof GrafcetProcedure)
			{
				GrafcetProcedure ms = (GrafcetProcedure) obj;

				deleteHierarchyDocs(ms.myContentDocument);

				if (ms.frame != null)
				{
					try
					{
						ms.frame.setClosed(true);
					}
					catch (Exception x) {}
				}
			}

			pos = sel.getNextObjectPos(pos);
			obj = sel.getObjectAtPos(pos);
		}
	}

	public void deleteHierarchyDocs(GCDocument doc)
	{

		JGoListPosition pos = doc.getFirstObjectPos();
		JGoObject obj = doc.getObjectAtPos(pos);

		while ((obj != null) && (pos != null))
		{
			if (obj instanceof MacroStep)
			{
				MacroStep ms = (MacroStep) obj;

				deleteHierarchyDocs(ms.myContentDocument);

				if (ms.frame != null)
				{
					try
					{
						ms.frame.setClosed(true);
					}
					catch (Exception x) {}

					ms.frame = null;
					myApp.myCurrentView = ms.parentView;

					myApp.myCurrentView.requestFocus();
					AppAction.updateAllActions();
				}
			}

			if (obj instanceof GrafcetProcedure)
			{
				GrafcetProcedure ms = (GrafcetProcedure) obj;

				deleteHierarchyDocs(ms.myContentDocument);

				if (ms.frame != null)
				{
					try
					{
						ms.frame.setClosed(true);
					}
					catch (Exception x) {}

					ms.frame = null;
					myApp.myCurrentView = ms.parentView;

					myApp.myCurrentView.requestFocus();
					AppAction.updateAllActions();
				}
			}

			pos = doc.getNextObjectPos(pos);
			obj = doc.getObjectAtPos(pos);
		}
	}

	public void viewChanged(JGoViewEvent e)
	{

		// if the selection changed, maybe some commands need to
		// be disabled or re-enabled
		myApp.processViewChange(e);
	}

	protected JGoRectangle myGhost = new JGoRectangle(new Point(), new Dimension());

	public void dragOver(DropTargetDragEvent e)
	{

		super.dragOver(e);

		if (e.getDropAction() != DnDConstants.ACTION_NONE)
		{
			if (myGhost.getView() != this)
			{

				// set a default size for the ghost rectangle
				myGhost.setSize(10, 10);
				addObjectAtTail(myGhost);
			}

			myGhost.setTopLeft(viewToDocCoords(e.getLocation()));
		}
	}

	public void dragExit(DropTargetEvent e)
	{

		if (myGhost.getView() == this)
		{
			removeObject(myGhost);
		}

		super.dragExit(e);
	}

	public boolean isDropFlavorAcceptable(DropTargetDragEvent e)
	{
		return super.isDropFlavorAcceptable(e) || e.isDataFlavorSupported(DataFlavor.stringFlavor);
	}

	public void newLink(JGoPort from, JGoPort to)
	{

		if (!connected(from, to))
		{
			GCLink nlink = new GCLink(from, to);
			GCDocument viewDoc = getDoc();

			if (from instanceof GCStepExceptionOutPort)
			{
				nlink.setWide();
			}

			viewDoc.setSuspendUpdates(true);
			viewDoc.addObjectAtHead(nlink);
			viewDoc.setSuspendUpdates(false);

			// if (from.getParent() instanceof GCStep) {
			// GCStep s = (GCStep)from.getParent();
			// GCTransition t = (GCTransition)to.getParent();
			// s.addSucceedingTransition(t);
			// t.addPrecedingStep(s);
			// }
			// 
			// if (from.getParent() instanceof GCTransition) {
			// GCTransition t = (GCTransition)from.getParent();
			// GCStep s = (GCStep)to.getParent();
			// t.addSucceedingStep(s);
			// s.addPrecedingTransition(t);
			// }
		}
	}

	public boolean connected(JGoPort from, JGoPort to)
	{

		boolean found = false;
		JGoListPosition pos = from.getFirstLinkPos();

		while (pos != null)
		{
			JGoLink l = from.getLinkAtPos(pos);

			found = found || (to == l.getToPort());
			pos = from.getNextLinkPos(pos);
		}

		return found;
	}

	public boolean validLink(JGoPort from, JGoPort to)
	{

		// System.out.println("validLink");
		boolean valid = from.validLink(to);

		// System.out.println(valid);
		return valid;
	}

	public void drop(DropTargetDropEvent e)
	{

		try
		{
			JGoCopyEnvironment map = getDoc().createDefaultCopyEnvironment();

			if (doDrop(e, map))
			{
				Iterator i = map.values().iterator();

				while (i.hasNext())
				{
					Object o = i.next();

					// System.out.println("In drop " + o);
					JGoObject jo = (JGoObject) o;

					mySnapObject(jo);

					// if (jo instanceof ProcedureStep) {
					// ProcedureStep go = (ProcedureStep)jo;
					// go.viewOwner = this;
					// }
					if (o instanceof GCStep)
					{		// Is this needed?
						GCStep obj = (GCStep) o;

						// System.out.println("In drop: GCStep " + obj);
						GCDocument doc = getDoc();

						doc.setSuspendUpdates(true);

						stepCounter++;

						obj.myLabel.setText("S" + stepCounter);
						doc.addObjectAtTail(obj);
						doc.setSuspendUpdates(false);
					}
				}

				return;
			}
		}
		catch (Exception x)
		{
			x.printStackTrace();
		}

		e.rejectDrop();
	}

	// public void removeDeletedPointers(JGoSelection sel) {
	// 
	// JGoObject obj = sel.getPrimarySelection();
	// JGoListPosition pos = sel.getFirstObjectPos();
	// while (obj != null && pos != null) {
	// if (obj instanceof GCStep) {
	// GCStep s = (GCStep)obj;
	// s.removePointers();
	// }
	// if (obj instanceof GCTransition) {
	// GCTransition t = (GCTransition)obj;
	// t.removePointers();
	// }
	// if (obj instanceof GCLink) {
	// GCLink l = (GCLink)obj;
	// l.removePointers();
	// }
	// obj = sel.getObjectAtPos(pos);
	// pos = sel.getNextObjectPos(pos);
	// }
	// }
	private class GrafcetThread
		extends Thread
	{

		public void run()
		{

			while (doIt)
			{
				executeOnce((GCDocument) getDoc());

				try
				{
					sleep(getDoc().getSpeed());
				}
				catch (InterruptedException e) {}
			}
		}
	}

	void executeOnce(GCDocument doc)
	{

		// Read from digital inputs
		JGoListPosition pos = doc.getFirstObjectPos();
		JGoObject obj = doc.getObjectAtPos(pos);

		while ((obj != null) && (pos != null))
		{
			if (obj instanceof DigitalIn)
			{
				DigitalIn digIn = (DigitalIn) obj;

				digIn.readInput();
			}

			pos = doc.getNextObjectPos(pos);
			obj = doc.getObjectAtPos(pos);
		}

		// Step Coding
		stepCodeDocument(doc);

		// Change State
		changeStateDocument(doc);
	}

	void stepCodeDocument(GCDocument doc)
	{

		// System.out.println("stepCodeDocument");
		JGoListPosition pos = doc.getFirstObjectPos();
		JGoObject obj = doc.getObjectAtPos(pos);

		while ((obj != null) && (pos != null))
		{
			if (obj instanceof GenericTransition)
			{
				GenericTransition t = (GenericTransition) obj;

				// System.out.println("TestandFire " + t);
				t.testAndFire();
			}

			if ((obj instanceof MacroStep) &&!(obj instanceof ProcedureStep))
			{
				MacroStep ms = (MacroStep) obj;

				stepCodeDocument(ms.myContentDocument);
			}

			if (obj instanceof ProcedureStep)
			{
				ProcedureStep ps = (ProcedureStep) obj;

				if (ps.myContentDocument != null)
				{
					stepCodeDocument(ps.myContentDocument);
				}
			}

			pos = doc.getNextObjectPos(pos);
			obj = doc.getObjectAtPos(pos);
		}
	}

	void changeStateDocument(GCDocument doc)
	{

		JGoListPosition pos = doc.getFirstObjectPos();
		JGoObject obj = doc.getObjectAtPos(pos);

		while ((obj != null) && (pos != null))
		{
			if (obj instanceof GCStep)
			{
				GCStep s = (GCStep) obj;

				s.changeState();
			}

			if (obj instanceof DigitalOut)
			{
				DigitalOut out = (DigitalOut) obj;

				out.effectuateNormalActions();
			}

			if ((obj instanceof MacroStep) &&!(obj instanceof ProcedureStep))
			{
				MacroStep ms = (MacroStep) obj;

				ms.changeState();
				changeStateDocument(ms.myContentDocument);
			}

			if (obj instanceof ProcedureStep)
			{
				ProcedureStep ps = (ProcedureStep) obj;

				if (ps.myContentDocument != null)
				{
					ps.changeState();
					changeStateDocument(ps.myContentDocument);
				}
			}

			pos = doc.getNextObjectPos(pos);
			obj = doc.getObjectAtPos(pos);
		}
	}

	void initializeDocument(GCDocument doc)
	{

		JGoListPosition pos = doc.getFirstObjectPos();
		JGoObject obj = doc.getObjectAtPos(pos);

		while ((obj != null) && (pos != null))
		{
			if (obj instanceof DigitalIn)
			{
				DigitalIn in = (DigitalIn) obj;

				in.initialize();
			}

			if (obj instanceof DigitalOut)
			{
				DigitalOut in = (DigitalOut) obj;

				in.compile();
			}

			pos = doc.getNextObjectPos(pos);
			obj = doc.getObjectAtPos(pos);
		}

		pos = doc.getFirstObjectPos();
		obj = doc.getObjectAtPos(pos);

		while ((obj != null) && (pos != null))
		{
			if (obj instanceof GCStepInitial)
			{
				GCStepInitial init = (GCStepInitial) obj;

				// System.out.println("Initialize InitialStep");
				init.activate();
				init.executeStoredActions();
				init.executeNormalActions(true);
			}
			;

			if (obj instanceof GCTransition)
			{
				GCTransition t = (GCTransition) obj;

				// System.out.println("Initialize Transition");
				t.initialize();
			}

			if (obj instanceof ExceptionTransition)
			{
				ExceptionTransition et = (ExceptionTransition) obj;

				et.initialize();
			}

			if ((obj instanceof MacroStep) &&!(obj instanceof ProcedureStep))
			{
				MacroStep ms = (MacroStep) obj;

				// System.out.println("Initialize MacroStep");
				initializeDocument(ms.myContentDocument);
			}

			pos = doc.getNextObjectPos(pos);
			obj = doc.getObjectAtPos(pos);
		}
	}

	void stopDocument(GCDocument doc)
	{

		JGoListPosition pos = doc.getFirstObjectPos();
		JGoObject obj = doc.getObjectAtPos(pos);

		while ((obj != null) && (pos != null))
		{
			if (obj instanceof GCStep)
			{
				GCStep s = (GCStep) obj;

				s.deactivate();
				s.changeState();
			}
			;

			if (obj instanceof GCTransition)
			{
				GCTransition t = (GCTransition) obj;

				t.stop();
			}

			if (obj instanceof ExceptionTransition)
			{
				ExceptionTransition t = (ExceptionTransition) obj;

				t.stop();
			}

			if (obj instanceof DigitalIn)
			{
				DigitalIn in = (DigitalIn) obj;

				in.stop();
			}

			if ((obj instanceof MacroStep) &&!(obj instanceof ProcedureStep))
			{
				MacroStep ms = (MacroStep) obj;

				ms.deactivate();
				ms.changeState();
				stopDocument(ms.myContentDocument);
			}

			if (obj instanceof ProcedureStep)
			{
				ProcedureStep ps = (ProcedureStep) obj;

				ps.deactivate();
				ps.changeState();

				ps.myContentDocument = null;
			}

			pos = doc.getNextObjectPos(pos);
			obj = doc.getObjectAtPos(pos);
		}
	}

	public void start()
	{

		executing = true;
		doIt = true;
		grafcetThread = new GrafcetThread();

		grafcetThread.start();
	}

	public void stopThread()
	{
		executing = false;
		doIt = false;
	}
}
