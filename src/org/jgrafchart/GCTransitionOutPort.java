
/*
 * Test of JGo for Grafchart.
 */
package org.jgrafchart;



import com.nwoods.jgo.*;

import java.awt.*;


public class GCTransitionOutPort
	extends JGoPort
{

	public GCTransitionOutPort()
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
		boolean valid2 = ((to.getParent() instanceof GCStep) | (to.getParent() instanceof ParallelSplit) | (to.getParent() instanceof MacroStep));

		// boolean valid2 = ! ((GCIdent)(to.getParent())).isTransition();
		// System.out.println("GCLinkValidTrans");
		boolean valid = (valid1 && valid2);

		return valid;
	}
}
