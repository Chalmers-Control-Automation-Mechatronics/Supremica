//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters GUI
//# PACKAGE: net.sourceforge.waters.gui.command
//# CLASS:   CreateNodeCommand
//###########################################################################
//# $Id: CopyGraphCommand.java,v 1.1 2007-02-22 20:34:17 siw4 Exp $
//###########################################################################

package net.sourceforge.waters.gui.command;

import java.awt.Point;
import java.awt.geom.Point2D;

import net.sourceforge.waters.gui.transfer.GraphContainer;
import net.sourceforge.waters.gui.EditorGraph;
import net.sourceforge.waters.gui.renderer.LabelProxyShape;
import net.sourceforge.waters.subject.base.IndexedSetSubject;
import net.sourceforge.waters.subject.module.GraphSubject;
import net.sourceforge.waters.subject.module.LabelGeometrySubject;
import net.sourceforge.waters.subject.module.NodeSubject;
import net.sourceforge.waters.subject.module.PointGeometrySubject;
import net.sourceforge.waters.subject.module.SimpleNodeSubject;
import net.sourceforge.waters.model.base.DuplicateNameException;
import java.util.Collection;
import net.sourceforge.waters.subject.module.EdgeSubject;
import java.util.ArrayList;


/**
 * The Command for creation of nodes.
 *
 * @author Simon Ware
 */

public class CopyGraphCommand
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
  public CopyGraphCommand(final GraphSubject graph, final GraphContainer cont, 
                          Point pos)
  {
    if (pos == null) {
      pos = new Point(0, 0);
    }
    mGraph = graph;
    System.out.println("nodes");
    IndexedSetSubject nodes = cont.getNodes(pos);
    System.out.println("edges");
    mEdges = cont.getEdges(nodes, pos);
    System.out.println("done");
    Collection<NodeSubject> check;
    Collection<NodeSubject> tobeChecked = nodes;
    while (!tobeChecked.isEmpty()) {
      check = tobeChecked;
      tobeChecked = new ArrayList<NodeSubject>();
      for (NodeSubject node : check) {
        if (graph.getNodesModifiable().containsName(node.getName())) {
          tobeChecked.add(node);
        }
      }
      for (NodeSubject node : tobeChecked) {
        node.setName("copy of " + node.getName());
      }
    }
    mNodes = new ArrayList<NodeSubject>(nodes);
    nodes.removeAll(mNodes);
    System.out.println("names");
  }

  public void execute()
  {
    mGraph.getNodesModifiable().addAll(mNodes);
    mGraph.getEdgesModifiable().addAll(mEdges);
  }

  public void undo()
  {
    mGraph.getNodesModifiable().removeAll(mNodes);
    mGraph.getEdgesModifiable().removeAll(mEdges);
  }

  public String getName()
  {
    return mDescription;
  }

  public boolean isSignificant()
  {
    return true;
  }

  //#########################################################################
  //# Data Members
  /** The ControlledSurface Edited with this Command */
  private final GraphSubject mGraph;
  /** The Node Created by this Command */
  private final Collection<NodeSubject> mNodes;
  private final Collection<EdgeSubject> mEdges;
  /** Description of Command */
  private final String mDescription = "Copy Graph";

}
