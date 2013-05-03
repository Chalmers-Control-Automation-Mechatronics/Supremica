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
import net.sourceforge.waters.subject.base.UndoInfo;
import net.sourceforge.waters.subject.module.SimpleNodeSubject;


/**
 * <P>A general command for changing the geometry of a single graphical
 * object.</P>
 *
 * <P>This command is typically used after the user has moved a
 * graphical object. It is passed a {@link GeometrySubject} to be
 * modified, and a dummy geometry containing the new values. When executed,
 * all changes are applied at the same time, in an attempt to reduce the
 * number of geometry change notifications fired.</P>
 *
 * <P>In addition, after all but the first execution, and after each undo,
 * the parent of the changed geometry object is selected in its panel. This
 * feature can be disabled.</P>
 *
 * <P>The internal mechanism for the assignment is the {@link
 * GeometrySubject#createUndoInfo(ProxySubject) createUndoInfo()} method,
 * which supports uniform assignments between subjects.</P>
 *
 * <P>This command only supports the geometry change for a single graphical
 * object. To modify several objects at the same time, multiple MoveCommand
 * objects can be grouped in a {@link CompoundCommand}.</P>
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
   * @param  newState  A template geometry to specify the desired state of the
   *                   subject after execution of the command. It should be
   *                   of a type assignable to the subject, but <I>not</I>
   *                   be the same object.
   */
  public MoveCommand(final GeometrySubject subject,
                     final GeometrySubject newState)
  {
    this(subject, newState, null);
  }

  /**
   * Creates a new move command.
   * @param  subject   The geometry affected by this command.
   * @param  newState  A template geometry to specify the desired state of the
   *                   subject after execution of the command. It should be
   *                   of a type assignable to the subject, but <I>not</I>
   *                   be the same object.
   * @param  panel     The panel that contains the item and controls the
   *                   selection, or <CODE>null</CODE> if the selection
   *                   should remain unchanged.
   */
  public MoveCommand(final GeometrySubject subject,
                     final GeometrySubject newState,
                     final SelectionOwner panel)
  {
    this(subject, newState, panel, null);
  }

  /**
   * Creates a new move command.
   * @param  subject   The geometry affected by this command.
   * @param  newState  A template geometry to specify the desired state of the
   *                   subject after execution of the command. It should be
   *                   of a type assignable to the subject, but <I>not</I>
   *                   be the same object.
   * @param  panel     The panel that contains the item and controls the
   *                   selection, or <CODE>null</CODE> if the selection
   *                   should remain unchanged.
   * @param  name      The description of the command.
   */
  public MoveCommand(final GeometrySubject subject,
                     final GeometrySubject newState,
                     final SelectionOwner panel,
                     final String name)
  {
    super(panel, name, panel != null);
    mSubject = subject;
    mUndoInfo = subject.createUndoInfo(newState, null);
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


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.command.Command
  public void execute()
  {
    mUndoInfo.redo(mSubject);
    if (mHasBeenExecuted) {
      updateSelection();
    } else {
      mHasBeenExecuted = true;
    }
  }

  public void undo()
  {
    mUndoInfo.undo(mSubject);
    updateSelection();
  }


  //#########################################################################
  //# Auxiliary Methods
  private void updateSelection()
  {
    if (getUpdatesSelection()) {
      final SelectionOwner panel = getPanel();
      final ProxySubject parent = (ProxySubject) mSubject.getParent();
      ProxySubject selected = parent;
      if(parent instanceof SimpleNodeSubject){
        final SimpleNodeSubject node = (SimpleNodeSubject)parent;
        if(node.getLabelGeometry() == mSubject){
          selected = mSubject;
        }
      }
      final List<ProxySubject> list = Collections.singletonList(selected);
      panel.replaceSelection(list);
      panel.scrollToVisible(list);
      panel.activate();
    }
  }


  //#########################################################################
  //# Data Members
  private final GeometrySubject mSubject;
  private final UndoInfo mUndoInfo;
  private boolean mHasBeenExecuted;

}
