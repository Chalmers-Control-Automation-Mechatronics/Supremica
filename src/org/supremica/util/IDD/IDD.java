
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
import java.io.*;
import org.supremica.log.*;

public final class IDD
{
	private static Logger logger = LoggerFactory.createLogger(IDD.class);
	private final int nbrOfLevels;
	private final int[] nbrOfBranches;
	private final List[] theNodes;
	private final HashMap theCopyCache;
	private final HashMap theComputeCache;
	int nbrOfNodes = 0;
	private final Node rootNode;
	private final Node terminal1Node;
	private final Node terminal2Node;

	/**
	 * Initially the IDD is a tautology, i.e., always evaluates to true;
	 */
	public IDD(int[] nbrOfBranches)
	{
		nbrOfLevels = nbrOfBranches.length;
		this.nbrOfBranches = nbrOfBranches;
		theNodes = new List[nbrOfLevels];
		theCopyCache = new HashMap();
		theComputeCache = new HashMap();

		for (int i = 0; i < nbrOfLevels; i++)
		{
			List newArray = new ArrayList();

			theNodes[i] = newArray;
		}

		// Create Root node
		rootNode = new Node(nbrOfBranches[0]);

		rootNode.setAsRoot();
		addNode(rootNode);

		// Create True node
		terminal1Node = new Node(0);

		terminal1Node.setAsTrue();
		addNode(terminal1Node);

		// Create False node
		terminal2Node = new Node(0);

		terminal2Node.setAsFalse();
		addNode(terminal2Node);

		// Set all children in the rootNode to reference the true node
		for (int i = 0; i < nbrOfBranches[0]; i++)
		{
			rootNode.setChild(i, terminal1Node);
		}
	}

	public IDD(int[] nbrOfBranches, int variable, int value)
	{
		this(nbrOfBranches);

		Node parentNode = rootNode;
		Node falseNode = getFalseNode();
		Node trueNode = getTrueNode();
		Node currNode = rootNode;

		for (int i = 1; i < variable + 1; i++)
		{
			currNode = new Node(nbrOfBranches[i], i);

			addNode(currNode);

			Node[] parentChildren = parentNode.getChildren();

			for (int j = 0; j < parentChildren.length; j++)
			{
				parentChildren[j] = currNode;
			}

			parentNode = currNode;
		}

		Node[] children = currNode.getChildren();

		for (int i = 0; i < children.length; i++)
		{
			if (i == value)
			{
				children[i] = trueNode;
			}
			else
			{
				children[i] = falseNode;
			}
		}
	}

	/**
	 * variable is the index of the variable to set.
	 * values is a set of values to evaluate to true for variable.
	 * It is assumed that values is sorted -- smallest first
	 * Other variables are don't care.
	 */
	public IDD(int[] nbrOfBranches, int variable, int[] values)
	{
		this(nbrOfBranches);

		Node parentNode = rootNode;
		Node falseNode = getFalseNode();
		Node trueNode = getTrueNode();
		Node currNode = rootNode;

		for (int i = 1; i < variable + 1; i++)
		{
			currNode = new Node(nbrOfBranches[i], i);

			addNode(currNode);

			Node[] parentChildren = parentNode.getChildren();

			for (int j = 0; j < parentChildren.length; j++)
			{
				parentChildren[j] = currNode;
			}

			parentNode = currNode;
		}

		Node[] children = currNode.getChildren();
		int j = 0;

		for (int i = 0; i < children.length; i++)
		{
			if (i == values[j])
			{
				children[i] = trueNode;

				j++;
			}
			else
			{
				children[i] = falseNode;
			}
		}
	}

	public static IDD getTrueIDD(int[] nbrOfBranches)
	{
		return new IDD(nbrOfBranches);
	}

	public static IDD getFalseIDD(int[] nbrOfBranches)
	{
		IDD newIDD = getTrueIDD(nbrOfBranches);

		newIDD.not();

		return newIDD;
	}

