//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: org.supremica.gui.ide
//# CLASS:   EditorEventsPanel
//###########################################################################
//# $Id: EditorEventsPanel.java,v 1.16 2006-09-24 20:40:49 knut Exp $
//###########################################################################


package org.supremica.gui.ide;

import javax.swing.JList;
import javax.swing.ListModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

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

	EditorModuleEventPopupMenu popup = new EditorModuleEventPopupMenu();
	PopupListener popupListener;


	EditorEventsPanel(ModuleContainer moduleContainer, String name)
	{
		this.moduleContainer = moduleContainer;
		this.name = name;
		final ModuleSubject module = moduleContainer.getModule();
		mEventList = new EventDeclListView(module);
		getViewport().add(mEventList);

		popupListener = new PopupListener();
		addMouseListener(popupListener);
		mEventList.addMouseListener(popupListener);
	}

	public String getName()
	{
		return name;
	}

	public void addModuleEvent()
	{
		final EditorPanel panel = moduleContainer.getEditorPanel();
        new EventEditorDialog(panel, false, false);
	}

	public void addComponentEvent()
	{
		final EditorPanel panel = moduleContainer.getEditorPanel();
        new EventEditorDialog(panel, false, false);
	}

	class PopupListener extends MouseAdapter {
		public void mousePressed(MouseEvent e)
		{
			maybeShowPopup(e);
		}

		public void mouseReleased(MouseEvent e)
		{
			maybeShowPopup(e);
		}

		private void maybeShowPopup(MouseEvent e)
		{
//			System.err.println("maybeShowPopup");
			if (e.isPopupTrigger()) {
				popup.show(e.getComponent(),
						   e.getX(), e.getY());
			}
		}
	}
}
