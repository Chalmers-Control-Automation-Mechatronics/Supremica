package org.supremica.external.jgrafchart.toSMV.SFCDataStruct;

public class SFCVariable
{
	String name;
	String type;
	String value;
	String initialValue = null;

	public SFCVariable(String name, String type, String value, String initialValue)
	{
		this.name = name;
		this.type = type;
		this.value = value;
		this.initialValue = initialValue;
	}

	public SFCVariable(String name, String type, String value)
	{
		this.name = name;
		this.type = type;
		this.value = value;
	}

	public void setName(String newName)
	{
		name = newName;
	}

	public void setValue(String newValue)
	{
		value = newValue;
	}

	public void setInitialValue(String newInitValue)
	{
		initialValue = newInitValue;
	}

	public void setType(String newType)
	{
		type = newType;
	}

	public String getName()
	{
		return name;
	}

	public String getType()
	{
		return type;
	}

	public String getValue()
	{
		return value;
	}

	public String getInitialValue()
	{
		return initialValue;
	}
}
