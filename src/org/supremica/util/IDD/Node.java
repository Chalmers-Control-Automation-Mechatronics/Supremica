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

import java.util.*;

public final class Node
{
	// level -1: true node
	// level -2: false node
	// level -3: undefined
	// level 0: root node
	// level >=1: non-terminal nodes (except the root node)
	private int level = -3;

	private static int nextFreeId = 0;

	private final int id;

	private Node[] children;

	public Node(int nbrOfChildren)
	{
		this(nbrOfChildren, -3);
	}

	public Node(int nbrOfChildren, int level)
	{
		this.level = level;

		id = nextFreeId++;

		if (nbrOfChildren > 0)
		{ // The terminal nodes does not have children
			children = new Node[nbrOfChildren];
		}
		else
		{
			children = null;
		}
	}

	/**
	 * Creates a new node with the same number of children.
	 * The children are not copied.
	 */
	public Node(Node cloneNode)
	{
		this.level = cloneNode.level;

		int nbrOfChildren = cloneNode.children.length;
		if (nbrOfChildren > 0)
		{
			children = new Node[nbrOfChildren];
		}
		else
		{
			children = null;
		}

		id = nextFreeId++;
	}


	/**
	 * Two nodes are equal if they have the same children.
	 */
	public boolean equals(Object other)
	{
		Node otherNode = (Node)other;
		return id == otherNode.id;
	}

	public boolean sameChildren(Node other)
	{
		Node[] otherChildren = other.children;
		for (int i = 0; i < children.length; i++)
		{
			// Same is the same objects
			if (children[i] != otherChildren[i])
			{
				return false;
			}
		}
		return true;
	}

	public int hashCode()
	{
		return id;
	}

	public void dispose()
	{
		children = null;
	}

	public void setLevel(int level)
	{
		this.level = level;
	}

	public int getLevel()
	{
		return level;
	}

	public Node[] getChildren()
	{
		return children;
	}

	public Node getChild(int index)
	{
		return children[index];
	}

	public void setChild(int index, Node child)
	{
		children[index] = child;
	}

	public boolean isRoot()
	{
		return level == 0;
	}

	public void setAsRoot()
	{
		this.level = 0;
	}

	public void setAsTrue()
	{
		this.level = -1;
	}

	public void setAsFalse()
	{
		this.level = -2;
	}

	public boolean isTerminal()
	{
		return level < 0;
	}

	public boolean isTrue()
	{
		return level == -1;
	}

	public boolean isFalse()
	{
		return level == -2;
	}

	public void negate()
	{
		if (level == -1)
		{
			level = -2;
		}
		else if (level == -2)
		{
			level = -1;
		}
	}

	public int getId()
	{
		return id;
	}

	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("Id: " + id);
		if (isRoot())
		{
			sb.append(" Root");
		}
		else if (isTrue())
		{
			sb.append(" True");
		}
		else if (isFalse())
		{
			sb.append(" False");
		}
		sb.append("\nChildren: ");
		if (children != null)
		{
			for (int i = 0; i < children.length; i++)
			{
				sb.append(children[i] + " ");
			}
			sb.append("\n");
		}
		else
		{
			sb.append("none\n");
		}
		return sb.toString();
	}
}
