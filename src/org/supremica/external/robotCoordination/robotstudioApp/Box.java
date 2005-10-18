package org.supremica.external.robotCoordination.robotstudioApp;  

import org.supremica.external.robotCoordination.*;
import org.supremica.external.comInterfaces.robotstudio_3_1.RobotStudio.*;
import com.inzoom.comjni.ComJniException;
import java.awt.Color;
import org.supremica.external.robotCoordination.*;  

/**
 * Representerar en RobotStudio-box.
 */
public class Box {
    private Coordinate coord;
    private Variant color;
    private double transparency;

    public Box(Coordinate coord, Variant color, double transparency) 
	throws ComJniException
    {
	this.coord = coord;
	this.color = color;
	this.transparency = transparency;
    }
}