package org.supremica.gui.cellEditor;

import javax.swing.*;
import org.supremica.gui.VisualProject;


public class CellEditor
	extends JFrame
{
	protected VisualProject theProject = null;

	public CellEditor(VisualProject theProject)
	{
		this.theProject = theProject;
	}

	public static CellEditor createEditor(VisualProject theProject)
	{
		return new CellEditor(theProject);
	}
}