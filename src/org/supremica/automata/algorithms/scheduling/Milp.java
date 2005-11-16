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
//  	private final String MODEL_FILE_NAME = "C:\\Documents and Settings\\Avenir\\Skrivbord\\temp.mod";

	private Automata theAutomata, robots, zones;

	/** int[zone_nr][robot_nr][state_nr] - stores the states that fire booking/unbooking events */
	private int[][][] bookingTics, unbookingTics;

//  	public native void jniTest();
	
	public Milp(Automata theAutomata) 
		throws Exception
	{
		logger.info("In the Milp.java");

		initAutomata(theAutomata);
logger.info("initAutomattat");
		initMutexStates();
logger.info("initMutexat");
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

	/**
	 * Goes through the supplied automata and synchronizes all specifications 
	 * that do not represent zones with corresponding robot automata. 
	 * This assumes that robots and the specifications regulating their behavior 
	 * have similar roots. For example if robot.name = "ROBOT_A", the specification.name
	 * should include "ROBOT_A". The resulting robot and zone automata are stored globally.
	 */
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

		for (int i=0; i<robots.size(); i++)
			robots.getAutomatonAt(i).remapStateIndices();

		for (int i=0; i<zones.size(); i++)
			zones.getAutomatonAt(i).remapStateIndices();
	}

	private void initMutexStates()
		throws Exception
	{
		bookingTics = new int[zones.size()][robots.size()][1];
		unbookingTics = new int[zones.size()][robots.size()][1];

		// Initializing all book/unbook-state indices to -1. 
		// The ones that remain -1 at the output of this method correspond 
		// to non-conflicting robot-zone-pairs. 
		for (int i=0; i<zones.size(); i++)
		{
			for (int j=0; j<robots.size(); j++) 
			{
				bookingTics[i][j][0] = -1;
				unbookingTics[i][j][0] = -1;
			}
		}

		for (int i=0; i<zones.size(); i++) 
		{
			State initial = zones.getAutomatonAt(i).getInitialState();

			ArcIterator bookingArcs = initial.outgoingArcsIterator();

			while (bookingArcs.hasNext())
			{
				Arc bookingArc = bookingArcs.nextArc();
				Arc unbookingArc = initial.nextState(bookingArc.getEvent()).outgoingArcsIterator().nextArc();
		
				// The robot set is searched for the states from which the above book/unbook events can be fired
				for (int j=0; j<robots.size(); j++)
				{
					ArrayList<State> bookingStates = new ArrayList<State>();
					ArrayList<State> unbookingStates = new ArrayList<State>();
					Automaton currRobot = robots.getAutomatonAt(j);

					// The following is done only for the robot that contains the corresponding booking-event
					if (currRobot.getAlphabet().contains(unbookingArc.getEvent()))
					{
						// States that can lead directly to unbooking-events are found
						for (ArcIterator robotArcs = currRobot.arcIterator(); robotArcs.hasNext(); )
						{
							Arc currArc = robotArcs.nextArc();

							if (unbookingArc.getLabel().equals(currArc.getLabel()))
								unbookingStates.add(currArc.getFromState());
						}

						// For each "unbooking"-state, a search up the robot is done until corresponding 
						// "booking"-states are found. Note that one "u"-state can correspond to several "b"-states.
						for (int k=0; k<unbookingStates.size(); k++)
						{
							// If there are alternative paths to the current state, several booking states will be found.
							// They should be matched by equal number of unbooking states.
							int alternativeBookings = 0;

							ArrayList<State> upstreamStates = new ArrayList<State>();
							upstreamStates.add(unbookingStates.get(k));
							
							while (!upstreamStates.isEmpty())
							{
								State currState = upstreamStates.remove(0);
								
								// Every roadsplit is an alternative. 
								alternativeBookings += (currState.nbrOfIncomingArcs() - 1);
						
								for (ArcIterator incomingArcs = currState.incomingArcsIterator(); incomingArcs.hasNext(); )
								{
									Arc currArc = incomingArcs.nextArc();
									
									if (bookingArc.getLabel().equals(currArc.getLabel()))
									{
										State candidateBookingState = currArc.getFromState();

										// If the alternative path is ending in an already examined booking state, 
										// this path was not really an alternative (since several instances of a state are 
										// not necessary).
										if (!bookingStates.contains(candidateBookingState))
											bookingStates.add(candidateBookingState);
										else
										{
											logger.error(candidateBookingState.getName() + " is ALREADY checked");
											alternativeBookings--;
										}
									}
									else
										upstreamStates.add(currArc.getFromState());
								}

								// A copy of the current unbooking state is added for every alternative path 
								// between the unbook state and the book states.
								for (int l=0; l<alternativeBookings; l++)
									unbookingStates.add(k, unbookingStates.get(k));
							}
						}

						bookingTics[i][j] = new int[bookingStates.size()];
						unbookingTics[i][j] = new int[unbookingStates.size()];

						if (bookingTics[i][j].length != unbookingTics[i][j].length)
							throw new Exception("The numbers of book/unbook-states do not correspond. Something is wrong....");

						for (int k=0; k<bookingTics[i][j].length; k++)
						{
							bookingTics[i][j][k] = bookingStates.get(k).getIndex();
							unbookingTics[i][j][k] = unbookingStates.get(k).getIndex();
						}

						// This assumes that each zone-event is used by exactly one robot
						break;
					}
				}			
			}
		}
	}

	private void convertXmlToMod() 
		throws Exception
	{	
		int nrOfRobots = robots.size();
		int nrOfZones = zones.size();

		// The string containing precedence constraints 
		String precConstraints = "";

		// The string containing mutex constraints
		String mutexConstraints = "";

		// The string containing mutex variables
		String mutexVariables = "";
		
		// The string containing times for each state (delta-times)
		String deltaTime = "param deltaTime default 0\n:";


		////////////////////////////////////////////////////////////////////////////////////
		//	                          The constructing part                               //
		////////////////////////////////////////////////////////////////////////////////////

		// Finding maximum number of time variables per robot (i.e. max nr of states)
		int nrOfTics = 0;
		for (int i=0; i<nrOfRobots; i++)
		{
			int nbrOfStates = robots.getAutomatonAt(i).nbrOfStates();
			if (nbrOfStates > nrOfTics)
				nrOfTics = nbrOfStates;
		}
// 		nrOfTics--;

		// Making deltaTime-header
		for (int i=0; i<nrOfTics; i++)
			deltaTime += "\t" + i;

		deltaTime += " :=\n";

		// Extracting deltaTimes for each robot
		for (int i=0; i<nrOfRobots; i++) 
		{
			deltaTime += i;
			
			Automaton currRobot = robots.getAutomatonAt(i);

			// Each index correspond to a Tic. For each Tic, a deltaTime is added
			// (StateIterator gives wrong indices).
			for (int j=0; j<currRobot.nbrOfStates(); j++) 
			{
				State currState = currRobot.getStateWithIndex(j);

				deltaTime += "\t" + currState.getCost();
				
				// If the current state has successors, add precedence constraints
				int nbrOfOutgoingArcs = currState.nbrOfOutgoingArcs();
				if (nbrOfOutgoingArcs > 0)
				{
					if (currState.isInitial())
					{
						precConstraints += "initial_" + currRobot.getName() + "_" + currState.getName() + " : ";
						precConstraints += "time[" + i + ", " + currState.getIndex() + "] >= deltaTime[" + i + ", " + currState.getIndex() + "];\n";
					}
					
					StateIterator nextStates = currState.nextStateIterator();
					
					if (nbrOfOutgoingArcs == 1) 
					{
						State nextState = nextStates.nextState();
						
						precConstraints += "prec_" + currRobot.getName() + "_" + currState.getName() + "_" + nextState.getName() + " : ";
						precConstraints += "time[" + i + ", " + nextState.getIndex() + "] >= time[" + i + ", " + currState.getIndex() + "] + deltaTime[" + i + ", " + nextState.getIndex() + "];\n";
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
				}
			}

			// If the number of states of the current automaton is less 
			// than max_nr_of_states, the deltaTime-matrix is filled with points 
			// i.e zeros. 
			for (int j=currRobot.nbrOfStates(); j<nrOfTics; j++)
				deltaTime += "\t.";

			if (i == nrOfRobots - 1)
				deltaTime += ";";

			deltaTime += "\n";
		}

		// Constructing the mutex constraints
		for (int i=0; i<bookingTics.length; i++)
		{
			for (int j1=0; j1<bookingTics[i].length-1; j1++)
			{
				for (int j2=j1+1; j2<bookingTics[i].length; j2++)
				{
					for (int k1=0; k1<bookingTics[i][j1].length; k1++)
					{
						for (int k2=0; k2<bookingTics[i][j2].length; k2++)
						{
							if (bookingTics[i][j1][0] != -1 && bookingTics[i][j2][0] != -1)
							{
// 								String currMutexVariable = "mutex_Z" + i + "_R" + j1 + "_R" + j2;
								String currMutexVariable = "r" + j1 + "_books_z" + i + "_before_r" + j2;
								
								mutexVariables += "var " + currMutexVariable + ", binary;\n";
								
								mutexConstraints += "mutex_Z" + i + "_R" + j1 + "_R" + j2 + " : time[" + j1 + ", " + bookingTics[i][j1][k1] + "] >= " + "time[" + j2 + ", " + unbookingTics[i][j2][k2] + "] - bigM*" + currMutexVariable + ";\n";
								mutexConstraints += "dual_mutex_Z" + i + "_R" + j1 + "_R" + j2 + " : time[" + j2 + ", " + bookingTics[i][j2][k2] + "] >= " + "time[" + j1 + ", " + unbookingTics[i][j1][k1] + "] - bigM*(1 - " + currMutexVariable + ");\n";
							}
						}
					}
				}
			}
		}


		////////////////////////////////////////////////////////////////////////////////////
		//	                          The writing part                                    //
		////////////////////////////////////////////////////////////////////////////////////

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
		w.write("param bigM;");
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
		w.write(mutexVariables);
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
		
		// The mutex constraints
		w.newLine();
		w.write(mutexConstraints);
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
		w.write("param bigM := " + Short.MAX_VALUE + ";");
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