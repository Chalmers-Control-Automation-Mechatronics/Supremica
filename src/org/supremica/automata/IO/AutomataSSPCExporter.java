

package org.supremica.automata.IO;

import org.supremica.automata.*;

import java.io.*;
import java.util.*;


/**
 * <pre>
 * Exports from Supremica to SSPC format, as specified in its manual.
 * SSPC is a BDD-based DES tools designed by A. Sanchez et. al.
 *
 * Note that SSPC does not support the FSC-operator ||.
 * to build a full synchronous composition, we suspect that you need to add
 * a CAUSAL_BEHAVIOR file.
 *
 *
 * PS. this file was design for portability, not readability
 * /Arash
 * </pre>
 */


public class AutomataSSPCExporter {
	private File file;
	private String path;
	private HashMap fileMap, eventMap;
	private int event_count = 0;

	/** save the automata to disk, use 'file' for the system (project) file name */
	public AutomataSSPCExporter (Automata automata, String file)  throws IOException {
		this.file = new File(file);
		this.path = this.file.getParent();
		this.fileMap = new HashMap();
		this.eventMap = new HashMap();

		PrintWriter system = new PrintWriter( new FileOutputStream(file) );

		for(Iterator autIt = automata.iterator(); autIt.hasNext(); ) {
			Automaton currAutomaton = (Automaton) autIt.next();
			String name = getName(currAutomaton);
			system.println(name + ".fsm");
			saveOne(currAutomaton, name);
		}

		system.flush();
		system.close();
	}

	/** map event-label -> unique-integer */
	private int getEvent(String name) {
		Integer ret = (Integer ) eventMap.get(name);
		if(ret == null) {
			ret = new Integer(event_count++);
			eventMap.put(name, ret);
		}
		return ret.intValue();
	}

	/** map Automaton -> filename */
	private String getName(Automaton a) {
		String ret = (String) fileMap.get(a);
		if(ret == null) {
			String name = trim(a.getName() );
			for(int x = 1; ; x++) {
				String name2 = (x == 1) ? name : (name + x); // try 'X', 'X2', 'X3' etc...
				File file2 = new File(path, name2 + ".fsm");
				if(!file2.exists()) {
					fileMap.put(a,name2);
					return name2;
				}
			}
		}
		return ret;
	}

	/** remove bad chars and the extension (hopefully :) */
	private String trim(String x) {
		int len = x.lastIndexOf('.');
		if(len == -1) len = x.length();
		StringBuffer  sb = new StringBuffer(len);
		for(int i = 0; i < len; i++) {
			char c = x.charAt(i);
			sb.append( isGood(c) ? c : '_');
		}
		return sb.toString();
	}

	/** what chars we like */
	private boolean isGood(char c) {
		return ( (c >= '0' && c <= '9') || (c >= 'a' && c <= 'z')  || (c >= 'A' && c <= 'Z')  || c == '_');
	}

	/** save one automaton to file  */
	private void saveOne(Automaton a, String name) throws IOException {
		File file2 = new File(path, name+ ".fsm");
		PrintWriter me = new PrintWriter( new FileOutputStream(file2) );

		me.println("FSM " + name);
		me.println();


		for (Iterator states = a.stateIterator(); states.hasNext(); ){
			State state = (State) states.next();

			me.print("\tSTATE " + state.getName());
			if(state.isInitial()) me.print(" INITIAL");
			if(state.isAccepting()) me.print(" MARKED");

			me.println();

			for(ArcIterator aci = state.outgoingArcsIterator(); aci.hasNext();) {
				Arc arc = (Arc) aci.next();
				// String tname = arc.getLabel();
				int tname = getEvent( arc.getLabel() );
				me.println("\t\tTRANSITION " + tname + " TO " + arc.getToState().getName() );
			}
			me.println();
		}

		int count = 0;
		for(EventIterator ei = a.eventIterator(); ei.hasNext(); ) {
			LabeledEvent le = (LabeledEvent) ei.next();
			if(!le.isControllable()) {
				me.print( (count == 0) ? "\t" : ", ");
				me.print("" + getEvent( le.getLabel()) );
				count++;
			}
		}

		if(count != 0) me.println();
		me.println();
		me.println("END");

		me.flush();
		me.close();

	}
}