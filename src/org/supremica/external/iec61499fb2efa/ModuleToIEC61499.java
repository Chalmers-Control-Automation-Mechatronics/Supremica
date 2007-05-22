/*
 *   Copyright (C) 2006 Goran Cengic
 *
 *   This file is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Lesser General Public
 *   License as published by the Free Software Foundation; either
 *   version 2.1 of the License, or (at your option) any later version.
 *
 *   This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *   Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public
 *   License along with this library; if not, write to the Free Software
 *   Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */

/*
 * @author Goran Cengic (cengic@chalmers.se)
 */
package org.supremica.external.iec61499fb2efa;

import java.io.*;
import java.util.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.supremica.log.*;
import org.supremica.automata.*;
import org.supremica.util.SupremicaException;

//TODO: Make state variables int

/** This class generates an IEC-61499 function block
 * application implementing the automata in the 
 * current project.
 *
 * @author Goran Cengic
 *
 */
public class ModuleToIEC61499
{

	private static final String xmlnsLibraryElementString = "xmlns=\"http://www.holobloc.com/xml/LibraryElement\" ";
	private static Logger logger = LoggerFactory.createLogger(ModuleToIEC61499.class);
	private Project theProject;
	private Alphabet allEvents;
	private boolean comments = false;
	private boolean useXmlns = true;
	private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	private PrintWriter pw;


	// Constructor
	public ModuleToIEC61499(Project theProject)
	{
		this.theProject = theProject;
		allEvents = this.theProject.setIndices();
	}

	private ModuleToIEC61499(Project theProject,boolean comments, boolean useXmlns)
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

	public void useXmlNameSpace(boolean use)
	{
		useXmlns = use;
	}

	public void setPrintWriter(PrintWriter pw)
	{
		this.pw = pw;
	}

