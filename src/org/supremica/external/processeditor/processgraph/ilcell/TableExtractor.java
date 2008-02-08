package org.supremica.external.processeditor.processgraph.ilcell;

import org.supremica.manufacturingTables.xsd.il.*;

import java.math.BigInteger;
import java.util.*;

public class TableExtractor {

	private static ObjectFactory factory = new ObjectFactory();
	
	private static final String ACTUATOR = "Actuator";
	private static final String SENSOR = "Sensor";
	private static final String VARIABLE = "Variable";
	
	/*
	 * If the column needs additional information it is stored at
	 * this row.
	 */
	private static final int TYPE_ROW = 0;
	
	private static final String ZONE_SEPARATOR = ",";
	
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
			
			extComp.setMachine(table.getColumnName(col));
			extComp.setComponent(table.getValueAt(TYPE_ROW,col).toString());
			
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
		String tmp = "";
		String strings[] = null;
		
		Zones zones = factory.createZones();
		
		int numberOfColumns = table.getColumnCount();
		int numberOfRows = table.getRowCount();
		
		//search all rows and colums
		for(int col = 0; col < numberOfColumns; col++){
			for(int row = 0; row < numberOfRows; row++){
				
				//parse zone string from table
				tmp = table.getValueAt(row, col).toString();
				strings = tmp.split(ZONE_SEPARATOR);
				
				//add zone only one time
				if(strings != null){
					for(int i = 0; i < strings.length; i++){
						if(strings[i].length() != 0 &&
						   !zones.getZone().contains(strings[i])){
							zones.getZone().add(strings[i]);
						}
					}
				}
			}
		}
		return zones;
	}
	
	public static Operations
		getOperationsFromTable(BasicTable table)
	{
		String tmp = "";
		String strings[] = null;
	
		Operations ops = factory.createOperations();
	
		int numberOfColumns = table.getColumnCount();
		int numberOfRows = table.getRowCount();
	
		//search all rows and columns
		for(int col = 0; col < numberOfColumns; col++){
			for(int row = 0; row < numberOfRows; row++){
			
				//parse zone string from table
				tmp = table.getValueAt(row, col).toString();
				strings = tmp.split(ZONE_SEPARATOR);
			
				//add zone only one time
				if(strings != null){
					for(int i = 0; i < strings.length; i++){
						if(strings[i].length() != 0 &&
								!ops.getOperation().contains(strings[i])){
							ops.getOperation().add(strings[i]);
						}
					}
				}
			}
		}
		return ops;
	}
	
	
	
	
	
	
	
	
	public static Term[] getTerms(BasicTablePane tableInternal, 
					   BasicTablePane tableExternal,
					   BasicTablePane tableOperation,
					   BasicTablePane tableZone){
		
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
		Term[] terms = new Term[numberOfRows - 1];
		for(int i = 0; i < terms.length; i++){
			terms[i] = factory.createTerm();
			terms[i].setRow(BigInteger.valueOf(i));
		}
		
		/*
		 * Add information from internalTable
		 * Actuator
		 * Sensor
		 * Variable
		 */
		numberOfColumns = tableInternal.getColumnCount();
		for(col = 0; col < numberOfColumns; col++){
			
			if("Actuator".equals(tableInternal.getValueAt(0, col)))
			{	
				//Actuator
				for(int i = 0; i < terms.length; i++){
					row = i + 1;
					
					actVal = factory.createActuatorValue();
					actVal.setActuator(tableInternal.getColumnName(col));
					actVal.setValue(tableInternal.getValueAt(row, col).toString());
					
					terms[i].getActuatorValue().add(actVal);
				}	
			}
			else if("Sensor".equals(tableInternal.getValueAt(0, col)))
			{ 
				//Sensor
				for(int i = 0; i < terms.length; i++){
					row = i + 1;
					
					senVal = factory.createSensorValue();
					senVal.setSensor(tableInternal.getColumnName(col));
					senVal.setValue(tableInternal.getValueAt(row, col).toString());
					
					terms[i].getSensorValue().add(senVal);
				}	
			}
			else if("Variable".equals(tableInternal.getValueAt(0, col)))
			{ 
				//Variable
				for(int i = 0; i < terms.length; i++){
					row = i + 1;
					
					varVal = factory.createVariableValue();
					varVal.setVariable(tableInternal.getColumnName(col));
					varVal.setValue(tableInternal.getValueAt(row, col).toString());
					
					terms[i].getVariableValue().add(varVal);
				}	
			}
			else
			{
				;
			}
		}
		//----------------------------------------------------------------------------------------//
		
		
		
		
		
		/*
		 * Add information from externalTable
		 * Actuator
		 * Sensor
		 * Variable
		 */
		numberOfColumns = tableExternal.getColumnCount();
		for(col = 0; col < numberOfColumns; col++){
			
			extComp = factory.createExternalComponent();
			extComp.setMachine(tableExternal.getColumnName(col));
			extComp.setComponent(tableExternal.getValueAt(0, col).toString());
			
			for(int i = 0; i < terms.length; i++){
				row = i + 1;
				
				extCompVal = factory.createExternalComponentValue();
				extCompVal.setExternalComponent(extComp);
				
				extCompVal.setValue(tableExternal.getValueAt(row, col).toString());
				terms[i].getExternalComponentValue().add(extCompVal);
			}	
		}
		//----------------------------------------------------------------------------------------//
		
		
		ZoneCheck zoneCheck = null;
		AfterZones afterZones = null;
		BeforeZones beforeZones = null;
		
		/*
		 * Add information from zoneTable
		 * Actuator
		 * Sensor
		 * Variable
		 */
		numberOfColumns = tableZone.getColumnCount();
		if(numberOfColumns == 2){
			for(int i = 0; i < terms.length; i++){
				row = i + 1;
				
				beforeZones = factory.createBeforeZones();
				String[] zones = tableZone.getValueAt(row, 0).toString().split(",");
				for(int z = 0; z < zones.length; z++){
					beforeZones.getZone().add(zones[z]);
				}
				
				
				afterZones = factory.createAfterZones();
				zones = tableZone.getValueAt(row, 1).toString().split(",");
				for(int z = 0; z < zones.length; z++){
					afterZones.getZone().add(zones[z]);
				}
				
				zoneCheck = factory.createZoneCheck();
				
				zoneCheck.setBeforeZones(beforeZones);
				zoneCheck.setAfterZones(afterZones);
				
				terms[i].getZoneCheck().add(zoneCheck);
			}	
		}
		//----------------------------------------------------------------------------------------//
		
		return terms;
	}
}
