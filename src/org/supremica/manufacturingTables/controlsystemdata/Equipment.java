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
 * The abstract class Equipment states the methods and data that equipment (for instance sensors and actuators) 
 * have in common.
 *
 * Created: Wen May  03 13:39:32 2006
 *
 * @author Oscar
 * @version 1.0
 */
package org.supremica.manufacturingTables.controlsystemdata;

import java.util.List;
import java.util.LinkedList;

abstract public class Equipment implements EquipmentContainer
{
    
    protected String name;
    private String description;
    //private List actuators;
    protected List<Sensor> sensors; 
    // The order for the sensors (and states and hardwareConnections below) are not important but I allways 
    // iterate through all elements in the List. Normally very few elements are used.
    protected List<String> states;
    protected List<String> hardwareConnections;
    
    public Equipment(String name)
    {
	this.name = name;
	description = null;
	states = new LinkedList<String>();
	//actuators = new LinkedList();
	sensors = new LinkedList<Sensor>();
	hardwareConnections = new LinkedList<String>();
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
     

    final public List<String> getStates()
    {
	return states;
    }
  
    final public void addState(String stateToAdd)
    {
	states.add(stateToAdd);
    }

    abstract public void addActuator(Actuator actuatorToAdd);

    public List<Sensor> getSensors()
    {
	return sensors;
    }
  
    public void addSensor(Sensor sensorToAdd)
    {
	sensors.add(sensorToAdd);
    }

    final public List<String> getHardwareConnections()
    {
	return hardwareConnections;
    }
  
    final public void addHardwareConnection(String hardwareConnectionToAdd)
    {
	hardwareConnections.add(hardwareConnectionToAdd);
    }
}


