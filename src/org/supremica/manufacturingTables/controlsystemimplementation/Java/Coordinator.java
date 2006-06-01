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
 * The Coordinator class sends EOPNumbers to the Machines, via a mailbox,
 * according to the SOP for the current task.
 *
 * Created: Mon Apr  24 14:20:32 2006
 *
 * @author Oscar
 * @version 1.0
 */
package org.supremica.manufacturingTables.controlsystemimplementation.Java;

public class Coordinator implements Listener
{
    private Mailbox mailbox;
    private boolean performsTask;
    private String ID;

    public Coordinator(Mailbox mailbox)
    {
	this.mailbox = mailbox;
	performsTask = false;
	ID = "Coordinator";
	mailbox.register(this);
    }

    public void performTask(String task)
    {
	if (task.equals("weld floor") && !performsTask)
	    {
		performsTask = true;
		//read the SOP...!

		//Message.TYPE[0];
		mailbox.send(new Message(ID, "150FIX152", "performEOP", 44));
	    }
	else 
	    {
		System.err.println("Unknown task or already busy performing a task!");
	    }
    }
    
    // Do not need to check if the message is for me since it allways is!
    public void receiveMessage(Message msg)
    {
	if (performsTask && msg.getType().equals("EOPDone"))
	    {
		// Here must be added code to check who sent the message
		if (((Boolean) msg.getContent()).booleanValue())
		    {
			System.err.println("The EOP has been performed with outstanding results!");
		    }
		else
		    {
			System.out.println("The EOP could not be performed!");
		    }
		performsTask = false;
	    }
	else
	    {
		System.err.println("Wrong message or message type sent to Coordinator!");
	    }
    }
    
    public String getID()
    {
	return ID;
    }
    
}