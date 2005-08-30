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

/**
 * the Command for Creation of nodes
 *
 * @author Simon Ware
 */

public class DeleteNodeCommand
    extends AbstractUndoableEdit
    implements Command
{
    private boolean mFirstExecution = true;
    /** The ControlledSurface Edited with this Command */
    private final ControlledSurface mSurface;
    /** The Node Removed by this Command */
    private final EditorNode mDeleted;
    /** the Edge Deletion Commands Associated with this Command */
    private final Collection<DeleteEdgeCommand> mDelEdge = new LinkedList();

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
	mSurface.delNode(mDeleted);
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
	mSurface.addNode(mDeleted);
	for (DeleteEdgeCommand d : mDelEdge) {
	    d.undo();
	}
	mSurface.getEditorInterface().setDisplayed();
    }

    public String getPresentationName()
    {
	return this.getClass().getName();
    }
}
