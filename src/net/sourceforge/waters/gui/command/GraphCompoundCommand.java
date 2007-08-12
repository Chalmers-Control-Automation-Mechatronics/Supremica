//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters GUI
//# PACKAGE: net.sourceforge.waters.gui.command
//# CLASS:   GraphCompoundCommand
//###########################################################################
//# $Id: GraphCompoundCommand.java,v 1.1 2007-08-12 07:55:18 robi Exp $
//###########################################################################

package net.sourceforge.waters.gui.command;

import net.sourceforge.waters.subject.module.GraphSubject;
import net.sourceforge.waters.gui.EditorGraph;


/**
 * A more specialised compound command for graphs.
 * This command automatically updates the graph's group node
 * hierarchy after execution and undo.
 *
 * @author Robi Malik
 */

public class GraphCompoundCommand
  extends CompoundCommand
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new graph compound command.
   * @param graph       The graph edited by this command and which needs
   *                    update of the group node hierarchy,
   *                    or <CODE>null</CODE>.
   * @param description The description to be used for the command, or
   *                    <CODE>null</CODE> to use a default name.
   */
  public GraphCompoundCommand(final GraphSubject graph,
                              final String description)
  {
    super(description);
    mGraph = graph;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.command.Command
  public void execute()
  {
    super.execute();
    if (mGraph != null) {
      EditorGraph.updateChildNodes(mGraph);
    }
  }

  public void undo()
  {
    super.undo();
    if (mGraph != null) {
      EditorGraph.updateChildNodes(mGraph);
    }
  }


  //#########################################################################
  //# Data Members
  private final GraphSubject mGraph;

}
