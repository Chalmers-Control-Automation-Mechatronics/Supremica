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

package net.sourceforge.waters.gui.util;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;


/**
 * An action that closes a dialog window.
 * This action closes the dialog window that contains the component that
 * invoked it. It can be attached to the 'Cancel' button of dialogs,
 * and to the ESCAPE key press in dialogs.
 *
 * @author Robi Malik
 */

public class DialogCancelAction
  extends AbstractAction
{

  //#########################################################################
  //# Singleton Pattern
  public static Action getInstance()
  {
    return INSTANCE;
  }

  public static void register(final JDialog dialog)
  {
    final String name = (String) INSTANCE.getValue(Action.NAME);
    final KeyStroke key =
      (KeyStroke) INSTANCE.getValue(Action.ACCELERATOR_KEY);
    final JRootPane root = dialog.getRootPane();
    final InputMap imap = root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
    final ActionMap amap = root.getActionMap();
    imap.put(key, name);
    amap.put(name, INSTANCE);
  }


  //#########################################################################
  //# Constructor
  private DialogCancelAction()
  {
    final KeyStroke key = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
    putValue(Action.NAME, "Cancel");
    putValue(Action.SHORT_DESCRIPTION,
             "Close this window without saving any changes");
    putValue(Action.ACCELERATOR_KEY, key);
  }


  //#########################################################################
  //# Interface java.awt.event.ActionListener
  public void actionPerformed(final ActionEvent event)
  {
    final Component comp = (Component) event.getSource();
    final Window window = SwingUtilities.windowForComponent(comp);
    if (window instanceof Dialog) {
      window.dispose();
    }
  }


  //#########################################################################
  //# Data Members
  private static final Action INSTANCE = new DialogCancelAction();


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
