package net.sourceforge.waters.gui.command;

import net.sourceforge.waters.gui.EditorGraph;
import net.sourceforge.waters.subject.module.GroupNodeSubject;
import net.sourceforge.waters.subject.module.GraphSubject;

import net.sourceforge.waters.subject.module.EdgeSubject;

/**
 * the Command for Creation of nodes
 *
 * @author Simon Ware
 */

public class DeleteNodeGroupCommand
    implements Command
{
    /** The ControlledSurface Edited with this Command */
    private final GraphSubject mGraph;
    /** The Node Created by this Command */
    private final GroupNodeSubject mDeleted;
    /** the Edge Deletion Commands Associated with this Command */
    //private final CompoundCommand mCommands = new CompoundCommand();
    private final String mDescription = "Group Node Deletion";

    /**
     * Constructs a new CreateNodeCommand with the specified surface and
     * creates the node in the x,y position specified
     *
     * @param graph the surface edited by this command
     */
    public DeleteNodeGroupCommand(GraphSubject graph, GroupNodeSubject nodeGroup)
    {
		mGraph = graph;
		// Find a unique name!
		mDeleted = nodeGroup;
		/*
		for (EdgeSubject e : mGraph.getEdgesModifiable()) {
			if ((e.getSource() == nodeGroup || (e.getTarget() == nodeGroup))) {
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

	public boolean isSignificant()
	{
		return true;
	}
	 
    public void undo()
    {
      mGraph.getNodesModifiable().add(mDeleted);
      EditorGraph.updateChildNodes(mGraph);
      //mCommands.undo();
    }

    public String getName()
    {
	return mDescription;
    }
}
