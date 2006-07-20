package net.sourceforge.waters.gui.command;

import net.sourceforge.waters.gui.ControlledSurface;
import net.sourceforge.waters.model.module.SimpleNodeProxy;

import net.sourceforge.waters.subject.module.EdgeSubject;
import net.sourceforge.waters.subject.module.GraphSubject;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

/**
 * the Command for deleting of edges
 *
 * @author Simon Ware
 */
public class DeleteEdgeCommand
    implements Command
{

  /** The ControlledSurface Edited with this Command */
  private final GraphSubject mGraph;
  /** The Node Created by this Command */
  private final EdgeSubject mDeleted;

  private final String mDescription = "Edge Deletion";

  /**
   * Deletes an edge on the specified surface.
   *
   * @param surface the surface edited by this command
   */
  public DeleteEdgeCommand(GraphSubject graph, EdgeSubject edge)
  {
    mGraph = graph;
    mDeleted = edge;
  }

  /**
   * Does the Command.
   */

  public void execute()
  {
		mGraph.getEdgesModifiable().remove(mDeleted);
	}


  /** 
   * Undoes the Command.
   */    

  public void undo()
  {
    mGraph.getEdgesModifiable().add(mDeleted);
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
