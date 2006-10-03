//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: org.supremica.gui.ide
//# CLASS:   ComponentEditorPanel
//###########################################################################
//# $Id: ComponentEditorPanel.java,v 1.29 2006-10-03 19:33:06 knut Exp $
//###########################################################################

package org.supremica.gui.ide;

import net.sourceforge.waters.gui.renderer.GeometryAbsentException;
import javax.swing.*;
import javax.swing.plaf.basic.BasicSplitPaneUI;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.*;

import net.sourceforge.waters.gui.ControlledSurface;
import net.sourceforge.waters.gui.ControlledToolbar;
import net.sourceforge.waters.gui.EditorEvents;
import net.sourceforge.waters.gui.EditorMenu;
import net.sourceforge.waters.gui.EditorWindowInterface;
import net.sourceforge.waters.gui.EventEditorDialog;
import net.sourceforge.waters.gui.EventTableModel;
import net.sourceforge.waters.gui.ModuleWindowInterface;
import net.sourceforge.waters.gui.command.UndoInterface;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.subject.base.NamedSubject;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;
import net.sourceforge.waters.subject.module.SimpleIdentifierSubject;

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

public class ComponentEditorPanel
	extends JPanel
	implements EditorWindowInterface
{
	private static final long serialVersionUID = 1L;

	private ModuleContainer mModuleContainer;
	private ControlledSurface surface;
	private EditorEvents events;
	private EditorMenu menu;
	private SimpleComponentSubject element = null;
	private ModuleSubject mModule = null;
	private boolean isSaved = false;
	private GraphicsToClipboard toClipboard = null;


	/**
	 * Creates a new component editor panel.
	 * @param  moduleContainer  the module container as a handle to the
	 *                          IDE application.
	 * @param  element          the simple component containing the graph
	 *                          to be edited.
	 * @param  size             the expected total size of the panel.
	 */
	public ComponentEditorPanel(final ModuleContainer moduleContainer,
                              final SimpleComponentSubject element,
                              final Dimension size)
    throws GeometryAbsentException

	{
		this.element = element;
		mModuleContainer = moduleContainer;
		mModule = moduleContainer.getModule();
		surface = new ControlledSurface
			(element.getGraph(), mModule, this,
			 (ControlledToolbar) mModuleContainer.getIDE().getToolBar());
		surface.setPreferredSize(IDEDimensions.rightEditorPreferredSize);
		surface.setMinimumSize(IDEDimensions.rightEditorMinimumSize);

		final ExpressionParser parser = mModuleContainer.getExpressionParser();
		events = new EditorEvents(mModule, element, parser, this);
		menu = new EditorMenu(surface, this);

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

		surface.createOptions(this);
	}

	public boolean isSaved()
	{
		return isSaved;
	}

	public void setSaved(boolean s)
	{
		isSaved = s;
	}

	public JFrame getFrame()
	{
		return mModuleContainer.getFrame();
	}

	public ControlledSurface getControlledSurface()
	{
		return surface;
	}

	public EditorEvents getEventPane()
	{
		return events;
	}

/*
	public void repaint()
	{
		System.err.println("ComponentEditorPanel.repaint");
		//scrollsurface.invalidate();
		super.repaint();
	}
*/

    public void setDisplayed()
    {
		EditorPanel editorPanel = mModuleContainer.getEditorPanel();
		editorPanel.setRightComponent(this);
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


	public void exportPDF() {}

	public void exportPostscript() {}

	public void printFigure() {}

	public void createEvent()
	{
		final ModuleWindowInterface root = mModuleContainer.getEditorPanel();
		final EditorWindowInterface gedit =
			mModuleContainer.getActiveEditorWindowInterface();
		final EventEditorDialog diag = new EventEditorDialog(root);
		diag.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent event) {
					final NamedSubject decl = diag.getEditedItem();
					final String name = decl.getName();
					final SimpleIdentifierSubject ident =
						new SimpleIdentifierSubject(name);
					final EditorEvents eventpane = gedit.getEventPane();
					final EventTableModel model =
						(EventTableModel) eventpane.getModel();
					model.addIdentifier(ident);
				}
			});
	}

}
