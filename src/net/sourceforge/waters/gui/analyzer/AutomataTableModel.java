//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2017 Robi Malik
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

package net.sourceforge.waters.gui.analyzer;

import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.table.AbstractTableModel;

import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;

import org.supremica.gui.ide.ModuleContainer;


class AutomataTableModel
  extends AbstractTableModel
{

  //#########################################################################
  //# Constructor
  AutomataTableModel(final ModuleContainer ModContainer)
  {
    super();
    mCompiledDES = ModContainer.getCompiledDES();
    if(mCompiledDES != null)
      mAutomataList = new ArrayList<>(mCompiledDES.getAutomata());
    else
      mAutomataList = new ArrayList<>();
    //desktop.attach(this);
  }


  //#########################################################################
  //# Simple Access
  /**
   * Gets the automaton represented by the index-th row of the sorted table.
   * @param  index   The index of the row of the table.
   * @return The automaton represented at that index.
   */
  AutomatonProxy getAutomaton(final int index)
  {
    return mAutomataList.get(index);
  }

  int getIndex(final AutomatonProxy aut)
  {
    for (int looper = 0; looper < this.getRowCount(); looper++) {
      if ((mAutomataList.get(looper).getName()).compareTo(aut.getName()) == 0) {
        return looper;
      }
    }
    return -1;

  }


  //#########################################################################
  //# Interface javax.swing.table.TableModel
  @Override
  public int getColumnCount()
  {
    return 5;
  }

  @Override
  public int getRowCount()
  {
    return mAutomataList.size();
  }

  @Override
  public Class<?> getColumnClass(final int column)
  {
    switch (column) {
    case 0:
      return Icon.class;
    case 1:
      return String.class;
    case 2:
      return String.class;
    case 3:
      return String.class;
    case 4:
      return String.class;
    default:
      throw new ArrayIndexOutOfBoundsException(
          "Bad column number for markings table model!");
    }
  }

  @Override
  public Object getValueAt(final int row, final int col)
  {
    final AutomatonProxy currentAutomaton = mAutomataList.get(row);
    switch(col) {
    case 0:
      return currentAutomaton.getKind();
    case 1:
      return currentAutomaton.getName();
    case 2:
      return currentAutomaton.getStates().size();
    case 3:
      return currentAutomaton.getEvents().size();
    case 4:
      return currentAutomaton.getTransitions().size();
    default:
      throw new ArrayIndexOutOfBoundsException(
          "Bad column number for markings table model!");
    }
  }

  @Override
  public String getColumnName(final int columnVal)
  {
    switch (columnVal) {
    case 0:
      return "";
    case 1:
      return "Automaton";
    case 2:
      return "States";
    case 3:
      return "Events";
    case 4:
      return "Transitions";
    default:
      return "Invalid";
    }
  }

  //#########################################################################
  //# Data Members
  private final ProductDESProxy mCompiledDES;
  private final List<AutomatonProxy> mAutomataList;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 470265246231865258L;

}