	/*
	 *       public IDD(int[] nbrOfBranches, int[] evaluateToTrue)
	 *       {
	 *               this(nbrOfBranches);
	 *
	 *               Node parentNode = rootNode;
	 *               Node falseNode = getFalseNode();
	 *
	 *               for (int i = 1; i < evaluateToTrue.length; i++)
	 *               {
	 *
	 *                       Node newNode;
	 *                       if (i < evaluateToTrue)
	 *                       {
	 *                               newNode = new Node(nbrOfBranches[i], i);
	 *                               add(newNode);
	 *                       }
	 *                       else
	 *                       {
	 *                               newNode = getTrueNode();
	 *                       }
	 *
	 *                       Node[] currChildren = parentNode.getChildren();
	 *
	 *                       int currValue = evaluateToTrue[i - 1];
	 *
	 *                       // if currValue == -1 then this variable is a don't care
	 *                       if (currValue == -1)
	 *                       {
	 *                               for (int j = 0; j < nbrOfBranches[i - 1]; j++)
	 *                               {
	 *                                       currChildren[j] = newNode;
	 *                               }
	 *                       }
	 *                       else
	 *                       {
	 *                               for (int j = 0; j < nbrOfBranches[i - 1]; j++)
	 *                               {
	 *                                       if (currValue == j)
	 *                                       {
	 *                                               currChildren[j] = newNode;
	 *                                       }
	 *                                       else
	 *                                       {
	 *                                               currChildren[j] = falseNode;
	 *                                       }
	 *                               }
	 *                       }
	 *
	 *                       parentNode = newNode;
	 *               }
	 *       }
	 */
	public IDD(IDD cloneIDD)
	{
		this(cloneIDD.nbrOfBranches);
	}

	/*
	 *       private Node getNextNode(int[] evaluateToTrue, int level)
	 *       {
	 *               if (level)
	 *       }
	 *
	 */

	/**
	 * The level must be: 1 <= level <= nbrOfLevels - 1
	 * where nbrOfLevels is equals to nbrOfBranches.length
	 */
	public List getNodesAtLevel(int level)
	{
		if (level < 1)
		{
			return null;    // Throw exception?
		}

		if (level >= nbrOfLevels)
		{
			return null;    // Throw exception?
		}

		return theNodes[level];
	}

	/**
	 * Return a node equal to theNode if it exists
	 * otherwise return null
	 */
	public Node getNode(Node theNode)
	{
		int level = theNode.getLevel();
		Iterator nodeIt = theNodes[level].iterator();

		while (nodeIt.hasNext())
		{
			Node currNode = (Node) nodeIt.next();

			if (theNode.sameChildren(currNode))
			{
				return currNode;
			}
		}

		return null;
	}

	/**
	 * The level of theNode must be: 1 <= level <= nbrOfLevels - 1
	 * where nbrOfLevels is equals to nbrOfBranches.length
	 */
	public void addNode(Node theNode)
	{
		int level = theNode.getLevel();

		if (level > 0)
		{
			List levelList = theNodes[level];

			levelList.add(theNode);
		}

		nbrOfNodes++;
	}

	public int nbrOfSatisfyingAssignments()
	{
		boolean debug = true;

		if (debug)
		{
			System.err.println("nbrOfLevels " + nbrOfLevels);
			System.err.println("nbrOfBranches " + arrayToString(nbrOfBranches));
		}

		return nbrOfSatisfyingAssignments(rootNode, -1);
	}

	private int nbrOfSatisfyingAssignments(Node currNode, int parentLevel)
	{
		if (currNode.isTerminal())
		{
			if (currNode.isTrue())
			{
				if (parentLevel < nbrOfLevels - 1)
				{
					int tot = 1;

					for (int i = parentLevel + 1; i < nbrOfLevels; i++)
					{
						tot = tot * nbrOfBranches[i];
					}

					return tot;
				}

				return 1;
			}

			return 0;
		}

		Node[] children = currNode.getChildren();
		int tot = 0;

		for (int i = 0; i < children.length; i++)
		{
			tot = tot + nbrOfSatisfyingAssignments(children[i], parentLevel + 1);
		}

		return tot;
	}

	public Node getRootNode()
	{
		return rootNode;
	}

	public void not()
	{
		terminal1Node.negate();
		terminal2Node.negate();
	}

	public static IDD or(IDD source1IDD, IDD source2IDD)
	{
		IDD newIDD = new IDD(source1IDD);

		newIDD.computeOr(source1IDD.rootNode, source2IDD.rootNode, newIDD.rootNode);

		return newIDD;
	}

	public static IDD and(IDD source1IDD, IDD source2IDD)
	{
		IDD newIDD = new IDD(source1IDD);

		newIDD.computeAnd(source1IDD.rootNode, source2IDD.rootNode, newIDD.rootNode);

		return newIDD;
	}

