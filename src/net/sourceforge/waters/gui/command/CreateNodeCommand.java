//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui.command
//# CLASS:   CreateNodeCommand
//###########################################################################
//# $Id: CreateNodeCommand.java,v 1.10 2006-07-20 02:28:37 robi Exp $
//###########################################################################


package net.sourceforge.waters.gui.command;

import java.awt.Point;

import java.util.Collection;
import java.util.Collections;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import net.sourceforge.waters.gui.ControlledSurface;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.subject.module.EventListExpressionSubject;
import net.sourceforge.waters.subject.module.PlainEventListSubject;
import net.sourceforge.waters.subject.module.GraphSubject;
import net.sourceforge.waters.subject.module.PointGeometrySubject;
import net.sourceforge.waters.subject.module.LabelGeometrySubject;
import net.sourceforge.waters.subject.module.SimpleNodeSubject;
import net.sourceforge.waters.gui.renderer.LabelProxyShape;


/**
 * The Command for creation of nodes.
 *
 * @author Simon Ware
 */

public class CreateNodeCommand
    implements Command
{

	//#######################################################################
	//# Constructor
  /**
   * Constructs a new CreateNodeCommand with the specified surface and
   * creates the node in the x,y position specified
   * @param surface the surface edited by this command
   * @param x the position upon which the node is created
   * @param y the position upon which the node is created
   */
  public CreateNodeCommand(GraphSubject graph, int x, int y)
  {
    mGraph = graph;
    // Find a unique name!
    final Collection<Proxy> empty = Collections.emptyList();
    final EventListExpressionSubject props =
      new PlainEventListSubject(empty);
    final PointGeometrySubject point = new PointGeometrySubject(
      new Point(x, y));
    final LabelGeometrySubject label = new LabelGeometrySubject(
      new Point(LabelProxyShape.DEFAULTOFFSETX, LabelProxyShape.DEFAULTOFFSETY));
    
    String n = "S0";
    for (int i = 0; graph.getNodesModifiable().containsName(n); i++) {
      n = "S" + i;
    }
    mCreated = new SimpleNodeSubject(n, props, false, point, null, label);
  }

  /**
   * Executes the Creation of the Node
   */
  public void execute()
  {
    mGraph.getNodesModifiable().add(mCreated);
  }

  /** 
   * Undoes the Command
   */    

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

	//#######################################################################
	//# Data Members
  /** The ControlledSurface Edited with this Command */
  private final GraphSubject mGraph;
  /** The Node Created by this Command */
  private final SimpleNodeSubject mCreated;
  /** Description of Command */
  private final String mDescription = "Node Creation";

}
