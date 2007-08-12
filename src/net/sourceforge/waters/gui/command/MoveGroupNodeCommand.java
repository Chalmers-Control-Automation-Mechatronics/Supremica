//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters GUI
//# PACKAGE: net.sourceforge.waters.gui.command
//# CLASS:   MoveGroupNodeCommand
//###########################################################################
//# $Id: MoveGroupNodeCommand.java,v 1.1 2007-08-12 07:55:18 robi Exp $
//###########################################################################

package net.sourceforge.waters.gui.command;

import java.awt.geom.Rectangle2D;

import net.sourceforge.waters.subject.module.BoxGeometrySubject;
import net.sourceforge.waters.subject.module.GroupNodeSubject;


/**
 * A command to change the geometry of a group node in a graph.
 *
 * @author Simon Ware, Robi Malik
 */

public class MoveGroupNodeCommand
  implements Command
{

  //#########################################################################
  //# Constructors
  public MoveGroupNodeCommand(final GroupNodeSubject orig,
                              final GroupNodeSubject dummy)
  {
    mNode = orig;
    mOldGeometry = mNode.getGeometry().clone();
    mNewGeometry = dummy.getGeometry().clone();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.command.Command
  public void execute()
  {
    mNode.setGeometry(mNewGeometry);
  }

  public void undo()
  {
    mNode.setGeometry(mOldGeometry);
  }

  public boolean isSignificant()
  {
    return true;
  }

  public String getName()
  {
    final Rectangle2D oldrect = mOldGeometry.getRectangle();
    final Rectangle2D newrect = mNewGeometry.getRectangle();
    if (oldrect.getWidth() != newrect.getWidth() ||
        oldrect.getHeight() != newrect.getHeight()) {
      return "Group Node Reshaping";
    } else {
      return "Group Node Movement";
    }
  }


  //#########################################################################
  //# Data Members
  private final GroupNodeSubject mNode;
  private final BoxGeometrySubject mOldGeometry;
  private final BoxGeometrySubject mNewGeometry;

}
