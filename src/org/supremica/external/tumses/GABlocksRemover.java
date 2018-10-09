package org.supremica.external.tumses;

import java.util.ArrayList;
import java.util.List;

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
import org.supremica.gui.ide.ModuleContainer;
import org.supremica.gui.ide.actions.IDEActionInterface;

public class GABlocksRemover
{
  private static final long serialVersionUID = 1L;
  private final static Logger logger = LogManager.getLogger();

  ////////////////////////////////////////////////////////////
  // TODO: * Automatic Save as?                             //
  //       * Undo support?                                  //
  ////////////////////////////////////////////////////////////
  public static void RemoveGABlocksAction(final IDEActionInterface ide) {
    // TODO: add a confirmation dialog, add a second argument (true by default)
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
}
