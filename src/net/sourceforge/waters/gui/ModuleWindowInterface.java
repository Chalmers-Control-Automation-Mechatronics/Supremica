//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   ModuleWindowInterface
//###########################################################################
//# $Id: ModuleWindowInterface.java,v 1.2 2006-08-09 02:53:58 robi Exp $
//###########################################################################

package net.sourceforge.waters.gui;

import java.awt.Frame;

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
{

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
  public EditorWindowInterface showEditor(SimpleComponentSubject comp);

}
