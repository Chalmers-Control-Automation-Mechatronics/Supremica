package org.supremica.gui.ide;

import javax.swing.*;
import org.supremica.gui.WhiteScrollPane;

class EditorParametersPanel
	extends WhiteScrollPane
{
	private IDE ide;
	private ModuleContainer moduleContainer;
	private String title;

	EditorParametersPanel(IDE ide, ModuleContainer moduleContainer, String title)
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