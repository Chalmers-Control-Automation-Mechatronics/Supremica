
/******************* TextFrame.java *********************/

// Simple editor implementation for output, especially
// useful during debug. TextWriter encapsulates this
// panel so all writer functions can write to TextPanel
package org.supremica.gui.texteditor;

import java.io.*;
import java.awt.*;
import javax.swing.*;
import org.supremica.gui.Utility;

public class TextFrame
	extends JFrame
{
	private static final long serialVersionUID = 1L;
	TextPanel textpanel = null;
	static final int WIDTH = 500;
	static final int HEIGHT = 600;

	public TextFrame(String title)
	{
		super(title);

		this.textpanel = new TextPanel();

		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(textpanel, BorderLayout.CENTER);
		pack();
		Utility.setupFrame(this, WIDTH, HEIGHT);
		show();
	}

	TextPanel getTextPanel()
	{
		return textpanel;
	}

	public PrintWriter getPrintWriter()
	{
		return new PrintWriter(new TextWriter(textpanel));
	}
}
