/*
 * Supremica Software License Agreement
 *
 * The Supremica software is not in the public domain
 * However, it is freely available without fee for education,
 * research, and non-profit purposes.  By obtaining copies of
 * this and other files that comprise the Supremica software,
 * you, the Licensee, agree to abide by the following
 * conditions and understandings with respect to the
 * copyrighted software:
 *
 * The software is copyrighted in the name of Supremica,
 * and ownership of the software remains with Supremica.
 *
 * Permission to use, copy, and modify this software and its
 * documentation for education, research, and non-profit
 * purposes is hereby granted to Licensee, provided that the
 * copyright notice, the original author's names and unit
 * identification, and this permission notice appear on all
 * such copies, and that no charge be made for such copies.
 * Any entity desiring permission to incorporate this software
 * into commercial products or to use it for commercial
 * purposes should contact:
 *
 * Knut Akesson (KA), knut@supremica.org
 * Supremica,
 * Knarrhogsgatan 10
 * SE-431 60 MOLNDAL
 * SWEDEN
 *
 * to discuss license terms. No cost evaluation licenses are
 * available.
 *
 * Licensee may not use the name, logo, or any other symbol
 * of Supremica nor the names of any of its employees nor
 * any adaptation thereof in advertising or publicity
 * pertaining to the software without specific prior written
 * approval of the Supremica.
 *
 * SUPREMICA AND KA MAKES NO REPRESENTATIONS ABOUT THE
 * SUITABILITY OF THE SOFTWARE FOR ANY PURPOSE.
 * IT IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED WARRANTY.
 *
 * Supremica or KA shall not be liable for any damages
 * suffered by Licensee from the use of this software.
 *
 * Supremica is owned and represented by KA.
 */

/**
 * The ControlSystemDataBuilder class is used to load a Factory
 * application, defined by XML- files, into a Java-representation. This medium-level 
 * Java-representation can then be transformed to different PLC programs.
 *
 *
 * Created: Mon Dec  05 13:49:32 2005
 *
 * @author Oscar
 * @version 1.0
 */
package org.supremica.manufacturingTables.management;

import java.util.Iterator;
import java.util.List;
import java.io.*;
import javax.xml.bind.*;
import org.supremica.manufacturingTables.xsd.factory.*;
import org.supremica.manufacturingTables.xsd.eop.*;
import org.supremica.manufacturingTables.management.*;
import org.supremica.automationobjects.xsd.libraryelement.*;
import net.sourceforge.fuber.xsd.libraryelement.*;
import org.supremica.manufacturingTables.controlsystemdata.*;

public class ControlSystemDataBuilder
{

    // Variable to keep track of the indentation, just for now for printing the XML code
    private int nbrOfBlanks;
    private String blanks;
    private ManufacturingCell manufacturingCell;

    public ControlSystemDataBuilder()
    {
	nbrOfBlanks = 0;
	blanks = null;
	manufacturingCell = null;
    }
    
    public ManufacturingCell getManufacturingCell()
    {
	return manufacturingCell;
    }

