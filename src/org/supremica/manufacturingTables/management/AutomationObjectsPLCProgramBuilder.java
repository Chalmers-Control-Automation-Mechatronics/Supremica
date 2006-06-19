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
 * The Loader class uses JAXB to load a Factory
 * application into a PLC program structure.
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
import net.sourceforge.fuber.xsd.libraryelement.*;


public class AutomationObjectsPLCProgramBuilder
{

    // Variable to keep track of the indentation, just for now for printing the XML code
    private int nbrOfBlanks;
    private String blanks;
    private AOApplication aoApplication;
    private org.supremica.automationobjects.xsd.libraryelement.ObjectFactory objFactory;

    public AutomationObjectsPLCProgramBuilder()
    {
    }

    public void buildPLCProgram(FactoryType factory)
    {
	// Create an Automation Object application using the ObjectFactory
	objFactory = new org.supremica.automationobjects.xsd.libraryelement.ObjectFactory();
	try
	    {
		aoApplication = objFactory.createAOApplication();
	    }
	catch (JAXBException je)
	    {
		System.err.println("Failed to create an AOApplication");
		je.printStackTrace();
		return;
	    }

	// Variable to keep track of the indentation, just for now for printing the XML code
	nbrOfBlanks = 0;
	blanks = "                                                                              ";

	// Factory
	System.err.println(blanks.substring(0,nbrOfBlanks) + "<Factory name=\"" + factory.getName() + "\">");
	aoApplication.setName("Factory " + factory.getName());

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

			// Description
			if(currentCell.getDescription()!=null)
			    {
				System.err.println(blanks.substring(0,nbrOfBlanks) + "<Description>" + currentCell.getDescription() + "</Description>");
			    }

			// Equipment does not longer exist on cell level
			//if (currentCell.getEquipment()!=null)
			//    {
			//	buildEquipment(currentCell.getEquipment());
			//    }

			// Machines
			buildMachines(currentCell.getMachines());

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

    private void buildMachines(MachinesType machines)
    {
	System.err.println(blanks.substring(0,nbrOfBlanks) + "<Machines>");

	// Nbr of Machines
	List machineList = machines.getMachine();
	nbrOfBlanks++;
	for (Iterator machineIter = machineList.iterator();machineIter.hasNext();)
	    {
		buildMachine((MachineType) machineIter.next());
	    }
	nbrOfBlanks--;
	System.err.println(blanks.substring(0,nbrOfBlanks) + "</Machines>");
    }

    private void buildMachine(MachineType machine)
    {
	System.err.println(blanks.substring(0,nbrOfBlanks) + "<Machine machineType=\"" + machine.getType() + "\" " + "name=\"" + machine.getName() + "\">");
	nbrOfBlanks++;

	// Description
	if (machine.getDescription()!=null)
	    {
		System.err.println(blanks.substring(0,nbrOfBlanks) + "<Description>" + machine.getDescription() + "</Description>");
	    }
	// Variables
	if (machine.getVariables()!=null)
	    {
		VariablesType variables = machine.getVariables();
		System.err.println(blanks.substring(0,nbrOfBlanks) + "<Variables>");
		// Nbr of Variables
		List variableList = variables.getVariable();
		nbrOfBlanks++;
		for (Iterator variableIter = variableList.iterator();variableIter.hasNext();)
		    {
			// Since there is a class Variable this XML simpletype should be represented by a Variable object,
			// but it is a String.
			// In JABX version 2.0 there will be an opportunity to set mapSimpleTypeDef="true" and that will
			// hopefully sort this out
			//Variable currentVariable = (Variable) variableIter.next();
			String currentVariable = (String) variableIter.next();
			//System.err.println(blanks.substring(0,nbrOfBlanks) + "<Variable>" + currentVariable.getValue() + "</Variable>");
			System.err.println(blanks.substring(0,nbrOfBlanks) + "<Variable>" + currentVariable + "</Variable>");
		    }
		nbrOfBlanks--;
		System.err.println(blanks.substring(0,nbrOfBlanks) + "</Variables>");
	    }

	// Equipment
	if (machine.getEquipment()!=null)
	    {
		buildEquipment(machine.getEquipment());
	    }

	nbrOfBlanks--;
	System.err.println(blanks.substring(0,nbrOfBlanks) + "</Machine>");
    }



    private void buildEquipment(EquipmentType equip)
    {
	System.err.println(blanks.substring(0,nbrOfBlanks) + "<Equipment>");

	// Nbr of EquipmentEntities
	List equipList = equip.getEquipmentEntity();
	nbrOfBlanks++;
	for (Iterator equipIter = equipList.iterator();equipIter.hasNext();)
	    {
		EquipmentEntityType currentEquip = (EquipmentEntityType) equipIter.next();
		buildEquipmentEntity(currentEquip);
	    }
	nbrOfBlanks--;
	System.err.println(blanks.substring(0,nbrOfBlanks) + "</Equipment>");
    }

    private void buildEquipmentEntity(EquipmentEntityType equipEnt)
    {
	System.err.println(blanks.substring(0,nbrOfBlanks) + "<EquipmentEntity equipmentType=\"" + equipEnt.getType() + "\" " + "name=\"" + equipEnt.getName() + "\">");
	nbrOfBlanks++;

	// Description
	if (equipEnt.getDescription()!=null)
	    {
		System.err.println(blanks.substring(0,nbrOfBlanks) + "<Description>" + equipEnt.getDescription() + "</Description>");
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
		// but it is a String.
		// In JABX version 2.0 there will be an opportunity to set mapSimpleTypeDef="true" and that will
		// hopefully sort this out
		//State currentState = (State) stateIter.next();
		String currentState = (String) stateIter.next();
		//System.err.println(blanks.substring(0,nbrOfBlanks) + "<State>" + currentState.getValue() + "</State>");
		System.err.println(blanks.substring(0,nbrOfBlanks) + "<State>" + currentState + "</State>");
	    }
	nbrOfBlanks--;
	System.err.println(blanks.substring(0,nbrOfBlanks) + "</States>");

	// Elements
	if (equipEnt.getElements()!=null)
	    {
		buildElements(equipEnt.getElements());
	    }

	// Equipment
	if (equipEnt.getEquipment()!=null)
	    {
		buildEquipment(equipEnt.getEquipment());
	    }

	nbrOfBlanks--;
	System.err.println(blanks.substring(0,nbrOfBlanks) + "</EquipmentEntity>");


	// Instatiating the corresponding Automation Object
	try
	    {

		// Check if it is a sensor EquipmentEntity
		if (equipEnt.getType().compareTo("Sensor") == 0)
		    {
			AO currentAO = objFactory.createAO();
			// there is no setAO method in AOApplication but the getAO method returns the actual list
			aoApplication.getAO().add(currentAO);
			currentAO.setName(equipEnt.getType() + "_" + equipEnt.getName());

			System.err.println("This is a sensor!");
			currentAO.setType("Sensor.aot");

			// Instantiate the Views, model and controller for the sensor
			Model model = objFactory.createModel();
			model.setType("Sensor_Model.fbt");
			currentAO.setModel(model);

			Controller controller = objFactory.createController();
			controller.setType("StateMonitor_Controller.fbt");
			controller.setName("StateMonitor_Controller");
			// there is no setController method in AO but the getController method returns the actual list
			currentAO.getController().add(controller);

			View view1 = objFactory.createView();
			view1.setType("StateRequest_View.fbt");
			view1.setName("StateRequest_View");
			currentAO.getView().add(view1);
			View view2 = objFactory.createView();
			view2.setType("StateCheck_View.fbt");
			view2.setName("StateCheck_View");
			currentAO.getView().add(view2);
			View view3 = objFactory.createView();
			view3.setType("Sensor_StateOrder_View.fbt");
			view3.setName("Sensor_StateOrder_View");
			currentAO.getView().add(view3);
			View view4 = objFactory.createView();
			view4.setType("Sensor_StateMonitor_View.fbt");
			view4.setName("StateMonitor_View");
			currentAO.getView().add(view4);

			//InterfaceListType interfaceList = objFactory.createInterfaceList();
			//EventInputsType eventInputs = objFactory.createEventInputs();
			//JaxbEvent eventREQ = objFactory.createJaxbEvent();
			//eventREQ.setName("REQ");
			// there is no setEvent method in EventInputs but the getEvent method returns the actual list
			//eventInputs.getEvent().add(eventREQ);
			//interfaceList.setEventInputs(eventInputs);
			//FB.setInterfaceList(interfaceList);

			// The FB needs to have versionInfo
			//net.sourceforge.fuber.xsd.libraryelement.VersionInfoType versionInfo = objFactory.createVersionInfo();
			//versionInfo.setOrganization("Chalmers");
			//versionInfo.setVersion("0.1");
			//versionInfo.setAuthor("Oscar Ljungkrantz");
			//versionInfo.setDate((new java.util.Date()).toLocaleString());
			// there is no setVersionInfo method in FBType but the getVersionInfo method returns the actual list
			//FB.getVersionInfo().add(versionInfo);
			// Create an xml-file of the AO
			XMLCreator xmlCreator = new XMLCreator();
			xmlCreator.createXMLFile(aoApplication,"C:/Documents and Settings/oscar/My Documents/Supremica/examples/functionblocks/FBRuntime/manufacturingTables" , "test.aoa");
		    }
	    }
	catch (JAXBException je)
	    {
		je.printStackTrace();
	    }

    }


    private void buildElements(ElementsType elements)
    {
	System.err.println(blanks.substring(0,nbrOfBlanks) + "<Elements>");

	// Nbr of Elements
	List elementList = elements.getElement();
	nbrOfBlanks++;
	for (Iterator elementIter = elementList.iterator();elementIter.hasNext();)
	    {
		ElementType currentElement = (ElementType) elementIter.next();
		buildElement(currentElement);
	    }
	nbrOfBlanks--;
	System.err.println(blanks.substring(0,nbrOfBlanks) + "</Elements>");
    }

    private void buildElement(ElementType element)
    {
	//	System.err.println(blanks.substring(0,nbrOfBlanks) + "<Element elementType=\"" + element.getType() + "\" " + "name=\"" + element.getName() + "\">");
	nbrOfBlanks++;

	// Description
	if (element.getDescription()!=null)
	    {
		System.err.println(blanks.substring(0,nbrOfBlanks) + "<Description>" + element.getDescription() + "</Description>");
	    }

	nbrOfBlanks--;
	System.err.println(blanks.substring(0,nbrOfBlanks) + "</Element>");
    }

}
