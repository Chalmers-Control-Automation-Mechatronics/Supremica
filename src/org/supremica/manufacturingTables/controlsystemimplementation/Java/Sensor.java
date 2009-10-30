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
 * The abstract Sensor class describes all the information in common for low level
 * and top level sensors.
 *
 *
 * Created: Mon Apr  24 11:17:32 2006
 *
 * @author Oscar
 * @version 1.0
 */
package org.supremica.manufacturingTables.controlsystemimplementation.Java;

import java.util.List;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.Map;

public abstract class Sensor
{
    protected String name;
    private String description;
    protected Map<String, String> states; // HashMap will be used for quick access to the states
    protected List<Sensor> sensors; 
    // The order for the sensors (and hardwareConnections below) are not important but I allways iterate 
    // through all elements in the list. Normally very few elements are used.
    protected List<String> hardwareConnections;

    public Sensor(String name)
    {
	this.name = name;
	states = new HashMap<String, String>(5); //initital capacity 5 and default load factor (0,75) suits me fine
	hardwareConnections = new LinkedList<String>();
	sensors = new LinkedList<Sensor>();
    }

    final public String getName()
    {
	return name;
    }

    final public void setDescription(String newDescription)
    {
	description = newDescription;
    }
   
    final public String getDescription()
    {
	return description;
    }
    
    final public void addState(String stateToAdd)
    {
	states.put(stateToAdd, stateToAdd); 	
	// Now Strings are used both as values and keys, but the value may in the future be a State object

    }

    final public void addHardwareConnection(String hardwareConnectionToAdd)
    {
	hardwareConnections.add(hardwareConnectionToAdd);
    }

    final public void addSensor(Sensor sensorToAdd)
    {
	sensors.add(sensorToAdd);
    }
  
    final public boolean hasState(String state)
    {
	return states.containsKey(state); // containsValue are more expensive than containsKey
    }

    abstract protected String requestState(); 
    
}
