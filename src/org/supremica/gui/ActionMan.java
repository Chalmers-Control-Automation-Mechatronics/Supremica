/*********************** ActionMan.java *************************/
// This is the guy that ties together the Gui and the menus
// This is nothing but the Controller in the ModelViewController pattern
package org.supremica.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.util.*;
import java.io.*;

import org.supremica.*;
import org.supremica.automata.*;
import org.supremica.automata.algorithms.*;
import org.supremica.comm.xmlrpc.*;
import org.supremica.gui.Gui;
import org.supremica.gui.editor.*;
import org.supremica.gui.help.*;
// import org.supremica.gui.TestCasesDialog;

public class ActionMan
{
	private static int getIntegerInDialogWindow(String text, Component parent)
	{
		boolean finished = false;
		String theInteger = "";
		int theIntValue = -1;

		while (!finished)
		{
			theInteger = JOptionPane.showInputDialog(parent, text);
			try
			{
				theIntValue = Integer.parseInt(theInteger);
				finished = true;
			}
			catch (Exception e)
			{
				JOptionPane.showMessageDialog(parent, "Not a valid integer", "Alert", JOptionPane.ERROR_MESSAGE);
			}
		}
		return theIntValue;
	}

     // Automata.AlphabetAnalyzer action performed
     public static void alphabetAnalyzer_actionPerformed(Gui gui)
     {
		Collection selectedAutomata = gui.getSelectedAutomataAsCollection();
		if (selectedAutomata.size() >= 2)
		{
			Iterator autIt = selectedAutomata.iterator();
			Automata currAutomata = new Automata();
			while (autIt.hasNext())
			{
				Automaton currAutomaton = (Automaton)autIt.next();
				currAutomata.addAutomaton(currAutomaton);
			}
			AlphabetAnalyzer theAnalyzer =
				new AlphabetAnalyzer(currAutomata);

			try
			{
				theAnalyzer.execute();
			}
			catch (Exception ex)
			{
				gui.error("Exception in AlphabetAnalyzer");
			}
		}
 		else
 		{
			JOptionPane.showMessageDialog(gui.getComponent(), "At least two automata must be selected!", "Alert", JOptionPane.ERROR_MESSAGE);
		}
	}
	// Automata.AddSelfLoopArcs action performed
	public static void automataAddSelfLoopArcs_actionPerformed(Gui gui)
	{
		Collection selectedAutomata = gui.getSelectedAutomataAsCollection();
		if (selectedAutomata.size() < 1)
		{
			JOptionPane.showMessageDialog(gui.getComponent(), "At least one automata must be selected!", "Alert", JOptionPane.ERROR_MESSAGE);
			return;
		}

		Iterator autIt = selectedAutomata.iterator();
		while (autIt.hasNext())
		{
			Automaton currAutomaton = (Automaton)autIt.next();
			try
			{
				AddSelfArcs.execute(currAutomaton, true);
			}
			catch (Exception ex)
			{
				gui.error("Exception in AutomataAddSelfLoopArcs. Automaton: " + currAutomaton.getName());
			}
		}
	}
	// Automaton.AllAccepting action performed
	public static void automataAllAccepting_actionPerformed(Gui gui)
	{
		Collection selectedAutomata = gui.getSelectedAutomataAsCollection();
		if (selectedAutomata.size() < 1)
		{
			JOptionPane.showMessageDialog(gui.getComponent(), "At least one automata must be selected!", "Alert", JOptionPane.ERROR_MESSAGE);
			return;
		}

		Iterator autIt = selectedAutomata.iterator();
		while (autIt.hasNext())
		{
			Automaton currAutomaton = (Automaton)autIt.next();
			AutomatonAllAccepting allAccepting = new AutomatonAllAccepting(currAutomaton);
			try
			{
				allAccepting.execute();
			}
			catch (Exception ex)
			{
				gui.error("Exception in AutomataAllAccepting. Automaton: " + currAutomaton.getName());
			}
		}
	}
	// Automaton.Complement action performed
	public static void automataComplement_actionPerformed(Gui gui)
	{
		Collection selectedAutomata = gui.getSelectedAutomataAsCollection();
		if (selectedAutomata.size() < 1)
		{
			JOptionPane.showMessageDialog(gui.getComponent(), "At least one automata must be selected!", "Alert", JOptionPane.ERROR_MESSAGE);
			return;
		}

		Iterator autIt = selectedAutomata.iterator();
		while (autIt.hasNext())
		{
			Automaton currAutomaton = (Automaton)autIt.next();
			String newAutomatonName = gui.getNewAutomatonName("Please enter a new name", currAutomaton.getName() + "_c");

			if (newAutomatonName == null)
			{
				return;
			}

			try
			{
				AutomatonComplement automataComplement = new AutomatonComplement(currAutomaton);
				Automaton newAutomaton = automataComplement.execute();
				newAutomaton.setName(newAutomatonName);
				gui.getAutomatonContainer().add(newAutomaton);
			}
			catch (Exception ex)
			{
				gui.error("Exception in AutomatonMinimize. Automaton: " + currAutomaton.getName());
			}
		}
	}
	// Automaton.Copy action performed
	public static void automataCopy_actionPerformed(Gui gui)
	{
		Collection selectedAutomata = gui.getSelectedAutomataAsCollection();
		if (selectedAutomata.size() < 1)
		{
			JOptionPane.showMessageDialog(gui.getComponent(), "At least one automata must be selected!", "Alert", JOptionPane.ERROR_MESSAGE);
			return;
		}

		Iterator autIt = selectedAutomata.iterator();
		while (autIt.hasNext())
		{
			Automaton currAutomaton = (Automaton)autIt.next();
			String newAutomatonName = gui.getNewAutomatonName("Please enter a new name", currAutomaton.getName() + "(2)");
			if (newAutomatonName == null)
				return;
			try
			{
				Automaton newAutomaton = new Automaton(currAutomaton);
				newAutomaton.setName(newAutomatonName);
				gui.getAutomatonContainer().add(newAutomaton);
			}
			catch (Exception ex)
			{
				gui.error("Exception while copying the automaton");
			}
		}
	}
	//** Delete - remove from the container, clear the selection, mark the project as dirty but do not close the project
	public static void automataDelete_actionPerformed(Gui gui)
	{
		Collection selectedAutomata = gui.getSelectedAutomataAsCollection();
		if (selectedAutomata.size() < 1)
		{
			JOptionPane.showMessageDialog(gui.getComponent(), "At least one automata must be selected!", "Alert", JOptionPane.ERROR_MESSAGE);
			return;
		}

		Iterator autIt = selectedAutomata.iterator();
		while (autIt.hasNext())
		{
			Automaton currAutomaton = (Automaton)autIt.next();
			String currAutomatonName = currAutomaton.getName();
			try
			{
				gui.getAutomatonContainer().remove(currAutomatonName);
			}
			catch (Exception ex)
			{
				gui.error("Exception while removing " + currAutomatonName);
			}
		}
		/* And this "closes" the project, should it, really?
		if (theAutomatonContainer.getSize() == 0)
		{
			theAutomatonContainer.setProjectFile(null);
		}
		*/
		// and we should have no notion of a "table" here
		// theAutomatonTable.clearSelection();
		gui.clearSelection();
	}
	//** Export - shouldn't there be an exporter object?
	public static void automataExport(Gui gui)
	{
		Collection selectedAutomata = gui.getSelectedAutomataAsCollection();
		if (selectedAutomata.size() < 1)
		{
			JOptionPane.showMessageDialog(gui.getComponent(), "At least one automata must be selected!", "Alert", JOptionPane.ERROR_MESSAGE);
			return;
		}

		String xmlString = "xml";
		String dotString = "dot";
		String dsxString = "dsx";

		Object[] possibleValues = { xmlString, dotString, dsxString };
		Object selectedValue = JOptionPane.showInputDialog(
			gui.getComponent(), "Export as", "Input", JOptionPane.INFORMATION_MESSAGE,
			null, possibleValues, possibleValues[0]);

		if (selectedValue == null)
			return;

		int exportMode = -1;
		if (selectedValue == xmlString)
		{
			exportMode = 1;
		}
		else if (selectedValue == dotString)
		{
			exportMode = 2;
		}
		else if (selectedValue == dsxString)
		{
			exportMode = 3;
		}
		else
		{
			return;
		}

		Iterator autIt = selectedAutomata.iterator();
		while (autIt.hasNext())
		{
			Automaton currAutomaton = (Automaton)autIt.next();
			JFileChooser fileExporter = null;
			if (exportMode == 1)
			{
				fileExporter = FileDialogs.getXMLFileExporter();
			}
			else if (exportMode == 2)
			{
				fileExporter = FileDialogs.getDOTFileExporter();
			}
			else if (exportMode == 3)
			{
				fileExporter = FileDialogs.getDSXFileExporter();
			}
			else
			{
				return;
			}
			if (fileExporter.showSaveDialog(gui.getComponent()) == JFileChooser.APPROVE_OPTION)
			{
				File currFile = fileExporter.getSelectedFile();
				if (currFile != null)
				{
					if (!currFile.isDirectory())
					{
						try
						{
							if (exportMode == 1)
							{
								Automata currAutomata = new Automata();
								currAutomata.addAutomaton(currAutomaton);
								AutomataToXml exporter = new AutomataToXml(currAutomata);
								exporter.serialize(currFile.getAbsolutePath());
							}
							else if (exportMode == 2)
							{
								AutomatonToDot exporter = new AutomatonToDot(currAutomaton);
								exporter.serialize(currFile.getAbsolutePath());
							}
							else if (exportMode == 3)
							{
								AutomatonToDsx exporter = new AutomatonToDsx(currAutomaton);
								exporter.serialize(currFile.getAbsolutePath());
							}
						}
						catch (Exception ex)
						{
							gui.error("Exception while exporting " + currFile.getAbsolutePath());
						}
					}
				}
			}
		}
	}
	//** Extend
	public static void automataExtend_actionPerformed(Gui gui)
	{
		Collection selectedAutomata = gui.getSelectedAutomataAsCollection();
		if (selectedAutomata.size() < 1)
		{
			JOptionPane.showMessageDialog(gui.getComponent(), "At least one automata must be selected!", "Alert", JOptionPane.ERROR_MESSAGE);
			return;
		}

		Iterator autIt = selectedAutomata.iterator();
		while (autIt.hasNext())
		{
			Automaton currAutomaton = (Automaton)autIt.next();
			String newAutomatonName = gui.getNewAutomatonName("Please enter a new name", "");

			if (newAutomatonName == null)
			{
				return;
			}

			int k = getIntegerInDialogWindow("Select k", gui.getComponent());
			AutomataExtender extender = new AutomataExtender(currAutomaton);
			extender.setK(k);
			try
			{
				extender.execute();
				Automaton newAutomaton = extender.getNewAutomaton();
				newAutomaton.setName(newAutomatonName);
				gui.getAutomatonContainer().add(newAutomaton);
			}
			catch (Exception ex)
			{
				gui.error("Exception in AutomataExtend. Automaton: " + currAutomaton.getName());
			}
		}
	}
	//** Purge
	public static void automataPurge_actionPerformed(Gui gui)
	{
		Collection selectedAutomata = gui.getSelectedAutomataAsCollection();
		if (selectedAutomata.size() < 1)
		{
			JOptionPane.showMessageDialog(gui.getComponent(), "At least one automata must be selected!", "Alert", JOptionPane.ERROR_MESSAGE);
			return;
		}

		Iterator autIt = selectedAutomata.iterator();
		while (autIt.hasNext())
		{
			Automaton currAutomaton = (Automaton)autIt.next();
			AutomatonPurge automatonPurge = new AutomatonPurge(currAutomaton);
			try
			{
				automatonPurge.execute();
			}
			catch (Exception ex)
			{
				gui.error("Exception in AutomataPurge. Automaton: " + currAutomaton.getName());
			}
		}
	}
	//** RemovePass - removes all pass events
	public static void automataRemovePass_actionPerformed(Gui gui)
	{
		Collection selectedAutomata = gui.getSelectedAutomataAsCollection();
		if (selectedAutomata.size() < 1)
		{
			JOptionPane.showMessageDialog(gui.getComponent(), "At least one automata must be selected!", "Alert", JOptionPane.ERROR_MESSAGE);
			return;
		}

		Iterator autIt = selectedAutomata.iterator();
		while (autIt.hasNext())
		{
			Automaton currAutomaton = (Automaton)autIt.next();
			try
			{
				RemovePassEvent.execute(currAutomaton);
			}
			catch (Exception ex)
			{
				gui.error("Exception in AutomataRemovePass. Automaton: " + currAutomaton.getName());
			}
		}
	}
	//** RemoveSelfLoopArcs
	public static void automataRemoveSelfLoopArcs_actionPerformed(Gui gui)
	{
		Collection selectedAutomata = gui.getSelectedAutomataAsCollection();
		if (selectedAutomata.size() < 1)
		{
			JOptionPane.showMessageDialog(gui.getComponent(), "At least one automata must be selected!", "Alert", JOptionPane.ERROR_MESSAGE);
			return;
		}

		Iterator autIt = selectedAutomata.iterator();
		while (autIt.hasNext())
		{
			Automaton currAutomaton = (Automaton)autIt.next();
			try
			{
				RemoveSelfArcs.execute(currAutomaton);
			}
			catch (Exception ex)
			{
				gui.error("Exception in RemoveSelfArcs. Automaton: " + currAutomaton.getName());
			}
		}
	}
	//** Rename
	public static void automataRename_actionPerformed(Gui gui)
	{
		Collection selectedAutomata = gui.getSelectedAutomataAsCollection();
		if (selectedAutomata.size() < 1)
		{
			JOptionPane.showMessageDialog(gui.getComponent(), "At least one automata must be selected!", "Alert", JOptionPane.ERROR_MESSAGE);
			return;
		}

		Iterator autIt = selectedAutomata.iterator();
		while (autIt.hasNext())
		{
			Automaton currAutomaton = (Automaton)autIt.next();
			String currAutomatonName = currAutomaton.getName();
			try
			{
				String newName = gui.getNewAutomatonName("Enter a new name for " + currAutomatonName, currAutomatonName);
				if (newName != null)
				{
					gui.getAutomatonContainer().rename(currAutomaton, newName);
				}
			}
			catch (Exception ex)
			{
				gui.error("Exception while renaming the automaton " + currAutomatonName, ex);
			}
		}
	}
	//** Synchronize - Threaded version
	public static void automataSynchronize_actionPerformed(Gui gui)
	{
		Collection selectedAutomata = gui.getSelectedAutomataAsCollection();

 		if (selectedAutomata.size() < 2)
 		{
			JOptionPane.showMessageDialog(gui.getComponent(), "At least two automata must be selected!", "Alert", JOptionPane.ERROR_MESSAGE);
			return;
		}

		String newAutomatonName = gui.getNewAutomatonName("Please enter a new name", "");

		if (newAutomatonName == null)
		{
			return;
		}

		Automata currAutomata = new Automata();

		Iterator autIt = selectedAutomata.iterator();
		while (autIt.hasNext())
		{
			Automaton currAutomaton = (Automaton)autIt.next();
			String currAutomatonName = currAutomaton.getName();
			if (currAutomaton.getInitialState() == null)
			{
				JOptionPane.showMessageDialog(gui.getComponent(),
					"The automaton " + currAutomatonName + " does not have an initial state!",
					"Alert",
					JOptionPane.ERROR_MESSAGE);
				return;
			}
			currAutomata.addAutomaton(currAutomaton);
		}

		SynchronizationOptions syncOptions;
		try
		{
			syncOptions = new SynchronizationOptions(
				WorkbenchProperties.syncNbrOfExecuters(),
				SynchronizationType.Prioritized,
				WorkbenchProperties.syncInitialHashtableSize(),
				WorkbenchProperties.syncExpandHashtable(),
				WorkbenchProperties.syncForbidUncontrollableStates(),
				WorkbenchProperties.syncExpandForbiddenStates(),
				false,
				false,
				true,
				WorkbenchProperties.verboseMode()
			);
		}
		catch (Exception ex)
		{
				JOptionPane.showMessageDialog(gui.getComponent(),
					"Invalid synchronizationOptions",
					"Alert",
					JOptionPane.ERROR_MESSAGE);
				return;
		}

		AutomataSynchronizerWorker worker = new AutomataSynchronizerWorker(gui, currAutomata, newAutomatonName, syncOptions);
	}														//!! was 'this' here^^^^^^^^^^^^, now takes a Gui
	//** Synthesize
	public static void automataSynthesize_actionPerformed(Gui gui)
	{
		SynthesizerOptions synthesizerOptions = new SynthesizerOptions();
		SynthesizerDialog synthesizerDialog = new SynthesizerDialog(gui.getFrame(), synthesizerOptions);
		synthesizerDialog.show();				//!! was 'this' here^^^^^^^^^^^^, takes a JFrame

		if (!synthesizerOptions.getDialogOK())
			return;

		Date startDate = new Date();
		Collection selectedAutomata = gui.getSelectedAutomataAsCollection();
		if (selectedAutomata.size() < 1)
		{
			JOptionPane.showMessageDialog(gui.getFrame(), "At least one automata must be selected!", "Alert", JOptionPane.ERROR_MESSAGE);
			return;
		}

		if (selectedAutomata.size() > 1)
		{
			SynchronizationOptions syncOptions;
			try
			{
				syncOptions = new SynchronizationOptions(
					WorkbenchProperties.syncNbrOfExecuters(),
					SynchronizationType.Prioritized,
					WorkbenchProperties.syncInitialHashtableSize(),
					WorkbenchProperties.syncExpandHashtable(),
					true, // WorkbenchProperties.syncForbidUncontrollableStates(),
					WorkbenchProperties.syncExpandForbiddenStates(),
					false,
					false,
					true,
					WorkbenchProperties.verboseMode()
					);
			}
			catch (Exception ex)
			{
				JOptionPane.showMessageDialog(gui.getComponent(),
											  "Invalid synchronizationOptions",
											  "Alert",
											  JOptionPane.ERROR_MESSAGE);
				return;
			}

			Automata currAutomata = new Automata();
			Iterator autIt = selectedAutomata.iterator();
			while (autIt.hasNext())
			{
				Automaton currAutomaton = (Automaton)autIt.next();
				String currAutomatonName = currAutomaton.getName();
				if (currAutomaton.getInitialState() == null)
				{
					JOptionPane.showMessageDialog(gui.getComponent(), "The automaton " + currAutomatonName + " does not have an initial state!", "Alert", JOptionPane.ERROR_MESSAGE);
					return;
				}
				currAutomata.addAutomaton(currAutomaton);
			}

			AutomataSynthesizer synthesizer = new AutomataSynthesizer(gui, currAutomata, syncOptions, synthesizerOptions);
			try											//!! was 'this' here^^^^^^^^^^^^, now expects a Gui
			{
				synthesizer.execute();
			}
			catch (Exception ex)
			{
				gui.error("Exception in AutomataSynthesizer: " + ex);
			}									//!! was 'e' here^^, should be 'exe?
		}
		else
		{
			Iterator autIt = selectedAutomata.iterator();
			while (autIt.hasNext())
			{
				Automaton currAutomaton = (Automaton)autIt.next();
				try
				{
					AutomatonSynthesizer synthesizer = new AutomatonSynthesizer(currAutomaton);
					if (synthesizerOptions.getSynthesisType() == 0)      // Controllable
						synthesizer.synthesizeControllable();
					else if (synthesizerOptions.getSynthesisType() == 1) // Non-blocking
						gui.error("Option not implemented...");
					else if (synthesizerOptions.getSynthesisType() == 2) // Both
						synthesizer.synthesize();
					else
						gui.error("Unavailable option chosen.");
					if (synthesizerOptions.getPurge())
					{
						AutomatonPurge automatonPurge = new AutomatonPurge(currAutomaton);
						automatonPurge.execute();
					}
				}
				catch (Exception ex)
				{
					gui.error("Exception in AutomatonSynthesizer. Automaton: " + currAutomaton.getName());
				}
			}
		}
		Date endDate = new Date();
		gui.info("Execution completed after " + (endDate.getTime()-startDate.getTime())/1000.0 + " seconds.");
	}
	// Automaton.Verify action performed
	// Threaded version
	public static void automataVerify_actionPerformed(Gui gui)
	{
		VerificationOptions verificationOptions = new VerificationOptions();
		VerificationDialog verificationDialog = new VerificationDialog(gui.getFrame(), verificationOptions);
		verificationDialog.show();

		if (!verificationOptions.getDialogOK())
			return;

		Collection selectedAutomata = gui.getSelectedAutomataAsCollection();

 		if (selectedAutomata.size() < 1)
 		{
			JOptionPane.showMessageDialog(gui.getFrame(), "At least one automata must be selected!", "Alert", JOptionPane.ERROR_MESSAGE);
			return;
		}

		Automata currAutomata = new Automata();

		// The Automata must have initial states.
		Iterator autIt = selectedAutomata.iterator();
		while (autIt.hasNext())
		{
			Automaton currAutomaton = (Automaton)autIt.next();
			String currAutomatonName = currAutomaton.getName();
			if (currAutomaton.getInitialState() == null)
			{
				JOptionPane.showMessageDialog(gui.getFrame(),
					"The automaton " + currAutomatonName + " does not have an initial state!",
					"Alert",
					JOptionPane.ERROR_MESSAGE);
				return;
			}
			currAutomata.addAutomaton(currAutomaton);
		}

		SynchronizationOptions syncOptions;
		try
		{
			syncOptions = new SynchronizationOptions();
		}
		catch (Exception ex)
		{
			JOptionPane.showMessageDialog(gui.getFrame(),
										  "Invalid synchronizationOptions",
										  "Alert",
										  JOptionPane.ERROR_MESSAGE);
			return;
		}

		AutomataVerificationWorker worker = new AutomataVerificationWorker(gui, currAutomata, syncOptions, verificationOptions);
	}
	// Automaton.Alphabet action performed
	public static void automatonAlphabet_actionPerformed(Gui gui)
	{
		Collection selectedAutomata = gui.getSelectedAutomataAsCollection();
		if (selectedAutomata.size() < 1)
		{
			JOptionPane.showMessageDialog(gui.getFrame(), "At least one automata must be selected!", "Alert", JOptionPane.ERROR_MESSAGE);
			return;
		}

		Iterator autIt = selectedAutomata.iterator();
		while (autIt.hasNext())
		{
			Automaton currAutomaton = (Automaton)autIt.next();
			String currAutomatonName = currAutomaton.getName();
			try
			{
				AlphabetViewer viewer = gui.getAutomatonContainer().getAlphabetViewer(currAutomatonName);
			}
			catch (Exception ex)
			{
				// thisCategory.error("Exception in AlphabetViewer. Automaton: " + currAutomaton.getName());
				gui.error("Exception in AlphabetViewer. Automaton: " + currAutomaton.getName());
			}
		}
    }
	// Automaton.Explore action performed
    public static void automatonExplore_actionPerformed(Gui gui)
    {
		Collection selectedAutomata = gui.getSelectedAutomataAsCollection();
		if (selectedAutomata.size() < 1)
		{
			JOptionPane.showMessageDialog(gui.getFrame(), "At least one automata must be selected!", "Alert", JOptionPane.ERROR_MESSAGE);
			return;
		}

		Iterator autIt = selectedAutomata.iterator();
		/*
		while (autIt.hasNext())
		{
			Automaton currAutomaton = (Automaton)autIt.next();
			String currAutomatonName = currAutomaton.getName();
			if (currAutomaton.getInitialState() == null)
			{
				JOptionPane.showMessageDialog(this, "The automaton " + currAutomatonName + " does not have an initial state!", "Alert", JOptionPane.ERROR_MESSAGE);
			}
			else
			{
				try
				{
					AutomatonExplorer explorer = getAutomatonContainer.getAutomatonExplorer(currAutomatonName);
				}
				catch (Exception ex)
				{
					thisCategory.error("Exception in AutomatonExplorer. Automaton: " + currAutomaton.getName());
				}
			}
		}
		*/

		if (selectedAutomata.size() == 1)
		{   // One automata selected
			Automaton currAutomaton = (Automaton)autIt.next();
			String currAutomatonName = currAutomaton.getName();
			if (currAutomaton.getInitialState() == null)
			{
				JOptionPane.showMessageDialog(gui.getFrame(), "The automaton " + currAutomatonName + " does not have an initial state!", "Alert", JOptionPane.ERROR_MESSAGE);
			}
			try
			{
				AutomatonExplorer explorer = gui.getAutomatonContainer().getAutomatonExplorer(currAutomatonName);
			}
			catch (Exception ex)
			{
				// thisCategory.error("Exception in AutomatonExplorer. Automaton: " + currAutomaton.getName());
				gui.error("Exception in AutomatonExplorer. Automaton: " + currAutomaton.getName());
			}
		}
		else
		{   // Many automata selected
			Automata currAutomata = new Automata();

			while (autIt.hasNext())
			{
				Automaton currAutomaton = (Automaton)autIt.next();
				String currAutomatonName = currAutomaton.getName();
				if (currAutomaton.getInitialState() == null)
				{
					JOptionPane.showMessageDialog(gui.getFrame(),
												  "The automaton " + currAutomatonName + " does not have an initial state!",
												  "Alert",
												  JOptionPane.ERROR_MESSAGE);
					return;
				}
				currAutomata.addAutomaton(currAutomaton);
			}

			try
			{
			    AutomataExplorer explorer = new AutomataExplorer(currAutomata);
				explorer.setVisible(true);
				explorer.initialize();
			}
			catch (Exception ex)
			{
				// thisCategory.error("Exception in AutomataExplorer.");
				gui.error("Exception in AutomataExplorer.");
			}
		}
	}
	// Automaton.Minimization action performed
	public static void automatonMinimize_actionPerformed(Gui gui)
	{
		Collection selectedAutomata = gui.getSelectedAutomataAsCollection();
		if (selectedAutomata.size() < 1)
		{
			JOptionPane.showMessageDialog(gui.getFrame(), "At least one automata must be selected!", "Alert", JOptionPane.ERROR_MESSAGE);
			return;
		}

		Iterator autIt = selectedAutomata.iterator();
		while (autIt.hasNext())
		{
			Automaton currAutomaton = (Automaton)autIt.next();
			String newAutomatonName = gui.getNewAutomatonName("Please enter a new name", "");

			if (newAutomatonName == null)
			{
				return;
			}

			try
			{
				AutomatonMinimizer autMinimizer = new AutomatonMinimizer(currAutomaton);
				Automaton newAutomaton = autMinimizer.getMinimizedAutomaton(true);
				newAutomaton.setName(newAutomatonName);
				gui.getAutomatonContainer().add(newAutomaton);
			}
			catch (Exception ex)
			{
				// thisCategory.error("Exception in AutomatonMinimize. Automaton: " + currAutomaton.getName());
				gui.error("Exception in AutomatonMinimize. Automaton: " + currAutomaton.getName());
			}
		}
	}
    // Automaton.Status action performed
    public static void automatonStatus_actionPerformed(Gui gui)
    {
		// thisCategory.info("Number of automata: " + gui.getAutomatonContainer().getSize());
		gui.info("Number of automata: " + gui.getAutomatonContainer().getSize());
		Collection selectedAutomata = gui.getSelectedAutomataAsCollection();
		if (selectedAutomata.size() < 1)
		{
			return;
		}

		StringBuffer infoStr = new StringBuffer();

		infoStr.append("Number of selected automata: " + selectedAutomata.size());

		double potentialNumberOfStates = 1.0;
		for (Iterator autIt = selectedAutomata.iterator(); autIt.hasNext();)
		{
			Automaton currAutomaton = (Automaton)autIt.next();
			potentialNumberOfStates = potentialNumberOfStates * currAutomaton.nbrOfStates();
		}
		infoStr.append("\n\tNumber of potential states: " + potentialNumberOfStates);
		// thisCategory.info(infoStr.toString());
		gui.info(infoStr.toString());

		for (Iterator autIt = selectedAutomata.iterator(); autIt.hasNext();)
		{
			Automaton currAutomaton = (Automaton)autIt.next();
			StringBuffer statusStr = new StringBuffer();
			statusStr.append("Status for automaton: " + currAutomaton.getName());
			statusStr.append("\n\tNumber of states: " + currAutomaton.nbrOfStates());
			statusStr.append("\n\tNumber of events: " + currAutomaton.nbrOfEvents());
			statusStr.append("\n\tNumber of accepting states: " + currAutomaton.nbrOfAcceptingStates());
			statusStr.append("\n\tNumber of forbidden states: " + currAutomaton.nbrOfForbiddenStates());
			int acceptingAndForbiddenStates = currAutomaton.nbrOfAcceptingAndForbiddenStates();
			if (acceptingAndForbiddenStates > 0)
			{
				statusStr.append("\n\tNumber of accepting and forbidden states: " + acceptingAndForbiddenStates);
			}
			// thisCategory.info(statusStr.toString());
			gui.info(statusStr.toString());
		}
    }
    // Automaton.View action performed
    public static void automatonView_actionPerformed(Gui gui)
    {
    	// gui.debug("ActionMan to the rescue!");
		Collection selectedAutomata = gui.getSelectedAutomataAsCollection();
		if (selectedAutomata.size() < 1)
		{
			JOptionPane.showMessageDialog(gui.getFrame(), "At least one automaton must be selected!", "Alert", JOptionPane.ERROR_MESSAGE);
			return;
		}

		Iterator autIt = selectedAutomata.iterator();
		while (autIt.hasNext())
		{
			Automaton currAutomaton = (Automaton)autIt.next();
			String currAutomatonName = currAutomaton.getName();
			int maxNbrOfStates = WorkbenchProperties.getDotMaxNbrOfStatesWithoutWarning();
			if  (maxNbrOfStates < currAutomaton.nbrOfStates())
			{
				StringBuffer msg = new StringBuffer();
				msg.append(currAutomatonName + " has " + currAutomaton.nbrOfStates() + " states. ");
				msg.append("It is not recommended to display an automaton with more than " + maxNbrOfStates + " states.\n");
				msg.append("Do you want to abort viewing?");
				int res = JOptionPane.showOptionDialog(
													   gui.getFrame(), msg, "Warning",
													   JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE,
													   null, null, null
													   );
				if (res == 0)
				{ // Abort - YES
					return;
				}
			}
			if (!currAutomaton.hasInitialState())
			{
				JOptionPane.showMessageDialog(gui.getFrame(), "The automaton does not have an initial state!", "Alert", JOptionPane.ERROR_MESSAGE);
				return;
			}

			try
			{
				AutomatonViewer viewer = gui.getAutomatonContainer().getAutomatonViewer(currAutomatonName);
			}
			catch (Exception ex)
			{
				// thisCategory.error("Exception in AutomatonViewer. Automaton: " + currAutomaton.getName());
				gui.error("Exception in AutomatonViewer. Automaton: " + currAutomaton.getName());
				return;
			}

		}
    }
	// Variable declared here, wanted it to be local to this func, but...
	static PreferencesDialog thePreferencesDialog = null;
	public static void configurePreferences_actionPerformed(Gui gui)
	{

		if (thePreferencesDialog == null)
		{
			thePreferencesDialog = new PreferencesDialog(gui.getFrame());
		}
		thePreferencesDialog.setVisible(true);
    }
    // File.Exit action performed
    public static void fileExit(Gui gui)
    {
        System.exit(0);
    }

