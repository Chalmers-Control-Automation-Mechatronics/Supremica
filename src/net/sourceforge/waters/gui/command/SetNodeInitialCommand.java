package net.sourceforge.waters.gui.command;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.waters.subject.module.GraphSubject;
import net.sourceforge.waters.subject.module.NodeSubject;
import net.sourceforge.waters.subject.module.SimpleNodeSubject;

public class SetNodeInitialCommand
	implements Command
{
	private List<SimpleNodeSubject> mPreviousInitial;
	private SimpleNodeSubject mNewInitial;
	
	public SetNodeInitialCommand(GraphSubject graph, SimpleNodeSubject newInitial)
	{
		mNewInitial = newInitial;
		mPreviousInitial = new ArrayList<SimpleNodeSubject>();
		for (NodeSubject node : graph.getNodesModifiable())
		{
			if (node instanceof SimpleNodeSubject)
			{
				SimpleNodeSubject n = (SimpleNodeSubject)node;
				if (n.isInitial())
				{
					mPreviousInitial.add(n);
				}
			}
		}
	}
	
	public void execute()
	{
		for (SimpleNodeSubject node : mPreviousInitial)
		{
			node.setInitial(false);
		}
		mNewInitial.setInitial(true);
	}
	
	public void undo()
	{
		mNewInitial.setInitial(false);
		for (SimpleNodeSubject node : mPreviousInitial)
		{
			node.setInitial(true);
		}
	}
	
	public boolean isSignificant()
	{
		return true;
	}
	
	public String getName()
	{
		return "Set Initial";
	}
}
