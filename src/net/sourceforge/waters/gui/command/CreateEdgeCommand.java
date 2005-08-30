package net.sourceforge.waters.gui.command;

import net.sourceforge.waters.gui.ControlledSurface;
import net.sourceforge.waters.gui.EditorEdge;
import net.sourceforge.waters.gui.EditorNode;
import net.sourceforge.waters.gui.EditorNodeGroup;
import net.sourceforge.waters.gui.EditorObject;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.EdgeProxy;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

/**
 * the Command for Creation of nodes
 *
 * @author Simon Ware
 */

public class CreateEdgeCommand
    extends AbstractUndoableEdit
    implements Command
{

    /** The ControlledSurface Edited with this Command */
    private final ControlledSurface mSurface;
    /** The Node Created by this Command */
    private final EditorEdge mCreated;

    /**
     * Constructs a new CreateNodeCommand with the specified surface and
     * creates the node in the x,y position specified
     *
     * @param surface the surface edited by this command
     * @param x,y the position upon which the node is created
     */
    public CreateEdgeCommand(ControlledSurface surface, EditorObject source, EditorNode target, int x, int y)
    {
	mSurface = surface;
	// Find a unique name!
	// Create a new EdgeProxy
	EdgeProxy ep;
	if (source.getType() == EditorObject.NODE)
	    {
		ep = new EdgeProxy((NodeProxy) ((EditorNode) source).getProxy(), (NodeProxy) target.getProxy());
	    }
	else
	    {
		ep = new EdgeProxy((NodeProxy) ((EditorNodeGroup) source).getProxy(), (NodeProxy) target.getProxy());
	    }
	mCreated = new EditorEdge(source, target, x, y, ep);
    }

    /**
     * Executes the Creation of the Node
     */

    public void execute()
    {
	mSurface.addEdge(mCreated);
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
	mSurface.delEdge(mCreated);
	mSurface.getEditorInterface().setDisplayed();
    }

    public String getPresentationName()
    {
	return this.getClass().getName();
    }
}
