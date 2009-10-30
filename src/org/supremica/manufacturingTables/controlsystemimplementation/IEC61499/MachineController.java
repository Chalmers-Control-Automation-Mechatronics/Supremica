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
 * The abstract class MachineController is the part of the machine that handles the controll of the machine, 
 * either as a complete control system or by forwarding the operations to the machines own control system, as
 * with for example robots. 
 *
 *
 * Created: Tue Oct 30 12:05 2006
 *
 * @author Oscar
 * @version 1.0
 */
package org.supremica.manufacturingTables.controlsystemimplementation.IEC61499;

import java.util.HashMap;
import java.util.Map;
import java.util.LinkedList;
import java.util.List;
import java.util.Iterator;
import java.util.Map.Entry;

abstract public class MachineController 
{
    protected Machine machine;
    protected boolean performsEOP; // can only perform one EOP at a time
    protected String ID;
    protected Map<Comparable, List<EOP>> EOPs;  // HashMap will be used for quick access to the states
    protected EOPRow currentEOPRow;
    protected EOP currentEOP;
    protected Map<EOP, EOPInitialRow> currAltEOPToInitRowMap; 
    // Current AlternativeEOP_To_InitialRow Map. This is a map that maps all possible alternative EOPs to the
    // current clone of their initial rows.
    // Hence the initial row in the maps can be checked and the elements removed as I request the states of the machine
    // and compares to the different alternative EOPs.

    public MachineController()
    {
	performsEOP = false;
	ID = "MachineController";
	currentEOPRow = null;
	currentEOP = null;
	EOPs = new HashMap<Comparable, List<EOP>>(); // default capacity (16) and load factor (0,75) suits me fine
	currAltEOPToInitRowMap = new HashMap<EOP, EOPInitialRow>(6); // initial capacity (6) 
    }

    // abstract public boolean performEOP(String EOPName);
    abstract public void performEOP(String EOPName);
        
    final public String getID()
    {
	return ID;
    }
    

    // Check if the value/state is the current value/state of the specified component of this machine.
    abstract public ComponentConfirmation checkComponent(ComponentCheck componentCheck);
    
    // confirmation from another machine whether the external component holds the desired value/state or not.
    public void confirmExternalComponent(String externalMachine, ComponentConfirmation compConf)
    {
//  	if (performsEOP)
// 	{
// 	    if (compConf != null && compConf.getConfirmation())
// 	    {
// 		// ExternalComponent
// 		// Create a new EOPExternalComponent which should be equal to the one in the map
// 		EOPExternalComponent confirmedComponent = new EOPExternalComponent(compConf.getComponentName(), 
// 										   externalMachine);
// 		if (currentEOPRow.getExternalComponentToStateMap().containsKey(confirmedComponent))
// 		{
// 		    System.err.println("The machine " + externalMachine + " confirmed the component " 
// 				       + compConf.getComponentName());
// 		    currentEOPRow.getExternalComponentToStateMap().remove(confirmedComponent); 
// 		    checkEOPRowPerformed();
// 		}		
// 		else
// 		{
// 		    System.err.println("Unknown external component " + compConf.getComponentName() + " for machine controller in machine " + machine.getName() + ".");
// 		}
// 	    }
// 	    else 
// 	    {
// 		System.err.println("The Machine " + machine.getName() + " has to stop due to component errors in another machine!");
// 		performsEOP = false;
// 	    }
// 	}
// 	else 
// 	{
// 	    System.err.println("This MachineController is not performing an EOP and is not interrested in other machine´s components!");
// 	}
    }

    // Request the value/state of the specified component of this machine.
    abstract public ComponentReport requestComponent(String component);

