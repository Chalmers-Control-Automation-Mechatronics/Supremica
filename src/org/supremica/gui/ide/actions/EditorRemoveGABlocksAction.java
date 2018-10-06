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
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
// import javax.swing.ImageIcon;
// import javax.swing.JFileChooser;
// import javax.swing.JOptionPane;

import net.sourceforge.waters.subject.base.AbstractSubject;
import net.sourceforge.waters.subject.module.EdgeSubject;
import net.sourceforge.waters.subject.module.GraphSubject;
import net.sourceforge.waters.subject.module.GuardActionBlockSubject;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;
import net.sourceforge.waters.subject.module.VariableComponentSubject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.gui.ide.DocumentContainer;
import org.supremica.gui.ide.IDE;
import org.supremica.gui.ide.ModuleContainer;
// import org.supremica.properties.Config;

public class EditorRemoveGABlocksAction extends IDEAction
{
  //#########################################################################
  //# Constructor
  public EditorRemoveGABlocksAction(final List<IDEAction> actionList)
  {
    super(actionList);
    setEditorActiveRequired(true);

    final String actName = "Remove all Guards and Actions of the current module";
    final String description = "Remove all Guards and Actions of the current module";

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
    //       * Undo support?                                  //
    ////////////////////////////////////////////////////////////
    logger.info("Removing all Guards and Actions of the current module...");
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

    final List<AbstractSubject> absSubjects = moduleSubject.getComponentListModifiable();
	boolean graphFound = false;
	int varsFound = 0;
    int gaBlocksFound = 0;
    if (absSubjects != null) {
      final List<VariableComponentSubject> removedVariables = new ArrayList<>();
      for (final AbstractSubject absSubject : absSubjects) {
        if (absSubject instanceof SimpleComponentSubject) {
          // Remove the GuardActionBlock of all Edges the Graph
          final SimpleComponentSubject componentSubject = (SimpleComponentSubject) absSubject;
          final GraphSubject graphSubject = componentSubject.getGraph();
          if (graphSubject != null) {
			graphFound = true;
            final List<EdgeSubject> edgeSubjects = graphSubject.getEdgesModifiable();
            if (edgeSubjects != null) {
              for (final EdgeSubject edgeSubject : edgeSubjects) {
                final GuardActionBlockSubject guardActionBlockSubject = edgeSubject.getGuardActionBlock();
                if (guardActionBlockSubject != null){
                  gaBlocksFound += 1;
                  edgeSubject.setGuardActionBlock(null);
                }
              }
            }
          }
        }
        if (absSubject instanceof VariableComponentSubject) {
          // Retrieve all VariableComponent
          // NOTA: Since the absSubjects are use in the main for-loop, do not remove them directly
          varsFound += 1;
          removedVariables.add((VariableComponentSubject) absSubject);
        }
      }
      // Delete all VariableComponent found
      if (removedVariables.size() != 0) {
        moduleSubject.getComponentListModifiable().removeAll(removedVariables);
      }
    }
	if (!graphFound) {
	  logger.error("No Graph could be retrieved.");
      return;
	}
    if (gaBlocksFound == 0) {
      logger.info("No Guards/Actions block to be removed.");
    } else {
      logger.info("Guards and Actions successfully removed!");
      logger.info("\t" + gaBlocksFound + " Guards/Actions block(s) removed.");
    }
    if (varsFound == 0) {
      logger.info("No variable declaration to be removed.");
    } else {
      logger.info("Variables successfully removed!");
      logger.info("\t" + varsFound + " Variables declaration(s) removed.");
    }
  }

  //#########################################################################
  //# Class Constants
  private final Logger logger = LogManager.getLogger(IDE.class);
  private final static long serialVersionUID = 1L;
 }