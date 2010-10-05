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


public class AnalyzerPanel
    extends MainPanel
{
    private static final long serialVersionUID = 1L;

    private final JTabbedPane tabPanel;
    private final JComponent automatonViewerPanel;
    private final AnalyzerAutomataPanel automataPanel;

    private final DocumentContainer mDocumentContainer;

    public AnalyzerPanel(DocumentContainer moduleContainer, String name)
    {
        super(name);
        setPreferredSize(IDEDimensions.mainPanelPreferredSize);
        setMinimumSize(IDEDimensions.mainPanelMinimumSize);

        mDocumentContainer = moduleContainer;

        tabPanel = new JTabbedPane(JTabbedPane.BOTTOM);
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

			public void actionPerformed(ActionEvent e)
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
    public boolean updateAutomata()
      throws EvalException
    {
      if (mDocumentContainer instanceof ModuleContainer) {
        final ModuleContainer moduleContainer =
          (ModuleContainer) mDocumentContainer;
        final ProductDESProxy des = moduleContainer.getCompiledDES();
        final DocumentManager manager =
          mDocumentContainer.getIDE().getDocumentManager();
        final ProjectBuildFromWaters builder =
          new ProjectBuildFromWaters(manager);
        final Project supremicaProject = builder.build(des);
        addProject(supremicaProject);
        mVisualProject.updated();
        /*
          if (Config.GUI_ANALYZER_AUTOMATONVIEWER_USE_CONTROLLED_SURFACE.isTrue())
          {
            ProductDESImporter importer = new ProductDESImporter(ModuleSubjectFactory.getInstance());
            ModuleSubject flatModule = (ModuleSubject) importer.importModule(mVisualProject);
            flatModuleContainer = new ModuleContainer(getIDE(), flatModule);
          }
        */
      }
      return true;
    }

    public VisualProject getVisualProject()
    {
        return mVisualProject;
    }

    public Actions getActions()
    {
        return mDocumentContainer.getIDE().getActions();
    }

    public String getNewAutomatonName(String msg, String nameSuggestion)
    {
        boolean finished = false;
        String newName = "";

        while (!finished)
        {
            newName = (String) JOptionPane.showInputDialog(this, msg, "Enter a new name.", JOptionPane.QUESTION_MESSAGE, null, null, nameSuggestion);

            if (newName == null)
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
                finished = true;
            }
        }

        return newName;
    }


    public int numberOfSelectedAutomata()
    {
        return getSelectedAutomata().size();
    }

    public boolean addAutomaton(Automaton theAutomaton)
    {
        int size = mVisualProject.nbrOfAutomata();
        mVisualProject.addAutomaton(theAutomaton);
        return mVisualProject.nbrOfAutomata() > size;
    }

    public int addAutomata(Automata theAutomata)
    {
        int size = mVisualProject.nbrOfAutomata();
        mVisualProject.addAutomata(theAutomata);
        return mVisualProject.nbrOfAutomata() - size;
    }

    public int addProject(Project project)
    {       
        mVisualProject.clear();
        mVisualProject.addAutomata(project);        
        mVisualProject.addAttributes(project);
        return project.size();
    }

    public void sortAutomataByName()
    {
        automataPanel.sortAutomataByName();
    }

    private final VisualProject mVisualProject = new VisualProject();
}
