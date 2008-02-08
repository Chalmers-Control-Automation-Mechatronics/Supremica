package org.supremica.external.processeditor.processgraph.ilcell;

import javax.swing.table.AbstractTableModel;
import java.util.List;
import java.util.LinkedList;

public class BasicTableModel
						extends 
							AbstractTableModel
{
	private List<String> columnNames;
	private List<String> rowNames;
	
	private List<List<Object>> dataList;

	BasicTableModel(){
		
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
	
	public void setRowName(int row, String name) {
		rowNames.set(row, name);
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
	
	public void insertRow(List<Object> rowData, int rowIndex, String rowName){
		if(rowData == null ||
		   rowIndex > dataList.size()){
			return;
		}
		
		if(rowData.size() != getRowCount()){
			return;
		}
		
		
		dataList.add(rowIndex, rowData);
		fireTableStructureChanged();
		
		System.out.println("Row data added");
	}
}
