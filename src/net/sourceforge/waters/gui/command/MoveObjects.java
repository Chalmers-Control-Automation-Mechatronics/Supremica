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
    extends Move
    implements Command
{

    /** The ControlledSurface Edited with this Command */
    private final ControlledSurface mSurface;
    /** The Objects moved by this command */
    private final Collection<EditorObject> mMoved;
    /** The Original Position of the Object */
    private final Point2D mDisplacement = new Point2D.Double();     

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
    }

    /**
     * Executes the Creation of the Node
     */

    public void execute()
    {    
    }

    /** 
     * Redoes the Command
     *
     * @throws CannotRedoException if CanRedo returns false
     */
    
    public void redo() throws CannotRedoException
    {
	super.redo();
	for (EditorObject o : mMoved) {
	    if (o.getType() == EditorObject.NODEGROUP) {
		EditorNodeGroup ng = (EditorNodeGroup) o;
		if (ng.getResizing()) {
		    ng.resize((int)(ng.getX() + mDisplacement.getX()), (int)(ng.getY() + mDisplacement.getY()));
		    continue;
		}
	    }
	    if ((o.getType() != EditorObject.LABELGROUP && o.getType() != EditorObject.LABEL)
		|| mMoved.size() == 1) {	
		o.setPosition(o.getX() + mDisplacement.getX(), o.getY() + mDisplacement.getY());
	    }
	}
	mSurface.getEditorInterface().setDisplayed();   
    }

    /** 
     * Undoes the Command
     *
     * @throws CannotUndoException if CanUndo returns false
     */    

    public void undo() throws CannotUndoException
    {
	super.undo();
	for (EditorObject o : mMoved) {
	    if (o.getType() == EditorObject.NODEGROUP) {
		EditorNodeGroup ng = (EditorNodeGroup) o;
		if (ng.getResizing()) {
		    ng.resize((int)(ng.getX() - mDisplacement.getX()), (int)(ng.getY() - mDisplacement.getY()));
		    continue;
		}
	    }
	    if ((o.getType() != EditorObject.LABELGROUP && o.getType() != EditorObject.LABEL)
		|| mMoved.size() == 1) {	  
		o.setPosition(o.getX() - mDisplacement.getX(), o.getY() - mDisplacement.getY());
	    }
	}
	mSurface.getEditorInterface().setDisplayed();   
    }

    public String getPresentationName()
    {
	return this.getClass().getName();
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
