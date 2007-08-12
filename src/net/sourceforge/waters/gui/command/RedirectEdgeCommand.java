//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui.command
//# CLASS:   RedirectEdgeCommand
//###########################################################################
//# $Id: RedirectEdgeCommand.java,v 1.1 2007-08-12 07:55:18 robi Exp $
//###########################################################################


package net.sourceforge.waters.gui.command;

import java.awt.geom.Point2D;

import java.util.Collections;
import java.util.List;

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

public class RedirectEdgeCommand
  implements Command
{

  //#########################################################################
  //# Constructor
  /**
   * Creates a new edge move command.
   * @param  edge     The edge to be modified.
   * @param  neo      The new source or target node.
   * @param  isSource True if the source node is changed,
   *                  false if the target node is changed.
   * @param  point    The changed position, or <CODE>null</CODE>.
   */
  public RedirectEdgeCommand(final EdgeSubject edge,
                             final NodeSubject neo,
                             final boolean isSource,
                             final Point2D point)
  {
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
      mDescription = "Edge Source Change";
    } else {
      mOld = edge.getTarget();
      mOPos = edge.getEndPoint();
      mDescription = "Edge Target Change";
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
  private final EdgeSubject mEdge;
  private final NodeSubject mOld;
  private final NodeSubject mNew;
  private final boolean mIsSource;
  private final PointGeometrySubject mNPos;
  private final PointGeometrySubject mOPos;
  private final SplineGeometrySubject mOldGeo;
  private final String mDescription;	

}
