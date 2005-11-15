package org.supremica.automata.algorithms.scheduling;

import java.util.*;
import java.io.*;

import org.supremica.log.*;
import org.supremica.automata.*;
import org.supremica.automata.algorithms.*;

public class Milp 
	implements Scheduler
{
	private static Logger logger = LoggerFactory.createLogger(Milp.class);

	private final String MODEL_FILE_NAME = "C:\\Documents and Settings\\avenir\\Desktop\\temp.mod";
// 	private final String MODEL_FILE_NAME = "C:\\Documents and Settings\\Avenir\\Skrivbord\\temp.mod";

	private Automata theAutomata, robots, zones;

//  	public native void jniTest();
	
	public Milp(Automata theAutomata) 
		throws Exception
	{
		logger.info("In the Milp.java");

		initAutomata(theAutomata);

		convertXmlToMod();

		logger.info("Outta Milp.java");
	}

	public int[] schedule()
	{
		return   null;
	}

	public Automaton buildScheduleAutomaton(int[] markedNode) 
	{
		return null;
	}

	private void initAutomata(Automata theAutomata)
		throws Exception
	{
		this.theAutomata = theAutomata;

		robots = theAutomata.getPlantAutomata(); 
		zones = theAutomata.getSpecificationAutomata(); 

		// The robots synchronized with all corresponding specifications (except mutex zone specifications)
		Automata restrictedRobots = new Automata();

		for (int i=0; i<robots.size(); i++)
		{
			Automaton currRobot = robots.getAutomatonAt(i);
			String currRobotName = currRobot.getName();
			
			for (int j=0; j<zones.size(); j++)
			{
				Automaton currSpec = zones.getAutomatonAt(j);
				
				if (currSpec.getName().contains(currRobotName))
				{
					Automata toBeSynched = new Automata(currRobot);
					toBeSynched.addAutomaton(currSpec);

					currRobot = AutomataSynchronizer.synchronizeAutomata(toBeSynched);
				
					currRobot.setName(currRobotName);
					currRobot.setType(AutomatonType.Plant);

					zones.removeAutomaton(currSpec);
				}
			}
			
			restrictedRobots.addAutomaton(currRobot);
		}

		robots = restrictedRobots;
	}

	private void convertXmlToMod() 
		throws Exception
	{	
		int nrOfRobots = robots.size();
		int nrOfZones = zones.size();

		// Finding maximum number of time variables per robot (i.e. max nr of states)
		int nrOfTics = 0;
		for (int i=0; i<nrOfRobots; i++)
		{
			int nbrOfStates = robots.getAutomatonAt(i).nbrOfStates();
			if (nbrOfStates > nrOfTics)
				nrOfTics = nbrOfStates;
		}
		nrOfTics--;

		// The string containing precedence constraints 
		String precConstraints = "";
		
		// Making deltaTime-header
		String deltaTime = "param deltaTime default 0\n:";

		for (int i=0; i<nrOfTics; i++)
			deltaTime += "\t" + i;

		deltaTime += " :=\n";

		// Extracting deltaTimes for each robot
		for (int i=0; i<nrOfRobots; i++) 
		{
			int currTic = 0;
			deltaTime += i;
			
			Automaton currRobot = robots.getAutomatonAt(i);
			StateIterator states = currRobot.stateIterator();

			while (states.hasNext())
			{
				State currState = states.nextState();
				
				if (!currState.isAccepting())
				{
					deltaTime += "\t" + currState.getCost();	

					StateIterator nextStates = currState.nextStateIterator();
					int nbrOfOutgoingArcs = currState.nbrOfOutgoingArcs();

					if (nbrOfOutgoingArcs == 1) 
					{
						State nextState = nextStates.nextState();
						
						precConstraints += "prec_" + currRobot.getName() + "_" + currState.getName() + "_" + nextState.getName() + ": ";
						precConstraints += "time[" + i + ", " + nextState.getIndex() + "] >= time[" + i + ", " + currState.getIndex() + "] + deltaTime[" + i + ", " + currState.getIndex() + "];\n";
					}
					else if (nbrOfOutgoingArcs == 2)
					{
						// Implement t1 >= t0 + delta_t0 - M*alt0; t1_prim >= t0 + delta_t0 - M*(1 - alt0);
					}
					else if (nbrOfOutgoingArcs > 2)
					{
						// Implement t1 >= t0 + delta_t0 - M*alt0; t1_prim >= t0 + delta_t0 - M*alt0_prim; 
						// t1_prim_prim >= t0 + delta_t0 - M*alt0_prim_prim; alt0 + alt0_prim + alt0_prim_prim = 1;
					}

					currTic++;
				}
			}

			// If the number of states of the current automaton is less 
			// than max_nr_of_states, the deltaTime-matrix is filled with points 
			// i.e zeros. 
			for (int j=currTic; j<nrOfTics; j++)
				deltaTime += "\t.";

			deltaTime += "\n";
		}

		// The writing part

		File modelFile = new File(MODEL_FILE_NAME);
		if (!modelFile.exists())
			modelFile.createNewFile();

		BufferedWriter w = new BufferedWriter(new FileWriter(modelFile));

		// Definitions of parameters
		w.write("param nrOfRobots >= 0;");
		w.newLine();
		w.write("param nrOfZones >= 0;");
		w.newLine();
		w.write("param maxTic >= 0;");
		w.newLine();
		
		// Definitions of sets
		w.newLine();
		w.write("set Robots := 0..nrOfRobots;");
		w.newLine();
		w.write("set Zones := 0..nrOfZones;");
		w.newLine();
		w.write("set Tics := 0..maxTic;");
		w.newLine();

		// Definitions of parameters, using sets as their input (must be in this order to avoid GLPK-complaints)
		w.newLine();
		w.write("param deltaTime{r in Robots, t in Tics};");
		w.newLine();

		// Definitions of variables
		w.newLine();
		w.write("var time{r in Robots, t in Tics} >= 0;");
		w.newLine();
		w.write("var c;");
		w.newLine();

		// The objective function
		w.newLine();
		w.write("minimize makespan: c;");
		w.newLine();

		// The constraints section
		w.newLine();
		w.write("subject to");
		w.newLine();

		// The cycle time constraints
		w.newLine();
		w.write("cycle_time{r in Robots}: c >= time[r, maxTic];");
		w.newLine();
		
		// The precedence constraints
		w.newLine();
		w.write(precConstraints);
		w.newLine();
		
// 		// The precedence constraints
// 		w.newLine();
// 		w.write("s.t. precedence{r in Robots, t in Tics: t>0}:");
// 		w.newLine();
// 		w.write("\ttime[r,t] >= time[r,t-1] + deltaTime[r,t];");
// 		w.newLine();
	
		// The end of the model-section and the beginning of the data-section
		w.newLine();
		w.write("data;");
		w.newLine();

		// The numbers of robots resp. zones are given
		w.newLine();
		w.write("param nrOfRobots := " + (nrOfRobots - 1) + ";");
		w.newLine();
		w.write("param nrOfZones := " + (nrOfZones - 1) + ";");
		w.newLine();
		// Behovs maxTic verkligen???
		w.write("param maxTic := " + (nrOfTics - 1) + ";");
		w.newLine();

// 		String altRouteConstraints = "";
// 		String precedenceConstraints = "";

// 		// The header of the deltaTime-parameter
// 		String deltaTime = ":";
// 		for (int i=0; i<nrOfTics; i++)
// 			deltaTime += "\t" + i;
// 		deltaTime += " := ";

// 		w.newLine();
// 		w.write("param deltaTime default 0");
// 		w.newLine();
// 		w.write(deltaTime);
// 		w.newLine();

// 			currStates.add(currAuto.getInitialState());

// 			while (!currStates.isEmpty())
// 			{		
// 				State currState = currStates.remove(0);				
// 				StateIterator nprecedenceConstraintsextStates = currState.nextStatesIterator();

// 				closedStates.add(0, currState);

// 				while (nextStates.hasNext())
// 				{
// 					State nextState = nextStates.next();
					
// 					if (!nextStates.isAccepting())
// 					{
// 						if (!closedStates.contains(nextState))
// 						{
// 							deltaTimes += "\t" + nextState.getCost();
// 							currTic++;
// 						}

// 						int parentTick = closedStates.size();
// 						precedenceConstraints += "time[" + i + ", " + currTic + "] >= time [" + i + ", " + parentTick + "] deltaTime[" + i + ", " +  parentTick + "]";
						
// 					}
// 					currStates.add(nextState);
// 				}

// 				deltaTime += "\t" + currState.getCost();

// 			}
			
		w.newLine();
		w.write(deltaTime);
		w.newLine();

		w.newLine();
		w.write("end;");
		w.flush();
	}
	
}