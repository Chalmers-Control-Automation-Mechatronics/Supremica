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
	private final ControlledSurface mSurface;
	private final EditorLabelGroup mGroup;
	private final EventListExpressionSubject mList;
	private final IdentifierSubject mIdentifier;
	private final int mPosition;
	private final String mDescription = "Remove Event";
	
	public RemoveEventCommand(ControlledSurface surface,
							  EditorLabelGroup group,
							  IdentifierSubject identifier)						   
	{
		mSurface = surface;
		mGroup = group;
		mList = group.getSubject();
		mIdentifier = identifier;
		mPosition = mList.getEventList().indexOf(mIdentifier);
	}
	
	public void execute()
	{
		mList.getEventListModifiable().remove(mIdentifier);
		mSurface.unselectAll();
		mSurface.select(mGroup);
		mSurface.getEditorInterface().setDisplayed();
	}

    /** 
     * Undoes the Command
     */    
    public void undo()
    {
		mList.getEventListModifiable().add(mPosition, mIdentifier);
		mSurface.unselectAll();
		mSurface.select(mGroup);
		mGroup.setSelectedLabel(mPosition);
		mSurface.getEditorInterface().setDisplayed();
    }

    public String getName()
    {
		return mDescription;
    }
}
