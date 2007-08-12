//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters GUI
//# PACKAGE: net.sourceforge.waters.gui.command
//# CLASS:   MoveLabelBlockCommand
//###########################################################################
//# $Id: MoveLabelBlockCommand.java,v 1.1 2007-08-12 07:55:18 robi Exp $
//###########################################################################

package net.sourceforge.waters.gui.command;

import net.sourceforge.waters.subject.module.LabelGeometrySubject;
import net.sourceforge.waters.subject.module.LabelBlockSubject;


/**
 * A command to change the geometry of a label block in a graph.
 *
 * @author Simon Ware, Robi Malik
 */

public class MoveLabelBlockCommand
  implements Command
{

  //#########################################################################
  //# Constructors
  public MoveLabelBlockCommand(final LabelBlockSubject orig,
			       final LabelBlockSubject dummy)
  {
    mLabelBlock = orig;
    mOldGeometry = mLabelBlock.getGeometry().clone();
    mNewGeometry = dummy.getGeometry().clone();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.command.Command
  public void execute()
  {
    mLabelBlock.setGeometry(mNewGeometry);
  }

  public void undo()
  {
    mLabelBlock.setGeometry(mOldGeometry);
  }

  public boolean isSignificant()
  {
    return true;
  }

  public String getName()
  {
    return "Event Label Movement";
  }


  //#########################################################################
  //# Data Members
  private final LabelBlockSubject mLabelBlock;
  private final LabelGeometrySubject mOldGeometry;
  private final LabelGeometrySubject mNewGeometry;

}
