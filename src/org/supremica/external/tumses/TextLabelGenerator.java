package org.supremica.external.tumses;

import java.util.List;

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
import org.supremica.gui.ide.ModuleContainer;
import org.supremica.gui.ide.actions.IDEActionInterface;

public class TextLabelGenerator
{
  private static final long serialVersionUID = 1L;
  private final static Logger logger = LogManager.getLogger();

  ////////////////////////////////////////////////////////////
  // TODO: * Automatic Save as?                             //
  //       * Option: Guards and/or Actions                  //
  //       * Option: Replace all / only if missing          //
  //       * Remove unused imports                          //
  //       * Add some more != null checks before for-loops  //
  //         even though they are less likely to occur      //
  //         (Graph, Edges, ...)                            //
  ////////////////////////////////////////////////////////////

  public static void GenerateTextLabel(final IDEActionInterface ide) {
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
}
