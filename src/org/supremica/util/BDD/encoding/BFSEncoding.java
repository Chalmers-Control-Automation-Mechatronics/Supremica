

package org.supremica.util.BDD.encoding;

import org.supremica.util.BDD.*;
import java.util.*;


/**
 * Do a BFS encoding from the first state [initial/marked].
 * States that are not reachable will be encoded in default order.
 */

public class BFSEncoding implements Encoding {

	private boolean from_i;
	public BFSEncoding(boolean from_i) {
		this.from_i = from_i;
	}

	public void encode(Automaton a) {
		State [] states = a.getStates().getStateVector();
		int size = states.length;
		int count = 0;

		State [] stack = new State[size];
		State [] stack2 = new State[size];

		int [] codes = new int[size];
		int tos = 0, tos2 = 0;

		for(int i = 0; i < size; i++){
			states[i].extra1 = i; // our "f: State -> int"  map
			codes[i] = -1; // this state is not reached yet

			// fill the initial round
			if((from_i && states[i].isInitial()) || (!from_i && states[i].isMarked()) ){
				stack[tos++] = states[i];
			}
		}

		while(tos > 0) {

			tos2 = 0;
			for(int i = 0; i < tos; i++) {
				State state = stack[i];
				codes[state.extra1] = count++;

				for(Enumeration e = from_i ? state.out() : state.in(); e.hasMoreElements(); ) {
					Arc arc = (Arc)e.nextElement();
					State s = from_i ? arc.toState() : arc.fromState();
					int index = s.extra1;
					if(codes[index] == -1){
						codes[index] = -2; // can be BFS-reachable with same depth from different states. avoid diuble insertation
						stack2[tos2++] = s;
					}
				}
			}

			State [] tmp = stack; stack = stack2; stack2 = tmp;
			tos = tos2;
		}


		// write the results to the structure:
		for(int i = 0; i < size; i++) {
			if(codes[i] == -1) codes[i] = count++;	// ok, just give the rest some default numbers:
			states[i].setCode(codes[i]);
		}
	}
}
