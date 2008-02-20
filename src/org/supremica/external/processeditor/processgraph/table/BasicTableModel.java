package org.supremica.external.processeditor.processgraph.table;

import javax.swing.table.AbstractTableModel;
import java.util.List;
import java.util.LinkedList;

public class BasicTableModel
						extends 
							AbstractTableModel
{
	private List<String> columnNames = null;
	private List<String> rowNames = null;
	
	private List<List<Object>> dataList = null;
	private List<Integer> noEditableRowList = null;
	
	public BasicTableModel(){
		
		columnNames = new LinkedList<String>();
		rowNames = new LinkedList<String>();
		
		dataList = new LinkedList<List<Object>>();
		dataList.add(new LinkedList<Object>());
	}
	
	public int getColumnCount() {
		return columnNames.size();
	}

	public int getRowCount() {
		return rowNames.size();
	}

	public String getColumnName(int col) {
		return columnNames.get(col);
	}
	
	public String getRowName(int row) {
		return rowNames.get(row);
	}
	
	public Object getValueAt(int row, int col) {
		try{
			return dataList.get(row).get(col);
		}catch(Exception e){
			return null;
		}
	}

	/*
	 * JTable uses this method to determine the default renderer/
	 * editor for each cell.
	 */
	public Class getColumnClass(int c) {
		return getValueAt(0, c).getClass();
	}

	public void setRowEditable(int row, boolean editable){
		
		if(noEditableRowList == null){
			noEditableRowList = new LinkedList<Integer>();
			if(!editable){
				noEditableRowList.add(row);
			}
			return;
		}
		
		if(editable){
			//try to remove from list
			noEditableRowList.remove(new Integer(row));
		}else{
			//add to list
			if(!noEditableRowList.contains(new Integer(row))){
				noEditableRowList.add(row);
			}
		}
	}
	
	/*
	 * Don't need to implement this method unless your table's
	 * editable.
	 */
	public boolean isCellEditable(int row, int col) {
		
		if(noEditableRowList == null){
			return true;
		}
		
		if(noEditableRowList.contains(new Integer(row))){
			return false;
		}
		
		return true;
	}

	/*
	 * Don't need to implement this method unless your table's
	 * data can change.
	 */
	public void setValueAt(Object value, int row, int col) {
		dataList.get(row).set(col, value);
		fireTableCellUpdated(row, col);
	}
	
	public void addCol(String name) {
		for(List<Object> l : dataList){
			l.add("");
		}
		
		columnNames.add(name);
		
		fireTableStructureChanged();
	}
	
	public int removeCol(String name) {
		int index = columnNames.indexOf(name);
		removeCol(index);
		return index;
	}
	
	public void removeCol(int index) {
		if(index < 0 || index >= getColumnCount()){
			return;
		}
		
		for(List<Object> row : dataList){
			row.remove(index);
		}
		columnNames.remove(index);
		
		fireTableStructureChanged();
	}
	
	
	public void setRowName(int row, String name) {
		rowNames.set(row, name);
	}
	
	public void removeRow(String name) {
		removeCol(rowNames.indexOf(name));
	}
	
	public void addRow(String name) {
		List<Object> newRow = new LinkedList<Object>();
		
		for(int i = 0; i < getColumnCount(); i++){
			newRow.add("");
		}
		
		dataList.add(newRow);
		rowNames.add(name);
		
		fireTableStructureChanged();
	}
	
	public void removeRow(int index) {
		if(index >= getRowCount()){
			return;
		}
		dataList.remove(index);
		rowNames.remove(index);
		
		fireTableStructureChanged();
	}
	
	public List<Object> getRow(int rowIndex){
		if(dataList == null || rowIndex > dataList.size()){
			return null;
		}
		
		return dataList.get(rowIndex);
	}
	
	public void addRow(List<Object> rowData, int rowIndex, String rowName){
		
		if(rowData == null){
			rowData = new LinkedList<Object>();
		}
		
		if(rowData.size() > getColumnCount()){
			rowData = rowData.subList(0, getColumnCount()-1);
		}
		
		while(rowData.size() < getColumnCount()){
			rowData.add("");
		}
		
		dataList.add(rowIndex, rowData);
		rowNames.add(rowName);
		
		fireTableStructureChanged();
	}
}
