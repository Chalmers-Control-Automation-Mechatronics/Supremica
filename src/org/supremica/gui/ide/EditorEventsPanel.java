package org.supremica.gui.ide;

import javax.swing.*;
import org.supremica.gui.WhiteScrollPane;

class EditorEventsPanel
	extends WhiteScrollPane
{
	private String title;
	private ModuleContainer moduleContainer;

	EditorEventsPanel(ModuleContainer moduleContainer, String title)
	{
		this.moduleContainer = moduleContainer;
		this.title = title;
	}

	public String getTitle()
	{
		System.err.println("getTitle: " + title);
		return title;
	}

}