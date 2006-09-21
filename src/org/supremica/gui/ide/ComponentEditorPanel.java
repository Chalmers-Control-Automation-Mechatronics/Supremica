//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: org.supremica.gui.ide
//# CLASS:   ComponentEditorPanel
//###########################################################################
//# $Id: ComponentEditorPanel.java,v 1.24 2006-09-21 14:03:12 robi Exp $
//###########################################################################

package org.supremica.gui.ide;

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

	public ComponentEditorPanel(final ModuleContainer moduleContainer,
								final SimpleComponentSubject element)
	{
		//setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		//setTitle(title);
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

		// final Container panel = getContentPane();
		final GridBagLayout gridbag = new GridBagLayout();
		final GridBagConstraints constraints = new GridBagConstraints();

		constraints.gridy = 0;
		constraints.weighty = 1.0;
		constraints.anchor = GridBagConstraints.NORTH;

		setLayout(gridbag);
		//gridbag.setConstraints(toolbar, constraints);
		//add(toolbar);

		final JScrollPane scrollsurface = new JScrollPane(surface);
		final JScrollPane scrollevents = new JScrollPane(events);
		final JViewport viewevents = scrollevents.getViewport();
		final JSplitPane split = new JSplitPane
			(JSplitPane.HORIZONTAL_SPLIT, scrollevents, scrollsurface);
		viewevents.setBackground(Color.WHITE);

		constraints.weightx = 1.0;
		constraints.fill = GridBagConstraints.BOTH;

		gridbag.setConstraints(split, constraints);
		add(split);
		if (events.getBestWidth() > mModuleContainer.getEditorPanel().getRightComponent().getWidth()/2) {
		    split.setDividerLocation((int)mModuleContainer.getEditorPanel().getRightComponent().getWidth()/2);
		} else {
		    split.setDividerLocation(events.getBestWidth());
		}
		//System.out.println(split.getDividerLocation());
		//System.out.println("MAX :" + split.getMaximumDividerLocation());
		//System.out.println("PREF :" + events.getPreferredSize().getWidth());
		//System.out.println("WIDTH :" + mModuleContainer.getEditorPanel().getRightComponent().getWidth());
		//setJMenuBar(menu);
//		pack();
		setVisible(true);

		this.element = element;

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
