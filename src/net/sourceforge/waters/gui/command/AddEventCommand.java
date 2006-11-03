package net.sourceforge.waters.gui.command;

import net.sourceforge.waters.subject.base.AbstractSubject;
import net.sourceforge.waters.subject.module.EventListExpressionSubject;

public class AddEventCommand
	implements Command
{
	private final EventListExpressionSubject mList;
	private final AbstractSubject mIdentifier;
	private final int mPosition;
	private final String mDescription = "Add Event";
	
	public AddEventCommand(EventListExpressionSubject list,
						   AbstractSubject identifier,
						   int position)
	{
		mList = list;
		mIdentifier = identifier.clone();
		mPosition = position;
	}
	
	public void execute()
	{
		mList.getEventListModifiable().add(mPosition, mIdentifier);
	}
	
    /** 
     * Undoes the Command
     *
     */    
    public void undo()
    {
		mList.getEventListModifiable().remove(mIdentifier);
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
