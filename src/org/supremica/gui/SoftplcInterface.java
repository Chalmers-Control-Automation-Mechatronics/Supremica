package org.supremica.gui;

import java.io.File;

public class SoftplcInterface
	extends File
{
	private static final long serialVersionUID = 1L;

	private SoftplcInterface()
	{
		super("");
	}

	public SoftplcInterface(String path)
	{
		super(path);
	}

	public String toString()
	{
		return super.getName();
	}
}
