package org.supremica.external.robotCoordination;

public class CollisionData
{
	private Volume volume;
	private boolean isEntering;
	private int time;	

	public CollisionData(Volume volume, boolean isEntering, int time)
	{
		this.volume = volume;
		this.isEntering = isEntering;
		this.time = time;
	}

	public Volume getVolume()
	{
		return volume;
	}

	public boolean isEntering()
	{
		return isEntering;
	}

	public int getTime()
	{
		return time;
	}

	public String toString()
	{
		return "Volume: " + volume + ", isEntering: " + isEntering + ", time: " + time;
	}
}