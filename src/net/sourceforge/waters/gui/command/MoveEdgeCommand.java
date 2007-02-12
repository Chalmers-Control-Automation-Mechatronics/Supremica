//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui.command
//# CLASS:   MoveEdgeCommand
//###########################################################################
//# $Id: MoveEdgeCommand.java,v 1.9 2007-02-12 21:38:49 robi Exp $
//###########################################################################


package net.sourceforge.waters.gui.command;

import java.awt.geom.Point2D;

import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.gui.ControlledSurface;
import net.sourceforge.waters.gui.renderer.GeometryTools;

import net.sourceforge.waters.subject.module.EdgeSubject;
import net.sourceforge.waters.subject.module.GroupNodeSubject;
import net.sourceforge.waters.subject.module.NodeSubject;
import net.sourceforge.waters.subject.module.PointGeometrySubject;
import net.sourceforge.waters.subject.module.SplineGeometrySubject;


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
   * @param  point    The changed position, or <CODE>null</CODE>.
   */
  public MoveEdgeCommand(final ControlledSurface surface,
                         final EdgeSubject edge,
                         final NodeSubject neo,
                         final boolean isSource,
                         final Point2D point)
  {
    mSurface = surface;
    mEdge = edge;
    mNew = neo;
    if (neo instanceof GroupNodeSubject && point != null) {
      mNPos = new PointGeometrySubject(point);
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
    final SplineGeometrySubject geo = edge.getGeometry();
    mOldGeo = geo == null ? geo : geo.clone();
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
    GeometryTools.createDefaultGeometry(mEdge);
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
    mEdge.setGeometry(mOldGeo);
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
  private final SplineGeometrySubject mOldGeo;
  private final String mDescription;	

}
