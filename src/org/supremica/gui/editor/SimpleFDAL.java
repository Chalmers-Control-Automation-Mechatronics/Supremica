
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
package org.supremica.gui.editor;

import java.awt.Color;
import java.awt.Point;
import com.nwoods.jgo.layout.JGoForceDirectedAutoLayout;
import com.nwoods.jgo.layout.JGoNetwork;
import com.nwoods.jgo.layout.JGoNetworkLink;
import com.nwoods.jgo.layout.JGoNetworkNode;
import com.nwoods.jgo.JGoSelection;

public class SimpleFDAL
	extends JGoForceDirectedAutoLayout
{
	public SimpleFDAL()
	{
		super();
	}

	public SimpleFDAL(AutomatonDocument pGoDoc)
	{
		super(pGoDoc);
	}

	public SimpleFDAL(JGoSelection sel)
	{
		super(sel);
	}

	public SimpleFDAL(AutomatonDocument pGoDoc, JGoNetwork pNetwork, int Nmax_iterations)
	{
		super(pGoDoc, pNetwork, Nmax_iterations);
	}

	public SimpleFDAL(AutomatonDocument pGoDoc, int Nmax_iterations, double gravfieldX, double gravfieldY, double rcharge, double rmass, boolean rfixed, double gcharge, double gmass, boolean gfixed, double bcharge, double bmass, boolean bfixed, double rrlength, double rrstiffness, double rglength, double rgstiffness, double rblength, double rbstiffness, double gglength, double ggstiffness, double gblength, double gbstiffness, double bblength, double bbstiffness, AutomataEditor app)
	{
		super(pGoDoc, Nmax_iterations);

		mygravfieldX = gravfieldX;
		mygravfieldY = gravfieldY;
		myrcharge = rcharge;
		myrmass = rmass;
		myrfixed = rfixed;
		mygcharge = gcharge;
		mygmass = gmass;
		mygfixed = gfixed;
		mybcharge = bcharge;
		mybmass = bmass;
		mybfixed = bfixed;
		myrrlength = rrlength;
		myrrstiffness = rrstiffness;
		myrglength = rglength;
		myrgstiffness = rgstiffness;
		myrblength = rblength;
		myrbstiffness = rbstiffness;
		mygglength = gglength;
		myggstiffness = ggstiffness;
		mygblength = gblength;
		mygbstiffness = gbstiffness;
		mybblength = bblength;
		mybbstiffness = bbstiffness;
		myApp = app;
	}

	public SimpleFDAL(AutomatonDocument pGoDoc, JGoNetwork pNetwork, int Nmax_iterations, double gravfieldX, double gravfieldY, double rcharge, double rmass, boolean rfixed, double gcharge, double gmass, boolean gfixed, double bcharge, double bmass, boolean bfixed, double rrlength, double rrstiffness, double rglength, double rgstiffness, double rblength, double rbstiffness, double gglength, double ggstiffness, double gblength, double gbstiffness, double bblength, double bbstiffness, AutomataEditor app)
	{
		super(pGoDoc, pNetwork, Nmax_iterations);

		mygravfieldX = gravfieldX;
		mygravfieldY = gravfieldY;
		myrcharge = rcharge;
		myrmass = rmass;
		myrfixed = rfixed;
		mygcharge = gcharge;
		mygmass = gmass;
		mygfixed = gfixed;
		mybcharge = bcharge;
		mybmass = bmass;
		mybfixed = bfixed;
		myrrlength = rrlength;
		myrrstiffness = rrstiffness;
		myrglength = rglength;
		myrgstiffness = rgstiffness;
		myrblength = rblength;
		myrbstiffness = rbstiffness;
		mygglength = gglength;
		myggstiffness = ggstiffness;
		mygblength = gblength;
		mygbstiffness = gbstiffness;
		mybblength = bblength;
		mybbstiffness = bbstiffness;
		myApp = app;
	}

	public double getElectricalFieldX(Point xy)
	{
		double border = 50.0;
		double min = 0.0;
		double max = getDocument().getDocumentSize().width;

		if (xy.x <= 0.0)
		{
			return 300.0;
		}

		if (xy.x < min + border)
		{
			return (300.0 / ((min - xy.x) * (min - xy.x)));
		}

		return 0.0;
	}

	public double getElectricalFieldY(Point xy)
	{
		double border = 50.0;
		double min = 0.0;
		double max = getDocument().getDocumentSize().height;

		if (xy.x <= 0.0)
		{
			return 300.0;
		}

		if (xy.y < min + border)
		{
			return (300.0 / ((min - xy.y) * (min - xy.y)));
		}

		return 0.0;
	}

	public double getGravitationalFieldX(Point xy)
	{
		return mygravfieldX;
	}

	public double getGravitationalFieldY(Point xy)
	{
		return mygravfieldY;
	}

	public double getElectricalCharge(JGoNetworkNode pNode)
	{
		if (pNode.getJGoObject() != null)
		{
			Color color = ((StateNode) (pNode.getJGoObject())).getColor();

			if (color == Color.red)
			{
				return myrcharge;
			}
			else if (color == Color.green)
			{
				return mygcharge;
			}
			else if (color == Color.blue)
			{
				return mybcharge;
			}

			return myrcharge;
		}

		return super.getElectricalCharge(pNode);
	}

	public double getGravitationalMass(JGoNetworkNode pNode)
	{
		if (pNode.getJGoObject() != null)
		{
			Color color = ((StateNode) (pNode.getJGoObject())).getColor();

			if (color == Color.red)
			{
				return myrmass;
			}
			else if (color == Color.green)
			{
				return mygmass;
			}
			else if (color == Color.blue)
			{
				return mybmass;
			}

			return myrmass;
		}

		return super.getGravitationalMass(pNode);
	}

	public boolean isFixed(JGoNetworkNode pNode)
	{
		if (pNode.getJGoObject() != null)
		{
			Color color = ((StateNode) (pNode.getJGoObject())).getColor();

			if (color == Color.red)
			{
				return myrfixed;
			}
			else if (color == Color.green)
			{
				return mygfixed;
			}
			else if (color == Color.blue)
			{
				return mybfixed;
			}

			return myrfixed;
		}

		return super.isFixed(pNode);
	}

	public double getSpringLength(JGoNetworkLink pLink)
	{
		JGoNetworkNode pFromNode = pLink.getFromNode();
		JGoNetworkNode pToNode = pLink.getToNode();

		if ((pFromNode.getJGoObject() != null) && (pToNode.getJGoObject() != null))
		{
			Color fromColor = ((StateNode) (pFromNode.getJGoObject())).getColor();
			Color toColor = ((StateNode) (pToNode.getJGoObject())).getColor();

			if (fromColor == Color.red)
			{
				if (toColor == Color.red)
				{
					return myrrlength;
				}
				else if (toColor == Color.green)
				{
					return myrglength;
				}
				else if (toColor == Color.blue)
				{
					return myrblength;
				}
			}
			else if (fromColor == Color.green)
			{
				if (toColor == Color.red)
				{
					return myrglength;
				}
				else if (toColor == Color.green)
				{
					return mygglength;
				}
				else if (toColor == Color.blue)
				{
					return mygblength;
				}
			}
			else if (fromColor == Color.blue)
			{
				if (toColor == Color.red)
				{
					return myrblength;
				}
				else if (toColor == Color.green)
				{
					return mygblength;
				}
				else if (toColor == Color.blue)
				{
					return mybblength;
				}
			}

			return myrrlength;
		}

		return super.getSpringLength(pLink);
	}

	public double getSpringStiffness(JGoNetworkLink pLink)
	{
		JGoNetworkNode pFromNode = pLink.getFromNode();
		JGoNetworkNode pToNode = pLink.getToNode();

		if ((pFromNode.getJGoObject() != null) && (pToNode.getJGoObject() != null))
		{
			Color fromColor = ((StateNode) (pFromNode.getJGoObject())).getColor();
			Color toColor = ((StateNode) (pToNode.getJGoObject())).getColor();

			if (fromColor == Color.red)
			{
				if (toColor == Color.red)
				{
					return myrrstiffness;
				}
				else if (toColor == Color.green)
				{
					return myrgstiffness;
				}
				else if (toColor == Color.blue)
				{
					return myrbstiffness;
				}
			}
			else if (fromColor == Color.green)
			{
				if (toColor == Color.red)
				{
					return myrgstiffness;
				}
				else if (toColor == Color.green)
				{
					return myggstiffness;
				}
				else if (toColor == Color.blue)
				{
					return mygbstiffness;
				}
			}
			else if (fromColor == Color.blue)
			{
				if (toColor == Color.red)
				{
					return myrbstiffness;
				}
				else if (toColor == Color.green)
				{
					return mygbstiffness;
				}
				else if (toColor == Color.blue)
				{
					return mybbstiffness;
				}
			}

			return myrrstiffness;
		}

		return super.getSpringStiffness(pLink);
	}

	public void progressUpdate(double progress)
	{
		if (progress == 1.0)
		{
			myApp.setStatus("Simple Force-Directed Auto-Layout: 100% done.");
		}
		else
		{
			String s = Double.toString(100 * progress);
			int index = s.indexOf('.');
			String value;

			if (index != -1)
			{
				value = s.substring(0, Math.min(s.length(), index + 4));
			}
			else
			{
				value = s;
			}

			myApp.setStatus("Simple Force-Directed Auto-Layout: " + value + "% done.");
			myApp.getCurrentView().paintImmediately(myApp.getCurrentView().getViewRect());
		}
	}

	double mygravfieldX;
	double mygravfieldY;
	double myrcharge;
	double myrmass;
	boolean myrfixed;
	double mygcharge;
	double mygmass;
	boolean mygfixed;
	double mybcharge;
	double mybmass;
	boolean mybfixed;
	double myrrlength;
	double myrrstiffness;
	double myrglength;
	double myrgstiffness;
	double myrblength;
	double myrbstiffness;
	double mygglength;
	double myggstiffness;
	double mygblength;
	double mygbstiffness;
	double mybblength;
	double mybbstiffness;
	AutomataEditor myApp = null;
}
