/******************* TextPanel.java *********************/
// Simple editor implementation for output, especially
// useful during debug. TextWriter encapsulates this
// panel so all writer functions can write to TextPanel

package org.supremica.gui.texteditor;

import java.io.*;
import java.awt.*;
import java.lang.*;
import java.util.*;
import javax.swing.*;


public class TextPanel
	extends JScrollPane
{
	JTextArea textarea = null;
	
	public TextPanel(int width, int height)
	{
		this("", width, height);
	}
	
	public TextPanel(String str, int width, int height)
	{
		this.textarea = new JTextArea(str);
		init(width, height);
	}
	
	private void init(int width, int height)
	{
		textarea.setFont(new Font("monospaced", Font.PLAIN, 12));
		textarea.setLineWrap(true);
		textarea.setWrapStyleWord(true);
		textarea.setTabSize(4);
		textarea.setEditable(false);

		// setBorder(BorderFactory.createEtchedBorder());		
        // JScrollPane areaScrollPane = new JScrollPane(this);
		getViewport().add(textarea);
		
        // From the docs:
        // If you put a text area in a scroll pane, be sure to
     	// set the scroll pane's preferred size or use a text area constructor 
     	// that sets the number of rows and columns for the text area. 
        // areaScrollPane.setPreferredSize(new Dimension(width, height));
       // areaScrollPane.setBorder(new Border
		// add(areaScrollPane);
		

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



