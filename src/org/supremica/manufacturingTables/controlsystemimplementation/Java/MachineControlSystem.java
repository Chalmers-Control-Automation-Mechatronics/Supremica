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
 * The MachineControlSystem reads the EOPs and sends the corresponding messages to the different parts of the machine. 
 * It contains information about which mailbox to send messages to. 
 *
 *
 * Created: Mon May 15 14:20:32 2006
 *
 * @author Oscar
 * @version 1.0
 */
package org.supremica.manufacturingTables.controlsystemimplementation.Java;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.List;
import java.util.Map.Entry;
import java.util.Iterator;
public class MachineControlSystem extends MachineController implements Listener 
{
    private Mailbox mailbox;
    private boolean EOPPerformedOK; // to tell the machine whether the EOP was performed successfully or not
    private Map<String, ComponentConfirmation> componentChecks; // stores which components are checked externally
    private Map<String, ComponentReport> componentRequests; // stores which components are requested externally

    public MachineControlSystem(Mailbox mailbox)
    {
	super();
	this.mailbox = mailbox;
	EOPPerformedOK = false;
	mailbox.register(this);
	componentChecks = new HashMap<String, ComponentConfirmation>(5); // initial capacity (5) and default load factor (0,75) suits me fine
	componentRequests = new HashMap<String, ComponentReport>(5);
    }
    
