
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
package org.supremica.util.IDD;



import java.io.*;

import java.util.*;


public class IDDToDot
{

	private IDD theIDD;
	private boolean leftToRight = false;
	private boolean withCircles = true;
	private boolean withId = false;
	private boolean withLevel = false;

	public IDDToDot(IDD theIDD)
	{
		this.theIDD = theIDD;
	}

	public boolean isLeftToRight()
	{
		return leftToRight;
	}

	public void setLeftToRight(boolean leftToRight)
	{
		this.leftToRight = leftToRight;
	}

	public void setWithCircles(boolean withCircles)
	{
		this.withCircles = withCircles;
	}

	public void setWithId(boolean withId)
	{
		this.withId = withId;
	}

	public void serialize(PrintWriter pw)
		throws Exception
	{

		pw.println("digraph IDD {");
		pw.println("\tcenter = true;");

		if (leftToRight)
		{
			pw.println("\trankdir = LR;");
		}

		if (withCircles)
		{
			pw.println("\tnode [shape = circle];");
		}
		else
		{
			pw.println("\tnode [shape = plaintext];");
		}

		// DestStateMap destStateMap = null;
		// destStateMap = new DestStateMap(theIDD.nbrOfNodes());
		// Write out all ranks
		pw.println("{ rank = same;");
		pw.print("\"level_" + 0 + "\"; ");
		pw.print("\"" + theIDD.getRootNode().getId() + "\";");
		pw.println("}");

		int nbrOfLevels = theIDD.nbrOfLevels();

		for (int i = 1; i < nbrOfLevels; i++)
		{
			pw.println("{ rank = same; ");
			pw.println("\"level_" + i + "\"; ");

			List currNodeList = theIDD.getNodesAtLevel(i);
			Iterator nodeIt = currNodeList.iterator();

			while (nodeIt.hasNext())
			{
				Node currNode = (Node) nodeIt.next();

				pw.print("\"" + currNode.getId() + "\"; ");
			}

			pw.println("}");
		}

		pw.println("{ rank = same; ");
		pw.println("\"level_" + nbrOfLevels + "\"; ");
		pw.print("\"" + theIDD.getFalseNode().getId() + "\"; ");
		pw.print("\"" + theIDD.getTrueNode().getId() + "\"; ");
		pw.println("}");

		// Write out all arcs
		serialize(pw, theIDD.getRootNode());

		for (int i = 1; i < nbrOfLevels; i++)
		{
			List currNodeList = theIDD.getNodesAtLevel(i);
			Iterator nodeIt = currNodeList.iterator();

			while (nodeIt.hasNext())
			{
				Node currNode = (Node) nodeIt.next();

				serialize(pw, currNode);
			}
		}

		serialize(pw, theIDD.getFalseNode());
		serialize(pw, theIDD.getTrueNode());

		for (int i = 0; i < nbrOfLevels; i++)
		{
			int j = i + 1;

			pw.print("\t\"" + "level_" + i + "\" [label = \"");
			pw.print(i);
			pw.println("\"]; ");
			pw.print("\t\"" + "level_" + i + "\" -> \"" + "level_" + j);
			pw.println("\";");
		}

		int j = nbrOfLevels;

		pw.print("\t\"" + "level_" + j + "\" [label = \"");
		pw.print(j);
		pw.println("\"]; ");
		pw.println("}");
		pw.flush();
		pw.close();
	}

	private void serialize(PrintWriter pw, Node theNode)
	{

		pw.print("\t\"" + theNode.getId() + "\" [label = \"");

		boolean prevText = false;

		if (theNode.isFalse())
		{
			pw.print("0");

			prevText = true;
		}
		else if (theNode.isTrue())
		{
			pw.print("1");

			prevText = true;
		}

		if (withId)
		{
			if (prevText)
			{
				pw.print(":");
			}

			pw.print(theNode.getId());

			prevText = true;
		}

		if (withLevel)
		{
			if (prevText)
			{
				pw.print(":");
			}

			pw.print(theNode.getLevel());
		}

		pw.println("\"]; ");

		Node[] children = theNode.getChildren();

		if (children != null)
		{
			for (int i = 0; i < children.length; i++)
			{
				Node currChild = children[i];

				pw.print("\t\"" + theNode.getId() + "\" -> \"" + currChild.getId());
				pw.print("\" [ label = \"");
				pw.print(i);
				pw.println("\" ];");
			}
		}
	}

	public void serialize(String fileName)
		throws Exception
	{
		serialize(new PrintWriter(new FileWriter(fileName)));
	}
}
