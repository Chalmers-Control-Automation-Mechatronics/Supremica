package org.supremica.external.robotCoordination;

public class Coordinate 
{
	protected int x;
	protected int y;
	protected int z;
	
	Coordinate(int x, int y, int z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public int getX() { return x; };
	public int getY() { return y; };
	public int getZ() { return z; };

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
