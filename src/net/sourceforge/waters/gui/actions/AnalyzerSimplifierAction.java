//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
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

import javax.swing.Action;

import net.sourceforge.waters.gui.analyzer.AutomataTable;
import net.sourceforge.waters.gui.analyzer.SimplifierDialog;
import net.sourceforge.waters.gui.observer.EditorChangedEvent;

import org.supremica.gui.ide.IDE;


/**
 * The action to invoke the Simplifier dialog in the Waters analyser.
 *
 * @author Benjamin Wheeler
 */

public class AnalyzerSimplifierAction extends WatersAnalyzerAction
{
  //#########################################################################
  //# Constructor
  protected AnalyzerSimplifierAction(final IDE ide)
  {
    super(ide);
    putValue(Action.NAME, "Simplify ...");
    updateEnabledStatus();
  }


  //#########################################################################
  //# Interface java.awt.event.ActionListener
  @Override
  public void actionPerformed(final ActionEvent arg0)
  {
    final IDE ide = getIDE();
    if (ide != null) {
      new SimplifierDialog(getAnalyzerPanel());
    }
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
    final AutomataTable table = getAnalyzerTable();
    if (table == null || table.getSelectedRowCount() != 1) {
      setEnabled(false);
      putValue(Action.SHORT_DESCRIPTION,
               "Apply a simplification algorithm to an automaton");
    } else {
      setEnabled(true);
      putValue(Action.SHORT_DESCRIPTION,
               "Apply a simplification algorithm to the selected automaton");
    }
  }


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 636028154288275788L;

}
