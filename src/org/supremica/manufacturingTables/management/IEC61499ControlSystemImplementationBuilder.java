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
 * The IEC61499ControlSystemImplementationBuilder class is used to build a PLCProgram 
 * in IEC61499 that runs on for instance FUBER.
 *
 *
 * Created: Tue Oct 30 12:49 2006
 *
 * @author Oscar
 * @version 1.0
 */
package org.supremica.manufacturingTables.management;

import java.io.File;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Map.Entry;
import org.supremica.manufacturingTables.controlsystemdata.*;
import org.supremica.manufacturingTables.controlsystemimplementation.IEC61499.*;

import net.sourceforge.fuber.model.Device;

public class IEC61499ControlSystemImplementationBuilder extends ControlSystemImplementationBuilder
{
    // Objects needed in the Fuber application:
    private Set<org.supremica.manufacturingTables.controlsystemimplementation.IEC61499.Machine> cellMachines;
    private Set<org.supremica.manufacturingTables.controlsystemimplementation.IEC61499.MachineCoordinator> machineCoordinators;
    private Set<org.supremica.manufacturingTables.controlsystemimplementation.IEC61499.Zone> zones;
    private org.supremica.manufacturingTables.controlsystemimplementation.IEC61499.Coordinator coordinator;

    public IEC61499ControlSystemImplementationBuilder()
    {
	super();
    }

