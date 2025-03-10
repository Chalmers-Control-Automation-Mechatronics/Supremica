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
 * Knarrhogsgatan 10
 * SE-431 60 MOLNDAL
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

/*
 * @author Goran Cengic (cengic@chalmers.se)
 */

package org.supremica.automata.IO;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import net.sourceforge.waters.config.Version;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.automata.Alphabet;
import org.supremica.automata.Automaton;
import org.supremica.automata.LabeledEvent;
import org.supremica.automata.Project;
import org.supremica.automata.State;
import org.supremica.util.SupremicaException;


/** This class generates an IEC-61499 function block
 *  application implementing the automata in the
 *  current project.
 */
public class AutomataToIEC61499
{

	private static final String xmlnsLibraryElementString = "xmlns=\"http://www.holobloc.com/xml/LibraryElement\" ";
	private static Logger logger = LogManager.getLogger(AutomataToIEC61499.class);
	private final Project theProject;
	private final Alphabet allEvents;
	private boolean comments = false;
	private boolean useXmlns = true;
	private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	private PrintWriter pw;


	// Constructor
	public AutomataToIEC61499(final Project theProject)
	{
		this.theProject = theProject;
		allEvents = this.theProject.setIndices();
	}

	private AutomataToIEC61499(final Project theProject,final boolean comments, final boolean useXmlns)
	{
		this.theProject = theProject;
		allEvents = this.theProject.setIndices();
		this.comments = comments;
		this.useXmlns = useXmlns;
	}


	public void commentsOn()
	{
		comments = true;
	}

	public void useXmlNameSpace(final boolean use)
	{
		useXmlns = use;
	}

	public void setPrintWriter(final PrintWriter pw)
	{
		this.pw = pw;
	}

	public void printSources(final File file)
	{

		final String systemName = file.getName().substring(0,file.getName().length()-4);

		try
		{

			// First generate FBs for all models
			for (final Iterator<Automaton> autIt = theProject.iterator(); autIt.hasNext();)
			{
				final Project tempProject = new Project();

				tempProject.addAutomaton(autIt.next());

				final File tmpFile = new File(file.getParent() + "/" + tempProject.getAutomatonAt(0).getName() + ".fbt");

				final AutomataToIEC61499 tempToIEC61499 = new AutomataToIEC61499(tempProject,comments,useXmlns);

				tempToIEC61499.setPrintWriter(new PrintWriter(new FileWriter(tmpFile)));

				tempToIEC61499.printSource();
			}


			// Then generate the sync FB
			File tmpFile = new File(file.getParent() + "/" + systemName + "_SYNC.fbt");

			pw = new PrintWriter(new FileWriter(tmpFile));

			printSyncFB(systemName);

			pw.close();


			// Generate the System application

			pw = new PrintWriter(new FileWriter(file));

			printSystem(systemName);

			pw.close();


			// Generate the Device type and Resource type for FBDK
			if (!useXmlns)
			{
				tmpFile = new File(file.getParent() + "/AutogenDevice.fbt");

				pw = new PrintWriter(new FileWriter(tmpFile));

				pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
				pw.println("<!DOCTYPE DeviceType SYSTEM \"http://www.holobloc.com/xml/LibraryElement.dtd\">");
				pw.println("<DeviceType Name=\"AutogenDevice\" >");
				pw.println("  <VersionInfo Author=\"Automatically Generated\" Organization=\"Chalmers\" Version=\"1.0\" Date=\"" + dateFormat.format(new Date()) + "\" />");
				pw.println("</DeviceType>");

				pw.close();
			}

			// Finnally generate the merge2, merge, split and restart blocks

			// merge 2
			tmpFile = new File(file.getParent() + "/E_MERGE2.fbt");

			pw = new PrintWriter(new FileWriter(tmpFile));

			printMerge(2);

			pw.close();

			// merge
			tmpFile = new File(file.getParent() + "/E_MERGE" + theProject.size() + ".fbt");

			pw = new PrintWriter(new FileWriter(tmpFile));

			printMerge(theProject.size());

			pw.close();

			// split
			tmpFile = new File(file.getParent() + "/E_SPLIT" + theProject.size() + ".fbt");

			pw = new PrintWriter(new FileWriter(tmpFile));

			printSplit(theProject.size());

			pw.close();

			// restart
			tmpFile = new File(file.getParent() + "/E_RESTART.fbt");

			pw = new PrintWriter(new FileWriter(tmpFile));

			printRestart();

			pw.close();

		}
		catch (final Exception e)
		{
			e.printStackTrace(System.err);
		}
	}

