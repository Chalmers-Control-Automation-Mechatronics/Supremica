//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui.command
//# CLASS:   CreateEdgeCommand
//###########################################################################
//# $Id: CreateEdgeCommand.java,v 1.8 2006-03-02 12:12:50 martin Exp $
//###########################################################################


package net.sourceforge.waters.gui.command;

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

public class CreateEdgeCommand
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
    public CreateEdgeCommand(final ControlledSurface surface,
							 final EditorObject source,
							 final EditorNode target,
							 final int x,
							 final int y)
    {
		mSurface = surface;
		final NodeSubject sourceSubject = (NodeSubject) source.getSubject();
		final NodeSubject targetSubject = (NodeSubject) target.getSubject(); 
		final Collection<Proxy> empty = Collections.emptyList();
		final LabelBlockSubject labelBlock =
			new LabelBlockSubject(empty, null);
		final EdgeSubject edgeSubject =
			new EdgeSubject(sourceSubject, targetSubject, labelBlock);
		mCreated = new EditorEdge(source, target, x, y, edgeSubject, surface);
    }

    /**
     * Executes the Creation of the Node
     */
    public void execute()
    {
		mSurface.addEdge(mCreated);
		mSurface.getEditorInterface().setDisplayed();
    }

    /** 
     * Undoes the Command
     */
    public void undo()
    {
		mSurface.delEdge(mCreated);
		mSurface.getEditorInterface().setDisplayed();
    }

    public String getName()
    {
		return mDescription;
    }
	
	public boolean isSignificant()
	{
		return true;
	}


	//#######################################################################
	//# Data Members
    /** The ControlledSurface Edited with this Command */
    private final ControlledSurface mSurface;
    /** The Node Created by this Command */
    private final EditorEdge mCreated;
    private final String mDescription = "Edge Creation";

}
