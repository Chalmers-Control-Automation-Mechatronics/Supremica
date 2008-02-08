package org.supremica.external.processeditor.processgraph.ilcell;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.Component;
import javax.swing.BorderFactory;
import java.awt.*;



public class InternalTablePane 
						extends 
							BasicTablePane
{
	InternalTablePane(){
		super();
		setHeader("Internal components state");
		
		addCol("Y18");
		addCol("Y16");
		addCol("Y14");
		
		addRow("Type");
		addRow("Initial");
		addRow("Action1");
		
		setUpTypeRow();
	}
	
	private void setUpTypeRow() {
		
		int numberOfColumns = table.getColumnCount();
		for(int col = 0; col < numberOfColumns; col++){
			table.getColumnModel().
				  getColumn( col ).
				  	setCellEditor( new InternalCellEditor() );
		}
	}
	
	public void addCol(String name){
		super.addCol( name );
		setUpTypeRow();
	}
	
	public void addRow(String name){
		super.addRow( name );
		
		int numberOfRows = getRowCount();
		Object previousValue;
		
		if(numberOfRows > 1){
			for(int col = 0; col < getColumnCount(); col++){
				previousValue = getValueAt(numberOfRows - 2, col);
				getTable().setValueAt(previousValue, numberOfRows - 1, col);
			}
		}
		
		setUpTypeRow();
	}
}


/**
 * Class for simplify editing of internal components table
 * @author David Millares
 *
 */
class InternalCellEditor extends
							DefaultCellEditor
{
	private int editor = -1;
	
	JComboBox comboBox = null;
	JTextField txtField = null;
	
	InternalCellEditor(){
		super(new JTextField());
		
		//Set up the editor
		comboBox = new JComboBox();
		
		comboBox.addItem("Actuator");
		comboBox.addItem("Variable");
		comboBox.addItem("Sensor");
		
		txtField = new JTextField();
		txtField.setBorder(BorderFactory.createLineBorder(Color.black));
		
		//setClickCountToStart(1);
	}
	
	//override in DefaultCellEditor
	public Component getTableCellEditorComponent(JTable table,
												 Object value,
												 boolean isSelected,
												 int row,
												 int column)
	{	
		switch(row){
		case 0:
			editor = 1;
			return comboBox;
		default:
			editor = 0;
			txtField.setText(value.toString());
			return txtField;
		}
	}
	
	//override in DefaultCellEditor
	public Object getCellEditorValue(){
		
		switch(editor){
		case 0:
			return txtField.getText();
		case 1:
			return comboBox.getSelectedItem();
		}
		
		//default return
		return super.getCellEditorValue();
	}
}
