/*
 * Supremica Software License Agreement
 *
 * The Supremica software is not in the public domain
 * However, it is freely available without fee for education,
 * research, and non-profit purposes.  By obtaining copies of
 * this and other files that comprise the Supremica software,
 * you, the Licensee, agree to abide by the following
 * conditions and understandings with respect to the
 * copyrighted software:
 *
 * The software is copyrighted in the name of Supremica,
 * and ownership of the software remains with Supremica.
 *
 * Permission to use, copy, and modify this software and its
 * documentation for education, research, and non-profit
 * purposes is hereby granted to Licensee, provided that the
 * copyright notice, the original author's names and unit
 * identification, and this permission notice appear on all
 * such copies, and that no charge be made for such copies.
 * Any entity desiring permission to incorporate this software
 * into commercial products or to use it for commercial
 * purposes should contact:
 *
 * Knut Akesson (KA), knut@supremica.org
 * Supremica,
 * Haradsgatan 26A
 * 431 42 Molndal
 * SWEDEN
 *
 * to discuss license terms. No cost evaluation licenses are
 * available.
 *
 * Licensee may not use the name, logo, or any other symbol
 * of Supremica nor the names of any of its employees nor
 * any adaptation thereof in advertising or publicity
 * pertaining to the software without specific prior written
 * approval of the Supremica.
 *
 * SUPREMICA AND KA MAKES NO REPRESENTATIONS ABOUT THE
 * SUITABILITY OF THE SOFTWARE FOR ANY PURPOSE.
 * IT IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED WARRANTY.
 *
 * Supremica or KA shall not be liable for any damages
 * suffered by Licensee from the use of this software.
 *
 * Supremica is owned and represented by KA.
 */
package org.supremica.automata.IO;

import java.io.*;
import java.util.*;
import java.text.DateFormat;

import org.supremica.log.*;
import org.supremica.automata.*;
import org.supremica.util.SupremicaException;
import org.supremica.automata.algorithms.SynchronizationType;

//TODO: Make state variables int

/** This class generates an IEC-61499 function block 
 * implementing the automata in the current project.
 *  
 * <p>Note that the event in the IEC-61499 is not the 
 * same as an automaton event. That is why the
 * implementation of the interface is done this way. KA 
 * and I are planing several different interfaces so this 
 * is only the first one.
 * 
 * @author Goran Cengic
 * @author cengic@s2.chalmers.se
 */ 
public class AutomataToIEC61499

{
	
    private static Logger logger = LoggerFactory.createLogger(AutomataToIEC61499.class);
    private Project theProject;
    private IEC61131Helper theHelper;
    private Alphabet allEvents;
    private SynchronizationType syncType = SynchronizationType.Prioritized;
    private boolean comments = false;	
	
    public void commentsOn()
    {
	comments = true;
    }
	
    public void commentsOff()
    {
	comments = false;
    }
	
    // Constructor
    public AutomataToIEC61499(Project theProject)
    {
	this.theProject = theProject;
	this.theHelper = IEC61131Helper.getInstance();
	allEvents = this.theProject.setIndicies();
    }


    /** Makes the beginning of the function block type declaration. */
    private void printBeginProgram(PrintWriter pw)
    {
	if (comments)
	{
	    pw.println("(* This function block was automatically generated from Supremica *)");
	    pw.println("(* Supremica version: " + org.supremica.Version.version() + "*)");
	    pw.println("(* Time of generation: " + DateFormat.getDateTimeInstance().format(new Date()) + " *)");
	}
	
	if (theProject.getNbrOfAutomata() >= 2)
	{
	    pw.println("FUNCTION_BLOCK AUTOGEN_FB");
	}
	else
	{
	    pw.println("FUNCTION_BLOCK AUTOGEN_" + theProject.getAutomatonAt(0).getName());	    
	}
    }




