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
 * The class MachineControlCommunicator is for machines with own control systems, for instance robots, and this
 * class handles the communication between the cell control program and the machines own control system.
 *
 *
 * Created: Tue Jun 13 16:24:20 2006
 *
 * @author Oscar
 * @version 1.0
 */
package org.supremica.manufacturingTables.controlsystemimplementation.Java;

import java.util.List;
import java.util.Map.Entry;
import java.util.Iterator;
import java.io.*;

public class MachineControlCommunicator extends MachineController 
{
    private boolean EOPPerformedOK; // to tell the machine whether the EOP was performed successfully or not

    public MachineControlCommunicator()
    {
	super();
  	EOPPerformedOK = false;
  }
    
    public boolean performEOP(String EOPName)
    {
	// The line below can be changed to test only the COPs and not perform the actual EOPs
	if (EOPName==null)
	    return false;


	EOPPerformedOK = false; // this means that if the EOP is empty, a false will be returned

	List<EOP> EOPList = EOPs.get(EOPName);
	if ( EOPList!=null && !performsEOP )
	{
	    System.out.println("Machine with own control system performing EOP: " + EOPName);
	    performsEOP = true;
	    
	    // Put all alternative EOPs in a map with their initial rows
	    currAltEOPToInitRowMap.clear();  
	    for(EOP EOP: EOPList)
	    {
		currAltEOPToInitRowMap.put(EOP,(EOPInitialRow) EOP.getEOPInitialRowClone());
	    }

	    // Start with the first alternative
	    // I thereby require that all alternatives have the same components, it´s just the values/states
	    // that may differ
	    currentEOP = EOPList.get(0);

	    // EOP Initial Row
	    // For basic EOPs I ignore components and variables who's state/value is unimportant (EOP.IGNORE_TOKEN).
	    currentEOPRow = currentEOP.getEOPInitialRowActions();
	    if (currentEOP.getType().equals(EOP.ALTERNATIVE_TYPE))
	    {
		// For alternative EOPs I will ask for the state of all components and variables.
		currentEOPRow = currentEOP.getEOPInitialRowClone();
	    }
	    currentEOP.startActions();
	    performInitialRow();
	}		
	else 
	{
	    System.err.println("Unknown EOP or already busy performing an EOP!");
	}
	// In the real control program some communication with the real machine has to be handled ..!
	return EOPPerformedOK;
    }
    
    
    // Perform the control activities according to the initial EOP row, stored as currentEOPRow.
    // The activities are different for different EOP types but for a machine with own control system 
    // it should only be asking the value for external components and zones and communicating with 
    // the machine about internal variables.
    private void performInitialRow()
    {
	// I make a clone of the current EOP row to iterate through. 
	EOPInitialRow initialRow = (EOPInitialRow) ( (EOPInitialRow) currentEOPRow ).clone(); 	
	
	// External Component
	for (Iterator externalIter = initialRow.getExternalComponentToStateMap().entrySet().iterator(); externalIter.hasNext();)
	{
	    Entry componentToState = (Entry) externalIter.next();
	    EOPExternalComponent extComp = (EOPExternalComponent) componentToState.getKey();
	    String state = (String) componentToState.getValue();
	    if (currentEOP.getType().equals(EOP.ALTERNATIVE_TYPE))
	    {
		machine.requestExternalComponent(extComp.getMachine(), extComp.getComponentName());
	    }
	    else 
	    {
		machine.checkExternalComponent(extComp.getMachine(), extComp.getComponentName(), state);
	    }
	}
	
	// Variables, basic and alternative EOP
	for (Iterator variableIter = initialRow.getVariableToValueMap().entrySet().iterator(); variableIter.hasNext();)
	{
	    Entry variableToValue = (Entry) variableIter.next();
	    if (currentEOP.getType().equals(EOP.ALTERNATIVE_TYPE))
	    {
		System.err.println("Requesting the value of the variable " + (String) variableToValue.getKey() 
				   + ", in the machine " + machine.getName()); 
	    }
	    else
	    {
		System.err.println("Checking the value of the variable " + (String) variableToValue.getKey() 
				   + ", in the machine " + machine.getName() 
				   + ", which should be " + (String) variableToValue.getValue() + ".");
	    }	    
	    try
	    {
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		
		//String newValue = null;
		// Here some testing could be done to check that this is a correct value of the variable
		// but that requires us to know all variables and values, which may a range of integers.
		//while (!((String) variableToValue.getValue()).equals(newState))
		System.out.print("Type the state for variable " + variableToValue.getKey() + ": ");
		System.out.flush();
		String readValue =  in.readLine();

		// Alternative EOP
		if (currentEOP.getType().equals(EOP.ALTERNATIVE_TYPE))
		{
		    for (Iterator<Entry<EOP, EOPInitialRow>> EOPIter = currAltEOPToInitRowMap.entrySet().iterator(); 
			 EOPIter.hasNext();)
		    {
			EOPRow EOPRow = EOPIter.next().getValue();
			
			if ( EOPRow.getVariableToValueMap().containsKey( variableToValue.getKey() ) )
			{
			    String value = (String) EOPRow.getVariableToValueMap().remove( variableToValue.getKey() );
			    System.out.println("EOP " + currentEOP.getId() + ": variable " + variableToValue.getKey()
					       + " with value " + value + " was removed."); 
			    if (! ( value.equals(readValue) || value.equals(EOP.IGNORE_TOKEN) ) )
			    {
				EOPIter.remove();
				System.out.println("The entire alternative EOP was removed");
			    }
			}		
		    }		    
		}
		// Basic EOP
		else {
		    if (((String) variableToValue.getValue()).equals(readValue))
		    {
			// remove the variable from the current EOP row (not the clone)
			currentEOPRow.getVariableToValueMap().remove((String) variableToValue.getKey());
		    }
		    else 
		    {
			System.err.println("Broken equipment: The machine's variable value was not the expected one, the machine has to stop");
			System.err.println("The Machine has to stop due to component errors!");
			performsEOP = false;
			
			//newValue = null;
		    }
		}
	    }
	    catch(IOException exception)
	    {
		System.err.println("IOException, could not read the input from the keybord!");
	    }
	}
	// Zones
	for (Iterator zoneIter = initialRow.getZoneToStateMap().entrySet().iterator(); zoneIter.hasNext();)
	{
	    Entry zoneToState = (Entry) zoneIter.next();
	    if (currentEOP.getType().equals(EOP.ALTERNATIVE_TYPE))
	    {
		machine.requestZone( (String) zoneToState.getKey() );
	    }
	    else
	    {
		machine.checkZone( (String) zoneToState.getKey(), (String) zoneToState.getValue() );
	    }
	}


	if (currAltEOPToInitRowMap.size() == 1)
	{
	    Entry<EOP, EOPInitialRow> EOPToInitRowEntry = currAltEOPToInitRowMap.entrySet().iterator().next();
	    currentEOP = EOPToInitRowEntry.getKey();
	    currentEOPRow = EOPToInitRowEntry.getValue();
	    System.out.println("Only one alternative left!");
	    
	    checkEOPRowPerformed();
	}
    }
    
    