    public void buildEOP(OperationType operation)
    {
	// Variable to keep track of the indentation, just for now for printing the XML code
	nbrOfBlanks = 0;
	blanks = "                                                                              ";

	// Operation
	System.err.println(blanks.substring(0,nbrOfBlanks) + "<Operation OpID=\"" + operation.getOpID().intValue() + "\">"); 
	nbrOfBlanks++;

	// Create EOPData	
	EOPData EOPData = new EOPData(operation.getOpID().intValue(), operation.getType());

	System.err.println(blanks.substring(0,nbrOfBlanks) + "<Type>" + operation.getType() + "</Type>");

	if (operation.getComment()!=null)
	    {
		EOPData.setComment(operation.getComment());
		System.err.println(blanks.substring(0,nbrOfBlanks) + "<Comment>" + operation.getComment() + "</Comment>"); 
	    }
	
	// Register EOP to the correct machine
	String machineName = manufacturingCell.getName().substring(manufacturingCell.getName().length()-3, manufacturingCell.getName().length()) + operation.getMachine(); 
	// (The EOP has shorter names not containing the last of the cell name)
	
	if (manufacturingCell.getMachine(machineName)==null)
	    {
		System.err.println("The machine " + machineName + " was not found!");
		return;
	    }
	manufacturingCell.getMachine(machineName).registerEOP(EOPData);
	System.err.println(blanks.substring(0,nbrOfBlanks) + "<Machine>" + operation.getMachine() + "</Machine>"); 
	
	// EOP
	EOPType EOPType = operation.getEOP();
	System.err.println(blanks.substring(0,nbrOfBlanks) + "<EOP>"); 
	nbrOfBlanks++;


	// At the moment no control is made to check that the internal and external components correspond
	// to the ones used in the initial state and the action.

	// InternalComponents
	InternalComponentsType internalComponents = EOPType.getInternalComponents();
	System.err.println(blanks.substring(0,nbrOfBlanks) + "<InternalComponents>"); 
	nbrOfBlanks++;
	for (Iterator actuatorIter = internalComponents.getActuator().iterator(); actuatorIter.hasNext();)
	    {
		System.err.println(blanks.substring(0,nbrOfBlanks) + "<Actuator>" + (String) actuatorIter.next() + "</Actuator>"); 
	    }
	for (Iterator sensorIter = internalComponents.getSensor().iterator(); sensorIter.hasNext();)
	    {
		System.err.println(blanks.substring(0,nbrOfBlanks) + "<Sensor>" + (String) sensorIter.next() + "</Sensor>"); 
	    }
	nbrOfBlanks--;
	System.err.println(blanks.substring(0,nbrOfBlanks) + "</InternalComponents>"); 

	// ExternalComponents
	ExternalComponentsType externalComponents = EOPType.getExternalComponents();
	System.err.println(blanks.substring(0,nbrOfBlanks) + "<ExternalComponents>"); 
	nbrOfBlanks++;
	for (Iterator variableIter = externalComponents.getExternalVariable().iterator(); variableIter.hasNext();)
	    {
		System.err.println(blanks.substring(0,nbrOfBlanks) + "<ExternalVariable>" + (String) variableIter.next() + "</ExternalVariable>"); 
	    }
	nbrOfBlanks--;
	System.err.println(blanks.substring(0,nbrOfBlanks) + "</ExternalComponents>"); 
	
	// InitialState
	InitialStateType initialState = EOPType.getInitialState();
	System.err.println(blanks.substring(0,nbrOfBlanks) + "<InitialState>"); 
	nbrOfBlanks++;

	// Create and set InitialRow
	EOPInitialRowData initialRow = new EOPInitialRowData(); 
	EOPData.setEOPInitialRow(initialRow);

	//   ActuatorValue
	for (Iterator actuatorIter = initialState.getActuatorValue().iterator(); actuatorIter.hasNext();)
	    {
		System.err.println(blanks.substring(0,nbrOfBlanks) + "<ActuatorValue>"); 
		nbrOfBlanks++;
		ActuatorValueType actuatorValue = (ActuatorValueType) actuatorIter.next();
		System.err.println(blanks.substring(0,nbrOfBlanks) + "<Actuator>" + actuatorValue.getActuator() + "</Actuator>"); 
		System.err.println(blanks.substring(0,nbrOfBlanks) + "<Value>" + actuatorValue.getValue() + "</Value>"); 
		nbrOfBlanks--;
		System.err.println(blanks.substring(0,nbrOfBlanks) + "</ActuatorValue>"); 

		// Add actuatorToState
		String actuatorName = machineName.substring( machineName.length()-3, machineName.length() ) + actuatorValue.getActuator(); 
		// (The EOP has shorter actuator names not containing the last of the machine name)
		initialRow.addActuatorToState(actuatorName, actuatorValue.getValue());
	    }
	
	//   SensorValue
	for (Iterator sensorIter = initialState.getSensorValue().iterator(); sensorIter.hasNext();)
	    {
		System.err.println(blanks.substring(0,nbrOfBlanks) + "<SensorValue>"); 
		nbrOfBlanks++;
		SensorValueType sensorValue = (SensorValueType) sensorIter.next();
		System.err.println(blanks.substring(0,nbrOfBlanks) + "<Sensor>" + sensorValue.getSensor() + "</Sensor>"); 
		System.err.println(blanks.substring(0,nbrOfBlanks) + "<Value>" + sensorValue.getValue() + "</Value>"); 
		nbrOfBlanks--;
		System.err.println(blanks.substring(0,nbrOfBlanks) + "</SensorValue>"); 

		// Add sensorToState
		String sensorName = machineName.substring( machineName.length()-3, machineName.length() ) + sensorValue.getSensor(); 
		// (The EOP has shorter sensor names not containing the last of the machine name)
		initialRow.addSensorToState(sensorName, sensorValue.getValue());
	    }
	
	//   ExternalVariableValue
	for (Iterator variableIter = initialState.getExternalVariableValue().iterator(); variableIter.hasNext();)
	    {
		System.err.println(blanks.substring(0,nbrOfBlanks) + "<ExternalVariableValue>"); 
		nbrOfBlanks++;
		ExternalVariableValueType externalVariableValue = (ExternalVariableValueType) variableIter.next();
		System.err.println(blanks.substring(0,nbrOfBlanks) + "<ExternalVariable>" + externalVariableValue.getExternalVariable() + "</ExternalVariable>"); 
		System.err.println(blanks.substring(0,nbrOfBlanks) + "<Value>" + externalVariableValue.getValue() + "</Value>"); 
		nbrOfBlanks--;
		System.err.println(blanks.substring(0,nbrOfBlanks) + "</ExternalVariableValue>"); 

		// Add externalVariableToState
		initialRow.addExternalVariableToState(externalVariableValue.getExternalVariable(), externalVariableValue.getValue());
	    }
	
	//   InitialStateCheck
	System.err.println(blanks.substring(0,nbrOfBlanks) + "<InitialStateCheck>"); 
	nbrOfBlanks++;
	InitialStateCheckType initialStateCheck = initialState.getInitialStateCheck();
	System.err.println(blanks.substring(0,nbrOfBlanks) + "<AlarmType>" + initialStateCheck.getAlarmType() + "</AlarmType>"); 
	
	// Set AlarmType
	initialRow.setAlarmType(initialStateCheck.getAlarmType());

	System.err.println(blanks.substring(0,nbrOfBlanks) + "<AlarmDelay>" + initialStateCheck.getAlarmDelay() + "</AlarmDelay>"); 

	// Set AlarmDelay
	initialRow.setAlarmDelay(initialStateCheck.getAlarmDelay());

	nbrOfBlanks--;
	System.err.println(blanks.substring(0,nbrOfBlanks) + "</InitialStateCheck>"); 

	nbrOfBlanks--;
	System.err.println(blanks.substring(0,nbrOfBlanks) + "</InitialState>"); 


 	// Actions
 	for (Iterator actionIter = EOPType.getAction().iterator(); actionIter.hasNext();)
 	    {
 		ActionType action = (ActionType) actionIter.next();
 		System.err.println(blanks.substring(0,nbrOfBlanks) + "<Action ActionNbr=\"" + action.getActionNbr().intValue() + "\">"); 
		nbrOfBlanks++;

		// Create and add ActionRow
		EOPActionRowData actionRow = new EOPActionRowData(); 
		EOPData.addEOPActionRow(actionRow);

		//   ActuatorValue
		for (Iterator actuatorIter = action.getActuatorValue().iterator(); actuatorIter.hasNext();)
		    {
			System.err.println(blanks.substring(0,nbrOfBlanks) + "<ActuatorValue>"); 
			nbrOfBlanks++;
			ActuatorValueType actuatorValue = (ActuatorValueType) actuatorIter.next();
			System.err.println(blanks.substring(0,nbrOfBlanks) + "<Actuator>" + actuatorValue.getActuator() + "</Actuator>"); 
			System.err.println(blanks.substring(0,nbrOfBlanks) + "<Value>" + actuatorValue.getValue() + "</Value>"); 
			nbrOfBlanks--;
			System.err.println(blanks.substring(0,nbrOfBlanks) + "</ActuatorValue>"); 
	
			// Add actuatorToState
			String actuatorName = machineName.substring( machineName.length()-3, machineName.length() ) + actuatorValue.getActuator(); 
			// (The EOP has shorter actuator names not containing the last of the machine name)
			actionRow.addActuatorToState(actuatorName, actuatorValue.getValue());
		    }
	
		//   SensorValue
		for (Iterator sensorIter = action.getSensorValue().iterator(); sensorIter.hasNext();)
		    {
			System.err.println(blanks.substring(0,nbrOfBlanks) + "<SensorValue>"); 
			nbrOfBlanks++;
			SensorValueType sensorValue = (SensorValueType) sensorIter.next();
			System.err.println(blanks.substring(0,nbrOfBlanks) + "<Sensor>" + sensorValue.getSensor() + "</Sensor>"); 
			System.err.println(blanks.substring(0,nbrOfBlanks) + "<Value>" + sensorValue.getValue() + "</Value>"); 
			nbrOfBlanks--;
			System.err.println(blanks.substring(0,nbrOfBlanks) + "</SensorValue>"); 

			// Add sensorToState
			String sensorName = machineName.substring( machineName.length()-3, machineName.length() ) + sensorValue.getSensor(); 
			// (The EOP has shorter sensor names not containing the last of the machine name)
			actionRow.addSensorToState(sensorName, sensorValue.getValue());
		    }
		
		//   Zones
		System.err.println(blanks.substring(0,nbrOfBlanks) + "<Zones>" + action.getZones()  + "<Zones>"); 
		
		// Add bookingZones and unbookingZones in private method. 
		if ( !parseZoneString( action.getZones(), actionRow ) )
		    {
			return;
		    }
		
		nbrOfBlanks--;
		System.err.println(blanks.substring(0,nbrOfBlanks) + "</Action>"); 
	    }
	
	nbrOfBlanks--;
	System.err.println(blanks.substring(0,nbrOfBlanks) + "</EOP>"); 
	
	nbrOfBlanks--;
	System.err.println(blanks.substring(0,nbrOfBlanks) + "</Operation>"); 
    }
    
