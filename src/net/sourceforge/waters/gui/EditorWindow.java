//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.gui
//# CLASS:   EditorWindow
//###########################################################################
//# $Id: EditorWindow.java,v 1.15 2005-07-05 02:32:07 siw4 Exp $
//###########################################################################
package net.sourceforge.waters.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.dnd.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseListener;
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
import java.io.FileOutputStream;
import java.io.IOException;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;



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
	    System.out.println("ahgha");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setTitle(title);

		surface = new ControlledSurface(this);
		toolbar = new EditorToolbar(surface);

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

	public void copyAsWMFToClipboard()
	{
		if (toClipboard == null)
		{
			toClipboard = GraphicsToClipboard.getInstance();
		}

		Graphics theGraphics = toClipboard.getGraphics(surface.getWidth(), surface.getHeight());

		surface.print(theGraphics);
		toClipboard.copyToClipboard();
	}

	public void createPDF(File file)
	{

		int width = surface.getWidth();
		int height = surface.getHeight();
		Document document = new Document(new com.lowagie.text.Rectangle(width, height));

		try
		{
			PdfWriter writer= PdfWriter.getInstance(document,  new FileOutputStream(file));

			document.addAuthor("Supremica");
			document.open();

			PdfContentByte cb = writer.getDirectContent();
			PdfTemplate tp = cb.createTemplate(width, height);
			Graphics2D g2 = tp.createGraphics(width, height, new DefaultFontMapper());
			surface.print(g2);
			//Rectangle2D rectangle2D = new Rectangle2D.Double(0, 0, width, height);
			//chart.draw(g2, rectangle2D);
			g2.dispose();
			cb.addTemplate(tp, 0, 0);

		}
		catch (DocumentException de)
		{
			System.err.println(de.getMessage());
		}
		catch (IOException ioe)
		{
			System.err.println(ioe.getMessage());
		}

		document.close();
	}
}