    // Perform the control activities according to the current actionRow, with only changing variables and zones 
    // left, stored as currentEOPRow
    private void performActions()
    {
	EOPActionRow actionRow = (EOPActionRow) ( (EOPActionRow) currentEOPRow ).clone(); 	
	
	// Variables
	for (Iterator variableIter = actionRow.getVariableToValueMap().entrySet().iterator(); variableIter.hasNext();)
	{
	    Entry variableToValue = (Entry) variableIter.next();
	    System.err.println("Ordering the machine " + machine.getName() + " to change the value of the variable " 
			       + (String) variableToValue.getKey() + " to " 
			       + (String) variableToValue.getValue() + ".");
	    // Right now we have no confirmation from the machine that a variable is set correctly so we assume it was
	    // and removes the variables from the current EOPRow (not the clone)
	    currentEOPRow.getVariableToValueMap().remove((String) variableToValue.getKey());
	}
	
	
	
	// The same EOP format is used for machines with own control systems. Still there are restrictions on
	// the way an eop is interpreted and performed. It first performs the eop rows which should only
	// include external components and own variables and booking zones. Then it tells the real machine to perform
	// the operation and finally it performs any unbooking of zones which are suppossed to be on the final, 
	// second, row.

	// Zones
	for (Iterator zoneIter = actionRow.getZoneToStateMap().entrySet().iterator(); zoneIter.hasNext();)
	{
	    Entry zoneToState = (Entry) zoneIter.next();
	    // "Booking zones and not last row" or "Unbooking zones and last row"
	    if ( ( ((String) zoneToState.getValue()).equals(Zone.BOOKED_ZONE_TOKEN) && currentEOP.hasMoreActions() ) ||
		 ( ((String) zoneToState.getValue()).equals(Zone.FREE_ZONE_TOKEN) && !currentEOP.hasMoreActions() ) )
	    {
		machine.orderZone( (String) zoneToState.getKey(), (String) zoneToState.getValue() );
	    }
	    else 
	    {
		System.err.println("Error: Can only unbook zones at the last EOP row, and may not book zones "
				   + "at the last row, for machines with own control system!");
		performsEOP = false;
	    }
	}
	
	
	// The following line is necessary if one EOP row is equal to the last 
	checkEOPRowPerformed();
    }
    
