package net.sourceforge.waters.gui.command;

import net.sourceforge.waters.gui.ControlledSurface;
import net.sourceforge.waters.gui.EditorNode;
import net.sourceforge.waters.gui.EditorNodeGroup;
import net.sourceforge.waters.model.module.BoxGeometryProxy;
import net.sourceforge.waters.model.module.GroupNodeProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

/**
 * the Command for Creation of nodes
 *
 * @author Simon Ware
 */

public class CreateNodeGroupCommand
    extends AbstractUndoableEdit
    implements Command
{

    /** The ControlledSurface Edited with this Command */
    private final ControlledSurface mSurface;
    /** The Node Created by this Command */
    private final EditorNodeGroup mCreated;
    private final String mDescription = "Group Node Creation";
    /**
     * Constructs a new CreateNodeCommand with the specified surface and
     * creates the node in the x,y position specified
     *
     * @param surface the surface edited by this command
     * @param x,y the position upon which the node is created
     */
    public CreateNodeGroupCommand(ControlledSurface surface, EditorNodeGroup created)
    {
	mSurface = surface;
	mCreated = created;       
    }

    /**
     * Executes the Creation of the Node
     */

    public void execute()
    {
	mSurface.addNodeGroup(mCreated);
	mSurface.unselectAll();
	mSurface.select(mCreated);	
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
	mSurface.delNodeGroup(mCreated);
	mSurface.unselectAll();
	mSurface.getEditorInterface().setDisplayed();
    }

    public String getPresentationName()
    {
	return mDescription;
    }
}
