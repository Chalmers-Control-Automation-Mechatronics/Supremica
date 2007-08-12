//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters GUI
//# PACKAGE: net.sourceforge.waters.gui.command
//# CLASS:   MoveSimpleNodeCommand
//###########################################################################
//# $Id: MoveSimpleNodeCommand.java,v 1.1 2007-08-12 07:55:18 robi Exp $
//###########################################################################

package net.sourceforge.waters.gui.command;

import java.awt.geom.Point2D;

import net.sourceforge.waters.subject.module.LabelGeometrySubject;
import net.sourceforge.waters.subject.module.SimpleNodeSubject;
import net.sourceforge.waters.subject.module.PointGeometrySubject;


/**
 * A command to change the geometry of a simple node in a graph.
 *
 * @author Simon Ware, Robi Malik
 */

public class MoveSimpleNodeCommand
  implements Command
{

  //#########################################################################
  //# Constructors
  public MoveSimpleNodeCommand(final SimpleNodeSubject orig,
			       final SimpleNodeSubject dummy)
  {
    mNode = orig;
    mOldGeometry = mNode.getPointGeometry().clone();
    mNewGeometry = dummy.getPointGeometry().clone();
    if (mNode.getInitialArrowGeometry() != null) {
      mOldArrow = mNode.getInitialArrowGeometry().clone();
      mNewArrow = dummy.getInitialArrowGeometry().clone();
    } else {
      mOldArrow = null;
      mNewArrow = null;
    }
    final LabelGeometrySubject oldgeo = orig.getLabelGeometry();
    final LabelGeometrySubject newgeo = dummy.getLabelGeometry();
    if (oldgeo.equalsWithGeometry(newgeo)) {
      mLabelMove = null;
    } else {
      mLabelMove = new MoveLabelGeometryCommand(oldgeo, newgeo);
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.command.Command
  public void execute()
  {
    mNode.setPointGeometry(mNewGeometry);
    mNode.setInitialArrowGeometry(mNewArrow);
    if (mLabelMove != null) {
      mLabelMove.execute();
    }
  }

  public void undo()
  {
    mNode.setPointGeometry(mOldGeometry);
    mNode.setInitialArrowGeometry(mOldArrow);
    if (mLabelMove != null) {
      mLabelMove.undo();
    }
  }

  public boolean isSignificant()
  {
    return true;
  }

  public String getName()
  {
    final Point2D oldpos = mOldGeometry.getPoint();
    final Point2D newpos = mNewGeometry.getPoint();
    if (!oldpos.equals(newpos)) {
      return "Node Movement";
    }
    if (mOldArrow != null) {
      final Point2D oldinit = mOldArrow.getPoint();
      final Point2D newinit = mNewArrow.getPoint();
      if (!oldinit.equals(newinit)) {
	return "Initial Arrow Movement";
      }
    }
    if (mLabelMove != null) {
      return mLabelMove.getName();
    }
    return "(No node movement)";
  }


  //#########################################################################
  //# Data Members
  private final SimpleNodeSubject mNode;
  private final PointGeometrySubject mOldGeometry;
  private final PointGeometrySubject mNewGeometry;
  private final PointGeometrySubject mOldArrow;
  private final PointGeometrySubject mNewArrow;
  private final Command mLabelMove;

}
