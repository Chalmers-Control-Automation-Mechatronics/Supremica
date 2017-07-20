//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 1999-2017 Knut Akesson, Martin Fabian, Robi Malik
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

import javax.swing.table.*;
import javax.swing.event.TableModelListener;
import javax.swing.event.TableModelEvent;

public class TableMap
	extends AbstractTableModel
	implements TableModelListener
{
	private static final long serialVersionUID = 1L;
	protected TableModel model;

	public TableModel getModel()
	{
		return model;
	}

	public void setModel(TableModel model)
	{
		this.model = model;

		model.addTableModelListener(this);
	}

	// By default, implement TableModel by forwarding all messages
	// to the model.
	public Object getValueAt(int aRow, int aColumn)
	{
		return model.getValueAt(aRow, aColumn);
	}

	public void setValueAt(Object aValue, int aRow, int aColumn)
	{
		model.setValueAt(aValue, aRow, aColumn);
	}

	public int getRowCount()
	{
		return (model == null)
			   ? 0
			   : model.getRowCount();
	}

	public int getColumnCount()
	{
		return (model == null)
			   ? 0
			   : model.getColumnCount();
	}

	public String getColumnName(int aColumn)
	{
		return model.getColumnName(aColumn);
	}

	public Class<?> getColumnClass(int aColumn)
	{
		return model.getColumnClass(aColumn);
	}

	public boolean isCellEditable(int row, int column)
	{
		return model.isCellEditable(row, column);
	}

	//
	// Implementation of the TableModelListener interface,
	//
	// By default forward all events to all the listeners.
	public void tableChanged(TableModelEvent e)
	{
		fireTableChanged(e);
	}
}
