//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui.command
//# CLASS:   CreateEdgeCommand
//###########################################################################
//# $Id: CreateEdgeCommand.java,v 1.16 2007-02-02 02:55:13 robi Exp $
//###########################################################################


package net.sourceforge.waters.gui.command;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Collections;

import net.sourceforge.waters.gui.renderer.GeometryTools;
import net.sourceforge.waters.gui.renderer.LabelBlockProxyShape;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.subject.module.GuardActionBlockSubject;
import net.sourceforge.waters.subject.module.PointGeometrySubject;
import net.sourceforge.waters.subject.module.SplineGeometrySubject;
import net.sourceforge.waters.subject.module.EdgeSubject;
import net.sourceforge.waters.subject.module.LabelGeometrySubject;
import net.sourceforge.waters.subject.module.LabelBlockSubject;
import net.sourceforge.waters.subject.module.NodeSubject;
import net.sourceforge.waters.subject.module.GraphSubject;

import net.sourceforge.waters.xsd.module.SplineKind;


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
   */
  public CreateEdgeCommand(final GraphSubject graph,
                           final NodeSubject source,
                           final NodeSubject target,
                           Point2D startPoint,
                           Point2D endPoint)
  {
    mGraph = graph;
    final PointGeometrySubject startGeo;
    if (source instanceof SimpleNodeProxy) {
      final SimpleNodeProxy simple = (SimpleNodeProxy) source;
      startPoint = simple.getPointGeometry().getPoint();
      startGeo = null;
    } else {
      startGeo = new PointGeometrySubject(startPoint);
    }
    final PointGeometrySubject endGeo;
    if (target instanceof SimpleNodeProxy) {
      final SimpleNodeProxy simple = (SimpleNodeProxy) target;
      endPoint = simple.getPointGeometry().getPoint();
      endGeo = null;
    } else {
      endGeo = new PointGeometrySubject(endPoint);
    }
    final Point2D point;
    if (!startPoint.equals(endPoint)) {
      point = GeometryTools.getMidPoint(startPoint, endPoint);
    } else {
      point = new Point((int) endPoint.getX() + 20,
                        (int) endPoint.getY() + 20);
    }
    final Collection<Point2D> points = Collections.singleton(point);
    final LabelGeometrySubject offset =
      new LabelGeometrySubject(new Point(LabelBlockProxyShape.DEFAULTOFFSETX,
                                         LabelBlockProxyShape.DEFAULTOFFSETY));
    final LabelBlockSubject labelBlock = 
      new LabelBlockSubject(null, offset);
    final GuardActionBlockSubject guardActionBlock = null;
    final SplineGeometrySubject spline = 
      new SplineGeometrySubject(points, SplineKind.INTERPOLATING);
    mCreated =	new EdgeSubject(source, target, labelBlock, guardActionBlock,
                                spline, startGeo, endGeo);
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


  //#########################################################################
  //# Data Members
  /** The ControlledSurface edited with this Command */
  private final GraphSubject mGraph;
  /** The Node Created by this Command */
  private final EdgeSubject mCreated;
  private final String mDescription = "Edge Creation";

}
