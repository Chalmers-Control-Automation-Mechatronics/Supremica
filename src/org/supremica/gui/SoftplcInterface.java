package org.supremica.gui;

import java.io.File;

public class SoftplcInterface
	extends File
{
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

	/** 
	 * Why does this class have to implement this method!!!? It's because 
	 * File says it implements Comparable<File>, I guess? 
	 */
	public int compareTo(Object object)
	{
		return super.compareTo((File) object);
	}
}
