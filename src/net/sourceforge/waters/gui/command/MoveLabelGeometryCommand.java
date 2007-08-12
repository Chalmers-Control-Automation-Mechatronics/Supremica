//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters GUI
//# PACKAGE: net.sourceforge.waters.gui.command
//# CLASS:   MoveLabelGeometryCommand
//###########################################################################
//# $Id: MoveLabelGeometryCommand.java,v 1.1 2007-08-12 07:55:18 robi Exp $
//###########################################################################

package net.sourceforge.waters.gui.command;

import java.awt.geom.Point2D;

import net.sourceforge.waters.subject.module.LabelGeometrySubject;


/**
 * A command to change a label geometry in a graph.
 *
 * @author Simon Ware, Robi Malik
 */

public class MoveLabelGeometryCommand
  implements Command
{

  //#########################################################################
  //# Constructors
  public MoveLabelGeometryCommand(final LabelGeometrySubject orig,
                                  final LabelGeometrySubject dummy)
  {
    mLabel = orig;
    mOldPoint = mLabel.getOffset();
    mNewPoint = dummy.getOffset();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.command.Command
  public void execute()
  {
    mLabel.setOffset(mNewPoint);
  }

  public void undo()
  {
    mLabel.setOffset(mOldPoint);
  }

  public boolean isSignificant()
  {
    return true;
  }

  public String getName()
  {
    return "Node Label Movement";
  }


  //#########################################################################
  //# Data Members
  private final LabelGeometrySubject mLabel;
  private final Point2D mOldPoint;
  private final Point2D mNewPoint;

}
