package net.sourceforge.waters.gui.command;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import net.sourceforge.waters.gui.ControlledSurface;
import net.sourceforge.waters.gui.EditorLabelGroup;

import net.sourceforge.waters.subject.module.EventListExpressionSubject;
import net.sourceforge.waters.subject.module.IdentifierSubject;

public class RemoveEventCommand
	implements Command
{
	private final EventListExpressionSubject mList;
	private final IdentifierSubject mIdentifier;
	private final int mPosition;
	private final String mDescription = "Remove Event";
	
	public RemoveEventCommand(EventListExpressionSubject group,
							  IdentifierSubject identifier)						   
	{
		mList = group;
		mIdentifier = identifier;
		mPosition = mList.getEventList().indexOf(mIdentifier);
	}
	
	public void execute()
	{
		mList.getEventListModifiable().remove(mIdentifier);
	}

    /** 
     * Undoes the Command
     */    
    public void undo()
    {
		mList.getEventListModifiable().add(mPosition, mIdentifier);
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
