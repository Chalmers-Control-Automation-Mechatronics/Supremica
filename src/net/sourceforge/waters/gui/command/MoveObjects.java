//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters GUI
//# PACKAGE: net.sourceforge.waters.gui.command
//# CLASS:   MoveObjects
//###########################################################################
//# $Id: MoveObjects.java,v 1.19 2007-08-10 04:34:31 robi Exp $
//###########################################################################

package net.sourceforge.waters.gui.command;

import net.sourceforge.waters.subject.module.GraphSubject;
import net.sourceforge.waters.gui.EditorGraph;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
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
 * A general command to move a group of graphical objects in a graph.
 * This command supports various types of movements of the selection,
 * including reshaping of group nodes and edges in respsonse to the
 * the dragging of handles.
 *
 * @author Simon Ware
 */

public class MoveObjects
  implements Command
{

  //#########################################################################
  //# Constructors
  /**
   * Constructs a new move objects command.
   * @param objects  A map which contains the old and new state of all
   *                 graphical objects to be moved. The keys in the map
   *                 are the original subjects, the values are 'dummy'
   *                 copies the geometry of which is to be assigned to
   *                 the original subjects.
   * @param graph    The graph edited by this command.
   */
  public MoveObjects(final Map<ProxySubject, ProxySubject> objects,
                     final GraphSubject graph)
  {
    this(objects, graph, null);
  }

  /**
   * Constructs a new move objects command with a given description.
   * @param objects     A map which contains the old and new state of all
   *                    graphical objects to be moved. The keys in the map
   *                    are the original subjects, the values are 'dummy'
   *                    copies the geometry of which is to be assigned to
   *                    the original subjects.
   * @param graph       The graph edited by this command.
   * @param description The description to be used for the command, or
   *                    <CODE>null</CODE> to compute one automatically
   *                    from the type of objects used.
   */
  public MoveObjects(final Map<ProxySubject,ProxySubject> objects,
                     final GraphSubject graph,
                     final String description)
  {
    mGraph = graph;
    mCommands = new CompoundCommand();
    for (Map.Entry<ProxySubject,ProxySubject> entry : objects.entrySet()) {
      final ProxySubject orig = entry.getKey();
      final ProxySubject dummy = entry.getValue();
      final ElementaryMoveCommand cmd;
      if (orig instanceof SimpleNodeSubject) {
        cmd = new MoveSimpleNode((SimpleNodeSubject) orig,
                                 (SimpleNodeSubject) dummy);
      } else if (orig instanceof GroupNodeSubject) {
        cmd = new MoveGroupNode((GroupNodeSubject) orig,
                                (GroupNodeSubject) dummy);
      } else if (orig instanceof EdgeSubject) {
        cmd = new MoveEdge((EdgeSubject) orig,
                           (EdgeSubject) dummy);
      } else if (orig instanceof LabelBlockSubject) {
        cmd = new MoveLabelBlock((LabelBlockSubject) orig,
                                 (LabelBlockSubject) dummy);
      } else if (orig instanceof GuardActionBlockSubject) {
        cmd = new MoveGuardActionBlock((GuardActionBlockSubject) orig,
                                       (GuardActionBlockSubject) dummy);
      } else if (orig instanceof LabelGeometrySubject) {
        cmd = new MoveLabelGeometry((LabelGeometrySubject) orig,
                                    (LabelGeometrySubject) dummy);
      } else {
        throw new ClassCastException
          ("Unknown subject type for MoveObjects command: " +
           orig.getClass().getName());
      }
      mCommands.addCommand(cmd);
    }
    mCommands.end();
    if (description != null) {
      mDescription = description;
    } else {
      mDescription = "Movement";
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.command.Command
  public void execute()
  {
    mCommands.execute();
    EditorGraph.updateChildNodes(mGraph);
  }

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


  //#########################################################################
  //# Inner Class ElementaryMoveCommand
  private abstract static class ElementaryMoveCommand
    implements Command
  {
    //#######################################################################
    //# Interface net.sourceforge.waters.gui.command.Command
    public boolean isSignificant()
    {
      return true;
    }

    public String getName()
    {
      return getTypeName() + " Movement";
    }

    //#######################################################################
    //# Naming
    abstract String getTypeName();

  }


  //#########################################################################
  //# Inner Class MoveSimpleNode
  private static class MoveSimpleNode
    extends ElementaryMoveCommand
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

    //#######################################################################
    //# Naming
    String getTypeName()
    {
      final Point2D oldpos = mOrig.getPoint();
      final Point2D newpos = mNew.getPoint();
      if (!oldpos.equals(newpos)) {
	return "Node";
      }
      if (mOArrow != null) {
	final Point2D oldinit = mOArrow.getPoint();
	final Point2D newinit = mNArrow.getPoint();
	if (!oldinit.equals(newinit)) {
	  return "Initial State Arrow";
	}
      }
      return mLabel.getTypeName();
    }

  }


  //#########################################################################
  //# Inner Class MoveGroupNode
  private static class MoveGroupNode
    extends ElementaryMoveCommand
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

    public String getName()
    {
      final Rectangle2D oldrect = mOrig.getRectangle();
      final Rectangle2D newrect = mNew.getRectangle();
      if (oldrect.getWidth() != newrect.getWidth() ||
          oldrect.getHeight() != newrect.getHeight()) {
        return getTypeName() + " Reshaping";
      } else {
        return super.getName();
      }
    }

    //#######################################################################
    //# Naming
    String getTypeName()
    {
      return "Group Node";
    }

  }


  //#########################################################################
  //# Inner Class MoveEdge
  private static class MoveEdge
    extends ElementaryMoveCommand
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

    public String getName()
    {
      return getTypeName() + " Reshaping";
    }

    //#######################################################################
    //# Naming
    String getTypeName()
    {
      return "Edge";
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


  //#########################################################################
  //# Inner Class MoveLabelBlock
  private static class MoveLabelBlock
    extends ElementaryMoveCommand
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

    //#######################################################################
    //# Naming
    String getTypeName()
    {
      return "Label Block";
    }

  }


  //#########################################################################
  //# Inner Class MoveGuardActionBlock
  private static class MoveGuardActionBlock
    extends ElementaryMoveCommand
  {

    public MoveGuardActionBlock(final GuardActionBlockSubject orig,
                                final GuardActionBlockSubject dummy)
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

    //#######################################################################
    //# Naming
    String getTypeName()
    {
      return "Guard/Action Block";
    }

    //#######################################################################
    //# Data Members
    private final GuardActionBlockSubject mGA;
    private final LabelGeometrySubject mOGeo;
    private final LabelGeometrySubject mNGeo;

  }


  //#########################################################################
  //# Inner Class MoveLabelGeometry
  private static class MoveLabelGeometry
    extends ElementaryMoveCommand
  {
    private final LabelGeometrySubject mLabel;
    private final Point2D mOrig;
    private final Point2D mNew;

    public MoveLabelGeometry(final LabelGeometrySubject orig,
                             final LabelGeometrySubject dummy)
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

    //#######################################################################
    //# Naming
    String getTypeName()
    {
      return "Node Label";
    }

  }


  //#########################################################################
  //# Data Members
  /**
   * The elementary move commands to be executed.
   * The move objects command basically is a compound command,
   * consisting of elementary commands moving each object individually.
   */
  private final CompoundCommand mCommands;

  /**
   * A textual description of the command.
   * The description can be calculated from the objects moved,
   * or be given by the constructor.
   */
  private final String mDescription;

  /**
   * The graph edited by this command.
   */
  private final GraphSubject mGraph;

}
