package org.supremica.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.util.*;
import java.io.*;
import org.supremica.log.*;
import javax.help.*;
import org.supremica.*;
import org.supremica.automata.algorithms.*;
import org.supremica.comm.xmlrpc.*;
import org.supremica.gui.editor.*;
import org.supremica.gui.help.*;
import org.supremica.automata.Automaton;
import org.supremica.automata.AutomatonContainer;
import org.supremica.automata.AutomatonType;

class TypeCellEditor
	implements CellEditorListener
{
	private JTable theTable;
	private TableSorter theTableSorter;
	private JComboBox automatonTypeCombo;
	private AutomatonContainer theAutomatonContainer;
	private static Logger logger = LoggerFactory.createLogger(TypeCellEditor.class);

	public TypeCellEditor(JTable theTable, TableSorter theTableSorter, AutomatonContainer theAutomatonContainer)
	{
		this.theTable = theTable;
		this.theAutomatonContainer = theAutomatonContainer;
		this.theTableSorter = theTableSorter;
		automatonTypeCombo = new JComboBox();

		Iterator typeIt = AutomatonType.iterator();

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
						currAutomaton = theAutomatonContainer.getAutomatonAt(orgRow);
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
						System.exit(0);
					}

					currAutomaton.setType(selectedValue);
				}
			}
		}
	}
}