	/** Makes the basic function block type declaration. */
	public void printSource()
	{

		printBeginFB();
		printInterfaceList();
		printBeginBasicFB();
		printInternalVariableList();
		printEccDeclaration();
		try
		{
			printAlgorithmDeclarations();
		}
		catch (final Exception ex)
		{
			logger.error(ex);
		}
		printEndBasicFB();
		printEndFB();

		pw.close();

	}

	/** Makes the beginning of the function block type declaration. */
	private void printBeginFB()
	{

		pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		if (!useXmlns)
		{
			pw.println("<!DOCTYPE FBType SYSTEM \"http://www.holobloc.com/xml/LibraryElement.dtd\">");
		}

		if (comments)
		{
			pw.println("<!--");
			pw.println(" This function block was automatically generated from Supremica.");
			pw.println(" " + Version.getInstance().toString());
			pw.println(" Time of generation: " + DateFormat.getDateTimeInstance().format(new Date()));
			pw.println("-->");
		}

		pw.println("<FBType " + (useXmlns ?  xmlnsLibraryElementString : "") + "Name=\"" + theProject.getAutomatonAt(0).getName() + "\" >");
		pw.println("  <VersionInfo Author=\"Automatically Generated\" Organization=\"Chalmers\" Version=\"1.0\" Date=\"" + dateFormat.format(new Date()) + "\" />");

		if (!useXmlns)
		{
			pw.println("  <CompilerInfo header=\"package fb.rt.fbruntime;\" />");
		}

	}

	/** Makes the fb_interface_list production rule of the standard. */
	private void printInterfaceList()
	{

		// The event_input_list. This is the same for all FBs of automata.
		// For now the input events are INIT, RESET and OCURED.
		// INIT event does the initialization
		// RESET event makes all of the automata go to their initial state.
		// OCCURRED event signals a new automaton event to the automata and is thus coupled
		// to the input variables that represent the automaton events.
		pw.println("  <InterfaceList>");
		pw.println("    <EventInputs>");
		pw.println("      <Event Name=\"INIT\" Type=\"INIT_EVENT\" />");
		pw.println("      <Event Name=\"RESET\" />");
		pw.println("      <Event Name=\"OCCURRED\" >");
		for(final Iterator<?> alphIt=allEvents.iterator(); alphIt.hasNext();)
		{
			final LabeledEvent currEvent = (LabeledEvent) alphIt.next();
			pw.println("        <With Var=\"EI_" + currEvent.getLabel()+ "\" />");
		}
		pw.println("      </Event>");
		pw.println("    </EventInputs>");



		// The event_output_list. This is the same for all FBs of automata also.
		// For now the only output event is DONE and it is coupled with the output variables that
		// represent the state of the automata after the transition upon receving a automaton event.
		pw.println("    <EventOutputs>");
		pw.println("      <Event Name=\"INITO\" Type=\"INIT_EVENT\" />");
		pw.println("      <Event Name=\"DONE\" >");
		for (final Iterator<?> alphIt = allEvents.iterator(); alphIt.hasNext();)
		{
			final LabeledEvent currEvent = (LabeledEvent) alphIt.next();
			pw.println("        <With Var=\"EO_" + currEvent.getLabel()+ "\" />");
		}
		pw.println("      </Event>");
		pw.println("    </EventOutputs>");



		// The input_variable_list. Input variables represent the automaton events and are of the
		// bool type. Only one should be TRUE when the OCCURRED event happens but if several are TRUE
		// all of the transitions will take place. The user have to make shure that this is possible!
		pw.println("    <InputVars>");
		for (final Iterator<?> alphIt = allEvents.iterator(); alphIt.hasNext();)
		{
			final LabeledEvent currEvent = (LabeledEvent) alphIt.next();
			if (comments)
			{
				pw.println("      <!-- " + currEvent.getLabel() + " -->");
			}
			pw.println("      <VarDeclaration Name=\"EI_" + currEvent.getLabel() + "\" Type=\"BOOL\" />");
		}
		pw.println("    </InputVars>");


		// The output_variable_list. Output variables represent the automaton events that
		// are enabled after the transition. More than one of tese can be true when the DONE event occurres.
		pw.println("    <OutputVars>");
		for (final Iterator<?> alphIt = allEvents.iterator(); alphIt.hasNext();)
		{
			final LabeledEvent currEvent = (LabeledEvent) alphIt.next();
			if (comments)
			{
				pw.println("      <!-- " + currEvent.getLabel() + " -->");
			}
			pw.println("      <VarDeclaration Name=\"EO_" + currEvent.getLabel() + "\" Type=\"BOOL\" />");
		}
		pw.println("    </OutputVars>");

		pw.println("  </InterfaceList>");
	}

