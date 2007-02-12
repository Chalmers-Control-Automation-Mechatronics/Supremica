//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui.command
//# CLASS:   CreateNodeCommand
//###########################################################################
//# $Id: CreateNodeCommand.java,v 1.17 2007-02-12 21:38:49 robi Exp $
//###########################################################################


package net.sourceforge.waters.gui.command;

import net.sourceforge.waters.gui.EditorGraph;
import java.awt.Point;

import net.sourceforge.waters.gui.renderer.LabelProxyShape;
import net.sourceforge.waters.subject.base.IndexedSetSubject;
import net.sourceforge.waters.subject.module.GraphSubject;
import net.sourceforge.waters.subject.module.LabelGeometrySubject;
import net.sourceforge.waters.subject.module.NodeSubject;
import net.sourceforge.waters.subject.module.PlainEventListSubject;
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
   * @param graph the surface edited by this command
   * @param x the position upon which the node is created
   * @param y the position upon which the node is created
   */
  public CreateNodeCommand(GraphSubject graph, int x, int y)
  {
    mGraph = graph;
    final PlainEventListSubject props =
      new PlainEventListSubject(null);
    final PointGeometrySubject point = new PointGeometrySubject(
      new Point(x, y));
    final LabelGeometrySubject label = new LabelGeometrySubject(
      new Point(LabelProxyShape.DEFAULTOFFSETX,
                LabelProxyShape.DEFAULTOFFSETY));
    final IndexedSetSubject<NodeSubject> nodes = graph.getNodesModifiable();
    final boolean initial = nodes.isEmpty();
    final PointGeometrySubject initarrow = initial ? 
                                           new PointGeometrySubject(new Point(-5, -5))
                                           :  null;
                                                                              
    // Find a unique name!
    String name = "S0";
    for (int i = 0; nodes.containsName(name); i++) {
      name = "S" + i;
    }
    mCreated = new SimpleNodeSubject(name, props, initial, point, initarrow, label);
  }

  /**
   * Executes the Creation of the Node
   */
  public void execute()
  {
    mGraph.getNodesModifiable().add(mCreated);
//    EditorGraph.updateChildNodes(mGraph);
  }

  /** 
   * Undoes the Command
   */    
  public void undo()
  {
    mGraph.getNodesModifiable().remove(mCreated);
    //EditorGraph.updateChildNodes(mGraph);
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
