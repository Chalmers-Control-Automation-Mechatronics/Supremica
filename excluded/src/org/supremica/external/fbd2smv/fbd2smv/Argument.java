package org.supremica.external.fbd2smv.fbd2smv;

public class Argument
{
	private String name;
	private String type;

	public Argument(String name, String type)
	{
		this.name = name;
		this.type = type;
	}

	public String getName()
	{
		return name;
	}

	public String getType()
	{
		return type;
	}
}
