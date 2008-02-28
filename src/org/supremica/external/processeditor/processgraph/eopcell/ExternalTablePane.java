package org.supremica.external.processeditor.processgraph.eopcell;

import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.JTextField;

import org.supremica.external.processeditor.processgraph.table.BasicTablePane;
import org.supremica.manufacturingTables.xsd.eop.*;

public class ExternalTablePane
						extends 
							BasicTablePane
						implements
							ActionListener,
							MouseListener
{
	private ExternalDataEditor editor = null;
	private List<JComboBox> comboBoxList = null;
	
	public ExternalTablePane(ExternalComponents externalComponents){
		super();
		setHeader("External components state");
		
		comboBoxList = new LinkedList<JComboBox>();
		
		jbTableHeader.addActionListener(this);
		jbTableHeader.addMouseListener(this);
		
		table.getModel().setRowEditable(0, false);
		
		
		if(externalComponents == null){
			return;
		}
		
		for(ExternalComponent extComp : externalComponents.getExternalComponent()){
			addExternalComponent(extComp);
		}
	}
	
	public ExternalComponents getExternalComponents(){
		return EOPTableExtractor.getExternalComponentsFromTable(table);
	}
	
	public void addExternalComponent(ExternalComponent component){
		addExternalComponent(component, null);
	}
	
	public void insertActions(List<Action> actionList){
		for(Action term : actionList){
			EOPTableFiller.insertExternalConditionFromActionToTable(term, table);
		}
		
		//set only initial state editable
		for(int i = 2; i < table.getRowCount(); i++){
			table.getModel().setRowEditable(i, false);
		}
	}
	
	public void addRow(String rowName){
		super.addRow(rowName);
		
		if(2 > table.getRowCount()){
			table.getModel().setRowEditable(table.getRowCount()-1, false);
		}
	}
	
	public void addRow(int rowIndex, String rowName){
		super.addRow(rowIndex, rowName);
		if(2 > table.getRowCount()){
			table.getModel().setRowEditable(table.getRowCount()-1, false);
		}
	}
	
	public ExternalComponentValue[] getExternalComponentsInitialValue(){
		return EOPTableExtractor.getExternalComponentsInitialValueFromTable(table);
	}
	
	public void fillExternalComponentsInitialValue(List<ExternalComponentValue> extCompValList){
		EOPTableFiller.insertExternalConditionFromInitialToTable(extCompValList, table);
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
	
	/* --- ActionListener --- */
	public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();
        if(o == jbTableHeader){
        	Point pos =  jbTableHeader.getLocationOnScreen();
        	
        	editor = new ExternalDataEditor(this);
        	pos.translate( 0, -editor.getHeight()/2 );
        	
        	if(pos.y < 0){
        		pos.translate( 0, editor.getHeight()/2 );
        	}
        	
        	editor.setLocation(pos);     	
        	editor.setVisible(true);
        	
		}else{
        	System.err.println("unknown source " + o);
        }
    }
	
	/* --- MouseListener --- */
    public void mouseClicked(MouseEvent e){}
    public void mouseEntered(MouseEvent e){
    	if(e.getSource().equals(jbTableHeader)){
    		jbTableHeader.setContentAreaFilled(true);
    		jbTableHeader.setBorderPainted(true);
    	}
    }
    public void mouseExited(MouseEvent e){
    	if(e.getSource().equals(jbTableHeader)){
    		jbTableHeader.setContentAreaFilled(false);
    		jbTableHeader.setBorderPainted(false);
    	}
    } 
    public void mousePressed(MouseEvent e){}
    public void mouseReleased(MouseEvent e){}
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

