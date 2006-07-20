package net.sourceforge.waters.gui.command;

import net.sourceforge.waters.subject.module.GraphSubject;
import net.sourceforge.waters.subject.module.NodeSubject;
import net.sourceforge.waters.subject.module.SimpleNodeSubject;

public class ToggleNodeInitialCommand
	implements Command
{
	private SimpleNodeSubject mNewInitial;
	
	public ToggleNodeInitialCommand(SimpleNodeSubject node)
	{
		mNewInitial = node;
	}
	
	public void execute()
	{
		mNewInitial.setInitial(!mNewInitial.isInitial());
	}
	
	public void undo()
	{
		execute();
	}
	
	public boolean isSignificant()
	{
		return true;
	}
	
	public String getName()
	{
		return "Toggle Initial";
	}
}
