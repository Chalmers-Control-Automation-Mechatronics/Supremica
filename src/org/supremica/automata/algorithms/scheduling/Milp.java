package org.supremica.automata.algorithms.scheduling;

import java.util.*;
import java.io.*;

import org.supremica.log.*;
import org.supremica.automata.*;

public class Milp 
	implements Scheduler
{
	private static Logger logger = LoggerFactory.createLogger(Milp.class);

// 	private final String MODEL_FILE_NAME = "C:\\Documents and Settings\\avenir\\Desktop\\temp.mod";
	private final String MODEL_FILE_NAME = "C:\\Documents and Settings\\Avenir\\Skrivbord\\temp.mod";

	private Automata theAutomata;

//  	public native void jniTest();
	
	public Milp(Automata theAutomata) 
		throws Exception
	{
		this.theAutomata = theAutomata;

		logger.info("In the Milp.java");

		convertXmlToMod();

		logger.info("Outta Milp.java");
	}

	public int[] schedule()
	{
		return null;
	}

	public Automaton buildScheduleAutomaton(int[] markedNode) 
	{
		return null;
	}

	private void convertXmlToMod() 
		throws Exception
	{
		Automata robots = theAutomata.getPlantAutomata(); 
		Automata zones = theAutomata.getSpecificationAutomata(); 

		int nrOfRobots = robots.size();
		int nrOfZones = zones.size();

		File modelFile = new File(MODEL_FILE_NAME);
		if (!modelFile.exists())
			modelFile.createNewFile();

		// The writing part

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
m		w.write("minimize makespan: c;");
		w.newLine();
		
		// The precedence constraints
		w.newLine();
		w.write("s.t. precedence{r in Robots, t in Tics: t>0}:");
		w.newLine();
		w.write("\ttime[r,t] >= time[r,t-1] + deltaTime[r,t];");
		w.newLine();

		// The cycle time constraints
		w.newLine();
		w.write("s.t. cycle_time{r in Robots}: c >= time[r, maxTic];");
		w.newLine();
		
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
		
	// 	// This part should be automatized
		int nrOfTics = 0;
		for (int i=0; i<nrOfRobots; i++)
		{
			if (nbrOfStates > nrOfTics)
				nrOfTics = nbrOfStates;
		}

		// behovs nrOfTics i *.mod-filen?
		w.write("param maxTic := " + (nrOfTics - 1) + ";");
		w.newLine();

		String altRouteConstraints = "";
		String precedenceConstraints = "";

		// The header of the deltaTime-parameter
		String deltaTime = ":";
		for (int i=0; i<nrOfTics; i++)
			deltaTime += "\t" + i;
		deltaTime += " := ";

		w.newLine();
		w.write("param deltaTime default 0");
		w.newLine();
		w.write(deltaTime);
		w.newLine();
		
		for (int i=0; i<nrOfRobots; i++) 
		{
			int currTic = 0;
			deltaTime = "" + i + "\t0";
			
			ArrayList<State> currStates = new ArrayList<State>();
			ArrayList<State> closedStates = new ArrayList<State>();
					
			Automaton currAuto = robots.getAutomatonAt(i);
			currStates.add(currAuto.getInitialState());

			while (!currStates.isEmpty())
			{		
				State currState = currStates.remove(0);				
				StateIterator nprecedenceConstraintsextStates = currState.nextStatesIterator();

				closedStates.add(0, currState);

				while (nextStates.hasNext())
				{
					State nextState = nextStates.next();
					
					if (!nextStates.isAccepting())
					{
						if (!closedStates.contains(nextState))
						{
							deltaTimes += "\t" + nextState.getCost();
							currTic++;
						}

						int parentTick = closedStates.size();
						precedenceConstraints += "time[" + i + ", " + currTic + "] >= time [" + i + ", " + parentTick + "] deltaTime[" + i + ", " +  parentTick + "]";
						
					}
					currStates.add(nextState);
				}

				deltaTime += "\t" + currState.getCost();

			}
			
		}

		String[] deltaTimes = new String[nrOfRobots + 1];
		deltaTimes[0] = ":\t0";
		for (int j=0; j<nrOfRobots; j++) 
		{
			deltaTimes[j] += j + "\t0";
		}
		for (int i=1; i<nrOfTics; i++)
		{
			deltaTime[0] += "\t" + i;
			
			for (int  j=0; j<nrOfRobots; j++) 
			{
				deltaTimes[j] += j + "\t0";
			}
		}
		deltaTimes[0] += " :=";
// 		w.write(":\t0\t1\t2\t3\t4\t5 :=");
// 		w.newLine();
// 		w.write("0\t0\t79\t65\t394\t29\t433");
// 		w.newLine();
// 		w.write("1\t0\t198\t411\t389\t0\t2 ;");
// 		w.newLine();

		w.newLine();
		w.write("end;");
		w.flush();
	}

}