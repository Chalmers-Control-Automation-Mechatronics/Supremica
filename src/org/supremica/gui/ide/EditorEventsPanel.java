package org.supremica.gui.ide;

import javax.swing.*;
import org.supremica.gui.WhiteScrollPane;

class EditorEventsPanel
	extends WhiteScrollPane
{
	private IDE ide;
	private String title;

	EditorEventsPanel(IDE ide, String title)
	{
		this.ide = ide;
		this.title = title;
	}

	public String getTitle()
	{
		return title;
	}

}