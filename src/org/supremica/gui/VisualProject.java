
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
 * Knarrhogsgatan 10
 * SE-431 60 MOLNDAL
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
package org.supremica.gui;

import java.util.*;
import java.io.*;
import javax.swing.*;
import java.awt.*;
import javax.swing.table.*;
import org.supremica.automata.*;
import org.supremica.log.*;
import org.supremica.gui.animators.scenebeans.AnimationItem;
import org.supremica.gui.animators.scenebeans.Animator;
import org.supremica.gui.simulator.SimulatorExecuter;
import grafchart.sfc.JGrafchartSupremicaEditor;
import org.supremica.properties.Config;
import org.supremica.util.ResourceClassLoader;
import org.swixml.SwingEngine;
//import org.swixml.Localizer;
import org.supremica.util.SupremicaException;
import org.supremica.automata.IO.EncodingHelper; // should really be in the util-package, not?
import org.supremica.gui.ide.AnalyzerAutomataPanel;

/**
 * VisualProject is responsible for keeping track of all windows and other "visual" resources
 * that are associated with a project.
 */
public class VisualProject
    extends Project
{
    private static Logger logger = LoggerFactory.createLogger(VisualProject.class);
    private Automata selectedAutomata = null;
    private ActionAndControlViewer theActionAndControlViewer = null;    // Lazy construction
    private Animator theAnimator = null;    // Lazy construction
    private SwingEngine theSwingEngine = null;    // Lazy construction
    private Container theUserInterface = null;    // Lazy construction
    private SimulatorExecuter theSimulator = null;    // Lazy construction
    private JGrafchartSupremicaEditor theJGrafchartEditor = null;    // Lazy construction
    private HashMap theAutomatonViewerContainer = new HashMap();
    private HashMap theAutomatonExplorerContainer = new HashMap();
//	private HashMap theAutomatonFrameContainer = new HashMap();
    private HashMap theAlphabetViewerContainer = new HashMap();
    private LightTableModel lightTableModel = new LightTableModel();
    private FullTableModel fullTableModel = new FullTableModel();
    private AnalyzerTableModel analyzerTableModel = new AnalyzerTableModel();
    private File projectFile = null;

    public VisualProject()
    {
        initialize();
    }

    public VisualProject(String name)
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

    public void automatonRenamed(Automaton aut, String oldName)
    {
        AutomatonViewer theViewer = (AutomatonViewer) theAutomatonViewerContainer.get(oldName);

        if (theViewer != null)
        {
            theAutomatonViewerContainer.remove(oldName);
            theAutomatonViewerContainer.put(aut.getName(), theViewer);
        }

        AutomatonExplorer theExplorer = (AutomatonExplorer) theAutomatonExplorerContainer.get(oldName);

        if (theExplorer != null)
        {
            theAutomatonExplorerContainer.remove(oldName);
            theAutomatonExplorerContainer.put(aut.getName(), theExplorer);
        }

        AlphabetViewer theAlphabetViewer = (AlphabetViewer) theAlphabetViewerContainer.get(oldName);

        if (theAlphabetViewer != null)
        {
            theAlphabetViewerContainer.remove(oldName);
            theAlphabetViewerContainer.put(aut.getName(), theAlphabetViewer);
        }

        super.automatonRenamed(aut, oldName);
    }

    public void removeAutomaton(Automaton aut)
    {
        super.removeAutomaton(aut);

        AutomatonViewer theViewer = (AutomatonViewer) theAutomatonViewerContainer.get(aut.getName());

        if (theViewer != null)
        {
            theViewer.setVisible(false);
            theViewer.dispose();
            theAutomatonViewerContainer.remove(aut.getName());
        }

        AutomatonExplorer theExplorer = (AutomatonExplorer) theAutomatonExplorerContainer.get(aut.getName());

        if (theExplorer != null)
        {
            theExplorer.setVisible(false);
            theExplorer.dispose();
            theAutomatonExplorerContainer.remove(aut.getName());
        }


        AlphabetViewer theAlphabetViewer = (AlphabetViewer) theAlphabetViewerContainer.get(aut.getName());

        if (theAlphabetViewer != null)
        {
            theAlphabetViewer.setVisible(false);
            theAlphabetViewer.dispose();
            theAlphabetViewerContainer.remove(aut.getName());
        }
    }

    public void setSelectedAutomata(Automata theAutomata)
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
        String title = "Supremica - " + getName();
    }

    public File getProjectFile()
    {
        return projectFile;
    }

    public void setProjectFile(File projectFile)
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
    public AutomatonViewer getAutomatonViewer(String automatonName)
    throws Exception
    {
        return getAutomatonViewer(automatonName, new DefaultAutomatonViewerFactory());
    }

    public AutomatonViewer getAutomatonViewer(String automatonName, AutomatonViewerFactory maker)
    throws Exception
    {
        Automaton automaton = getAutomaton(automatonName);
        if(automaton == null)
        {
            throw new SupremicaException(automatonName + " does not exist in VisualProjectContainer");
        }

        if (existsAutomatonViewer(automaton))
        {
            // Check with the user that its ok to display the automaton
            if (showAutomatonViewer(automaton))
            {
                AutomatonViewer viewer = returnAutomatonViewer(automaton);

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
                    AutomatonViewer viewer = createAutomatonViewer(automaton, maker);
                    viewer.setVisible(true);

                    return viewer;
                }
                else return null;	// null here means "viewer not created since user cancelled due to large state-space"
            }
            catch (Exception ex)
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
    public boolean showAutomatonViewer(Automaton automaton)
    {
        int maxNbrOfStates = Config.DOT_MAX_NBR_OF_STATES.get();
        if (maxNbrOfStates < automaton.nbrOfStates())
        {
            String msg = "The automata " + automaton + " has " + automaton.nbrOfStates() + " states. It is not recommended to display an automaton with more than " + maxNbrOfStates + " states.";
            msg = EncodingHelper.linebreakAdjust(msg);

            Object[] options = { "Continue", "Abort" };
            int response = JOptionPane.showOptionDialog(ActionMan.gui.getFrame(), msg, "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[1]);
            if(response == JOptionPane.NO_OPTION)
            {
                return false; // user chose to "Abort"
            }
        }

        return true;
    }

    // When will any of these throw an exception?? When automaton == null, but else...?
    public boolean existsAutomatonViewer(Automaton automaton)
    throws Exception
    {
        return theAutomatonViewerContainer.containsKey(automaton.getName());
    }

    public AutomatonViewer returnAutomatonViewer(Automaton automaton)
    throws Exception
    {
        return (AutomatonViewer) theAutomatonViewerContainer.get(automaton.getName());
    }

    public AutomatonViewer createAutomatonViewer(Automaton automaton, AutomatonViewerFactory maker)
    throws Exception
    {
        AutomatonViewer viewer = maker.createAutomatonViewer(automaton);
        theAutomatonViewerContainer.put(automaton.getName(), viewer);
        return viewer;
    }

    public AutomatonExplorer getAutomatonExplorer(String automaton)
    throws Exception
    {
        if (theAutomatonExplorerContainer.containsKey(automaton))
        {
            AutomatonExplorer explorer = (AutomatonExplorer) theAutomatonExplorerContainer.get(automaton);

            explorer.setVisible(true);

            return explorer;
        }
        else
        {
            Automaton currAutomaton = getAutomaton(automaton);

            if (currAutomaton != null)
            {
                try
                {
                    AutomatonExplorer explorer = new AutomatonExplorer(this, currAutomaton);

                    theAutomatonExplorerContainer.put(automaton, explorer);
                    explorer.setVisible(true);
                    explorer.initialize();

                    return explorer;
                }
                catch (Exception ex)
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

    public AlphabetViewer getAlphabetViewer(String automaton)
    throws Exception
    {
        logger.debug("VisualProject::getAlphabetViewer(" + automaton + ")");

        if (theAlphabetViewerContainer.containsKey(automaton))
        {
            AlphabetViewer viewer = (AlphabetViewer) theAlphabetViewerContainer.get(automaton);

            viewer.setVisible(true);

            return viewer;
        }
        else
        {
            Automaton currAutomaton = getAutomaton(automaton);

            if (currAutomaton != null)
            {
                try
                {
                    AlphabetViewer viewer = new AlphabetViewer(currAutomaton);

                    theAlphabetViewerContainer.put(automaton, viewer);
                    viewer.setVisible(true);
                    viewer.initialize();

                    return viewer;
                }
                catch (Exception ex)
                {
                    throw new SupremicaException("Error while viewing: " + automaton);
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
            theAnimator = AnimationItem.createInstance(getAnimationURL());
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
                ResourceClassLoader resourceClassLoader = new ResourceClassLoader(ClassLoader.getSystemClassLoader());
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
            catch (Exception ex)
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
            theSimulator = new SimulatorExecuter(this, Config.SIMULATION_IS_EXTERNAL.isTrue());
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

    public JGrafchartSupremicaEditor getJGrafchartEditor()
    {
        if (theJGrafchartEditor == null)
        {
            String[] args = new String[1];

            args[0] = "";

            JGrafchartSupremicaEditor theEditor = new JGrafchartSupremicaEditor(args);

            grafchart.sfc.Editor.singleton = theEditor;
            theJGrafchartEditor = theEditor;

            //theRecipeEditor = org.supremica.gui.recipeEditor.RecipeEditor.createEditor(this);
        }

        theJGrafchartEditor.setVisible(true);

        return theJGrafchartEditor;
    }

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
        public LightTableModel()
        {}

        public int getColumnCount()
        {
            return 1;
        }

        public String getColumnName(int columnIndex)
        {
            if (columnIndex == AnalyzerAutomataPanel.TABLE_NAME_COLUMN)
            {
                return "Automata";
            }

            return "Unknown";
        }

        public Class getColumnClass(int column)
        {
            if (column == AnalyzerAutomataPanel.TABLE_NAME_COLUMN)
            {
                return String.class;
            }

            return String.class;
        }

        public int getRowCount()
        {
            return nbrOfAutomata();
        }

        public int getSize()
        {
            return getRowCount();
        }

        public Object getValueAt(int rowIndex, int columnIndex)
        {
            Automaton theAutomaton = getAutomatonAt(rowIndex);

            if (columnIndex == AnalyzerAutomataPanel.TABLE_NAME_COLUMN)
            {
                return theAutomaton.getName();
            }

            return "Unknown";
        }

        public boolean isCellEditable(int rowIndex, int columnIndex)
        {
            return false;
        }

        public void setValueAt(Object aValue, int rowIndex, int columnIndex)
        {}

        public void updateListeners()
        {
            fireTableDataChanged();
        }

        public void automatonAdded(Automata automata, Automaton automaton)
        {
            updateListeners();
        }

        public void automatonRemoved(Automata automata, Automaton automaton)
        {
            updateListeners();
        }

        public void automatonRenamed(Automata automata, Automaton automaton)
        {
            updateListeners();
        }

        public void actionsOrControlsChanged(Automata automata)
        {    // Do nothing
        }

        public void updated(Object theObject)
        {
            updateListeners();
        }
    }

    public class FullTableModel
        extends AbstractTableModel
        implements AutomataListener
    {
        public FullTableModel()
        {}

        public int getColumnCount()
        {
            return 4;
        }

        public String getColumnName(int columnIndex)
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

        public Class getColumnClass(int column)
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

        public int getRowCount()
        {
            return nbrOfAutomata();
        }

        public int getSize()
        {
            return getRowCount();
        }

        public Object getValueAt(int rowIndex, int columnIndex)
        {
            Automaton theAutomaton = getAutomatonAt(rowIndex);

            if (columnIndex == AnalyzerAutomataPanel.TABLE_NAME_COLUMN)
            {
                return theAutomaton.getName();
            }

            if (columnIndex == AnalyzerAutomataPanel.TABLE_TYPE_COLUMN)
            {
                AutomatonType currType = theAutomaton.getType();

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

        public boolean isCellEditable(int rowIndex, int columnIndex)
        {
            if (columnIndex == AnalyzerAutomataPanel.TABLE_TYPE_COLUMN)
            {
                return true;
            }

            return false;
        }

        public void setValueAt(Object aValue, int rowIndex, int columnIndex)
        {}

        public void updateListeners()
        {
            fireTableDataChanged();
        }

        public void automatonAdded(Automata automata, Automaton automaton)
        {
            updateListeners();
        }

        public void automatonRemoved(Automata automata, Automaton automaton)
        {
            updateListeners();
        }

        public void automatonRenamed(Automata automata, Automaton automaton)
        {
            updateListeners();
        }

        public void actionsOrControlsChanged(Automata automata)
        {
            // Do nothing
        }

        public void updated(Object theObject)
        {
            updateListeners();
        }
    }


    public class AnalyzerTableModel
        extends AbstractTableModel
        implements AutomataListener
    {
        public AnalyzerTableModel()
        {}

        public int getColumnCount()
        {
            return 5;
        }

        public String getColumnName(int columnIndex)
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

        public Class getColumnClass(int column)
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

        public int getRowCount()
        {
            return nbrOfAutomata();
        }

        public int getSize()
        {
            return getRowCount();
        }

        public Object getValueAt(int rowIndex, int columnIndex)
        {
            Automaton theAutomaton = getAutomatonAt(rowIndex);

            if (columnIndex == AnalyzerAutomataPanel.TABLE_NAME_COLUMN)
            {
                return theAutomaton.getName();
            }

            if (columnIndex == AnalyzerAutomataPanel.TABLE_TYPE_COLUMN)
            {
                AutomatonType currType = theAutomaton.getType();

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

        public boolean isCellEditable(int rowIndex, int columnIndex)
        {
            return false;
        }

        public void setValueAt(Object aValue, int rowIndex, int columnIndex)
        {}

        public void updateListeners()
        {
            fireTableDataChanged();
        }

		public void automatonAdded(Automata automata, Automaton automaton)
        {
            updateListeners();
        }

        public void automatonRemoved(Automata automata, Automaton automaton)
        {
            updateListeners();
        }

        public void automatonRenamed(Automata automata, Automaton automaton)
        {
            updateListeners();
        }

        public void actionsOrControlsChanged(Automata automata)
        {    // Do nothing
        }

        public void updated(Object theObject)
        {
            updateListeners();
        }
    }
}
