package org.supremica.external.processeditor.processgraph.eopcell;

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

import org.supremica.external.processeditor.processgraph.table.BasicTablePane;
import org.supremica.external.processeditor.processgraph.table.ValueChangedCellRenderer;
import org.supremica.manufacturingTables.xsd.eop.*;

public class InternalTablePane
						extends
							BasicTablePane
						implements
							ActionListener,
							MouseListener
{
    private static final long serialVersionUID = 1L;

    public static final String ACTUATOR = "Actuator";
	public static final String VARIABLE = "Variable";
	public static final String SENSOR = "Sensor";

	private List<JComboBox<String>> comboBoxList = null;
	private InternalDataEditor editor = null;

	public InternalTablePane(final InternalComponents internalComponents){
		super();
		setHeader("Internal components state");

		jbTableHeader.addActionListener(this);
		jbTableHeader.addMouseListener(this);

		//first row not editable
		table.getModel().setRowEditable(0, false);
		table.setDefaultRenderer(Object.class, new ValueChangedCellRenderer());

		comboBoxList = new LinkedList<JComboBox<String>>();

		if(internalComponents == null){
			setUpTypeRow();
			return;
		}

		//Add columns
		List<String> stringList = internalComponents.getActuator();
		for(final String actuator : stringList){
			addActuator(actuator);
		}

		stringList = internalComponents.getVariable();
		for(final String variable : stringList){
			addVariable(variable);
		}

		stringList = internalComponents.getSensor();
		for(final String sensor : stringList){
			addSensor(sensor);
		}
		setUpTypeRow();
	}

	public void insertActions(final List<Action> actionList){
		for(final Action action : actionList){
			EOPTableFiller.insertInternalConditionFromActionToTable(action, table);
		}
	}

	public InternalComponents getInternalComponents(){
		return EOPTableExtractor.getInternalComponentsFromTable(table);
	}

	private void setUpTypeRow() {
		final int numberOfColumns = table.getColumnCount();
		for(int col = 0; col < numberOfColumns; col++){
			table.getColumnModel().
				  getColumn( col ).
				  	setCellEditor( new InternalCellEditor(comboBoxList.get(col)) );
		}
	}

	public void addActuator(final String name){
		addActuator(name, null);
	}

	public void addActuator(final String name, final String[] values){
		addCol( name );
		table.setValueAt(ACTUATOR, 0, getColumnCount()-1);
		comboBoxList.add(buildComboBox(values));
		setUpTypeRow();
	}

	public void addVariable(final String name){
		addVariable(name, null);
	}

	public void addVariable(final String name, final String[] values){
		addCol( name );
		table.setValueAt(VARIABLE, 0, getColumnCount()-1);
		comboBoxList.add(buildComboBox(values));
		setUpTypeRow();
	}

	public void addSensor(final String name){
		addSensor(name, null);
	}

	public void addSensor(final String name, final String[] values){
		addCol( name );
		table.setValueAt(SENSOR, 0, getColumnCount()-1);
		comboBoxList.add(buildComboBox(values));
		setUpTypeRow();
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

	public void removeInternalComponent(final String name){
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

	// --- ActionListener ---
	public void actionPerformed(final ActionEvent e) {
        final Object o = e.getSource();
        if(o == jbTableHeader){
        	final Point pos =  jbTableHeader.getLocationOnScreen();

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
 * Class for simplify editing of internal components table
 * @author David Millares
 *
 */
class InternalCellEditor extends
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

	public InternalCellEditor( final JComboBox<String> dataComboBox ){
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






