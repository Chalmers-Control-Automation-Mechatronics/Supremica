
/*
 * Test of JGo for Grafchart.
 */
package org.jgrafchart;

import com.nwoods.jgo.*;
import java.awt.*;

public class GCStepOutPort
	extends JGoPort
{
	public GCStepOutPort()
	{
		super();

		setSelectable(false);
		setDraggable(false);
		setStyle(StyleRectangle);
		setValidDestination(false);
		setValidSource(true);
	}

	public boolean validLink(JGoPort to)
	{
		boolean valid1 = super.validLink(to);
		boolean valid2 = ((to.getParent() instanceof GCTransition) | (to.getParent() instanceof ParallelJoin) | (to.getParent() instanceof MacroStep));

		// boolean valid2 = ! ((GCIdent)(to.getParent())).isStep();
		// System.out.println("GCLinkValidStep");
		boolean valid = (valid1 && valid2);

		return valid;
	}
}
