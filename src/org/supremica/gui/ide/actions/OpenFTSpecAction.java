package org.supremica.gui.ide.actions;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JFrame;

import net.sourceforge.waters.gui.actions.IDEAction;
import net.sourceforge.waters.gui.util.IconAndFontLoader;
import net.sourceforge.waters.subject.module.ModuleSubject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.automata.FTSpecEFABuilder;
import org.supremica.gui.ide.IDE;

/**
 * Zenuity 2019 Hackfest
 *
 * The action to open specifications of a fault tree. The specification
 * file is currently in the text format.
 *
 * @author zhefei
 */

public class OpenFTSpecAction extends IDEAction
{

  private final Logger logger = LogManager.getLogger(OpenFTSpecAction.class);

  public OpenFTSpecAction(final IDE ide)
  {
    super(ide);
    putValue(Action.NAME, "Import FT spec ...");
    putValue(Action.SHORT_DESCRIPTION, "Add spec for FT");
    putValue(Action.SMALL_ICON, IconAndFontLoader.ICON_BINDING);
  }

  //# Interface java.awt.event.ActionListener
  @Override
  public void actionPerformed(final ActionEvent event)
  {
    // Get the state and dialog ...
    final IDE ide = getIDE();

    // Get module with the same name
    final ModuleSubject module =
      ide.getActiveDocumentContainer().getEditorPanel().getModuleSubject();

    if (module.getComponentList().size() > 0) {
      final JFileChooser chooser = ide.getFileChooser();
      // Set up the dialog ...
      chooser.setDialogType(JFileChooser.OPEN_DIALOG);
      chooser.resetChoosableFileFilters();
      // Show the dialog ...
      final JFrame frame = ide.getFrame();
      final int choice = chooser.showOpenDialog(frame);
      if (choice == JFileChooser.APPROVE_OPTION) {
        final File spec = chooser.getSelectedFile();
        logger.info("Building specification EFSMs ...");
        /* The actual code to build FT */
        FTSpecEFABuilder specBuilder = null;
        specBuilder = new FTSpecEFABuilder(spec, module);
        specBuilder.buildEFA();
      }
    } else {
      logger.error("No model exists!");
    }
  }

  // #########################################################################
  // # Class Constants
  private static final long serialVersionUID = 1L;
}
