//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 1999-2021 Knut Akesson, Martin Fabian, Robi Malik
//###########################################################################
//# This file is part of Waters/Supremica IDE.
//# Waters/Supremica IDE is free software: you can redistribute it and/or
//# modify it under the terms of the GNU General Public License as published
//# by the Free Software Foundation, either version 2 of the License, or
//# (at your option) any later version.
//# Waters/Supremica IDE is distributed in the hope that it will be useful,
//# but WITHOUT ANY WARRANTY; without even the implied warranty of
//# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
//# Public License for more details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters/Supremica IDE statically or dynamically with other modules
//# is making a combined work based on Waters/Supremica IDE. Thus, the terms
//# and conditions of the GNU General Public License cover the whole
//# combination.
//# In addition, as a special exception, the copyright holders of
//# Waters/Supremica IDE give you permission to combine Waters/Supremica IDE
//# with code included in the standard release of Supremica under the
//# Supremica Software License Agreement (or modified versions of such code,
//# with unchanged license). You may copy and distribute such a system
//# following the terms of the GNU GPL for Waters/Supremica IDE and the
//# licenses of the other code concerned.
//# Note that people who make modified versions of Waters/Supremica IDE are
//# not obligated to grant this special exception for their modified versions;
//# it is their choice whether to do so. The GNU General Public License gives
//# permission to release a modified version without this exception; this
//# exception also makes it possible to release a modified version which
//# carries forward this exception.
//###########################################################################

package org.supremica.gui;

import java.awt.Component;
import java.awt.Container;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import net.sourceforge.waters.model.options.OptionFileManager;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.ProxyUnmarshaller;
import net.sourceforge.waters.model.marshaller.SAXModuleMarshaller;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.plain.module.ModuleElementFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.automata.Alphabet;
import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.automata.Project;
import org.supremica.automata.IO.AutomataSSPCExporter;
import org.supremica.automata.IO.AutomataToC;
import org.supremica.automata.IO.AutomataToCommunicationGraph;
import org.supremica.automata.IO.AutomataToControlBuilderSFC;
import org.supremica.automata.IO.AutomataToIEC61499;
import org.supremica.automata.IO.AutomataToJava;
import org.supremica.automata.IO.AutomataToNQC;
import org.supremica.automata.IO.AutomataToSMV;
import org.supremica.automata.IO.AutomataToSTS;
import org.supremica.automata.IO.AutomataToSattLineSFC;
import org.supremica.automata.IO.AutomataToSattLineSFCForBallProcess;
import org.supremica.automata.IO.AutomataToXML;
import org.supremica.automata.IO.AutomatonToDot;
import org.supremica.automata.IO.AutomatonToDsx;
import org.supremica.automata.IO.AutomatonToFSM;
import org.supremica.automata.IO.EncodingHelper;
import org.supremica.automata.IO.FileFormats;
import org.supremica.automata.IO.ProjectBuildFromFSM;
import org.supremica.automata.IO.ProjectBuildFromHISC;
import org.supremica.automata.IO.ProjectBuildFromHYB;
import org.supremica.automata.IO.ProjectBuildFromSwingEngine;
import org.supremica.automata.IO.ProjectBuildFromWaters;
import org.supremica.automata.IO.ProjectBuildFromXML;
import org.supremica.automata.IO.ProjectToHtml;
import org.supremica.automata.algorithms.AddSelfArcs;
import org.supremica.automata.algorithms.AlphabetAnalyzer;
import org.supremica.automata.algorithms.AlphabetNormalize;
import org.supremica.automata.algorithms.AutomataCommunicationHelper;
import org.supremica.automata.algorithms.AutomataExtender;
import org.supremica.automata.algorithms.AutomataSynthesizer;
import org.supremica.automata.algorithms.AutomatonAllAccepting;
import org.supremica.automata.algorithms.AutomatonComplement;
import org.supremica.automata.algorithms.AutomatonPurge;
import org.supremica.automata.algorithms.ComputerHumanExtender;
import org.supremica.automata.algorithms.GeneticAlgorithms;
import org.supremica.automata.algorithms.RemovePassEvent;
import org.supremica.automata.algorithms.RemoveSelfArcs;
import org.supremica.automata.algorithms.SynchronizationOptions;
import org.supremica.automata.algorithms.SynthesizerOptions;
import org.supremica.automata.algorithms.VerificationOptions;
import org.supremica.automata.algorithms.VerificationType;
import org.supremica.automata.algorithms.minimization.MinimizationOptions;
import org.supremica.automata.templates.TemplateItem;
import org.supremica.gui.animators.scenebeans.AnimationItem;
import org.supremica.gui.animators.scenebeans.Animator;
import org.supremica.gui.automataExplorer.AutomataExplorer;
import org.supremica.gui.examplegenerator.TestCasesDialog;
import org.supremica.gui.ide.actions.IDEAction;
import org.supremica.gui.simulator.SimulatorExecuter;
import org.supremica.gui.texteditor.TextFrame;
import org.supremica.gui.useractions.HelpAction;
import org.supremica.gui.useractions.OpenAction;
import org.supremica.gui.useractions.SaveAction;
import org.supremica.gui.useractions.SaveAsAction;
import org.supremica.gui.useractions.SynthesizeAction;
import org.supremica.properties.Config;

import org.swixml.SwingEngine;

// -- MF -- Abstract class to save on duplicate code
// -- From this class is instantiated anonymous classes that implement the openFile properly

