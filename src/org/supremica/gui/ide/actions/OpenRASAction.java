//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 1999-2020 Knut Akesson, Martin Fabian, Robi Malik
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
import java.io.File;

import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JFrame;

import net.sourceforge.waters.gui.util.IconAndFontLoader;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;

import org.supremica.automata.FlowerEFABuilder;
import org.supremica.gui.ide.DocumentContainerManager;
import org.supremica.gui.ide.IDE;

/**
 * Create a EFA model for a Resource Allocation System (RAS)
 *
 * @author Sajed, Zhennan
 */

public class OpenRASAction extends net.sourceforge.waters.gui.actions.IDEAction {

    // # Constructor
    OpenRASAction(final IDE ide) {
        super(ide);
        putValue(Action.NAME, "Open RAS ...");
        putValue(Action.SHORT_DESCRIPTION, "Open/import a RAS module");
        putValue(Action.SMALL_ICON, IconAndFontLoader.ICON_TOOL_OPEN);
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
