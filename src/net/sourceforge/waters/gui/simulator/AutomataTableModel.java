//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
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

package net.sourceforge.waters.gui.simulator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import net.sourceforge.waters.gui.ModuleContext;
import net.sourceforge.waters.gui.util.IconAndFontLoader;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.subject.module.VariableComponentSubject;

import org.supremica.gui.ide.ModuleContainer;


class AutomataTableModel
  extends SimulationTableModel
  implements SimulationObserver, InternalFrameObserver
{

  //#########################################################################
  //# Constructor
  AutomataTableModel(final Simulation sim, final AutomatonDesktopPane desktop)
  {
    super(sim);
    getRawData();
    desktop.attach(this);
    mComparator = new AutomatonTableComparator();
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
    final Simulation sim = getSimulation();
    final String finder = (String)mRawData.get(index).get(1);
    return sim.getAutomatonFromName(finder);
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

  AutomatonTableComparator getComparator()
  {
    return mComparator;
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
    return getSimulation().getOrderedAutomata().size();
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
      return Icon.class;
    case 3:
      return ImageIcon.class;
    case 4:
      return String.class;
    default:
      throw new ArrayIndexOutOfBoundsException
        ("Bad column number for automata table model!");
    }
  }

  @Override
  public Object getValueAt(final int row, final int col)
  {
    return mRawData.get(row).get(col);
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
      return "Act";
    case 3:
      return "Mrk";
    case 4:
      return "State";
    default:
      throw new ArrayIndexOutOfBoundsException
        ("Bad column number for automata table model!");
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.simulator.SimulationObserver
  @Override
  public void simulationChanged(final SimulationChangeEvent event)
  {
    getRawData();
    fireTableDataChanged();
  }


  //#########################################################################
  //# Interface InternalFrameObserver
  @Override
  public void onFrameEvent(final InternalFrameEvent event)
  {
    // This should identify the cells that have changed and then fire
    // a more appropriate change event. This will only increase performance
    // by a marginal amount, however.
    getRawData();
    fireTableDataChanged();
  }


  //#########################################################################
  //# Sorting
  void addSortingMethod(final int column)
  {
    mComparator.addNewSortingMethod(column);
    getRawData();
    fireTableDataChanged();
  }


  //#########################################################################
  //# Auxiliary Methods
  private void getRawData()
  {
    final ModuleContainer container = getModuleContainer();
    if (container != null) {
      final List<List<Object>> output = new ArrayList<List<Object>>();
      final List<AutomatonProxy> automata =
        getSimulation().getOrderedAutomata();
      for (final AutomatonProxy aut : automata) {
        final List<Object> row = new ArrayList<Object>();
        if (container.getSourceInfoMap().get(aut).getSourceObject().getClass() ==
            VariableComponentSubject.class) {
          row.add(IconAndFontLoader.ICON_VARIABLE);
        } else {
          row.add(ModuleContext.getComponentKindIcon(aut.getKind()));
        }
        row.add(aut.getName());
        row.add(getSimulation().getAutomatonActivityIcon(aut));
        final StateProxy state = getSimulation().getCurrentState(aut);
        if (state != null) {
          row.add(getSimulation().getMarkingIcon(state, aut));
          row.add(state.getName());
        } else {
          row.add(null);
          row.add("");
        }
        output.add(row);
      }
      Collections.sort(output, mComparator);
      mRawData = output;
    } else {
      mRawData = new ArrayList<List<Object>>();
    }
  }


  //#########################################################################
  //# Data Members
  private List<List<Object>> mRawData;
  private final AutomatonTableComparator mComparator;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 470265246231865258L;

}
