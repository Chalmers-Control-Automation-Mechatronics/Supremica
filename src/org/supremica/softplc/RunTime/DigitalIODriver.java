package org.supremica.softplc.RunTime;

/**
 *  Interface that our specific card drivers
 *  have to implement
 *  @author Niclas Hamp
 *  @version 1.0
 *  @author Anders Röding //some minor changes
 */
public interface DigitalIODriver
{

	/**
	 *  setSignal sets a specific signal on the digital I/O-card
	 *  @param signal which port we would like to set
	 *  @param value new value for port
	 */

	// void setSignal(int signal, boolean value) throws Exception;

	/** setSignalArray sets all output signals on the I/O-card
	 *  @param values new values for outputs
	 */
	void setSignalArray(boolean[] values)
		throws Exception;

	/** get a specific signal
	 *  @param signal which port we would like to set
	 *  @return value for port
	 */

	// boolean getSignal(int signal) throws Exception;

	/** getSignal gets the values of all input signals
	 *  @param values for the input signals
	 */
	void getSignalArray(boolean[] values)
		throws Exception;

	short getNrOfSignalsIn();

	short getNrOfSignalsOut();

	boolean hasInputDescriptions();

	boolean hasOutputDescriptions();

	String[] getInputDescriptions();

	String[] getOutputDescriptions();
}
