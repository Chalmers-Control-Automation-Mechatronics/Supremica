package org.supremica.external.processeditor.processgraph.eopcell;

import org.supremica.external.processeditor.processgraph.table.BasicTable;
import org.supremica.manufacturingTables.xsd.eop.*;
import java.util.List;

public class EOPTableFiller {
	
	/* final */
	private static final String SEPARATOR = ",";
	private static final String SPACE = " ";
	
	/* variables */
	private static int row = -1;
	private static int col = -1;
	
	/**
	 * Fill table to row + 1 from term. Because row 0 is supposed to contain
	 * additional information. Reset private variables col and row.
	 * 
	 * @param term
	 * @param table
	 */
	private static void setUp(Action action, BasicTable table){
		
		row = -1;
		col = -1;
		
		row = action.getActionNbr().intValue();
		row = row + 1; //row 0 contains additional information
		
		//make sure tables have rows
		while(row >= table.getRowCount()){
			table.addRow("");
		}
	}
	
	
	/**
	 * Insert InternalCondition data to a table. This information can be extracted
	 * by EOPTableExtractor. 
	 * 
	 * table is supposed to contain necessary columns. Column head should contain
	 * component name. 
	 * 
	 * @param term
	 * @param table
	 */
	public static void insertInternalConditionFromActionToTable(Action action, BasicTable table){
		
		//check input
		if(action == null || table == null){
			return;
		}
		
		setUp(action, table);
		
		//add actuator values
		for(ActuatorValue actVal : action.getActuatorValue()){			
			col = table.findColumn(actVal.getActuator());
			if(-1 != col){
				table.setValueAt(actVal.getValue(), row, col);
			}
		}
		
		//add sensor values
		for(SensorValue sensVal : action.getSensorValue()){			
			col = table.findColumn(sensVal.getSensor());
			if(-1 != col){
				table.setValueAt(sensVal.getValue(), row, col);
			}
		}
		
		//add variable values
		for(VariableValue varVal : action.getVariableValue()){			
			col = table.findColumn(varVal.getVariable());
			if(-1 != col){
				table.setValueAt(varVal.getValue(), row, col);
			}
		}
	}
	
	/**
	 * Does nothing only initial state contains information about external components.
	 * 
	 * @param term
	 * @param table
	 */
	public static void insertExternalConditionFromActionToTable(Action action, BasicTable table){
		//check input
		if(action == null || table == null){
			return;
		}
		
		setUp(action, table);
		
		/*
		for(ExternalComponentValue extCompVal : action.){
			col = table.findColumn(extCompVal.getExternalComponent().getComponent());
			table.setValueAt(extCompVal.getValue(), row, col);
		}
		*/
	}
	
	/**
	 * Insert ExternalCondition data to a table. This information can be extracted
	 * by EOPTableExtractor. 
	 * 
	 * table is supposed to contain necessary columns. Column head should contain
	 * component name. 
	 * 
	 * @param term
	 * @param table
	 */
	public static void insertExternalConditionFromInitialToTable(List<ExternalComponentValue> extCompValList, BasicTable table){
		//check input
		if(extCompValList == null || table == null){
			return;
		}
		
		row = 1;
		
		for(ExternalComponentValue extCompVal : extCompValList){
			col = table.findColumn(extCompVal.getExternalComponent().getComponent());
			table.setValueAt(extCompVal.getValue(), row, col);
		}
	}
	
	
	/**
	 * Insert ZoneCondition data to a table. This information can be extracted
	 * by EOPTableExtractor. 
	 * 
	 * table is supposed to have two columns.
	 * 
	 * @param term
	 * @param table
	 */
	public static void insertZoneConditionFromActionToTable(Action action, BasicTable table){
		//check input
		if(action == null || table == null){
			return;
		}
		
		setUp(action, table);
		
	
		for(ZoneState val : action.getZoneState()){
			col = table.findColumn(val.getZone());
			table.setValueAt(val.getState(), row, col);
		}
		
	}
	
	/**
	 * Insert ZoneCondition data to a table. This information can be extracted
	 * by EOPTableExtractor. 
	 * 
	 * table is supposed to have two columns.
	 * 
	 * @param term
	 * @param table
	 */
	public static void insertIdFromActionToTable(Action action, BasicTable table){
		//check input
		if(action == null || table == null){
			return;
		}
		
		setUp(action, table);
		
	    col = 0;
		table.setValueAt(action.getId(), row, col);
		
	}
	
	
	
	
	
	
	
}
