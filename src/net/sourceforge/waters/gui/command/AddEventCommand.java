package net.sourceforge.waters.gui.command;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import net.sourceforge.waters.gui.ControlledSurface;
import net.sourceforge.waters.gui.EditorObject;
import net.sourceforge.waters.gui.EditorLabelGroup;

import net.sourceforge.waters.subject.module.EventListExpressionSubject;
import net.sourceforge.waters.subject.module.IdentifierSubject;

public class AddEventCommand
	implements Command
{
	private final ControlledSurface mSurface;
	private final EditorObject mObject;
	private final EventListExpressionSubject mList;
	private final IdentifierSubject mIdentifier;
	private final int mPosition;
	private final String mDescription = "Add Event";
	
	public AddEventCommand(ControlledSurface surface,
						   EditorObject object,
						   EventListExpressionSubject list,
						   IdentifierSubject identifier,
						   int position)
	{
		mSurface = surface;
		mObject = object;
		mList = list;
		mIdentifier = identifier;
		mPosition = position;
	}
	
	public void execute()
	{
		mList.getEventListModifiable().add(mPosition, mIdentifier);
		if (mObject instanceof EditorLabelGroup)
		{
			((EditorLabelGroup) mObject).setSelectedLabel(mPosition);
		}
		mSurface.getEditorInterface().setDisplayed();
	}
	
    /** 
     * Undoes the Command
     *
     */    
    public void undo()
    {
		mList.getEventListModifiable().remove(mIdentifier);
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
}