    public boolean performEOP(int EOPNbr)
    {
	// The line below can be changed to test only the SOPs and not perform the actual EOPs
	if (EOPNbr==0)
	    return true;

	EOPPerformedOK = false; // this means that if the EOP is empty, a false will be returned
	
	List<EOP> EOPList = EOPs.get(EOPNbr);
	if ( EOPList!=null && !performsEOP )
	{
	    System.out.println("Performing EOP: " + EOPNbr);
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
	
	return EOPPerformedOK;
    }
    
    // Perform the control activities according to the initial EOP row, stored as currentEOPRow.
    // The activities are different for different EOP types.
    private void performInitialRow()
    {
	// I make a clone of the current EOP row to iterate through so the orders can be sent to the sensors, 
	// actuators etc, be carried out, sent back to this MachineController and remove the performed element
	// from the current EOP row without interfering with this iterated clone.
	EOPInitialRow initialRow = (EOPInitialRow) ( (EOPInitialRow) currentEOPRow ).clone(); 	
	
	String stateInquiryType = "checkState"; 
	// For basic EOPs we should check that the machine is in the desired initial state
 	// (i.e. Initial Row + basic EOP -> checkState) 
 	if (currentEOP.getType().equals(EOP.ALTERNATIVE_TYPE))
	{
	    stateInquiryType = "requestState";
	    // For alternative EOPs we request the state of the machine and compares it with the different alternatives 
	    // (i.e. Initial Row + alternative EOP -> requestState)
	}
	
	// For now I dont care about the alarm types and delays
	// get Alarm Type and Alarm Delay
	
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
	
	// Actuators
	for (Iterator actuatorIter = initialRow.getActuatorToStateMap().entrySet().iterator(); actuatorIter.hasNext();)
	{
	    Entry actuatorToState = (Entry) actuatorIter.next();
	    sendMessage( new Message( ID,  (String) actuatorToState.getKey(), stateInquiryType, (String) actuatorToState.getValue() ) );
	}
	// Variables
	for (Iterator variableIter = initialRow.getVariableToValueMap().entrySet().iterator(); variableIter.hasNext();)
	{
	    Entry variableToValue = (Entry) variableIter.next();
	    sendMessage( new Message( ID,  (String) variableToValue.getKey(), stateInquiryType, (String) variableToValue.getValue() ) );
	}
	// Sensors
	for (Iterator sensorIter = initialRow.getSensorToStateMap().entrySet().iterator(); sensorIter.hasNext();)
	{
	    Entry sensorToState = (Entry) sensorIter.next();
	    // Initial Row + sensor -> monitorState
	    // State monitoring can be put on for all sensors for alternative EOPs
	    sendMessage( new Message( ID, (String) sensorToState.getKey(), "monitorState", true ) );
	    
	    sendMessage( new Message( ID, (String) sensorToState.getKey(), stateInquiryType, (String) sensorToState.getValue() ) );
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

    }
    
    // Private method for sending messages. Sends messages to the actuators and sensors only if the machine is
    // running (performsEOP == true). Code could be added to allow to send messages to the cell if machine has 
    // been stopped. 
    // This has been improved since a running machine is not the same as one performing an eop. A machine should be 
    // able to send messages as responses to other machines even when not performing an eop.
    private void sendMessage(Message msg)
    {
	if (performsEOP  || 
	    ( ( (String) msg.getType() ).equals("checkState") && !componentChecks.isEmpty() ) ||
	    ( ( (String) msg.getType() ).equals("requestState") && !componentRequests.isEmpty() ) )
	{
	    System.out.println("Sending message " + msg.getType() + " with content " +  msg.getContent() + " to " + msg.getReceiver() + ".");
	    mailbox.send(msg);
	}
	else
	{
	    System.err.println("Message could not be sent. The machine " + machine.getName() 
			       + " is not performing an EOP.");
	    System.err.println("Message type is " + (String) msg.getType());
	}
    }
    
    // Perform the control activities according to the current actionRow, with only changing actuators, sensors 
    // variables and zones left, stored as currentEOPRow
    private void performActions()
    {
	
	// I make a clone of the current EOP row to iterate through so the orders can be sent to the sensors, 
	// actuators etc, be carried out, sent back to this MachineController and remove the performed element
	// from the current EOP row without interfering with this iterated clone.
	EOPActionRow actionRow = (EOPActionRow) ( (EOPActionRow) currentEOPRow ).clone(); 	
	
	
	// Changed actuator, variable or sensor -> orderState
	
	// Actuators
	for (Iterator actuatorIter = actionRow.getActuatorToStateMap().entrySet().iterator(); actuatorIter.hasNext();)
	{
	    Entry actuatorToState = (Entry) actuatorIter.next();
	    sendMessage( new Message( ID,  (String) actuatorToState.getKey(), "orderState", (String) actuatorToState.getValue() ) );
	    
	}
	// Variables
	for (Iterator variableIter = actionRow.getVariableToValueMap().entrySet().iterator(); variableIter.hasNext();)
	{
	    Entry variableToValue = (Entry) variableIter.next();
	    sendMessage( new Message( ID,  (String) variableToValue.getKey(), "orderState", (String) variableToValue.getValue() ) );
	    
	}
	// Sensors
	for (Iterator sensorIter = actionRow.getSensorToStateMap().entrySet().iterator(); sensorIter.hasNext();)
	{
	    Entry sensorToState = (Entry) sensorIter.next();
	    // Changed sensor -> monitorState off
	    sendMessage( new Message( ID, (String) sensorToState.getKey(), "monitorState", false ) );
	    
	    sendMessage( new Message( ID, (String) sensorToState.getKey(), "orderState", (String) sensorToState.getValue() ) );
	}

	// Zones
	for (Iterator zoneIter = actionRow.getZoneToStateMap().entrySet().iterator(); zoneIter.hasNext();)
	{
	    Entry zoneToState = (Entry) zoneIter.next();
	    machine.orderZone( (String) zoneToState.getKey(), (String) zoneToState.getValue() );
	}
	
	// The following line is necessary if one EOP row is equal to the last 
	checkEOPRowPerformed();
	

    }
    
    public void receiveMessage(Message msg) 
    {
	//confirmations from booking and unbooking also has to be checked

	// Stores the sender of the message
	String sender = msg.getSender();
	System.err.println("Received message in MachineController: "+ msg.getType() + ", " + msg.getContent()); 

	// Report State
	if ( msg.getType().equals("reportState") )
	{
	    // reportState for component that is requested by another machine
	    if ( !componentRequests.isEmpty() && componentRequests.containsKey( sender ) )
	    {
		///The following does not change the element in the map
		// 		ComponentReport componentReport = componentRequests.get(sender);
		// 		componentReport = new ComponentReport( sender, (String) msg.getContent() );
		
		// So I have to do this:
		ComponentReport componentReport = componentRequests.remove(sender);
		componentReport = new ComponentReport( sender, (String) msg.getContent() );
		componentRequests.put(sender, componentReport);
	    }
	    // Handle reportStates for alternative EOPs
	    else if (performsEOP)
	    {
		for (Iterator<Entry<EOP, EOPInitialRow>> EOPIter = currAltEOPToInitRowMap.entrySet().iterator(); 
		     EOPIter.hasNext();)
		{
		    EOPRow EOPRow = EOPIter.next().getValue();
		    
		    // Actuator
		    if ( EOPRow.getActuatorToStateMap().containsKey( sender ) )
		    {
			String state = (String) EOPRow.getActuatorToStateMap().remove(  sender );
// 			System.out.println("EOP " + currentEOP.getId() + ": actuator " + sender 
// 					   + " with state " + state + " was removed."); 
			if (! ( state.equals( (String) msg.getContent() ) || state.equals(EOP.IGNORE_TOKEN) ) )
			{
			    EOPIter.remove();
// 			    System.out.println("The entire alternative EOP was removed");
			}
		    }		
		    // Sensor
		    else if ( EOPRow.getSensorToStateMap().containsKey( sender ) )
		    {
			String state = (String) EOPRow.getSensorToStateMap().remove(  sender );
			if (! ( state.equals( (String) msg.getContent() ) || state.equals(EOP.IGNORE_TOKEN) ) )
			{
			    EOPIter.remove();
			}
		    }		
		    // Variable
		    else if ( EOPRow.getVariableToValueMap().containsKey( sender ) )
		    {
			String value = (String) EOPRow.getVariableToValueMap().remove(  sender );
			if (! ( value.equals( (String) msg.getContent() ) || value.equals(EOP.IGNORE_TOKEN) ) )
			{
			    EOPIter.remove();
			}
		    }		
	
		    else
		    {
			System.err.println("Unknown component");
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
		System.out.println("Unknown reportState sent to machineController in machine " + machine.getName());
	    }
	}
	
	// Confirm State
	else if ( msg.getType().equals("confirmState"))
	{
	    
	    // confirmState for component that is checked by a request/check from another machine
	    // I handle those before the confirmations for this machine and EOP
	    if ( componentChecks.containsKey( sender ) )
	    {
		ComponentConfirmation compConf = componentChecks.remove(sender);
		compConf = new ComponentConfirmation( sender, ( (Boolean) msg.getContent() ).booleanValue() );
		componentChecks.put(sender, compConf);
	    }
	    
	    // Check if the confirmation was true
	    else if (performsEOP && ((Boolean) msg.getContent()).booleanValue())
	    {
		System.err.println("The component " + sender + " holds the supposed state.");
		

		// Actuator
		if ( currentEOPRow.getActuatorToStateMap().containsKey( sender ) )
		{
		    currentEOPRow.getActuatorToStateMap().remove(  sender );
		}		
		// Sensor
		else if ( currentEOPRow.getSensorToStateMap().containsKey( sender ) )
		{
		    currentEOPRow.getSensorToStateMap().remove(  sender );
		}		
		// Variable
		else if ( currentEOPRow.getVariableToValueMap().containsKey( sender ) )
		{
		    currentEOPRow.getVariableToValueMap().remove(  sender );
		}		
		else
		{
		    System.err.println("Unknown component");
		}
		checkEOPRowPerformed();
	    }
	    else
	    {
		System.err.println("The Machine has to stop due to component errors!");
		performsEOP = false;
	    }
	}
	else
	{
	    System.err.println("Wrong message or message type (" + msg.getType() + ") sent to MachineController!");
	}
    }
    
    private boolean EOPRowPerformed()
    {
 	return ( performsEOP 
		 && currentEOPRow.getActuatorToStateMap().size() == 0 &&  currentEOPRow.getSensorToStateMap().size() == 0 
		 && currentEOPRow.getVariableToValueMap().size() == 0 
		 && ( currentEOPRow.getExternalComponentToStateMap() == null ||
		      currentEOPRow.getExternalComponentToStateMap().size() == 0 ) 
		 && ( currentEOPRow.getZoneToStateMap().size() == 0 ) );
	
    }
    
    // Check if the EOP row has been performed. If so perform the next row. If no more rows, the EOP is done.
    protected void checkEOPRowPerformed()
    {
 	if (EOPRowPerformed())
	{
	    System.err.println("The EOP row has been performed.");
	    if (currentEOP.hasMoreActions())
	    {
		currentEOPRow = currentEOP.getNextActions();
		performActions();
	    }
	    else
	    {
		// sensors + last row -> monitorState off
		// It is OK to set state monitoring off for all sensors, even those who were not started.
 		EOPRow lastRow = currentEOP.getLastRow();
 		for (Iterator sensorIter = lastRow.getSensorToStateMap().entrySet().iterator(); sensorIter.hasNext();)
 		{
 		    Entry sensorToState = (Entry) sensorIter.next();
 		    sendMessage( new Message( ID, (String) sensorToState.getKey(), "monitorState", false ) );
 		}
		
		performsEOP = false;
		System.err.println("The EOP is done!");
		EOPPerformedOK = true;
		
	    }
	    
	}
    }
    
    // I assume that I get some answer when the question is forwared to the component via the mailbox,
    // and that answer will set the confirmation of the current component and I will send it to the asking 
    // machine. The code is a bit clumsy when I get a normal
    // Java method request and I have to send messages through mailboxes to get the answer.
    public ComponentConfirmation checkComponent(ComponentCheck componentCheck)
    {
	componentChecks.put(componentCheck.getComponentName(), null);
	sendMessage( new Message( ID, componentCheck.getComponentName(), "checkState", componentCheck.getValueToCheck() ) );
	return componentChecks.remove(componentCheck.getComponentName());
    }
    
    public ComponentReport requestComponent(String componentName)
    {
	componentRequests.put(componentName, null);
	sendMessage( new Message( ID, componentName, "requestState", null ) );
	return componentRequests.remove(componentName);
    }

}
