
/*
 *  Supremica Software License Agreement
 *
 *  The Supremica software is not in the public domain
 *  However, it is freely available without fee for education,
 *  research, and non-profit purposes.  By obtaining copies of
 *  this and other files that comprise the Supremica software,
 *  you, the Licensee, agree to abide by the following
 *  conditions and understandings with respect to the
 *  copyrighted software:
 *
 *  The software is copyrighted in the name of Supremica,
 *  and ownership of the software remains with Supremica.
 *
 *  Permission to use, copy, and modify this software and its
 *  documentation for education, research, and non-profit
 *  purposes is hereby granted to Licensee, provided that the
 *  copyright notice, the original author's names and unit
 *  identification, and this permission notice appear on all
 *  such copies, and that no charge be made for such copies.
 *  Any entity desiring permission to incorporate this software
 *  into commercial products or to use it for commercial
 *  purposes should contact:
 *
 *  Knut Akesson (KA), knut@supremica.org
 *  Supremica,
 *  Haradsgatan 26A
 *  431 42 Molndal
 *  SWEDEN
 *
 *  to discuss license terms. No cost evaluation licenses are
 *  available.
 *
 *  Licensee may not use the name, logo, or any other symbol
 *  of Supremica nor the names of any of its employees nor
 *  any adaptation thereof in advertising or publicity
 *  pertaining to the software without specific prior written
 *  approval of the Supremica.
 *
 *  SUPREMICA AND KA MAKES NO REPRESENTATIONS ABOUT THE
 *  SUITABILITY OF THE SOFTWARE FOR ANY PURPOSE.
 *  IT IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED WARRANTY.
 *
 *  Supremica or KA shall not be liable for any damages
 *  suffered by Licensee from the use of this software.
 *
 *  Supremica is owned and represented by KA.
 */

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
import org.supremica.properties.SupremicaProperties;
import org.supremica.automata.templates.*;
import org.supremica.automata.algorithms.*;
import org.supremica.comm.xmlrpc.*;
import org.supremica.gui.editor.*;
import org.supremica.gui.help.*;
import org.supremica.automata.*;
import org.supremica.gui.animators.scenebeans.*;
import org.supremica.log.*;

// -- MF -- Abstract class to save on duplicate code
// -- From this class is instantiated anonymous classes that implement the openFile properly
abstract class FileImporter
{
	FileImporter(JFileChooser fileOpener, Gui gui)
	{
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
		}
	}

	abstract void openFile(Gui gui, File file);
}

// --------------------
public class ActionMan
{
	private static Logger logger = LoggerFactory.createLogger(ActionMan.class);
	private static final int    // instead of using constants later below :)
		FORMAT_UNKNOWN = -1, FORMAT_XML = 1, FORMAT_DOT = 2, FORMAT_DSX = 3, FORMAT_RCP = 4;

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

	// File.New action performed
	public static void fileNew(Gui gui) {}

	// File.NewFromTemplate action performed
	public static void fileNewFromTemplate(Gui gui, TemplateItem item)
	{

		// logger.debug("ActionMan.fileNewFromTemplate Start");
		Automata newAutomata;

		try
		{
			newAutomata = item.createInstance(new VisualProjectFactory());

			gui.addAutomata(newAutomata);

			// logger.debug("ActionMan.fileNewFromTemplate");
		}
		catch (Exception ex)
		{
			JOptionPane.showMessageDialog(gui.getComponent(), "Error while creating the template!", "Alert", JOptionPane.ERROR_MESSAGE);
		}
	}

	// File.Login action performed
	public static void fileLogin(Gui gui)
	{
		FileSecurity fileSecurity = gui.getFileSecurity();

		if (fileSecurity.hasCurrentUser())
		{
			JOptionPane.showMessageDialog(gui.getComponent(), "You are only allowed to log in once. You are logged in as: " + fileSecurity.getCurrentUser(), "Already logged in", JOptionPane.ERROR_MESSAGE);

			return;
		}

		boolean finished = false;
		String newName = null;

		while (!finished)
		{
			newName = JOptionPane.showInputDialog(gui.getComponent(), "Enter your username");

			if ((newName == null) || newName.equals(""))
			{
				JOptionPane.showMessageDialog(gui.getComponent(), "An empty name is not allowed", "alert", JOptionPane.ERROR_MESSAGE);
			}
			else if (fileSecurity.isSuperUser(newName))
			{
				if (fileSecurity.allowSuperUserLogin())
				{
					finished = true;
				}
				else
				{
					JOptionPane.showMessageDialog(gui.getComponent(), "You are not allowed to login as " + newName, "alert", JOptionPane.ERROR_MESSAGE);
				}
			}
			else
			{
				finished = true;
			}
		}

		fileSecurity.setCurrentUser(newName);
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
				Automaton currAutomaton = (Automaton) autIt.next();

				currAutomata.addAutomaton(currAutomaton);
			}

			AlphabetAnalyzer theAnalyzer = new AlphabetAnalyzer(currAutomata);

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

