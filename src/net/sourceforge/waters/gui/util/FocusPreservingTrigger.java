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


import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;


/**
 * @author Robi Malik
 */

public class FocusPreservingTrigger
  extends AbstractAction
{

  //#########################################################################
  //# Static Access
  public static void addAccelerator(final AbstractButton button,
                                    final char accelerator)
  {
    final int code = KeyEvent.getExtendedKeyCodeForChar(accelerator);
    final KeyStroke stroke = KeyStroke.getKeyStroke(code, KeyEvent.ALT_MASK);
    final Action action = new FocusPreservingTrigger(button, stroke);
    final String text = button.getText();
    final int mnemonicIndex =
      text == null ? -1 : text.toLowerCase().indexOf(accelerator);
    button.setDisplayedMnemonicIndex(mnemonicIndex);
    button.getActionMap().put(NAME, action);
    final InputMap map = button.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
    map.put(stroke, NAME);
  }


  //#########################################################################
  //# Constructor
  private FocusPreservingTrigger(final AbstractButton button,
                                 final KeyStroke accelerator)
  {
    mButton = button;
    putValue(Action.ACCELERATOR_KEY, accelerator);
  }


  //#########################################################################
  //# Interface java.awt.event.ActionListener
  @Override
  public void actionPerformed(final ActionEvent event)
  {
    mButton.doClick();
  }


  //#########################################################################
  //# Data Members
  private final AbstractButton mButton;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 4648821223332844567L;

  private static final String NAME = "triggerByKey";

}
