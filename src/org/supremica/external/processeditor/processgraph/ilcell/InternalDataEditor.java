package org.supremica.external.processeditor.processgraph.ilcell;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

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
		super();
		getContentPane().setLayout(new BorderLayout());
		
		this.internalTable = internalTable;
		
		//tabel panel
		tablePane = new DataTablePane();
		fillTable(internalTable.getTable());
		
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
		
		pack();
	}
	
	private void fillTable(BasicTable table){
		int numberOfColumns = table.getColumnCount();
		
		Object value = null;
		
		for(int col = 0; col < numberOfColumns; col++){
			tablePane.getTable().addRow("");
			
			value = table.getColumnName(col);
			tablePane.getTable().setValueAt(value, col, 0);
			
			value = table.getValueAt(0, col);
			tablePane.getTable().setValueAt(value, col, 1);
		}
	}
	
	private void updateInternalTable(){
		
		BasicTable table = tablePane.getTable();
		int numberOfRows = table.getRowCount();
		String name, type, value; 
		String[] values = null;
		
		for(int row = 0; row < numberOfRows; row++){
			
			name = table.getValueAt(row, 0).toString();
			type = table.getValueAt(row, 1).toString();
			value = table.getValueAt(row, 2).toString();
			
			if(name.length() > 0 && type.length() > 0){
				
				values = value.split(",");
				
				if(InternalTablePane.ACTUATOR.equals(type)){
					internalTable.addActuator(name, values);
				}else if(InternalTablePane.SENSOR.equals(type)){
					internalTable.addSensor(name, values);
				}else if(InternalTablePane.VARIABLE.equals(type)){
					internalTable.addVariable(name, values);
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
	
	// --- ActionListener ---
	public void actionPerformed(ActionEvent e) {
    	
        Object o = e.getSource();
        
        if(o == jbOk){
        	updateInternalTable();
        }else if(o == jbApply){
        	updateInternalTable();
        }else if(o == jbCancel){
        	;
		}else{
        	System.err.println("unknown source " + o);
        }
    }
}
