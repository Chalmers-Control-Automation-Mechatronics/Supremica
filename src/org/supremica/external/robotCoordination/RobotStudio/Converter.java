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
 * Haradsgatan 26A
 * 431 42 Molndal
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

import com.inzoom.comjni.Variant;
import org.supremica.external.robotCoordination.*;
import org.supremica.external.comInterfaces.robotstudio_3_1.RobotStudio.ITransform;

/**
 * A static class that contains conversion methods between Supremica and RobotStudio.
 */
public class Converter {
    /**
     * Typecast i into Variant, for convenience! (Variant is something like
     * VB:s counterpart of java's Object.)
     */
    static Variant var(int i)
		throws Exception
    {
		return new Variant(i);
    }

    /**
     * Typecast i into Variant, for convenience! (Variant is something like
     * VB:s counterpart of java's Object.)
     */
    static Variant var(boolean i)
		throws Exception
    {
		return new Variant(i);
    }

    /**
     * Typecast i into Variant, for convenience! (Variant is something like
     * VB:s counterpart of java's Object.)
     */
    static Variant var(String i)
		throws Exception
    {
		return new Variant(i);
    }

    /**
     * Converts a RobotStudio coordinate to a Supremica Coordinate (or Supremica index)
     */
    static Coordinate toCoordinate(double x, double y, double z) 
    {
	double[] scaling = RSCell.boxDimensions;
		
	x /= scaling[0];
	y /= scaling[1];
	z /= scaling[2];

	return new Coordinate((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z));
    }

    /**
     * Converts a RobotStudio transform to a Supremica Coordinate (or Supremica index)
     */
    static Coordinate toCoordinate(ITransform trans) 
    {
	try
	{
	    return toCoordinate(trans.getX(), trans.getY(), trans.getZ());
	}
	catch (Exception e) 
	{
	    e.printStackTrace();
	    return null;
	}
    }

    /**
     * Converts a Supremica Coordinate to a 3D-point in the world frame of RobotStudio
     */
    static double[] toRSPoint(Coordinate coord) 
    {
	double[] scaling = RSCell.boxDimensions;
	double[] pointCoord = new double[3];
		
	pointCoord[0] = coord.getX() * scaling[0];
	pointCoord[1] = coord.getY() * scaling[1];
	pointCoord[2] = coord.getZ() * scaling[2];
		
	return pointCoord;
    }
}