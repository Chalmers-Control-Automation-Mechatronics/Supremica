//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui.command
//# CLASS:   MoveEdgeCommand
//###########################################################################
//# $Id: MoveEdgeCommand.java,v 1.1 2006-01-09 02:21:09 siw4 Exp $
//###########################################################################


package net.sourceforge.waters.gui.command;

import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.awt.Point;

import java.util.Collection;
import java.util.Collections;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import net.sourceforge.waters.gui.ControlledSurface;
import net.sourceforge.waters.gui.EditorEdge;
import net.sourceforge.waters.gui.EditorNode;
import net.sourceforge.waters.gui.EditorNodeGroup;
import net.sourceforge.waters.gui.EditorObject;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.subject.module.EdgeSubject;
import net.sourceforge.waters.subject.module.LabelBlockSubject;
import net.sourceforge.waters.subject.module.NodeSubject;


/**
 * the Command for Creation of nodes
 *
 * @author Simon Ware
 */

public class MoveEdgeCommand
    extends AbstractUndoableEdit
    implements Command
{

	//#######################################################################
	//# Constructor
    /**
     * Constructs a new CreateNodeCommand with the specified surface and
     * creates the node in the x,y position specified.
     * @param surface the surface edited by this command
     * @param x the position upon which the node is created
     * @param y the position upon which the node is created
     */
    public MoveEdgeCommand(final EditorEdge edge,
						   final EditorObject neo,
						   boolean source,
						   int x,
						   int y)
    {
		mEdge = edge;
		mNew = neo;
		mSource = source;
		mNPos = new Point(x, y);
		mOPos = new Point();
		mOPos.setLocation(edge.getSubject().getStartPoint().getPoint());
		if (source)
		{
			mOld = edge.getStartNode();
			mDescription = "Change Edge Source";
		}
		else
		{
			mOld = edge.getEndNode();
			mDescription = "Change Edge Target";
		}
		mOTPoint = new Point2D.Double();
		mOTPoint.setLocation(edge.getPosition());
    }

    /**
     * Executes the Creation of the Node
     */
    public void execute()
    {
		if (mSource)
		{
			mEdge.setStartNode(mNew, (int)mNPos.getX(), (int)mNPos.getY());
		}
		else
		{
			mEdge.setEndNode((EditorNode)mNew);
		}
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
		if (mSource)
		{
			mEdge.setStartNode(mOld, (int)mOPos.getX(), (int)mOPos.getX());
		}
		else
		{
			mEdge.setEndNode((EditorNode)mOld);
		}
		mEdge.setPosition(mOTPoint.getX(), mOTPoint.getY());
    }

    public String getPresentationName()
    {
		return mDescription;
    }


	//#######################################################################
	//# Data Members
	private final EditorEdge mEdge;
	private final EditorObject mOld;
	private final EditorObject mNew;
	private final boolean mSource;
	private final Point2D mNPos;
	private final Point2D mOPos;
	private final Point2D mOTPoint;
	private final String mDescription;	
}
