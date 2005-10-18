package org.supremica.external.robotCoordination;  

import org.supremica.external.comInterfaces.robotstudio_3_1.RobotStudio.*;
import com.inzoom.comjni.ComJniException;
import java.awt.Color;

/**
 * Representerar en RobotStudio-box.
 */
public class Box {
    private Coordinate coord;
    private Color color;
    private double transparency;

    public Box(Coordinate coord, Color color, double transparency) 
	throws ComJniException
    {
	this.coord = coord;
	this.color = color;
	this.transparency = transparency;
    }
}