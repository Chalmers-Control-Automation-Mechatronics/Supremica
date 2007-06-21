//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: net.sourceforge.waters.gui.actions
//# CLASS:   IDEAction
//###########################################################################
//# $Id: IDEAction.java,v 1.1 2007-06-21 20:56:53 robi Exp $
//###########################################################################


package net.sourceforge.waters.gui.actions;

import javax.swing.AbstractAction;

import net.sourceforge.waters.gui.command.UndoInterface;
import net.sourceforge.waters.gui.observer.Observer;
import net.sourceforge.waters.gui.observer.EditorChangedEvent;
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
 * WatersActionManager#getClass(Class) getClass()} method. There is no
 * other way to create or obtain action objects.</DD>
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
 * This class has been rewritten to replace the old actions in package
 * <CODE>org.supremica.gui.ide.actions</CODE>, which cannot support
 * enablement and disablement.
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
    setEnabled(false);
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


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.observer.Observer
  /**
   * Callback for state changes of the IDE. This method gets called
   * automatically when the user changes the IDE state, switches panels,
   * load new modules, etc. In response, it should check the new state
   * of the IDE and enable or disable the action accordingly. The default
   * implementation only calls {@link #updateEnabledStatus()} if the user
   * has switched main panels.
   * @param  event  An event object providing some details on what exactly
   *                has changed.
   */
  public void update(final EditorChangedEvent event)
  {
    switch (event.getKind()) {
    case MAINPANEL_SWITCH:
      updateEnabledStatus();
      break;
    default:
      break;
    }
  }


  //#########################################################################
  //# Enabling and Disabling
  /**
   * Enables or disables this action.
   * This method is called by the {@link #update(EditorChangedEvent)
   * update()} method when a significant change in the IDE has been
   * detected. It has to reevaluate the state and enable or * disable this
   * action accordingly using the {@link *
   * javax.swing.Action@setEnabled(boolean) setEnabled()} method. Some more
   * specific subclasses may provide a default implementation of this
   * method.
   */
  public abstract boolean updateEnabledStatus();


  //#########################################################################
  //# Data Members
  private final IDE mIDE;

}