    private boolean parseZoneString( String zoneString, EOPActionRowData actionRow)
    { 
	// Add bookingZones and unbookingZones. For now I do not accept both booking and unbooking 
	// zones at the same row.
	boolean bookingZones = false;
	boolean unbookingZones = false;
	char previousChar = ')'; 
	// I allways check that the current character is the expected compared to the previous.
	// It is OK with a new booking or unbooking substring after the previuos one has 
	// ended (ends with an ')').
	
	// A zone string typically looks like "u(1,2,20)" or "b(5,12)". It is OK with
	// string like  "u(1,20)b(5,12)" and whitespaces between digits and tokens
	// are also OK, but ignored. 
	for (int i = 0; i < zoneString.length(); i++)
	    {
		// "-" means no booking or unbooking
		if ( zoneString.charAt(i) == '-' )
		    {
			break;
		    }
		
		// booking zones
		else if ( zoneString.charAt(i) == 'b' && previousChar == ')' )
		    {
			bookingZones = true;
		    }
		// unbooking zones
		else if ( zoneString.charAt(i) == 'u' && previousChar == ')' )
		    {
			unbookingZones = true;
		    }
		
		// left parenthesis
		else if ( zoneString.charAt(i) == '(' && ( previousChar == 'u' || previousChar == 'b' ) )
		    {
			// This is OK as expected but nothing happens
		    }			
		// right parenthesis
		else if ( zoneString.charAt(i)  == ')' && Character.isDigit(previousChar) )
		    {
			unbookingZones = false;
			bookingZones = false;
		    }		
		// comma
		else if ( zoneString.charAt(i) == ',' && Character.isDigit(previousChar) )
		    {
			// This is OK as expected but nothing happens
		    }	
		// A number with one or more digits
		else if ( Character.isDigit( zoneString.charAt(i) ) && ( previousChar == ',' || previousChar == '(' ) )
		    {
			String number = zoneString.substring(i, i+1); 
			while ( i < zoneString.length()-1 && Character.isDigit( zoneString.charAt(i+1) ) )
			    {
				i++;
				number += zoneString.substring(i, i+1);
			    }
			// Add booking zone
			if ( bookingZones )
			    {
				actionRow.addBookingZone(number);
			    }
			// Add unbooking zone
			else if ( unbookingZones )
			    {
				actionRow.addUnbookingZone(number);
			    }
			else
			    {
				System.err.println("The zone booking/unbooking string was incorrect!");
				return false;
			    }
		    }
		// White space
		else if ( zoneString.charAt(i) == ' ' )
		    {
			// Shall not make this the previous character since it is not important
			continue;
		    }
		else
		    {
			System.err.println("The zone booking/unbooking string was incorrect!");
			return false;
		    }
		previousChar = zoneString.charAt(i);
	    }
	return true;
    }
    
