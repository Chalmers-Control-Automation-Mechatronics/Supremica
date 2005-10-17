package org.supremica.external.robotCoordination.robotstudioApp;

import org.supremica.external.comInterfaces.robotstudio_3_1.RobotStudio.IPosition;
import org.supremica.external.robotCoordination.Coordinate;
import com.inzoom.comjni.ComJniException;

/**  
 * Lagrar en Supremica-koordinat
 */ 
public class RSCoordinate 
    implements Coordinate
{
    private double x,y,z;
    
    public RSCoordinate(double x, double y, double z) 
	{
		this.x = x;
		this.y = y;
		this.z = z;
    }
	
    public RSCoordinate(IPosition pos) throws ComJniException 
	{
		this.x = pos.getX();
		this.y = pos.getY();
		this.z = pos.getZ();
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
	
    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
    public void setZ(double z) { this.z = z; }
}