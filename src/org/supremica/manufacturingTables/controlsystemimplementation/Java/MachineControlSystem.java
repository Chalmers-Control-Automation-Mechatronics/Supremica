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
import java.util.Map;
import java.util.Map.Entry;
import java.util.Iterator;
public class MachineControlSystem extends MachineController implements Listener 
{
    private Mailbox mailbox;
    private boolean EOPperformedOK; // to tell the machine whether the EOP was performed successfully or not
    private Map EOPs;  // HashMap will be used for quick access to the states
    private EOPRow currentEOPRow;
    private EOP currentEOP;
    
    public MachineControlSystem(Mailbox mailbox)
    {
	super();
	this.mailbox = mailbox;
	EOPperformedOK = false;
	mailbox.register(this);
	EOPs = new HashMap(); //default capacity (16) and load factor (0,75) suits me fine
	currentEOPRow = null;
	currentEOP = null;
    }
    
    public void registerEOP(EOP EOP)  
    {
	EOPs.put(EOP.getId(), EOP);
    }
    
    public boolean performEOP(int EOPNbr)
    {
	if ( EOPs.containsKey(EOPNbr) && !performsEOP )
	{
	    System.out.println("Performing EOP: " + ((EOP) EOPs.get(EOPNbr)).getId());
	    performsEOP = true;
	    EOPperformedOK = false; // this means that if the EOP is empty, a false will be returned
	    currentEOP = (EOP) EOPs.get(EOPNbr);
	    
	    // EOP Initial Row
	    currentEOPRow = currentEOP.getEOPInitialRowActions();
	    performInitialRow();
	    currentEOP.startActions();
	}		
	else 
	{
	    System.err.println("Unknown EOP or already busy performing an EOP!");
	}
	
	return EOPperformedOK;
    }
    
    // Perform the control activities according to the initial EOP row, stored as currentEOPRow.
    // The activities are different for different EOP types.
    private void performInitialRow()
    {
	// I make a clone of the current EOP row to iterate through so the orders can be sent to the sensors, 
	// actuators etc, be carried out, sent back to this MachineController and remove the performed element
	// from the current EOP row without interfering with this iterated clone.
	EOPInitialRow initialRow = (EOPInitialRow) ( (EOPInitialRow) currentEOPRow ).clone(); 	
	
	//check type
	// Must fix so that we can have alternative EOPS, for now this is not done
	
// 	// Initial Row + alternative EOP -> requestState
// 	if (currentEOP.getType().equals("alternative"))
// 	{
// 	    System.err.println("There is currently no support for alternative EOPs!");
// 	}
	
// 	// Initial Row + basic EOP -> checkState 
// 	else if (currentEOP.getType().equals("basic"))
// 	{
	    // For now I dont care about the alarm types and delays
	    // get Alarm Type and Alarm Delay
	    
	    // External Variable
	    for (Iterator externalIter = initialRow.getExternalVariableToStateMap().entrySet().iterator(); externalIter.hasNext();)
	    {
		Entry variableToState = (Entry) externalIter.next();
		if ( !variableToState.getValue().equals("*") );
		{
		    //send some message to Machine with the value and key (the whole entry?)
		}
	    }
	    // Actuators
	    for (Iterator actuatorIter = initialRow.getActuatorToStateMap().entrySet().iterator(); actuatorIter.hasNext();)
	    {
		Entry actuatorToState = (Entry) actuatorIter.next();
// 		if (!actuatorToState.getValue().equals("*"))
// 		{
		sendMessage( new Message( ID,  (String) actuatorToState.getKey(), "checkState", (String) actuatorToState.getValue() ) );
// 		}
// 		else
// 		{
// 		    System.err.println("* for actuator was removed: " + currentEOPRow.getActuatorToStateMap().remove( (String) actuatorToState.getKey() ) );
// 		}
	    }
	    // Sensors
	    for (Iterator sensorIter = initialRow.getSensorToStateMap().entrySet().iterator(); sensorIter.hasNext();)
	    {
		Entry sensorToState = (Entry) sensorIter.next();
// 		if (!sensorToState.getValue().equals("*"))
// 		{
 		    // Initial Row + sensor -> monitorState
 		    sendMessage( new Message( ID, (String) sensorToState.getKey(), "monitorState", true ) );
		    
 		    sendMessage( new Message( ID, (String) sensorToState.getKey(), "checkState", (String) sensorToState.getValue() ) );
// 		}
// 		else
// 		{
// 		    System.err.println("* for sensor was removed: " + currentEOPRow.getSensorToStateMap().remove( (String) sensorToState.getKey() ) );
// 		}
	    }
	}
// 	else
// 	{
// 	    System.err.println("Unknown EOP type " + currentEOP.getType());
// 	}
//     }
    
