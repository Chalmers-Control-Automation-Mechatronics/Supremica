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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Iterator;

@SuppressWarnings("unchecked")
public class EOPActionRow extends EOPRow implements Cloneable
{

//     private Set<String> bookingZones;
//     private Set<String> unbookingZones;

    public EOPActionRow()
    {
	super();
    }

    // An EOPActionRow contains no external components
    public Map<EOPExternalComponent, String> getExternalComponentToStateMap()
    {
 	return null;
    }

    public Object clone()
    {
	EOPActionRow clone = null;
	try
	    {
		clone =(EOPActionRow) super.clone(); // Create space and clone the trivial data
		// The maps and sets has to be cloned separately since we want to be able to
		// remove elements from the cloned maps and not effect the original maps.
		clone.sensorToStateMap = (Map<String,String>) ((HashMap<?,?>) this.sensorToStateMap).clone();
		clone.actuatorToStateMap = (Map<String,String>) ((HashMap<?,?>) this.actuatorToStateMap).clone();
		clone.variableToValueMap = (Map<String,String>) ((HashMap<?,?>) this.variableToValueMap).clone();
		clone.zoneToStateMap = (Map<String,String>) ((HashMap<?,?>) this.zoneToStateMap).clone();
		// The order of the original map may be changed when elements are removed from the cloned hashmap
		// but this does not matter since the order is not important.
	    }
	catch (final CloneNotSupportedException e)
	    {
		System.err.println("The EOPActionRow could not be cloned!");
	    }
	return clone;
    }

    // This method compares the current EOPActionRow with the previous EOPRow and removes the unchanged components
    // of this EOPActionRow. The sensor and actuator maps are ought to contain the same components/keys when started
    public void removeUnchangedComponents(final EOPRow previousEOPRow)
    {
	// Sensors
	final Iterator<?> sensorIter = sensorToStateMap.entrySet().iterator();
	while (sensorIter.hasNext())
	    {
		final Entry<?,?> currentSensorToState = (Entry<?,?>) sensorIter.next();

		// Check if the previous EOPRow contains the same sensor name
		if (previousEOPRow.sensorToStateMap.containsKey( (String) currentSensorToState.getKey() ))
		    {
			// Check if the proposed state of the sensors are the same in current and previous EOPRows or
			// if the state is EOP.IGNORE_TOKEN, which means that the state is not important.
			if ( ( (String) previousEOPRow.sensorToStateMap.get( (String) currentSensorToState.getKey() ) ).equals( (String) currentSensorToState.getValue() ) || ( (String) currentSensorToState.getValue() ).equals(EOP.IGNORE_TOKEN) )
			    {
				sensorIter.remove();
			    }
		    }
		else
		    {
			System.err.println("The previos EOPRow does not contain the sensor " + (String) currentSensorToState.getKey() + "!");
		    }
	    }
	// Actuators. It is not absolutely necessary to remove unchanged actuators, but is better not to send messages that
	// are of no meaning.
	final Iterator<?> actuatorIter = actuatorToStateMap.entrySet().iterator();
	while (actuatorIter.hasNext())
	{
	    final Entry<?,?> currentActuatorToState = (Entry<?,?>) actuatorIter.next();

	    // Check if the previous EOPRow contains the same actuator name
	    if (previousEOPRow.actuatorToStateMap.containsKey( (String) currentActuatorToState.getKey() ))
	    {
		// Check if the proposed state of the actuators are the same in current and previous EOPRows
		if ( ( (String) previousEOPRow.actuatorToStateMap.get( (String) currentActuatorToState.getKey() ) ).equals( (String) currentActuatorToState.getValue() )  || ( (String) currentActuatorToState.getValue() ).equals(EOP.IGNORE_TOKEN) )
		{
		    actuatorIter.remove();
		}
	    }
	    else
	    {
		System.err.println("The previos EOPRow does not contain the actuator " + (String) currentActuatorToState.getKey() + "!");
	    }
	}
	// Variables
	final Iterator<?> variableIter = variableToValueMap.entrySet().iterator();
	while (variableIter.hasNext())
	{
	    final Entry<?,?> currentVariableToValue = (Entry<?,?>) variableIter.next();

	    // Check if the previous EOPRow contains the same variable name
	    if (previousEOPRow.variableToValueMap.containsKey( (String) currentVariableToValue.getKey() ))
		    {
			// Check if the proposed state of the variables are the same in current and previous EOPRows
			if ( ( (String) previousEOPRow.variableToValueMap.get( (String) currentVariableToValue.getKey() ) ).equals( (String) currentVariableToValue.getValue() )  || ( (String) currentVariableToValue.getValue() ).equals(EOP.IGNORE_TOKEN) )
			{
			    variableIter.remove();
			}
		    }
	    else
	    {
		System.err.println("The previos EOPRow does not contain the variable " + (String) currentVariableToValue.getKey() + "!");
	    }
	}

	// Zones
	final Iterator<?> zoneIter = zoneToStateMap.entrySet().iterator();
	while (zoneIter.hasNext())
	    {
		final Entry<?,?> currentZoneToState = (Entry<?,?>) zoneIter.next();

		// Check if the previous EOPRow contains the same zone name
		if (previousEOPRow.zoneToStateMap.containsKey( (String) currentZoneToState.getKey() ))
		    {
			// Check if the proposed state of the zones are the same in current and previous EOPRows or
			// if the state is EOP.IGNORE_TOKEN, which means that the state is not important.
			if ( ( (String) previousEOPRow.zoneToStateMap.get( (String) currentZoneToState.getKey() ) ).equals( (String) currentZoneToState.getValue() ) || ( (String) currentZoneToState.getValue() ).equals(EOP.IGNORE_TOKEN) )
			    {
				zoneIter.remove();
			    }
		    }
		else
		    {
			System.err.println("The previos EOPRow does not contain the zone " + (String) currentZoneToState.getKey() + "!");
		    }
	    }



    }
}