abstract class FileImporter
{
    FileImporter(final JFileChooser fileOpener, final Gui gui)
    {
        if (fileOpener.showOpenDialog(gui.getFrame()) == JFileChooser.APPROVE_OPTION)
        {
            final File[] currFiles = fileOpener.getSelectedFiles();

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

/**
 * Handles most of the actions in Supremica. Will be reimplemented to {@link IDEAction}s.
 */
public class ActionMan
{
    private static Logger logger = LogManager.getLogger(ActionMan.class);

    // Ugly fixx here. We need a good way to globally get at the selected automata, the current project etc
    // gui here is filled in by (who?)
    public static Gui gui = null;
    //public static final LanguageRestrictor languageRestrictor = new LanguageRestrictor();
    //public static final EventHider eventHider = new EventHider(getGui());
    public static final FindStates findStates = new FindStates();
    public static final StateEnumerator stateEnumerator = new StateEnumerator();
    public static final HelpAction helpAction = new HelpAction();
    public static final OpenAction openAction = new OpenAction();    // defined in MainToolBar (just for fun :-)
    public static final SaveAction saveAction = new SaveAction();
    public static final SaveAsAction saveAsAction = new SaveAsAction();
    public static final SynthesizeAction synthesizeAction = new SynthesizeAction();

    public static Gui getGui()
    {
        return gui;
    }

    private static int getIntegerInDialogWindow(final String text, final Component parent)
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
            catch (final Exception ex)
            {
                JOptionPane.showMessageDialog(parent, "Not a valid integer", "Alert", JOptionPane.ERROR_MESSAGE);
                logger.debug(ex.getStackTrace());
            }
        }

        return theIntValue;
    }

    // File.New action performed
    public static void fileNew(final Gui gui)
    {}

    // File.NewFromTemplate action performed
    public static void fileNewFromTemplate(final Gui gui, final TemplateItem item)
    {
        // logger.debug("ActionMan.fileNewFromTemplate Start");
        Project project;

        try
        {
            project = item.createInstance(new VisualProjectFactory());
            if (!project.isDeterministic())
            {
                logger.warn("Nondeterministic automaton loaded. Some algorithms are not guaranteed to work.");
            }

            try
            {
                final int nbrOfAddedAutomata = gui.addProject(project);
                logger.info("Successfully added " + nbrOfAddedAutomata + " automata.");
            }
            catch (final Exception excp)
            {
                logger.error("Error adding automata ", excp);
                logger.debug(excp.getStackTrace());

                return;
            }

            // logger.debug("ActionMan.fileNewFromTemplate");
        }
        catch (final Exception ex)
        {
            JOptionPane.showMessageDialog(gui.getComponent(), "Error while creating the template!", "Alert", JOptionPane.ERROR_MESSAGE);
            logger.debug(ex.getStackTrace());
        }
    }

    // Automata.AlphabetAnalyzer action performed
    public static void alphabetAnalyzer_actionPerformed(final Gui gui)
    {
        final Automata selectedAutomata = gui.getSelectedAutomata();

        if (!selectedAutomata.sanityCheck(gui.getComponent(), 2, false, false, true, true))
        {
            return;
        }

        // Analyze the alphabets
        final AlphabetAnalyzer theAnalyzer = new AlphabetAnalyzer(selectedAutomata);

        try
        {
            theAnalyzer.execute();
        }
        catch (final Exception ex)
        {
            logger.error("Exception in AlphabetAnalyzer ", ex);
            logger.debug(ex.getStackTrace());
        }

        logger.info("Size of union alphabet: " + selectedAutomata.getUnionAlphabet().size());

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

    // Automata.AddSelfLoopArcs action performed
    public static void automataAddSelfLoopArcs_actionPerformed(final Gui gui)
    {
        final Automata selectedAutomata = gui.getSelectedAutomata();

        if (!selectedAutomata.sanityCheck(gui.getComponent(), 1))
        {
            return;
        }

        final Iterator<?> autIt = selectedAutomata.iterator();
        while (autIt.hasNext())
        {
            final Automaton currAutomaton = (Automaton) autIt.next();

            try
            {
                AddSelfArcs.execute(currAutomaton, true);
            }
            catch (final Exception ex)
            {
                logger.error("Exception in AutomataAddSelfLoopArcs. Automaton: " + currAutomaton.getName(), ex);
                logger.debug(ex.getStackTrace());
            }
        }
    }

    // Automaton.AllAccepting action performed
    public static void automataAllAccepting_actionPerformed(final Gui gui)
    {
        final Automata selectedAutomata = gui.getSelectedAutomata();

        if (!selectedAutomata.sanityCheck(gui.getComponent(), 1))
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
        final Iterator<?> autIt = selectedAutomata.iterator();

        while (autIt.hasNext())
        {
            final Automaton currAutomaton = (Automaton) autIt.next();
            final AutomatonAllAccepting allAccepting = new AutomatonAllAccepting(currAutomaton);

            try
            {
                allAccepting.execute();
            }
            catch (final Exception ex)
            {
                logger.error("Exception in AutomataAllAccepting. Automaton: " + currAutomaton.getName(), ex);
                logger.debug(ex.getStackTrace());
            }
        }
    }

    // Automaton.Complement action performed
    public static void automataComplement_actionPerformed(final Gui gui)
    {
        final Automata selectedAutomata = gui.getSelectedAutomata();

        if (!selectedAutomata.sanityCheck(gui.getComponent(), 1))
        {
            return;
        }

        final Iterator<?> autIt = selectedAutomata.iterator();
        while (autIt.hasNext())
        {
            final Automaton currAutomaton = (Automaton) autIt.next();
            final String newAutomatonName = gui.getNewAutomatonName("Please enter a new name", currAutomaton.getName() + "_c");

            if (newAutomatonName == null)
            {
                return;
            }

            try
            {
                final AutomatonComplement automataComplement = new AutomatonComplement(currAutomaton);
                final Automaton newAutomaton = automataComplement.execute();

                newAutomaton.setName(newAutomatonName);
                gui.getVisualProjectContainer().getActiveProject().addAutomaton(newAutomaton);
            }
            catch (final Exception ex)
            {
                logger.error("Exception in AutomatonComplement. Automaton: " + currAutomaton.getName(), ex);
                logger.debug(ex.getStackTrace());
            }
        }
    }

    // Automata.Copy action performed
    public static void automataCopy_actionPerformed(final Gui gui)
    {
        final Automata selectedAutomata = gui.getSelectedAutomata();

        if (!selectedAutomata.sanityCheck(gui.getComponent(), 1))
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
        final Iterator<?> autIt = selectedAutomata.iterator();

        while (autIt.hasNext())
        {
            final Automaton currAutomaton = (Automaton) autIt.next();
            final String newAutomatonName = gui.getNewAutomatonName("Please enter a new name", currAutomaton.getName() + "(2)");

            if (newAutomatonName == null)
            {
                return;
            }

            try
            {
                final Automaton newAutomaton = new Automaton(currAutomaton);

                newAutomaton.setName(newAutomatonName);
                gui.getVisualProjectContainer().getActiveProject().addAutomaton(newAutomaton);
            }
            catch (final Exception ex)
            {
                logger.error("Exception while copying the automaton ", ex);
                logger.debug(ex.getStackTrace());
            }
        }
    }

    // ** Delete - remove from the container, clear the selection,
    // mark the project as dirty but do not close the project
    public static void automataDelete_actionPerformed(final Gui gui)
    {
        final Automata selectedAutomata = gui.getSelectedAutomata();

        final Iterator<?> autIt = selectedAutomata.iterator();

        while (autIt.hasNext())
        {
            final Automaton currAutomaton = (Automaton) autIt.next();
            final String currAutomatonName = currAutomaton.getName();

            try
            {
                gui.getVisualProjectContainer().getActiveProject().removeAutomaton(currAutomatonName);
            }
            catch (final Exception ex)
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
    public static void automataMove_actionPerformed(final Gui gui, final boolean directionIsUp, final boolean allTheWay)
    {
        final Automata selectedAutomata = gui.getSelectedAutomata();
        if (!selectedAutomata.sanityCheck(gui.getComponent(), 1))
        {
            return;
        }

        final Project theProject = gui.getVisualProjectContainer().getActiveProject();

        if (selectedAutomata.size() == theProject.size())
        {
            JOptionPane.showMessageDialog(gui.getComponent(), "No point in moving all automata, right?", "Alert", JOptionPane.ERROR_MESSAGE);

            return;
        }

        // selectionIndices are the indices of the automata that should be selected after the move!
        final int[] selectionIndices = new int[selectedAutomata.size()];
        int index = 0;

        if (allTheWay)
        {
            // Move all the way...
            if (directionIsUp)
            {
                int i = 0;

                for (final Iterator<Automaton> autIt = selectedAutomata.iterator();
                autIt.hasNext(); )
                {
                    theProject.moveAutomaton(autIt.next(), i);

                    selectionIndices[index++] = i++;
                }
            }
            else
            {
                int i = theProject.size() - 1;

                for (final Iterator<Automaton> autIt = selectedAutomata.backwardsIterator();
                autIt.hasNext(); )
                {
                    theProject.moveAutomaton(autIt.next(), i);

                    selectionIndices[index++] = i--;
                }
            }
        }
        else
        {

            // Avoid automata that can't move any further
            Iterator<?> autIt;

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


    // ** Export - shouldn't there be an exporter object?
    // it is now (ARASH)
    public static void automataExport(final Gui gui)
    {
        final Automata selectedAutomata = gui.getSelectedAutomata();

        if (!selectedAutomata.sanityCheck(gui.getComponent(), 1))
        {
            return;
        }

        final ExportDialog dlg = new ExportDialog(gui.getFrame());

        dlg.show();

        if (dlg.wasCancelled())
        {
            return;
        }

        final ExportFormat exportMode = dlg.getExportMode();

        if (exportMode != ExportFormat.UNKNOWN)
        {
            automataExport(gui, exportMode);
        }
    }

    // Exporter when the type is already known
    // Add new export functions here and to the function above
    // MF: It's not that simple. The code below defeats that purpose. Where are the exporter objects?
    // OO was invented just to avoid the type of code below. It's a maintenance nightmare!!
    public static void automataExport(final Gui gui, final ExportFormat exportMode)
    {
        final Automata selectedAutomata = gui.getSelectedAutomata();

        if (!selectedAutomata.sanityCheck(gui.getComponent(), 1))
        {
            return;
        }

        if ((exportMode == ExportFormat.FSM_DEBUG) || (exportMode == ExportFormat.FSM))
        {
            // UMDES cannot deal with forbidden states
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
        if (exportMode == ExportFormat.XML_DEBUG)
        {
            final AutomataToXML xport = new AutomataToXML(gui.getSelectedProject());
            final TextFrame textframe = new TextFrame("XML debug output");

            xport.serialize(textframe.getPrintWriter());

            return;
        }
/*
        if (exportMode == ExportFormat.SP_DEBUG)
        {
            ProjectToSP exporter = new ProjectToSP(gui.getSelectedProject());
            TextFrame textframe = new TextFrame("SP debug output");

            exporter.serialize(textframe.getPrintWriter());

            return;
        }
*/
        if (exportMode == ExportFormat.DOT_DEBUG)
        {
            for (final Iterator<?> autIt = selectedAutomata.iterator();
            autIt.hasNext(); )
            {
                final Automaton currAutomaton = (Automaton) autIt.next();
                final AutomatonToDot exporter = new AutomatonToDot(currAutomaton);
                final TextFrame textframe = new TextFrame("Dot debug output");

                try
                {
                    exporter.serialize(textframe.getPrintWriter());
                }
                catch (final Exception ex)
                {
                    logger.debug(ex.getStackTrace());
                }
            }

            return;
        }

        if (exportMode == ExportFormat.DSX_DEBUG)
        {
            for (final Iterator<?> autIt = selectedAutomata.iterator();
            autIt.hasNext(); )
            {
                final Automaton currAutomaton = (Automaton) autIt.next();
                final AutomatonToDsx exporter = new AutomatonToDsx(currAutomaton);
                final TextFrame textframe = new TextFrame("DSX debug output");

                try
                {
                    exporter.serialize(textframe.getPrintWriter());
                }
                catch (final Exception ex)
                {
                    logger.debug(ex.getStackTrace());
                }
            }

            return;
        }

        if (exportMode == ExportFormat.FSM_DEBUG)
        {
            for (final Iterator<?> autIt = selectedAutomata.iterator();
            autIt.hasNext(); )
            {
                final Automaton currAutomaton = (Automaton) autIt.next();
                final AutomatonToFSM exporter = new AutomatonToFSM(currAutomaton);
                final TextFrame textframe = new TextFrame("FSM debug output");

                try
                {
                    exporter.serialize(textframe.getPrintWriter());
                }
                catch (final Exception ex)
                {
                    logger.debug(ex.getStackTrace());
                }
            }

            return;
        }

        if (exportMode == ExportFormat.PCG_DEBUG)
        {
            final AutomataToCommunicationGraph a2cg = new AutomataToCommunicationGraph(selectedAutomata);
            final TextFrame textframe = new TextFrame("PCG debug output");

            try
            {
                a2cg.serialize(textframe.getPrintWriter());
            }
            catch (final Exception ex)
            {
                logger.debug(ex.getStackTrace());
            }

            return;
        }
        else if ((exportMode == ExportFormat.PCG) || (exportMode == ExportFormat.SSPC))
        {
            final JFileChooser fileExporter = new JFileChooser();

            fileExporter.setDialogTitle("Save as ...");

            if (fileExporter.showSaveDialog(gui.getComponent()) == JFileChooser.APPROVE_OPTION)
            {
                final File currFile = fileExporter.getSelectedFile();

                if (currFile == null)
                {
                    return;
                }

                try
                {
                    if (exportMode == ExportFormat.PCG)
                    {
                        final AutomataToCommunicationGraph a2cg = new AutomataToCommunicationGraph(selectedAutomata);

                        a2cg.serialize(currFile.getAbsolutePath());
                    }
                    else
                    {
                        new AutomataSSPCExporter(selectedAutomata, currFile.getAbsolutePath());
                    }
                }
                catch (final Exception ex)
                {
                    logger.debug(ex.getStackTrace());
                    ex.printStackTrace();    // TEMP!
                }
            }

            return;
        }

                /*              if(exportMode == ExportFormat.HTML_DEBUG)
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
        if ((exportMode == ExportFormat.DOT) || (exportMode == ExportFormat.DSX) || (exportMode == ExportFormat.FSM) || (exportMode == ExportFormat.PCG) || (exportMode == ExportFormat.STS))
        {
            for (final Iterator<?> autIt = selectedAutomata.iterator();
            autIt.hasNext(); )
            {
                final Automaton currAutomaton = (Automaton) autIt.next();

                automatonExport(gui, exportMode, currAutomaton);
            }
        }
        else
        {
            if (exportMode == ExportFormat.XML)
            {
                FileDialogs.getXMLFileExporter();

                return;
            }
        }
    }

    // Exporter when the type is already known
    // Add new export functions here and to the function above
    public static void automatonExport(final Gui gui, final ExportFormat exportMode, final Automaton currAutomaton)
    {
        JFileChooser fileExporter = null;

        if (exportMode == ExportFormat.XML)
        {
            fileExporter = FileDialogs.getExportFileChooser(FileFormats.XML);
        }
        else if (exportMode == ExportFormat.DOT)
        {
            fileExporter = FileDialogs.getExportFileChooser(FileFormats.DOT);
        }
        else if (exportMode == ExportFormat.DSX)
        {
            fileExporter = FileDialogs.getExportFileChooser(FileFormats.DSX);
        }
        else if (exportMode == ExportFormat.FSM)
        {
            fileExporter = FileDialogs.getExportFileChooser(FileFormats.FSM);
        }
        else if (exportMode == ExportFormat.STS)
        {
            fileExporter = FileDialogs.getExportFileChooser(FileFormats.STS);
        }
//        else if (exportMode == ExportFormat.SP)
//        {
//            fileExporter = FileDialogs.getExportFileChooser(FileFormats.SP);
//        }
        else
        {
            return;
        }

        // ARASH: ain't it good to see what we're doin' ??
        fileExporter.setDialogTitle("Save " + currAutomaton.getName() + " as ...");

        if (fileExporter.showSaveDialog(gui.getComponent()) == JFileChooser.APPROVE_OPTION)
        {
            final File currFile = fileExporter.getSelectedFile();

            if (currFile != null)
            {
                if (!currFile.isDirectory())
                {
                    try
                    {
                        if (exportMode == ExportFormat.XML)
                        {
                            final Automata currAutomata = new Automata();
                            currAutomata.addAutomaton(currAutomaton);
                            final AutomataToXML exporter = new AutomataToXML(currAutomata);
                            exporter.serialize(currFile);
                        }
                        else if (exportMode == ExportFormat.DOT)
                        {
                            final AutomatonToDot exporter = new AutomatonToDot(currAutomaton);
                            exporter.serialize(currFile.getAbsolutePath());
                        }
                        else if (exportMode == ExportFormat.DSX)
                        {
                            final AutomatonToDsx exporter = new AutomatonToDsx(currAutomaton);
                            exporter.serialize(currFile.getAbsolutePath());
                        }
                        else if (exportMode == ExportFormat.FSM)
                        {
                            final AutomatonToFSM exporter = new AutomatonToFSM(currAutomaton);
                            exporter.serialize(currFile.getAbsolutePath());
                        }
                        else if (exportMode == ExportFormat.STS)
                        {
                            final Automata currAutomata = new Automata();
                            currAutomata.addAutomaton(currAutomaton);
                            final AutomataToSTS exporter = new AutomataToSTS(currAutomata);
                            exporter.serialize(currFile);
                        }
//                        else if (exportMode == ExportFormat.SP)
//                        {
//                            Project selectedProject = gui.getSelectedProject();
//                            Project newProject = new Project();
//                            newProject.addAttributes(selectedProject);
//                            //newProject.addActions(selectedProject.getActions());
//                            //newProject.addControls(selectedProject.getControls());
//                            //newProject.setAnimationURL(selectedProject.getAnimationURL());
//                            ProjectToSP exporter = new ProjectToSP(newProject);
//                            exporter.serialize(currFile);
//                        }

                        /*
                          else if (exportMode == ExportFormat.HTML)
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
                    catch (final Exception ex)
                    {
                        logger.error("Exception while exporting " + currFile.getAbsolutePath(), ex);
                        logger.debug(ex.getStackTrace());
                    }
                }
            }
        }
    }

    // ** Extend
    public static void automataExtend_actionPerformed(final Gui gui)
    {
        final Automata selectedAutomata = gui.getSelectedAutomata();

        if (!selectedAutomata.sanityCheck(gui.getComponent(), 1))
        {
            return;
        }

        final Iterator<?> autIt = selectedAutomata.iterator();

        while (autIt.hasNext())
        {
            final Automaton currAutomaton = (Automaton) autIt.next();
            final String newAutomatonName = gui.getNewAutomatonName("Please enter a new name", "");

            if (newAutomatonName == null)
            {
                return;
            }

            final int k = getIntegerInDialogWindow("Select k", gui.getComponent());
            //			int m = getIntegerInDialogWindow("Select m", gui.getComponent());
            final AutomataExtender extender = new AutomataExtender(currAutomaton);

            extender.setK(k);
            //			extender.setM(m);

            try
            {
                extender.execute();

                final Automaton newAutomaton = extender.getNewAutomaton();

                newAutomaton.setName(newAutomatonName);
                gui.getVisualProjectContainer().getActiveProject().addAutomaton(newAutomaton);
            }
            catch (final Exception ex)
            {
                logger.error("Exception in AutomataExtend. Automaton: " + currAutomaton.getName(), ex);
                logger.debug(ex.getStackTrace());
            }
        }
    }

    // ** Lifting according to the computer human theory
    public static void automataLifting_actionPerformed(final Gui gui)
    {
        final Automata selectedAutomata = gui.getSelectedAutomata();

        if (!selectedAutomata.sanityCheck(gui.getComponent(), 1))
        {
            return;
        }

        final int k = getIntegerInDialogWindow("Select k", gui.getComponent());
        final int m = getIntegerInDialogWindow("Select m", gui.getComponent());

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
        final ComputerHumanExtender extender = new ComputerHumanExtender(selectedAutomata, k, m);

        try
        {
            extender.execute();

            final Automaton newAutomaton = extender.getNewAutomaton();

            gui.getVisualProjectContainer().getActiveProject().addAutomaton(newAutomaton);
        }
        catch (final Exception ex)
        {
            logger.error("Error in ComputerHumanExtender.");
            logger.debug(ex.getStackTrace());
        }
    }

    /**
     * Purge - remove all forbidden states.
     */
    public static void automataPurge_actionPerformed(final Gui gui)
    {
        final Automata selectedAutomata = gui.getSelectedAutomata();

        if (!selectedAutomata.sanityCheck(gui.getComponent(), 1))
        {
            return;
        }

        final Iterator<?> autIt = selectedAutomata.iterator();

        while (autIt.hasNext())
        {
            final Automaton currAutomaton = (Automaton) autIt.next();
            final AutomatonPurge automatonPurge = new AutomatonPurge(currAutomaton);

            try
            {
                automatonPurge.execute();
            }
            catch (final Exception ex)
            {
                logger.error("Exception in AutomataPurge. Automaton: " + currAutomaton.getName(), ex);
                logger.debug(ex.getStackTrace());
            }
        }
    }

    // ** RemovePass - removes all pass events
    public static void automataRemovePass_actionPerformed(final Gui gui)
    {
        final Automata selectedAutomata = gui.getSelectedAutomata();

        if (!selectedAutomata.sanityCheck(gui.getComponent(), 1))
        {
            return;
        }

        final Iterator<?> autIt = selectedAutomata.iterator();

        while (autIt.hasNext())
        {
            final Automaton currAutomaton = (Automaton) autIt.next();

            try
            {
                RemovePassEvent.execute(currAutomaton);
            }
            catch (final Exception ex)
            {
                logger.error("Exception in AutomataRemovePass. Automaton: " + currAutomaton.getName(), ex);
                logger.debug(ex.getStackTrace());
            }
        }
    }

    // ** RemoveSelfLoopArcs
    public static void automataRemoveSelfLoopArcs_actionPerformed(final Gui gui)
    {
        final Automata selectedAutomata = gui.getSelectedAutomata();

        if (!selectedAutomata.sanityCheck(gui.getComponent(), 1))
        {
            return;
        }

        final Iterator<?> autIt = selectedAutomata.iterator();

        while (autIt.hasNext())
        {
            final Automaton currAutomaton = (Automaton) autIt.next();

            try
            {
                RemoveSelfArcs.execute(currAutomaton);
            }
            catch (final Exception ex)
            {
                logger.error("Exception in RemoveSelfArcs. Automaton: " + currAutomaton.getName(), ex);
                logger.debug(ex.getStackTrace());
            }
        }
    }

    // ** Rename
    public static void automataRename_actionPerformed(final Gui gui)
    {
        final Automata selectedAutomata = gui.getSelectedAutomata();

        if (!selectedAutomata.sanityCheck(gui.getComponent(), 1))
        {
            return;
        }

        final Iterator<?> autIt = selectedAutomata.iterator();

        while (autIt.hasNext())
        {
            final Automaton currAutomaton = (Automaton) autIt.next();
            final String currAutomatonName = currAutomaton.getName();

            try
            {
                final String newName = gui.getNewAutomatonName("Enter a new name for " + currAutomatonName, currAutomatonName);

                if (newName != null)
                {
                    gui.getVisualProjectContainer().getActiveProject().renameAutomaton(currAutomaton, newName);
                }
            }
            catch (final Exception ex)
            {
                logger.error("Exception while renaming the automaton " + currAutomatonName, ex);
                logger.debug(ex.getStackTrace());
            }
        }
    }

    // ** Synchronize - Threaded version
    public static void automataSynchronize_actionPerformed(final Gui gui)
    {
        // Retrieve the selected automata and make a sanity check
        final Automata selectedAutomata = gui.getSelectedAutomata();
        if (!selectedAutomata.sanityCheck(gui.getComponent(), 2, true, false, true, true))
        {
            return;
        }

        // Get the current options
        SynchronizationOptions synchronizationOptions;

        try
        {
            synchronizationOptions = new SynchronizationOptions();
        }
        catch (final Exception ex)
        {
            JOptionPane.showMessageDialog(gui.getComponent(), "Error constructing synchronizationOptions: " + ex.getMessage(), "Alert", JOptionPane.ERROR_MESSAGE);
            logger.debug(ex.getStackTrace());

            return;
        }

        // Start a dialog to allow the user changing the options
        final SynchronizationDialog synchronizationDialog = new SynchronizationDialog(gui.getFrame(), synchronizationOptions);

        synchronizationDialog.show();

        if (!synchronizationOptions.getDialogOK())
        {
            return;
        }

        // Start worker thread - perform the task.
        //AutomataSynchronizerWorker worker = new AutomataSynchronizerWorker(gui, selectedAutomata, "" /* newAutomatonName */, synchronizationOptions);
        System.err.println("Migrate to IDE, old Supremica no longer supported.");
    }

    // ** Synthesize
    public static void automataSynthesize_actionPerformed(final Gui gui)
    {
        // Retrieve the selected automata and make a sanity check
        final Automata selectedAutomata = gui.getSelectedAutomata();

        if (!selectedAutomata.sanityCheck(gui.getComponent(), 1, true, true, true, true))
        {
            return;
        }

        // Get the current options and allow the user to change them...
        final SynthesizerOptions options = new SynthesizerOptions();
        final SynthesizerDialog synthesizerDialog = new SynthesizerDialog(gui.getFrame(), selectedAutomata.size(), options);
        synthesizerDialog.setVisible(true);
        if (!options.getDialogOK())
        {
            return;
        }

        // AutomataSynthesisWorker worker = new AutomataSynthesisWorker(gui, selectedAutomata, options);
        System.err.println("Migrate to IDE, old Supremica no longer supported.");

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
    public static void automataVerify_actionPerformed(final Gui gui)
    {
        // Retrieve the selected automata and make a sanity check
        final Automata selectedAutomata = gui.getSelectedAutomata();
        if (!selectedAutomata.sanityCheck(gui.getComponent(), 1, true, false, true, true))
        {
            return;
        }

        // Get the current options and allow the user to change them...
        final VerificationOptions vOptions = new VerificationOptions();
        final MinimizationOptions mOptions = MinimizationOptions.getDefaultVerificationOptions();
        final VerificationDialog verificationDialog = new VerificationDialog(gui.getFrame(), vOptions, mOptions);
        verificationDialog.show();
        if (!vOptions.getDialogOK())
        {
            return;
        }
        if (vOptions.getVerificationType() == VerificationType.LANGUAGEINCLUSION)
        {
            vOptions.setInclusionAutomata(gui.getUnselectedAutomata());
        }
        final SynchronizationOptions sOptions = SynchronizationOptions.getDefaultVerificationOptions();
        final JFrame owner = gui.getFrame();

        // Work!
        new AutomataVerificationWorker(owner, selectedAutomata,
                                       vOptions, sOptions, mOptions);
    }

    // Automaton.ActionAndControlViewer action performed
    public static void actionAndControlViewer_actionPerformed(final Gui gui)
    {
        try
        {
            gui.getVisualProjectContainer().getActiveProject().getActionAndControlViewer();
        }
        catch (final Exception ex)
        {
            logger.error("Exception in ActionAndControlViewer.", ex);
            logger.debug(ex.getStackTrace());

            return;
        }
    }

    // Automaton.ActionAndControlViewer action performed
    public static void animator_actionPerformed(final Gui gui)
    {
        try
        {
            final VisualProject currProject = gui.getVisualProjectContainer().getActiveProject();

            if (!currProject.hasAnimation())
            {
                logger.info("No animation present");

                return;
            }

            currProject.getAnimator();
        }
        catch (final Exception ex)
        {
            logger.error("Exception while getting Animator.", ex);
            logger.debug(ex.getStackTrace());
        }
    }

    // ActionMan.userInterface_action performed
    public static void userInterface_actionPerformed(final Gui gui)
    {
        try
        {
            final VisualProject currProject = gui.getVisualProjectContainer().getActiveProject();

            if (!currProject.hasUserInterface())
            {
                logger.info("No user interface present");

                return;
            }

            final Container userInterface = currProject.getUserInterface();
            userInterface.setVisible(true);
        }
        catch (final Exception ex)
        {
            logger.error("Exception while getting user interface.", ex);
            logger.debug(ex.getStackTrace());
        }
    }

    // ActionMan.userInterface_action performed
    public static void generateUserInterfaceAutomata_actionPerformed(final Gui gui)
    {
        try
        {
            final VisualProject currProject = gui.getVisualProjectContainer().getActiveProject();

            if (!currProject.hasUserInterface())
            {
                logger.info("No user interface present");

                return;
            }

            final SwingEngine swingEngine = currProject.getSwingEngine();
            final ProjectBuildFromSwingEngine projectBuilder = new ProjectBuildFromSwingEngine();

            try
            {
                final Project newProject = projectBuilder.build(swingEngine);
                final int nbrOfAddedAutomata = gui.addAutomata(newProject);
                logger.info("Successfully created " + nbrOfAddedAutomata + " user interface automata.");
            }
            catch (final Exception ex)
            {
                logger.error("Error while creating user interface automata", ex);
                logger.debug(ex.getStackTrace());

                return;
            }

        }
        catch (final Exception ex)
        {
            logger.error("Exception while getting user interface.", ex);
            logger.debug(ex.getStackTrace());
        }
    }

    // Automaton.Explore action performed
    public static void automatonExplore_actionPerformed(final Gui gui)
    {
        // Retrieve the selected automata and make a sanity check
        final Automata selectedAutomata = gui.getSelectedAutomata();

        // Sanitycheck
        if (!selectedAutomata.sanityCheck(gui.getComponent(), 1, true, false, false, true))
        {
            return;
        }

        // How many selected?
        if (selectedAutomata.size() == 1)
        {
            // One automaton selected

            // Get automaton
            final Automaton theAutomaton = selectedAutomata.getFirstAutomaton();
            final String currAutomatonName = theAutomaton.getName();

            // Get AutomatonExplorer
            try
            {
                gui.getVisualProjectContainer().getActiveProject().getAutomatonExplorer(currAutomatonName);
            }
            catch (final Exception ex)
            {
                logger.error("Exception in AutomatonExplorer. Automaton: " + theAutomaton.getName(), ex);
                logger.debug(ex.getStackTrace());
            }
        }
        else
        {
            // Many automata selected

            // The AutomataExplorer can not take care of nondeterministic processes...
            if (!selectedAutomata.isDeterministic())
            {
                logger.error("The current project is nondeterministic. " +
                    "Exploration of nondeterministic automata " +
                    "is currently not supported.");
            }

            // Get AutomataExplorer
            try
            {
                JOptionPane.showMessageDialog(gui.getComponent(), "The automata explorer only works in the \"forward\" direction!", "Alert", JOptionPane.INFORMATION_MESSAGE);

                final AutomataExplorer explorer = new AutomataExplorer(selectedAutomata);

                explorer.setVisible(true);
                explorer.initialize();
            }
            catch (final Exception ex)
            {
                logger.error("Exception in AutomataExplorer.", ex);
                logger.debug(ex.getStackTrace());
            }
        }
    }

    // Project.Simulator action performed
    public static void simulator_actionPerformed(final Gui gui)
    {
        // We can not simulate nondeterministic processes properly just yet...
        if (!gui.getVisualProjectContainer().getActiveProject().isDeterministic())
        {
            logger.error("The current project is nondeterministic. Simulation of nondeterminism is currently not supported.");
        }

        try
        {
            final VisualProject currProject = gui.getVisualProjectContainer().getActiveProject();

            if (!currProject.hasAnimation())
            {
                logger.info("No simulation present");

                return;
            }

            final SimulatorExecuter simulator = currProject.getSimulator();

            if (simulator != null)
            {
                simulator.setVisible(true);
                simulator.initialize();
            }
        }
        catch (final Exception ex)
        {
            logger.error("Exception in Simulator", ex);
            logger.debug(ex.getStackTrace());
        }
    }

    // Project.SimulatorClear action performed
    public static void simulatorClear_actionPerformed(final Gui gui)
    {
        try
        {
            final VisualProject currProject = gui.getVisualProjectContainer().getActiveProject();

            currProject.clearSimulationData();
        }
        catch (final Exception ex)
        {
            logger.error("Exception in Simulator");
        }
    }

    // Automaton.Minimization action performed
    public static void automatonMinimize_actionPerformed(final Gui gui)
    {
        // Retrieve the selected automata and make a sanity check
        final Automata selectedAutomata = gui.getSelectedAutomata();
        if (!selectedAutomata.sanityCheck(gui.getComponent(), 1))
        {
            return;
        }

        // Get the current options and allow the user to change them...
        final MinimizationOptions options = new MinimizationOptions();
        final MinimizationDialog dialog = new MinimizationDialog(gui.getFrame(), options, selectedAutomata);
        dialog.show();
        if (!options.getDialogOK())
        {
            return;
        }

		final Project currProject = gui.getVisualProjectContainer().getActiveProject();
        final AutomataMinimizationWorker amw = new AutomataMinimizationWorker(gui.getFrame(), selectedAutomata, currProject, options);
		amw.start();
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
    public static void automatonStatus_actionPerformed(final Gui gui)
    {
        final int nbrOfAutomata = gui.getVisualProjectContainer().getActiveProject().nbrOfAutomata();
        //gui.info("Number of automata: " + nbrOfAutomata);

        final Automata selectedAutomata = gui.getSelectedAutomata();
        if (!selectedAutomata.sanityCheck(gui.getComponent(), 1, false, false, true, true))
        {
            return;
        }
        logger.info("Number of selected automata: " + selectedAutomata.size() + " (" + nbrOfAutomata + ")");
        logger.info("Size of union alphabet: " + selectedAutomata.getUnionAlphabet().size());

        for (final Iterator<?> autIt = selectedAutomata.iterator(); autIt.hasNext(); )
        {
            final Automaton currAutomaton = (Automaton) autIt.next();
            final StringBuilder statusStr = new StringBuilder();

            statusStr.append("Status for automaton: " + currAutomaton.getName());

            statusStr.append("\n\tnumber of states: " + currAutomaton.nbrOfStates());
            statusStr.append("\n\tnumber of events: " + currAutomaton.nbrOfEvents());
            statusStr.append("\n\tnumber of transitions: " + currAutomaton.nbrOfTransitions());
            statusStr.append("\n\tnumber of accepting states: " + currAutomaton.nbrOfAcceptingStates());
            //statusStr.append("\n\tNumber of mutually accepting states: " + currAutomaton.nbrOfMutuallyAcceptingStates());
            statusStr.append("\n\tnumber of forbidden states: " + currAutomaton.nbrOfForbiddenStates());

            final int acceptingAndForbiddenStates = currAutomaton.nbrOfAcceptingAndForbiddenStates();
            if (acceptingAndForbiddenStates > 0)
            {
                statusStr.append("\n\tnumber of accepting AND forbidden states: " + acceptingAndForbiddenStates);
            }

            if (currAutomaton.isDeterministic())
            {
                final Alphabet redundantEvents = currAutomaton.getRedundantEvents();
                if (redundantEvents.nbrOfEvents() > 0)
                    statusStr.append("\n\talphabet of redundant events: " + redundantEvents);
                statusStr.append("\n\tthe automaton is deterministic");
            }

            if ((currAutomaton.getComment() != null) && !currAutomaton.getComment().equals(""))
            {
                statusStr.append("\n\tcomment: \"" + currAutomaton.getComment() + "\"");
            }

            // logger.info(statusStr.toString());
            logger.info(statusStr.toString());
        }

        if (selectedAutomata.size() > 1)
        {
            double potentialNumberOfStates = 1.0;

            for (final Iterator<?> autIt = selectedAutomata.iterator();
            autIt.hasNext(); )
            {
                final Automaton currAutomaton = (Automaton) autIt.next();

                potentialNumberOfStates = potentialNumberOfStates * currAutomaton.nbrOfStates();
            }

            logger.info("Number of potential states: " + new Double(potentialNumberOfStates).longValue());
        }
    }

    // View hierarchy action performed
    public static void hierarchyView_actionPerformed(final Gui gui)
    {
        final Automata selectedAutomata = gui.getSelectedAutomata();

        // Sanity check
        if (!selectedAutomata.sanityCheck(gui.getComponent(), 2, false, false, true, false))
        {
            return;
        }

        // Warn if there are too many "states" i.e. automata
        final int maxNbrOfStates = Config.DOT_MAX_NBR_OF_STATES.getValue();
        if (maxNbrOfStates < selectedAutomata.size())
        {
            String msg = "You have selected " + selectedAutomata.size() + " automata. It is not " +
                "recommended to display the modular structure for more than " + maxNbrOfStates +
                " automata.";
            msg = EncodingHelper.linebreakAdjust(msg);

            final Object[] options = { "Continue", "Abort" };
            final int response = JOptionPane.showOptionDialog(ActionMan.gui.getFrame(), msg, "Warning",
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
            final AutomataHierarchyViewer viewer = new AutomataHierarchyViewer(selectedAutomata);

            viewer.setVisible(true);

            //viewer.setState(Frame.NORMAL);
        }
        catch (final Exception ex)
        {
            logger.error("Exception in AutomataHierarchyViewer.", ex);
            logger.debug(ex.getStackTrace());

            return;
        }
    }

    // View the automatas individual states in a tree structure
    public static void statesView_actionPerformed(final Gui gui)
    {
        final Automata selectedAutomata = gui.getSelectedAutomata();

        if (!selectedAutomata.sanityCheck(gui.getComponent(), 1, false, false, true, false))
        {
            return;
        }

        try
        {
            final AutomataViewer statesViewer = new AutomataViewer(selectedAutomata, false, true);

            statesViewer.setVisible(true);
        }
        catch (final Exception ex)
        {
            // logger.error("Exception in AlphabetViewer", ex);
            logger.error("Exception in AutomataViewer: " + ex);
            logger.debug(ex.getStackTrace());

            return;
        }
    }

    // Automaton.Alphabet action performed
    // public static void automatonAlphabet_actionPerformed(Gui gui)
    public static void alphabetView_actionPerformed(final Gui gui)
    {
        //logger.debug("ActionMan::automatonAlphabet_actionPerformed(gui)");
        final Automata selectedAutomata = gui.getSelectedAutomata();

        if (!selectedAutomata.sanityCheck(gui.getComponent(), 1, false, false, true, false))
        {
            return;
        }

        // Why not simply instantiate an AlphabetViewer with the given
        // automata object?? Use AutomataViewer instead!
        try
        {
            // AlphabetViewer alphabetviewer = new AlphabetViewer(selectedAutomata);
            final AutomataViewer alphabetViewer = new AutomataViewer(selectedAutomata, true, false);
            alphabetViewer.setVisible(true);
        }
        catch (final Exception ex)
        {
            // logger.error("Exception in AlphabetViewer", ex);
            logger.error("Exception in AutomataViewer: " + ex);
            logger.debug(ex.getStackTrace());

            return;
        }
    }

    // Automaton.View action performed
    public static void automatonView_actionPerformed(final Gui gui)
    {
        // gui.debug("ActionMan to the rescue!");
        // Retrieve the selected automata and make a sanity check
        final Automata selectedAutomata = gui.getSelectedAutomata();

        if (!selectedAutomata.sanityCheck(gui.getComponent(), 1, true, false, false, false))
        {
            return;
        }

        final Iterator<?> autIt = selectedAutomata.iterator();
        while (autIt.hasNext())
        {
            final Automaton currAutomaton = (Automaton) autIt.next();

            try
            {
                gui.getVisualProjectContainer().getActiveProject().getAutomatonViewer(currAutomaton.getName());
            }
            catch (final Exception ex)
            {
                logger.error("Exception in AutomatonViewer. Automaton: " + currAutomaton, ex);
                logger.debug(ex.getStackTrace());

                return;
            }
        }
    }

     /*
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
      */

    // Variable declared here, wanted it to be local to this func, but...
//    static PropertiesDialog thePropertiesDialog = null;

    public static void configurePreferences_actionPerformed(final Gui gui)
    {
      System.err.println("Attempted to open old properties!");
//        if (thePropertiesDialog == null)
//        {
//            thePropertiesDialog = new PropertiesDialog(gui.getFrame());
//        }
//
//        thePropertiesDialog.setVisible(true);
    }

    // File.Exit action performed
    public static void fileExit(final Gui gui)
    {
        fileClose(gui);
    }

    // File.Close action performed
    public static void fileClose(final Gui gui)
    {
        OptionFileManager.savePropertiesOnExit();
        gui.close();
    }

    public static void fileExportDesco(final Gui gui)
    {
        automataExport(gui, ExportFormat.DSX);
    }

    public static void fileExportDot(final Gui gui)
    {
        automataExport(gui, ExportFormat.DOT);
    }

    public static void fileExportSupremica(final Gui gui)
    {
        automataExport(gui, ExportFormat.XML);
    }

    public static void fileExportHtml(final Gui gui)
    {
        try
        {
            final File dir = new File("C:\\Temp\\");
            final Project selectedProject = gui.getSelectedProject();
            final ProjectToHtml exporter = new ProjectToHtml(selectedProject, dir);

            exporter.serialize();
        }
        catch (final Exception ex)
        {
            logger.error("fileExportHtml: Exception - ", ex);
            logger.debug(ex.getStackTrace());
        }
    }

    // -------------- TODO: ADD EXPORTES FOR THESE TOO ------------------------------------
    public static void fileExportUMDES(final Gui gui)
    {
        automataExport(gui);
    }

    public static void fileExportValid(final Gui gui)
    {
        automataExport(gui);
    }

    public static void fileImportWaters(final Gui gui)
    {
        new FileImporter(FileDialogs.getWatersFileImporter(), gui)    // anonymous class
        {
            @Override
            void openFile(final Gui g, final File f)
            {
                importWatersFile(g, f);
            }
        };
    }

    public static void fileImportHYB(final Gui gui)
    {
        new FileImporter(FileDialogs.getHYBFileImporter(), gui)    // anonymous class
        {
            @Override
            void openFile(final Gui g, final File f)
            {
                importHYBFile(g, f);
            }
        };
    }

    public static void fileImportHISC(final Gui gui)
    {
        new FileImporter(FileDialogs.getHISCFileImporter(), gui)    // anonymous class
        {
            @Override
            void openFile(final Gui g, final File f)
            {
                importHISCFile(g, f);
            }
        };
    }

    public static void fileImportUMDES(final Gui gui)
    {
        new FileImporter(FileDialogs.getImportFileChooser(FileFormats.FSM), gui)    // anonymous class
        {
            @Override
            void openFile(final Gui g, final File f)
            {
                importUMDESFile(g, f);
            }
        };
    }

    /*
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
     */

    // Aldebaran format, a simple format for specifying des
    public static void fileImportAut(final Gui gui)
    {
        new FileImporter(FileDialogs.getAutFileImporter(), gui)    // anonymous class
        {
            @Override
            void openFile(final Gui g, final File f)
            {
                importAutFile(g, f);
            }
        };
    }

    // File.Open action performed
    public static void fileOpen(final Gui gui)
    {
        new FileImporter(FileDialogs.getXMLFileImporter(), gui)    // anonymous class
        {
            @Override
            void openFile(final Gui g, final File f)
            {
                openProjectXMLFile(g, f);
                Config.FILE_OPEN_PATH.set(f.getParentFile().getAbsolutePath());
            }
        };
    }

    // Why this indirection?
    public static void openFile(final Gui gui, final File file)
    {
        openProjectXMLFile(gui, file);
    }

    public static void openProjectXMLFile(final Gui gui, final File file)
    {
        Project currProject = null;

        logger.info("Opening " + file.getAbsolutePath() + " ...");

        try
        {
            final ProjectBuildFromXML builder = new ProjectBuildFromXML(new VisualProjectFactory());

            currProject = builder.build(file);
        }
        catch (final Exception ex)
        {
            // this exception is caught while opening
            logger.error("Error while opening " + file.getAbsolutePath() + " .", ex);
            logger.debug(ex.getStackTrace());
            return;
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

            logger.warn("Nondeterministic automaton loaded. Some algorithms are not guaranteed to work.");
        }

        final int nbrOfAutomataBeforeOpening = gui.getVisualProjectContainer().getActiveProject().nbrOfAutomata();

        try
        {
            final int nbrOfAddedAutomata = gui.addProject(currProject);
            logger.info("Successfully opened and added " + nbrOfAddedAutomata + " automata.");
        }
        catch (final Exception excp)
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
            final File projectFile = gui.getVisualProjectContainer().getActiveProject().getProjectFile();

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
    public static void fileSave(final Gui gui)
    {
        // Get the file corresponding to the current project
        final File currFile = gui.getVisualProjectContainer().getActiveProject().getProjectFile();
        // Was there no open project? Use fileSaveAs!
        if (currFile == null)
        {
            fileSaveAs(gui);

            return;
        }

        // Get the current project
        final Project currProject = gui.getVisualProjectContainer().getActiveProject();

        // Is this project empty? If so, maybe we shouldn't save it?
        if (currProject.size() == 0)
        {
            //if (JOptionPane.NO_OPTION == JOptionPane.showConfirmDialog(gui.getComponent(), "This project is empty. Do you really want to save?", "Do you really want to save an empty project?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE))
            //if (JOptionPane.NO_OPTION == JOptionPane.showConfirmDialog(gui.getComponent(), "This project is empty. Do you really want to save?", "Do you really want to save an empty project?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, new Object[]={"Yes", "No"}, "No"))
            final Object[] objects = {"Yes", "No"};
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

                    final AutomataToXML exporter = new AutomataToXML(currProject);

                    exporter.serialize(currFile.getAbsolutePath());

                }
                catch (final Exception ex)
                {
                    logger.error("Exception while Save As " + currFile.getAbsolutePath(), ex);
                    logger.debug(ex.getStackTrace());
                }
            }
        }
    }

    // File.SaveAs action performed
    public static void fileSaveAs(final Gui gui)
    {

        final JFileChooser fileSaveAs = FileDialogs.getXMLFileSaveAs();
        final String projectName = gui.getVisualProjectContainer().getActiveProject().getName();

        if (projectName != null)
        {
            final File currDirectory = fileSaveAs.getCurrentDirectory();

            fileSaveAs.setSelectedFile(new File(currDirectory, projectName + ".xml"));
        }

        if (fileSaveAs.showSaveDialog(gui.getFrame()) == JFileChooser.APPROVE_OPTION)
        {
            final File currFile = fileSaveAs.getSelectedFile();

            if (currFile != null)
            {
                gui.getVisualProjectContainer().getActiveProject().setProjectFile(currFile);
                fileSave(gui);
                Config.FILE_SAVE_PATH.set(currFile.getParentFile().getAbsolutePath());
            }
        }
    }

    public static void importAutFile(final Gui gui, final File file)
    {
        logger.info("Importing " + file.getAbsolutePath() + " ...");
        try
        {
            final Automata currAutomata = null;    // AutomataBuildFromAut.build(file);
            final int nbrOfAddedAutomata = gui.addAutomata(currAutomata);
            logger.info("Successfully imported " + nbrOfAddedAutomata + " automata.");
        }
        catch (final Exception ex)
        {
            logger.error("Error while importing " + file.getAbsolutePath(), ex);
            logger.debug(ex.getStackTrace());

            return;
        }
    }

    public static void importWatersFile(final Gui gui, final File file)
    {
        logger.info("Importing " + file.getAbsolutePath() + " ...");

        try
        {
            // Build Waters ModuleProxy
            final ModuleProxyFactory factory =
	      ModuleElementFactory.getInstance();
            final OperatorTable optable = CompilerOperatorTable.getInstance();
            final ProxyUnmarshaller<ModuleProxy> unMarshaller =
              new SAXModuleMarshaller(factory, optable);
	    final DocumentManager manager = new DocumentManager();
	    manager.registerUnmarshaller(unMarshaller);
            final ModuleProxy module = (ModuleProxy) manager.load(file);
            final ProjectBuildFromWaters builder =
	      new ProjectBuildFromWaters(manager, new VisualProjectFactory());
            final Automata currAutomata = builder.build(module);
            final int nbrOfAddedAutomata = gui.addAutomata(currAutomata);

            logger.info("Successfully imported " + nbrOfAddedAutomata + " automata.");
        }
        catch (final Exception ex)
        {
            logger.error("Error while importing " + file.getAbsolutePath() + ". " + ex);
            logger.debug(ex.getStackTrace());

            return;
        }
    }

    @SuppressWarnings("deprecation")
	public static void importHYBFile(final Gui gui, final File file)
    {
        logger.info("Importing " + file.getAbsolutePath() + " ...");

        try
        {
            final ProjectBuildFromHYB builder = new ProjectBuildFromHYB(new VisualProjectFactory());
            final Automata currAutomata = builder.build(file.toURL());
            final int nbrOfAddedAutomata = gui.addAutomata(currAutomata);

            logger.info("Successfully imported " + nbrOfAddedAutomata + " automata.");
        }
        catch (final Exception ex)
        {
            logger.error("Error while importing " + file.getAbsolutePath() + ". ", ex);
            logger.debug(ex.getStackTrace());

            return;
        }
    }

    @SuppressWarnings("deprecation")
	public static void importHISCFile(final Gui gui, final File file)
    {
        logger.info("Importing " + file.getAbsolutePath() + " ...");

        try
        {
            final ProjectBuildFromHISC builder = new ProjectBuildFromHISC(new VisualProjectFactory());
            final Automata currAutomata = builder.build(file.toURL());
            final int nbrOfAddedAutomata = gui.addAutomata(currAutomata);

            logger.info("Successfully imported " + nbrOfAddedAutomata + " automata.");
        }
        catch (final Exception ex)
        {
            logger.error("Error while importing " + file.getAbsolutePath() + ". ", ex);
            logger.debug(ex.getStackTrace());

            return;
        }
    }

    @SuppressWarnings("deprecation")
	public static void importUMDESFile(final Gui gui, final File file)
    {
        logger.info("Importing " + file.getAbsolutePath() + " ...");

        try
        {
            final ProjectBuildFromFSM builder = new ProjectBuildFromFSM(new VisualProjectFactory());
            final Automata currAutomata = builder.build(file.toURL());
            final int nbrOfAddedAutomata = gui.addAutomata(currAutomata);

            logger.info("Successfully imported " + nbrOfAddedAutomata + " automata.");
        }
        catch (final Exception ex)
        {
            logger.error("Error while importing " + file.getAbsolutePath() + ". ", ex);
            logger.debug(ex.getStackTrace());

            return;
        }
    }

    /*
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
     */

    // Automata.AlphabetNormalize action performed
    public static void normalizeAlphabet_actionPerformed(final Gui gui)
    {
        final Automata selectedAutomata = gui.getSelectedAutomata();
        if (!selectedAutomata.sanityCheck(gui.getComponent(), 1, false, false, true, false))
        {
            return;
        }

        final Iterator<?> autIt = selectedAutomata.iterator();
        while (autIt.hasNext())
        {
            final Automaton currAutomaton = (Automaton) autIt.next();

            try
            {
                final AlphabetNormalize alphabetNormalize = new AlphabetNormalize(currAutomaton);

                alphabetNormalize.execute();
            }
            catch (final Exception ex)
            {
                logger.error("Exception in AlphabetNormalizer. Automaton: " + currAutomaton.getName(), ex);
                logger.debug(ex.getStackTrace());
            }
        }
    }

    // selectAll action performed
    public static void selectAll_actionPerformed(final Gui gui)
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
    public static void automataDeleteAll_actionPerformed(final Gui gui)
    {
        gui.getVisualProjectContainer().getActiveProject().clear();
        gui.clearSelection();
    }

    // Crop to selection - delete all unselected automata
    public static void automataCrop_actionPerformed(final Gui gui)
    {

        //Collection selectedAutomata = gui.getSelectedAutomataAsCollection();
        final Automata selectedAutomata = gui.getSelectedAutomata();

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
            catch (final Exception ex)
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
                catch (final Exception ex)
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
    public static void automataInvert_actionPerformed(final Gui gui)
    {
        gui.invertSelection();
    }

    /**
     * Calculates table with information for use with an (external) genetic programming system.
     * This is a part of a project in a course in Evolutionary Computation, FFR105 (2002) at
     * Chalmers University of Technology.
     *
     * Writes 16 columns of data and a correct value on each line of an output file
     */
    public static void evoCompSynchTable(final boolean append)
    {
        final Automata selectedAutomata = gui.getSelectedAutomata();

        if (!selectedAutomata.sanityCheck(gui.getComponent(), 1))
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

            final int dataAmount = 1000;

            for (int i = 0; i < dataAmount; i++)
            {

                // Find two random automata
                automatonA = selectedAutomata.getAutomatonAt((int) (Math.random() * selectedAutomata.size()));
                automatonB = selectedAutomata.getAutomatonAt((int) (Math.random() * selectedAutomata.size()));

                //System.out.println(automatonA.getName() + " " + automatonB.getName());
                data = GeneticAlgorithms.extractData(automatonA, automatonB);

                final Automata theTwoAutomata = new Automata();

                theTwoAutomata.addAutomaton(automatonA);
                theTwoAutomata.addAutomaton(automatonB);

                final double correctValue = GeneticAlgorithms.calculateSynchronizationSize(theTwoAutomata, syncOptions);

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
        catch (final Exception ex)
        {
            JOptionPane.showMessageDialog(gui.getComponent(), "Error in ActionMan.evoCompSynchTable(): " + ex.getMessage(), "Alert", JOptionPane.ERROR_MESSAGE);

            return;
        }
    }

    public static void evoCompPredictSize()
    {
        final Automata selectedAutomata = gui.getSelectedAutomata();

        if (!selectedAutomata.sanityCheck(gui.getComponent(), 1))
        {
            return;
        }
        else if (selectedAutomata.size() != 2)
        {
            JOptionPane.showMessageDialog(gui.getComponent(), "Exactly two automata must be selected.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        final double predictedSize = GeneticAlgorithms.predictSynchronizationSize(selectedAutomata.getAutomatonAt(0), selectedAutomata.getAutomatonAt(1));

        if (predictedSize > 0.0)
        {
            final double[] data = GeneticAlgorithms.extractData(selectedAutomata.getAutomatonAt(0), selectedAutomata.getAutomatonAt(1));

            final int realSize = GeneticAlgorithms.calculateSynchronizationSize(selectedAutomata);
            final int worstSize = (int) (data[0] * data[1]);

            JOptionPane.showMessageDialog(gui.getComponent(), "The synchronization is predicted to have " + (float) predictedSize + " states. \nSynchronization actually " + "gives exactly " + realSize + " states (worst case " + worstSize + ").", "Prediction", JOptionPane.INFORMATION_MESSAGE);
        }
        else
        {
            JOptionPane.showMessageDialog(gui.getComponent(), "The prediction failed. (Predicted size: " + predictedSize + ")", "Prediction", JOptionPane.ERROR_MESSAGE);
        }
    }

    /*
    // Hugo's test for controlling a train simulator from Supremica.
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

    /*
      static CellExaminer theCellExaminer = null;
      public static void showCellExaminer(Gui gui)
      {
      Thread thread = new Thread(new Runnable()
      {
      public void run()
      {
      if (theCellExaminer == null)
      {
      //theCellExaminer = new CellExaminer(ActionMan.getGui().getFrame());
      theCellExaminer = new CellExaminer(null);
      }

      theCellExaminer.setVisible(true);
      }
      });

      thread.start();
      }
     */

    /**
     * TestCases... - open the test cases dialog, and add the result to
     * the current set of automata public static void testCases(Gui gui)
     */
    public static void testCases(final Gui gui)
    throws Exception
    {
        final TestCasesDialog testCasesDialog = new TestCasesDialog(gui.getFrame(), gui);

        testCasesDialog.setVisible(true);

        //Project project = testCasesDialog.getProject();

                /*
                  if (project != null)
                  {
                  gui.addProject(project);
                  }
                 */
    }

    /**
     * Animations
     */
    public static void animator(final Gui gui, final AnimationItem item)
    {
        try
        {
            final Animator animator = item.createInstance();

            animator.setVisible(true);
        }
        catch (final Exception ex)
        {
            logger.error("Exception in animator.", ex);
            logger.debug(ex.getStackTrace());
        }
    }

    /**
     * Generate SattLine SFCs
     */
    public static void AutomataToSattLineSFC(final Gui gui)
    {
        final Project selectedProject = gui.getSelectedProject();

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

        final JFileChooser fileExporter = FileDialogs.getSFileExporter();

        if (fileExporter.showSaveDialog(gui.getComponent()) == JFileChooser.APPROVE_OPTION)
        {
            final File currFile = fileExporter.getSelectedFile();

            if (currFile != null)
            {
                if (!currFile.isDirectory())
                {
                    String prefixName = null;
                    final String pathName = currFile.getAbsolutePath();
                    final String filename = currFile.getName();

                    if (pathName.endsWith(".s"))
                    {
                        prefixName = pathName.substring(0, pathName.length() - 2);
                    }
                    else
                    {
                        prefixName = pathName;
                    }

                    final File sFile = new File(prefixName + ".s");
                    final File gFile = new File(prefixName + ".g");
                    final File lFile = new File(prefixName + ".l");
                    final File pFile = new File(prefixName + ".p");

                    try
                    {
                        final AutomataToSattLineSFC exporter = new AutomataToSattLineSFC(selectedProject);

                        exporter.serialize_s(sFile, filename);
                        exporter.serialize_g(gFile, filename);
                        exporter.serialize_l(lFile, filename);
                        exporter.serialize_p(pFile, filename);
                    }
                    catch (final Exception ex)
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

    /**
     * Generate SattLine SFCs for the Ball Process
     */
    public static void AutomataToSattLineSFCForBallProcess(final Gui gui)
    {
        final Project selectedProject = gui.getSelectedProject();

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

        final JFileChooser fileExporter = FileDialogs.getSFileExporter();

        if (fileExporter.showSaveDialog(gui.getComponent()) == JFileChooser.APPROVE_OPTION)
        {
            final File currFile = fileExporter.getSelectedFile();

            if (currFile != null)
            {
                if (!currFile.isDirectory())
                {
                    String prefixName = null;
                    final String pathName = currFile.getAbsolutePath();
                    final String filename = currFile.getName();

                    if (pathName.endsWith(".s"))
                    {
                        prefixName = pathName.substring(0, pathName.length() - 2);
                    }
                    else
                    {
                        prefixName = pathName;
                    }

                    final File sFile = new File(prefixName + ".s");
                    final File gFile = new File(prefixName + ".g");
                    final File lFile = new File(prefixName + ".l");
                    final File pFile = new File(prefixName + ".p");

                    try
                    {
                        final AutomataToSattLineSFCForBallProcess exporter = new AutomataToSattLineSFCForBallProcess(selectedProject);

                        exporter.serialize_s(sFile, filename);
                        exporter.serialize_g(gFile, filename);
                        exporter.serialize_l(lFile, filename);
                        exporter.serialize_p(pFile, filename);
                    }
                    catch (final Exception ex)
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

    /**
     * Generate ABB Control Builder SFCs
     */
    public static void AutomataToControlBuilderSFC(final Gui gui)
    {
        final Project selectedProject = gui.getSelectedProject();

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

        final JFileChooser fileExporter = FileDialogs.getPRJFileExporter();

        if (fileExporter.showSaveDialog(gui.getComponent()) == JFileChooser.APPROVE_OPTION)
        {
            final File currFile = fileExporter.getSelectedFile();

            if (currFile != null)
            {
                if (!currFile.isDirectory())
                {
                    final String pathName = currFile.getAbsolutePath();
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

                    final File appFile = new File(prefixName + ".app");
                    final File prjFile = new File(prefixName + ".prj");

                    try
                    {
                        final AutomataToControlBuilderSFC exporter = new AutomataToControlBuilderSFC(selectedProject);

                        exporter.serializeApp(appFile, filename);
                        exporter.serializePrj(prjFile, filename);
                    }
                    catch (final Exception ex)
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

    /**
     * Generate C-code
     */
    public static void AutomataToC(final Gui gui)
    {
        final Automata selectedAutomata = gui.getSelectedAutomata();

        if (selectedAutomata.size() < 1)
        {
            JOptionPane.showMessageDialog(gui.getComponent(), "At least one automaton must be selected!", "Alert", JOptionPane.ERROR_MESSAGE);

            return;
        }

        final JFileChooser fileExporter = FileDialogs.getExportFileChooser(FileFormats.C);

        if (fileExporter.showSaveDialog(gui.getComponent()) == JFileChooser.APPROVE_OPTION)
        {
            final File currFile = fileExporter.getSelectedFile();

            if (currFile != null)
            {
                if (!currFile.isDirectory())
                {
                    try
                    {
                        final AutomataToC exporter = new AutomataToC(selectedAutomata);
                        final PrintWriter theWriter = new PrintWriter(new FileWriter(currFile));

                        exporter.serialize(theWriter);
                        theWriter.close();
                    }
                    catch (final Exception ex)
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

    /**
     * Generate Java-code
     */
    public static void AutomataToJava(final Gui gui)
    {
        final Project selectedProject = gui.getSelectedProject();

        if (selectedProject.size() < 1)
        {
            JOptionPane.showMessageDialog(gui.getComponent(), "At least one automaton must be selected!", "Alert", JOptionPane.ERROR_MESSAGE);

            return;
        }

        final JFileChooser fileExporter = FileDialogs.getExportFileChooser(FileFormats.JAVA);

        if (fileExporter.showSaveDialog(gui.getComponent()) == JFileChooser.APPROVE_OPTION)
        {
            final File currFile = fileExporter.getSelectedFile();

            if (currFile != null)
            {
                if (!currFile.isDirectory())
                {
                    try
                    {

                        //Assuming a filename in the form classname.java
                        final String classname = currFile.getName().substring(0, currFile.getName().length() - 5);
                        final AutomataToJava exporter = new AutomataToJava(selectedProject, classname);
                        final PrintWriter theWriter = new PrintWriter(new FileWriter(currFile));

                        exporter.serialize(theWriter);
                        theWriter.close();
                    }
                    catch (final Exception ex)
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

    /**
     * Generate Mindstorm NQC (Not Quite C)
     */
    public static void AutomataToMindstormNQC(final Gui gui)
    {
        final Automata selectedAutomata = gui.getSelectedAutomata();

        if (selectedAutomata.size() < 1)
        {
            JOptionPane.showMessageDialog(gui.getComponent(), "At least one automaton must be selected!", "Alert", JOptionPane.ERROR_MESSAGE);

            return;
        }

        final JFileChooser fileExporter = FileDialogs.getNQCFileExporter();

        if (fileExporter.showSaveDialog(gui.getComponent()) == JFileChooser.APPROVE_OPTION)
        {
            final File currFile = fileExporter.getSelectedFile();

            if (currFile != null)
            {
                if (!currFile.isDirectory())
                {
                    try
                    {
                        final AutomataToNQC exporter = new AutomataToNQC(selectedAutomata);
                        final PrintWriter theWriter = new PrintWriter(new FileWriter(currFile));

                        exporter.serializeNQC(theWriter);
                        theWriter.close();
                    }
                    catch (final Exception ex)
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

    /**
     * Generate SMV (Symbolic Model Verifier)
     */
    public static void AutomataToSMV(final Gui gui)
    {
        final Automata selectedAutomata = gui.getSelectedAutomata();

        if (selectedAutomata.size() < 1)
        {
            JOptionPane.showMessageDialog(gui.getComponent(), "At least one automaton must be selected!", "Alert", JOptionPane.ERROR_MESSAGE);

            return;
        }

        if (!selectedAutomata.isAllEventsPrioritized())
        {
            JOptionPane.showMessageDialog(gui.getComponent(), "All events must be prioritized in this mode!", "Not supported", JOptionPane.ERROR_MESSAGE);

            return;
        }

        final JFileChooser fileExporter = FileDialogs.getExportFileChooser(FileFormats.SMV);

        if (fileExporter.showSaveDialog(gui.getComponent()) == JFileChooser.APPROVE_OPTION)
        {
            final File currFile = fileExporter.getSelectedFile();

            if (currFile != null)
            {
                if (!currFile.isDirectory())
                {
                    try
                    {
                        final AutomataToSMV exporter = new AutomataToSMV(selectedAutomata);
                        final PrintWriter theWriter = new PrintWriter(new FileWriter(currFile));

                        exporter.serializeSMV(theWriter);
                        theWriter.close();
                    }
                    catch (final Exception ex)
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

    /**
     * Generate IEC-61499 Function Block
     */
    public static void ProjectToIEC61499(final Gui gui)
    {
        // Automata selectedProject = gui.getselectedProject();
        final Project selectedProject = gui.getSelectedProject();

        if (selectedProject.size() < 2)
        {
            JOptionPane.showMessageDialog(gui.getComponent(),
                "At least two automatons must be selected!",
                "Alert",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        final JFileChooser fileExporter = FileDialogs.getExportFileChooser(FileFormats.SYS);

        if (fileExporter.showSaveDialog(gui.getComponent()) == JFileChooser.APPROVE_OPTION)
        {
            final File theFile = fileExporter.getSelectedFile();

            if (theFile != null)
            {
                try
                {
                    // Make a deep project copy before changing the names
                    final Project copyOfSelectedProject = new Project(selectedProject, false);
                    copyOfSelectedProject.normalizeAutomataNames();
                    final AutomataToIEC61499 exporter =  new AutomataToIEC61499(copyOfSelectedProject);

                    // ask to turn on comments
                    final int comments =
                        JOptionPane.showConfirmDialog(gui.getComponent(),
                        "Do you want to add comments to the source files?",
                        "Add comments...",
                        JOptionPane.YES_NO_OPTION);
                    if (comments == JOptionPane.YES_OPTION)
                    {
                        exporter.commentsOn();
                    }

                    //ask to export to FBDK or FBRuntime
                    final String[] options = {"FBDK","FBRuntime"};
                    final int exportTo =
                        JOptionPane.showOptionDialog(gui.getComponent(),
                        "What runtime do you want to generate the code for?",
                        "Export to...",
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        options,
                        options[1]);
                    if (exportTo == 0) // FBDK
                    {
                        exporter.useXmlNameSpace(false);
                    }

                    exporter.printSources(theFile);

                }
                catch (final Exception ex)
                {
                    logger.error("Exception while generating IEC-61499 Function Block code to file "
                        + theFile.getAbsolutePath());
                    logger.debug(ex.getMessage());
                    logger.debug(ex.getStackTrace());

                    return;
                }

                logger.info("IEC-61499 Function Block System file successfully generated at "
                    + theFile.getAbsolutePath());
            }
        }
    }

    /**
     * Method for quick evaluation without generating a new action class.
     */
    public static void testMethod(final Gui gui)
    {
        try
        {
                    /*
                    // Test plantification
                    Automata selectedAutomata = gui.getSelectedAutomata();
                        for (Automaton automaton: selectedAutomata)
                        {
                            automaton.setAllStatesAsAccepting();
                        }
                        MinimizationHelper.plantify(selectedAutomata);
                     */

            // Test defaultsynthesismethod
            final Automata selectedAutomata = gui.getSelectedAutomata();
            AutomataSynthesizer.synthesizeControllableNonblocking(selectedAutomata);
        }
        catch (final Exception ex)
        {
            logger.error("Test failed: " + ex);
        }
    }


    ///////////////
    // BDD STUFF //
    ///////////////

    // BDD developer stuff: these are disabled if org.supremica.util.BDD.Options.dev_mode == false
    /**
     * Mark (select) automata in the dependency group of the selected automata.
     */
    public static void markDependencySet()
    {
        try
        {
            final Automata all = gui.getVisualProjectContainer().getActiveProject();
            final Collection<?> v = AutomataCommunicationHelper.getDependencyGroup(gui.getSelectedAutomata(), all);

            gui.selectAutomata(v);
        }
        catch (final Exception ex)
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
            final Automata all = gui.getVisualProjectContainer().getActiveProject();
            final Collection<?> v = AutomataCommunicationHelper.getMaximalComponent(gui.getSelectedAutomata(), all);

            gui.selectAutomata(v);
        }
        catch (final Exception ex)
        {
            logger.error(ex);
        }
    }
}

// ActionMan
