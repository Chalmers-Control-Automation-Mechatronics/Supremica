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
	private final Color VALUE_CELL_COLOR = new Color(153,186,243,80);
	private final Color SELECTED_CELL_BODER_COLOR = new Color(0,0,0,100);
	
	public BasicCellRenderer() {
		setOpaque(true);
		
		//center text in cell
		setHorizontalAlignment(SwingConstants.CENTER);
	}

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		
		//set default
		this.setBackground(table.getBackground());
		this.setForeground(table.getForeground());
		
		if(hasFocus){
			setBorder(BorderFactory.createLineBorder(SELECTED_CELL_BODER_COLOR));
		}else{
			setBorder(BorderFactory.createEmptyBorder());
		}
		
		if(isSelected){
			//cell is selected
			this.setBackground(table.getSelectionBackground());
			this.setForeground(table.getSelectionForeground());
		}else if(!table.isCellEditable(row, column)){
			//cell is not editable
			this.setBackground(NOT_EDITABLE_CELL_COLOR);
		}else if(0 != value.toString().length()){
			//cell has value
			this.setBackground(VALUE_CELL_COLOR);
		}
		
		//set text
		this.setText(value.toString());
		
		//set tooltiptext if value to long
		if(getFontMetrics(getFont()).stringWidth(getText()) > table.getColumnModel().getColumn(column).getWidth()){
			setToolTipText(getText());
		}else{
			setToolTipText(null);
		}
		
		return this;
	}
}

