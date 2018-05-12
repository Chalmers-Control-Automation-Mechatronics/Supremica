//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 1999-2018 Knut Akesson, Martin Fabian, Robi Malik
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

import java.awt.Frame;

import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.gui.ide.actions.IDEActionInterface;
import org.supremica.properties.Config;


class AnalyzerPopupMenu
    extends JPopupMenu
{
    private static Logger logger = LogManager.getLogger(AnalyzerPopupMenu.class);

    private static final long serialVersionUID = 1L;
    private final IDEActionInterface ide;

    public AnalyzerPopupMenu(final Frame parent, final IDEActionInterface ide)
    {
        setInvoker(parent);
        this.ide = ide;

        try
        {
            initPopups();
        }
        catch (final Exception ex)
        {
            logger.error(ex);
        }
    }

    private void initPopups()
    throws Exception
    {
        final JMenu viewMenu = new JMenu("View");
        viewMenu.setToolTipText("Different graphical representations of the selected automata");
        viewMenu.setIcon(new ImageIcon(IDE.class.getResource("/toolbarButtonGraphics/general/Zoom16.gif")));
        add(viewMenu);
        viewMenu.add(ide.getActions().analyzerViewAutomatonAction.getMenuItem());
        viewMenu.add(ide.getActions().analyzerViewAlphabetAction.getMenuItem());
        viewMenu.add(ide.getActions().analyzerViewStatesAction.getMenuItem());
        viewMenu.add(ide.getActions().analyzerViewModularStructureAction.getMenuItem());
        addSeparator();
        add(ide.getActions().analyzerSynchronizerAction.getMenuItem());
        add(ide.getActions().analyzerSynthesizerAction.getMenuItem());
        add(ide.getActions().analyzerVerifierAction.getMenuItem());
        add(ide.getActions().analyzerMinimizeAction.getMenuItem());
        addSeparator();
        add(ide.getActions().analyzerPlantifyAction.getMenuItem());
        add(ide.getActions().analyzerEventHiderAction.getMenuItem());
        add(ide.getActions().analyzerPurgeAction.getMenuItem());
        add(ide.getActions().analyzerEnumerateAction.getMenuItem());
        addSeparator();
        add(ide.getActions().analyzerExploreStatesAction.getMenuItem());
        add(ide.getActions().analyzerFindStatesAction.getMenuItem());
        add(ide.getActions().analyzerWorkbenchAction.getMenuItem());
        addSeparator();
        add(ide.getActions().analyzerStatisticsAction.getMenuItem());
        add(ide.getActions().analyzerExportAction.getMenuItem());
        addSeparator();
        //add(ide.getActions().analyzerDeleteSelectedAction.getMenuItem());
        //add(ide.getActions().analyzerDeleteAllAction.getMenuItem());
        add(ide.getActions().analyzerRenameAction.getMenuItem());
        add(ide.getActions().analyzerSendToEditorAction.getMenuItem());

        if (Config.INCLUDE_EXPERIMENTAL_ALGORITHMS.get())
        {
            final JMenu experimentMenu = new JMenu("Experimental");
            experimentMenu.setToolTipText("Experimental functions (under development)");
            experimentMenu.setIcon(new ImageIcon(IDE.class.getResource("/toolbarButtonGraphics/development/Jar16.gif")));
            add(experimentMenu);

            experimentMenu.add(ide.getActions().analyzerPredictSizeAction.getMenuItem());
            experimentMenu.add(ide.getActions().analyzerScheduleAction.getMenuItem());

            experimentMenu.addSeparator();

            experimentMenu.add(ide.getActions().analyzerDeadEventsDetectorAction.getMenuItem());

            experimentMenu.addSeparator();

            experimentMenu.add(ide.getActions().simulatorLaunchAnimatorAction.getMenuItem());
            experimentMenu.add(ide.getActions().simulatorLaunchSimulatorAction.getMenuItem());

            experimentMenu.addSeparator();
            experimentMenu.add(ide.getActions().analyzerSMVAction.getMenuItem());


            experimentMenu.add(ide.getActions().analyzerSatAction.getMenuItem());

            experimentMenu.addSeparator();

            experimentMenu.add(ide.getActions().analyzerModularForbidder.getMenuItem());
            experimentMenu.addSeparator();

            // To try out new code, use this action...
            experimentMenu.add(ide.getActions().analyzerExperimentAction.getMenuItem());
        }
    }
}
