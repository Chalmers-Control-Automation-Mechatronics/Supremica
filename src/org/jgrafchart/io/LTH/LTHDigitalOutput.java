
// Knut Åkesson
package org.jgrafchart.io.LTH;

import org.jgrafchart.io.DigitalOutput;
import se.lth.control.realtime.DigitalOut;

public class LTHDigitalOutput
	implements DigitalOutput
{
	private DigitalOut digOut = null;
	private int channel;

	public LTHDigitalOutput(int channel)
		throws Exception
	{
		this.channel = channel;
		digOut = new DigitalOut(channel);
	}

	public void set(boolean value)
	{
		if (digOut != null)
		{
			digOut.set(value);
		}
		else
		{
			System.err.println("digOut == null");
		}
	}
}
