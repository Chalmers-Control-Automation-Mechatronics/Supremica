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
 * The Coordinator class (via the MachineCoordinators) send EOPNumbers to the Machines,
 * according to the COP for the current task.
 *
 * Created: Tue Oct 30 11:49 2006
 *
 * @author Oscar
 * @version 1.0
 */
package org.supremica.manufacturingTables.controlsystemimplementation.IEC61499;

import net.sourceforge.fuber.demo.CoordinatorThread;
import java.util.HashMap;
import java.util.Map;
import java.util.Collections;

public class Coordinator
{
    private Mailbox mailbox;
    // All communication with the MachineCoordinators is now done via the CoordinatorThread
    private CoordinatorThread coordinatorThread; 
    private boolean performsTask; // In this version of the concept, only one product at a time is allowed
    private String ID;
    private Map<String, MachineCoordinator> machineCoordinators; // The String corresponds to the machine name
    // The Coordinator creates all the MachineCoordinators and can reach them by references.
    private Map<String, Boolean> machineCoordinatorsStarted; // used to keep track of which machines/machineControllers that are started.
 
    public Coordinator(Mailbox mailbox)
    {
	this.mailbox = mailbox;
	coordinatorThread = null;
	performsTask = false;
	ID = "Coordinator";
	machineCoordinators = new HashMap<String, MachineCoordinator>(); 
	// default initital capacity (16) and load factor (0,75) suits me fine
	machineCoordinatorsStarted = Collections.synchronizedMap(new HashMap<String, Boolean>()); 
    }

    // The machineCoordinators must be reached to be used in the Fuber application:
    public Map<String, MachineCoordinator> getMachineCoordinators()
    {
	return machineCoordinators;
    }

    public void setThread(CoordinatorThread coordinatorThread)
    {
	this.coordinatorThread = coordinatorThread;
    }
 

    // When a COP shall be registerad by the Coordinator, a new MachineCoordinator is created if it has not
    // already been created. Then the COP is set to that MachineCoordinator. This makes it easy in the future to 
    // distinguish between different COPs for different products. The Coordinator hence can communicate with the
    // machineCoordinators directly. The machinecoordinators in turn communicate with the machines and with each 
    // other via the cell mailbox. This is improved from the Java implementation.
    public void registerCOP(COP COP)
    {
	if ( !machineCoordinators.containsKey( COP.getMachine() ) )
	{
	    MachineCoordinator machineCoordinator = new MachineCoordinator(COP.getMachine(), mailbox, this);
	    //	    MachineCoordinatorThread machineCoordinatorThread = new MachineCoordinatorThread();
	    //	    MachineCoordinator machineCoordinator = new MachineCoordinator(COP.getMachine(), machineCoordinatorThread);
	    machineCoordinator.setCOP(COP);  
	    //	    coordinatorThread.setCOP(COP);
	    machineCoordinators.put( COP.getMachine(), machineCoordinator );  
	}
	else
	{
	    System.err.println( "The COP for machine " + COP.getMachine() + " is changed to " + COP.getID() );
	    machineCoordinators.get( COP.getMachine() ).setCOP( COP ); 
	    //	    coordinatorThread.setCOP(COP);
	}
    }

    public void performTask(String task)
    {
	if (task.equals("weld floor") && !performsTask)
	    {
		performsTask = true;
		//testrad
		// mailbox.send( new Message( ID, "Coordinator150R3325", "performCOP", "weld floor" ) );

		// Register that the machines are started. Has to be handled separate from starting the machines /
		// machineCoordinators since otherwise the machineCoordinatorsStarted map could be empty (making us believe 		
		// that we are done) when only a few machines has been started and finished.
		for (String machineName : machineCoordinators.keySet())
		{
		    machineCoordinatorsStarted.put(machineName, true);
		}
		
		for (MachineCoordinator machineCoordinator : machineCoordinators.values())
		{
		    //machineCoordinator.start();
		    // The row below is not perfect... since we contact the machineCoordinator both by
		    // reference and via the Functionblocks.
		    coordinatorThread.startMC( machineCoordinator.getMachine() );
		}
	    }
	else 
	    {
		System.err.println("Unknown task or already busy performing a task!");
	    }
    }
    
    public void COPDone(String machineName, boolean performedOK)
    {
	if (performedOK)
	{
	    if (machineCoordinatorsStarted.containsKey(machineName))
	    {
		System.err.println("The COP for machine " + machineName + " has been performed with outstanding results!");
		machineCoordinatorsStarted.remove(machineName);
	    }
	    else
	    {
		System.err.println("The machine " + machineName + "has never been started!");
	    }
	}
	else
	{
	    System.out.println("The COP could not be performed!");
	    Boolean temp = machineCoordinatorsStarted.get(machineName);
	    temp = Boolean.FALSE; // do not know why I have to separate this two lines
	}
	if (machineCoordinatorsStarted.isEmpty())
	{
	    System.out.println("The whole cell manufacturing cycle is done!");
	    performsTask = false;
	}
    }
}