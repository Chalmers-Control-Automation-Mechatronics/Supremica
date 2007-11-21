//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters GUI
//# PACKAGE: net.sourceforge.waters.gui.command
//# CLASS:   EditVariableComponentCommand
//###########################################################################
//# $Id: EditVariableComponentCommand.java,v 1.1 2007-11-21 23:42:26 robi Exp $
//###########################################################################

package net.sourceforge.waters.gui.command;

import net.sourceforge.waters.subject.module.VariableComponentSubject;


/**
 * A command for modifying a variable. This command is used after the user
 * has edited a variable using a dialog, to commit the changes.
 *
 * @author Robi Malik
 */

public class EditVariableComponentCommand
  extends AbstractEditCommand
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new variable edit command.
   * @param  decl      The variable affected by this command.
   * @param  newstate  A template variable to specify the desired state of
   *                   the variable after execution of the command.
   */
  public EditVariableComponentCommand(final VariableComponentSubject decl,
                                      final VariableComponentSubject newstate)
  {
    super(decl, newstate);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.command.Command
  public String getName()
  {
    return mDescription;
  }


  //#########################################################################
  //# Class Constants
  private static final String mDescription = "Edit Variable";

}
