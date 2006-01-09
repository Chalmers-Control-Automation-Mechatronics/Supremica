package net.sourceforge.waters.gui.command;

import net.sourceforge.waters.gui.EditorSurface;
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
    extends AbstractUndoableEdit
    implements Command
{
    private boolean mFirstExecution = true;
    /** The EditorSurface Edited with this Command */
    private final EditorSurface mSurface;
    /** The Node Created by this Command */
    private final EditorNodeGroup mDeleted;
    /** the Edge Deletion Commands Associated with this Command */
    private final Collection<DeleteEdgeCommand> mDelEdge = new LinkedList();
    private final String mDescription = "Group Node Deletion";

    /**
     * Constructs a new CreateNodeCommand with the specified surface and
     * creates the node in the x,y position specified
     *
     * @param surface the surface edited by this command
     * @param x,y the position upon which the node is created
     */
    public DeleteNodeGroupCommand(EditorSurface surface, EditorNodeGroup nodeGroup)
    {
	mSurface = surface;
	// Find a unique name!
	mDeleted = nodeGroup;
	for (Object o: surface.getEdges()) {
	    EditorEdge e = (EditorEdge)o;
	    if ((e.getStartNode() == nodeGroup)) {
		mDelEdge.add(new DeleteEdgeCommand(mSurface, e));
	    }
	}
    }

    /**
     * Executes the Creation of the Node
     */

    public void execute()
    {
	if (mFirstExecution) {
	    for (DeleteEdgeCommand d : mDelEdge) {
		d.execute();
	    }
	} else {
	    for (DeleteEdgeCommand d : mDelEdge) {
		d.redo();
	    }
	}
	mSurface.delNodeGroup(mDeleted);
	mSurface.getEditorInterface().setDisplayed();
	mFirstExecution = false;
    }

    /** 
     * Redoes the Command
     *
     * @throws CannotRedoException if CanRedo returns false
     */
    
    public void redo() throws CannotRedoException
    {
	super.redo();
	execute();
    }

    /** 
     * Undoes the Command
     *
     * @throws CannotUndoException if CanUndo returns false
     */    

    public void undo() throws CannotUndoException
    {
	super.undo();
	mSurface.addNodeGroup(mDeleted);
	for (DeleteEdgeCommand d : mDelEdge) {
	    d.undo();
	}
	mSurface.getEditorInterface().setDisplayed();
    }

    public String getPresentationName()
    {
	return mDescription;
    }
}
