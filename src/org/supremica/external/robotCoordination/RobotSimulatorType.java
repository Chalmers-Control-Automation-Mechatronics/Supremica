package org.supremica.external.robotCoordination;

import java.util.*;

public class RobotSimulatorType
{
	private static List collection = new LinkedList();
	public static final RobotSimulatorType Undefined = new RobotSimulatorType("Undefined", false);
	public static final RobotSimulatorType RobotStudio = new RobotSimulatorType("RobotStudio", true);
	private String identifier;

	private RobotSimulatorType(String identifier, boolean add)
	{
		if (add)
		{
			collection.add(this);
		}

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

	public static RobotSimulatorType toType(String type)
	{
		if (equalType(RobotStudio, type))
		{
			return RobotStudio;
		}

		return Undefined;
	}

	public static Object[] toArray()
	{
		return collection.toArray();
	}

	private static boolean equalType(RobotSimulatorType type, String ident)
	{
		if ((type == null) || (ident == null))
		{
			return false;
		}

		return ident.toLowerCase().equals(type.toString().toLowerCase());
	}
}
