package org.supremica.gui.ide;

import javax.swing.*;
import org.supremica.gui.WhiteScrollPane;

class EditorEventsPanel
	extends WhiteScrollPane
{
	private IDE ide;
	private String title;
	private ModuleContainer moduleContainer;

	EditorEventsPanel(IDE ide, ModuleContainer moduleContainer, String title)
	{
		this.ide = ide;
		this.moduleContainer = moduleContainer;
		this.title = title;
	}

	public String getTitle()
	{
		return title;
	}

}