	// Automaton.UpdateInterface action performed
	public static void automatonUpdateInterface_actionPerformed(Gui gui)
	{
		Automata selectedAutomata = gui.getSelectedAutomata();

		if (selectedAutomata.size() < 1)
		{
			JOptionPane.showMessageDialog(gui.getComponent(), "At least one automata must be selected!", "Alert", JOptionPane.ERROR_MESSAGE);

			return;
		}
		if (selectedAutomata.size() > 1)
		{
			JOptionPane.showMessageDialog(gui.getComponent(), "At most one automaton must be selected!", "Alert", JOptionPane.ERROR_MESSAGE);

			return;
		}

		Automaton theInterface = selectedAutomata.getAutomatonAt(0);
		if (theInterface == null)
		{
			JOptionPane.showMessageDialog(gui.getComponent(), "Could not find the interface!", "Alert", JOptionPane.ERROR_MESSAGE);

			return;
		}

		VisualProjectContainer projectContainer = gui.getVisualProjectContainer();
		VisualProject theProject = (VisualProject)projectContainer.getActiveProject();

		UpdateInterface updateInterface = new UpdateInterface(gui.getFrame(), theProject, theInterface);

		try
		{
			updateInterface.execute();
		}
		catch (Exception ex)
		{

			// logger.error(excp.toString());
			gui.error(ex.toString());
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
			Automaton currAutomaton = (Automaton) autIt.next();

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
			Automaton currAutomaton = (Automaton) autIt.next();
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
			Automaton currAutomaton = (Automaton) autIt.next();
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
				gui.getVisualProjectContainer().getActiveProject().addAutomaton(newAutomaton);
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
			Automaton currAutomaton = (Automaton) autIt.next();
			String newAutomatonName = gui.getNewAutomatonName("Please enter a new name", currAutomaton.getName() + "(2)");

			if (newAutomatonName == null)
			{
				return;
			}

			try
			{
				Automaton newAutomaton = new Automaton(currAutomaton);

				newAutomaton.setName(newAutomatonName);
				gui.getVisualProjectContainer().getActiveProject().addAutomaton(newAutomaton);
			}
			catch (Exception ex)
			{
				gui.error("Exception while copying the automaton");
			}
		}
	}

	// ** Delete - remove from the container, clear the selection, mark the project as dirty but do not close the project
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
			Automaton currAutomaton = (Automaton) autIt.next();
			String currAutomatonName = currAutomaton.getName();

