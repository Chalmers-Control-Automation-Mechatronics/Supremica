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

import org.supremica.external.robotCoordination.Configuration;
import org.supremica.external.comInterfaces.robotstudio_3_1.RobotStudio.Target;
import org.supremica.external.comInterfaces.robotstudio_3_1.RobotStudio.ITarget;
import org.supremica.log.*;

/**
 * Implementation of the Configuration-interface for RobotStudio.
 */
public class RSConfiguration
    extends Configuration
{
    private static Logger logger = LoggerFactory.createLogger(RSConfiguration.class);

	/** The RobotStudio target. */
    private Target target;

    public RSConfiguration(ITarget target)
    {
		try
	    {
			this.target = Target.getTargetFromUnknown(target);
	    }
		catch (Exception ex)
	    {

			// Was there a problem?
			System.err.println("Error in constructor RSConfiguration." + ex);
	    }
    }

    public String toString()
    {
		return "'" + getName() + "'";
    }

    /**
     * Returns the RobotStudio target.
     */
    public Target getRobotStudioTarget()
    {
		return target;
    }

    /////////////////////////////////////
    // Configuration interface methods //
    /////////////////////////////////////

    public String getName()
    {
		try
	    {
			// Return the name (remove the last two characters ":1" since they are ugly)
			return target.getName().substring(0, target.getName().length() - 2);
	    }
		catch (Exception ex)
	    {
			System.err.println("Robot has no name? " + ex);
	    }

		return "";
    }

	public boolean equals(Object other)
	{
		return getRobotStudioTarget().equals(((RSConfiguration) other).getRobotStudioTarget());
	}
}