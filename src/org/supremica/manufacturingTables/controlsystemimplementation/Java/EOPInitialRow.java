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
 * The EOPInitialRow class describes the initial state of the EOP and alarm type and delay for 
 * the initial state check.
 *
 * Created: Mon May  15 16:08:32 2006
 *
 * @author Oscar
 * @version 1.0
 */
package org.supremica.manufacturingTables.controlsystemimplementation.Java;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class EOPInitialRow extends EOPRow implements Cloneable
{
    private Map externalVariableToStateMap;
    private String alarmType;
    private String alarmDelay;
    
    public EOPInitialRow()
    {
	super();
	externalVariableToStateMap = new HashMap(5); //initital capacity 5 and default load factor (0,75) suits me fine
	alarmType = null;
	alarmDelay = null;
    }

    public void setAlarmType(String alarmType)
    {
	this.alarmType = alarmType;
    }
    
    public String getAlarmType()
    {
	return alarmType;
    }

    public void setAlarmDelay(String alarmDelay)
    {
	this.alarmDelay = alarmDelay;
    }
    
    public String getAlarmDelay()
    {
	return alarmDelay;
    }

    public void addExternalVariableToState(String variable, String state)
    {
	externalVariableToStateMap.put(variable, state);
    }

    public Map getExternalVariableToStateMap() 
    {
	return externalVariableToStateMap;
    }

    // An EOPInitialRow contains no booking zones
    public Set getBookingZones() 
    {
	return null;
    }

    // An EOPInitialRow contains no unbooking zones
    public Set getUnbookingZones() 
    {
	return null;
    }

   public Object clone() 
    {
	EOPInitialRow clone = null;
	try
	    {
		clone =(EOPInitialRow) super.clone(); // Create space and clone the trivial data
		// The externalVariable-, sensor- and actuatorToStateMap has to be cloned separately since we
		// want to be able to remove elements from the cloned maps and not effect the original maps.
		clone.sensorToStateMap = (Map) ((HashMap) this.sensorToStateMap).clone();
		clone.actuatorToStateMap = (Map) ((HashMap) this.actuatorToStateMap).clone();
		clone.externalVariableToStateMap = (Map) ((HashMap) this.externalVariableToStateMap).clone();
		// The order of the original map may be changed when elements are removed from the cloned hashmap
		// but this does not matter since the order is not important.
	    }
	catch (CloneNotSupportedException e)
	    {
		System.err.println("The EOPInitialRow could not be cloned!");
	    } 
	return clone;
    }

}