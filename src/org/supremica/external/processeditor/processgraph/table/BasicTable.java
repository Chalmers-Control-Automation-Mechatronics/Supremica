package org.supremica.external.processeditor.processgraph.table;

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
            	comp = getDefaultRenderer(tableModel.
            								getColumnClass(col)).
            								getTableCellRendererComponent(this, getValueAt(row, col),false, false, 0, col);
            	
            	//take the widest
            	if(cellWidth < comp.getPreferredSize().width){
            		cellWidth = comp.getPreferredSize().width;
            	}
            }
            column.setPreferredWidth(Math.max(headerWidth, cellWidth));
        }
    }
    

	/**
	 * Return table model from class <code>BasicTableModel</code>
	 */
	public BasicTableModel getModel(){
		return tableModel;
	}
	
	/**
	 * Add a column to the table. 
	 * @param name - column name
	 */
	public void addCol(String name){
		tableModel.addCol(name);
		
		if(tableListener != null){
			tableListener.columnAdded(new TableEvent(this));
		}
	}
	
	/**
	 * Add a new row to the table.
	 * @param name - row name
	 */
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
	
	public void setRowName(int row, String name){
		tableModel.setRowName(row, name);
	}
	
	/**
	 * Removes one row.
	 * @param name - name of row to be removed.
	 */
	public void removeRow(String name){
		
		int numberOfCols = getColumnCount();
		TableCellEditor[] cellEditor = new TableCellEditor[numberOfCols];
		
		for(int col = 0; col < numberOfCols; col++){
			cellEditor[col] = getColumnModel().getColumn(col).getCellEditor();
		}
		
		tableModel.removeRow(name);
		
		for(int col = 0; col < numberOfCols; col++){
			getColumnModel().getColumn(col).setCellEditor(cellEditor[col]);
		}
		
		if(tableListener != null){
			tableListener.rowRemoved(new TableEvent(this));
		}
	}
	
	/**
	 * Removes one row.
	 * @param index - row to be removed
	 */
	public void removeRow(int index){
		
		int numberOfCols = getColumnCount();
		TableCellEditor[] cellEditor = new TableCellEditor[numberOfCols];
		
		for(int col = 0; col < numberOfCols; col++){
			cellEditor[col] = getColumnModel().getColumn(col).getCellEditor();
		}
		
		tableModel.removeRow(index);
		
		for(int col = 0; col < numberOfCols; col++){
			getColumnModel().getColumn(col).setCellEditor(cellEditor[col]);
		}
		
		if(tableListener != null){
			tableListener.rowRemoved(new TableEvent(this));
		}
	}
	
	
	/**
	 * Removes one column
	 * @param name - the name of the column to be removed
	 * @return the index of the removed column, or -1 if no column were removed.
	 */
	public int removeCol(String name){
		int index = tableModel.removeCol(name);
		if(tableListener != null){
			tableListener.columnRemoved(new TableEvent(this));
		}
		return index;
	}
	
	/**
	 * Remove one column from table.
	 * @param index - the index of column to be removed
	 */
	public void removeCol(int index){
		tableModel.removeCol(index);
		if(tableListener != null){
			tableListener.columnRemoved(new TableEvent(this));
		}
	}
	
	/**
	 * Return the index of the first column with name.
	 * @param name - the name of column to find.
	 * @return index of column if found or -1
	 */
	public int findColumn(String name){
		return tableModel.findColumn(name);
	}
	/**
	 * Set the table listener for this table.
	 * @param l - TableListener
	 */
	public void addTableListener(TableListener l){
		tableListener = l;
	}
	
	/**
	 * Removes the table listener from this table.
	 */
	public void removeTableListener(){
		tableListener = null;
	}
	
	/**
	 * Get all data from a row.
	 * @param rowIndex - index of row to get data from.
	 * @return A List<Object> with data from row.
	 */
	public List<Object> getRow(int rowIndex){
		return tableModel.getRow(rowIndex);
	}
	
	/**
	 * Add one row to table.
	 * @param rowData - List<Object> with data to be inserted to the row.
	 * @param rowIndex - the index of the inserted row.
	 * @param rowName - Name of the new row.
	 */
	public void addRow(List<Object> rowData, int rowIndex, String rowName){
		int numberOfCols = getColumnCount();
		TableCellEditor[] cellEditor = new TableCellEditor[numberOfCols];
		
		for(int col = 0; col < numberOfCols; col++){
			cellEditor[col] = getColumnModel().getColumn(col).getCellEditor();
		}
		
		
		tableModel.addRow(rowData, rowIndex, rowName);
		
		for(int col = 0; col < numberOfCols; col++){
			getColumnModel().getColumn(col).setCellEditor(cellEditor[col]);
		}
		
		if(tableListener != null){
			tableListener.rowAdded(new TableEvent(this));
		}
	}
	
	/**
	 * Removes all the selected rows from table.
	 */
	public void removeSelectedRows(){
    	int rows[] = getSelectedRows();
    	
    	int numberOfCols = getColumnCount();
		TableCellEditor[] cellEditor = new TableCellEditor[numberOfCols];
		
		for(int col = 0; col < numberOfCols; col++){
			cellEditor[col] = getColumnModel().getColumn(col).getCellEditor();
		}
		
    	for(int i = 0; i < rows.length; i++){
    		getModel().removeRow(rows[i]-i);
    	}
    	
    	for(int col = 0; col < numberOfCols; col++){
			getColumnModel().getColumn(col).setCellEditor(cellEditor[col]);
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
