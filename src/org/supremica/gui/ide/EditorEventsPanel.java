//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: org.supremica.gui.ide
//# CLASS:   EditorEventsPanel
//###########################################################################
//# $Id: EditorEventsPanel.java,v 1.12 2006-08-09 02:53:58 robi Exp $
//###########################################################################


package org.supremica.gui.ide;

import java.awt.datatransfer.*;
import java.awt.dnd.*;
import javax.swing.JList;
import javax.swing.ListModel;

import net.sourceforge.waters.gui.EventEditorDialog;
import net.sourceforge.waters.gui.EventListCell;
import net.sourceforge.waters.gui.IndexedListModel;
import net.sourceforge.waters.subject.base.IndexedListSubject;
import net.sourceforge.waters.subject.module.EventDeclSubject;
import net.sourceforge.waters.subject.module.ModuleSubject;
import org.supremica.gui.WhiteScrollPane;


class EditorEventsPanel
	extends WhiteScrollPane
{
	private static final long serialVersionUID = 1L;

	private String name;
	private ModuleContainer moduleContainer;

	private JList mEventList = null;
	private ListModel mEventListModel = null;
	private boolean modified = true;

	EditorEventsPanel(ModuleContainer moduleContainer, String name)
	{
		this.moduleContainer = moduleContainer;
		this.name = name;
		createEventsPane();
	}

	public String getName()
	{
		return name;
	}

	public void createEventsPane()
	{
		ModuleSubject module = moduleContainer.getModule();
		final IndexedListSubject<EventDeclSubject> events =
			module.getEventDeclListModifiable();
		mEventListModel = new IndexedListModel<EventDeclSubject>(events);
		mEventList = new JList(mEventListModel);
		mEventList.setCellRenderer(new EventListCell());
		getViewport().add(mEventList);
/*
		mDragSource = DragSource.getDefaultDragSource();
		mDGListener = new DGListener(mEventList);
		mDSListener = new DSListener();

		// component, action, listener
		mDragSource.createDefaultDragGestureRecognizer(mEventList,
													   mDragAction,
													   mDGListener);
		mDragSource.addDragSourceListener(mDSListener);

*/
	}

	public void addEvent()
	{
		final EditorPanel panel = moduleContainer.getEditorPanel();
        new EventEditorDialog(panel, true, false);
	}

}