    // Check if the EOP row has been performed. If so perform the next row. If no more rows, the EOP is done.
    abstract protected void checkEOPRowPerformed();

    
    // Response from another machine with the state of the external component.
    public void reportExternalComponent(String externalMachine, ComponentReport compReport)
    {
// 	if (performsEOP)
// 	{
// 	    // Create a new EOPExternalComponent which state/value should be compared to the ones in the
// 	    // alternative EOPs
// 	    EOPExternalComponent reqComponent = new EOPExternalComponent(compReport.getComponentName(),
// 									 externalMachine);
// 	    for (Iterator<Entry<EOP, EOPInitialRow>> EOPIter = currAltEOPToInitRowMap.entrySet().iterator(); 
// 		 EOPIter.hasNext();)
// 	    {
// 		EOPRow EOPRow = EOPIter.next().getValue();
// 		// External Component
// 		if ( EOPRow.getExternalComponentToStateMap().containsKey( reqComponent ) )
// 		{
// 		    String state = (String) EOPRow.getExternalComponentToStateMap().remove( reqComponent );

// 		    if (! (state.equals(compReport.getValue()) || state.equals(EOP.IGNORE_TOKEN) ) )
// 		    {
// 			EOPIter.remove();
// 		    }
// 		}		
// 	    }	
// 	    if (currAltEOPToInitRowMap.size() == 1)
// 	    {
// 		Entry<EOP, EOPInitialRow> EOPToInitRowEntry = currAltEOPToInitRowMap.entrySet().iterator().next();
// 		currentEOP = EOPToInitRowEntry.getKey();
// 		currentEOPRow = EOPToInitRowEntry.getValue();
// 		System.out.println("Only one alternative left!");
		
// 		checkEOPRowPerformed();
// 	    }
	    
// 	}
// 	else 
// 	{
// 	    System.err.println("This MachineController is not performing an EOP and is not interrested in other machine´s components!");
// 	}
    }
    
    // Report from the zone about the state of the zone.
    public void reportZone(String zone, String state)
    {
 	if (performsEOP)
 	{
 	    // Compare the zone and state with the ones in the alternative EOPs
 	    for (Iterator<Entry<EOP, EOPInitialRow>> EOPIter = currAltEOPToInitRowMap.entrySet().iterator(); 
 		 EOPIter.hasNext();)
 	    {
 		EOPRow EOPRow = EOPIter.next().getValue();
 		// Zones
 		if ( EOPRow.getZoneToStateMap().containsKey( zone ) )
 		{
 		    String currentState = (String) EOPRow.getZoneToStateMap().remove( zone );

 		    if (! ( currentState.equals(state) || currentState.equals(EOP.IGNORE_TOKEN) ) )
 		    {
 			EOPIter.remove();
 		    }
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
 	else 
 	{
 	    System.err.println("This MachineController is not performing an EOP and is not interrested in zones!");
 	}
    }

    // Confirmation from the zone whether the zone was successfully ordered/checked.
    public void confirmZone(String zone, boolean confirmation)
    {
 	if (performsEOP)
 	{
 	    // Check that the confirmation is true. It should always be true in automatic mode because the EOPs are 
 	    // scheduled that way. Still, in the future it may be possible to instead wait for a zone to be unbooked
 	    // if it should be occupied by another machine.
 	    if (confirmation)
 	    {
 		if (currentEOPRow.getZoneToStateMap().containsKey(zone))
 		{
 		    System.err.println("The zone " + zone + " was confirmed OK to be in state " + 
 				       currentEOPRow.getZoneToStateMap().remove(zone) );
 		    checkEOPRowPerformed();
 		}
 		else
 		{
 		    System.err.println("Unknown zone " + zone + " for machine controller in machine " 
 				       + machine.getName() + ".");
 		}
 	    }
 	    else
 	    {
 		System.err.println("The Machine " + machine.getName() + " has to stop since the zone "
 				   + zone + "was occupied by another machine.");
 		performsEOP = false;
 	    }
 	}
 	else 
 	{
	    System.err.println("This MachineController is not performing an EOP and is not interrested in zones!");
 	}
    }
    
    final public void setMachine(Machine machine)
    {
	this.machine = machine;
    }
    
    final public Machine getMachine()
    {
	return machine;
    }

    // Add/register an eop to the machine controller. For alternative EOPs many EOPs can have the same operation
    // number and those are stored in a list
     final public void registerEOP(EOP eop)  
     {
 	List<EOP> EOPList = EOPs.get(eop.getId());
	if (EOPList != null && eop.getType().equals(EOP.ALTERNATIVE_TYPE))
 	{
 	    EOPList.add(eop);
 	}
 	else if (EOPList == null)
 	{
 	    EOPList = new LinkedList<EOP>();
 	    EOPList.add(eop);
 	    EOPs.put(eop.getId(), EOPList);
 	}
 	else
 	{
 	    System.err.println("Warning: Can not have multiple basic EOPs!");
 	}
     }
    
}