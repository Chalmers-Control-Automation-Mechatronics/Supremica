
// ************** RegexpDialog.java *********************//
// * Implements a (simple) dialog for entering regular
// * expressions. Includes help on regexp and selection
// * of regexps from a menu. This is all modeled after
// * Horstmann ex 9-22 (is this good?)
package org.supremica.gui;



import java.awt.*;
import java.awt.event.*;

import javax.swing.*;


// 
public class RegexpDialog
	extends JDialog		/* implements ActionListener */
{

	private JTextField reg_exp;
	private boolean ok = false;

	private void setOk()
	{
		ok = true;
	}

	private void doRepaint()
	{
		repaint();
	}

	private void replaceSelection(String s)
	{
		reg_exp.replaceSelection(s);
	}

	private JButton setDefaultButton(JButton b)
	{

		getRootPane().setDefaultButton(b);

		return b;
	}

	class RegexpMenuItem
		extends JMenuItem
		implements ActionListener
	{

		String pattern;

		public RegexpMenuItem(String s, String p)
		{

			super(s + " - " + p);

			pattern = p;

			addActionListener(this);
		}

		public void actionPerformed(ActionEvent event)
		{
			replaceSelection(pattern);
			doRepaint();
		}
	}

	class RegexpMenuBar
		extends JMenuBar
	{

		public RegexpMenuBar()
		{

			JMenu menu = new JMenu("Expressions");

			menu.add(new RegexpMenuItem("any string", ".*"));
			menu.add(new RegexpMenuItem("any uppercase", "[A-Z]"));
			menu.add(new RegexpMenuItem("any lowercase", "[a-z]"));
			menu.add(new RegexpMenuItem("any alphabetic", "[a-zA-Z]"));
			menu.add(new RegexpMenuItem("any digit", "[0-9]"));
			this.add(menu);

			JMenu help = new JMenu("Help");

			help.add(new JMenuItem("Help Topics"));
			help.add(new JSeparator());
			help.add(new JMenuItem("About..."));
			this.add(help);
		}
	}

	class OkButton
		extends JButton
	{

		public OkButton()
		{

			super("OK");

			setToolTipText("Exit, add regexp");
			addActionListener(new ActionListener()
			{

				public void actionPerformed(ActionEvent e)
				{
					action(e);
				}
			});
		}

		void action(ActionEvent e)
		{
			setOk();
			dispose();
		}
	}

	class CancelButton
		extends JButton
	{

		public CancelButton()
		{

			super("Cancel");

			setToolTipText("Exit, do not add regexp");
			addActionListener(new ActionListener()
			{

				public void actionPerformed(ActionEvent e)
				{
					action(e);
				}
			});
		}

		void action(ActionEvent e)
		{
			dispose();
		}
	}

	// * Pop up this dialog with the textfield set to str
	public RegexpDialog(JFrame parent, String str)
	{

		super(parent, "Enter regular expression", true);	// modal

		JPanel p1 = new JPanel();

		p1.add(new JLabel("Regexp:"));
		p1.add(reg_exp = new JTextField(str, 30));

		JPanel p2 = new JPanel();

		p2.add(setDefaultButton(new OkButton()));
		p2.add(new CancelButton());

		Container content_pane = getContentPane();

		content_pane.add("Center", p1);
		content_pane.add("South", p2);
		setJMenuBar(new RegexpMenuBar());

		// System.err.println(getMinimumSize()); // java.awt.Dimension[width=137,height=68]
		pack();

		// System.err.println(getMinimumSize()); // java.awt.Dimension[width=145,height=95]
		Dimension dim = getMinimumSize();

		setLocation(Utility.getPosForCenter(dim));
		setResizable(false);
		reg_exp.selectAll();	// set the whole string as selected
		show();

		if (ok)
		{
			str = reg_exp.getText();
		}
	}

	public boolean isOk()
	{
		return ok;
	}

	public String getText()
	{
		return reg_exp.getText();
	}
}
