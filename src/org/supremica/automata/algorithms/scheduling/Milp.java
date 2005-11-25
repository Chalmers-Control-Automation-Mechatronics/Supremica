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

	// Puts the *.mod file into ~/dist
	private final String FILE_ROOT = "temp";

// 	private final String MODEL_FILE_NAME = "C:\\Documents and Settings\\avenir\\Desktop\\temp.mod";
//  	private final String MODEL_FILE_NAME = "C:\\Documents and Settings\\Avenir\\Skrivbord\\temp.mod";

	private Automata theAutomata, robots, zones;

	/** int[zone_nr][robot_nr][state_nr] - stores the states that fire booking/unbooking events */
	private int[][][] bookingTics, unbookingTics;

	/** Ordered info that allows to build the optimal schedule - stores [robot_index, state_index, state_time] */
	private TreeSet<int[]> scheduleInfo;

	private int makespan;
	
	/** The *.mod file that serves as an input to the Glpk-solver */
	private File modelFile;

	/** The *.sol file that stores the solution, i.e. the output of the Glpk-solver */
	private File solutionFile;

	public Milp(Automata theAutomata) 
		throws Exception
	{
		// SÅ SKALL DET VARA NÄR ALLTING ÄR KLART (OM), MEN NU... FÖR ATT UNDERLÄTTA...
// 		modelFile = File.createTempFile("milp", ".mod");
// 		modelFile.deleteOnExit();

// 		solutionFile = File.createTempFile("milp", ".sol");
// 		solutionFile.deleteOnExit();

		// ... SÅ HÄR ISTÄLLET...
		modelFile = new File("C:\\avenir\\progg\\milp3.mod");
		solutionFile = new File("C:\\avenir\\progg\\milp3.sol");
		modelFile.createNewFile();
		solutionFile.createNewFile();

		initAutomata(theAutomata);
		initMutexStates();

		convertXmlToMod();

		//		new GlpkBridge().bridge("temp");
		callGlpk();

		convertSolToXml();
	}

	public int[] schedule()
		throws Exception
	{
		if (scheduleInfo == null)
			throw new Exception("ScheduleInfo is empty. Something must have gone wrong during optimization.....");
			
		return scheduleInfo.last();
	}

	public Automaton buildScheduleAutomaton(int[] markedNode) 
		throws Exception
	{
		if (scheduleInfo == null)
			throw new Exception("ScheduleInfo is empty. Something must have gone wrong during optimization.....");
		
		Automaton schedule = new Automaton();
		schedule.setComment("Schedule");

// 		// Tillf 
// 		int counter = 0;





// 		ArrayList<int[]> misplacedInfos = new ArrayList<int[]>();
// 		int[] activeStates = new int[robots.size()];

// 		State trailState = new State("q" + counter++);


		// TILLF - FLUM
		State trailState = new State("INITIAL");
		trailState.setInitial(true);
		schedule.addState(trailState);

		State accState = new State("ACCEPTING");
		accState.setAccepting(true);
		schedule.addState(accState);

		LabeledEvent event = new LabeledEvent("42");
		schedule.getAlphabet().addEvent(event);
		schedule.addArc(new Arc(trailState, accState, event));

		logger.info("TOTAL CYCLE TIME: " + makespan + ".............................");

		return schedule;
		
// 		for (Iterator<int[]> infoIterator = scheduleInfo.iterator(); infoIterator.hasNext(); )
// 		{
// 			int[] currInfo = infoIterator.next();

// 			// if the initial state of the current robot has not yet been examined
// 			if (activeStates[currInfo[0]] == null)
// 			{
// 				// if the current state is the initial state of the current robot
// 				if (robots.getAutomatonAt(currInfo[0]).getInitialState().getIndex() == currInfo[1])
// 				{
// 					State newState = new State("q" + counter++);
// 					newState.setCost(currInfo[2]);

// 					schedule.addState(newState);
// 					activeStates[currInfo[0]] = 

// 					// if the current state is the first initial state to be examined
// 					if (trailState == null)
// 					{
// 						newState.setInitial(true);
// 						trailState = newState;
// 					}
// 					else
// 					{
						
// 					}
// 				}
// 					;// Add the initial state
// 				else
// 					misplacedInfos.add(currInfo);
// 			}
// 		}
		














// 		Iterator<int[]> scheduleInfoIterator = scheduleInfo.iterator(); 
// // 		ArrayList<int[]> equalCostInfos = new ArrayList<int[]>();
// // 		equalCostInfos.add(scheduleInfoIterator.next());
// // 		int currentCost = equalCostInfos.get(0)[2];
// // 		State lastState = null;

// 		ArrayList<int[]> sleepingInfos = new ArrayList<int[]>();

// 		ArrayList<int[]> enabledInfosArray = new ArrayList<int[]>();
// 		for (int i=0; i<robots.size(); i++)
// 			int[] enabledInfo = new int[]{i, robots.getAutomatonAt(i).getInitialState().getIndex()};

// 		while(scheduleInfoIterator.hasNext())
// 		{
// 			int[] currInfo = scheduleInfoIterator.next();
			
// // 			if (infoInArrayList(enabledInfos, currInfo));

// 			for (Iterator enabledInfos = enabledInfosArray.iterator(); enabledInfos.hasNext(); )
// 			{
// 				int[] currEnabledInfo = enabledInfos.next();
				
// 				if (currEnabledInfo[0] == currInfo[0] && currEnabledInfo[1] == currInfo[1])
// 				{
// 					enabledInfos.remove();
					
					
// 				}
// 			}
			
// // 			if (currInfo[2] != currentCost)
// // 			{
// // 				currentCost = currInfo[2];

// // 				// process ArrayList
				
				

// // 				equalCostInfos.clear();
// 			}

// // 			equalCostInfos.add(currInfo);
// 		}

// 		return null;
	}

