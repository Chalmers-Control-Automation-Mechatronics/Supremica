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
import javax.swing.JTextField;

import org.supremica.external.processeditor.processgraph.table.BasicTablePane;
import org.supremica.manufacturingTables.xsd.eop.Action;
import org.supremica.manufacturingTables.xsd.eop.Zones;

public class ZoneTablePane 
						extends 
							BasicTablePane
						implements
							ActionListener,
							MouseListener
{
	private List<JComboBox> comboBoxList = null;
	private ZoneDataEditor editor = null;
	
	public ZoneTablePane(Zones zones){
		super();
		setHeader("Zones");
		
		table.getModel().setRowEditable(0, false);
		
		jbTableHeader.addActionListener(this);
		jbTableHeader.addMouseListener(this);
		
		comboBoxList = new LinkedList<JComboBox>();
		
		if( null == zones ){
			return;
		}
		
		//Add columns
		List<String> stringList = zones.getZone();
		for(String actuator : stringList){
			addZone(actuator);
		}
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
	
	public void addZone(String zone){
		addZone(zone, null);
	}
	public void addZone(String zone, String[] values){
		addCol( zone );
		table.setValueAt("- # -", 0, getColumnCount()-1);
		comboBoxList.add(buildComboBox(values));
		setUpTypeRow();
	}
	
	public void removeZone(String name){
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
	
	public Zones getZones(){
		return EOPTableExtractor.getZonesFromTable(table);
	}
	
	public void insertActions(List<Action> actionList){
		for(Action term : actionList){
			EOPTableFiller.insertZoneConditionFromActionToTable(term, table);
		}
	}
	
	
	// --- ActionListener ---
	public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();
        if(o == jbTableHeader){
        	Point pos =  jbTableHeader.getLocationOnScreen();
        	
        	editor = new ZoneDataEditor(this);
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
 * Class for simplify editing of zone components table
 * @author David Millares
 *
 */
class ZoneCellEditor extends
							DefaultCellEditor
{
	private static final String ACTUATOR = "Actuator";
	private static final String VARIABLE = "Variable";
	private static final String SENSOR = "Sensor";
	
	private int editor = -1;
	
	JComboBox validTypesComboBox = null;
	JComboBox validDataComboBox = null;
	
	JTextField txtField = null;
	
	public ZoneCellEditor( JComboBox dataComboBox ){
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
