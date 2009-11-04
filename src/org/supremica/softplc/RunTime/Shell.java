package org.supremica.softplc.RunTime;

import java.util.Timer;
import java.util.TimerTask;
import org.supremica.softplc.CompILer.CodeGen.IEC_Interfaces.*;
import java.lang.reflect.*;

/**
 * Schedule a task that executes once every x ms.
 */
public class Shell
{
	public Timer timer;
	public static String ioclass = new String();
	public static String ilclass = new String();
	public boolean[] inSignals;
	public boolean[] outSignals;
	static short nr_Of_Signals_In;
	static short nr_Of_Signals_Out;
	public static DigitalIODriver driver;

	/** execute once every [interval] ms */
	static short interval = 40;
	Constructor<?> classConstructor;

	public Shell(String io, String path, String name)
	{
		if ((io.length() > 0) && (name.length() > 0))
		{
			Class<?> IOClass;

			try
			{
				System.out.println("IO Class: " + io);

				IOClass = Class.forName(io);
				driver = (DigitalIODriver) IOClass.newInstance();
				nr_Of_Signals_In = driver.getNrOfSignalsIn();
				nr_Of_Signals_Out = driver.getNrOfSignalsOut();
				inSignals = new boolean[nr_Of_Signals_In];
				outSignals = new boolean[nr_Of_Signals_Out];
			}
			catch (Exception e)
			{
				e.printStackTrace();
				System.err.println("3: " + e);
				System.exit(-1);
			}

			ILShell(path, name);
		}
		else if ((io.length() > 0) && (name.length() == 0))
		{
			try
			{
				DigitalIODisplayView frame = new DigitalIODisplayView(io);

				frame.pack();
				frame.setVisible(true);
			}
			catch (Exception e)
			{
				System.err.println(e);
			}
		}
	}

	public void ILShell(String path, String name)
	{
		Class<?> ILClass;
		Class<?>[] constructorArgumentTypes = { boolean[].class,
											 boolean[].class };

		try
		{
			org.supremica.util.FileClassLoader loader = new org.supremica.util.FileClassLoader(path);

			ILClass = loader.loadClass(name);
			classConstructor = ILClass.getConstructor(constructorArgumentTypes);
		}
		catch (Exception e)
		{
			System.err.println("1: " + e);
			System.err.println(e.getMessage());
		}

		timer = new Timer();

		timer.schedule(new ILTask(inSignals, outSignals), 0,    // initial delay
					   1 * interval);    // subsequent rate
	}

	class ILTask
		extends TimerTask
	{
		private IEC_Program il_program;

		public ILTask(boolean[] inputSignals, boolean[] outputSignals)
		{
			try
			{
				il_program = (IEC_Program) classConstructor.newInstance(new Object[]{ inputSignals,
																					  outputSignals });
			}
			catch (Exception exc)
			{
				exc.printStackTrace();
				System.err.println("2: " + exc.getMessage());
			}
		}

		public void run()
		{
			if (System.currentTimeMillis() - scheduledExecutionTime() >= interval * 0.01)
			{
				System.err.println("=============================");
				System.err.println("The time limit was exceeded!");
				System.err.println("=============================");
			}

			try
			{
				driver.getSignalArray(inSignals);
			}
			catch (Exception t)
			{
				System.err.println("4: " + t);
			}

			il_program.run();

			try
			{
				driver.setSignalArray(outSignals);
			}
			catch (Exception t)
			{
				System.err.println("4: " + t);
			}

			System.out.println("run() körs!");

			// timer.cancel(); //Not necessary because we call System.exit
			// System.exit(0);   //Stops the AWT thread
		}
	}

	// används bara nu när vi kör från prompt
	public static int process_args(String args[])
	{
		int i;
		int type = 0;

		for (i = 0; i < args.length; i += 2)
		{
			if (args[i].equals("-IO"))
			{
				ioclass = args[i + 1];
				type += 1;
			}
			else if (args[i].equals("-IL"))
			{
				ilclass = args[i + 1];
				type += 2;
			}
		}

		return type;
	}

	// används bara nu när vi kör från prompt
	public static void main(String args[])
	{
		process_args(args);
		new Shell(ioclass, ".", ilclass);

		/*
		 *  case 1:
		 *       try {
		 *               DigitalIODisplayView frame = new DigitalIODisplayView(ioclass);
		 *               frame.setVisible(true);
		 *       }
		 */
	}
}
