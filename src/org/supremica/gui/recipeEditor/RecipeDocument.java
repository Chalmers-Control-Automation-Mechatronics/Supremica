
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
import javax.swing.*;
import java.io.*;
import com.nwoods.jgo.*;

//import com.nwoods.jgo.examples.*;
//import org.supremica.gui.recipeEditor.*;
// Implement the Recipe model data structure container.
// This class is responsible for loading and storing documents to files,
// and for creating activities (nodes) and flows (links).
//
// RecipeDocument, for this example app, has just a few properties:
// Name, Location and Link Pen.
// The latter two may appear to belong to a view instead of being
// part of a document, but here these attributes can be conveniently
// stored persistently.
public class RecipeDocument
	extends JGoDocument
{
	public RecipeDocument()
	{
		setUndoManager(new JGoUndoManager());

		myLinksLayer = addLayerAfter(getFirstLayer());
	}

	// Basic properties: name and location (pathname)
	public String getName()
	{
		return myName;
	}

	public void setName(String newname)
	{
		String oldName = myName;

		if (!oldName.equals(newname))
		{
			myName = newname;

			fireUpdate(NAME_CHANGED, 0, null, 0, oldName);
		}
	}

	public String getLocation()
	{
		return myLocation;
	}

	public void setLocation(String newloc)
	{
		String oldLocation = myLocation;

		if (!oldLocation.equals(newloc))
		{
			myLocation = newloc;

			fireUpdate(LOCATION_CHANGED, 0, null, 0, oldLocation);
			updateLocationModifiable();
		}
	}

	public boolean getLinksJumpOver()
	{
		return myLinksJumpOver;
	}

	public void setLinksJumpOver(boolean b)
	{
		boolean old = myLinksJumpOver;

		if (old != b)
		{
			myLinksJumpOver = b;

			fireUpdate(LINKS_JUMP_OVER_CHANGED, 0, null, (old
														  ? 1
														  : 0), null);

			// now update all links
			JGoListPosition pos = getFirstObjectPos();

			while (pos != null)
			{
				JGoObject obj = getObjectAtPos(pos);

				// only consider top-level objects
				pos = getNextObjectPosAtTop(pos);

				if (obj instanceof JGoLink)
				{
					JGoLink link = (JGoLink) obj;

					link.setJumpsOver(b);
				}
			}
		}
	}

	public JGoLayer getLinksLayer()
	{
		return myLinksLayer;
	}

	// read-only property--can the file be written?
	public boolean isLocationModifiable()
	{
		return myIsLocationModifiable;    // just return cached value
	}

	// There's no setLocationModifiable, because that's controlled externally
	// in the file system.  But because we're caching the writableness,
	// we need a method to update the cache.
	public void updateLocationModifiable()
	{
		boolean canwrite = true;

		if (!getLocation().equals(""))
		{
			File file = new File(getLocation());

			if (file.exists() &&!file.canWrite())
			{
				canwrite = false;
			}
		}

		if (isLocationModifiable() != canwrite)
		{
			boolean oldIsModifiable = isModifiable();

			myIsLocationModifiable = canwrite;

			if (oldIsModifiable != isModifiable())
			{
				fireUpdate(JGoDocumentEvent.MODIFIABLE_CHANGED, 0, null, (oldIsModifiable
																		  ? 1
																		  : 0), null);
			}
		}
	}

	// override to include whether the file can be written
	public boolean isModifiable()
	{
		return super.isModifiable() && isLocationModifiable();
	}

	public void updatePaperColor()
	{
		if (isModifiable())
		{
			setPaperColor(Color.white);
		}
		else
		{
			setPaperColor(new Color(0xDD, 0xDD, 0xDD));
		}

		if ((myHighlightPen != null) &&!getPaperColor().equals(myHighlightPen.getColor()))
		{
			myHighlightPen = JGoPen.make(JGoPen.SOLID, 6, getPaperColor());

			// now update all links
			JGoListPosition pos = getFirstObjectPos();

			while (pos != null)
			{
				JGoObject obj = getObjectAtPos(pos);

				// only consider top-level objects
				pos = getNextObjectPosAtTop(pos);

				if (obj instanceof JGoLink)
				{
					JGoLink link = (JGoLink) obj;

					link.setHighlight(myHighlightPen);
				}
			}
		}
	}

	// new property--has the document been changed?
	public boolean isModified()
	{
		return myIsModified;
	}

	public void setModified(boolean b)
	{
		if (myIsModified != b)
		{
			myIsModified = b;

			// don't need to notify document listeners
		}
	}

	// Some, but not all, changes to the document should make it "modified"
	public void fireUpdate(int hint, int flags, Object object, int prevInt, Object prevVal)
	{

		// changing the read-only-ness isn't considered modifying the document
		if (hint == JGoDocumentEvent.MODIFIABLE_CHANGED)
		{
			updatePaperColor();
		}
		else if (hint != JGoDocumentEvent.PAPER_COLOR_CHANGED)
		{

			// don't consider the paper color as part of the document, either
			setModified(true);
		}

		if ((hint == JGoDocumentEvent.REMOVED) && (object instanceof JGoLink))
		{
			JGoLink l = (JGoLink) object;

			if ((l.getToPort() != null) && (l.getToPort().getParent() instanceof BasicNode))
			{
				BasicNode bn = (BasicNode) l.getToPort().getParent();

				if (bn.getPort().getNumLinks() == 0)
				{
					removeObject(bn);

					if (bn instanceof RemoteConnectorNode)
					{
						RemoteConnectorNode other = ((RemoteConnectorNode) bn).getOtherConnector();

						removeObject(other);
					}
				}
			}

			if ((l.getFromPort() != null) && (l.getFromPort().getParent() instanceof BasicNode))
			{
				BasicNode bn = (BasicNode) l.getFromPort().getParent();

				if (bn.getPort().getNumLinks() == 0)
				{
					removeObject(bn);

					if (bn instanceof RemoteConnectorNode)
					{
						RemoteConnectorNode other = ((RemoteConnectorNode) bn).getOtherConnector();

						removeObject(other);
					}
				}
			}
		}
		else if (hint == JGoDocumentEvent.CHANGED)
		{
			if ((flags == JGoText.ChangedText) && (object instanceof JGoText))
			{
				JGoText lab = (JGoText) object;

				if ((lab.getParent() != null) && (lab.getParent() instanceof RemoteConnectorNode))
				{
					RemoteConnectorNode bn = (RemoteConnectorNode) lab.getParent();
					RemoteConnectorNode other = bn.getOtherConnector();

					if (other != null)
					{
						other.setText(bn.getText());
					}
				}
			}
		}

		super.fireUpdate(hint, flags, object, prevInt, prevVal);
	}

	// creating a new activity
	public OperationNode newNode(int acttype)
	{
		OperationNode snode = new OperationNode();

		snode.initialize(acttype, getNextNodeID());
		addObjectAtTail(snode);
		snode.addScatteredPorts((int) (Math.random() * 5) + 1);

		return snode;
	}

	public int getNextNodeID()
	{
		return ++myLastNodeID;
	}

	public OperationNode findNodeByID(int id)
	{

		// for larger documents, it would be more efficient to keep a
		// hash table mapping id to OperationNode
		// for this example, we won't bother with the hash table
		JGoListPosition pos = getFirstObjectPos();

		while (pos != null)
		{
			JGoObject obj = getObjectAtPos(pos);

			// only consider top-level objects
			pos = getNextObjectPosAtTop(pos);

			if (obj instanceof OperationNode)
			{
				OperationNode node = (OperationNode) obj;

				if (node.getID() == id)
				{
					return node;
				}
			}
		}

		return null;
	}

	// creating a new flow between activities
	public FlowLink newLink(JGoPort from, JGoPort to)
	{
		FlowLink ll = new FlowLink(from, to);

		ll.setPen(getLinkPen());
		ll.setHighlight(myHighlightPen);
		ll.setJumpsOver(getLinksJumpOver());
		getLinksLayer().addObjectAtTail(ll);
		ll.calculateStroke();

		return ll;
	}

	public JGoPen getLinkPen()
	{
		return myPen;
	}

	public void setLinkPen(JGoPen p)
	{
		if (!myPen.equals(p))
		{
			myPen = p;

			// now update all links
			JGoListPosition pos = getFirstObjectPos();

			while (pos != null)
			{
				JGoObject obj = getObjectAtPos(pos);

				// only consider top-level objects
				pos = getNextObjectPosAtTop(pos);

				if (obj instanceof JGoLink)
				{
					JGoLink link = (JGoLink) obj;

					link.setPen(p);
				}
			}
		}
	}

	public JGoPen getLinkHighlightPen()
	{
		return myHighlightPen;
	}

	public void setLinkHighlightPen(JGoPen p)
	{
		if (!myHighlightPen.equals(p))
		{
			myHighlightPen = p;

			// now update all links
			JGoListPosition pos = getFirstObjectPos();

			while (pos != null)
			{
				JGoObject obj = getObjectAtPos(pos);

				// only consider top-level objects
				pos = getNextObjectPosAtTop(pos);

				if (obj instanceof JGoLink)
				{
					JGoLink link = (JGoLink) obj;

					link.setHighlight(p);
				}
			}
		}
	}

	// override these for application-specific smarts about routing constraints
	public boolean isAvoidable(JGoObject obj)
	{
		if (!obj.isVisible())
		{
			return false;
		}

		return (obj instanceof OperationNode);
	}

	public Rectangle getAvoidableRectangle(JGoObject obj, Rectangle rect)
	{
		if (rect == null)
		{
			rect = new Rectangle();
		}

		if (obj instanceof OperationNode)
		{
			OperationNode node = (OperationNode) obj;

			if (node.getIcon() != null)
			{
				rect.setBounds(node.getIcon().getBoundingRect());
			}
		}

		return rect;
	}

	public int getNextConnectorID()
	{
		return ++myLastConnectorID;
	}

	// For this sample application, just read and write process documents
	// as files using the default serialization or as a simple XML document.
	public static RecipeDocument open(Component parent, String defaultLocation)
	{
		JFileChooser chooser = new JFileChooser();

		if ((defaultLocation != null) && (!defaultLocation.equals("")))
		{
			File currentFile = new File(defaultLocation);

			chooser.setCurrentDirectory(currentFile);
		}
		else
		{
			chooser.setCurrentDirectory(null);
		}

		WFLFilter wflFilter = new WFLFilter();
		XMLFilter xmlFilter = new XMLFilter();

		chooser.addChoosableFileFilter(wflFilter);
		chooser.addChoosableFileFilter(xmlFilter);
		chooser.setFileFilter(wflFilter);

		int returnVal = chooser.showOpenDialog(parent);

		if (returnVal == JFileChooser.APPROVE_OPTION)
		{
			String name = chooser.getSelectedFile().getName();
			String loc = chooser.getSelectedFile().getAbsolutePath();
			FileInputStream fstream = null;

			try
			{
				fstream = new FileInputStream(loc);

				RecipeDocument doc = null;

				if (loc.endsWith(".xml"))
				{
					doc = loadXML(fstream);
				}
				else
				{
					doc = loadObjects(fstream);
				}

				if (doc == null)
				{
					return null;
				}

				doc.setName(name);
				doc.updateLocationModifiable();
				doc.updatePaperColor();
				doc.setModified(false);

				// the UndoManager is transient and must be setup again when
				// created from serialization
				// but we also need to ignore all changes up to now anyway,
				// so we'll just throw away the old manager and create a new one
				doc.setUndoManager(new JGoUndoManager());

				return doc;
			}
			catch (IOException x)
			{
				JOptionPane.showMessageDialog(null, x, "Open Document Error", javax.swing.JOptionPane.ERROR_MESSAGE);

				return null;
			}
			catch (Exception x)
			{
				JOptionPane.showMessageDialog(null, x, "Loading Document Exception", javax.swing.JOptionPane.ERROR_MESSAGE);

				return null;
			}
			finally
			{
				try
				{
					if (fstream != null)
					{
						fstream.close();
					}
				}
				catch (Exception x) {}
			}
		}
		else
		{
			return null;
		}
	}

	public void save()
	{
		if (getLocation().equals(""))
		{
			saveAs(".wfl");
		}
		else
		{
			store();
		}
	}

	public void store()
	{
		if (!getLocation().equals(""))
		{
			FileOutputStream fstream = null;

			try
			{
				fstream = new FileOutputStream(getLocation());

				if (getLocation().endsWith(".xml"))
				{
					storeXML(fstream);
				}
				else
				{
					storeObjects(fstream);
				}
			}
			catch (Exception x)
			{
				JOptionPane.showMessageDialog(null, x, "Save Document Error", javax.swing.JOptionPane.ERROR_MESSAGE);
			}
			finally
			{
				try
				{
					if (fstream != null)
					{
						fstream.close();
					}
				}
				catch (Exception x) {}

				setModified(false);
			}
		}
	}

	public void saveAs(String fileType)
	{
		JFileChooser chooser = new JFileChooser();
		String loc = getLocation();
		File currentFile = new File(loc);

		chooser.setCurrentDirectory(currentFile);

		WFLFilter wflFilter = new WFLFilter();
		XMLFilter xmlFilter = new XMLFilter();

		chooser.addChoosableFileFilter(wflFilter);
		chooser.addChoosableFileFilter(xmlFilter);

		if (fileType.equalsIgnoreCase(".xml"))
		{
			chooser.setFileFilter(xmlFilter);
		}
		else
		{
			chooser.setFileFilter(wflFilter);
		}

		int returnVal = chooser.showSaveDialog(null);

		if (returnVal == JFileChooser.APPROVE_OPTION)
		{
			String ext = ".wfl";
			javax.swing.filechooser.FileFilter fileFilter = chooser.getFileFilter();

			if (fileFilter.getDescription() == ".xml")
			{
				ext = ".xml";
			}

			String name = chooser.getSelectedFile().getName();

			setName(name);

			loc = chooser.getSelectedFile().getAbsolutePath();

			String loc2 = loc.toLowerCase();

			if (loc2.indexOf(".") == -1)
			{
				loc += ext;
			}

			setLocation(loc);
			store();
		}
	}

	static public RecipeDocument loadObjects(InputStream ins)
		throws IOException, ClassNotFoundException
	{
		ObjectInputStream istream = new ObjectInputStream(ins);
		Object newObj = istream.readObject();

		if (newObj instanceof RecipeDocument)
		{
			RecipeDocument doc = (RecipeDocument) newObj;

			return doc;
		}
		else
		{
			return null;
		}
	}

	public void storeObjects(OutputStream outs)
		throws IOException
	{
		ObjectOutputStream ostream = new ObjectOutputStream(outs);

		ostream.writeObject(this);
		ostream.flush();
	}

	static public RecipeDocument loadXML(InputStream ins)
		throws IOException, UnsupportedOperationException
	{
		throw new UnsupportedOperationException("XML support not enabled.  Edit RecipeDocument.java to un-comment-out /*XML ... XML*/ code.");
	}

	public void storeXML(OutputStream outs)
		throws IOException, UnsupportedOperationException
	{
		throw new UnsupportedOperationException("XML support not enabled.  Edit RecipeDocument.java to un-comment-out /*XML ... XML*/ code.");
	}

	public void copyNewValueForRedo(JGoDocumentChangedEdit e)
	{
		switch (e.getHint())
		{

		case NAME_CHANGED :
			e.setNewValue(getName());

			return;

		case LOCATION_CHANGED :
			e.setNewValue(getLocation());

			return;

		case LINKS_JUMP_OVER_CHANGED :
			e.setNewValueBoolean(getLinksJumpOver());

			return;

		default :
			super.copyNewValueForRedo(e);

			return;
		}
	}

	public void changeValue(JGoDocumentChangedEdit e, boolean undo)
	{
		switch (e.getHint())
		{

		case NAME_CHANGED :
			setName((String) e.getValue(undo));

			return;

		case LOCATION_CHANGED :
			setLocation((String) e.getValue(undo));

			return;

		case LINKS_JUMP_OVER_CHANGED :
			setLinksJumpOver(e.getValueBoolean(undo));

			return;

		default :
			super.changeValue(e, undo);

			return;
		}
	}

	public void endTransaction(String pname)
	{
		super.endTransaction(pname);
		AppAction.updateAllActions();
	}

	// Constants
	private static final String processTag = "Recipe";
	private static final String activityTag = "Operation";
	private static final String flowTag = "Flow";

	// Event hints
	public static final int NAME_CHANGED = JGoDocumentEvent.LAST + 1;
	public static final int LOCATION_CHANGED = JGoDocumentEvent.LAST + 2;
	public static final int LINKS_JUMP_OVER_CHANGED = JGoDocumentEvent.LAST + 3;

	// State
	private String myName = "";
	private String myLocation = "";
	private boolean myLinksJumpOver = true;
	private JGoLayer myLinksLayer = null;
	private int myLastNodeID = -1;
	private int myLastConnectorID = 0;
	private JGoPen myPen = JGoPen.make(JGoPen.SOLID, 2, Color.blue);
	private JGoPen myHighlightPen = JGoPen.make(JGoPen.SOLID, 6, Color.white);
	private transient boolean myIsLocationModifiable = true;
	private transient boolean myIsModified = false;
}
