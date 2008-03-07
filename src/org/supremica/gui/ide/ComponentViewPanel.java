//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: org.supremica.gui.ide
//# CLASS:   ComponentViewPanel
//###########################################################################
//# $Id: ComponentViewPanel.java,v 1.10 2008-03-07 04:11:02 robi Exp $
//###########################################################################

package org.supremica.gui.ide;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

import java.awt.*;
import java.io.*;
import java.awt.print.*;
import java.util.Locale;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.swing.*;

import net.sourceforge.waters.gui.ControlledSurface;
import net.sourceforge.waters.gui.ControlledToolbar;
import net.sourceforge.waters.gui.EditorWindowInterface;
import net.sourceforge.waters.gui.GraphEventPanel;
import net.sourceforge.waters.gui.ModuleWindowInterface;
import net.sourceforge.waters.gui.actions.WatersPopupActionManager;
import net.sourceforge.waters.gui.command.UndoInterface;
import net.sourceforge.waters.gui.renderer.GeometryAbsentException;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;

import org.supremica.gui.GraphicsToClipboard;


/**
 * A Swing component for editing a Waters graph.
 * A component editor panel allows the user to edit an object of type
 * {@link SimpleComponentSubject} graphically. It consists of a splitpane
 * with two windows: the event list to the left and the graph editor
 * surface to the right.
 *
 * @author Knut &Aring;kesson
 */

public class ComponentViewPanel
    extends JPanel
    implements EditorWindowInterface
{
    private static final long serialVersionUID = 1L;
    
    private ModuleContainer mModuleContainer;
    private ControlledSurface surface;
    private GraphEventPanel events;
    private SimpleComponentSubject element = null;
    private ModuleSubject mModule = null;
    private GraphicsToClipboard toClipboard = null;
    
    
    /**
     * Creates a new component editor panel.
     * @param  moduleContainer  the module container as a handle to the
     *                          IDE application.
     * @param  element          the simple component containing the graph
     *                          to be edited.
     * @param  size             the expected total size of the panel.
     */
    public ComponentViewPanel(final ModuleContainer moduleContainer,
        final SimpleComponentSubject element,
        final Dimension size)
        throws GeometryAbsentException
        
    {
        this.element = element;
        mModuleContainer = moduleContainer;
        mModule = moduleContainer.getModule();
		final IDE ide = moduleContainer.getIDE();
		final WatersPopupActionManager manager = ide.getPopupActionManager();
        surface = new ControlledSurface
            (element.getGraph(), mModule, this,
			 (ControlledToolbar) ide.getToolBar(), manager);
        surface.setPreferredSize(IDEDimensions.rightAnalyzerPreferredSize);
        surface.setMinimumSize(IDEDimensions.rightAnalyzerMinimumSize);
        events = new GraphEventPanel(this, element, manager);
        
        final LayoutManager layout = new BorderLayout();
        setLayout(layout);
        
        final JScrollPane scrollsurface = new JScrollPane(surface);
        final JScrollPane scrollevents = new JScrollPane(events);
        final JViewport viewevents = scrollevents.getViewport();
        final JSplitPane split = new JSplitPane
            (JSplitPane.HORIZONTAL_SPLIT, scrollevents, scrollsurface);
        final int halfwidth = size.width >> 1;
        final int prefeventswidth = events.getPreferredSize().width;
        final int divide = Math.min(prefeventswidth, halfwidth);
        split.setDividerLocation(divide);
        add(split, BorderLayout.CENTER);
    }


	//########################################################################
    //# Interface net.sourceforge.waters.gui.EditorWindowInterface
    public SimpleComponentSubject getComponent()
    {
        return element;
    }
    
    public JFrame getFrame()
    {
        return mModuleContainer.getFrame();
    }
    
    public ControlledSurface getControlledSurface()
    {
        return surface;
    }
    
    public GraphEventPanel getEventPanel()
    {
        return events;
    }

	public ModuleWindowInterface getModuleWindowInterface()
	{
		return mModuleContainer.getEditorPanel();
	}

    public UndoInterface getUndoInterface()
    {
        return mModuleContainer;
    }
    
    public void copyAsWMFToClipboard()
    {
        if (toClipboard == null)
        {
            toClipboard = GraphicsToClipboard.getInstance();
        }
        
        //Rectangle2D bb = surface.getBoundingBox();
        //double minX = bb.getMinX();
        //double maxX = bb.getMaxX();
        //double minY = bb.getMinY();
        //double maxY = bb.getMaxY();
        //logger.debug("minX: " + minX + " maxX: " + maxX + " minY: " + minY + " maxY: " + maxY);
        //create a WMF object
        //int width = (int)(maxX - minX) + 1;
        //int height = (int)(maxY - minY) + 1;
        // Copy a larger area, approx 10 percent, there seems to be
        // a problem with the size of wmf-data
        //width += (int)0.1*width;
        //height += (int)0.1*height;
        Graphics theGraphics = toClipboard.getGraphics(surface.getWidth(), surface.getHeight());
        
        surface.print(theGraphics);
        toClipboard.copyToClipboard();
    }
    
}
