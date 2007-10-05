package org.supremica.external.processAlgebraPetriNet.algorithms.dop2efa.gui;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import java.util.List;
import java.util.LinkedList;

public class ROPtableModel extends AbstractTableModel {
	
	public final int NAME_COL = 0;
	public final int TYPE_COL = 1;
	public final int COMMENT_COL = 2;
	public final int BOOLEAN_COL = 3;
	public final int FILE_PATH_COL = 4;
	
	private boolean DEBUG = false;
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

    /*
     * Don't need to implement this method unless your table's
     * editable.
     */
    public boolean isCellEditable(int row, int col) {
        //Note that the data/cell address is constant,
        //no matter where the cell appears onscreen.
        if (col < 3) {
            return false;
        } else {
            return true;
        }
    }

    /*
     * Don't need to implement this method unless your table's
     * data can change.
     */
    public void setValueAt(Object value, int row, int col) {
        if (DEBUG) {
            System.out.println("Setting value at " + row + "," + col
                               + " to " + value
                               + " (an instance of "
                               + value.getClass() + ")");
        }
        
        Object[] o = dataList.get(row);
        o[col] = value;
        
        fireTableCellUpdated(row, col);

        if (DEBUG) {
            System.out.println("New value of data:");
            printDebugData();
        }
    }
    
    public void insertRow(Object[] row) {
    	
    	if(row == null || row.length != COLUMS){
        	return;
        }
    	dataList.add(row);
    }
    
    public void deleteRow(Object[] row) {
    	
    	if(row == null || row.length != COLUMS){
        	return;
        }
    	dataList.remove(row);
    }
    
    public void deleteRow(int row) {
    	
    	if(row >= dataList.size()){
        	return;
        }
    	
    	dataList.remove(row);
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
    
    private void printDebugData() {
        int numRows = getRowCount();
        int numCols = getColumnCount();

        for (int i=0; i < numRows; i++) {
            System.out.print("    row " + i + ":");
            for (int j=0; j < numCols; j++) {
                System.out.print("  ");
            }
            System.out.println();
        }
        System.out.println("--------------------------");
    }
}
