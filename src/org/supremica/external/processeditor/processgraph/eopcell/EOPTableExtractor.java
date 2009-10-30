package org.supremica.external.processeditor.processgraph.eopcell;

import org.supremica.external.processeditor.processgraph.table.BasicTable;
import org.supremica.manufacturingTables.xsd.eop.*;

import java.math.BigInteger;

public class EOPTableExtractor {

	private static ObjectFactory factory = new ObjectFactory();
	
	private static final String ACTUATOR = "Actuator";
	private static final String SENSOR = "Sensor";
	private static final String VARIABLE = "Variable";
	
	/*
	 * If the column needs additional information it is stored at
	 * this row.
	 */
	private static final int TYPE_ROW = 0;
	
	
	/**
	 * Extract ExternalComponent information from a BasicTable
	 * 
	 * @param table
	 * @return
	 */
	
	public static ExternalComponents
		getExternalComponentsFromTable(BasicTable table)
	{
		ExternalComponents extComponents = factory.createExternalComponents();
		ExternalComponent extComp = factory.createExternalComponent();
		
		int numberOfComponents = table.getColumnCount();
		
		/*
		 * Search all columns
		 */
		for(int col = 0; col < numberOfComponents; col++){
			extComp = factory.createExternalComponent();
		
			extComp.setComponent(table.getColumnName(col));
			extComp.setMachine(table.getValueAt(TYPE_ROW,col).toString());
			
			extComponents.getExternalComponent().add(extComp);
		}
		
		return extComponents;
	}
	
	/**
	 * Extract InternalComponents from a BasicTable.
	 * Actuator, Sensor and Variable type is taken from
	 * row number stored in TYPE_ROW.
	 * 
	 * @param table
	 * @return InternalComponents
	 */
	public static InternalComponents
		getInternalComponentsFromTable(BasicTable table)
	{
		InternalComponents internalComponents = factory.createInternalComponents();
		int numberOfComponents = table.getColumnCount();
		
		/*
		 * Search all columns
		 */
		for(int col = 0; col < numberOfComponents; col++){
			if( ACTUATOR.equals( table.getValueAt( TYPE_ROW, col ) )){	
				//Actuator
				internalComponents.getActuator().add(table.getColumnName(col));
			}else if( SENSOR.equals(table.getValueAt(TYPE_ROW, col))){ 
				//Sensor
				internalComponents.getSensor().add( table.getColumnName( col ) );
			}else if( VARIABLE.equals( table.getValueAt( TYPE_ROW, col ) )){ 
				//Variable
				internalComponents.getVariable().add( table.getColumnName( col ) );
			}else{
				//Unsuportet type
				//System.err.println("UNKNOWN: " + table.getValueAt(TYPE_ROW, col).toString());
			}
		}
		
		return internalComponents;
	}
	
	
	/**
	 * Extract ExternalComponentValue from a BasicTable.
	 * Only the initial state contains external information
	 * 
	 * @param table
	 * @return ExternalComponentValue[]
	 */
	public static ExternalComponentValue[]
		getExternalComponentsInitialValueFromTable(BasicTable table)
	{
		/*
		 * Row header contains component name
		 * row 0 contains machine
		 * row 1 contains value
		 */
		
		int row = 1; // row there value is to find
		
		ExternalComponent extComp = null;
		int numberOfComponents = table.getColumnCount();
		ExternalComponentValue[] extComponents = new ExternalComponentValue[numberOfComponents];
		
		for(int i = 0; i < extComponents.length; i++){
			extComponents[i] = factory.createExternalComponentValue();
		}
		
		/*
		 * Search all columns
		 */
		for(int col = 0; col < numberOfComponents; col++){
			extComp = factory.createExternalComponent();
			
			extComp.setComponent(table.getColumnName(col).trim());
			extComp.setMachine(table.getValueAt(TYPE_ROW, col).toString().trim());
			
			extComponents[col].setExternalComponent(extComp);
			extComponents[col].setValue(table.getValueAt(row, col).toString().trim());
			
		}
		
		return extComponents;
	}
	
	/**
	 * Extract zones from a BasicTable. 
	 * Zones are in the column names
	 * 
	 * @param table
	 * @return zones
	 */
	public static Zones
		getZonesFromTable(BasicTable table)
	{
		
		Zones zones = factory.createZones();
		
		int numberOfColumns = table.getColumnCount();
		
		//add all columns
		for(int col = 0; col < numberOfColumns; col++){
			if(!zones.getZone().contains(table.getColumnName(col))){
				zones.getZone().add(table.getColumnName(col));
			}
		}
		return zones;
	}
	
