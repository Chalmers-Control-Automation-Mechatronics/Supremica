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
	
	public static class JGrafchartReader
	{
		private JGrafchartReader()
		{
		}
		
		public static String getName(WorkspaceObject theWorkspace)
		{
			return theWorkspace.getName();
		}
		
		public static AutomatonType getType(WorkspaceObject theWorkspace)
		{
			return AutomatonType.Plant;
		}
		
		public static String getName(GCStep theStep)
		{
			logger.info("steplabel: " + theStep.getLabel() + " stepname: " + theStep.getName());
			return theStep.getName();
		}
		
		public static boolean isInitial(GCStep theStep)
		{
			return (theStep instanceof GCStepInitial);
		}

		public static boolean isAccepting(GCStep theStep)
		{
			return true;
		}	

		public static Iterator getEventIterator(GCTransition theTransition)
		{
			//StringTokenizer tokenizer = new StringTokenizer(theTransition);
			return null;
			//return theStep.getName();
			
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
		
		Automaton currAutomaton = new Automaton(JGrafchartReader.getName(theWorkspace));
		Alphabet theAlphabet = currAutomaton.getAlphabet();
		currAutomaton.setType(JGrafchartReader.getType(theWorkspace));
		GCDocument theDocument = theWorkspace.myContentDocument;
		
		// First create all states
		JGoListPosition pos = theDocument.getFirstObjectPos();
		while (pos != null)
		{
			Object currObject = theDocument.getObjectAtPos(pos);
			pos = theDocument.getNextObjectPosAtTop(pos);
			
			if (currObject instanceof GCStep)
			{
				GCStep currStep = (GCStep)currObject;
				String stateName = JGrafchartReader.getName(currStep);
				if (stateName == null || stateName.equals(""))
				{
					stateName = "q";
				}
				State newState = currAutomaton.createAndAddUniqueState(stateName);
				if (JGrafchartReader.isInitial(currStep))
				{
					newState.setInitial(true);
					currAutomaton.setInitialState(newState);
				}
				newState.setAccepting(JGrafchartReader.isAccepting(currStep));
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

				GCTransition currTransition = (GCTransition)currObject;
				String label = currTransition.getLabelText();
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
						precedingStep = (GCStep)currParent;
						logger.info("precedingStep: "  + precedingStep.getName());
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
						succeedingStep = (GCStep)currParent;
						logger.info("precedingStep: "  + succeedingStep.getName());
					}		
					else
					{
						logger.error("toPort.getParent() is not a GCStep");
					}

				}				
				State precedingState = (State)stepToStateMap.get(precedingStep);		
				if (precedingState == null)
				{
					logger.error("Could not find preceding state");
					return null;
				}				
				State succeedingState = (State)stepToStateMap.get(succeedingStep);		
				if (succeedingState == null)
				{
					logger.error("Could not find succeeding state");
					return null;
				}	
			
				LabeledEvent currEvent = new LabeledEvent(label);
				if (!theAlphabet.contains(currEvent))
				{
					theAlphabet.addEvent(currEvent);
				}
				else
				{
					currEvent = theAlphabet.getEvent(currEvent);
				}
				Arc newArc = new Arc(precedingState, succeedingState, currEvent);
				currAutomaton.addArc(newArc);
				
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
//	        logger.debug("The class of " + currObject + " is " + currObject.getClass().getName());
			
	        if (currObject instanceof WorkspaceObject)
	        {
	        	WorkspaceObject currWorkspace = (WorkspaceObject)currObject;
	        	Automaton currAutomaton = buildAutomaton(currWorkspace);
	        	currProject.addAutomaton(currAutomaton);
	        }
			else if (currObject instanceof GCStep || currObject instanceof GCTransition)
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
			theProject = (VisualProject)projectContainer.getActiveProject();
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
			GCDocument currDoc = (GCDocument)it.next();
			Project currProject = buildProject(currDoc);
			theProject.updateAutomata(currProject);
			// logger.info(currDoc.getName());
		}
		
	}
	
	public void fillDocument(GCDocument doc)
	{
		int yPos = 30;
		GCDocument jgSupervisor = doc;
		jgSupervisor.setWorkspaceName("Automata"); // Top level
		jgSupervisor.setFrameRectangle(new Rectangle(0,0,500,400));

		WorkspaceObject wo = jgSupervisor.createWorkspaceObject(100,50,"Supervisor(Plant)");
		GCDocument supervisor = wo.getSubWorkspace();
		
		// Create Grafcet
		int xpos = 100;
		int ypos = 100;
		GCStepInitial s1 = supervisor.createInitialStep(xpos, 100, "s1(accepting)", "");
		GCTransition t1 = supervisor.createTransition(xpos, 200, "{e1,e2}");
		GCStep s2 = supervisor.createStep(xpos, 300, "s2", "");
		GCTransition t2 = supervisor.createTransition(xpos, 400, "{!e2,e3}");
		supervisor.connect(s1,t1);
		supervisor.connect(t1,s2);
		supervisor.connect(s2,t2);	
		supervisor.connect(t2,s1);		

	}	
}
