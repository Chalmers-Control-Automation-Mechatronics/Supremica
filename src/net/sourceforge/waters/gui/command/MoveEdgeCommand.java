//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters GUI
//# PACKAGE: net.sourceforge.waters.gui.command
//# CLASS:   MoveEdgeCommand
//###########################################################################
//# $Id: MoveEdgeCommand.java,v 1.11 2007-08-12 07:55:18 robi Exp $
//###########################################################################

package net.sourceforge.waters.gui.command;

import java.awt.geom.Point2D;

import net.sourceforge.waters.subject.module.EdgeSubject;
import net.sourceforge.waters.subject.module.PointGeometrySubject;
import net.sourceforge.waters.subject.module.SplineGeometrySubject;


/**
 * A command to change the geometry of an edge in a graph.
 *
 * @author Simon Ware, Robi Malik
 */

public class MoveEdgeCommand
  implements Command
{

  //#########################################################################
  //# Constructors
  public MoveEdgeCommand(final EdgeSubject orig, final EdgeSubject dummy)
  {
    mEdge = orig;
    final SplineGeometrySubject oGeo = mEdge.getGeometry();
    mOldGeometry = oGeo != null ? oGeo.clone() : null;
    final SplineGeometrySubject nGeo = dummy.getGeometry();
    mNewGeometry = nGeo != null ? nGeo.clone() : null;
    final PointGeometrySubject oStart = mEdge.getStartPoint();
    mOldStart = oStart != null ? oStart.clone() : null;
    final PointGeometrySubject oEnd = mEdge.getEndPoint();
    mOldEnd = oEnd != null ? oEnd.clone() : null;
    final PointGeometrySubject nStart = dummy.getStartPoint();
    mNewStart = nStart != null ? nStart.clone() : null;
    final PointGeometrySubject nEnd = dummy.getEndPoint();
    mNewEnd = nEnd != null ? nEnd.clone() : null;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.command.Command
  public void execute()
  {
    mEdge.setGeometry(mNewGeometry);
    mEdge.setStartPoint(mNewStart);
    mEdge.setEndPoint(mNewEnd);
  }

  public void undo()
  {
    mEdge.setGeometry(mOldGeometry);
    mEdge.setStartPoint(mOldStart);
    mEdge.setEndPoint(mOldEnd);
  }

  public boolean isSignificant()
  {
    return true;
  }

  public String getName()
  {
    return "Edge Reshaping";
  }


  //#########################################################################
  //# Data Members
  private final EdgeSubject mEdge;
  private final SplineGeometrySubject mOldGeometry;
  private final SplineGeometrySubject mNewGeometry;
  private final PointGeometrySubject mOldStart;
  private final PointGeometrySubject mNewStart;
  private final PointGeometrySubject mOldEnd;
  private final PointGeometrySubject mNewEnd;

}
