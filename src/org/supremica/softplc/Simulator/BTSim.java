package org.supremica.softplc.Simulator;

/**BTSim is the simulator as seen by a SoftPLC
 * @author Anders Röding
 * @version 1.0
 */
public class BTSim
	implements DigitalIODriver
{
	private short nrOfSignalsIn = 32;
	private short nrOfSignalsOut = 32;

	// inputs to simulator [16]
	private boolean[] inSignals = { false, false, false, false, false, false,
									false, false, false, false, false, false,
									false, false, false, false };

	// outputs from simulator [23]
	private boolean[] outSignals = { false, true, false, false, false, false,
									 false, true, false, false, false, false,
									 false, false, false, true, false, false,
									 false, true, true, false, false };

	public BTSim()
	{

		// Start a RouteController thread
		RouteController p = new RouteController(outSignals, this);
		Thread pThread = new Thread(p);

		pThread.start();
	}

	/** setSignalArray sets all signals
	 *  @param values new value for port
	 */
	public synchronized void setSignalArray(boolean[] values)
		throws Exception
	{

		// när skall vi ge exception???
		System.arraycopy(values, 0, inSignals, 0, inSignals.length);
	}

	/** getSignalArray gets all signals
	 *  @return all the values for the signals
	 */
	public void getSignalArray(boolean[] values)
		throws Exception
	{

		// när skall vi ge exception???
		System.arraycopy(outSignals, 0, values, 0, outSignals.length);
	}

	/*
	 * Methods used internally by the simulator
	 */

	/**setOutSignals is used by RouteController
	 * @param values the output signals to be set
	 */
	public synchronized void setOutSignals(boolean[] values)
	{
		System.arraycopy(values, 0, outSignals, 0, outSignals.length);
	}

	/**getInSignals is used by RouteController
	 * @return the signals set by....*/
	public boolean[] getInSignals()
	{
		return inSignals;
	}

	public short getNrOfSignalsIn()
	{
		return nrOfSignalsIn;
	}

	public short getNrOfSignalsOut()
	{
		return nrOfSignalsOut;
	}

	public boolean hasInputDescriptions()
	{
		return true;
	}

	public boolean hasOutputDescriptions()
	{
		return true;
	}

	public String[] getInputDescriptions()
	{
		String[] temp = new String[nrOfSignalsIn];

		temp[0] = new String("KulaPortvakt"); //Names the signals in use
		temp[1] = new String("MätlyftNere");
		temp[2] = new String("KulaMätlyft");
		temp[3] = new String("MätLyftUppe");
		temp[4] = new String("KulaMätstation");
		temp[5] = new String("StorKula");
		temp[6] = new String("LitenKula");
		temp[7] = new String("HissNere");
		temp[8] = new String("KulaHiss");
		temp[9] = new String("HissVån1");
		temp[10] = new String("KulaVån1");
		temp[11] = new String("PlockaVån1");
		temp[12] = new String("HissVån2");
		temp[13] = new String("KulaVån2");
		temp[14] = new String("PlockaVån2");
		temp[15] = new String("ArmHemma");
		temp[16] = new String("ArmVån1");
		temp[17] = new String("ArmVån2");
		temp[18] = new String("KulaFast");
		temp[19] = new String("Autostart");
		temp[20] = new String("ManuellStart");
		temp[21] = new String("Nödstopp");
		temp[22] = new String("LarmKvittering");

		for (int i = 23; i < nrOfSignalsOut; i++) //names the signals NOT in use
			{

			temp[i] = new String(Integer.toString(i));
		}

		return temp;
	}

	public String[] getOutputDescriptions()
	{
		String[] temp = new String[nrOfSignalsOut];

		temp[0] = new String("InPortvakt"); //Names the signals in use
		temp[1] = new String("UrPortvakt");
		temp[2] = new String("UppMätlyft");
		temp[3] = new String("UrMätning");
		temp[4] = new String("Mät");
		temp[5] = new String("UppHissVån1");
		temp[6] = new String("UppHissVån2");
		temp[7] = new String("UtVån1");
		temp[8] = new String("LyftVån1");
		temp[9] = new String("UtVån2");
		temp[10] = new String("LyftVån2");
		temp[11] = new String("UppArmVån1");
		temp[12] = new String("UppArmVån2");
		temp[13] = new String("VridArmHöger");
		temp[14] = new String("Sug");
		temp[15] = new String("TändLampa");

		for (int i = 16; i < nrOfSignalsOut; i++) //Nubmers the signals NOT in use
		{
			temp[i] = new String(Integer.toString(i));
		}
		return temp;
	}
}
