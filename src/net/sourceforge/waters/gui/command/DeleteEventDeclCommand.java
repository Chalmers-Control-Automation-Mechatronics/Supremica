//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters GUI
//# PACKAGE: net.sourceforge.waters.gui.command
//# CLASS:   DeleteEventDeclCommand
//###########################################################################
//# $Id: DeleteEventDeclCommand.java,v 1.2 2007-06-11 15:07:51 robi Exp $
//###########################################################################

package net.sourceforge.waters.gui.command;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import net.sourceforge.waters.subject.module.EventDeclSubject;
import net.sourceforge.waters.subject.module.ModuleSubject;


/**
 * <P>A command for deleting one ore more event declarations.</P>
 *
 * @author Robi Malik
 */

public class DeleteEventDeclCommand
  implements Command
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a command to delete a single event declaration.
   * @param  victim      The event declaration to be deleted.
   * @param  module    The module from which it is to be deleted.
   */
  public DeleteEventDeclCommand(final EventDeclSubject victim,
                                final ModuleSubject module)
  {
    this(Collections.singletonList(victim), module);
  }

  /**
   * Creates a command to delete several event declarations.
   * @param  decls     Collection of event declarations to be deleted.
   * @param  module    The module from which they are to be deleted.
   */
  public DeleteEventDeclCommand(final Iterable<EventDeclSubject> victims,
                                final ModuleSubject module)
  {
    mVictims = new LinkedList<EventDeclSubject>();
    mModule = module;
    for (final EventDeclSubject victim : victims) {
      mVictims.add(victim);
    }
  }


  //#########################################################################
  //# Simple Access
  /**
   * Gets the event declaration deleted by this command.
   */
  public Collection<EventDeclSubject> getVictims()
  {
    return mVictims;
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
    collection.removeAll(mVictims);
  }

  public void undo()
  {
    final Collection<EventDeclSubject> collection =
      mModule.getEventDeclListModifiable();
    collection.addAll(mVictims);
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
  private final Collection<EventDeclSubject> mVictims;
  private final ModuleSubject mModule;


  //#########################################################################
  //# Class Constants
  private static final String mDescription = "Delete Event Declarations";

}
