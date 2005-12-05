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
 * Created: Tue Nov  23 13:49:32 2005
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
import org.supremica.functionblocks.xsd.libraryelement.*;
import org.supremica.functionblocks.xsd.libraryelement.impl.*;
import org.supremica.properties.SupremicaProperties;


public class Loader
{
    private JAXBContext jaxbContext;
    private Unmarshaller u;
    
    // Variable to keep track of the indentation, just for now for printing the XML code
    protected int nbrOfBlanks;
    protected String blanks;

    public Loader()
    {
	try
	    {
		java.lang.System.err.println("Entered the Loader constructor.");
		jaxbContext = JAXBContext.newInstance("org.supremica.manufacturingTables.xsd.factory");
		java.lang.System.err.println("jaxbcontext skapat");
		u = jaxbContext.createUnmarshaller();
		java.lang.System.err.println("unmarshaller skapat");
		// enable validation
		u.setValidating( true );
		java.lang.System.err.println("validation satt till true");

		// We will allow the Unmarshaller's default
		// ValidationEventHandler to receive notification of warnings
		// and errors which will be sent to java.lang.System.err.  The default
		// ValidationEventHandler will cause the unmarshal operation
		// to fail with an UnmarshalException after encountering the
		// first error or fatal error.
	    }
	catch(JAXBException je)
	    {
		je.printStackTrace();
	    }
    }

    public void load(String path, String fileName)
    {
	try
	    {
		File theFile = getFile(path, fileName);
	       

		if(theFile!=null)
		    {
			// Unmarshall from the file with the fileName
			Object o = u.unmarshal(theFile);
			java.lang.System.err.println("filen unmarshallad");

			// return (FactoryType) o;
			buildPLCProgram((FactoryType) o);
		    }
		else
		    {
			java.lang.System.err.println("Problems reading the file!");
		    }
	    }
	catch(UnmarshalException ue)
	    {
		java.lang.System.err.println("Invalid XML code (UnmarshalException)" );
		ue.printStackTrace();
	    }
	catch(JAXBException je)
	    {
		java.lang.System.err.println("JAXBException caught!");
		je.printStackTrace();
	    }
	return;
    }

    private void buildPLCProgram(FactoryType factory)
    {
	// Variable to keep track of the indentation, just for now for printing the XML code
	nbrOfBlanks = 0;
	blanks = "                                                                              ";
	// Factory
	java.lang.System.err.println(blanks.substring(0,nbrOfBlanks) + "<Factory name=\"" + factory.getName() + "\">");
	if(factory.getDescription()!=null)
	    {
		java.lang.System.err.println(blanks.substring(0,nbrOfBlanks) + "<Description>" + factory.getDescription() + "</Description>");
	    }
	// Areas
	nbrOfBlanks++;
	AreasType areas = factory.getAreas();
	java.lang.System.err.println(blanks.substring(0,nbrOfBlanks) + "<Areas>");

	// Nbr of Areas
	List areaList = areas.getArea();
	nbrOfBlanks++;
	for (Iterator areaIter = areaList.iterator();areaIter.hasNext();)
	    {
		AreaType currentArea = (AreaType) areaIter.next();
		java.lang.System.err.println(blanks.substring(0,nbrOfBlanks) + "<Area name=\"" + currentArea.getName() + "\">");
		if(currentArea.getDescription()!=null)
		    {
			java.lang.System.err.println(blanks.substring(0,nbrOfBlanks) + "<Description>" + currentArea.getDescription() + "</Description>");
		    }
		// Cells
		nbrOfBlanks++;
		CellsType cells = currentArea.getCells();
		java.lang.System.err.println(blanks.substring(0,nbrOfBlanks) + "<Cells>");

		// Nbr of Cells
		List cellList = cells.getCell();
		nbrOfBlanks++;
		for (Iterator cellIter = cellList.iterator();cellIter.hasNext();)
		    {
			CellType currentCell = (CellType) cellIter.next();
			java.lang.System.err.println(blanks.substring(0,nbrOfBlanks) + "<Cell name=\"" + currentCell.getName() + "\">");
			nbrOfBlanks++;
			
			// Description
			if(currentCell.getDescription()!=null)
			    {
				java.lang.System.err.println(blanks.substring(0,nbrOfBlanks) + "<Description>" + currentCell.getDescription() + "</Description>");
			    }

			// Equipment
			if (currentCell.getEquipment()!=null)
			    {
				buildEquipment(currentCell.getEquipment());
			    }

			// Machines
			buildMachines(currentCell.getMachines());
			
			nbrOfBlanks--;
			java.lang.System.err.println(blanks.substring(0,nbrOfBlanks) + "</Cell>");
		    }
		nbrOfBlanks--;
		java.lang.System.err.println(blanks.substring(0,nbrOfBlanks) + "</Cells>");
		nbrOfBlanks--;
		java.lang.System.err.println(blanks.substring(0,nbrOfBlanks) + "</Area>");
		
	    }
	nbrOfBlanks--;
	java.lang.System.err.println(blanks.substring(0,nbrOfBlanks) + "</Areas>");
	nbrOfBlanks--;
	java.lang.System.err.println(blanks.substring(0,nbrOfBlanks) + "</Factory>");
    }
    