    // Private method for sending messages. Sends messages to the actuators and sensors only if the machine is
    // running (performsEOP == true). Code could be added to allow to send messages to the cell if machine has 
    // been stopped. 
    private void sendMessage(Message msg)
    {
	if (performsEOP)
	{
	    System.out.println("Sending message " + msg.getType() + " with content " +  msg.getContent() + " to " + msg.getReceiver() + ".");
	    mailbox.send(msg);
	}
	else
	{
	    System.err.println("The machine has been stopped!");
	}
    }
    
    // Perform the control activities according to the current actionRow with only changing actuators and sensors left, 
    // stored as currentEOPRow
    private void performActions()
    {
	
	// I make a clone of the current EOP row to iterate through so the orders can be sent to the sensors, 
	// actuators etc, be carried out, sent back to this MachineController and remove the performed element
	// from the current EOP row without interfering with this iterated clone.
	EOPActionRow actionRow = (EOPActionRow) ( (EOPActionRow) currentEOPRow ).clone(); 	
	
	
	// Changed actuator or sensor -> orderState
	
	// Actuators
	for (Iterator actuatorIter = actionRow.getActuatorToStateMap().entrySet().iterator(); actuatorIter.hasNext();)
	{
	    Entry actuatorToState = (Entry) actuatorIter.next();
	    sendMessage( new Message( ID,  (String) actuatorToState.getKey(), "orderState", (String) actuatorToState.getValue() ) );
	    
	}
	
	// Sensors
	for (Iterator sensorIter = actionRow.getSensorToStateMap().entrySet().iterator(); sensorIter.hasNext();)
	{
	    Entry sensorToState = (Entry) sensorIter.next();
	    // Changed sensor -> monitorState off
	    sendMessage( new Message( ID, (String) sensorToState.getKey(), "monitorState", false ) );
	    
	    sendMessage( new Message( ID, (String) sensorToState.getKey(), "orderState", (String) sensorToState.getValue() ) );
	}
	
	// Wait with booking and unbooking zones
	
    }
    
    public void receiveMessage(Message msg) 
    {
	//confirmations from variables , booking and unbooking also has to be checked
	
	// Confirm State
	if (performsEOP && msg.getType().equals("confirmState"))
	{
	    System.err.println("Received message in MachineController: "+ msg.getType() + ", " + msg.getContent()); 
	    
	    // Check if the confirmation was true
	    if (((Boolean) msg.getContent()).booleanValue())
	    {
		String sender = msg.getSender();
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
		else
		{
		    System.err.println("Unknown component");
		}
		
		// Check if the EOP row has been performed
		// Variables, booking and unbooking also has to be checked (and that they are not null)
		if ( currentEOPRow.getActuatorToStateMap().size() == 0 &&  currentEOPRow.getSensorToStateMap().size() == 0 )
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
			EOPRow lastRow = currentEOP.getLastRow();
			for (Iterator sensorIter = lastRow.getSensorToStateMap().entrySet().iterator(); sensorIter.hasNext();)
			{
			    Entry sensorToState = (Entry) sensorIter.next();
			    sendMessage( new Message( ID, (String) sensorToState.getKey(), "monitorState", false ) );
			}
			
			performsEOP = false;
			System.err.println("The EOP is done!");
			EOPperformedOK = true;
			
		    }
		    
		}
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
    
}