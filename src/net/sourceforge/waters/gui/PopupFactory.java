//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
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

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.List;

import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import net.sourceforge.waters.gui.actions.IDEAction;
import net.sourceforge.waters.gui.actions.WatersPopupActionManager;
import net.sourceforge.waters.gui.transfer.SelectionOwner;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.module.ModuleProxy;


/**
 * <P>Main entry point for creating pop-up menus.</P>
 *
 * <P>Any component using pop-up menus should use a subclass of this class
 * and override some of the methods:</P>
 * <UL>
 * <LI>{@link #addDefaultMenuItems()}</LI>
 * <LI>{@link #addItemSpecificMenuItems(Proxy) addItemSpecificMenuItems()}</LI>
 * <LI>{@link #addCommonMenuItems()}</LI>
 * </UL>
 * <P>Then it should register mouse listeners that call {@link
 * #maybeShowPopup(Component, MouseEvent, Proxy) maybeShowPopup()} to
 * display the popup menu.</P>
 *
 * <P>The actions for the popup menu are provided by an action manager
 * ({@link WatersPopupActionManager} or subclass). The action manager
 * can retrieve shared IDE actions or create specific context-sensitive
 * actions for an item under the cursor.</P>
 *
 * @author Robi Malik
 */

public abstract class PopupFactory
{

  //#########################################################################
  //# Constructor
  /**
   * Creates a new popup factory.
   * @param  master  The action manager that provides the action objects
   *                 used in the pop-up menu.
   */
  protected PopupFactory(final WatersPopupActionManager master)
  {
    mMaster = master;
    mPopupMenu = null;
  }


  //#########################################################################
  //# Invocation
  /**
   * Checks whether the given mouse event should trigger a popup menu,
   * and if so, attempts to display the popup menu.
   * @param  invoker A component trying to display the popup.
   * @param  event   A mouse event that is suspected to trigger a popup.
   * @param  proxy   The object under the mouse cursor when the popup is
   *                 invoked, or <CODE>null</CODE>.
   */
  public void maybeShowPopup(final Component invoker,
                             final MouseEvent event,
                             final Proxy proxy)
  {
    if (event.isPopupTrigger()) {
      if (invoker instanceof SelectionOwner &&
          proxy != null &&
          !(proxy instanceof ModuleProxy)){
        final SelectionOwner selection = (SelectionOwner)invoker;
        if (!selection.isSelected(proxy)) {
          final List<Proxy> items = Collections.singletonList(proxy);
          selection.replaceSelection(items);
        }
      }
      // Paranoia. These popups require the invoking component to own the
      // keyboard focus. If we do not have it, request it first. However,
      // the focus is not granted immediately, and maybe not at all ...
      if (invoker.isFocusOwner()) {
        showPopup(invoker, event, proxy);
      } else {
        invoker.requestFocusInWindow();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
              if (invoker.isFocusOwner()) {
                showPopup(invoker, event, proxy);
              }
            }
          });
      }
    }
  }

  /**
   * Displays a popup menu for the given component, with position determined
   * by the mouse event given.
   * @param  invoker The component displaying the popup.
   * @param  event   The mouse event that triggered the popup.
   * @param  proxy   The object under the mouse cursor when the popup is
   *                 invoked, or <CODE>null</CODE>.
   */
  public void showPopup(final Component invoker,
                        final MouseEvent event,
                        final Proxy proxy)
  {
    final JPopupMenu popup = createPopup(proxy, event);
    popup.show(invoker, event.getX(), event.getY());
  }

  /**
   * Creates a popup menu for the given item.
   * @param  proxy   The object under the mouse cursor when the popup is
   *                 invoked, or <CODE>null</CODE>.
   */
  public JPopupMenu createPopup(final Proxy proxy, final MouseEvent event)
  {
    final JPopupMenu popup = mPopupMenu = new JPopupMenu();
    mEvent = event;
    if (proxy == null) {
      addDefaultMenuItems();
    } else {
      addItemSpecificMenuItems(proxy);
    }
    addCommonMenuItems();
    mPopupMenu = null;
    return popup;
  }


  //#########################################################################
  //# Simple Access
  protected WatersPopupActionManager getMaster()
  {
    return mMaster;
  }

  protected JPopupMenu getPopup()
  {
    return mPopupMenu;
  }

  protected MouseEvent getEvent()
  {
    return mEvent;
  }


  //#########################################################################
  //# Shared Menu Items
  /**
   * Adds default menu items to the menu.
   * This method is called when creating a popup without any item under
   * the cursor. Menu items are inserted at the beginning of the menu,
   * instead of any item-specific actions, before the common menu items.
   */
  protected void addDefaultMenuItems()
  {
    final IDEAction delete = mMaster.getDeleteAction();
    mPopupMenu.add(delete);
  }

  /**
   * Adds menu items for a specific object to the menu.
   * This method is called when creating a popup without with the given
   * <CODE>proxy</CODE> under the cursor. Menu items are inserted at the
   * beginning of the menu, before the common menu items.
   * This method can be overridden by subclasses to provided context-specific
   * popup menus. The default implementation merely calls {@link
   * #addPropertiesAndDeleteMenuItems(Proxy) addPropertiesAndDeleteMenuItems()}.
   */
  protected void addItemSpecificMenuItems(final Proxy proxy)
  {
    addPropertiesAndDeleteMenuItems(proxy);
  }

  /**
   * Adds menu Properties and Delete menu items for the given
   * <CODE>proxy</CODE>, provided that these actions are enabled.
   * This is method is called by the default implementation of
   * {@link #addItemSpecificMenuItems(Proxy) addItemSpecificMenuItems()}.
   */
  protected void addPropertiesAndDeleteMenuItems(final Proxy proxy)
  {
    final IDEAction props = mMaster.getPropertiesAction(proxy);
    if (props.isEnabled()) {
      mPopupMenu.add(props);
    }
    final IDEAction delete = mMaster.getDeleteAction(proxy);
    if (delete.isEnabled()) {
      mPopupMenu.add(delete);
    }
  }

  /**
   * Adds common menu items to the menu.
   * This method is called after the insertion of the item-specific (or
   * default) menu items has been completed. It adds menu items that are
   * always available in the panel. The default creates the generic
   * 'cut', 'copy', 'paste', 'select all', and 'deselect all' menu buttons.
   */
  protected void addCommonMenuItems()
  {
    if (mPopupMenu.getComponentCount() > 0) {
      mPopupMenu.addSeparator();
    }
    final IDEAction cut = mMaster.getCutAction();
    mPopupMenu.add(cut);
    final IDEAction copy = mMaster.getCopyAction();
    mPopupMenu.add(copy);
    final IDEAction paste = mMaster.getPasteAction();
    mPopupMenu.add(paste);
    mPopupMenu.addSeparator();
    final IDEAction select = mMaster.getSelectAllAction();
    mPopupMenu.add(select);
    final IDEAction deselect = mMaster.getDeselectAllAction();
    mPopupMenu.add(deselect);
  }


  //#######################################################################
  //# Data Members
  private final WatersPopupActionManager mMaster;

  private MouseEvent mEvent;
  private JPopupMenu mPopupMenu;

}
