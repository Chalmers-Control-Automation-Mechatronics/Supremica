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

import java.awt.Component;

import net.sourceforge.waters.gui.GraphEditorPanel;
import net.sourceforge.waters.gui.GraphEventPanel;
import net.sourceforge.waters.gui.observer.EditorChangedEvent;

import org.supremica.gui.ide.ComponentEditorPanel;
import org.supremica.gui.ide.EditorPanel;
import org.supremica.gui.ide.IDE;
import org.supremica.gui.ide.ModuleContainer;


/**
 * The class of all actions that can be applied to a graph in the editor
 * panel. This is a more convenient subclass of {@link WatersAction} for
 * actions that expect the presence of an editor panel, a module, and an
 * open graph. In addition to {@link WatersAction}, it provides access to
 * the graph panel, and an enablement condition that requires the presence
 * of a graph.
 *
 * @author Robi Malik
 */

public abstract class WatersGraphAction
  extends WatersAction
{

  //#########################################################################
  //# Constructors
  protected WatersGraphAction(final IDE ide)
  {
    super(ide);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.observer.Observer
  /**
   * Callback for state changes of the IDE. This default implementation
   * only updates the status if the user has switched panels, enabling
   * the action if a graph editor is active.
   */
  @Override
  public void update(final EditorChangedEvent event)
  {
    switch (event.getKind()) {
    case CONTAINER_SWITCH:
    case MAINPANEL_SWITCH:
    case SUBPANEL_SWITCH:
      final ComponentEditorPanel gui = getActiveComponentEditorPanel();
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
   * Retrieves a references to the active graph editor panel.
   * @return  An editor window interface to access the active graph editor
   *          panel, or <CODE>null</CODE> if no graph editor panel is\
   *          currently active.
   */
  ComponentEditorPanel getActiveComponentEditorPanel()
  {
    final ModuleContainer container = getActiveModuleContainer();
    if (container == null) {
      return null;
    }
    final Component panel = container.getActivePanel();
    if (panel instanceof EditorPanel) {
      final EditorPanel epanel = (EditorPanel) panel;
      return epanel.getActiveComponentEditorPanel();
    } else {
      return null;
    }
  }

  /**
   * Retrieves the controlled surface of the active graph editor panel.
   * @return  The currently active controlled surface, or <CODE>null</CODE>
   *          if no graph is being edited.
   */
  GraphEditorPanel getActiveGraphEditorPanel()
  {
    final ComponentEditorPanel gui = getActiveComponentEditorPanel();
    return gui == null ? null : gui.getGraphEditorPanel();
  }

  /**
   * Retrieves the event panel of the active graph editor panel.
   * @return  The currently active event panel, or <CODE>null</CODE>
   *          if no graph is being edited.
   */
  GraphEventPanel getActiveGraphEventPanel()
  {
    final ComponentEditorPanel gui = getActiveComponentEditorPanel();
    return gui == null ? null : gui.getEventPanel();
  }


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
