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
 * The LowLevelSensor class describes the low level sensor
 * in order for higer level actuators and sensors to communicate with it. No alarms are given on this lower level.
 *
 *
 * Created: Mon Apr  24 11:17:32 2006
 *
 * @author Oscar
 * @version 1.0
 */
package org.supremica.manufacturingTables.controlsystemimplementation.Java;

import java.util.Iterator;
import java.io.*;

public class LowLevelSensor extends Sensor 
{
    private String currentState;

    public LowLevelSensor(String name)
    {
	super(name);
	currentState = null;
    }

    protected String requestState()
    {
	// For now I assume that when sensors contains sensors, the lower level sensors are equivalent
	if (!sensors.isEmpty())
	    {
		Iterator sensorIter = sensors.iterator();
		currentState = ((Sensor) sensorIter.next()).requestState();
		while (sensorIter.hasNext())
		    {
			if ( !currentState.equals( ( (Sensor) sensorIter.next() ).requestState() ) )
			    {
				// System.err.println("Broken equipment: " + name + " !");
				return null;
			    }
		    }
	    }

	else if (!hardwareConnections.isEmpty())
	    {
		// Normally a sensor can not have more than one hardwareConnection but I assume they can have many. 
		// In that case they should all be the same (have the same value/state).
		Iterator hardwareIter = hardwareConnections.iterator();
		try
		    {
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			String newState = null;
			while (newState == null)
			    {
				System.out.print("Type the state for sensor " + name + " (" + getDescription()  + ") and hardwareConnection " + (String) hardwareConnections.get(0) + ":");
				System.out.flush();
				newState =  in.readLine();
				if (hasState(newState))
				    {
					currentState = newState;
				    }
				else 
				    {
					System.err.println("Unknown state for the sensor, try again");
					newState = null;
				    }
			    }
			hardwareIter.next();
		
			while (hardwareIter.hasNext())
			    {
				System.out.print("Type the state for sensor " + name + " and hardwareConnection " + (String) hardwareIter.next() + ":");
				System.out.flush();
				if (!currentState.equals(in.readLine()))
				    {
					//System.err.println("Broken equipment: " + name + " !");
					return null;
				    }
			    }
		    }
		catch(IOException exception)
		    {
			System.err.println("IOException, could not read the input from the keybord!");
		    }
	    }
	
	else
	    {
		System.err.println("Strange sensor " + name + " containing nothing!");
		return null;
	    }
	return currentState;
	
    }
   
}
