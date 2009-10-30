package org.supremica.external.processeditor.processgraph.eopcell;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JMenuItem;

import org.supremica.external.processeditor.processgraph.table.BasicTable;
import org.supremica.external.processeditor.processgraph.table.TableEvent;
import org.supremica.external.processeditor.processgraph.table.TableGroupPane;
import org.supremica.manufacturingTables.xsd.eop.Action;
import org.supremica.manufacturingTables.xsd.eop.ActuatorValue;
import org.supremica.manufacturingTables.xsd.eop.EOP;
import org.supremica.manufacturingTables.xsd.eop.ExternalComponentValue;
import org.supremica.manufacturingTables.xsd.eop.ExternalComponents;
import org.supremica.manufacturingTables.xsd.eop.InitialState;
import org.supremica.manufacturingTables.xsd.eop.InternalComponents;
import org.supremica.manufacturingTables.xsd.eop.ObjectFactory;
import org.supremica.manufacturingTables.xsd.eop.SensorValue;
import org.supremica.manufacturingTables.xsd.eop.VariableValue;
import org.supremica.manufacturingTables.xsd.eop.ZoneState;
import org.supremica.manufacturingTables.xsd.eop.Zones;


public class EOPTableGroupPane 
	extends TableGroupPane
{
    private static final long serialVersionUID = 1L;
	private static final String ROWNAME = "Action "; 
	
	InternalTablePane tableInternal = null;
	ExternalTablePane tableExternal = null;
	ZoneTablePane tableZone = null;
	StateChangeIdTablePane tableId = null;
	
	boolean showTableId = true;
	boolean showTableInternal = true;
	boolean showTableExternal = true;
	boolean showTableZone = true;
	
	boolean showRowHeader = true;
	
	private long timeStamp = 0;
	private boolean controlDown = false;
	
	private List<Action> actionCopyList = null;
	
	public EOPTableGroupPane(EOP eop){
		super();
		
		if(eop == null){
			eop = (new ObjectFactory()).createEOP();
		}
		
		tableId = new StateChangeIdTablePane();
		tableInternal = new InternalTablePane(eop.getInternalComponents());
		tableExternal = new ExternalTablePane(eop.getExternalComponents());
    	tableZone = new ZoneTablePane(eop.getZones());
    	
    	addActionRow(); //first row contain additional information
    	addActionRow(); //first action row
    	
    	insertActions(eop.getAction());
    	
    	insertInitialState(eop.getInitialState());
    	
    	setRowNames();
    	
    	//add all tables one time
    	addTable( tableId );
    	addTable( tableInternal );
    	addTable( tableExternal );
    	addTable( tableZone );		
    	
    	//show selected tables only
    	showTables();
	}
	
	public void insertActions(List<Action> actionList){
		
		tableId.insertActions( actionList );
		tableInternal.insertActions( actionList );
		tableExternal.insertActions( actionList );
		tableZone.insertActions( actionList );
		
	}
	
	public void insertInitialState(InitialState initialState){
		List<Action> actionList = new LinkedList<Action>();
		
		Action action = (new ObjectFactory()).createAction();
		
		if(null == initialState){
			return;
		}
		
		for(ActuatorValue val : initialState.getActuatorValue()){
			action.getActuatorValue().add(val);
		}
		
		for(SensorValue val : initialState.getSensorValue()){
			action.getSensorValue().add(val);
		}
		
		for(VariableValue val : initialState.getVariableValue()){
			action.getVariableValue().add(val);
		}
		
		for(ZoneState val : initialState.getZoneState()){
			action.getZoneState().add(val);
		}
		
		action.setActionNbr(BigInteger.valueOf(0));
		
		actionList.add(action);
		
		tableExternal.fillExternalComponentsInitialValue(initialState.getExternalComponentValue());
		
		tableInternal.insertActions(actionList);
		tableExternal.insertActions(actionList);
		tableZone.insertActions(actionList);
		
	}
	
	private void showTables(){
		boolean rowHeader = showRowHeader;
		
		removeAll();
		validate();
		
		if( showTableId ){
			tableId.showRowHeader(rowHeader);
			rowHeader = false;
			
			addTable( tableId );
		}
		
		if( showTableInternal ){
			tableInternal.showRowHeader(rowHeader);
			rowHeader = false;
			
			addTable( tableInternal );
		}
		
		if( showTableZone ){
			tableZone.showRowHeader(rowHeader);
			rowHeader = false;
			
			addTable( tableZone );
		}
		
		if( showTableExternal ){
			tableExternal.showRowHeader(rowHeader);
			rowHeader = false;
			
			addTable( tableExternal );
		}
		
		validate();
		repaint();
	}
	
	public void setRowHeaderVisible(boolean show){
		if(showRowHeader == show){
			return;
		}
		
		showRowHeader = show;
		showTables();
	}
	
	public InternalComponents getInternalComponents(){
		return tableInternal.getInternalComponents();
	}
	
	public ExternalComponents getExternalComponents(){
		return tableExternal.getExternalComponents();
	}
	
	public ExternalComponentValue[] getExternalComponentsInitialValue(){
		return tableExternal.getExternalComponentsInitialValue();
	}
	
	public Zones getZones(){
		return tableZone.getZones();
	}
	
	public Action[] getActions(){
		return EOPTableExtractor.getActions(tableId.getTable(),
				                            tableInternal.getTable(),
										    tableExternal.getTable(),
										    tableZone.getTable());
	}
	
	public void showStateChangeIdTable(boolean show){
		if(show == showTableId){
			return;
		}
		
		showTableId = show;
		showTables();
	}
	
	public void showInternalTable(boolean show){
		if(show == showTableInternal){
			return;
		}
		
		showTableInternal = show;
		showTables();
	}
	
	public void showExternalTable(boolean show){
		
		if(show == showTableExternal){
			return;
		}
		
		showTableExternal = show;
		showTables();
	}
	
	public void showZoneTable( boolean show ){
		
		if(show == showTableZone){
			return;
		}
		
		showTableZone = show;
		showTables();
	}
	
	
	
	public void copyActions(){
		Action[] actions = getActions();
		
		if(selectedRows.length > 0){
			actionCopyList = new LinkedList<Action>();
			
			for(int i = 0; i < selectedRows.length; i++){
				
				/*
				 * terms[0] is row 1 in table.
				 * Thats why selectedRows[i]-1 
				 */
				if(0 <= selectedRows[i] - 1){
					actionCopyList.add(actions[selectedRows[i]-1]);
				}
			}
		}
	}
	
	public void insertActionCopies(){
		int diff = 0;
		
		if(null == actionCopyList || actionCopyList.size() == 0){
			return;
		}
		
		if(selectedRows.length == 0){
			return;
		}
		
		diff = selectedRows[0] - actionCopyList.get(0).getActionNbr().intValue();
		for(Action term : actionCopyList){
			
			term.setActionNbr(term.getActionNbr().add(BigInteger.valueOf(diff - 1)));
			//add empty row
			addActionRow(term.getActionNbr().intValue() + 1, null);
		}
		
		//writhe to empty row
		insertActions( actionCopyList );
	}
	
	public void addActionRow(){
		
		tableId.addRow(ROWNAME);
		tableInternal.addRow(ROWNAME);
		tableExternal.addRow(ROWNAME);
    	tableZone.addRow(ROWNAME);
    	
    	setRowNames();
	}
	
	public void addActionRow(int rowIndex, BasicTable table){
		
		if(rowIndex == 0){
			rowIndex = 1;
		}else if(rowIndex > tableInternal.getRowCount()){
			addActionRow();
			return;
		}
		
		tableId.addRow(rowIndex, ROWNAME);
		tableInternal.addRow(rowIndex, ROWNAME);
		tableExternal.addRow(rowIndex, ROWNAME);
    	tableZone.addRow(rowIndex, ROWNAME);
    	
    	setRowNames();
    	
    	if(null != table){
    		table.getSelectionModel().setSelectionInterval(rowIndex, rowIndex);
    	}
	}
	
	public void deleteActionRow(int index){
		
		//don't remove type identifier row
		if(index == 0){
			return;
		}
		
		//don't delete last condition row
		if(tableInternal.getRowCount() <= 2){
			return;
		}
		
		tableId.removeRow(index);
		tableInternal.removeRow(index);
		tableExternal.removeRow(index);
    	tableZone.removeRow(index);
    	
    	setRowNames();
	}
	
	private void deleteSelectedRows( BasicTable table ){
		
		if(null == selectedRows || selectedRows.length == 0){
			return;
		}
		
		int row = selectedRows[0];
		
		for(int i = 0; i < selectedRows.length; i++){
			deleteActionRow(selectedRows[i]);
		}
		
		if(row >= table.getRowCount()){
			row = table.getRowCount() - 1;
		}
		
		table.getSelectionModel().setSelectionInterval(row, row);
	}
	
	
	//override
	protected void makePopupMenu(){
	
		// Create some menu items for the popup
		JMenuItem menuEdit = new JMenuItem( "Insert action" );
		popupMenu.add( menuEdit );
		menuEdit.addActionListener( this );
		
		menuEdit = new JMenuItem( "Remove action" );
		popupMenu.add( menuEdit );
		menuEdit.addActionListener( this );
		
		popupMenu.addSeparator();
		
		super.makePopupMenu();
	}
	
	//override
	public void actionPerformed( ActionEvent event ){
		if(event.getActionCommand().equals("Insert action")){
			if(null != selectedRows && selectedRows.length > 0){
				addActionRow(selectedRows[0],null);
			}else{
				addActionRow();
			}
			
			hidePopupMenu();
			
		}else if(event.getActionCommand().equals("Remove action")){
			if(null != selectedRows){
				for(int i = 0; i < selectedRows.length; i++){
					deleteActionRow(selectedRows[i]);
				}
			}
			
			hidePopupMenu();
			
		}else{
			super.actionPerformed(event);
		}
	}
	
	public void columnRemoved(TableEvent e){
		actionCopyList = null;
	}
	
	private void setRowNames(){
		
    	for(int i = 0; i < tableInternal.getRowCount(); i++ ){
    	
    		switch(i){
    			case 0:
    				tableId.setRowName(0, "");
    				tableInternal.setRowName(0, "");
    				tableExternal.setRowName(0, "");
    		    	tableZone.setRowName(0, "");
    		    	break;
    			case 1:
    				tableId.setRowName(1, "Initial");
    				tableInternal.setRowName(1, "Initial");
    	    		tableExternal.setRowName(1, "Initial");
    	    		tableZone.setRowName(1, "Initial");
    	    		break;
    	    	default:
    	    		tableId.setRowName(i, ROWNAME + (i-1));
    	    		tableInternal.setRowName(i, ROWNAME + (i-1));
        			tableExternal.setRowName(i, ROWNAME + (i-1));
        			tableZone.setRowName(i, ROWNAME + (i-1));	
    		}
    	}
    	
    	tableId.showRowHeader(tableId.isRowHeaderVisible());
    	tableInternal.showRowHeader(tableInternal.isRowHeaderVisible());
    	tableExternal.showRowHeader(tableExternal.isRowHeaderVisible());
    	tableZone.showRowHeader(tableZone.isRowHeaderVisible());
	}
	
	//override
	public void keyPressed(KeyEvent e){
		controlDown = (e.getModifiersEx() == KeyEvent.CTRL_DOWN_MASK);
	} 
	
	public void keyReleased(KeyEvent e){
		
		/*
		 * All tables fire KeyEvent on same event
		 * if we have a different time stamp it is a new event
		 */
		
		if(timeStamp != e.getWhen()){
			timeStamp = e.getWhen();
			
			//----------------------------------------
			//	DELETE
			//----------------------------------------
			if(e.getKeyCode() == KeyEvent.VK_DELETE){
				if(e.getSource() instanceof BasicTable){
					deleteSelectedRows((BasicTable) e.getSource());
				}
			}
			
			//Is CTRL down 
			if(controlDown){
				
				//----------------------------------
				//	CTRL + N
				//----------------------------------
				if(e.getKeyCode() == KeyEvent.VK_N){
					if(selectedRows.length > 0){
						if(e.getSource() instanceof BasicTable){
							addActionRow(selectedRows[0] + 1, (BasicTable) e.getSource());
						}
					}
				}
				
				//----------------------------------
				//	CTRL + C
				//----------------------------------
				if(e.getKeyCode() == KeyEvent.VK_C){
						copyActions();
				}
				
				//------------------------------------
				//	CTRL + V
				//------------------------------------
				if(e.getKeyCode() == KeyEvent.VK_V){
					int row = selectedRows[0];
					insertActionCopies();	
					
					if(e.getSource() instanceof BasicTable){
						
						((BasicTable)e.getSource())
								.getSelectionModel()
									.setSelectionInterval(row,row);
					}
				}
				
				//------------------------------------
				//	CTRL + X
				//------------------------------------
				if(e.getKeyCode() == KeyEvent.VK_X){
					copyActions();
					if(e.getSource() instanceof BasicTable){
						deleteSelectedRows((BasicTable) e.getSource());
					}
				}
			}
		}
	}
}
