//# -*- tab-width: 4  indent-tabs-mode: nil  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: org.supremica.gui.ide.actions
//# CLASS:   OpenAction
//###########################################################################
//# $Id: OpenAction.java,v 1.22 2007-06-23 10:16:00 robi Exp $
//###########################################################################


package org.supremica.gui.ide.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;

import net.sourceforge.waters.model.base.DocumentProxy;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.WatersUnmarshalException;
import org.supremica.gui.ide.IDE;


public class OpenAction
    extends net.sourceforge.waters.gui.actions.IDEAction
{

    //#######################################################################
    //# Constructor
    OpenAction(final IDE ide)
    {
        super(ide);
        putValue(Action.NAME, "Open/Import...");
        putValue(Action.SHORT_DESCRIPTION, "Open/import a project");
        putValue(Action.MNEMONIC_KEY, KeyEvent.VK_O);
        putValue(Action.ACCELERATOR_KEY,
                 KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
        putValue(Action.SMALL_ICON,
                 new ImageIcon(IDE.class.getResource
                               ("/toolbarButtonGraphics/general/Open16.gif")));
    }


    //#######################################################################
    //# Interface java.awt.event.ActionListener
    public void actionPerformed(final ActionEvent event)
    {
        // Get the state and dialog ...
        final IDE ide = getIDE();
        final DocumentManager manager = ide.getDocumentManager();
        final JFileChooser chooser = ide.getFileChooser();
        final FileFilter current = chooser.getFileFilter();
        // Set up the dialog ...
        chooser.setDialogType(JFileChooser.OPEN_DIALOG);
        chooser.setMultiSelectionEnabled(true);
        chooser.resetChoosableFileFilters();
        final List<FileFilter> filters = manager.getSupportedFileFilters();
        boolean reselect = false;
        for (final FileFilter filter : filters) {
            chooser.addChoosableFileFilter(filter);
            if (filter == current) {
                reselect = true;
            }
        }
        // Select the first filter ...
        final FileFilter first =
            reselect ? current : filters.iterator().next();
        chooser.setFileFilter(first);
        // Show the dialog ...
        new WatersFileImporter(chooser);
    }


    //#######################################################################
    //# Opening Files
    public boolean openFile(final File file)
    {
        final IDE ide = getIDE();
        try {
            // The documentmanager does the loading, by extension.
            final DocumentManager manager = ide.getDocumentManager();
            final DocumentProxy document = manager.load(file);
            ide.installContainer(document);
            return true;
        } catch (final WatersUnmarshalException exception) {
            JOptionPane.showMessageDialog(ide.getFrame(),
                                          "Error opening file:" +
                                          exception.getMessage());
            return false;
        } catch (final IOException exception) {
            JOptionPane.showMessageDialog(ide.getFrame(),
                                          "Error opening file:" +
                                          exception.getMessage());
            return false;
        }
    }


    //#######################################################################
    //# Local Class WatersFileImporter
    private class WatersFileImporter extends FileImporter
    {
        //###################################################################
        //# Constructors
        private WatersFileImporter(final JFileChooser chooser)
        {
            super(chooser, getIDE());
        }

        //###################################################################
        //# Overrides for Base Class org.supremica.gui.ide.actions.FileImporter
        void openFile(final File file)
        {
            OpenAction.this.openFile(file);
        }
    }

}