	public static void fileExportDesco(Gui gui)
	{
		automataExport(gui);
	}
	public static void fileExportDot(Gui gui)
	{
		automataExport(gui);
	}
	public static void fileExportSupremica(Gui gui)
	{
		automataExport(gui);
	}
	public static void fileExportTCT(Gui gui)
	{
		automataExport(gui);
	}
	public static void fileExportUMDES(Gui gui)
	{
		automataExport(gui);
	}
	public static void fileExportValid(Gui gui)
	{
		automataExport(gui);
	}
	public static void fileImportDesco(Gui gui)
	{
		/*
		JFileChooser fileOpener = FileDialogs.getDescoFileImporter();
		if (fileOpener.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
		{
			File[] currFiles = fileOpener.getSelectedFiles();
			if (currFiles != null)
			{
				for (int i = 0; i < currFiles.length; i++)
				{
					if (currFiles[i].isFile())
					{
						importDescoFile(currFiles[i]);
					}
				}
			}
			repaint();
			theAutomatonTable.repaint();
       	}
       	*/
    }
	public static void fileImportValid(Gui gui)
	{
		JFileChooser fileOpener = FileDialogs.getVALIDFileImporter();
		if (fileOpener.showOpenDialog(gui.getFrame()) == JFileChooser.APPROVE_OPTION)
		{
			File[] currFiles = fileOpener.getSelectedFiles();
			if (currFiles != null)
			{
				for (int i = 0; i < currFiles.length; i++)
				{
					if (currFiles[i].isFile())
					{
						importValidFile(gui, currFiles[i]);
					}
				}
			}
			gui.repaint();
			// theAutomatonTable.repaint(); // shoudl this really be necessary??
       	}
    }
	// File.Open action performed
	public static void fileOpen(Gui gui)
	{
		JFileChooser fileOpener = FileDialogs.getXMLFileImporter();
		if (fileOpener.showOpenDialog(gui.getFrame()) == JFileChooser.APPROVE_OPTION)
		{
			File[] currFiles = fileOpener.getSelectedFiles();
			if (currFiles != null)
			{
				for (int i = 0; i < currFiles.length; i++)
				{
					if (currFiles[i].isFile())
					{
						openFile(gui, currFiles[i]);
					}
				}
			}
			gui.getFrame().repaint();
			// theAutomatonTable.repaint(); // necessary?
       	}
    }
	// Why this indirection?
	public static void openFile(Gui gui, File file)
	{
		openAutomataXMLFile(gui, file);
	}