    // Check if the EOP row has been performed. If so perform the next row. If no more rows, the EOP is done.
    protected void checkEOPRowPerformed()
    {
	if ( performsEOP 
	     && currentEOPRow.getVariableToValueMap().size() == 0 
	     && ( currentEOPRow.getExternalComponentToStateMap() == null || 
		  currentEOPRow.getExternalComponentToStateMap().size() == 0 ) 
	     && ( currentEOPRow.getZoneToStateMap().size() == 0 ) )
	{
	    System.err.println("The EOP row has been performed.");

	    // The same EOP format is used for machines with own control systems. Still there are restrictions on
	    // the way an eop is interpreted and performed. 
	    // No row can contain actuators and sensors. 
	    // Besides that, the initial row may look like the initial row for other machines.
	    // The first action row is interpreted as things (booking zones, variables...) that should be 
	    // performed before the actual machine performs the operation.
	    // Then the actual operation is performed by the real machine.
	    // Finally, on the second, optional row any operations (unbooking zones) that is to be performed
	    // after the real machines operation, is performed.
	    if ( (!currentEOP.hasMoreActions() && currentEOP.getCurrentRowIndex()<2) ||
		 (currentEOP.hasMoreActions() && currentEOP.getCurrentRowIndex()==1) )
		
	    {
		// The EOP is mostly done. Now we have to communicate with the real machine and
		// after that perform the last row (unbook any zones) if there is such a row.

		// ...Sending the message to the real machine to tell it to perform the operation and wait for
		// it to be performed....
		System.out.println("Telling machine with own control system to perform operation " 
				   + currentEOP.getId());
	    }
	    if (!currentEOP.hasMoreActions())
	    {
		    performsEOP = false;
		    System.err.println("The EOP is done!");
		    EOPPerformedOK = true;
	    }
	    else 
	    {
		currentEOPRow = currentEOP.getNextActions();
		performActions();
	    }
	}
    }
    
    
    public ComponentConfirmation checkComponent(ComponentCheck componentCheck)
    {
	System.err.println("Checking the value of the component " + componentCheck.getComponentName() 
			   + ", in the machine " + machine.getName() 
			   + ", which should be " + componentCheck.getValueToCheck() + ".");

	ComponentConfirmation confirmation = null;
	try
	{
	    BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	    
	    System.out.print("Type the state for component " + componentCheck.getComponentName() + ": ");
	    System.out.flush();
	    String newValue =  in.readLine();
	    
	    if (componentCheck.getValueToCheck().equals(newValue))
	    {
		confirmation = new ComponentConfirmation(componentCheck.getComponentName(), true);
	    }
	    else 
	    {
		confirmation = new ComponentConfirmation(componentCheck.getComponentName(), false);
	    }
	}
	catch(IOException exception)
	{
	    System.err.println("IOException, could not read the input from the keybord!");
	}
	return confirmation;
    }
    
    public ComponentReport requestComponent(String componentName)
    {
	System.err.println("Requesting the value of the component " + componentName 
			   + ", in the machine " + machine.getName() + ".");
	
	ComponentReport report = null;
	try
	{
	    BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	    
	    System.out.print("Type the state for component " + componentName + ": ");
	    System.out.flush();
	    String newValue =  in.readLine();
	    report = new ComponentReport(componentName, newValue);
	}
	catch(IOException exception)
	{
	    System.err.println("IOException, could not read the input from the keybord!");
	}
	return report;
    }
    
}