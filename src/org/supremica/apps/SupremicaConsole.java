
// try to get rid of the Swing GUI
// old unix style user interface here :)
package org.supremica.apps;

import org.supremica.gui.Gui;
import org.supremica.console.*;

public class SupremicaConsole
{
	private static ConsoleUI gui = null;

	private SupremicaConsole() {}

	private static void doSplash()
	{
		System.err.println("------- Supremica console -----------");
	}

	private static void startSupremicaConsole(String[] args)
	{
		doSplash();

		gui = new ConsoleUI(args);

		gui.serv();
	}

	// the Main method
	public static void main(String[] args)
	{
		if (args.length > 0)
		{
			if (args[0].equalsIgnoreCase("Supremica"))
			{
				startSupremicaConsole(args);
			}
			else if (args[0].equalsIgnoreCase("JGrafChart"))
			{
				System.err.println("JGpafChart not supported in console mode");
			}
		}
		else
		{
			startSupremicaConsole(null);
		}
	}
}
;
