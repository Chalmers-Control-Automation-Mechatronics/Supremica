//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 1999-2019 Knut Akesson, Martin Fabian, Robi Malik
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

package org.supremica.gui.ide.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Iterator;
import java.util.List;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.automata.IO.AutomataSSPCExporter;
import org.supremica.automata.IO.AutomataToCommunicationGraph;
import org.supremica.automata.IO.AutomataToSTS;
import org.supremica.automata.IO.AutomataToXML;
import org.supremica.automata.IO.AutomatonToDot;
import org.supremica.automata.IO.AutomatonToDsx;
import org.supremica.automata.IO.AutomatonToFSM;
import org.supremica.automata.IO.FileFormats;
import org.supremica.automata.algorithms.minimization.MinimizationHelper;
import org.supremica.gui.ExportDialog;
import org.supremica.gui.ExportFormat;
import org.supremica.gui.FileDialogs;
import org.supremica.gui.ide.IDE;
import org.supremica.gui.texteditor.TextFrame;


public class AnalyzerExportAction
    extends IDEAction
{
    private final Logger logger = LogManager.getLogger(IDE.class);

    private static final long serialVersionUID = 1L;

    public AnalyzerExportAction(final List<IDEAction> actionList)
    {
        super(actionList);

        setEditorActiveRequired(false);
        setAnalyzerActiveRequired(true);

        putValue(Action.NAME, "Export");
        putValue(Action.SHORT_DESCRIPTION, "Export");
//        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_A));
//        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
        putValue(Action.SMALL_ICON, new ImageIcon(IDE.class.getResource("/toolbarButtonGraphics/general/Export16.gif")));
    }

    @Override
    public void actionPerformed(final ActionEvent e)
    {
        doAction();
    }

    /**
     * The code that is run when the action is invoked.
     */
    @Override
    public void doAction()
    {
        final Automata selectedAutomata = ide.getActiveDocumentContainer().getSupremicaAnalyzerPanel().getSelectedAutomata();

        if (!selectedAutomata.sanityCheck(ide.getIDE(), 1))
        {
            return;
        }

        final ExportDialog dlg = new ExportDialog(ide.getFrame());

        dlg.show();

        if (dlg.wasCancelled())
        {
            return;
        }

        final ExportFormat exportMode = dlg.getExportMode();

        if (exportMode != ExportFormat.UNKNOWN)
        {
            automataExport(exportMode);
        }
    }


    // Exporter when the type is already known
    // Add new export functions here and to the function above
    // MF: It's not that simple. The code below defeats that purpose. Where are the exporter objects?
    // OO was invented just to avoid the type of code below. It's a maintenance nightmare!!
    private void automataExport(final ExportFormat exportMode)
    {
        final Automata selectedAutomata = ide.getActiveDocumentContainer().getSupremicaAnalyzerPanel().getSelectedAutomata();

        if (!selectedAutomata.sanityCheck(ide.getIDE(), 1))
        {
            return;
        }

        if ((exportMode == ExportFormat.FSM_DEBUG) || (exportMode == ExportFormat.FSM))
        {
            // UMDES cannot deal with forbidden states
            if (selectedAutomata.hasForbiddenState())
            {
                JOptionPane.showMessageDialog(ide.getIDE(), "UMDES cannot handle forbidden states", "Alert", JOptionPane.ERROR_MESSAGE);

                return;
            }
        }

        // Take care of the new debug stuff first. This is really silly.
        // Proper design would have solved this problem
        if (exportMode == ExportFormat.XML_DEBUG)
        {
            final AutomataToXML xport = new AutomataToXML(selectedAutomata);
            final TextFrame textframe = new TextFrame("XML debug output");

            xport.serialize(textframe.getPrintWriter());

            return;
        }
/*
        if (exportMode == ExportFormat.SP_DEBUG)
        {
            ProjectToSP exporter = new ProjectToSP(ide.getActiveProject());
            TextFrame textframe = new TextFrame("SP debug output");

            exporter.serialize(textframe.getPrintWriter());

            return;
        }
*/
        if (exportMode == ExportFormat.DOT_DEBUG)
        {
            for (final Iterator<Automaton> autIt = selectedAutomata.iterator();
            autIt.hasNext(); )
            {
                final Automaton currAutomaton = autIt.next();
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
            for (final Iterator<Automaton> autIt = selectedAutomata.iterator();
            autIt.hasNext(); )
            {
                final Automaton currAutomaton = autIt.next();
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
            for (final Iterator<Automaton> autIt = selectedAutomata.iterator();
            autIt.hasNext(); )
            {
                final Automaton currAutomaton = autIt.next();
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

            if (fileExporter.showSaveDialog(ide.getIDE()) == JFileChooser.APPROVE_OPTION)
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
        if ((exportMode == ExportFormat.DOT) || (exportMode == ExportFormat.DSX) || (exportMode == ExportFormat.FSM) || (exportMode == ExportFormat.PCG))
        {
            for (final Iterator<Automaton> autIt = selectedAutomata.iterator();
            autIt.hasNext(); )
            {
                final Automaton currAutomaton = autIt.next();

                automatonExport(exportMode, currAutomaton);
            }
        }
        else
        {
            JFileChooser fileExporter = null;

            if (exportMode == ExportFormat.XML)
            {
                fileExporter = FileDialogs.getXMLFileExporter();

                //return;
            }
            else if(exportMode == ExportFormat.STS)
            {
                fileExporter = FileDialogs.getSTSFileExporter();
            }
 /*
            else if (exportMode == ExportFormat.SP)
            {
                fileExporter = FileDialogs.getSPFileExporter();
            }
 */
            else
            {
                return;
            }

            fileExporter.setDialogTitle("Save Project as ...");

            if (fileExporter.showSaveDialog(ide.getIDE()) == JFileChooser.APPROVE_OPTION)
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
                                final AutomataToXML exporter = new AutomataToXML(selectedAutomata);
                                exporter.serialize(currFile);
                            }
                            else if (exportMode == ExportFormat.STS)
                            {
                                final Automata automata = selectedAutomata;
                                MinimizationHelper.plantify(automata);
                                final AutomataToSTS exporter = new AutomataToSTS(automata);
                                exporter.serialize(currFile);

                                fileExporter = FileDialogs.getSTSFileExporter();
                                fileExporter.setDialogTitle("Save spec as ...");
                                if (fileExporter.showSaveDialog(ide.getIDE()) == JFileChooser.APPROVE_OPTION)
                                {
                                    final File currFileSpec = fileExporter.getSelectedFile();
                                    if (currFileSpec != null)
                                    {
                                        if (!currFileSpec.isDirectory())
                                        {
                                            try
                                            {
                                                exporter.createSpec(currFileSpec);
                                            }
                                            catch (final Exception ex)
                                            {
                                                logger.error("Exception while exporting " + currFileSpec.getAbsolutePath(), ex);
                                                logger.debug(ex.getStackTrace());
                                            }
                                        }
                                    }
                                }
                            }
                            /*
                            else if (exportMode == ExportFormat.SP)
                            {
                                ProjectToSP exporter = new ProjectToSP(ide.getIDE().getActiveProject());
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
    }


    // Exporter when the type is already known
    // Add new export functions here and to the function above
    public void automatonExport(final ExportFormat exportMode, final Automaton currAutomaton)
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
/*
        else if (exportMode == ExportFormat.SP)
        {
            fileExporter = FileDialogs.getExportFileChooser(FileFormats.SP);
        }
  */
        else
        {
            return;
        }

        // ARASH: ain't it good to see what we're doin' ??
        fileExporter.setDialogTitle("Save " + currAutomaton.getName() + " as ...");

        if (fileExporter.showSaveDialog(ide.getIDE()) == JFileChooser.APPROVE_OPTION)
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
/*
                        else if (exportMode == ExportFormat.SP)
                        {
                            Project activeProject = ide.getActiveProject();
                            //Project newProject = new Project();
                            //newProject.addAttributes(selectedProject);
                            //newProject.addActions(selectedProject.getActions());
                            //newProject.addControls(selectedProject.getControls());
                            //newProject.setAnimationURL(selectedProject.getAnimationURL());
                            ProjectToSP exporter = new ProjectToSP(activeProject);
                            exporter.serialize(currFile);
                        }
*/
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
}