    public void buildPLCData(FactoryType factory)
    {

	// Variable to keep track of the indentation, just for now for printing the XML code
	nbrOfBlanks = 0;
	blanks = "                                                                              ";

	// Factory
	System.err.println(blanks.substring(0,nbrOfBlanks) + "<Factory name=\"" + factory.getName() + "\">");

	if(factory.getDescription()!=null)
	    {
		System.err.println(blanks.substring(0,nbrOfBlanks) + "<Description>" + factory.getDescription() + "</Description>");
	    }

	// Areas
	nbrOfBlanks++;
	AreasType areas = factory.getAreas();
	System.err.println(blanks.substring(0,nbrOfBlanks) + "<Areas>");

	// Nbr of Areas
	List areaList = areas.getArea();
	nbrOfBlanks++;

	// For now the controlprogram is for just one cell and I assume that the Factory only
	// contains one cell

	for (Iterator areaIter = areaList.iterator();areaIter.hasNext();)
	    {
		AreaType currentArea = (AreaType) areaIter.next();
		System.err.println(blanks.substring(0,nbrOfBlanks) + "<Area name=\"" + currentArea.getName() + "\">");
		if(currentArea.getDescription()!=null)
		    {
			System.err.println(blanks.substring(0,nbrOfBlanks) + "<Description>" + currentArea.getDescription() + "</Description>");
		    }
		// Cells
		nbrOfBlanks++;
		CellsType cells = currentArea.getCells();
		System.err.println(blanks.substring(0,nbrOfBlanks) + "<Cells>");

		// Nbr of Cells
		List cellList = cells.getCell();
		nbrOfBlanks++;

		for (Iterator cellIter = cellList.iterator();cellIter.hasNext();)
		    {
			CellType currentCell = (CellType) cellIter.next();
			System.err.println(blanks.substring(0,nbrOfBlanks) + "<Cell name=\"" + currentCell.getName() + "\">");
			nbrOfBlanks++;

			//Creating the ManufacturingCell
			manufacturingCell = new ManufacturingCell(currentCell.getName(), new Coordinator(), new Mailbox());


			// Description
			if(currentCell.getDescription()!=null)
			    {
				System.err.println(blanks.substring(0,nbrOfBlanks) + "<Description>" + currentCell.getDescription() + "</Description>");
				manufacturingCell.setDescription(currentCell.getDescription());
			    }
			
			// Machines
			buildMachines(currentCell.getMachines(), manufacturingCell);

			nbrOfBlanks--;
			System.err.println(blanks.substring(0,nbrOfBlanks) + "</Cell>");
		    }
		nbrOfBlanks--;
		System.err.println(blanks.substring(0,nbrOfBlanks) + "</Cells>");
		nbrOfBlanks--;
		System.err.println(blanks.substring(0,nbrOfBlanks) + "</Area>");

	    }
	nbrOfBlanks--;
	System.err.println(blanks.substring(0,nbrOfBlanks) + "</Areas>");
	nbrOfBlanks--;
	System.err.println(blanks.substring(0,nbrOfBlanks) + "</Factory>");

    }

