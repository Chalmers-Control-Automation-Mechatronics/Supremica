package org.supremica.softplc.Simulator;

/**BTSim is the simulator as seen by a SoftPLC
 * @author Anders Röding
 * @version 1.0
 */
public class BTSim
	implements org.supremica.softplc.RunTime.DigitalIODriver
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

		temp[0] = new String("0 - KulaPortvakt"); //Names the signals in use
		temp[1] = new String("1 - MätlyftNere");
		temp[2] = new String("2 - KulaMätlyft");
		temp[3] = new String("3 - MätLyftUppe");
		temp[4] = new String("4 - KulaMätstation");
		temp[5] = new String("5 - StorKula");
		temp[6] = new String("6 - LitenKula");
		temp[7] = new String("7 - HissNere");
		temp[8] = new String("10 - KulaHiss");
		temp[9] = new String("11 - HissVån1");
		temp[10] = new String("12 - KulaVån1");
		temp[11] = new String("13 - PlockaVån1");
		temp[12] = new String("14 - HissVån2");
		temp[13] = new String("15 - KulaVån2");
		temp[14] = new String("16 - PlockaVån2");
		temp[15] = new String("17 - ArmHemma");
		temp[16] = new String("20 - ArmVån1");
		temp[17] = new String("21 - ArmVån2");
		temp[18] = new String("22 - KulaFast");
		temp[19] = new String("23 - Autostart");
		temp[20] = new String("24 - ManuellStart");
		temp[21] = new String("25 - Nödstopp");
		temp[22] = new String("26 - LarmKvittering");

		for (int i = 23; i < nrOfSignalsOut; i++) //names the signals NOT in use
			{

			temp[i] = new String(Integer.toString(i));
		}

		return temp;
	}

	public String[] getOutputDescriptions()
	{
		String[] temp = new String[nrOfSignalsOut];

		temp[0] = new String("0 - InPortvakt"); //Names the signals in use
		temp[1] = new String("1 - UrPortvakt");
		temp[2] = new String("2 - UppMätlyft");
		temp[3] = new String("3 - UrMätning");
		temp[4] = new String("4 - Mät");
		temp[5] = new String("5 - UppHissVån1");
		temp[6] = new String("6 - UppHissVån2");
		temp[7] = new String("7 - UtVån1");
		temp[8] = new String("10 - LyftVån1");
		temp[9] = new String("11 - UtVån2");
		temp[10] = new String("12 - LyftVån2");
		temp[11] = new String("13 - UppArmVån1");
		temp[12] = new String("14 - UppArmVån2");
		temp[13] = new String("15 - VridArmHöger");
		temp[14] = new String("16 - Sug");
		temp[15] = new String("17 - TändLampa");

		for (int i = 16; i < nrOfSignalsOut; i++) //Nubmers the signals NOT in use
		{
			temp[i] = new String(Integer.toString(i));
		}
		return temp;
	}
}
