
// Knut Åkesson
package org.jgrafchart.io.LTH;

import org.jgrafchart.io.DigitalInput;
import se.lth.control.realtime.DigitalIn;

public class LTHDigitalInput
	implements DigitalInput
{
	private DigitalIn digIn = null;
	private int channel;

	public LTHDigitalInput(int channel)
		throws Exception
	{
		this.channel = channel;
		digIn = new DigitalIn(channel);
	}

	public boolean get()
	{
		if (digIn != null)
		{
			return digIn.get();
		}
		else
		{
			System.err.println("digOut == false");

			return false;
		}
	}
}
