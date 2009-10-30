
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

/**
 * In a chain of data manipulators some behaviour is common. TableMap
 * provides most of this behavour and can be subclassed by filters
 * that only need to override a handful of specific methods. TableMap
 * implements TableModel by routing all requests to its model, and
 * TableModelListener by routing all events to its listeners. Inserting
 * a TableMap which has not been subclassed into a chain of table filters
 * should have no effect.
 *
 * @version 1.4 12/17/97
 * @author Philip Milne
 */
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
