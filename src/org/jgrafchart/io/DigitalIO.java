// Knut Åkesson

package org.jgrafchart.io;

public interface DigitalIO
{
	public DigitalInput getInput(int channel)
		throws Exception;

	public DigitalOutput getOutput(int channel)
		throws Exception;
}

