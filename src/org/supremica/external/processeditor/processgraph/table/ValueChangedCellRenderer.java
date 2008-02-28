package org.supremica.external.processeditor.processgraph.table;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;

public class ValueChangedCellRenderer 
								extends
									BasicCellRenderer
{
	private final Color CHANGED_VALUE_CELL_COLOR = Color.ORANGE;
	
	public ValueChangedCellRenderer() {
		super();
	}

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		
		Component comp = super.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,column);
		
		
		if(isSelected || !table.isCellEditable(row, column)){
			return comp;
		}
		
		if(row > 1){
			if(!table.getValueAt(row-1, column).equals(value) && value.toString().length() > 0 ){
				comp.setBackground(CHANGED_VALUE_CELL_COLOR);
			}
		}
		
		return comp;
	}
}
