package org.supremica.external.processeditor.tools.copextractor;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import java.util.List;
import java.util.LinkedList;

public class BasicTableModel
					extends AbstractTableModel {
	
	public final int NAME_COL = 0;
	public final int TYPE_COL = 1;
	public final int COMMENT_COL = 2;
	public final int BOOLEAN_COL = 3;
	public final int FILE_PATH_COL = 4;
	
	private final int COLUMS = 5;
	
    private String[] columnNames = {"Name",
    								"Type",
                                    "Comment",
                                    "Convert"};
    
    private List<Object[]> dataList = new LinkedList<Object[]>();
    	
    public int getColumnCount() {
        return columnNames.length;
    }

    public int getRowCount() {
        return dataList.size();
    }

    public String getColumnName(int col) {
        return columnNames[col];
    }

    public Object getValueAt(int row, int col) {
    	Object[] o = null;
    	
    	/*
    	 * valid row
    	 */
    	if(0 <= row && row < dataList.size()){
    		o = dataList.get(row);
    	}else{
    		return null;
    	}
    	
    	/*
    	 * valid col
    	 */
    	if(o != null){
    		if(0 <= col && col < o.length){
    			return o[col];
    		}
    	}
    	
    	return null;
    }

    /*
     * JTable uses this method to determine the default renderer/
     * editor for each cell.  If we didn't implement this method,
     * then the last column would contain text ("true"/"false"),
     * rather than a check box.
     */
    public Class getColumnClass(int c) {
    	if(c == 3){
    		return Boolean.class;
    	}else{
    		return String.class;
    	}
    }

    public boolean isCellEditable(int row, int col) {
        //Note that the data/cell address is constant,
        //no matter where the cell appears onscreen.
        if (col < 3) {
            return false;
        } else {
            return true;
        }
    }

    public void setValueAt(Object value, int row, int col) {
        Object[] o = dataList.get(row);
        o[col] = value;
        fireTableCellUpdated(row, col);
    }
    
    public void insertRow(Object[] row) {
    	
    	if(row == null || row.length != COLUMS){
        	return;
        }
    	dataList.add(row);
    	fireTableRowsInserted(0,getRowCount());
    }
    
    public void deleteRow(Object[] o) {
    	int row = -1;
    	if(o == null || o.length != COLUMS){
        	return;
        }
    	
    	row  = dataList.indexOf(o);
    	deleteRow(row);
    }
    
    public void deleteRow(int row) {
    	
    	if(row >= dataList.size()){
        	return;
        }
    	
    	dataList.remove(row);
    	fireTableRowsDeleted(row, row);
    }
    
    public boolean rowExist(Object[] o) {
    	
    	//check input
    	if(o == null || o.length != 5){
    		return false;
    	}
    	
    	/*
    	 *	if same file the whole row is equal 
    	 */
    	if(o[FILE_PATH_COL] instanceof String){
    		String tmp = (String)o[FILE_PATH_COL];
    		for(int i = 0; i < getRowCount(); i++){
    			if(tmp.equals((String)getValueAt(i,FILE_PATH_COL))){
    				return true;
    			}
    		}
    	}
    	
    	//no match
    	return false;
    }
}
