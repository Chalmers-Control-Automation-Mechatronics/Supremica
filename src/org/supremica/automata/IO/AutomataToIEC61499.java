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

/**
 * @author cengic
 *
 * This class generates an IEC 61499 function block implementing 
 * the automata in the current project.
 *  
 * An important notice is that the event in the IEC-61499 is not the 
 * same as the automaton event, that is why the
 * implementation of the interface is done this way
 * 
 * KA and I are planing several different interfaces so this is only the first one.
 * 
 */

public class AutomataToIEC61499

{
	
	private static Logger logger = LoggerFactory.createLogger(AutomataToIEC61499.class);
	
	private Project theProject;
	
	private AutomataToIEC1131 theAutomataToIEC1131;
	
	private Alphabet allEvents;
	
	
	
	
	// Constructor
	
	public AutomataToIEC61499(Project theProject)
	{
		
		this.theProject = theProject;
		this.initialize();
		
		try 
		{
			theAutomataToIEC1131 = new AutomataToIEC1131(theProject);
		} 
		catch (Exception e) 
		{
			logger.error("Something wrong with instantiation of AutomataToIEC1131");
		}
	
	}

	
	
	
	
	
	private void initialize()
	{
		allEvents = theProject.setIndicies();
		
	}




	// Makes the beginning of the function block type declaration
	private void printBeginProgram(PrintWriter pw)
	{

		//pw.println("(* This file was automatically generated from Supremica *)");
		//pw.println("(* Supremica version: " + org.supremica.Version.version() + "*)");
		//pw.println("(* Time of generation: + DateFormat.getDateTimeInstance().format(new Date()) +" *)");
				
		pw.println("FUNCTION_BLOCK AUTOGEN_AUTOMATA_FUNCTION_BLOCK"
					+ "\t"
					+ "(* " 
					+ "Supremica version: " 
					+ org.supremica.Version.version()
					+ "\t"
					+ "Time of generation: " 
					+ DateFormat.getDateTimeInstance().format(new Date()) 
					+ " *)"
					);

	}




	// Makes the fb_interface_list production rule of the standard
	private void printInterfaceList(PrintWriter pw) 
	{


		// The event_input_list. This is the same for all FBs of automata.
		// For now the input events are RESET and OCURED.
		// RESET event makes all of the automata go to their initial state.
		// OCCURRED event signals a new automaton event to the automata and is thus coupled 
		// to the input variables that represent the automaton events.
	 	pw.println("EVENT_INPUT");
	 	pw.println("\tINIT;");
		pw.println("\tRESET;");
		pw.println("\tOCCURRED WITH "); 
		for(Iterator alphIt=allEvents.iterator(); alphIt.hasNext();)
		{								
			LabeledEvent currEvent = (LabeledEvent) alphIt.next();
			if (alphIt.hasNext())
			{
				pw.println("\t\tEI_" + currEvent.getSynchIndex()+ ",");
			} 
			else 
			{
				pw.println("\t\tEI_" + currEvent.getSynchIndex()+ ";");
			}
		}
		pw.println("END_EVENT");
		

		// The event_output_list. This is the same for all FBs of automata also.
		// For now the only output event is DONE and it is coupled with the output variables that
		// represent the state of the automata after the transition upon receving a automaton event.
		pw.println("EVENT_OUTPUT");
		pw.println("\tDONE WITH ");
		for (Iterator alphIt = allEvents.iterator(); alphIt.hasNext();) 
		{
			LabeledEvent currEvent = (LabeledEvent) alphIt.next();
			if (alphIt.hasNext()) 
			{
				pw.println("\t\tEO_" + currEvent.getSynchIndex() + ",");
			} 
			else 
			{
				pw.println("\t\tEO_" + currEvent.getSynchIndex() + ";");
			}
		}
		pw.println("END_EVENT");
		
		
		// The input_variable_list. Input variables represent the automaton events and are of the
		// bool type. Only one can be true when the OCCURRED event happens.
		pw.println("VAR_INPUT");		
		for (Iterator alphIt = allEvents.iterator(); alphIt.hasNext();)
		{
			LabeledEvent currEvent = (LabeledEvent) alphIt.next();
			pw.println("\tEI_" + currEvent.getSynchIndex() + " : BOOL;\t(* " + currEvent.getLabel() + " *)");
		}
		pw.println("END_VAR");

		
		// The output_variable_list. Output variables represent the automaton events that 
		// are enabled after the transition. More than one of tese can be true when the DONE event occurres.
		pw.println("VAR_OUTPUT");
		for (Iterator alphIt = allEvents.iterator(); alphIt.hasNext();)
		{
			LabeledEvent currEvent = (LabeledEvent) alphIt.next();
			pw.println("\tEO_" + currEvent.getSynchIndex() + " : BOOL;\t(* " + currEvent.getLabel() + " *)");
		}
		pw.println("END_VAR");		
	
	}