// 	private boolean infoInArrayList(ArrayList<int[]> arrayList, int[] info)
// 	{	
// 		for (Iterator infos = arrayList.iterator(); infos.hasNext(); )
// 		{
// 			int[] currArrayInfo = infos.next();

// 			if (currArrayInfo[0] == info[0] && currArrayInfo[1] == info[1])
// 				return true;
// 		}
// 	}

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

		// The string containing initial (precedence) constraints
		String initPrecConstraints = "";

		// The string containing mutex constraints
		String mutexConstraints = "";

		// The string containing mutex variables
		String mutexVariables = "";

		// The string containg alternative paths constraints
		String altPathsConstraints = "";
		
		// The string containing alternative paths variables
		String altPathsVariables = "";

		// The string containing the cycle time constraints
		String cycleTimeConstraints = "";
		
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
				
				// If the current state has successors and is not initial, add precedence constraints
				// If the current state is initial, add an initial (precedence) constraint
				int nbrOfOutgoingArcs = currState.nbrOfOutgoingArcs();
				if (nbrOfOutgoingArcs > 0)
				{
					if (currState.isInitial())
					{
						initPrecConstraints += "initial_" + currRobot.getName() + "_" + currState.getName() + " : ";
						initPrecConstraints += "time[" + i + ", " + currState.getIndex() + "] >= deltaTime[" + i + ", " + currState.getIndex() + "];\n";
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
						State nextLeftState = nextStates.nextState();
						State nextRightState = nextStates.nextState();

						String currAltPathsVariable = currRobot.getName() + "_goes_from_" + currState.getName() + "_to_" + nextLeftState.getName();
								
						altPathsVariables += "var " + currAltPathsVariable + ", binary;\n";

						altPathsConstraints += "alt_paths_" + currRobot.getName() + "_" + currState.getName() + " : "; 
						altPathsConstraints += "time[" + i + ", " + nextLeftState.getIndex() + "] >= time[" + i + ", " + currState.getIndex() + "] + deltaTime[" + i + ", "  + nextLeftState.getIndex() + "] - bigM*" + currAltPathsVariable + ";\n";

						altPathsConstraints += "dual_alt_paths_" + currRobot.getName() + "_" + currState.getName() + " : "; 
						altPathsConstraints += "time[" + i + ", " + nextRightState.getIndex() + "] >= time[" + i + ", " + currState.getIndex() + "] + deltaTime[" + i + ", "  + nextRightState.getIndex() + "] - bigM*(1 - " + currAltPathsVariable + ");\n";
					}
					else if (nbrOfOutgoingArcs > 2)
					{
						int currAlternative = 0;
						
						while (nextStates.hasNext())
						{
							String currAltPathsVariable = currRobot.getName() + "_" + currState.getName() + "_path_" + currAlternative;
							State nextState = nextStates.nextState();

							altPathsVariables += "var " + currAltPathsVariable + ", binary;\n";

							altPathsConstraints += "alt_paths_" + currAltPathsVariable + " : ";
							altPathsConstraints += "time[" + i + ", " + nextState.getIndex() + "] >= time[" + i + ", " + currState.getIndex() + "] + deltaTime[" + i + ", "  + nextState.getIndex() + "] - bigM*(1 - " + currAltPathsVariable + ");\n";

							currAlternative++;
						}

						altPathsConstraints += "alt_paths_" + currRobot.getName() + "_" + currState.getName() + "_TOT : ";
						for (int k=0; k<currAlternative - 1; k++)
						{
							altPathsConstraints += currRobot.getName() + "_" + currState.getName() + "_path_" + k + " + ";
						}
						altPathsConstraints += currRobot.getName() + "_" + currState.getName() + "_path_" + (currAlternative - 1) + " = 1;\n";

						// Implement t1 >= t0 + delta_t0 - M*alt0; t1_prim >= t0 + delta_t0 - M*alt0_prim; 
						// t1_prim_prim >= t0 + delta_t0 - M*alt0_prim_prim; alt0 + alt0_prim + alt0_prim_prim = 1;
					}
				}
				else if (currState.isAccepting())
				{   
					// If the current state is accepting, a cycle time constaint is added,
					// ensuring that the makespan is at least as long as the minimum cycle time of this robot
					cycleTimeConstraints += "cycle_time_" + currRobot.getName() + " : c >= " + "time[" + i + ", " + currState.getIndex() + "];\n";
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
		// for every zone...
		for (int i=0; i<bookingTics.length; i++)
		{
			// for every robot pair...
			for (int j1=0; j1<bookingTics[i].length-1; j1++)
			{
				for (int j2=j1+1; j2<bookingTics[i].length; j2++)
				{
					// updates the variable index if the booking event is repeated;
					int repeatedBooking = 0;

					// for every path combination that contains the event that books the current zone
					for (int k1=0; k1<bookingTics[i][j1].length; k1++)
					{
						for (int k2=0; k2<bookingTics[i][j2].length; k2++)
						{
							if (bookingTics[i][j1][0] != -1 && bookingTics[i][j2][0] != -1)
							{
								repeatedBooking++;

								String currMutexVariable = "r" + j1 + "_books_z" + i + "_before_r" + j2 + "_var" + repeatedBooking;
								
								mutexVariables += "var " + currMutexVariable + ", binary;\n";
								
								mutexConstraints += "mutex_Z" + i + "_R" + j1 + "_R" + j2 + "_var" + repeatedBooking + " : time[" + j1 + ", " + bookingTics[i][j1][k1] + "] >= " + "time[" + j2 + ", " + unbookingTics[i][j2][k2] + "] - bigM*" + currMutexVariable + ";\n";
								mutexConstraints += "dual_mutex_Z" + i + "_R" + j1 + "_R" + j2  + "_var" + repeatedBooking + " : time[" + j2 + ", " + bookingTics[i][j2][k2] + "] >= " + "time[" + j1 + ", " + unbookingTics[i][j1][k1] + "] - bigM*(1 - " + currMutexVariable + ");\n";
							}
						}
					}
				}
			}
		}


		////////////////////////////////////////////////////////////////////////////////////
		//	                          The writing part                                    //
		////////////////////////////////////////////////////////////////////////////////////

// 		if (!modelFile.exists())
// 			modelFile.createNewFile();

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
		w.write("var time{r in Robots, t in Tics};"); // >= 0;");
		w.newLine();
		w.write("var c;");
		w.newLine();
		w.write(altPathsVariables);
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
		w.write(cycleTimeConstraints);
// 		w.write("cycle_time{r in Robots}: c >= time[r, maxTic];");
// 		w.newLine();
		
		// The initial (precedence) constraints
		w.newLine();
		w.write(initPrecConstraints);

		// The precedence constraints
		w.newLine();
		w.write(precConstraints);
		
		// The alternative paths constraints
		w.newLine();
		w.write(altPathsConstraints);

		// The mutex constraints
		w.newLine();
		w.write(mutexConstraints);
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
		w.write("param bigM := " + Short.MAX_VALUE + ";");
		w.newLine();
		// Behovs maxTic verkligen???
		w.write("param maxTic := " + (nrOfTics - 1) + ";");
		w.newLine();
			
		w.newLine();
		w.write(deltaTime);
		w.newLine();

		w.newLine();
		w.write("end;");
		w.flush();
	}
	
	private void convertSolToXml()
		throws Exception
	{
// 		scheduleInfo = new TreeSet<int[]>(new CostComparator(robots));
// 		scheduleInfo = new TreeSet[robots.size()];
// 		for (int i=0; i<scheduleInfo.length; i++)
// 			scheduleInfo[i] = new TreeSet<int[]>(new CostComparator(robots.getAutomatonAt(i)));

		scheduleInfo = new TreeSet<int[]>(new CostComparator());

// 		File solutionFile = new File(FILE_ROOT + ".sol");

		if (!solutionFile.isFile())
			throw new Exception(solutionFile + " was NOT FOUND..........");

		BufferedReader r = new BufferedReader(new FileReader(solutionFile));
		String str = r.readLine();

		while (str != null)
		{
			// For every solution line containing positive time value...
			if (str.indexOf(" time[") > -1)
			{
				String strRobotIndex = str.substring(str.indexOf("[") + 1, str.indexOf(",")).trim();
				String strStateIndex = str.substring(str.indexOf(",") + 1, str.indexOf("]")).trim();
				String strCost = str.substring(str.indexOf("]") + 1).trim(); 
			
				int robotIndex = (new Integer(strRobotIndex)).intValue(); 
				int stateIndex = (new Integer(strStateIndex)).intValue(); 
 				int cost = (new Integer(strCost)).intValue();

				int[] currScheduleInfo = new int[]{robotIndex, stateIndex, cost};
// 				scheduleInfo[robotIndex].add(currScheduleInfo);
				scheduleInfo.add(currScheduleInfo);
			}
			else if (str.indexOf("c ") >  -1)
			{
				String strMakespan = str.substring(str.indexOf("c") + 1).trim();
				makespan = (new Integer(strMakespan)).intValue();
			}
				
			str = r.readLine();
		}


// 		for (int i=0; i<robots.size(); i++)
// 		{
// 			logger.warn("Robot " + i);
// 			for (Iterator<int[]> iter = scheduleInfo[i].iterator(); iter.hasNext(); )
// 			{
// 				int[] temp = iter.next();
// 				logger.info("tree -> " + temp[0] + " " + temp[1] + " " + temp[2]);
// 			}
// 		}

		/*
		for (Iterator<int[]> iter = scheduleInfo.iterator(); iter.hasNext(); )
		{
			int[] temp = iter.next();
			logger.info("tree -> " + temp[0] + " " + temp[1] + " " + temp[2]);
		}
		*/
	}

	private void callGlpk()
		throws Exception
	{
		// Defines the name of the .exe-file as well the arguments (.mod and .sol file names)
		String[] cmds = new String[5];
		cmds[0] = "C:\\Program Files\\glpk\\bin\\glpsol.exe";
		cmds[1] = "-m";
		cmds[2] = modelFile.getAbsolutePath();
		cmds[3] = "-o";
		cmds[4] = solutionFile.getAbsolutePath();

		// Runs the MILP-solver with the arguments defined above
		Process milpProcess = Runtime.getRuntime().exec(cmds);

		// Listens for the output of MILP (that is the input to this application)...
		BufferedReader milpEcho = new BufferedReader(new InputStreamReader(new DataInputStream(milpProcess.getInputStream())));

		// ...and prints it to stdout
		logger.warn("MILP output starts ");
		String milpEchoStr;
		while ((milpEchoStr = milpEcho.readLine()) != null)
		{
			logger.info(milpEchoStr);
		}
		logger.warn("MILP output ends");
	}
}

