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
	extends JPanel
{
	JTextArea textarea = null;
	
	public TextPanel(int width, int height)
	{
		this.textarea = new JTextArea();
		init(width, height);
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
		
        JScrollPane areaScrollPane = new JScrollPane(textarea);
//        areaScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        areaScrollPane.setPreferredSize(new Dimension(width, height));
 /*       areaScrollPane.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createCompoundBorder(
                                BorderFactory.createTitledBorder("Plain Text"),
                                BorderFactory.createEmptyBorder(5,5,5,5)),
                areaScrollPane.getBorder()));*/
		// JViewport port = scroller.getViewport();
		// port.add(textarea);
		add(areaScrollPane);
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

