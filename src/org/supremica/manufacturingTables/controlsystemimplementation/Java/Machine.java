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
 * The Machine contains information about the MachineController and the variables.
 * The MachineController communicates with the actuators and sensors via a mailbox.
 *
 *
 * Created: Mon Apr  24 13:39:32 2006
 *
 * @author Oscar
 * @version 1.0
 */
package org.supremica.manufacturingTables.controlsystemimplementation.Java;

public class Machine implements Listener
{
    protected final String [] types = {"Conveyor", "Robot", "Memory", "Fixture", "TurnTable", "Other"};
    private String name;
    private String type;
    private String description;
    private MachineController machineController;
    private Mailbox cellMailbox;

    public Machine(String name, String type, MachineController machineController, Mailbox cellMailbox)
    {
	this.name = name;
	this.type = type;
	this.machineController = machineController;
	description = null;
	this.cellMailbox = cellMailbox;
	cellMailbox.register(this);
    }
    
    public String getID() // to implement the Listener interface
    {
	return getName();
    }

    public void receiveMessage(Message msg)
    {
	if (msg.getType().equals("performEOP"))
	{
	    System.err.println("Machine " +name+ " performing EOP " + ((Integer) msg.getContent()).intValue() + " .");
	    boolean EOPperformedOK = machineController.performEOP( ( (Integer) msg.getContent() ).intValue() );
	    cellMailbox.send( new Message( name,  msg.getSender(), "EOPDone", EOPperformedOK ) );
	    
	}
	else if (msg.getType().equals("externalCheckOfComponent"))
	{
	    cellMailbox.send( new Message( name, msg.getSender(), "confirmExternalComponent", machineController.checkComponent( (ComponentCheck) msg.getContent() ) ) );
	}
	else if (msg.getType().equals("confirmExternalComponent"))
	{
	    machineController.confirmExternalComponent(msg.getSender(), (ComponentConfirmation) msg.getContent());
	}
	else if (msg.getType().equals("externalRequestOfComponent"))
	{
	    cellMailbox.send( new Message( name, msg.getSender(), "reportExternalComponent", machineController.requestComponent( (String) msg.getContent() ) ) );
	}
	else if (msg.getType().equals("reportExternalComponent"))
	{
	    machineController.reportExternalComponent(msg.getSender(), (ComponentReport) msg.getContent());
	}
	else if (msg.getType().equals("confirmZoneState"))
	{
	    machineController.confirmZone( msg.getSender(), ( (Boolean) msg.getContent() ).booleanValue() );
	}
	else if (msg.getType().equals("reportZoneState"))
	{
	    machineController.reportZone( msg.getSender(), (String) msg.getContent() );
	}
	else
	{
	    System.err.println("Wrong message type sent to Coordinator!");
	}
    }

    public void checkExternalComponent(String machine, String componentName, String value)
    {
	cellMailbox.send( new Message( name, machine, "externalCheckOfComponent", new ComponentCheck(componentName, value) ) ); 
    }

    public void requestExternalComponent(String machine, String componentName)
    {
	cellMailbox.send( new Message( name, machine, "externalRequestOfComponent", componentName ) ); 
    }
    
    public void orderZone(String zone, String state)
    {
	cellMailbox.send( new Message( name, zone, "orderState", state ) );
    }

    public void checkZone(String zone, String state)
    {
	cellMailbox.send( new Message( name, zone, "checkState", state ) );
    }

    public void requestZone(String zone)
    {
	cellMailbox.send( new Message( name, zone, "requestState", null ) );
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

}