	// Makes the fb_internal_variable_list production rule of the standard
	private void printInternalVariableList(PrintWriter pw) 
	{
		pw.println("VAR");

		// Internal event variables
		for (Iterator alphIt = allEvents.iterator(); alphIt.hasNext();) 
		{
			LabeledEvent currEvent = (LabeledEvent) alphIt.next();
			pw.println("\te_" + currEvent.getSynchIndex() + " : BOOL;");
		}

		// Variables needed for the AutomataToIEC1131
		pw.println(	"\tenabledEvent : BOOL;" + "(* True if an event is enabled, false otherwise *)");
		theAutomataToIEC1131.printStateVariables(pw,1);
		pw.println("END_VAR");
	}




	// Makes the fb_ecc_declaration
	private void printEccDeclaration(PrintWriter pw) 
	{
		
		// Execution Control Chart (ECC) is the same for all function blocks of this type		
		
		
		// States of the ECC
		pw.println("EC_STATES");
		pw.println("\tSTART;");
		pw.println("\tINIT : INIT;");
		pw.println("\tRESET : RESET;");
		pw.println("\tTRANSITION : TRANSITION;");
		pw.println("\tCOMP_ENABLED : COMP_ENABLED -> DONE;");
		pw.println("END_STATES");
	
	
		// Transitions of the ECC
		pw.println("EC_TRANSITIONS");
		pw.println("\tSTART TO INIT := INIT;");
		pw.println("\tSTART TO RESET := RESET;");
		pw.println("\tSTART TO TRANSITION := OCCURRED;");
		pw.println("\tINIT TO RESET := 1;");
		pw.println("\tRESET TO COMP_ENABLED := 1;");
		pw.println("\tCOMP_ENABLED TO START := 1;");
		pw.println("\tTRANSITION TO COMP_ENABLED := 1;");
		pw.println("END_TRANSITIONS");
	
	
	}




	// Makes the fb_algorithm_declaration
	private void printAlgorithmDeclaration(PrintWriter pw) 
	{

		
		// INIT algorithm is empty for now. Reserved for future development.
		// If needed for representation specific initialization for example.
		pw.println("ALGORITHM INIT IN ST :");
		pw.println("END_ALGORITHM");



		// RESET algorithm resets the automata in the function block. In other words it
		// makes automata enter the initial state.
		pw.println("ALGORITHM RESET IN ST :");
		pw.println("\tinitialized := FALSE;");
		theAutomataToIEC1131.printInitializationStructureAsST(pw);
		pw.println("END_ALGORITHM");


		
		// TRANSITION algorithm makes the transition corresponding to the automaton event
		// that occurred.
		pw.println("ALGORITHM TRANSITION IN ST :");
		// input variables to internal variables
		for (Iterator alphIt = allEvents.iterator(); alphIt.hasNext();)
		{
			LabeledEvent currEvent = (LabeledEvent) alphIt.next();
			pw.println("\te_" + currEvent.getSynchIndex() + " := " + "\tEI_" + currEvent.getSynchIndex() + ";");
		}
		try 
		{
			theAutomataToIEC1131.printComputeEnabledEventsAsST(pw);
			theAutomataToIEC1131.printComputeSingleEnabledEventAsST(pw);
			theAutomataToIEC1131.printChangeStateTransitionsAsST(pw); 
		} 
		catch(Exception e) 
		{
			logger.error("Something wrong with IEC1131 methods in TRANSITION!");
		};
		pw.println("END_ALGORITHM");


		// COMP_ENABLED algorithm computes the enabled automata events in the states of automat after
		// the transition.
		pw.println("ALGORITHM COMP_ENABLED IN ST :");
		theAutomataToIEC1131.printComputeEnabledEventsAsST(pw);
		// internal variables to output variables
		pw.println();
		for (Iterator alphIt = allEvents.iterator(); alphIt.hasNext();)
		{
			LabeledEvent currEvent = (LabeledEvent) alphIt.next();
			pw.println("\tEO_" + currEvent.getSynchIndex() + " := " + "\te_" + currEvent.getSynchIndex() + ";");
		}
		pw.println();
		pw.println("END_ALGORITHM");

	}




	// Makes the end of the function block type declaration
	private void printEndProgram(PrintWriter pw)
	{

		pw.println("END_FUNCTION_BLOCK");

	}




	// Put together function block type declaration
	public void serializeStructuredText(PrintWriter pw)
	{

		printBeginProgram(pw);
		printInterfaceList(pw);
		printInternalVariableList(pw);
		printEccDeclaration(pw);
		printAlgorithmDeclaration(pw);
		printEndProgram(pw);
	
//		printSignalVariables(pw);   -- not used yet, we'll have to see how to handle this.

	}


}
