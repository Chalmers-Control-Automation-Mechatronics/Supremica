//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.gui
//# CLASS:   EditorWindow
//###########################################################################
//# $Id: EditorWindow.java,v 1.9 2005-02-22 21:24:31 robi Exp $
//###########################################################################
package net.sourceforge.waters.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.dnd.*;
import java.io.File;
import java.io.IOException;
import javax.xml.bind.JAXBException;
import net.sourceforge.waters.model.module.IdentifiedElementProxy;
import net.sourceforge.waters.model.base.ProxyMarshaller;
import net.sourceforge.waters.model.module.ModuleMarshaller;
import net.sourceforge.waters.model.module.*;
import java.util.ArrayList;
import net.sourceforge.waters.model.expr.IdentifierProxy;
import org.supremica.gui.GraphicsToClipboard;
import java.awt.geom.Rectangle2D;

public class EditorWindow
	extends JFrame
	implements EditorWindowInterface
{
	private EditorToolbar toolbar;
	private ControlledSurface surface;
	private EditorEvents events;
	private EditorMenu menu;
	private SimpleComponentProxy element = null;
	private ModuleProxy module = null;
	private boolean isSaved = false;
	private GraphicsToClipboard toClipboard = null;

	public EditorWindow(String title, ModuleProxy module, SimpleComponentProxy element)
	{
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setTitle(title);

		toolbar = new EditorToolbar();
		surface = new ControlledSurface(toolbar, this);

		surface.setPreferredSize(new Dimension(500, 500));
		surface.setMinimumSize(new Dimension(0, 0));

		events = new EditorEvents(module, element);
		menu = new EditorMenu(surface, this);

		final Container panel = getContentPane();
		final GridBagLayout gridbag = new GridBagLayout();
		final GridBagConstraints constraints = new GridBagConstraints();

		constraints.gridy = 0;
		constraints.weighty = 1.0;
		constraints.anchor = GridBagConstraints.NORTH;

		panel.setLayout(gridbag);
		gridbag.setConstraints(toolbar, constraints);
		panel.add(toolbar);

		final JScrollPane scrollsurface = new JScrollPane(surface);
		final JScrollPane scrollevents = new JScrollPane(events);
		final JViewport viewevents = scrollevents.getViewport();
		final JSplitPane split = new JSplitPane
			(JSplitPane.HORIZONTAL_SPLIT, scrollsurface, scrollevents);
		viewevents.setBackground(Color.WHITE);
		split.setResizeWeight(1.0);

		constraints.weightx = 1.0;
		constraints.fill = GridBagConstraints.BOTH;

		gridbag.setConstraints(split, constraints);
		panel.add(split);
		setJMenuBar(menu);
		pack();

		// Try to set the divider location so the event panel is displayed
		// at its preferred size.
		final int splitwidth = split.getSize().width;
		final int surfacewidth = surface.getSize().width;
		final int eventswidth = events.getSize().width;
		final int separatorwidth = splitwidth - surfacewidth - eventswidth;
		final int halfwidth = (splitwidth - separatorwidth) >> 1;
		if (halfwidth > 0) {
			final int prefeventswidth = events.getPreferredSize().width;
			final int setwidth = Math.min(prefeventswidth, halfwidth);
			final int divider = splitwidth - setwidth - separatorwidth;
			split.setDividerLocation(divider);
		}

		setVisible(true);

		this.module = module;
		this.element = element;

		if ((element != null) && (module != null))
		{
			surface.loadElement(module, element);
		}

		surface.createOptions(this);
	}

	public IdentifierProxy getBuffer()
	{
		return events.getBuffer();
	}

	public void setBuffer(IdentifierProxy i)
	{
		events.setBuffer(i);
	}

	public boolean isSaved()
	{
		return isSaved;
	}

	public void setSaved(boolean s)
	{
		isSaved = s;
	}

	public java.util.List getEventDeclList()
	{
		return module.getEventDeclList();
	}

	public JFrame getFrame()
	{
		return (JFrame) this;
	}

	public ControlledSurface getControlledSurface()
	{
		return surface;
	}

	public EditorEvents getEventPane()
	{
		return events;
	}

	void copyAsWMFToClipboard()
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

		surface.paint(theGraphics);
		toClipboard.copyToClipboard();
	}
}
