
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
package org.supremica.gui.editor;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

public class NodeDialog
	extends JDialog
{
	JPanel panel1 = new JPanel();
	JLabel label1 = new JLabel();
	JLabel label2 = new JLabel();
	JTextField ports = new JTextField();
	JTextField label = new JTextField();
	ButtonGroup group1 = new ButtonGroup();
	JRadioButton horizontal = new JRadioButton();
	JRadioButton vertical = new JRadioButton();
	JButton OKButton = new JButton();
	JButton CancelButton = new JButton();

	public NodeDialog(Frame frame, String title, boolean modal, AutomatonView view)
	{
		super(frame, title, modal);

		try
		{
			myView = view;

			jbInit();
			pack();
			UpdateDialog();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	public NodeDialog()
	{
		this(null, "", false, null);
	}

	void jbInit()
		throws Exception
	{
		panel1.setLayout(null);
		panel1.setMinimumSize(new Dimension(250, 100));
		panel1.setPreferredSize(new Dimension(250, 100));
		OKButton.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				OKButton_actionPerformed(e);
			}
		});
		CancelButton.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				CancelButton_actionPerformed(e);
			}
		});
		OKButton.addKeyListener(new java.awt.event.KeyAdapter()
		{
			public void keyPressed(KeyEvent e)
			{
				OKButton_keyPressed(e);
			}
		});
		CancelButton.addKeyListener(new java.awt.event.KeyAdapter()
		{
			public void keyPressed(KeyEvent e)
			{
				CancelButton_keyPressed(e);
			}
		});
		getContentPane().add(panel1);
		OKButton.setText("OK");
		OKButton.setFont(new Font("Dialog", Font.PLAIN, 12));
		OKButton.setBounds(new Rectangle(165, 10, 80, 22));
		panel1.add(OKButton);
		CancelButton.setText("Cancel");
		CancelButton.setFont(new Font("Dialog", Font.PLAIN, 12));
		CancelButton.setBounds(new Rectangle(166, 40, 80, 22));
		panel1.add(CancelButton);
		label1.setText("Ports");
		label1.setBounds(new Rectangle(10, 10, 50, 20));
		panel1.add(label1);
		label2.setText("Label");
		label2.setBounds(new Rectangle(10, 35, 50, 20));
		panel1.add(label2);
		ports.setText("1");
		ports.setBounds(new Rectangle(60, 10, 80, 20));
		panel1.add(ports);
		label.setBounds(new Rectangle(60, 35, 80, 20));
		panel1.add(label);
		group1.add(horizontal);
		group1.add(vertical);
		horizontal.setText("Horizontal");
		horizontal.setSelected(true);
		horizontal.setBounds(new Rectangle(10, 60, 80, 20));
		panel1.add(horizontal);
		vertical.setText("Vertical");
		vertical.setBounds(new Rectangle(10, 80, 80, 20));
		panel1.add(vertical);
	}

	void UpdateDialog() {}

	void UpdateControl()
	{

		/*
		 *   if(myView != null){
		 *     String s = label.getText();
		 *     int numPorts = Integer.parseInt(ports.getText());
		 *     if(numPorts <= 0)
		 *       numPorts = 1;
		 *     else if(numPorts > 100)
		 *       numPorts = 100;
		 *     if (s.length() > 0)
		 *       myView.insertNode(null, s, numPorts, horizontal.isSelected());
		 *     else
		 *       myView.insertNode(null, null, numPorts, horizontal.isSelected());
		 *   }
		 */
	}

	public void addNotify()
	{

		// Record the size of the window prior to calling parents addNotify.
		Dimension d = getSize();

		super.addNotify();

		if (fComponentsAdjusted)
		{
			return;
		}

		// Adjust components according to the insets
		Insets insets = getInsets();

		setSize(insets.left + insets.right + d.width, insets.top + insets.bottom + d.height);

		Component components[] = getComponents();

		for (int i = 0; i < components.length; i++)
		{
			Point p = components[i].getLocation();

			p.translate(insets.left, insets.top);
			components[i].setLocation(p);
		}

		fComponentsAdjusted = true;
	}

	// Used for addNotify check.
	boolean fComponentsAdjusted = false;

	/**
	 * Shows or hides the component depending on the boolean flag b.
	 * @param b  if true, show the component; otherwise, hide the component.
	 * @see javax.swing.JComponent#isVisible
	 */
	public void setVisible(boolean b)
	{
		if (b)
		{
			Rectangle bounds = getParent().getBounds();
			Rectangle abounds = getBounds();

			setLocation(bounds.x + Math.abs((bounds.width - abounds.width) / 2), bounds.y + Math.abs((bounds.height - abounds.height) / 2));
		}

		super.setVisible(b);
	}

	void OKButton_actionPerformed(ActionEvent e)
	{
		OnOK();
	}

	void OnOK()
	{
		try
		{
			UpdateControl();
			this.dispose();    // Free system resources
		}
		catch (Exception e) {}
	}

	void CancelButton_actionPerformed(ActionEvent e)
	{
		OnCancel();
	}

	void OnCancel()
	{
		try
		{
			this.dispose();    // Free system resources
		}
		catch (Exception e) {}
	}

	void OKButton_keyPressed(KeyEvent e)
	{
		if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER)
		{
			OnOK();
		}
		else if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ESCAPE)
		{
			OnCancel();
		}
	}

	void CancelButton_keyPressed(KeyEvent e)
	{
		if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER)
		{
			OnCancel();
		}
		else if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ESCAPE)
		{
			OnCancel();
		}
	}

	AutomatonView myView = null;
}
