
/*
 * Supremica Software License Agreement
 *
 * The Supremica software is not in the public domain
 * However, it is freely available without fee for education,
 * research, and non-profit purposes.  By obtaining copies of
 * this and other files that comprise the Supremica software,
 * you, the Licensee, agree to abide by the following
 * conditions and understandings with respect to the
 * copyrighted software:
 *
 * The software is copyrighted in the name of Supremica,
 * and ownership of the software remains with Supremica.
 *
 * Permission to use, copy, and modify this software and its
 * documentation for education, research, and non-profit
 * purposes is hereby granted to Licensee, provided that the
 * copyright notice, the original author's names and unit
 * identification, and this permission notice appear on all
 * such copies, and that no charge be made for such copies.
 * Any entity desiring permission to incorporate this software
 * into commercial products or to use it for commercial
 * purposes should contact:
 *
 * Knut Akesson (KA), knut@supremica.org
 * Supremica,
 * Haradsgatan 26A
 * 431 42 Molndal
 * SWEDEN
 *
 * to discuss license terms. No cost evaluation licenses are
 * available.
 *
 * Licensee may not use the name, logo, or any other symbol
 * of Supremica nor the names of any of its employees nor
 * any adaptation thereof in advertising or publicity
 * pertaining to the software without specific prior written
 * approval of the Supremica.
 *
 * SUPREMICA AND KA MAKES NO REPRESENTATIONS ABOUT THE
 * SUITABILITY OF THE SOFTWARE FOR ANY PURPOSE.
 * IT IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED WARRANTY.
 *
 * Supremica or KA shall not be liable for any damages
 * suffered by Licensee from the use of this software.
 *
 * Supremica is owned and represented by KA.
 */
package org.supremica.log;

import java.awt.Color;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.*;
import java.io.*;
import java.net.URL;
import java.util.*;

import javax.swing.*;

import javax.swing.text.*;

import org.apache.log4j.*;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.helpers.Loader;
import org.apache.log4j.helpers.QuietWriter;
import org.apache.log4j.helpers.OptionConverter;
import org.supremica.gui.*;

