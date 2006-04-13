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
 * Command for the flipping of edges
 *
 * @author Hugo Flordal
 */

public class FlipEdgeCommand
    implements Command
{
    /** The Edge Edited by this Command */
    private final EditorEdge edge;
    private final String description = "Edge Flipping";

    /**
     * Flips an edge on the specified surface.
     *
     * @param surface the surface edited by this command
     */
    public FlipEdgeCommand(EditorEdge edge)
    {
		this.edge = edge;
    }

    /**
     * Does the Command.
     */
    public void execute()
    {
		edge.flipEdge();
    }

    /** 
     * Undoes the Command
     */    
    public void undo()
    {
		edge.flipEdge();
    }

   	public boolean isSignificant()
	{
		// I have no idea what to return here... I return false, true?!
		return true;
	}

    public String getName()
    {
		return description;
    }
}