	private void printBeginBasicFB()
	{
		pw.println("  <BasicFB>");
	}


	/** Makes the fb_internal_variable_list production rule of the standard. */
	private void printInternalVariableList()
	{
		pw.println("    <InternalVars>");

		// State variables
		for (final Iterator<?> autIt = theProject.iterator(); autIt.hasNext(); )
		{
			final Automaton currAutomaton = (Automaton) autIt.next();
			currAutomaton.getSynchIndex();
			for (final Iterator<?> stateIt = currAutomaton.stateIterator(); stateIt.hasNext(); )
			{
				final State currState = (State) stateIt.next();
				final int currStateIndex = currState.getSynchIndex();
				if (comments)
				{
					pw.println("      <!-- " + currState.getName() + " -->");
				}
				pw.println("      <VarDeclaration Name=\"Q_" + currStateIndex + "\" Type=\"BOOL\" />");
			}
		}

		pw.println("    </InternalVars>");
	}




	/** Makes the fb_ecc_declaration production rule of the standard. */
	private void printEccDeclaration()
	{

		// Execution Control Chart (ECC) is the same for all function blocks of this type

		pw.println("    <ECC>");

		// States of the ECC
		pw.println("      <ECState Name=\"START\" />");
		pw.println("      <ECState Name=\"INIT\" >");
        pw.println("        <ECAction Algorithm=\"INIT\" />");
		pw.println("        <ECAction Algorithm=\"RESET\" />");
		pw.println("        <ECAction Algorithm=\"COMP_ENABLED\" Output=\"INITO\" />");
		pw.println("      </ECState>");
		pw.println("      <ECState Name=\"RESET\" >");
		pw.println("        <ECAction Algorithm=\"RESET\" />");
		pw.println("      </ECState>");
		pw.println("      <ECState Name=\"TRANSITION\" >");
		pw.println("        <ECAction Algorithm=\"TRANSITION\" />");
		pw.println("      </ECState>");
		pw.println("      <ECState Name=\"COMP_ENABLED\" >");
		pw.println("        <ECAction Algorithm=\"COMP_ENABLED\" Output=\"DONE\" />");
		pw.println("      </ECState>");

		// Transitions of the ECC
		pw.println("      <ECTransition Source=\"START\" Destination=\"INIT\" Condition=\"INIT\" />");
		pw.println("      <ECTransition Source=\"INIT\" Destination=\"START\" Condition=\"1\" />");
		pw.println("      <ECTransition Source=\"START\" Destination=\"RESET\"  Condition=\"RESET\" />");
		pw.println("      <ECTransition Source=\"START\" Destination=\"TRANSITION\"  Condition=\"OCCURRED\" />");
		pw.println("      <ECTransition Source=\"RESET\" Destination=\"COMP_ENABLED\"  Condition=\"1\" />");
		pw.println("      <ECTransition Source=\"COMP_ENABLED\" Destination=\"START\"  Condition=\"1\" />");
		pw.println("      <ECTransition Source=\"TRANSITION\" Destination=\"COMP_ENABLED\" Condition=\"1\" />");

		pw.println("    </ECC>");

	}

	private void printStartAlgorithm(final String name)
	{
		pw.println("    <Algorithm Name=\"" + name + "\">");
		pw.println("      <Other Language=\"Java\" Text=\"");

	}

	private void printEndAlgorithm()
	{
		pw.println("      \" />");
		pw.println("    </Algorithm>");

	}

