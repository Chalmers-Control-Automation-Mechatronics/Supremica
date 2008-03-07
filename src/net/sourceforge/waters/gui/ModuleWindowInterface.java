//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   ModuleWindowInterface
//###########################################################################
//# $Id: ModuleWindowInterface.java,v 1.13 2008-03-07 04:11:02 robi Exp $
//###########################################################################

package net.sourceforge.waters.gui;

import java.awt.Frame;
import java.awt.event.ActionListener;

import net.sourceforge.waters.gui.command.UndoInterface;
import net.sourceforge.waters.gui.renderer.GeometryAbsentException;
import net.sourceforge.waters.gui.transfer.SelectionOwner;
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
   * Gets the undo manager used to pass commands to this GUI.
   */
  public UndoInterface getUndoInterface();
    
  /**
   * Gets the module edited by the GUI represented by this inteface.
   */
  public ModuleSubject getModuleSubject();
    
  /**
   * Gets the module context to be used for name-based lookups in the
   * module.
   */
  public ModuleContext getModuleContext();

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
   * Gets the components panel for the module edited by this GUI.
   */
  public SelectionOwner getComponentsPanel();

  /**
   * Gets the events panel for the module edited by this GUI.
   */
  public SelectionOwner getEventsPanel();

  /**
   * Shows the list of components of the module so the user can edit it.
   */
  public void showComponents();

  /**
   * Shows the list of events of the module so the user can edit it.
   */
  public void showEvents();

  /**
   * Opens a graph editor for the given component.
   */
  public EditorWindowInterface showEditor(SimpleComponentSubject comp)
    throws GeometryAbsentException;
 
  /**
   * Gets the graph editor for the given component.
   * @return The editor window interface to edit the given component,
   *         or <CODE>null</CODE> if the component has not yet been
   *         edited.
   */
  public EditorWindowInterface getEditorWindowInterface
    (SimpleComponentSubject comp);
    
  /**
   * Gets the currently graph editor that is currently displayed,
   * or <CODE>null</CODE>. The returned editor does not necessarily
   * own the keyboard focus.
   */
  public EditorWindowInterface getActiveEditorWindowInterface();
    
  /**
   * Shows the comment editor panel for the current module.
   */
  public void showComment();

  /**
   * Shows the given panel in the module window, so the user can edit
   * its contents.
   * @param  panel   The panel to be activated. Should be a panel this
   *                 GUI has control over.
   */
  public void showPanel(SelectionOwner panel);

}