	private void computeOr(Node source1Node, Node source2Node, Node destNode)
	{
		int level = source1Node.getLevel();
		int nbrOfChildren = nbrOfBranches[level];
		boolean debug = false;

		if (debug)
		{
			System.err.println("computeOr s1: " + source1Node.getId() + " s2: " + source2Node.getId() + " d: " + destNode.getId());
		}

		for (int i = 0; i < nbrOfChildren; i++)
		{
			Node source1ChildNode = source1Node.getChild(i);
			Node source2ChildNode = source2Node.getChild(i);

			if (debug)
			{
				System.err.println("Checking child: " + i);
			}

			if (source1ChildNode.isTrue() || source2ChildNode.isTrue())
			{
				if (debug)
				{
					System.err.println("s1 or s2 true");
				}

				destNode.setChild(i, getTrueNode());
			}
			else if (source1ChildNode.isFalse() && source2ChildNode.isFalse())
			{
				if (debug)
				{
					System.err.println("s1 and s2 false");
				}

				destNode.setChild(i, getFalseNode());
			}
			else if (source1ChildNode.isFalse())
			{    // Note that the source1ChildNode is false but

				// source2ChildNode is not
				// The result is determined by source2ChildNode
				if (debug)
				{
					System.err.println("s1 false");
				}

				Node destChildNode = getFromCopyCache(source2ChildNode);

				if (destChildNode != null)
				{
					destNode.setChild(i, destChildNode);
				}
				else
				{
					destChildNode = new Node(source2ChildNode);

					copySubGraph(source2ChildNode, destChildNode);

					Node destChildNodeCopy = getNode(destChildNode);

					if (destChildNodeCopy != null)
					{
						destChildNode.dispose();
						destNode.setChild(i, destChildNodeCopy);
						addToCopyCache(source2ChildNode, destChildNodeCopy);
					}
					else
					{
						addNode(destChildNode);
						destNode.setChild(i, destChildNode);
						addToCopyCache(source2ChildNode, destChildNode);
					}
				}
			}
			else if (source2ChildNode.isFalse())
			{    // Note that the source2ChildNode is false but

				// source1Child node is not
				// The result is determined by source2ChildNode
				if (debug)
				{
					System.err.println("s2 false");
				}

				Node destChildNode = getFromCopyCache(source1ChildNode);

				if (destChildNode != null)
				{
					destNode.setChild(i, destChildNode);
				}
				else
				{
					destChildNode = new Node(source1ChildNode);

					copySubGraph(source1ChildNode, destChildNode);

					Node destChildNodeCopy = getNode(destChildNode);

					if (destChildNodeCopy != null)
					{
						destChildNode.dispose();
						destNode.setChild(i, destChildNodeCopy);
						addToCopyCache(source1ChildNode, destChildNodeCopy);
					}
					else
					{
						addNode(destChildNode);
						destNode.setChild(i, destChildNode);
						addToCopyCache(source1ChildNode, destChildNode);
					}
				}
			}
			else
			{    // Neither source1ChildNode nor source2ChildNode is

				// a terminal node.
				if (debug)
				{
					System.err.println("neither s1 nor s2 is a terminal");
				}

				// Check if this already is computed
				Node destChildNode = getFromComputeCache(source1ChildNode, source2ChildNode);

				if (destChildNode != null)
				{
					destNode.setChild(i, destChildNode);
				}
				else
				{
					destChildNode = new Node(source1ChildNode);

					computeOr(source1ChildNode, source2ChildNode, destChildNode);

					Node destChildNodeCopy = getNode(destChildNode);

					if (destChildNodeCopy != null)
					{
						destChildNode.dispose();
						destNode.setChild(i, destChildNodeCopy);
						addToComputeCache(source1ChildNode, source2ChildNode, destChildNodeCopy);
					}
					else
					{
						destNode.setChild(i, destChildNode);
						addNode(destChildNode);
						addToComputeCache(source1ChildNode, source2ChildNode, destChildNode);
					}
				}
			}
		}
	}

