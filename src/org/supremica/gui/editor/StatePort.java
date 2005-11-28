
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
package org.supremica.gui.editor;

import com.nwoods.jgo.*;
import java.awt.*;

/**
* To get the link arrowheads' point at the edge of the ellipse,
* we need to override how the link point is computed, rather
* than depending on the built-in mechanism specifying a spot.
*/
public class StatePort
	extends JGoPort
{
	public JGoEllipse myEllipse = null;

	public StatePort()
	{
		super();

		setSelectable(false);
		setDraggable(false);
		setStyle(StyleEllipse);    // black circle/ellipse

		// use custom link spots for both links coming in and going out
		setFromSpot(JGoObject.NoSpot);
		setToSpot(JGoObject.NoSpot);
	}

	/**
	* Return a point on the edge of the ellipse
	*/
	public Point getLinkPointFromPoint(int x, int y, Point p)
	{
		if (p == null)
		{
			p = new Point();
		}

		p.x = x;
		p.y = y;

		Point center = getSpotLocation(JGoObject.Center);
		double x1 = (double) x;
		double y1 = (double) y;
		double x2 = center.x;
		double y2 = center.y;

		center = myEllipse.getSpotLocation(JGoObject.Center);

		double U = center.x;
		double V = center.y;
		double P = myEllipse.getWidth();
		double Q = myEllipse.getHeight();
		double A = (4.0 / (P * P)) * (x2 - x1) * (x2 - x1) + (4.0 / (Q * Q)) * (y2 - y1) * (y2 - y1);
		double B = (8.0 / (P * P)) * (x2 - x1) * (x1 - U) + (8.0 / (Q * Q)) * (y2 - y1) * (y1 - V);
		double C = (4.0 / (P * P)) * (x1 - U) * (x1 - U) + (4.0 / (Q * Q)) * (y1 - V) * (y1 - V) - 1.0;
		double D = B * B - 4.0 * A * C;

		if (D < 0)
		{
			return p;
		}

		double T1 = (-1.0 * B - Math.sqrt(D)) / (2.0 * A);
		double T2 = (-1.0 * B + Math.sqrt(D)) / (2.0 * A);
		double t1 = Math.min(T1, T2);
		double t2 = Math.max(T1, T2);

		if ((0.0 <= t1) && (t1 <= 1.0))
		{
			p.x = (int) Math.round(x1 + t1 * (x2 - x1));
			p.y = (int) Math.round(y1 + t1 * (y2 - y1));

			return p;
		}
		else if ((0.0 <= t2) && (t2 <= 1.0))
		{
			p.x = (int) Math.round(x1 + t2 * (x2 - x1));
			p.y = (int) Math.round(y1 + t2 * (y2 - y1));

			return p;
		}

		return p;
	}

	/*
	 *       boolean isValidDestination()
	 *       {
	 *               return true;
	 *       }
	 *
	 *       boolean isValidLink()
	 *       {
	 *               return true;
	 *       }
	 *
	 *       boolean isValidSource()
	 *       {
	 *               return true;
	 *       }
	 */
}