	/** Makes the fb_algorithm_declaration production rule of the standard. */
	private void printAlgorithmDeclarations()
		throws Exception
	{


		// INIT algorithm is empty for now. Reserved for future development.
		// If needed for representation specific initialization for example.
		printStartAlgorithm("INIT");
		printEndAlgorithm();


		// RESET algorithm resets the automata in the function block. In other words it
		// makes automata enter the initial state.
		printStartAlgorithm("RESET");

		if (comments)
		{
			pw.println("        &#47;* Reset all automata to initial state *&#47;");
			pw.println();
			pw.println("        &#47;* First set all states to FALSE *&#47;");
		}

		// Set all state variables to FALSE
		for (final Iterator<?> autIt = theProject.iterator(); autIt.hasNext(); )
		{
			final Automaton currAutomaton = (Automaton) autIt.next();
			currAutomaton.getSynchIndex();
			for (final Iterator<?> stateIt = currAutomaton.stateIterator(); stateIt.hasNext(); )
			{
				final State currState = (State) stateIt.next();
				final int currStateIndex = currState.getSynchIndex();

				pw.println("        Q_" + currStateIndex + (useXmlns ?  "" : ".value") + " = false;");
			}
		}


		if (comments)
		{
			pw.println();
			pw.println("        &#47;* Then set the initial states to TRUE *&#47;");
		}

		// Then set the initital states to TRUE
		for (final Iterator<?> autIt = theProject.iterator(); autIt.hasNext();)
		{
			final Automaton currAutomaton = (Automaton) autIt.next();

			if (currAutomaton.getInitialState() == null)
			{
				final String errMessage = "AutomataToIEC61499.printAlgorithmDeclarations: " + "all automata must have an initial state but automaton " + currAutomaton.getName() + "doesn't";

				logger.error(errMessage);

				throw new IllegalStateException(errMessage);
			}

			pw.println("        Q_" + currAutomaton.getInitialState().getSynchIndex() + (useXmlns ?  "" : ".value") + " = true;");
		}

		printEndAlgorithm();



		// TRANSITION algorithm makes the transition corresponding to the automaton events that came with the
		// OCCURED.
		printStartAlgorithm("TRANSITION");

		if (comments)
		{
			pw.println("        &#47;* Change state in the automata *&#47;");
		}

		// Iterate over all events and make transitions for the enabled events
		for (final Iterator<?> alphIt = allEvents.iterator(); alphIt.hasNext();)
		{
			final LabeledEvent currEvent = (LabeledEvent) alphIt.next();
			final int currEventIndex = currEvent.getIndex();

			if (comments)
			{
				pw.println();
				pw.println("        &#47;* Transitions for event " + currEvent.getLabel() + " *&#47;");
			}
			pw.println("        if (EI_" + currEvent.getLabel() + (useXmlns ?  "" : ".value") + " &#38;&#38; EO_" + currEvent.getLabel() + (useXmlns ?  "" : ".value") + ")");
			pw.println("        {");

			for (final Iterator<?> autIt = theProject.iterator(); autIt.hasNext();)
			{
				final Automaton currAutomaton = (Automaton) autIt.next();
				final Alphabet theAlphabet = currAutomaton.getAlphabet();
				currAutomaton.getSynchIndex();
				if (theAlphabet.contains(currEvent.getLabel()))
				{
					final LabeledEvent currAutomatonEvent = currAutomaton.getAlphabet().getEvent(currEvent.getLabel());

					if (currAutomatonEvent == null)
					{
						throw new SupremicaException("AutomataToIEC61499.printAlgorithmDeclarations: " + "Could not find " + currEvent.getLabel() + " in automaton " + currAutomaton.getName());
					}

					boolean previousState = false;

					for (final Iterator<?> stateIt = currAutomaton.statesThatEnableEventIterator(currEvent.getLabel()); stateIt.hasNext();)
					{
						final State currState = (State) stateIt.next();
						final int currStateIndex = currState.getSynchIndex();
						final State toState = currState.nextState(currAutomatonEvent);

						if (toState == null)
						{
							throw new SupremicaException("AutomataToIEC61499.printAlgorithmDeclarations: " + "Could not find the next state from state " + currState.getName() + " with label " + currEvent.getLabel() + " in automaton " + currAutomaton.getName());
						}

						final int toStateIndex = toState.getSynchIndex();

						if (currState != toState)
						{
							if (!previousState)
							{
								pw.print("          if");

								previousState = true;
							}
							else
							{
								pw.println("          }");
								pw.print("          else if");
							}

							pw.println(" (Q_" + currStateIndex + (useXmlns ?  "" : ".value") + ")");
							pw.println("          {");
							pw.println("            System.out.println(&#34;" + currAutomaton.getName() + ".fbt:TRANSITION: Changing state from Q_" + currStateIndex + " to Q_" + toStateIndex + " on event " + currEvent.getLabel() + "&#34;);");
							pw.println("            Q_" + toStateIndex + (useXmlns ?  "" : ".value") + " = true;");
							pw.println("            Q_" + currStateIndex + (useXmlns ?  "" : ".value") + " = false;");
						}
						else
						{
							if (comments)
							{
								pw.println("          &#47;* Q_" + currStateIndex + "  has EI_" + currEventIndex + " as self loop, no transition *&#47;");
							}
						}
					}

					if (previousState)
					{
						pw.println("           }");
					}
				}
			}
			pw.println("        }");
		}


		printEndAlgorithm();


		// COMP_ENABLED algorithm computes the enabled automata events in the states of automat after
		// the transition.
		printStartAlgorithm("COMP_ENABLED");

		// Iterate over all events and compute which events that are enabled
		for (final Iterator<?> alphIt = allEvents.iterator(); alphIt.hasNext(); )
		{
			while (alphIt.hasNext())
			{
				final LabeledEvent currEvent = (LabeledEvent) alphIt.next();
				if (comments)
				{
					pw.println("        &#47;* Enabled condition for event " + currEvent.getLabel() + " *&#47; &#13;&#10;");
				}
				pw.print("        EO_" + currEvent.getLabel() + (useXmlns ?  "" : ".value") + " = ");

				for (final Iterator<?> autIt = theProject.iterator(); autIt.hasNext(); )
				{
					final Automaton currAutomaton = (Automaton) autIt.next();
					final Alphabet currAlphabet = currAutomaton.getAlphabet();
					currAutomaton.getSynchIndex();
					if (currAlphabet.containsEqualEvent(currEvent) && currAlphabet.isPrioritized(currEvent))
					{    // Find all states that enable this event

						boolean previousState = false;

						for (final Iterator<?> stateIt = currAutomaton.statesThatEnableEventIterator(currEvent.getLabel()); stateIt.hasNext();)
						{
							final State currState = (State) stateIt.next();
							final int currStateIndex = currState.getSynchIndex();

							if (previousState)
							{
								pw.print(" || ");
							}
							else
							{
								previousState = true;
							}

							pw.print("Q_" + currStateIndex + (useXmlns ?  "" : ".value"));
						}

						if (!previousState)
						{
							pw.print(" false");
						}

						pw.println(";");
					}
				}
			}
		}

		printEndAlgorithm();

	}


