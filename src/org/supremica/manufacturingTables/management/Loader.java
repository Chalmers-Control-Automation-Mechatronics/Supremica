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

import java.io.File;
import java.io.FileInputStream;
import java.util.Iterator;
import java.util.List;
import java.io.IOException;
import javax.xml.bind.*;
import org.supremica.manufacturingTables.xsd.factory.*;

import org.supremica.properties.SupremicaProperties;


public class Loader
{
    private JAXBContext jaxbContext;
    private Unmarshaller u;
    
    // variabel för att hålla reda på  tabuleringen, bara för utskriften av XML-koden tills vidare
    protected int nbrOfBlanks;
    protected String blanks;

    public Loader()
    {
	try
	    {
		System.err.println("Entered the Loader constructor.");
		//		jaxbContext = JAXBContext.newInstance("org.supremica.manufacturingTables.xsd.factory", this.getClass().getClassLoader());
		jaxbContext = JAXBContext.newInstance("org.supremica.manufacturingTables.xsd.factory");
		System.err.println("jaxbcontext skapat");
		u = jaxbContext.createUnmarshaller();
		System.err.println("unmarshaller skapat");
		// enable validation
		u.setValidating( true );
		System.err.println("validation satt till true");

		// We will allow the Unmarshaller's default
		// ValidationEventHandler to receive notification of warnings
		// and errors which will be sent to System.out.  The default
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
			System.err.println("filen unmarshallad");

			//return (FactoryType) o;
			buildPLCProgram((FactoryType) o);
		    }
		else
		    {
			System.err.println("Problems reading the file!");
		    }
	    }
	catch(UnmarshalException ue)
	    {
		System.out.println("Invalid XML code (UnmarshalException)" );
		ue.printStackTrace();
	    }
	catch(JAXBException je)
	    {
		System.err.println("JAXBException caucht!");
		je.printStackTrace();
	    }
	return;
    }

    private void buildPLCProgram(FactoryType factory)
    {
	// variabel för att hålla reda på  tabuleringen, bara för utskriften av XML-koden
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

			// Equipment
			if (currentCell.getEquipment()!=null)
			    {
				buildEquipment(currentCell.getEquipment());
			    }

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
	System.err.println(blanks.substring(0,nbrOfBlanks) + "<Machine machineType=\"" + machine.getMachineType() + "\" " + "name=\"" + machine.getName() + "\">");
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
			// Eftersom det finns en variable-klass borde detta simpletype vara en Variable, men det är en String
			// I version 2.0 av JAXB kommer man ha möjlighet att sätta mapSimpleTypeDef="true" som förhoppningsvis 
			// råder bot på detta
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
	System.err.println(blanks.substring(0,nbrOfBlanks) + "<EquipmentEntity equipmentType=\"" + equipEnt.getEquipmentType() + "\" " + "name=\"" + equipEnt.getName() + "\">");
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
		// Eftersom det finns en state-klass borde detta simpletype vara ett State, men det är en String
		// I version 2.0 av JAXB kommer man ha möjlighet att sätta mapSimpleTypeDef="true" som förhoppningsvis 
		// råder bot på detta
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
	System.err.println(blanks.substring(0,nbrOfBlanks) + "<Element elementType=\"" + element.getElementType() + "\" " + "name=\"" + element.getName() + "\">");
	nbrOfBlanks++;

	// Description
	if (element.getDescription()!=null)
	    {
		System.err.println(blanks.substring(0,nbrOfBlanks) + "<Description>" + element.getDescription() + "</Description>");
	    }

	nbrOfBlanks--;
	System.err.println(blanks.substring(0,nbrOfBlanks) + "</Element>");
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
			System.err.println("The specified directory is invalid!");
			return null;
		    }
	    }
	if (theFile.isFile())
	    {
		return theFile;
	    }
	else
	    {
		System.err.println("The file does not exist in the specified directory!");
		return null;
	    }
    }
}
