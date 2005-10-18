package org.supremica.external.robotCoordination;

import java.util.*;

public interface Robot
{
    /**
     * Returns true if the robot collides with a given box.
     */
    public boolean collidesWith(Box box);
    
    /**
     * Generates span for a pair of positions.
     */
    public void generateSpan(Position from, Position to)
		throws Exception;
	
    /** 
     * Returnerar Supremica-koordinater för robotens bas 
     */ 
    public Coordinate getBaseCoordinates()
		throws Exception;
	
    /**
     * Returns the home position.
     */
    public Position getHomePosition()
		throws Exception;
	
    /**
     * Returns the name of the robot.
     */
    public String getName()
		throws Exception;
	
    /**
     * Returns list of the positions for this robot.
     */
    public List<Position> getPositions()
		throws Exception;
	
    /**
     * Hides span in simulation environment (just for esthetics)
     */
    public void hideSpan()
		throws Exception;
	
    /**
     * Jumps the robot to position.
     */
    public void jumpToPosition(Position position)
		throws Exception;
	
    /**
     * Initialization of robot, before simulations start.
     */
    public void start()
		throws Exception;
	
    /**
     * Finalization of robot, after completed simulation.
     */
    public void stop()
		throws Exception;
}
