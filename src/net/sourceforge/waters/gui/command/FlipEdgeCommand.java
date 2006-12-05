package net.sourceforge.waters.gui.command;

import net.sourceforge.waters.subject.module.NodeSubject;
import net.sourceforge.waters.subject.module.PointGeometrySubject;
import net.sourceforge.waters.subject.module.EdgeSubject;
import java.awt.geom.Point2D;

/**
 * Command for the flipping of edges
 *
 * @author Hugo Flordal
 */
public class FlipEdgeCommand
    implements Command
{
    /** The Edge Edited by this Command */
    private final EdgeSubject mEdge;
    private final String mDescription = "Edge Flipping";

    /**
     * Flips an edge on the specified surface.
     *
     * @param graph the surface edited by this command
     */
    public FlipEdgeCommand(EdgeSubject edge)
    {
        mEdge = edge;
    }

    /**
     * Does the Command.
     */
    public void execute()
    {
      NodeSubject node1 = mEdge.getSource();
      NodeSubject node2 = mEdge.getTarget();
      Point2D p1 = mEdge.getStartPoint().getPoint();
      Point2D p2 = mEdge.getEndPoint().getPoint();
      mEdge.setSource(node2);
      mEdge.setTarget(node1);
      mEdge.setStartPoint(new PointGeometrySubject(p2));
      mEdge.setEndPoint(new PointGeometrySubject(p1));
    }

    /** 
     * Undoes the Command
     */    
    public void undo()
    {
		execute();
    }

   	public boolean isSignificant()
	{
		// I have no idea what to return here... I return false, true?!
		return true;
	}

  public String getName()
  {
    return mDescription;
  }
}
