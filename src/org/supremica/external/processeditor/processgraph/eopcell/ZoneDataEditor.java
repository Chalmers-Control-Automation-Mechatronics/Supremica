package org.supremica.external.processeditor.processgraph.eopcell;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.table.TableColumn;
import javax.swing.*;

import org.supremica.external.processeditor.processgraph.table.BasicTable;
import org.supremica.external.processeditor.processgraph.table.DataTablePane;

public class ZoneDataEditor 
							extends
								JFrame
							implements
								ActionListener
{
	private JButton jbOk = null;
	private JButton jbCancel = null;
	private JButton jbApply = null;
	
	private DataTablePane tablePane = null;
	private ZoneTablePane zoneTable = null;
	
	public ZoneDataEditor(ZoneTablePane zoneTable){
		super("Zones");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		getContentPane().setLayout(new BorderLayout());
		
		this.zoneTable = zoneTable;
		
		//tabel panel
		tablePane = new DataTablePane();
		
		tablePane.getTable().addCol("Zone");
		tablePane.getTable().addCol("Value set");
		tablePane.getTable().initColumnSizes();
		
		fillTable(zoneTable.getTable());
		getContentPane().add(tablePane, BorderLayout.CENTER);
		
		//buttonPane
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new FlowLayout());
		
		jbOk = new JButton("Ok");
		jbOk.addActionListener(this);
		
		jbApply = new JButton("Apply");
		jbApply.addActionListener(this);
		
		jbCancel = new JButton("Cancel");
		jbCancel.addActionListener(this);
		
		buttonPane.add(jbOk);
		buttonPane.add(jbApply);
		buttonPane.add(jbCancel);
		
		getContentPane().add(buttonPane, BorderLayout.SOUTH);
		
		setSize(350,500);
	}
	
	private void fillTable(BasicTable table){
		int numberOfColumns = table.getColumnCount();
		
		Object value = null;
		
		for(int col = 0; col < numberOfColumns; col++){
			tablePane.getTable().addRow("");
			
			//get name
			value = table.getColumnName(col);
			tablePane.getTable().setValueAt(value, col, 0);
			
			//get valid values
			value = table.getColumnModel().getColumn(col).getCellEditor();
			if(value instanceof InternalCellEditor){
				Object[] os = ((InternalCellEditor)value).getValidValues();
				
				if(os != null && os.length >= 2){
					String tmp = os[0].toString();
					
					for(int i = 1; i < os.length; i++){
						tmp = tmp.concat(",");
						tmp = tmp.concat(os[i].toString());
					}
					tablePane.getTable().setValueAt(tmp, col, 1);
				}
			}
		}
		
		//Add some empty rows
		tablePane.getTable().addRow("");
		tablePane.getTable().addRow("");
		tablePane.getTable().addRow("");
	}
	
	private void updateZoneTable(){
		
		BasicTable table = tablePane.getTable();
		
		int numberOfRows = table.getRowCount();
		
		String name, value; 
		String[] values = null;
		boolean isNewComponent = false;
		
		removeDeletedColumns();
		
		for(int row = 0; row < numberOfRows; row++){
			
			name = table.getValueAt(row, 0).toString();
			value = table.getValueAt(row, 1).toString();
			
			if(name.length() > 0){
				
				values = value.split(",");
				if(values.length == 1 && values[0].length() == 0){
					values = null;
				}
				
				isNewComponent = true;
				for(int col = 0; col < zoneTable.getColumnCount(); col++){
					if(zoneTable.getColumnName(col).equals(name)){
						zoneTable.setCellEditor(col, values);
						isNewComponent = false;
					}
				}
				
				if(isNewComponent){
					zoneTable.addZone(name, values);
				}
			}
			
			values = null;
			value = "";
		}
		zoneTable.getTable().initColumnSizes();
		zoneTable.getTable().setPreferredScrollableViewportSize(zoneTable.getTable().getPreferredSize());
		zoneTable.validate();
		zoneTable.repaint();
	}
	
	public void removeDeletedColumns(){
		
		//remove columns in zoneTable
		int col = 0;
		
		String name = "";
		BasicTable table = tablePane.getTable();
		int numberOfRows = table.getRowCount();
		boolean remove = true;
		
		while(col < zoneTable.getColumnCount()){
			remove = true;
			
			for(int row = 0; row < numberOfRows; row++){
				name = table.getValueAt(row, 0).toString();
				if(zoneTable.getColumnName(col).equals(name)){
					remove = false;
				}
			}
			
			if(remove){
				name = zoneTable.getColumnName(col);
				zoneTable.removeZone(name);
			}else{
				col++;
			}
		}
	}
	
	
	// --- ActionListener ---
	public void actionPerformed(ActionEvent e) {
    	
        Object o = e.getSource();
        
        if(o == jbOk){
        	updateZoneTable();
        	setVisible(false);
        	dispose();
        }else if(o == jbApply){
        	updateZoneTable();
        }else if(o == jbCancel){
        	setVisible(false);
        	dispose();
		}else{
        	System.err.println("unknown source " + o);
        }
    }
}
