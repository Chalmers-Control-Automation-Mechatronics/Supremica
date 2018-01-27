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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.gui.useractions.BuildObserverAction;
import org.supremica.gui.useractions.SaturateAction;
import org.supremica.gui.useractions.ScheduleAction;
import org.supremica.gui.useractions.SplitAction;
import org.supremica.gui.useractions.WorkbenchAction;
import org.supremica.properties.Config;
import org.supremica.util.SupremicaMenuItem;


class MainPopupMenu
    extends JPopupMenu
{
    private static Logger logger = LogManager.getLogger(MainPopupMenu.class);

    private static final long serialVersionUID = 1L;
    private MenuHandler menuHandler = null;

    // local utilities
    private Gui getGui()
    {
        return (Gui) getInvoker();
    }

    // except for access, these are copied straight from gui.Supremica
    private void initPopups()
    throws Exception
    {
        final JMenuItem selectAllItem = new JMenuItem("Select all");

        menuHandler.add(selectAllItem, 0);
        menuHandler.addSeparator();

        final JMenuItem statusItem = new JMenuItem("Statistics");
        statusItem.setToolTipText("Displays some statistics of the selected automata");
        menuHandler.add(statusItem, 0);

        final JMenuItem exploreItem = new JMenuItem("Explore states");
        exploreItem.setToolTipText("Explore states one by one interactively");
        menuHandler.add(exploreItem, 1);

        final JMenu viewMenu = new JMenu("View");
        menuHandler.add(viewMenu, 1);

        if (Config.DOT_USE.isTrue())
        {
            final JMenuItem viewItem = new JMenuItem("View automaton");
            viewItem.setToolTipText("Display graphical representation of the selected automata");
            //menuHandler.add(viewItem, 1);
            viewMenu.add(viewItem);

            viewItem.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(final ActionEvent e)
                {
                    ActionMan.automatonView_actionPerformed(getGui());
                    getGui().repaint();
                }
            });
        }

        final JMenuItem hierarchyItem = new JMenuItem("View modular structure");
        hierarchyItem.setToolTipText("Display graphically the connections between different modules");
        //menuHandler.add(hierarchyItem, 1);
        viewMenu.add(hierarchyItem);

        final JMenuItem alphabetItem = new JMenuItem("View alphabet");
        alphabetItem.setToolTipText("Display information about the alphabets of the selected automata");
        //menuHandler.add(alphabetItem, 1);
        viewMenu.add(alphabetItem);

        final JMenuItem statesItem = new JMenuItem("View states");
        statesItem.setToolTipText("Display information about the states of the selected automata");
        //menuHandler.add(statesItem, 1);
        viewMenu.add(statesItem);

        menuHandler.addSeparator();

        final JMenuItem synchronizeItem = new JMenuItem("Synchronize...");
        synchronizeItem.setToolTipText("Calculate the synchronous composition of the selected automata");
        menuHandler.add(synchronizeItem, 2);
        synchronizeItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(final ActionEvent e)
            {
                ActionMan.automataSynchronize_actionPerformed(getGui());
                getGui().repaint();
            }
        });

        final JMenuItem verifyItem = new JMenuItem("Verify...");
        verifyItem.setToolTipText("Verify properties");
        verifyItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(final ActionEvent e)
            {
                ActionMan.automataVerify_actionPerformed(getGui());
                getGui().repaint();
            }
        });

        final JMenuItem synthesizeItem = new SupremicaMenuItem(ActionMan.synthesizeAction);
        synthesizeItem.setToolTipText("Synthesize supervisor");

        if (Config.GENERAL_STUDENT_VERSION.isTrue())
        {
            verifyItem.setToolTipText("Verification is disabled--use the Workbench!");
            synthesizeItem.setToolTipText("Synthesis is disabled--use the Workbench!");
            menuHandler.add(verifyItem, MenuHandler.DISABLED);
            menuHandler.add(synthesizeItem, MenuHandler.DISABLED);
        }
        else
        {
            menuHandler.add(verifyItem, 1);
            menuHandler.add(synthesizeItem, 1);
        }

        final JMenuItem minimizeItem = new JMenuItem("Minimize...");
        minimizeItem.setToolTipText("Minimize automata");
        menuHandler.add(minimizeItem, 1);
        minimizeItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(final ActionEvent e)
            {
                ActionMan.automatonMinimize_actionPerformed(getGui());
                getGui().repaint();
            }
        });

        menuHandler.addSeparator();

        final JMenuItem workbench = new SupremicaMenuItem(new WorkbenchAction());
        menuHandler.add(workbench, 1);
        // JMenuItem testbench = new SupremicaMenuItem(new TestBenchAction());
        // menuHandler.add(testbench, 1);

        menuHandler.addSeparator();

        final JMenuItem purgeItem = new JMenuItem("Purge");
        purgeItem.setToolTipText("Remove all states marked as forbidden");
        menuHandler.add(purgeItem, 1);

        // These are the "standard" algorithms
        // Submenu stuff won't work here, the menuHandler concept has painted us into a corner
        // ** This has to be reworked ** Use the Action concept instead **
        // JMenu standardalgos = JMenu("Standard Algorithms");
        // menuHandler.add(standardalgos, 0);

                /* These are rarely if ever used...
                JMenuItem allAcceptingItem = new JMenuItem("Set all states as accepting");
                allAcceptingItem.setToolTipText("Make all states accepting (marked)");
                menuHandler.add(allAcceptingItem, 1);
                allAcceptingItem.addActionListener(new ActionListener()
                {
                        public void actionPerformed(ActionEvent e)
                        {
                                ActionMan.automataAllAccepting_actionPerformed(getGui());
                                getGui().repaint();
                        }
                });

                JMenuItem stateEnumerator = new JMenuItem(ActionMan.stateEnumerator);
                menuHandler.add(stateEnumerator, 1);

                JMenuItem complementItem = new JMenuItem("Automaton complement");
                complementItem.setToolTipText("Generate an automaton with complementary marked language");
                menuHandler.add(complementItem, 1);
                complementItem.addActionListener(new ActionListener()
                {
                        public void actionPerformed(ActionEvent e)
                        {
                                ActionMan.automataComplement_actionPerformed(getGui());
                                getGui().repaint();
                        }
                });

                // Do this...
                JMenuItem languageRestrictor = new SupremicaMenuItem(ActionMan.languageRestrictor);
                menuHandler.add(languageRestrictor, 1);
                 */

        // Do this...
        /*
        JMenuItem eventHider = new SupremicaMenuItem(ActionMan.eventHider);
        menuHandler.add(eventHider, 1);
         */

                /* ...and you can forget about this
                languageRestrictor.addActionListener(new ActionListener()
                {
                                public void actionPerformed(ActionEvent e)
                                {
                                                ActionMan.languageRestrictor_actionPerformed(getGui());
                                                getGui().repaint();
                                }
                });*/

                /*
                JMenuItem interfaceItem = new JMenuItem("Interface Properties...");
                menuHandler.add(interfaceItem, 1);
                menuHandler.addSeparator();
                interfaceItem.addActionListener(new ActionListener()
                {
                        public void actionPerformed(ActionEvent e)
                        {
                                ActionMan.automatonUpdateInterface_actionPerformed(getGui());
                                getGui().repaint();
                        }
                });
                 */

        if (Config.INCLUDE_BOUNDED_UNCON_TOOLS.isTrue())
        {
            final JMenuItem extendItem = new JMenuItem("Extend");
            menuHandler.add(extendItem, 1);
            extendItem.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(final ActionEvent e)
                {
                    ActionMan.automataExtend_actionPerformed(getGui());
                    getGui().repaint();
                }
            });

            final JMenuItem liftingItem = new JMenuItem("Compute lifting automaton");
            menuHandler.add(liftingItem, 1);
            liftingItem.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(final ActionEvent e)
                {
                    ActionMan.automataLifting_actionPerformed(getGui());
                    getGui().repaint();
                }
            });

            final JMenuItem removePassItem = new JMenuItem("Remove pass events");
            menuHandler.add(removePassItem, 1);
            removePassItem.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(final ActionEvent e)
                {
                    ActionMan.automataRemovePass_actionPerformed(getGui());
                    getGui().repaint();
                }
            });

            final JMenuItem addSelfLoopArcsItem = new JMenuItem("Add self-loop arcs");
            addSelfLoopArcsItem.setToolTipText("Add self loops so that each state has the whole alphabet elabled");
            menuHandler.add(addSelfLoopArcsItem, 1);
            addSelfLoopArcsItem.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(final ActionEvent e)
                {
                    ActionMan.automataAddSelfLoopArcs_actionPerformed(getGui());
                    getGui().repaint();
                }
            });

            final JMenuItem removeSelfLoopArcsItem = new JMenuItem("Remove self-loop arcs");
            removeSelfLoopArcsItem.setToolTipText("Remove all self-loops");
            menuHandler.add(removeSelfLoopArcsItem, 1);
            removeSelfLoopArcsItem.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(final ActionEvent e)
                {
                    ActionMan.automataRemoveSelfLoopArcs_actionPerformed(getGui());
                    getGui().repaint();
                }
            });

            final JMenuItem normalizeAlphabetItem = new JMenuItem("Normalize alphabet");
            menuHandler.add(normalizeAlphabetItem, 1);
            menuHandler.addSeparator();
            normalizeAlphabetItem.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(final ActionEvent e)
                {
                    ActionMan.normalizeAlphabet_actionPerformed(getGui());
                    getGui().repaint();
                }
            });
        }

        final JMenuItem alphabetAnalyzerItem = new JMenuItem("Analyze alphabets");
        menuHandler.add(alphabetAnalyzerItem, 2);
        alphabetAnalyzerItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(final ActionEvent e)
            {
                ActionMan.alphabetAnalyzer_actionPerformed(getGui());
                getGui().repaint();
            }
        });

        //-- MF Find States --
        final JMenuItem findStatesItem = new JMenuItem(ActionMan.findStates);

        menuHandler.add(findStatesItem, 1);

