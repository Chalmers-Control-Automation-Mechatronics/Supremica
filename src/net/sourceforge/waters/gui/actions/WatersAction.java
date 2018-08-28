//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2018 Robi Malik
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

package net.sourceforge.waters.gui.actions;

import net.sourceforge.waters.gui.ModuleContext;
import net.sourceforge.waters.gui.ModuleWindowInterface;
import net.sourceforge.waters.gui.command.UndoInterface;
import net.sourceforge.waters.gui.observer.EditorChangedEvent;

import org.supremica.gui.ide.DocumentContainer;
import org.supremica.gui.ide.IDE;
import org.supremica.gui.ide.MainPanel;
import org.supremica.gui.ide.ModuleContainer;


/**
 * The class of all actions that can be applied to a module in the editor
 * panel. This is a more convenient subclass of {@link IDEAction} for
 * actions that expect the presence of an editor panel and a module. It
 * provides some support for access to the editor panel. It also provides a
 * more specific enablement condition, implementing an action that is
 * enabled only if the Waters module editor is active.
 *
 * @author Robi Malik
 */

public abstract class WatersAction
  extends IDEAction
{

  //#########################################################################
  //# Constructors
  protected WatersAction(final IDE ide)
  {
    super(ide);
    setEnabled(false);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.observer.Observer
  /**
   * Callback for state changes of the IDE. This default implementation
   * only updates the status if the user has switched main panels, enabling
   * the action if the editor is active.
   */
  @Override
  public void update(final EditorChangedEvent event)
  {
    switch (event.getKind()) {
    case CONTAINER_SWITCH:
    case MAINPANEL_SWITCH:
      final ModuleWindowInterface gui = getActiveModuleWindowInterface();
      final boolean enabled = gui != null;
      setEnabled(enabled);
      break;
    default:
      break;
    }
  }


  //#########################################################################
  //# Accessing the IDE
  /**
   * Retrieves a references to the active module container if a Waters
   * panel is active. If the Supremica analyser is in use, it is assumed
   * assumed that no module is available and this method returns
   * <CODE>false</CODE>. This is used to disable actions from the Waters
   * framework (such Copy/Paste) in the Supremica panels that do not
   * support them.
   * @return  The current module container,
   *          or <CODE>null</CODE> if no module container is currently active
   *          or the Supremica analyser is active.
   */
  @Override
  ModuleContainer getActiveModuleContainer()
  {
    final IDE ide = getIDE();
    final DocumentContainer container = ide.getActiveDocumentContainer();
    if (container == null || !(container instanceof ModuleContainer)) {
      return null;
    }
    final MainPanel panel = container.getActivePanel();
    if (panel.isWatersPanel()) {
      return (ModuleContainer) container;
    } else {
      return null;
    }
  }

  /**
   * Retrieves a references to the current module context.
   * @return  A module context object providing access to the module
   *          being edited, or <CODE>null</CODE> if no module is currently
   *          available.
   */
  ModuleContext getModuleContext()
  {
    final ModuleContainer container = getActiveModuleContainer();
    if (container == null) {
      return null;
    } else {
      return container.getModuleContext();
    }
  }

  /**
   * Retrieves a references to undo interface for the currently edited module.
   * @return  An undo interface that can be used to send commands to the
   *          edited document, or <CODE>null</CODE> if no editor panel is
   *          currently active.
   */
  UndoInterface getActiveUndoInterface()
  {
    final IDE ide = getIDE();
    final DocumentContainer container = ide.getActiveDocumentContainer();
    if (container == null) {
      return null;
    } else {
      return container.getActiveUndoInterface();
    }
  }


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = -4921817198677668185L;

}
