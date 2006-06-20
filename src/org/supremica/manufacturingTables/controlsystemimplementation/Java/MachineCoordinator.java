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
 * The SOP class describes a SOP, Sequence of OPeration, that is to be read by the Coordinator for the 
 * whole manufacturing cell. A SOP contains the order to perform different operations for one machine.
 * 
 *
 * Created: Fri Jun  09 09:00:13 2006
 *
 * @author Oscar
 * @version 1.0
 */
package org.supremica.manufacturingTables.controlsystemimplementation.Java;

import java.util.List;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.HashMap;
import java.util.Map;

public class MachineCoordinator implements Listener
{
    private String machine;
    private Mailbox mailbox;
    private SOPActivity currentActivity;
    private boolean performsSOP;
    private SOP currentSOP; // In the future it will be possible with different SOPs
    private Map<String, SOPPredecessor> predecessorsFulfilled; 
    // predecessorsFulfilled is a Map (Hashmap) with the interresting operations (predecessors to 
    // operations this machineCoordinator shall perform) performed by other machines.
 
    public MachineCoordinator(String machine, Mailbox mailbox)
    {
	this.machine = machine;
	this.mailbox = mailbox;
	performsSOP = false;
	currentSOP = null;
	currentActivity = null;
	mailbox.register(this);
	predecessorsFulfilled = new HashMap<String, SOPPredecessor>(8); 
	// (initital capacity 8 and default load factor (0,75) suits me fine)
	
    }

    public String getMachine()
    {
	return machine;
    }

    public void setSOP(SOP SOP)
    {
	currentSOP = SOP;
    }

    // This Coordinator has to have a unique id for the message handling.
    public String getID()
    {
	return "Coordinator" + machine;
    }
 
    protected void start()
    {
	if (performsSOP)
	{
	    System.err.println("Already performing a SOP!");
	}
	else
	{
	    performsSOP = true;
	    currentSOP.start();
	    if (currentSOP.hasMoreActivities())
	    {
		currentActivity = currentSOP.getNextActivity();
		runSOP();
	    }
	}
    }
    
    private void runSOP()
    {
	if (currentActivity.hasPredecessors())
	{
	    for (SOPPredecessor predecessor : currentActivity.getPredecessors())
	    {
		if (predecessorsFulfilled.containsKey(predecessor.getID()))
		{
		    // The predecessor seems to be fullfilled
		    if (predecessorsFulfilled.get(predecessor.getID()).equals(predecessor))
		    {
			// The predecessor was fullfilled
			predecessorsFulfilled.remove(predecessor.getID());
		    }
		    else
		    {
			// This is strange and should never happen. 
			System.err.println("The predecessor found is not equal to the supposed one!");
			return;
		    }
		}
		else
		{
		    // We (still) have to wait for operations from other machines
		    return;
		}
	    }
	}
	// Now all (if any) predecessors are fulfilled
	// Time to run the operation (EOP)
	mailbox.send( new Message( getID(), currentSOP.getMachine(), "performEOP", currentActivity.getOperation() ) );
	
	// Now we will wait for the Machine to report back
    }

    public void receiveMessage(Message msg)
    {
	if (!performsSOP && msg.getType().equals("performSOP"))
	{
	    start();
	}
	else if (msg.getType().equals("operationDone"))  
	    // This machines SOP does not have to be started (performsEOP not checked)
	{
	    SOPPredecessor predecessorDone = (SOPPredecessor) ((SOPPredecessor) msg.getContent()).clone();
	    predecessorsFulfilled.put(predecessorDone.getID(), predecessorDone);
	    if (performsSOP) // If we have started
	    {
		runSOP();
	    }
	}
	else if (performsSOP && msg.getType().equals("EOPDone"))
	{
	    if (((Boolean) msg.getContent()).booleanValue())
	    {
		System.err.println("The EOP has been performed with outstanding results!");

		// Check if there are successors
		List<SOPSuccessor> successors = null;
		int performedOperation = -1;
		if (currentActivity.hasSuccessors())
		{
		    successors = currentActivity.getSuccessors();
		    performedOperation = currentActivity.getOperation();
		}

		// Check if there are more activities. We have to change the currentActivity before the successors 
		// are performed, otherwise they can make the current action be done again if they are predecessors to 
		// some later action in this SOP.
		if (currentSOP.hasMoreActivities())
		{
		    currentActivity = currentSOP.getNextActivity();
		}
		else
		{
		    // The complete SOP is done!!
		    performsSOP = false;
		    System.out.println("The SOP " + currentSOP.getID() + " is done!");
		    mailbox.send( new Message( getID(), "Coordinator", "SOPDone", true ) );
		}

		// Handle successors, it is OK to do here after the SOP might be done
		if (successors != null)
		{
		    for (SOPSuccessor successor : successors)
		    {
			mailbox.send( new Message( getID(), "Coordinator" + successor.getMachine(), "operationDone", new SOPPredecessor(performedOperation, machine) ) );
		    }
		}

		// If the EOP is still running (not finished) we perform the next operation
		if (performsSOP)
		{
		    runSOP();
		}

	    }
	    else
	    {
		System.err.print("The EOP could not be performed!");
		System.err.println(" (says message sender: " + msg.getSender() + ")");
		mailbox.send( new Message( getID(), "Coordinator", "SOPDone", false ) );
		performsSOP = false;
	    }
	}
	else
	{
	    System.err.println("MachineCoordinator " + getID() + " can not handle the message " + msg.getType() + " at the present time.");
	}
    }
    
    
}