			try
			{
				gui.getVisualProjectContainer().getActiveProject().removeAutomaton(currAutomatonName);
			}
			catch (Exception ex)
			{
				gui.error("Exception while removing " + currAutomatonName);
			}
		}

		/*
		 *  And this "closes" the project, should it, really?
		 *  if (theVisualProjectContainer.getSize() == 0)
		 *  {
		 *  theVisualProjectContainer.setProjectFile(null);
		 *  }
		 */

		// and we should have no notion of a "table" here
		// theAutomatonTable.clearSelection();
		gui.clearSelection();
	}

	// ** Export - shouldn't there be an exporter object?
	// it is now (ARASH)
	public static void automataExport(Gui gui)
	{

		// this one comes back in the next function. we need to have duplicates otherwise we would
		// ask for the type and first then complain if nonthing is selected
		Collection selectedAutomata = gui.getSelectedAutomataAsCollection();

		if (selectedAutomata.size() < 1)
		{
			JOptionPane.showMessageDialog(gui.getComponent(), "At least one automata must be selected!", "Alert", JOptionPane.ERROR_MESSAGE);

			return;
		}

		String xmlString = "xml";
		String dotString = "dot";
		String dsxString = "dsx";
		String rcpString = "rcp";                         // ++ ARASH
		Object[] possibleValues =
		{
			xmlString, dotString, dsxString
		};
		Object selectedValue = JOptionPane.showInputDialog(gui.getComponent(), "Export as", "Input", JOptionPane.INFORMATION_MESSAGE, null, possibleValues, possibleValues[0]);

		if (selectedValue == null)
		{
			return;
		}

		int exportMode = FORMAT_UNKNOWN;

		if (selectedValue == xmlString)
		{
			exportMode = FORMAT_XML;
		}
		else if (selectedValue == dotString)
		{
			exportMode = FORMAT_DOT;
		}
		else if (selectedValue == dsxString)
		{
			exportMode = FORMAT_DSX;
		}
		else if (selectedValue == rcpString)
		{
			exportMode = FORMAT_RCP;
		}
		else
		{
			return;
		}

		automataExport(gui, exportMode);
	}

	// Exporter when the type is already known
	// Add new export functions here and to the function above
	public static void automataExport(Gui gui, int exportMode)
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
			Automaton currAutomaton = (Automaton) autIt.next();
			JFileChooser fileExporter = null;

			if (exportMode == FORMAT_XML)
			{
				fileExporter = FileDialogs.getXMLFileExporter();
			}
			else if (exportMode == FORMAT_DOT)
			{
				fileExporter = FileDialogs.getDOTFileExporter();
			}
			else if (exportMode == FORMAT_DSX)
			{
				fileExporter = FileDialogs.getDSXFileExporter();
			}
			else if (exportMode == FORMAT_RCP)
			{
				fileExporter = FileDialogs.getRCPFileExporter();
			}
			else
			{
				return;
			}

			// ARASH: ain't it good to see what we're doin' ??
			fileExporter.setDialogTitle("Save " + currAutomaton.getName() + " as ...");

			if (fileExporter.showSaveDialog(gui.getComponent()) == JFileChooser.APPROVE_OPTION)
			{
				File currFile = fileExporter.getSelectedFile();

				if (currFile != null)
				{
					if (!currFile.isDirectory())
					{
						try
						{
							if (exportMode == FORMAT_XML)
							{
								Automata currAutomata = new Automata();

								currAutomata.addAutomaton(currAutomaton);

								AutomataToXml exporter = new AutomataToXml(currAutomata);

								exporter.serialize(currFile.getAbsolutePath());
							}
							else if (exportMode == FORMAT_DOT)
							{
								AutomatonToDot exporter = new AutomatonToDot(currAutomaton);

								exporter.serialize(currFile.getAbsolutePath());
							}
							else if (exportMode == FORMAT_DSX)
							{
								AutomatonToDsx exporter = new AutomatonToDsx(currAutomaton);

								exporter.serialize(currFile.getAbsolutePath());
							}
							else if (exportMode == FORMAT_RCP)
							{
								AutomatonToRcp exporter = new AutomatonToRcp(currAutomaton);

								exporter.serialize(currFile.getAbsolutePath());
							}
						}
						catch (Exception ex)
						{
							gui.error("Exception while exporting " + currFile.getAbsolutePath() + " : " + ex.toString());
						}
					}
				}
			}
		}
	}

	// ** Extend
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
			Automaton currAutomaton = (Automaton) autIt.next();
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
				gui.getVisualProjectContainer().getActiveProject().addAutomaton(newAutomaton);
			}
			catch (Exception ex)
			{
				gui.error("Exception in AutomataExtend. Automaton: " + currAutomaton.getName());
			}
		}
	}

	// ** Purge
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
			Automaton currAutomaton = (Automaton) autIt.next();
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

	// ** RemovePass - removes all pass events
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
			Automaton currAutomaton = (Automaton) autIt.next();

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

	// ** RemoveSelfLoopArcs
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
			Automaton currAutomaton = (Automaton) autIt.next();

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

	// ** Rename
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
			Automaton currAutomaton = (Automaton) autIt.next();
			String currAutomatonName = currAutomaton.getName();

			try
			{
				String newName = gui.getNewAutomatonName("Enter a new name for " + currAutomatonName, currAutomatonName);

				if (newName != null)
				{
					gui.getVisualProjectContainer().getActiveProject().renameAutomaton(currAutomaton, newName);
				}
			}
			catch (Exception ex)
			{
				gui.error("Exception while renaming the automaton " + currAutomatonName, ex);
			}
		}
	}

	// ** Synchronize - Threaded version
	public static void automataSynchronize_actionPerformed(Gui gui)
	{
		Collection selectedAutomata = gui.getSelectedAutomataAsCollection();

		if (selectedAutomata.size() < 2)
		{
			JOptionPane.showMessageDialog(gui.getComponent(), "At least two automata must be selected!", "Alert", JOptionPane.ERROR_MESSAGE);

			return;
		}

		SynchronizationOptions synchronizationOptions = null;

		try
		{
			synchronizationOptions = new SynchronizationOptions();
		}
		catch (Exception ex)
		{
			JOptionPane.showMessageDialog(gui.getComponent(), "Error constructing synchronizationOptions: " + ex.getMessage(), "Alert", JOptionPane.ERROR_MESSAGE);

			return;
		}

		SynchronizationDialog synchronizationDialog = new SynchronizationDialog(gui.getFrame(), synchronizationOptions);

		synchronizationDialog.show();

		if (!synchronizationOptions.getDialogOK())
		{
			return;
		}

		//-- MF - Isn't this "wrong". An automaton can exist without a name.
		//-- MF - The name is a gui-thing, and should be handled there
		//-- MF - When an unnamed automatan is added, the gui should ask for a name
		/** Yes, it's wrong, let it be handled elsewhere -- But where?? **/
		String newAutomatonName = "Dummy";

		if (synchronizationOptions.buildAutomaton())
		{
			newAutomatonName = gui.getNewAutomatonName("Please enter a new name", "");

			if (newAutomatonName == null)
			{
				return;
			}
		}/**/

		Automata currAutomata = new Automata();
		Iterator autIt = selectedAutomata.iterator();

		while (autIt.hasNext())
		{
			Automaton currAutomaton = (Automaton) autIt.next();
			String currAutomatonName = currAutomaton.getName();

			if (currAutomaton.getInitialState() == null)
			{
				JOptionPane.showMessageDialog(gui.getComponent(), "The automaton " + currAutomatonName + " does not have an initial state!", "Alert", JOptionPane.ERROR_MESSAGE);

				return;
			}

			currAutomata.addAutomaton(currAutomaton);
		}

		AutomataSynchronizerWorker worker = new AutomataSynchronizerWorker(gui, currAutomata, newAutomatonName, synchronizationOptions);
	}

	// ** Synthesize
	public static void automataSynthesize_actionPerformed(Gui gui)
	{
		Collection selectedAutomata = gui.getSelectedAutomataAsCollection();

		if (selectedAutomata.size() < 1)
		{
			JOptionPane.showMessageDialog(gui.getFrame(), "At least one automata must be selected!", "Alert", JOptionPane.ERROR_MESSAGE);

			return;
		}

		SynthesizerOptions synthesizerOptions = new SynthesizerOptions();
		SynthesizerDialog synthesizerDialog = new SynthesizerDialog(gui.getFrame(), selectedAutomata.size(), synthesizerOptions);

		synthesizerDialog.show();

		if (!synthesizerOptions.getDialogOK())
		{
			return;
		}

		Date startDate = new Date();

		if (selectedAutomata.size() > 1)
		{
			SynchronizationOptions syncOptions;

			try
			{
				syncOptions = new SynchronizationOptions(SupremicaProperties.syncNbrOfExecuters(), SynchronizationType.Prioritized, SupremicaProperties.syncInitialHashtableSize(), SupremicaProperties.syncExpandHashtable(), true,

				// SupremicaProperties.syncForbidUncontrollableStates(),
				SupremicaProperties.syncExpandForbiddenStates(), false, false, true, SupremicaProperties.verboseMode(), true, true);
			}
			catch (Exception ex)
			{
				JOptionPane.showMessageDialog(gui.getComponent(), "Invalid synchronizationOptions", "Alert", JOptionPane.ERROR_MESSAGE);

				return;
			}

			Automata currAutomata = new Automata();
			Iterator autIt = selectedAutomata.iterator();

			while (autIt.hasNext())
			{
				Automaton currAutomaton = (Automaton) autIt.next();
				String currAutomatonName = currAutomaton.getName();

				if (currAutomaton.getInitialState() == null)
				{
					JOptionPane.showMessageDialog(gui.getComponent(), "The automaton " + currAutomatonName + " does not have an initial state!", "Alert", JOptionPane.ERROR_MESSAGE);

					return;
				}

				currAutomata.addAutomaton(currAutomaton);
			}

			try
			{
				AutomataSynthesizer synthesizer = new AutomataSynthesizer(gui, currAutomata, syncOptions, synthesizerOptions);

				synthesizer.execute();
			}
			catch (Exception ex)
			{
				gui.error("Exception in AutomataSynthesizer: " + ex);
				ex.printStackTrace();
			}
		}
		else // single automaton selected
		{
			Iterator autIt = selectedAutomata.iterator();

			while (autIt.hasNext())
			{
				Automaton currAutomaton = (Automaton) autIt.next();
				AutomatonSynthesizer synthesizer;

				try
				{
					synthesizer = new AutomatonSynthesizer(gui, currAutomaton, synthesizerOptions);
				}
				catch (Exception e)
				{
					JOptionPane.showMessageDialog(gui.getComponent(), e.toString(), "Alert", JOptionPane.ERROR_MESSAGE);

					return;
				}

				try
				{
					synthesizer.synthesize();
				}
				catch (Exception ex)
				{
					gui.error("Exception in AutomatonSynthesizer. Automaton: " + currAutomaton.getName());
				}
			}
		}

		Date endDate = new Date();

		gui.info("Execution completed after " + (endDate.getTime() - startDate.getTime()) / 1000.0 + " seconds.");
	}

	// Automaton.Verify action performed
	// Threaded version
	public static void automataVerify_actionPerformed(Gui gui)
	{
		VerificationOptions verificationOptions = new VerificationOptions();
		VerificationDialog verificationDialog = new VerificationDialog(gui.getFrame(), verificationOptions);

		verificationDialog.show();

		if (!verificationOptions.getDialogOK())
		{
			return;
		}

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
			Automaton currAutomaton = (Automaton) autIt.next();
			String currAutomatonName = currAutomaton.getName();

			if (currAutomaton.getInitialState() == null)
			{
				JOptionPane.showMessageDialog(gui.getFrame(), "The automaton " + currAutomatonName + " does not have an initial state!", "Alert", JOptionPane.ERROR_MESSAGE);

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
			JOptionPane.showMessageDialog(gui.getFrame(), "Invalid synchronizationOptions", "Alert", JOptionPane.ERROR_MESSAGE);

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
			Automaton currAutomaton = (Automaton) autIt.next();
			String currAutomatonName = currAutomaton.getName();

			try
			{
				AlphabetViewer viewer = gui.getVisualProjectContainer().getActiveProject().getAlphabetViewer(currAutomatonName);
			}
			catch (Exception ex)
			{

				// logger.error("Exception in AlphabetViewer. Automaton: " + currAutomaton.getName());
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

		if (selectedAutomata.size() == 1)
		{

			// One automata selected
			Automaton currAutomaton = (Automaton) autIt.next();
			String currAutomatonName = currAutomaton.getName();

			if (currAutomaton.getInitialState() == null)
			{
				JOptionPane.showMessageDialog(gui.getFrame(), "The automaton " + currAutomatonName + " does not have an initial state!", "Alert", JOptionPane.ERROR_MESSAGE);
			}

			try
			{
				AutomatonExplorer explorer = gui.getVisualProjectContainer().getActiveProject().getAutomatonExplorer(currAutomatonName);
			}
			catch (Exception ex)
			{

				// logger.error("Exception in AutomatonExplorer. Automaton: " + currAutomaton.getName());
				gui.error("Exception in AutomatonExplorer. Automaton: " + currAutomaton.getName());
			}
		}
		else
		{

			// Many automata selected
			Automata currAutomata = new Automata();

			while (autIt.hasNext())
			{
				Automaton currAutomaton = (Automaton) autIt.next();
				String currAutomatonName = currAutomaton.getName();

				if (currAutomaton.getInitialState() == null)
				{
					JOptionPane.showMessageDialog(gui.getFrame(), "The automaton " + currAutomatonName + " does not have an initial state!", "Alert", JOptionPane.ERROR_MESSAGE);

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

				// logger.error("Exception in AutomataExplorer.");
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
			Automaton currAutomaton = (Automaton) autIt.next();
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
				gui.getVisualProjectContainer().getActiveProject().addAutomaton(newAutomaton);
			}
			catch (Exception ex)
			{

				// logger.error("Exception in AutomatonMinimize. Automaton: " + currAutomaton.getName());
				gui.error("Exception in AutomatonMinimize. Automaton: " + currAutomaton.getName());
			}
		}
	}

	// Automaton.Status action performed
	public static void automatonStatus_actionPerformed(Gui gui)
	{

		// logger.info("Number of automata: " + gui.getVisualProjectContainer().getSize());
		gui.info("Number of automata: " + gui.getVisualProjectContainer().getActiveProject().getNbrOfAutomata());

		Collection selectedAutomata = gui.getSelectedAutomataAsCollection();

		if (selectedAutomata.size() < 1)
		{
			return;
		}

		gui.info("Number of selected automata: " + selectedAutomata.size());

		for (Iterator autIt = selectedAutomata.iterator(); autIt.hasNext(); )
		{
			Automaton currAutomaton = (Automaton) autIt.next();
			StringBuffer statusStr = new StringBuffer();

			statusStr.append("Status for automaton: " + currAutomaton.getName());
			if(currAutomaton.getComment() != null && !currAutomaton.getComment().equals(""))
			{
				statusStr.append("\nComment: \"" + currAutomaton.getComment() + "\"");
			}
			statusStr.append("\n\tNumber of states: " + currAutomaton.nbrOfStates());
			statusStr.append("\n\tNumber of events: " + currAutomaton.nbrOfEvents());
			statusStr.append("\n\tNumber of accepting states: " + currAutomaton.nbrOfAcceptingStates());
			statusStr.append("\n\tNumber of forbidden states: " + currAutomaton.nbrOfForbiddenStates());

			int acceptingAndForbiddenStates = currAutomaton.nbrOfAcceptingAndForbiddenStates();

			if (acceptingAndForbiddenStates > 0)
			{
				statusStr.append("\n\tNumber of accepting and forbidden states: " + acceptingAndForbiddenStates);
			}

			// logger.info(statusStr.toString());
			gui.info(statusStr.toString());
		}

		if(selectedAutomata.size() > 1)
		{
			double potentialNumberOfStates = 1.0;

			for (Iterator autIt = selectedAutomata.iterator(); autIt.hasNext(); )
			{
				Automaton currAutomaton = (Automaton) autIt.next();

				potentialNumberOfStates = potentialNumberOfStates * currAutomaton.nbrOfStates();
			}

			gui.info("\n\tNumber of potential states: " + new Double(potentialNumberOfStates).longValue());
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
			Automaton currAutomaton = (Automaton) autIt.next();
			String currAutomatonName = currAutomaton.getName();
			int maxNbrOfStates = SupremicaProperties.getDotMaxNbrOfStatesWithoutWarning();

			if (maxNbrOfStates < currAutomaton.nbrOfStates())
			{
				StringBuffer msg = new StringBuffer();

				msg.append(currAutomatonName + " has " + currAutomaton.nbrOfStates() + " states. ");
				msg.append("It is not recommended to display an automaton with more than " + maxNbrOfStates + " states.\n");
				msg.append("Do you want to abort viewing?");

				int res = JOptionPane.showOptionDialog(gui.getFrame(), msg, "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, null, null);

				if (res == 0)
				{

					// Abort - YES
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
				AutomatonViewer viewer = gui.getVisualProjectContainer().getActiveProject().getAutomatonViewer(currAutomatonName);
			}
			catch (Exception ex)
			{

				// logger.error("Exception in AutomatonViewer. Automaton: " + currAutomaton.getName());
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
		if (SupremicaProperties.fileAllowQuit())
		{
			System.exit(0);
		}
		else
		{
			fileClose(gui);
		}
	}

	// File.Close action performed
	public static void fileClose(Gui gui)
	{
		if (SupremicaProperties.fileAllowQuit())
		{
			System.exit(0);
		}
		else
		{
			gui.close();
		}
	}

	// ++ ARASH:
	public static void fileExportRCP(Gui gui)
	{
		automataExport(gui, FORMAT_RCP);
	}

	public static void fileExportDesco(Gui gui)
	{
		automataExport(gui, FORMAT_DSX);
	}

	public static void fileExportDot(Gui gui)
	{
		automataExport(gui, FORMAT_DOT);
	}

	public static void fileExportSupremica(Gui gui)
	{
		automataExport(gui, FORMAT_XML);
	}

	// -------------- TODO: ADD EXPORTES FOR THESE TOO ------------------------------------
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

	public static void fileImportDesco(Gui gui) {}

	public static void fileImportValid(Gui gui)
	{
		new FileImporter(FileDialogs.getVALIDFileImporter(), gui)    // anonymous class
		{
			void openFile(Gui g, File f)
			{
				importValidFile(g, f);
			}
		};
	}

	// Aldebaran format, a simple format for specifying des
	public static void fileImportAut(Gui gui)
	{
		new FileImporter(FileDialogs.getAutFileImporter(), gui)    // anonymous class
		{
			void openFile(Gui g, File f)
			{
				importAutFile(g, f);
			}
		};
	}

	// File.Open action performed
	public static void fileOpen(Gui gui)
	{
		new FileImporter(FileDialogs.getXMLFileImporter(), gui)    // anonymous class
		{
			void openFile(Gui g, File f)
			{
				openProjectXMLFile(g, f);
			}
		};
	}

	// Why this indirection?
	public static void openFile(Gui gui, File file)
	{
		openProjectXMLFile(gui, file);
	}

	public static void openProjectXMLFile(Gui gui, File file)
	{
		Project currProject = null;

		gui.info("Opening " + file.getAbsolutePath() + " ...");

		try
		{
			ProjectBuildFromXml builder = new ProjectBuildFromXml(new VisualProjectFactory());

			currProject = builder.build(file);
		}
		catch (Exception e)
		{

			// this exception is caught while opening
			gui.error("Error while opening " + file.getAbsolutePath() + " " + e.getMessage());

			return;
		}

		FileSecurity fileSecurity = gui.getFileSecurity();

		if (SupremicaProperties.generalUseSecurity())
		{
			if (!fileSecurity.allowOpening(currProject))
			{
				JOptionPane.showMessageDialog(gui.getComponent(), "You are not allowed to open this file", "alert", JOptionPane.ERROR_MESSAGE);

				return;
			}
		}

		// We should always check owner and hash when it is present
		if (!((currProject.getOwner() == null) && (currProject.getHash() == null)) &&!fileSecurity.hasCorrectHash(currProject))
		{
			JOptionPane.showMessageDialog(gui.getComponent(), "The project has an invalid hash", "alert", JOptionPane.WARNING_MESSAGE);
		}

		int nbrOfAutomataBeforeOpening = gui.getVisualProjectContainer().getActiveProject().getNbrOfAutomata();

		try
		{
			int nbrOfAddedAutomata = gui.addAutomata(currProject);

			gui.info("Successfully opened and added " + nbrOfAddedAutomata + " automata.");
		}
		catch (Exception excp)
		{
			gui.error("Error adding automata " + file.getAbsolutePath() + " " + excp.getMessage());

			return;
		}

		if (nbrOfAutomataBeforeOpening == 0)
		{
			String projectName = currProject.getName();

			if (projectName != null)
			{
				gui.getVisualProjectContainer().getActiveProject().setName(projectName);
				gui.info("Project name changed to \"" + projectName + "\"");
				gui.getVisualProjectContainer().getActiveProject().updateFrameTitles();
			}
		}

		if (nbrOfAutomataBeforeOpening > 0)
		{
			File projectFile = gui.getVisualProjectContainer().getActiveProject().getProjectFile();

			if (projectFile != null)
			{
				gui.getVisualProjectContainer().getActiveProject().setProjectFile(null);
			}
		}
		else
		{
			gui.getVisualProjectContainer().getActiveProject().setProjectFile(file);
		}
	}

	// File.Save action performed
	public static void fileSave(Gui gui)
	{
		File currFile = gui.getVisualProjectContainer().getActiveProject().getProjectFile();

		if (currFile == null)
		{
			fileSaveAs(gui);

			return;
		}

		Automata currAutomata = gui.getVisualProjectContainer().getActiveProject();

		if (currFile != null)
		{
			if (!currFile.isDirectory())
			{
				try
				{
					FileSecurity fileSecurity = gui.getFileSecurity();

					if (SupremicaProperties.generalUseSecurity())
					{
						if (!fileSecurity.hasCurrentUser())
						{
							JOptionPane.showMessageDialog(gui.getComponent(), "You must be logged in to save!", "Alert", JOptionPane.ERROR_MESSAGE);

							return;
						}

						currAutomata.setOwner(fileSecurity.getCurrentUser());
						currAutomata.setHash(currAutomata.computeHash());
					}

					AutomataToXml exporter = new AutomataToXml(currAutomata);

					exporter.serialize(currFile.getAbsolutePath());
				}
				catch (Exception ex)
				{

					// logger.error("Exception while saveAs " + currFile.getAbsolutePath());
					gui.error("Exception while saveAs " + currFile.getAbsolutePath());
				}
			}
		}
	}

	// File.SaveAs action performed
	public static void fileSaveAs(Gui gui)
	{
		FileSecurity fileSecurity = gui.getFileSecurity();

		if (SupremicaProperties.generalUseSecurity())
		{
			if (!fileSecurity.hasCurrentUser())
			{
				JOptionPane.showMessageDialog(gui.getComponent(), "You must be logged in to save!", "Alert", JOptionPane.ERROR_MESSAGE);

				return;
			}
		}

		JFileChooser fileSaveAs = FileDialogs.getXMLFileSaveAs();
		String projectName = gui.getVisualProjectContainer().getActiveProject().getName();

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
				gui.getVisualProjectContainer().getActiveProject().setProjectFile(currFile);
				fileSave(gui);
			}
		}
	}

	public static void importAutFile(Gui gui, File file)
	{
		gui.info("Importing " + file.getAbsolutePath() + " ...");

		try
		{
			Automata currAutomata = null;    // AutomataBuildFromAut.build(file);
			int nbrOfAddedAutomata = gui.addAutomata(currAutomata);

			gui.info("Successfully imported " + nbrOfAddedAutomata + " automata.");
		}
		catch (Exception e)
		{
			gui.error("Error while importing " + file.getAbsolutePath() + " " + e.getMessage());

			return;
		}
	}

	public static void importValidFile(Gui gui, File file)
	{

		// logger.info("Importing " + file.getAbsolutePath() + " ...");
		gui.info("Importing " + file.getAbsolutePath() + " ...");

		try
		{
			AutomataBuildFromVALID builder = new AutomataBuildFromVALID(new VisualProjectFactory());
			Automata currAutomata = builder.build(file);
			int nbrOfAddedAutomata = gui.addAutomata(currAutomata);

			gui.info("Successfully imported " + nbrOfAddedAutomata + " automata.");
		}
		catch (Exception e)
		{
			gui.error("Error while importing " + file.getAbsolutePath() + " " + e.getMessage());

			return;
		}
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
			Automaton currAutomaton = (Automaton) autIt.next();

			try
			{
				AlphabetNormalize alphabetNormalize = new AlphabetNormalize(currAutomaton);

				alphabetNormalize.execute();
			}
			catch (Exception ex)
			{

				// logger.error("Exception in AlphabetNormalizer. Automaton: " + currAutomaton.getName());
				// logger.error(ex);
				gui.error("Exception in AlphabetNormalizer. Automaton: " + currAutomaton.getName(), ex);
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

	// Find States... action selected
	public static void findStates_action(Gui gui)
	{
		VisualProject theProject = gui.getVisualProjectContainer().getActiveProject();
		Automata selectedAutomata = gui.getSelectedAutomata();
		// gui.info("Nbr of selected automata: " + selectedAutomata.size());
		FindStates find_states = new FindStates(theProject, selectedAutomata);

		try
		{
			find_states.execute();
		}
		catch (Exception ex)
		{
			// logger.error(excp.toString());
			gui.error(ex.toString());
		}
	}

	// Delete All - this really implements Close Project
	public static void automataDeleteAll_actionPerformed(Gui gui)
	{
		gui.getVisualProjectContainer().getActiveProject().clear();
		gui.clearSelection();
		gui.getVisualProjectContainer().getActiveProject().setProjectFile(null);
	}

	// TestCases... - open the test cases dialog, and add the result to the current set of automata
	public static void testCases(Gui gui)
		throws Exception
	{
		TestCasesDialog testCasesDialog = new TestCasesDialog(gui.getFrame());

		testCasesDialog.show();

		Automata automata = testCasesDialog.getAutomata();

		if (automata != null)
		{
			gui.addAutomata(automata);
		}
	}

	// Animations
	public static void animator(Gui gui, AnimationItem item)
	{
		try
		{
			Animator animator = item.createInstance();

			animator.setVisible(true);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	// Generate SattLine SFCs
	public static void AutomataToSattLineSFC(Gui gui)
	{
		Automata selectedAutomata = gui.getSelectedAutomata();

		if (selectedAutomata.size() < 1)
		{
			JOptionPane.showMessageDialog(gui.getComponent(), "At least one automaton must be selected!", "Alert", JOptionPane.ERROR_MESSAGE);

			return;
		}
		if (selectedAutomata.hasSelfLoop())
		{
			JOptionPane.showMessageDialog(gui.getComponent(), "Self-loops are not supported in SFC. The ST and IL mode can handle self-loops!", "Not supported", JOptionPane.ERROR_MESSAGE);
			return;
		}
		if (!selectedAutomata.isAllEventsPrioritized())
		{
			JOptionPane.showMessageDialog(gui.getComponent(), "All events must prioritized in this mode. The ST and IL mode can handle non-prioritized events!", "Not supported", JOptionPane.ERROR_MESSAGE);
			return;
		}

		JFileChooser fileExporter = FileDialogs.getSFileExporter();

		if (fileExporter.showSaveDialog(gui.getComponent()) == JFileChooser.APPROVE_OPTION)
		{
			File currFile = fileExporter.getSelectedFile();

			if (currFile != null)
			{
				if (!currFile.isDirectory())
				{
					String prefixName = null;
					String pathName = currFile.getAbsolutePath();

					if (pathName.endsWith(".s"))
					{
						prefixName = pathName.substring(0, pathName.length() - 2);
					}
					else
					{
						prefixName = pathName;
					}
					try
					{
						AutomataToSattLineSFC exporter = new AutomataToSattLineSFC(selectedAutomata);


						PrintWriter pw_s = new PrintWriter(new FileWriter(prefixName + ".s"));
						PrintWriter pw_g = new PrintWriter(new FileWriter(prefixName + ".g"));
						PrintWriter pw_l = new PrintWriter(new FileWriter(prefixName + ".l"));
						PrintWriter pw_p = new PrintWriter(new FileWriter(prefixName + ".p"));

						exporter.serialize_s(pw_s);
						exporter.serialize_g(pw_g);
						exporter.serialize_l(pw_l);
						exporter.serialize_p(pw_p);
						pw_s.close();
						pw_g.close();
						pw_l.close();
						pw_p.close();
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
						gui.error("Exception while generating SattLine code to files " + prefixName + "{\".s\", \".g\", \".l\", \".p\"}");
						return;
					}
					logger.info("SattLine files successfully generated at " + prefixName + "{\".s\", \".g\", \".l\", \".p\"}");

				}
			}
		}
	}

	// Generate ABB Control Builder SFCs
	public static void AutomataToControlBuilderSFC(Gui gui)
	{
		Automata selectedAutomata = gui.getSelectedAutomata();

		if (selectedAutomata.size() < 1)
		{
			JOptionPane.showMessageDialog(gui.getComponent(), "At least one automaton must be selected!", "Alert", JOptionPane.ERROR_MESSAGE);

			return;
		}
		if (selectedAutomata.hasSelfLoop())
		{
			JOptionPane.showMessageDialog(gui.getComponent(), "Self-loops are not supported in SFC. The ST and IL mode can handle self-loops!", "Not supported", JOptionPane.ERROR_MESSAGE);
			return;
		}
		if (!selectedAutomata.isAllEventsPrioritized())
		{
			JOptionPane.showMessageDialog(gui.getComponent(), "All events must prioritized in this mode. The ST and IL mode can handle non-prioritized events!", "Not supported", JOptionPane.ERROR_MESSAGE);
			return;
		}
		JFileChooser fileExporter = FileDialogs.getPRJFileExporter();

		if (fileExporter.showSaveDialog(gui.getComponent()) == JFileChooser.APPROVE_OPTION)
		{
			File currFile = fileExporter.getSelectedFile();

			if (currFile != null)
			{
				if (!currFile.isDirectory())
				{
					String pathName = currFile.getAbsolutePath();
					String prefixName = null;
					if (pathName.endsWith(".prj"))
					{
						prefixName = pathName.substring(0, pathName.length() - 4);
					}
					else
					{
						prefixName = pathName;
					}
					File appFile = new File(prefixName + ".app");
					File prjFile = new File(prefixName + ".prj");
					try
					{
						AutomataToControlBuilderSFC exporter = new AutomataToControlBuilderSFC(selectedAutomata);

						PrintWriter pw_prj = new PrintWriter(new FileWriter(prjFile));

						exporter.serialize_app(appFile);
						exporter.serialize_prj(pw_prj);

						pw_prj.close();
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
						gui.error("Exception while generating ControlBuilder code to files " + prefixName + "{\".prj\", \".app\"}");
						return;
					}
					logger.info("ControlBuilder files successfully generated at " + prefixName + "{\".prj\", \".app\"}");
				}
			}
		}
	}


	// Generate 1131 Structured Text
	public static void AutomataTo1131ST(Gui gui)
	{
		Automata selectedAutomata = gui.getSelectedAutomata();

		if (selectedAutomata.size() < 1)
		{
			JOptionPane.showMessageDialog(gui.getComponent(), "At least one automaton must be selected!", "Alert", JOptionPane.ERROR_MESSAGE);

			return;
		}

		JFileChooser fileExporter = FileDialogs.getSTFileExporter();

		if (fileExporter.showSaveDialog(gui.getComponent()) == JFileChooser.APPROVE_OPTION)
		{
			File currFile = fileExporter.getSelectedFile();

			if (currFile != null)
			{
				if (!currFile.isDirectory())
				{
					String prefixName = null;

					try
					{

						SynchronizationOptions synchronizationOptions = new SynchronizationOptions();
						AutomataToIEC1131 exporter = new AutomataToIEC1131(selectedAutomata, synchronizationOptions);

						PrintWriter theWriter = new PrintWriter(new FileWriter(currFile));

						exporter.serializeStructuredText(theWriter);

						theWriter.close();

					}
					catch (Exception ex)
					{
						ex.printStackTrace();
						gui.error("Exception while generating 1131 Structured text code to file " + currFile.getAbsolutePath());
					}
				}
			}
		}
	}

	// Generate 1131 Instruction List
	public static void AutomataTo1131IL(Gui gui)
	{
		Automata selectedAutomata = gui.getSelectedAutomata();

		if (selectedAutomata.size() < 1)
		{
			JOptionPane.showMessageDialog(gui.getComponent(), "At least one automaton must be selected!", "Alert", JOptionPane.ERROR_MESSAGE);

			return;
		}

		JFileChooser fileExporter = FileDialogs.getILFileExporter();

		if (fileExporter.showSaveDialog(gui.getComponent()) == JFileChooser.APPROVE_OPTION)
		{
			File currFile = fileExporter.getSelectedFile();

			if (currFile != null)
			{
				if (!currFile.isDirectory())
				{
					String prefixName = null;

					try
					{

						SynchronizationOptions synchronizationOptions = new SynchronizationOptions();
						AutomataToIEC1131 exporter = new AutomataToIEC1131(selectedAutomata, synchronizationOptions);

						PrintWriter theWriter = new PrintWriter(new FileWriter(currFile));

						exporter.serializeInstructionList(theWriter);

						theWriter.close();

					}
					catch (Exception ex)
					{
						ex.printStackTrace();
						gui.error("Exception while generating 1131 Instruction list code to file " + currFile.getAbsolutePath());
					}
				}
			}
		}
	}
}

// ActionMan
