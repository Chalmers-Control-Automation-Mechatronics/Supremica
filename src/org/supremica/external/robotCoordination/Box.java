package org.supremica.external.robotCoordination;  

import org.supremica.external.comInterfaces.robotstudio_3_1.RobotStudio.*;
import com.inzoom.comjni.ComJniException;
import java.awt.Color;

/**
 * Represents an abstract box.
 */
public abstract class Box {
    protected Coordinate coord;
    protected Color color;
    protected double transparency;
    protected String name;
	
    public Box(String name, Coordinate coord, Color color, double transparency) 
		throws ComJniException
    {
		this.name = name;
		this.coord = coord;
		this.color = color;
		this.transparency = transparency;
    }
    
    public Coordinate getCoordinate() { return coord; }

    public abstract void delete() throws Exception;
}