	private void printEndBasicFB()
	{
		pw.println("  </BasicFB>");
	}


	/** Makes the end of the function block type declaration */
	private void printEndFB()
	{
		pw.println("</FBType>");
	}


	private void printSyncFB(final String sysName)
	{

		// BeginProgram
		pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		if (!useXmlns)
		{
			pw.println("<!DOCTYPE FBType SYSTEM \"http://www.holobloc.com/xml/LibraryElement.dtd\">");
		}

		if (comments)
		{
			pw.println("<!--");
			pw.println(" This function block was automatically generated from Supremica.");
			pw.println(" " + Version.getInstance().toString());
			pw.println(" Time of generation: " + DateFormat.getDateTimeInstance().format(new Date()));
			pw.println("-->");
		}

		pw.println("<FBType " + (useXmlns ?  xmlnsLibraryElementString : "") + "Name=\"" + sysName + "_SYNC\" >");
		pw.println("  <VersionInfo Author=\"Automatically Generated\" Organization=\"Chalmers\" Version=\"1.0\" Date=\"" + dateFormat.format(new Date()) + "\" />");


		// InterfaceList
		// The event_input_list.
		// Only input is ENABLED.
		pw.println("  <InterfaceList>");
		pw.println("    <EventInputs>");
		pw.println("      <Event Name=\"ENABLED\" >");
		for(final Iterator<?> autIt=theProject.iterator();autIt.hasNext();)
		{
			final Automaton tmpAut = (Automaton) autIt.next();
			for(final Iterator<?> alphIt=tmpAut.getAlphabet().iterator(); alphIt.hasNext();)
			{
				final LabeledEvent currEvent = (LabeledEvent) alphIt.next();
				pw.println("        <With Var=\"EI_" + tmpAut.getName() + "_" + currEvent.getLabel()+ "\" />");
			}
		}
		pw.println("      </Event>");
		pw.println("    </EventInputs>");


		// The event_output_list.
		// The only output event is DONE.
		pw.println("    <EventOutputs>");
		pw.println("      <Event Name=\"DONE\" >");
		for(final Iterator<?> autIt=theProject.iterator();autIt.hasNext();)
		{
			final Automaton tmpAut = (Automaton) autIt.next();
			for(final Iterator<?> alphIt=tmpAut.getAlphabet().iterator(); alphIt.hasNext();)
			{
				final LabeledEvent currEvent = (LabeledEvent) alphIt.next();
				pw.println("        <With Var=\"EO_" + tmpAut.getName() + "_" + currEvent.getLabel()+ "\" />");
			}
		}
		pw.println("      </Event>");
		pw.println("    </EventOutputs>");


		// The input_variable_list.
		pw.println("    <InputVars>");
		for(final Iterator<?> autIt=theProject.iterator();autIt.hasNext();)
		{
			final Automaton tmpAut = (Automaton) autIt.next();
			for (final Iterator<?> alphIt = tmpAut.getAlphabet().iterator(); alphIt.hasNext();)
			{
				final LabeledEvent currEvent = (LabeledEvent) alphIt.next();
				if (comments)
				{
					pw.println("      <!-- " + tmpAut.getName() + currEvent.getLabel() + " -->");
				}
				pw.println("      <VarDeclaration Name=\"EI_" + tmpAut.getName() + "_" + currEvent.getLabel() + "\" Type=\"BOOL\" />");
			}
		}
		pw.println("    </InputVars>");


		// The output_variable_list.
		pw.println("    <OutputVars>");
		for(final Iterator<?> autIt=theProject.iterator();autIt.hasNext();)
		{
			final Automaton tmpAut = (Automaton) autIt.next();
			for (final Iterator<?> alphIt = tmpAut.getAlphabet().iterator(); alphIt.hasNext();)
			{
				final LabeledEvent currEvent = (LabeledEvent) alphIt.next();
				if (comments)
				{
					pw.println("      <!-- " + tmpAut.getName() + currEvent.getLabel() + " -->");
				}
				pw.println("      <VarDeclaration Name=\"EO_" + tmpAut.getName() + "_" + currEvent.getLabel() + "\" Type=\"BOOL\" />");
			}
		}
		pw.println("    </OutputVars>");

		pw.println("  </InterfaceList>");

		printBeginBasicFB();

		// Execution Control Chart (ECC) is the same for all function blocks of this type
		pw.println("    <ECC>");

		// States of the ECC
		pw.println("      <ECState Name=\"START\" />");
		pw.println("      <ECState Name=\"SYNC\" >");
		pw.println("        <ECAction Algorithm=\"SYNC\" Output=\"DONE\" />");
		pw.println("      </ECState>");

		// Transitions of the ECC
		pw.println("      <ECTransition Source=\"START\" Destination=\"SYNC\" Condition=\"ENABLED\" />");
		pw.println("      <ECTransition Source=\"SYNC\" Destination=\"START\" Condition=\"1\" />");

		pw.println("    </ECC>");

		// SYNC algorithm
		printStartAlgorithm("SYNC");

		for (final Iterator<?> autIt = theProject.iterator(); autIt.hasNext();)
		{
			final Automaton curAut = (Automaton) autIt.next();
			for (final Iterator<?> alphIt = curAut.getAlphabet().iterator(); alphIt.hasNext(); )
			{
				final LabeledEvent curEv = (LabeledEvent) alphIt.next();
				pw.print("        EO_" + curAut.getName() + "_" + curEv.getLabel() + " = ");
				for (final Iterator<?> innerAutIt = theProject.iterator(); innerAutIt.hasNext();)
				{
					final Automaton tmpAut = (Automaton) innerAutIt.next();
					for (final Iterator<?> innerAlphIt = tmpAut.getAlphabet().iterator(); innerAlphIt.hasNext();)
					{
						final LabeledEvent tmpEv = (LabeledEvent) innerAlphIt.next();
						if(tmpEv.getLabel().equals(curEv.getLabel()))
						{
							pw.print("EI_" + tmpAut.getName() + "_" + tmpEv.getLabel() + " &#38;&#38; ");
						}
					}
				}
				pw.println("true;");
			}
		}

		printEndAlgorithm();

		printEndBasicFB();

		printEndFB();
	}


