
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
import org.supremica.gui.automataExplorer.AutomataExplorer;
import org.supremica.gui.simulator.SimulatorExecuter;
import org.supremica.log.*;
import org.supremica.automata.IO.FileFormats;

import org.supremica.gui.texteditor.TextFrame;

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
	// gui here is filled in by
	public static Gui gui = null;
	public static LanguageRestrictor languageRestrictor = new LanguageRestrictor();
	public static StateEnumerator stateEnumerator = new StateEnumerator();

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

			gui.addProject((Project)newAutomata);

			// logger.debug("ActionMan.fileNewFromTemplate");
		}
		catch (Exception ex)
		{
			JOptionPane.showMessageDialog(gui.getComponent(), "Error while creating the template!", "Alert", JOptionPane.ERROR_MESSAGE);
			logger.debug(ex.getStackTrace());
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
				logger.error("Exception in AlphabetAnalyzer ", ex);
				logger.debug(ex.getStackTrace());
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
			logger.error(ex.toString());
			logger.debug(ex.getStackTrace());
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
				logger.error("Exception in AutomataAddSelfLoopArcs. Automaton: " + currAutomaton.getName(), ex);
				logger.debug(ex.getStackTrace());
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
				logger.error("Exception in AutomataAllAccepting. Automaton: " + currAutomaton.getName(), ex);
				logger.debug(ex.getStackTrace());
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
				logger.error("Exception in AutomatonComplement. Automaton: " + currAutomaton.getName(), ex);
				logger.debug(ex.getStackTrace());
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
				logger.error("Exception while copying the automaton ", ex);
				logger.debug(ex.getStackTrace());
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
				logger.error("Exception while removing " + currAutomatonName, ex);
				logger.debug(ex.getStackTrace());
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

	// This is baaad!
	private static final int    // instead of using constants later below :)
		FORMAT_UNKNOWN = -1,
		FORMAT_XML = 1, FORMAT_DOT = 2, FORMAT_DSX = 3,
		FORMAT_RCP = 4, FORMAT_SP = 5, FORMAT_HTML = 6,
		FORMAT_XML_DEBUG = 7, FORMAT_DOT_DEBUG = 8, FORMAT_DSX_DEBUG = 9,
		FORMAT_RCP_DEBUG = 10, FORMAT_SP_DEBUG = 11, FORMAT_HTML_DEBUG = 12;

	// This class should really act as a factory for exporter objects, but that
	// would mean rewriting the entire export/saveAs functionality. Should I bother?
	static class ExportDialog
	//	extends JDialog
	{
		private final String xmlString = "xml";
		private final String spString = "sp";
		private final String dotString = "dot";
		private final String dsxString = "dsx";
		private final String htmlString ="html";
		private final String rcpString = "rcp";                         // ++ ARASH

		private final Object[] possibleValues =
		{
			xmlString, spString, dotString, dsxString, htmlString, rcpString
		};

		private JOptionPane pane = null;
		private JDialog dialog = null;
		private JCheckBox checkbox = null;
		private Object selectedValue = null;

		ExportDialog(Frame comp)
		{
			this.pane = new JOptionPane("Export as::",
										JOptionPane.INFORMATION_MESSAGE,
										JOptionPane.OK_CANCEL_OPTION,
										null,	// icon
										null, 	// options
										null);	// initialValue

			pane.setWantsInput(true);
			pane.setSelectionValues(possibleValues);
			pane.setInitialSelectionValue(possibleValues[0]);
			pane.setComponentOrientation(((comp == null) ? pane.getRootFrame() : comp).getComponentOrientation());
			pane.selectInitialValue();

			this.checkbox = new JCheckBox("Export to debugview");
			pane.add(checkbox);

			// int style = styleFromMessageType(JOptionPane.INFORMATION_MESSAGE);
			dialog = pane.createDialog(comp, "Export");
		}

		public void show()
		{
			// this.selectedValue = JOptionPane.showInputDialog(comp, "Export as", "Export", JOptionPane.INFORMATION_MESSAGE, null, possibleValues, possibleValues[0]);
			dialog.show();
	        dialog.dispose();

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
				if(checkbox.isSelected())
				{
					return FORMAT_XML_DEBUG;
				}
				return FORMAT_XML;	// Should return an XmlExporter object
			}
			else if (selectedValue == dotString)
			{
				if(checkbox.isSelected())
				{
					return FORMAT_DOT_DEBUG;
				}
				return FORMAT_DOT;	// Should return a DotExporter object
			}
			else if (selectedValue == dsxString)
			{
				if(checkbox.isSelected())
				{
					return FORMAT_DSX_DEBUG;
				}
				return FORMAT_DSX;	// Should return a DsxExporter object
			}
			else if (selectedValue == rcpString)
			{
				if(checkbox.isSelected())
				{
					return FORMAT_RCP_DEBUG;
				}
				return FORMAT_RCP;	// Should return an RcpExporter object
			}
			else if (selectedValue == spString)
			{
				if(checkbox.isSelected())
				{
					return FORMAT_SP_DEBUG;
				}
				return FORMAT_SP;	// Should return a SpExporter object
			}
			else if (selectedValue == htmlString)
			{
				if(checkbox.isSelected())
				{
					return FORMAT_HTML_DEBUG;
				}
				return FORMAT_HTML;	// Should return a HtmlExporter object
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

		// this one comes back in the next function. we need to have duplicates otherwise we would
		// ask for the type and first then complain if nonthing is selected
		Collection selectedAutomata = gui.getSelectedAutomataAsCollection();

		if (selectedAutomata.size() < 1)
		{
			JOptionPane.showMessageDialog(gui.getComponent(), "At least one automata must be selected!", "Alert", JOptionPane.ERROR_MESSAGE);

			return;
		}

		ExportDialog dlg = new ExportDialog(gui.getFrame());
		dlg.show();
		if(dlg.wasCancelled())
		{
			return;
		}

		int exportMode = dlg.getExportMode();

		if(exportMode != FORMAT_UNKNOWN)
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

		if (selectedAutomata.size() < 1)
		{
			JOptionPane.showMessageDialog(gui.getComponent(), "At least one automata must be selected!", "Alert", JOptionPane.ERROR_MESSAGE);

			return;
		}

		// Take care of the new debug stuff first. This is really silly.
		// Proper design would have solved this problem
		if(exportMode == FORMAT_XML_DEBUG)
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
		if(exportMode == FORMAT_DOT_DEBUG)
		{
			for(Iterator autIt = selectedAutomata.iterator(); autIt.hasNext(); )
			{
				Automaton currAutomaton = (Automaton) autIt.next();
				AutomatonToDot exporter = new AutomatonToDot(currAutomaton);
				TextFrame textframe = new TextFrame("Dot debug output");
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
		if(exportMode == FORMAT_DSX_DEBUG)
		{
			for(Iterator autIt = selectedAutomata.iterator(); autIt.hasNext(); )
			{
				Automaton currAutomaton = (Automaton) autIt.next();
				AutomatonToDsx exporter = new AutomatonToDsx(currAutomaton);
				TextFrame textframe = new TextFrame("DSX debug output");
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
		if(exportMode == FORMAT_RCP_DEBUG)
		{
			for(Iterator autIt = selectedAutomata.iterator(); autIt.hasNext(); )
			{
				Automaton currAutomaton = (Automaton) autIt.next();
				AutomatonToRcp exporter = new AutomatonToRcp(currAutomaton);
				TextFrame textframe = new TextFrame("RCP debug output");
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
/*		if(exportMode == FORMAT_HTML_DEBUG)
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
		if (exportMode == FORMAT_DOT || exportMode == FORMAT_DSX || exportMode == FORMAT_RCP)
		{
			for(Iterator autIt = selectedAutomata.iterator(); autIt.hasNext(); )
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
		else if (exportMode == FORMAT_RCP)
		{
			fileExporter = FileDialogs.getExportFileChooser(FileFormats.RCP);
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
						else if (exportMode == FORMAT_RCP)
						{
							AutomatonToRcp exporter = new AutomatonToRcp(currAutomaton);

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
				logger.error("Exception in AutomataExtend. Automaton: " + currAutomaton.getName(), ex);
				logger.debug(ex.getStackTrace());
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
				logger.error("Exception in AutomataPurge. Automaton: " + currAutomaton.getName(), ex);
				logger.debug(ex.getStackTrace());
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
				logger.error("Exception in AutomataRemovePass. Automaton: " + currAutomaton.getName(), ex);
				logger.debug(ex.getStackTrace());
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
				logger.error("Exception in RemoveSelfArcs. Automaton: " + currAutomaton.getName(), ex);
				logger.debug(ex.getStackTrace());
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
				logger.error("Exception while renaming the automaton " + currAutomatonName, ex);
				logger.debug(ex.getStackTrace());
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
			logger.debug(ex.getStackTrace());
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
				syncOptions = new SynchronizationOptions(SupremicaProperties.syncNbrOfExecuters(),
														SynchronizationType.Prioritized,
														SupremicaProperties.syncInitialHashtableSize(),
														SupremicaProperties.syncExpandHashtable(),
														true,
														SupremicaProperties.syncExpandForbiddenStates(),
														false,
														false,
														true,
														SupremicaProperties.verboseMode(),
														true,
														true);
			}
			catch (Exception ex)
			{
				JOptionPane.showMessageDialog(gui.getComponent(), "Invalid synchronizationOptions", "Alert", JOptionPane.ERROR_MESSAGE);
				logger.debug(ex.getStackTrace());
				return;
			}

			Automata currAutomata = new Automata();
			Iterator autIt = selectedAutomata.iterator();

			while (autIt.hasNext())
			{
				Automaton currAutomaton = (Automaton) autIt.next();
				String currAutomatonName = currAutomaton.getName();

				// No initial state -- remove from synthesis (or cancel entirely)
				if (currAutomaton.getInitialState() == null)
				{
					int cont = JOptionPane.showConfirmDialog(gui.getComponent(),
												"The automaton " + currAutomatonName + " does not have an initial state.\nSkip it or cancel...",
												"Alert",
												JOptionPane.OK_CANCEL_OPTION,
												JOptionPane.WARNING_MESSAGE);

					if(cont == JOptionPane.OK_OPTION)
					{
						continue; // skip currAutomaton from the synthesis
					}
					else // JOptionPane.CANCEL_OPTION
					{
						return;	// cancel entirely
					}
				}
				// Undefined type -- remove from synthesis (or cancel entirely)
				if(currAutomaton.getType() == AutomatonType.Undefined)
				{
					int cont = JOptionPane.showConfirmDialog(gui.getComponent(),
														"The automaton " + currAutomatonName + " is of 'Undefined' type.\nSkip it or cancel...",
														"Alert",
														JOptionPane.OK_CANCEL_OPTION,
														JOptionPane.WARNING_MESSAGE);
					if(cont == JOptionPane.OK_OPTION)
					{
						continue; // skip currAutomaton from the synthesis
					}
					else // JOptionPane.CANCEL_OPTION
					{
						return;	// cancel entirely
					}
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
				logger.error("Exception in AutomataSynthesizer. ", ex);
				logger.debug(ex.getStackTrace());
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
				catch (Exception ex)
				{
					JOptionPane.showMessageDialog(gui.getComponent(), ex.toString(), "Alert", JOptionPane.ERROR_MESSAGE);
					logger.debug(ex.getStackTrace());
					return;
				}

				try
				{
					synthesizer.synthesize();
				}
				catch (Exception ex)
				{
					logger.error("Exception in AutomatonSynthesizer. Automaton: " + currAutomaton.getName(), ex);
					logger.debug(ex.getStackTrace());
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
			logger.debug(ex.getStackTrace());
			return;
		}

		AutomataVerificationWorker worker = new AutomataVerificationWorker(gui, currAutomata, syncOptions, verificationOptions);
	}

	// Automaton.Alphabet action performed
	public static void automatonAlphabet_actionPerformed(Gui gui)
	{
		logger.debug("ActionMan::automatonAlphabet_actionPerformed(gui)");

		Automata selectedAutomata = gui.getSelectedAutomata();

		if (selectedAutomata.size() < 1)
		{
			JOptionPane.showMessageDialog(gui.getFrame(), "At least one automata must be selected!", "Alert", JOptionPane.ERROR_MESSAGE);

			return;
		}

	/* I don't understand all this, and I don't see the meaning (and there are no comments to explain)

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
	*/
		// Why not simpy instantiate an AlphabetViewer with the given automata object?? Use AutomataViewer instead!
		try
		{
			// AlphabetViewer alphabetviewer = new AlphabetViewer(selectedAutomata);
			AutomataViewer alphabetviewer = new AutomataViewer(selectedAutomata, true, false);
			alphabetviewer.setVisible(true);
		}
		catch(Exception ex)
		{
			logger.error("Exception in AlphabetViewer", ex);
			logger.debug(ex.getStackTrace());
			return;
		}
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
			gui.error("Exception in ActionAndControlViewer.");
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
				logger.error("Exception in AutomatonExplorer. Automaton: " + currAutomaton.getName(), ex);
				logger.debug(ex.getStackTrace());
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
				logger.error("Exception in AutomataExplorer.", ex);
				logger.debug(ex.getStackTrace());
			}
		}
	}


	// Project.Simulator action performed
	public static void simulator_actionPerformed(Gui gui)
	{
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
				Automaton newAutomaton = autMinimizer.getMinimizedAutomaton();
				// Automaton newAutomaton = autMinimizer.getMinimizedAutomaton(true);

				newAutomaton.setName(newAutomatonName);
				gui.getVisualProjectContainer().getActiveProject().addAutomaton(newAutomaton);
			}
			catch (Exception ex)
			{
				logger.error("Exception in AutomatonMinimize. Automaton: " + currAutomaton.getName(), ex);
				logger.debug(ex.getStackTrace());
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
			statusStr.append("\n\tis deterministic: " + currAutomaton.isDeterministic());
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
		Automata selectedAutomata = gui.getSelectedAutomata();

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
				logger.error("Exception in AutomatonViewer. Automaton: " + currAutomaton.getName(), ex);
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
		catch (Exception ex)
		{
			// this exception is caught while opening
			logger.error("Error while opening " + file.getAbsolutePath(), ex);
			logger.debug(ex.getStackTrace());
			return;
		}

		if (!currProject.isDeterministic())
		{
			JOptionPane.showMessageDialog(gui.getComponent(), "All automata are not determinstic. Operation aborted", "alert", JOptionPane.ERROR_MESSAGE);
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

		// logger.info("Importing " + file.getAbsolutePath() + " ...");
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
			logger.error("Error while importing " + file.getAbsolutePath(), ex);
			logger.debug(ex.getStackTrace());
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

		Project project = testCasesDialog.getProject();

		if (project != null)
		{
			gui.addProject(project);
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
			logger.error("Exception in animator.", ex);
			logger.debug(ex.getStackTrace());
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
						logger.error("Exception while generating SattLine code to files " + prefixName + "{\".s\", \".g\", \".l\", \".p\"}");
						logger.debug(ex.getStackTrace());
						return;
					}
					logger.info("SattLine SFC files successfully generated at " + prefixName + "{\".s\", \".g\", \".l\", \".p\"}");

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
						AutomataToControlBuilderSFC exporter = new AutomataToControlBuilderSFC(selectedAutomata);

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


	// Generate ABB Control Builder IL
	public static void AutomataToControlBuilderIL(Gui gui)
	{
		Automata selectedAutomata = gui.getSelectedAutomata();

		if (selectedAutomata.size() < 1)
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
						AutomataToControlBuilderIL exporter = new AutomataToControlBuilderIL(selectedAutomata);

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
	public static void AutomataToControlBuilderST(Gui gui)
	{
		Automata selectedAutomata = gui.getSelectedAutomata();

		if (selectedAutomata.size() < 1)
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
						AutomataToControlBuilderST exporter = new AutomataToControlBuilderST(selectedAutomata);
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
					try
					{

						AutomataToIEC1131 exporter = new AutomataToIEC1131(selectedAutomata);

						PrintWriter theWriter = new PrintWriter(new FileWriter(currFile));

						exporter.serializeStructuredText(theWriter);

						theWriter.close();

					}
					catch (Exception ex)
					{
						logger.error("Exception while generating 1131 Structured text code to file " + currFile.getAbsolutePath());
						logger.debug(ex.getStackTrace());
						return;
					}
					logger.info("IEC-61131 ST file successfully generated at " + currFile.getAbsolutePath());
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
						AutomataToIEC1131 exporter = new AutomataToIEC1131(selectedAutomata);

						PrintWriter theWriter = new PrintWriter(new FileWriter(currFile));

						exporter.serializeInstructionList(theWriter);

						theWriter.close();

					}
					catch (Exception ex)
					{
						logger.error("Exception while generating 1131 Instruction list code to file " + currFile.getAbsolutePath());
						logger.debug(ex.getStackTrace());
						return;
					}
					logger.info("IEC-61131 IL file successfully generated at " + currFile.getAbsolutePath());
				}
			}
		}
	}
}

// ActionMan
