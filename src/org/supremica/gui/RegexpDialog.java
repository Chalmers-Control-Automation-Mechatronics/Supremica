//************** RegexpDialog.java *********************//
//* Implements a (simple) dialog for entering regular
//* expressions. Includes help on regexp and selection
//* of regexps from a menu. This is all modeled after
//* Horstmann ex 9-22 (is this good?)

package org.supremica.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class RegexpDialog extends JDialog /* implements ActionListener */
{
	private JTextField reg_exp;
	private boolean ok = false;

	private void setOk() { ok = true; }
	private JRootPane getOurRootPane() { return getRootPane(); }
	
	class OkButton extends JButton
	{
		public OkButton()
		{
			super("Ok");
			setToolTipText("Exit, add regexp");
			getOurRootPane().setDefaultButton(this);
			addActionListener(
				new ActionListener()
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
	
	class CancelButton extends JButton
	{
		public CancelButton()
		{
			super("Cancel");
			setToolTipText("Exit, do not add regexp");
			addActionListener(
				new ActionListener()
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
	
	//* Pop up this dialog with the textfield set to str
	public RegexpDialog(JFrame parent, String str)
	{
		super(parent, "Enter regular expression", true); // modal
		
		JPanel p1 = new JPanel();
		p1.add(new JLabel("Regexp:"));
		p1.add(reg_exp = new JTextField(str, 30));
		
		JPanel p2 = new JPanel();
		p2.add(new OkButton());
		p2.add(new CancelButton());
		
		Container content_pane = getContentPane();
		content_pane.add("Center", p1);
		content_pane.add("South", p2);

//		System.err.println(getMinimumSize()); // java.awt.Dimension[width=137,height=68]
		pack();
//		System.err.println(getMinimumSize()); // java.awt.Dimension[width=145,height=95]
		Dimension dim = getMinimumSize();
		setLocation(Utility.getPosForCenter(dim));
		setResizable(false);
		reg_exp.selectAll();
		show();
		
		if(ok)
			str = reg_exp.getText();
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

/*
public class RegexpDialog extends JFrame implements ActionListener
{
	private theDialog = null;
	
	public RegexpDialog(String str)
	{
		Utility.setupFrame(this, 300, 300);
		setTitle("Enter regular expression");
		
		/*
		JMenuBar mbar = new JMenuBar();
		setMenuBar(mbar);
		JMenu fileMenu = new JMenu("File");
		mbar.add(fileMenu);	
		*//*
	}
	
	public void actionPerformed(ActionEvent ev)
	{
		Object source = ev.getSource();
		
	}
}*/