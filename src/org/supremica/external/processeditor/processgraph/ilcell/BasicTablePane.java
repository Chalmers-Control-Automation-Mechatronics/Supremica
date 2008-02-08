package org.supremica.external.processeditor.processgraph.ilcell;

import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JList;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.BorderFactory;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

import java.awt.event.*;
import javax.swing.event.*;

import java.awt.Color;

public class BasicTablePane 
						extends
							JPanel

{
	protected JLabel tableHeaderLabel = null; 
	protected BasicTable table = null;
	protected JScrollPane scrollPane = null;
	
	private boolean showRowHeader = false;
	
	BasicTablePane(){
		super();
		setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
		
		tableHeaderLabel = new JLabel("Table");
		//tableHeaderLabel.setBorder(BorderFactory.createLineBorder(Color.black));
		//setBorder(BorderFactory.createLineBorder(Color.black));
		
		table = new BasicTable();
		
		table.setPreferredScrollableViewportSize(new Dimension(500, 70));
        table.setFillsViewportHeight(true);

        scrollPane = new JScrollPane(table);
        
        add(tableHeaderLabel);
        add(scrollPane);
	}
	
	public void setHeader(String headerText){
		tableHeaderLabel.setText(headerText);
	}
	
	public void showRowHeader(boolean show){
		
		showRowHeader = show;
		
		if(!show){
			scrollPane.setRowHeaderView(null);
			return;
		}
		
		int rows = table.getModel().getRowCount();
		String headers[];
		JList rowHeader;
		
		headers = new String[rows];
		for(int i = 0; i < rows; i++ ){
			headers[i] = table.getModel().getRowName(i);
		}
		
		rowHeader = new JList(headers);
	    rowHeader.setFixedCellWidth(50);
	    rowHeader.setFixedCellHeight(table.getRowHeight());
	    rowHeader.setCellRenderer(new RowHeaderRenderer(table));
		
	    scrollPane.setRowHeaderView(rowHeader);
	}
	
	public void addCol(String colName){
		table.getModel().addCol(colName);
		showRowHeader(showRowHeader);
	}
	
	public void addRow(String rowName){
		table.addRow(rowName);
		showRowHeader(showRowHeader);
	}
	
	public void removeRow(int index){
		table.getModel().removeRow(index);
		showRowHeader(showRowHeader);
	}
	
	public BasicTable getTable(){
		return table;
	}
	
	public int getRowCount(){
		return table.getRowCount();
	}
	
	public int getColumnCount(){
		return table.getColumnCount();
	}
	
	public Object getValueAt(int row, int column){
		return table.getValueAt(row, column);
	}
	
	public String getColumnName(int column){
		return table.getColumnName(column);
	}
	
	public String getRowName(int row){
		return table.getModel().getRowName(row);
	}
	
	public void setRowSelectionIntervall(int index0, int index1){
		table.setRowSelectionInterval(index0, index1);
	}
	
	public int[] getSelectedRows(){
		return table.getSelectedRows();
	}
	
	
	public void addtableListener(TableListener l){
		table.addTableListener(l);
	}
	
	public void removeTableListener(){
		table.removeTableListener();
	}	
}

class RowHeaderRenderer extends 
							JLabel 
						implements 
							ListCellRenderer
{
	  RowHeaderRenderer(JTable table) {
	    JTableHeader header = table.getTableHeader();
	    setOpaque(true);
	    setBorder(UIManager.getBorder("TableHeader.cellBorder"));
	    setHorizontalAlignment(CENTER);
	    setForeground(header.getForeground());
	    setBackground(header.getBackground());
	    setFont(header.getFont());
	  }
	  
	  public Component getListCellRendererComponent( JList list, 
	         Object value, int index, boolean isSelected, boolean cellHasFocus) {
	    setText((value == null) ? "" : value.toString());
	    return this;
	  }
}

