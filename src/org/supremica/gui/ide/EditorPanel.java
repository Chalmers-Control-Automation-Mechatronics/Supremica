package org.supremica.gui.ide;

import javax.swing.*;
import org.supremica.gui.WhiteScrollPane;

class EditorPanel
	extends WhiteScrollPane
{
	private String title;

	EditorPanel(String title)
	{
		this.title = title;
	}

	public String getTitle()
	{
		return title;
	}

}