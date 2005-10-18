package org.supremica.external.robotCoordination.RobotStudio;  

import org.supremica.external.comInterfaces.robotstudio_3_1.RobotStudio.*;
import com.inzoom.comjni.ComJniException;
import java.awt.Color;
import org.supremica.external.robotCoordination.*;

import com.inzoom.comjni.Variant;
import com.inzoom.comjni.SafeArray;

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

	public void setColor(Color color)
		throws Exception
	{
		Variant varColor = new Variant(new SafeArray(new int[]{color.getRed(), color.getGreen(), color.getBlue()}), false);
		RSRobotCell.station.getParts().item(RSRobotCell.var(name)).setColor(varColor);

		this.color = color;
	}
}