
// Knut Åkesson
package org.jgrafchart.io;

public interface DigitalIO
{
	DigitalInput getInput(int channel)
		throws Exception;

	DigitalOutput getOutput(int channel)
		throws Exception;
}