	private void printSystem(final String name)
	{

		pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		if (!useXmlns)
		{
			pw.println("<!DOCTYPE System SYSTEM \"http://www.holobloc.com/xml/LibraryElement.dtd\">");
		}
		pw.println("<System " + (useXmlns ?  xmlnsLibraryElementString : "") + "Name=\"" + name + "\" >");
		pw.println("  <VersionInfo Author=\"Automatically Generated\" Organization=\"Chalmers\" Version=\"1.0\" Date=\"" + dateFormat.format(new Date()) + "\" />");
		//pw.println("  <Device Name=\"Test_Device\" Type=\"DeviceType_not_used\">");
		//pw.println("    <Resource Name=\"Test_Resource\" Type=\"ResourceType_not_used\" >");
		pw.println("    <Application Name=\"" + name + "\" >");
		pw.println("      <FBNetwork>");

        pw.println("        <FB Name=\"restart\" Type=\"E_RESTART\" />");
        pw.println("        <FB Name=\"merge2\" Type=\"E_MERGE2\" />");
        pw.println("        <FB Name=\"merge\" Type=\"E_MERGE" + theProject.size() + "\" />");
        pw.println("        <FB Name=\"split\" Type=\"E_SPLIT" + theProject.size() + "\" />");
		// Model FBs
		for (final Iterator<?> autIt = theProject.iterator(); autIt.hasNext();)
		{
			final Automaton curAut = (Automaton) autIt.next();
			pw.println("        <FB Name=\"" + curAut.getName()+ "_inst\" Type=\"" + curAut.getName() + "\" />");
		}
        pw.println("        <FB Name=\"" + name + "_SYNC_inst\" Type=\"" + name + "_SYNC\" />");


		// Event Connections
		pw.println("        <EventConnections>");
		boolean first_model = true;
		String firstName;
		String prevName = "";
		for (final Iterator<?> autIt = theProject.iterator(); autIt.hasNext();)
		{
			final Automaton curAut = (Automaton) autIt.next();
			final String curName = curAut.getName();

			if (first_model)
			{
				firstName = curAut.getName();
				pw.println("          <Connection Source=\"restart.COLD\" Destination=\"" + firstName + "_inst.INIT\" />");
			}
			else if (autIt.hasNext() && !first_model)
			{
				pw.println("          <Connection Source=\"" + prevName + "_inst.INITO\" Destination=\"" + curName + "_inst.INIT\" />");
			}
			else if (!autIt.hasNext())
			{
				pw.println("          <Connection Source=\"" + prevName + "_inst.INITO\" Destination=\"" + curName + "_inst.INIT\" />");
				pw.println("          <Connection Source=\"" + curName + "_inst.INITO\" Destination=\"merge2.EI2\" />");
			}

			prevName = curAut.getName();
			first_model = false;

		}
		int signal = 1;
		for (final Iterator<?> autIt = theProject.iterator(); autIt.hasNext();)
		{
			final Automaton curAut = (Automaton) autIt.next();
			pw.println("          <Connection Source=\"" + curAut.getName() + "_inst.DONE\" Destination=\"merge.EI" + signal + "\" />");
			signal = signal + 1;
		}
		signal = 1;
		for (final Iterator<?> autIt = theProject.iterator(); autIt.hasNext();)
		{
			final Automaton curAut = (Automaton) autIt.next();
			pw.println("          <Connection Source=\"split.EO" + signal + "\" Destination=\"" + curAut.getName() + "_inst.OCCURRED\" />");
			signal = signal + 1;
		}
		pw.println("          <Connection Source=\"merge.EO\" Destination=\"merge2.EI1\" />");
		pw.println("          <Connection Source=\"merge2.EO\" Destination=\"" + name + "_SYNC_inst.ENABLED\" />");
		pw.println("          <Connection Source=\"" + name + "_SYNC_inst.DONE\" Destination=\"split.EI\" />");

		pw.println("        </EventConnections>");


		// Data Connections
		pw.println("        <DataConnections>");
		for (final Iterator<?> autIt = theProject.iterator(); autIt.hasNext();)
		{
			final Automaton curAut = (Automaton) autIt.next();
			final String aut = curAut.getName();
			for (final Iterator<?> alphIt = curAut.getAlphabet().iterator(); alphIt.hasNext(); )
			{
				final LabeledEvent curEv = (LabeledEvent) alphIt.next();
				final String ev = curEv.getLabel();
				pw.println("          <Connection Source=\"" + name + "_SYNC_inst.EO_" + aut + "_" + ev + "\" Destination=\"" + aut + "_inst.EI_" + ev + "\" />");
			}
			for (final Iterator<?> alphIt = curAut.getAlphabet().iterator(); alphIt.hasNext(); )
			{
				final LabeledEvent curEv = (LabeledEvent) alphIt.next();
				final String ev = curEv.getLabel();
				pw.println("          <Connection Source=\"" + aut + "_inst.EO_" + ev + "\" Destination=\"" + name + "_SYNC_inst.EI_" + aut + "_" + ev + "\" />");
			}
		}
		pw.println("        </DataConnections>");


		pw.println("      </FBNetwork>");
		pw.println("    </Application>");
		//pw.println("    </Resource>");
		//pw.println("  </Device>");

		pw.println("   <Device Name=\"Test_Device\" Type=\"FRAME_DEVICE\" />");

		pw.println("</System>");
	}

