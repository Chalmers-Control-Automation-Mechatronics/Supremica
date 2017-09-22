//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 1999-2017 Knut Akesson, Martin Fabian, Robi Malik
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
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.gui.ide.IDE;

public class EditorPrintAction
    extends IDEAction
{
    private static final long serialVersionUID = 1L;

    public EditorPrintAction(final List<IDEAction> actionList)
    {
        super(actionList);
        putValue(Action.NAME, "Print...");
        putValue(Action.SHORT_DESCRIPTION, "Print current editor figure");
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_P));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK));
        putValue(Action.SMALL_ICON, new ImageIcon(IDE.class.getResource("/toolbarButtonGraphics/general/Print16.gif")));
        setEnabled(false);
    }

    @Override
    public void actionPerformed(final ActionEvent e)
    {
        doAction();
    }

    @Override
    public void doAction()
    {
        try
        {
            ide.getActiveDocumentContainer().getEditorPanel().getActiveEditorWindowInterface().printFigure();
        }
        catch (final NullPointerException ex)
        {
            // This action should only be enabled when there's an editor panel open!
            final Logger logger = LogManager.getLogger();
            logger.error("Must have an editor panel open.");
        }
    }

    /**
     * Is enabled if it is possible to get a hold of an active EditorWindowInterface.
     */
    /* Should use setEnabled when the property becomes true...?
    public boolean isEnabled()
    {
        try
        {
            return (null != ide.getActiveModuleContainer().getEditorPanel().getActiveEditorWindowInterface());
        }
        catch (NullPointerException ex)
        {
            return false;
        }
    }
     */
}
