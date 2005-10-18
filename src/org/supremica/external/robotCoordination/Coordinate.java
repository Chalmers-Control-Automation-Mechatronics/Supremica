package org.supremica.external.robotCoordination;

public abstract class Coordinate 
{
	protected int x;
	protected int y;
	protected int z;
	
	Coordinate(int x, int y, int z)
	{
	}
	
	public abstract int getX();
	public abstract int getY();
	public abstract int getZ();

	public int hashCode()
	{
		return x + 229*y + 3571*z;
	}
	
	public boolean equals(Object obj)
	{
		Coordinate other = (Coordinate) obj;
		return (x == other.x) && (y == other.y) && (z == other.z);
	}
}
