package org.supremica.external.robotCoordination;

/**
 * A (discretized) coordinate in the simulation environment. The discretization
 * steps dx, dy and dz are specified through the RobotCell interface.
 *
 * @see #RobotCell.setBoxDimensions(double[])
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
		return equals(other);
	}

	public boolean equals(Coordinate other)
	{
		return (x == other.x) && (y == other.y) && (z == other.z);
	}

	public String toString() 
	{
		String str = x + "_" + y + "_" + z;
		return str.replace("-", "m"); 
	}
}
