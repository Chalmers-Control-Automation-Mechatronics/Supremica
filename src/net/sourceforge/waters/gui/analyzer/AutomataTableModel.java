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

import java.util.List;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.table.AbstractTableModel;

import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;


class AutomataTableModel
  extends AbstractTableModel
{

  //#########################################################################
  //# Constructor
  AutomataTableModel(final ProductDESProxy DESList)
  {
    super();
    mDESList = DESList;
    //desktop.attach(this);
    //mComparator = new AutomatonTableComparator();
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
    //final WatersAnalyzer sim = getWatersAnalyzer();
    //final String finder = (String)mRawData.get(index).get(1);
    @SuppressWarnings("unused")
    final Set<AutomatonProxy> APSet = mDESList.getAutomata();
    //final AutomatonProxy AP = A
    return null;
  }

  int getIndex(final AutomatonProxy aut)
  {
    for (int looper = 0; looper < this.getRowCount(); looper++) {
      if (((String) mRawData.get(looper).get(1)).compareTo(aut.getName()) == 0) {
        return looper;
      }
    }
    return -1;
  }

  //AutomatonTableComparator getComparator()
  //{
  //  return mComparator;
  //}


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
    final Set<AutomatonProxy> APSet = mDESList.getAutomata();
    return APSet.size();
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
    final Set<AutomatonProxy> APSet = mDESList.getAutomata();
    final Object APArray[] = APSet.toArray();
    try {
      return APArray[row];
    } catch (final NullPointerException np) {
      return null;
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
  private List<List<Object>> mRawData;
  private final ProductDESProxy mDESList;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 470265246231865258L;

}