	public static void openAutomataXMLFile(Gui gui, File file)
	{
		int nbrOfAutomataBeforeOpening = gui.getAutomatonContainer().getSize();

		// thisCategory.info("Opening " + file.getAbsolutePath() + " ...");
		gui.info("Opening " + file.getAbsolutePath() + " ...");
		int nbrOfAddedAutomata = 0;
		try
		{
			Automata currAutomata = AutomataBuildFromXml.build(file);

			if (nbrOfAutomataBeforeOpening == 0)
			{
				String projectName = currAutomata.getName();
				if (projectName != null)
				{
					gui.getAutomatonContainer().setProjectName(projectName);
					// thisCategory.info("Project name changed to \"" + projectName + "\"");
					gui.info("Project name changed to \"" + projectName + "\"");
					gui.getAutomatonContainer().updateFrameTitles();
				}
			}

			Iterator autIt = currAutomata.iterator();
			while (autIt.hasNext())
			{
				Automaton currAutomaton = (Automaton)autIt.next();
				boolean add = true;

				// Force the user to enter a new name if the name is ""
				// Note that a null name is not allowed by AutomataBuildFromXml
				if (currAutomaton.getName().equals(""))
				{
					String autName = gui.getNewAutomatonName("Enter a new name", "");
					if (autName == null)
					{
						add = false;
						return; // It's not ok to cancel!
					}
					else
					{
						currAutomaton.setName(autName);
					}
				}

				if (gui.getAutomatonContainer().containsAutomaton(currAutomaton.getName()))
				{
					String autName = currAutomaton.getName();

					JOptionPane.showMessageDialog(gui.getFrame(), autName + " already exists", "Alert", JOptionPane.ERROR_MESSAGE);

					autName = gui.getNewAutomatonName("Enter a new name", autName + "(2)");
					if (autName == null)
					{
						add = false;
						return; // It's not ok to cancel!
					}
					else
					{
						currAutomaton.setName(autName);
					}
				}
				if (add)
				{
					nbrOfAddedAutomata++;
					gui.getAutomatonContainer().add(currAutomaton);
				}
			}
		}
		catch (Exception e)
		{
			// thisCategory.error("Error while opening " + file.getAbsolutePath() + " " + e.getMessage());
			gui.error("Error while opening " + file.getAbsolutePath() + " " + e.getMessage());
			return;
		}
		// thisCategory.info("Successfully opened " + nbrOfAddedAutomata + " automata.");
		gui.info("Successfully opened " + nbrOfAddedAutomata + " automata.");

		if (nbrOfAutomataBeforeOpening > 0)
		{
			File projectFile = gui.getAutomatonContainer().getProjectFile();
			if (projectFile != null)
			{
				gui.getAutomatonContainer().setProjectFile(null);
			}
		}
		else
		{
			gui.getAutomatonContainer().setProjectFile(file);
		}
	}

