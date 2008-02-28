package org.supremica.external.processeditor.processgraph.table;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;

public class BasicCellRenderer 
								extends 
									JLabel  
								implements
									TableCellRenderer 
{
	private final Color NOT_EDITABLE_CELL_COLOR = Color.LIGHT_GRAY;
	
	public BasicCellRenderer() {
		setOpaque(true);
		setHorizontalAlignment(SwingConstants.CENTER);
	}

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		
		this.setBackground(table.getBackground());
		this.setForeground(table.getForeground());
		
		if(isSelected){
			this.setBackground(table.getSelectionBackground());
			this.setForeground(table.getSelectionForeground());
		}else{
			if(!table.isCellEditable(row, column)){
				this.setBackground(NOT_EDITABLE_CELL_COLOR);
			}
		}
		
		this.setText(value.toString());
		return this;
	}
}

