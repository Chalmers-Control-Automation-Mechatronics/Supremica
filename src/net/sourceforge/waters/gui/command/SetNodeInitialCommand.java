package net.sourceforge.waters.gui.command;

import java.awt.Point;
import net.sourceforge.waters.subject.module.PointGeometrySubject;
import java.util.ArrayList;
import java.util.List;

import net.sourceforge.waters.subject.module.GraphSubject;
import net.sourceforge.waters.subject.module.NodeSubject;
import net.sourceforge.waters.subject.module.SimpleNodeSubject;

public class SetNodeInitialCommand
	implements Command
{
	private List<Wrapper> mPreviousInitial;
	private SimpleNodeSubject mNewInitial;
	
	public SetNodeInitialCommand(GraphSubject graph, SimpleNodeSubject newInitial)
	{
		mNewInitial = newInitial;
		mPreviousInitial = new ArrayList<Wrapper>();
		for (NodeSubject node : graph.getNodesModifiable())
		{
			if (node instanceof SimpleNodeSubject)
			{
				SimpleNodeSubject n = (SimpleNodeSubject)node;
				if (n.isInitial())
				{
					mPreviousInitial.add(new Wrapper(n));
				}
			}
		}
	}
	
	public void execute()
	{
		for (Wrapper n : mPreviousInitial)
		{
			n.mNode.setInitial(false);
      n.mNode.setInitialArrowGeometry(null);
		}
		mNewInitial.setInitial(true);
    mNewInitial.setInitialArrowGeometry(new PointGeometrySubject(new Point(-5, -5)));
	}
	
	public void undo()
	{
		mNewInitial.setInitial(false);
    mNewInitial.setInitialArrowGeometry(null);
		for (Wrapper n : mPreviousInitial)
		{
			n.mNode.setInitial(true);
      n.mNode.setInitialArrowGeometry(n.mPoint);
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
  
  private static class Wrapper
  {
    final SimpleNodeSubject mNode;
    final PointGeometrySubject mPoint;
    
    public Wrapper(SimpleNodeSubject node)
    {
      mNode = node;
      mPoint = node.getInitialArrowGeometry();
    }
  }
}
