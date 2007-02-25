//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters GUI
//# PACKAGE: net.sourceforge.waters.gui.command
//# CLASS:   CopyGraphCommand
//###########################################################################
//# $Id: CopyGraphCommand.java,v 1.3 2007-02-25 09:42:49 robi Exp $
//###########################################################################

package net.sourceforge.waters.gui.command;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;

import net.sourceforge.waters.gui.transfer.GraphContainer;
import net.sourceforge.waters.subject.base.ProxySubject;
import net.sourceforge.waters.subject.module.EdgeSubject;
import net.sourceforge.waters.subject.module.GraphSubject;
import net.sourceforge.waters.subject.module.NodeSubject;


/**
 * The Command to paste a set of nodes and edges into a graph.
 *
 * @author Simon Ware
 */

public class CopyGraphCommand
  implements Command
{

  //#########################################################################
  //# Constructor
  /**
   * Constructs a new CopyGraphCommand.
   * @param graph  The graph edited by this command.
   * @param cont   The container holding the objects to be pasted.
   * @param pos    The position where the objects are inserted.
   */
  public CopyGraphCommand(final GraphSubject graph,
                          final GraphContainer cont, 
                          final Point pos)
  {
    final GraphContainer copy = new GraphContainer(cont, graph, pos);
    mGraph = graph;
    mNodes = copy.getNodes();
    mEdges = copy.getEdges();
    // *** BUG ***
    // What about the blocked event list?
    // ***
  }


  //#########################################################################
  //# Simple Access
  /**
   * Gets a collection containing all nodes and edges created by this
   * command.
   */
  public Collection<ProxySubject> getPastedObjects()
  {
    final Collection<ProxySubject> objects =
      new ArrayList<ProxySubject>(mNodes.size() + mEdges.size());
    objects.addAll(mNodes);
    objects.addAll(mEdges);
    return objects;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.command.Command
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
    if (mEdges.isEmpty()) {
      return PASTE_NODES;
    } else {
      return PASTE_BOTH;
    }
  }

  public boolean isSignificant()
  {
    return true;
  }


  //#########################################################################
  //# Data Members
  /**
   * The Graph edited with this command.
   */
  private final GraphSubject mGraph;
  /**
   * The nodes created by this command.
   */
  private final Collection<NodeSubject> mNodes;
  /**
   * The edges created by this command.
   */
  private final Collection<EdgeSubject> mEdges;


  //#########################################################################
  //# Data Members
  private static final String PASTE_NODES = "Paste Nodes";
  private static final String PASTE_BOTH = "Paste Nodes and Edges";

}
