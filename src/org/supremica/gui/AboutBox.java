
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
 * Knarrhogsgatan 10
 * SE-431 60 MOLNDAL
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
package org.supremica.gui;

import org.supremica.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

public class AboutBox
	extends JDialog
	implements ActionListener
{
	private static final long serialVersionUID = 1L;
	JPanel panel1 = new JPanel();
	JPanel panel2 = new JPanel();
	JPanel insetsPanel1 = new JPanel();
	JPanel insetsPanel2 = new JPanel();
	JPanel insetsPanel3 = new JPanel();
	JButton button1 = new JButton();
	JLabel imageControl1 = new JLabel();
	ImageIcon imageIcon;
	JLabel label1 = new JLabel();
	JLabel label2 = new JLabel();
	JLabel label3 = new JLabel();
	JLabel label4 = new JLabel();
	JLabel label5 = new JLabel();
	JLabel label6 = new JLabel();
	JLabel label7 = new JLabel();
	JLabel label8 = new JLabel();
	JLabel label9 = new JLabel();
	JLabel label10 = new JLabel();

	//JLabel label5 = new JLabel();
	BorderLayout borderLayout1 = new BorderLayout();
	BorderLayout borderLayout2 = new BorderLayout();
	FlowLayout flowLayout1 = new FlowLayout();
	FlowLayout flowLayout2 = new FlowLayout();
	GridLayout gridLayout1 = new GridLayout();
	String product = "Supremica";
	String version = Version.version();
	String copyright = "Copyright © 1999-2005";
	String authors1 = "Knut \u00c5kesson, Hugo Flordal, Martin Fabian";
	String authors2 = "Anders Hellgren, Arash Vahidi, Goran Cengic";
	String comments1 = "Northwoods Software - http://www.nwoods.com";
	String comments2 = "AT&T Research - http://www.graphviz.org";

	//String thanks = "";
	public AboutBox(Frame parent)
	{
		super(parent);

		enableEvents(AWTEvent.WINDOW_EVENT_MASK);

		try
		{
			jbInit();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		imageControl1.setIcon(imageIcon);
		pack();
	}

	private void jbInit()
		throws Exception
	{
		imageIcon = new ImageIcon(getClass().getResource("/icons/Supremica.gif"));

		this.setTitle("About");
		setResizable(false);
		panel1.setLayout(borderLayout1);
		panel2.setLayout(borderLayout2);
		insetsPanel1.setLayout(flowLayout1);
		insetsPanel2.setLayout(flowLayout1);
		insetsPanel2.setBorder(new EmptyBorder(10, 10, 10, 10));
		gridLayout1.setRows(10);
		gridLayout1.setColumns(1);

		label1.setText(product);
		label2.setText("Version: " + version);
		label3.setText(copyright);
		label4.setText("");
		label5.setText(authors1);
		label6.setText(authors2);
		label7.setText("");
		label8.setText("Thanks to:");
		label9.setText("  " + comments1);
		label10.setText("  " + comments2);

		insetsPanel3.setLayout(gridLayout1);
		insetsPanel3.setBorder(new EmptyBorder(10, 60, 10, 10));
		button1.setText("Ok");
		button1.addActionListener(this);
		insetsPanel2.add(imageControl1, null);
		panel2.add(insetsPanel2, BorderLayout.WEST);
		this.getContentPane().add(panel1, null);
		insetsPanel3.add(label1, null);
		insetsPanel3.add(label2, null);
		insetsPanel3.add(label3, null);
		insetsPanel3.add(label4, null);
		insetsPanel3.add(label5, null);
		insetsPanel3.add(label6, null);
		insetsPanel3.add(label7, null);
		insetsPanel3.add(label8, null);
		insetsPanel3.add(label9, null);
		insetsPanel3.add(label10, null);
		panel2.add(insetsPanel3, BorderLayout.CENTER);
		insetsPanel1.add(button1, null);
		panel1.add(insetsPanel1, BorderLayout.SOUTH);
		panel1.add(panel2, BorderLayout.NORTH);
	}

	protected void processWindowEvent(WindowEvent e)
	{
		if (e.getID() == WindowEvent.WINDOW_CLOSING)
		{
			cancel();
		}

		super.processWindowEvent(e);
	}

	void cancel()
	{
		dispose();
	}

	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == button1)
		{
			cancel();
		}
	}
}
