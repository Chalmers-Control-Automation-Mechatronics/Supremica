package org.supremica.external.processeditor.processgraph.eopcell;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.supremica.external.processeditor.SOCFrame;
import org.supremica.external.processeditor.processgraph.table.BasicTable;
import org.supremica.external.processeditor.processgraph.table.DataTablePane;
import org.supremica.manufacturingTables.xsd.eop.*;

public class ExternalDataEditor 
							extends
								JFrame
							implements
								ActionListener
{
    private static final long serialVersionUID = 1L;

    private JButton jbOk = null;
	private JButton jbCancel = null;
	private JButton jbApply = null;
	
	private DataTablePane tablePane = null;
	private ExternalTablePane externalTable = null;
	
	public ExternalDataEditor(ExternalTablePane externalTable){
		super("External components");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		//SOC icon
    	this.setIconImage(Toolkit.getDefaultToolkit().
  			  getImage(SOCFrame.class.getClass().
  				   getResource("/icons/processeditor/icon.gif")));	
		
		getContentPane().setLayout(new BorderLayout());
		
		this.externalTable = externalTable;
		
		//table panel
		tablePane = new DataTablePane();
		
		tablePane.getTable().addCol("Component");
		tablePane.getTable().addCol("Machine");
		tablePane.getTable().addCol("Value set");
		tablePane.getTable().initColumnSizes();
		
		fillTable(externalTable.getTable());
		
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
		
		setSize(300,500);
	}
	
	private void fillTable(BasicTable table){
		int numberOfColumns = table.getColumnCount();
		
		Object value = null;
		
		for(int col = 0; col < numberOfColumns; col++){
			tablePane.getTable().addRow("");
			
			//get name
			value = table.getColumnName(col);
			tablePane.getTable().setValueAt(value, col, 0);
			
			//get machine
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
	
	private void updateExternalTable(){
		
		BasicTable table = tablePane.getTable();
		
		int numberOfRows = table.getRowCount();
		
		String component, machine, value;
		String[] values = null;
		boolean isNewComponent = false;
		
		removeDeletedColumns();
		
		for(int row = 0; row < numberOfRows; row++){
			
			component = table.getValueAt(row, 0).toString();
			machine = table.getValueAt(row, 1).toString();
			value = table.getValueAt(row, 2).toString();
			
			if(component.length() > 0 && machine.length() > 0){
				
				values = value.split(",");
				if(values.length == 1 && values[0].length() == 0){
					values = null;
				}
				
				isNewComponent = true;
				for(int col = 0; col < externalTable.getColumnCount(); col++){
					if(externalTable.getColumnName(col).equals(component)){
						externalTable.getTable().setValueAt(machine, 0, col);
						externalTable.setCellEditor(col, values);
						isNewComponent = false;
					}
				}
				
				if(isNewComponent){
					ExternalComponent extComp = (new ObjectFactory()).createExternalComponent();
					
					extComp.setComponent(component);
					extComp.setMachine(machine);
					
					externalTable.addExternalComponent(extComp, values);
				}
			}
			
			values = null;
			value = "";
		}
		externalTable.getTable().initColumnSizes();
		externalTable.getTable().setPreferredScrollableViewportSize(externalTable.getTable().getPreferredSize());
		externalTable.validate();
		externalTable.repaint();
	}
	
	public void removeDeletedColumns(){
		
		//remove columns in externalTable
		int col = 0;
		
		String name = "";
		BasicTable table = tablePane.getTable();
		int numberOfRows = table.getRowCount();
		boolean remove = true;
		
		while(col < externalTable.getColumnCount()){
			remove = true;
			
			for(int row = 0; row < numberOfRows; row++){
				name = table.getValueAt(row, 0).toString();
				if(externalTable.getColumnName(col).equals(name)){
					remove = false;
				}
			}
			
			if(remove){
				
				ExternalComponent extComp = (new ObjectFactory()).createExternalComponent();
				
				name = externalTable.getColumnName(col);
				extComp.setComponent(name);
				
				name = externalTable.getValueAt(0, col).toString();
				extComp.setMachine(name);
				
				externalTable.removeExternalComponent(extComp);
			}else{
				col++;
			}
		}
	}
	
	
	/* ---ActionListener--- */
	public void actionPerformed(ActionEvent e) {
    	
        Object o = e.getSource();
        
        if(o == jbOk){
        	updateExternalTable();
        	setVisible(false);
        	dispose();
        }else if(o == jbApply){
        	updateExternalTable();
        }else if(o == jbCancel){
        	setVisible(false);
        	dispose();
		}else{
        	System.err.println("unknown source " + o);
        }
    }
}