/*              findStatesItem.addActionListener(new ActionListener()
                                {

                                                // anonymous class (is this a good thing?)
                                                public void actionPerformed(ActionEvent e)
                                                {
                                                                ActionMan.findStates_action(getGui());
                                                                getGui().repaint();
                                                }
                                });
 */
        menuHandler.addSeparator();

        // ----------------------------------------------
        final JMenuItem copyItem = new JMenuItem("Copy");

        menuHandler.add(copyItem, 1);

        final JMenuItem deleteItem = new JMenuItem("Delete");

        menuHandler.add(deleteItem, 1);

        final JMenuItem deleteAllItem = new JMenuItem("Delete all");

        menuHandler.add(deleteAllItem, 0);

        //JMenuItem cropItem = new JMenuItem("Crop to selection");
        final JMenuItem cropItem = new JMenuItem("Delete unselected");

        menuHandler.add(cropItem, 0);

        final JMenuItem invertItem = new JMenuItem("Invert selection");

        menuHandler.add(invertItem, 0);

        final JMenuItem renameItem = new JMenuItem("Rename");

        menuHandler.add(renameItem, 1);
        menuHandler.addSeparator();

        // JMenuItem saveAsItem = new JMenuItem("Save As...");
        // menuHandler.add(saveAsItem, 1);
        if (Config.FILE_ALLOW_EXPORT.isTrue())
        {
            // This is how it would be done with an export command object
            // JMenuItem exportItem = new SupremicaMenuItem(ActionMan.exportItem);
            // menuHandler.add(exportItem, 1);
            final JMenuItem exportItem = new JMenuItem("Export...");

            menuHandler.add(exportItem, 1);
            exportItem.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(final ActionEvent e)
                {
                    ActionMan.automataExport(getGui());
                    getGui().repaint();
                }
            });
        }

        // --------------------------------------------------------------
        // ***************** UNDER DEVELOPMENT MENUES ARE ADDED HERE:
        if (Config.INCLUDE_EXPERIMENTAL_ALGORITHMS.isTrue())
        {
            final JMenu expMenu = new JMenu("Experimental algorithms");
            menuHandler.add(expMenu, 1);

            expMenu.add(new SupremicaMenuItem(new SaturateAction()));
            expMenu.add(new SupremicaMenuItem(new BuildObserverAction()));
            expMenu.add(new SupremicaMenuItem(new SplitAction()));

            // Test
            final JMenuItem testItem = new JMenuItem("Test");
            testItem.setToolTipText("Experimental method without a name yet");
            testItem.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(final ActionEvent e)
                {
                    ActionMan.testMethod(getGui());
                }
            });
            expMenu.add(testItem);

            expMenu.addSeparator();

            expMenu.add(new SupremicaMenuItem(new ScheduleAction()));

            JMenuItem mMd, mMmc, predictCompositionSize;

            expMenu.addSeparator();
            expMenu.add(mMd = new JMenuItem("Select dependency set"));
            expMenu.add(mMmc = new JMenuItem("Select maximal component"));
            expMenu.add(predictCompositionSize = new JMenuItem("Predict composition size"));
            mMd.setToolTipText("Select the automata that share events with the currently selected automata");
            mMd.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(final ActionEvent e)
                {
                    ActionMan.markDependencySet();
                }
            });
            mMmc.setToolTipText("Selects all automata that are directly or indirectly connected to the selected automata");
            mMmc.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(final ActionEvent e)
                {
                    ActionMan.markMaximalComponent();
                }
            });
            predictCompositionSize.setToolTipText("Predicts the size of the composition of two selected automata");
            predictCompositionSize.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(final ActionEvent e)
                {
                    ActionMan.evoCompPredictSize();
                }
            });
            expMenu.addSeparator();
        }

        // ------------------------------------------------------------------
        selectAllItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(final ActionEvent e)
            {
                ActionMan.selectAll_actionPerformed(getGui());
            }
        });
        statusItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(final ActionEvent e)
            {
                ActionMan.automatonStatus_actionPerformed(getGui());
            }
        });
        exploreItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(final ActionEvent e)
            {
                ActionMan.automatonExplore_actionPerformed(getGui());
                getGui().repaint();
            }
        });
        hierarchyItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(final ActionEvent e)
            {
                ActionMan.hierarchyView_actionPerformed(getGui());
                getGui().repaint();
            }
        });
        alphabetItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(final ActionEvent e)
            {
                // ActionMan.automatonAlphabet_actionPerformed(getGui());
                ActionMan.alphabetView_actionPerformed(getGui());
                getGui().repaint();
            }
        });
        statesItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(final ActionEvent e)
            {
                ActionMan.statesView_actionPerformed(getGui());
                getGui().repaint();
            }
        });
        purgeItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(final ActionEvent e)
            {
                ActionMan.automataPurge_actionPerformed(getGui());
                getGui().repaint();
            }
        });
        copyItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(final ActionEvent e)
            {
                ActionMan.automataCopy_actionPerformed(getGui());
                getGui().repaint();
            }
        });
        deleteItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(final ActionEvent e)
            {
                ActionMan.automataDelete_actionPerformed(getGui());
                getGui().repaint();
            }
        });
        deleteAllItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(final ActionEvent e)
            {
                ActionMan.automataDeleteAll_actionPerformed(getGui());
                getGui().repaint();
            }
        });
        cropItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(final ActionEvent e)
            {
                ActionMan.automataCrop_actionPerformed(getGui());
                ActionMan.selectAll_actionPerformed(getGui());
                getGui().repaint();
            }
        });
        invertItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(final ActionEvent e)
            {
                ActionMan.automataInvert_actionPerformed(getGui());
                getGui().repaint();
            }
        });
        renameItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(final ActionEvent e)
            {
                ActionMan.automataRename_actionPerformed(getGui());
                getGui().repaint();
            }
        });

        // Here is revelaed that this one knows that the interface is built around a table. Not good!
        // It should popup when ordered so by the gui, not de ide for itself when to
        // It should have no notion of rows/cols, these things it should get from the gui
    }

    public void show(final int num_selected, final Component c, final int x, final int y)
    {
        final JPopupMenu regionPopup = menuHandler.getDisabledPopupMenu(num_selected);

        regionPopup.show(c, x, y);
    }

    public MainPopupMenu(final Gui gui)
    {
        setInvoker(gui.getFrame());

        // Ugly fixx, "temporary"    :o)
        ActionMan.gui = gui;
        menuHandler = new MenuHandler();

        try
        {
            initPopups();
        }
        catch (final Exception ex)
        {
            logger.error(ex);
        }
    }
}