	// File.Save action performed
	public static void fileSave(Gui gui)
	{
		File currFile = gui.getAutomatonContainer().getProjectFile();
		if (currFile == null)
		{
			fileSaveAs(gui);
			return;
		}

		Automata currAutomata = gui.getAutomatonContainer().getAutomata();

		if (currFile != null)
		{
			if (!currFile.isDirectory())
			{
				try
				{
					AutomataToXml exporter = new AutomataToXml(currAutomata);
					exporter.serialize(currFile.getAbsolutePath());
				}
				catch (Exception ex)
				{
					// thisCategory.error("Exception while saveAs " + currFile.getAbsolutePath());
					gui.error("Exception while saveAs " + currFile.getAbsolutePath());
				}
			}
		}
	}
	// File.SaveAs action performed
	public static void fileSaveAs(Gui gui)
	{
		JFileChooser fileSaveAs = FileDialogs.getXMLFileSaveAs();

		String projectName = gui.getAutomatonContainer().getProjectName();
		if (projectName != null)
		{
			File currDirectory = fileSaveAs.getCurrentDirectory();
			fileSaveAs.setSelectedFile(new File(currDirectory, projectName + ".xml"));
		}

		if (fileSaveAs.showSaveDialog(gui.getFrame()) == JFileChooser.APPROVE_OPTION)
		{
			File currFile = fileSaveAs.getSelectedFile();
			if (currFile != null)
			{
				gui.getAutomatonContainer().setProjectFile(currFile);
				fileSave(gui);
			}
		}
	}
	public static void importValidFile(Gui gui, File file)
	{
		// thisCategory.info("Importing " + file.getAbsolutePath() + " ...");
		gui.info("Importing " + file.getAbsolutePath() + " ...");
		int nbrOfAddedAutomata = 0;

		try
		{
  			Automata currAutomata = AutomataBuildFromVALID.build(file);
			Iterator autIt = currAutomata.iterator();
			while (autIt.hasNext())
			{
				Automaton currAutomaton = (Automaton)autIt.next();
				boolean add = true;
				if (currAutomaton.getName().equals(""))
				{
					String autName = gui.getNewAutomatonName("Enter a new name", "");
					if (autName == null)
					{
						add = false;
						return; // It's not ok to cancel!
					}
					else
					{
						currAutomaton.setName(autName);
					}
				}

				if (gui.getAutomatonContainer().containsAutomaton(currAutomaton.getName()))
				{
					String autName = currAutomaton.getName();

					JOptionPane.showMessageDialog(gui.getFrame(), autName + " already exists", "Alert",
												  JOptionPane.ERROR_MESSAGE);

					autName = gui.getNewAutomatonName("Enter a new name", autName + "(2)");
					if (autName == null)
					{
						add = false; // It's not ok to cancel!
					}
					else
					{
						currAutomaton.setName(autName);
					}
				}
				if (add)
				{
					nbrOfAddedAutomata++;
					gui.getAutomatonContainer().add(currAutomaton);
				}
			}
		}
		catch (Exception e)
		{
			// thisCategory.error("Error while importing " + file.getAbsolutePath() + " " + e.getMessage());
			gui.error("Error while importing " + file.getAbsolutePath() + " " + e.getMessage());
			return;
		}
		// thisCategory.info("Successfully imported " + nbrOfAddedAutomata + " automata.");
		gui.info("Successfully imported " + nbrOfAddedAutomata + " automata.");
	}
	// Automata.AlphabetNormalize action performed
	public static void normalizeAlphabet_actionPerformed(Gui gui)
	{
		Collection selectedAutomata = gui.getSelectedAutomataAsCollection();
		if (selectedAutomata.size() < 1)
		{
			JOptionPane.showMessageDialog(gui.getFrame(), "At least one automata must be selected!", "Alert", JOptionPane.ERROR_MESSAGE);
			return;
		}

		Iterator autIt = selectedAutomata.iterator();
		while (autIt.hasNext())
		{
			Automaton currAutomaton = (Automaton)autIt.next();
			try
			{
				AlphabetNormalize alphabetNormalize = new AlphabetNormalize(currAutomaton);
				alphabetNormalize.execute();
			}
			catch (Exception ex)
			{
				// thisCategory.error("Exception in AlphabetNormalizer. Automaton: " + currAutomaton.getName());
				// thisCategory.error(ex);
				gui.error("Exception in AlphabetNormalizer. Automaton: " + currAutomaton.getName());
				gui.error(ex);
				ex.printStackTrace();
			}
		}
	}
    // selectAll action performed
    public static void selectAll_actionPerformed(Gui gui)
    {
		// theAutomatonTable.selectAll();
		gui.selectAll();
    }
	//
	public static void findStates_action(Gui gui)
	{
		FindStates find_states = new FindStates(gui.getSelectedAutomata());
		try
		{
			find_states.execute();
		}
		catch(Exception excp)
		{
			// thisCategory.error(excp.toString());
			gui.error(excp.toString());
		}

	}
	// Delete All - this really implements Close Project
	public static void automataDeleteAll_actionPerformed(Gui gui)
	{
		gui.getAutomatonContainer().clear();
		gui.clearSelection();
		gui.getAutomatonContainer().setProjectFile(null);
	}

	// TestCases... - open the test cases dialog, and add the result to the current set of automata
	public static void testCases(Gui gui)
		throws Exception
	{
		TestCasesDialog tc_dlg = new TestCasesDialog(gui.getFrame());
		tc_dlg.show();
		Automata automata = tc_dlg.getAutomata();
		if(automata != null)
		{
			gui.addAutomata(automata);
		}
	}

} // ActionMan
