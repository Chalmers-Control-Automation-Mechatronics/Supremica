
// ** MF ********************* Utility.java *******************//
// ** license, blah blah blah **//
// Heap of useful classes we should all use
package org.supremica.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

// ** And the utility class that contains the functions that do the real job
public class Utility
{

	// Returns a point for the upper left corner of a centered component of size comp_d
	static Point getPosForCenter(Dimension comp_d)
	{
		Toolkit tool_kit = Toolkit.getDefaultToolkit();
		Dimension screen_d = tool_kit.getScreenSize();

		return new Point((screen_d.width - comp_d.width) / 2, (screen_d.height - comp_d.height) / 2);
	}

	static void setupFrame(JFrame frame, int width, int height)
	{

		// Toolkit tool_kit = Toolkit.getDefaultToolkit();
		// Dimension d = tool_kit.getScreenSize();
		frame.setSize(width, height);                 // from Component
		frame.setLocation(getPosForCenter(new Dimension(width, height)));    // from Component
		frame.setIconImage(Supremica.cornerImage);    // from Frame
	}

	static void setupPane(JScrollPane pane)
	{
		pane.getViewport().setBackground(Color.white);
	}
	
	static JButton setDefaultButton(JFrame frame, JButton b)
	{
		frame.getRootPane().setDefaultButton(b);
		return b;
	}
}
