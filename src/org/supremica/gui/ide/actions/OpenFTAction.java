package org.supremica.gui.ide.actions;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JFrame;

import net.sourceforge.waters.gui.actions.IDEAction;
import net.sourceforge.waters.gui.util.IconAndFontLoader;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;

import org.supremica.automata.FTEFABuilder;
import org.supremica.gui.ide.DocumentContainerManager;
import org.supremica.gui.ide.IDE;

/**
 * Zenuity 2019 Hackfest
 *
 * The action to open a fault tree which in the XML format exported from
 * SystemWaver.
 *
 * @author zhefei
 */

public class OpenFTAction extends IDEAction
{

  public OpenFTAction(final IDE ide)
  {
    super(ide);
    putValue(Action.NAME, "Import FT model ...");
    putValue(Action.SHORT_DESCRIPTION, "Open/build an FT module");
    putValue(Action.SMALL_ICON, IconAndFontLoader.ICON_TOOL_IMPORT);
  }

  //# Interface java.awt.event.ActionListener
  @Override
  public void actionPerformed(final ActionEvent event)
  {
    // Get the state and dialog ...
    final IDE ide = getIDE();
    final JFileChooser chooser = ide.getFileChooser();
    // Set up the dialog ...
    chooser.setDialogType(JFileChooser.OPEN_DIALOG);
    chooser.resetChoosableFileFilters();

    // Show the dialog ...
    final JFrame frame = ide.getFrame();
    final int choice = chooser.showOpenDialog(frame);

    // Load the files ...
    if (choice == JFileChooser.APPROVE_OPTION) {
      final File selectedFT = chooser.getSelectedFile();
      final String name = selectedFT.getName();
      final ModuleSubjectFactory factory = ModuleSubjectFactory.getInstance();
      final ModuleSubject module = factory.createModuleProxy(name, null);

      /* The actual code to build FT */
      new FTEFABuilder(selectedFT, module);

      final DocumentContainerManager cmanager =
        ide.getDocumentContainerManager();
      cmanager.newContainer(module);
    }
  }

  // #########################################################################
  // # Class Constants
  private static final long serialVersionUID = 1L;
}