    /** Makes the fb_interface_list production rule of the standard. */
    private void printInterfaceList(PrintWriter pw) 
    {


	// The event_input_list. This is the same for all FBs of automata.
	// For now the input events are INIT, RESET and OCURED.
	// INIT event does the initialization
	// RESET event makes all of the automata go to their initial state.
	// OCCURRED event signals a new automaton event to the automata and is thus coupled 
	// to the input variables that represent the automaton events.
	pw.println("EVENT_INPUT");
	pw.println("\tINIT : INIT_EVENT;");
	pw.println("\tRESET;");
	pw.println("\tOCCURRED WITH "); 
	for(Iterator alphIt=allEvents.iterator(); alphIt.hasNext();)
	{								
	    LabeledEvent currEvent = (LabeledEvent) alphIt.next();
	    if (alphIt.hasNext())
	    {
		pw.println("\t\tEI_" + currEvent.getLabel()+ ",");
	    } 
	    else 
	    {
		pw.println("\t\tEI_" + currEvent.getLabel()+ ";");
	    }
	}
	pw.println("END_EVENT");
		


	// The event_output_list. This is the same for all FBs of automata also.
	// For now the only output event is DONE and it is coupled with the output variables that
	// represent the state of the automata after the transition upon receving a automaton event.
	pw.println("EVENT_OUTPUT");
	pw.println("\tINITO : INIT_EVENT");
	pw.println("\tDONE WITH ");
	for (Iterator alphIt = allEvents.iterator(); alphIt.hasNext();) 
	{
	    LabeledEvent currEvent = (LabeledEvent) alphIt.next();
	    if (alphIt.hasNext()) 
	    {
		pw.println("\t\tEO_" + currEvent.getLabel() + ",");
	    } 
	    else 
	    {
		pw.println("\t\tEO_" + currEvent.getLabel() + ";");
	    }
	}
	pw.println("END_EVENT");
		
		
		
	// The input_variable_list. Input variables represent the automaton events and are of the
	// bool type. Only one should be TRUE when the OCCURRED event happens but if several are TRUE
	// all of the transitions will take place. The user have to make shure that this is possible!
	pw.println("VAR_INPUT");		
	for (Iterator alphIt = allEvents.iterator(); alphIt.hasNext();)
	{
	    LabeledEvent currEvent = (LabeledEvent) alphIt.next();
	    pw.print("\tEI_" + currEvent.getLabel() + " : BOOL;"); 
	    if (comments)
	    {
		pw.print("\t(* " + currEvent.getLabel() + " *)");
	    }
	    pw.print("\n");
	}
	pw.println("END_VAR");

		
	// The output_variable_list. Output variables represent the automaton events that 
	// are enabled after the transition. More than one of tese can be true when the DONE event occurres.
	pw.println("VAR_OUTPUT");
	for (Iterator alphIt = allEvents.iterator(); alphIt.hasNext();)
	{
	    LabeledEvent currEvent = (LabeledEvent) alphIt.next();
	    pw.print("\tEO_" + currEvent.getLabel() + " : BOOL;");
	    if (comments)
	    {
		pw.print("\t(* " + currEvent.getLabel() + " *)");
	    }
	    pw.print("\n");
	}
	pw.println("END_VAR");		
	
    }




    /** Makes the fb_internal_variable_list production rule of the standard. */
    private void printInternalVariableList(PrintWriter pw) 
    {
	pw.println("VAR");
		
	// State variables
	for (Iterator autIt = theProject.iterator(); autIt.hasNext(); )
	{
	    Automaton currAutomaton = (Automaton) autIt.next();
	    int currAutomatonIndex = currAutomaton.getSynchIndex();

	    for (Iterator stateIt = currAutomaton.stateIterator(); stateIt.hasNext(); )
	    {
		State currState = (State) stateIt.next();
		int currStateIndex = currState.getSynchIndex();

		pw.print("\tQ_" + currAutomatonIndex + "_" + currStateIndex + " : BOOL;");
		if (comments)
		{
		    pw.print(" (* " + currState.getName() + " in " + currAutomaton.getName() + " *)");
		}
		pw.print("\n");
	    }
	}
				
	pw.println("END_VAR");
    }




