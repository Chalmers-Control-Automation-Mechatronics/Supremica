
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
import javax.swing.*;
import java.util.*;
import java.io.*;
import org.supremica.properties.SupremicaProperties;
import org.supremica.automata.templates.*;
import org.supremica.automata.algorithms.*;
import org.supremica.automata.algorithms.minimization.*;
import org.supremica.automata.*;
import org.supremica.gui.animators.scenebeans.*;

//import org.supremica.gui.animators.tsim.*;
import org.supremica.gui.automataExplorer.AutomataExplorer;
import org.supremica.gui.simulator.SimulatorExecuter;
import org.supremica.external.robotCoordination.*;
import org.supremica.external.robotCoordinationABB.*;
import org.supremica.external.shoefactory.plantBuilder.*;
import org.supremica.external.shoefactory.Configurator.*;
import org.supremica.log.*;
import org.supremica.automata.IO.*;
//import org.supremica.automata.algorithms.RobotStudioLink;
import org.supremica.gui.useractions.*;
import org.supremica.gui.texteditor.TextFrame;
import org.swixml.SwingEngine;

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

	// Ugly fixx here. We need a good way to globally get at the selected automata, the current project etc
	// gui here is filled in by (who?)
	public static Gui gui = null;
	public static final LanguageRestrictor languageRestrictor = new LanguageRestrictor();
	public static final EventHider eventHider = new EventHider();
	public static final FindStates findStates = new FindStates();
	public static final StateEnumerator stateEnumerator = new StateEnumerator();
	public static final HelpAction helpAction = new HelpAction();
	public static final OpenAction openAction = new OpenAction();    // defined in MainToolBar (just for fun :-)
	public static final SaveAction saveAction = new SaveAction();
	public static final SaveAsAction saveAsAction = new SaveAsAction();
	public static final SynthesizeAction synthesizeAction = new SynthesizeAction();
	public static final OpenJGrafchartAction openJGrafchartAction = new OpenJGrafchartAction();
	public static final UpdateFromJGrafchartAction updateFromJGrafchartAction = new UpdateFromJGrafchartAction();

	public static Gui getGui()
	{
		return gui;
	}

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
			catch (Exception ex)
			{
				JOptionPane.showMessageDialog(parent, "Not a valid integer", "Alert", JOptionPane.ERROR_MESSAGE);
				logger.debug(ex.getStackTrace());
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

			gui.addProject((Project) newAutomata);

			// logger.debug("ActionMan.fileNewFromTemplate");
		}
		catch (Exception ex)
		{
			JOptionPane.showMessageDialog(gui.getComponent(), "Error while creating the template!", "Alert", JOptionPane.ERROR_MESSAGE);
			logger.debug(ex.getStackTrace());
		}
	}

	// File.NewFromTemplate action performed
	//public static void updateFromJGrafchart(Gui gui)
	//{
	//}
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
			else if (fileSecurity.isSuperuser(newName))
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
		Automata selectedAutomata = gui.getSelectedAutomata();

		if (!selectedAutomata.sanityCheck(gui, 2, false, false, true, true))
		{
			return;
		}

		// Analyze the alphabets
		AlphabetAnalyzer theAnalyzer = new AlphabetAnalyzer(selectedAutomata);

		try
		{
			theAnalyzer.execute();
		}
		catch (Exception ex)
		{
			logger.error("Exception in AlphabetAnalyzer ", ex);
			logger.debug(ex.getStackTrace());
		}

		gui.info("Size of union alphabet: " + selectedAutomata.getUnionAlphabet().size());

		/*
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
						logger.error("Exception in AlphabetAnalyzer ", ex);
						logger.debug(ex.getStackTrace());
				}
		}
		else
		{
				JOptionPane.showMessageDialog(gui.getComponent(), "At least two automata must be selected!", "Alert", JOptionPane.ERROR_MESSAGE);
		}
		*/
	}

	// Automaton.UpdateInterface action performed
	//
	// What is this method used for? When? /Hguo.
	public static void automatonUpdateInterface_actionPerformed(Gui gui)
	{
		Automata selectedAutomata = gui.getSelectedAutomata();

		if (selectedAutomata.size() < 1)
		{
			JOptionPane.showMessageDialog(gui.getComponent(), "At least one automaton must be selected!", "Alert", JOptionPane.ERROR_MESSAGE);

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
		VisualProject theProject = (VisualProject) projectContainer.getActiveProject();
		UpdateInterface updateInterface = new UpdateInterface(gui.getFrame(), theProject, theInterface);

		try
		{
			updateInterface.execute();
		}
		catch (Exception ex)
		{
			logger.error(ex.toString());
			logger.debug(ex.getStackTrace());
		}
	}

	// Automata.AddSelfLoopArcs action performed
	public static void automataAddSelfLoopArcs_actionPerformed(Gui gui)
	{
		Automata selectedAutomata = gui.getSelectedAutomata();

		if (!selectedAutomata.sanityCheck(gui, 1))
		{
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
				logger.error("Exception in AutomataAddSelfLoopArcs. Automaton: " + currAutomaton.getName(), ex);
				logger.debug(ex.getStackTrace());
			}
		}
	}

	// Automaton.AllAccepting action performed
	public static void automataAllAccepting_actionPerformed(Gui gui)
	{
		Automata selectedAutomata = gui.getSelectedAutomata();

		if (!selectedAutomata.sanityCheck(gui, 1))
		{
			return;
		}

		/*
		Collection selectedAutomata = gui.getSelectedAutomataAsCollection();

		if (selectedAutomata.size() < 1)
		{
				JOptionPane.showMessageDialog(gui.getComponent(), "At least one automaton must be selected!", "Alert", JOptionPane.ERROR_MESSAGE);

				return;
		}
		*/
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
				logger.error("Exception in AutomataAllAccepting. Automaton: " + currAutomaton.getName(), ex);
				logger.debug(ex.getStackTrace());
			}
		}
	}

	// Automaton.Complement action performed
	public static void automataComplement_actionPerformed(Gui gui)
	{
		Automata selectedAutomata = gui.getSelectedAutomata();

		if (!selectedAutomata.sanityCheck(gui, 1))
		{
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
				logger.error("Exception in AutomatonComplement. Automaton: " + currAutomaton.getName(), ex);
				logger.debug(ex.getStackTrace());
			}
		}
	}

	// Automata.Copy action performed
	public static void automataCopy_actionPerformed(Gui gui)
	{
		Automata selectedAutomata = gui.getSelectedAutomata();

		if (!selectedAutomata.sanityCheck(gui, 1))
		{
			return;
		}

		/*
		  Collection selectedAutomata = gui.getSelectedAutomataAsCollection();

		if (selectedAutomata.size() < 1)
		{
				JOptionPane.showMessageDialog(gui.getComponent(), "At least one automaton must be selected!", "Alert", JOptionPane.ERROR_MESSAGE);

				return;
		}
		*/
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
				logger.error("Exception while copying the automaton ", ex);
				logger.debug(ex.getStackTrace());
			}
		}
	}

	// ** Delete - remove from the container, clear the selection,
	// mark the project as dirty but do not close the project
	public static void automataDelete_actionPerformed(Gui gui)
	{
		Automata selectedAutomata = gui.getSelectedAutomata();

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
				logger.error("Exception while removing " + currAutomatonName, ex);
				logger.debug(ex.getStackTrace());
			}
		}

		gui.clearSelection();
	}

	/**
	 * Moves selected automata one step up or down in the list
	 *
	 * @param directionIsUp Boolean deciding the direction of the move, true->up false->down.
	 * @param allTheWay Boolean deciding is the move is all the way to the top or bottom.
	 */
	public static void automataMove_actionPerformed(Gui gui, boolean directionIsUp, boolean allTheWay)
	{
		Automata selectedAutomata = gui.getSelectedAutomata();
		if (!selectedAutomata.sanityCheck(gui, 1))
		{
			return;
		}

		Project theProject = gui.getVisualProjectContainer().getActiveProject();

		if (selectedAutomata.size() == theProject.size())
		{
			JOptionPane.showMessageDialog(gui.getComponent(), "No point in moving all automata, right?", "Alert", JOptionPane.ERROR_MESSAGE);

			return;
		}

		// selectionIndices are the indices of the automata that should be selected after the move!
		int[] selectionIndices = new int[selectedAutomata.size()];
		int index = 0;

		if (allTheWay)
		{
			// Move all the way...
			if (directionIsUp)
			{
				int i = 0;

				for (AutomatonIterator autIt = selectedAutomata.iterator();
						autIt.hasNext(); )
				{
					theProject.moveAutomaton(autIt.nextAutomaton(), i);

					selectionIndices[index++] = i++;
				}
			}
			else
			{
				int i = theProject.size() - 1;

				for (AutomatonIterator autIt = selectedAutomata.backwardsIterator();
						autIt.hasNext(); )
				{
					theProject.moveAutomaton(autIt.nextAutomaton(), i);

					selectionIndices[index++] = i--;
				}
			}
		}
		else
		{

			// Avoid automata that can't move any further
			Iterator autIt;

			if (directionIsUp)
			{
				autIt = selectedAutomata.iterator();

				// Avoid the automata already at the top!
				int i = 0;

				while (selectedAutomata.containsAutomaton(theProject.getAutomatonAt(i)))
				{
					autIt.next();

					selectionIndices[index++] = i++;
				}
			}
			else
			{
				autIt = selectedAutomata.backwardsIterator();

				// Avoid the automata already at the bottom!
				int i = theProject.size() - 1;

				while (selectedAutomata.containsAutomaton(theProject.getAutomatonAt(i)))
				{
					autIt.next();

					selectionIndices[index++] = i--;
				}
			}

			// Move automata that can move! The thing is that we're using the same iterator here and above!!!!!!!!!!
			Automaton currAutomaton;

			while (autIt.hasNext())
			{
				currAutomaton = (Automaton) autIt.next();

				theProject.moveAutomaton(currAutomaton, directionIsUp);

				selectionIndices[index++] = theProject.getAutomatonIndex(currAutomaton);
			}
		}

		// Update the selection
		gui.clearSelection();
		gui.selectAutomata(selectionIndices);
	}

	// This is baaad!
	private static final int    // instead of using constants later below :)
		 FORMAT_UNKNOWN = -1, FORMAT_XML = 1, FORMAT_DOT = 2, FORMAT_DSX = 3,
		 FORMAT_SP = 4, FORMAT_HTML = 5, FORMAT_XML_DEBUG = 6,
		 FORMAT_DOT_DEBUG = 7, FORMAT_DSX_DEBUG = 8, FORMAT_SP_DEBUG = 9,
		 FORMAT_HTML_DEBUG = 10, FORMAT_FSM = 11, FORMAT_FSM_DEBUG = 12,
		 FORMAT_PCG = 13, FORMAT_PCG_DEBUG = 14,    // ARASH: process communication graphs
		 FORMAT_SSPC = 15;    // ARASH: Sanchez SSPC tool

	// This class should really act as a factory for exporter objects, but that
	// would mean rewriting the entire export/saveAs functionality. Should I bother?
	static class ExportDialog

	//      extends JDialog
	{
		private static final String xmlString = "xml";
		private static final String spString = "sp";
		private static final String dotString = "dot";
		private static final String dsxString = "dsx";
		private static final String htmlString = "html";
		private static final String fsmString = "fsm";
		private static final String pcgString = "pcg";
		private static final String sspcString = "sspc";
		private static final Object[] possibleValues = { xmlString, spString,
														 dotString, dsxString,
														 fsmString,
														 htmlString,
														 pcgString,
														 sspcString };
		private JOptionPane pane = null;
		private JDialog dialog = null;
		private JCheckBox checkbox = null;
		private Object selectedValue = null;

		ExportDialog(Frame comp)
		{
			this.pane = new JOptionPane("Export as::", JOptionPane.INFORMATION_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null,    // icon
										null,    // options
										null);    // initialValue

			pane.setWantsInput(true);
			pane.setSelectionValues(possibleValues);
			pane.setInitialSelectionValue(possibleValues[0]);
			pane.setComponentOrientation(((comp == null)
										  ? JOptionPane.getRootFrame()
										  : comp).getComponentOrientation());
			pane.selectInitialValue();

			this.checkbox = new JCheckBox("Export to debugview");

			pane.add(checkbox);

			// int style = styleFromMessageType(JOptionPane.INFORMATION_MESSAGE);
			dialog = pane.createDialog(comp, "Export");
		}

		public void show()
		{

			// this.selectedValue = JOptionPane.showInputDialog(comp, "Export as", "Export", JOptionPane.INFORMATION_MESSAGE, null, possibleValues, possibleValues[0]);
			dialog.setVisible(true);
			dialog.dispose();

			// Is this the right thing to do? It seems to work, but the manuals...
			if (((Integer) pane.getValue()).intValue() == JOptionPane.CANCEL_OPTION)
			{
				selectedValue = null;

				return;
			}

			selectedValue = pane.getInputValue();
		}

		public boolean wasCancelled()
		{
			return selectedValue == null;
		}

		// public Exporter getExporter()
		public int getExportMode()
		{
			if (selectedValue == xmlString)
			{
				if (checkbox.isSelected())
				{
					return FORMAT_XML_DEBUG;
				}

				return FORMAT_XML;    // Should return an XmlExporter object
			}
			else if (selectedValue == dotString)
			{
				if (checkbox.isSelected())
				{
					return FORMAT_DOT_DEBUG;
				}

				return FORMAT_DOT;    // Should return a DotExporter object
			}
			else if (selectedValue == dsxString)
			{
				if (checkbox.isSelected())
				{
					return FORMAT_DSX_DEBUG;
				}

				return FORMAT_DSX;    // Should return a DsxExporter object
			}
			else if (selectedValue == fsmString)
			{
				if (checkbox.isSelected())
				{
					return FORMAT_FSM_DEBUG;
				}

				return FORMAT_FSM;    // Should return a FsmExporter object
			}
			else if (selectedValue == spString)
			{
				if (checkbox.isSelected())
				{
					return FORMAT_SP_DEBUG;
				}

				return FORMAT_SP;    // Should return a SpExporter object
			}
			else if (selectedValue == htmlString)
			{
				if (checkbox.isSelected())
				{
					return FORMAT_HTML_DEBUG;
				}

				return FORMAT_HTML;    // Should return a HtmlExporter object
			}
			else if (selectedValue == pcgString)
			{
				return (checkbox.isSelected())
					   ? FORMAT_PCG_DEBUG
					   : FORMAT_PCG;
			}
			else if (selectedValue == sspcString)
			{
				return FORMAT_SSPC;    // no debugview here (multiple files)
			}
			else
			{
				return FORMAT_UNKNOWN;
			}
		}
	}

	// ** Export - shouldn't there be an exporter object?
	// it is now (ARASH)
	public static void automataExport(Gui gui)
	{
		Automata selectedAutomata = gui.getSelectedAutomata();

		if (!selectedAutomata.sanityCheck(gui, 1))
		{
			return;
		}

		ExportDialog dlg = new ExportDialog(gui.getFrame());

		dlg.show();

		if (dlg.wasCancelled())
		{
			return;
		}

		int exportMode = dlg.getExportMode();

		if (exportMode != FORMAT_UNKNOWN)
		{
			automataExport(gui, exportMode);
		}
	}

	// Exporter when the type is already known
	// Add new export functions here and to the function above
	// MF: It's not that simple. The code below defeats that purpose. Where are the exporter objects?
	// OO was invented just to avoid the type of code below. It's a maintenance nightmare!!
	public static void automataExport(Gui gui, int exportMode)
	{
		Automata selectedAutomata = gui.getSelectedAutomata();

		if (!selectedAutomata.sanityCheck(gui, 1))
		{
			return;
		}

		if ((exportMode == FORMAT_FSM_DEBUG) || (exportMode == FORMAT_FSM))
		{    // UMDES cannot deal with forbidden states
			if (selectedAutomata.hasForbiddenState())
			{
				JOptionPane.showMessageDialog(gui.getComponent(), "UMDES cannot handle forbidden states", "Alert", JOptionPane.ERROR_MESSAGE);

				return;
			}
		}

		/*
		Automata selectedAutomata = gui.getSelectedAutomata();

		if (selectedAutomata.size() < 1)
		{
				JOptionPane.showMessageDialog(gui.getComponent(), "At least one automaton must be selected!", "Alert", JOptionPane.ERROR_MESSAGE);

				return;
		}
		*/

		// Take care of the new debug stuff first. This is really silly.
		// Proper design would have solved this problem
		if (exportMode == FORMAT_XML_DEBUG)
		{
			AutomataToXml xport = new AutomataToXml(gui.getSelectedProject());
			TextFrame textframe = new TextFrame("XML debug output");

			xport.serialize(textframe.getPrintWriter());

			return;
		}

		if (exportMode == FORMAT_SP_DEBUG)
		{
			ProjectToSP exporter = new ProjectToSP(gui.getSelectedProject());
			TextFrame textframe = new TextFrame("SP debug output");

			exporter.serialize(textframe.getPrintWriter());

			return;
		}

		if (exportMode == FORMAT_DOT_DEBUG)
		{
			for (Iterator autIt = selectedAutomata.iterator();
					autIt.hasNext(); )
			{
				Automaton currAutomaton = (Automaton) autIt.next();
				AutomatonToDot exporter = new AutomatonToDot(currAutomaton);
				TextFrame textframe = new TextFrame("Dot debug output");

				try
				{
					exporter.serialize(textframe.getPrintWriter());
				}
				catch (Exception ex)
				{
					logger.debug(ex.getStackTrace());
				}
			}

			return;
		}

		if (exportMode == FORMAT_DSX_DEBUG)
		{
			for (Iterator autIt = selectedAutomata.iterator();
					autIt.hasNext(); )
			{
				Automaton currAutomaton = (Automaton) autIt.next();
				AutomatonToDsx exporter = new AutomatonToDsx(currAutomaton);
				TextFrame textframe = new TextFrame("DSX debug output");

				try
				{
					exporter.serialize(textframe.getPrintWriter());
				}
				catch (Exception ex)
				{
					logger.debug(ex.getStackTrace());
				}
			}

			return;
		}

		if (exportMode == FORMAT_FSM_DEBUG)
		{
			for (Iterator autIt = selectedAutomata.iterator();
					autIt.hasNext(); )
			{
				Automaton currAutomaton = (Automaton) autIt.next();
				AutomatonToFSM exporter = new AutomatonToFSM(currAutomaton);
				TextFrame textframe = new TextFrame("FSM debug output");

				try
				{
					exporter.serialize(textframe.getPrintWriter());
				}
				catch (Exception ex)
				{
					logger.debug(ex.getStackTrace());
				}
			}

			return;
		}

		if (exportMode == FORMAT_PCG_DEBUG)
		{
			AutomataToCommunicationGraph a2cg = new AutomataToCommunicationGraph(selectedAutomata);
			TextFrame textframe = new TextFrame("PCG debug output");

			try
			{
				a2cg.serialize(textframe.getPrintWriter());
			}
			catch (Exception ex)
			{
				logger.debug(ex.getStackTrace());
			}

			return;
		}
		else if ((exportMode == FORMAT_PCG) || (exportMode == FORMAT_SSPC))
		{
			JFileChooser fileExporter = new JFileChooser();

			fileExporter.setDialogTitle("Save as ...");

			if (fileExporter.showSaveDialog(gui.getComponent()) == JFileChooser.APPROVE_OPTION)
			{
				File currFile = fileExporter.getSelectedFile();

				if (currFile == null)
				{
					return;
				}

				try
				{
					if (exportMode == FORMAT_PCG)
					{
						AutomataToCommunicationGraph a2cg = new AutomataToCommunicationGraph(selectedAutomata);

						a2cg.serialize(currFile.getAbsolutePath());
					}
					else
					{
						new AutomataSSPCExporter(selectedAutomata, currFile.getAbsolutePath());
					}
				}
				catch (Exception ex)
				{
					logger.debug(ex.getStackTrace());
					ex.printStackTrace();    // TEMP!
				}
			}

			return;
		}

/*              if(exportMode == FORMAT_HTML_DEBUG)
				{
						for(Iterator autIt = selectedAutomata.iterator(); autIt.hasNext(); )
						{
								Automaton currAutomaton = (Automaton) autIt.next();
								AutomatonToHtml exporter = new AutomatonToHtml(currAutomaton);
								TextFrame textframe = new TextFrame("HTML debug output");
								try
								{
										exporter.serialize(textframe.getPrintWriter());
								}
								catch(Exception ex)
								{
										logger.debug(ex.getStackTrace());
								}
						}
						return;
				}
*/
		if ((exportMode == FORMAT_DOT) || (exportMode == FORMAT_DSX) || (exportMode == FORMAT_FSM) || (exportMode == FORMAT_PCG))
		{
			for (Iterator autIt = selectedAutomata.iterator();
					autIt.hasNext(); )
			{
				Automaton currAutomaton = (Automaton) autIt.next();

				automatonExport(gui, exportMode, currAutomaton);
			}
		}
		else
		{
			JFileChooser fileExporter = null;

			if (exportMode == FORMAT_XML)
			{
				fileExporter = FileDialogs.getXMLFileExporter();

				return;
			}
			else if (exportMode == FORMAT_SP)
			{
				fileExporter = FileDialogs.getSPFileExporter();
			}
			else
			{
				return;
			}

			Project selectedProject = gui.getSelectedProject();

			fileExporter.setDialogTitle("Save Project as ...");

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
								AutomataToXml exporter = new AutomataToXml(selectedProject);

								exporter.serialize(currFile);
							}
							else if (exportMode == FORMAT_SP)
							{
								ProjectToSP exporter = new ProjectToSP(selectedProject);

								exporter.serialize(currFile);
							}
						}
						catch (Exception ex)
						{
							logger.error("Exception while exporting " + currFile.getAbsolutePath(), ex);
							logger.debug(ex.getStackTrace());
						}
					}
				}
			}
		}
	}

	// Exporter when the type is already known
	// Add new export functions here and to the function above
	public static void automatonExport(Gui gui, int exportMode, Automaton currAutomaton)
	{
		JFileChooser fileExporter = null;

		if (exportMode == FORMAT_XML)
		{
			fileExporter = FileDialogs.getExportFileChooser(FileFormats.XML);
		}
		else if (exportMode == FORMAT_DOT)
		{
			fileExporter = FileDialogs.getExportFileChooser(FileFormats.DOT);
		}
		else if (exportMode == FORMAT_DSX)
		{
			fileExporter = FileDialogs.getExportFileChooser(FileFormats.DSX);
		}
		else if (exportMode == FORMAT_FSM)
		{
			fileExporter = FileDialogs.getExportFileChooser(FileFormats.FSM);
		}
		else if (exportMode == FORMAT_SP)
		{
			fileExporter = FileDialogs.getExportFileChooser(FileFormats.SP);
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

							exporter.serialize(currFile);
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
						else if (exportMode == FORMAT_FSM)
						{
							AutomatonToFSM exporter = new AutomatonToFSM(currAutomaton);

							exporter.serialize(currFile.getAbsolutePath());
						}
						else if (exportMode == FORMAT_SP)
						{
							Project selectedProject = gui.getSelectedProject();
							Project newProject = new Project();

							newProject.addAttributes(selectedProject);

							//newProject.addActions(selectedProject.getActions());
							//newProject.addControls(selectedProject.getControls());
							//newProject.setAnimationURL(selectedProject.getAnimationURL());
							ProjectToSP exporter = new ProjectToSP(newProject);

							exporter.serialize(currFile);
						}

						/*
						else if (exportMode == FORMAT_HTML)
						{
								Project selectedProject = gui.getSelectedProject();
								Project newProject = new Project();
								newProject.addActions(selectedProject.getActions());
								newProject.addControls(selectedProject.getControls());
								newProject.setAnimationURL(selectedProject.getAnimationURL());

								ProjectToHtml exporter = new ProjectToHtml(newProject);

								exporter.serialize(currFile);
						}
						*/
					}
					catch (Exception ex)
					{
						logger.error("Exception while exporting " + currFile.getAbsolutePath(), ex);
						logger.debug(ex.getStackTrace());
					}
				}
			}
		}
	}

	// ** Extend
	public static void automataExtend_actionPerformed(Gui gui)
	{
		Automata selectedAutomata = gui.getSelectedAutomata();

		if (!selectedAutomata.sanityCheck(gui, 1))
		{
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
//			int m = getIntegerInDialogWindow("Select m", gui.getComponent());
			AutomataExtender extender = new AutomataExtender(currAutomaton);

			extender.setK(k);
//			extender.setM(m);

			try
			{
				extender.execute();

				Automaton newAutomaton = extender.getNewAutomaton();

				newAutomaton.setName(newAutomatonName);
				gui.getVisualProjectContainer().getActiveProject().addAutomaton(newAutomaton);
			}
			catch (Exception ex)
			{
				logger.error("Exception in AutomataExtend. Automaton: " + currAutomaton.getName(), ex);
				logger.debug(ex.getStackTrace());
			}
		}
	}

	// ** Lifting according to the computer human theory
	public static void automataLifting_actionPerformed(Gui gui)
	{
		Automata selectedAutomata = gui.getSelectedAutomata();

		if (!selectedAutomata.sanityCheck(gui, 1))
		{
			return;
		}

		int k = getIntegerInDialogWindow("Select k", gui.getComponent());
		int m = getIntegerInDialogWindow("Select m", gui.getComponent());

		if (k < 1)
		{
			logger.info("k must >= 1. Try again.");
			return;
		}
		if (m < 1)
		{
			logger.info("m must >= 1. Try again.");
			return;
		}
		ComputerHumanExtender extender = new ComputerHumanExtender(selectedAutomata, k, m);

		try
		{
			extender.execute();

			Automaton newAutomaton = extender.getNewAutomaton();

			gui.getVisualProjectContainer().getActiveProject().addAutomaton(newAutomaton);
		}
		catch (Exception ex)
		{
			logger.error("Error in ComputerHumanExtender.");
			logger.debug(ex.getStackTrace());
		}
	}

	// ** Purge
	public static void automataPurge_actionPerformed(Gui gui)
	{
		Automata selectedAutomata = gui.getSelectedAutomata();

		if (!selectedAutomata.sanityCheck(gui, 1))
		{
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
				logger.error("Exception in AutomataPurge. Automaton: " + currAutomaton.getName(), ex);
				logger.debug(ex.getStackTrace());
			}
		}
	}

	// ** RemovePass - removes all pass events
	public static void automataRemovePass_actionPerformed(Gui gui)
	{
		Automata selectedAutomata = gui.getSelectedAutomata();

		if (!selectedAutomata.sanityCheck(gui, 1))
		{
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
				logger.error("Exception in AutomataRemovePass. Automaton: " + currAutomaton.getName(), ex);
				logger.debug(ex.getStackTrace());
			}
		}
	}

	// ** RemoveSelfLoopArcs
	public static void automataRemoveSelfLoopArcs_actionPerformed(Gui gui)
	{
		Automata selectedAutomata = gui.getSelectedAutomata();

		if (!selectedAutomata.sanityCheck(gui, 1))
		{
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
				logger.error("Exception in RemoveSelfArcs. Automaton: " + currAutomaton.getName(), ex);
				logger.debug(ex.getStackTrace());
			}
		}
	}

	// ** Rename
	public static void automataRename_actionPerformed(Gui gui)
	{
		Automata selectedAutomata = gui.getSelectedAutomata();

		if (!selectedAutomata.sanityCheck(gui, 1))
		{
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
				logger.error("Exception while renaming the automaton " + currAutomatonName, ex);
				logger.debug(ex.getStackTrace());
			}
		}
	}

	// ** Synchronize - Threaded version
	public static void automataSynchronize_actionPerformed(Gui gui)
	{
		// Retrieve the selected automata and make a sanity check
		Automata selectedAutomata = gui.getSelectedAutomata();
		if (!selectedAutomata.sanityCheck(gui, 2, true, false, true, true))
		{
			return;
		}

		// Get the current options
		SynchronizationOptions synchronizationOptions;

		try
		{
			synchronizationOptions = new SynchronizationOptions();
		}
		catch (Exception ex)
		{
			JOptionPane.showMessageDialog(gui.getComponent(), "Error constructing synchronizationOptions: " + ex.getMessage(), "Alert", JOptionPane.ERROR_MESSAGE);
			logger.debug(ex.getStackTrace());

			return;
		}

		// Start a dialog to allow the user changing the options
		SynchronizationDialog synchronizationDialog = new SynchronizationDialog(gui.getFrame(), synchronizationOptions);

		synchronizationDialog.show();

		if (!synchronizationOptions.getDialogOK())
		{
			return;
		}

		// Start worker thread - perform the task.
		AutomataSynchronizerWorker worker = new AutomataSynchronizerWorker(gui, selectedAutomata, "" /* newAutomatonName */, synchronizationOptions);
	}

	// ** Synthesize
	public static void automataSynthesize_actionPerformed(Gui gui)
	{
		// Retrieve the selected automata and make a sanity check
		Automata selectedAutomata = gui.getSelectedAutomata();

		if (!selectedAutomata.sanityCheck(gui, 1, true, true, true, true))
		{
			return;
		}

		// Get the current options and allow the user to change them...
		SynthesizerOptions options = new SynthesizerOptions();
		SynthesizerDialog synthesizerDialog = new SynthesizerDialog(gui.getFrame(), selectedAutomata.size(), options);
		synthesizerDialog.show();
		if (!options.getDialogOK())
		{
			return;
		}

		AutomataSynthesisWorker worker = new AutomataSynthesisWorker(gui, selectedAutomata, options);

		/*
		ActionTimer timer = null;

		// One or more automata selected?
		if (selectedAutomata.size() > 1)
		{
			SynchronizationOptions syncOptions = SynchronizationOptions.getDefaultSynthesisOptions();

			try
			{
				AutomataSynthesizer synthesizer = new AutomataSynthesizer(gui, selectedAutomata, syncOptions,
																		  synthesizerOptions);
				synthesizer.execute();

				timer = synthesizer.getTimer();
			}
			catch (Exception ex)
			{
				logger.error("Exception in AutomataSynthesizer. " + ex);
				logger.debug(ex.getStackTrace());
			}
		}
		else    // single automaton selected
		{
			Automaton theAutomaton = selectedAutomata.getFirstAutomaton();

			try
			{
				// ARASH: this is IDIOTIC! why didnt we prepare for more than one monolithc algorithm???
				// (this is a dirty fix, should use a factory instead)
				AutomatonSynthesizer synthesizer = (synthesizerOptions.getSynthesisAlgorithm() == SynthesisAlgorithm.MonolithicSingleFixpoint)
					? new AutomatonSynthesizerSingleFixpoint(theAutomaton, synthesizerOptions)
					: new AutomatonSynthesizer(theAutomaton, synthesizerOptions);

				// AutomatonSynthesizer synthesizer = new AutomatonSynthesizer(theAutomaton,synthesizerOptions);
				synthesizer.synthesize();
			}
			catch (Exception ex)
			{
				logger.error("Exception in AutomatonSynthesizer. Automaton: " + theAutomaton.getName(), ex);
				logger.debug(ex.getStackTrace());
			}
		}

		if (timer != null)
		{
			logger.info("Execution completed after " + timer.toString());
		}
		*/
	}

	// Automaton.Verify action performed
	// Threaded version
	public static void automataVerify_actionPerformed(Gui gui)
	{
		// Retrieve the selected automata and make a sanity check
		Automata selectedAutomata = gui.getSelectedAutomata();
		if (!selectedAutomata.sanityCheck(gui, 1, true, false, true, true))
		{
			return;
		}

		// Get the current options and allow the user to change them...
		VerificationOptions vOptions = new VerificationOptions();
		MinimizationOptions mOptions = MinimizationOptions.getDefaultVerificationOptions();
		//MinimizationOptions mOptions = new MinimizationOptions();
		VerificationDialog verificationDialog = new VerificationDialog(gui.getFrame(), vOptions, mOptions);
		verificationDialog.show();
		if (!vOptions.getDialogOK())
		{
			return;
		}
		if (vOptions.getVerificationType() == VerificationType.LanguageInclusion)
		{
			vOptions.setInclusionAutomata(gui.getUnselectedAutomata());
		}

		SynchronizationOptions sOptions = SynchronizationOptions.getDefaultVerificationOptions();

		// Work!
		AutomataVerificationWorker worker = new AutomataVerificationWorker(gui, selectedAutomata,
																		   vOptions, sOptions, mOptions);
	}

	// Automaton.ActionAndControlViewer action performed
	public static void actionAndControlViewer_actionPerformed(Gui gui)
	{
		try
		{
			ActionAndControlViewer viewer = gui.getVisualProjectContainer().getActiveProject().getActionAndControlViewer();
		}
		catch (Exception ex)
		{
			logger.error("Exception in ActionAndControlViewer.", ex);
			logger.debug(ex.getStackTrace());

			return;
		}
	}

	// Automaton.ActionAndControlViewer action performed
	public static void animator_actionPerformed(Gui gui)
	{
		try
		{
			VisualProject currProject = gui.getVisualProjectContainer().getActiveProject();

			if (!currProject.hasAnimation())
			{
				logger.info("No animation present");

				return;
			}

			Animator animator = currProject.getAnimator();
		}
		catch (Exception ex)
		{
			logger.error("Exception while getting Animator.", ex);
			logger.debug(ex.getStackTrace());
		}
	}

	// ActionMan.userInterface_action performed
	public static void userInterface_actionPerformed(Gui gui)
	{
		try
		{
			VisualProject currProject = gui.getVisualProjectContainer().getActiveProject();

			if (!currProject.hasUserInterface())
			{
				logger.info("No user interface present");

				return;
			}

			Container userInterface = currProject.getUserInterface();
			userInterface.setVisible(true);
		}
		catch (Exception ex)
		{
			logger.error("Exception while getting user interface.", ex);
			logger.debug(ex.getStackTrace());
		}
	}

	// ActionMan.userInterface_action performed
	public static void generateUserInterfaceAutomata_actionPerformed(Gui gui)
	{
		try
		{
			VisualProject currProject = gui.getVisualProjectContainer().getActiveProject();

			if (!currProject.hasUserInterface())
			{
				logger.info("No user interface present");

				return;
			}

			SwingEngine swingEngine = currProject.getSwingEngine();
			ProjectBuildFromSwingEngine projectBuilder = new ProjectBuildFromSwingEngine();

			try
			{
				Project newProject = projectBuilder.build(swingEngine);
				int nbrOfAddedAutomata = gui.addAutomata(newProject);

				gui.info("Successfully created " + nbrOfAddedAutomata + " user interface automata.");
			}
			catch (Exception ex)
			{
				logger.error("Error while creating user interface automata", ex);
				logger.debug(ex.getStackTrace());

				return;
			}

		}
		catch (Exception ex)
		{
			logger.error("Exception while getting user interface.", ex);
			logger.debug(ex.getStackTrace());
		}
	}

	// Automaton.Explore action performed
	public static void automatonExplore_actionPerformed(Gui gui)
	{
		// Retrieve the selected automata and make a sanity check
		Automata selectedAutomata = gui.getSelectedAutomata();

		// Sanitycheck
		if (!selectedAutomata.sanityCheck(gui, 1, true, false, false, true))
		{
			return;
		}

		// How many selected?
		if (selectedAutomata.size() == 1)
		{
			// One automaton selected

			// Get automaton
			Automaton theAutomaton = selectedAutomata.getFirstAutomaton();
			String currAutomatonName = theAutomaton.getName();

			// Get AutomatonExplorer
			try
			{
				AutomatonExplorer explorer = gui.getVisualProjectContainer().getActiveProject().getAutomatonExplorer(currAutomatonName);
			}
			catch (Exception ex)
			{
				logger.error("Exception in AutomatonExplorer. Automaton: " + theAutomaton.getName(), ex);
				logger.debug(ex.getStackTrace());
			}
		}
		else
		{    // Many automata selected

			// The AutomataExplorer can not take care of nondeterministic processes...
			if (!selectedAutomata.isDeterministic())
			{
				logger.error("The current project is nondeterministic. Exploration of nondeterministic automata " +
							 "is currently not supported.");
			}

			// Get AutomataExplorer
			try
			{
				JOptionPane.showMessageDialog(gui.getComponent(), "The automata explorer only works in the \"forward\" direction!", "Alert", JOptionPane.INFORMATION_MESSAGE);

				AutomataExplorer explorer = new AutomataExplorer(selectedAutomata);

				explorer.setVisible(true);
				explorer.initialize();
			}
			catch (Exception ex)
			{
				logger.error("Exception in AutomataExplorer.", ex);
				logger.debug(ex.getStackTrace());
			}
		}
	}

	// Project.Simulator action performed
	public static void simulator_actionPerformed(Gui gui)
	{
		// We can not simulate nondeterministic processes properly just yet...
		if (!gui.getVisualProjectContainer().getActiveProject().isDeterministic())
		{
			logger.error("The current project is nondeterministic. Simulation of nondeterminism is currently not supported.");
		}

		try
		{
			VisualProject currProject = gui.getVisualProjectContainer().getActiveProject();

			if (!currProject.hasAnimation())
			{
				logger.info("No simulation present");

				return;
			}

			SimulatorExecuter simulator = currProject.getSimulator();

			if (simulator != null)
			{
				simulator.setVisible(true);
				simulator.initialize();
			}
		}
		catch (Exception ex)
		{
			logger.error("Exception in Simulator", ex);
			logger.debug(ex.getStackTrace());
		}
	}

	// Project.SimulatorClear action performed
	public static void simulatorClear_actionPerformed(Gui gui)
	{
		try
		{
			VisualProject currProject = gui.getVisualProjectContainer().getActiveProject();

			currProject.clearSimulationData();
		}
		catch (Exception ex)
		{
			logger.error("Exception in Simulator");
		}
	}

	// Automaton.Minimization action performed
	public static void automatonMinimize_actionPerformed(Gui gui)
	{
		// Retrieve the selected automata and make a sanity check
		Automata selectedAutomata = gui.getSelectedAutomata();
		if (!selectedAutomata.sanityCheck(gui, 1))
		{
			return;
		}

		// Get the current options and allow the user to change them...
		MinimizationOptions options = new MinimizationOptions();
		MinimizationDialog dialog = new MinimizationDialog(gui.getFrame(), options, selectedAutomata);
		dialog.show();
		if (!options.getDialogOK())
		{
			return;
		}

		AutomataMinimizationWorker worker = new AutomataMinimizationWorker(gui, selectedAutomata, options);
	}

	/*
		// Timer
		ActionTimer timer = new ActionTimer();
		timer.start();

		// The result...
		Automata result = new Automata();

		// Minimize!
		if (!options.getCompositionalMinimization())
		{
			// Iterate over automata and minimize each on itself
			Iterator autIt = selectedAutomata.iterator();
			while (autIt.hasNext())
			{
				Automaton currAutomaton = (Automaton) autIt.next();

				// Minimize this one
				try
				{
					AutomatonMinimizer minimizer = new AutomatonMinimizer(currAutomaton);
					Automaton newAutomaton = minimizer.getMinimizedAutomaton(options);
					result.addAutomaton(newAutomaton);
				}
				catch (Exception ex)
				{
					logger.error("Exception in AutomatonMinimizer. Automaton: " +
								 currAutomaton.getName() + " " + ex);
					logger.debug(ex.getStackTrace());
				}

				if (!options.getKeepOriginal())
				{
					gui.getVisualProjectContainer().getActiveProject().removeAutomaton(currAutomaton);
				}
			}
		}
		else
		{
			// Compositional minimization!
			try
			{
				AutomataMinimizer minimizer = new AutomataMinimizer(selectedAutomata);
				Automaton newAutomaton = minimizer.getCompositionalMinimization(options);
				result.addAutomaton(newAutomaton);
			}
			catch (Exception ex)
			{
				logger.error("Exception in AutomatonMinimizer when compositionally minimizing " +
							 selectedAutomata + " " + ex);
				logger.debug(ex.getStackTrace());
			}

			if (!options.getKeepOriginal())
			{
				gui.getVisualProjectContainer().getActiveProject().removeAutomata(selectedAutomata);
			}
		}

		// Timer
		timer.stop();
		logger.info("Execution completed after " + timer.toString());

		// Add new automata
		try
		{
			gui.addAutomata(result);
		}
		catch (Exception ex)
		{
			logger.error(ex);
		}
	}
	*/

	// Automaton.Status action performed
	public static void automatonStatus_actionPerformed(Gui gui)
	{
		int nbrOfAutomata = gui.getVisualProjectContainer().getActiveProject().nbrOfAutomata();
		//gui.info("Number of automata: " + nbrOfAutomata);

		Automata selectedAutomata = gui.getSelectedAutomata();
		if (!selectedAutomata.sanityCheck(gui, 1, false, false, true, true))
		{
			return;
		}

		gui.info("Number of selected automata: " + selectedAutomata.size() + " (" + nbrOfAutomata + ")");
		gui.info("Size of union alphabet: " + selectedAutomata.getUnionAlphabet().size());

		for (Iterator autIt = selectedAutomata.iterator(); autIt.hasNext(); )
		{
			Automaton currAutomaton = (Automaton) autIt.next();
			StringBuffer statusStr = new StringBuffer();

			statusStr.append("Status for automaton: " + currAutomaton.getName());

			if ((currAutomaton.getComment() != null) && !currAutomaton.getComment().equals(""))
			{
				statusStr.append("\n\tComment: \"" + currAutomaton.getComment() + "\"");
			}

			statusStr.append("\n\tis deterministic: " + currAutomaton.isDeterministic());
			statusStr.append("\n\tNumber of states: " + currAutomaton.nbrOfStates());
			statusStr.append("\n\tNumber of events: " + currAutomaton.nbrOfEvents());
			statusStr.append("\n\tNumber of transitions: " + currAutomaton.nbrOfTransitions());
			statusStr.append("\n\tNumber of accepting states: " + currAutomaton.nbrOfAcceptingStates());
			//statusStr.append("\n\tNumber of mutually accepting states: " + currAutomaton.nbrOfMutuallyAcceptingStates());
			statusStr.append("\n\tNumber of forbidden states: " + currAutomaton.nbrOfForbiddenStates());

			int acceptingAndForbiddenStates = currAutomaton.nbrOfAcceptingAndForbiddenStates();

			if (acceptingAndForbiddenStates > 0)
			{
				statusStr.append("\n\tNumber of accepting and forbidden states: " + acceptingAndForbiddenStates);
			}

			// logger.info(statusStr.toString());
			gui.info(statusStr.toString());
		}

		if (selectedAutomata.size() > 1)
		{
			double potentialNumberOfStates = 1.0;

			for (Iterator autIt = selectedAutomata.iterator();
					autIt.hasNext(); )
			{
				Automaton currAutomaton = (Automaton) autIt.next();

				potentialNumberOfStates = potentialNumberOfStates * currAutomaton.nbrOfStates();
			}

			gui.info("Number of potential states: " + new Double(potentialNumberOfStates).longValue());
		}
	}

	// View hierarchy action performed
	public static void hierarchyView_actionPerformed(Gui gui)
	{
		Automata selectedAutomata = gui.getSelectedAutomata();

		// Sanity check
		if (!selectedAutomata.sanityCheck(gui, 2, false, false, true, false))
		{
			return;
		}

		// Warn if there are too many "states" i.e. automata
		int maxNbrOfStates = SupremicaProperties.getDotMaxNbrOfStatesWithoutWarning();
		if (maxNbrOfStates < selectedAutomata.size())
		{
			String msg = "You have selected " + selectedAutomata.size() + " automata. It is not " +
				"recommended to display the modular structure for more than " + maxNbrOfStates +
				" automata.";
			msg = EncodingHelper.linebreakAdjust(msg);
			
			Object[] options = { "Continue", "Abort" };
			int response = JOptionPane.showOptionDialog(ActionMan.gui.getFrame(), msg, "Warning",
														JOptionPane.YES_NO_OPTION, 
														JOptionPane.WARNING_MESSAGE, 
														null, options, options[1]);
			if(response == JOptionPane.NO_OPTION)
			{
				return;
			}
		}

		// View
		try
		{
			AutomataHierarchyViewer viewer = new AutomataHierarchyViewer(selectedAutomata);

			viewer.setVisible(true);

			//viewer.setState(Frame.NORMAL);
		}
		catch (Exception ex)
		{
			logger.error("Exception in AutomataHierarchyViewer.", ex);
			logger.debug(ex.getStackTrace());

			return;
		}
	}

	// View the automatas individual states in a tree structure
	public static void statesView_actionPerformed(Gui gui)
	{
		Automata selectedAutomata = gui.getSelectedAutomata();

		if (!selectedAutomata.sanityCheck(gui, 1, false, false, true, false))
		{
			return;
		}

		try
		{
			AutomataViewer statesViewer = new AutomataViewer(selectedAutomata, false, true);

			statesViewer.setVisible(true);
		}
		catch (Exception ex)
		{
			// logger.error("Exception in AlphabetViewer", ex);
			logger.error("Exception in AutomataViewer: " + ex);
			logger.debug(ex.getStackTrace());

			return;
		}
	}

	// Automaton.Alphabet action performed
	// public static void automatonAlphabet_actionPerformed(Gui gui)
	public static void alphabetView_actionPerformed(Gui gui)
	{
		//logger.debug("ActionMan::automatonAlphabet_actionPerformed(gui)");
		Automata selectedAutomata = gui.getSelectedAutomata();

		if (!selectedAutomata.sanityCheck(gui, 1, false, false, true, false))
		{
			return;
		}

		// Why not simpy instantiate an AlphabetViewer with the given
		// automata object?? Use AutomataViewer instead!
		try
		{
			// AlphabetViewer alphabetviewer = new AlphabetViewer(selectedAutomata);
			AutomataViewer alphabetViewer = new AutomataViewer(selectedAutomata, true, false);

			alphabetViewer.setVisible(true);
		}
		catch (Exception ex)
		{
			// logger.error("Exception in AlphabetViewer", ex);
			logger.error("Exception in AutomataViewer: " + ex);
			logger.debug(ex.getStackTrace());

			return;
		}
	}

	// Automaton.View action performed
	public static void automatonView_actionPerformed(Gui gui)
	{
		// gui.debug("ActionMan to the rescue!");
		// Retrieve the selected automata and make a sanity check
		Automata selectedAutomata = gui.getSelectedAutomata();

		if (!selectedAutomata.sanityCheck(gui, 1, true, false, false, false))
		{
			return;
		}

		Iterator autIt = selectedAutomata.iterator();
		while (autIt.hasNext())
		{
			Automaton currAutomaton = (Automaton) autIt.next();

			try
			{
				AutomatonViewer viewer = gui.getVisualProjectContainer().getActiveProject().getAutomatonViewer(currAutomaton.getName());
			}
			catch (Exception ex)
			{
				logger.error("Exception in AutomatonViewer. Automaton: " + currAutomaton, ex);
				logger.debug(ex.getStackTrace());

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

	public static void fileExportHtml(Gui gui)
	{
		try
		{
			File dir = new File("C:\\Temp\\");
			Project selectedProject = gui.getSelectedProject();
			ProjectToHtml exporter = new ProjectToHtml(selectedProject, dir);

			exporter.serialize();
		}
		catch (Exception ex)
		{
			logger.error("fileExportHtml: Exception - ", ex);
			logger.debug(ex.getStackTrace());
		}
	}

	// -------------- TODO: ADD EXPORTES FOR THESE TOO ------------------------------------
	public static void fileExportUMDES(Gui gui)
	{
		automataExport(gui);
	}

	public static void fileExportValid(Gui gui)
	{
		automataExport(gui);
	}

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

	public static void fileImportHYB(Gui gui)
	{
		new FileImporter(FileDialogs.getHYBFileImporter(), gui)    // anonymous class
		{
			void openFile(Gui g, File f)
			{
				importHYBFile(g, f);
			}
		};
	}

	public static void fileImportUMDES(Gui gui)
	{
		new FileImporter(FileDialogs.getImportFileChooser(FileFormats.FSM), gui)    // anonymous class
		{
			void openFile(Gui g, File f)
			{
				importUMDESFile(g, f);
			}
		};
	}

	public static void fileImportRobotCoordination(Gui gui)
	{
		new FileImporter(FileDialogs.getXMLFileImporter(), gui)    // anonymous class
		{
			void openFile(Gui g, File f)
			{
				importRobotCoordinationFile(g, f);
			}
		};
	}

	/**
	 * What is this!?!?? Wasn't the friggin' point of this whole thing to AVOID having
	 * application specific import functions?!? Why is there a fileImportRobotCoordination
	 * AND a fileImportRobotCoordinationABB????? GRRRRRrrrrRRRR!! /hguo
	 */

	// File.Import.FromRobotCoordinationABB (format representing RobotStudio station)
	public static void fileImportRobotCoordinationABB(Gui gui)
	{
		new FileImporter(FileDialogs.getXMLFileImporter(), gui)    // anonymous class
		{
			void openFile(Gui g, File f)
			{
				importRobotCoordinationFileABB(g, f);
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
		catch (Exception ex)
		{
			// this exception is caught while opening
			logger.error("Error while opening " + file.getAbsolutePath() + " .", ex);
			logger.debug(ex.getStackTrace());

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

		if (!currProject.isDeterministic())
		{
			/*
			Object[] options = { "Continue", "Abort" };
			int conf = JOptionPane.showOptionDialog(gui.getComponent(), "All automata are not determinstic. Abort?", "Non-determinism Found", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[1]);

			if (conf == JOptionPane.YES_OPTION)
			{
				logger.warn("Non-deterministic automaton loaded. Some algorithms are not guaranteed to work.");
			}
			else    // NO_OPTION
			{
				return;
			}
			*/

			logger.warn("Non-deterministic automaton loaded. Some algorithms are not guaranteed to work.");
		}

		// We should always check owner and hash when it is present
		if (!((currProject.getOwner() == null) && (currProject.getHash() == null)) &&!fileSecurity.hasCorrectHash(currProject))
		{
			JOptionPane.showMessageDialog(gui.getComponent(), "The project has an invalid hash", "alert", JOptionPane.WARNING_MESSAGE);
		}

		int nbrOfAutomataBeforeOpening = gui.getVisualProjectContainer().getActiveProject().nbrOfAutomata();

		try
		{
			int nbrOfAddedAutomata = gui.addProject(currProject);

			//gui.addActions(currProject.getActions());
			//gui.addControls(currProject.getControls());
			gui.info("Successfully opened and added " + nbrOfAddedAutomata + " automata.");
		}
		catch (Exception excp)
		{
			logger.error("Error adding automata " + file.getAbsolutePath(), excp);
			logger.debug(excp.getStackTrace());

			return;
		}

		/*
		if (nbrOfAutomataBeforeOpening == 0)
		{
				String projectName = currProject.getName();

				if (projectName != null)
				{
						gui.getVisualProjectContainer().getActiveProject().setName(projectName);
						//gui.info("Project name changed to \"" + projectName + "\"");
						gui.getVisualProjectContainer().getActiveProject().updateFrameTitles();
				}
		}
		*/
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
		// Get the file corresponding to the current project
		File currFile = gui.getVisualProjectContainer().getActiveProject().getProjectFile();
		// Was there no open project? Use fileSaveAs!
		if (currFile == null)
		{
			fileSaveAs(gui);

			return;
		}

		// Get the current project
		Project currProject = gui.getVisualProjectContainer().getActiveProject();

		// Is this project empty? If so, maybe we shouldn't save it?
		if (currProject.size() == 0)
		{
			//if (JOptionPane.NO_OPTION == JOptionPane.showConfirmDialog(gui.getComponent(), "This project is empty. Do you really want to save?", "Do you really want to save an empty project?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE))
			//if (JOptionPane.NO_OPTION == JOptionPane.showConfirmDialog(gui.getComponent(), "This project is empty. Do you really want to save?", "Do you really want to save an empty project?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, new Object[]={"Yes", "No"}, "No"))
			Object[] objects = {"Yes", "No"};
			if (JOptionPane.NO_OPTION == JOptionPane.showOptionDialog(gui.getComponent(), "This project is empty. Do you really want to save?", "Do you really want to save an empty project?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, objects, objects[1]))
			{
				return;
			}
		}

		// Go ahead!
		if (currFile != null)
		{
			if (!currFile.isDirectory())
			{
				try
				{
					if (currProject.hasExecutionParameters())
					{
						if (JOptionPane.NO_OPTION == JOptionPane.showConfirmDialog(gui.getComponent(), "The project contains an execution part which will be lost when saving. Do a backup copy of " + currFile.getPath() + " first. Continue saving (and erase execution part from file)?", "Saving will erase execution part from file", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE))
						{
							return;
						}
					}

					FileSecurity fileSecurity = gui.getFileSecurity();

					if (SupremicaProperties.generalUseSecurity())
					{
						if (!fileSecurity.hasCurrentUser())
						{
							JOptionPane.showMessageDialog(gui.getComponent(), "You must be logged in to save!", "Alert", JOptionPane.ERROR_MESSAGE);

							return;
						}

						currProject.setOwner(fileSecurity.getCurrentUser());
						currProject.setHash(currProject.computeHash());
					}

					AutomataToXml exporter = new AutomataToXml(currProject);

					exporter.serialize(currFile.getAbsolutePath());
				}
				catch (Exception ex)
				{
					logger.error("Exception while Save As " + currFile.getAbsolutePath(), ex);
					logger.debug(ex.getStackTrace());
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
		catch (Exception ex)
		{
			logger.error("Error while importing " + file.getAbsolutePath(), ex);
			logger.debug(ex.getStackTrace());

			return;
		}
	}

	public static void importValidFile(Gui gui, File file)
	{
		gui.info("Importing " + file.getAbsolutePath() + " ...");

		try
		{
			AutomataBuildFromVALID builder = new AutomataBuildFromVALID(new VisualProjectFactory());
			Automata currAutomata = builder.build(file);
			int nbrOfAddedAutomata = gui.addAutomata(currAutomata);

			gui.info("Successfully imported " + nbrOfAddedAutomata + " automata.");
		}
		catch (Exception ex)
		{
			logger.error("Error while importing " + file.getAbsolutePath() + ". " + ex);
			logger.debug(ex.getStackTrace());

			return;
		}
	}

	public static void importHYBFile(Gui gui, File file)
	{
		gui.info("Importing " + file.getAbsolutePath() + " ...");

		try
		{
			ProjectBuildFromHYB builder = new ProjectBuildFromHYB(new VisualProjectFactory());
			Automata currAutomata = builder.build(file.toURL());
			int nbrOfAddedAutomata = gui.addAutomata(currAutomata);

			gui.info("Successfully imported " + nbrOfAddedAutomata + " automata.");
		}
		catch (Exception ex)
		{
			logger.error("Error while importing " + file.getAbsolutePath() + ". ", ex);
			logger.debug(ex.getStackTrace());

			return;
		}
	}

	public static void importUMDESFile(Gui gui, File file)
	{
		gui.info("Importing " + file.getAbsolutePath() + " ...");

		try
		{
			ProjectBuildFromFSM builder = new ProjectBuildFromFSM(new VisualProjectFactory());
			Automata currAutomata = builder.build(file.toURL());
			int nbrOfAddedAutomata = gui.addAutomata(currAutomata);

			gui.info("Successfully imported " + nbrOfAddedAutomata + " automata.");
		}
		catch (Exception ex)
		{
			logger.error("Error while importing " + file.getAbsolutePath() + ". ", ex);
			logger.debug(ex.getStackTrace());

			return;
		}
	}

	public static void importRobotCoordinationFile(Gui gui, File file)
	{

		// logger.info("Importing " + file.getAbsolutePath() + " ...");
		gui.info("Importing " + file.getAbsolutePath() + " ...");

		try
		{
			AutomataBuilder builder = new AutomataBuilder(new VisualProjectFactory());
			Automata currAutomata = builder.build(file);
			int nbrOfAddedAutomata = gui.addAutomata(currAutomata);

			gui.info("Successfully imported " + nbrOfAddedAutomata + " automata.");
		}
		catch (Exception ex)
		{
			logger.error("Error while importing " + file.getAbsolutePath(), ex);
			logger.debug(ex.getStackTrace());

			return;
		}
	}

	// File.Import.FromRobotCoordinationABB
	public static void importRobotCoordinationFileABB(Gui gui, File file)
	{

		// logger.info("Importing " + file.getAbsolutePath() + " ...");
		gui.info("Importing " + file.getAbsolutePath() + " ...");
		ConvertToAutomata.conversionToAutomata(file);
	}

	// Automata.AlphabetNormalize action performed
	public static void normalizeAlphabet_actionPerformed(Gui gui)
	{
		Automata selectedAutomata = gui.getSelectedAutomata();
		if (!selectedAutomata.sanityCheck(gui, 1, false, false, true, false))
		{
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
				logger.error("Exception in AlphabetNormalizer. Automaton: " + currAutomaton.getName(), ex);
				logger.debug(ex.getStackTrace());
			}
		}
	}

	// selectAll action performed
	public static void selectAll_actionPerformed(Gui gui)
	{

		// theAutomatonTable.selectAll();
		gui.selectAll();
	}

/* Moved to the FindStates UserAction
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
						logger.error("Exception in Find States. ", ex);
						logger.debug(ex.getStackTrace());
				}
		}
*/

	// Delete All - this really implements Close Project
	public static void automataDeleteAll_actionPerformed(Gui gui)
	{
		gui.getVisualProjectContainer().getActiveProject().clear();
		gui.clearSelection();
	}

	// Crop to selection - delete all unselected automata
	public static void automataCrop_actionPerformed(Gui gui)
	{

		//Collection selectedAutomata = gui.getSelectedAutomataAsCollection();
		Automata selectedAutomata = gui.getSelectedAutomata();

		if (selectedAutomata.size() == 0)
		{
			// Use DeleteAll instead
			automataDeleteAll_actionPerformed(gui);

			return;
		}

		Automaton currAutomaton;
		String currAutomatonName;

		for (int i = 0;
				i < gui.getVisualProjectContainer().getActiveProject().nbrOfAutomata();
				i++)
		{
			try
			{
				currAutomaton = gui.getVisualProjectContainer().getActiveProject().getAutomatonAt(i);
			}
			catch (Exception ex)
			{
				logger.error("Exception in VisualProjectContainer. " + ex);
				logger.debug(ex.getStackTrace());

				return;
			}

			currAutomatonName = currAutomaton.getName();

			if (!selectedAutomata.containsAutomaton(currAutomaton))
			{
				try
				{
					gui.getVisualProjectContainer().getActiveProject().removeAutomaton(currAutomatonName);
				}
				catch (Exception ex)
				{
					logger.error("Exception while removing " + currAutomatonName, ex);
					logger.debug(ex.getStackTrace());

					return;
				}

				i--;    // Step back! One automaton has been removed!
			}
		}

		gui.clearSelection();
	}

	// Invert selection - select all unselected automata instead
	public static void automataInvert_actionPerformed(Gui gui)
	{
		gui.invertSelection();
	}

	/**
	 * Calculates table with information for use with an (external) genetic programming system.
	 * This is a part of a project in a course in Evolutionary Computation, FFR105 (2002) at
	 * Chalmers University of Technology.
	 *
	 * To use this you have to set a boolean in GeneticAlgorithms.java.
	 *
	 * Writes 16 columns of data and a correct value on each line of an output file
	 */
	public static void evoCompSynchTable(Gui gui, boolean append)
	{
		Automata selectedAutomata = gui.getSelectedAutomata();

		if (!selectedAutomata.sanityCheck(gui, 1))
		{
			return;
		}

		Automaton automatonA;
		Automaton automatonB;
		FileWriter outFile = null;

		// Reuse syncOptions
		SynchronizationOptions syncOptions;
		double[] data;    // = new double[8+1];

		try
		{
			// Synchronize the automata using default options (prediction will
			// probably be a problem if there are non-prioritized events)
			syncOptions = new SynchronizationOptions();
			outFile = new FileWriter("SynchTable.txt", append);

			int dataAmount = 1000;

			for (int i = 0; i < dataAmount; i++)
			{

				// Find two random automata
				automatonA = selectedAutomata.getAutomatonAt((int) (Math.random() * selectedAutomata.size()));
				automatonB = selectedAutomata.getAutomatonAt((int) (Math.random() * selectedAutomata.size()));

				//System.out.println(automatonA.getName() + " " + automatonB.getName());
				data = GeneticAlgorithms.extractData(automatonA, automatonB);

				Automata theTwoAutomata = new Automata();

				theTwoAutomata.addAutomaton(automatonA);
				theTwoAutomata.addAutomaton(automatonB);

				double correctValue = (double) GeneticAlgorithms.calculateSynchronizationSize(theTwoAutomata, syncOptions);

				if ((i > dataAmount / 4) && (data[0] * data[1] == correctValue))
				{

					// Too much data of this kind otherwise...
					i--;
				}
				else
				{

					// Writes data[0]..data[GA_DATA_SIZE] and correctValue to the file
					for (int j = 0; j < data.length; j++)
					{
						outFile.write(data[j] + "\t");
					}

					outFile.write(correctValue + "\t");
					outFile.write(automatonA.getName() + " " + automatonB.getName() + "\n");
					outFile.flush();
				}
			}

			outFile.close();
		}
		catch (Exception ex)
		{
			JOptionPane.showMessageDialog(gui.getComponent(), "Error in ActionMan.evoCompSynchTable(): " + ex.getMessage(), "Alert", JOptionPane.ERROR_MESSAGE);

			return;
		}
	}

	public static void evoCompPredictSize(Gui gui)
	{
		Automata selectedAutomata = gui.getSelectedAutomata();

		if (!selectedAutomata.sanityCheck(gui, 1))
		{
			return;
		}

		double predictedSize = GeneticAlgorithms.predictSynchronizationSize(selectedAutomata);

		if (predictedSize > 0.0)
		{
			double[] data;

			if (selectedAutomata.size() == 2)
			{
				data = GeneticAlgorithms.extractData(selectedAutomata.getAutomatonAt(0), selectedAutomata.getAutomatonAt(1));
			}
			else if (selectedAutomata.size() == 1)
			{
				data = GeneticAlgorithms.extractData(selectedAutomata.getAutomatonAt(0), selectedAutomata.getAutomatonAt(0));
			}
			else
			{
				return;
			}

			int realSize = GeneticAlgorithms.calculateSynchronizationSize(selectedAutomata);
			int worstSize = (int) (data[0] * data[1]);

			JOptionPane.showMessageDialog(gui.getComponent(), "The synchronization is predicted to have " + (float) predictedSize + " states. \nSynchronization actually " + "gives exactly " + realSize + " states (worst case " + worstSize + ").", "Prediction", JOptionPane.INFORMATION_MESSAGE);
		}
		else
		{
			JOptionPane.showMessageDialog(gui.getComponent(), "The prediction failed. (Predicted size: " + predictedSize + ")", "Prediction", JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Just a test...
	 */
	/*
	public static void trainSimulator(Gui gui)
	{
			Thread thread = new Thread(new Runnable()
			{
					public void run()
					{
							TrainSimulator trainSimulator = new TrainSimulator();
							trainSimulator.exec();
					}
			});
			thread.start();
			//TrainSimulator trainSimulator = new TrainSimulator();
			//trainSimulator.exec();
	}
	*/

	static RobotCellExaminer theRobotCellExaminer = null;
	public static void showRobotCellExaminer(Gui gui)
	{
		Thread thread = new Thread(new Runnable()
		{
			public void run()
			{
				if (theRobotCellExaminer == null)
				{
					//theRobotCellExaminer = new RobotCellExaminer(ActionMan.getGui().getFrame());
					theRobotCellExaminer = new RobotCellExaminer(null);
				}

				theRobotCellExaminer.setVisible(true);

				/*
				RobotCellExaminer examiner = new RobotCellExaminer(ActionMan.getGui().getFrame());
				examiner.setVisible(true);
				*/
			}
		});

		thread.start();
	}

	/*
	public static void robotStudioOpenStation(Gui gui)
	{
		new FileImporter(FileDialogs.getRobotStudioStationFileImporter(), gui)    // anonymous class
		{
			void openFile(Gui g, File f)
			{
				String stationName;

				stationName = f.getAbsolutePath();

				RobotStudioLink robotStudioLink = new RobotStudioLink(g, stationName);

				robotStudioLink.init();
			}
		};
	}

	public static void robotStudioCreateMutexZones(Gui gui)
	{
		Thread thread = new Thread(new Runnable()
		{
			public void run()
			{
				RobotStudioLink.createMutexZonesManual();
			}
		});

		thread.start();
	}

	public static void robotStudioCreateMutexZonesGrid(Gui gui)
	{
		Thread thread = new Thread(new Runnable()
		{
			public void run()
			{
				RobotStudioLink.createMutexZonesGrid();
			}
		});

		thread.start();
	}

	public static void robotStudioCreateMutexZonesFromSpan(Gui gui)
	{
		Thread thread = new Thread(new Runnable()
		{
			public void run()
			{
				RobotStudioLink.createMutexZonesFromSpan();
			}
		});

		thread.start();
	}

	public static void robotStudioExtractAutomata(Gui gui)
	{
		Thread thread = new Thread(new Runnable()
		{
			public void run()
			{
				RobotStudioLink.extractAutomata();
			}
		});

		thread.start();
	}

	public static void robotStudioExecuteRobot(Gui gui)
	{
		Automata selectedAutomata = gui.getSelectedAutomata();

		if (selectedAutomata.size() != 1)
		{
			JOptionPane.showMessageDialog(gui.getComponent(), "Exactly one automaton must be selected!", "Alert", JOptionPane.ERROR_MESSAGE);

			return;
		}

		RobotStudioLink.executeRobotAutomaton(selectedAutomata.getAutomatonAt(0));
	}

	public static void robotStudioKill()
	{
		RobotStudioLink.kill();
	}

	public static void robotStudioTest(Gui gui)
	{
		RobotStudioLink.test(gui);
	}
	*/

	// CoordinationABB of robots in Robot Studio
	public static void createPathsInRS(Gui gui)
	{
		Thread thread = new Thread(new Runnable()
		{
			public void run()
			{
				CreateXml.createPathsInRS();
			}
		});

		thread.start();
	}

	public static void createSpansInRS(Gui gui)
	{
		Thread thread = new Thread(new Runnable()
		{
			public void run()
			{
				CreateXml.createSpansInRS();
			}
		});

		thread.start();
	}

	public static void createMutexZonesInRS(Gui gui)
	{
		Thread thread = new Thread(new Runnable()
		{
			public void run()
			{
				CreateXml.createMutexZonesInRS();
			}
		});

		thread.start();
	}

	public static void addViaPointsInRS(Gui gui)
	{
		Thread thread = new Thread(new Runnable()
		{
			public void run()
			{
				CreateXml.addViaPointsInRS();
			}
		});

		thread.start();
	}

	public static void buildXmlFile(Gui gui)
	{
		CreateXml.buildXmlFile();
	}

	public static void executeScheduledAutomaton(Gui gui)
	{
		Automata selectedAutomata = gui.getSelectedAutomata();

		if (selectedAutomata.size() != 1)
		{
			JOptionPane.showMessageDialog(gui.getComponent(), "Exactly one automaton must be selected!", "Alert", JOptionPane.ERROR_MESSAGE);

			return;
		}

		final Automaton a = new Automaton(selectedAutomata.getAutomatonAt(0));
		Thread thread = new Thread(new Runnable()
		{
			public void run()
			{
				CreateXml.executeScheduledAutomaton(a);
			}
		});

		thread.start();
	}

	public static void demonstrate(Gui gui)
	{
		Thread thread = new Thread(new Runnable()
		{
			public void run()
			{
				CreateXml.demoCoordination();
			}
		});

		thread.start();
	}

	// TestCases... - open the test cases dialog, and add the result to the current set of automata public static void testCases(Gui gui)
	public static void testCases(Gui gui)
		throws Exception
	{
		TestCasesDialog testCasesDialog = new TestCasesDialog(gui.getFrame(), gui);

		testCasesDialog.setVisible(true);

		//Project project = testCasesDialog.getProject();

/*
				if (project != null)
				{
						gui.addProject(project);
				}
*/
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
			logger.error("Exception in animator.", ex);
			logger.debug(ex.getStackTrace());
		}
	}

	// Generate SattLine SFCs
	public static void AutomataToSattLineSFC(Gui gui)
	{
		Project selectedProject = gui.getSelectedProject();

		if (selectedProject.size() < 1)
		{
			JOptionPane.showMessageDialog(gui.getComponent(), "At least one automaton must be selected!", "Alert", JOptionPane.ERROR_MESSAGE);

			return;
		}

		if (!selectedProject.isAllEventsPrioritized())
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
					String filename = currFile.getName();

					if (pathName.endsWith(".s"))
					{
						prefixName = pathName.substring(0, pathName.length() - 2);
					}
					else
					{
						prefixName = pathName;
					}

					File sFile = new File(prefixName + ".s");
					File gFile = new File(prefixName + ".g");
					File lFile = new File(prefixName + ".l");
					File pFile = new File(prefixName + ".p");

					try
					{
						AutomataToSattLineSFC exporter = new AutomataToSattLineSFC(selectedProject);

						exporter.serialize_s(sFile, filename);
						exporter.serialize_g(gFile, filename);
						exporter.serialize_l(lFile, filename);
						exporter.serialize_p(pFile, filename);
					}
					catch (Exception ex)
					{
						logger.error("Exception while generating SattLine code to files " + prefixName + "{\".s\", \".g\", \".l\", \".p\"}");
						logger.debug(ex.getStackTrace());

						return;
					}

					logger.info("SattLine SFC files successfully generated at " + prefixName + "{\".s\", \".g\", \".l\", \".p\"}");
				}
			}
		}
	}

	// Generate SattLine SFCs for the Ball Process
	public static void AutomataToSattLineSFCForBallProcess(Gui gui)
	{
		Project selectedProject = gui.getSelectedProject();

		if (selectedProject.size() < 1)
		{
			JOptionPane.showMessageDialog(gui.getComponent(), "At least one automaton must be selected!", "Alert", JOptionPane.ERROR_MESSAGE);

			return;
		}

		if (!selectedProject.isAllEventsPrioritized())
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
					String filename = currFile.getName();

					if (pathName.endsWith(".s"))
					{
						prefixName = pathName.substring(0, pathName.length() - 2);
					}
					else
					{
						prefixName = pathName;
					}

					File sFile = new File(prefixName + ".s");
					File gFile = new File(prefixName + ".g");
					File lFile = new File(prefixName + ".l");
					File pFile = new File(prefixName + ".p");

					try
					{
						AutomataToSattLineSFCForBallProcess exporter = new AutomataToSattLineSFCForBallProcess(selectedProject);

						exporter.serialize_s(sFile, filename);
						exporter.serialize_g(gFile, filename);
						exporter.serialize_l(lFile, filename);
						exporter.serialize_p(pFile, filename);
					}
					catch (Exception ex)
					{
						logger.error("Exception while generating Ball Process SattLine code to files " + prefixName + "{\".s\", \".g\", \".l\", \".p\"}");
						logger.debug(ex.getStackTrace());

						return;
					}

					logger.info("SattLine SFC files for the Ball Process successfully generated at " + prefixName + "{\".s\", \".g\", \".l\", \".p\"}");
				}
			}
		}
	}

	// Generate ABB Control Builder SFCs
	public static void AutomataToControlBuilderSFC(Gui gui)
	{
		Project selectedProject = gui.getSelectedProject();

		if (selectedProject.size() < 1)
		{
			JOptionPane.showMessageDialog(gui.getComponent(), "At least one automaton must be selected!", "Alert", JOptionPane.ERROR_MESSAGE);

			return;
		}

		if (selectedProject.hasSelfLoop())
		{
			JOptionPane.showMessageDialog(gui.getComponent(), "Self-loops are not supported in SFC. The ST and IL mode can handle self-loops!", "Not supported", JOptionPane.ERROR_MESSAGE);

			return;
		}

		if (!selectedProject.isAllEventsPrioritized())
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
					String filename = currFile.getName();

					if (pathName.endsWith(".prj"))
					{
						prefixName = pathName.substring(0, pathName.length() - 4);
						filename = filename.substring(0, filename.length() - 4);
					}
					else
					{
						prefixName = pathName;
					}

					File appFile = new File(prefixName + ".app");
					File prjFile = new File(prefixName + ".prj");

					try
					{
						AutomataToControlBuilderSFC exporter = new AutomataToControlBuilderSFC(selectedProject);

						exporter.serializeApp(appFile, filename);
						exporter.serializePrj(prjFile, filename);
					}
					catch (Exception ex)
					{
						logger.error("Exception while generating Control Builder code to files " + prefixName + "{\".prj\", \".app\"}");
						logger.debug(ex.getStackTrace());

						return;
					}

					logger.info("ABB Control Builder SFC files successfully generated at " + prefixName + "{\".prj\", \".app\"}");
				}
			}
		}
	}

	//open JgrafchartEditor
	public static void openJGrafchartEditor(Gui gui)
	{
		try
		{
			VisualProjectContainer projectContainer = gui.getVisualProjectContainer();
			VisualProject theProject = (VisualProject) projectContainer.getActiveProject();

			theProject.getJGrafchartEditor();
		}
		catch (Exception ex)
		{
			logger.error("Exception while getting JGrafchart Editor");
			logger.debug(ex.getStackTrace());

			return;
		}
	}

	//shoeFactory - Config
	public static void shoeFactoryConfigurator()
	{
		Configit con = new Configit(gui);

		con.setLocationRelativeTo(null);
		con.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		try
		{
			con.setVisible(true);
		}
		catch (Exception ex)
		{
			logger.error("shoeFactoryConfigurator: " + ex.getMessage());
		}
	}

	// shoeFactory - build plant
	public static void shoeFactoryBuildPlant(Gui gui)
	{

		//Project selectedProject = gui.getSelectedProject();
		Plant newPlant = new Plant();
		Project newProject = newPlant.getPlant();

		try
		{
			gui.addProject(newProject);
		}
		catch (Exception ex)
		{
			logger.error("shoeFactoryBuildPlant: " + ex.getMessage());
		}
	}

	//shoeFactory - build SFC
	public static void shoeFactoryConfiguratorDEMO()
	{
		ConfigitDEMO con = new ConfigitDEMO(gui);

		con.setLocationRelativeTo(null);
		con.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		try
		{
			con.setVisible(true);
		}
		catch (Exception ex)
		{
			logger.error("shoeFactoryConfigurator: " + ex.getMessage());
		}
	}

	// Generate ABB Control Builder IL
	public static void ProjectToControlBuilderIL(Gui gui)
	{
		Project selectedProject = gui.getSelectedProject();

		if (selectedProject.size() < 1)
		{
			JOptionPane.showMessageDialog(gui.getComponent(), "At least one automaton must be selected!", "Alert", JOptionPane.ERROR_MESSAGE);

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
					String filename = currFile.getName();

					if (pathName.endsWith(".prj"))
					{
						prefixName = pathName.substring(0, pathName.length() - 4);
						filename = filename.substring(0, filename.length() - 4);
					}
					else
					{
						prefixName = pathName;
					}

					File appFile = new File(prefixName + ".app");
					File prjFile = new File(prefixName + ".prj");

					try
					{
						AutomataToControlBuilderIL exporter = new AutomataToControlBuilderIL(selectedProject);

						exporter.serializeApp(appFile, filename);
						exporter.serializePrj(prjFile, filename);
					}
					catch (Exception ex)
					{
						logger.error("Exception while generating Control Builder code to files " + prefixName + "{\".prj\", \".app\"}");
						logger.debug(ex.getStackTrace());

						return;
					}

					logger.info("ABB Control Builder IL files successfully generated at " + prefixName + "{\".prj\", \".app\"}");
				}
			}
		}
	}

	// Generate ABB Control Builder ST
	public static void ProjectToControlBuilderST(Gui gui)
	{
		Project selectedProject = gui.getSelectedProject();

		if (selectedProject.size() < 1)
		{
			JOptionPane.showMessageDialog(gui.getComponent(), "At least one automaton must be selected!", "Alert", JOptionPane.ERROR_MESSAGE);

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
					String filename = currFile.getName();

					if (pathName.endsWith(".prj"))
					{
						prefixName = pathName.substring(0, pathName.length() - 4);
						filename = filename.substring(0, filename.length() - 4);
					}
					else
					{
						prefixName = pathName;
					}

					File appFile = new File(prefixName + ".app");
					File prjFile = new File(prefixName + ".prj");

					try
					{
						AutomataToControlBuilderST exporter = new AutomataToControlBuilderST(selectedProject);

						exporter.serializeApp(appFile, filename);
						exporter.serializePrj(prjFile, filename);
					}
					catch (Exception ex)
					{
						logger.error("Exception while generating Control Builder code to files " + prefixName + "{\".prj\", \".app\"}");
						logger.debug(ex.getStackTrace());

						return;
					}

					logger.info("ABB Control Builder ST files successfully generated at " + prefixName + "{\".prj\", \".app\"}");
				}
			}
		}
	}

	// Generate C-code
	public static void AutomataToC(Gui gui)
	{
		Automata selectedAutomata = gui.getSelectedAutomata();

		if (selectedAutomata.size() < 1)
		{
			JOptionPane.showMessageDialog(gui.getComponent(), "At least one automaton must be selected!", "Alert", JOptionPane.ERROR_MESSAGE);

			return;
		}

		JFileChooser fileExporter = FileDialogs.getExportFileChooser(FileFormats.C);

		if (fileExporter.showSaveDialog(gui.getComponent()) == JFileChooser.APPROVE_OPTION)
		{
			File currFile = fileExporter.getSelectedFile();

			if (currFile != null)
			{
				if (!currFile.isDirectory())
				{
					try
					{
						AutomataToC exporter = new AutomataToC(selectedAutomata);
						PrintWriter theWriter = new PrintWriter(new FileWriter(currFile));

						exporter.serialize(theWriter);
						theWriter.close();
					}
					catch (Exception ex)
					{
						logger.error("Exception while generating C code to file " + currFile.getAbsolutePath());
						logger.debug(ex.getStackTrace());

						return;
					}

					logger.info("C file successfully generated at " + currFile.getAbsolutePath());
				}
			}
		}
	}

	// Generate Java-code
	public static void AutomataToJava(Gui gui)
	{
		Project selectedProject = gui.getSelectedProject();

		if (selectedProject.size() < 1)
		{
			JOptionPane.showMessageDialog(gui.getComponent(), "At least one automaton must be selected!", "Alert", JOptionPane.ERROR_MESSAGE);

			return;
		}

		JFileChooser fileExporter = FileDialogs.getExportFileChooser(FileFormats.JAVA);

		if (fileExporter.showSaveDialog(gui.getComponent()) == JFileChooser.APPROVE_OPTION)
		{
			File currFile = fileExporter.getSelectedFile();

			if (currFile != null)
			{
				if (!currFile.isDirectory())
				{
					try
					{

						//Assuming a filename in the form classname.java
						String classname = currFile.getName().substring(0, currFile.getName().length() - 5);
						AutomataToJava exporter = new AutomataToJava(selectedProject, classname);
						PrintWriter theWriter = new PrintWriter(new FileWriter(currFile));

						exporter.serialize(theWriter);
						theWriter.close();
					}
					catch (Exception ex)
					{
						logger.error("Exception while generating Java code to file " + currFile.getAbsolutePath());
						logger.debug(ex.getStackTrace());

						return;
					}

					logger.info("Java file successfully generated at " + currFile.getAbsolutePath());
				}
			}
		}
	}

	// Generate Mindstorm NQC (Not Quite C)
	public static void AutomataToMindstormNQC(Gui gui)
	{
		Automata selectedAutomata = gui.getSelectedAutomata();

		if (selectedAutomata.size() < 1)
		{
			JOptionPane.showMessageDialog(gui.getComponent(), "At least one automaton must be selected!", "Alert", JOptionPane.ERROR_MESSAGE);

			return;
		}

		JFileChooser fileExporter = FileDialogs.getNQCFileExporter();

		if (fileExporter.showSaveDialog(gui.getComponent()) == JFileChooser.APPROVE_OPTION)
		{
			File currFile = fileExporter.getSelectedFile();

			if (currFile != null)
			{
				if (!currFile.isDirectory())
				{
					try
					{
						AutomataToNQC exporter = new AutomataToNQC(selectedAutomata);
						PrintWriter theWriter = new PrintWriter(new FileWriter(currFile));

						exporter.serializeNQC(theWriter);
						theWriter.close();
					}
					catch (Exception ex)
					{
						logger.error("Exception while generating Mindstorm NQC text code to file " + currFile.getAbsolutePath());
						logger.debug(ex.getStackTrace());

						return;
					}

					logger.info("Mindstorm NQC file successfully generated at " + currFile.getAbsolutePath());
				}
			}
		}
	}

	// Generate SMV (Symbolic Model Verifier)
	public static void AutomataToSMV(Gui gui)
	{
		Automata selectedAutomata = gui.getSelectedAutomata();

		if (selectedAutomata.size() < 1)
		{
			JOptionPane.showMessageDialog(gui.getComponent(), "At least one automaton must be selected!", "Alert", JOptionPane.ERROR_MESSAGE);

			return;
		}

		if (!selectedAutomata.isAllEventsPrioritized())
		{
			JOptionPane.showMessageDialog(gui.getComponent(), "All events must prioritized in this mode!", "Not supported", JOptionPane.ERROR_MESSAGE);

			return;
		}

		JFileChooser fileExporter = FileDialogs.getExportFileChooser(FileFormats.SMV);

		if (fileExporter.showSaveDialog(gui.getComponent()) == JFileChooser.APPROVE_OPTION)
		{
			File currFile = fileExporter.getSelectedFile();

			if (currFile != null)
			{
				if (!currFile.isDirectory())
				{
					try
					{
						AutomataToSMV exporter = new AutomataToSMV(selectedAutomata);
						PrintWriter theWriter = new PrintWriter(new FileWriter(currFile));

						exporter.serializeSMV(theWriter);
						theWriter.close();
					}
					catch (Exception ex)
					{
						logger.error("Exception while generating SMV text code to file " + currFile.getAbsolutePath());
						logger.debug(ex.getStackTrace());

						return;
					}

					logger.info("SMVfile successfully generated at " + currFile.getAbsolutePath());
				}
			}
		}
	}

	// Generate IEC-61499 Function Block
	public static void ProjectToIEC61499(Gui gui)
	{
		// Automata selectedProject = gui.getselectedProject();
		Project selectedProject = gui.getSelectedProject();

		if (selectedProject.size() < 1) {
			JOptionPane.showMessageDialog(
						      gui.getComponent(),
						      "At least one automaton must be selected!",
						      "Alert",
						      JOptionPane.ERROR_MESSAGE);

			return;
		}

		JFileChooser fileExporter = FileDialogs.getExportFileChooser(FileFormats.FBT);

		if (fileExporter.showSaveDialog(gui.getComponent()) == JFileChooser.APPROVE_OPTION)
		{
			File currFile = fileExporter.getSelectedFile();

			if (currFile != null)
			{
				try
				{
					AutomataToIEC61499 exporter = new AutomataToIEC61499(selectedProject);
					exporter.printSources(currFile);
				}
				catch (Exception ex)
				{
					logger.error("Exception while generating IEC-61499 Function Block code to file "
						     + currFile.getAbsolutePath());
					logger.debug(ex.getMessage());
					logger.debug(ex.getStackTrace());

					return;
				}

				logger.info(
					    "IEC-61499 Function Block file successfully generated at "
					    + currFile.getAbsolutePath());
			}
		}
	}

        // Generate 1131 Structured Text
	public static void ProjectTo1131ST(Gui gui)
	{
		// Automata selectedProject = gui.getselectedProject();
		Project selectedProject = gui.getSelectedProject();

		if (selectedProject.size() < 1)
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
					try
					{
						AutomataToIEC1131 exporter = new AutomataToIEC1131(selectedProject);
						PrintWriter theWriter = new PrintWriter(new FileWriter(currFile));

						exporter.serializeStructuredText(theWriter);
						theWriter.close();
					}
					catch (Exception ex)
					{
						logger.error("Exception while generating 1131 Structured text code to file " + currFile.getAbsolutePath());
						logger.debug(ex.getMessage());
						logger.debug(ex.getStackTrace());

						return;
					}

					logger.info("IEC-61131 ST file successfully generated at " + currFile.getAbsolutePath());
				}
			}
		}
	}

	// Generate 1131 Instruction List
	public static void ProjectTo1131IL(Gui gui)
	{

		//Automata selectedProject = gui.getselectedProject();
		Project selectedProject = gui.getSelectedProject();

		if (selectedProject.size() < 1)
		{
			JOptionPane.showMessageDialog(gui.getComponent(), "At least one automaton must be selected!", "Alert", JOptionPane.ERROR_MESSAGE);

			return;
		}

		if (!selectedProject.validExecutionParameters())
		{
			JOptionPane.showMessageDialog(gui.getComponent(), "The project has illegal execution parameters", "Alert", JOptionPane.ERROR_MESSAGE);

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
						AutomataToIEC1131 exporter = new AutomataToIEC1131(selectedProject);
						PrintWriter theWriter = new PrintWriter(new FileWriter(currFile));

						exporter.serializeInstructionList(theWriter);
						theWriter.close();
					}
					catch (Exception ex)
					{
						logger.error("Exception while generating 1131 Instruction list code to file " + currFile.getAbsolutePath());
						logger.debug(ex.getMessage());
						logger.debug(ex.getStackTrace());

						return;
					}

					logger.info("IEC-61131 IL file successfully generated at " + currFile.getAbsolutePath());
				}
			}
		}
	}

	// Generate Java Bytecode
	public static void AutomataToJavaBytecode(Gui gui)
	{
		Project selectedProject = gui.getSelectedProject();

		if (selectedProject.size() < 1)
		{
			JOptionPane.showMessageDialog(gui.getComponent(), "At least one automaton must be selected!", "Alert", JOptionPane.ERROR_MESSAGE);

			return;
		}

		JFileChooser outputDir = new JFileChooser();

		outputDir.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		if (outputDir.showSaveDialog(gui.getComponent()) == JFileChooser.APPROVE_OPTION)
		{
			File currFile = outputDir.getSelectedFile();

			if (currFile != null)
			{
				if (currFile.isDirectory())
				{
					try
					{
						File tmpFile = File.createTempFile("softplc", ".il");

						tmpFile.deleteOnExit();

						AutomataToIEC1131 exporter = new AutomataToIEC1131(selectedProject);
						PrintWriter theWriter = new PrintWriter(new FileWriter(tmpFile));

						exporter.serializeInstructionList(theWriter);
						theWriter.close();
						new org.supremica.softplc.CompILer.ilc(tmpFile.getAbsolutePath(), currFile.getAbsolutePath());
					}
					catch (Exception ex)
					{
						logger.error("Exception while generating Java Bytecode to file " + currFile.getAbsolutePath());
						logger.debug(ex.getStackTrace());

						return;
					}

					logger.info("Java Bytecode file successfully generated at " + currFile.getAbsolutePath());
				}
				else
				{
					logger.info("Select a directory to export bytecode to.");
				}
			}
		}
	}

	// Run simulation
	public static void runSoftPLCSimulation(Gui gui)
	{
		Project selectedProject = gui.getSelectedProject();
		File tmpdir;

		if (selectedProject.size() < 1)
		{
			JOptionPane.showMessageDialog(gui.getComponent(), "At least one automaton must be selected!", "Alert", JOptionPane.ERROR_MESSAGE);

			return;
		}

		SoftplcSimulationDialog d = new SoftplcSimulationDialog(null, "Run Simulation...", true);

		if (!d.showDialog())
		{
			return;
		}

		System.out.println(d.getIOInterface().getPath());

		try
		{
			File tmpFile = File.createTempFile("softplc", ".il");

			tmpFile.deleteOnExit();

			AutomataToIEC1131 exporter = new AutomataToIEC1131(selectedProject);
			PrintWriter theWriter = new PrintWriter(new FileWriter(tmpFile));

			exporter.serializeInstructionList(theWriter);
			theWriter.close();

			tmpdir = org.supremica.softplc.Utils.TempFileUtils.createTempDir("softplc");

			new org.supremica.softplc.CompILer.ilc(tmpFile.getAbsolutePath(), tmpdir.getAbsolutePath());
			new org.supremica.softplc.RunTime.Shell("org.supremica.softplc.Simulator.BTSim", tmpdir.getCanonicalPath(), "AutomaticallyGeneratedProgram");
		}
		catch (Exception ex)
		{
			logger.error("Exception while generating Java Bytecode to file");
			logger.debug(ex.getStackTrace());

			return;
		}

		logger.info("Java Bytecode file successfully generated");
	}

	public static void startRecipeEditor(Gui gui)
	{
		try
		{
			VisualProjectContainer projectContainer = gui.getVisualProjectContainer();
			VisualProject theProject = (VisualProject) projectContainer.getActiveProject();

			theProject.getRecipeEditor();
		}
		catch (Exception ex)
		{
			logger.error("Exception while getting Recipe Editor");
			logger.debug(ex.getStackTrace());

			return;
		}
	}

	public static void startCellEditor(Gui gui)
	{
		try
		{
			VisualProjectContainer projectContainer = gui.getVisualProjectContainer();
			VisualProject theProject = (VisualProject) projectContainer.getActiveProject();

			theProject.getCellEditor();
		}
		catch (Exception ex)
		{
			logger.error("Exception while getting Recipe Editor");
			logger.debug(ex.getStackTrace());

			return;
		}
	}

	/**
	 * Simplify the Supremica project.
	 */
	public static void simplifyProject()
	{
		try
		{
			Project all = gui.getVisualProjectContainer().getActiveProject();
			Automata new_ = AutomataSimplifier.simplify(all);

			// clear the current automata
			gui.getVisualProjectContainer().getActiveProject().clear();
			gui.clearSelection();

			// insert the new project
			gui.addAutomata(new_);
		}
		catch (Exception ex)
		{
			logger.error(ex);
		}
	}

	///////////////
	// BDD STUFF //
	///////////////

	// BDD developer stuff: these are disabled if org.supremica.util.BDD.Options.dev_mode == false
	public static void DoBDDReachability()
	{
		org.supremica.util.BDD.test.DeveloperTest.DoReachability(gui.getSelectedAutomata());
	}

	public static void DoBDDCoReachability()
	{
		org.supremica.util.BDD.test.DeveloperTest.DoCoReachability(gui.getSelectedAutomata());
	}

	public static void DoBDDUnderConstruction()
	{
		org.supremica.util.BDD.test.DeveloperTest.DoUnderConstruction(gui.getSelectedAutomata());
	}

	/**
	 * Mark (select) automata in the dependency group of the selected automata.
	 */
	public static void markDependencySet()
	{
		try
		{
			Automata all = gui.getVisualProjectContainer().getActiveProject();
			Collection v = AutomataCommunicationHelper.getDependencyGroup(gui.getSelectedAutomata(), all);

			gui.selectAutomata(v);
		}
		catch (Exception ex)
		{
			logger.error(ex);
		}
	}

	/**
	 * select the maximal component the current selection is a part of
	 * (the current selection must be connected!)
	 */
	public static void markMaximalComponent()
	{
		try
		{
			Automata all = gui.getVisualProjectContainer().getActiveProject();
			Collection v = AutomataCommunicationHelper.getMaximalComponent(gui.getSelectedAutomata(), all);

			gui.selectAutomata(v);
		}
		catch (Exception ex)
		{
			logger.error(ex);
		}
	}
}

// ActionMan
