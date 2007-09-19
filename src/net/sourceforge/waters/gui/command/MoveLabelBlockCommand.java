//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters GUI
//# PACKAGE: net.sourceforge.waters.gui.command
//# CLASS:   MoveLabelBlockCommand
//###########################################################################
//# $Id: MoveLabelBlockCommand.java,v 1.2 2007-09-19 00:33:02 robi Exp $
//###########################################################################

package net.sourceforge.waters.gui.command;

import net.sourceforge.waters.model.module.ModuleProxyCloner;
import net.sourceforge.waters.subject.module.LabelGeometrySubject;
import net.sourceforge.waters.subject.module.LabelBlockSubject;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;


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
    final ModuleProxyCloner cloner = ModuleSubjectFactory.getCloningInstance();
    mOldGeometry = (LabelGeometrySubject) cloner.getClone(orig.getGeometry());
    mNewGeometry = (LabelGeometrySubject) cloner.getClone(dummy.getGeometry());
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
