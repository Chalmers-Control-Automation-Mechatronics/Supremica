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
		InternalComponents intComponents = factory.createInternalComponents();
		int numberOfComponents = table.getColumnCount();
		for(int col = 0; col < numberOfComponents; col++){
			if(ACTUATOR.equals(table.getValueAt(TYPE_ROW, col))){	//Actuator
				intComponents.getActuator().add(table.getColumnName(col));
			}else if(SENSOR.equals(table.getValueAt(TYPE_ROW, col))){ //Sensor
				intComponents.getSensor().add(table.getColumnName(col));
			}else if(VARIABLE.equals(table.getValueAt(TYPE_ROW, col))){ //Variable
				intComponents.getVariable().add(table.getColumnName(col));
			}else{
				//System.err.println("UNKNOWN: " + table.getValueAt(TYPE_ROW, col).toString());
			}
		}
		return intComponents;
	}
	
	public static ExternalComponentValue[]
		getExternalComponentsInitialValueFromTable(BasicTable table)
	{
		int row = 1;
		
		ExternalComponent extComp = null;
		int numberOfComponents = table.getColumnCount();
		ExternalComponentValue[] extComponents = new ExternalComponentValue[numberOfComponents];
		
		for(int i = 0; i < extComponents.length; i++){
			extComponents[i] = factory.createExternalComponentValue();
		}
		
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
	 * Extract zones from a BasicTable. Search all cells in table and parse  
	 * Zone names as separated by ZONE_SEPARATOR.
	 * 
	 * @param table
	 * @return
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
	
	public static Action[] getActions(BasicTable tableInternal, 
									  BasicTable tableExternal,
									  BasicTable tableZone)
	{
		
		int col = -1;
		int row = -1;
		
		ObjectFactory factory = new ObjectFactory();
		
		ActuatorValue actVal = null;
		SensorValue senVal = null;
		VariableValue varVal = null;
		
		ExternalComponentValue extCompVal = null;
		ExternalComponent extComp = null;
		
		//all tables have the same numbers of rows
		int numberOfRows = tableInternal.getRowCount();
		int numberOfColumns = -1;
		
		//create all terms
		//first row contains additional information 
		Action[] actions = new Action[numberOfRows - 1];
		for(int i = 0; i < actions.length; i++){
			actions[i] = factory.createAction();
			actions[i].setActionNbr(BigInteger.valueOf(i));
		}
		
		//-----------------------------------------------------
		//	Internal components information from tableInternal
		//-----------------------------------------------------
		if(null != tableInternal){
			numberOfColumns = tableInternal.getColumnCount();
			for(col = 0; col < numberOfColumns; col++){
			
				if("Actuator".equals(tableInternal.getValueAt(0, col)))
				{	
					//Actuator
					for(int i = 0; i < actions.length; i++){
						row = i + 1;
					
						actVal = factory.createActuatorValue();
						actVal.setActuator(tableInternal.getColumnName(col));
						actVal.setValue(tableInternal.getValueAt(row, col).toString().trim());
					
						actions[i].getActuatorValue().add(actVal);
					}	
				}
				else if("Sensor".equals(tableInternal.getValueAt(0, col)))
				{ 
					//Sensor
					for(int i = 0; i < actions.length; i++){
						row = i + 1;
					
						senVal = factory.createSensorValue();
						
						senVal.setSensor(tableInternal.getColumnName(col));
						if(null == tableInternal.getValueAt(row, col)){
							senVal.setValue("");
						}else{
							senVal.setValue(tableInternal.getValueAt(row, col).toString().trim());
						}
						actions[i].getSensorValue().add(senVal);
					}	
				}
				else if("Variable".equals(tableInternal.getValueAt(0, col)))
				{ 
					//Variable
					for(int i = 0; i < actions.length; i++){
						row = i + 1;
					
						varVal = factory.createVariableValue();
						varVal.setVariable(tableInternal.getColumnName(col));
						varVal.setValue(tableInternal.getValueAt(row, col).toString().trim());
					
						actions[i].getVariableValue().add(varVal);
					}	
				}
				else
				{
					;
				}
			}
		}//end if
		
		
		
		//-----------------------------------------------------
		//	External components information from tableExternal
		//-----------------------------------------------------
		if(null != tableExternal){
			numberOfColumns = tableExternal.getColumnCount();
			for(col = 0; col < numberOfColumns; col++){
			
				extComp = factory.createExternalComponent();
				extComp.setComponent(tableExternal.getColumnName(col));
				extComp.setMachine(tableExternal.getValueAt(0, col).toString().trim());
			
			
				for(int i = 0; i < actions.length; i++){
					row = i + 1;
				
					extCompVal = factory.createExternalComponentValue();
					extCompVal.setExternalComponent(extComp);
				
					extCompVal.setValue(tableExternal.getValueAt(row, col).toString().trim());
					//actions[i].getExternalComponentValue() .add(extCompVal);
				}
			}
		}
		
		ZoneState zoneState = null;
		//-----------------------------------------------------
		//	Zone information from tableZone
		//-----------------------------------------------------
		if(null != tableZone){
			numberOfColumns = tableZone.getColumnCount();
			for(col = 0; col < numberOfColumns; col++){
				
				for(int i = 0; i < actions.length; i++){
					row = i + 1;
					
					zoneState = factory.createZoneState();
					zoneState.setZone(tableZone.getColumnName(col));
					zoneState.setState(tableZone.getValueAt(row, col).toString().trim());
					
					actions[i].getZoneState().add(zoneState);
				}
			}
		}
		
		
		
		return actions;
	}
}
