package org.supremica.external.processeditor.processgraph.ilcell;

import org.supremica.external.processeditor.processgraph.table.BasicTable;
import org.supremica.manufacturingTables.xsd.il.*;

public class ILTableFiller {
	
	/* final */
	private static final String SEPARATOR = ",";
	private static final String SPACE = " ";
	
	private static final int NOT_STARTED_COL = 0;
	private static final int NOT_ONGOING_COL = 1;
	
	private static final int ZONE_BEFORE_COL = 0;
	private static final int ZONE_AFTER_COL = 1;
	
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
	private static void setUp(Term term, BasicTable table){
		
		row = -1;
		col = -1;
		
		row = term.getRow().intValue();
		row = row + 1; //row 0 contains additional information
		
		//make sure tables have rows
		while(row >= table.getRowCount()){
			table.addRow("");
		}
	}
	
	/**
	 * Insert InternalCondition data to a table. This information can be extracted
	 * by ILTableExtractor. 
	 * 
	 * table is supposed to contain necessary columns. Column head should contain
	 * component name. 
	 * 
	 * @param term
	 * @param table
	 */
	public static void insertModeConditionFromTermToTable(Term term, BasicTable table){
		//check input
		if(term == null || table == null){
			return;
		}
		
		setUp(term, table);
		
		table.setValueAt(term.getMode(), row, 0);
	}
	
	/**
	 * Insert InternalCondition data to a table. This information can be extracted
	 * by ILTableExtractor. 
	 * 
	 * table is supposed to contain necessary columns. Column head should contain
	 * component name. 
	 * 
	 * @param term
	 * @param table
	 */
	public static void insertProductConditionFromTermToTable(Term term, BasicTable table){
		
		String strTmp = "";
		
		//check input
		if(term == null || table == null){
			return;
		}
		
		setUp(term, table);
		
		for(Products products : term.getProducts()){
			if( products.getProduct().size() > 0 ){
				
				strTmp = "";
				for(String product : products.getProduct()){
					strTmp = strTmp.concat(product + SEPARATOR + SPACE);
				}
			
				//remove last separator and space
				strTmp = strTmp.substring(0, strTmp.length() - 2);
			
				//add to table
				table.setValueAt(strTmp, row, 0);
			}
		}
	}
	
	/**
	 * Insert InternalCondition data to a table. This information can be extracted
	 * by ILTableExtractor. 
	 * 
	 * table is supposed to contain necessary columns. Column head should contain
	 * component name. 
	 * 
	 * @param term
	 * @param table
	 */
	public static void insertInternalConditionFromTermToTable(Term term, BasicTable table){
		
		//check input
		if(term == null || table == null){
			return;
		}
		
		setUp(term, table);
		
		//add actuator values
		for(ActuatorValue actVal : term.getActuatorValue()){			
			col = table.findColumn(actVal.getActuator());
			table.setValueAt(actVal.getValue(), row, col);
		}
		
		//add sensor values
		for(SensorValue sensVal : term.getSensorValue()){			
			col = table.findColumn(sensVal.getSensor());
			table.setValueAt(sensVal.getValue(), row, col);
		}
		
		//add variable values
		for(VariableValue varVal : term.getVariableValue()){			
			col = table.findColumn(varVal.getVariable());
			table.setValueAt(varVal.getValue(), row, col);
		}
	}
	
	/**
	 * Insert ExternalCondition data to a table. This information can be extracted
	 * by ILTableExtractor. 
	 * 
	 * table is supposed to contain necessary columns. Column head should contain
	 * component name. 
	 * 
	 * @param term
	 * @param table
	 */
	public static void insertExternalConditionFromTermToTable(Term term, BasicTable table){
		//check input
		if(term == null || table == null){
			return;
		}
		
		setUp(term, table);
		
		for(ExternalComponentValue extCompVal : term.getExternalComponentValue()){
			col = table.findColumn(extCompVal.getExternalComponent().getComponent());
			table.setValueAt(extCompVal.getValue(), row, col);
		}
	}
	
	/**
	 * Insert OperationCondition data to a table. This information can be extracted
	 * by ILTableExtractor. 
	 * 
	 * table is supposed to have two columns.
	 * 
	 * @param term
	 * @param table
	 */
	public static void insertOperationConditionFromTermToTable(Term term, BasicTable table){
		
		//check input
		if(term == null || table == null){
			return;
		}
		
		setUp(term, table);
		
		for(OperationCheck opCheck : term.getOperationCheck()){
			
			String strTmp = "";
			
			//----------------------------------------------------------
			//Not ongoing operations
			//----------------------------------------------------------
			if( opCheck.getNotOngoing().getOperation().size() > 0 ){
				
				strTmp = "";
				for(String operation : opCheck.getNotOngoing().getOperation()){
					strTmp = strTmp.concat(operation + SEPARATOR + SPACE);
				}
			
				//remove last separator and space
				strTmp = strTmp.substring(0, strTmp.length() - 2);
			
				//add to table
				table.setValueAt(strTmp, row, NOT_ONGOING_COL);
			}
			
			
			//-----------------------------------------------------------
			//Not started operations
			//-----------------------------------------------------------
			if( opCheck.getNotStarted().getOperation().size() > 0 ){
				
				strTmp = "";
				for(String operation : opCheck.getNotStarted().getOperation()){
					strTmp = strTmp.concat(operation + SEPARATOR + SPACE);
				}
			
				//remove last separator and space
				strTmp = strTmp.substring(0, strTmp.length() - 2);
			
				//add to table
				table.setValueAt(strTmp, row, NOT_STARTED_COL);
			}
		}
	}
	
	/**
	 * Insert ZoneCondition data to a table. This information can be extracted
	 * by ILTableExtractor. 
	 * 
	 * table is supposed to have two columns.
	 * 
	 * @param term
	 * @param table
	 */
	public static void insertZoneConditionFromTermToTable(Term term, BasicTable table){
		//check input
		if(term == null || table == null){
			return;
		}
		
		setUp(term, table);
		
		
		for(ZoneCheck zoneCheck : term.getZoneCheck()){
			
			String strTmp = "";
			
			//----------------------------------------------------------
			//Before zones
			//----------------------------------------------------------
			if( zoneCheck.getBeforeZones().getZone().size() > 0 ){
				
				strTmp = "";
				for(String operation : zoneCheck.getBeforeZones().getZone()){
					strTmp = strTmp.concat(operation + SEPARATOR + SPACE);
				}
			
				//remove last separator and space
				strTmp = strTmp.substring(0, strTmp.length() - 2);
			
				//add to table
				table.setValueAt(strTmp, row, ZONE_BEFORE_COL);
			}
			
			
			//-----------------------------------------------------------
			//After zones
			//-----------------------------------------------------------
			if( zoneCheck.getAfterZones().getZone().size() > 0 ){
				
				strTmp = "";
				for(String operation : zoneCheck.getAfterZones().getZone()){
					strTmp = strTmp.concat(operation + SEPARATOR + SPACE);
				}
			
				//remove last separator and space
				strTmp = strTmp.substring(0, strTmp.length() - 2);
			
				//add to table
				table.setValueAt(strTmp, row, ZONE_AFTER_COL);
			}
		}
	}
}
