//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters GUI
//# PACKAGE: net.sourceforge.waters.gui.command
//# CLASS:   MoveGuardActionBlockCommand
//###########################################################################
//# $Id: MoveGuardActionBlockCommand.java,v 1.1 2007-08-12 07:55:18 robi Exp $
//###########################################################################

package net.sourceforge.waters.gui.command;

import net.sourceforge.waters.subject.module.GuardActionBlockSubject;
import net.sourceforge.waters.subject.module.LabelGeometrySubject;


/**
 * A command to change the geometry of a guard/action block in a graph.
 *
 * @author Simon Ware, Robi Malik
 */

public class MoveGuardActionBlockCommand
  implements Command
{

  //#########################################################################
  //# Constructors
  public MoveGuardActionBlockCommand(final GuardActionBlockSubject orig,
                                     final GuardActionBlockSubject dummy)
  {
    mGuardActionBlock = orig;
    mOldGeometry = mGuardActionBlock.getGeometry().clone();
    mNewGeometry = dummy.getGeometry().clone();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.command.Command
  public void execute()
  {
    mGuardActionBlock.setGeometry(mNewGeometry);
  }

  public void undo()
  {
    mGuardActionBlock.setGeometry(mOldGeometry);
  }

  public boolean isSignificant()
  {
    return true;
  }

  public String getName()
  {
    return "Guard/Action Movement";
  }


  //#########################################################################
  //# Data Members
  private final GuardActionBlockSubject mGuardActionBlock;
  private final LabelGeometrySubject mOldGeometry;
  private final LabelGeometrySubject mNewGeometry;

}
