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

package org.supremica.gui.ide;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;

import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.gui.TableSorter;
import org.supremica.gui.VisualProject;
import org.supremica.gui.WhiteScrollPane;
import org.supremica.properties.Config;


public class AnalyzerAutomataPanel
    extends WhiteScrollPane
    implements TableModelListener
{
    private static final long serialVersionUID = 1L;

    private static Logger logger = LogManager.getLogger(AnalyzerAutomataPanel.class);

    private final SupremicaAnalyzerPanel analyzerPanel;
    private final DocumentContainer moduleContainer;
    private JTable theAutomatonTable;
    private TableSorter theTableSorter;
    private TableModel analyzerTableModel;

    public static int TABLE_NAME_COLUMN = 0;
    public static int TABLE_TYPE_COLUMN = 1;
    public static int TABLE_STATES_COLUMN = 2;
    public static int TABLE_EVENTS_COLUMN = 3;
    public static int TABLE_TRANSITIONS_COLUMN = 4;

    private static final float PREFERRED_WIDTH = 0.4f;

    AnalyzerAutomataPanel(final SupremicaAnalyzerPanel analyzerPanel, final DocumentContainer moduleContainer)
    {
      this.analyzerPanel = analyzerPanel;
      this.moduleContainer = moduleContainer;
      initialize();
      final int width =
        Math.round(PREFERRED_WIDTH * Config.GUI_IDE_WIDTH.getValue());
      final Dimension size = new Dimension(width, 0);
      setPreferredSize(size);
    }
    private void initialize()
    {
        analyzerTableModel = getActiveProject().getAnalyzerTableModel();
        theTableSorter = new TableSorter(analyzerTableModel);
        theAutomatonTable = new JTable(theTableSorter);
        final Font font = theAutomatonTable.getFont();
        final int height = (int) Math.ceil(1.5f * font.getSize2D());
        theAutomatonTable.setRowHeight(height);
        theAutomatonTable.setTableHeader(new JTableHeader(theAutomatonTable.getColumnModel())
        {
			private static final long serialVersionUID = 1L;

			@Override
      public String getToolTipText(final MouseEvent e)
            {
                final int i = columnAtPoint(e.getPoint());
                if (i == TABLE_NAME_COLUMN)
                {
                    return "Sort on name";
                }
                else if (i == TABLE_TYPE_COLUMN)
                {
                    return "Sort on type";
                }
                else if (i == TABLE_STATES_COLUMN)
                {
                    return "Sort on number of states";
                }
                else if (i == TABLE_EVENTS_COLUMN)
                {
                    return "Sort on number of events";
                }
                else if (i == TABLE_TRANSITIONS_COLUMN)
                {
                    return "Sort on number of transitions";
                }
                else
                {
                    return null;
                }
            }
        });

        theAutomatonTable.getTableHeader().setReorderingAllowed(false);

        getViewport().add(theAutomatonTable);

        theTableSorter.addMouseListenerToHeaderInTable(theAutomatonTable);

        analyzerTableModel.addTableModelListener(this);
        theAutomatonTable.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyPressed(final KeyEvent e)
            {
                if (e.getKeyCode() == KeyEvent.VK_DELETE)
                {
//					ActionMan.automataDelete_actionPerformed(getGui());
                }
            }

            @Override
            public void keyReleased(final KeyEvent e)
            {}

            @Override
            public void keyTyped(final KeyEvent e)
            {}
        });

        // Set the preferred column width of the automaton table
        //int tableWidth = theAutomatonTable.getWidth();
        final int tableWidth = getWidth()+220; // getWidth() returns 0?
        final int tableWidthUnit = tableWidth / 11;
        final TableColumnModel theTableColumnModel = theAutomatonTable.getColumnModel();
        for (int i = 0; i < theAutomatonTable.getColumnCount(); i++)
        {
            //System.out.println(tableWidth + " " + i);
            final TableColumn currColumn = theTableColumnModel.getColumn(i);

            if (i == TABLE_NAME_COLUMN)
            {
                currColumn.setPreferredWidth(tableWidthUnit * 5);
            }
            else if (i == TABLE_TYPE_COLUMN)
            {
                currColumn.setPreferredWidth(tableWidthUnit * 3);
            }
            else
            {
                currColumn.setPreferredWidth(tableWidthUnit * 1);
            }
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

                    // Show automaton in the panel
                    if (col == TABLE_NAME_COLUMN)
                    {
                        final Automata selectedAutomata = getSelectedAutomata();

                        if (selectedAutomata.size() >= 2)
                        {
                            //moduleContainer.getVisualProject();

                            for (final Iterator<Automaton> autIt = selectedAutomata.iterator(); autIt.hasNext();)
                            {
                                final Automaton currAutomaton = autIt.next();

                                try
                                {
                                    analyzerPanel.getVisualProject().getAutomatonViewer(currAutomaton.getName());
                                }
                                catch (final Exception ex)
                                {
                                    logger.error("Exception in AutomatonViewer. Automaton: " + currAutomaton, ex);
                                    return;
                                }
                            }
                        }
                        else if (selectedAutomata.size() == 1)
                        {
                            if (!Config.GUI_ANALYZER_AUTOMATON_VIEWER_USE_CONTROLLED_SURFACE.getValue())
                            {
                                final Automaton selectedAutomaton = selectedAutomata.getFirstAutomaton();
                                final AnalyzerAutomatonViewerPanel automatonPanel = new AnalyzerAutomatonViewerPanel("Dot View", selectedAutomaton);
                                analyzerPanel.setRightComponent(automatonPanel);
                            }
                            /*
                            else
                            {
                                Automaton selectedAutomaton = selectedAutomata.getFirstAutomaton();
                                ModuleContainer flatModuleContainer = moduleContainer.getFlatModuleContainer();

                                //GraphProxy currGraphProxy = moduleContainer.getFlatGraphProxy(selectedAutomaton.getName());
                                //ModuleProxy currModuleProxy = moduleContainer.getFlatModuleProxy();
//                                                                if (currGraphProxy == null)
//                                                                {
//                                                                        logger.error("AnalyzerAutomataPanel.currGraphProxy == null");
//                                                                        return;
//                                                                }
//                                                                if (currModuleProxy == null)
//                                                                {
//                                                                        logger.error("AnalyzerAutomataPanel.currModuleProxy == null");
//                                                                        return;
//                                                                }

                                //boolean isSubject = currGraphProxy instanceof GraphSubject;
                                //logger.info("isGraphSubject: " + isSubject);
                                //isSubject = currModuleProxy instanceof ModuleSubject;
                                //logger.info("isModuleSubject: " + isSubject);

                                try
                                {
                                    //EditorSurface surface = new EditorSurface((GraphSubject)currGraphProxy, (ModuleSubject)currModuleProxy, new SubjectShapeProducer((GraphSubject)currGraphProxy, currModuleProxy));
                                    //ControlledSurface surface = new ControlledSurface((GraphSubject)currGraphProxy, (ModuleSubject)currModuleProxy);

                                    ComponentViewPanel componentView = flatModuleContainer.getComponentViewPanel(selectedAutomaton.getName());

                                    analyzerPanel.setRightComponent(componentView);
                                }
                                catch (Exception ex)
                                {
                                    logger.error(ex);
                                    return;
                                }

                            }
                             */
                        }
                        else
                        {
                            return;
                        }

//						ActionMan.automatonView_actionPerformed(getGui());
//						getGui().repaint();
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
		    final Frame root =
		      (Frame) analyzerPanel.getTopLevelAncestor();
		    final JPopupMenu popup =
		      new AnalyzerPopupMenu(root, moduleContainer.getIDE());
                    popup.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });

        // --------------------------------------//
    }

    private VisualProject getActiveProject()
    {
        return analyzerPanel.getVisualProject();
    }

    public void valueChanged(final ListSelectionEvent e)
    {
        if (!e.getValueIsAdjusting())
        {}
    }

    @Override
    public void tableChanged(final TableModelEvent e)
    {
        //theAutomatonTable.revalidate();
    }

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

    /**
     * Same as getSelectedAutomata but include execution information
     */
     /*
    public Project getSelectedProject()
    {
        int[] selectedRowIndices = theAutomatonTable.getSelectedRows();
        Project selectedProject = new Project();

        for (int i = 0; i < selectedRowIndices.length; i++)
        {
            try
            {
                int currIndex = selectedRowIndices[i];
                int orgIndex = theTableSorter.getOriginalRowIndex(currIndex);
                Automaton currAutomaton = getActiveProject().getAutomatonAt(orgIndex);

                selectedProject.addAutomaton(currAutomaton);
            }
            catch (Exception ex)
            {
                logger.error("Trying to get an automaton that does not exist. Index: " + i);
                logger.debug(ex.getStackTrace());
            }
        }

        Project activeProject = getActiveProject();

        if (activeProject != null)
        {
            selectedProject.addAttributes(activeProject);

            //selectedProject.addActions(activeProject.getActions());
            //selectedProject.addControls(activeProject.getControls());
            //selectedProject.setAnimationURL(activeProject.getAnimationURL());
        }

        return selectedProject;
    }*/

    public Automata getAllAutomata()
    {
        return getActiveProject();
    }


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

    public void selectAllAutomata()
    {
        theAutomatonTable.selectAll();
    }

    public void sortAutomataByName()
    {
        theTableSorter.sortByColumn(TABLE_NAME_COLUMN);
    }
}
