//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   ModuleWindowInterface
//###########################################################################
//# $Id: ModuleWindowInterface.java,v 1.7 2006-10-17 23:31:07 flordal Exp $
//###########################################################################

package net.sourceforge.waters.gui;

import java.awt.Frame;
import java.awt.event.ActionListener;

import net.sourceforge.waters.gui.command.UndoInterface;
import net.sourceforge.waters.gui.renderer.GeometryAbsentException;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.xsd.base.EventKind;


/**
 * An interface representing the capabilities of a GUI component
 * that contains a module or part of it. It is passed to dialogs
 * so they can access the data associated with their parent
 * without needing to know the precise class of parent they are
 * invoked from.
 *
 * @author Robi Malik
 * @author Simon Ware
 */

public interface ModuleWindowInterface
  extends ActionListener
{

  /**
   * Gets the undo manager used to pass commands to this GUI.
   */
  public UndoInterface getUndoInterface();

  /**
   * Gets the module edited by the GUI represented by this inteface.
   */
  public ModuleSubject getModuleSubject();

  /**
   * Gets the shared expression parser used by this GUI.
   */
  public ExpressionParser getExpressionParser();

  /**
   * Tries to determine an event kind for the given identifier.
   * Given an event name, this method inspects the module associated with
   * this window to determine whether the name represents a controllable
   * event, and uncontrollable event, or a proposition. Depending on the
   * implementation, it may or may not return accurate type information.
   * @param  ident   The identifier representing the event name to be
   *                 checked.
   * @return The event kind that will be associated with the given
   *         identifier after compilation of the module, or <CODE>null</CODE>
   *         that the event kind cannot be determined.
   */
  public EventKind guessEventKind(IdentifierProxy ident);

  /**
   * Gets the root window of this GUI.
   * Dialogs will use this as their owner.
   */
  public Frame getRootWindow();

  /**
   * Opens a graph editor for the given component.
   */
  public EditorWindowInterface showEditor(SimpleComponentSubject comp)
    throws GeometryAbsentException;

  /**
   * Shows the comment editor panel for the current module.
   */
  public void showComment();
}
