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
 * The Zone class keeps track of whether the zone is booked or not and, if booked, which machine that
 * has booked the zone. The zone is unbooked as default.
 *
 * Created: Tue Okt  30 11:17 2006
 *
 * @author Oscar
 * @version 1.0
 */
package org.supremica.manufacturingTables.controlsystemimplementation.IEC61499;

public class Zone implements Listener
{
    public static final String FREE_ZONE_TOKEN = 
	org.supremica.manufacturingTables.controlsystemdata.ZoneData.FREE_ZONE_TOKEN;
    public static final String BOOKED_ZONE_TOKEN = 
	org.supremica.manufacturingTables.controlsystemdata.ZoneData.BOOKED_ZONE_TOKEN;
    public static final String OCCUPIED_ZONE_TOKEN = "o";
    // This means that the zone is booked by another machine then the requesting

    private Mailbox mailbox; // No longer used in the Fuber implementation
    private ZoneThread zoneThread; // All communication with the mailbox is now done via the zoneThread
    private String zoneName;
    private boolean booked;
    private String bookingMachine;

    // Constructor. The zone is unbooked as default.
    public Zone(String zoneName, Mailbox mailbox)
    {
	this.mailbox = mailbox;
	zoneThread = null;
	this.zoneName = zoneName;
	//	zoneThread.register(this);
	booked = false;
	bookingMachine = null;
    }

    public void setThread(ZoneThread zoneThread)
    {
	this.zoneThread = zoneThread;
	this.zoneThread.register(this);
    }
 
    public synchronized void receiveMessage(Message msg)
    {
	// Request ZoneState
	// Report the state, according to the requesting machine, of this zone
	if (msg.getType().equals("requestState"))
	{
	    // Is the zone booked by this machine?
	    if (booked && bookingMachine.equals(msg.getSender()))
	    {
		System.err.println("Zone " + zoneName + " is booked by the requesting machine.");
		zoneThread.send( new Message( zoneName, msg.getSender(), "reportZoneState", BOOKED_ZONE_TOKEN ) );
	    }
	    // Is the zone free?
	    else if (!booked)
	    {
		System.err.println("Zone " + zoneName + " reports that the zone is free.");
		zoneThread.send( new Message( zoneName, msg.getSender(), "reportZoneState", FREE_ZONE_TOKEN ) );
	    }
	    // Here the zone is occupied by another machine
	    else
	    {
		System.err.println("Zone " + zoneName + " is occupied by another machine: " + bookingMachine);
		zoneThread.send( new Message( zoneName, msg.getSender(), "reportZoneState", OCCUPIED_ZONE_TOKEN ) );
	    }
	}
	// Check ZoneState
	else if (msg.getType().equals("checkState"))
	{
	    // Checking if the zone is free, and it is 
	    if ( ( (String) msg.getContent() ).equals( FREE_ZONE_TOKEN ) && !booked )
	    {
		zoneThread.send( new Message( zoneName, msg.getSender(), "confirmZoneState", true ) );
	    }
	    // Checking if the zone is booked by this machine, and it is
	    else if ( ( (String) msg.getContent() ).equals( BOOKED_ZONE_TOKEN ) && booked
		      && bookingMachine.equals(msg.getSender()) )
	    {
		zoneThread.send( new Message( zoneName, msg.getSender(), "confirmZoneState", true ) );
	    }
	    // Otherwise the zone is not in the expected state
	    else
	    {
		zoneThread.send( new Message( zoneName, msg.getSender(), "confirmZoneState", false ) );
	    }
	}
	//Order ZoneState
	else if (msg.getType().equals("orderState"))
	{
	    // Currently not booked zone
	    if (!booked)
	    {
		// Shall be booked
		if ( ( (String) msg.getContent() ).equals( BOOKED_ZONE_TOKEN ) )
		{
		    booked = true;
		    bookingMachine = msg.getSender();
		    System.err.println("Zone " + zoneName + " was successfully booked by machine: " + bookingMachine);
		}
	    }
	    // Currently booked zone
	    else
	    {
		// Booked by this machine
		if (bookingMachine.equals(msg.getSender()))
		{
		    // Shall be unbooked
		    if ( ( (String) msg.getContent() ).equals( FREE_ZONE_TOKEN ) )
		    {
			booked = false;
			System.err.println("Zone " + zoneName + " was successfully unbooked by machine: " 
					   + bookingMachine);
			bookingMachine = null;
		    }
		}
		// Not booked by this machine
		// Here might instead be added code that waits fore the zone to be unbooked by the other machine
		// but now we return a false.
		else 
		{
		    System.err.println("The zone " + zoneName + " is booked by another machine: " + bookingMachine);
		    zoneThread.send( new Message( zoneName, msg.getSender(), "confirmZoneState", false ) );
		    return;
		}
	    }
	    zoneThread.send( new Message( zoneName, msg.getSender(), "confirmZoneState", true ) );
	}
	
	else
	{
	    System.err.println("Unknown message type for the zone " + zoneName + "!");
	}
    }    

    public String getID()
    {
	return zoneName;
    }
    
}