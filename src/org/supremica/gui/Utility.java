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
//** This one sets its own background to white
class WhitePane extends JScrollPane
{
	private void setWhite()
	{
		Utility.setupPane(this);;
	}
	
	public WhitePane()
	{
		setWhite();
	}
	
	public WhitePane(Component view)
	{
		super(view);
		setWhite();
	}
	
	public WhitePane(Component view, int vsbPolicy, int hsbPolicy)
	{
		super(view, vsbPolicy, hsbPolicy);
		setWhite();
	}
	
	public WhitePane(int vsbPolicy, int hsbPolicy) 
	{
		super(vsbPolicy, hsbPolicy);
		setWhite();
	}
}
//** And the utility class that contains the functions that do the real job
public class Utility
{
	static void setupFrame(JFrame frame, int width, int height)
	{
		Toolkit tool_kit = Toolkit.getDefaultToolkit();
		Dimension d = tool_kit.getScreenSize();
		frame.setSize(width, height);	// from Component
		frame.setLocation((d.width-width)/2, (d.height-height)/2); // from Component
		frame.setIconImage(Supremica.cornerImage);	// from Frame

	}
	static void setupPane(JScrollPane pane)
	{
		pane.getViewport().setBackground(Color.white);
	}
}