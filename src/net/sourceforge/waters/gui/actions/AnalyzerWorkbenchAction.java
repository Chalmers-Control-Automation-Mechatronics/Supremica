//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2020 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

package net.sourceforge.waters.gui.actions;

import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.List;

import javax.swing.Action;

import net.sourceforge.waters.gui.analyzer.AutomataTable;
import net.sourceforge.waters.gui.analyzer.AutomataTableModel;
import net.sourceforge.waters.gui.analyzer.WatersAnalyzerPanel;
import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.model.analysis.des.AbstractConflictChecker;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.AutomatonTools;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.automata.Automata;
import org.supremica.automata.AutomataListener;
import org.supremica.automata.Automaton;
import org.supremica.automata.Project;
import org.supremica.automata.IO.AutomataToWaters;
import org.supremica.automata.IO.ProjectBuildFromWaters;
import org.supremica.gui.VisualProject;
import org.supremica.gui.ide.IDE;
import org.supremica.workbench.Workbench;


/**
 * The action to invoke the Workbench dialog in the Waters analyser.
 *
 * @author Benjamin Wheeler
 */

public class AnalyzerWorkbenchAction extends WatersAnalyzerAction
{
  //#########################################################################
  //# Constructor
  protected AnalyzerWorkbenchAction(final IDE ide)
  {
    super(ide);
    putValue(Action.NAME, "Workbench...");
    //putValue(Action.SMALL_ICON, IconAndFontLoader.ICON_ANALYZER_SYNTH);
    //putValue(Action.MNEMONIC_KEY, KeyEvent.VK_Y);
    //putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Y, ActionEvent.ALT_MASK));
    updateEnabledStatus();
  }


  //#########################################################################
  //# Interface java.awt.event.ActionListener
  @Override
  public void actionPerformed(final ActionEvent event)
  {
    final IDE ide = getIDE();
    if (ide != null) {
      final WatersAnalyzerPanel panel = getAnalyzerPanel();
      final AutomataTable table = panel.getAutomataTable();
      final List<AutomatonProxy> automata = table.getOperationArgument();
      final ProductDESProxyFactory factory = ProductDESElementFactory.getInstance();
      final ProductDESProxy des = AutomatonTools.createProductDESProxy
        ("workbenchProject", automata, factory);
      final DocumentManager manager = ide.getDocumentManager();
      final ProjectBuildFromWaters builder = new ProjectBuildFromWaters(manager);

      final Logger logger = LogManager.getLogger();
      Project project;
      try {
        project = builder.build(des);
      } catch (final EvalException exception) {
        logger.error("Failed to convert automata: " + exception.getMessage());
        return;
      }
      final VisualProject visualProject = new VisualProject();
      visualProject.setSelectedAutomata(project);
      final Automata selection = project;
      if (selection.size() <= 0) {
        logger.info("No automata selected.");
        return;
      }
      visualProject.addListener(new AutomataListener() {
        @Override
        public void updated(final Object o) {}

        @Override
        public void automatonAdded(final Automata auta, final Automaton automaton)
        {
          final AutomataToWaters atw = new AutomataToWaters(factory, des,
                                                            AbstractConflictChecker.getMarkingProposition(des));
          final AutomatonProxy proxy = atw.convertAutomaton(automaton);
          final AutomataTableModel model = panel.getAutomataTableModel();
          model.insertRow(proxy);
          final List<AutomatonProxy> list = Arrays.asList(new AutomatonProxy[] {proxy});
          final AutomataTable table = panel.getAutomataTable();
          panel.getAutomataTable().scrollToVisible(list);
          table.clearSelection();
          table.addToSelection(list);
        }

        @Override
        public void automatonRemoved(final Automata automata, final Automaton automaton) {}

        @Override
        public void automatonRenamed(final Automata automata, final Automaton automaton) {}

        @Override
        public void actionsOrControlsChanged(final Automata automata) {}
      });
      try {
        final Workbench workbench = new Workbench(visualProject, selection);
        workbench.setVisible(true);
      } catch (final Exception exception) {
        logger.error("Error starting Workbench.");
      }
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.observer.Observer
  @Override
  public void update(final EditorChangedEvent event)
  {
    if (event.getKind() == EditorChangedEvent.Kind.SELECTION_CHANGED) {
      updateEnabledStatus();
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private void updateEnabledStatus()
  {
    final AutomataTable table = getAnalyzerTable();
    if (table == null) {
      setEnabled(false);
      putValue(Action.SHORT_DESCRIPTION,
               "Display workbench");
    } else if (table.getSelectedRowCount() > 0) {
      setEnabled(true);
      putValue(Action.SHORT_DESCRIPTION,
               "Display workbench for selected automata");
    } else {
      setEnabled(table.getRowCount() > 0);
      putValue(Action.SHORT_DESCRIPTION,
               "Display workbench for all automata");
    }
  }


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 636028154288275788L;

}
