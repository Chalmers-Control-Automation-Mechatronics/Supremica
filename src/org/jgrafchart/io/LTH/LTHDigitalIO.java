// Knut Åkesson

package org.jgrafchart.io.LTH;

import org.jgrafchart.io.DigitalIO;
import org.jgrafchart.io.DigitalInput;
import org.jgrafchart.io.DigitalOutput;

public class LTHDigitalIO
	implements DigitalIO
{
	public LTHDigitalIO()
	{
	}

	public DigitalInput getInput(int channel)
		throws Exception
	{
		return new LTHDigitalInput(channel);
	}

	public DigitalOutput getOutput(int channel)
		throws Exception
	{
		return new LTHDigitalOutput(channel);
	}
}

