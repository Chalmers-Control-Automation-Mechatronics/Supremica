
/*
 * Test of JGo for Grafchart.
 */
package org.jgrafchart;



import com.nwoods.jgo.*;

import java.awt.*;


public class GCLink
	extends JGoLink
{

	private static JGoPen wideLinkPen = new JGoPen(JGoPen.SOLID, 2, new Color(0.0F, 0.0F, 0.0F));

	public GCLink(JGoPort from, JGoPort to)
	{

		super(from, to);

		setOrthogonal(true);
		setSelectable(true);

		// setDraggable(false);
		setResizable(true);
		setGrabChildSelection(true);
	}

	public GCLink()
	{
		super();
	}

	public void setWide()
	{
		setPen(wideLinkPen);
	}

	public void removePointers()
	{

		GCStep s;
		GCTransition t;
		JGoPort fromPort = getFromPort();
		JGoPort toPort = getToPort();

		if (fromPort instanceof GCStepOutPort)
		{
			GCStepOutPort sPort = (GCStepOutPort) fromPort;

			s = (GCStep) sPort.getParent();

			GCTransitionInPort tPort = (GCTransitionInPort) toPort;

			t = (GCTransition) tPort.getParent();

			s.removeSucceedingTransition(t);
			t.removePrecedingStep(s);
		}

		if (fromPort instanceof GCTransitionOutPort)
		{
			GCTransitionOutPort t1Port = (GCTransitionOutPort) fromPort;

			t = (GCTransition) t1Port.getParent();

			GCStepInPort s1Port = (GCStepInPort) toPort;

			s = (GCStep) s1Port.getParent();

			t.removeSucceedingStep(s);
			s.removePrecedingTransition(t);
		}
	}
}
