
/*
 * Supremica Software License Agreement
 *
 * The Supremica software is not in the public domain
 * However, it is freely available without fee for education,
 * research, and non-profit purposes.  By obtaining copies of
 * this and other files that comprise the Supremica software,
 * you, the Licensee, agree to abide by the following
 * conditions and understandings with respect to the
 * copyrighted software:
 *
 * The software is copyrighted in the name of Supremica,
 * and ownership of the software remains with Supremica.
 *
 * Permission to use, copy, and modify this software and its
 * documentation for education, research, and non-profit
 * purposes is hereby granted to Licensee, provided that the
 * copyright notice, the original author's names and unit
 * identification, and this permission notice appear on all
 * such copies, and that no charge be made for such copies.
 * Any entity desiring permission to incorporate this software
 * into commercial products or to use it for commercial
 * purposes should contact:
 *
 * Knut Akesson (KA), knut@supremica.org
 * Supremica,
 * Haradsgatan 26A
 * 431 42 Molndal
 * SWEDEN
 *
 * to discuss license terms. No cost evaluation licenses are
 * available.
 *
 * Licensee may not use the name, logo, or any other symbol
 * of Supremica nor the names of any of its employees nor
 * any adaptation thereof in advertising or publicity
 * pertaining to the software without specific prior written
 * approval of the Supremica.
 *
 * SUPREMICA AND KA MAKES NO REPRESENTATIONS ABOUT THE
 * SUITABILITY OF THE SOFTWARE FOR ANY PURPOSE.
 * IT IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED WARRANTY.
 *
 * Supremica or KA shall not be liable for any damages
 * suffered by Licensee from the use of this software.
 *
 * Supremica is owned and represented by KA.
 */

// ASCII interface when you cant afford swing
// (or just need batch control)
package org.supremica.console;

import java.util.*;
import java.io.*;
import javax.swing.JFrame;    // jag VILL inte ha sånt i mitt program!
import java.awt.Component;    // se ovan
import org.supremica.automata.algorithms.*;
import org.supremica.automata.*;
import org.supremica.gui.*;

public class ConsoleUI
	implements Gui
{
	private VisualProjectContainer theVisualProjectContainer;
	private BufferedReader reader;

	public ConsoleUI(String[] args)
	{
		theVisualProjectContainer = new VisualProjectContainer();
		reader = new BufferedReader(new InputStreamReader(System.in));

		// todo : handle args
	}

	// menu stuffs
	private void showMenu()
	{
		System.err.print("\nCommands are:\n" + "help or ? (guess)                      quit (guess)\n" + "new <name>                             open <file>\n" + "list                                   save <name> <file>\n" + "mark <name>                            unmark <name>\n" + "sync <to-name> <name1> ... <name b>\n" + "shell <command> [<args>]\n");
	}

	private void openFile(String name)
		throws IOException
	{
		Automata aut = null;

		try
		{
			AutomataBuildFromXml builder = new AutomataBuildFromXml(new DefaultProjectFactory());

			aut = builder.build(new File(name));

			addAutomata(aut);
		}
		catch (Exception exx)
		{
			throw new IOException("failed when reading from " + name + "; " + exx.getMessage());
		}
	}

	// menu handlers:
	private void cmdOpen(int argc, StringTokenizer args)
		throws IOException
	{
		if (argc == 0)
		{
			throw new IOException("[open] no file given");
		}

		while (args.hasMoreTokens())
		{
			openFile(args.nextToken());
		}
	}

	// TODO: shell output is not visible
	// and the input stream is always empty. FIX THIS!
	private void cmdShell(int argc, StringTokenizer args)
		throws IOException
	{
		if (argc == 0)
		{
			throw new IOException("[shell] no command");
		}

		StringBuffer sb = new StringBuffer();

		while (args.hasMoreTokens())
		{
			sb.append(args.nextToken());
			sb.append(' ');
		}

		String command = sb.toString();
		Runtime rt = Runtime.getRuntime();
		Process pr = rt.exec(command);

		try
		{
			int ret = pr.waitFor();

			if (ret != 0)
			{
				System.err.println("Command returned " + ret);
			}
		}
		catch (InterruptedException ex)
		{
			System.err.println("(command interrupted)");
		}
	}

	// -------------------------------------------------------
	private boolean handleCommand(String cmd, StringTokenizer args)
		throws IOException
	{
		System.out.println("YOUR CHOICE: " + cmd);

		int argc = args.countTokens();

		if (cmd.equals("open"))
		{
			cmdOpen(argc, args);
		}
		else if (cmd.equals("shell"))
		{
			cmdShell(argc, args);
		}
		else if (cmd.equals("quit"))
		{
			return false;    // <--- we are done
		}

		return true;
	}

	public StringTokenizer getChoice()
		throws IOException
	{
		String line = reader.readLine();

		if (line == null)    /* EOF */
		{
			throw new IOException("EOF");
		}

		return new StringTokenizer(line);
	}

	public void serv()
	{
		boolean quit = false;
		StringTokenizer st;

		showMenu();

		try
		{
			do
			{
				System.err.print("> ");
				System.err.flush();

				st = getChoice();

				if (st.hasMoreTokens())
				{
					String cmd = st.nextToken();

					try
					{
						quit = !handleCommand(cmd, st);
					}
					catch (IOException exx)
					{
						error(cmd + " failed", exx);
					}
				}
			}
			while (!quit);
		}
		catch (IOException ignored) {}

		System.err.println();
	}

	// ---------------- Gui implementation
	public void error(String msg)
	{
		System.err.println("ERROR : " + msg);
	}

	public void error(String msg, Throwable t)
	{
		System.err.println("ERROR : " + msg);
		System.err.println("\\-TYPE: " + t.toString());
	}

	public void info(String msg)
	{
		System.err.println("INFO  : " + msg);
	}

	public void debug(String msg)
	{
		System.err.println("DEBUG : " + msg);
	}

	public void repaint()
	{

		// TODO
	}

	public String getNewAutomatonName(String str, String def)
	{
		return "TODO";
	}

	public void clearSelection()
	{

		// TODO
	}

	public void selectAll()
	{

		// TODO
	}

	public void close()
	{

		// TODO
	}

	public int addAutomata(Automata a)
		throws Exception
	{
		return -1;    // TODO
	}

	public boolean addAutomaton(Automaton a)
	{
		return false;    // TODO
	}

	public Component getComponent()
	{
		return null;
	}

	public JFrame getFrame()
	{
		return null;
	}

	public VisualProjectContainer getVisualProjectContainer()
	{
		return null;    // TODO
	}

	public Collection getSelectedAutomataAsCollection()
	{
		return null;    // TODO
	}

	public Automata getSelectedAutomata()
	{
		return null;    // TODO
	}

	public FileSecurity getFileSecurity()
	{
		return null;
	}
}
