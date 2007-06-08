//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters GUI
//# PACKAGE: net.sourceforge.waters.gui.command
//# CLASS:   AbstractEditCommand
//###########################################################################
//# $Id: AbstractEditCommand.java,v 1.1 2007-06-08 16:09:52 robi Exp $
//###########################################################################

package net.sourceforge.waters.gui.command;

import net.sourceforge.waters.subject.base.ProxySubject;


/**
 * <P>A command for modifying an object.</P>
 *
 * <P>This command is typically used after the user has edited some settings
 * using a dialog. It is passed a subject to be modified, and a dummy
 * object containing the new values. When executed, all changes are
 * applied at the same time, in an attempt to reduce the number of
 * change notifications fired.</P>
 *
 * <P>The internal mechanism for the assignment is the {@link
 * ProxySubject#assignFrom(ProxySubject) assignFrom()} method, which
 * supports uniform assignments between subjects.</P>
 *
 * <P>Different subclasses of this class exist, for the different types
 * of objects that can be edited.</P>
 *
 * @author Robi Malik
 */

public abstract class AbstractEditCommand
  implements Command
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new edit command.
   * @param  subject   The subject affected by this command.
   * @param  newstate  A template subject to specify the desired state of the
   *                   subject after execution of the command. It should be
   *                   of a type assignable to the subject, but <I>not</I>
   *                   be the same object.
   */
  public AbstractEditCommand(final ProxySubject subject,
                             final ProxySubject newstate)
  {
    mSubject = subject;
    mOldState = subject.clone();
    mNewState = newstate;
  }
        

  //#########################################################################
  //# Simple Access
  /**
   * Gets the subject affected by this command, in its current state.
   */
  public ProxySubject getSubject()
  {
    return mSubject;
  }

  /**
   * Gets the state of the affected subject before execution of the command.
   * The object returned is a clone of the original subject in the state
   * before the command was first executed.
   */
  public ProxySubject getOldState()
  {
    return mOldState;
  }

  /**
   * Gets the state of the affected subject after execution of the command.
   * The object returned is the dummy object given by the user to specify
   * the desired value of the subject after this command.
   */
  public ProxySubject getNewState()
  {
    return mNewState;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.command.Command
  public void execute()
  {
    mSubject.assignFrom(mNewState);
  }

  public void undo()
  {
    mSubject.assignFrom(mOldState);
  }

  public boolean isSignificant()
  {
    return true;
  }


  //#########################################################################
  //# Data Members
  private final ProxySubject mSubject;
  private final ProxySubject mNewState;
  private final ProxySubject mOldState;

}
