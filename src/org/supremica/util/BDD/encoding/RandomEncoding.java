

package org.supremica.util.BDD.encoding;

import org.supremica.util.BDD.*;

public class RandomEncoding implements Encoding {

	public void encode(Automaton a) {
		State [] states = a.getStates().getStateVector();
		int size = states.length;
		int [] p = Util.permutate(size);
		for(int i = 0; i < size; i++) states[i].setCode(p[i]);

	}
}