    private void buildMachines(MachinesType machines)
    {
	java.lang.System.err.println(blanks.substring(0,nbrOfBlanks) + "<Machines>");

	// Nbr of Machines
	List machineList = machines.getMachine();
	nbrOfBlanks++;
	for (Iterator machineIter = machineList.iterator();machineIter.hasNext();)
	    {
		buildMachine((MachineType) machineIter.next());
	    }
	nbrOfBlanks--;
	java.lang.System.err.println(blanks.substring(0,nbrOfBlanks) + "</Machines>");
    }

    private void buildMachine(MachineType machine)
    {
	java.lang.System.err.println(blanks.substring(0,nbrOfBlanks) + "<Machine machineType=\"" + machine.getMachineType() + "\" " + "name=\"" + machine.getName() + "\">");
	nbrOfBlanks++;

	// Description
	if (machine.getDescription()!=null)
	    {
		java.lang.System.err.println(blanks.substring(0,nbrOfBlanks) + "<Description>" + machine.getDescription() + "</Description>");
	    }
	// Variables
	if (machine.getVariables()!=null)
	    {
		VariablesType variables = machine.getVariables();
		java.lang.System.err.println(blanks.substring(0,nbrOfBlanks) + "<Variables>");
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
			//java.lang.System.err.println(blanks.substring(0,nbrOfBlanks) + "<Variable>" + currentVariable.getValue() + "</Variable>");
			java.lang.System.err.println(blanks.substring(0,nbrOfBlanks) + "<Variable>" + currentVariable + "</Variable>");
		    }
		nbrOfBlanks--;
		java.lang.System.err.println(blanks.substring(0,nbrOfBlanks) + "</Variables>");
	    }
	
	// Equipment
	if (machine.getEquipment()!=null)
	    {
		buildEquipment(machine.getEquipment());
	    }

