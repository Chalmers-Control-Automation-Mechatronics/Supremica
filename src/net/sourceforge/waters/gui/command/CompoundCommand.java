package net.sourceforge.waters.gui.command;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class CompoundCommand
	implements Command
{
	private boolean mInProgress;
	private List<Command> mCommands;
	private String mDescription;
	
	public CompoundCommand()
	{
		mInProgress = true;
		mCommands = new ArrayList();
		mDescription = "Compound Command";
	}
	
	public CompoundCommand(String name)
	{
		this();
		mDescription = name;
	}
	
	public boolean addCommand(Command c)
	{
		if (mInProgress && c != null)
		{
			mCommands.add(c);
			return true;
		}
		return false;
	}
	
	public void end()
	{
		mInProgress = false;
	}
	
	public void execute()
	{
		for (Command c : mCommands)
		{
			c.execute();
		}
	}
	
	public void undo()
	{
		ListIterator<Command> li = mCommands.listIterator(mCommands.size());
		while(li.hasPrevious())
		{
			li.previous().undo();
		}
	}
	
	public boolean isSignificant()
	{
		for (Command c : mCommands)
		{
			if (c.isSignificant())
			{
				return true;
			}
		}
		return false;
	}
	
	public String getName()
	{
		return mDescription;
	}
}
