package org.supremica.util.BDD.encoding;

import org.supremica.util.BDD.*;

/**
 * sets the BDD encoding of the states in an automataon.
 * the encoding is a boolean vector represented by a number: "int State.code"
 *
 */
public interface Encoding
{
	public void encode(Automaton a);
}
