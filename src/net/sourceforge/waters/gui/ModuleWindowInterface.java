//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   ModuleWindowInterface
//###########################################################################
//# $Id: ModuleWindowInterface.java,v 1.5 2006-09-25 03:55:30 siw4 Exp $
//###########################################################################

package net.sourceforge.waters.gui;

import net.sourceforge.waters.gui.renderer.GeometryAbsentException;
import java.awt.Frame;
import java.awt.event.ActionListener;

import net.sourceforge.waters.gui.command.UndoInterface;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;
import net.sourceforge.waters.model.expr.ExpressionParser;


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
   * Gets the root window of this GUI.
   * Dialogs will use this as their owner.
   */
  public Frame getRootWindow();

  /**
   * Opens a graph editor for the given component.
   */
  public EditorWindowInterface showEditor(SimpleComponentSubject comp)
    throws GeometryAbsentException;

}
