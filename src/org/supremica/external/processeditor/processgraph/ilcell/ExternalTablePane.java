package org.supremica.external.processeditor.processgraph.ilcell;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.JTextField;

import org.supremica.manufacturingTables.xsd.il.ExternalComponents;
import org.supremica.manufacturingTables.xsd.il.ExternalComponent;

public class ExternalTablePane 
						extends 
							BasicTablePane
						implements
							ActionListener
{
	private ExternalDataEditor editor = null;
	private List<JComboBox> comboBoxList = null;
	
	public ExternalTablePane(ExternalComponents externalComponents){
		super();
		setHeader("External components state");
		
		comboBoxList = new LinkedList<JComboBox>();
		
		tableHeader.addActionListener(this);
		
		addRow("Type");
		addRow("Initial");
		addRow("Action1");
		
		if(externalComponents == null){
			return;
		}
		
		for(ExternalComponent extComp : externalComponents.getExternalComponent()){
			addExternalComponent(extComp);
		}
	}
	
	public ExternalComponents getExternalComponents(){
		return TableExtractor.getExternalComponentsFromTable(table);
	}
	
	public void addExternalComponent(ExternalComponent component){
		addExternalComponent(component, null);
	}
	
	public void addExternalComponent(ExternalComponent component, String[] values){
		addCol( component.getComponent() );
		table.setValueAt(component.getMachine(), 0, getColumnCount()-1);
		
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
	
	private void setUpTypeRow() {
		int numberOfColumns = table.getColumnCount();
		for(int col = 0; col < numberOfColumns; col++){
			table.getColumnModel().
				  getColumn( col ).
				  	setCellEditor( new InternalCellEditor(comboBoxList.get(col)) );
		}
	}
	
	public void setCellEditor(int col, String[] values){
		comboBoxList.set(col, buildComboBox(values));
		setUpTypeRow();
	}
	
	public void removeExternalComponent(ExternalComponent component){
		if(component == null){
			return;
		}
		comboBoxList.remove(table.removeCol(component.getComponent()));
		setUpTypeRow();
	}
	
	public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();
        if(o == tableHeader){
        	editor = new ExternalDataEditor(this);
        	editor.setVisible(true);
		}else{
        	System.err.println("unknown source " + o);
        }
    }
}




class ExternalCellEditor
					extends
						DefaultCellEditor
{
	private int editor = -1;

	JComboBox validDataComboBox = null;

	JTextField txtField = null;

	public ExternalCellEditor( JComboBox dataComboBox ){
		super(new JTextField());

		validDataComboBox = dataComboBox;

		txtField = new JTextField();
		txtField.setBorder(BorderFactory.createLineBorder(Color.black));
	}

	public Object[] getValidValues(){
		if(validDataComboBox == null){
			return null;
		}

		int numberOfItems = validDataComboBox.getItemCount();

		Object[] os = new Object[numberOfItems];
		for(int i = 0; i < numberOfItems; i++){
			os[i] = validDataComboBox.getItemAt(i);
		}
		return os;
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
			return txtField;
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
			return txtField.getText();
		case 2:
			return validDataComboBox.getSelectedItem();
		}

		//default return
		return super.getCellEditorValue();
	}
}

