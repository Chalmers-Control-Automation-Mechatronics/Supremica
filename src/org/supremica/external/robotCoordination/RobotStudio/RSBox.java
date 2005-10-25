package org.supremica.external.robotCoordination.RobotStudio;  

import org.supremica.external.comInterfaces.robotstudio_3_1.RobotStudio.*;
import com.inzoom.comjni.ComJniException;
import java.awt.Color;
import org.supremica.external.robotCoordination.*;

import com.inzoom.comjni.Variant;
import com.inzoom.comjni.SafeArray;

/**
 * Represents a RobotStudio box.
 */
public class RSBox
    extends RSVolume
    implements Box
{
//     protected String name;
    protected Coordinate coord;

    public RSBox(String name, Coordinate coord) 
	throws Exception
    {
 	super(name);
// 	this.name = name;
	this.coord = coord;
    }

    protected IEntity getEntity()
	throws Exception
    {
	IPart boxPart = RSCell.station.getParts().item(Converter.var(RSCell.BOXPART_NAME));
	return boxPart.getEntities().item(Converter.var(name));
    }

    public Coordinate getCoordinate() 
    {
	return coord;
    }

    public void setCoordinate(Coordinate coord) 
    {
	this.coord = coord;
    }


}