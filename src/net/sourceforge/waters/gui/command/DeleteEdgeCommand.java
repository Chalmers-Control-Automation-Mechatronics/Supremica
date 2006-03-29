package net.sourceforge.waters.gui.command;

import net.sourceforge.waters.gui.ControlledSurface;
import net.sourceforge.waters.gui.EditorEdge;
import net.sourceforge.waters.gui.EditorNode;
import net.sourceforge.waters.gui.EditorObject;
import net.sourceforge.waters.model.module.SimpleNodeProxy;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

/**
 * the Command for Creation of nodes
 *
 * @author Simon Ware
 */

public class DeleteEdgeCommand
    implements Command
{

    /** The ControlledSurface Edited with this Command */
    private final ControlledSurface mSurface;
    /** The Node Created by this Command */
    private final EditorEdge mDeleted;
    private final String mDescription = "Edge Deletion";

    /**
     * Deletes an edge on the specified surface.
     *
     * @param surface the surface edited by this command
     */
    public DeleteEdgeCommand(ControlledSurface surface, EditorEdge edge)
    {
		mSurface = surface;
		mDeleted = edge;
    }

    /**
     * Executes the Creation of the Node
     */

    public void execute()
    {
		mSurface.delEdge(mDeleted);
		mSurface.getEditorInterface().setDisplayed();
    }

    /** 
     * Undoes the Command
     */    

    public void undo()
    {
	mSurface.addEdge(mDeleted);
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
