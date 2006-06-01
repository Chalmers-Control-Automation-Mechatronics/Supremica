/*
 * Supremica Software License Agreement
 *
 * The Supremica software is not in the public domain
 * However, it is freely available without fee for education,
 * research, and non-profit purposes.  By obtaining copies of
 * this and other files that comprise the Su	for (Iterator machineIter = machines.iterator(); machineIter.hasNext();)
 	    {
		org.supremica.manufacturingTables.controlsystemdata.Machine machineData = (org.supremica.manufacturingTables.controlsystemdata.Machine) machineIter.next();
premica software,
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
 * The JavaControlSystemImplementationBuilder class is used to build a PLCProgram 
 * in Java that runs on a Java virtual machine.
 *
 *
 * Created: Fri May 12 10:00:39 2006
 *
 * @author Oscar
 * @version 1.0
 */
package org.supremica.manufacturingTables.management;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.supremica.manufacturingTables.controlsystemdata.*;
import org.supremica.manufacturingTables.controlsystemimplementation.PLCProgram;
import org.supremica.manufacturingTables.controlsystemimplementation.Java.*;

public class JavaControlSystemImplementationBuilder extends ControlSystemImplementationBuilder
{

    public JavaControlSystemImplementationBuilder()
    {
	super();
    }

    public void createNewPLCProgram(ManufacturingCell cell)
    {
	// PLCProgram with mailbox and coordinator are created.
	// We have to start by creating a mailbox, to be able to create a coordinator, 
	// to be able to create a PLCProgramJava. The controlsystemdata mailbox and coordinator contains no information 
	// so they are actully not needed to create the Javaimplementation.
	org.supremica.manufacturingTables.controlsystemimplementation.Java.Mailbox plcProgramMailbox = new  org.supremica.manufacturingTables.controlsystemimplementation.Java.Mailbox();  
	
	org.supremica.manufacturingTables.controlsystemimplementation.Java.Coordinator coordinator = new  org.supremica.manufacturingTables.controlsystemimplementation.Java.Coordinator(plcProgramMailbox);
	
	plcProgram = new PLCProgramJava(cell.getName(), coordinator);

	// Description
	((PLCProgramJava) plcProgram).setDescription(cell.getDescription()); // (could be null)
	
	// Machines
	Map machines = cell.getMachines();
	for (Iterator machineIter = machines.values().iterator(); machineIter.hasNext();)
 	    {
		org.supremica.manufacturingTables.controlsystemdata.Machine machineData = (org.supremica.manufacturingTables.controlsystemdata.Machine) machineIter.next();
		
		// For now I just ignore the machines that has own control system
		if (!(machineData.getType().equals("Fixture") || machineData.getType().equals("TurnTable")))
		    {
			continue; //jumps to the end of the current iteration in this for-loop
		    } 

		// Machine with MachineController and mailbox are created.
		// We have to start by creating a machine mailbox, to be able to create a MachineController, 
		// to be able to create a Machine. The controlsystemdata mailbox and MachineController contains no
		// information so they are actully not needed to create the Javaimplementation.
		org.supremica.manufacturingTables.controlsystemimplementation.Java.Mailbox machineMailbox = new  org.supremica.manufacturingTables.controlsystemimplementation.Java.Mailbox(); //then you shall register() listeners to the mailbox? 
		org.supremica.manufacturingTables.controlsystemimplementation.Java.MachineController machineController = new org.supremica.manufacturingTables.controlsystemimplementation.Java.MachineController(machineMailbox);

		org.supremica.manufacturingTables.controlsystemimplementation.Java.Machine machine = new org.supremica.manufacturingTables.controlsystemimplementation.Java.Machine(machineData.getName(), machineData.getDescription(), machineController, plcProgramMailbox);
		System.err.println("Creating Java PLCCode for machine: " + machineData.getName());
		

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
			// Add External Variables to State
			for (Iterator externalIter = EOPInitialRowData.getExternalVariableToStateMap().entrySet().iterator(); externalIter.hasNext();)
			    {
				Entry variableToState = (Entry) externalIter.next();
				EOPInitialRow.addExternalVariableToState( (String) variableToState.getKey(), (String) variableToState.getValue() );
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


			// EOP Action Rows
			List EOPActionRows = EOPData.getEOPActionRows();
			for (Iterator actionIter = EOPActionRows.iterator(); actionIter.hasNext();)
			    {
				EOPActionRowData actionRowData = (EOPActionRowData) actionIter.next();
				EOPActionRow actionRow = new EOPActionRow();
				EOP.addEOPActionRow(actionRow);
			
				// Add Booking Zones 
				for (Iterator bookingIter = actionRowData.getBookingZones().iterator(); bookingIter.hasNext();)
				    {
					String bookingZone = (String) bookingIter.next();
					actionRow.addBookingZone( bookingZone );
				    }

				// Add Unbooking Zones 
				for (Iterator unbookingIter = actionRowData.getUnbookingZones().iterator(); unbookingIter.hasNext();)
				    {
					String unbookingZone = (String) unbookingIter.next();
					actionRow.addUnbookingZone( unbookingZone );
				    }
				
				// Add Actuator to State
				for (Iterator actuatorIter = actionRowData.getActuatorToStateMap().entrySet().iterator(); actuatorIter.hasNext();)
				    {
					Entry actuatorToState = (Entry) actuatorIter.next();
					actionRow.addActuatorToState( (String) actuatorToState.getKey(), (String) actuatorToState.getValue() );
				    }
				// Add Sensor to State
				for (Iterator sensorIter = EOPInitialRowData.getSensorToStateMap().entrySet().iterator(); sensorIter.hasNext();)
				    {
					Entry sensorToState = (Entry) sensorIter.next();
					EOPInitialRow.addSensorToState( (String) sensorToState.getKey(), (String) sensorToState.getValue() );
				    }
				
			    }
		    }
		
		// Variables
		List variables = machineData.getVariables();
		for (Iterator variableIter = variables.iterator(); variableIter.hasNext();)
		    {
			org.supremica.manufacturingTables.controlsystemdata.Variable variableData = (org.supremica.manufacturingTables.controlsystemdata.Variable) variableIter.next();
			org.supremica.manufacturingTables.controlsystemimplementation.Java.Variable variable = new org.supremica.manufacturingTables.controlsystemimplementation.Java.Variable(variableData.getName());
			
			machine.addVariable(variable); 
			
			// Values
			List values = variableData.getValues();
			for (Iterator valueIter = values.iterator(); valueIter.hasNext();)
			    {
				variable.addValue((String) valueIter.next());
			    }
		    }

		// TopLevelSensors
		List sensors = machineData.getSensors();
		for (Iterator sensorIter = sensors.iterator(); sensorIter.hasNext();)
		    {
			org.supremica.manufacturingTables.controlsystemdata.Sensor sensorData = (org.supremica.manufacturingTables.controlsystemdata.Sensor) sensorIter.next();
			
			org.supremica.manufacturingTables.controlsystemimplementation.Java.Sensor sensor = new org.supremica.manufacturingTables.controlsystemimplementation.Java.TopLevelSensor(sensorData.getName(), machineMailbox);
			System.err.println("Adding top level sensor: " + sensorData.getName());
			
			// Description
			sensor.setDescription(sensorData.getDescription());
			
			// LowLevelSensors
			List llSensors = sensorData.getSensors();
			for (Iterator llSensorIter = llSensors.iterator(); llSensorIter.hasNext();)
			    {
				sensor.addSensor(createLowLevelSensor((org.supremica.manufacturingTables.controlsystemdata.Sensor) llSensorIter.next()));
			    }

			System.err.println("Adding states");
			// States 
			List states = sensorData.getStates();
			for (Iterator stateIter = states.iterator(); stateIter.hasNext();)
			    {
				sensor.addState((String) stateIter.next());
			    }
			System.err.println("Adding hardwareConnections");
			// hardwareConnections 
			List hardware = sensorData.getHardwareConnections();
			for (Iterator hardwareIter = hardware.iterator(); hardwareIter.hasNext();)
			    {
				sensor.addHardwareConnection((String) hardwareIter.next());
			    }
		    }
		
		
		// TopLevelActuators
		List actuators = machineData.getActuators();
		for (Iterator actuatorIter = actuators.iterator(); actuatorIter.hasNext();)
		    {
			org.supremica.manufacturingTables.controlsystemdata.Actuator actuatorData = (org.supremica.manufacturingTables.controlsystemdata.Actuator) actuatorIter.next();
			
			org.supremica.manufacturingTables.controlsystemimplementation.Java.Actuator actuator = new org.supremica.manufacturingTables.controlsystemimplementation.Java.TopLevelActuator(actuatorData.getName(), machineMailbox);
			System.err.println("Adding top level actuator: " + actuatorData.getName());
			
			// Description
			actuator.setDescription(actuatorData.getDescription());
			
			System.err.println("Adding low level sensors");
			// LowLevelSensors
			List llSensors = actuatorData.getSensors();
			for (Iterator llSensorIter = llSensors.iterator(); llSensorIter.hasNext();)
			    {
				actuator.addSensor(createLowLevelSensor((org.supremica.manufacturingTables.controlsystemdata.Sensor) llSensorIter.next()));
			    }

			System.err.println("Adding low level actuators");
			// LowLevelActuators
			List llActuators = actuatorData.getActuators();
			for (Iterator llActuatorIter = llActuators.iterator(); llActuatorIter.hasNext();)
			    {
				actuator.addActuator(createLowLevelActuator((org.supremica.manufacturingTables.controlsystemdata.Actuator) llActuatorIter.next()));
			    }
			
			System.err.println("Adding states");
			// States 
			List states = actuatorData.getStates();
			for (Iterator stateIter = states.iterator(); stateIter.hasNext();)
			    {
				actuator.addState((String) stateIter.next());
			    }

			System.err.println("Adding hardwareConnections");
			// hardwareConnections 
			List hardware = actuatorData.getHardwareConnections();
			for (Iterator hardwareIter = hardware.iterator(); hardwareIter.hasNext();)
			    {
				actuator.addHardwareConnection((String) hardwareIter.next());
			    }
		    }
 	    }
	
 	
    }
    
