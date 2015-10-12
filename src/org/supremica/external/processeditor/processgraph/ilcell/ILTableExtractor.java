package org.supremica.external.processeditor.processgraph.ilcell;

import java.math.BigInteger;

import org.supremica.external.processeditor.processgraph.table.BasicTable;
import org.supremica.manufacturingTables.xsd.il.ActuatorValue;
import org.supremica.manufacturingTables.xsd.il.AfterZones;
import org.supremica.manufacturingTables.xsd.il.BeforeZones;
import org.supremica.manufacturingTables.xsd.il.ExternalComponent;
import org.supremica.manufacturingTables.xsd.il.ExternalComponentValue;
import org.supremica.manufacturingTables.xsd.il.ExternalComponents;
import org.supremica.manufacturingTables.xsd.il.InternalComponents;
import org.supremica.manufacturingTables.xsd.il.NotOngoing;
import org.supremica.manufacturingTables.xsd.il.NotStarted;
import org.supremica.manufacturingTables.xsd.il.ObjectFactory;
import org.supremica.manufacturingTables.xsd.il.OperationCheck;
import org.supremica.manufacturingTables.xsd.il.Operations;
import org.supremica.manufacturingTables.xsd.il.Products;
import org.supremica.manufacturingTables.xsd.il.SensorValue;
import org.supremica.manufacturingTables.xsd.il.Term;
import org.supremica.manufacturingTables.xsd.il.VariableValue;
import org.supremica.manufacturingTables.xsd.il.ZoneCheck;
import org.supremica.manufacturingTables.xsd.il.Zones;

