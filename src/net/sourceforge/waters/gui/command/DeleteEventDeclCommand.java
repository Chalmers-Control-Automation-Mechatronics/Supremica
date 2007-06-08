//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters GUI
//# PACKAGE: net.sourceforge.waters.gui.command
//# CLASS:   DeleteEventDeclCommand
//###########################################################################
//# $Id: DeleteEventDeclCommand.java,v 1.1 2007-06-08 16:09:52 robi Exp $
//###########################################################################

package net.sourceforge.waters.gui.command;

import java.util.Collection;

import net.sourceforge.waters.subject.module.EventDeclSubject;
import net.sourceforge.waters.subject.module.ModuleSubject;


/**
 * <P>A command for deleting an event declaration.</P>
 *
 * @author Robi Malik
 */

public class DeleteEventDeclCommand
  implements Command
{

  //#########################################################################
  //# Constructors
  /**
   * Deletes a new event declaration delete command.
   * @param  decl      The event declaration to be deleted.
   * @param  module    The module from which it is to be deleted.
   */
  public DeleteEventDeclCommand(final EventDeclSubject decl,
                                final ModuleSubject module)
  {
    mEventDecl = decl;
    mModule = module;
  }
        

  //#########################################################################
  //# Simple Access
  /**
   * Gets the event declaration deleted by this command.
   */
  public EventDeclSubject getEventDecl()
  {
    return mEventDecl;
  }

  /**
   * Gets the module affected by this command.
   */
  public ModuleSubject getModule()
  {
    return mModule;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.command.Command
  public void execute()
  {
    final Collection<EventDeclSubject> collection =
      mModule.getEventDeclListModifiable();
    collection.remove(mEventDecl);
  }

  public void undo()
  {
    final Collection<EventDeclSubject> collection =
      mModule.getEventDeclListModifiable();
    collection.add(mEventDecl);
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
  private final EventDeclSubject mEventDecl;
  private final ModuleSubject mModule;


  //#########################################################################
  //# Class Constants
  private static final String mDescription = "Delete Event Declaration";

}
