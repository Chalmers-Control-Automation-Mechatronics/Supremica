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
 * The Coordinator class sends EOPNumbers to the Machines, via a mailbox,
 * according to the SOP for the current task.
 *
 * Created: Mon Apr  24 14:20:32 2006
 *
 * @author Oscar
 * @version 1.0
 */
package org.supremica.manufacturingTables.controlsystemimplementation.Java;

import java.util.HashMap;
import java.util.Map;

public class Coordinator implements Listener
{
    private Mailbox mailbox;
    private boolean performsTask; // In this version of the concept, only one product at a time is allowed
    private String ID;
    private Map<String, MachineCoordinator> machineCoordinators; 
    // The Coordinator creates all the MachineCoordinators and can reach them both by references and by 
    // message handling.
    private Map<String, Boolean> machineCoordinatorsStarted; // used to keep track of which machines/machineControllers that are started.
 
    public Coordinator(Mailbox mailbox)
    {
	this.mailbox = mailbox;
	performsTask = false;
	ID = "Coordinator";
	mailbox.register(this);
	machineCoordinators = new HashMap<String, MachineCoordinator>(); 
	// default initital capacity (16) and load factor (0,75) suits me fine
	machineCoordinatorsStarted = new HashMap<String, Boolean>(); 
    }

    // When a SOP shall be registerad by the Coordinator, a new MachineCoordinator is created if it has not
    // already been created. Then the SOP is set to that MachineCoordinator. This makes it easy in the future to 
    // distinguish between different SOPs for different products. The Coordinator hence can communicate with the
    // machineCoordinators both directly and via the mailbox, which may be a little strange. This is also done
    // to make it easy in the future to make the MachineCoordinator a part of the machine or to make it only a part 
    // of the cell and remove / not use the message handling.
    public void registerSOP(SOP SOP)
    {
	if ( !machineCoordinators.containsKey( SOP.getMachine() ) )
	{
	    MachineCoordinator machineCoordinator = new MachineCoordinator(SOP.getMachine(), mailbox);
	    machineCoordinator.setSOP(SOP);
	    machineCoordinators.put(machineCoordinator.getID(), machineCoordinator);
	}
	else
	{
	    System.err.println( "The SOP for machine " + SOP.getMachine() + " is changed to " + SOP.getID() );
	    machineCoordinators.get( SOP.getMachine() ).setSOP( SOP );
	}
    }

    public void performTask(String task)
    {
	if (task.equals("weld floor") && !performsTask)
	    {
		performsTask = true;

		// Register that the machines are started. Has to be handled separate from starting the machines /
		// machineCoordinators since otherwise the machineCoordinatorsStarted map could be empty (making us believe 
		// that we are done) when only a few machines has been started and finished.
		for (MachineCoordinator machineCoordinator : machineCoordinators.values())
		{
		    machineCoordinatorsStarted.put(machineCoordinator.getID(), true);
		}
		
		for (MachineCoordinator machineCoordinator : machineCoordinators.values())
		{
		    mailbox.send( new Message( ID, machineCoordinator.getID(), "performSOP", "weld floor" ) );
		    // Otherwise the machinecoordinators start() method could be called
		}
	    }
	else 
	    {
		System.err.println("Unknown task or already busy performing a task!");
	    }
    }
    
    // Do not need to check if the message is for me since it allways is!
    public void receiveMessage(Message msg)
    {
	if (performsTask && msg.getType().equals("SOPDone"))
	{
	    if (((Boolean) msg.getContent()).booleanValue())
	    {
		    if (machineCoordinatorsStarted.containsKey(msg.getSender()))
		    {
			System.err.println("The SOP for machine " + machineCoordinators.get(msg.getSender()).getMachine() + " has been performed with outstanding results!");
			machineCoordinatorsStarted.remove(msg.getSender());
		    }
		    else
		    {
			System.err.println("The machine " + machineCoordinators.get(msg.getSender()).getMachine() + "has never been started!");
		    }
	    }
	    else
	    {
		System.out.println("The SOP could not be performed!");
		Boolean temp = machineCoordinatorsStarted.get(msg.getSender());
		temp = Boolean.FALSE; // do not know why I have to separate this two lines
	    }
	    if (machineCoordinatorsStarted.isEmpty())
	    {
		System.out.println("The whole cell manufacturing cycle is done!");
		performsTask = false;
	    }
	}
	else
	{
	    System.err.println("Wrong message or message type sent to Coordinator!");
	}
    }
    
    public String getID()
    {
	return ID;
    }
    
}