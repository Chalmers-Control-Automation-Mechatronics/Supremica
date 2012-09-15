package org.supremica.gui.ide.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;

import org.supremica.automata.FlowerEFABuilder;
import org.supremica.gui.ide.IDE;

/**
 *
 * @ author Sajed, Zhennan
 */

public class OpenRASAction extends net.sourceforge.waters.gui.actions.IDEAction {

    // # Constructor
    OpenRASAction(final IDE ide) {
        super(ide);
        putValue(Action.NAME, "Open RAS ...");
        putValue(Action.SHORT_DESCRIPTION, "Open/import a RAS module");
        putValue(Action.SMALL_ICON,
                new ImageIcon(IDE.class.getResource("/toolbarButtonGraphics/general/Open16.gif")));
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

           FlowerEFABuilder flbuilder = null;
           try{
                flbuilder = new FlowerEFABuilder(selectedRAS, ide.getActiveDocumentContainer().getEditorPanel().getModuleSubject());
           }catch(final IOException e){e.printStackTrace();};
           flbuilder.buildEFA();
        }
    }
    // #########################################################################
    // # Class Constants
    private static final long serialVersionUID = 1L;
}
