//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.gui
//# CLASS:   ShadeTable
//###########################################################################
//# $Id: ShadeTable.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.gui;

import javax.swing.*;
import javax.swing.table.*;
import java.util.*;
import java.awt.*;

public class ShadeTable extends AbstractTableModel
{
	private String[] columnNames = {"Name","Colour"};
	private Object[][] data;
	public ShadeTable (ArrayList Shades)
	{
		UpdateData(Shades);
	}

	public void UpdateShades(ArrayList Shades)
	{
		EditorShade s;
		Color c;
		for (int i = 0; i < Shades.size(); i++)
		{
			s = (EditorShade)Shades.get(i);
			c = (Color)data[i][1];
			s.setName((String)data[i][0]);
			s.setRGB(c.getRGB());
			Shades.set(i, s);
		}	
	}

	public void UpdateData(ArrayList Shades)
	{
		EditorShade s;
		data = new Object[Shades.size()] [2];
		for (int i = 0; i < Shades.size(); i++)
		{
			s = (EditorShade)Shades.get(i);
			data[i][0] = s.getName();
			data[i][1] = new Color(s.getRGB());
		}
	}		
	
	public void addRow(EditorShade s, ArrayList Shades)
	{
		UpdateShades(Shades);
		Shades.add(s);
		UpdateData(Shades);
	}

        public int getColumnCount() {
            return columnNames.length;
        }

        public int getRowCount() {
            return data.length;
        }

        public String getColumnName(int col) {
            return columnNames[col];
        }

        public Object getValueAt(int row, int col) {
            return data[row][col];
        }

        /*
         * JTable uses this method to determine the default renderer/
         * editor for each cell.  If we didn't implement this method,
         * then the last column would contain text ("true"/"false"),
         * rather than a check box.
         */
        public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }

        /*
         * Don't need to implement this method unless your table's
         * editable.
         */
        public boolean isCellEditable(int row, int col) {
            //Note that the data/cell address is constant,
            //no matter where the cell appears onscreen.
            return true;
            
        }

        /*
         * Don't need to implement this method unless your table's
         * data can change.
         */
        public void setValueAt(Object value, int row, int col) {
            data[row][col] = value;
            fireTableCellUpdated(row, col);
        }
}
