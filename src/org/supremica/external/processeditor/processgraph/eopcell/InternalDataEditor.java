package org.supremica.external.processeditor.processgraph.eopcell;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.table.TableColumn;
import javax.swing.*;

import org.supremica.external.processeditor.processgraph.table.BasicTable;
import org.supremica.external.processeditor.processgraph.table.DataTablePane;

public class InternalDataEditor 
							extends
								JFrame
							implements
								ActionListener
{
	private JButton jbOk = null;
	private JButton jbCancel = null;
	private JButton jbApply = null;
	
	private DataTablePane tablePane = null;
	private InternalTablePane internalTable = null;
	
	public InternalDataEditor(InternalTablePane internalTable){
		super("Internal components");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		getContentPane().setLayout(new BorderLayout());
		
		this.internalTable = internalTable;
		
		//tabel panel
		tablePane = new DataTablePane();
		
		tablePane.getTable().addCol("Component");
		tablePane.getTable().addCol("Type");
		tablePane.getTable().addCol("Value set");
		tablePane.getTable().initColumnSizes();
		
		fillTable(internalTable.getTable());
		
		//Set up the editor
		JComboBox validTypesComboBox = new JComboBox();
		validTypesComboBox.addItem("Actuator");
		validTypesComboBox.addItem("Variable");
		validTypesComboBox.addItem("Sensor");
		
		TableColumn column = tablePane.getTable().getColumnModel().getColumn(1);
		column.setCellEditor(new DefaultCellEditor(validTypesComboBox));
		
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
			
			//get type
			value = table.getValueAt(0, col);
			tablePane.getTable().setValueAt(value, col, 1);
			
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
					tablePane.getTable().setValueAt(tmp, col, 2);
				}
			}
		}
		
		//Add some empty rows
		tablePane.getTable().addRow("");
		tablePane.getTable().addRow("");
		tablePane.getTable().addRow("");
	}
	
	private void updateInternalTable(){
		
		BasicTable table = tablePane.getTable();
		
		int numberOfRows = table.getRowCount();
		
		String name, type, value; 
		String[] values = null;
		boolean isNewComponent = false;
		
		removeDeletedColumns();
		
		for(int row = 0; row < numberOfRows; row++){
			
			name = table.getValueAt(row, 0).toString();
			type = table.getValueAt(row, 1).toString();
			value = table.getValueAt(row, 2).toString();
			
			if(name.length() > 0 && type.length() > 0){
				
				values = value.split(",");
				if(values.length == 1 && values[0].length() == 0){
					values = null;
				}
				
				isNewComponent = true;
				for(int col = 0; col < internalTable.getColumnCount(); col++){
					if(internalTable.getColumnName(col).equals(name)){
						internalTable.getTable().setValueAt(type, 0, col);
						internalTable.setCellEditor(col, values);
						isNewComponent = false;
					}
				}
				
				if(isNewComponent){
					if(InternalTablePane.ACTUATOR.equals(type)){
						internalTable.addActuator(name, values);
					}else if(InternalTablePane.SENSOR.equals(type)){
						internalTable.addSensor(name, values);
					}else if(InternalTablePane.VARIABLE.equals(type)){
						internalTable.addVariable(name, values);
					}
				}
			}
			
			values = null;
			value = "";
		}
		internalTable.getTable().initColumnSizes();
		internalTable.getTable().setPreferredScrollableViewportSize(internalTable.getTable().getPreferredSize());
		internalTable.validate();
		internalTable.repaint();
	}
	
	public void removeDeletedColumns(){
		
		//remove columns in internalTable
		int col = 0;
		
		String name = "";
		BasicTable table = tablePane.getTable();
		int numberOfRows = table.getRowCount();
		boolean remove = true;
		
		while(col < internalTable.getColumnCount()){
			remove = true;
			
			for(int row = 0; row < numberOfRows; row++){
				name = table.getValueAt(row, 0).toString();
				if(internalTable.getColumnName(col).equals(name)){
					remove = false;
				}
			}
			
			if(remove){
				name = internalTable.getColumnName(col);
				internalTable.removeInternalComponent(name);
			}else{
				col++;
			}
		}
	}
	
	
	// --- ActionListener ---
	public void actionPerformed(ActionEvent e) {
    	
        Object o = e.getSource();
        
        if(o == jbOk){
        	updateInternalTable();
        	setVisible(false);
        	dispose();
        }else if(o == jbApply){
        	updateInternalTable();
        }else if(o == jbCancel){
        	setVisible(false);
        	dispose();
		}else{
        	System.err.println("unknown source " + o);
        }
    }
}
