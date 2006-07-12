package org.supremica.gui.ide;

import javax.swing.*;
import java.util.*;
import java.awt.event.*;
import org.supremica.gui.WhiteScrollPane;
import net.sourceforge.waters.gui.EventListCell;
import net.sourceforge.waters.gui.EventTableModel;
import net.sourceforge.waters.model.module.*;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.EventDeclSubject;
import net.sourceforge.waters.subject.module.SimpleIdentifierSubject;
import java.awt.datatransfer.*;
import java.awt.dnd.*;

class EditorEventsPanel
	extends WhiteScrollPane
	implements ActionListener
{
	private static final long serialVersionUID = 1L;

	private String name;
	private ModuleContainer moduleContainer;

	private JList dataList = null;
	private DefaultListModel data = null;
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
		final ArrayList l;
		ModuleSubject module = moduleContainer.getModule();

		data = new DefaultListModel();
		if (module != null)
		{
			for (final EventDeclProxy event : module.getEventDeclList())
			{
				data.addElement(event);
			}
		}

		dataList = new JList(data);
		dataList.setCellRenderer(new EventListCell());
		getViewport().add(dataList);
/*
		mDragSource = DragSource.getDefaultDragSource();
		mDGListener = new DGListener(dataList);
		mDSListener = new DSListener();

		// component, action, listener
		mDragSource.createDefaultDragGestureRecognizer(dataList,
													   mDragAction,
													   mDGListener);
		mDragSource.addDragSourceListener(mDSListener);

*/
	}

	public DefaultListModel getEventDataList()
	{
		return data;
	}

	public void addEvent()
	{
		EventEditorDialog editor = new EventEditorDialog(moduleContainer.getEditorPanel().getEditorPanelInterface());

		EventDeclSubject newEvent = editor.getEventDeclSubject();
		if (newEvent != null)
		{
			//ModuleSubject module = moduleContainer.getModule();
			//module.getEventDeclListModifiable().add(newEvent); // Add it to the model
			data.addElement(newEvent); // Add it to the UI

			//final EventTableModel model = (EventTableModel) moduleContainer.getEditorPanel();
			//final EventTableModel model = (EventTableModel) mEventPane.getModel();

			//EventTableModel model = (EventTableModel) moduleContainer.getComponentEditorPanel().getEventPane().getModel();
			EventTableModel model = (EventTableModel) moduleContainer.getActiveEditorWindowInterface().getEventPane().getModel();

			SimpleIdentifierSubject identifier = new SimpleIdentifierSubject(newEvent.getName());
			model.addIdentifier(identifier);
		}
	}

	public void actionPerformed(ActionEvent e)
	{
	}


}
