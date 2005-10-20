package org.supremica.external.robotCoordination;  

//import org.supremica.external.comInterfaces.robotstudio_3_1.RobotStudio.*;
import com.inzoom.comjni.ComJniException;
import java.awt.Color;

/**
 * Represents an abstract box.
 */
public abstract class Box 
{
    protected String name;
    protected Coordinate coord;
	
    public Box(String name, Coordinate coord) 
		throws Exception
    {
		this.name = name;
		this.coord = coord;
    }
    	
    public Coordinate getCoordinate() { return coord; }

    public abstract void delete() throws Exception;

	public abstract void setColor(Color color) throws Exception;

	public abstract void setTransparency(double transparency) throws Exception;

	public String getName()
	{
		return name;
	}
}