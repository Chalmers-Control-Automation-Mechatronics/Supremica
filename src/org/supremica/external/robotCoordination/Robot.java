package org.supremica.external.robotCoordination;

import java.util.*;

public interface Robot
{
    /**
     * Returns true if the robot collides with a given box.
     */
    public boolean collidesWith(Box box)
		throws Exception;
    
    /**
     * Generates span for a pair of positions.
     */
    public void generateSpan(Configuration from, Configuration to)
		throws Exception;
	
    /** 
     * Returnerar Supremica-koordinater f�r robotens bas 
     */ 
    public Coordinate getBaseCoordinates()
		throws Exception;
	
    /**
     * Returns the home configuration.
     */
    public Configuration getHomeConfiguration()
		throws Exception;
	
    /**
     * Returns the name of the robot.
     */
    public String getName()
		throws Exception;
	
    /**
     * Returns list of the configurations for this robot.
     */
    public List<Configuration> getConfigurations()
		throws Exception;
	
    /**
     * Hides span in simulation environment (just for esthetics)
     */
    public void hideSpan()
		throws Exception;
	
    /**
     * Jumps the robot to configuration.
     */
    public void jumpToConfiguration(Configuration configuration)
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
