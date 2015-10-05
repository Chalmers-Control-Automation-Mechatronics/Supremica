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
 * The abstract class EOPRow describes a row (intial state or action) of an EOP
 * 
 *
 * Created: Mon May  22 15:42:32 2006
 *
 * @author Oscar
 * @version 1.0
 */
package org.supremica.manufacturingTables.controlsystemimplementation.Java;

import java.util.HashMap;
import java.util.Map;

abstract public class EOPRow 
{
    protected Map<String, String> sensorToStateMap; // HashMap will be used for quick access to the states
    protected Map<String, String> actuatorToStateMap;
    protected Map<String, String> variableToValueMap;
    protected Map<String, String> zoneToStateMap;
    
    public EOPRow()
    {
	sensorToStateMap = new HashMap<String, String>(10); //initital capacity 10 and default load factor (0,75) suits me fine
	actuatorToStateMap = new HashMap<String, String>(10);
	variableToValueMap = new HashMap<String, String>(5);
	zoneToStateMap = new HashMap<String, String>(10);
    }

    final public void addSensorToState(String sensor, String state)
    {
	sensorToStateMap.put(sensor, state);
    }

    final public Map<String, String> getSensorToStateMap() 
    {
	return sensorToStateMap;
    }

    final public void addActuatorToState(String actuator, String state)
    {
	actuatorToStateMap.put(actuator, state);
    }

    final public Map<String, String> getActuatorToStateMap() 
    {
	return actuatorToStateMap;
    }

    final public void addZoneToState(String zone, String state)
    {
	if (state.equals(Zone.FREE_ZONE_TOKEN) || state.equals(Zone.BOOKED_ZONE_TOKEN)
	    || state.equals(EOP.IGNORE_TOKEN) )
	{
	    zoneToStateMap.put(zone, state);
	}
	else
	{
	    System.err.println("Error: Not allowed state for a zone, must be " + Zone.FREE_ZONE_TOKEN 
			       + " or " + Zone.BOOKED_ZONE_TOKEN 
			       + " or " + EOP.IGNORE_TOKEN + "!");
	}
    }

    final public Map<String, String> getZoneToStateMap() 
    {
	return zoneToStateMap;
    }

    final public void addVariableToValue(String variable, String value)
    {
	variableToValueMap.put(variable, value);
    }

    final public Map<String, String> getVariableToValueMap() 
    {
	return variableToValueMap;
    }

    abstract public Map<EOPExternalComponent, String> getExternalComponentToStateMap(); 
   
    
    

}