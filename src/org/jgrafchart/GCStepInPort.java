
/*
 * Test of JGo for Grafchart.
 */
package org.jgrafchart;

import com.nwoods.jgo.*;
import java.awt.*;

public class GCStepInPort
	extends JGoPort
{
	public GCStepInPort()
	{
		super();

		setSelectable(false);
		setDraggable(false);

		// setStyle(StyleRectangle);
		setValidDestination(true);
		setValidSource(false);
	}
}
