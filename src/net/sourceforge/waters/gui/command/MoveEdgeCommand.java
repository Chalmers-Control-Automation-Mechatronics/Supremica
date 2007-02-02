//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui.command
//# CLASS:   MoveEdgeCommand
//###########################################################################
//# $Id: MoveEdgeCommand.java,v 1.8 2007-02-02 02:55:13 robi Exp $
//###########################################################################


package net.sourceforge.waters.gui.command;

import java.awt.geom.Point2D;
import java.awt.Point;

import java.util.Collection;
import java.util.Collections;

import net.sourceforge.waters.gui.ControlledSurface;
import net.sourceforge.waters.gui.renderer.GeometryTools;

import net.sourceforge.waters.subject.module.EdgeSubject;
import net.sourceforge.waters.subject.module.GroupNodeSubject;
import net.sourceforge.waters.subject.module.NodeSubject;
import net.sourceforge.waters.subject.module.PointGeometrySubject;
import net.sourceforge.waters.subject.module.SplineGeometrySubject;
import net.sourceforge.waters.xsd.module.SplineKind;


/**
 * The command for changing the source or target of an edge.
 *
 * @author Simon Ware
 */

public class MoveEdgeCommand
    implements Command
{

  //#########################################################################
  //# Constructor
  /**
   * Creates a new edge move command.
   * @param  surface  The panel affected.
   * @param  edge     The edge to be modified.
   * @param  neo      The new source or target node.
   * @param  isSource True if the source node is changed,
   *                  false if the target node is changed.
   * @param  x        The x coordinate of the changed position.
   * @param  y        The y coordinate of the changed position.
   */
  public MoveEdgeCommand(final ControlledSurface surface,
                         final EdgeSubject edge,
                         final NodeSubject neo,
                         final boolean isSource,
                         final int x,
                         final int y)
  {
    mSurface = surface;
    mEdge = edge;
    mNew = neo;
    if (neo instanceof GroupNodeSubject) {
      mNPos = new PointGeometrySubject(new Point(x, y));
    } else {
      mNPos = null;
    }
    mIsSource = isSource;
    if (isSource) {
      mOld = edge.getSource();
      mOPos = edge.getStartPoint();
      mDescription = "Change Edge Source";
    } else {
      mOld = edge.getTarget();
      mOPos = edge.getEndPoint();
      mDescription = "Change Edge Target";
    }
    mOTPoint = edge.getGeometry().clone();
  }

  public void execute()
  {
    if (mIsSource) {
      mEdge.setSource(mNew);
      mEdge.setStartPoint(mNPos);
    } else {
      mEdge.setTarget(mNew);
      mEdge.setEndPoint(mNPos);
    }
    final Point2D p;
    if (mEdge.getTarget() != mEdge.getSource()) {
      final Point2D start = GeometryTools.getStartPoint(mEdge);
      final Point2D end = GeometryTools.getEndPoint(mEdge);
      p = GeometryTools.getMidPoint(start, end);
    } else {
      p = GeometryTools.getStartPoint(mEdge);
      p.setLocation(p.getX() + 20, p.getY() + 20);
    }
    final Collection<Point2D> points = Collections.singleton(p);
    mEdge.setGeometry(new SplineGeometrySubject(points, 
                                                SplineKind.INTERPOLATING));
    mSurface.getEditorInterface().setDisplayed();
  }

  public void undo()
  {
    if (mIsSource) {
      mEdge.setSource(mOld);
      mEdge.setStartPoint(mOPos);
    } else {
      mEdge.setTarget(mOld);
      mEdge.setEndPoint(mOPos);
    }
    mEdge.setGeometry(mOTPoint);
    mSurface.getEditorInterface().setDisplayed();
  }
	
  public boolean isSignificant()
  {
    return true;
  }

  public String getName()
  {
    return mDescription;
  }


  //#########################################################################
  //# Data Members
  private final ControlledSurface mSurface;
  private final EdgeSubject mEdge;
  private final NodeSubject mOld;
  private final NodeSubject mNew;
  private final boolean mIsSource;
  private final PointGeometrySubject mNPos;
  private final PointGeometrySubject mOPos;
  private final SplineGeometrySubject mOTPoint;
  private final String mDescription;	

}
