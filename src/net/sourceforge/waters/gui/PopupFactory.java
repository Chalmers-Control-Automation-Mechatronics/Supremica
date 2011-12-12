//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   PopupFactory
//###########################################################################
//# $Id$
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

import org.supremica.util.VPopupMenu;


public abstract class PopupFactory
{

  //#########################################################################
  //# Constructor
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
      if(invoker instanceof SelectionOwner && proxy != null && !(proxy instanceof ModuleProxy)){
        final SelectionOwner selection = (SelectionOwner)invoker;
        if(!selection.isSelected(proxy)){
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
    final JPopupMenu popup = mPopupMenu = new VPopupMenu();
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
