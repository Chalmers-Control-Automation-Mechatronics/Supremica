package org.supremica.softplc.Drivers;

import org.supremica.util.SupremicaException;

/**
 *  IO-driver for our Adlink card
 *
 *  @author Niclas Hamp
 *  @author Knut Åkesson
 *  @version 1.0
 */
public class AdlinkPCI7432
	implements org.supremica.softplc.RunTime.DigitalIODriver
{
	private static final short PCI_7432 = 16;
	private static final int nrOfDigitalInputs = 32;
	private static final int nrOfDigitalOutputs = 32;

	private native static short RegisterCard(short cardid, short cardnr);

	private native static void ReleaseCard(short card);

	private native static void WritePort(short card, short channel, int value);

	private native static int ReadPort(short card, short channel);

	private boolean initialized = false;
	private short card = -1;
	private boolean invertInputs = false;
	private boolean invertOutputs = false;
	private static final boolean[] defaultOutputs = new boolean[nrOfDigitalOutputs];
	private static final short nrOfSignalsIn = 32;
	private static final short nrOfSignalsOut = 32;
	private boolean[] outputs = null;

	public AdlinkPCI7432()
		throws Exception
	{
		this(0);
	}

	public AdlinkPCI7432(int cardNumber)
		throws Exception
	{
		this(cardNumber, false, true);
	}

	public AdlinkPCI7432(int cardNumber, boolean invertInputs, boolean invertOutputs)
		throws Exception
	{
		if (cardNumber > Short.MAX_VALUE)
		{
			throw new SupremicaException("Illegal argument");
		}

		card = AdlinkPCI7432.RegisterCard(PCI_7432, (short) cardNumber);

		if (card < 0)
		{
			throw new SupremicaException("RegisterCard failed");
		}

		initialized = true;
		this.invertInputs = invertInputs;
		this.invertOutputs = invertOutputs;

		setSignalArray(defaultOutputs);
	}

	public void releaseCard()
	{
		if (initialized)
		{
			AdlinkPCI7432.ReleaseCard(card);

			initialized = false;
		}
	}

	public static int numberOfDigitalOutputs()
	{
		return nrOfDigitalOutputs;
	}

	public static int numberOfDigitalInputs()
	{
		return nrOfDigitalInputs;
	}

	public synchronized void setSignalArray(boolean[] outputs)
		throws Exception
	{
		if (outputs == null)
		{
			throw new SupremicaException("outputs must be non-null");
		}

		if (outputs.length > nrOfDigitalOutputs)
		{
			throw new SupremicaException("The card has " + nrOfDigitalOutputs + " outputs");
		}

		if (!initialized)
		{
			throw new SupremicaException("The card is not initialized");
		}

		int value = toInt(outputs, invertOutputs);

		this.outputs = outputs;

		//              System.err.println("Wrote: " + value);
		AdlinkPCI7432.WritePort(card, (short) 0, value);
	}

	public synchronized void getSignalArray(boolean[] values)
		throws Exception
	{
		if (!initialized)
		{
			throw new SupremicaException("The card is not initialized");
		}

		int value = AdlinkPCI7432.ReadPort(card, (short) 0);

		fromInt(value, values, invertInputs);
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

		for (int i = 0; i < nrOfSignalsOut; i++)
		{
			temp[i] = new String(Integer.toString(i));
		}

		return temp;
	}

	public String[] getOutputDescriptions()
	{
		String[] temp = new String[nrOfSignalsOut];

		for (int i = 0; i < nrOfSignalsOut; i++)
		{
			temp[i] = new String(Integer.toString(i));
		}

		return temp;
	}

	// help functions
	private static final int toInt(boolean[] values, boolean invert)
	{
		int value = 0;

		for (int i = 0; i < values.length; i++)
		{
			value <<= 1;

			if ((!invert && values[31 - i]) || (invert &&!values[31 - i]))
			{
				value |= 1;
			}
		}

		return value;
	}

	private static final void fromInt(int value, boolean[] values, boolean invert)
	{
		for (int i = 0; i < values.length; i++)
		{
			int currValue = value & 1;

			if (currValue == 0)
			{
				values[i] = false ^ invert;
			}
			else
			{
				values[i] = true ^ invert;
			}

			value >>= 1;
		}
	}

	public static final String toString(boolean[] values)
	{
		StringBuffer sb = new StringBuffer();

		for (int i = 0; i < values.length; i++)
		{
			if (values[i])
			{
				sb.append(i + ": 1 ");
			}
			else
			{
				sb.append(i + ": 0 ");
			}
		}

		return sb.toString();
	}

	public void printInputs()
		throws Exception
	{
		boolean[] inputs = new boolean[nrOfDigitalInputs];

		getSignalArray(inputs);
		System.out.println(AdlinkPCI7432.toString(inputs));
	}

	public void printOutputs()
		throws Exception
	{
		System.out.println(AdlinkPCI7432.toString(outputs));
	}

	public static void main(String[] args)
		throws Exception
	{
		AdlinkPCI7432 driver = new AdlinkPCI7432(0, false, true);
		boolean[] inputs = new boolean[32];
		boolean[] outputs = new boolean[32];

		System.out.println("s 1: for setting output 1");
		System.out.println("r 2: for resetting output 2");
		System.out.println("d: for displaying inputs");
		System.out.println("o: for displaying outputs");
		System.out.println("q: for quitting");

		boolean cont = true;

		while (cont)
		{
			String response = Console.readLine("> ");
			char operation = response.charAt(0);

			if (operation == 's')
			{
				String channelString = response.substring(2, response.length());
				int channel = Integer.parseInt(channelString);

				outputs[channel] = true;

				driver.setSignalArray(outputs);
			}
			else if (operation == 'r')
			{
				String channelString = response.substring(2, response.length());
				int channel = Integer.parseInt(channelString);

				outputs[channel] = false;

				driver.setSignalArray(outputs);
			}
			else if (operation == 'd')
			{
				driver.printInputs();
			}
			else if (operation == 'o')
			{
				driver.printOutputs();
			}
			else if (operation == 'q')
			{
				System.out.println("quitting");

				cont = false;
			}
			else
			{
				System.out.println("Error in command");
			}
		}

		driver.releaseCard();
	}

	static
	{
		System.load("C:/Documents and Settings/cengic/My Documents/devel/Supremica/src/org/supremica/softplc/Drivers/AdlinkPCI7432.dll");
	}
}
