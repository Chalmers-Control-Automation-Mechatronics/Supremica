

package org.supremica.util.BDD.graph;

import org.supremica.util.BDD.*;

import java.util.*;

/**
 * build a communication graph from automata
 * <p>
 *
 * each nodes corressponds to an automataon with the fields:
 *  owner => automaton object
 *  extra1 => the orifinal order
 *  weight => number of bits in the state vector
 *
 * each edge has the following fields:
 * n1, n2 => the two edge nodes representing two dependent automata
 * weight => communication complexity
 */

public class FromAutomata {

	public static Graph build(Automata a) {

		Vector<Automaton> automata = a.getAutomata();
		int size = automata.size();
		Graph g = new Graph(false);
		HashMap<Automaton,Node> automata2node = new HashMap<Automaton,Node>();

		// add the nodes:
		int i = 0;
		for (Enumeration<Automaton> e = automata.elements(); e.hasMoreElements(); i++)
		{
			Automaton a1 = e.nextElement();
			Node n = new Node(i);
			n.owner = a1;
			n.label = a1.getName();
			n.extra1 = i;
			n.weight = a1.nrOfBitsNeededForStateEncoding();
			automata2node.put(a1, n);
			g.addNode(n);
		}


		// add the edges
		try {
			int[][] weightMatrix = a.getCommunicationMatrix();
			for (Enumeration<Node> e = g.getNodes().elements(); e.hasMoreElements(); )
			{
				Node n1 = e.nextElement();
				int j = n1.extra1;
	
				for( i = 0; i < size; i++) {
					if(weightMatrix[j][i] > 0 && i != j) {
						Automaton a2 = (Automaton) automata.elementAt(i);
						Node n2 = automata2node.get(a2);
						Edge ed = g.addEdge(n1, n2);
						ed.weight = weightMatrix[j][i];
					}
				}
			}
		} catch (BDDException e) {
			throw new RuntimeException(e);
		}

		return g;
	}
}

