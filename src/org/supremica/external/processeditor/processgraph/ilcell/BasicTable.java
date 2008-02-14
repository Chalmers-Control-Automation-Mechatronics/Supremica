package org.supremica.external.processeditor.processgraph.ilcell;

import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableCellRenderer;
import java.awt.Component;

import javax.swing.table.TableCellEditor;
import javax.swing.event.*;
import java.util.*;

public class BasicTable 
					extends 
						JTable
{
	private BasicTableModel tableModel = null;
	private TableListener tableListener = null;
	
	public BasicTable(){
		super();
		tableModel = new BasicTableModel();
		setModel(tableModel);
		initColumnSizes();
	}
	
	
	/*
     * This method picks good column sizes.
     *
     */
    public void initColumnSizes() {
        
        TableColumn column = null;
        Component comp = null;
        int headerWidth = 0;
        int cellWidth = 0;
        
        TableCellRenderer headerRenderer = getTableHeader().getDefaultRenderer();
        
        //All columns
        for (int col = 0; col < getColumnCount(); col++) {
            column = getColumnModel().getColumn(col);

            comp = headerRenderer.getTableCellRendererComponent(null, column.getHeaderValue(),false, false, 0, 0);
            headerWidth = comp.getPreferredSize().width;
            
            //All rows in column
            for(int row = 0; row < getRowCount(); row++){
            	comp = getDefaultRenderer(tableModel.getColumnClass(col)).getTableCellRendererComponent(this, getValueAt(row, col),false, false, 0, col);
            	
            	//take the widest
            	if(cellWidth < comp.getPreferredSize().width){
            		cellWidth = comp.getPreferredSize().width;
            	}
            }
            column.setPreferredWidth(Math.max(headerWidth, cellWidth));
        }
    }
    

	
	public BasicTableModel getModel(){
		return tableModel;
	}
	
	public void addCol(String name){
		tableModel.addCol(name);
		
		if(tableListener != null){
			tableListener.columnAdded(new TableEvent(this));
		}
	}
	
	public void addRow(String name){
		
		int numberOfCols = getColumnCount();
		TableCellEditor[] cellEditor = new TableCellEditor[numberOfCols];
		
		for(int col = 0; col < numberOfCols; col++){
			cellEditor[col] = getColumnModel().getColumn(col).getCellEditor();
		}
		
		tableModel.addRow(name);
		
		for(int col = 0; col < numberOfCols; col++){
			getColumnModel().getColumn(col).setCellEditor(cellEditor[col]);
		}
		
		if(tableListener != null){
			tableListener.rowAdded(new TableEvent(this));
		}
	}
	
	public void removeRow(String name){
		tableModel.removeRow(name);
		if(tableListener != null){
			tableListener.rowRemoved(new TableEvent(this));
		}
	}
	
	public int removeCol(String name){
		int index = tableModel.removeCol(name);
		if(tableListener != null){
			tableListener.columnRemoved(new TableEvent(this));
		}
		return index;
	}
	
	public void removeCol(int index){
		tableModel.removeCol(index);
		if(tableListener != null){
			tableListener.columnRemoved(new TableEvent(this));
		}
	}
	
	public int findColumn(String name){
		return tableModel.findColumn(name);
	}
	
	public void addTableListener(TableListener l){
		tableListener = l;
	}
	
	public void removeTableListener(){
		tableListener = null;
	}
	
	public List<Object> getRow(int rowIndex){
		return tableModel.getRow(rowIndex);
	}
	
	public void insertRow(List<Object> rowData, int rowIndex, String rowName){
		tableModel.insertRow(rowData, rowIndex, rowName);
	}
	
	public void removeSelectedRows(){
    	int rows[] = getSelectedRows();
    	for(int i = 0; i < rows.length; i++){
    		getModel().removeRow(rows[i]-i);
    	}
    }
	
	//override
	public void valueChanged(ListSelectionEvent e){
		super.valueChanged(e);
		
		if(tableListener != null){
			tableListener.tableSelectionChanged(new TableEvent(this));
		}
	}
}