	private void computeAnd(Node source1Node, Node source2Node, Node destNode)
	{
		int level = source1Node.getLevel();
		int nbrOfChildren = nbrOfBranches[level];
		boolean debug = false;

		if (debug)
		{
			System.err.println("computeAnd s1: " + source1Node.getId() + " s2: " + source2Node.getId() + " d: " + destNode.getId());
		}

		for (int i = 0; i < nbrOfChildren; i++)
		{
			Node source1ChildNode = source1Node.getChild(i);
			Node source2ChildNode = source2Node.getChild(i);

			// System.err.println("Checking child: " + i);
			if (source1ChildNode.isTrue() && source2ChildNode.isTrue())
			{
				if (debug)
				{
					System.err.println("s1 && s2 true");
				}

				destNode.setChild(i, getTrueNode());
			}
			else if (source1ChildNode.isFalse() || source2ChildNode.isFalse())
			{
				if (debug)
				{
					System.err.println("s1 or s2 false");
				}

				destNode.setChild(i, getFalseNode());
			}
			else if (source1ChildNode.isTrue())
			{    // Note that the source1ChildNode is true but

				// source2ChildNode is not
				// The result is determined by source2ChildNode
				if (debug)
				{
					System.err.println("s1 true");
				}

				Node destChildNode = getFromCopyCache(source2ChildNode);

				if (destChildNode != null)
				{
					destNode.setChild(i, destChildNode);
				}
				else
				{
					destChildNode = new Node(source2ChildNode);

					copySubGraph(source2ChildNode, destChildNode);

					Node destChildNodeCopy = getNode(destChildNode);

					if (destChildNodeCopy != null)
					{
						destChildNode.dispose();
						destNode.setChild(i, destChildNodeCopy);
						addToCopyCache(source2ChildNode, destChildNodeCopy);
					}
					else
					{
						addNode(destChildNode);
						destNode.setChild(i, destChildNode);
						addToCopyCache(source2ChildNode, destChildNode);
					}
				}
			}
			else if (source2ChildNode.isTrue())
			{    // Note that the source2ChildNode is true but

				// source1Child node is not
				// The result is determined by source2ChildNode
				if (debug)
				{
					System.err.println("s2 true");
				}

				Node destChildNode = getFromCopyCache(source1ChildNode);

				if (destChildNode != null)
				{
					destNode.setChild(i, destChildNode);
				}
				else
				{
					destChildNode = new Node(source1ChildNode);

					copySubGraph(source1ChildNode, destChildNode);

					Node destChildNodeCopy = getNode(destChildNode);

					if (destChildNodeCopy != null)
					{
						destChildNode.dispose();
						destNode.setChild(i, destChildNodeCopy);
						addToCopyCache(source1ChildNode, destChildNodeCopy);
					}
					else
					{
						addNode(destChildNode);
						destNode.setChild(i, destChildNode);
						addToCopyCache(source1ChildNode, destChildNode);
					}
				}
			}
			else
			{    // Neither source1ChildNode nor source2ChildNode is

				// a terminal node.
				if (debug)
				{
					System.err.println("neither s1 nor s2 is a terminal");
				}

				// Check if this already is computed
				Node destChildNode = getFromComputeCache(source1ChildNode, source2ChildNode);

				if (destChildNode != null)
				{
					destNode.setChild(i, destChildNode);
				}
				else
				{
					destChildNode = new Node(source1ChildNode);

					computeAnd(source1ChildNode, source2ChildNode, destChildNode);

					Node destChildNodeCopy = getNode(destChildNode);

					if (destChildNodeCopy != null)
					{
						destChildNode.dispose();
						destNode.setChild(i, destChildNodeCopy);
						addToComputeCache(source1ChildNode, source2ChildNode, destChildNodeCopy);
					}
					else
					{
						destNode.setChild(i, destChildNode);
						addNode(destChildNode);
						addToComputeCache(source1ChildNode, source2ChildNode, destChildNode);
					}
				}
			}
		}
	}

	public boolean valueOf(int[] variableValues)
	{
		Node currNode = rootNode;
		int variableIndex = 0;

		while (true)
		{
			currNode = currNode.getChild(variableValues[variableIndex]);

			if (currNode.isTrue())
			{
				return true;
			}

			if (currNode.isFalse())
			{
				return false;
			}
		}
	}

	public void copySubGraph(Node sourceNode, Node destNode)
	{
		boolean debug = false;

		if (debug)
		{
			System.err.println("copySubGraph s: " + sourceNode.getId() + " d: " + destNode.getId());
		}

		int level = sourceNode.getLevel();
		int nbrOfChildren = nbrOfBranches[level];

		for (int i = 0; i < nbrOfChildren; i++)
		{
			Node sourceChildNode = sourceNode.getChild(i);

			if (sourceChildNode.isTerminal())
			{
				if (sourceChildNode.isTrue())
				{
					destNode.setChild(i, getTrueNode());
				}
				else
				{
					destNode.setChild(i, getFalseNode());
				}
			}
			else
			{

				// Check copy cache
				Node destChildNode = getFromCopyCache(sourceChildNode);

				if (destChildNode != null)
				{    // Node found in cache
					destNode.setChild(i, destChildNode);
				}
				else
				{    // Node not found in cache
					destChildNode = new Node(sourceChildNode);

					copySubGraph(sourceChildNode, destChildNode);

					Node destChildNodeCopy = getNode(destChildNode);

					if (destChildNodeCopy != null)
					{
						destChildNode.dispose();
						destNode.setChild(i, destChildNodeCopy);
						addToCopyCache(sourceChildNode, destChildNodeCopy);
					}
					else
					{
						destNode.setChild(i, destChildNode);
						addNode(destChildNode);
						addToCopyCache(sourceChildNode, destChildNode);
					}
				}
			}
		}
	}

