package net.sourceforge.waters.gui.command;

import net.sourceforge.waters.gui.ControlledSurface;
import net.sourceforge.waters.gui.EditorEdge;
import net.sourceforge.waters.gui.EditorNode;
import net.sourceforge.waters.gui.EditorNodeGroup;
import net.sourceforge.waters.model.module.SimpleNodeProxy;

import java.util.Collection;
import java.util.LinkedList;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

/**
 * the Command for Creation of nodes
 *
 * @author Simon Ware
 */

public class DeleteNodeGroupCommand
    implements Command
{
    /** The ControlledSurface Edited with this Command */
    private final ControlledSurface mSurface;
    /** The Node Created by this Command */
    private final EditorNodeGroup mDeleted;
    /** the Edge Deletion Commands Associated with this Command */
    private final CompoundCommand mCommands = new CompoundCommand();
    private final String mDescription = "Group Node Deletion";

    /**
     * Constructs a new CreateNodeCommand with the specified surface and
     * creates the node in the x,y position specified
     *
     * @param surface the surface edited by this command
     * @param x,y the position upon which the node is created
     */
    public DeleteNodeGroupCommand(ControlledSurface surface, EditorNodeGroup nodeGroup)
    {
		mSurface = surface;
		// Find a unique name!
		mDeleted = nodeGroup;
		for (Object o: surface.getEdges()) {
			EditorEdge e = (EditorEdge)o;
			if ((e.getStartNode() == nodeGroup)) {
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
		mSurface.delNodeGroup(mDeleted);
		mSurface.getEditorInterface().setDisplayed();
    }

    /** 
     * Undoes the Command
     */    

	public boolean isSignificant()
	{
		return true;
	}
	 
    public void undo()
    {
		mSurface.addNodeGroup(mDeleted);
		mCommands.undo();
		mSurface.getEditorInterface().setDisplayed();
    }

    public String getName()
    {
	return mDescription;
    }
}
