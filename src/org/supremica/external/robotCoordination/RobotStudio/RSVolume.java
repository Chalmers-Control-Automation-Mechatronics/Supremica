package org.supremica.external.robotCoordination.RobotStudio;  

import org.supremica.external.comInterfaces.robotstudio_3_1.RobotStudio.*;
import com.inzoom.comjni.ComJniException;
import java.awt.Color;
import org.supremica.external.robotCoordination.*;

import com.inzoom.comjni.Variant;
import com.inzoom.comjni.SafeArray;

/**
 * Represents a RobotStudio volume.
 */
public class RSVolume 
    implements Volume
{
    protected String name;

    public RSVolume(String name)
    {
		this.name = name;
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

    public void setName(String name)
		throws Exception
    {
		getEntity().setName(name);
    }

    public String getName()
    {
		return name;
    }

    protected IEntity getEntity()
		throws Exception
    {
		IPart volumePart = RSCell.station.getParts().item(Converter.var(RSCell.VOLUMEPART_NAME));
		return volumePart.getEntities().item(Converter.var(name));
    }
}