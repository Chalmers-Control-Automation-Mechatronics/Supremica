package net.sourceforge.waters.gui.command;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import net.sourceforge.waters.subject.module.EventListExpressionSubject;
import net.sourceforge.waters.subject.module.IdentifierSubject;

public class AddEventCommand
	extends AbstractUndoableEdit
	implements Command
{
	private final EventListExpressionSubject mList;
	private final IdentifierSubject mIdentifier;
	private final int mPosition;
	private final String mDescription = "Add Event";
	
	public AddEventCommand(EventListExpressionSubject list,
						   IdentifierSubject identifier,
						   int position)
	{
		mList = list;
		mIdentifier = identifier;
		mPosition = position;
	}
	
	public void execute()
	{
		mList.getEventListModifiable().add(mPosition, mIdentifier);
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
		mList.getEventListModifiable().remove(mIdentifier);
    }

    public String getPresentationName()
    {
		return mDescription;
    }
}
