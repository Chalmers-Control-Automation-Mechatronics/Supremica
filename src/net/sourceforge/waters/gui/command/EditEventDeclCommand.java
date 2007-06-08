//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters GUI
//# PACKAGE: net.sourceforge.waters.gui.command
//# CLASS:   EditEventDeclCommand
//###########################################################################
//# $Id: EditEventDeclCommand.java,v 1.1 2007-06-08 16:09:52 robi Exp $
//###########################################################################

package net.sourceforge.waters.gui.command;

import net.sourceforge.waters.subject.module.EventDeclSubject;


/**
 * A command for modifying an event declaration. This command is used after
 * the user has edited an event declaration using a dialog, to commit the
 * changes.
 *
 * @author Robi Malik
 */

public class EditEventDeclCommand
  extends AbstractEditCommand
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new event declaration edit command.
   * @param  decl      The event declaration affected by this command.
   * @param  newstate  A template event declaration to specify the desired
   *                   state of the event declaration after execution of
   *                   the command.
   */
  public EditEventDeclCommand(final EventDeclSubject decl,
                              final EventDeclSubject newstate)
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
  private static final String mDescription = "Edit Event Declaration";

}