    private void buildMachines(MachinesType machines, ManufacturingCell manufacturingCell)
    {
	System.err.println(blanks.substring(0,nbrOfBlanks) + "<Machines>");

	// Nbr of Machines
	List machineList = machines.getMachine();
	nbrOfBlanks++;
	for (Iterator machineIter = machineList.iterator();machineIter.hasNext();)
	    {
		buildMachine((MachineType) machineIter.next(), manufacturingCell);
	    }
	nbrOfBlanks--;
	System.err.println(blanks.substring(0,nbrOfBlanks) + "</Machines>");
    }

    private void buildMachine(MachineType machineType, ManufacturingCell manufacturingCell)
    {
	System.err.println(blanks.substring(0,nbrOfBlanks) + "<Machine machineType=\"" + machineType.getType() + "\" " + "name=\"" + machineType.getName() + "\">");
	nbrOfBlanks++;

	// Create Machine, MachineController and Mailbox and add the Machine to the cell
	MachineController machineController = new MachineController();
	Mailbox mailbox = new Mailbox();
	org.supremica.manufacturingTables.controlsystemdata.Machine machine = new org.supremica.manufacturingTables.controlsystemdata.Machine(machineType.getName(), machineType.getType(), machineController, mailbox); 
	
	manufacturingCell.addMachine(machine);

	// Description
	if (machineType.getDescription()!=null)
	    {
		System.err.println(blanks.substring(0,nbrOfBlanks) + "<Description>" + machineType.getDescription() + "</Description>");
		machine.setDescription(machineType.getDescription());
	    }

	// Variables
	if (machineType.getVariables()!=null)
	    {
		buildVariables(machineType.getVariables(), machine);
	    }
	
	// Equipment
	if (machineType.getEquipment()!=null)
	    {
		buildEquipment(machineType.getEquipment(), machine);
	    }
	
	nbrOfBlanks--;
	System.err.println(blanks.substring(0,nbrOfBlanks) + "</Machine>");
    }

