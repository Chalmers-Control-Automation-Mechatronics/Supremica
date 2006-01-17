package net.sourceforge.waters.gui.command;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

public class UndoableCommand
	extends AbstractUndoableEdit
{
	private final Command mCommand;
	
	public UndoableCommand(Command command)
	{
		mCommand = command;
	}
	
	/** 
     * Redoes the Command
     *
     * @throws CannotRedoException if CanRedo returns false
     */
    
    public void redo() throws CannotRedoException
    {
		super.redo();
		mCommand.execute();
    }

    /** 
     * Undoes the Command
     *
     * @throws CannotUndoException if CanUndo returns false
     */    

    public void undo() throws CannotUndoException
    {
		super.undo();
		mCommand.undo();
    }
	
	public boolean isSignificant()
	{
		return mCommand.isSignificant();
	}

    public String getPresentationName()
    {
		return mCommand.getName();
    }
}
