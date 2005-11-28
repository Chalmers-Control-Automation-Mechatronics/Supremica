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

package org.supremica.external.robotCoordination.RobotStudio;

import org.supremica.external.comInterfaces.robotstudio_3_1.RobotStudio.*;
import org.supremica.log.*;

/**
 * Keeps track of the controller status, is it started or shut down?
 * The _MechanismEventsAdapter lacks information about the current mechanism
 * and its controller but this information is supplied in this class...
 */
public class ControllerListener
    extends _MechanismEventsAdapter
{
    private static Logger logger = LoggerFactory.createLogger(ControllerListener.class);

    private boolean controllerRunning = false;    // Used in the wait-methods

    /**
     * Initialize the listener with the isRunning argument.
     */
    public ControllerListener(boolean isRunning)
    {
		controllerRunning = isRunning;
    }

    /**
     * Returns when the controller has started
     *
     * This method is not used (?) because it's need seems to have
     * disappeared with RS3.0.
     */
    public synchronized void waitForControllerStart()
    {
		try
	    {
			while (!controllerRunning)
		    {
				wait();
		    }

			// Make sure the controller is really started before we return
			//Thread.sleep(3000);
	    }
		catch (Exception ex)
	    {

			//System.out.println("Interrupted! " + ex);
			logger.error("Interrupted! " + ex);
	    }

		return;
    }

    /**
     * Returns when the controller has shut down
     */
    public synchronized void waitForControllerShutDown()
    {
		try
	    {
			while (controllerRunning)
		    {
				wait();
		    }

			// Make sure the controller is really shut down before we return
			//Thread.sleep(2500);
	    }
		catch (Exception ex)
	    {
			//System.out.println("Interrupted! " + ex);
			logger.error("Interrupted! " + ex);
	    }

		return;
    }

    // Implementation of _Mechanismorg.supremica.external.comInterfaces.robotstudio_3_1.RobotStudioEventsAdapter methods

    public int beforeControllerStarted()
    {
		// This method works fine... but is quite useless here? Post some info...
		try
	    {
			logger.debug("Starting Virtual Controller for " + RSCell.station.getName() + "...");
	    }
		catch (Exception whatever) {}

		return 0;
    }

    public int afterControllerStarted()
    {
		// This never happens!? (But since RS3.0 we don't seem to need it!?)
		controllerRunning = true;

		logger.fatal("AfterControllerStarted. Tell Hugo you got this message!");
		notify();

		return 0;
    }

    public int afterControllerShutdown()
    {
		// Works fine?
		controllerRunning = false;

		try
	    {
			logger.debug("Virtual Controller shut down for " + RSCell.station.getName() + ".");
	    }
		catch (Exception whatever) {}

		notify();

		return 0;
    }
}