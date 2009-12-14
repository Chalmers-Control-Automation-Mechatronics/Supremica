//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: net.sourceforge.waters.gui.actions
//# CLASS:   IDEAction
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import net.sourceforge.waters.gui.observer.Observer;
import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.gui.transfer.FocusTracker;
import net.sourceforge.waters.gui.transfer.SelectionOwner;
import net.sourceforge.waters.model.base.Proxy;
import org.supremica.gui.ide.IDE;


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
 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionListener)
 * actionPerformed()} method is automatically called when the user invokes
 * the action.</DD>
 *
 * <DT>Enablement.</DT> <DD>All actions implement the {@link Observer}
 * interface and as such are notified of changes to the IDE's state. They
 * should check their enablement conditions in the {@link
 * Observer#update(EditorChangedEvent) update()} method, and enable or
 * disable the action accordingly using the {@link
 * javax.swing.Action@setEnabled(boolean) setEnabled()} method. At the same
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