    // Low Level Sensor
    private org.supremica.manufacturingTables.controlsystemimplementation.Java.Sensor createLowLevelSensor(org.supremica.manufacturingTables.controlsystemdata.Sensor sensorData)
    {
	org.supremica.manufacturingTables.controlsystemimplementation.Java.Sensor sensor = new org.supremica.manufacturingTables.controlsystemimplementation.Java.LowLevelSensor(sensorData.getName());
	System.err.println("Adding low level sensor: " + sensorData.getName());

	// Description
	sensor.setDescription(sensorData.getDescription());
	
	// LowLevelSensors
	List llSensors = sensorData.getSensors();
	for (Iterator llSensorIter = llSensors.iterator(); llSensorIter.hasNext();)
	    {
		sensor.addSensor(createLowLevelSensor((org.supremica.manufacturingTables.controlsystemdata.Sensor) llSensorIter.next()));
	    }
	
	// States 
	List states = sensorData.getStates();
	for (Iterator stateIter = states.iterator(); stateIter.hasNext();)
	    {
		sensor.addState((String) stateIter.next());
	    }
	
	// hardwareConnections 
	List hardware = sensorData.getHardwareConnections();
	for (Iterator hardwareIter = hardware.iterator(); hardwareIter.hasNext();)
	    {
		sensor.addHardwareConnection((String) hardwareIter.next());
	    }

	return sensor;
    }
    
