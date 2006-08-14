package org.supremica.gui.useractions;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import org.supremica.gui.Supremica;
import org.supremica.gui.VisualProject;
import org.supremica.gui.VisualProjectContainer;
import org.supremica.gui.ActionMan;
import grafchart.sfc.JGrafchartSupremicaEditor;
import org.supremica.log.*;
import grafchart.sfc.*;
import java.util.*;
import org.supremica.automata.*;
import com.nwoods.jgo.*;

public class UpdateFromJGrafchartAction
	extends AbstractAction
{
	private static Logger logger = LoggerFactory.createLogger(AbstractAction.class);

	public class JGrafchartStepReader
	{
		private GCStep theStep;
		private String theName;
		private boolean isAccepting = false;
		private boolean isForbidden = false;

		public JGrafchartStepReader() {}

		private void reset()
		{
			String theName = "";

			isAccepting = false;
			isForbidden = false;
		}

		public void updateData(GCStep theStep)
		{
			reset();

			this.theStep = theStep;

			String stepLabel = theStep.getName();

			// First find the state name
			int firstParanthesisIndex = stepLabel.indexOf("(");
			int lastParanthesisIndex = stepLabel.indexOf(")");

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
			String arguments = stepLabel.substring(firstParanthesisIndex + 1, lastParanthesisIndex);

			//logger.info("step arguments: " + arguments);          
			StringTokenizer st = new StringTokenizer(arguments, ",");

			while (st.hasMoreTokens())
			{
				String currentParameter = st.nextToken();

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

		public void setAttributes(State theState)
		{

			// theState.setName(theName); Do not set the name here, it is already set
			theState.setInitial(isInitial());
			theState.setAccepting(isAccepting);
			theState.setForbidden(isForbidden);
		}
	}

	public class JGrafchartTransitionReader
	{
		private GCTransition theTransition;
		private Alphabet theEvents = null;
		private boolean isControllable = true;
		private boolean isPrioritized = true;
		private boolean isObservable = true;
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

		public void updateData(GCTransition theTransition)
		{
			theEvents.clear();

			this.theTransition = theTransition;

			String transitionLabel = theTransition.getLabelText();
			StringTokenizer st = new StringTokenizer(transitionLabel, ",{}");

			while (st.hasMoreTokens())
			{
				reset();

				String currentParameter = st.nextToken();

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
				LabeledEvent currEvent = new LabeledEvent(theLabel);

				currEvent.setControllable(isControllable);
				currEvent.setPrioritized(isPrioritized);
				currEvent.setObservable(isObservable);
				currEvent.setImmediate(isImmediate);
				currEvent.setEpsilon(isEpsilon);
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

		public void updateData(WorkspaceObject theWorkspace)
		{
			theName = "";
			theType = AutomatonType.SPECIFICATION;

			String workspaceLabel = theWorkspace.getName();

			// First find the state name
			int firstParanthesisIndex = workspaceLabel.indexOf("(");
			int lastParanthesisIndex = workspaceLabel.indexOf(")");

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
			String arguments = workspaceLabel.substring(firstParanthesisIndex + 1, lastParanthesisIndex);

			//logger.info("step arguments: " + arguments);          
			StringTokenizer st = new StringTokenizer(arguments, ",");

			while (st.hasMoreTokens())
			{
				String currentParameter = st.nextToken();

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

	public Automaton buildAutomaton(WorkspaceObject theWorkspace)
	{
		HashMap stepToStateMap = new HashMap();
		JGrafchartTransitionReader transitionReader = new JGrafchartTransitionReader();
		JGrafchartStepReader stepReader = new JGrafchartStepReader();
		JGrafchartWorkbenchReader workspaceReader = new JGrafchartWorkbenchReader();

		workspaceReader.updateData(theWorkspace);

		Automaton currAutomaton = new Automaton(workspaceReader.getName());
		Alphabet theAlphabet = currAutomaton.getAlphabet();

		currAutomaton.setType(workspaceReader.getType());

		GCDocument theDocument = theWorkspace.myContentDocument;

		// First create all states
		JGoListPosition pos = theDocument.getFirstObjectPos();
		int minStateIndex = 0;

		while (pos != null)
		{
			Object currObject = theDocument.getObjectAtPos(pos);

			pos = theDocument.getNextObjectPosAtTop(pos);

			if (currObject instanceof GCStep)
			{
				GCStep currStep = (GCStep) currObject;

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

				State newState = new State(stateName);

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
			Object currObject = theDocument.getObjectAtPos(pos);

			pos = theDocument.getNextObjectPosAtTop(pos);

			if (currObject instanceof GCTransition)
			{
				GCTransition currTransition = (GCTransition) currObject;

				transitionReader.updateData(currTransition);

				//String label = currTransition.getLabelText();
				//logger.info("Found GCTransition: " + label ); 
				// Find preceding step
				GCStep precedingStep = null;
				GCTransitionInPort transInPort = currTransition.getInPort();
				JGoListPosition currLinkPos = transInPort.getFirstLinkPos();

				while (currLinkPos != null)
				{
					JGoLink currLink = transInPort.getLinkAtPos(currLinkPos);

					currLinkPos = transInPort.getNextLinkPos(currLinkPos);

					if (currLinkPos != null)
					{
						logger.error("Multiple incoming links to a transition is not allowed");
					}

					JGoPort fromPort = currLink.getOtherPort(transInPort);
					Object currParent = fromPort.getParent();

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
				GCTransitionOutPort transOutPort = currTransition.getOutPort();

				currLinkPos = transOutPort.getFirstLinkPos();

				while (currLinkPos != null)
				{
					JGoLink currLink = transOutPort.getLinkAtPos(currLinkPos);

					currLinkPos = transOutPort.getNextLinkPos(currLinkPos);

					if (currLinkPos != null)
					{
						logger.error("Multiple outgoing links from a transition is not allowed");
					}

					JGoPort toPort = currLink.getOtherPort(transOutPort);
					Object currParent = toPort.getParent();

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

				State precedingState = (State) stepToStateMap.get(precedingStep);

				if (precedingState == null)
				{
					logger.error("Could not find preceding state");

					return null;
				}

				State succeedingState = (State) stepToStateMap.get(succeedingStep);

				if (succeedingState == null)
				{
					logger.error("Could not find succeeding state");

					return null;
				}

				Alphabet theEvents = transitionReader.getEvents();

				for (Iterator alphIt = theEvents.iterator(); alphIt.hasNext(); )
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

					Arc newArc = new Arc(precedingState, succeedingState, currEvent);

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

	public Project buildProject(GCDocument theDocument)
	{

		// logger.info("Top Workspace (" + theDocument.getNumObjects() +"," + theDocument.workspaces.size() + "): " + theDocument.getName());
		Project currProject = new Project(theDocument.getName());

		// Create the automata
		JGoListPosition pos = theDocument.getFirstObjectPos();

		while (pos != null)
		{
			Object currObject = theDocument.getObjectAtPos(pos);

			pos = theDocument.getNextObjectPosAtTop(pos);

//              logger.debug("The class of " + currObject + " is " + currObject.getClass().getName());
			if (currObject instanceof WorkspaceObject)
			{
				WorkspaceObject currWorkspace = (WorkspaceObject) currObject;
				Automaton currAutomaton = buildAutomaton(currWorkspace);

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

	public void actionPerformed(ActionEvent e)
	{

		//ActionMan.updateFromJGrafchart(ActionMan.getGui());
		VisualProject theProject = null;
		JGrafchartSupremicaEditor theEditor = null;

		try
		{
			VisualProjectContainer projectContainer = ActionMan.getGui().getVisualProjectContainer();

			theProject = (VisualProject) projectContainer.getActiveProject();
			theEditor = theProject.getJGrafchartEditor();
		}
		catch (Exception ex)
		{
			logger.error("Exception while getting JGrafchart Editor");
			logger.debug(ex.getStackTrace());

			return;
		}

		//GCDocument workspace1 = theEditor.newWorkspace();                             
		//fillDocument(workspace1);
		// Print top level workspaces
		GrafchartStorage theStorage = EditorAPI.topGrafcharts;
		ArrayList topLevelWorkspaceList = theStorage.getStorage();

		for (Iterator it = topLevelWorkspaceList.iterator(); it.hasNext(); )
		{
			GCDocument currDoc = (GCDocument) it.next();
			Project currProject = buildProject(currDoc);

			theProject.updateAutomata(currProject);

			// logger.info(currDoc.getName());
		}

		ActionMan.getGui().show();
	}

	public void fillDocument(GCDocument doc)
	{
		int yPos = 30;
		GCDocument jgSupervisor = doc;

		jgSupervisor.setWorkspaceName("Automata");    // Top level
		jgSupervisor.setFrameRectangle(new Rectangle(0, 0, 500, 400));

		WorkspaceObject wo = jgSupervisor.createWorkspaceObject(100, 50, "Supervisor(Plant)");
		GCDocument supervisor = wo.getSubWorkspace();

		// Create Grafcet
		int xpos = 100;
		int ypos = 100;
		GCStepInitial s1 = supervisor.createInitialStep(xpos, 100, "s1(accepting)", "");
		GCTransition t1 = supervisor.createTransition(xpos, 200, "{e1,e2}");
		GCStep s2 = supervisor.createStep(xpos, 300, "s2", "");
		GCTransition t2 = supervisor.createTransition(xpos, 400, "{!e2,e3}");

		supervisor.connect(s1, t1);
		supervisor.connect(t1, s2);
		supervisor.connect(s2, t2);
		supervisor.connect(t2, s1);
	}
}
