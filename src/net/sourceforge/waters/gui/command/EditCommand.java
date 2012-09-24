//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters GUI
//# PACKAGE: net.sourceforge.waters.gui.command
//# CLASS:   EditCommand
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.gui.command;

import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.gui.language.ProxyNamer;
import net.sourceforge.waters.gui.transfer.SelectionOwner;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.subject.base.ProxySubject;
import net.sourceforge.waters.subject.base.UndoInfo;


/**
 * <P>A general command for modifying an object.</P>
 *
 * <P>This command is typically used after the user has edited some settings
 * using a dialog. It is passed a subject to be modified, and a dummy
 * object containing the new values. When executed, all changes are
 * applied at the same time, in an attempt to reduce the number of
 * change notifications fired.</P>
 *
 * <P>In addition, after all but the first execution, and after each undo,
 * the edited item is selected in its panel. This feature can be
 * disabled.</P>
 *
 * <P>The internal mechanism for the assignment is the {@link
 * ProxySubject#createUndoInfo(ProxySubject) createUndoInfo} method, which
 * supports uniform assignments between subjects.</P>
 *
 * @author Robi Malik
 */

public class EditCommand
  extends AbstractEditCommand
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new edit command that does not affect the selection.
   * @param  subject   The subject affected by this command.
   * @param  newState  A template subject to specify the desired state of the
   *                   subject after execution of the command. It should be
   *                   of a type assignable to the subject, but <I>not</I>
   *                   be the same object.
   */
  public EditCommand(final ProxySubject subject,
                     final ProxySubject newState)
  {
    this(subject, newState, null);
  }

  /**
   * Creates a new edit command.
   * @param  subject   The subject affected by this command.
   * @param  newState  A template subject to specify the desired state of the
   *                   subject after execution of the command. It should be
   *                   of a type assignable to the subject, but <I>not</I>
   *                   be the same object.
   * @param  panel     The panel that contains the item and controls the
   *                   selection, or <CODE>null</CODE> if the selection
   *                   should remain unchanged.
   */
  public EditCommand(final ProxySubject subject,
                     final ProxySubject newState,
                     final SelectionOwner panel)
  {
    this(subject, newState, panel, null);
  }

  /**
   * Creates a new edit command.
   * @param  subject   The subject affected by this command.
   * @param  newState  A template subject to specify the desired state of the
   *                   subject after execution of the command. It should be
   *                   of a type assignable to the subject, but <I>not</I>
   *                   be the same object.
   * @param  panel     The panel that contains the item and controls the
   *                   selection, or <CODE>null</CODE> if the selection
   *                   should remain unchanged.
   * @param  name      The description of the command.
   */
  public EditCommand(final ProxySubject subject,
                     final ProxySubject newState,
                     final SelectionOwner panel,
                     final String name)
  {
    this(subject, subject.createUndoInfo(newState, null), panel, name);
  }

  /**
   * Creates a new edit command.
   * @param  subject   The subject affected by this command.
   * @param  info      Undo information containing assignment instructions.
   * @param  panel     The panel that contains the item and controls the
   *                   selection, or <CODE>null</CODE> if the selection
   *                   should remain unchanged.
   * @param  name      The description of the command.
   */
  public EditCommand(final ProxySubject subject,
                     final UndoInfo info,
                     final SelectionOwner panel,
                     final String name)
  {
    super(panel, name, panel != null);
    mSubject = subject;
    mUndoInfo = info;
    if (name == null) {
      final String newname = ProxyNamer.getItemClassName(subject) + " Edit";
      setName(newname);
    }
    mHasBeenExecuted = false;
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
      final Proxy ancestor = panel.getSelectableAncestor(mSubject);
      final List<Proxy> list = Collections.singletonList(ancestor);
      panel.replaceSelection(list);
      panel.scrollToVisible(list);
      panel.activate();
    }
  }


  //#########################################################################
  //# Data Members
  private final ProxySubject mSubject;
  private final UndoInfo mUndoInfo;
  private boolean mHasBeenExecuted;

}
