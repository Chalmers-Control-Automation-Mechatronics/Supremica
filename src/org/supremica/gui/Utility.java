
// ** MF ********************* Utility.java *******************//
// ** license, blah blah blah **//
// Heap of useful stuff we should all use
package org.supremica.gui;

import java.awt.*;
import javax.swing.*;

// ** And the utility class that contains the functions that do the real job
public class Utility
{

	// Returns a point for the upper left corner of a centered component of size comp_d
	public static Point getPosForCenter(Dimension comp_d)
	{
		Toolkit tool_kit = Toolkit.getDefaultToolkit();
		Dimension screen_d = tool_kit.getScreenSize();

		return new Point((screen_d.width - comp_d.width) / 2, (screen_d.height - comp_d.height) / 2);
	}

	public static void setupFrame(JFrame frame, int width, int height)
	{
		frame.setSize(width, height);                 // from Component
		frame.setLocation(getPosForCenter(new Dimension(width, height)));    // from Component
		frame.setIconImage(Supremica.cornerImage);    // from Frame
	}

	public static void setupPane(JScrollPane pane)
	{
		pane.getViewport().setBackground(Color.white);
	}

	public static void setupDialog(JDialog dialog, int width, int height)
	{
		dialog.setSize(width, height);                 // from Component
		dialog.setLocation(getPosForCenter(new Dimension(width, height)));    // from Component
		// dialog.setIconImage(Supremica.cornerImage);    // from Frame
	}

	public static JButton setDefaultButton(JFrame frame, JButton b)
	{
		frame.getRootPane().setDefaultButton(b);
		return b;
	}

	public static JButton setDefaultButton(JDialog dialog, JButton b)
	{
		dialog.getRootPane().setDefaultButton(b);
		return b;
	}

	public static JButton setDisabledButton(JFrame frame, JButton b)
	{
		b.setEnabled(false);
		return b;
	}



}
