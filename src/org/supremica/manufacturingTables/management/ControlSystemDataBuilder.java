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
import org.supremica.manufacturingTables.xsd.factory.*;
import org.supremica.manufacturingTables.xsd.eop.*;
import org.supremica.manufacturingTables.xsd.rop.*;
//import net.sourceforge.fuber.xsd.libraryelement.*;
import org.supremica.manufacturingTables.controlsystemdata.*;

public class ControlSystemDataBuilder
{

    // Variable to keep track of the indentation, just for now for printing the XML code
    private int nbrOfBlanks;
    private String blanks;
    private ManufacturingCell manufacturingCell;

    public static final char BOOKING_TOKEN = 'b';
    public static final char UNBOOKING_TOKEN = 'u';
    public static final char NOBOOKING_TOKEN = '-';
    public static final char START_EXPR_TOKEN = '(';
    public static final char END_EXPR_TOKEN = ')';
    public static final char DELIMIT_TOKEN = ',';
    public static final char BLANK_TOKEN = ' ';
    
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
    
    public void buildCOP(ROP COP)
    {
	// Variable to keep track of the indentation, just for now for printing the XML code
	nbrOfBlanks = 0;
	blanks = "                                                                              ";
	
	
	// Check ROP Type (ROP or COP)
	if (!COP.getType().value().equals("COP"))
	{
	    System.err.println("This is not a COP!");
	    return;
	}
	
	else 
	{
	    // ROP
	    System.out.println(blanks.substring(0,nbrOfBlanks) + "<ROP id=\"" + COP.getId() + "\" " + "type=\"" + COP.getType() +  "\">"); 
	    nbrOfBlanks++;
	    
	    // Create COPData
	    String cellName = manufacturingCell.getName().substring(manufacturingCell.getName().length()-3, manufacturingCell.getName().length());
	    String machineName = cellName + COP.getMachine(); 
	    // 	(The COP has shorter names not containing the last of the cell name)
	    
	    COPData COPData = new COPData(COP.getId(), machineName);
	    
	    System.out.println(blanks.substring(0,nbrOfBlanks) + "<Machine>" + COP.getMachine() + "</Machine>");
	    
	    // Comment	    
	    if (COP.getComment()!=null)
	    {
		COPData.setComment(COP.getComment());
		System.out.println(blanks.substring(0,nbrOfBlanks) + "<Comment>" + COP.getComment() + "</Comment>"); 
	    }
	    
	    // Register the COP to the manufacturing cell
	    manufacturingCell.registerCOP(COPData);
	    
	    // Relation
	    Relation relation = COP.getRelation();
	    System.out.println(blanks.substring(0,nbrOfBlanks) + "<Relation type=\"" + relation.getType() +  "\">"); 
	    nbrOfBlanks++;
	    
	    // Check the type of the relation
	    if (!relation.getType().value().equals("Sequence"))
	    {
		System.err.println("This is not a sequence!");
	    }
	    else
	    {
		// Activities
		for (Iterator activityIter = relation.getActivity().iterator(); activityIter.hasNext();)
		{
		    // Current Activity
		    Activity activity = (Activity) activityIter.next();
		    // Create COP Activity and add to COPData
		    COPActivity COPActivity = new COPActivity(activity.getOperation());
		    COPData.addCOPActivity(COPActivity);
 
		    System.out.println(blanks.substring(0,nbrOfBlanks) + "<Activity>"); 
		    nbrOfBlanks++;
		    
		    // Precondition
		    Precondition precondition = activity.getPrecondition(); 
		    if (precondition!= null)
		    {
			System.out.println(blanks.substring(0,nbrOfBlanks) + "<Precondition>"); 
			nbrOfBlanks++;
			
			// Predecessors
			for (Iterator predecessorIter = precondition.getPredecessor().iterator(); predecessorIter.hasNext();)
			{
			    // Current Predecessor (are kinds of OperationReferences)
			    OperationReferenceType predecessor = (OperationReferenceType) predecessorIter.next();
			    // Create predecessor and add to COP activity
			    Predecessor predecessorData = new Predecessor( predecessor.getOperation(), cellName + predecessor.getMachine() ); 
			    COPActivity.addPredecessor(predecessorData);

			    System.out.println(blanks.substring(0,nbrOfBlanks) + "<Predecessor>"); 
			    nbrOfBlanks++;
			    
			    // Machine
			    System.out.println(blanks.substring(0,nbrOfBlanks) + "<Machine>" + predecessor.getMachine() + "</Machine>"); 
			    // Operation in predecessor
			    System.out.println(blanks.substring(0,nbrOfBlanks) + "<Operation>" + predecessor.getOperation() + "</Operation>"); 
			    
			    nbrOfBlanks--;
			    System.out.println(blanks.substring(0,nbrOfBlanks) + "</Predecessor>"); 
			}
			
	        	nbrOfBlanks--;
			System.out.println(blanks.substring(0,nbrOfBlanks) + "</Precondition>"); 
			
		    }
		    // Operation in current Machine
		    System.out.println(blanks.substring(0,nbrOfBlanks) + "<Operation>" + activity.getOperation() + "</Operation>");
		    
		    nbrOfBlanks--;
		    System.out.println(blanks.substring(0,nbrOfBlanks) + "</Activity>"); 
		}
		
        	nbrOfBlanks--;
		System.out.println(blanks.substring(0,nbrOfBlanks) + "</Relation>"); 
		
	    }
	    
	    nbrOfBlanks--;
	    System.out.println(blanks.substring(0,nbrOfBlanks) + "</ROP>"); 
	}
    }
    
//     // Hack, that converts the operation name to an int.
//     // If the operation name really was an int, the value will be unchanged.
//     // This is to allow the user to use any operation names in the COPs, but
//     // the Java implementation only allows int operation names.
//     private int operationNameToNumber(String operation)
//     {
// 	int op;
// 	try
// 	{
// 	    op = Integer.parseInt(operation, 10);
// 	}
// 	catch(NumberFormatException ne)
// 	{
// 	    op = operation.hashCode();
// 	}
// 	return op;
//     }
    
