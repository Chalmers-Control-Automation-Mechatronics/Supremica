//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: org.supremica.gui.ide
//# CLASS:   AnalyzerPanel
//###########################################################################
//# $Id$
//###########################################################################

package org.supremica.gui.ide;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;

import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.marshaller.DocumentManager;

import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.automata.Project;
import org.supremica.automata.IO.ProjectBuildFromWaters;
import org.supremica.gui.VisualProject;
import org.supremica.gui.ide.actions.Actions;
import org.supremica.properties.Config;


public class AnalyzerPanel
    extends MainPanel
{
    private static final long serialVersionUID = 1L;

    private final JTabbedPane tabPanel;
    private final JComponent automatonViewerPanel;
    private final AnalyzerAutomataPanel automataPanel;

    private final DocumentContainer mDocumentContainer;

    public AnalyzerPanel(final DocumentContainer moduleContainer, final String name)
    {
        super(name);
        setPreferredSize(IDEDimensions.mainPanelPreferredSize);
        setMinimumSize(IDEDimensions.mainPanelMinimumSize);

        mDocumentContainer = moduleContainer;

        tabPanel = new JTabbedPane();
        tabPanel.setPreferredSize(IDEDimensions.leftAnalyzerPreferredSize);
        tabPanel.setMinimumSize(IDEDimensions.leftAnalyzerMinimumSize);

        automataPanel = new AnalyzerAutomataPanel(this, moduleContainer, "All");
        automataPanel.setPreferredSize(IDEDimensions.leftAnalyzerPreferredSize);
        automataPanel.setMinimumSize(IDEDimensions.leftAnalyzerMinimumSize);
        tabPanel.add(automataPanel);

        tabPanel.setSelectedComponent(automataPanel);

        automatonViewerPanel = getEmptyRightPanel();
		setLeftComponent(tabPanel);
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

    /**
     * Gets the selected automata.
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
        for (final String warning : warnings) {
          ide.warn(warning);
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
        final int size = mVisualProject.nbrOfAutomata();
        mVisualProject.addAutomaton(theAutomaton);
        return mVisualProject.nbrOfAutomata() > size;
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

    private final VisualProject mVisualProject = new VisualProject();
}
