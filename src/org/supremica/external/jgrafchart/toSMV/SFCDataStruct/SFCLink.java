package org.supremica.external.jgrafchart.toSMV.SFCDataStruct;

public class SFCLink
{

	String fromObjectId = null;
	String toObjectId = null;
	public SFCLink(String fromObjectId , String toObjectId)
	{
		this.fromObjectId = fromObjectId;
		this.toObjectId = toObjectId;
	}

	public String getToObjectId()
	{
		return toObjectId;
	}
	public String getFromObjectId()
	{
		return fromObjectId;
	}
}