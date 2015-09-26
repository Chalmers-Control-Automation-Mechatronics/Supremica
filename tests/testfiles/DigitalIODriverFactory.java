/*
 * Created on Apr 27, 2004
 *
 */
import org.supremica.softplc.RunTime.DigitalIODriver;
import org.supremica.softplc.Simulator.BTSim;
/**
 * @author torda
 * This class is used by BallProcess.java
 * By letting BallProcess use this class,
 * an animation window that simulates the
 * ball process (kulbanan) will appear.
 */
public class DigitalIODriverFactory {
	public static DigitalIODriver createDigitalIODriver()
	{
		return new BTSim();
	}
}
