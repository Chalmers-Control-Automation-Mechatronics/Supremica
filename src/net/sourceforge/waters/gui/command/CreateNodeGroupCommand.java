package net.sourceforge.waters.gui.command;

import net.sourceforge.waters.gui.ControlledSurface;
import net.sourceforge.waters.model.module.BoxGeometryProxy;
import net.sourceforge.waters.model.module.GroupNodeProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import net.sourceforge.waters.subject.module.GraphSubject;
import net.sourceforge.waters.subject.module.GroupNodeSubject;
import java.awt.geom.Rectangle2D;
import net.sourceforge.waters.subject.module.BoxGeometrySubject;
import java.util.Collection;
import net.sourceforge.waters.model.module.NodeProxy;
import java.util.Collections;
import net.sourceforge.waters.subject.module.PlainEventListSubject;

/**
 * the Command for Creation of nodes
 *
 * @author Simon Ware
 */

public class CreateNodeGroupCommand
    implements Command
{

  /** The ControlledSurface Edited with this Command */
  private final GraphSubject mGraph;
  /** The Node Created by this Command */
  private final GroupNodeSubject mCreated;
  private final String mDescription = "Group Node Creation";

  /**
   * Constructs a new CreateNodeCommand with the specified surface and
   * creates the node in the x,y position specified
   *
   * @param surface the surface edited by this command
   */
  public CreateNodeGroupCommand(GraphSubject graph, Rectangle2D geom)
  {
    Collection<NodeProxy> ic = Collections.emptyList();
    String n = "G0";
    for (int i = 0; graph.getNodesModifiable().containsName(n); i++) {
      n = "G" + i;
    }
    BoxGeometrySubject g = new BoxGeometrySubject(geom);
    mCreated = new GroupNodeSubject(n, new PlainEventListSubject(), ic, g);
    mGraph = graph;
  }

  /**
   * Executes the Creation of the Node
   */
  public void execute()
  {
    mGraph.getNodesModifiable().add(mCreated);
  }

  /** 
   * Undoes the Command
   */    

  public void undo()
  {
    mGraph.getNodesModifiable().remove(mCreated);
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
