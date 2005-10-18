package org.supremica.external.robotCoordination;

/**
 * A (discretized) coordinate in the simulation environment.
 */
public class Coordinate 
{
	protected int x;
	protected int y;
	protected int z;
	
	public Coordinate(int x, int y, int z)
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

	public String toString() 
	{
		String str = "";

		if (x < 0)
			str += "n" + Math.abs(x);
		else
			str += x;

		if (y < 0)
			str += "_n" + Math.abs(y);
		else
			str += "_" + y;

		if (z < 0)
			str += "_n" + Math.abs(z);
		else
			str += "_" + z;

		return str;
	}
}