public class ILTableExtractor {

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
	 */
	public static ExternalComponents
		getExternalComponentsFromTable(final BasicTable table)
	{
		final ExternalComponents extComponents = factory.createExternalComponents();
		ExternalComponent extComp = factory.createExternalComponent();

		final int numberOfComponents = table.getColumnCount();

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
		getInternalComponentsFromTable(final BasicTable table)
	{
		final InternalComponents intComponents = factory.createInternalComponents();
		final int numberOfComponents = table.getColumnCount();
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
	 */
	public static Zones
		getZonesFromTable(final BasicTable table)
	{
		String tmp = "";
		String strings[] = null;

		final Zones zones = factory.createZones();

		final int numberOfColumns = table.getColumnCount();
		final int numberOfRows = table.getRowCount();

		//search all rows and columns, except first row
		for(int col = 0; col < numberOfColumns; col++){
			for(int row = 1; row < numberOfRows; row++){

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
		getOperationsFromTable(final BasicTable table)
	{
		String tmp = "";
		String strings[] = null;

		final Operations ops = factory.createOperations();

		final int numberOfColumns = table.getColumnCount();
		final int numberOfRows = table.getRowCount();

		//search all rows and columns, except first row
		for(int col = 0; col < numberOfColumns; col++){
			for(int row = 1; row < numberOfRows; row++){

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

	public static Term[] getTerms(final BasicTable tableMode,
								  final BasicTable tableInternal,
					   			  final BasicTable tableExternal,
					   			  final BasicTable tableOperation,
					   			  final BasicTable tableZone,
					   			  final BasicTable tableProduct)
	{

		int col = -1;
		int row = -1;

		final ObjectFactory factory = new ObjectFactory();

		ActuatorValue actVal = null;
		SensorValue senVal = null;
		VariableValue varVal = null;

		ExternalComponentValue extCompVal = null;
		ExternalComponent extComp = null;


		int numberOfRows = -1;
		int numberOfColumns = -1;

		//all tables have the same numbers of rows
		if(null != tableInternal){
			numberOfRows = tableInternal.getRowCount();
		}else if(null != tableExternal){
			numberOfRows = tableExternal.getRowCount();
		}else if(null != tableOperation){
			numberOfRows = tableOperation.getRowCount();
		}else if(null != tableZone){
			numberOfRows = tableZone.getRowCount();
		}else if(null != tableProduct){
			numberOfRows = tableProduct.getRowCount();
		}else if(null != tableMode){
			numberOfRows = tableMode.getRowCount();
		}

		//create all terms
		//first row contains additional information
		final Term[] terms = new Term[numberOfRows - 1];
		for(int i = 0; i < terms.length; i++){
			terms[i] = factory.createTerm();
			terms[i].setRow(BigInteger.valueOf(i));
		}

		//-----------------------------------------------------
		//	Mode information from tableMode
		//-----------------------------------------------------
		if(null != tableMode){
			for(int i = 0; i < terms.length; i++){
				row = i + 1;

				if(null != tableMode.getValueAt(row, 0)){
					terms[i].setMode(tableMode.getValueAt(row, 0).toString().trim());
				}else{
					terms[i].setMode("");
				}
			}
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
					for(int i = 0; i < terms.length; i++){
						row = i + 1;

						actVal = factory.createActuatorValue();
						actVal.setActuator(tableInternal.getColumnName(col));
						actVal.setValue(tableInternal.getValueAt(row, col).toString().trim());

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
						if(null == tableInternal.getValueAt(row, col)){
							senVal.setValue("");
						}else{
							senVal.setValue(tableInternal.getValueAt(row, col).toString().trim());
						}
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
						varVal.setValue(tableInternal.getValueAt(row, col).toString().trim());

						terms[i].getVariableValue().add(varVal);
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


				for(int i = 0; i < terms.length; i++){
					row = i + 1;

					extCompVal = factory.createExternalComponentValue();
					extCompVal.setExternalComponent(extComp);

					extCompVal.setValue(tableExternal.getValueAt(row, col).toString().trim());
					terms[i].getExternalComponentValue().add(extCompVal);
				}
			}
		}


		ZoneCheck zoneCheck = null;
		AfterZones afterZones = null;
		BeforeZones beforeZones = null;

		//-----------------------------------------------------
		//	Zone information from tableZone
		//-----------------------------------------------------
		if(null != tableZone){
			numberOfColumns = tableZone.getColumnCount();
			if(numberOfColumns == 2){
				for(int i = 0; i < terms.length; i++){
					row = i + 1;

					beforeZones = factory.createBeforeZones();
					String[] zones = tableZone.getValueAt(row, 0).toString().split(",");
					for(int z = 0; z < zones.length; z++){
						beforeZones.getZone().add(zones[z].trim());
					}

					afterZones = factory.createAfterZones();
					zones = tableZone.getValueAt(row, 1).toString().split(",");
					for(int z = 0; z < zones.length; z++){
						afterZones.getZone().add(zones[z].trim());
					}

					zoneCheck = factory.createZoneCheck();

					zoneCheck.setBeforeZones(beforeZones);
					zoneCheck.setAfterZones(afterZones);

					terms[i].getZoneCheck().add(zoneCheck);
				}
			}
		}



		OperationCheck opCheck = null;
		NotOngoing notOngoing = null;
		NotStarted notStarted = null;

		//-----------------------------------------------------
		//	Operation information from tableOperation
		//-----------------------------------------------------
		if(null != tableOperation){
			numberOfColumns = tableOperation.getColumnCount();
			if(numberOfColumns == 2){
				for(int i = 0; i < terms.length; i++){
					row = i + 1;

					//not started operations
					notStarted = factory.createNotStarted();
					String[] operations = tableOperation.getValueAt(row, 0).toString().split(",");
					for(int op = 0; op < operations.length; op++){
						notStarted.getOperation().add(operations[op].trim());
					}

					//not ongoing operations
					notOngoing = factory.createNotOngoing();
					operations = tableOperation.getValueAt(row, 1).toString().split(",");
					for(int op = 0; op < operations.length; op++){
						notOngoing.getOperation().add(operations[op].trim());
					}

					opCheck = factory.createOperationCheck();
					opCheck.setNotStarted(notStarted);
					opCheck.setNotOngoing(notOngoing);

					//add to term
					terms[i].getOperationCheck().add(opCheck);
				}
			}
		}


		Products products = null;

		//-----------------------------------------------------
		//	Product information from tableProduct
		//-----------------------------------------------------
		if(null != tableProduct){

			for(int i = 0; i < terms.length; i++){
				row = i + 1;

				//not started operations
				products = factory.createProducts();

				if(null != tableProduct.getValueAt(row, 0)){
					final String[] productNames = tableProduct.getValueAt(row, 0).toString().split(",");
					for(int p = 0; p < productNames.length; p++){
						products.getProduct().add(productNames[p].trim());
					}
				}else{
					products.getProduct().add("");
				}
				terms[i].getProducts().add(products);
			}
		}


		return terms;
	}
}
