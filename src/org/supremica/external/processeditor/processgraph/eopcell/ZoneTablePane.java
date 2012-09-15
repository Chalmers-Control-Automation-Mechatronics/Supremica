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
import org.supremica.external.processeditor.processgraph.table.ValueChangedCellRenderer;
import org.supremica.manufacturingTables.xsd.eop.Action;
import org.supremica.manufacturingTables.xsd.eop.Zones;

public class ZoneTablePane
						extends
							BasicTablePane
						implements
							ActionListener,
							MouseListener
{
    private static final long serialVersionUID = 1L;

    private List<JComboBox<String>> comboBoxList = null;
	private ZoneDataEditor editor = null;

	public ZoneTablePane(final Zones zones){
		super();
		setHeader("Zones");

		table.getModel().setRowEditable(0, false);
		table.setDefaultRenderer(Object.class, new ValueChangedCellRenderer());

		jbTableHeader.addActionListener(this);
		jbTableHeader.addMouseListener(this);

		comboBoxList = new LinkedList<JComboBox<String>>();

		if( null == zones ){
			return;
		}

		//Add columns
		final List<String> stringList = zones.getZone();
		for(final String actuator : stringList){
			addZone(actuator);
		}
	}

	private JComboBox<String> buildComboBox(final String[] values){
		JComboBox<String> comboBox = null;
		if(values != null && values.length > 0){
			comboBox = new JComboBox<String>();
			for(int i = 0; i < values.length; i++){
				comboBox.addItem(values[i]);
			}
		}
		return comboBox;
	}

	private void setUpTypeRow() {
		final int numberOfColumns = table.getColumnCount();
		for(int col = 0; col < numberOfColumns; col++){
			table.getColumnModel().
				  getColumn( col ).
				  	setCellEditor( new InternalCellEditor(comboBoxList.get(col)) );
		}
	}

	public void addZone(final String zone){
		addZone(zone, null);
	}
	public void addZone(final String zone, final String[] values){
		addCol( zone );
		table.setValueAt("- # -", 0, getColumnCount()-1);
		comboBoxList.add(buildComboBox(values));
		setUpTypeRow();
	}

	public void removeZone(final String name){
		if(name == null){
			return;
		}
		comboBoxList.remove(table.removeCol(name));
		setUpTypeRow();
	}

	public void setCellEditor(final int col, final String[] values){
		comboBoxList.set(col, buildComboBox(values));
		setUpTypeRow();
	}

	public Zones getZones(){
		return EOPTableExtractor.getZonesFromTable(table);
	}

	public void insertActions(final List<Action> actionList){
		for(final Action term : actionList){
			EOPTableFiller.insertZoneConditionFromActionToTable(term, table);
		}
	}


	// --- ActionListener ---
	public void actionPerformed(final ActionEvent e) {
        final Object o = e.getSource();
        if(o == jbTableHeader){
        	final Point pos =  jbTableHeader.getLocationOnScreen();

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
    public void mouseClicked(final MouseEvent e){}
    public void mouseEntered(final MouseEvent e){
    	if(e.getSource().equals(jbTableHeader)){
    		jbTableHeader.setContentAreaFilled(true);
    		jbTableHeader.setBorderPainted(true);
    	}
    }
    public void mouseExited(final MouseEvent e){
    	if(e.getSource().equals(jbTableHeader)){
    		jbTableHeader.setContentAreaFilled(false);
    		jbTableHeader.setBorderPainted(false);
    	}
    }
    public void mousePressed(final MouseEvent e){}
    public void mouseReleased(final MouseEvent e){}
}

/**
 * Class for simplify editing of zone components table
 * @author David Millares
 *
 */
class ZoneCellEditor extends
						DefaultCellEditor
{
    private static final long serialVersionUID = 1L;
	private static final String ACTUATOR = "Actuator";
	private static final String VARIABLE = "Variable";
	private static final String SENSOR = "Sensor";

	private int editor = -1;

	JComboBox<String> validTypesComboBox = null;
	JComboBox<String> validDataComboBox = null;

	JTextField txtField = null;

	public ZoneCellEditor( final JComboBox<String> dataComboBox ){
		super(new JTextField());

		//Set up the editor
		validTypesComboBox = new JComboBox<String>();

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

		final int numberOfItems = validDataComboBox.getItemCount();

		final Object[] os = new Object[numberOfItems];
		for(int i = 0; i < numberOfItems; i++){
			os[i] = validDataComboBox.getItemAt(i);
		}
		return os;
	}

	//override in DefaultCellEditor
	public Component getTableCellEditorComponent(final JTable table,
												 final Object value,
												 final boolean isSelected,
												 final int row,
												 final int column)
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
