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

package net.sourceforge.waters.gui.analyzer;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.table.AbstractTableModel;

import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.gui.observer.Observer;
import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.base.EventKind;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.AutomatonTools;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.expr.ParseException;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;

import org.supremica.gui.ide.ModuleContainer;


public class AutomataTableModel extends AbstractTableModel implements Observer
{

  //#########################################################################
  //# Constructor
  AutomataTableModel(final ModuleContainer container)
  {
    mModuleContainer = container;
    mModuleContainer.attach(this);
    updateCompiledDES();
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

  List<AutomatonProxy> getAutomataList()
  {
    return mAutomataList;
  }

  int getIndex(final AutomatonProxy aut)
  {
    return mAutomataList.indexOf(aut);
  }

  Map<String,EventProxy> getEventMap()
  {
    return mEventMap;
  }

  void deleteRows(final TIntArrayList deleteIndexList)
  {
    deleteIndexList.sort();
    int start = -1;
    int end = -1;
    for (int p = deleteIndexList.size() - 1; p >= 0; p--) {
      final int deleteIndex = deleteIndexList.get(p);
      if (start < 0) {
        start = end = deleteIndex;
      } else if (deleteIndex == start - 1) {
        start = deleteIndex;
      } else {
        deleteRows(start, end);
        start = end = deleteIndex;
      }
    }
    if (start >= 0) {
      deleteRows(start, end);
    }
  }

  private void deleteRows(final int start, final int end)
  {
    for (int i = end; i >= start; i--) {
      final AutomatonProxy aut = mAutomataList.remove(i);
      mAutomataNames.remove(aut.getName());
      removeFromDisplayMap(aut);
    }
    fireTableRowsDeleted(start, end);
  }

  void insertRows(final Collection<? extends AutomatonProxy> insertList)
  {
    final int count = mAutomataList.size();
    mAutomataList.addAll(insertList);
    for (final AutomatonProxy aut : insertList) {
      mAutomataNames.add(aut.getName());
    }
    fireTableRowsInserted(count, count + (insertList.size()-1));
  }

  public void insertRow(final AutomatonProxy aut)
  {
    final List<AutomatonProxy> list = Collections.singletonList(aut);
    insertRows(list);
  }

  public String getUniqueAutomatonName(final String name)
  {
    return AutomatonTools.getUniqueName(name, mAutomataNames);
  }

  public void checkNewAutomatonName(final IdentifierProxy ident)
    throws ParseException
  {
    final String name = ident.toString();
    if (mAutomataNames.contains(name)) {
      final StringBuilder buffer = new StringBuilder("Name '");
      buffer.append(name);
      buffer.append("' is already taken by an automaton!");
      final String msg = buffer.toString();
      throw new ParseException(msg, 0);
    }
  }

  public void replaceAutomaton(final AutomatonProxy oldAut,
                               final AutomatonProxy newAut)
  {
    final int i = mAutomataList.indexOf(oldAut);
    if (i >= 0) {
      mAutomataList.set(i, newAut);
      final String oldName = oldAut.getName();
      final String newName = newAut.getName();
      if (!oldName.equals(newName)) {
        mAutomataNames.remove(oldName);
        mAutomataNames.add(newName);
      }
      fireTableRowsUpdated(i, i);
      final SimpleComponentSubject comp = mDisplayMap.get(oldAut);
      if (comp != null) {
        mDisplayMap.remove(oldAut);
        mDisplayMap.put(newAut, comp);
      }
    } else {
      throw new IllegalArgumentException(ProxyTools.getShortClassName(this) +
                                         " does not contain the automaton '" +
                                         oldAut.getName() + "'!");
    }
  }

  boolean containsDisplayMap(final AutomatonProxy aut)
  {
    return mDisplayMap.containsKey(aut);
  }

  SimpleComponentSubject getCompFromDisplayMap(final AutomatonProxy aut)
  {
    return mDisplayMap.get(aut);
  }

  void addToDisplayMap(final AutomatonProxy aut,
                       final SimpleComponentSubject comp)
  {
    mDisplayMap.put(aut, comp);
  }

  void removeFromDisplayMap(final AutomatonProxy aut)
  {
    mDisplayMap.remove(aut);
  }

  void close()
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
    case 3:
    case 4:
      return Integer.class;
    default:
      throw new ArrayIndexOutOfBoundsException
        ("Bad column number for automata table model!");
    }
  }

  @Override
  public Object getValueAt(final int row, final int col)
  {
    final AutomatonProxy aut = mAutomataList.get(row);
    switch (col) {
    case 0:
      return aut.getKind();
    case 1:
      return aut.getName();
    case 2:
      return aut.getStates().size();
    case 3:
      int count = 0;
      for (final EventProxy event : aut.getEvents()) {
        if (event.getKind() != EventKind.PROPOSITION) {
          count++;
        }
      }
      return count;
    case 4:
      return aut.getTransitions().size();
    default:
      throw new ArrayIndexOutOfBoundsException
        ("Bad column number for automata table model!");
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
      throw new ArrayIndexOutOfBoundsException
        ("Bad column number for automata table model!");
    }
  }


  //#########################################################################
  //# Updating
  @Override
  public void update(final EditorChangedEvent event)
  {
    if (event.getKind() == EditorChangedEvent.Kind.MAINPANEL_SWITCH &&
        mModuleContainer.getActivePanel() instanceof WatersAnalyzerPanel) {
      updateCompiledDES();
    }
  }

  private void updateCompiledDES()
  {
    final ProductDESProxy newDES = mModuleContainer.getCompiledDES();
    if (newDES != mCompiledDES || mAutomataList == null) {
      mCompiledDES = newDES;
      if (newDES != null) {
        mAutomataList = new ArrayList<>(mCompiledDES.getAutomata());
        Collections.sort(mAutomataList);
        mAutomataNames = new THashSet<>(mAutomataList.size());
        mEventMap = new HashMap<>(mCompiledDES.getEvents().size());
        for (final AutomatonProxy aut : mAutomataList) {
          mAutomataNames.add(aut.getName());
          for (final EventProxy event : aut.getEvents()) {
            final String name = event.getName();
            mEventMap.put(name, event);
          }
        }
      } else {
        mAutomataList = new ArrayList<>();
        mAutomataNames = Collections.emptySet();
        mEventMap = Collections.emptyMap();
      }
      mDisplayMap = new HashMap<>();
      fireTableDataChanged();
    }
  }


  //#########################################################################
  //# Data Members
  private final ModuleContainer mModuleContainer;

  private ProductDESProxy mCompiledDES;
  private List<AutomatonProxy> mAutomataList;
  private Set<String> mAutomataNames;
  private Map<String,EventProxy> mEventMap;
  private Map<AutomatonProxy,SimpleComponentSubject> mDisplayMap;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 470265246231865258L;

}