    // Low Level Actuator
    private org.supremica.manufacturingTables.controlsystemimplementation.Java.Actuator createLowLevelActuator(org.supremica.manufacturingTables.controlsystemdata.Actuator actuatorData)
    {
	org.supremica.manufacturingTables.controlsystemimplementation.Java.Actuator actuator = new org.supremica.manufacturingTables.controlsystemimplementation.Java.LowLevelActuator(actuatorData.getName());
	System.err.println("Adding low level actuator: " + actuatorData.getName());
			
	// Description
	actuator.setDescription(actuatorData.getDescription());
			
	// LowLevelSensors
	List llSensors = actuatorData.getSensors();
	for (Iterator llSensorIter = llSensors.iterator(); llSensorIter.hasNext();)
	    {
		actuator.addSensor(createLowLevelSensor((org.supremica.manufacturingTables.controlsystemdata.Sensor) llSensorIter.next()));
	    }
	
	// LowLevelActuators
	List llActuators = actuatorData.getActuators();
	for (Iterator llActuatorIter = llActuators.iterator(); llActuatorIter.hasNext();)
	    {
		actuator.addActuator(createLowLevelActuator((org.supremica.manufacturingTables.controlsystemdata.Actuator) llActuatorIter.next()));
	    }
	
	// States 
	List states = actuatorData.getStates();
	for (Iterator stateIter = states.iterator(); stateIter.hasNext();)
	    {
		actuator.addState((String) stateIter.next());
	    }
	
	// hardwareConnections 
	List hardware = actuatorData.getHardwareConnections();
	for (Iterator hardwareIter = hardware.iterator(); hardwareIter.hasNext();)
	    {
		actuator.addHardwareConnection((String) hardwareIter.next());
	    }
   
	return actuator;
 }
}
