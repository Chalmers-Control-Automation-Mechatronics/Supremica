package org.supremica.external.processeditor.processgraph.ilcell;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;

import java.awt.AWTEvent;
import java.awt.Point;
import java.awt.event.*;

import javax.swing.event.*;
import java.util.*;

public class BasicTable 
					extends 
						JTable
{
	private BasicTableModel tableModel = null;
	private TableListener tableListener = null;
	
	BasicTable(){
		super();
		tableModel = new BasicTableModel();
		setModel(tableModel);
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
		tableModel.addRow(name);
		if(tableListener != null){
			tableListener.rowAdded(new TableEvent(this));
		}
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
	
	//override
	public void valueChanged(ListSelectionEvent e){
		super.valueChanged(e);
		
		if(tableListener != null){
			tableListener.tableSelectionChanged(new TableEvent(this));
		}
	}
}
