//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 1999-2017 Knut Akesson, Martin Fabian, Robi Malik
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

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.help.CSH;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import net.sourceforge.waters.config.Version;
import net.sourceforge.waters.gui.about.AboutPopup;
import net.sourceforge.waters.gui.util.IconAndFontLoader;

import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.automata.Project;
import org.supremica.automata.IO.EncodingHelper;
import org.supremica.automata.IO.ProjectBuildFromXML;
import org.supremica.comm.xmlrpc.Server;
import org.supremica.gui.help.ContentHelp;
import org.supremica.gui.ide.IDEReportInterface;
import org.supremica.log.Logger;
import org.supremica.log.LoggerFactory;
import org.supremica.properties.Config;


public class Supremica
    extends JFrame
    implements IDEReportInterface, TableModelListener,
               Gui, VisualProjectContainerListener
{
    private static final long serialVersionUID = 1L;
    private final Logger logger = LoggerFactory.createLogger(Supremica.class);
    private final LogDisplay theLogDisplay = LogDisplay.getInstance();
    private JPanel contentPane;
    private final MainMenuBar menuBar = new MainMenuBar(this);
    private final MainToolBar toolBar = new MainToolBar(this);
    private final MainPopupMenu mainPopupMenu = new MainPopupMenu(this);
    private VisualProjectContainer theVisualProjectContainer;
    @SuppressWarnings("unused")
	private TypeCellEditor typeEditor;
    private BorderLayout layout;
    private JTable theAutomatonTable;
    private TableSorter theTableSorter;
    private TableModel fullTableModel;
    private JScrollPane theAutomatonTableScrollPane;
    @SuppressWarnings("unused")
	private MenuHandler menuHandler;
    private JSplitPane splitPaneVertical;
    @SuppressWarnings("unused")
	private Server xmlRpcServer = null;
    private ContentHelp help = null;
    @SuppressWarnings("unused")
	private CSH.DisplayHelpFromSource helpDisplayer = null;

    // MF -- made publically available
    public static int TABLE_IDENTITY_COLUMN = 0;
    public static int TABLE_TYPE_COLUMN = 1;
    public static int TABLE_STATES_COLUMN = 2;
    public static int TABLE_EVENTS_COLUMN = 3;

    // Construct the frame
    public Supremica()
    {
        //setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        //setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        theVisualProjectContainer = new VisualProjectContainer();
        theVisualProjectContainer.addListener(this);
        final VisualProject theVisualProject = new VisualProject("");
        theVisualProjectContainer.addProject(theVisualProject);
        setActiveProject(theVisualProject);
        // theVisualProjectContainer = currProject.getVisualProjectContainer();
        // theVisualProjectContainer.addListener(this);

        logger.info("Supremica version: " + Version.getInstance().toString());

        if (Config.XML_RPC_ACTIVE.isTrue())
        {
            boolean serverStarted = true;

            try
            {
                xmlRpcServer = new Server(theVisualProjectContainer, Config.XML_RPC_PORT.get());
            }
            catch (final Exception e)
            {
                serverStarted = false;

                logger.warn("Another server already running on port " + Config.XML_RPC_PORT.get() + ". XML-RPC server not started!");
            }

            if (serverStarted)
            {
                logger.info("XML-RPC server running on port " + Config.XML_RPC_PORT.get());
            }
        }

        layout = new BorderLayout();
        fullTableModel = getActiveProject().getFullTableModel();
        theTableSorter = new TableSorter(fullTableModel);
        theAutomatonTable = new JTable(theTableSorter);

        theAutomatonTable.getTableHeader().setReorderingAllowed(false);
        theTableSorter.addMouseListenerToHeaderInTable(theAutomatonTable);

        menuHandler = new MenuHandler(
            /**
             *  theAutomatonTable
             */
            );
        theAutomatonTableScrollPane = new JScrollPane(theAutomatonTable);

        final JViewport vp = theAutomatonTableScrollPane.getViewport();

        vp.setBackground(Color.white);
        theAutomatonTable.setBackground(Color.white);

        splitPaneVertical = new JSplitPane(JSplitPane.VERTICAL_SPLIT, theAutomatonTableScrollPane, theLogDisplay.getComponent());
        theLogDisplay.getComponent().updateUI();

        enableEvents(AWTEvent.WINDOW_EVENT_MASK);

        help = new ContentHelp();

        try
        {
            jbInit();
        }
        catch (final Exception ex)
        {
            logger.debug(ex.getStackTrace());
        }

        // This code used to be in the popup menu -------------
        theAutomatonTable.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(final MouseEvent e)
            {
                if (e.getClickCount() == 2)
                {
                    final int col = theAutomatonTable.columnAtPoint(e.getPoint());
                    final int row = theAutomatonTable.rowAtPoint(e.getPoint());

                    if (row < 0)
                    {
                        return;
                    }

                    if (Config.DOT_USE.isTrue())
                    {
                        if (col == TABLE_IDENTITY_COLUMN)
                        {
                            ActionMan.automatonView_actionPerformed(getGui());
                            getGui().repaint();
                        }
                    }
                }
            }

            @Override
            public void mousePressed(final MouseEvent e)
            {
                // This is needed for the Linux platform
                // where isPopupTrigger is true only on mousePressed.
                maybeShowPopup(e);
            }

            @Override
            public void mouseReleased(final MouseEvent e)
            {
                // This is for triggering the popup on Windows platforms
                maybeShowPopup(e);
            }

            private void maybeShowPopup(final MouseEvent e)
            {
                if (e.isPopupTrigger())
                {
                    final int currRow = theAutomatonTable.rowAtPoint(e.getPoint());

                    if (currRow < 0)
                    {
                        return;
                    }

                    if (!theAutomatonTable.isRowSelected(currRow))
                    {
                        theAutomatonTable.clearSelection();
                        theAutomatonTable.setRowSelectionInterval(currRow, currRow);
                    }

                    mainPopupMenu.show(theAutomatonTable.getSelectedRowCount(), e.getComponent(), e.getX(), e.getY());
                }
            }
        });

        // --------------------------------------//
    }

    public Supremica(final String arg)
    {
        this();

        if (arg != null)
        {
            openProjectXMLFile(new File(arg));
        }
    }

    // local helper utility
    Gui getGui()
    {
        return this;
    }

    public void setActiveProject(final VisualProject activeProject)
    {
        theVisualProjectContainer.setActiveProject(activeProject);
        updateTitle();
    }

    public VisualProject getActiveProject()
    {
        return theVisualProjectContainer.getActiveProject();
    }

    public void updateTitle()
    {
        final Project currProject = getActiveProject();

        if (currProject != null)
        {
            final String projectName = currProject.getName();

            setTitle("Supremica " + projectName);
        }
        else
        {
            setTitle("Supremica");
        }
    }

    // Component initialization
    private void jbInit()
    throws Exception
    {
        contentPane = (JPanel) getContentPane();

        contentPane.setLayout(layout);
        contentPane.setOpaque(true);
        contentPane.setBackground(Color.white);
        setSize(new Dimension(800, 600));

        // theVisualProjectContainer.updateFrameTitles();
        // Enables stylish rollover buttions - JDK 1.4 required
//              toolBar.setRollover(true);
        contentPane.add(toolBar, BorderLayout.NORTH);
        contentPane.add(splitPaneVertical, BorderLayout.CENTER);
        splitPaneVertical.setContinuousLayout(false);
        splitPaneVertical.setOneTouchExpandable(false);
        fullTableModel.addTableModelListener(this);
        theAutomatonTable.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyPressed(final KeyEvent e)
            {
                if (e.getKeyCode() == KeyEvent.VK_DELETE)
                {
                    ActionMan.automataDelete_actionPerformed(getGui());
                }
            }

            @Override
            public void keyReleased(final KeyEvent e)
            {}

            @Override
            public void keyTyped(final KeyEvent e)
            {}
        });

        typeEditor = new TypeCellEditor(theAutomatonTable, theTableSorter, theVisualProjectContainer);
        helpDisplayer = new CSH.DisplayHelpFromSource(help.getStandardHelpBroker());

        // initMenubar();
        setJMenuBar(menuBar);

        // initToolbar();
    }

    public void initialize()
    {
        final List<Image> images = IconAndFontLoader.ICONLIST_APPLICATION;
        setIconImages(images);
        setVisible(true);
        splitPaneVertical.setDividerLocation(0.7);

        // Set the preferred column width of the automaton table
        final int tableWidth = theAutomatonTable.getWidth();
        final int tableWidthEntity = tableWidth / 12;
        final TableColumnModel theTableColumnModel = theAutomatonTable.getColumnModel();

        for (int i = 0; i < theAutomatonTable.getColumnCount(); i++)
        {
            final TableColumn currColumn = theTableColumnModel.getColumn(i);

            if (i == 0)
            {
                currColumn.setPreferredWidth(tableWidthEntity * 5);
            }
            else if (i == 1)
            {
                currColumn.setPreferredWidth(tableWidthEntity * 3);
            }
            else
            {
                currColumn.setPreferredWidth(tableWidthEntity * 2);
            }
        }

        //setVisible(false);
    }

    // ** MF ** Implementation of Gui stuff
    @Override
    public void error(final String msg)
    {
        logger.error(msg);
    }

    @Override
    public void error(final String msg, final Throwable t)
    {
        logger.error(msg, t);
    }

    @Override
    public void info(final String msg)
    {
        logger.info(msg);
    }

    @Override
    public void debug(final String msg)
    {
        logger.debug(msg);
    }

    @Override
    public void clearSelection()
    {
        theAutomatonTable.clearSelection();
    }

    public void selectAutomaton(final Automaton a)
    {
        // XXX: where is my implementation dude???
    }

    @Override
    public void selectAutomata(final Collection<?> whichAutomata)
    {
        // make it a name set. reason: automata object may be _equal_ but not _same_ :(
        final Collection<String> which = new HashSet<String>();

        for (final Iterator<?> it = whichAutomata.iterator(); it.hasNext(); )
        {
            final Automaton a = (Automaton) it.next();

            which.add(a.getName());
        }

        theAutomatonTable.clearSelection();
        theAutomatonTable.getSelectedRows();
        for (int i = 0; i < theAutomatonTable.getRowCount(); i++)
        {
            try
            {
                final int orgIndex = theTableSorter.getOriginalRowIndex(i);
                final Automaton currAutomaton = getActiveProject().getAutomatonAt(orgIndex);
                final boolean should_select = which.contains(currAutomaton.getName());

                if (should_select)
                {
                    theAutomatonTable.changeSelection(i, 0, true, false);
                }
            }
            catch (final Exception ex)
            {
                logger.error("Trying to get an automaton that does not exist. Index: " + i);
                logger.debug(ex.getStackTrace());
            }
        }
    }

    /**
     * Selects the automata indicated by selectionIndices
     */
    @Override
    public void selectAutomata(final int[] selectionIndices)
    {
        // We must set the autoscrolls property false for esthetical reasons
        // but keep it unchanged after the operation for consistency
        final boolean autoscrolls = theAutomatonTable.getAutoscrolls();

        theAutomatonTable.setAutoscrolls(false);

        for (int i = 0; i < selectionIndices.length; i++)
        {
            theAutomatonTable.changeSelection(selectionIndices[i], 0, true, false);
        }

        theAutomatonTable.setAutoscrolls(autoscrolls);
    }

    /**
     * Unselects automaton indicated by automaton index
     *
     * @param index The relative index of the automaton among the other
     * selected automata. (Should be Automata.getAutomatonIndex)
     */
    @Override
    public void unselectAutomaton(final int index)
    {
        final int[] selectedRowIndices = theAutomatonTable.getSelectedRows();

        theAutomatonTable.changeSelection(selectedRowIndices[index], 0, true, false);
    }

    /**
     * Inverts the selection in theAutomatonTable.
     */
    @Override
    public void invertSelection()
    {
        // We must set the autoscrolls property false for esthetical reasons
        // but keep it unchanged after the operation for consistency
        final boolean autoscrolls = theAutomatonTable.getAutoscrolls();

        theAutomatonTable.setAutoscrolls(false);

        for (int i = 0; i < theAutomatonTable.getRowCount(); i++)
        {
            theAutomatonTable.changeSelection(i, 0, true, false);
        }

        theAutomatonTable.setAutoscrolls(autoscrolls);
    }

    @Override
    public void selectAll()
    {
        theAutomatonTable.selectAll();
    }


    @Override
    public JFrame getFrame()
    {
        return this;
    }

    @Override
    public Component getComponent()
    {
        return getFrame();
    }

    /**
     * This is a deprecated method, use getSelectedAutomata instead.
     *
     * THIS METHOD IS USED BY (AT LEAST) ActionMan AND AutomataVerificationWorker!!!
     *
     *@return  The selectedAutomataAsCollection value
     */
    @Override
    public Collection<Automaton> getSelectedAutomataAsCollection()
    {
        final int[] selectedRowIndices = theAutomatonTable.getSelectedRows();
        final LinkedList<Automaton> selectedAutomata = new LinkedList<Automaton>();

        for (int i = 0; i < selectedRowIndices.length; i++)
        {
            try
            {
                final int currIndex = selectedRowIndices[i];
                final int orgIndex = theTableSorter.getOriginalRowIndex(currIndex);
                final Automaton currAutomaton = getActiveProject().getAutomatonAt(orgIndex);

                selectedAutomata.add(currAutomaton);
            }
            catch (final Exception ex)
            {
                logger.error("Trying to get an automaton that does not exist. Index: " + i);
                logger.debug(ex.getStackTrace());
            }
        }

        return selectedAutomata;
    }

    @Override
    public Automata getSelectedAutomata()
    {
        final int[] selectedRowIndices = theAutomatonTable.getSelectedRows();
        final Automata selectedAutomata = new Automata();

        for (int i = 0; i < selectedRowIndices.length; i++)
        {
            try
            {
                final int currIndex = selectedRowIndices[i];
                final int orgIndex = theTableSorter.getOriginalRowIndex(currIndex);
                final Automaton currAutomaton = getActiveProject().getAutomatonAt(orgIndex);

                selectedAutomata.addAutomaton(currAutomaton);
            }
            catch (final Exception ex)
            {
                logger.error("Trying to get an automaton that does not exist. Index: " + i);
                logger.debug(ex.getStackTrace());
            }
        }

        return selectedAutomata;
    }

    @Override
    public Automata getUnselectedAutomata()
    {
                /* Simple... but flickery!
                   invertSelection();
                   Automata unSelectedAutomata = getSelectedAutomata();
                   invertSelection();
                   return unSelectedAutomata;
                 */
        final int[] selectedRowIndices = theAutomatonTable.getSelectedRows();
        final Automata unselectedAutomata = new Automata();
        int j = 0;

        for (int i = 0; i < theAutomatonTable.getRowCount(); i++)
        {
            if ((j >= selectedRowIndices.length) || (i != selectedRowIndices[j]))
            {
                try
                {
                    final int currIndex = i;
                    final int orgIndex = theTableSorter.getOriginalRowIndex(currIndex);
                    final Automaton currAutomaton = getActiveProject().getAutomatonAt(orgIndex);

                    unselectedAutomata.addAutomaton(currAutomaton);
                }
                catch (final Exception ex)
                {
                    logger.error("Trying to get an automaton that does not exist. Index: " + i);
                    logger.debug(ex.getStackTrace());
                }
            }
            else
            {
                j++;
            }
        }

        return unselectedAutomata;
    }

    /**
     * Same as getSelectedAutomata but include execution information
     */
    @Override
    public Project getSelectedProject()
    {
        final int[] selectedRowIndices = theAutomatonTable.getSelectedRows();
        final Project selectedProject = new Project();

        for (int i = 0; i < selectedRowIndices.length; i++)
        {
            try
            {
                final int currIndex = selectedRowIndices[i];
                final int orgIndex = theTableSorter.getOriginalRowIndex(currIndex);
                final Automaton currAutomaton = getActiveProject().getAutomatonAt(orgIndex);

                selectedProject.addAutomaton(currAutomaton);
            }
            catch (final Exception ex)
            {
                logger.error("Trying to get an automaton that does not exist. Index: " + i);
                logger.debug(ex.getStackTrace());
            }
        }

        final Project activeProject = getActiveProject();

        if (activeProject != null)
        {
            selectedProject.addAttributes(activeProject);

            //selectedProject.addActions(activeProject.getActions());
            //selectedProject.addControls(activeProject.getControls());
            //selectedProject.setAnimationURL(activeProject.getAnimationURL());
        }

        return selectedProject;
    }

    public void renameProject()
    {
        final String newName = getNewProjectName();

        if (newName != null)
        {
            getActiveProject().setName(newName);
            getActiveProject().setProjectFile(null);
        }
    }

    public void commentProject()
    {
        final String newComment = getNewProjectComment();

        if (newComment != null)
        {
            getActiveProject().setComment(newComment);

            //getActiveProject().setProjectFile(null);
        }
    }

    /**
     * Help.About action performed
     */
    public void helpAbout()
    {
      final AboutPopup popup = new AboutPopup(this);
      popup.setVisible(true);
    }

    // Overridden so we can exit when window is closed
    @Override
    protected void processWindowEvent(final WindowEvent e)
    {
        super.processWindowEvent(e);

        if (e.getID() == WindowEvent.WINDOW_CLOSING)
        {
            ActionMan.fileExit(this);
        }
    }

    public String getNewProjectName()
    {
        final String msg = "Enter new project name";
        boolean finished = false;
        final String oldName = getActiveProject().getName();
        String newName = "";

        while (!finished)
        {
            newName = JOptionPane.showInputDialog(this, msg, oldName);

            if (newName == null)
            {
                return null;
            }
            else if (newName.equals(""))
            {
                JOptionPane.showMessageDialog(this, "An empty name is not allowed", "Alert", JOptionPane.ERROR_MESSAGE);
            }
            else
            {
                finished = true;
            }
        }

        return newName;
    }

    public String getNewProjectComment()
    {
        // String msg = "Enter new project comment";
        boolean finished = false;
        final String oldComment = getActiveProject().getComment();
        String newComment = "";

        while (!finished)
        {

            // newComment = JOptionPane.showInputDialog(this, msg, oldComment);
            final EditCommentDialog dialog = new EditCommentDialog(this, oldComment);

            newComment = dialog.getComment();

            if (newComment == null)
            {
                return null;
            }
            else
            {
                finished = true;
            }
        }

        return newComment;
    }

	// This duplicates code in AnalyzerPanel, why?
    @Override
    public String getNewAutomatonName(final String msg, final String nameSuggestion)
    {
        while (true)
        {
            final String newName = (String) JOptionPane.showInputDialog(this, msg, "Enter a new name.", JOptionPane.QUESTION_MESSAGE, null, null, nameSuggestion);

            if (newName == null)
            {
                return null;
            }
            else if (newName.trim().equals(""))
            {
                JOptionPane.showMessageDialog(this, "An empty name is not allowed.", "Alert", JOptionPane.ERROR_MESSAGE);
            }
            else if (getActiveProject().containsAutomaton(newName))
            {
                JOptionPane.showMessageDialog(this, "'" + newName + "' already exists.", "Alert", JOptionPane.ERROR_MESSAGE);
            }
            else
            {
                return newName;
            }
        }
    }

    @SuppressWarnings("unused")
	private int getIntegerInDialogWindow(final String text)
    {
        boolean finished = false;
        String theInteger = "";
        int theIntValue = -1;

        while (!finished)
        {
            theInteger = JOptionPane.showInputDialog(this, text);

            try
            {
                theIntValue = Integer.parseInt(theInteger);
                finished = true;
            }
            catch (final Exception e)
            {
                JOptionPane.showMessageDialog(this, "Not a valid integer", "Alert", JOptionPane.ERROR_MESSAGE);
            }
        }

        return theIntValue;
    }

    void openFile(final File file)
    {
        openProjectXMLFile(file);
    }

    public void valueChanged(final ListSelectionEvent e)
    {
        if (!e.getValueIsAdjusting())
        {}
    }

    @Override
    public void tableChanged(final TableModelEvent e)
    {
        // logger.debug("Supremica.tableChanged");
        theAutomatonTable.revalidate();
    }

    public void openProjectXMLFile(final File file)
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
            logger.error("Error while opening " + file.getAbsolutePath() + " " + ex.getMessage());
            logger.debug(ex.getStackTrace());

            return;
        }

        final int nbrOfProjectBeforeOpening = getActiveProject().nbrOfAutomata();

        try
        {
            final int nbrOfAddedProject = addAutomata(currProject);

            logger.info("Successfully opened and added " + nbrOfAddedProject + " automata.");
        }
        catch (final Exception ex)
        {
            logger.error("Error adding automata " + file.getAbsolutePath() + " " + ex.getMessage());
            logger.debug(ex.getStackTrace());

            return;
        }

        if (nbrOfProjectBeforeOpening == 0)
        {
            final String projectName = currProject.getName();

            if (projectName != null)
            {
                getActiveProject().setName(projectName);

                //logger.debug("Project name changed to \"" + projectName + "\"");
            }
        }

        if (nbrOfProjectBeforeOpening > 0)
        {
            final File projectFile = getActiveProject().getProjectFile();

            if (projectFile != null)
            {
                getActiveProject().setProjectFile(null);
            }
        }
        else
        {
            getActiveProject().setProjectFile(file);
        }
    }

    @Override
    public VisualProjectContainer getVisualProjectContainer()
    {
        return theVisualProjectContainer;
    }

    public MainPopupMenu getMainPopupMenu()
    {
        return mainPopupMenu;
    }

    @Override
    public void addAttributes(final Project otherProject)
    {
        final Project currProject = getActiveProject();

        currProject.addAttributes(otherProject);
    }

