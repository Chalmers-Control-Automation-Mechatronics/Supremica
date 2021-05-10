//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 1999-2021 Knut Akesson, Martin Fabian, Robi Malik
//###########################################################################
//# This file is part of Waters/Supremica IDE.
//# Waters/Supremica IDE is free software: you can redistribute it and/or
//# modify it under the terms of the GNU General Public License as published
//# by the Free Software Foundation, either version 2 of the License, or
//# (at your option) any later version.
//# Waters/Supremica IDE is distributed in the hope that it will be useful,
//# but WITHOUT ANY WARRANTY; without even the implied warranty of
//# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
//# Public License for more details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters/Supremica IDE statically or dynamically with other modules
//# is making a combined work based on Waters/Supremica IDE. Thus, the terms
//# and conditions of the GNU General Public License cover the whole
//# combination.
//# In addition, as a special exception, the copyright holders of
//# Waters/Supremica IDE give you permission to combine Waters/Supremica IDE
//# with code included in the standard release of Supremica under the
//# Supremica Software License Agreement (or modified versions of such code,
//# with unchanged license). You may copy and distribute such a system
//# following the terms of the GNU GPL for Waters/Supremica IDE and the
//# licenses of the other code concerned.
//# Note that people who make modified versions of Waters/Supremica IDE are
//# not obligated to grant this special exception for their modified versions;
//# it is their choice whether to do so. The GNU General Public License gives
//# permission to release a modified version without this exception; this
//# exception also makes it possible to release a modified version which
//# carries forward this exception.
//###########################################################################

package org.supremica.gui;

import java.util.Iterator;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.automata.Automaton;
import org.supremica.automata.AutomatonType;


class TypeCellEditor
	implements CellEditorListener
{
	private final JTable theTable;
	private final TableSorter theTableSorter;
	private final JComboBox<AutomatonType> automatonTypeCombo;
	private final VisualProjectContainer theVisualProjectContainer;
	private static Logger logger = LogManager.getLogger(TypeCellEditor.class);

	public TypeCellEditor(final JTable theTable, final TableSorter theTableSorter, final VisualProjectContainer theVisualProjectContainer)
	{
		this.theTable = theTable;
		this.theVisualProjectContainer = theVisualProjectContainer;
		this.theTableSorter = theTableSorter;
		automatonTypeCombo = new JComboBox<AutomatonType>();

		final Iterator<AutomatonType> typeIt = AutomatonType.iterator();

		while (typeIt.hasNext())
		{
			automatonTypeCombo.addItem(typeIt.next());
		}

		final TableColumnModel columnModel = theTable.getColumnModel();
		final TableColumn typeColumn = columnModel.getColumn(Supremica.TABLE_TYPE_COLUMN);
		final DefaultCellEditor cellEditor = new DefaultCellEditor(automatonTypeCombo);

		cellEditor.setClickCountToStart(2);
		typeColumn.setCellEditor(cellEditor);
		cellEditor.addCellEditorListener(this);
	}

	@Override
  public void editingCanceled(final ChangeEvent e) {}

	@Override
  public void editingStopped(final ChangeEvent e)
	{
		if (automatonTypeCombo.getSelectedIndex() >= 0)
		{
			final AutomatonType selectedValue = (AutomatonType) automatonTypeCombo.getSelectedItem();

			if (selectedValue != null)
			{
				final int selectedRow = theTable.getSelectedRow();
				final int orgRow = theTableSorter.getOriginalRowIndex(selectedRow);

				if (selectedRow >= 0)
				{
					Automaton currAutomaton = null;

					try
					{
						currAutomaton = theVisualProjectContainer.getActiveProject().getAutomatonAt(orgRow);
					}
					catch (final Exception ex)
					{
						logger.error("Could not find automaton at row " + orgRow);
						logger.debug(ex.getStackTrace());

						return;
					}

					currAutomaton.setType(selectedValue);
				}
			}
		}
	}
}
