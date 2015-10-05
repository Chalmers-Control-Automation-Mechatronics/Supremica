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
import java.util.HashMap;
import java.util.Map.Entry;
import org.supremica.manufacturingTables.controlsystemdata.*;
import org.supremica.manufacturingTables.controlsystemimplementation.Java.*;
import org.supremica.manufacturingTables.controlsystemimplementation.Java.COPActivity;

public class JavaControlSystemImplementationBuilder extends ControlSystemImplementationBuilder
{

  public JavaControlSystemImplementationBuilder()
  {
    super();
  }

  public void createNewPLCProgram(final ManufacturingCell cell)
  {
    // PLCProgram with mailbox and coordinator are created.
    // We have to start by creating a mailbox, to be able to create a coordinator,
    // to be able to create a PLCProgramJava. The controlsystemdata mailbox and coordinator contains
    // no information so they are actully not needed to create the Javaimplementation.
    final org.supremica.manufacturingTables.controlsystemimplementation.Java.Mailbox plcProgramMailbox = new  org.supremica.manufacturingTables.controlsystemimplementation.Java.Mailbox();

    final org.supremica.manufacturingTables.controlsystemimplementation.Java.Coordinator coordinator = new  org.supremica.manufacturingTables.controlsystemimplementation.Java.Coordinator(plcProgramMailbox);

    // Creating zones which are registered to the PLC program mailbox
    for ( final ZoneData zoneData : cell.getZones() )
    {
      new Zone(zoneData.getZoneName(), plcProgramMailbox);
    }

    plcProgram = new PLCProgramJava(cell.getName(), coordinator);

    // Description
    ((PLCProgramJava) plcProgram).setDescription(cell.getDescription()); // (could be null)

    // COPs
    // A HashMap is used to store all activities for each machine for fast and easy access later when adding
    // successors at the appropriate places. For now I assume that we have only one COP per Machine!
    final Map< String, Map< String, org.supremica.manufacturingTables.controlsystemimplementation.Java.COPActivity > > machinesActivities = new HashMap< String, Map< String, org.supremica.manufacturingTables.controlsystemimplementation.Java.COPActivity > >();
    for (final Iterator<COPData> COPIter = cell.getCOPs().iterator(); COPIter.hasNext();)
    {
      // As mentioned above a HashMap is used for each Machine/COP to store all the activities for fast and
      // easy access.
      final Map<String, org.supremica.manufacturingTables.controlsystemimplementation.Java.COPActivity> activities = new HashMap<String, org.supremica.manufacturingTables.controlsystemimplementation.Java.COPActivity>();

      final COPData COPData = COPIter.next();
      final COP cop = new COP(COPData.getId(), COPData.getMachine());
      cop.setComment(COPData.getComment());
      //COP Activities
      for (final Iterator<org.supremica.manufacturingTables.controlsystemdata.COPActivity> activityIter = COPData.getCOPActivities().iterator(); activityIter.hasNext();)
      {
        final org.supremica.manufacturingTables.controlsystemdata.COPActivity COPActivityData =
          activityIter.next();
        final org.supremica.manufacturingTables.controlsystemimplementation.Java.COPActivity activity = new org.supremica.manufacturingTables.controlsystemimplementation.Java.COPActivity(COPActivityData.getOperation());
        // Add activity to hashmap
        activities.put(String.valueOf(activity.getOperation()), activity);
        // Predecessors
        for (final Iterator<Predecessor> predecessorIter = COPActivityData.getPredecessors().iterator(); predecessorIter.hasNext();)
        {
          final Predecessor predecessorData = predecessorIter.next();
          final COPPredecessor predecessor = new COPPredecessor( predecessorData.getOperation(), predecessorData.getMachine() );
          // Add predecesor
          activity.addPredecessor(predecessor);
          // Create an Hashmap
        }
        // Add activity
        cop.addCOPActivity(activity);
      }
      // Register COP to Coordinator
      coordinator.registerCOP(cop);
      // Add activity map to hashmap with all activities for all machines
      machinesActivities.put(cop.getMachine(), activities);
    }
    // Now when all COPs are added we will iterate through them again and add successors where appropriate
    for (final Iterator<Entry<String,Map<String,COPActivity>>> machineIter = machinesActivities.entrySet().iterator(); machineIter.hasNext();)
    {
      final Entry<String,Map<String,COPActivity>> entry = machineIter.next();
      final Map<String, org.supremica.manufacturingTables.controlsystemimplementation.Java.COPActivity> activities = (Map<String, org.supremica.manufacturingTables.controlsystemimplementation.Java.COPActivity>) entry.getValue();
      final String machine = (String) entry.getKey();
      for (final org.supremica.manufacturingTables.controlsystemimplementation.Java.COPActivity activity : activities.values())
      {
        for (final COPPredecessor predecessor : activity.getPredecessors())
        {
          if ( machinesActivities.containsKey( predecessor.getMachine() ) && machinesActivities.get( predecessor.getMachine() ).containsKey( String.valueOf( predecessor.getOperation() ) ) )
          {
            // && means that the second criteria will not be tested if the first one is not fullfilled
            // otherwise this would lead to a nullpointerException
            final org.supremica.manufacturingTables.controlsystemimplementation.Java.COPActivity predActivity = machinesActivities.get( predecessor.getMachine() ).get( String.valueOf( predecessor.getOperation() ) );
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
    final Map<String,MachineData> machines = cell.getMachines();
    for (final Iterator<MachineData> machineIter = machines.values().iterator(); machineIter.hasNext();)
    {
      final MachineData machineData = machineIter.next();

      Machine machine;
      org.supremica.manufacturingTables.controlsystemimplementation.Java.MachineController machineController;
      org.supremica.manufacturingTables.controlsystemimplementation.Java.Mailbox machineMailbox = null;
      // Check if the machine has own control system

      // Machines with own control system
      if (machineData.hasOwnControlSystem())
      {
        //continue; //jumps to the end of the current iteration in this for-loop
        machineController = new org.supremica.manufacturingTables.controlsystemimplementation.Java.MachineControlCommunicator();
        machine = new org.supremica.manufacturingTables.controlsystemimplementation.Java.Machine(machineData.getName(), machineData.getDescription(), machineController, plcProgramMailbox);
        System.err.println("Creating Java PLCCode for machine with own control system: " + machineData.getName());
      }

      // Machines with no own control system
      else
      {
        // Machine with MachineController and mailbox are created.
        // We have to start by creating a machine mailbox, to be able to create a MachineController,
        // to be able to create a Machine. The controlsystemdata mailbox and MachineController contains no
        // information so they are actully not needed to create the Javaimplementation.
        machineMailbox = new  org.supremica.manufacturingTables.controlsystemimplementation.Java.Mailbox(); //then you shall register() listeners to the mailbox?
        machineController = new org.supremica.manufacturingTables.controlsystemimplementation.Java.MachineControlSystem(machineMailbox);

        machine = new Machine(machineData.getName(), machineData.getDescription(), machineController, plcProgramMailbox);
        System.err.println("Creating Java PLCCode for machine: " + machineData.getName());
      }


      // EOPs
      final List<EOPData> EOPs = machineData.getEOPs();
      for (final Iterator<EOPData> EOPIter = EOPs.iterator(); EOPIter.hasNext();)
      {
        final EOPData EOPData = EOPIter.next();
        // Create and register EOP
        final EOP EOP = new EOP( EOPData.getId(), EOPData.getType() );
        machineController.registerEOP(EOP);

        EOP.setComment( EOPData.getComment() );
        System.out.println("Adding EOP with number: " + EOPData.getId());

        // EOP Initial Row
        final EOPInitialRowData EOPInitialRowData = EOPData.getEOPInitialRow();
        final EOPInitialRow EOPInitialRow = new EOPInitialRow();
        EOP.setEOPInitialRow(EOPInitialRow);

        // Set Alarm Type
        EOPInitialRow.setAlarmType(EOPInitialRowData.getAlarmType());
        // Set Alarm Delay
        EOPInitialRow.setAlarmDelay(EOPInitialRowData.getAlarmDelay());
        // Add External Components to State
        for (final Iterator<Entry<EOPExternalComponentData,String>> externalIter = EOPInitialRowData.getExternalComponentToStateMap().entrySet().iterator(); externalIter.hasNext();)
        {
          final Entry<EOPExternalComponentData,String> componentToState = externalIter.next();
          final EOPExternalComponentData extCompData = componentToState.getKey();
          final EOPExternalComponent externalComponent = new EOPExternalComponent(extCompData.getComponentName(), extCompData.getMachine());
          EOPInitialRow.addExternalComponentToState(externalComponent, componentToState.getValue());
        }
        // Add Actuator to State
        for (final Iterator<Entry<String,String>> actuatorIter = EOPInitialRowData.getActuatorToStateMap().entrySet().iterator(); actuatorIter.hasNext();)
        {
          final Entry<String,String> actuatorToState = actuatorIter.next();
          EOPInitialRow.addActuatorToState(actuatorToState.getKey(), actuatorToState.getValue() );
        }
        // Add Sensor to State
        for (final Iterator<Entry<String,String>> sensorIter = EOPInitialRowData.getSensorToStateMap().entrySet().iterator(); sensorIter.hasNext();)
        {
          final Entry<String,String> sensorToState = sensorIter.next();
          EOPInitialRow.addSensorToState(sensorToState.getKey(), sensorToState.getValue());
        }
        // Add Variable to Value
        for (final Iterator<Entry<String,String>> variableIter = EOPInitialRowData.getVariableToValueMap().entrySet().iterator(); variableIter.hasNext();)
        {
          final Entry<String,String> variableToValue = variableIter.next();
          EOPInitialRow.addVariableToValue(variableToValue.getKey(), variableToValue.getValue() );
        }

        // Add Zone to State
        for (final Iterator<Entry<String,String>> zoneIter = EOPInitialRowData.getZoneToStateMap().entrySet().iterator(); zoneIter.hasNext();)
        {
          final Entry<String,String> zoneToState = zoneIter.next();
          EOPInitialRow.addZoneToState(zoneToState.getKey(), zoneToState.getValue());
        }

        // EOP Action Rows
        final List<EOPRowData> EOPActionRows = EOPData.getEOPActionRows();
        for (final Iterator<EOPRowData> actionIter = EOPActionRows.iterator(); actionIter.hasNext();)
        {
          final EOPActionRowData actionRowData = (EOPActionRowData) actionIter.next();
          final EOPActionRow actionRow = new EOPActionRow();
          EOP.addEOPActionRow(actionRow);

          // Add Actuator to State
          for (final Iterator<Entry<String,String>> actuatorIter = actionRowData.getActuatorToStateMap().entrySet().iterator(); actuatorIter.hasNext();)
          {
            final Entry<String,String> actuatorToState = actuatorIter.next();
            actionRow.addActuatorToState(actuatorToState.getKey(), actuatorToState.getValue());
          }
          // Add Sensor to State
          for (final Iterator<Entry<String,String>> sensorIter = actionRowData.getSensorToStateMap().entrySet().iterator(); sensorIter.hasNext();)
          {
            final Entry<String,String> sensorToState = sensorIter.next();
            actionRow.addSensorToState(sensorToState.getKey(), sensorToState.getValue());
          }
          // Add Variable to Value
          for (final Iterator<Entry<String,String>> variableIter = actionRowData.getVariableToValueMap().entrySet().iterator(); variableIter.hasNext();)
          {
            final Entry<String,String> variableToValue = variableIter.next();
            actionRow.addVariableToValue(variableToValue.getKey(), variableToValue.getValue() );
          }
          // Add Zone to State
          for (final Iterator<Entry<String,String>> zoneIter = actionRowData.getZoneToStateMap().entrySet().iterator(); zoneIter.hasNext();)
          {
            final Entry<String,String> zoneToState = zoneIter.next();
            actionRow.addZoneToState(zoneToState.getKey(), zoneToState.getValue());
          }
        }
      }

      if (!machineData.hasOwnControlSystem())
      {
        // TopLevelSensors
        final List<org.supremica.manufacturingTables.controlsystemdata.Sensor> sensors = machineData.getSensors();
        for (final Iterator<org.supremica.manufacturingTables.controlsystemdata.Sensor> sensorIter = sensors.iterator(); sensorIter.hasNext();)
        {
          final org.supremica.manufacturingTables.controlsystemdata.Sensor sensorData =
            sensorIter.next();
          final org.supremica.manufacturingTables.controlsystemimplementation.Java.Sensor sensor = new org.supremica.manufacturingTables.controlsystemimplementation.Java.TopLevelSensor(sensorData.getName(), machineMailbox);
          System.err.println("Adding top level sensor: " + sensorData.getName());

          // Description
          sensor.setDescription(sensorData.getDescription());

          // LowLevelSensors
          final List<org.supremica.manufacturingTables.controlsystemdata.Sensor> llSensors = sensorData.getSensors();
          for (final Iterator<org.supremica.manufacturingTables.controlsystemdata.Sensor> llSensorIter = llSensors.iterator(); llSensorIter.hasNext();)
          {
            sensor.addSensor(createLowLevelSensor(llSensorIter.next()));
          }

          System.err.println("Adding states");
          // States
          final List<String> states = sensorData.getStates();
          for (final Iterator<String> stateIter = states.iterator(); stateIter.hasNext();)
          {
            sensor.addState(stateIter.next());
          }
          System.err.println("Adding hardwareConnections");
          // hardwareConnections
          final List<String> hardware = sensorData.getHardwareConnections();
          for (final Iterator<String> hardwareIter = hardware.iterator(); hardwareIter.hasNext();)
          {
            sensor.addHardwareConnection(hardwareIter.next());
          }
        }

        // TopLevelActuators
        final List<org.supremica.manufacturingTables.controlsystemdata.Actuator> actuators = machineData.getActuators();
        for (final Iterator<org.supremica.manufacturingTables.controlsystemdata.Actuator> actuatorIter = actuators.iterator(); actuatorIter.hasNext();)
        {
          final org.supremica.manufacturingTables.controlsystemdata.Actuator actuatorData =
            actuatorIter.next();
          final org.supremica.manufacturingTables.controlsystemimplementation.Java.Actuator actuator = new org.supremica.manufacturingTables.controlsystemimplementation.Java.TopLevelActuator(actuatorData.getName(), machineMailbox);
          System.err.println("Adding top level actuator: " + actuatorData.getName());

          // Description
          actuator.setDescription(actuatorData.getDescription());

          System.err.println("Adding low level sensors");
          // LowLevelSensors
          final List<org.supremica.manufacturingTables.controlsystemdata.Sensor> llSensors = actuatorData.getSensors();
          for (final Iterator<org.supremica.manufacturingTables.controlsystemdata.Sensor> llSensorIter = llSensors.iterator(); llSensorIter.hasNext();)
          {
            actuator.addSensor(createLowLevelSensor(llSensorIter.next()));
          }

          System.err.println("Adding low level actuators");
          // LowLevelActuators
          final List<org.supremica.manufacturingTables.controlsystemdata.Actuator> llActuators = actuatorData.getActuators();
          for (final Iterator<org.supremica.manufacturingTables.controlsystemdata.Actuator> llActuatorIter = llActuators.iterator(); llActuatorIter.hasNext();)
          {
            actuator.addActuator(createLowLevelActuator(llActuatorIter.next()));
          }

          System.err.println("Adding states");
          // States
          final List<String> states = actuatorData.getStates();
          for (final Iterator<String> stateIter = states.iterator(); stateIter.hasNext();)
          {
            actuator.addState(stateIter.next());
          }

          System.err.println("Adding hardwareConnections");
          // hardwareConnections
          final List<String> hardware = actuatorData.getHardwareConnections();
          for (final Iterator<String> hardwareIter = hardware.iterator(); hardwareIter.hasNext();)
          {
            actuator.addHardwareConnection(hardwareIter.next());
          }
        }

        // Variables, for machines with own control system the variables should be included in that control system
        final List<VariableData> variables = machineData.getVariables();
        for (final Iterator<VariableData> variableIter = variables.iterator(); variableIter.hasNext();)
        {
          final VariableData variableData = variableIter.next();
          final Variable variable = new Variable(variableData.getName(), machineMailbox);

          // Values
          final List<String> values = variableData.getValues();
          for (final Iterator<String> valueIter = values.iterator(); valueIter.hasNext();)
          {
            variable.addValue(valueIter.next());
          }
          // Initial Value
          variable.setCurrentValue(variableData.getInitialValue());
        }
      }
      // Set machine to machine controller
      machineController.setMachine(machine);
    }
  }

  // Low Level Sensor
  private org.supremica.manufacturingTables.controlsystemimplementation.Java.Sensor createLowLevelSensor(final org.supremica.manufacturingTables.controlsystemdata.Sensor sensorData)
  {
    final org.supremica.manufacturingTables.controlsystemimplementation.Java.Sensor sensor = new org.supremica.manufacturingTables.controlsystemimplementation.Java.LowLevelSensor(sensorData.getName());
    System.err.println("Adding low level sensor: " + sensorData.getName());

    // Description
    sensor.setDescription(sensorData.getDescription());

    // LowLevelSensors
    final List<org.supremica.manufacturingTables.controlsystemdata.Sensor> llSensors = sensorData.getSensors();
    for (final Iterator<org.supremica.manufacturingTables.controlsystemdata.Sensor> llSensorIter = llSensors.iterator(); llSensorIter.hasNext();)
    {
      sensor.addSensor(createLowLevelSensor(llSensorIter.next()));
    }

    // States
    final List<String> states = sensorData.getStates();
    for (final Iterator<String> stateIter = states.iterator(); stateIter.hasNext();)
    {
      sensor.addState(stateIter.next());
    }

    // hardwareConnections
    final List<String> hardware = sensorData.getHardwareConnections();
    for (final Iterator<String> hardwareIter = hardware.iterator(); hardwareIter.hasNext();)
    {
      sensor.addHardwareConnection((String) hardwareIter.next());
    }

    return sensor;
  }

  // Low Level Actuator
  private org.supremica.manufacturingTables.controlsystemimplementation.Java.Actuator createLowLevelActuator(final org.supremica.manufacturingTables.controlsystemdata.Actuator actuatorData)
  {
    final org.supremica.manufacturingTables.controlsystemimplementation.Java.Actuator actuator = new org.supremica.manufacturingTables.controlsystemimplementation.Java.LowLevelActuator(actuatorData.getName());
    System.err.println("Adding low level actuator: " + actuatorData.getName());

    // Description
    actuator.setDescription(actuatorData.getDescription());

    // LowLevelSensors
    final List<org.supremica.manufacturingTables.controlsystemdata.Sensor> llSensors = actuatorData.getSensors();
    for (final Iterator<org.supremica.manufacturingTables.controlsystemdata.Sensor> llSensorIter = llSensors.iterator(); llSensorIter.hasNext();)
    {
      actuator.addSensor(createLowLevelSensor(llSensorIter.next()));
    }

    // LowLevelActuators
    final List<org.supremica.manufacturingTables.controlsystemdata.Actuator> llActuators = actuatorData.getActuators();
    for (final Iterator<org.supremica.manufacturingTables.controlsystemdata.Actuator> llActuatorIter = llActuators.iterator(); llActuatorIter.hasNext();)
    {
      actuator.addActuator(createLowLevelActuator(llActuatorIter.next()));
    }

    // States
    final List<String> states = actuatorData.getStates();
    for (final Iterator<String> stateIter = states.iterator(); stateIter.hasNext();)
    {
      actuator.addState((String) stateIter.next());
    }

    // hardwareConnections
    final List<String> hardware = actuatorData.getHardwareConnections();
    for (final Iterator<String> hardwareIter = hardware.iterator(); hardwareIter.hasNext();)
    {
      actuator.addHardwareConnection((String) hardwareIter.next());
    }

    return actuator;
  }
}
