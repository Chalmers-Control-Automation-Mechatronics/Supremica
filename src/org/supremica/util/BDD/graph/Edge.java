
package org.supremica.util.BDD.graph;

/**
 * Edge in graph: contains (u,v), the graph may or may not be directed
 *
 * (stolen from JDD)
 */

public class Edge
/* implements Sortable */
{

	public Node n1, n2;
	public int id, flags, extra1, extra2;
	public double weight, extra3;
	public Edge next, prev;
	public String label;

	public Edge(Node n1, Node n2, int id) {
		this(n1,n2,id,null);
	}
	public Edge(Node n1, Node n2, int id, String label) {
		this.n1 = n1;
		this.n2 = n2;
		this.id = id;
		this.flags = 0;
		this.weight = 1; // unit weight for all
		this.next = null;
		this.prev = null;
		this.label = label;

	}

	/*
  public boolean greater_than(Sortable s) {
		return this.weight > ((Edge)s).weight;
	}
	*/

}
