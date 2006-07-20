package net.sourceforge.waters.gui.command;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import net.sourceforge.waters.gui.ControlledSurface;

import net.sourceforge.waters.subject.base.AbstractSubject;
import net.sourceforge.waters.subject.module.EventListExpressionSubject;

public class RemoveEventCommand
	implements Command
{
	private final EventListExpressionSubject mList;
	private final AbstractSubject mIdentifier;
	private final int mPosition;
	private final String mDescription = "Remove Event";
	
	public RemoveEventCommand(EventListExpressionSubject group,
							  AbstractSubject identifier) 
	{
		mList = group;
		mIdentifier = identifier;
		mPosition = mList.getEventListModifiable().indexOf(mIdentifier);
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
		int pos = mPosition;
		if (pos > mList.getEventListModifiable().size())
		{
			pos = mList.getEventListModifiable().size();
		}
		mList.getEventListModifiable().add(pos, mIdentifier);
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
