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
		Automaton currAutomaton = new Automaton(JGrafchartReader.getName(theWorkspace));
		currAutomaton.setType(JGrafchartReader.getType(theWorkspace));
		GCDocument theDocument = theWorkspace.myContentDocument;
		
	
		// Create the steps
		ArrayList theSteps = theDocument.steps;
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
		for (Iterator it = theTransitions.iterator(); it.hasNext(); )
		{
			GCTransition currTransition = (GCTransition)it.next();
			
			/*
			State newState = new State(JGrafchartReader.getName(currStep));
			newState.setInitial(JGrafchartReader.isInitial(currStep));
			newState.setAccepting(JGrafchartReader.isAccepting(currStep));
			currAutomaton.addState(newState);
			*/
		}
		
		return currAutomaton;
	}	
	
	public Project buildProject(GCDocument theDocument)
	{
		logger.info("Top Workspace (" + theDocument.getNumObjects() +"," + theDocument.workspaces.size() + "): " + theDocument.getName());
		
		Project currProject = new Project(theDocument.getName());
	
		// Create the automata
		JGoListPosition pos = theDocument.getFirstObjectPos();
		while (pos != null)
		{
			Object currObject = theDocument.getObjectAtPos(pos);
			pos = theDocument.getNextObjectPosAtTop(pos);
	        logger.debug("The class of " + currObject + " is " + currObject.getClass().getName());
			
	        if (currObject instanceof WorkspaceObject)
	        {
	        	WorkspaceObject currWorkspace = (WorkspaceObject)currObject;
	        	buildAutomaton(currWorkspace);
	        }
	        /*
			if (currObject instanceof GCDocument)
			{
				logger.info("Found GCDocument: " + ((GCDocument)theDocument).getName());
			}
			if (currObject instanceof GCVariable)
			{
				logger.info("Found GCVariable: " + ((GCDocument)theDocument).getName());
			}
			*/
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
		
		JGrafchartSupremicaEditor theEditor = null;
		try
		{
			VisualProjectContainer projectContainer = ActionMan.getGui().getVisualProjectContainer();
			VisualProject theProject = (VisualProject)projectContainer.getActiveProject();
			theEditor = theProject.getJGrafchartEditor();
		}
		catch (Exception ex)
		{
			logger.error("Exception while getting JGrafchart Editor");
			logger.debug(ex.getStackTrace());
			return;
		}	
		
		// List information

		//theEditor.setTitle("JGrafchart as Supremica Editor");
		//Editor.singleton = theEditor;
		//EditorAPI.removePaletteAction();

		//URL url = Supremica.class.getResource("/shoefactory/ShoeFactory.xml");
		
		//needed to replace %20 with spaces in the path");
		//String xmlPath = (url.getPath()).replaceAll("%20"," ");
		//top = e.openWorkspace(xmlPath);

		GCDocument workspace1 = theEditor.newWorkspace();		
		GCDocument workspace2 = theEditor.newWorkspace();			
		fillDocument(workspace1, 1);
		
		// Print top level workspaces
		GrafchartStorage theStorage = EditorAPI.topGrafcharts;
		ArrayList topLevelWorkspaceList = theStorage.getStorage();
		for (Iterator it = topLevelWorkspaceList.iterator(); it.hasNext(); )
		{
			GCDocument currDoc = (GCDocument)it.next();
			Project currProject = buildProject(currDoc);
			
			// logger.info(currDoc.getName());
		}
		
	}
	
	public void fillDocument(GCDocument doc, int nr)
	{
		int yPos = 30;
		GCDocument jgSupervisor = doc;
		jgSupervisor.setWorkspaceName("JgrafSupervisor"); // Top level
		jgSupervisor.setFrameRectangle(new Rectangle(0,0,800,400));

		WorkspaceObject wo = jgSupervisor.createWorkspaceObject(100,50,"Supervisor");
		GCDocument supervisor = wo.getSubWorkspace();
		
//		BooleanVariable[] bv1 = new BooleanVariable[2];
		
		BooleanVariable b1 = jgSupervisor.createBooleanVariable(300,50,"success","0");
		BooleanVariable b2 = jgSupervisor.createBooleanVariable(700,150,"turn","0");
		
		// Create Grafcet
		int xpos = 100;
		int ypos = 100;
		GCStepInitial s1 = supervisor.createInitialStep(xpos, 100, "s1", "");
		GCTransition t1 = supervisor.createTransition(xpos, 150, "e1");
		GCStep s2 = supervisor.createStep(xpos, 200, "s2", "");
		GCTransition t2 = supervisor.createTransition(xpos, 250, "e2");
		supervisor.connect(s1,t1);
		supervisor.connect(t1,s2);
		supervisor.connect(s2,t2);	
		supervisor.connect(t2,s1);		
		/*	
		GCTransition t1 = supervisor.createTransition(100,yPos,"nrOfShoes>0 & success");
		yPos+=50;
		GCStep s2 = supervisor.createStep(100,yPos,"sA","S index=applyStaticMethod(\"org.supremica.external.shoefactory.Executor.FactoryExecutor\",\"getShoeIndex\",\"int\",\"int\",0);\nS turn=1 ;\nX turn=0;");
		yPos+=100;
		GCTransition tr = supervisor.createTransition(100,yPos,"1");
		yPos+=50;
		GCStep sB = supervisor.createStep(100,yPos,"sB",";");
		yPos+=100;
		GCTransition trC = supervisor.createTransition(100,yPos+30,"");
		
		GCStep sC = supervisor.createStep(100,yPos+200,"sC","S index=applyStaticMethod(\"org.supremica.external.shoefactory.Executor.FactoryExecutor\",\"getShoeIndex\",\"int\",\"int\",index);\nS success = applyStaticMethod(\"org.supremica.external.shoefactory.Executor.FactoryExecutor\",\"saveValues\",\"boolean\",\"int\",nrOfShoes);");
		GCTransition trDa = supervisor.createTransition(100,yPos+300,"index>0");
		GCTransition trDb = supervisor.createTransition(300,yPos+300,"index<0 & nrOfShoes>0");
		
		for(int i=1; i<putEventsA.length+1; i++)
		{
			GCTransition tr = checkTablePutEvents(supervisor,sB,i-1);
			bv1[i-1] = jgSupervisor.createBooleanVariable(100*i,250,putEventsA[i-1],"0");
			supervisor.connect(tr,sC);
		}
		
		for(int i=1; i<putEventsB.length+1; i++)
		{
			GCTransition tr = checkTableWithStationPutEvents(supervisor,sB,i-1);
			bv1[putEventsA.length] = jgSupervisor.createBooleanVariable(100*i,350,putEventsB[i-1],"0");
			supervisor.connect(tr,sC);
		}
		
		for(int i=1; i<putEventsC.length+1; i++)
		{
			GCTransition tr = checkStationPutEvents(supervisor,sB,i-1);
			bv1[putEventsA.length+putEventsB.length] = jgSupervisor.createBooleanVariable(100*i,450,putEventsC[i-1],"0");
			supervisor.connect(tr,sC);
		}
		
		for(int i=1; i<getEventsA.length+1; i++)
		{
			GCTransition tr = checkTableGetEvents(supervisor,sB,i-1);
			bv1[putEventsA.length+putEventsB.length+putEventsC.length] = jgSupervisor.createBooleanVariable(100*i,550,getEventsA[i-1],"0");
			supervisor.connect(tr,sC);
		}
		
		for(int i=1; i<getEventsB.length+1; i++)
		{
			GCTransition tr = checkTableWithStationGetEvents(supervisor,sB,i-1);
			bv1[putEventsA.length+putEventsB.length+putEventsC.length+getEventsA.length] = jgSupervisor.createBooleanVariable(100*i,650,getEventsB[i-1],"0");
			supervisor.connect(tr,sC);
		}
		
		for(int i=1; i<getEventsC.length+1; i++)
		{
			GCTransition tr = checkStationGetEvents(supervisor,sB,i-1);
			bv1[putEventsA.length+putEventsB.length+putEventsC.length+getEventsA.length+getEventsB.length] = jgSupervisor.createBooleanVariable(100*i,750,getEventsC[i-1],"0");
			supervisor.connect(tr,sC);
		}
		
		GCTransition trEr1 = supervisor.createTransition(9100,yPos,"errorEvent");
		GCStep sEr = supervisor.createStep(9100,yPos+50,"Error","S success = applyStaticMethod(\"org.supremica.external.jgrafchart.Supervisor\",\"executeEvent\",\"boolean\",\"string\",supervisor,\"string\",\"Error\"+index);\nS success = applyStaticMethod(\"org.supremica.external.shoefactory.Animator.JgrafSupervisor\",\"moveInitial\",\"boolean\",\"string\",\"shoeSpec\"+index,\"string\",\"Error\"+index);\nS currEvent = \"Shoe\"+index+\".Events.error\";\nS currEvent^=1;");
		GCTransition trEr2 = supervisor.createTransition(9100,yPos+150,"Error.T>1");
		
		trC.setConditionText("!("+conditionString+"errorEvent)");
		sB.setActionText(actionString);
		
		supervisor.connect(initialStep,trA);
		supervisor.connect(trA,sA);
		supervisor.connect(sA,trB);
		supervisor.connect(trB,sB);
		supervisor.connect(sB,trEr1);
		supervisor.connect(trEr1,sEr);
		supervisor.connect(sEr,trEr2);
		supervisor.connect(sB,trC);
		supervisor.connect(trEr2,sC);
		supervisor.connect(trC,sC);
		supervisor.connect(sC,trDa);
		supervisor.connect(sC,trDb);
		supervisor.connect(trDa,sB);
		supervisor.connect(trDb,sA);
		*/
	}	
}