	private void printMerge(final int size)
	{
		pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		if (!useXmlns)
		{
			pw.println("<!DOCTYPE FBType SYSTEM \"http://www.holobloc.com/xml/LibraryElement.dtd\">");
		}
		pw.println("<FBType " + (useXmlns ?  xmlnsLibraryElementString : "") + "Name=\"E_MERGE" + size + "\" >");
		pw.println("  <VersionInfo Author=\"Automatically Generated\" Organization=\"Chalmers\" Version=\"1.0\" Date=\"" + dateFormat.format(new Date()) + "\" />");
		pw.println("  <InterfaceList>");
		pw.println("    <EventInputs>");
		for(int i=1; i<=size; i++)
		{
			pw.println("      <Event Name=\"EI" + i + "\" />");
		}
		pw.println("    </EventInputs>");
		pw.println("    <EventOutputs>");
		pw.println("      <Event Name=\"EO\" />");
		pw.println("    </EventOutputs>");
		pw.println("  </InterfaceList>");
		pw.println("  <BasicFB>");
		pw.println("    <ECC>");
		pw.println("      <ECState Name=\"S0\" />");
		pw.println("      <ECState Name=\"S1\">");
		pw.println("        <ECAction Output=\"EO\" />");
		pw.println("      </ECState>");
		pw.print("      <ECTransition Source=\"S0\" Destination=\"S1\" Condition=\"");
		for(int i=1; i<=size; i++)
		{
			if (i==size)
			{
				pw.print("EI" + i);
			}
			else
			{
				pw.print("EI" + i + " OR ");
			}
		}
		pw.println("\" />");
  		pw.println("      <ECTransition Source=\"S1\" Destination=\"S0\" Condition=\"1\" />");
		pw.println("    </ECC>");
		pw.println("  </BasicFB>");
		pw.println("</FBType>");
	}

