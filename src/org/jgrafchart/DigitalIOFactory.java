// Knut Åkesson

package org.jgrafchart;

import org.jgrafchart.io.*;
import org.jgrafchart.io.LTH.*;

public class DigitalIOFactory
{
	private static DigitalIO digIO = null;

	public synchronized static DigitalIO getDigitialIO()
	{
		if (digIO == null)
		{
			digIO = new LTHDigitalIO();
		}
		return digIO;
	}
}
