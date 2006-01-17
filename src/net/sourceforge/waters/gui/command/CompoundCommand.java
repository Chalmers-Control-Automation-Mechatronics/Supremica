package net.sourceforge.waters.gui.command;

import java.util.ArrayList;
import java.util.List;

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
		if (mInProgress)
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
		for (Command c : mCommands)
		{
			c.undo();
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
