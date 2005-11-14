package org.supremica.automata.algorithms.scheduling;

import java.util.*;
import java.io.*;

import org.supremica.log.*;
import org.supremica.automata.*;

public class Milp 
	implements Scheduler
{
	private static Logger logger = LoggerFactory.createLogger(Milp.class);

	private final String MODEL_FILE_NAME = "C:\\Documents and Settings\\avenir\\Desktop\\temp.mod";

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

logger.info("aha");
		int nrOfRobots = robots.size();
		int nrOfZones = zones.size();
		logger.info("oho");
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
		w.write("param deltaTime{r in Robots, t in Tics};");
		w.newLine();

		// Definitions of sets
		w.newLine();
		w.write("set Robots := 0..nrOfRobots");
		w.newLine();
		w.write("set Zones := 0..nrOfZones");
		w.newLine();
		w.write("set Tics := 0..maxTic");
		w.newLine();

		// Definitions of variables
		w.newLine();
		w.write("var time{r in Robots, t in Tics} >= 0;");
		w.newLine();
		w.write("var c");
		w.newLine();

		// The objective function
		w.newLine();
		w.write("minimize makespan: c;");
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

		w.newLine();
		w.write("nrOfRobots := " + nrOfRobots + ";");
		w.newLine();
		w.write("nrOfZones := " + nrOfZones + ";");
		w.newLine();
		
		// This part should be automatized
		w.write("maxTic := 5;");
		w.newLine();
		w.write("param deltaTime : 	0	1	2	3	4 	5:=");
		w.newLine();
		w.write("\t 0	0	79	65	394	29	433");
		w.newLine();
		w.write("\t 1	0	198	411	389	0	2 ;");
		w.newLine();

		w.newLine();
		w.write("end;");
		w.flush();
	}

}