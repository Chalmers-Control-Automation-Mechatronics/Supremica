//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2020 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

package net.sourceforge.waters.gui;

import net.sourceforge.waters.gui.command.UndoInterface;
import net.sourceforge.waters.gui.renderer.GeometryAbsentException;
import net.sourceforge.waters.gui.transfer.SelectionOwner;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;

import org.supremica.gui.ide.ComponentEditorPanel;
import org.supremica.gui.ide.IDE;


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
  public IDE getRootWindow();

  /**
   * Gets the components panel for the module edited by this GUI.
   */
  public SelectionOwner getComponentsPanel();

  /**
   * Gets the events panel for the module edited by this GUI.
   */
  public SelectionOwner getEventsPanel();

  /**
   * Gets the alias panel for the module edited by this GUI.
   */
  public SelectionOwner getConstantAliasesPanel();

  /**
   * Gets the event alias panel for the module edited by this GUI.
   */
  public SelectionOwner getEventAliasesPanel();

  /**
   * Gets the instance panel for the module edited by this GUI.
   */
  public SelectionOwner getInstancePanel();

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
  public ComponentEditorPanel showEditor(SimpleComponentSubject comp)
    throws GeometryAbsentException;

  /**
   * Gets the graph editor for the given component.
   * @return The editor window interface to edit the given component,
   *         or <CODE>null</CODE> if the component has not yet been
   *         edited.
   */
  public ComponentEditorPanel getComponentEditorPanel
    (SimpleComponentSubject comp);

  /**
   * Gets the currently graph editor that is currently displayed,
   * or <CODE>null</CODE>. The returned editor does not necessarily
   * own the keyboard focus.
   */
  public ComponentEditorPanel getActiveComponentEditorPanel();

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
