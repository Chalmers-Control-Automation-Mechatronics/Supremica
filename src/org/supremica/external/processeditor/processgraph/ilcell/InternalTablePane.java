package org.supremica.external.processeditor.processgraph.ilcell;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.JTable;
import java.awt.Component;
import java.awt.Point;
import javax.swing.BorderFactory;
import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.LinkedList;

import org.supremica.manufacturingTables.xsd.il.InternalComponents;
import org.supremica.manufacturingTables.xsd.il.Term;

public class InternalTablePane 
						extends 
							BasicTablePane
						implements
							ActionListener,
							MouseListener
{
	public static final String ACTUATOR = "Actuator";
	public static final String VARIABLE = "Variable";
	public static final String SENSOR = "Sensor";
	
	private List<JComboBox> comboBoxList = null;
	private InternalDataEditor editor = null;
	
	public InternalTablePane(InternalComponents internalComponents){
		super();
		setHeader("Internal components state");
		
		jbTableHeader.addActionListener(this);
		jbTableHeader.addMouseListener(this);
		
		//first row not editable
		table.getModel().setRowEditable(0, false);
		
		comboBoxList = new LinkedList<JComboBox>();
		
		if(internalComponents == null){
			setUpTypeRow();
			return;
		}
		
		//Add columns
		List<String> stringList = internalComponents.getActuator();
		for(String actuator : stringList){
			addActuator(actuator);
		}
		
		stringList = internalComponents.getVariable();
		for(String variable : stringList){
			addVariable(variable);
		}
		
		stringList = internalComponents.getSensor();
		for(String sensor : stringList){
			addSensor(sensor);
		}
		setUpTypeRow();
	}
	
	public void insertTerms(List<Term> termList){
		for(Term term : termList){
			ILTableFiller.insertInternalConditionFromTermToTable(term, table);
		}
	}
	
	public InternalComponents getInternalComponents(){
		return ILTableExtractor.getInternalComponentsFromTable(table);
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
		setUpTypeRow();
	}
	
	
	public void setCellEditor(int col, String[] values){
		comboBoxList.set(col, buildComboBox(values));
		setUpTypeRow();
	}
	
	//override
	public void addRow(String name){
		super.addRow( name );
		
		int numberOfRows = getRowCount();
		Object previousValue;
		
		//copy last row data to next row
		if(numberOfRows > 2){
			for(int col = 0; col < getColumnCount(); col++){
				previousValue = getValueAt(numberOfRows - 2, col);
				getTable().setValueAt(previousValue, numberOfRows - 1, col);
			}
		}
		
		setUpTypeRow();
	}
	
	// --- ActionListener ---
	public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();
        if(o == jbTableHeader){
        	Point pos =  jbTableHeader.getLocationOnScreen();
        	
        	editor = new InternalDataEditor(this);
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






