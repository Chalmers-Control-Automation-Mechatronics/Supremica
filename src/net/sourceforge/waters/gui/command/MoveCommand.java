//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters GUI
//# PACKAGE: net.sourceforge.waters.gui.command
//# CLASS:   MoveCommand
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.gui.command;

import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.gui.language.ProxyNamer;
import net.sourceforge.waters.gui.transfer.SelectionOwner;
import net.sourceforge.waters.subject.base.GeometrySubject;
import net.sourceforge.waters.subject.base.ProxySubject;


/**
 * <P>A general command for changing a geometry object.</P>
 *
 * <P>This command is typically used after the user has moved some settings
 * some graphical object. a dialog. It is passed a geometry subject to be
 * modified, and a dummy geometry containing the new values. When executed,
 * all changes are applied at the same time, in an attempt to reduce the
 * number of geometry change notifications fired.</P>
 *
 * <P>In addition, after all but the first execution, and after each undo,
 * the parent of the changed geometry object is selected in its panel. This
 * feature can be disabled.</P>
 *
 * <P>The internal mechanism for the assignment is the {@link
 * GeometrySubject#assignFrom(GeometrySubject) assignFrom()} method, which
 * supports uniform assignments between subjects.</P>
 *
 * @author Robi Malik
 */

public class MoveCommand
  extends AbstractEditCommand
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new move command that does not affect the selection.
   * @param  subject   The geometry affected by this command.
   * @param  newstate  A template geometry to specify the desired state of the
   *                   subject after execution of the command. It should be
   *                   of a type assignable to the subject, but <I>not</I>
   *                   be the same object.
   */
  public MoveCommand(final GeometrySubject subject,
                     final GeometrySubject newstate)
  {
    this(subject, newstate, null);
  }

  /**
   * Creates a new move command.
   * @param  subject   The geometry affected by this command.
   * @param  newstate  A template geometry to specify the desired state of the
   *                   subject after execution of the command. It should be
   *                   of a type assignable to the subject, but <I>not</I>
   *                   be the same object.
   * @param  panel     The panel that contains the item and controls the
   *                   selection, or <CODE>null</CODE> if the selection
   *                   should remain unchanged.
   */
  public MoveCommand(final GeometrySubject subject,
                     final GeometrySubject newstate,
                     final SelectionOwner panel)
  {
    this(subject, newstate, panel, null);
  }

  /**
   * Creates a new move command.
   * @param  subject   The geometry affected by this command.
   * @param  newstate  A template geometry to specify the desired state of the
   *                   subject after execution of the command. It should be
   *                   of a type assignable to the subject, but <I>not</I>
   *                   be the same object.
   * @param  panel     The panel that contains the item and controls the
   *                   selection, or <CODE>null</CODE> if the selection
   *                   should remain unchanged.
   * @param  name      The description of the command.
   */
  public MoveCommand(final GeometrySubject subject,
                     final GeometrySubject newstate,
                     final SelectionOwner panel,
                     final String name)
  {
    super(panel, name, panel != null);
    mSubject = subject;
    mOldState = subject.clone();
    mNewState = newstate;
    if (name == null) {
      final ProxySubject parent = (ProxySubject) subject.getParent();
      final String newname = ProxyNamer.getItemClassName(parent) + " Movement";
      setName(newname);
    }
    mHasBeenExecuted = false;
  }
        

  //#########################################################################
  //# Simple Access
  /**
   * Gets the geometry affected by this command, in its current state.
   */
  public GeometrySubject getSubject()
  {
    return mSubject;
  }

  /**
   * Gets the state of the affected geometry before execution of the command.
   * The object returned is a clone of the original geometry in the state
   * before the command was first executed.
   */
  public GeometrySubject getOldState()
  {
    return mOldState;
  }

  /**
   * Gets the state of the affected geometry after execution of the command.
   * The object returned is the dummy object given by the user to specify
   * the desired value of the geometry after this command.
   */
  public GeometrySubject getNewState()
  {
    return mNewState;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.command.Command
  public void execute()
  {
    mSubject.assignFrom(mNewState);
    if (mHasBeenExecuted) {
      updateSelection();
    } else {
      mHasBeenExecuted = true;
    }
  }

  public void undo()
  {
    mSubject.assignFrom(mOldState);
    updateSelection();
  }


  //#########################################################################
  //# Auxiliary Methods
  private void updateSelection()
  {
    if (getUpdatesSelection()) {
      final SelectionOwner panel = getPanel();
      final ProxySubject parent = (ProxySubject) mSubject.getParent();
      final List<ProxySubject> list = Collections.singletonList(parent);
      panel.replaceSelection(list);
      panel.scrollToVisible(list);
      panel.activate();
    }
  }


  //#########################################################################
  //# Data Members
  private final GeometrySubject mSubject;
  private final GeometrySubject mNewState;
  private final GeometrySubject mOldState;
  private boolean mHasBeenExecuted;

}
