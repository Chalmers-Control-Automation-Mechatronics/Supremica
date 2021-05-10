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

import java.awt.Container;
import java.awt.Frame;
import java.io.File;
import java.net.URL;
import java.util.HashMap;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.automata.Automata;
import org.supremica.automata.AutomataListener;
import org.supremica.automata.Automaton;
import org.supremica.automata.AutomatonType;
import org.supremica.automata.Project;
import org.supremica.automata.IO.EncodingHelper; // should really be in the util-package, not?
import org.supremica.gui.animators.scenebeans.AnimationItem;
import org.supremica.gui.animators.scenebeans.Animator;
import org.supremica.gui.ide.AnalyzerAutomataPanel;
import org.supremica.gui.simulator.SimulatorExecuter;
import org.supremica.properties.Config;
import org.supremica.util.ResourceClassLoader;
import org.supremica.util.SupremicaException;

import org.swixml.SwingEngine;


/**
 * VisualProject is responsible for keeping track of all windows and other "visual" resources
 * that are associated with a project.
 */
public class VisualProject
    extends Project
{
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LogManager.getLogger(VisualProject.class);

    private Automata selectedAutomata = null;
    private ActionAndControlViewer theActionAndControlViewer = null;    // Lazy construction
    private Animator theAnimator = null;    // Lazy construction
    private SwingEngine theSwingEngine = null;    // Lazy construction
    private Container theUserInterface = null;    // Lazy construction
    private SimulatorExecuter theSimulator = null;    // Lazy construction
    private final HashMap<String, AutomatonViewer> theAutomatonViewerContainer = new HashMap<String, AutomatonViewer>();
    private final HashMap<String, AutomatonExplorer> theAutomatonExplorerContainer = new HashMap<String, AutomatonExplorer>();
//	private HashMap theAutomatonFrameContainer = new HashMap();
    private final HashMap<String, AlphabetViewer> theAlphabetViewerContainer = new HashMap<String, AlphabetViewer>();
    private final LightTableModel lightTableModel = new LightTableModel();
    private final FullTableModel fullTableModel = new FullTableModel();
    private final AnalyzerTableModel analyzerTableModel = new AnalyzerTableModel();
    private File projectFile = null;

    public VisualProject()
    {
        initialize();
    }

    public VisualProject(final String name)
    {
        super(name);

        initialize();
    }

    private void initialize()
    {
        addListener(lightTableModel);
        addListener(fullTableModel);
        addListener(analyzerTableModel);
    }

	@Override
    public void clear()
    {
        super.clear();

        if (theAnimator != null)
        {
            theAnimator.setVisible(false);
            theAnimator.dispose();

            theAnimator = null;
        }

        if (theActionAndControlViewer != null)
        {
            theActionAndControlViewer.setVisible(false);
            theActionAndControlViewer.dispose();

            theActionAndControlViewer = null;
        }

        projectFile = null;
    }

	@Override
    public void automatonRenamed(final Automaton aut, final String oldName)
    {
        //System.err.println("Rename " + aut + " from " + oldName);

        final AutomatonViewer theViewer = theAutomatonViewerContainer.get(oldName);

        if (theViewer != null)
        {
            theAutomatonViewerContainer.remove(oldName);
            theAutomatonViewerContainer.put(aut.getName(), theViewer);
        }

        final AutomatonExplorer theExplorer = theAutomatonExplorerContainer.get(oldName);

        if (theExplorer != null)
        {
            theAutomatonExplorerContainer.remove(oldName);
            theAutomatonExplorerContainer.put(aut.getName(), theExplorer);
        }

        final AlphabetViewer theAlphabetViewer = theAlphabetViewerContainer.get(oldName);

        if (theAlphabetViewer != null)
        {
            theAlphabetViewerContainer.remove(oldName);
            theAlphabetViewerContainer.put(aut.getName(), theAlphabetViewer);
        }

        super.automatonRenamed(aut, oldName);
    }

	@Override
    public void removeAutomaton(final Automaton aut)
    {
        super.removeAutomaton(aut);

        final AutomatonViewer theViewer = theAutomatonViewerContainer.get(aut.getName());

        if (theViewer != null)
        {
            theViewer.setVisible(false);
            theViewer.dispose();
            theAutomatonViewerContainer.remove(aut.getName());
        }

        final AutomatonExplorer theExplorer = theAutomatonExplorerContainer.get(aut.getName());

        if (theExplorer != null)
        {
            theExplorer.setVisible(false);
            theExplorer.dispose();
            theAutomatonExplorerContainer.remove(aut.getName());
        }


        final AlphabetViewer theAlphabetViewer = theAlphabetViewerContainer.get(aut.getName());

        if (theAlphabetViewer != null)
        {
            theAlphabetViewer.setVisible(false);
            theAlphabetViewer.dispose();
            theAlphabetViewerContainer.remove(aut.getName());
        }
    }

    public void setSelectedAutomata(final Automata theAutomata)
    {
        this.selectedAutomata = theAutomata;
    }

    public Automata getSelectedAutomata()
    {
        return selectedAutomata;
    }


    public int numberOfSelectedAutomata()
    {
        return getSelectedAutomata().size();
    }

    public void clearSelection()
    {
        selectedAutomata = null;
    }

    public void updateFrameTitles()
    {
        // String title = "Supremica - " + getName();
    }

    public File getProjectFile()
    {
        return projectFile;
    }

    public void setProjectFile(final File projectFile)
    {
        this.projectFile = projectFile;
    }

        /* This code is no good, but slightly better now.
         * The default behavior was (and is) to create a new viewer if none existed
         * Now there is at least a possibility to simply ask for viewers and not have
         * new ones generated if none already exists (however, if a non-visible viewer exists
         * it will be shown, is this good?). It is also possible to reuse this code
         * for using your own personal favorite viewer, that's what factories are for.
         * Note that the previous (bad) default behavior is preserved.
         *
         * Suggestion for better code. Break this into three functions
         * existsAutomatonViewer, createAutomatonViewer, returnAutomatonViewer
         * None of these should alter the visibility of the viewer!
         * This would also solve the exception problem that occurs when you simply want to
         * as whether a viewer exists for a certain automaton..
         */
    public AutomatonViewer getAutomatonViewer(final String automatonName)
    throws Exception
    {
        return getAutomatonViewer(automatonName, new DefaultAutomatonViewerFactory());
    }

    public AutomatonViewer getAutomatonViewer(final String automatonName, final AutomatonViewerFactory maker)
    throws Exception
    {
        final Automaton automaton = getAutomaton(automatonName);
        if(automaton == null)
        {
            throw new SupremicaException(automatonName + " does not exist in VisualProjectContainer");
        }

        if (existsAutomatonViewer(automaton))
        {
            // Check with the user that its ok to display the automaton
            if (showAutomatonViewer(automaton))
            {
                final AutomatonViewer viewer = returnAutomatonViewer(automaton);

                viewer.setVisible(true);
                viewer.setState(Frame.NORMAL);

                return viewer;
            }
            else
            {
                // The user didn't like what was presented to her
                return null;
            }
        }
        else if(maker != null)
        {
            try
            {
                if(showAutomatonViewer(automaton))
                {
                    final AutomatonViewer viewer = createAutomatonViewer(automaton, maker);
                    viewer.setVisible(true);

                    return viewer;
                }
                else return null;	// null here means "viewer not created since user cancelled due to large state-space"
            }
            catch (final Exception ex)
            {
                throw new SupremicaException("Error while viewing: " + automatonName);
            }
        }
        else
        {
            return null; // null here means "no viewer exists and you didn't want me to construct a new one"
        }
    }

    // This is what it should really be like, one task - one function...
    public boolean showAutomatonViewer(final Automaton automaton)
    {
        final int maxNbrOfStates = Config.DOT_MAX_NBR_OF_STATES.getValue();
        if (maxNbrOfStates < automaton.nbrOfStates())
        {
            String msg = "The automata " + automaton + " has " + automaton.nbrOfStates() + " states. It is not recommended to display an automaton with more than " + maxNbrOfStates + " states.";
            msg = EncodingHelper.linebreakAdjust(msg);

            final Object[] options = { "Continue", "Abort" };
            final int response = JOptionPane.showOptionDialog(ActionMan.gui.getFrame(), msg, "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[1]);
            if(response == JOptionPane.NO_OPTION)
            {
                return false; // user chose to "Abort"
            }
        }

        return true;
    }

    // When will any of these throw an exception?? When automaton == null, but else...?
    public boolean existsAutomatonViewer(final Automaton automaton)
    throws Exception
    {
        return theAutomatonViewerContainer.containsKey(automaton.getName());
    }

    public AutomatonViewer returnAutomatonViewer(final Automaton automaton)
    throws Exception
    {
        return theAutomatonViewerContainer.get(automaton.getName());
    }

    public AutomatonViewer createAutomatonViewer(final Automaton automaton, final AutomatonViewerFactory maker)
    throws Exception
    {
        final AutomatonViewer viewer = maker.createAutomatonViewer(automaton);
        theAutomatonViewerContainer.put(automaton.getName(), viewer);
        return viewer;
    }

    public AutomatonExplorer getAutomatonExplorer(final String automaton)
    throws Exception
    {
        if (theAutomatonExplorerContainer.containsKey(automaton))
        {
            final AutomatonExplorer explorer = theAutomatonExplorerContainer.get(automaton);

            explorer.setVisible(true);

            return explorer;
        }
        else
        {
            final Automaton currAutomaton = getAutomaton(automaton);

            if (currAutomaton != null)
            {
                try
                {
                    final AutomatonExplorer explorer = new AutomatonExplorer(this, currAutomaton);

                    theAutomatonExplorerContainer.put(automaton, explorer);
                    explorer.setVisible(true);
                    explorer.initialize();

                    return explorer;
                }
                catch (final Exception ex)
                {
                    throw new SupremicaException("Error while exploring: " + automaton);
                }
            }
            else
            {
                throw new SupremicaException(automaton + " does not exist in VisualProjectContainer");
            }
        }
    }

    public ActionAndControlViewer getActionAndControlViewer()
    throws Exception
    {
        if (theActionAndControlViewer == null)
        {
            theActionAndControlViewer = new ActionAndControlViewer(this);
        }

        theActionAndControlViewer.setVisible(true);

        return theActionAndControlViewer;
    }

    public Animator getAnimator()
    throws Exception
    {
        if (!hasAnimation())
        {
            return null;
        }

        if (theAnimator == null)
        {
            final URL url = getAnimationURL();
//            url = new URL("file:/C:/Supremica/trunk/animations/scenebeans/agv/agv.xml");
            theAnimator = AnimationItem.createInstance(url);
        }

        theAnimator.setVisible(true);

        return theAnimator;
    }


    public Container getUserInterface()
    throws Exception
    {
        if (!hasUserInterface())
        {
            return null;
        }

        if (theUserInterface == null)
        {
            getSwingEngine();
        }

        return theUserInterface;
    }

    public SwingEngine getSwingEngine()
    throws Exception
    {
        if (!hasUserInterface())
        {
            return null;
        }

        if (theSwingEngine == null)
        {
            try
            {
                theSwingEngine = new SwingEngine();
                final ResourceClassLoader resourceClassLoader = new ResourceClassLoader(ClassLoader.getSystemClassLoader());
                theSwingEngine.setClassLoader(resourceClassLoader);
                //Localizer localizer = theSwingEngine.getLocalizer();
                //localizer.setClassLoader(resourceClassLoader);
                theUserInterface = theSwingEngine.render(getUserInterfaceURL());
                if (theUserInterface instanceof JDialog)
                {
                    ((JDialog) theUserInterface).pack();
                }
                if (theUserInterface instanceof JFrame)
                {
                    ((JFrame) theUserInterface).setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                }
            }
            catch (final Exception ex)
            {
                logger.error(ex);
            }
        }

        return theSwingEngine;
    }

    public SimulatorExecuter getSimulator()
    throws Exception
    {
        if (theSimulator == null)
        {

            // ARASH: WAS
            // theSimulator = new SimulatorExecuter(this, false);
            theSimulator = new SimulatorExecuter(this, Config.SIMULATION_IS_EXTERNAL.getValue());
        }

        theSimulator.setVisible(true);

        return theSimulator;
    }

/*
                public SimulatorExecuter getExternalExecuter()
                                throws Exception
                {
                                if (theSimulator == null)
                                {
                                                theSimulator = new SimulatorExecuter(this, true);
                                }
                                theSimulator.setVisible(true);
                                return theSimulator;
                }
 */

    public void clearSimulationData()
    {
                /*
                Actions theActions = getActions();
                if (theActions != null)
                {
                                theActions.clear();
                }
                Controls theControls = getControls();
                if (theControls != null)
                {
                                theControls.clear();
                }*/
        if (theActionAndControlViewer != null)
        {
            theActionAndControlViewer.setVisible(false);

            theActionAndControlViewer = null;
        }

        if (theAnimator != null)
        {
            theAnimator.setVisible(false);

            theAnimator = null;
        }

        if (theSimulator != null)
        {
            theSimulator.setVisible(false);

            theSimulator = null;
        }
    }

    public int getSize()
    {
        return lightTableModel.getRowCount();
    }

    public TableModel getLightTableModel()
    {
        return lightTableModel;
    }

    public TableModel getFullTableModel()
    {
        return fullTableModel;
    }

    public TableModel getAnalyzerTableModel()
    {
        return analyzerTableModel;
    }

    public class LightTableModel
        extends AbstractTableModel
        implements AutomataListener
    {
        private static final long serialVersionUID = 1L;

        public LightTableModel()
        {}

        @Override
        public int getColumnCount()
        {
            return 1;
        }

        @Override
        public String getColumnName(final int columnIndex)
        {
            if (columnIndex == AnalyzerAutomataPanel.TABLE_NAME_COLUMN)
            {
                return "Automata";
            }

            return "Unknown";
        }

        @Override
        public Class<?> getColumnClass(final int column)
        {
            if (column == AnalyzerAutomataPanel.TABLE_NAME_COLUMN)
            {
                return String.class;
            }

            return String.class;
        }

        @Override
        public int getRowCount()
        {
            return nbrOfAutomata();
        }

        public int getSize()
        {
            return getRowCount();
        }

        @Override
        public Object getValueAt(final int rowIndex, final int columnIndex)
        {
            final Automaton theAutomaton = getAutomatonAt(rowIndex);

            if (columnIndex == AnalyzerAutomataPanel.TABLE_NAME_COLUMN)
            {
                return theAutomaton.getName();
            }

            return "Unknown";
        }

        @Override
        public boolean isCellEditable(final int rowIndex, final int columnIndex)
        {
            return false;
        }

        @Override
        public void setValueAt(final Object aValue, final int rowIndex, final int columnIndex)
        {}

        public void updateListeners()
        {
            fireTableDataChanged();
        }

        @Override
        public void automatonAdded(final Automata automata, final Automaton automaton)
        {
            updateListeners();
        }

        @Override
        public void automatonRemoved(final Automata automata, final Automaton automaton)
        {
            updateListeners();
        }

        @Override
        public void automatonRenamed(final Automata automata, final Automaton automaton)
        {
            updateListeners();
        }

        @Override
        public void actionsOrControlsChanged(final Automata automata)
        {    // Do nothing
        }

        @Override
        public void updated(final Object theObject)
        {
            updateListeners();
        }
    }

    public class FullTableModel
        extends AbstractTableModel
        implements AutomataListener
    {
        private static final long serialVersionUID = 1L;

        public FullTableModel()
        {}

        @Override
        public int getColumnCount()
        {
            return 4;
        }

        @Override
        public String getColumnName(final int columnIndex)
        {
            if (columnIndex == AnalyzerAutomataPanel.TABLE_NAME_COLUMN)
            {
                return "Automata";
            }

            if (columnIndex == AnalyzerAutomataPanel.TABLE_TYPE_COLUMN)
            {
                return "Type";
            }

            if (columnIndex == AnalyzerAutomataPanel.TABLE_STATES_COLUMN)
            {
                return "Number of states";
            }

            if (columnIndex == AnalyzerAutomataPanel.TABLE_EVENTS_COLUMN)
            {
                return "Number of events";
            }

            return "Unknown";
        }

        @Override
        public Class<?> getColumnClass(final int column)
        {
            if (column == AnalyzerAutomataPanel.TABLE_NAME_COLUMN)
            {
                return String.class;
            }

            if (column == AnalyzerAutomataPanel.TABLE_TYPE_COLUMN)
            {
                return String.class;
            }

            if ((column == AnalyzerAutomataPanel.TABLE_STATES_COLUMN) || (column == AnalyzerAutomataPanel.TABLE_EVENTS_COLUMN))
            {
                return Integer.class;
            }

            return String.class;
        }

        @Override
        public int getRowCount()
        {
            return nbrOfAutomata();
        }

        public int getSize()
        {
            return getRowCount();
        }

        @Override
        public Object getValueAt(final int rowIndex, final int columnIndex)
        {
            final Automaton theAutomaton = getAutomatonAt(rowIndex);

            if (columnIndex == AnalyzerAutomataPanel.TABLE_NAME_COLUMN)
            {
                return theAutomaton.getName();
            }

            if (columnIndex == AnalyzerAutomataPanel.TABLE_TYPE_COLUMN)
            {
                final AutomatonType currType = theAutomaton.getType();

                return currType.toString();
            }

            if (columnIndex == AnalyzerAutomataPanel.TABLE_STATES_COLUMN)
            {
                return new Integer(theAutomaton.nbrOfStates());
            }

            if (columnIndex == AnalyzerAutomataPanel.TABLE_EVENTS_COLUMN)
            {
                return new Integer(theAutomaton.nbrOfEvents());
            }

            return "Unknown";
        }

        @Override
        public boolean isCellEditable(final int rowIndex, final int columnIndex)
        {
            if (columnIndex == AnalyzerAutomataPanel.TABLE_TYPE_COLUMN)
            {
                return true;
            }

            return false;
        }

        @Override
        public void setValueAt(final Object aValue, final int rowIndex, final int columnIndex)
        {}

        public void updateListeners()
        {
            fireTableDataChanged();
        }

        @Override
        public void automatonAdded(final Automata automata, final Automaton automaton)
        {
            updateListeners();
        }

        @Override
        public void automatonRemoved(final Automata automata, final Automaton automaton)
        {
            updateListeners();
        }

        @Override
        public void automatonRenamed(final Automata automata, final Automaton automaton)
        {
            updateListeners();
        }

        @Override
        public void actionsOrControlsChanged(final Automata automata)
        {
            // Do nothing
        }

        @Override
        public void updated(final Object theObject)
        {
            updateListeners();
        }
    }


    public class AnalyzerTableModel
        extends AbstractTableModel
        implements AutomataListener
    {
        private static final long serialVersionUID = 1L;

        public AnalyzerTableModel()
        {}

        @Override
        public int getColumnCount()
        {
            return 5;
        }

        @Override
        public String getColumnName(final int columnIndex)
        {
            if (columnIndex == AnalyzerAutomataPanel.TABLE_NAME_COLUMN)
            {
                return "Name";
            }

            if (columnIndex == AnalyzerAutomataPanel.TABLE_TYPE_COLUMN)
            {
                return "Type";
            }

            if (columnIndex == AnalyzerAutomataPanel.TABLE_STATES_COLUMN)
            {
                return "|Q|";
            }

            if (columnIndex == AnalyzerAutomataPanel.TABLE_EVENTS_COLUMN)
            {
                return "|\u03a3|";
            }

            if (columnIndex == AnalyzerAutomataPanel.TABLE_TRANSITIONS_COLUMN)
            {
                return "|\u2192|";
            }

            return "Unknown";
        }

        @Override
        public Class<?> getColumnClass(final int column)
        {
            if (column == AnalyzerAutomataPanel.TABLE_NAME_COLUMN)
            {
                return String.class;
            }

            if (column == AnalyzerAutomataPanel.TABLE_TYPE_COLUMN)
            {
                return String.class;
            }

            if ((column == AnalyzerAutomataPanel.TABLE_STATES_COLUMN) || (column == AnalyzerAutomataPanel.TABLE_EVENTS_COLUMN) || (column == AnalyzerAutomataPanel.TABLE_TRANSITIONS_COLUMN))
            {
                return Integer.class;
            }

            return String.class;
        }

        @Override
        public int getRowCount()
        {
            return nbrOfAutomata();
        }

        public int getSize()
        {
            return getRowCount();
        }

        @Override
        public Object getValueAt(final int rowIndex, final int columnIndex)
        {
            final Automaton theAutomaton = getAutomatonAt(rowIndex);

            if (columnIndex == AnalyzerAutomataPanel.TABLE_NAME_COLUMN)
            {
                return theAutomaton.getName();
            }

            if (columnIndex == AnalyzerAutomataPanel.TABLE_TYPE_COLUMN)
            {
                final AutomatonType currType = theAutomaton.getType();

                return currType.toString();
            }

            if (columnIndex == AnalyzerAutomataPanel.TABLE_STATES_COLUMN)
            {
                return new Integer(theAutomaton.nbrOfStates());
            }

            if (columnIndex == AnalyzerAutomataPanel.TABLE_EVENTS_COLUMN)
            {
                return new Integer(theAutomaton.nbrOfEvents());
            }

            if (columnIndex == AnalyzerAutomataPanel.TABLE_TRANSITIONS_COLUMN)
            {
                return new Integer(theAutomaton.nbrOfTransitions());
            }

            return "Unknown";
        }

        @Override
        public boolean isCellEditable(final int rowIndex, final int columnIndex)
        {
            return false;
        }

        @Override
        public void setValueAt(final Object aValue, final int rowIndex, final int columnIndex)
        {}

        public void updateListeners()
        {
            fireTableDataChanged();
        }

		@Override
    public void automatonAdded(final Automata automata, final Automaton automaton)
        {
            updateListeners();
        }

        @Override
        public void automatonRemoved(final Automata automata, final Automaton automaton)
        {
            updateListeners();
        }

        @Override
        public void automatonRenamed(final Automata automata, final Automaton automaton)
        {
            updateListeners();
        }

        @Override
        public void actionsOrControlsChanged(final Automata automata)
        {    // Do nothing
        }

        @Override
        public void updated(final Object theObject)
        {
            updateListeners();
        }
    }
}
