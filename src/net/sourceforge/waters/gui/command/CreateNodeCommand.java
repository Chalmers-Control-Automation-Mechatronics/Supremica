package net.sourceforge.waters.gui.command;

import net.sourceforge.waters.gui.ControlledSurface;
import net.sourceforge.waters.gui.EditorNode;
import net.sourceforge.waters.model.module.SimpleNodeProxy;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

/**
 * the Command for Creation of nodes
 *
 * @author Simon Ware
 */

public class CreateNodeCommand
    extends AbstractUndoableEdit
    implements Command
{

    /** The ControlledSurface Edited with this Command */
    private final ControlledSurface mSurface;
    /** The Node Created by this Command */
    private final EditorNode mCreated;

    /**
     * Constructs a new CreateNodeCommand with the specified surface and
     * creates the node in the x,y position specified
     *
     * @param surface the surface edited by this command
     * @param x,y the position upon which the node is created
     */
    public CreateNodeCommand(ControlledSurface surface, int x, int y)
    {
	mSurface = surface;
	// Find a unique name!
	int i = 0;
	for (i = 0; i <= mSurface.getNodes().size(); i++) {
	    boolean found = false;
	    for (int j=0; j<mSurface.getNodes().size(); j++) {
		if (((EditorNode) mSurface.getNodes().get(j)).getName().equals("s" + i)) {
		    found = true;
		    break;
		}
	    }
	    if (!found) {
		break;
	    }
	}
	mCreated = new EditorNode(x, y, new SimpleNodeProxy("s" + i), surface);       
    }

    /**
     * Executes the Creation of the Node
     */

    public void execute()
    {
	mSurface.addNode(mCreated);
	mSurface.getEditorInterface().setDisplayed();
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
	mSurface.delNode(mCreated);
	mSurface.getEditorInterface().setDisplayed();
    }
}
