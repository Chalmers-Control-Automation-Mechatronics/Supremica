//** MF ********************* Utility.java *******************//
//** license, blah blah blah **//

// Heap of useful classes we should all use
package org.supremica.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

//** This one is useful for all centered frames
//** It sets the size and the centers on the screen
class CenteredFrame extends JFrame
{
	public CenteredFrame(int width, int height)
	{
		Utility.setupFrame(this, width, height);
	}
}
