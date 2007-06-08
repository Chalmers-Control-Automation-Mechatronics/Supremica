//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters GUI
//# PACKAGE: net.sourceforge.waters.gui.command
//# CLASS:   CreateEventDeclCommand
//###########################################################################
//# $Id: CreateEventDeclCommand.java,v 1.1 2007-06-08 16:09:52 robi Exp $
//###########################################################################

package net.sourceforge.waters.gui.command;

import java.util.Collection;

import net.sourceforge.waters.subject.module.EventDeclSubject;
import net.sourceforge.waters.subject.module.ModuleSubject;


/**
 * <P>A command for creating an event declaration.</P>
 *
 * @author Robi Malik
 */

public class CreateEventDeclCommand
  implements Command
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new event declaration create command.
   * @param  decl      The event declaration to be added.
   *                   The given object will be added to the module,
   *                   without any cloning.
   * @param  module    The module to which it is to be added.
   */
  public CreateEventDeclCommand(final EventDeclSubject decl,
                                final ModuleSubject module)
  {
    mEventDecl = decl;
    mModule = module;
  }
        

  //#########################################################################
  //# Simple Access
  /**
   * Gets the event declaration added by this command.
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
    collection.add(mEventDecl);
  }

  public void undo()
  {
    final Collection<EventDeclSubject> collection =
      mModule.getEventDeclListModifiable();
    collection.remove(mEventDecl);
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
  private static final String mDescription = "Create Event Declaration";

}
