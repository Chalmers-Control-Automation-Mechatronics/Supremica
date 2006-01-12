//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui.command
//# CLASS:   CreateNodeCommand
//###########################################################################
//# $Id: CreateNodeCommand.java,v 1.7 2006-01-12 21:51:53 siw4 Exp $
//###########################################################################


package net.sourceforge.waters.gui.command;

import java.util.Collection;
import java.util.Collections;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import net.sourceforge.waters.gui.ControlledSurface;
import net.sourceforge.waters.gui.EditorNode;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.subject.module.EventListExpressionSubject;
import net.sourceforge.waters.subject.module.PlainEventListSubject;
import net.sourceforge.waters.subject.module.SimpleNodeSubject;


/**
 * The Command for creation of nodes.
 *
 * @author Simon Ware
 */

public class CreateNodeCommand
    implements Command
{

	//#######################################################################
	//# Constructor
    /**
     * Constructs a new CreateNodeCommand with the specified surface and
     * creates the node in the x,y position specified
     * @param surface the surface edited by this command
     * @param x the position upon which the node is created
     * @param y the position upon which the node is created
     */
    public CreateNodeCommand(ControlledSurface surface, int x, int y)
    {
		mSurface = surface;
		// Find a unique name!
		int i = 0;
		for (i = 0; i <= mSurface.getNodes().size(); i++) {
			boolean found = false;
			for (int j=0; j<mSurface.getNodes().size(); j++) {
				if (((EditorNode) mSurface.getNodes().get(j)).getName().equals("s" + i)) {
					found = true;
					break;
				}
			}
			if (!found) {
				break;
			}
		}
		final String name = "s" + i;
		final Collection<Proxy> empty = Collections.emptyList();
		final EventListExpressionSubject props =
			new PlainEventListSubject(empty);
		final SimpleNodeSubject node = new SimpleNodeSubject(name, props);
		mCreated = new EditorNode(x, y, node, surface);       
    }

    /**
     * Executes the Creation of the Node
     */
    public void execute()
    {
		mSurface.addNode(mCreated);
		mSurface.unselectAll();
		mSurface.select(mCreated);
		mSurface.getEditorInterface().setDisplayed();
    }
	
    /** 
     * Undoes the Command
     */    

    public void undo()
    {
		mSurface.delNode(mCreated);
		mSurface.unselectAll();		
		mSurface.getEditorInterface().setDisplayed();
    }

    public String getName()
    {
		return mDescription;
    }


	//#######################################################################
	//# Data Members
    /** The ControlledSurface Edited with this Command */
    private final ControlledSurface mSurface;
    /** The Node Created by this Command */
    private final EditorNode mCreated;
    /** Description of Command */
    private final String mDescription = "Node Creation";

}