public class LogDisplay
	extends AppenderSkeleton
{
	private static final InterfaceManager theInterfaceManager = InterfaceManager.getInstance();
	private static LogDisplay theLogDisplay = null;
	private JScrollPane theTextPaneScrollPane;
	private JTextPane textpane;
	private StyledDocument doc;
	private LoggerPopupMenu popup = new LoggerPopupMenu();

	// private TracerPrintWriter tp;
	private StringWriter sw;
	private QuietWriter qw;
	private Hashtable attributes;
	private Hashtable icons;
	private String label;
	private boolean fancy;
	final String LABEL_OPTION = "Label";
	final String COLOR_OPTION_FATAL = "Color.Emerg";
	final String COLOR_OPTION_ERROR = "Color.Error";
	final String COLOR_OPTION_WARN = "Color.Warn";
	final String COLOR_OPTION_INFO = "Color.Info";
	final String COLOR_OPTION_DEBUG = "Color.Debug";
	final String COLOR_OPTION_BACKGROUND = "Color.Background";
	final String FANCY_OPTION = "Fancy";
	final String FONT_NAME_OPTION = "Font.Name";
	final String FONT_SIZE_OPTION = "Font.Size";

	private LogDisplay()
	{
		super();

		layout = new PatternLayout("%-5p %m%n");
		name = "Debug";

		setTextPane(new JTextPane());

		theTextPaneScrollPane = new JScrollPane(textpane);

		createAttributes();
		createIcons();

		this.label = "";
		this.sw = new StringWriter();
		this.qw = new QuietWriter(sw, errorHandler);

		// this.tp = new TracerPrintWriter(qw);
		this.fancy = true;


		// This code used to be in the popup menu -------------
		textpane.addMouseListener(new MouseAdapter()
		{
			public void mousePressed(MouseEvent e)
			{

				// This is needed for the Linux platform
				// where isPopupTrigger is true only on mousePressed.
				maybeShowPopup(e);
			}

			public void mouseReleased(MouseEvent e)
			{

				// This is for triggering the popup on Windows platforms
				maybeShowPopup(e);
			}

			private void maybeShowPopup(MouseEvent e)
			{
				if (e.isPopupTrigger())
				{
					popup.show(textpane, e.getX(), e.getY());
				}
			}
		});

		// --------------------------------------//
	}

	public synchronized static LogDisplay getInstance()
	{
		if (theLogDisplay == null)
		{
			theLogDisplay = new LogDisplay();
		}

		return theLogDisplay;
	}


	private void createAttributes()
	{
		Priority prio[] = Priority.getAllPossiblePriorities();

		attributes = new Hashtable();

		for (int i = 0; i < prio.length; i++)
		{
			MutableAttributeSet att = new SimpleAttributeSet();

			attributes.put(prio[i], att);
			StyleConstants.setFontSize(att, 14);
		}

		StyleConstants.setForeground((MutableAttributeSet) attributes.get(Priority.ERROR), Color.red);
		StyleConstants.setForeground((MutableAttributeSet) attributes.get(Priority.WARN), Color.red);
		StyleConstants.setForeground((MutableAttributeSet) attributes.get(Priority.INFO), new Color(0, 80, 0));
		StyleConstants.setForeground((MutableAttributeSet) attributes.get(Priority.DEBUG), Color.blue);
	}

	public void close() {}

	public void clear()
	{
		textpane.setText("");
	}


	public void cut()
	{
		textpane.cut();
	}

	public void copy()
	{
		textpane.copy();
	}

	private void createIcons()
	{
		Priority prio[] = Priority.getAllPossiblePriorities();

		icons = new Hashtable();

		for (int i = 0; i < prio.length; i++)
		{
			if (prio[i].equals(Priority.FATAL))
			{
				icons.put(prio[i], new ImageIcon(Supremica.class.getResource("/icons/RedFlag.gif")));
			}

			if (prio[i].equals(Priority.ERROR))
			{
				icons.put(prio[i], new ImageIcon(Supremica.class.getResource("/icons/RedFlag.gif")));
			}

			if (prio[i].equals(Priority.WARN))
			{
				icons.put(prio[i], new ImageIcon(Supremica.class.getResource("/icons/RedFlag.gif")));
			}

			if (prio[i].equals(Priority.INFO))
			{
				icons.put(prio[i], new ImageIcon(Supremica.class.getResource("/icons/GreenFlag.gif")));
			}

			if (prio[i].equals(Priority.DEBUG))
			{
				icons.put(prio[i], new ImageIcon(Supremica.class.getResource("/icons/BlueFlag.gif")));
			}
		}
	}

	public void append(LoggingEvent event)
	{
		String text = this.layout.format(event);
		String trace = "";

		// Print Stacktrace
		// Quick Hack maybe there is a better/faster way?

		/*
		 * This is not compiling anylonger - fix this  /Knut
		 *       if (event.throwableInfo != null)
		 *       {
		 *               //event.throwable.printStackTrace(tp);
		 *               for (int i=0; i< sw.getBuffer().length(); i++)
		 *               {
		 *                       if (sw.getBuffer().charAt(i)=='\t')
		 *                       {
		 *                               sw.getBuffer().replace(i,i+1,"        ");
		 *                       }
		 *               }
		 *               trace = sw.toString();
		 *               sw.getBuffer().delete(0,sw.getBuffer().length());
		 *       }
		 */
		textpane.setCaretPosition(doc.getLength());

		try
		{
			if (fancy)
			{
				textpane.setEditable(true);
				textpane.insertIcon((ImageIcon) icons.get(event.priority));
				textpane.setEditable(false);
			}

			doc.insertString(doc.getLength(), text + trace, (MutableAttributeSet) attributes.get(event.priority));
		}
		catch (BadLocationException badex)
		{
			System.err.println(badex);
		}
	}

	public JComponent getComponent()
	{
		return theTextPaneScrollPane;
	}

	public String getLabel()
	{
		return label;
	}

	public String[] getOptionStrings()
	{
		return new String[]{ LABEL_OPTION, COLOR_OPTION_FATAL,
							 COLOR_OPTION_ERROR, COLOR_OPTION_WARN,
							 COLOR_OPTION_INFO, COLOR_OPTION_DEBUG,
							 COLOR_OPTION_BACKGROUND, FANCY_OPTION,
							 FONT_NAME_OPTION, FONT_SIZE_OPTION };
	}

	private Color parseColor(String v)
	{
		StringTokenizer st = new StringTokenizer(v, ",");
		int val[] = { 255, 255, 255, 255 };
		int i = 0;

		while (st.hasMoreTokens())
		{
			val[i] = Integer.parseInt(st.nextToken());

			i++;
		}

		return new Color(val[0], val[1], val[2], val[3]);
	}

	public void setLayout(Layout layout)
	{
		this.layout = layout;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	private void setTextPane(JTextPane textpane)
	{
		this.textpane = textpane;

		textpane.setEditable(false);
		textpane.setBackground(Color.white);

		this.doc = textpane.getStyledDocument();
	}

	private void setColor(Priority p, String v)
	{
		StyleConstants.setForeground((MutableAttributeSet) attributes.get(p), parseColor(v));
	}

	private void setFontSize(int size)
	{
		Enumeration e = attributes.elements();

		while (e.hasMoreElements())
		{
			StyleConstants.setFontSize((MutableAttributeSet) e.nextElement(), size);
		}

		return;
	}

	private void setFontName(String name)
	{
		Enumeration e = attributes.elements();

		while (e.hasMoreElements())
		{
			StyleConstants.setFontFamily((MutableAttributeSet) e.nextElement(), name);
		}

		return;
	}

	public void setOption(String option, String value)
	{
		if (option.equalsIgnoreCase(LABEL_OPTION))
		{
			this.label = value;
		}

		if (option.equalsIgnoreCase(COLOR_OPTION_FATAL))
		{
			setColor(Priority.FATAL, value);
		}

		if (option.equalsIgnoreCase(COLOR_OPTION_ERROR))
		{
			setColor(Priority.ERROR, value);
		}

		if (option.equalsIgnoreCase(COLOR_OPTION_WARN))
		{
			setColor(Priority.WARN, value);
		}

		if (option.equalsIgnoreCase(COLOR_OPTION_INFO))
		{
			setColor(Priority.INFO, value);
		}

		if (option.equalsIgnoreCase(COLOR_OPTION_DEBUG))
		{
			setColor(Priority.DEBUG, value);
		}

		if (option.equalsIgnoreCase(COLOR_OPTION_BACKGROUND))
		{
			textpane.setBackground(parseColor(value));
		}

		if (option.equalsIgnoreCase(FANCY_OPTION))
		{
			fancy = OptionConverter.toBoolean(value, fancy);
		}

		if (option.equalsIgnoreCase(FONT_SIZE_OPTION))
		{
			setFontSize(Integer.parseInt(value));
		}

		if (option.equalsIgnoreCase(FONT_NAME_OPTION))
		{
			setFontName(value);
		}

		return;
	}

	public boolean requiresLayout()
	{
		return true;
	}



	class LoggerPopupMenu
		extends JPopupMenu
	{
		// except for access, these are copied straight from gui.Supremica
		private void initPopups()
		{
			JMenuItem cutItem = new JMenuItem("Cut");
			cutItem.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					clear();
				}
			});
			add(cutItem);

			JMenuItem copyItem = new JMenuItem("Copy");
			copyItem.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					copy();
				}
			});
			add(copyItem);

			addSeparator();

			JMenuItem clearItem = new JMenuItem("Clear");
			clearItem.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					clear();
				}
			});
			add(clearItem);

		}

		public LoggerPopupMenu()
		{
			initPopups();
		}
	}
}
