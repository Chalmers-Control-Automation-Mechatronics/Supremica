package net.sourceforge.waters.gui.command;

import java.util.List;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import net.sourceforge.waters.gui.ControlledSurface;
import net.sourceforge.waters.gui.EditorLabelGroup;

import net.sourceforge.waters.subject.base.AbstractSubject;
import net.sourceforge.waters.subject.module.EventListExpressionSubject;
import net.sourceforge.waters.subject.module.IdentifierSubject;

public class ReorganizeListCommand
	implements Command
{
	private final ControlledSurface mSurface;
	private final EditorLabelGroup mGroup;
	private final EventListExpressionSubject mList;
	private final IdentifierSubject mIdentifier;
	private final int mPosition;
	private final int mNewPosition;
	private final String mDescription = "Move Event";
	
	public ReorganizeListCommand(ControlledSurface surface,
								 EditorLabelGroup group,								 
								 IdentifierSubject identifier,
								 int newPosition)
	{
		mSurface = surface;
		mGroup = group;
		mList = group.getSubject();
		mIdentifier = identifier;
		mPosition = mList.getEventList().indexOf(mIdentifier);
		mNewPosition = newPosition;
	}
	
	public void execute()
	{
		final List<AbstractSubject> list =
						mList.getEventListModifiable();		
		// Remove label and add to new position in list
		list.remove(mIdentifier);
		list.add(mNewPosition, mIdentifier);
		mGroup.setSelectedLabel(mNewPosition);
		mSurface.getEditorInterface().setDisplayed();
	}
	
    /** 
     * Undoes the Command
     */    
    public void undo()
    {
		final List<AbstractSubject> list =
						mList.getEventListModifiable();		
		// Remove label and add to new position in list
		list.remove(mIdentifier);
		list.add(mPosition, mIdentifier);
		mGroup.setSelectedLabel(mPosition);
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
