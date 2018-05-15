//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 1999-2018 Knut Akesson, Martin Fabian, Robi Malik
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

package org.supremica.gui.ide;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.print.PrinterJob;
import java.util.Locale;

import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttribute;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.JobName;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import net.sourceforge.waters.gui.EditorWindowInterface;
import net.sourceforge.waters.gui.GraphEditorPanel;
import net.sourceforge.waters.gui.GraphEventPanel;
import net.sourceforge.waters.gui.ModuleWindowInterface;
import net.sourceforge.waters.gui.actions.WatersPopupActionManager;
import net.sourceforge.waters.gui.command.UndoInterface;
import net.sourceforge.waters.gui.renderer.GeometryAbsentException;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.properties.SupremicaPropertyChangeEvent;
import org.supremica.properties.SupremicaPropertyChangeListener;


/**
 * A Swing component for editing a Waters graph.
 * A component editor panel allows the user to edit an object of type
 * {@link SimpleComponentSubject} graphically. It consists of a splitpane
 * with two windows: the event list to the left and the graph editor
 * surface to the right.
 *
 * @author Knut &Aring;kesson
 */

public class ComponentEditorPanel
    extends JPanel
    implements EditorWindowInterface, SupremicaPropertyChangeListener
{

    //########################################################################
    //# Constructor
    /**
     * Creates a new component editor panel.
     * @param  moduleContainer  the module container as a handle to the
     *                          IDE application.
     * @param  component        the simple component containing the graph
     *                          to be edited.
     * @param  size             the expected total size of the panel.
     */
    public ComponentEditorPanel(final ModuleContainer moduleContainer,
								final SimpleComponentSubject component,
								final Dimension size)
        throws GeometryAbsentException
    {
        mComponent = component;
        mModuleContainer = moduleContainer;
        mModule = moduleContainer.getModule();
		final IDE ide = moduleContainer.getIDE();
		final WatersPopupActionManager manager = ide.getPopupActionManager();
        mSurface = new GraphEditorPanel
            (component.getGraph(), mModule, mModuleContainer, this,
			 ide.getToolBar(), manager);
        mEventsPane = new GraphEventPanel(this, component, manager);

        final LayoutManager layout = new BorderLayout();
        setLayout(layout);

        final JScrollPane scrollsurface = new JScrollPane(mSurface);
        final JScrollPane scrollevents = new JScrollPane(mEventsPane);
        final JSplitPane split = new JSplitPane
            (JSplitPane.HORIZONTAL_SPLIT, scrollevents, scrollsurface);
        final int halfwidth = size.width >> 1;
        final int prefeventswidth = mEventsPane.getPreferredSize().width;
        final int divide = Math.min(prefeventswidth, halfwidth);
        split.setDividerLocation(divide);
        add(split, BorderLayout.CENTER);
    }


    //########################################################################
    //# Interface net.sourceforge.waters.gui.EditorWindowInterface
    @Override
    public SimpleComponentSubject getComponent()
    {
        return mComponent;
    }

    @Override
    public ModuleWindowInterface getModuleWindowInterface()
    {
        return mModuleContainer.getEditorPanel();
    }

    @Override
    public GraphEditorPanel getGraphEditorPanel()
    {
        return mSurface;
    }

    @Override
    public GraphEventPanel getEventPanel()
    {
        return mEventsPane;
    }

    @Override
    public UndoInterface getUndoInterface()
    {
        return mModuleContainer;
    }

    /**
     * Open a print dialog and let the user choose how to print.
     */
    public void printFigure()
    {
        try
        {
            final PrinterJob printJob = PrinterJob.getPrinterJob();
            if (printJob.getPrintService() == null)
            {
                LOGGER.error("No default printer set.");
                return;
            }
            printJob.setPrintable(mSurface);

            // Printing attributes
            final PrintRequestAttributeSet attributes = new HashPrintRequestAttributeSet();
            final PrintRequestAttribute name = new JobName("Supremica Printing", Locale.ENGLISH);
            attributes.add(name);

            // Show printing dialog
            if (printJob.printDialog(attributes))
            {
                LOGGER.debug("Printing...");

                // Print!
                printJob.print();
                //printJob.print(attributes);

                LOGGER.debug("Printing done!");
            }
        }
        catch (final Exception ex)
        {
            System.err.println(ex);
            System.err.println(ex.getStackTrace());
        }
    }


	//#######################################################################
	//# Interface org.supremica.properties.SupremicaPropertyChangeListener
	@Override
  public void propertyChanged(final SupremicaPropertyChangeEvent event)
	{
		mSurface.propertyChanged(event);
	}


    //########################################################################
    //# Data Members
    private final ModuleContainer mModuleContainer;
    private final GraphEditorPanel mSurface;
    private final GraphEventPanel mEventsPane;
    private final SimpleComponentSubject mComponent;
    private final ModuleSubject mModule;


    //########################################################################
    //# Static Class Constants
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER =
		LogManager.getLogger(ComponentEditorPanel.class);

}
