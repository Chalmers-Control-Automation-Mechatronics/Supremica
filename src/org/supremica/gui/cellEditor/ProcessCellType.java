package org.supremica.gui.cellEditor;

import java.util.*;

public class ProcessCellType
{
	private static List collection = new LinkedList();
	public static final ProcessCellType Undefined = new ProcessCellType("Undefined");
	public static final ProcessCellType Continuous = new ProcessCellType("Continuous");
	public static final ProcessCellType Discrete = new ProcessCellType("Discrete");
	public static final ProcessCellType Batch = new ProcessCellType("Batch");

	private String identifier;

	private ProcessCellType(String identifier)
	{
		collection.add(this);
		this.identifier = identifier;
	}

	public static Iterator iterator()
	{
		return collection.iterator();
	}

	public String toString()
	{
		return identifier;
	}

	public static ProcessCellType toType(String type)
	{
		if (equalType(Continuous, type))
		{
			return Continuous;
		}

		if (equalType(Discrete, type))
		{
			return Discrete;
		}

		if (equalType(Batch, type))
		{
			return Batch;
		}

		return Undefined;
	}

	public static Object[] toArray()
	{
		return collection.toArray();
	}

	private static boolean equalType(ProcessCellType type, String ident)
	{
		if (type == null || ident == null)
		{
			return false;
		}
		return ident.toLowerCase().equals(type.toString().toLowerCase());
	}

}