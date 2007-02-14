//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters GUI
//# PACKAGE: net.sourceforge.waters.gui.command
//# CLASS:   CreateNodeCommand
//###########################################################################
//# $Id: CreateNodeCommand.java,v 1.18 2007-02-14 02:01:06 robi Exp $
//###########################################################################

package net.sourceforge.waters.gui.command;

import java.awt.Point;
import java.awt.geom.Point2D;

import net.sourceforge.waters.gui.EditorGraph;
import net.sourceforge.waters.gui.renderer.LabelProxyShape;
import net.sourceforge.waters.subject.base.IndexedSetSubject;
import net.sourceforge.waters.subject.module.GraphSubject;
import net.sourceforge.waters.subject.module.LabelGeometrySubject;
import net.sourceforge.waters.subject.module.NodeSubject;
import net.sourceforge.waters.subject.module.PointGeometrySubject;
import net.sourceforge.waters.subject.module.SimpleNodeSubject;


/**
 * The Command for creation of nodes.
 *
 * @author Simon Ware
 */

public class CreateNodeCommand
  implements Command
{

  //#########################################################################
  //# Constructor
  /**
   * Constructs a new CreateNodeCommand with the specified surface and
   * creates the node in the x,y position specified
   * @param graph  the graph edited by this command.
   * @param pos    the position where the node is created.
   */
  public CreateNodeCommand(final GraphSubject graph, final Point2D pos)
  {
    mGraph = graph;
    final PointGeometrySubject geo = new PointGeometrySubject(pos);
    final LabelGeometrySubject label =
      new LabelGeometrySubject(LabelProxyShape.DEFAULTOFFSET);
    final IndexedSetSubject<NodeSubject> nodes = graph.getNodesModifiable();
    final boolean initial = nodes.isEmpty();
    final PointGeometrySubject initgeo =
      initial ? new PointGeometrySubject(new Point(-5, -5)) : null;
    // Find a unique name!
    String name = "S0";
    for (int i = 0; nodes.containsName(name); i++) {
      name = "S" + i;
    }
    mCreated =
      new SimpleNodeSubject(name, null, initial, geo, initgeo, label);
  }

  public void execute()
  {
    mGraph.getNodesModifiable().add(mCreated);
  }

  public void undo()
  {
    mGraph.getNodesModifiable().remove(mCreated);
  }

  public String getName()
  {
    return mDescription;
  }

  public boolean isSignificant()
  {
    return true;
  }

  /**
   * Gets the node to be created by this command.
   */
  public SimpleNodeSubject getCreatedNode()
  {
    return mCreated;
  }


  //#########################################################################
  //# Data Members
  /** The ControlledSurface Edited with this Command */
  private final GraphSubject mGraph;
  /** The Node Created by this Command */
  private final SimpleNodeSubject mCreated;
  /** Description of Command */
  private final String mDescription = "Node Creation";

}
