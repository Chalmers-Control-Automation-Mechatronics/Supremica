package net.sourceforge.waters.gui.command;

import net.sourceforge.waters.subject.module.NodeSubject;
import net.sourceforge.waters.subject.module.PointGeometrySubject;
import net.sourceforge.waters.subject.module.EdgeSubject;
import java.awt.geom.Point2D;
import org.supremica.util.BDD.graph.Edge;

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
     * @param edge the edge to flip.
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
        //System.out.println("Flipping edge " + mEdge);
        
        final NodeSubject node1 = mEdge.getSource();
        final NodeSubject node2 = mEdge.getTarget();
        final PointGeometrySubject p1 = mEdge.getStartPoint();
        final PointGeometrySubject p2 = mEdge.getEndPoint();
        //if (p1 != null || p2 != null)
        {
            mEdge.setStartPoint(null);
            mEdge.setEndPoint(null);
            mEdge.setStartPoint(p2);
            mEdge.setEndPoint(p1);
            mEdge.setSource(node2);
            mEdge.setTarget(node1);
        }
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
        return true;
    }
    
    public String getName()
    {
        return mDescription;
    }
}