	/**
	 * Extract action step from basic tables.
	 * 
	 * @param tableInternal
	 * @param tableExternal
	 * @param tableZone
	 * @return Array of action there first is initial action and the rest follows.
	 */
	public static Action[] getActions(BasicTable tableId,
			                          BasicTable tableInternal, 
									  BasicTable tableExternal,
									  BasicTable tableZone)
	{
		ObjectFactory factory = new ObjectFactory();
		
		
		int numberOfRows = -1;
		
		//all tables have the same numbers of rows
		if(null != tableInternal){
			numberOfRows = tableInternal.getRowCount();
		}else if(null != tableExternal){
			numberOfRows = tableExternal.getRowCount();
		}else if(null != tableZone){
			numberOfRows = tableZone.getRowCount();
		}
		
		//create all actions
		//first row contains additional information 
		Action[] actions = new Action[numberOfRows - 1];
		for(int i = 0; i < actions.length; i++){
			actions[i] = factory.createAction();
			actions[i].setActionNbr(BigInteger.valueOf(i));
		}
		
		addIdActionState(actions, tableId);
		addInternalComponentsActionState(actions, tableInternal);
		addExternalComponentsActionState(actions, tableExternal);
		addZoneActionState(actions, tableZone);
		
		return actions;
	}
	
	
	/**
	 * Add internal components state to actions
	 * @param actions
	 * @param table
	 */
	private static void addInternalComponentsActionState(Action[] actions, BasicTable table){
		
		int col = -1;
		int row = -1;
		
		ObjectFactory factory = new ObjectFactory();
		
		ActuatorValue actVal = null;
		SensorValue senVal = null;
		VariableValue varVal = null;
		
		int numberOfColumns = -1;
		
		//check input
		if(null == table || null == actions|| 0 == actions.length){
			return;
		}
		
		numberOfColumns = table.getColumnCount();
		for(col = 0; col < numberOfColumns; col++){
			
			if("Actuator".equals(table.getValueAt(0, col)))
			{	
				//Actuator
				for(int i = 0; i < actions.length; i++){
					row = i + 1;
					
					actVal = factory.createActuatorValue();
					actVal.setActuator(table.getColumnName(col));
					actVal.setValue(table.getValueAt(row, col).toString().trim());
					
					actions[i].getActuatorValue().add(actVal);
				}	
			}
			else if("Sensor".equals(table.getValueAt(0, col)))
			{ 
				//Sensor
				for(int i = 0; i < actions.length; i++){
					row = i + 1;
					
					senVal = factory.createSensorValue();
						
					senVal.setSensor(table.getColumnName(col));
					if(null == table.getValueAt(row, col)){
						senVal.setValue("");
					}else{
						senVal.setValue(table.getValueAt(row, col).toString().trim());
					}
					actions[i].getSensorValue().add(senVal);
				}	
			}
			else if("Variable".equals(table.getValueAt(0, col)))
			{ 
				//Variable
				for(int i = 0; i < actions.length; i++){
					row = i + 1;
					
					varVal = factory.createVariableValue();
					varVal.setVariable(table.getColumnName(col));
					varVal.setValue(table.getValueAt(row, col).toString().trim());
					
					actions[i].getVariableValue().add(varVal);
				}	
			}
			else
			{
				;
			}
		}
	}
	
	/**
	 * Does nothing, external components state only exist in initial state.
	 * 
	 * @param actions
	 * @param table
	 */
	private static void addExternalComponentsActionState(Action[] actions, BasicTable table){
		
		return;
		
		/*
		ObjectFactory factory = new ObjectFactory();
		
		ExternalComponentValue extCompVal = null;
		ExternalComponent extComp = null;
		
		int numberOfColumns = table.getColumnCount();
		
		if(null == table || null == actions || 0 == actions.length){
			return;
		}
		
		for( int col = 0 ; col < numberOfColumns ; col++ ){
			
			extComp = factory.createExternalComponent();
			extComp.setComponent( table.getColumnName( col ) );
			extComp.setMachine( table.getValueAt( 0, col ).toString().trim() );
			
			for(int i = 0 ; i < actions.length ; i++ ){
				
				extCompVal = factory.createExternalComponentValue();
				extCompVal.setExternalComponent(extComp);
				
				extCompVal.setValue(table.getValueAt(i+1, col).toString().trim());
					//actions[i].getExternalComponentValue() .add(extCompVal);
			}
		}
		*/
	}
	
	/**
	 * Add zone state information to actions
	 * 
	 * @param actions
	 * @param table
	 */
	private static void addZoneActionState(Action[] actions, BasicTable table){
		
		
		int numberOfColumns = -1;
		
		ZoneState zoneState = null;
		ObjectFactory factory = new ObjectFactory();
		
		if(null == table || null == actions || 0 == actions.length){
			return;
		}
		
		numberOfColumns = table.getColumnCount();
		for(int col = 0; col < numberOfColumns; col++){
			for(int i = 0; i < actions.length; i++){
				zoneState = factory.createZoneState();
				zoneState.setZone(table.getColumnName(col));
				zoneState.setState(table.getValueAt(i+1, col).toString().trim());
				actions[i].getZoneState().add(zoneState);
			}
		}
	}
	
	
	/**
	 * Add id information to actions
	 * 
	 * @param actions
	 * @param table
	 */
	private static void addIdActionState(Action[] actions, BasicTable table){
		int numberOfColumns = -1;
		if(null == table || null == actions || 0 == actions.length){
			return;
		}
		numberOfColumns = table.getColumnCount();
		for(int col = 0; col < numberOfColumns; col++){
			for(int i = 0; i < actions.length; i++){
				actions[i].setId( table.getValueAt(i+1, col).toString().trim() );
			}
		}
	}
}