    private void buildVariables(VariablesType variables, org.supremica.manufacturingTables.controlsystemdata.Machine machine)
    {
	System.err.println(blanks.substring(0,nbrOfBlanks) + "<Variables>");
	
	// Nbr of Variables
	List variableList = variables.getVariable();
	nbrOfBlanks++;
	for (Iterator variableIter = variableList.iterator(); variableIter.hasNext(); )
	    {
		VariableType variableType = (VariableType) variableIter.next();

		System.err.println(blanks.substring(0,nbrOfBlanks) + "<Variable name=\"" + variableType.getName() + "\">");		
		org.supremica.manufacturingTables.controlsystemdata.Variable variable = new org.supremica.manufacturingTables.controlsystemdata.Variable(variableType.getName());
		
		ValuesType values = variableType.getValues();
		nbrOfBlanks++;
		System.err.println(blanks.substring(0,nbrOfBlanks) + "<Values>");
		
		// Nbr of Values
		List valueList = values.getValue();
		nbrOfBlanks++;
		for (Iterator valueIter = valueList.iterator(); valueIter.hasNext(); )
		    {
			// Since there is a class Value this XML simpletype should be represented by a Value object,
			// but it is a String.
			// In JABX version 2.0 there will be an opportunity to set mapSimpleTypeDef="true" and that will
			// hopefully sort this out
			//Value value = (Value) valueIter.next();
			String value = (String) valueIter.next();
			
			//System.err.println(blanks.substring(0,nbrOfBlanks) + "<Value>" + value.getValue() + "</Value>");
			System.err.println(blanks.substring(0,nbrOfBlanks) + "<Value>" + value + "</Value>");
			
			variable.addValue(value);
		    }
		nbrOfBlanks--;
		System.err.println(blanks.substring(0,nbrOfBlanks) + "</Values>");
		
		machine.addVariable(variable);

		nbrOfBlanks--;
		System.err.println(blanks.substring(0,nbrOfBlanks) + "</Variable>");
	    }
	nbrOfBlanks--;
	System.err.println(blanks.substring(0,nbrOfBlanks) + "</Variables>");
    }

    // The EquipmentContainer (interface) upperLevelEquipment could be either a machine 
    // (org.supremica.manufacturingTables.controlsystemdata.Machine)
    // or an Actuator and the current Equipment should be added to the corresponding 
    // upperLevelEquipment Equipment or Machine.
    private void buildEquipment(EquipmentType equip, EquipmentContainer upperLevelEquipment)
    {
	System.err.println(blanks.substring(0,nbrOfBlanks) + "<Equipment>");

	// Nbr of EquipmentEntities
	List equipList = equip.getEquipmentEntity();
	nbrOfBlanks++;
	for (Iterator equipIter = equipList.iterator();equipIter.hasNext();)
	    {
		EquipmentEntityType currentEquip = (EquipmentEntityType) equipIter.next();
		buildEquipmentEntity(currentEquip, upperLevelEquipment);
	    }
	nbrOfBlanks--;
	System.err.println(blanks.substring(0,nbrOfBlanks) + "</Equipment>");
    }
    
