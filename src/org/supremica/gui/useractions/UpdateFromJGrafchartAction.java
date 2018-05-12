//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 1999-2018 Knut Akesson, Martin Fabian, Robi Malik
//###########################################################################
//# This file is part of Waters/Supremica IDE.
//# Waters/Supremica IDE is free software: you can redistribute it and/or
//# modify it under the terms of the GNU General Public License as published
//# by the Free Software Foundation, either version 2 of the License, or
//# (at your option) any later version.
//# Waters/Supremica IDE is distributed in the hope that it will be useful,
//# but WITHOUT ANY WARRANTY; without even the implied warranty of
//# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
//# Public License for more details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters/Supremica IDE statically or dynamically with other modules
//# is making a combined work based on Waters/Supremica IDE. Thus, the terms
//# and conditions of the GNU General Public License cover the whole
//# combination.
//# In addition, as a special exception, the copyright holders of
//# Waters/Supremica IDE give you permission to combine Waters/Supremica IDE
//# with code included in the standard release of Supremica under the
//# Supremica Software License Agreement (or modified versions of such code,
//# with unchanged license). You may copy and distribute such a system
//# following the terms of the GNU GPL for Waters/Supremica IDE and the
//# licenses of the other code concerned.
//# Note that people who make modified versions of Waters/Supremica IDE are
//# not obligated to grant this special exception for their modified versions;
//# it is their choice whether to do so. The GNU General Public License gives
//# permission to release a modified version without this exception; this
//# exception also makes it possible to release a modified version which
//# carries forward this exception.
//###########################################################################

package org.supremica.gui.useractions;

import com.nwoods.jgo.JGoLink;
import com.nwoods.jgo.JGoListPosition;
import com.nwoods.jgo.JGoPort;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.automata.Alphabet;
import org.supremica.automata.Arc;
import org.supremica.automata.Automaton;
import org.supremica.automata.AutomatonType;
import org.supremica.automata.LabeledEvent;
import org.supremica.automata.Project;
import org.supremica.automata.State;
import org.supremica.gui.ActionMan;
import org.supremica.gui.Supremica;
import org.supremica.gui.VisualProject;
import org.supremica.gui.VisualProjectContainer;

import grafchart.sfc.EditorAPI;
import grafchart.sfc.GCDocument;
import grafchart.sfc.GCStep;
import grafchart.sfc.GCStepInitial;
import grafchart.sfc.GCTransition;
import grafchart.sfc.GCTransitionInPort;
import grafchart.sfc.GCTransitionOutPort;
import grafchart.sfc.GrafchartStorage;
import grafchart.sfc.WorkspaceObject;