	/**
	 * In the copy cache the identity of key is equal to key.getId()
*/
	public void addToCopyCache(Node key, Node value)
	{
		theCopyCache.put(new Integer(key.getId()), value);
	}

	/**
	 * Return null if key is not in the cache,
	 * or the cached if it exists.
	 */
	public Node getFromCopyCache(Node key)
	{
		return (Node) theCopyCache.get(new Integer(key.getId()));
	}

	public void addToComputeCache(Node key1, Node key2, Node value)
	{
		theComputeCache.put(new ComputeCacheNode(key1, key2), value);
	}

	/**
	 * Return null if <key1,key2> is not in the cache,
	 * or the cached if it exists.
	 */
	public Node getFromComputeCache(Node key1, Node key2)
	{
		return (Node) theComputeCache.get(new ComputeCacheNode(key1, key2));
	}

	public int nbrOfNodes()
	{
		return nbrOfNodes;
	}

	public int nbrOfLevels()
	{
		return nbrOfLevels;
	}

	public Node getTrueNode()
	{
		if (terminal1Node.isTrue())
		{
			return terminal1Node;
		}

		return terminal2Node;
	}

	public Node getFalseNode()
	{
		if (terminal1Node.isFalse())
		{
			return terminal1Node;
		}

		return terminal2Node;
	}

	public String toString()
	{
		return toString(rootNode);
	}

	public String toString(Node currNode)
	{
		StringBuffer sb = new StringBuffer();

		sb.append(currNode);

		Node[] children = currNode.getChildren();

		if (children != null)
		{
			for (int i = 0; i < children.length; i++)
			{
				sb.append(children[i].toString());
			}

			for (int i = 0; i < children.length; i++)
			{
				sb.append(toString((Node) children[i]));
			}
		}

		return sb.toString();
	}

	private String arrayToString(int[] theArray)
	{
		StringBuffer sb = new StringBuffer();

		sb.append("[");

		for (int i = 0; i < theArray.length; i++)
		{
			sb.append(theArray[i]);

			if (i < theArray.length - 1)
			{
				sb.append(" ");
			}
		}

		sb.append("]");

		return sb.toString();
	}

	public static void main(String[] args)
		throws Exception
	{
		int[] branches = new int[]{ 2, 2 };
		IDD idd1 = new IDD(branches, 0, 1);

		System.err.println("nbr sat 1: " + idd1.nbrOfSatisfyingAssignments());

		IDD idd2 = new IDD(branches, 1, 0);

		System.err.println("nbr sat 2: " + idd2.nbrOfSatisfyingAssignments());

		IDD idd3 = IDD.or(idd1, idd2);

		System.err.println("nbr sat 3: " + idd3.nbrOfSatisfyingAssignments());

		IDD idd4 = IDD.and(idd1, idd2);

		System.err.println("nbr sat 4: " + idd4.nbrOfSatisfyingAssignments());

		IDD idd5 = IDD.and(idd3, idd4);

		System.err.println("nbr sat 5: " + idd5.nbrOfSatisfyingAssignments());

		IDDToDot serializer = new IDDToDot(idd5);

		serializer.serialize(new PrintWriter(System.out));

		/*
		 *               System.err.println(trueIDD.toString());
		 *
		 *               IDD falseIDD = new IDD(branches);
		 *               falseIDD.not();
		 *
		 *               IDD resIDD = or(trueIDD, falseIDD);
		 */
	}
}

final class ComputeCacheNode
{
	private final Node key1;
	private final Node key2;

	public ComputeCacheNode(Node key1, Node key2)
	{
		this.key1 = key1;
		this.key2 = key2;
	}

	public boolean equals(Object other)
	{
		ComputeCacheNode otherNode = (ComputeCacheNode) other;

		return (otherNode.key1.getId() == key1.getId()) && (otherNode.key2.getId() == key1.getId());
	}

	public int hashCode()
	{
		return (key1.getId() * 0xFFFF0000) + key2.getId();
	}
}