    // The EquipmentContainer upperLevelEquipment could be either a machine 
    // (org.supremica.manufacturingTables.controlsystemdata.Machine)
    // or an Actuator and the current Equipment should be added to the corresponding Equipment or Machine.
    private void buildEquipmentEntity(EquipmentEntityType equipEnt, EquipmentContainer upperLevelEquipment)
    {
	org.supremica.manufacturingTables.controlsystemdata.Equipment equipment = null;
	// Sensors and Actuators are separated.
	if (equipEnt.getType().equals("Sensor"))
	    {
		equipment = new org.supremica.manufacturingTables.controlsystemdata.Sensor(equipEnt.getName());
		upperLevelEquipment.addSensor((org.supremica.manufacturingTables.controlsystemdata.Sensor) equipment);
	    }
	
	else if (equipEnt.getType().equals("Actuator"))
	    {
		equipment = new org.supremica.manufacturingTables.controlsystemdata.Actuator(equipEnt.getName());
		upperLevelEquipment.addActuator((org.supremica.manufacturingTables.controlsystemdata.Actuator) equipment);
	    }
	else
	    {
		System.err.println("The EquipmentEntity is neither a sensor nor an actuator!!!!");
		return;
	    }
	
	// The following should be performed fore both sensors and actuators
	
	System.err.println(blanks.substring(0,nbrOfBlanks) + "<EquipmentEntity equipmentType=\"" + equipEnt.getType() + "\" " + "name=\"" + equipEnt.getName() + "\">");
	nbrOfBlanks++;
	
	
	// Description
	if (equipEnt.getDescription()!=null)
	    {
		System.err.println(blanks.substring(0,nbrOfBlanks) + "<Description>" + equipEnt.getDescription() + "</Description>");
		equipment.setDescription(equipEnt.getDescription());
		
	    }
	
	
	// States
	StatesType states = equipEnt.getStates();
	System.err.println(blanks.substring(0,nbrOfBlanks) + "<States>");
	// Nbr of States
	List stateList = states.getState();
	nbrOfBlanks++;
	for (Iterator stateIter = stateList.iterator();stateIter.hasNext();)
	    {
		// Since there is a class State this XML simpletype should be represented by a State object,
		// but it is a String, as for the Value above.
		//State currentState = (State) stateIter.next();
		String currentState = (String) stateIter.next();
		//System.err.println(blanks.substring(0,nbrOfBlanks) + "<State>" + currentState.getValue() + "</State>");
		System.err.println(blanks.substring(0,nbrOfBlanks) + "<State>" + currentState + "</State>");
		
		equipment.addState(currentState);
	    }
	nbrOfBlanks--;
	System.err.println(blanks.substring(0,nbrOfBlanks) + "</States>");
	
	// Elements (Optional for an actuator, a sensor must have elements or lower level sensors. This i 
	// however not checked here.)
	if (equipEnt.getElements()!=null)
	    {
		System.err.println(blanks.substring(0,nbrOfBlanks) + "<Elements>");
		
		// Nbr of Elements
		// It may be hard to imagine an EquipmentEntity having more than one Elements 
		// but for now I divide it this way.
		List elementList = equipEnt.getElements().getElement();
		nbrOfBlanks++;
		for (Iterator elementIter = elementList.iterator();elementIter.hasNext();)
		    {
			ElementType currentElement = (ElementType) elementIter.next();
			printElement(currentElement);
			equipment.addHardwareConnection(currentElement.getName());
		    }
		nbrOfBlanks--;
		System.err.println(blanks.substring(0,nbrOfBlanks) + "</Elements>");
	    }
	
	// Lower level Equipment, could be possible even for sensors consisting of sensors, but not 
	// sensors consisting of actuators. This is however not checked here.
	if (equipEnt.getEquipment()!=null)
	    {
		buildEquipment(equipEnt.getEquipment(), equipment);
	    }
	
	nbrOfBlanks--;
	System.err.println(blanks.substring(0,nbrOfBlanks) + "</EquipmentEntity>");
    }
    
    
    // For printing the element data. 
    private void printElement(ElementType element)
    {
	System.err.println(blanks.substring(0,nbrOfBlanks) + "<Element name=\"" + element.getName() + "\">");
	nbrOfBlanks++;
	
	// Description, this is not added to the ControlSystemData
	if (element.getDescription()!=null)
	    {
		System.err.println(blanks.substring(0,nbrOfBlanks) + "<Description>" + element.getDescription() + "</Description>");
	    }

	nbrOfBlanks--;
	System.err.println(blanks.substring(0,nbrOfBlanks) + "</Element>");
    }

}
