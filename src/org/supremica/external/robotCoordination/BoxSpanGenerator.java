package org.supremica.external.robotCoordination;

import java.util.*;
import org.supremica.log.*;

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
	private static Logger logger = LoggerFactory.createLogger(BoxSpanGenerator.class);

    Cell cell; 
    Robot robot; 
    Hashtable<Coordinate, Status> matrix; 
    List<Coordinate> zoneboxes; 
    List<Coordinate> surfaceboxes;
	
	/**
	 * The BoxSpanGenerator needs a lot of info from the CellExaminer.
	 */
    public BoxSpanGenerator(Cell cell, Robot robot, Hashtable<Coordinate, Status> matrix, 
							List<Coordinate> zoneboxes, List<Coordinate> surfaceboxes)
	{
		this.cell = cell;
		this.robot = robot;
		this.matrix = matrix;
		this.zoneboxes = zoneboxes;
		this.surfaceboxes = surfaceboxes;
	}

    /**
     * Each time a robot collides with a box, add a layer of boxes,
     * (check with the matrix first).
     */
    public void collisionStart(Volume volume, int time)
		throws Exception
    {
		if (!(volume instanceof Box))
		{
			logger.warn("Caution! Robot " + robot + " unexpectedly collided with the object " + volume + 
						". If this is a static object in the cell, more advanced path planning " + 
						"is needed to avoid that object. This path should not be used!");
			return;
		}

		// So, it's a box!
		Box box = (Box) volume;
		Coordinate coord = box.getCoordinate();
		// Don't need this one anymore
		box.delete();
		
		// If someone has already collided with this box, this is now a zoneBox!!!
		Status status = matrix.get(coord);
		if (status.occupied)
		{
			// Beep and add!
			zoneboxes.add(coord);
			CellExaminer.beep();
		}
		// No matter what, box has now been occupied
		status.occupied = true;
		
		// Add new surfaceboxes!
		int x = coord.getX();
		int y = coord.getY();
		int z = coord.getZ();
		
		Coordinate newCoord;
		Box newBox;
		Status newStatus;
		
		List<Coordinate> list = new LinkedList<Coordinate>();
		// Up
		list.add(new Coordinate(x,y,z+1));
		// Down
		list.add(new Coordinate(x,y,z-1));
		// Left
		list.add(new Coordinate(x,y+1,z));
		// Right
		list.add(new Coordinate(x,y-1,z));
		// Forward
		list.add(new Coordinate(x+1,y,z));
		// Back
		list.add(new Coordinate(x-1,y,z));
		// Add boxes for these coordinates
		loop: while (list.size() != 0)
		{
			newCoord = list.remove(0);

			// If this is a coordinate that has never been examined
			// before or that has not been checked for this robot,
			// then create a new box!
			if (!matrix.containsKey(newCoord))
			{
				newStatus = new Status();
				newStatus.occupied = false;
				matrix.put(newCoord, newStatus);
			}
			else
			{
				newStatus = matrix.get(newCoord);
				// If it's already checked, move on!
				if (newStatus.checked)
				{
					continue loop;
				}
			}
			newBox = cell.createBox(newCoord);
			newBox.setColor(CellExaminer.BLACK);
			newBox.setTransparency(CellExaminer.TRANSPARENCY);
			newStatus.checked = true;
			surfaceboxes.add(newCoord);	
		}
	}

    /**
     * Don't care about these right now.
     */
    public void collisionEnd(Volume volume, int time)
    {		
    }
}