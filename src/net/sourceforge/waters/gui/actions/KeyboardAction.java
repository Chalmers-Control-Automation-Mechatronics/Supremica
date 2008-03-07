//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: net.sourceforge.waters.gui.actions
//# CLASS:   KeyboardAction
//###########################################################################
//# $Id: KeyboardAction.java,v 1.1 2008-03-07 04:11:02 robi Exp $
//###########################################################################


package net.sourceforge.waters.gui.actions;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Action;


/**
 * A simple wrapper to provide actions that look like they are always
 * enabled. This is used for keyboard maps to ensure bindings are overidden
 * even if the associated action is not enabled.
 *
 * @author Robi Malik
 */

class KeyboardAction implements Action
{

  //#########################################################################
  //# Constructor
  KeyboardAction(final Action action)
  {
    mAction = action;
  }


  //#########################################################################
  //# Interface javax.swing.Action
  public void addPropertyChangeListener(final PropertyChangeListener listener)
  {
    mAction.addPropertyChangeListener(listener);
  }

  public Object getValue(final String key)
  {
    return mAction.getValue(key);
  }

  public boolean isEnabled()
  {
    return true;
  }

  public void putValue(final String key, final Object value)
  {
    mAction.putValue(key, value);
  }

  public void removePropertyChangeListener
    (final PropertyChangeListener listener)
  {
    mAction.removePropertyChangeListener(listener);
  }

  public void setEnabled(final boolean enabled)
  {
    mAction.setEnabled(enabled);
  }


  //#########################################################################
  //# Interface java.awt.event.ActionListener
  public void actionPerformed(final ActionEvent event)
  {
    if (mAction.isEnabled()) {
      mAction.actionPerformed(event);
    }
  }


  //#########################################################################
  //# Data Members
  private final Action mAction;

}
