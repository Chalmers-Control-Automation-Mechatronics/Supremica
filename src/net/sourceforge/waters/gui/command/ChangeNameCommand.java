package net.sourceforge.waters.gui.command;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import net.sourceforge.waters.subject.base.NamedSubject;

public class ChangeNameCommand
	implements Command
{
	private final NamedSubject mNamed;
	private final String mOld;
	private final String mNew;
	private final String mDescription = "Change Name";
	
	public ChangeNameCommand(String old,
						   	 String next,
							 NamedSubject named)
	{
		mOld = old;
		mNew = next;
		mNamed = named; 
	}
	
	public void execute()
	{
		try
		{
			mNamed.setName(mNew);
		}
		catch (Throwable t)
		{
			System.out.println("This Shouldn't happen");
			t.printStackTrace();
		}
	}
	
	/** 
     * Undoes the Command
     */    
    public void undo()
    {
		try
		{
			mNamed.setName(mOld);
		}
		catch (Throwable t)
		{
			System.out.println("This Shouldn't happen");
			t.printStackTrace();
		}
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