	nbrOfBlanks--;
	java.lang.System.err.println(blanks.substring(0,nbrOfBlanks) + "</Machine>");
    }



    private void buildEquipment(EquipmentType equip)
    {
	java.lang.System.err.println(blanks.substring(0,nbrOfBlanks) + "<Equipment>");

	// Nbr of EquipmentEntities
	List equipList = equip.getEquipmentEntity();
	nbrOfBlanks++;
	for (Iterator equipIter = equipList.iterator();equipIter.hasNext();)
	    {
		EquipmentEntityType currentEquip = (EquipmentEntityType) equipIter.next();
		buildEquipmentEntity(currentEquip);
	    }
	nbrOfBlanks--;
	java.lang.System.err.println(blanks.substring(0,nbrOfBlanks) + "</Equipment>");
    }

    private void buildEquipmentEntity(EquipmentEntityType equipEnt)
    {
	java.lang.System.err.println(blanks.substring(0,nbrOfBlanks) + "<EquipmentEntity equipmentType=\"" + equipEnt.getEquipmentType() + "\" " + "name=\"" + equipEnt.getName() + "\">");
	nbrOfBlanks++;

	// Description
	if (equipEnt.getDescription()!=null)
	    {
		java.lang.System.err.println(blanks.substring(0,nbrOfBlanks) + "<Description>" + equipEnt.getDescription() + "</Description>");
	    }
	// States
	StatesType states = equipEnt.getStates();
	java.lang.System.err.println(blanks.substring(0,nbrOfBlanks) + "<States>");
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
		//java.lang.System.err.println(blanks.substring(0,nbrOfBlanks) + "<State>" + currentState.getValue() + "</State>");
		java.lang.System.err.println(blanks.substring(0,nbrOfBlanks) + "<State>" + currentState + "</State>");
	    }
	nbrOfBlanks--;
	java.lang.System.err.println(blanks.substring(0,nbrOfBlanks) + "</States>");

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
	java.lang.System.err.println(blanks.substring(0,nbrOfBlanks) + "</EquipmentEntity>");

	//Create an ObjectFactory to get instances of the interfaces
	org.supremica.functionblocks.xsd.libraryelement.ObjectFactory objFactory = new  org.supremica.functionblocks.xsd.libraryelement.ObjectFactory();
	// Create the corresponding FB
	try
	    {
		FBTypeType FB = objFactory.createFBType();
		FB.setName(equipEnt.getEquipmentType() + " " + equipEnt.getName());
		// Check if it is a sensor EquipmentEntity
		if (equipEnt.getEquipmentType().compareTo("Sensor") == 0)
		    {
			java.lang.System.err.println("Detta är en sensor!");
			InterfaceListType interfaceList = new InterfaceListImpl();
			EventInputsType eventInputs = new EventInputsImpl();
			EventType eventREQ = new EventImpl();
			eventREQ.setName("REQ");
			// there is no setEvent method in EventInputs but the getEvent method returns the actual list
			eventInputs.getEvent().add(eventREQ);
			interfaceList.setEventInputs(eventInputs);
			FB.setInterfaceList(interfaceList);		   
			
			// The FB needs to have versionInfo
			VersionInfoType versionInfo = new VersionInfoImpl();
			versionInfo.setOrganization("Chalmers");
			versionInfo.setVersion("0.1");
			versionInfo.setAuthor("Oscar Ljungkrantz");
			versionInfo.setDate((new java.util.Date()).toLocaleString());
			// there is no setVersionInfo method in FBType but the getVersionInfo method returns the actual list
			FB.getVersionInfo().add(versionInfo);
			// Create an xml-file of the FB
			createFBXMLFile((FBType) FB);
		    }
	    }
	catch (JAXBException je)
	    {
		je.printStackTrace();
	    }

    }

    private void createFBXMLFile(Object o)
    {
	try
	    {
	JAXBContext jaxbContext = JAXBContext.newInstance("org.supremica.functionblocks.xsd.libraryelement");

	Marshaller marshaller = jaxbContext.createMarshaller();

	//You can tell the Marshaller to format the resulting XML data with line breaks and indentation. The following statement turns this output format property on -- line breaks and indentation will appear in the output format: 
	marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,new Boolean(true));
 
	marshaller.marshal(o,new FileOutputStream("jaxbOutput.xml"));

	//Validation is not performed as part of the marshalling operation. In other words, unlike the case for unmarshalling, there is no setValidating method for marshalling. Instead, when marshalling data, you use the Validator class that is a part of the binding framework to validate a content tree against a schema. For example: 

	Validator validator = jaxbContext.createValidator();
	validator.validate(o);
	    }
	catch(PropertyException pe)
	    {
		java.lang.System.err.println("Couldn´t set the desired marshal properties!");
		pe.printStackTrace();
	    }
	catch(JAXBException je)
	    {
		java.lang.System.err.println("JAXBException caught!");
		je.printStackTrace();
	    }
	catch(FileNotFoundException fe)
	    {
		java.lang.System.err.println("Couldn´t create the file!");
		fe.printStackTrace();
	    }
    }

    private void buildElements(ElementsType elements)
    {
	java.lang.System.err.println(blanks.substring(0,nbrOfBlanks) + "<Elements>");

	// Nbr of Elements
	List elementList = elements.getElement();
	nbrOfBlanks++;
	for (Iterator elementIter = elementList.iterator();elementIter.hasNext();)
	    {
		ElementType currentElement = (ElementType) elementIter.next();
		buildElement(currentElement);
	    }
	nbrOfBlanks--;
	java.lang.System.err.println(blanks.substring(0,nbrOfBlanks) + "</Elements>");
    }

    private void buildElement(ElementType element)
    {
	java.lang.System.err.println(blanks.substring(0,nbrOfBlanks) + "<Element elementType=\"" + element.getElementType() + "\" " + "name=\"" + element.getName() + "\">");
	nbrOfBlanks++;

	// Description
	if (element.getDescription()!=null)
	    {
		java.lang.System.err.println(blanks.substring(0,nbrOfBlanks) + "<Description>" + element.getDescription() + "</Description>");
	    }

	nbrOfBlanks--;
	java.lang.System.err.println(blanks.substring(0,nbrOfBlanks) + "</Element>");
    }

    private File getFile(String path, String fileName)
    {
	File theFile = new File(fileName);
	if (path !=null)
	    {
		File pathFile = new File(path);
		if (pathFile.isDirectory())
		    {
			theFile = new File(path, fileName);
		    }
		else
		    {
			java.lang.System.err.println("The specified directory is invalid!");
			return null;
		    }
	    }
	if (theFile.isFile())
	    {
		return theFile;
	    }
	else
	    {
		java.lang.System.err.println("The file does not exist in the specified directory!");
		return null;
	    }
    }
}
