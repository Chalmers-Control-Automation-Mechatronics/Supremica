/******************* TextWriter.java *********************/
// Simple editor implementation for output, especially
// useful during debug. TextWriter encapsulates this
// panel so all writer functions can write to TextPanel

package org.supremica.gui.texteditor;

import java.io.*;
import java.awt.*;
import java.lang.*;
import java.util.*;
import javax.swing.*;

class TextWriter
	extends Writer
{
	TextPanel panel = null;
	boolean open = true; // instantiating implicitly opens the writer
	
	TextWriter(TextPanel panel)
	{
		this.panel = panel;
	}
	
	public void write(char[] cbuf, int off, int len)
		throws IOException
	{
		String str = new String(cbuf, off, len);
		
		panel.append(str);
	}
	
	public void flush()
		throws IOException
	{
		if(!open)
		{
			throw new IOException("Flushing closed Writer");
		}
	}
	
	public void close()
		throws IOException
	{
		if(open)
		{
			flush();
		}
		open = false;
	}
}
	