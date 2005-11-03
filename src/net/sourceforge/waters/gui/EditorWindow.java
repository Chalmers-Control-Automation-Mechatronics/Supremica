//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   EditorWindow
//###########################################################################
//# $Id: EditorWindow.java,v 1.21 2005-11-03 01:24:15 robi Exp $
//###########################################################################


package net.sourceforge.waters.gui;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

import java.awt.*;
import java.awt.dnd.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

import net.sourceforge.waters.gui.command.Command;
import net.sourceforge.waters.gui.command.UndoInterface;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.subject.module.IdentifierSubject;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;

import org.supremica.gui.GraphicsToClipboard;



public class EditorWindow
	extends JFrame
	implements EditorWindowInterface
{

	//#######################################################################
	//# Constructor
	public EditorWindow(final String title,
						final ModuleSubject module,
						final SimpleComponentSubject subject,
						final ModuleWindow root,
						final UndoInterface undoInterface)
	{
		mUndoInterface = undoInterface;
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setTitle(title);

		toolbar = new EditorToolbar();
		surface = new ControlledSurface(this, toolbar);

		surface.setPreferredSize(new Dimension(500, 500));
		surface.setMinimumSize(new Dimension(0, 0));

		final ExpressionParser parser = root.getExpressionParser();
		mEventPane = new EditorEvents(module, subject, parser);
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
		final JScrollPane scrollevents = new JScrollPane(mEventPane);
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
		final int eventswidth = mEventPane.getSize().width;
		final int separatorwidth = splitwidth - surfacewidth - eventswidth;
		final int halfwidth = (splitwidth - separatorwidth) >> 1;
		if (halfwidth > 0) {
			final int prefeventswidth = mEventPane.getPreferredSize().width;
			final int setwidth = Math.min(prefeventswidth, halfwidth);
			final int divider = splitwidth - setwidth - separatorwidth;
			split.setDividerLocation(divider);
		}

		mModule = module;
		mSubject = subject;
		if (mSubject != null && mModule != null) {
			surface.loadElement(mModule, mSubject);
		}
		surface.createOptions(this);

		setVisible(true);
	}

	public IdentifierSubject getBuffer()
	{
		return mEventPane.getBuffer();
	}

	public void setBuffer(final IdentifierSubject ident)
	{
		mEventPane.setBuffer(ident);
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
		return mModule.getEventDeclList();
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
		return mEventPane;
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

	public UndoInterface getUndoInterface()
	{
		return mUndoInterface;
	}

	public void setVisible(boolean visible)
	{
		super.setVisible(visible);
		if (visible) {
			mUndoInterface.attach(menu);
			menu.update();
		} else {
			mUndoInterface.detach(menu);
		}
	}

	public void setDisplayed()
	{
		setVisible(true);
		requestFocus();
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


	//#######################################################################
	//# Data Members
	private EditorToolbar toolbar;
	private ControlledSurface surface;
	private EditorMenu menu;
	private final EditorEvents mEventPane;
	private final SimpleComponentSubject mSubject;
	private final ModuleSubject mModule;
	private boolean isSaved = false;
	private GraphicsToClipboard toClipboard = null;
	private final UndoInterface mUndoInterface;

}
