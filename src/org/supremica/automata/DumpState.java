/************************* DumpState.java *******************/
/* Defines the specific dump state, if an automaton has one,
 * then it has only one. Dump states are created by a  call to
 * Automaton.getDumpState(true);
 *
 * Owner: MF
 */
package org.supremica.automata;

public class DumpState
		extends State
{
	public static final String DUMPSTATENAME = "dump:"; // this name should maybe be definable through teh settings, maybe...

	public DumpState()
	{
		super(DUMPSTATENAME);
	}

}