    public void buildEOP(org.supremica.manufacturingTables.xsd.eop.Operation operation)
    {
	// Variable to keep track of the indentation, just for now for printing the XML code
	nbrOfBlanks = 0;
	blanks = "                                                                              ";

	// Operation
	//System.out.println(blanks.substring(0,nbrOfBlanks) + "<Operation opID=\"" + operation.getOpID().intValue() + "\">"); 
	nbrOfBlanks++;

	// Create EOPData	
	EOPData EOPData = new EOPData(operation.getOpID(), operation.getType());

	//System.out.println(blanks.substring(0,nbrOfBlanks) + "<Type>" + operation.getType() + "</Type>");

	if (operation.getComment()!=null)
	    {
		EOPData.setComment(operation.getComment());
		//System.out.println(blanks.substring(0,nbrOfBlanks) + "<Comment>" + operation.getComment() + "</Comment>"); 
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
	//System.out.println(blanks.substring(0,nbrOfBlanks) + "<Machine>" + operation.getMachine() + "</Machine>"); 
	
	// EOP
	EOP EOP = operation.getEOP();
	//System.out.println(blanks.substring(0,nbrOfBlanks) + "<EOP>"); 
	nbrOfBlanks++;


	// At the moment no control is made to check that the internal and external components correspond
	// to the ones used in the initial state and the action.

	// InternalComponents
	InternalComponents internalComponents = EOP.getInternalComponents();
	//System.out.println(blanks.substring(0,nbrOfBlanks) + "<InternalComponents>"); 
	nbrOfBlanks++;
	for (Iterator actuatorIter = internalComponents.getActuator().iterator(); actuatorIter.hasNext();)
	    {
		System.out.println(blanks.substring(0,nbrOfBlanks) + "<Actuator>" + (String) actuatorIter.next() + "</Actuator>"); 
	    }
	for (Iterator sensorIter = internalComponents.getSensor().iterator(); sensorIter.hasNext();)
	    {
		System.out.println(blanks.substring(0,nbrOfBlanks) + "<Sensor>" + (String) sensorIter.next() + "</Sensor>"); 
	    }
	for (Iterator variableIter = internalComponents.getVariable().iterator(); variableIter.hasNext();)
	    {
		System.out.println(blanks.substring(0,nbrOfBlanks) + "<Variable>" + (String) variableIter.next() + "</Variable>"); 
	    }
	nbrOfBlanks--;
	//System.out.println(blanks.substring(0,nbrOfBlanks) + "</InternalComponents>"); 
	
	// ExternalComponents
	ExternalComponents externalComponents = EOP.getExternalComponents();
	if (externalComponents != null)
	{
	    //System.out.println(blanks.substring(0,nbrOfBlanks) + "<ExternalComponents>"); 
	    nbrOfBlanks++;
	    for (Iterator variableIter = externalComponents.getExternalComponent().iterator(); variableIter.hasNext();)
	    {
		ExternalComponent externalComponent = (ExternalComponent) variableIter.next();

		//System.out.println(blanks.substring(0,nbrOfBlanks) + "<ExternalComponent>");
		nbrOfBlanks++;
		//System.out.println(blanks.substring(0,nbrOfBlanks) + "<Component>" + externalComponent.getComponent() + "</Component>");
		//System.out.println(blanks.substring(0,nbrOfBlanks) + "<Machine>" + externalComponent.getMachine() + "</Machine>");
		nbrOfBlanks--;
		//System.out.println(blanks.substring(0,nbrOfBlanks) + "</ExternalComponent>");
	    }
	    nbrOfBlanks--;
	    //System.out.println(blanks.substring(0,nbrOfBlanks) + "</ExternalComponents>"); 
	}	

	// Zones
	org.supremica.manufacturingTables.xsd.eop.Zones zones = EOP.getZones();
	if (zones != null)
	{
	    System.out.println(blanks.substring(0,nbrOfBlanks) + "<Zones>"); 
	    nbrOfBlanks++;
	    for (Iterator zoneIter = zones.getZone().iterator(); zoneIter.hasNext();)
	    {
		System.out.println(blanks.substring(0,nbrOfBlanks) + "<Zone>" + (String) zoneIter.next() + "</Zone>"); 
	    }
	    nbrOfBlanks--;
	    System.out.println(blanks.substring(0,nbrOfBlanks) + "</Zones>"); 
	}
	
	// InitialState
	InitialState initialState = EOP.getInitialState();
	//System.out.println(blanks.substring(0,nbrOfBlanks) + "<InitialState>"); 
	nbrOfBlanks++;
	
	// Create and set InitialRow
	EOPInitialRowData initialRow = new EOPInitialRowData(); 
	EOPData.setEOPInitialRow(initialRow);
	
	//   ActuatorValue
	for (Iterator actuatorIter = initialState.getActuatorValue().iterator(); actuatorIter.hasNext();)
	{
	    //System.out.println(blanks.substring(0,nbrOfBlanks) + "<ActuatorValue>"); 
	    nbrOfBlanks++;
	    ActuatorValue actuatorValue = (ActuatorValue) actuatorIter.next();
	    //System.out.println(blanks.substring(0,nbrOfBlanks) + "<Actuator>" + actuatorValue.getActuator() + "</Actuator>"); 
	    //System.out.println(blanks.substring(0,nbrOfBlanks) + "<Value>" + actuatorValue.getValue() + "</Value>"); 
	    nbrOfBlanks--;
	    //System.out.println(blanks.substring(0,nbrOfBlanks) + "</ActuatorValue>"); 
	    
	    // Add actuatorToState
	    //String actuatorName = machineName.substring( machineName.length()-3, machineName.length() ) + actuatorValue.getActuator();
	    String actuatorName = operation.getMachine() + actuatorValue.getActuator();
	    // (The EOP has shorter actuator names not containing the machine name)
	    initialRow.addActuatorToState(actuatorName, actuatorValue.getValue());
	}
	
	//   SensorValue
	for (Iterator sensorIter = initialState.getSensorValue().iterator(); sensorIter.hasNext();)
	{
	    //System.out.println(blanks.substring(0,nbrOfBlanks) + "<SensorValue>"); 
	    nbrOfBlanks++;
	    SensorValue sensorValue = (SensorValue) sensorIter.next();
	    //System.out.println(blanks.substring(0,nbrOfBlanks) + "<Sensor>" + sensorValue.getSensor() + "</Sensor>"); 
	    //System.out.println(blanks.substring(0,nbrOfBlanks) + "<Value>" + sensorValue.getValue() + "</Value>"); 
	    nbrOfBlanks--;
	    //System.out.println(blanks.substring(0,nbrOfBlanks) + "</SensorValue>"); 
	    
	    // Add sensorToState
	    String sensorName = operation.getMachine() + sensorValue.getSensor(); 
	    // (The EOP has shorter sensor names not containing the last of the machine name)
	    initialRow.addSensorToState(sensorName, sensorValue.getValue());
	}

	//   VariableValue
	for (Iterator variableIter = initialState.getVariableValue().iterator(); variableIter.hasNext();)
	{
	    //System.out.println(blanks.substring(0,nbrOfBlanks) + "<VariableValue>"); 
	    nbrOfBlanks++;
	    VariableValue variableValue = (VariableValue) variableIter.next();
	    //System.out.println(blanks.substring(0,nbrOfBlanks) + "<Variable>" + variableValue.getVariable() + "</Variable>"); 
	    //System.out.println(blanks.substring(0,nbrOfBlanks) + "<Value>" + variableValue.getValue() + "</Value>"); 
	    nbrOfBlanks--;
	    //System.out.println(blanks.substring(0,nbrOfBlanks) + "</VariableValue>"); 
	    
	    // Add variableToValue
	    String variableName = operation.getMachine() + variableValue.getVariable();

	    // (The EOP has shorter variable names not containing the last of the machine name)
	    initialRow.addVariableToValue(variableName, variableValue.getValue());
	}
	
	//   ZoneStates
	for (Iterator zoneIter = initialState.getZoneState().iterator(); zoneIter.hasNext();)
	    {
		System.out.println(blanks.substring(0,nbrOfBlanks) + "<ZoneState>"); 
		nbrOfBlanks++;
		ZoneState zoneState = (ZoneState) zoneIter.next();
		System.out.println(blanks.substring(0,nbrOfBlanks) + "<Zone>" + zoneState.getZone() + "</Zone>"); 
		System.out.println(blanks.substring(0,nbrOfBlanks) + "<State>" + zoneState.getState() + "</State>"); 
		nbrOfBlanks--;
		System.out.println(blanks.substring(0,nbrOfBlanks) + "</ZoneState>"); 
		
		// Add zoneToState
		initialRow.addZoneToState(zoneState.getZone(), zoneState.getState());
	    }
	
	//   ExternalComponentValue
	for (Iterator externalIter = initialState.getExternalComponentValue().iterator(); externalIter.hasNext();)
	{
	    //System.out.println(blanks.substring(0,nbrOfBlanks) + "<ExternalComponentValue>"); 
	    nbrOfBlanks++;
	    ExternalComponentValue externalComponentValue = (ExternalComponentValue) externalIter.next();
	    ExternalComponent externalComponent = externalComponentValue.getExternalComponent();

	    //System.out.println(blanks.substring(0,nbrOfBlanks) + "<ExternalComponent>");
	    nbrOfBlanks++;
	    //System.out.println(blanks.substring(0,nbrOfBlanks) + "<Component>" + externalComponent.getComponent() + "</Component>");
	    //System.out.println(blanks.substring(0,nbrOfBlanks) + "<Machine>" + externalComponent.getMachine() + "</Machine>");
	    nbrOfBlanks--;
	    //System.out.println(blanks.substring(0,nbrOfBlanks) + "</ExternalComponent>");

	    String extMachineName = manufacturingCell.getName().substring(manufacturingCell.getName().length()-3, manufacturingCell.getName().length()) + externalComponent.getMachine(); 
	// (The EOP has shorter names not containing the last of the cell name)
	    EOPExternalComponentData externalComponentData= new EOPExternalComponentData(externalComponent.getComponent(), extMachineName);

	    //System.out.println(blanks.substring(0,nbrOfBlanks) + "<Value>" + externalComponentValue.getValue() + "</Value>"); 
	    nbrOfBlanks--;
	    //System.out.println(blanks.substring(0,nbrOfBlanks) + "</ExternalComponentValue>"); 
	    
	    // Add ExternalComponentToState
	    initialRow.addExternalComponentToState(externalComponentData, externalComponentValue.getValue());
	}
	
	//   InitialStateCheck
	//System.out.println(blanks.substring(0,nbrOfBlanks) + "<InitialStateCheck>"); 
	nbrOfBlanks++;
	InitialStateCheck initialStateCheck = initialState.getInitialStateCheck();
	//System.out.println(blanks.substring(0,nbrOfBlanks) + "<AlarmType>" + initialStateCheck.getAlarmType() + "</AlarmType>"); 
	
	// Set AlarmType
	initialRow.setAlarmType(initialStateCheck.getAlarmType());

	//System.out.println(blanks.substring(0,nbrOfBlanks) + "<AlarmDelay>" + initialStateCheck.getAlarmDelay() + "</AlarmDelay>"); 

	// Set AlarmDelay
	initialRow.setAlarmDelay(initialStateCheck.getAlarmDelay());

	nbrOfBlanks--;
	//System.out.println(blanks.substring(0,nbrOfBlanks) + "</InitialStateCheck>"); 

	nbrOfBlanks--;
	System.out.println(blanks.substring(0,nbrOfBlanks) + "</InitialState>"); 


 	// Actions
 	for (Iterator actionIter = EOP.getAction().iterator(); actionIter.hasNext();)
	{
	    Action action = (Action) actionIter.next();
	    //System.out.println(blanks.substring(0,nbrOfBlanks) + "<Action actionNbr=\"" + action.getActionNbr().intValue() + "\">"); 
	    nbrOfBlanks++;
	    
	    // Create and add ActionRow
	    EOPActionRowData actionRow = new EOPActionRowData(); 
	    EOPData.addEOPActionRow(actionRow);
	    
	    //   ActuatorValue
	    for (Iterator actuatorIter = action.getActuatorValue().iterator(); actuatorIter.hasNext();)
	    {
		//System.out.println(blanks.substring(0,nbrOfBlanks) + "<ActuatorValue>"); 
		nbrOfBlanks++;
		ActuatorValue actuatorValue = (ActuatorValue) actuatorIter.next();
		//System.out.println(blanks.substring(0,nbrOfBlanks) + "<Actuator>" + actuatorValue.getActuator() + "</Actuator>"); 
		//System.out.println(blanks.substring(0,nbrOfBlanks) + "<Value>" + actuatorValue.getValue() + "</Value>"); 
		nbrOfBlanks--;
		//System.out.println(blanks.substring(0,nbrOfBlanks) + "</ActuatorValue>"); 
		
		// Add actuatorToState
		String actuatorName = operation.getMachine() + actuatorValue.getActuator(); 
		// (The EOP has shorter actuator names not containing the last of the machine name)
		actionRow.addActuatorToState(actuatorName, actuatorValue.getValue());
	    }
	    
	    //   SensorValue
	    for (Iterator sensorIter = action.getSensorValue().iterator(); sensorIter.hasNext();)
	    {
		//System.out.println(blanks.substring(0,nbrOfBlanks) + "<SensorValue>"); 
		nbrOfBlanks++;
		SensorValue sensorValue = (SensorValue) sensorIter.next();
		//System.out.println(blanks.substring(0,nbrOfBlanks) + "<Sensor>" + sensorValue.getSensor() + "</Sensor>"); 
		//System.out.println(blanks.substring(0,nbrOfBlanks) + "<Value>" + sensorValue.getValue() + "</Value>"); 
		nbrOfBlanks--;
		//System.out.println(blanks.substring(0,nbrOfBlanks) + "</SensorValue>"); 
		
		// Add sensorToState
		String sensorName = operation.getMachine() + sensorValue.getSensor(); 
		// (The EOP has shorter sensor names not containing the last of the machine name)
		actionRow.addSensorToState(sensorName, sensorValue.getValue());
	    }

	    //   VariableValue
	    for (Iterator variableIter = action.getVariableValue().iterator(); variableIter.hasNext();)
	    {
		//System.out.println(blanks.substring(0,nbrOfBlanks) + "<VariableValue>"); 
		nbrOfBlanks++;
		VariableValue variableValue = (VariableValue) variableIter.next();
		//System.out.println(blanks.substring(0,nbrOfBlanks) + "<Variable>" + variableValue.getVariable() + "</Variable>"); 
		//System.out.println(blanks.substring(0,nbrOfBlanks) + "<Value>" + variableValue.getValue() + "</Value>"); 
		nbrOfBlanks--;
		//System.out.println(blanks.substring(0,nbrOfBlanks) + "</VariableValue>"); 
		
		// Add variableToValue
		String variableName = operation.getMachine() + variableValue.getVariable(); 
		// (The EOP has shorter variable names not containing the last of the machine name)
		actionRow.addVariableToValue(variableName, variableValue.getValue());
	    }
	    
	    //   ZoneStates
	    for (Iterator zoneIter = action.getZoneState().iterator(); zoneIter.hasNext();)
	    {
		System.out.println(blanks.substring(0,nbrOfBlanks) + "<ZoneState>"); 
		nbrOfBlanks++;
		ZoneState zoneState = (ZoneState) zoneIter.next();
		System.out.println(blanks.substring(0,nbrOfBlanks) + "<Zone>" + zoneState.getZone() + "</Zone>"); 
		System.out.println(blanks.substring(0,nbrOfBlanks) + "<State>" + zoneState.getState() + "</State>"); 
		nbrOfBlanks--;
		System.out.println(blanks.substring(0,nbrOfBlanks) + "</ZoneState>"); 
		
		// Add zoneToState
		actionRow.addZoneToState(zoneState.getZone(), zoneState.getState());
	    }
	    
// 	    // Add bookingZones and unbookingZones in private method. 
// 	    if ( !parseZoneString( action.getZones(), actionRow ) )
// 	    {
// 		return;
// 	    }
	    
	    nbrOfBlanks--;
	    //System.out.println(blanks.substring(0,nbrOfBlanks) + "</Action>"); 
	}
	
	nbrOfBlanks--;
	//System.out.println(blanks.substring(0,nbrOfBlanks) + "</EOP>"); 
	
	nbrOfBlanks--;
	//System.out.println(blanks.substring(0,nbrOfBlanks) + "</Operation>"); 
    }
    
//     private boolean parseZoneString( String zoneString, EOPActionRowData actionRow)
//     { 
// 	// Add bookingZones and unbookingZones. 
// 	boolean bookingZones = false;
// 	boolean unbookingZones = false;
// 	char previousChar = END_EXPR_TOKEN; 
// 	// I allways check that the current character is the expected compared to the previous.
// 	// It is OK with a new booking or unbooking substring after the previuos one has 
// 	// ended (ends with an END_EXPR_TOKEN).
	
// 	// A zone string typically looks like "u(1,2,20)" or "b(5,12)". It is OK with
// 	// string like  "u(1,20)b(5,12)" and whitespaces between digits and tokens
// 	// are also OK, but ignored. 
// 	for (int i = 0; i < zoneString.length(); i++)
// 	    {
// 		// no booking or unbooking
// 		if ( zoneString.charAt(i) == NOBOOKING_TOKEN )
// 		    {
// 			break;
// 		    }
		
// 		// booking zones
// 		else if ( zoneString.charAt(i) == BOOKING_TOKEN && previousChar == END_EXPR_TOKEN )
// 		    {
// 			bookingZones = true;
// 		    }
// 		// unbooking zones
// 		else if ( zoneString.charAt(i) == UNBOOKING_TOKEN && previousChar == END_EXPR_TOKEN )
// 		    {
// 			unbookingZones = true;
// 		    }
		
// 		// start (sub)expression
// 		else if ( zoneString.charAt(i) == START_EXPR_TOKEN && ( previousChar == UNBOOKING_TOKEN 
// 									   || previousChar == BOOKING_TOKEN ) )
// 		    {
// 			// This is OK as expected but nothing happens
// 		    }			
// 		// end (sub)expression
// 		else if ( zoneString.charAt(i)  == END_EXPR_TOKEN && Character.isDigit(previousChar) )
// 		    {
// 			unbookingZones = false;
// 			bookingZones = false;
// 		    }		
// 		// delimiter
// 		else if ( zoneString.charAt(i) == DELIMIT_TOKEN && Character.isDigit(previousChar) )
// 		    {
// 			// This is OK as expected but nothing happens
// 		    }	
// 		// A number with one or more digits
// 		else if ( Character.isDigit( zoneString.charAt(i) ) && ( previousChar == DELIMIT_TOKEN 
// 									 || previousChar == START_EXPR_TOKEN ) )
// 		{
// 		    String number = zoneString.substring(i, i+1); 
// 		    while ( i < zoneString.length()-1 && Character.isDigit( zoneString.charAt(i+1) ) )
// 		    {
// 			i++;
// 			number += zoneString.substring(i, i+1);
// 		    }
// 		    // Add booking zone
// 		    if ( bookingZones )
// 		    {
// 			actionRow.addBookingZone(number);
// 		    }
// 		    // Add unbooking zone
// 		    else if ( unbookingZones )
// 		    {
// 			actionRow.addUnbookingZone(number);
// 		    }
// 		    else
// 		    {
// 			System.err.println("The zone booking/unbooking string was incorrect!");
// 			return false;
// 		    }
// 		}
// 		// Blank 
// 		else if ( zoneString.charAt(i) == BLANK_TOKEN )
// 		{
// 		    // Shall not make this the previous character since it is not important
// 			continue;
// 		    }
// 		else
// 		    {
// 			System.err.println("The zone booking/unbooking string was incorrect!");
// 			return false;
// 		    }
// 		previousChar = zoneString.charAt(i);
// 	    }
// 	return true;
//     }
    
    public void buildPLCData(Factory factory)
    {

	// Variable to keep track of the indentation, just for now for printing the XML code
	nbrOfBlanks = 0;
	blanks = "                                                                              ";

	// Factory
	System.out.println(blanks.substring(0,nbrOfBlanks) + "<Factory name=\"" + factory.getName() + "\">");

	if(factory.getDescription()!=null)
	    {
		System.out.println(blanks.substring(0,nbrOfBlanks) + "<Description>" + factory.getDescription() + "</Description>");
	    }

	// Areas
	nbrOfBlanks++;
	Areas areas = factory.getAreas();
	System.out.println(blanks.substring(0,nbrOfBlanks) + "<Areas>");

	// Nbr of Areas
	List areaList = areas.getArea();
	nbrOfBlanks++;

	// For now the controlprogram is for just one cell and I assume that the Factory only
	// contains one cell

	for (Iterator areaIter = areaList.iterator();areaIter.hasNext();)
	    {
		Area currentArea = (Area) areaIter.next();
		System.out.println(blanks.substring(0,nbrOfBlanks) + "<Area name=\"" + currentArea.getName() + "\">");
		if(currentArea.getDescription()!=null)
		    {
			System.out.println(blanks.substring(0,nbrOfBlanks) + "<Description>" + currentArea.getDescription() + "</Description>");
		    }
		// Cells
		nbrOfBlanks++;
		Cells cells = currentArea.getCells();
		System.out.println(blanks.substring(0,nbrOfBlanks) + "<Cells>");

		// Nbr of Cells
		List cellList = cells.getCell();
		nbrOfBlanks++;

		for (Iterator cellIter = cellList.iterator();cellIter.hasNext();)
		    {
			Cell currentCell = (Cell) cellIter.next();
			System.out.println(blanks.substring(0,nbrOfBlanks) + "<Cell name=\"" + currentCell.getName() + "\">");
			nbrOfBlanks++;

			//Creating the ManufacturingCell
			manufacturingCell = new ManufacturingCell( currentCell.getName(), new Coordinator(), 
								  new Mailbox() );


			// Description
			if(currentCell.getDescription()!=null)
			{
			    System.out.println(blanks.substring(0,nbrOfBlanks) + "<Description>" + currentCell.getDescription() + "</Description>");
			    manufacturingCell.setDescription(currentCell.getDescription());
			}
			
			// Zones
			if (currentCell.getZones()!=null)
			{
			    System.out.println(blanks.substring(0,nbrOfBlanks) + "<Zones>");
			    nbrOfBlanks++;
			    for (Iterator zoneIter = currentCell.getZones().getZone().iterator(); zoneIter.hasNext();)
			    {
				String zone = (String) zoneIter.next();
				System.out.println(blanks.substring(0,nbrOfBlanks) + "<Zone>" + zone + "</Zone>");
				manufacturingCell.addZone(new ZoneData(zone));
			    }
			    nbrOfBlanks--;
			    System.out.println(blanks.substring(0,nbrOfBlanks) + "</Zones>");
			}

			// Machines
			buildMachines(currentCell.getMachines(), manufacturingCell);

			nbrOfBlanks--;
			System.out.println(blanks.substring(0,nbrOfBlanks) + "</Cell>");
		    }
		nbrOfBlanks--;
		System.out.println(blanks.substring(0,nbrOfBlanks) + "</Cells>");
		nbrOfBlanks--;
		System.out.println(blanks.substring(0,nbrOfBlanks) + "</Area>");

	    }
	nbrOfBlanks--;
	System.out.println(blanks.substring(0,nbrOfBlanks) + "</Areas>");
	nbrOfBlanks--;
	System.out.println(blanks.substring(0,nbrOfBlanks) + "</Factory>");

    }

    private void buildMachines(Machines machines, ManufacturingCell manufacturingCell)
    {
	System.out.println(blanks.substring(0,nbrOfBlanks) + "<Machines>");

	// Nbr of Machines
	List machineList = machines.getMachine();
	nbrOfBlanks++;
	for (Iterator machineIter = machineList.iterator();machineIter.hasNext();)
	    {
		buildMachine((org.supremica.manufacturingTables.xsd.factory.Machine) machineIter.next(), manufacturingCell);
	    }
	nbrOfBlanks--;
	System.out.println(blanks.substring(0,nbrOfBlanks) + "</Machines>");
    }

    private void buildMachine(org.supremica.manufacturingTables.xsd.factory.Machine machine, ManufacturingCell manufacturingCell)
    {
	System.out.println(blanks.substring(0,nbrOfBlanks) + "<Machine machineType=\"" + machine.getType() + "\" " + "name=\"" + machine.getName() + "\" ownControlSystem=\"" + machine.getOwnControlSystem() + "\">");
	nbrOfBlanks++;

	// Create Machine, MachineController and Mailbox and add the Machine to the cell
	MachineController machineController = new MachineController();
	Mailbox mailbox = new Mailbox();
	// Check if the machine has own control system or not
	boolean ownControlSystem = false;
	if ( machine.getOwnControlSystem().value().equals("Yes") )
	{
	    ownControlSystem = true;
	}
	else if ( machine.getOwnControlSystem().value().equals("No") )
	{
	    ownControlSystem = false;
	}
	else
	{
	    System.err.println("You must state whether the machine has own control system or not!");
	}
	

	MachineData machineData = new MachineData(machine.getName(), machine.getType().value(), machineController, mailbox, ownControlSystem); 
	
	manufacturingCell.addMachine(machineData);

	// Description
	if (machine.getDescription()!=null)
	    {
		System.out.println(blanks.substring(0,nbrOfBlanks) + "<Description>" + machine.getDescription() + "</Description>");
		machine.setDescription(machine.getDescription());
	    }

	// Variables
	if (machine.getVariables()!=null)
	    {
		buildVariables(machine.getVariables(), machineData);
	    }
	
	// Equipment
	if (machine.getEquipment()!=null)
	    {
		buildEquipment(machine.getEquipment(), machineData);
	    }
	
	nbrOfBlanks--;
	System.out.println(blanks.substring(0,nbrOfBlanks) + "</Machine>");
    }

    private void buildVariables(Variables variables, MachineData machineData)
    {
	System.out.println(blanks.substring(0,nbrOfBlanks) + "<Variables>");
	
	// Nbr of Variables
	List variableList = variables.getVariable();
	nbrOfBlanks++;
	for (Iterator variableIter = variableList.iterator(); variableIter.hasNext(); )
	    {
		org.supremica.manufacturingTables.xsd.factory.Variable variable = (org.supremica.manufacturingTables.xsd.factory.Variable) variableIter.next();

		System.out.println(blanks.substring(0,nbrOfBlanks) + "<Variable name=\"" + variable.getName() + "\">");		
		VariableData variableData = new VariableData(variable.getName());
		
		Values values = variable.getValues();
		nbrOfBlanks++;
		System.out.println(blanks.substring(0,nbrOfBlanks) + "<Values>");
		
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
			
			//System.out.println(blanks.substring(0,nbrOfBlanks) + "<Value>" + value.getValue() + "</Value>");
			System.out.println(blanks.substring(0,nbrOfBlanks) + "<Value>" + value + "</Value>");
			
			variableData.addValue(value);
		    }
		nbrOfBlanks--;
		System.out.println(blanks.substring(0,nbrOfBlanks) + "</Values>");
		
		machineData.addVariable(variableData);
		// Initial value
		variableData.setInitialValue(variable.getInitialValue());
		System.out.println(blanks.substring(0,nbrOfBlanks) + "<InitialValue>" + variable.getInitialValue() + "</InitialValue>");


		nbrOfBlanks--;
		System.out.println(blanks.substring(0,nbrOfBlanks) + "</Variable>");
	    }
	nbrOfBlanks--;
	System.out.println(blanks.substring(0,nbrOfBlanks) + "</Variables>");
    }

