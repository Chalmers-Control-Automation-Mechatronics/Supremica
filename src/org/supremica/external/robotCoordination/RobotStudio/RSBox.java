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
		throws Exception
	{
		super(name, coord, color, transparency);
	}

    public void delete() 
		throws Exception
    {
		getEntity().delete();
    }

	public void setColor(Color color)
		throws Exception
	{
		Variant varColor = new Variant(new SafeArray(new int[] {color.getRed(), color.getGreen(), color.getBlue()}), false);
		getEntity().setColor(varColor);
	}
	
	public void setTransparency(double transparency)
		throws Exception
	{
		getEntity().setRelativeTransparency((float) transparency);
	}

	protected IEntity getEntity()
		throws Exception
	{
		IPart boxPart = RSCell.station.getParts().item(Converter.var(RSCell.BOXPART_NAME));
		return boxPart.getEntities().item(Converter.var(name));
	}
}