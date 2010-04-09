package net.sourceforge.waters.gui.command;

import net.sourceforge.waters.subject.module.PointGeometrySubject;
import java.util.ArrayList;
import java.util.List;

import net.sourceforge.waters.subject.module.GraphSubject;
import net.sourceforge.waters.subject.module.NodeSubject;
import net.sourceforge.waters.subject.module.SimpleNodeSubject;

public class SetNodeInitialCommand
	implements Command
{
	private final List<Wrapper> mPreviousInitial;
	private final SimpleNodeSubject mNewInitial;

	public SetNodeInitialCommand(final GraphSubject graph, final SimpleNodeSubject newInitial)
	{
		mNewInitial = newInitial;
		mPreviousInitial = new ArrayList<Wrapper>();
		for (final NodeSubject node : graph.getNodesModifiable())
		{
			if (node instanceof SimpleNodeSubject)
			{
				final SimpleNodeSubject n = (SimpleNodeSubject)node;
				if (n.isInitial())
				{
					mPreviousInitial.add(new Wrapper(n));
				}
			}
		}
	}

	public void execute()
	{
		for (final Wrapper n : mPreviousInitial)
		{
			n.mNode.setInitial(false);
      n.mNode.setInitialArrowGeometry(null);
		}
		mNewInitial.setInitial(true);
	}

	public void undo()
	{
		mNewInitial.setInitial(false);
    mNewInitial.setInitialArrowGeometry(null);
		for (final Wrapper n : mPreviousInitial)
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

    public Wrapper(final SimpleNodeSubject node)
    {
      mNode = node;
      mPoint = node.getInitialArrowGeometry();
    }
  }
}