    public void createNewPLCProgram(ManufacturingCell cell)
    {
	// For the Fuber application:
	cellMachines = new HashSet<org.supremica.manufacturingTables.controlsystemimplementation.IEC61499.Machine>();
	machineCoordinators = new HashSet<org.supremica.manufacturingTables.controlsystemimplementation.IEC61499.MachineCoordinator>();
	zones = new HashSet<org.supremica.manufacturingTables.controlsystemimplementation.IEC61499.Zone>();
	
	// PLCProgram with mailbox and coordinator are created.
	// We have to start by creating a mailbox, to be able to create a coordinator, 
	// to be able to create a PLCProgramIEC61499. The controlsystemdata mailbox and coordinator contains 
	// no information so they are actully not needed to create the IEC61499implementation.
	org.supremica.manufacturingTables.controlsystemimplementation.IEC61499.Mailbox plcProgramMailbox = new  org.supremica.manufacturingTables.controlsystemimplementation.IEC61499.Mailbox();  
	
	//org.supremica.manufacturingTables.controlsystemimplementation.IEC61499.Coordinator coordinator = new  org.supremica.manufacturingTables.controlsystemimplementation.IEC61499.Coordinator(plcProgramMailbox);
	// Changed, for the Fuber application, to the class-locally private coordinator:
	coordinator = new  org.supremica.manufacturingTables.controlsystemimplementation.IEC61499.Coordinator(plcProgramMailbox);
	
	// Creating zones which are registered to the PLC program mailbox
	for ( ZoneData zoneData : cell.getZones() )
	{
	    //new Zone(zoneData.getZoneName(), plcProgramMailbox);
	    // For the Fuber application:
	    zones.add( new Zone( zoneData.getZoneName(), plcProgramMailbox ) );
	}

	plcProgram = new PLCProgramIEC61499(cell.getName(), coordinator);
	
	// Description
	((PLCProgramIEC61499) plcProgram).setDescription(cell.getDescription()); // (could be null)

	// COPs
	// A HashMap is used to store all activities for each machine for fast and easy access later when adding 
	// successors at the appropriate places. For now I assume that we have only one COP per Machine! 
	Map< String, Map< String, org.supremica.manufacturingTables.controlsystemimplementation.IEC61499.COPActivity > > machinesActivities = new HashMap< String, Map< String, org.supremica.manufacturingTables.controlsystemimplementation.IEC61499.COPActivity > >();
	for (Iterator COPIter = cell.getCOPs().iterator(); COPIter.hasNext();)
	{
	    // As mentioned above a HashMap is used for each Machine/COP to store all the activities for fast and 
	    // easy access.
	    Map<String, org.supremica.manufacturingTables.controlsystemimplementation.IEC61499.COPActivity> activities = new HashMap<String, org.supremica.manufacturingTables.controlsystemimplementation.IEC61499.COPActivity>();
	    
	    COPData COPData = (COPData) COPIter.next();
	    COP COP = new COP(COPData.getId(), COPData.getMachine());
	    COP.setComment(COPData.getComment());
	    //COP Activities
	    for (Iterator activityIter = COPData.getCOPActivities().iterator(); activityIter.hasNext();)
	    {
		org.supremica.manufacturingTables.controlsystemdata.COPActivity COPActivityData = (org.supremica.manufacturingTables.controlsystemdata.COPActivity) activityIter.next();
		org.supremica.manufacturingTables.controlsystemimplementation.IEC61499.COPActivity COPActivity = new org.supremica.manufacturingTables.controlsystemimplementation.IEC61499.COPActivity(COPActivityData.getOperation());
		// Add activity to hashmap
		activities.put(String.valueOf(COPActivity.getOperation()), COPActivity);
		// Predecessors
		for (Iterator predecessorIter = COPActivityData.getPredecessors().iterator(); predecessorIter.hasNext();)
		{
		    Predecessor predecessorData = (Predecessor) predecessorIter.next();
		    COPPredecessor predecessor = new COPPredecessor( predecessorData.getOperation(), predecessorData.getMachine() );
		    // Add predecesor
		    COPActivity.addPredecessor(predecessor);
		    // Create an Hashmap
		}
		// Add activity
		COP.addCOPActivity(COPActivity);
	    }
	    // Register COP to Coordinator
	    coordinator.registerCOP(COP);
	    // Add activity map to hashmap with all activities for all machines
	    machinesActivities.put(COP.getMachine(), activities);
	}
	// Now when all COPs are added we will iterate through them again and add successors where appropriate
	for (Iterator machineIter = machinesActivities.entrySet().iterator(); machineIter.hasNext();)
	{
	    Entry entry = (Entry) machineIter.next();
	    Map<String, org.supremica.manufacturingTables.controlsystemimplementation.IEC61499.COPActivity> activities = (Map<String, org.supremica.manufacturingTables.controlsystemimplementation.IEC61499.COPActivity>) entry.getValue();
	    String machine = (String) entry.getKey();
	    for (org.supremica.manufacturingTables.controlsystemimplementation.IEC61499.COPActivity activity : activities.values())
	    {
		for (COPPredecessor predecessor : activity.getPredecessors())
		{
		    if ( machinesActivities.containsKey( predecessor.getMachine() ) && machinesActivities.get( predecessor.getMachine() ).containsKey( String.valueOf( predecessor.getOperation() ) ) )
		    {
			// && means that the second criteria will not be tested if the first one is not fullfilled 
			// otherwise this would lead to a nullpointerException 
			org.supremica.manufacturingTables.controlsystemimplementation.IEC61499.COPActivity predActivity = machinesActivities.get( predecessor.getMachine() ).get( String.valueOf( predecessor.getOperation() ) );
			predActivity.addSuccessor( new COPSuccessor( activity.getOperation(), machine ) );
			System.out.println("Adding successor " + machine + ", " + activity.getOperation() + " to " + predecessor.getMachine() + ", " + predActivity.getOperation());   
		    }
		    else
		    {
			System.err.print("No COP found for machine " + predecessor.getMachine() + " or the predecessing activity for operation " +  predecessor.getOperation() + " was not found!");	 
			System.err.println(" (The current activity (operation) and machine is: " +  activity.getOperation() + ", " + machine + ")");  
			
		    }
		}
	    }
	}
	
	
	// Machines
	Map machines = cell.getMachines();
	for (Iterator machineIter = machines.values().iterator(); machineIter.hasNext();)
	{
	    MachineData machineData = (MachineData) machineIter.next();
	    
	    Machine machine; 
	    org.supremica.manufacturingTables.controlsystemimplementation.IEC61499.MachineController machineController;
	    org.supremica.manufacturingTables.controlsystemimplementation.IEC61499.Mailbox machineMailbox = null;
	    // Check if the machine has own control system
	    
	    // Machines with own control system
	    if (machineData.hasOwnControlSystem())
	    {
		//continue; //jumps to the end of the current iteration in this for-loop
		machineController = new org.supremica.manufacturingTables.controlsystemimplementation.IEC61499.MachineControlCommunicator();
		machine = new org.supremica.manufacturingTables.controlsystemimplementation.IEC61499.Machine(machineData.getName(), machineData.getDescription(), machineController, plcProgramMailbox);
		System.err.println("Creating IEC61499 PLCCode for machine with own control system: " + machineData.getName());
	    } 
	    
	    // Machines with no own control system
	    else
	    {
		//Not implemented yet for 61499
		machine = null;
		machineController = null;

// 		// Machine with MachineController and mailbox are created.
// 		// We have to start by creating a machine mailbox, to be able to create a MachineController, 
// 		// to be able to create a Machine. The controlsystemdata mailbox and MachineController contains no
// 		// information so they are actully not needed to create the IEC61499implementation.
// 		machineMailbox = new  org.supremica.manufacturingTables.controlsystemimplementation.IEC61499.Mailbox(); //then you shall register() listeners to the mailbox? 
// 		machineController = new org.supremica.manufacturingTables.controlsystemimplementation.IEC61499.MachineControlSystem(machineMailbox);
		
// 		machine = new Machine(machineData.getName(), machineData.getDescription(), machineController, plcProgramMailbox);
// 		System.err.println("Creating IEC61499 PLCCode for machine: " + machineData.getName());
	    }
	    

 	    // EOPs
 	    List EOPs = machineData.getEOPs();
 	    for (Iterator EOPIter = EOPs.iterator(); EOPIter.hasNext();)
 	    {
 		EOPData EOPData = (EOPData) EOPIter.next();
 		// Create and register EOP
 		EOP EOP = new EOP( EOPData.getId(), EOPData.getType() );
 		machineController.registerEOP(EOP);
		
 		EOP.setComment( EOPData.getComment() );
 		System.out.println("Adding EOP with number: " + EOPData.getId());
		
 		// EOP Initial Row
 		EOPInitialRowData EOPInitialRowData = EOPData.getEOPInitialRow();
 		EOPInitialRow EOPInitialRow = new EOPInitialRow();
 		EOP.setEOPInitialRow(EOPInitialRow);
		
 		// Set Alarm Type
 		EOPInitialRow.setAlarmType(EOPInitialRowData.getAlarmType());
 		// Set Alarm Delay
 		EOPInitialRow.setAlarmDelay(EOPInitialRowData.getAlarmDelay());
 		// Add External Components to State
 		for (Iterator externalIter = EOPInitialRowData.getExternalComponentToStateMap().entrySet().iterator(); externalIter.hasNext();)
 		{
 		    Entry componentToState = (Entry) externalIter.next();
 		    EOPExternalComponentData extCompData = (EOPExternalComponentData) componentToState.getKey();
 		    EOPExternalComponent externalComponent = new EOPExternalComponent(extCompData.getComponentName(), extCompData.getMachine());
 		    EOPInitialRow.addExternalComponentToState(externalComponent, (String) componentToState.getValue());
 		}
 		// Add Actuator to State
 		for (Iterator actuatorIter = EOPInitialRowData.getActuatorToStateMap().entrySet().iterator(); actuatorIter.hasNext();)
 		{
 		    Entry actuatorToState = (Entry) actuatorIter.next();
 		    EOPInitialRow.addActuatorToState( (String) actuatorToState.getKey(), (String) actuatorToState.getValue() );
 		}
 		// Add Sensor to State
 		for (Iterator sensorIter = EOPInitialRowData.getSensorToStateMap().entrySet().iterator(); sensorIter.hasNext();)
 		{
 		    Entry sensorToState = (Entry) sensorIter.next();
 		    EOPInitialRow.addSensorToState( (String) sensorToState.getKey(), (String) sensorToState.getValue() );
 		}
 		// Add Variable to Value
 		for (Iterator variableIter = EOPInitialRowData.getVariableToValueMap().entrySet().iterator(); variableIter.hasNext();)
 		{
 		    Entry variableToValue = (Entry) variableIter.next();
 		    EOPInitialRow.addVariableToValue( (String) variableToValue.getKey(), (String) variableToValue.getValue() );
 		}

 		// Add Zone to State
		for (Iterator zoneIter = EOPInitialRowData.getZoneToStateMap().entrySet().iterator(); zoneIter.hasNext();)
 		{
 		    Entry zoneToState = (Entry) zoneIter.next();
 		    EOPInitialRow.addZoneToState( (String) zoneToState.getKey(), (String) zoneToState.getValue() );
 		}
		
		
 		// EOP Action Rows
 		List EOPActionRows = EOPData.getEOPActionRows();
 		for (Iterator actionIter = EOPActionRows.iterator(); actionIter.hasNext();)
 		{
 		    EOPActionRowData actionRowData = (EOPActionRowData) actionIter.next();
 		    EOPActionRow actionRow = new EOPActionRow();
 		    EOP.addEOPActionRow(actionRow);
		    
 		    // Add Actuator to State
 		    for (Iterator actuatorIter = actionRowData.getActuatorToStateMap().entrySet().iterator(); actuatorIter.hasNext();)
 		    {
 			Entry actuatorToState = (Entry) actuatorIter.next();
 			actionRow.addActuatorToState( (String) actuatorToState.getKey(), (String) actuatorToState.getValue() );
 		    }
 		    // Add Sensor to State
 		    for (Iterator sensorIter = actionRowData.getSensorToStateMap().entrySet().iterator(); sensorIter.hasNext();)
 		    {
 			Entry sensorToState = (Entry) sensorIter.next();
 			actionRow.addSensorToState( (String) sensorToState.getKey(), (String) sensorToState.getValue() );
 		    }
 		    // Add Variable to Value
 		    for (Iterator variableIter = actionRowData.getVariableToValueMap().entrySet().iterator(); variableIter.hasNext();)
 		    {
 			Entry variableToValue = (Entry) variableIter.next();
 			actionRow.addVariableToValue( (String) variableToValue.getKey(), (String) variableToValue.getValue() );
 		    }

 		    // Add Zone to State
 		    for (Iterator zoneIter = actionRowData.getZoneToStateMap().entrySet().iterator(); zoneIter.hasNext();)
 		    {
 			Entry zoneToState = (Entry) zoneIter.next();
 			actionRow.addZoneToState( (String) zoneToState.getKey(), (String) zoneToState.getValue() );
 		    }
		    
 		}
 	    }
 
// 	    if (!machineData.hasOwnControlSystem())
// 	    {
// 		// TopLevelSensors
// 		List sensors = machineData.getSensors();
// 		for (Iterator sensorIter = sensors.iterator(); sensorIter.hasNext();)
// 		{
// 		    org.supremica.manufacturingTables.controlsystemdata.Sensor sensorData = (org.supremica.manufacturingTables.controlsystemdata.Sensor) sensorIter.next();
		    
// 		    org.supremica.manufacturingTables.controlsystemimplementation.IEC61499.Sensor sensor = new org.supremica.manufacturingTables.controlsystemimplementation.IEC61499.TopLevelSensor(sensorData.getName(), machineMailbox);
// 		    System.err.println("Adding top level sensor: " + sensorData.getName());
		    
// 		    // Description
// 		    sensor.setDescription(sensorData.getDescription());
		    
// 		    // LowLevelSensors
// 		    List llSensors = sensorData.getSensors();
// 		    for (Iterator llSensorIter = llSensors.iterator(); llSensorIter.hasNext();)
// 		    {
// 			sensor.addSensor(createLowLevelSensor((org.supremica.manufacturingTables.controlsystemdata.Sensor) llSensorIter.next()));
// 		    }
		    
// 		    System.err.println("Adding states");
// 		    // States 
// 		    List states = sensorData.getStates();
// 		    for (Iterator stateIter = states.iterator(); stateIter.hasNext();)
// 		    {
// 			sensor.addState((String) stateIter.next());
// 		    }
// 		    System.err.println("Adding hardwareConnections");
// 		    // hardwareConnections 
// 		    List hardware = sensorData.getHardwareConnections();
// 		    for (Iterator hardwareIter = hardware.iterator(); hardwareIter.hasNext();)
// 		    {
// 			sensor.addHardwareConnection((String) hardwareIter.next());
// 		    }
// 		}
		
		
// 		// TopLevelActuators
// 		List actuators = machineData.getActuators();
// 		for (Iterator actuatorIter = actuators.iterator(); actuatorIter.hasNext();)
// 		{
// 		    org.supremica.manufacturingTables.controlsystemdata.Actuator actuatorData = (org.supremica.manufacturingTables.controlsystemdata.Actuator) actuatorIter.next();
		    
// 		    org.supremica.manufacturingTables.controlsystemimplementation.IEC61499.Actuator actuator = new org.supremica.manufacturingTables.controlsystemimplementation.IEC61499.TopLevelActuator(actuatorData.getName(), machineMailbox);
// 		    System.err.println("Adding top level actuator: " + actuatorData.getName());
		    
// 		    // Description
// 		    actuator.setDescription(actuatorData.getDescription());
		    
// 		    System.err.println("Adding low level sensors");
// 		    // LowLevelSensors
// 		    List llSensors = actuatorData.getSensors();
// 		    for (Iterator llSensorIter = llSensors.iterator(); llSensorIter.hasNext();)
// 		    {
// 			actuator.addSensor(createLowLevelSensor((org.supremica.manufacturingTables.controlsystemdata.Sensor) llSensorIter.next()));
// 		    }
		    
// 		    System.err.println("Adding low level actuators");
// 		    // LowLevelActuators
// 		    List llActuators = actuatorData.getActuators();
// 		    for (Iterator llActuatorIter = llActuators.iterator(); llActuatorIter.hasNext();)
// 		    {
// 			actuator.addActuator(createLowLevelActuator((org.supremica.manufacturingTables.controlsystemdata.Actuator) llActuatorIter.next()));
// 		    }
		    
// 		    System.err.println("Adding states");
// 		    // States 
// 		    List states = actuatorData.getStates();
// 		    for (Iterator stateIter = states.iterator(); stateIter.hasNext();)
// 		    {
// 			actuator.addState((String) stateIter.next());
// 		    }
		    
// 		    System.err.println("Adding hardwareConnections");
// 		    // hardwareConnections 
// 		    List hardware = actuatorData.getHardwareConnections();
// 		    for (Iterator hardwareIter = hardware.iterator(); hardwareIter.hasNext();)
// 		    {
// 			actuator.addHardwareConnection((String) hardwareIter.next());
// 		    }
// 		}
		
// 		// Variables, for machines with own control system the variables should be included in that control system
// 		List variables = machineData.getVariables();
// 		for (Iterator variableIter = variables.iterator(); variableIter.hasNext();)
// 		{
// 		    VariableData variableData = (VariableData) variableIter.next();
// 		    Variable variable = new Variable(variableData.getName(), machineMailbox);
		    
// 		    // Values
// 		    List values = variableData.getValues();
// 		    for (Iterator valueIter = values.iterator(); valueIter.hasNext();)
// 		    {
// 			variable.addValue((String) valueIter.next());
// 		    }
// 		    // Initial Value
// 		    variable.setCurrentValue(variableData.getInitialValue());
// 		}
// 	    }
	    // Set machine to machine controller
	    machineController.setMachine(machine);
	    
	    // For the Fuber application:
	    cellMachines.add(machine);
	}
	// For the Fuber application:
	for (MachineCoordinator machineCoordinator : coordinator.getMachineCoordinators().values())
	{
	    machineCoordinators.add(machineCoordinator);
	}
	Device fuberDevice = new Device("DemoDevice", "App.sys", ".." + File.separatorChar + ".." + File.separatorChar + "Fuber" + File.separatorChar + "fblib", "demo" + File.pathSeparatorChar + "event" + File.pathSeparatorChar + "service" + File.pathSeparatorChar + "misc", 1, coordinator, machineCoordinators, zones, cellMachines);
	fuberDevice.run();
    }
    
//     // Low Level Sensor
//     private org.supremica.manufacturingTables.controlsystemimplementation.IEC61499.Sensor createLowLevelSensor(org.supremica.manufacturingTables.controlsystemdata.Sensor sensorData)
//     {
// 	org.supremica.manufacturingTables.controlsystemimplementation.IEC61499.Sensor sensor = new org.supremica.manufacturingTables.controlsystemimplementation.IEC61499.LowLevelSensor(sensorData.getName());
// 	System.err.println("Adding low level sensor: " + sensorData.getName());
	
// 	// Description
// 	sensor.setDescription(sensorData.getDescription());
	
// 	// LowLevelSensors
// 	List llSensors = sensorData.getSensors();
// 	for (Iterator llSensorIter = llSensors.iterator(); llSensorIter.hasNext();)
// 	{
// 	    sensor.addSensor(createLowLevelSensor((org.supremica.manufacturingTables.controlsystemdata.Sensor) llSensorIter.next()));
// 	}
	
// 	// States 
// 	List states = sensorData.getStates();
// 	for (Iterator stateIter = states.iterator(); stateIter.hasNext();)
// 	{
// 	    sensor.addState((String) stateIter.next());
// 	}
	
// 	// hardwareConnections 
// 	List hardware = sensorData.getHardwareConnections();
// 	for (Iterator hardwareIter = hardware.iterator(); hardwareIter.hasNext();)
// 	{
// 	    sensor.addHardwareConnection((String) hardwareIter.next());
// 	}

// 	return sensor;
//     }
    
//     // Low Level Actuator
//     private org.supremica.manufacturingTables.controlsystemimplementation.IEC61499.Actuator createLowLevelActuator(org.supremica.manufacturingTables.controlsystemdata.Actuator actuatorData)
//     {
// 	org.supremica.manufacturingTables.controlsystemimplementation.IEC61499.Actuator actuator = new org.supremica.manufacturingTables.controlsystemimplementation.IEC61499.LowLevelActuator(actuatorData.getName());
// 	System.err.println("Adding low level actuator: " + actuatorData.getName());
			
// 	// Description
// 	actuator.setDescription(actuatorData.getDescription());
			
// 	// LowLevelSensors
// 	List llSensors = actuatorData.getSensors();
// 	for (Iterator llSensorIter = llSensors.iterator(); llSensorIter.hasNext();)
// 	{
// 	    actuator.addSensor(createLowLevelSensor((org.supremica.manufacturingTables.controlsystemdata.Sensor) llSensorIter.next()));
// 	}
	
// 	// LowLevelActuators
// 	List llActuators = actuatorData.getActuators();
// 	for (Iterator llActuatorIter = llActuators.iterator(); llActuatorIter.hasNext();)
// 	{
// 	    actuator.addActuator(createLowLevelActuator((org.supremica.manufacturingTables.controlsystemdata.Actuator) llActuatorIter.next()));
// 	}
	
// 	// States 
// 	List states = actuatorData.getStates();
// 	for (Iterator stateIter = states.iterator(); stateIter.hasNext();)
// 	{
// 	    actuator.addState((String) stateIter.next());
// 	}
	
// 	// hardwareConnections 
// 	List hardware = actuatorData.getHardwareConnections();
// 	for (Iterator hardwareIter = hardware.iterator(); hardwareIter.hasNext();)
// 	{
// 	    actuator.addHardwareConnection((String) hardwareIter.next());
// 	}
   
// 	return actuator;
//     }
}
