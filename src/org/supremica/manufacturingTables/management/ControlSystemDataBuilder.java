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
import org.supremica.manufacturingTables.management.*;
import org.supremica.automationobjects.xsd.libraryelement.*;
import org.supremica.functionblocks.xsd.libraryelement.*;
import org.supremica.manufacturingTables.controlsystemdata.*;


public class ControlSystemDataBuilder
{

    // Variable to keep track of the indentation, just for now for printing the XML code
    private int nbrOfBlanks;
    private String blanks;

    public ControlSystemDataBuilder()
    {
    }

    public ManufacturingCell buildPLCData(FactoryType factory)
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
	ManufacturingCell cell = null;

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
			cell = new ManufacturingCell(currentCell.getName(), new Coordinator(), new Mailbox());


			// Description
			if(currentCell.getDescription()!=null)
			    {
				System.err.println(blanks.substring(0,nbrOfBlanks) + "<Description>" + currentCell.getDescription() + "</Description>");
				cell.setDescription(currentCell.getDescription());
			    }
			
			// Machines
			buildMachines(currentCell.getMachines(), cell);

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

	return cell;
    }

    private void buildMachines(MachinesType machines, ManufacturingCell cell)
    {
	System.err.println(blanks.substring(0,nbrOfBlanks) + "<Machines>");

	// Nbr of Machines
	List machineList = machines.getMachine();
	nbrOfBlanks++;
	for (Iterator machineIter = machineList.iterator();machineIter.hasNext();)
	    {
		buildMachine((MachineType) machineIter.next(), cell);
	    }
	nbrOfBlanks--;
	System.err.println(blanks.substring(0,nbrOfBlanks) + "</Machines>");
    }

    private void buildMachine(MachineType machineType, ManufacturingCell cell)
    {
	System.err.println(blanks.substring(0,nbrOfBlanks) + "<Machine machineType=\"" + machineType.getType() + "\" " + "name=\"" + machineType.getName() + "\">");
	nbrOfBlanks++;

	// Create Machine, MachineController and Mailbox and add the Machine to the cell
	MachineController machineController = new MachineController();
	Mailbox mailbox = new Mailbox();
	org.supremica.manufacturingTables.controlsystemdata.Machine machine = new org.supremica.manufacturingTables.controlsystemdata.Machine(machineType.getName(), machineType.getType(), machineController, mailbox); 
	cell.addMachine(machine);

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
	EquipmentInterface equipment = null;
	// Sensors and Actuators are separated.
	if (equipEnt.getType().equals("Sensor"))
	    {
		equipment = new Sensor(equipEnt.getName());
		upperLevelEquipment.addSensor((Sensor) equipment);
	    }
	
	else if (equipEnt.getType().equals("Actuator"))
	    {
		equipment = new Actuator(equipEnt.getName());
		upperLevelEquipment.addActuator((Actuator) equipment);
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
	
	// Elements (optional for an actuator)
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
	
	// Lower level Equipment, should only be possible for actuators
	if (equipEnt.getEquipment()!=null)
	    {
		if (equipment instanceof Actuator)
		    {
			buildEquipment(equipEnt.getEquipment(),(Actuator) equipment);
		    }
		else
		    {
			System.err.println("Only possible for actuators to contain lower level equipment!");
		    }
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
