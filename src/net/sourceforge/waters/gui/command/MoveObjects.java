package net.sourceforge.waters.gui.command;

import net.sourceforge.waters.subject.module.GraphSubject;
import net.sourceforge.waters.gui.EditorGraph;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import net.sourceforge.waters.gui.renderer.GeometryTools;
import net.sourceforge.waters.subject.base.ProxySubject;
import net.sourceforge.waters.subject.module.EdgeSubject;
import net.sourceforge.waters.xsd.module.SplineKind;
import net.sourceforge.waters.subject.module.GuardActionBlockSubject;
import net.sourceforge.waters.subject.module.SimpleNodeSubject;
import net.sourceforge.waters.subject.module.GroupNodeSubject;
import net.sourceforge.waters.subject.module.LabelBlockSubject;
import net.sourceforge.waters.subject.module.LabelGeometrySubject;
import net.sourceforge.waters.subject.module.PointGeometrySubject;
import net.sourceforge.waters.subject.module.BoxGeometrySubject;
import net.sourceforge.waters.subject.module.SplineGeometrySubject;

/**
 * the Command for Creation of nodes
 *
 * @author Simon Ware
 */

public class MoveObjects
  implements Command
{

  /**  the commands to be executed */
  private final CompoundCommand mCommands;
  private final String mDescription;
  private final GraphSubject mGraph;

  /**
   * Constructs a new CreateNodeCommand with the specified surface and
   * creates the node in the x,y position specified
   *
   * @param graph the surface edited by this command
   */
  public MoveObjects(Map<ProxySubject, ProxySubject> objects,
                     GraphSubject graph)
  {
    mGraph = graph;
    mCommands = new CompoundCommand();
    for (Map.Entry<ProxySubject, ProxySubject> entry : objects.entrySet()) {
      Command c = null;
      if (entry.getKey() instanceof SimpleNodeSubject) {
        c = new MoveSimpleNode((SimpleNodeSubject) entry.getKey(),
                               (SimpleNodeSubject) entry.getValue());
      } else if (entry.getKey() instanceof GroupNodeSubject) {
        c = new MoveGroupNode((GroupNodeSubject) entry.getKey(),
                              (GroupNodeSubject) entry.getValue());
      } else if (entry.getKey() instanceof EdgeSubject) {
        c = new MoveEdge((EdgeSubject) entry.getKey(),
                         (EdgeSubject) entry.getValue());
      } else if (entry.getKey() instanceof LabelBlockSubject) {
        c = new MoveLabelBlock((LabelBlockSubject) entry.getKey(),
                               (LabelBlockSubject) entry.getValue());
      } else if (entry.getKey() instanceof GuardActionBlockSubject) {
          c = new MoveGuardActionBlock((GuardActionBlockSubject) entry.getKey(),
                  (GuardActionBlockSubject) entry.getValue());
      } else if (entry.getKey() instanceof LabelGeometrySubject) {
        c = new MoveLabelGeometry((LabelGeometrySubject) entry.getKey(),
                                  (LabelGeometrySubject) entry.getValue());
      } else {
        assert(false);
      }
      mCommands.addCommand(c);
    }
    mCommands.end();
    mDescription = "movement";
  }

  public void execute()
  {
    mCommands.execute();
    EditorGraph.updateChildNodes(mGraph);
  }

  /**
   * Undoes the Command
   */

  public void undo()
  {
    mCommands.undo();
    EditorGraph.updateChildNodes(mGraph);
  }

	public boolean isSignificant()
	{
		return mCommands.isSignificant();
	}

  public String getName()
  {
    return mDescription;
  }

  private class MoveSimpleNode
    implements Command
  {
    private final SimpleNodeSubject mNode;
    private final PointGeometrySubject mOrig;
    private final PointGeometrySubject mNew;
    private final PointGeometrySubject mOArrow;
    private final PointGeometrySubject mNArrow;
    private final MoveLabelGeometry mLabel;

    public MoveSimpleNode(SimpleNodeSubject orig, SimpleNodeSubject dummy)
    {
      mNode = orig;
      mOrig = mNode.getPointGeometry().clone();
      mNew = dummy.getPointGeometry().clone();
      if (mNode.getInitialArrowGeometry() != null) {
        mOArrow = mNode.getInitialArrowGeometry().clone();
        mNArrow = dummy.getInitialArrowGeometry().clone();
      } else {
        mOArrow = null;
        mNArrow = null;
      }
      mLabel = new MoveLabelGeometry(orig.getLabelGeometry(),
                                     dummy.getLabelGeometry());
    }

    public void execute()
    {
      //System.out.println("new pos:" + mNew.getPoint());
      mNode.setPointGeometry(mNew);
      mNode.setInitialArrowGeometry(mNArrow);
      mLabel.execute();
    }

    public void undo()
    {
      mNode.setPointGeometry(mOrig);
      mNode.setInitialArrowGeometry(mOArrow);
      mLabel.undo();
    }

    public boolean isSignificant()
    {
      return true;
    }

    public String getName()
    {
      return "Move Node";
    }
  }

  private class MoveGroupNode
    implements Command
  {
    private final GroupNodeSubject mNode;
    private final BoxGeometrySubject mOrig;
    private final BoxGeometrySubject mNew;

    public MoveGroupNode(GroupNodeSubject orig, GroupNodeSubject dummy)
    {
      mNode = orig;
      mOrig = mNode.getGeometry().clone();
      mNew = dummy.getGeometry().clone();
    }

    public void execute()
    {
      mNode.setGeometry(mNew);

    }

    public void undo()
    {
      mNode.setGeometry(mOrig);
    }

    public boolean isSignificant()
    {
      return true;
    }

    public String getName()
    {
      return "Move Node";
    }
  }

  private class MoveEdge
    implements Command
  {
    //#######################################################################
    //# Constructor
    public MoveEdge(EdgeSubject orig, EdgeSubject dummy)
    {
      mEdge = orig;
      final SplineGeometrySubject oGeo = mEdge.getGeometry();
      mOGeo = oGeo != null ? oGeo.clone() : null;
      final SplineGeometrySubject nGeo = dummy.getGeometry();
      mNGeo = nGeo != null ? nGeo.clone() : null;
      final PointGeometrySubject oStart = mEdge.getStartPoint();
      mOStart = oStart != null ? oStart.clone() : null;
      final PointGeometrySubject oEnd = mEdge.getEndPoint();
      mOEnd = oEnd != null ? oEnd.clone() : null;
      final PointGeometrySubject nStart = dummy.getStartPoint();
      mNStart = nStart != null ? nStart.clone() : null;
      final PointGeometrySubject nEnd = dummy.getEndPoint();
      mNEnd = nEnd != null ? nEnd.clone() : null;
    }

    public void execute()
    {
      mEdge.setGeometry(mNGeo);
      mEdge.setStartPoint(mNStart);
      mEdge.setEndPoint(mNEnd);
    }

    public void undo()
    {
      mEdge.setGeometry(mOGeo);
      mEdge.setStartPoint(mOStart);
      mEdge.setEndPoint(mOEnd);
    }

    public boolean isSignificant()
    {
      return true;
    }

    public String getName()
    {
      return "Move Edge";
    }

    //#######################################################################
    //# Data Members
    private final EdgeSubject mEdge;
    private final SplineGeometrySubject mOGeo;
    private final SplineGeometrySubject mNGeo;
    private final PointGeometrySubject mOStart;
    private final PointGeometrySubject mNStart;
    private final PointGeometrySubject mOEnd;
    private final PointGeometrySubject mNEnd;

  }

  private class MoveLabelBlock
    implements Command
  {
    private final LabelBlockSubject mLabel;
    private final LabelGeometrySubject mOGeo;
    private final LabelGeometrySubject mNGeo;

    public MoveLabelBlock(LabelBlockSubject orig, LabelBlockSubject dummy)
    {
      mLabel = orig;
      mOGeo = mLabel.getGeometry().clone();
      mNGeo = dummy.getGeometry().clone();
    }

    public void execute()
    {
      mLabel.setGeometry(mNGeo);
    }

    public void undo()
    {
      mLabel.setGeometry(mOGeo);
    }

    public boolean isSignificant()
    {
      return true;
    }

    public String getName()
    {
      return "Move LabelBlock";
    }
  }
 
  private class MoveGuardActionBlock
  implements Command
{
  private final GuardActionBlockSubject mGA;
  private final LabelGeometrySubject mOGeo;
  private final LabelGeometrySubject mNGeo;

  public MoveGuardActionBlock(GuardActionBlockSubject orig, GuardActionBlockSubject dummy)
  {
    mGA = orig;
    mOGeo = mGA.getGeometry().clone();
    mNGeo = dummy.getGeometry().clone();
  }

  public void execute()
  {
    mGA.setGeometry(mNGeo);
  }

  public void undo()
  {
    mGA.setGeometry(mOGeo);
  }

  public boolean isSignificant()
  {
    return true;
  }

  public String getName()
  {
    return "Move GuardActionBlock";
  }
}

  private class MoveLabelGeometry
    implements Command
  {
    private final LabelGeometrySubject mLabel;
    private final Point2D mOrig;
    private final Point2D mNew;

    public MoveLabelGeometry(LabelGeometrySubject orig, LabelGeometrySubject dummy)
    {
      mLabel = orig;
      mOrig = mLabel.getOffset();
      mNew = dummy.getOffset();
    }

    public void execute()
    {
      mLabel.setOffset(mNew);
    }

    public void undo()
    {
      mLabel.setOffset(mOrig);
    }

    public boolean isSignificant()
    {
      return true;
    }

    public String getName()
    {
      return "Move Label";
    }
  }
}
