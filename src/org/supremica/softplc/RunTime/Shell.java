package org.supremica.softplc.RunTime;

import java.util.Timer;
import java.util.TimerTask;
import org.supremica.softplc.CompILer.CodeGen.IEC_Interfaces.*;

import java.util.*;
import java.lang.reflect.*;
import java.text.*;

//import org.supremica.softplc.Simulator.ThirdTest.*;

/**
 * Schedule a task that executes once every 100ms.
 */

public class Shell {
    public Timer timer;
	public IEC_Program il_program;
	public static String ioclass = new String();
	public static String ilclass = new String();

	public boolean[] inSignals = new boolean[32];
	public boolean[] outSignals = new boolean[32];

	public static short nr_Of_Signals_In;
    public static short nr_Of_Signals_Out;
	public static DigitalIODriver driver;
	public long last_invocation = 0;

	// execute once every 100ms
	public int interval = 100;

	Class[] constructorArgumentTypes = { boolean[].class, boolean[].class };
	Constructor classConstructor;
	Object[] constructorArgs = {inSignals,outSignals};

	Class ILClass;

	public boolean isInitialized = false;

	public Shell(String io, String il) {
		Class IOClass;

		try	{
			System.out.println(io);
			IOClass = Class.forName(io);
			driver = (DigitalIODriver) IOClass.newInstance();
			nr_Of_Signals_In = driver.getNrOfSignalsIn();
			nr_Of_Signals_Out = driver.getNrOfSignalsOut();
		}
		catch (Exception e) {
			e.printStackTrace();
			System.err.println("3: " + e);
			System.exit(-1);
		}

		//new Shell(il);
		ILShell(il);

	}

    public void ILShell(String dynClass) {

		try	{
			System.out.println("***********************");
			System.out.println(dynClass);
			ILClass = Class.forName(dynClass);
			classConstructor = ILClass.getConstructor(constructorArgumentTypes);
		}
		catch (Exception e) {
			System.out.println("***********************");
			System.out.println(dynClass);
			System.out.println("***********************");
			System.err.println("1: " + e);
			System.exit(-1);
		}

        timer = new Timer();
        timer.schedule(new ILTask(),
					   0,        //initial delay
					   1*interval);  //subsequent rate
    }

    class ILTask extends TimerTask {
        public void run() {
			if (System.currentTimeMillis() - scheduledExecutionTime() >= interval*0.01) {
				System.err.println("=============================");
				System.err.println("The time limit was exceeded!");
				System.err.println("=============================");
			}

			if (!isInitialized) {
				try {
					il_program = (IEC_Program) classConstructor.newInstance(constructorArgs);
				}
				catch (Exception exc) {
					System.err.println("2: " + exc);
					System.exit(-1);
				}
				isInitialized = true;
			}

			try {
				driver.getSignalArray(inSignals);
			}
			catch(Exception t) { System.err.println("4: " + t); }

			il_program.run();

			try {
				driver.setSignalArray(outSignals);
			}
			catch(Exception t) { System.err.println("4: " + t); }

			System.out.println("run() körs!");
			//timer.cancel(); //Not necessary because we call System.exit
			//System.exit(0);   //Stops the AWT thread
		}
	}

	// används bara nu när vi kör från prompt
	public static int process_args(String args[]) {
		int i;
		int type = 0;

		for (i=0;i<args.length;i+=2) {
			if (args[i].equals("-IO")) {
				ioclass = args[i+1];
				type += 1;
			}
			else if (args[i].equals("-IL")) {
				ilclass = args[i+1];
				type += 2;
			}
		}
		return type;
	}

	// används bara nu när vi kör från prompt
    public static void main(String args[]) {

		process_args(args);
		new Shell(ioclass,ilclass);

		/*  case 1:
			try {
				DigitalIODisplayView frame = new DigitalIODisplayView(ioclass);
				frame.setVisible(true);
			}
		*/
    }

}

