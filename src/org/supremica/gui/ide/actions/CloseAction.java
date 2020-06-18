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

import javax.swing.Action;

import net.sourceforge.waters.gui.observer.EditorChangedEvent;

import org.supremica.gui.ide.DocumentContainer;
import org.supremica.gui.ide.DocumentContainerManager;
import org.supremica.gui.ide.IDE;


public class CloseAction
    extends net.sourceforge.waters.gui.actions.IDEAction
{

	//#######################################################################
    //# Constructor
    CloseAction(final IDE ide)
    {
        super(ide);
        setEnabled(false);
        putValue(Action.NAME, "Close");
        putValue(Action.SHORT_DESCRIPTION, "Close the current module");
    }


    //#######################################################################
    //# Interface java.awt.event.ActionListener
    @Override
    public void actionPerformed(final ActionEvent event)
    {
        final IDE ide = getIDE();
        final DocumentContainerManager manager =
            ide.getDocumentContainerManager();
        manager.closeActiveContainer();
    }


    //#######################################################################
    //# Interface net.sourceforge.waters.gui.observer.Observer
    @Override
    public void update(final EditorChangedEvent event)
    {
        switch (event.getKind()) {
        case CONTAINER_SWITCH:
            final IDE ide = getIDE();
            final DocumentContainerManager manager =
                ide.getDocumentContainerManager();
            final DocumentContainer container = manager.getActiveContainer();
            final boolean enabled = container != null;
            setEnabled(enabled);


            break;
        default:
            break;
        }
    }


	//#######################################################################
    //# Class Constants
	private static final long serialVersionUID = 1L;

}
