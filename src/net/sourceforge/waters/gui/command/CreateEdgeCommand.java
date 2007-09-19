//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui.command
//# CLASS:   CreateEdgeCommand
//###########################################################################
//# $Id: CreateEdgeCommand.java,v 1.21 2007-09-19 00:33:02 robi Exp $
//###########################################################################


package net.sourceforge.waters.gui.command;

import java.awt.Point;
import java.awt.geom.Point2D;

import net.sourceforge.waters.gui.renderer.GeometryTools;
import net.sourceforge.waters.gui.renderer.LabelBlockProxyShape;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.module.GuardActionBlockProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.ModuleProxyCloner;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.subject.module.EdgeSubject;
import net.sourceforge.waters.subject.module.GraphSubject;
import net.sourceforge.waters.subject.module.GuardActionBlockSubject;
import net.sourceforge.waters.subject.module.LabelBlockSubject;
import net.sourceforge.waters.subject.module.LabelGeometrySubject;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;
import net.sourceforge.waters.subject.module.NodeSubject;
import net.sourceforge.waters.subject.module.PointGeometrySubject;


/**
 * The command for the creation of edges.
 *
 * @author Simon Ware
 */

public class CreateEdgeCommand
  implements Command
{

  //#########################################################################
  //# Constructor
  /**
   * Constructs a new edge creation command.
   * The command creates a straight edge between the given source and
   * target nodes.
   * @param graph      The graph affected by the new command.
   * @param source     The source node of the new edge.
   * @param target     The target node of the new edge.
   * @param startPoint The start point geometry of the new edge.
   *                   This parameter is ignored and should be
   *                   <CODE>null</CODE> unless the edge's source is a group
   *                   node.
   * @param endPoint   The end point geometry of the new edge.
   *                   This parameter is ignored and should be
   *                   <CODE>null</CODE> unless the edge's target is a group
   *                   node.
   * @param labelBlock The label block for the new edge, or <CODE>null</CODE>
   *                   to create an empty one. To be cloned.     
   * @param gaBlock    The guard/action block for the new edge,
   *                   or <CODE>null</CODE>. To be cloned.     
   */
  public CreateEdgeCommand(final GraphSubject graph,
                           final NodeSubject source,
                           final NodeSubject target,
                           final Point2D startPoint,
                           final Point2D endPoint,
                           final LabelBlockProxy labelBlock,
                           final GuardActionBlockProxy gaBlock)
  {
    mGraph = graph;
    final PointGeometrySubject startGeo;
    if (source instanceof SimpleNodeProxy || startPoint == null) {
      startGeo = null;
    } else {
      startGeo = new PointGeometrySubject(startPoint);
    }
    final PointGeometrySubject endGeo;
    if (target instanceof SimpleNodeProxy || endPoint == null) {
      endGeo = null;
    } else {
      endGeo = new PointGeometrySubject(endPoint);
    }
    final ModuleProxyCloner cloner = ModuleSubjectFactory.getCloningInstance();
    final LabelBlockSubject labelClone;
    if (labelBlock == null) {
      final LabelGeometrySubject offset =
        new LabelGeometrySubject(LabelBlockProxyShape.DEFAULT_OFFSET);
      labelClone = new LabelBlockSubject(null, offset);
    } else {
      labelClone = (LabelBlockSubject) cloner.getClone(labelBlock);
    }
    final GuardActionBlockSubject gaClone =
      (GuardActionBlockSubject) cloner.getClone(gaBlock);
    mCreated = new EdgeSubject(source, target, labelClone, gaClone,
                               null, startGeo, endGeo);
    GeometryTools.createDefaultGeometry(mCreated);
  }

  /**
   * Executes the Creation of the Node
   */
  public void execute()
  {
    mGraph.getEdgesModifiable().add(mCreated);
  }

  /** 
   * Undoes the Command
   */
  public void undo()
  {
    mGraph.getEdgesModifiable().remove(mCreated);
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
   * Gets the edge to be created by this command.
   */
  public EdgeSubject getCreatedEdge()
  {
    return mCreated;
  }


  //#########################################################################
  //# Data Members
  /** The ControlledSurface edited with this Command */
  private final GraphSubject mGraph;
  /** The Node Created by this Command */
  private final EdgeSubject mCreated;
  private final String mDescription = "Edge Creation";

}