/*
                public void addActions(Actions theActions)
                {
                                Project currProject = getActiveProject();
                                currProject.addActions(theActions);
                }

                public void addControls(Controls theControls)
                {
                                Project currProject = getActiveProject();
                                currProject.addControls(theControls);
                }

                public void setAnimationURL(URL animationURL)
                {
                                Project currProject = getActiveProject();
                                currProject.setAnimationURL(animationURL);
                }
 */

    /**
     * Adds the automata in currAutomata to the display.
     *
     * @param currAutomata the automata to be added.
     */
    @Override
    public int addAutomata(final Automata currAutomata)
    {
        assert (currAutomata.size() != 0);

        int nbrOfAddedAutomata = 0;
        for (final Automaton automaton : currAutomata)
        {
            if (addAutomaton(automaton))
            {
                nbrOfAddedAutomata++;
            }
            else
            {
                // Must have a way to say, "cancel all"?
            }
        }

        return nbrOfAddedAutomata;
    }

    @Override
    public int addProject(final Project theProject)
//		throws Exception
    {
        final int nbrOfAutomataBeforeOpening = getVisualProjectContainer().getActiveProject().nbrOfAutomata();
        final int nbrOfAddedAutomata = addAutomata(theProject);

        if (theProject != null)
        {
            addAttributes(theProject);

            //addActions(theProject.getActions());
            //addControls(theProject.getControls());
            //setAnimationURL(theProject.getAnimationURL());

                        /*
                        String animPath = theProject.getAnimationPath();
                        if (animPath != null && !animPath.equals(""))
                        {
                                        setAnimationPath(animPath);
                        }*/
        }

        if (theProject.getComment() != "")
        {
            JOptionPane.showMessageDialog(this, EncodingHelper.linebreakAdjust(theProject.getComment()), "Project information", JOptionPane.INFORMATION_MESSAGE);
        }

        if (theProject.hasAnimation())
        {
            JOptionPane.showMessageDialog(this, "This project includes an animation.", "Project information", JOptionPane.INFORMATION_MESSAGE);
        }

        if (nbrOfAutomataBeforeOpening == 0)
        {
            final String projectName = theProject.getName();
            final String projectComment = theProject.getComment();

            if (projectName != null)
            {
                getVisualProjectContainer().getActiveProject().setName(projectName);
                getVisualProjectContainer().getActiveProject().setComment(projectComment);

                //gui.info("Project name changed to \"" + projectName + "\"");
                getVisualProjectContainer().getActiveProject().updateFrameTitles();
            }
        }

        return nbrOfAddedAutomata;
    }

    /**
     * We need a single entry to add automata to the gui. Here we
     * manage all necessary user interaction
     */
    @Override
    public boolean addAutomaton(final Automaton currAutomaton)
    {
        logger.debug("Supremica::addAutomaton(" + currAutomaton.getName() + ")");

        // Force the user to enter a new name if it has no name
        //if (currAutomaton.getName() == null || currAutomaton.getName().equals(""))
        if (!currAutomaton.hasName() || currAutomaton.getName().equals(""))
        {
            final String autName = getNewAutomatonName("Enter a new name", currAutomaton.getComment());

            if (autName == null)
            {
                return false;

                // not added
            }
            else
            {
                currAutomaton.setName(autName);
            }
        }

        if (getActiveProject().containsAutomaton(currAutomaton.getName()))
        {
            final String autName = currAutomaton.getName();
            final String newName = getActiveProject().getUniqueAutomatonName(autName);

            currAutomaton.setName(newName);
            logger.info("Name conflict - '" + autName + "' does already exist. Changed name of new '" + autName + "' to '" + newName + "'.");
        }

        try
        {
            // throws Exception if the automaton already exists
            // logger.debug("Supremica.addAutomaton");
            getActiveProject().addAutomaton(currAutomaton);
        }
        catch (final Exception ex)
        {
            // should never occur, we test for this condition already
            logger.error("Error while adding: " + ex.getMessage());
            logger.debug(ex.getStackTrace());
        }

        return true;
    }

    @Override
    public void close()
    {
        setVisible(false);
        dispose();
    }

    public void destroy()
    {
        close();
    }

    @Override
    public void projectAdded(final VisualProjectContainer container, final Project theProject)
    {
        logger.info("Project added: " + theProject.getName());
    }

    @Override
    public void projectRemoved(final VisualProjectContainer container, final Project theProject)
    {
        logger.info("Project removed: " + theProject.getName());
    }

    @Override
    public void projectRenamed(final VisualProjectContainer container, final Project theProject)
    {
        logger.info("Project renamed: " + theProject.getName());
    }

    @Override
    public void updated(final Object theObject)
    {}
}
