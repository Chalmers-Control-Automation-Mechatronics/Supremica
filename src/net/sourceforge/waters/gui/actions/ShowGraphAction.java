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

package net.sourceforge.waters.gui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;

import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.gui.util.IconAndFontLoader;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;

import org.supremica.gui.ide.IDE;
import org.supremica.gui.ide.MainPanel;


/**
 * The action to edit a graph in a module.
 * This action displays the graph editor for the automaton currently
 * selected in the module components list.
 *
 * @author Robi Malik
 */

public class ShowGraphAction
  extends WatersAction
{

  //#########################################################################
  //# Constructors
  ShowGraphAction(final IDE ide)
  {
    this(ide, null);
  }

  ShowGraphAction(final IDE ide, final Proxy arg)
  {
    super(ide);
    mActionArgument = arg;
    putValue(Action.NAME, "Edit Automaton");
    putValue(Action.MNEMONIC_KEY, KeyEvent.VK_E);
    putValue(Action.SMALL_ICON, IconAndFontLoader.ICON_EDIT_AUTOMATON);
    updateEnabledStatus();
  }


  //#########################################################################
  //# Interface java.awt.event.ActionListener
  @Override
  public void actionPerformed(final ActionEvent event)
  {
    final MainPanel panel = getActiveMainPanel();
    final Proxy proxy = getActionArgument();
    panel.showGraph(proxy);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.observer.Observer
  @Override
  public void update(final EditorChangedEvent event)
  {
    if (event.getKind() == EditorChangedEvent.Kind.SELECTION_CHANGED) {
      updateEnabledStatus();
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private void updateEnabledStatus()
  {
    final Proxy proxy = getActionArgument();
    final boolean enabled;
    if (proxy == null) {
      enabled = false;
    } else if (proxy instanceof SimpleComponentSubject ||
               proxy instanceof AutomatonProxy) {
      enabled = true;
    } else {
      enabled = false;
    }
    setEnabled(enabled);
    if (enabled) {
      putValue(Action.SHORT_DESCRIPTION,
               "Show and edit the graph for this automaton");
    } else {
      putValue(Action.SHORT_DESCRIPTION,
               "Show and edit the graph for the selected automaton");
    }
  }

  private Proxy getActionArgument()
  {
    if (mActionArgument != null) {
      return mActionArgument;
    } else {
      return getSelectionAnchor();
    }
  }


  //#########################################################################
  //# Data Members
  private final Proxy mActionArgument;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
