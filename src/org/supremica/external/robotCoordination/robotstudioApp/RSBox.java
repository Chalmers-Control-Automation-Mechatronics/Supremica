package org.supremica.external.robotCoordination.robotstudioApp;  

import org.supremica.external.comInterfaces.robotstudio_3_1.RobotStudio.*;
import com.inzoom.comjni.ComJniException;
import java.awt.Color;
import org.supremica.external.robotCoordination.*;

/**
 * Representerar en RobotStudio-box.
 */
public class RSBox 
    extends Box 
{
    public RSBox(String name, Coordinate coord, Color color, double transparency) 
	throws ComJniException
    {
	super(name, coord, color, transparency);
    }

    public void delete() 
	throws Exception
    {
	RSRobotCell.station.getParts().item(RSRobotCell.var(name)).delete();
    }
}