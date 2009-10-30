//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: net.sourceforge.waters.gui.util
//# CLASS:   DialogCancelAction
//###########################################################################
//# $Id$
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