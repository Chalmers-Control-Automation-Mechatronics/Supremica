
package org.supremica.util.BDD.solvers;

import org.supremica.util.BDD.*;

import java.util.*;


/**
 * depth first ordering, AKA topological sort
 *
 * it traverses the nodes w.r.t weights - the implementatio is not as elegant as dijkstra's algo :(
 *
 * XXX: we havent tested if the starting point given by get_least_connected is really good !
 *
 */
public class DFSSolver extends Solver {

	private int count;

	public DFSSolver(Node [] org_ ) { super(org_); }

	public void solve() {
		for(int i = 0; i < size; i++) org[i].extra1 = org[i].extra2 = 0;

		count = 0;

		int [] stack = new int[size];
		int tos = 0;
		int first = get_least_connected() ;
		stack[tos++] = first;

		while(tos > 0) {
			int curr = stack[--tos];
			org[curr].extra1 = 1;
			org[curr].extra2 = count++;

			int start = tos;
			for(int i = 0; i < size; i++)
				if(i != curr && (org[i].wlocal[curr] > 0) && org[i].extra1 == 0) {
					stack[tos++] = i;
					org[i].extra1 = -1; // to make sure we dont select it again!
				}


			// sort element 'start' to 'tos'-1
			sort(stack, start, tos, curr);
		}

		// fix those with no ordering:
		for(int i = 0; i < size; i++) if(org[i].extra1 == 0)  org[i].extra2 = count++;


		// now, sort according to our new DFS order
		for(int i = 0; i < size; i++) solved[ org[i].extra2 ] = org[i];

	}

}
