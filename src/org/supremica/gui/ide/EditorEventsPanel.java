package org.supremica.gui.ide;

import javax.swing.*;
import java.util.*;
import java.awt.event.*;
import org.supremica.gui.WhiteScrollPane;
import net.sourceforge.waters.gui.EventListCell;
import net.sourceforge.waters.model.module.*;

class EditorEventsPanel
	extends WhiteScrollPane
	implements ActionListener
{
	private static final long serialVersionUID = 1L;

	private String name;
	private ModuleContainer moduleContainer;

	EditorEventsPanel(ModuleContainer moduleContainer, String name)
	{
		this.moduleContainer = moduleContainer;
		this.name = name;
		createEventsPane();
	}

	public String getName()
	{
		//System.err.println("getTitle: " + title);
		return name;
	}

	public void createEventsPane()
	{
		final ArrayList l;

		DefaultListModel data = new DefaultListModel();

		ModuleProxy module = moduleContainer.getModuleProxy();

		if (module != null)
		{
			for (int i = 0; i < module.getEventDeclList().size(); i++)
			{
				data.addElement(((EventDeclProxy) (module.getEventDeclList().get(i))));
			}
		}

		JList dataList = new JList(data);

		dataList.setCellRenderer(new EventListCell());

		getViewport().add(dataList);
/*
		JButton NewEventButton = new JButton("New Event");

		NewEventButton.setActionCommand("newevent");
		NewEventButton.addActionListener(this);

		JButton DeleteEventButton = new JButton("Delete Event");

		DeleteEventButton.setActionCommand("delevent");
		DeleteEventButton.addActionListener(this);

		Box jp = new Box(BoxLayout.PAGE_AXIS);
		JPanel p = new JPanel();

		p.add(NewEventButton);
		p.add(DeleteEventButton);
		jp.add(new JScrollPane(dataList));
		jp.add(p);

		p = new JPanel();

		p.add(jp);
		p.setLayout(new GridLayout(1, 1));

		return p;
*/
	}

	public void actionPerformed(ActionEvent e)
	{
	}
}