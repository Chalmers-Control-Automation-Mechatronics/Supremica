/******************* TextPanel.java *********************/
// Simple editor implementation for output, especially
// useful during debug. TextWriter encapsulates this
// panel so all writer functions can write to TextPanel

package org.supremica.gui.texteditor;

import java.io.*;
import java.awt.*;
import javax.swing.*;


public class TextPanel
	extends JScrollPane
{
	JTextArea textarea = null;
	
	public TextPanel()
	{
		this("");
	}
	
	public TextPanel(String str)
	{
		this.textarea = new JTextArea(str);

		textarea.setFont(new Font("monospaced", Font.PLAIN, 12));
		textarea.setLineWrap(true);
		textarea.setWrapStyleWord(true);
		textarea.setTabSize(4);
		textarea.setEditable(false);

		setBorder(BorderFactory.createEtchedBorder());		
		getViewport().add(textarea);
	}
	
	public void append(String str)
	{
		textarea.append(str);
	}
	
	JTextArea getTextArea()
	{
		return textarea;
	}
	
	public static void main(String[] args)
	{
		TextFrame textframe = new TextFrame("Testing...");
		
		PrintWriter pw = textframe.getPrintWriter();
		pw.println("Hello World!");
	}

}



