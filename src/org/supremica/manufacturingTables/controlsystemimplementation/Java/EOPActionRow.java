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
 * The EOPActionRow class describes the desired state for an action in an EOP
 * containing internal components and zones.
 *
 * Created: Mon May  15 16:30:32 2006
 *
 * @author Oscar
 * @version 1.0
 */
package org.supremica.manufacturingTables.controlsystemimplementation.Java;

import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Iterator;

public class EOPActionRow extends EOPRow implements Cloneable
{
    
    private Set bookingZones;
    private Set unbookingZones;
    
    public EOPActionRow()
    {
	super();
	bookingZones = new HashSet(10); //initital capacity 10 and default load factor (0,75) suits me fine
	unbookingZones = new HashSet(10);
    }

    public void addBookingZone(String zone)
    {
	bookingZones.add(zone);
    }

    public Set getBookingZones() 
    {
	return bookingZones;
    }
    
    public void addUnbookingZone(String zone)
    {
	unbookingZones.add(zone);
    }

    public Set getUnbookingZones() 
    {
	return unbookingZones;
    }

    // An EOPActionRow contains no variables
    public Map getExternalVariableToStateMap() 
    {
	return null;
    }

    public Object clone() 
    {
	EOPActionRow clone = null;
	try
	    {
		clone =(EOPActionRow) super.clone(); // Create space and clone the trivial data
		// The sensor- and actuatorToStateMap has to be cloned separately since we want to be able to 
		// remove elements from the cloned maps and not effect the original maps.
		clone.sensorToStateMap = (Map) ((HashMap) this.sensorToStateMap).clone();
		clone.actuatorToStateMap = (Map) ((HashMap) this.actuatorToStateMap).clone();
		// The order of the original map may be changed when elements are removed from the cloned hashmap
		// but this does not matter since the order is not important.
	    }
	catch (CloneNotSupportedException e)
	    {
		System.err.println("The EOPActionRow could not be cloned!");
	    } 
	return clone;
    }

    // This method compares the current EOPActionRow with the previous EOPRow and removes the unchanged components 
    // of this EOPActionRow. The sensor and actuator maps are ought to contain the same components/keys when started
    public void removeUnchangedComponents(EOPRow previousEOPRow)
    {
	Iterator sensorIter = sensorToStateMap.entrySet().iterator();
	while (sensorIter.hasNext())
	    {
		Entry currentSensorToState = (Entry) sensorIter.next();
		
		// Check if the previous EOPRow contains the same sensor name 
		if (previousEOPRow.sensorToStateMap.containsKey( (String) currentSensorToState.getKey() ))
		    {
			// Check if the proposed state of the sensors are the same in current and previous EOPRows
			if ( ( (String) previousEOPRow.sensorToStateMap.get( (String) currentSensorToState.getKey() ) ).equals( (String) currentSensorToState.getValue() ))
			    {
				sensorIter.remove();
				System.err.println("Removing sensor " + (String) currentSensorToState.getKey() + " with state " + currentSensorToState.getValue());
			    }
		    }
		else
		    {
			System.err.println("The previos EOPRow does not contain the sensor " + (String) currentSensorToState.getKey() + "!");
		    }
	    }
	// It is not absolutely necessary to remove unchanged actuators, but is better not to send messages that
	// are of no meaning.
	Iterator actuatorIter = actuatorToStateMap.entrySet().iterator();
	while (actuatorIter.hasNext())
	    {
		Entry currentActuatorToState = (Entry) actuatorIter.next();
		
		// Check if the previous EOPRow contains the same actuator name 
		if (previousEOPRow.actuatorToStateMap.containsKey( (String) currentActuatorToState.getKey() ))
		    {
			// Check if the proposed state of the actuators are the same in current and previous EOPRows
			if ( ( (String) previousEOPRow.actuatorToStateMap.get( (String) currentActuatorToState.getKey() ) ).equals( (String) currentActuatorToState.getValue() ))
			    {
				actuatorIter.remove();
				System.err.println("Removing actuator " + (String) currentActuatorToState.getKey() + " with state " + currentActuatorToState.getValue());
			    }
		    }
		else
		    {
			System.err.println("The previos EOPRow does not contain the actuator " + (String) currentActuatorToState.getKey() + "!");
		    }
	    }

    }
}