    /** Makes the fb_ecc_declaration production rule of the standard. */
    private void printEccDeclaration(PrintWriter pw) 
    {
		
	// Execution Control Chart (ECC) is the same for all function blocks of this type		
		
	// States of the ECC
	pw.println("EC_STATES");
	pw.println("\tSTART;");
	pw.println("\tINIT : INIT, RESET, COMP_ENABLED -> INITO;");
	pw.println("\tRESET : RESET;");
	pw.println("\tTRANSITION : TRANSITION;");
	pw.println("\tCOMP_ENABLED : COMP_ENABLED -> DONE;");
	pw.println("END_STATES");
	
	// Transitions of the ECC
	pw.println("EC_TRANSITIONS");
	pw.println("\tSTART TO INIT := INIT;");
	pw.println("\tINIT TO START := 1;");
	pw.println("\tSTART TO RESET := RESET;");
	pw.println("\tSTART TO TRANSITION := OCCURRED;");
	pw.println("\tRESET TO COMP_ENABLED := 1;");
	pw.println("\tCOMP_ENABLED TO START := 1;");
	pw.println("\tTRANSITION TO COMP_ENABLED := 1;");
	pw.println("END_TRANSITIONS");
	
    }




    /** Makes the fb_algorithm_declaration production rule of the standard. */
    private void printAlgorithmDeclarations(PrintWriter pw) 
	throws Exception
    {

		
	// INIT algorithm is empty for now. Reserved for future development.
	// If needed for representation specific initialization for example.
	pw.println("ALGORITHM INIT IN ST :");
	pw.println("END_ALGORITHM");



	// RESET algorithm resets the automata in the function block. In other words it
	// makes automata enter the initial state.
	pw.println("ALGORITHM RESET IN ST :");

	if (comments)
	{
	    pw.println("\t(* Reset all automata to initial state *)");
	    pw.println();
	    pw.println("\t(* First set all states to FALSE *)");
	}
		
	// Set all state variables to FALSE
	for (Iterator autIt = theProject.iterator(); autIt.hasNext(); )
	{
	    Automaton currAutomaton = (Automaton) autIt.next();
	    int currAutomatonIndex = currAutomaton.getSynchIndex();

	    for (Iterator stateIt = currAutomaton.stateIterator(); stateIt.hasNext(); )
	    {
		State currState = (State) stateIt.next();
		int currStateIndex = currState.getSynchIndex();

		pw.println("\tQ_" + currAutomatonIndex + "_" + currStateIndex + " := FALSE;");
	    }
	}


	if (comments)
	{
	    pw.println();
	    pw.println("\t(* Then set the initial states to TRUE *)");
	}

	// Then set the initital states to TRUE
	for (Iterator autIt = theProject.iterator(); autIt.hasNext();)
	{
	    Automaton currAutomaton = (Automaton) autIt.next();

	    if (currAutomaton.getInitialState() == null)
	    {
		String errMessage = "AutomataToIEC61499.printAlgorithmDeclarations: " + "all automata must have an initial state but automaton " + currAutomaton.getName() + "doesn't";

		logger.error(errMessage);

		throw new IllegalStateException(errMessage);
	    }

	    pw.println("\tQ_" + currAutomaton.getSynchIndex() + "_" + currAutomaton.getInitialState().getSynchIndex() + " := TRUE;");
	}
		
	pw.println("END_ALGORITHM");


		
	// TRANSITION algorithm makes the transition corresponding to the automaton events that came with the 
	// OCCURED.
	pw.println("ALGORITHM TRANSITION IN ST :");
						
	if (comments)
	{
	    pw.println("\t(* Change state in the automata *)");
	}

	// Iterate over all events and make transitions for the enabled events
	for (Iterator alphIt = allEvents.iterator(); alphIt.hasNext();)
	{
	    LabeledEvent currEvent = (LabeledEvent) alphIt.next();
	    int currEventIndex = currEvent.getSynchIndex();

	    if (comments)
	    {
		pw.println();
		pw.println("\t(* Transitions for event \"" + currEvent.getLabel() + "\" *)");
	    }

	    boolean previousCondition = false;

	    pw.println("\tIF (EI_" + currEvent.getLabel() + " AND EO_" + currEvent.getLabel() + ")");
	    pw.println("\tTHEN");

	    for (Iterator autIt = theProject.iterator(); autIt.hasNext();)
	    {
		Automaton currAutomaton = (Automaton) autIt.next();
		Alphabet theAlphabet = currAutomaton.getAlphabet();
		int currAutomatonIndex = currAutomaton.getSynchIndex();

		if (theAlphabet.contains(currEvent.getLabel()))
		{
		    LabeledEvent currAutomatonEvent = currAutomaton.getEvent(currEvent.getLabel());

		    if (currAutomatonEvent == null)
		    {
			throw new SupremicaException("AutomataToIEC61499.printAlgorithmDeclarations: " + "Could not find " + currEvent.getLabel() + " in automaton " + currAutomaton.getName());
		    }

		    if (comments)
		    {
			pw.println("\t\t(* Transitions in " + currAutomaton.getName() + " *)");
		    }

		    boolean previousState = false;

		    for (Iterator stateIt = currAutomaton.statesThatEnableEventIterator(currEvent.getLabel()); stateIt.hasNext();)
		    {
			State currState = (State) stateIt.next();
			int currStateIndex = currState.getSynchIndex();
			State toState = currState.nextState(currAutomatonEvent);

			if (toState == null)
			{
			    throw new SupremicaException("AutomataToIEC61499.printAlgorithmDeclarations: " + "Could not find the next state from state " + currState.getName() + " with label " + currEvent.getLabel() + " in automaton " + currAutomaton.getName());
			}

			int toStateIndex = toState.getSynchIndex();

			if (currState != toState)
			{
			    if (!previousState)
			    {
				pw.print("\t\tIF");

				previousState = true;
			    }
			    else
			    {
				pw.print("\t\tELSIF");
			    }

			    pw.println(" (Q_" + currAutomatonIndex + "_" + currStateIndex + ")");
			    pw.println("\t\tTHEN");
			    pw.println("\t\t\tQ_" + currAutomatonIndex + "_" + toStateIndex + " := TRUE;");
			    pw.println("\t\t\tQ_" + currAutomatonIndex + "_" + currStateIndex + " := FALSE;");
			}
			else
			{
			    if (comments)
			    {
				pw.println("\t\t(* Q_" + currAutomatonIndex + "_" + currStateIndex + "  has EI_" + currEventIndex + " as self loop, no transition *)");
			    }
			}
		    }

		    if (previousState)
		    {
			pw.println("\t\tEND_IF;");
		    }
		}
	    }

	    pw.println("\tEND_IF;");
	}

		
	pw.println("END_ALGORITHM");


	// COMP_ENABLED algorithm computes the enabled automata events in the states of automat after
	// the transition.
	pw.println("ALGORITHM COMP_ENABLED IN ST :");

	// Iterate over all events and compute which events that are enabled
	for (Iterator alphIt = allEvents.iterator(); alphIt.hasNext(); )
	{
	    while (alphIt.hasNext())
	    {
		LabeledEvent currEvent = (LabeledEvent) alphIt.next();
		int currEventIndex = currEvent.getSynchIndex();

		if (comments)
		{
		    pw.println();
		    pw.println("\t(* Enable condition for event \"" + currEvent.getLabel() + "\" *)");
		}

		boolean previousCondition = false;

		pw.print("\tEO_" + currEvent.getLabel() + " := ");

		for (Iterator autIt = theProject.iterator(); autIt.hasNext(); )
		{
		    Automaton currAutomaton = (Automaton) autIt.next();
		    Alphabet currAlphabet = currAutomaton.getAlphabet();
		    int currAutomatonIndex = currAutomaton.getSynchIndex();

		    if (syncType == SynchronizationType.Prioritized)
		    {    // All automata that has this event as prioritized must be able to execute it
			if (currAlphabet.containsEqualEvent(currEvent) && currAlphabet.isPrioritized(currEvent))
			{    // Find all states that enables this event

			    // Use OR between states in the same automaton.
			    // Use AND between states in different automata.
			    if (previousCondition)
			    {
				pw.print(" AND ");
			    }
			    else
			    {
				previousCondition = true;
			    }

			    boolean previousState = false;

			    pw.print("(");

			    for (Iterator stateIt = currAutomaton.statesThatEnableEventIterator(currEvent.getLabel());
				 stateIt.hasNext(); )
			    {
				State currState = (State) stateIt.next();
				int currStateIndex = currState.getSynchIndex();

				if (previousState)
				{
				    pw.print(" OR ");
				}
				else
				{
				    previousState = true;
				}

				pw.print("Q_" + currAutomatonIndex + "_" + currStateIndex);
			    }

			    if (!previousState)
			    {
				pw.print(" FALSE ");
			    }

			    pw.print(")");
			}
		    }
		    else
		    {
			String errMessage = "Unsupported SynchronizationType";

			logger.error(errMessage);

			throw new IllegalStateException(errMessage);
		    }
		}

		pw.println(";");
	    }
	}

	pw.println("END_ALGORITHM");

    }