public class UpdateFromJGrafchartAction
	extends AbstractAction
{
	private static final long serialVersionUID = 1L;
	private static Logger logger = LogManager.getLogger(AbstractAction.class);

	public class JGrafchartStepReader
	{
		private GCStep theStep;
		private String theName;
		private boolean isAccepting = false;
		private boolean isForbidden = false;

		public JGrafchartStepReader() {}

		private void reset()
		{
			isAccepting = false;
			isForbidden = false;
		}

		public void updateData(final GCStep theStep)
		{
			reset();

			this.theStep = theStep;

			final String stepLabel = theStep.getName();

			// First find the state name
			final int firstParanthesisIndex = stepLabel.indexOf("(");
			final int lastParanthesisIndex = stepLabel.indexOf(")");

			if (firstParanthesisIndex == -1)
			{    // No start paranthesis
				theName = stepLabel;

				return;
			}

			if (lastParanthesisIndex == -1)
			{    // No end paranthesis
				logger.error(stepLabel + " has no closing paranthesis");

				return;
			}

			// Here we have arguments
			theName = stepLabel.substring(0, firstParanthesisIndex);

			//logger.info("step name: " + theName);
			final String arguments = stepLabel.substring(firstParanthesisIndex + 1, lastParanthesisIndex);

			//logger.info("step arguments: " + arguments);
			final StringTokenizer st = new StringTokenizer(arguments, ",");

			while (st.hasMoreTokens())
			{
				final String currentParameter = st.nextToken();

				//logger.info("step currentParameter: " + currentParameter);
				if (currentParameter.equalsIgnoreCase("accepting") || currentParameter.equalsIgnoreCase("marked"))
				{
					isAccepting = true;
				}
				else if (currentParameter.equalsIgnoreCase("forbidden"))
				{
					isForbidden = true;
				}
				else
				{
					logger.warn("Unknown parameter: " + currentParameter);
				}
			}
		}

		public String getName()
		{
			return theName;
		}

		public boolean isInitial()
		{
			return (theStep instanceof GCStepInitial);
		}

		public boolean isAccepting()
		{
			return isAccepting;
		}

		public boolean isForbidden()
		{
			return isForbidden;
		}

		public void setAttributes(final State theState)
		{

			// theState.setName(theName); Do not set the name here, it is already set
			theState.setInitial(isInitial());
			theState.setAccepting(isAccepting);
			theState.setForbidden(isForbidden);
		}
	}

	public class JGrafchartTransitionReader
	{
		@SuppressWarnings("unused")
		private GCTransition theTransition;
		private Alphabet theEvents = null;
		private boolean isControllable = true;
		private boolean isPrioritized = true;
		private boolean isObservable = true;
		@SuppressWarnings("unused")
		private boolean isOperator = false;
		private boolean isImmediate = false;
		private boolean isEpsilon = false;
		private String theLabel = null;

		public JGrafchartTransitionReader()
		{
			theEvents = new Alphabet();
		}

		private void reset()
		{
			isControllable = true;
			isPrioritized = true;
			isObservable = true;
			isOperator = false;
			isImmediate = false;
			isEpsilon = false;
			theLabel = "";
		}

		public void updateData(final GCTransition theTransition)
		{
			theEvents.clear();

			this.theTransition = theTransition;

			final String transitionLabel = theTransition.getLabelText();
			final StringTokenizer st = new StringTokenizer(transitionLabel, ",{}");

			while (st.hasMoreTokens())
			{
				reset();

				final String currentParameter = st.nextToken();

				//logger.info("transition currentParameter: " + currentParameter);
				int minIndex = 0;
				int currIndex = currentParameter.indexOf("!");

				if (currIndex >= 0)
				{

					//logger.info("uncontrollable found");
					isControllable = false;

					if (currIndex >= minIndex)
					{
						minIndex = currIndex + 1;
					}
				}

				currIndex = currentParameter.indexOf("?");

				if (currIndex >= 0)
				{
					isPrioritized = false;

					if (currIndex >= minIndex)
					{
						minIndex = currIndex + 1;
					}
				}

				currIndex = currentParameter.indexOf("#");

				if (currIndex >= 0)
				{
					isImmediate = true;

					if (currIndex >= minIndex)
					{
						minIndex = currIndex + 1;
					}
				}

				currIndex = currentParameter.indexOf("@");

				if (currIndex >= 0)
				{
					isEpsilon = true;

					if (currIndex >= minIndex)
					{
						minIndex = currIndex + 1;
					}
				}

				currIndex = currentParameter.indexOf("$");

				if (currIndex >= 0)
				{
					isObservable = false;

					if (currIndex >= minIndex)
					{
						minIndex = currIndex + 1;
					}
				}

				theLabel = currentParameter.substring(minIndex);

				//logger.info("transition minIndex: " + minIndex);
				//logger.info("transition currentLabel: " + theLabel);
				final LabeledEvent currEvent = new LabeledEvent(theLabel);

				currEvent.setControllable(isControllable);
				currEvent.setPrioritized(isPrioritized);
				currEvent.setObservable(isObservable);
				currEvent.setImmediate(isImmediate);
				currEvent.setUnobservable(isEpsilon);
				theEvents.addEvent(currEvent);
			}
		}

		public Alphabet getEvents()
		{
			return theEvents;
		}
	}

	public static class JGrafchartWorkbenchReader
	{
		String theName = null;
		AutomatonType theType = null;

		public JGrafchartWorkbenchReader() {}

		public void updateData(final WorkspaceObject theWorkspace)
		{
			theName = "";
			theType = AutomatonType.SPECIFICATION;

			final String workspaceLabel = theWorkspace.getName();

			// First find the state name
			final int firstParanthesisIndex = workspaceLabel.indexOf("(");
			final int lastParanthesisIndex = workspaceLabel.indexOf(")");

			if (firstParanthesisIndex == -1)
			{    // No start paranthesis
				theName = workspaceLabel;

				return;
			}

			if (lastParanthesisIndex == -1)
			{    // No end paranthesis
				logger.error(workspaceLabel + " has no closing paranthesis");

				return;
			}

			// Here we have arguments
			theName = workspaceLabel.substring(0, firstParanthesisIndex);

			//logger.info("step name: " + theName);
			final String arguments = workspaceLabel.substring(firstParanthesisIndex + 1, lastParanthesisIndex);

			//logger.info("step arguments: " + arguments);
			final StringTokenizer st = new StringTokenizer(arguments, ",");

			while (st.hasMoreTokens())
			{
				final String currentParameter = st.nextToken();

				theType = AutomatonType.toType(currentParameter);

				if (theType == AutomatonType.UNDEFINED)
				{
					logger.error("Unknown parameter: " + currentParameter);
				}
			}
		}

		public String getName()
		{
			return theName;
		}

		public AutomatonType getType()
		{
			return theType;
		}
	}

	public UpdateFromJGrafchartAction()
	{
		super("Update from JGrafchart", new ImageIcon(Supremica.class.getResource("/toolbarButtonGraphics/general/Import16.gif")));

		putValue(SHORT_DESCRIPTION, "Update from JGrafchart");
	}

	public Automaton buildAutomaton(final WorkspaceObject theWorkspace)
	{
		final HashMap<GCStep, State> stepToStateMap = new HashMap<GCStep, State>();
		final JGrafchartTransitionReader transitionReader = new JGrafchartTransitionReader();
		final JGrafchartStepReader stepReader = new JGrafchartStepReader();
		final JGrafchartWorkbenchReader workspaceReader = new JGrafchartWorkbenchReader();

		workspaceReader.updateData(theWorkspace);

		final Automaton currAutomaton = new Automaton(workspaceReader.getName());
		final Alphabet theAlphabet = currAutomaton.getAlphabet();

		currAutomaton.setType(workspaceReader.getType());

		final GCDocument theDocument = theWorkspace.myContentDocument;

		// First create all states
		JGoListPosition pos = theDocument.getFirstObjectPos();
		int minStateIndex = 0;

		while (pos != null)
		{
			final Object currObject = theDocument.getObjectAtPos(pos);

			pos = theDocument.getNextObjectPosAtTop(pos);

			if (currObject instanceof GCStep)
			{
				final GCStep currStep = (GCStep) currObject;

				stepReader.updateData(currStep);

				String stateName = stepReader.getName();

				if ((stateName == null) || stateName.equals("") || currAutomaton.containsStateWithName(stateName))
				{
					do
					{
						stateName = "q" + minStateIndex;

						minStateIndex++;
					}
					while (currAutomaton.containsStateWithName(stateName));
				}

				final State newState = new State(stateName);

				stepReader.setAttributes(newState);
				currAutomaton.addState(newState);

/*
								String stateName = stepReader.getName();
								stepReader.
								if (stateName == null || stateName.equals(""))
								{
										stateName = "q";
								}
								State newState = currAutomaton.createAndAddUniqueState(stateName);
								if (stepReader.isInitial(currStep))
								{
										newState.setInitial(true);
										currAutomaton.setInitialState(newState);
								}
								newState.setAccepting(JGrafchartReader.isAccepting(currStep));
*/
				stepToStateMap.put(currStep, newState);
			}
		}

		// Then create all transitions
		pos = theDocument.getFirstObjectPos();

		while (pos != null)
		{
			final Object currObject = theDocument.getObjectAtPos(pos);

			pos = theDocument.getNextObjectPosAtTop(pos);

			if (currObject instanceof GCTransition)
			{
				final GCTransition currTransition = (GCTransition) currObject;

				transitionReader.updateData(currTransition);

				//String label = currTransition.getLabelText();
				//logger.info("Found GCTransition: " + label );
				// Find preceding step
				GCStep precedingStep = null;
				final GCTransitionInPort transInPort = currTransition.getInPort();
				JGoListPosition currLinkPos = transInPort.getFirstLinkPos();

				while (currLinkPos != null)
				{
					final JGoLink currLink = transInPort.getLinkAtPos(currLinkPos);

					currLinkPos = transInPort.getNextLinkPos(currLinkPos);

					if (currLinkPos != null)
					{
						logger.error("Multiple incoming links to a transition is not allowed");
					}

					final JGoPort fromPort = currLink.getOtherPort(transInPort);
					final Object currParent = fromPort.getParent();

					if (currParent instanceof GCStep)
					{
						precedingStep = (GCStep) currParent;

						//logger.info("precedingStep: "  + precedingStep.getName());
					}
					else
					{
						logger.error("fromPort.getParent() is not a GCStep");
					}
				}

				// Find preceding step
				GCStep succeedingStep = null;
				final GCTransitionOutPort transOutPort = currTransition.getOutPort();

				currLinkPos = transOutPort.getFirstLinkPos();

				while (currLinkPos != null)
				{
					final JGoLink currLink = transOutPort.getLinkAtPos(currLinkPos);

					currLinkPos = transOutPort.getNextLinkPos(currLinkPos);

					if (currLinkPos != null)
					{
						logger.error("Multiple outgoing links from a transition is not allowed");
					}

					final JGoPort toPort = currLink.getOtherPort(transOutPort);
					final Object currParent = toPort.getParent();

					if (currParent instanceof GCStep)
					{
						succeedingStep = (GCStep) currParent;

						//logger.info("precedingStep: "  + succeedingStep.getName());
					}
					else
					{
						logger.error("toPort.getParent() is not a GCStep");
					}
				}

				final State precedingState = stepToStateMap.get(precedingStep);

				if (precedingState == null)
				{
					logger.error("Could not find preceding state");

					return null;
				}

				final State succeedingState = stepToStateMap.get(succeedingStep);

				if (succeedingState == null)
				{
					logger.error("Could not find succeeding state");

					return null;
				}

				final Alphabet theEvents = transitionReader.getEvents();

				for (final Iterator<?> alphIt = theEvents.iterator(); alphIt.hasNext(); )
				{
					LabeledEvent currEvent = (LabeledEvent) alphIt.next();

					if (!theAlphabet.contains(currEvent))
					{
						theAlphabet.addEvent(currEvent);
					}
					else
					{
						currEvent = theAlphabet.getEvent(currEvent.getLabel());
					}

					final Arc newArc = new Arc(precedingState, succeedingState, currEvent);

					currAutomaton.addArc(newArc);
				}
			}
		}

/*
				// Create the steps
				ArrayList theSteps = theDocument.steps;
				logger.info("nbrofsteps: " + theSteps.size());
				for (Iterator it = theSteps.iterator(); it.hasNext(); )
				{
						GCStep currStep = (GCStep)it.next();
						State newState = new State(JGrafchartReader.getName(currStep));
						newState.setInitial(JGrafchartReader.isInitial(currStep));
						newState.setAccepting(JGrafchartReader.isAccepting(currStep));
						currAutomaton.addState(newState);
				}

				// Create the events
				ArrayList theTransitions = theDocument.transitions;
				logger.info("nbroftransitions: " + theTransitions.size());
				for (Iterator it = theTransitions.iterator(); it.hasNext(); )
				{
						GCTransition currTransition = (GCTransition)it.next();

						State newState = new State(JGrafchartReader.getName(currStep));
						newState.setInitial(JGrafchartReader.isInitial(currStep));
						newState.setAccepting(JGrafchartReader.isAccepting(currStep));
						currAutomaton.addState(newState);

				}
*/

		//currAutomaton.setAlphabet(theAlphabet);
		return currAutomaton;
	}

	public Project buildProject(final GCDocument theDocument)
	{

		// logger.info("Top Workspace (" + theDocument.getNumObjects() +"," + theDocument.workspaces.size() + "): " + theDocument.getName());
		final Project currProject = new Project(theDocument.getName());

		// Create the automata
		JGoListPosition pos = theDocument.getFirstObjectPos();

		while (pos != null)
		{
			final Object currObject = theDocument.getObjectAtPos(pos);

			pos = theDocument.getNextObjectPosAtTop(pos);

//              logger.debug("The class of " + currObject + " is " + currObject.getClass().getName());
			if (currObject instanceof WorkspaceObject)
			{
				final WorkspaceObject currWorkspace = (WorkspaceObject) currObject;
				final Automaton currAutomaton = buildAutomaton(currWorkspace);

				currProject.addAutomaton(currAutomaton);
			}
			else if ((currObject instanceof GCStep) || (currObject instanceof GCTransition))
			{
				logger.warn("Found a step or transition in top-level workspace. Each automaton should have its own workspace and the automaton workspace should be inside the top-level workspace.");
			}
		}

/*
				ArrayList workspaceList = doc.workspaces;
				for (Iterator it = workspaceList.iterator(); it.hasNext(); )
				{
						GCDocument currDoc = (GCDocument)it.next();
						logger.info("Workspace: " + currDoc.getName());
						//Automaton currAutomaton = buildAutomaton(currDoc);

						// logger.info(currDoc.getName());
				}
		*/
		return currProject;
	}

	@Override
  public void actionPerformed(final ActionEvent e)
	{

		//ActionMan.updateFromJGrafchart(ActionMan.getGui());
		VisualProject theProject = null;
		try
		{
			final VisualProjectContainer projectContainer = ActionMan.getGui().getVisualProjectContainer();

			theProject = projectContainer.getActiveProject();
			theProject.getJGrafchartEditor();
		}
		catch (final Exception ex)
		{
			logger.error("Exception while getting JGrafchart Editor");
			logger.debug(ex.getStackTrace());

			return;
		}

		//GCDocument workspace1 = theEditor.newWorkspace();
		//fillDocument(workspace1);
		// Print top level workspaces
		final GrafchartStorage theStorage = EditorAPI.topGrafcharts;
		final ArrayList<?> topLevelWorkspaceList = theStorage.getStorage();

		for (final Iterator<?> it = topLevelWorkspaceList.iterator(); it.hasNext(); )
		{
			final GCDocument currDoc = (GCDocument) it.next();
			final Project currProject = buildProject(currDoc);

			theProject.updateAutomata(currProject);

			// logger.info(currDoc.getName());
		}

		ActionMan.getGui().show();
	}

	public void fillDocument(final GCDocument doc)
	{
		final GCDocument jgSupervisor = doc;

		jgSupervisor.setWorkspaceName("Automata");    // Top level
		jgSupervisor.setFrameRectangle(new Rectangle(0, 0, 500, 400));

		final WorkspaceObject wo = jgSupervisor.createWorkspaceObject(100, 50, "Supervisor(Plant)");
		final GCDocument supervisor = wo.getSubWorkspace();

		// Create Grafcet
		final int xpos = 100;
		final GCStepInitial s1 = supervisor.createInitialStep(xpos, 100, "s1(accepting)", "");
		final GCTransition t1 = supervisor.createTransition(xpos, 200, "{e1,e2}");
		final GCStep s2 = supervisor.createStep(xpos, 300, "s2", "");
		final GCTransition t2 = supervisor.createTransition(xpos, 400, "{!e2,e3}");

		supervisor.connect(s1, t1);
		supervisor.connect(t1, s2);
		supervisor.connect(s2, t2);
		supervisor.connect(t2, s1);
	}
}
