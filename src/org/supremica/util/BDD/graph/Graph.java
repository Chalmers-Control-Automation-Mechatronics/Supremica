

package org.supremica.util.BDD.graph;

import java.util.*;

/**
 * <pre>
 * simple implementation of a graph: G = < V, E>
 *
 * to save memory, the adjacency list is inside each node.
 * Note that this gives a "directed" feel to the graph even if its is undirected.
 * the Vector "edges" contains all edges in case the the adjacency list is not enough
 *
 *
 * Note 1: if you remove a node in custom code, you must update "edges" and the adjacency lists!!
 * Note 2: duplicate nodes/edges are not allowed, and automatically removed by Graph
 * Note 3: self-loops are currently allowed, I dont know if they should be removed
 *
 * </pre>
 *
 * (stolen from JDD)
 */

public class Graph {
	protected Vector nodes, edges; /** V and E */
	protected int count_nodes, count_edges; /** used to create unique ID:s for each element */
	/* package */ boolean directed; /** G is a digraph */

	public Graph(boolean directed) {
		this.directed = directed;
		this.nodes = new Vector();
		this.edges = new Vector();

		// this is not a counter, its more of a label and can therefore start at 1
		this.count_nodes = 1;
		this.count_edges = 1;
	}

	// ----------------------------------------------------

	public Vector getNodes() { return nodes; }
	public Vector getEdges() { return edges; }

	public int numOfNodes() { return nodes.size(); }	// order of the graph
	public int numOfEdges() { return edges.size(); }

	public boolean isDirected() { return directed; }

	// ----------------------------------------------------------------

	public Node addNode(Node n) {
		Node n2 = findNode(n);
		if(n2 == null) {
			n.id = count_nodes++;
			nodes.add(n);
		}
		return n;
	}

	public Node addNode() {
		Node n = new Node(count_nodes++);
		nodes.add(n);
		return n;
	}


	// ----------------------------------------------------

	public Edge addEdge(Node n1, Node n2) {
		Edge e = findEdge(n1,n2);
		if(e == null) {
			e = new Edge(n1,n2, count_edges++);
			edges.add(e);
			e.next = n1.firstOut;	n1.firstOut = e;
			e.prev = n2.firstIn;	n2.firstIn  = e;
		}
		return e;
	}


	// ----------------------------------------------------
	public void removeEdge(Edge e) {
		edges.remove(e);
		removeForwardList(e, e.n1);
		removeBackwardList(e, e.n2);
	}

	/** remove edge "ed" from the forward chain of "n" */
	protected void removeForwardList(Edge ed, Node n) {
		while(n.firstOut != null && (n.firstOut == ed))	n.firstOut = n.firstOut.next;
		Edge e = n.firstOut, last = null;
		while(e != null) {
			if(e == ed) {
				last.next = e.next;
			} else last = e;
			e = e.next;
		}
	}

	/** remove edge "ed" from the backward chain of "n" */
	protected void removeBackwardList(Edge ed, Node n) {
		while(n.firstIn != null && (n.firstIn == ed))	n.firstIn = n.firstIn.prev;
		Edge e = n.firstIn, last = null;
		while(e != null) {
			if(e == ed) {
				last.prev = e.prev;
			} else last = e;
			e = e.prev;
		}
	}
	public void removeNode(Node n) {
		nodes.remove(n);

		Edge e = n.firstOut;
		while(e != null) { edges.remove(e); removeBackwardList(e, e.n2); e = e.next; }

		e = n.firstIn;
		while(e != null) {edges.remove(e); removeForwardList(e, e.n1); e = e.prev; }

		n.firstIn = n.firstOut = null;
	}

	// ----------------------------------------------------

	public void removeAllEdges()  {
		edges.removeAllElements();
		for (Enumeration e = nodes.elements() ; e.hasMoreElements() ;) {
			Node n = (Node) e.nextElement();
			n.firstIn = n.firstOut = null;
		}
	}

	public void removeAllNodes()  {
		edges.removeAllElements(); // <-- no nodes => no edges...
		nodes.removeAllElements();
	}

	// ----------------------------------------------------
	protected Edge findEdge(Node n1, Node n2) {
		for (Enumeration e = edges.elements() ; e.hasMoreElements() ;) {
			Edge edge = (Edge) e.nextElement();
			if(edge.n1 == n1 && edge.n2 == n2) return edge;
			if(! directed) {
				if(edge.n1 == n2 && edge.n2 == n1) return edge;
			}
		}
		return null;
	}
	protected Node findNode(Node n) {
		for (Enumeration e = nodes.elements() ; e.hasMoreElements() ;) {
			Node n2 = (Node) e.nextElement();
			if(n == n2) return n;
		}
		return null;
	}

	public Node findNode(String label) {
		for (Enumeration e = nodes.elements() ; e.hasMoreElements() ;) {
			Node n = (Node) e.nextElement();
			if(label.equals( n.label)) return n;
		}
		return null;
	}

	// DEBUG
	public void dump() {
		for (Enumeration it = nodes.elements() ; it.hasMoreElements() ;) {
			Node n = (Node) it.nextElement();
			System.out.println("\nNode " + n.label+ ", extras= " + n.extra1 +" " + n.extra2 + " " +
				n.extra3 + " " + n.extra4 + ", weight= " + n.weight);

			Edge e = n.firstOut;
			while(e != null) { System.out.println("\t" + e.n2.label);			e = e.next; }

			e = n.firstIn;
			while(e != null) {	System.out.println("\t" + e.n2.label); e = e.prev; }
		}
	}
}

