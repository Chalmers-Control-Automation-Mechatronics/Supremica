package net.sourceforge.waters.gui.command;

import net.sourceforge.waters.gui.ControlledSurface;
import net.sourceforge.waters.gui.EditorNode;
import net.sourceforge.waters.gui.EditorEdge;
import net.sourceforge.waters.model.module.SimpleNodeProxy;

import java.util.Collection;
import java.util.LinkedList;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CompoundEdit;

/**
 * the Command for Creation of nodes
 *
 * @author Simon Ware
 */

public class DeleteNodeCommand 
    implements Command
{
    /** The ControlledSurface Edited with this Command */
    private final ControlledSurface mSurface;
    /** The Node Removed by this Command */
    private final EditorNode mDeleted;
    /** the Edge Deletion Commands Associated with this Command */
    private final CompoundCommand mCommands = new CompoundCommand();
    private final String mDescription = "Node Deletion";

    /**
     * Constructs a new DeleteNodeCommand with the specified surface and
     * deletes the node specified
     *
     * @param surface the surface edited by this command
     * @param node the node which is to be removed
     */
    public DeleteNodeCommand(ControlledSurface surface, EditorNode node)
    {
		mSurface = surface;
		mDeleted = node;
		//find all attached edges
		for (Object o: surface.getEdges()) {
			EditorEdge e = (EditorEdge)o;
			if ((e.getEndNode() == node) || (e.getStartNode() == node)) {
				mCommands.addCommand(new DeleteEdgeCommand(mSurface, e));
			}
		}
		mCommands.end();
    }

    /**
     * Executes the Creation of the Node
     */

    public void execute()
    {
		mCommands.execute();
		mSurface.delNode(mDeleted);
		mSurface.getEditorInterface().setDisplayed();
    }

    /** 
     * Undoes the Command
     */    

    public void undo()
    {
		mSurface.addNode(mDeleted);
		mCommands.undo();
		mSurface.getEditorInterface().setDisplayed();
    }

	public boolean isSignificant()
	{
		return true;
	}
	
    public String getName()
    {
		return mDescription;
    }
}
