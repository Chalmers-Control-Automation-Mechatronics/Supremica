
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

import java.util.Iterator;
import java.util.LinkedList;

import com.nwoods.jgo.layout.JGoLayeredDigraphAutoLayout;
import com.nwoods.jgo.layout.JGoNetwork;
import com.nwoods.jgo.layout.JGoNetworkLink;
import com.nwoods.jgo.layout.JGoNetworkNode;
import com.nwoods.jgo.layout.JGoLayeredDigraphAutoLayoutLinkData;
import com.nwoods.jgo.JGoPort;
import com.nwoods.jgo.JGoLink;


public class SimpleLDAL
	extends JGoLayeredDigraphAutoLayout
{

	public SimpleLDAL()
	{
		super();
	}

	public SimpleLDAL(AutomatonDocument pGoDoc)
	{
		super(pGoDoc);
	}

	public SimpleLDAL(AutomatonDocument pGoDoc, JGoNetwork pNetwork)
	{
		super(pGoDoc, pNetwork);
	}

	public SimpleLDAL(AutomatonDocument pGoDoc, int NlayerSpacing, int NcolumnSpacing, int NdirectionOption, int NcycleremoveOption, int NlayeringOption, int NinitializeOption, int Niterations, int NaggressiveOption, AutomataEditor app)
	{

		super(pGoDoc, NlayerSpacing, NcolumnSpacing, NdirectionOption, NcycleremoveOption, NlayeringOption, NinitializeOption, Niterations, NaggressiveOption);

		myApp = app;
	}

	public SimpleLDAL(AutomatonDocument pGoDoc, JGoNetwork pNetwork, int NlayerSpacing, int NcolumnSpacing, int NdirectionOption, int NcycleremoveOption, int NlayeringOption, int NinitializeOption, int Niterations, int NaggressiveOption, AutomataEditor app)
	{

		super(pGoDoc, pNetwork, NlayerSpacing, NcolumnSpacing, NdirectionOption, NcycleremoveOption, NlayeringOption, NinitializeOption, Niterations, NaggressiveOption);

		myApp = app;
	}

	public int getLinkMinLength(JGoNetworkLink pLink)
	{

		JGoNetworkNode pFromNode = pLink.getFromNode();
		JGoNetworkNode pToNode = pLink.getToNode();

		if ((pFromNode.getJGoObject() != null) && (pToNode.getJGoObject() != null))
		{
			Color fromColor = ((StateNode) (pFromNode.getJGoObject())).getColor();
			Color toColor = ((StateNode) (pToNode.getJGoObject())).getColor();

			if (fromColor == toColor)
			{
				return 1 * super.getLinkMinLength(pLink);
			}
			else
			{
				return 2 * super.getLinkMinLength(pLink);
			}
		}

		return super.getLinkMinLength(pLink);
	}

	public void progressUpdate(double progress)
	{

		if (progress == 1.0)
		{
			myApp.setStatus("Simple Layered Digraph Auto-Layout: 100% done.");
		}
		else
		{
			myApp.setStatus("Simple Layered Digraph Auto-Layout: " + (100 * progress) + "% done.");
		}
	}

	private AutomataEditor myApp = null;
}