	private void printSplit(final int size)
	{
		pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		if (!useXmlns)
		{
			pw.println("<!DOCTYPE FBType SYSTEM \"http://www.holobloc.com/xml/LibraryElement.dtd\">");
		}
		pw.println("<FBType " + (useXmlns ?  xmlnsLibraryElementString : "") + "Name=\"E_SPLIT" + size + "\" >");
		pw.println("  <VersionInfo Author=\"Automatically Generated\" Organization=\"Chalmers\" Version=\"1.0\" Date=\"" + dateFormat.format(new Date()) + "\" />");
		pw.println("  <InterfaceList>");
		pw.println("    <EventInputs>");
		pw.println("      <Event Name=\"EI\" />");
		pw.println("    </EventInputs>");
		pw.println("    <EventOutputs>");
		for(int i=1; i<=size; i++)
		{
			pw.println("      <Event Name=\"EO" + i + "\" />");
		}
		pw.println("    </EventOutputs>");
		pw.println("  </InterfaceList>");
		pw.println("  <BasicFB>");
		pw.println("    <ECC>");
		pw.println("        <ECState Name=\"S0\" />");
		pw.println("        <ECState Name=\"S1\">");
		for(int i=1; i<=size; i++)
		{
			pw.println("			<ECAction Output=\"EO" + i + "\" />");
		}
		pw.println("        </ECState>");
		pw.println("        <ECTransition Source=\"S0\" Destination=\"S1\" Condition=\"EI\" />");
		pw.println("        <ECTransition Source=\"S1\" Destination=\"S0\" Condition=\"1\" />");
		pw.println("    </ECC>");
		pw.println("  </BasicFB>");
		pw.println("</FBType>");
	}

	private void printRestart()
	{
		pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		if (!useXmlns)
		{
			pw.println("<!DOCTYPE FBType SYSTEM \"http://www.holobloc.com/xml/LibraryElement.dtd\">");
		}
		pw.println("<FBType " + (useXmlns ?  xmlnsLibraryElementString : "") + "Name=\"E_RESTART\" >");
		pw.println("  <VersionInfo Author=\"Automatically Generated\" Organization=\"Chalmers\" Version=\"1.0\" Date=\"" + dateFormat.format(new Date()) + "\" />");
		pw.println("  <InterfaceList>");
		pw.println("    <EventOutputs>");
		pw.println("      <Event Name=\"COLD\" />");
		pw.println("      <Event Name=\"WARM\" />");
		pw.println("      <Event Name=\"STOP\" />");
		pw.println("    </EventOutputs>");
		pw.println("  </InterfaceList>");
		pw.println("</FBType>");
	}

}
