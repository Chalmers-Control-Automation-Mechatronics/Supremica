/************************* AutomatonToSMC.java ********************************
 * This is code to export to the State Machine Compiler format
 * http://smc.sourceforge.net/
 * 
 * This code just handles flat automata, but SMC allows transition actions,
 * so there will eventually be code to handle EFA, but that code will need
 * to parse wmods so it will be implemented on the waters side.
 */
package org.supremica.automata.IO;

import java.util.Iterator;

import java.io.FileWriter;
import java.io.PrintWriter;

import org.supremica.automata.Automaton;
import org.supremica.automata.Arc;
import org.supremica.automata.State;
import org.supremica.automata.LabeledEvent;
/**
 *
 * @author Fabian
 */
public class AutomatonToSMC
    implements AutomataSerializer
{

	private final Automaton the_automaton;
	
	public AutomatonToSMC(final Automaton aut)
	{
		this.the_automaton = aut;
	}
	
	@Override
	public void serialize(final PrintWriter pw) throws Exception
	{
		final String name = this.the_automaton.getName();	// Could need to sanitize this to remove bad chars
		serialize(pw, name);
	}

	@Override
	public void serialize(String fileName)
		throws Exception
	{
		final java.io.File file = new java.io.File(fileName);
		final String name = file.getName();
		serialize(new PrintWriter(new FileWriter(fileName)), name);
	}	
	
	private void serialize(final PrintWriter pw, final String name)
	{
		pw.println("%{\nSupremica generated SMC file of " + name);
		pw.println("For how to use this, see http://smc.sourceforge.net/SmcManual.htm");
		pw.println("\nAuthor: M. Fabian (2016)");
		pw.println("%}");
		
		pw.println("\n// The below elements need editing\n" +
		"// The %class keyword which specifies the application class to which this FSM is associated: Task.\n" +
		"// The %package keyword which specifies to which class package this FSM belongs.\n" +
		"// The %fsmclass keyword specifies the generated finite state machine class name. \n" +
		"// The %fsmfile keyword specifies the generated finite state machine class file name. \n" +
		"// The %access keyword is used to specify the generated class' accessibility level (only for Java and C# code).\n" +
		"// The %start keyword specifies the FSM's start state.\n" +
		"// The %map keyword is the FSM's name.");
		
		pw.println("%class YourClass");
		pw.println("%package org.your.package");
		pw.println("%fsmclass YourFSM");	// Should be the same as the filename (minus the .sm suffix)
		pw.println("%fsmfile YourFSM");		// Should (always?) be the same as %fsmclass
		pw.println("%access public");

		pw.println("\n// A %map name cannot be the same as the FSM class name.\n" +
		"%map TheFSM\n" +
		"%start TheFSM::" + this.the_automaton.getInitialState().getName() + "\n" +
		"%%\n");
		
		for(Iterator<State> states = this.the_automaton.stateIterator(); states.hasNext(); )
		{
			final State state = states.next();
			
			pw.println(state.getName());
			pw.println("{");
			
			for(Iterator<Arc> outgoingArcs = state.outgoingArcsIterator(); outgoingArcs.hasNext(); )
			{
				final Arc arc = (Arc) outgoingArcs.next();
				final State destState = arc.getToState();
				final LabeledEvent event = arc.getEvent();	
				
				pw.println("\t" + event.getLabel());
				pw.println("\t\t" + destState.getName());
				pw.println("\t\t{\n\t\t}");
			}
			
			pw.println("}");
		}
		
		pw.flush();
		pw.close();		
	}

}