class CostComparator 
	implements Comparator<int[]>
{
	/** Helps to put the initial states at the beginning of the schedule in case of ties - initialIndice[robotIndex] = state_index */
//  	private int[] initialIndices, acceptingIndices;
// 	private int initialIndex;

// 	public CostComparator(Automaton robot)
// 	{
// 		initialIndex = robot.getInitialState().getIndex();
// 		initialIndices = new int[theAutos.size()];

// 		for (int i=0; i<initialIndices.length; i++)
// 		{
// 			initialIndices[i] = theAutos.getAutomatonAt(i).getInitialState().getIndex();
// 			acceptingIndices[i] = theAutos.getAutomatonAt(i).getInitialState().getIndex();
// 	}
	
	/**
	 * Comparison is done according to the (time)cost of each stateInfo.
	 * If the cost is equal, the new state (a) is said to be smaller than the old one (b)
	 * to increase the comparison (somewhat).
	 */
	public int compare(int[] a, int[] b)
	{
		if (a[2] == b[2])
		{
			if (a[0] == b[0] && a[1] == b[1])
				return 0;
// 			else if (b[0] == initialIndex)
// 				return 1;
			else
				return -1;  
		}

		return a[2] - b[2];
	}

	/**
	 * Returns true if b corresponds to an initial state.
	 */
// 	private boolean isInitial(int[] b)
// 	{
// 	 	if (initialIndices[b[0]] == b[1])
// 			return true;

// 		return false;
// 	}
}