package net.sourceforge.waters.gui.command;

import net.sourceforge.waters.gui.EditorGraph;
import net.sourceforge.waters.subject.module.SimpleNodeSubject;
import net.sourceforge.waters.subject.module.GraphSubject;

import net.sourceforge.waters.subject.module.EdgeSubject;

/**
 * the Command for Creation of nodes
 *
 * @author Simon Ware
 */

public class DeleteNodeCommand
    implements Command
{
  /** The ControlledSurface Edited with this Command */
  private final GraphSubject mGraph;
  /** The Node Removed by this Command */
  private final SimpleNodeSubject mDeleted;
  /** the Edge Deletion Commands Associated with this Command */
  //private final CompoundCommand mCommands = new CompoundCommand();
  private final String mDescription = "Node Deletion";

  /**
   * Constructs a new DeleteNodeCommand with the specified surface and
   * deletes the node specified
   *
   * @param graph the surface edited by this command
   * @param node the node which is to be removed
   */
  public DeleteNodeCommand(GraphSubject graph, SimpleNodeSubject node)
  {
    mGraph = graph;
    mDeleted = node;
    /*
    //find all attached edges
    for (EdgeSubject e : mGraph.getEdgesModifiable()) {
      if ((e.getSource() == node) || (e.getTarget() == node)) {
        mCommands.addCommand(new DeleteEdgeCommand(mGraph, e));
      }
    }
    mCommands.end();
    */
  }

  /**
   * Executes the Creation of the Node
   */

  public void execute()
  {
    //mCommands.execute();
    mGraph.getNodesModifiable().remove(mDeleted);
    EditorGraph.updateChildNodes(mGraph);
  }

  /** 
   * Undoes the Command
   */    

  public void undo()
  {
    mGraph.getNodesModifiable().add(mDeleted);
    //mCommands.undo();
    EditorGraph.updateChildNodes(mGraph);
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
