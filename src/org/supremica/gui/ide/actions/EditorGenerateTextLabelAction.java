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

package org.supremica.gui.ide.actions;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.Action;
// import javax.swing.ImageIcon;
// import javax.swing.JFileChooser;
// import javax.swing.JOptionPane;

import net.sourceforge.waters.gui.command.Command;
import net.sourceforge.waters.gui.command.EditCommand;
import net.sourceforge.waters.model.module.ModuleProxyCloner;
import net.sourceforge.waters.subject.base.AbstractSubject;
import net.sourceforge.waters.subject.module.BinaryExpressionSubject;
import net.sourceforge.waters.subject.module.EdgeSubject;
import net.sourceforge.waters.subject.module.GraphSubject;
import net.sourceforge.waters.subject.module.GuardActionBlockSubject;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;
import net.sourceforge.waters.subject.module.SimpleExpressionSubject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.gui.ide.DocumentContainer;
import org.supremica.gui.ide.IDE;
import org.supremica.gui.ide.ModuleContainer;
// import org.supremica.properties.Config;

public class EditorGenerateTextLabelAction extends IDEAction
{
  //#########################################################################
  //# Constructor
  public EditorGenerateTextLabelAction(final List<IDEAction> actionList)
  {
    super(actionList);
    setEditorActiveRequired(true);

    final String actName = "Recompute Guards and Actions Text labels";
    final String description = "Recompute the XML Text labels of Guards and Actions";

    putValue(Action.NAME, actName);
    putValue(Action.SHORT_DESCRIPTION, description);
  }

  //#########################################################################
  //# Overridden methods
  @Override
  public void actionPerformed(final ActionEvent e)
  {
    doAction();
  }

  @Override
  public void doAction()
  {
    ////////////////////////////////////////////////////////////
    // TODO: * Automatic Save as?                             //
    //       * Option: Guards and/or Actions                  //
    //       * Option: Replace all / only if missing          //
    //       * Remove unused imports                          //
    //       * Add some more != null checks before for-loops  //
    //         even though they are less likely to occur      //
    //         (Graph, Edges, ...)                   //
    ////////////////////////////////////////////////////////////
    logger.info("Recomputing Guards (and Actions) Text labels...");
    logger.debug("\tRetrieving ModuleContainer and ModuleSubject...");
    final DocumentContainer docContainer = ide.getActiveDocumentContainer();
    final ModuleContainer moduleContainer;
    final ModuleSubject moduleSubject;
    if (docContainer instanceof ModuleContainer) {
      moduleContainer = (ModuleContainer) docContainer; // needed to execute the EditCommand
      moduleSubject = moduleContainer.getModule();
      logger.debug("\tModuleContainer and ModuleSubject successfully retrieved.");
    } else {
      // We should never get there
      logger.error("ModuleContainer and ModuleSubject could not be retrieved.");
      return;
    }

    final ModuleProxyCloner cloner = ModuleSubjectFactory.getCloningInstance();
    final List<AbstractSubject> absSubjects = moduleSubject.getComponentListModifiable();
    boolean graphFound = false;
    int guardsFound = 0;
    int actionsFound = 0;
    if (absSubjects != null) {
      for (final AbstractSubject absSubject : absSubjects) {
        if (absSubject instanceof SimpleComponentSubject) {
          final SimpleComponentSubject componentSubject = (SimpleComponentSubject) absSubject;
          final GraphSubject graphSubject = componentSubject.getGraph();
          if (graphSubject != null) {
            graphFound = true;
            final List<EdgeSubject> edgeSubjects = graphSubject.getEdgesModifiable();
            if (edgeSubjects != null) {
              for (final EdgeSubject edgeSubject : edgeSubjects) {
                // Retrieve and clone the GuardActionBlock
                final GuardActionBlockSubject guardActionBlockSubject = edgeSubject.getGuardActionBlock();
                if (guardActionBlockSubject != null){
                  // Clone the GuardActionBlock
                  final GuardActionBlockSubject newGuardActionBlockSubject = (GuardActionBlockSubject) cloner.getClone(guardActionBlockSubject);
                  logger.debug("\t1 GuardActionBlockSubject successfully cloned.");
                  // Loop on the guards
                  final List<SimpleExpressionSubject> guardSubjects = newGuardActionBlockSubject.getGuardsModifiable();
                  if (guardSubjects != null) {
                    for (final SimpleExpressionSubject guardSubject : guardSubjects) {
                      guardsFound += 1;
                      final String oldGuardPlainText = guardSubject.getPlainText();
                      final String newGuardPlainText = guardSubject.toString();
                      guardSubject.setPlainText(newGuardPlainText);
                      Command command = null;
                      // NOTA: here the space seems to be ignored: Override of equals at the String level?
                      if(!newGuardPlainText.equals(oldGuardPlainText)){
                        // Prepare the EditCommand to update the GuardActionBlock
                        command = new EditCommand(guardActionBlockSubject, newGuardActionBlockSubject, null);
                      }
                      if (command != null){
                        // Execute the EditCommand
                        moduleContainer.executeCommand(command);
                      }
                    }
                  }
                  // Loop on the actions
                  final List<BinaryExpressionSubject> actionSubjects = newGuardActionBlockSubject.getActionsModifiable();
                  if (actionSubjects != null) {
                    for (final BinaryExpressionSubject actionSubject : actionSubjects) {
                      actionsFound += 1;
                      final String oldActionPlainText = actionSubject.getPlainText();
                      final String newActionPlainText = actionSubject.toString();
                      actionSubject.setPlainText(newActionPlainText);
                      Command command = null;
                      if(!newActionPlainText.equals(oldActionPlainText)){
                        // Prepare the EditCommand to update the GuardActionBlock
                        command = new EditCommand(guardActionBlockSubject, newGuardActionBlockSubject, null);
                      }
                      if (command != null){
                        // Execute the EditCommand
                        moduleContainer.executeCommand(command);
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
    if (!graphFound) {
      logger.error("No GraphSubject could be retrieved.");
      return;
    }
    logger.info("Guards and Actions Text labels successfully recomputed!");
    logger.info("\t" + guardsFound + " Guards and " + actionsFound + " Actions found.");
  }

  //#########################################################################
  //# Class Constants
  private final Logger logger = LogManager.getLogger(IDE.class);
  private final static long serialVersionUID = 1L;
 }