    // The EquipmentContainer (interface) upperLevelEquipment could be either a machine 
    // (org.supremica.manufacturingTables.controlsystemdata.Machine)
    // or an Actuator and the current Equipment should be added to the corresponding 
    // upperLevelEquipment Equipment or Machine.
    private void buildEquipment(org.supremica.manufacturingTables.xsd.factory.Equipment equip, EquipmentContainer upperLevelEquipment)
    {
	System.out.println(blanks.substring(0,nbrOfBlanks) + "<Equipment>");

	// Nbr of EquipmentEntities
	List equipList = equip.getEquipmentEntity();
	nbrOfBlanks++;
	for (Iterator equipIter = equipList.iterator();equipIter.hasNext();)
	    {
		EquipmentEntity currentEquip = (EquipmentEntity) equipIter.next();
		buildEquipmentEntity(currentEquip, upperLevelEquipment);
	    }
	nbrOfBlanks--;
	System.out.println(blanks.substring(0,nbrOfBlanks) + "</Equipment>");
    }
    
    // The EquipmentContainer upperLevelEquipment could be either a machine 
    // (org.supremica.manufacturingTables.controlsystemdata.Machine)
    // or an Actuator and the current Equipment should be added to the corresponding Equipment or Machine.
    private void buildEquipmentEntity(EquipmentEntity equipEnt, EquipmentContainer upperLevelEquipment)
    {
	org.supremica.manufacturingTables.controlsystemdata.Equipment equipment = null;
	// Sensors and Actuators are separated.
	if (equipEnt.getType().value().equals("Sensor"))
	    {
		equipment = new org.supremica.manufacturingTables.controlsystemdata.Sensor(equipEnt.getName());
		upperLevelEquipment.addSensor((org.supremica.manufacturingTables.controlsystemdata.Sensor) equipment);
	    }
	
	else if (equipEnt.getType().value().equals("Actuator"))
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
	
	System.out.println(blanks.substring(0,nbrOfBlanks) + "<EquipmentEntity equipmentType=\"" + equipEnt.getType() + "\" " + "name=\"" + equipEnt.getName() + "\">");
	nbrOfBlanks++;
	
	
	// Description
	if (equipEnt.getDescription()!=null)
	    {
		System.out.println(blanks.substring(0,nbrOfBlanks) + "<Description>" + equipEnt.getDescription() + "</Description>");
		equipment.setDescription(equipEnt.getDescription());
		
	    }
	
	
	// States
	States states = equipEnt.getStates();
	System.out.println(blanks.substring(0,nbrOfBlanks) + "<States>");
	// Nbr of States
	List stateList = states.getState();
	nbrOfBlanks++;
	for (Iterator stateIter = stateList.iterator();stateIter.hasNext();)
	    {
		// Since there is a class State this XML simpletype should be represented by a State object,
		// but it is a String, as for the Value above.
		//State currentState = (State) stateIter.next();
		String currentState = (String) stateIter.next();
		//System.out.println(blanks.substring(0,nbrOfBlanks) + "<State>" + currentState.getValue() + "</State>");
		System.out.println(blanks.substring(0,nbrOfBlanks) + "<State>" + currentState + "</State>");
		
		equipment.addState(currentState);
	    }
	nbrOfBlanks--;
	System.out.println(blanks.substring(0,nbrOfBlanks) + "</States>");
	
	// Elements (Optional for an actuator, a sensor must have elements or lower level sensors. This is 
	// however not checked here.)
	if (equipEnt.getElements()!=null)
	    {
		System.out.println(blanks.substring(0,nbrOfBlanks) + "<Elements>");
		
		// Nbr of Elements
		// It may be hard to imagine an EquipmentEntity having more than one Elements 
		// but for now I divide it this way.
		List elementList = equipEnt.getElements().getElement();
		nbrOfBlanks++;
		for (Iterator elementIter = elementList.iterator();elementIter.hasNext();)
		    {
			org.supremica.manufacturingTables.xsd.factory.Element currentElement = (org.supremica.manufacturingTables.xsd.factory.Element) elementIter.next();
			printElement(currentElement);
			equipment.addHardwareConnection(currentElement.getName());
		    }
		nbrOfBlanks--;
		System.out.println(blanks.substring(0,nbrOfBlanks) + "</Elements>");
	    }
	
	// Lower level Equipment, could be possible even for sensors consisting of sensors, but not 
	// sensors consisting of actuators. This is however not checked here.
	if (equipEnt.getEquipment()!=null)
	    {
		buildEquipment(equipEnt.getEquipment(), equipment);
	    }
	
	nbrOfBlanks--;
	System.out.println(blanks.substring(0,nbrOfBlanks) + "</EquipmentEntity>");
    }
    
    
    // For printing the element data. 
    private void printElement(org.supremica.manufacturingTables.xsd.factory.Element element)
    {
	System.out.println(blanks.substring(0,nbrOfBlanks) + "<Element name=\"" + element.getName() + "\">");
	nbrOfBlanks++;
	
	// Description, this is not added to the ControlSystemData
	if (element.getDescription()!=null)
	    {
		System.out.println(blanks.substring(0,nbrOfBlanks) + "<Description>" + element.getDescription() + "</Description>");
	    }

	nbrOfBlanks--;
	System.out.println(blanks.substring(0,nbrOfBlanks) + "</Element>");
    }

}
