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
 * The ManufacturingCell contains information about the Mailbox, the Coordinator, zoneHandler 
 * and about all the Machines and zones in the cell
 *
 *
 * Created: Mon Apr  24 13:39:32 2006
 *
 * @author Oscar
 * @version 1.0
 */
package org.supremica.manufacturingTables.controlsystemdata;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;

public class ManufacturingCell
{
    private String name;
    private String description;
    private Map<String, MachineData> machines; // HashMap will be used for quick access to the machines when registering EOPs
    private List<COPData> COPs; // The order for the COPs are not important but I allways iterate through all elements in the list.
    private List<ZoneData> zones; // -||-

    private Mailbox mailbox;
    private Coordinator coordinator;
//     private ZoneHandlerData zoneHandler;

    public ManufacturingCell(String name, Coordinator coordinator, Mailbox mailbox)
    {
	this.name = name;
	this.coordinator = coordinator;
	description = null;
	machines = new HashMap<String, MachineData>();  //default capacity (16) and load factor (0,75) suits me fine
	this.mailbox = mailbox;
	COPs = new LinkedList<COPData>();
	zones = new LinkedList<ZoneData>();
    }

    public String getName()
    {
	return name;
    }
    
    public String getDescription()
    {
	return description;
    }

    public void setDescription(String newDescription)
    {
	description = newDescription;
    }

    public Mailbox getMailbox()
    {
	return mailbox;
    }

    public Coordinator getCoordinator()
    {
	return coordinator;
    }

    public List<ZoneData> getZones()
    {
	return zones;
    }
    
    public void addZone(ZoneData zoneToAdd)
    {
	zones.add(zoneToAdd);
    }

    public Map<String, MachineData> getMachines()
    {
	return machines;
    }
    
    public MachineData getMachine(String machineName)
    {
	return machines.get(machineName);
    }
    
    public void addMachine(MachineData machineToAdd)
    {
	machines.put(machineToAdd.getName(), machineToAdd);
    }

    public void registerCOP(COPData COPData)
    {
	COPs.add(COPData);
    }

    public List<COPData> getCOPs()
    {
	return COPs;
    }

}