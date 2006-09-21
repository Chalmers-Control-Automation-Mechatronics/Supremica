//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: org.supremica.gui.ide
//# CLASS:   EditorEventsPanel
//###########################################################################
//# $Id: EditorEventsPanel.java,v 1.13 2006-09-21 16:42:13 robi Exp $
//###########################################################################


package org.supremica.gui.ide;

import javax.swing.JList;
import javax.swing.ListModel;

import net.sourceforge.waters.gui.EventEditorDialog;
import net.sourceforge.waters.gui.EventDeclListView;
import net.sourceforge.waters.subject.module.ModuleSubject;
import org.supremica.gui.WhiteScrollPane;


class EditorEventsPanel
	extends WhiteScrollPane
{
	private static final long serialVersionUID = 1L;

	private String name;
	private ModuleContainer moduleContainer;

	private final JList mEventList;


	EditorEventsPanel(ModuleContainer moduleContainer, String name)
	{
		this.moduleContainer = moduleContainer;
		this.name = name;
		final ModuleSubject module = moduleContainer.getModule();
		mEventList = new EventDeclListView(module);
		getViewport().add(mEventList);
	}

	public String getName()
	{
		return name;
	}

	public void addEvent()
	{
		final EditorPanel panel = moduleContainer.getEditorPanel();
        new EventEditorDialog(panel, true, false);
	}

}
