
/*
 * Supremica Software License Agreement
 *
 * The Supremica software is not in the public domain
 * However, it is freely available without fee for education,
 * research, and non-profit purposes.  By obtaining copies of
 * this and other files that comprise the Supremica software,
 * you, the Licensee, agree to abide by the following
 * conditions and understandings with respect to the
 * copyrighted software:
 *
 * The software is copyrighted in the name of Supremica,
 * and ownership of the software remains with Supremica.
 *
 * Permission to use, copy, and modify this software and its
 * documentation for education, research, and non-profit
 * purposes is hereby granted to Licensee, provided that the
 * copyright notice, the original author's names and unit
 * identification, and this permission notice appear on all
 * such copies, and that no charge be made for such copies.
 * Any entity desiring permission to incorporate this software
 * into commercial products or to use it for commercial
 * purposes should contact:
 *
 * Knut Akesson (KA), knut@supremica.org
 * Supremica,
 * Knarrhogsgatan 10
 * SE-431 60 MOLNDAL
 * SWEDEN
 *
 * to discuss license terms. No cost evaluation licenses are
 * available.
 *
 * Licensee may not use the name, logo, or any other symbol
 * of Supremica nor the names of any of its employees nor
 * any adaptation thereof in advertising or publicity
 * pertaining to the software without specific prior written
 * approval of the Supremica.
 *
 * SUPREMICA AND KA MAKES NO REPRESENTATIONS ABOUT THE
 * SUITABILITY OF THE SOFTWARE FOR ANY PURPOSE.
 * IT IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED WARRANTY.
 *
 * Supremica or KA shall not be liable for any damages
 * suffered by Licensee from the use of this software.
 *
 * Supremica is owned and represented by KA.
 */
package org.supremica.gui;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.util.*;
import org.supremica.log.*;
import org.supremica.automata.Automaton;
import org.supremica.gui.VisualProjectContainer;
import org.supremica.automata.AutomatonType;

class TypeCellEditor
	implements CellEditorListener
{
	private JTable theTable;
	private TableSorter theTableSorter;
	private JComboBox automatonTypeCombo;
	private VisualProjectContainer theVisualProjectContainer;
	private static Logger logger = LoggerFactory.createLogger(TypeCellEditor.class);

	public TypeCellEditor(JTable theTable, TableSorter theTableSorter, VisualProjectContainer theVisualProjectContainer)
	{
		this.theTable = theTable;
		this.theVisualProjectContainer = theVisualProjectContainer;
		this.theTableSorter = theTableSorter;
		automatonTypeCombo = new JComboBox();

		Iterator<AutomatonType> typeIt = AutomatonType.iterator();

		while (typeIt.hasNext())
		{
			automatonTypeCombo.addItem(typeIt.next());
		}

		TableColumnModel columnModel = theTable.getColumnModel();
		TableColumn typeColumn = columnModel.getColumn(Supremica.TABLE_TYPE_COLUMN);
		DefaultCellEditor cellEditor = new DefaultCellEditor(automatonTypeCombo);

		cellEditor.setClickCountToStart(2);
		typeColumn.setCellEditor(cellEditor);
		cellEditor.addCellEditorListener(this);
	}

	public void editingCanceled(ChangeEvent e) {}

	public void editingStopped(ChangeEvent e)
	{
		if (automatonTypeCombo.getSelectedIndex() >= 0)
		{
			AutomatonType selectedValue = (AutomatonType) automatonTypeCombo.getSelectedItem();

			if (selectedValue != null)
			{
				int selectedRow = theTable.getSelectedRow();
				int orgRow = theTableSorter.getOriginalRowIndex(selectedRow);

				if (selectedRow >= 0)
				{
					Automaton currAutomaton = null;

					try
					{
						currAutomaton = theVisualProjectContainer.getActiveProject().getAutomatonAt(orgRow);
					}
					catch (Exception ex)
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
