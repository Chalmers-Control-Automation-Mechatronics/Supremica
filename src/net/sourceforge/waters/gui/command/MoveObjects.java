package net.sourceforge.waters.gui.command;

import net.sourceforge.waters.gui.ControlledSurface;
import net.sourceforge.waters.gui.EditorObject;
import net.sourceforge.waters.gui.EditorNodeGroup;

import java.awt.geom.Point2D;

import java.util.Collection;
import java.util.ArrayList;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

/**
 * the Command for Creation of nodes
 *
 * @author Simon Ware
 */

public class MoveObjects
    implements Move
{

    /** The ControlledSurface Edited with this Command */
    private final ControlledSurface mSurface;
    /** The Objects moved by this command */
    private final Collection<EditorObject> mMoved;
    /** The Original Position of the Object */
    private final Point2D mDisplacement = new Point2D.Double();
    private final String mDescription;

    /**
     * Constructs a new CreateNodeCommand with the specified surface and
     * creates the node in the x,y position specified
     *
     * @param surface the surface edited by this command
     * @param x,y the position upon which the node is created
     */
    public MoveObjects(ControlledSurface surface, Collection<? extends EditorObject> moved, Point2D displacement)
    {
		mSurface = surface;
		mMoved = new ArrayList(moved);	
		setDisplacement(displacement);
		boolean node = false;
		boolean edge = false;
		for (EditorObject o: mMoved)
		{
			if (o.getType() == EditorObject.EDGE)
			{
				edge = true;
			}
			if (o.getType() == EditorObject.NODE ||
				o.getType() == EditorObject.NODEGROUP)
			{
				node = true;
				break;
			}
		}
		if (node)
		{
			mDescription = "Node Movement";
		}
		else if (edge)
		{
			mDescription = "Edge Reshaping";
		}
		else
		{
			mDescription = "Label Movement";
		}
    }

    public void execute()
    {
		for (EditorObject o : mMoved) {
			if (mDescription.equals("Edge reshaping") || o.getType() != EditorObject.EDGE)
			{
				if (o.getType() == EditorObject.NODEGROUP) {
				EditorNodeGroup ng = (EditorNodeGroup) o;
				if (ng.getResizing()) {
					ng.resize((int)(ng.getX() + mDisplacement.getX()), (int)(ng.getY() + mDisplacement.getY()));
					continue;
				}
				}
				if ((o.getType() != EditorObject.LABELGROUP && o.getType() != EditorObject.LABEL)
				|| mDescription.equals("Label Movement"))
				{
					o.setPosition(o.getX() + mDisplacement.getX(), o.getY() + mDisplacement.getY());
				}
			}
		}
		mSurface.getEditorInterface().setDisplayed();
    }

    /** 
     * Undoes the Command
     */    

    public void undo()
    {
		for (EditorObject o : mMoved) {
			if (mDescription.equals("Edge reshaping") || o.getType() != EditorObject.EDGE)
			{
				if (o.getType() == EditorObject.NODEGROUP) {
				EditorNodeGroup ng = (EditorNodeGroup) o;
				if (ng.getResizing()) {
					ng.resize((int)(ng.getX() - mDisplacement.getX()), (int)(ng.getY() - mDisplacement.getY()));
					continue;
				}
				}
				if ((o.getType() != EditorObject.LABELGROUP && o.getType() != EditorObject.LABEL)
				|| mDescription.equals("Label Movement")) {	  
				o.setPosition(o.getX() - mDisplacement.getX(), o.getY() - mDisplacement.getY());	
				}
			}
		}
		mSurface.getEditorInterface().setDisplayed();  
    }
	
	public boolean isSignificant()
	{
		return true;
	}

    public String getName()
    {
	return mDescription;
    }

    public void setDisplacement(Point2D neo) 
    {
	mDisplacement.setLocation(neo);
    }

    public Point2D getDisplacement()
    {
	return (Point2D)mDisplacement.clone();
    }
}
