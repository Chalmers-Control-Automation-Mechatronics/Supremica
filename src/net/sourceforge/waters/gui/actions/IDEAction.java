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
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import net.sourceforge.waters.gui.ModuleContext;
import net.sourceforge.waters.gui.ModuleWindowInterface;
import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.gui.observer.Observer;
import net.sourceforge.waters.gui.transfer.FocusTracker;
import net.sourceforge.waters.gui.transfer.SelectionOwner;
import net.sourceforge.waters.model.base.Proxy;

import org.supremica.gui.ide.DocumentContainer;
import org.supremica.gui.ide.EditorPanel;
import org.supremica.gui.ide.IDE;
import org.supremica.gui.ide.ModuleContainer;


/**
 * <P>A common base class for all actions in the IDE.</P>
 *
 * <P>This is an implementation of Swing's SWING {@link Action} interface,
 * with some support to integrate into the Waters/Supremica IDE.</P>
 *
 * <DL>
 * <DT>Creation.</DT> <DD>All actions are created by the {@link
 * WatersActionManager}, and can be retrieved through its {@link
 * WatersActionManager#getAction(Class) getAction()} method.</DD>
 *
 * <DT>Invocation.</DT> <DD>All actions implement the {@link
 * java.awt.event.ActionListener} interface. The {@link
 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
 * actionPerformed()} method is automatically called when the user invokes
 * the action.</DD>
 *
 * <DT>Enablement.</DT> <DD>All actions implement the {@link Observer}
 * interface and as such are notified of changes to the IDE's state by the
 * {@link WatersActionManager}. They should check their enablement conditions
 * in the {@link Observer#update(EditorChangedEvent) update()} method, and
 * enable or disable the action accordingly using the {@link
 * javax.swing.Action#setEnabled(boolean) setEnabled()} method. At the same
 * time, other attributes such as the description can also be changed.</DD>
 *
 * <DT>Access to the IDE.</DT> <DD>All actions store a reference to the
 * application which can be retrieved by the {@link #getIDE()} method.
 * This is the only way how the environment should be accessed. Subclasses
 * may use it to provide more convenient access.</DD>
 * </DL>
 *
 * <P>This class has been rewritten to replace the old actions in package
 * <CODE>org.supremica.gui.ide.actions</CODE>, which cannot support
 * enablement and disablement.</P>
 *
 * @author Robi Malik
 */

public abstract class IDEAction
  extends AbstractAction
  implements Observer
{

  //#########################################################################
  //# Constructors
  protected IDEAction(final IDE ide)
  {
    mIDE = ide;
    setEnabled(true);
  }


  //#########################################################################
  //# Accessing the IDE
  /**
   * Retrieves a reference to the Waters/Supremica application that owns
   * this action. All information about the application state needs to
   * be retrieved through this reference.
   */
  public IDE getIDE()
  {
    return mIDE;
  }

  /**
   * Gets the focus tracker of the IDE.
   */
  public FocusTracker getFocusTracker()
  {
    return mIDE.getFocusTracker();
  }

  /**
   * Gets the current selection owner.
   * This method returns the panel of the IDE that currently owns the
   * keyboard focus, if that panel implements the {@link SelectionOwner}
   * interface; <CODE>null</CODE> otherwise.
   */
  public SelectionOwner getCurrentSelectionOwner()
  {
    return getFocusTracker().getWatersSelectionOwner();
  }

  /**
   * Gets the currently focused object.
   * This method retrieves the selection owner from the IDE, and identifies
   * the item that was last clicked or selected in that component. It is
   * this item that should be the target of 'properties' or similar actions.
   * @return The currently focused object, or <CODE>null</CODE> if no
   *         focused object can be identified.
   */
  public Proxy getSelectionAnchor()
  {
    final SelectionOwner panel = getCurrentSelectionOwner();
    if (panel == null) {
      return null;
    } else {
      return panel.getSelectionAnchor();
    }
  }

  /**
   * Retrieves a references to the active module container.
   * @return  A module container of the current module,
   *          or <CODE>null</CODE> if no module container currently active.
   */
  ModuleContainer getActiveModuleContainer()
  {
    final IDE ide = getIDE();
    final DocumentContainer container = ide.getActiveDocumentContainer();
    if (container == null || !(container instanceof ModuleContainer)) {
      return null;
    } else {
      return (ModuleContainer) container;
    }
  }

  /**
   * Retrieves a references to the active editor panel.
   * @return  A module window interface to access the active editor panel,
   *          or <CODE>null</CODE> if no editor panel is currently active.
   */
  ModuleWindowInterface getActiveModuleWindowInterface()
  {
    final IDE ide = getIDE();
    final DocumentContainer container = ide.getActiveDocumentContainer();
    if (container == null || !(container instanceof ModuleContainer)) {
      return null;
    }
    final Component panel = container.getActivePanel();
    if (panel instanceof EditorPanel) {
      return (ModuleWindowInterface) panel;
    } else {
      return null;
    }
  }

  /**
   * Retrieves the module context for the currently edited module.
   * @return  The module context, or <CODE>null</CODE> if no module is being
   *          edited.
   */
  ModuleContext getActiveModuleContext()
  {
    final ModuleContainer container = getActiveModuleContainer();
    if (container == null) {
      return null;
    } else {
      return container.getModuleContext();
    }
  }


  //#########################################################################
  //# Invocation
  /**
   * Programmatically executes this action.
   * This method simply calls this action's
   * {@link #actionPerformed(ActionEvent) actionPerformed()}
   * method with an appropriate {@link ActionEvent}.
   * @param  source   The originator of the event. This parameter will be
   *                  used as the event source of the {@link ActionEvent}.
   *                  It should identify the button or panel from which the
   *                  action has been triggered.
   */
  public void execute(final Object source)
  {
    final String name = (String) getValue(Action.NAME);
    final ActionEvent event =
      new ActionEvent(source, ActionEvent.ACTION_PERFORMED, name);
    actionPerformed(event);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.observer.Observer
  /**
   * Callback for state changes of the IDE. This method gets called
   * automatically when the user changes the IDE state, switches panels,
   * load new modules, etc. In response, it should check the new state
   * of the IDE and enable or disable the action accordingly. This
   * default implementation does nothing, producing an action that is
   * always enabled.
   * @param  event  An event object providing some details on what exactly
   *                has changed.
   */
  @Override
  public void update(final EditorChangedEvent event)
  {
  }


  //#########################################################################
  //# Data Members
  private final IDE mIDE;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
