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
 * The Machine contains information about the mailbox, the MachineController 
 * and about all the toplevel actuators and sensors in the machine
 *
 *
 * Created: Mon Apr  24 13:39:32 2006
 *
 * @author Oscar
 * @version 1.0
 */
package org.supremica.manufacturingTables.controlsystemimplementation;

import java.util.List;
//import java.io.*;

public class Machine
{
    protected final String [] types = {"Conveyor", "Robot", "Memory", "Fixture", "TurnTable", "Other"};
    private String name;
    private String type;
    private String description;
    //private List actuators;
    //private List sensors;
    //private Mailbox mailbox;
    private List variables;
    private MachineController machineController;

    public Machine(String name, String type, MachineController machineController)
    {
	this.name = name;

	boolean typeOK = false;
	for (int i=0; i<types.length; i++)
	    {
		if (types[i].equals(type))
		    {
			typeOK = true;
		    }
	    }
	if (!typeOK)
	    {
		System.err.println("Wrong machine type declared!");
		return;
	    }
	    
	this.type = type;
	this.machineController = machineController;
	variables = null;
	description = null;
    }

    public String getName()
    {
	return name;
    }
   
    public String getType()
    {
	return type;
    }
    
    public String getDescription()
    {
	return description;
    }

    public void setDescription(String descriptionToSet)
    {
	description = descriptionToSet;
    }

    public MachineController getMachineController()
    {
	return machineController;
    }

    public List getVariables()
    {
	return variables;
    }
  
    public void addVariable(Variable variableToAdd)
    {
	variables.add(variableToAdd);
    }

}