    /** Makes the end of the function block type declaration */
    private void printEndProgram(PrintWriter pw)
    {

	pw.println("END_FUNCTION_BLOCK");

    }




    /** Makes the basic function block type declaration. */
    public void printSource(PrintWriter pw)
    {

	printBeginProgram(pw);
	printInterfaceList(pw);
	printInternalVariableList(pw);
	printEccDeclaration(pw);
	try
	{
	    printAlgorithmDeclarations(pw);
	}
	catch (Exception ex)
	{
	    logger.error(ex);
	}
	printEndProgram(pw);
	
	//printSignalVariables(pw);   -- not used yet, we'll have to see how to handle this.

    }

    public void printSources(PrintWriter pw)
    {	
        // First generate FB for all models	
        for (Iterator autIt = theProject.iterator(); autIt.hasNext();)
        {
            Project tempProject = new Project();
            tempProject.addAutomaton((Automaton) autIt.next());
            AutomataToIEC61499 tempToIEC61499 = new AutomataToIEC61499(tempProject);
            tempToIEC61499.printSource(pw);
            pw.println();
        }


        // Then generate the sync FB
	// BeginProgram
	if (comments)
	{
	    pw.println("(* This function block was automatically generated from Supremica *)");
	    pw.println("(* Supremica version: " + org.supremica.Version.version() + "*)");
	    pw.println("(* Time of generation: " + DateFormat.getDateTimeInstance().format(new Date()) + " *)");
	}
	
	pw.println("FUNCTION_BLOCK AUTOGEN_SYNC");	    

	// InterfaceList
	// The event_input_list.
	// Only input is ENABLED.
	pw.println("EVENT_INPUT");
	pw.println("\tENABLED WITH "); 
	for(Iterator autIt=theProject.iterator();autIt.hasNext();)
	{
	    Automaton tmpAut = (Automaton) autIt.next();
	    for(Iterator alphIt=tmpAut.getAlphabet().iterator(); alphIt.hasNext();)
	    {								
		LabeledEvent currEvent = (LabeledEvent) alphIt.next();
		if (alphIt.hasNext())
		{
		    pw.println("\t\tEI_" + tmpAut.getName() + "_" + currEvent.getLabel()+ ",");
		} 
		else 
		{
		    pw.println("\t\tEI_" + tmpAut.getName() + "_" + currEvent.getLabel()+ ";");
		}
	    }
	}
	pw.println("END_EVENT");
	

	// The event_output_list. 
	// The only output event is DONE.
	pw.println("EVENT_OUTPUT");
	pw.println("\tDONE WITH ");
	for(Iterator autIt=theProject.iterator();autIt.hasNext();)
	{
	    Automaton tmpAut = (Automaton) autIt.next();
	    for(Iterator alphIt=tmpAut.getAlphabet().iterator(); alphIt.hasNext();)
	    {								
		LabeledEvent currEvent = (LabeledEvent) alphIt.next();
		if (alphIt.hasNext())
		{
		    pw.println("\t\tEO_" + tmpAut.getName() + "_" + currEvent.getLabel()+ ",");
		} 
		else 
		{
		    pw.println("\t\tEO_" + tmpAut.getName() + "_" + currEvent.getLabel()+ ";");
		}
	    }
	}
	pw.println("END_EVENT");

	
	// The input_variable_list.
	pw.println("VAR_INPUT");		
	for(Iterator autIt=theProject.iterator();autIt.hasNext();)
	{
	    Automaton tmpAut = (Automaton) autIt.next();
	    for (Iterator alphIt = tmpAut.getAlphabet().iterator(); alphIt.hasNext();)
	    {
		LabeledEvent currEvent = (LabeledEvent) alphIt.next();
		pw.print("\tEI_" + tmpAut.getName() + "_" + currEvent.getLabel() + " : BOOL;"); 
		if (comments)
		{
		    pw.print("\t(* " + tmpAut.getName() + "_" + currEvent.getLabel() + " *)");
		}
		pw.print("\n");
	    }
	}
	pw.println("END_VAR");
	
		
	// The output_variable_list. 
	pw.println("VAR_OUTPUT");
	for(Iterator autIt=theProject.iterator();autIt.hasNext();)
	{
	    Automaton tmpAut = (Automaton) autIt.next();
	    for (Iterator alphIt = tmpAut.getAlphabet().iterator(); alphIt.hasNext();)
	    {
		LabeledEvent currEvent = (LabeledEvent) alphIt.next();
		pw.print("\tEO_" + tmpAut.getName() + "_" + currEvent.getLabel() + " : BOOL;");
		if (comments)
		{
		    pw.print("\t(* " + tmpAut.getName() + "_" + currEvent.getLabel() + " *)");
		}
		pw.print("\n");
	    }
	}
	pw.println("END_VAR");		


	// Execution Control Chart (ECC) is the same for all function blocks of this type
	// States of the ECC
	pw.println("EC_STATES");
	pw.println("\tSTART;");
	pw.println("\tSYNC : SYNC -> DONE;");
	pw.println("END_STATES");
	
	// Transitions of the ECC
	pw.println("EC_TRANSITIONS");
	pw.println("\tSTART TO SYNC := ENABLED;");
	pw.println("\tSYNC TO START := 1;");
	pw.println("END_TRANSITIONS");


	// SYNC algorithm
	pw.println("ALGORITHM SYNC IN ST :");
	if (comments)
	{
	    pw.println("\t(* SYNC algorithm described in the paper *)");
	}
		
	for (Iterator autIt = theProject.iterator(); autIt.hasNext();)
	{
	    Automaton curAut = (Automaton) autIt.next();
	    for (Iterator alphIt = curAut.getAlphabet().iterator(); alphIt.hasNext(); )
	    {
		LabeledEvent curEv = (LabeledEvent) alphIt.next();
		pw.print("\tEO_" + curAut.getName() + "_" + curEv.getLabel() + " := ");
		for (Iterator innerAutIt = theProject.iterator(); innerAutIt.hasNext();)
		{
		    Automaton tmpAut = (Automaton) innerAutIt.next();
		    for (Iterator innerAlphIt = tmpAut.getAlphabet().iterator(); innerAlphIt.hasNext();)
		    {
			LabeledEvent tmpEv = (LabeledEvent) innerAlphIt.next();
			System.out.println("Labels: tmpEv = " + tmpEv.getLabel() + "; curEv = " + curEv.getLabel() + ";");
			if(tmpEv.getLabel().equals(curEv.getLabel()))
			{
			    pw.print("EI_" + tmpAut.getName() + "_" + tmpEv.getLabel() + " AND ");
			}
		    }		    
		}
		pw.println("TRUE;");
	    }
	}
	pw.println("END_ALGORITHM");

	pw.println("END_FUNCTION_BLOCK");
	
	
    }
}
