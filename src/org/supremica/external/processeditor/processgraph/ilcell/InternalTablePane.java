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
import java.awt.Color;
import java.util.List;
import java.util.LinkedList;

import org.supremica.manufacturingTables.xsd.il.InternalComponents;

public class InternalTablePane 
						extends 
							BasicTablePane
{
	public static final String ACTUATOR = "Actuator";
	public static final String VARIABLE = "Variable";
	public static final String SENSOR = "Sensor";
	
	private List<JComboBox> comboBoxList = null;
	
	public InternalTablePane(InternalComponents internalComponents){
		super();
		setHeader("Internal components state");
		
		//Add default rows
		addRow("Type");
		addRow("Initial");
		addRow("Action1");
		
		comboBoxList = new LinkedList<JComboBox>();
		
		if(internalComponents == null){
			setUpTypeRow();
			return;
		}
		
		List<String> stringList = internalComponents.getActuator();
		for(String actuator : stringList){
			addActuator(actuator, new String[]{"ON","OFF"});
		}
		
		stringList = internalComponents.getVariable();
		for(String variable : stringList){
			addVariable(variable,  new String[]{"0","1","2"});
		}
		
		stringList = internalComponents.getSensor();
		for(String sensor : stringList){
			addSensor(sensor);
		}
	}
	
	private void setUpTypeRow() {
		int numberOfColumns = table.getColumnCount();
		for(int col = 0; col < numberOfColumns; col++){
			table.getColumnModel().
				  getColumn( col ).
				  	setCellEditor( new InternalCellEditor(comboBoxList.get(col)) );
		}
	}
	
	public void addActuator(String name){
		addActuator(name, null);
	}
	
	public void addActuator(String name, String[] values){
		addCol( name );
		table.setValueAt(ACTUATOR, 0, getColumnCount()-1);
		comboBoxList.add(buildComboBox(values));
		setUpTypeRow();
	}
	
	public void addVariable(String name){
		addVariable(name, null);
	}
	
	public void addVariable(String name, String[] values){
		addCol( name );
		table.setValueAt(VARIABLE, 0, getColumnCount()-1);
		comboBoxList.add(buildComboBox(values));
		setUpTypeRow();
	}
	
	public void addSensor(String name){
		addSensor(name, null);
	}
	
	public void addSensor(String name, String[] values){
		addCol( name );
		table.setValueAt(SENSOR, 0, getColumnCount()-1);
		comboBoxList.add(buildComboBox(values));
		setUpTypeRow();
	}
	
	private JComboBox buildComboBox(String[] values){
		JComboBox comboBox = null;
		if(values != null && values.length > 0){
			comboBox = new JComboBox();
			for(int i = 0; i < values.length; i++){
				comboBox.addItem(values[i]);
			}
		}
		return comboBox;
	}
	
	public void removeInternalComponent(String name){
		if(name == null){
			return;
		}
		comboBoxList.remove(table.removeCol(name));
	}
	
	//override
	public void addRow(String name){
		super.addRow( name );
		
		int numberOfRows = getRowCount();
		Object previousValue;
		
		//copy last row data to next row
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
	private static final String ACTUATOR = "Actuator";
	private static final String VARIABLE = "Variable";
	private static final String SENSOR = "Sensor";
	
	private int editor = -1;
	
	JComboBox validTypesComboBox = null;
	JComboBox validDataComboBox = null;
	
	JTextField txtField = null;
	
	public InternalCellEditor( JComboBox dataComboBox ){
		super(new JTextField());
		
		//Set up the editor
		validTypesComboBox = new JComboBox();
		
		validDataComboBox = dataComboBox;
		
		validTypesComboBox.addItem(ACTUATOR);
		validTypesComboBox.addItem(VARIABLE);
		validTypesComboBox.addItem(SENSOR);
		
		txtField = new JTextField();
		txtField.setBorder(BorderFactory.createLineBorder(Color.black));
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
			return validTypesComboBox;
		default:
			if(validDataComboBox == null){
				editor = 0;
				txtField.setText(value.toString());
				return txtField;
			}
			
			editor = 2;
			return validDataComboBox;
		}
	}
	
	//override in DefaultCellEditor
	public Object getCellEditorValue(){
		
		switch(editor){
		case 0:
			return txtField.getText();
		case 1:
			return validTypesComboBox.getSelectedItem();
		case 2:
			return validDataComboBox.getSelectedItem();
		}
		
		//default return
		return super.getCellEditorValue();
	}
}