	public void printSources(File file)
	{

		String systemName = file.getName().substring(0,file.getName().length()-4);

		try
		{

			// First generate FBs for all models
			for (Iterator autIt = theProject.iterator(); autIt.hasNext();)
			{
				Project tempProject = new Project();

				tempProject.addAutomaton((Automaton) autIt.next());

				File tmpFile = new File(file.getParent() + "/" + tempProject.getAutomatonAt(0).getName() + ".fbt");

				ModuleToIEC61499 tempToIEC61499 = new ModuleToIEC61499(tempProject,comments,useXmlns);

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
		catch (Exception e)
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
		catch (Exception ex)
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
			pw.println(" Supremica version: " + org.supremica.Version.version());
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
		for(Iterator alphIt=allEvents.iterator(); alphIt.hasNext();)
		{
			LabeledEvent currEvent = (LabeledEvent) alphIt.next();
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
		for (Iterator alphIt = allEvents.iterator(); alphIt.hasNext();)
		{
			LabeledEvent currEvent = (LabeledEvent) alphIt.next();
			pw.println("        <With Var=\"EO_" + currEvent.getLabel()+ "\" />");
		}
		pw.println("      </Event>");
		pw.println("    </EventOutputs>");



		// The input_variable_list. Input variables represent the automaton events and are of the
		// bool type. Only one should be TRUE when the OCCURRED event happens but if several are TRUE
		// all of the transitions will take place. The user have to make shure that this is possible!
		pw.println("    <InputVars>");
		for (Iterator alphIt = allEvents.iterator(); alphIt.hasNext();)
		{
			LabeledEvent currEvent = (LabeledEvent) alphIt.next();
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
		for (Iterator alphIt = allEvents.iterator(); alphIt.hasNext();)
		{
			LabeledEvent currEvent = (LabeledEvent) alphIt.next();
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
		for (Iterator autIt = theProject.iterator(); autIt.hasNext(); )
		{
			Automaton currAutomaton = (Automaton) autIt.next();
			int currAutomatonIndex = currAutomaton.getSynchIndex();

			for (Iterator stateIt = currAutomaton.stateIterator(); stateIt.hasNext(); )
			{
				State currState = (State) stateIt.next();
				int currStateIndex = currState.getSynchIndex();

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

	private void printStartAlgorithm(String name)
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
		for (Iterator autIt = theProject.iterator(); autIt.hasNext(); )
		{
			Automaton currAutomaton = (Automaton) autIt.next();
			int currAutomatonIndex = currAutomaton.getSynchIndex();

			for (Iterator stateIt = currAutomaton.stateIterator(); stateIt.hasNext(); )
			{
				State currState = (State) stateIt.next();
				int currStateIndex = currState.getSynchIndex();

				pw.println("        Q_" + currStateIndex + (useXmlns ?  "" : ".value") + " = false;");
			}
		}


		if (comments)
		{
			pw.println();
			pw.println("        &#47;* Then set the initial states to TRUE *&#47;");
		}

		// Then set the initital states to TRUE
		for (Iterator autIt = theProject.iterator(); autIt.hasNext();)
		{
			Automaton currAutomaton = (Automaton) autIt.next();

			if (currAutomaton.getInitialState() == null)
			{
				String errMessage = "ModuleToIEC61499.printAlgorithmDeclarations: " + "all automata must have an initial state but automaton " + currAutomaton.getName() + "doesn't";

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
		for (Iterator alphIt = allEvents.iterator(); alphIt.hasNext();)
		{
			LabeledEvent currEvent = (LabeledEvent) alphIt.next();
			int currEventIndex = currEvent.getSynchIndex();

			if (comments)
			{
				pw.println();
				pw.println("        &#47;* Transitions for event " + currEvent.getLabel() + " *&#47;");
			}

			boolean previousCondition = false;

			pw.println("        if (EI_" + currEvent.getLabel() + (useXmlns ?  "" : ".value") + " &#38;&#38; EO_" + currEvent.getLabel() + (useXmlns ?  "" : ".value") + ")");
			pw.println("        {");

			for (Iterator autIt = theProject.iterator(); autIt.hasNext();)
			{
				Automaton currAutomaton = (Automaton) autIt.next();
				Alphabet theAlphabet = currAutomaton.getAlphabet();
				int currAutomatonIndex = currAutomaton.getSynchIndex();

				if (theAlphabet.contains(currEvent.getLabel()))
				{
					LabeledEvent currAutomatonEvent = currAutomaton.getAlphabet().getEvent(currEvent.getLabel());

					if (currAutomatonEvent == null)
					{
						throw new SupremicaException("ModuleToIEC61499.printAlgorithmDeclarations: " + "Could not find " + currEvent.getLabel() + " in automaton " + currAutomaton.getName());
					}

					boolean previousState = false;

					for (Iterator stateIt = currAutomaton.statesThatEnableEventIterator(currEvent.getLabel()); stateIt.hasNext();)
					{
						State currState = (State) stateIt.next();
						int currStateIndex = currState.getSynchIndex();
						State toState = currState.nextState(currAutomatonEvent);

						if (toState == null)
						{
							throw new SupremicaException("ModuleToIEC61499.printAlgorithmDeclarations: " + "Could not find the next state from state " + currState.getName() + " with label " + currEvent.getLabel() + " in automaton " + currAutomaton.getName());
						}

						int toStateIndex = toState.getSynchIndex();

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
		for (Iterator alphIt = allEvents.iterator(); alphIt.hasNext(); )
		{
			while (alphIt.hasNext())
			{
				LabeledEvent currEvent = (LabeledEvent) alphIt.next();
				int currEventIndex = currEvent.getSynchIndex();

				if (comments)
				{
					pw.println("        &#47;* Enabled condition for event " + currEvent.getLabel() + " *&#47; &#13;&#10;");
				}

				boolean previousCondition = false;

				pw.print("        EO_" + currEvent.getLabel() + (useXmlns ?  "" : ".value") + " = ");

				for (Iterator autIt = theProject.iterator(); autIt.hasNext(); )
				{
					Automaton currAutomaton = (Automaton) autIt.next();
					Alphabet currAlphabet = currAutomaton.getAlphabet();
					int currAutomatonIndex = currAutomaton.getSynchIndex();

					if (currAlphabet.containsEqualEvent(currEvent) && currAlphabet.isPrioritized(currEvent))
					{    // Find all states that enable this event

						boolean previousState = false;

						for (Iterator stateIt = currAutomaton.statesThatEnableEventIterator(currEvent.getLabel()); stateIt.hasNext();)
						{
							State currState = (State) stateIt.next();
							int currStateIndex = currState.getSynchIndex();

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


	private void printSyncFB(String sysName)
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
			pw.println(" Supremica version: " + org.supremica.Version.version());
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
		for(Iterator autIt=theProject.iterator();autIt.hasNext();)
		{
			Automaton tmpAut = (Automaton) autIt.next();
			for(Iterator alphIt=tmpAut.getAlphabet().iterator(); alphIt.hasNext();)
			{
				LabeledEvent currEvent = (LabeledEvent) alphIt.next();
				pw.println("        <With Var=\"EI_" + tmpAut.getName() + "_" + currEvent.getLabel()+ "\" />");
			}
		}
		pw.println("      </Event>");
		pw.println("    </EventInputs>");


		// The event_output_list.
		// The only output event is DONE.
		pw.println("    <EventOutputs>");
		pw.println("      <Event Name=\"DONE\" >");
		for(Iterator autIt=theProject.iterator();autIt.hasNext();)
		{
			Automaton tmpAut = (Automaton) autIt.next();
			for(Iterator alphIt=tmpAut.getAlphabet().iterator(); alphIt.hasNext();)
			{
				LabeledEvent currEvent = (LabeledEvent) alphIt.next();
				pw.println("        <With Var=\"EO_" + tmpAut.getName() + "_" + currEvent.getLabel()+ "\" />");
			}
		}
		pw.println("      </Event>");
		pw.println("    </EventOutputs>");


		// The input_variable_list.
		pw.println("    <InputVars>");
		for(Iterator autIt=theProject.iterator();autIt.hasNext();)
		{
			Automaton tmpAut = (Automaton) autIt.next();
			for (Iterator alphIt = tmpAut.getAlphabet().iterator(); alphIt.hasNext();)
			{
				LabeledEvent currEvent = (LabeledEvent) alphIt.next();
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
		for(Iterator autIt=theProject.iterator();autIt.hasNext();)
		{
			Automaton tmpAut = (Automaton) autIt.next();
			for (Iterator alphIt = tmpAut.getAlphabet().iterator(); alphIt.hasNext();)
			{
				LabeledEvent currEvent = (LabeledEvent) alphIt.next();
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

		for (Iterator autIt = theProject.iterator(); autIt.hasNext();)
		{
			Automaton curAut = (Automaton) autIt.next();
			for (Iterator alphIt = curAut.getAlphabet().iterator(); alphIt.hasNext(); )
			{
				LabeledEvent curEv = (LabeledEvent) alphIt.next();
				pw.print("        EO_" + curAut.getName() + "_" + curEv.getLabel() + " = ");
				for (Iterator innerAutIt = theProject.iterator(); innerAutIt.hasNext();)
				{
					Automaton tmpAut = (Automaton) innerAutIt.next();
					for (Iterator innerAlphIt = tmpAut.getAlphabet().iterator(); innerAlphIt.hasNext();)
					{
						LabeledEvent tmpEv = (LabeledEvent) innerAlphIt.next();
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


	private void printSystem(String name)
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
		for (Iterator autIt = theProject.iterator(); autIt.hasNext();)
		{
			Automaton curAut = (Automaton) autIt.next();
			pw.println("        <FB Name=\"" + curAut.getName()+ "_inst\" Type=\"" + curAut.getName() + "\" />");
		}
        pw.println("        <FB Name=\"" + name + "_SYNC_inst\" Type=\"" + name + "_SYNC\" />");


		// Event Connections
		pw.println("        <EventConnections>");
		boolean first_model = true;
		String firstName;
		String prevName = "";
		for (Iterator autIt = theProject.iterator(); autIt.hasNext();)
		{
			Automaton curAut = (Automaton) autIt.next();
			String curName = curAut.getName();

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
		for (Iterator autIt = theProject.iterator(); autIt.hasNext();)
		{
			Automaton curAut = (Automaton) autIt.next();
			pw.println("          <Connection Source=\"" + curAut.getName() + "_inst.DONE\" Destination=\"merge.EI" + signal + "\" />");
			signal = signal + 1;
		}
		signal = 1;
		for (Iterator autIt = theProject.iterator(); autIt.hasNext();)
		{
			Automaton curAut = (Automaton) autIt.next();
			pw.println("          <Connection Source=\"split.EO" + signal + "\" Destination=\"" + curAut.getName() + "_inst.OCCURRED\" />");
			signal = signal + 1;
		}
		pw.println("          <Connection Source=\"merge.EO\" Destination=\"merge2.EI1\" />");
		pw.println("          <Connection Source=\"merge2.EO\" Destination=\"" + name + "_SYNC_inst.ENABLED\" />");
		pw.println("          <Connection Source=\"" + name + "_SYNC_inst.DONE\" Destination=\"split.EI\" />");

		pw.println("        </EventConnections>");


		// Data Connections
		pw.println("        <DataConnections>");
		for (Iterator autIt = theProject.iterator(); autIt.hasNext();)
		{
			Automaton curAut = (Automaton) autIt.next();
			String aut = curAut.getName();
			for (Iterator alphIt = curAut.getAlphabet().iterator(); alphIt.hasNext(); )
			{
				LabeledEvent curEv = (LabeledEvent) alphIt.next();
				String ev = curEv.getLabel();
				pw.println("          <Connection Source=\"" + name + "_SYNC_inst.EO_" + aut + "_" + ev + "\" Destination=\"" + aut + "_inst.EI_" + ev + "\" />");
			}
			for (Iterator alphIt = curAut.getAlphabet().iterator(); alphIt.hasNext(); )
			{
				LabeledEvent curEv = (LabeledEvent) alphIt.next();
				String ev = curEv.getLabel();
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

	private void printMerge(int size)
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

	private void printSplit(int size)
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
