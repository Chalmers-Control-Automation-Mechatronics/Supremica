//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2018 Robi Malik
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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.table.AbstractTableModel;

import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.gui.observer.Observer;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;

import org.supremica.gui.ide.ModuleContainer;


class AutomataTableModel extends AbstractTableModel implements Observer
{

  //#########################################################################
  //# Constructor
  AutomataTableModel(final ModuleContainer ModContainer)
  {
    super();
    mModuleContainer = ModContainer;
    mModuleContainer.attach(this);
    mCompiledDES = ModContainer.getCompiledDES();
    updateCompiledDES();
    mDisplayMap = new HashMap<AutomatonProxy,SimpleComponentProxy>();
    if (mCompiledDES != null) {
      mAutomataList = new ArrayList<>(mCompiledDES.getAutomata());
    } else {
      mAutomataList = new ArrayList<>();
    }
  }

  //#########################################################################
  //# Simple Access
  /**
   * Gets the automaton represented by the index-th row of the sorted table.
   *
   * @param index
   *          The index of the row of the table.
   * @return The automaton represented at that index.
   */
  AutomatonProxy getAutomaton(final int index)
  {
    return mAutomataList.get(index);
  }

  List<AutomatonProxy> getAutomatonList()
  {
    return mAutomataList;
  }

  int getIndex(final AutomatonProxy aut)
  {
    return mAutomataList.indexOf(aut);
    /*
    for (int looper = 0; looper < this.getRowCount(); looper++) {
      if ((mAutomataList.get(looper).getName())
        .compareTo(aut.getName()) == 0) {
        return looper;
      }
    }
    return -1;
    */
  }

  Map<String,EventProxy> getEventMap()
  {
    mEventMap = new HashMap<String,EventProxy>();
    for (final AutomatonProxy aut : mAutomataList) {
      final Set<EventProxy> eventList = aut.getEvents();
      for (final EventProxy ep : eventList) {
        if (!mEventMap.containsKey(ep.getName()))
          mEventMap.put(ep.getName(), ep);
      }
    }
    return mEventMap;
  }

  public void deleteRows(final List<Integer> deleteList)
  {
    final Comparator<Integer> c = Collections.reverseOrder();
    Collections.sort(deleteList, c);

    int start = -1;
    int end = -1;
    for (final int i : deleteList) {
      final int position = i;
      if (start == -1) {
        start = end = position;
      } else if (position > (end + 1)) {
        for (int q = start; q >= end; q--) {
          removeFromDisplayMap(mAutomataList.get(q));
          mAutomataList.remove(q);
        }
        fireTableRowsDeleted(end, start);
        start = end = position;
      } else {
        end = position;
      }
    }
    for (int q = start; q >= end; q--) {
      removeFromDisplayMap(mAutomataList.get(q));
      mAutomataList.remove(q);
    }
    fireTableRowsDeleted(end, start);
  }

  public void insertRows(final List<AutomatonProxy> insertList)
  {
    final int count = mAutomataList.size()+1;
    for (final AutomatonProxy aut : insertList) {
      mAutomataList.add(aut);
    }
    fireTableRowsInserted(count, count+insertList.size());
  }

  public boolean containsAutomatonName(final String name) {
    for(final AutomatonProxy aut : mAutomataList)
      if(aut.getName().equals(name) == true)
        return true;
    return false;
  }

  public boolean containsDisplayMap(final AutomatonProxy aut) {
    return mDisplayMap.containsKey(aut);
  }

  public SimpleComponentProxy getCompFromDisplayMap(final AutomatonProxy aut) {
    return mDisplayMap.get(aut);
  }

  public void addToDisplayMap(final AutomatonProxy aut, final SimpleComponentProxy comp) {
    mDisplayMap.put(aut, comp);
  }

  public void removeFromDisplayMap(final AutomatonProxy aut) {
    mDisplayMap.remove(aut);
  }

  public void Close()
  {
    mModuleContainer.detach(this);
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
      return ComponentKind.class;
    case 1:
      return String.class;
    case 2:
      return Integer.class;
    case 3:
      return Integer.class;
    case 4:
      return Integer.class;
    default:
      throw new ArrayIndexOutOfBoundsException("Bad column number for markings table model!");
    }
  }

  @Override
  public Object getValueAt(final int row, final int col)
  {
    final AutomatonProxy currentAutomaton = mAutomataList.get(row);
    switch (col) {
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
      throw new ArrayIndexOutOfBoundsException("Bad column number for markings table model!");
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
      return "|Q|";
    case 3:
      return "|\u03a3|";
    case 4:
      return "|\u2192|";
    default:
      throw new ArrayIndexOutOfBoundsException("Bad column number for automata table model!");
    }
  }

  //#########################################################################
  //# Updating
  @Override
  public void update(final EditorChangedEvent event)
  {
    if (event.getKind() == EditorChangedEvent.Kind.MAINPANEL_SWITCH
        && mModuleContainer.getActivePanel() instanceof WatersAnalyzerPanel) {
      updateCompiledDES();
      mDisplayMap = new HashMap<AutomatonProxy,SimpleComponentProxy>();
    }
  }

  private void updateCompiledDES()
  {
    final ProductDESProxy newDES = mModuleContainer.getCompiledDES();
    if (newDES != mCompiledDES) {
      mCompiledDES = newDES;
      mAutomataList = new ArrayList<>(mCompiledDES.getAutomata());
      Collections.sort(mAutomataList);
      fireTableDataChanged();
    }
  }

  //#########################################################################
  //# Data Members
  private ProductDESProxy mCompiledDES;
  private List<AutomatonProxy> mAutomataList;
  private final ModuleContainer mModuleContainer;
  private Map<String,EventProxy> mEventMap;
  private Map<AutomatonProxy,SimpleComponentProxy> mDisplayMap;

  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 470265246231865258L;

}
