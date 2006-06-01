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
 * The TopLevelActuator class describes the actuators that the MachineController can communicate with
 * in order to build a control program. (Default maximum time for actuator to actuate is 10 seconds.)
 *
 *
 * Created: Mon Apr  24 13:39:32 2006
 *
 * @author Oscar
 * @version 1.0
 */
package org.supremica.manufacturingTables.controlsystemimplementation.Java;

import java.util.Iterator;

public class TopLevelActuator extends Actuator implements Listener
{
    private Mailbox mailbox;
    private String currentState;
    private double actuationTime; 
    
    public TopLevelActuator(String name, Mailbox mailbox)
    {
	super(name);
	this.mailbox = mailbox;
	mailbox.register(this);
	currentState = null; //must be different from null for actuators with no sensors
	actuationTime = 10;
    }

    public void setActuationTime(double newActuationTime)
    {
	actuationTime = newActuationTime;
    }

    public void receiveMessage(Message msg) 
    {
	if (msg.getType().equals("requestState"))
	    {
		mailbox.send(new Message(getID(), "MachineController", "reportState", requestState()));
	    }
	else if (msg.getType().equals("checkState"))
	    {
		mailbox.send(new Message(getID(), "MachineController", "confirmState",checkState((String) msg.getContent())));
	    }
	else if (msg.getType().equals("orderState"))
	    {
		mailbox.send(new Message(getID(), "MachineController", "confirmState",orderState((String) msg.getContent())));
	    }
	else
	    {
		System.err.println("Unknown message type for the top level actuator " + name + " !");
	    }
    }

    public String requestState() 
    {
	if (!actuators.isEmpty())
	    {
		Iterator actuatorIter = actuators.iterator();
		currentState = ((Actuator) actuators.get(0)).requestState();
		actuatorIter.next();
		while (actuatorIter.hasNext())
		    {
			if (!currentState.equals((String) ((Actuator) actuatorIter.next()).requestState()))
			    {
				System.err.println("Broken equipment: " + name + " !");
				return null;
			    }
		    }
	    }
	
	else if (!sensors.isEmpty())
	    {
		// Go through all sensors and if a sensor is in a state that is known by the actuator this should 
		// be the state of the actuator. Inconsistency between sensors regarding this state is detected.
		// For instance if an actuator could be in two states A and B and it has two sensors, one in each
		// position, the two sensors has the possible states "A" and "not A" and "B" and "not B" respectively. 
		// "A" and "B" shall not be given at the same time. Neither "not A" and "not B".
		
		boolean sensorExist = false;
		Iterator sensorIter = sensors.iterator();
		while (sensorIter.hasNext())
		    {
			String currentSensorState = ((Sensor) sensorIter.next()).requestState();
			if (hasState(currentSensorState))
			    {
				if (!sensorExist)
				    {
					sensorExist = true;
					currentState = currentSensorState;
				    }
				else if (!currentState.equals(currentSensorState))
				    {
					// different states in different sub level sensors
					System.err.println("Broken equipment: " + name + " !");
					return null;
				    }
			    }
		    }
		if (!sensorExist) 
		    // Actually means that the actuator is moving (or equipment is broken 
		    // but this should be detected by the stateOrder).
		    // The SOP should be calculated so that this does not happen but it is not
		    // fully covered by the concept.
		    {
			System.err.println("Broken equipment or actuator moving: " + name + " !");
			return null;
		    }
	    }
	
	// Else if no lower level actuators or sensors exist this could be the case for actuators with no sensors, 
	// where we assume that the state of this actuator is the last ordered state
	return currentState; 
    }

    protected boolean checkState(String desiredState)
    {
	if (requestState().equals(desiredState))
	    {
		return true;
	    }
	else 
	    {
		System.err.println("Alarm, alarmtype! Actuator: " + name  + "(in state " + currentState  +" )");
		return false;
	    }
    }
    
    public boolean orderState(String orderedState)
    {
	// No timing is implemented now. The author is thinking about whether the hardware should report when 
	// the state changes and what to do with the timing.
	
	//long currentTime = System.currentTimeMillis();
	
	//check if the orderedState is one of the actuators states
	if (!hasState(orderedState))
	    {
		System.err.println("Unknown state "+ orderedState  +" for actuator " + name);
		return false;
	    }

	// If the actuator has hardwareConnections all actuating is handled here
	if (!hardwareConnections.isEmpty())
	    {
		//start timer and a boolean that says that the operation is started
		
		// This hardware connections must, of course, contain more than the name of the actuator, 
		// for instance how many signals and what to signal.
		Iterator hardwareIter = hardwareConnections.iterator();
		while (hardwareIter.hasNext())
		    {
			System.err.println("Actuator " + (String) hardwareIter.next() + " shall go to state " + orderedState + ".");
		    }
		
	    }

	boolean actuatorOK = false;
	
	// If the actuator has lower level actuators those handle the actuating (if not handled above) and the sensors
	if (!actuators.isEmpty())
	    {
		//start timer and set boolean started if not started before
		Iterator actuatorIter = actuators.iterator();
		actuatorOK = true;
		while (actuatorIter.hasNext() && actuatorOK)
		    {
			actuatorOK = ((Actuator) actuatorIter.next()).orderState(orderedState);
		    }
	    }

	else if (!sensors.isEmpty())
	    {
		// In this case the stateRequest of this actuator goes through the sensors and returns the 
		// state of the actuator, which is exactly what we want to do. stateCheck could be used as well but 
		// this would include another alarm
		actuatorOK = requestState().equals(orderedState);
	    }
	
	// if the actuator is just actuating, no sensors, we assume the operation is fullfilled (should be timed)
	else
	    {
		actuatorOK = true;
	    }
	
	if (actuatorOK)
	    {
		currentState = orderedState;
	    }
	
	else
	    {
		System.err.println("Failure when moving actuator" + name + "to state" + orderedState + "!");
	    }

	return actuatorOK;
	    
    }

    
    // We shall also have a statemonitor that gives an alarm if the actuator is moved when not ordered to

    public String getID()
    {
	return getName();
    }

}