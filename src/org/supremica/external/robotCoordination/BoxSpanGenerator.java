package org.supremica.external.robotCoordination;

import java.awt.Toolkit;
import java.util.*;

/**
 * Creates the span in the box strategy. This class implements the
 * RobotListener interface and whenever a collision is detected with a
 * box (which is presumed to be a "surfacebox"), that box is deleted
 * (and if some other robot had been there, the box is now a zonebox)
 * and a new layer of "surfaceboxes" is added outside the deleted box.
 */
public class BoxSpanGenerator
    implements RobotListener
{
    Cell cell; 
    Robot robot; 
    Hashtable<Coordinate, Status> matrix; 
    List<Coordinate> zoneBoxes; 
    List<Coordinate> surfaceBoxes;
	
	/**
	 * The BoxSpanGenerator needs a lot of info from the CellExaminer.
	 */
    public BoxSpanGenerator(Cell cell, Robot robot, Hashtable<Coordinate, Status> matrix, 
							List<Coordinate> zoneBoxes, List<Coordinate> surfaceBoxes)
	{
		this.cell = cell;
		this.robot = robot;
		this.matrix = matrix;
		this.zoneBoxes = zoneBoxes;
		this.surfaceBoxes = surfaceBoxes;
	}

    /**
     * Each time a robot collides with a box, add a layer of boxes,
     * (check with the matrix first).
     */
    public void collisionStart(Volume volume, int time)
		throws Exception
    {
		assert(volume instanceof Box);

		Box box = (Box) volume;
		Coordinate coord = box.getCoordinate();
		// Don't need this one anymore
		box.delete();
		
		// If someone has already collided with this box, this is now a zoneBox!!!
		Status status = matrix.get(coord);
		if (status.occupied)
		{
			// Beep and add!
			Toolkit.getDefaultToolkit().beep();
			zoneBoxes.add(coord);
		}
		// No matter what, box has now been occupied
		status.occupied = true;
		assert(status.checked = true);
		status.checked = true;
		
		// Add new surfaceboxes!
		int x = coord.getX();
		int y = coord.getY();
		int z = coord.getZ();
		
		Coordinate newCoord;
		Box newBox;
		Status newStatus;
		
		List<Coordinate> list = new LinkedList<Coordinate>();
		// Down
		list.add(new Coordinate(x,y,z-1));
		// Right
		list.add(new Coordinate(x,y-1,z));
		// Back
		list.add(new Coordinate(x-1,y,z));
		//Forward
		list.add(new Coordinate(x+1,y,z));
		// Left
		list.add(new Coordinate(x,y+1,z));
		// Up
		list.add(new Coordinate(x,y,z+1));

		while (list.size() != 0)
		{
			newCoord = list.remove(0);
			if (!matrix.containsKey(newCoord))
			{
				newStatus = new Status();
				newStatus.occupied = false;
				newStatus.checked = true;
				matrix.put(newCoord, newStatus);
			}
			else
			{
				newStatus = matrix.get(newCoord);
				// If it's already checked, move on!
				if (!newStatus.checked)
				{
					newBox = cell.createBox(newCoord);
					surfaceBoxes.add(newCoord);
				}
			}
		}
	}

    /**
     * Don't care about these right now.
     */
    public void collisionEnd(Volume volume, int time)
    {		
    }
}