package org.supremica.gui.cellEditor;

public class ProcessCell
{
	protected String name;
	protected Units theUnits;
	protected ProcessCellType theType;

	public ProcessCell(String name)
	{
		this.name = name;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}
}
