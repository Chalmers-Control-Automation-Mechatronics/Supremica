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

package org.supremica.gui.ide;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.marshaller.DocumentManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.automata.Project;
import org.supremica.automata.IO.ProjectBuildFromWaters;
import org.supremica.gui.VisualProject;
import org.supremica.gui.ide.actions.Actions;
import org.supremica.properties.Config;


public class SupremicaAnalyzerPanel
    extends MainPanel
{
    private static final long serialVersionUID = 1L;

    private final JComponent automatonViewerPanel;
    private final AnalyzerAutomataPanel automataPanel;

    private final DocumentContainer mDocumentContainer;
    private final VisualProject mVisualProject = new VisualProject();

    public SupremicaAnalyzerPanel(final DocumentContainer moduleContainer, final String name)
    {
        super(name);

        mDocumentContainer = moduleContainer;

        automataPanel = new AnalyzerAutomataPanel(this, moduleContainer);
        automatonViewerPanel = getEmptyRightPanel();
		setLeftComponent(automataPanel);
		setRightComponent(automatonViewerPanel);

        // Add CTRL-A as a "Select All" action
        this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK), "SelectAll");
        this.getActionMap().put("SelectAll",
            new AbstractAction()
        {
			private static final long serialVersionUID = 1L;

			@Override
            public void actionPerformed(final ActionEvent e)
            {
                automataPanel.selectAllAutomata();
            }
        });
    }


    //#######################################################################
    //# Overrides for org.supremica.gui.ide.MainPanel
    @Override
    public boolean isWatersPanel()
    {
      return false;
    }

    @Override
    public void createPanelSpecificMenus(final IDEMenuBar menuBar)
    {
      menuBar.createSupremicaAnalyzeMenu();
      if (mVisualProject.hasAnimation()) {
        menuBar.createSupremicaToolsMenu();
      }
    }


    //#######################################################################
    //# Simple Access
    /**
     * Gets the selected automata. Returns a new list of the selected automata, changes to the selection
	 * are not reflected into this list after it has been returned. This is very contrary to the below
	 * getAllAutomata, which does NOT return a new list, but simply a reference to the VisualProject
     */
    public Automata getSelectedAutomata()
    {
        return automataPanel.getSelectedAutomata();
    }

/*
        public Project getSelectedProject()
        {
                return automataPanel.getSelectedProject();
        }
 */

    public Automata getUnselectedAutomata()
    {
        return automataPanel.getUnselectedAutomata();
    }

	/*
	 * This does NOT give you a list of all automata currently in the panel,
	 * It gives you a reference to the visualProject. So for instance, the
	 * number of automata before and after adding one or more CANNOT be determined
	 * from storing the Automata returned from this method (took me three days of
	 * bughunting to find out!) -- MF
	*/
    public Automata getAllAutomata()
    {
        return automataPanel.getAllAutomata();
    }

    /**
     * Updates the automata in the analyzer-tab.
     */
    public void updateAutomata(final ProductDESProxy des)
      throws EvalException
    {
      final IDE ide = mDocumentContainer.getIDE();
      final DocumentManager manager = ide.getDocumentManager();
      final ProjectBuildFromWaters builder =
        new ProjectBuildFromWaters(manager);
      builder.setIncludesProperties
        (Config.GUI_ANALYZER_SEND_PROPERTIES_TO_ANALYZER.isTrue());
      final Project supremicaProject = builder.build(des);
      final List<String> warnings = builder.getWarnings();
      if (!warnings.isEmpty()) {
        final Logger logger = LogManager.getLogger();
        for (final String warning : warnings) {
          logger.warn(warning);
        }
      }
      addProject(supremicaProject);
      mVisualProject.updated();
      automataPanel.sortAutomataByName();
      /*
        if (Config.GUI_ANALYZER_AUTOMATONVIEWER_USE_CONTROLLED_SURFACE.isTrue())
        {
          ProductDESImporter importer = new ProductDESImporter(ModuleSubjectFactory.getInstance());
          ModuleSubject flatModule = (ModuleSubject) importer.importModule(mVisualProject);
          flatModuleContainer = new ModuleContainer(getIDE(), flatModule);
        }
      */
    }

    public VisualProject getVisualProject()
    {
        return mVisualProject;
    }

    public Actions getActions()
    {
        return mDocumentContainer.getIDE().getActions();
    }

	// This duplicates code in Supremica (and Supremica implements it from Gui)
    public String getNewAutomatonName(final String msg, final String nameSuggestion)
    {
        while (true)
        {
            final String newName = (String) JOptionPane.showInputDialog(this, msg, "Enter a new name", JOptionPane.QUESTION_MESSAGE, null, null, nameSuggestion);

            if (newName == null)	// user cancelled
            {
                return null;
            }
            else if (newName.equals(""))
            {
                JOptionPane.showMessageDialog(this, "An empty name is not allowed.", "Alert", JOptionPane.ERROR_MESSAGE);
            }
            else if (getVisualProject().containsAutomaton(newName))
            {
                JOptionPane.showMessageDialog(this, "'" + newName + "' already exists.", "Alert", JOptionPane.ERROR_MESSAGE);
            }
            else
            {
				return newName;
            }
        }
    }


    public int numberOfSelectedAutomata()
    {
        return getSelectedAutomata().size();
    }

    public boolean addAutomaton(final Automaton theAutomaton)
    {
		/*** This is just an awkward way to implement the exact same things as...
        final int size = mVisualProject.nbrOfAutomata();
        mVisualProject.addAutomaton(theAutomaton);
        return mVisualProject.nbrOfAutomata() > size;
		**********************************************/// this:
		return mVisualProject.addAutomaton(theAutomaton, true);
    }

    public int addAutomata(final Automata theAutomata)
    {
		return addAutomata(theAutomata, false);
	}

	public int addAutomata(final Automata theAutomata, final boolean sanityCheck)
	{
        final int size = mVisualProject.nbrOfAutomata();
		if(sanityCheck == false) // just do it the old unsafe way, this fails ungracefully if adding automata named the same as an existing one
		{
			mVisualProject.addAutomata(theAutomata);
			return mVisualProject.nbrOfAutomata() - size;
		}

		// Here we do it the nice and graceful way
		for(final Automaton aut : theAutomata)
		{
			while(mVisualProject.addAutomaton(aut) == false)
			{
				final String name = getNewAutomatonName("Automaton exists: " + aut.getName(), aut.getComment());
				if(name == null) // then the user cancelled
					break;	// handle the next one

				aut.setName(name);
			}
		}
		return mVisualProject.nbrOfAutomata() - size;	// number of added automata
    }

    public int addProject(final Project project)
    {
        mVisualProject.clear();
        mVisualProject.addAutomata(project);
        mVisualProject.addAttributes(project);
        return project.size();
    }


}
