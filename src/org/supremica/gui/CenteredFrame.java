
// ** MF ********************* CenteredFrame.java *******************//
// ** license, blah blah blah **//
// Heap of useful classes we should all use
package org.supremica.gui;

import javax.swing.*;

// ** This one is useful for all centered frames
// ** It sets the size and the centers on the screen
public class CenteredFrame
	extends JFrame
{
	private static final long serialVersionUID = 1L;

	public CenteredFrame(int width, int height)
	{
		Utility.setupFrame(this, width, height);
	}
}
