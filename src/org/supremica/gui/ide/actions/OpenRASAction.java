package org.supremica.gui.ide.actions;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JFrame;

import net.sourceforge.waters.gui.util.IconLoader;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;

import org.supremica.automata.FlowerEFABuilder;
import org.supremica.gui.ide.DocumentContainerManager;
import org.supremica.gui.ide.IDE;

/**
 * Create a EFA model for a Resource Allocation System (RAS)
 *
 * @ author Sajed, Zhennan
 */

public class OpenRASAction extends net.sourceforge.waters.gui.actions.IDEAction {

    // # Constructor
    OpenRASAction(final IDE ide) {
        super(ide);
        putValue(Action.NAME, "Open RAS ...");
        putValue(Action.SHORT_DESCRIPTION, "Open/import a RAS module");
        putValue(Action.SMALL_ICON, IconLoader.ICON_TOOL_OPEN);
    }

    // # Interface java.awt.event.ActionListener
    @Override
    public void actionPerformed(final ActionEvent event) {
        // Get the state and dialog ...
        final IDE ide = getIDE();
        final JFileChooser chooser = ide.getFileChooser();
        // Set up the dialog ...
        chooser.setDialogType(JFileChooser.OPEN_DIALOG);

        // Show the dialog ...
        final JFrame frame = ide.getFrame();
        final int choice = chooser.showOpenDialog(frame);
        // Load the files ...
        if (choice == JFileChooser.APPROVE_OPTION) {
            final File selectedRAS = chooser.getSelectedFile();
            final String rasName = selectedRAS.getName();
            final ModuleSubject module = ModuleSubjectFactory.getInstance()
                    .createModuleProxy(rasName, null);
            FlowerEFABuilder flbuilder = null;
            flbuilder = new FlowerEFABuilder(selectedRAS, module);
            flbuilder.buildEFA();
            final DocumentContainerManager cmanager =
              ide.getDocumentContainerManager();
            cmanager.newContainer(module);
        }
    }

    // #########################################################################
    // # Class Constants
    private static final long serialVersionUID = 1L;
}
